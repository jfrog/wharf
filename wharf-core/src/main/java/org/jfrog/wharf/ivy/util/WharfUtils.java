/*
 *
 *  Copyright (C) 2010 JFrog Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.jfrog.wharf.ivy.util;

import org.apache.ivy.core.cache.CacheMetadataOptions;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.ChecksumHelper;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.handler.WharfUrlHandler;
import org.jfrog.wharf.ivy.repository.WharfArtifactResourceResolver;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.resolver.WharfResolver;
import org.jfrog.wharf.ivy.resolver.WharfResourceDownloader;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;

import java.io.*;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Tomer Cohen
 */
public class WharfUtils {
    private final static String VERSION = "version";
    private final static String CORE_PROPERTY_FILE = "/org/jfrog/wharf/wharf-core.properties";

    public static final String SHA1_ALGORITHM = "sha1";
    public static final String MD5_ALGORITHM = "md5";

    public static String getChecksumAlgorithm() {
        return SHA1_ALGORITHM;
    }

    public static String getCoreVersion() {
        try {
            Properties props = new Properties();
            props.load(WharfUtils.class.getResourceAsStream(CORE_PROPERTY_FILE));
            return props.getProperty(VERSION);
        } catch (IOException e) {
            return "Error reading " + CORE_PROPERTY_FILE + ": " + e.getMessage();
        }
    }

    public static void hackIvyBasicResolver(WharfResolver wharfResolver) {
        try {
            // Override the URLRepository
            wharfResolver.setRepository(new WharfURLRepository());
            // TODO: The following reflection can be removed once Ivy uses a getDownloader and getArtifactResourceResolver methods
            Field downloaderField = BasicResolver.class.getDeclaredField("downloader");
            downloaderField.setAccessible(true);
            downloaderField.set(wharfResolver, new WharfResourceDownloader(wharfResolver));
            Field artifactResourceResolverField = BasicResolver.class.getDeclaredField("artifactResourceResolver");
            artifactResourceResolverField.setAccessible(true);
            artifactResourceResolverField.set(wharfResolver, new WharfArtifactResourceResolver(wharfResolver));
        } catch (Exception e) {
            throw new RuntimeException("Could not hack Ivy :(", e);
        }
    }

    public static ResolvedResource convertToWharfResource(ResolvedResource resolvedResource) {
        if (resolvedResource == null) {
            return null;
        }
        Resource resource = resolvedResource.getResource();
        if (resource == null) {
            return resolvedResource;
        }
        if (resource instanceof WharfUrlResource) {
            return resolvedResource;
        }
        return new ResolvedResource(new WharfUrlResource(resource), resolvedResource.getRevision());
    }

    public static WharfUrlHandler getWharfUrlHandler() {
        // Enforce WharfUrlHandler TODO: Remove ugly static in Ivy
        URLHandler urlHandler = URLHandlerRegistry.getDefault();
        if (!(urlHandler instanceof WharfUrlHandler)) {
            urlHandler = new WharfUrlHandler();
            URLHandlerRegistry.setDefault(urlHandler);
        }
        return (WharfUrlHandler) urlHandler;
    }

    public static ResolvedModuleRevision findModuleInCache(WharfResolver wharfResolver, DependencyDescriptor dd, ResolveData data) {
        WharfCacheManager cacheManager = (WharfCacheManager) wharfResolver.getRepositoryCacheManager();
        // If check modified is true, make sure to clean the resource cache
        CacheMetadataOptions cacheOptions = wharfResolver.getCacheOptions(data);
        if (cacheOptions.isCheckmodified() != null && cacheOptions.isCheckmodified()) {
            Message.verbose("don't use cache for " + dd + ": checkModified=true");
            // TODO: Check if we can Remove this global flag
            WharfURLRepository.setAlwaysCheck(true);
            return null;
        }
        if (cacheManager.isChanging(dd, dd.getDependencyRevisionId(), cacheOptions)) {
            Message.verbose("don't use cache for " + dd + ": changing=true");
            // TODO: Check if we can Remove this global flag
            WharfURLRepository.setAlwaysCheck(true);
            return null;
        }
        return wharfResolver.basicFindModuleInCache(dd, data, false);
    }

    private enum OperatingSystem {
        OLD_WINDOWS {
            @Override
            void linkCacheFileToStorage(File storageFile, File cacheFile) throws IOException {
                FileUtil.copy(storageFile, cacheFile, new WharfCopyListener(), true);
            }
        },
        NEW_WINDOWS {
            @Override
            void linkCacheFileToStorage(File storageFile, File cacheFile) throws IOException {
                WindowsUtils.windowsSymlink(storageFile, cacheFile, new WharfCopyListener(), true);
            }
        },
        OS_X, OTHER;

        void linkCacheFileToStorage(File storageFile, File cacheFile) throws IOException {
            FileUtil.symlink(storageFile, cacheFile, new WharfCopyListener(), true);
        }
    }

    private static final OperatingSystem OS;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            if (osName.contains("vista") || osName.contains("7")) {
                OS = OperatingSystem.NEW_WINDOWS;
            } else {
                OS = OperatingSystem.OLD_WINDOWS;
            }
        } else if (osName.contains("mac os x")) {
            OS = OperatingSystem.OS_X;
        } else {
            OS = OperatingSystem.OTHER;
        }
    }

    public static void linkCacheFileToStorage(File storageFile, File cacheFile) throws IOException {
        OS.linkCacheFileToStorage(storageFile, cacheFile);
    }


    public static String getCleanChecksum(File checksumFile) throws IOException {
        if (!checksumFile.canRead()) {
            return null;
        }
        String csFileContent = FileUtil.readEntirely(
                new BufferedReader(new FileReader(checksumFile))).trim().toLowerCase(Locale.US);
        return getCleanChecksum(csFileContent);
    }

    public static String getCleanChecksum(String checksum) {
        if (checksum == null) {
            return null;
        }
        checksum = checksum.trim();
        String cleanChecksum;
        if (checksum.indexOf(' ') > -1
                && (checksum.startsWith("md") || checksum.startsWith("sha"))) {
            int lastSpaceIndex = checksum.lastIndexOf(' ');
            cleanChecksum = checksum.substring(lastSpaceIndex + 1);
        } else {
            int spaceIndex = checksum.indexOf(' ');
            if (spaceIndex != -1) {
                cleanChecksum = checksum.substring(0, spaceIndex);
                // IVY-1155: support some strange formats like this one:
                // http://repo2.maven.org/maven2/org/apache/pdfbox/fontbox/0.8.0-incubator/fontbox-0.8.0-incubator.jar.md5
                if (cleanChecksum.endsWith(":")) {
                    StringBuffer result = new StringBuffer();
                    char[] chars = checksum.substring(spaceIndex + 1).toCharArray();
                    for (char aChar : chars) {
                        if (!Character.isWhitespace(aChar)) {
                            result.append(aChar);
                        }
                    }
                    cleanChecksum = result.toString();
                }
            } else {
                cleanChecksum = checksum;
            }
        }
        return cleanChecksum;
    }

    public static String computeUUID(String content) {
        String algorithm = MD5_ALGORITHM;
        if (!ChecksumHelper.isKnownAlgorithm(algorithm)) {
            throw new IllegalArgumentException("unknown algorithm " + algorithm);
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();
            byte[] bytes = content.trim().toLowerCase(Locale.US).getBytes("UTF-8");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return ChecksumHelper.byteArrayToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            // Impossible
            throw new IllegalArgumentException("unknown algorithm " + algorithm, e);
        } catch (UnsupportedEncodingException e) {
            // Impossible except with IBM :)
            throw new IllegalArgumentException("unknown charset UTF-8", e);
        }
    }

    public static long getAndCheck(WharfResolver wharfResolver, Resource resource, File dest) throws IOException {
        if (!(resource instanceof WharfUrlResource)) {
            throw new IllegalArgumentException("The Wharf Resolver manage only WharfUrlResource");
        }
        WharfUrlResource wharfResource = (WharfUrlResource) resource;
        WharfCacheManager cacheManager = (WharfCacheManager) wharfResolver.getRepositoryCacheManager();
        // First get the checksum for this resource
        String checksumValue = wharfResource.getSha1();
        File tempStorageFile = cacheManager.getTempStorageFile();
        if (!tempStorageFile.getParentFile().exists()) {
            tempStorageFile.getParentFile().mkdirs();
        }

        try {
            WharfURLRepository wharfUrlRepository = wharfResolver.getWharfUrlRepository();
            if (checksumValue == null && wharfResolver.supportsWrongSha1()) {
                wharfUrlRepository.get(wharfResource, tempStorageFile);
                // Check with the actual sha1 now
                checksumValue = wharfResource.getActual().get(ChecksumType.sha1);
            }
            if (checksumValue == null) {
                throw new IOException(
                        "Checksum " + ChecksumType.sha1.alg() + " not found for " + resource.getName());
            }
            File storageFile = cacheManager.getStorageFile(checksumValue);
            if (!storageFile.exists()) {
                // Not in storage cache => download to temp if needed
                if (!tempStorageFile.exists()) {
                    wharfUrlRepository.get(wharfResource, tempStorageFile);
                }
                wharfUrlRepository.checkChecksums(wharfResource);
                if (!storageFile.getParentFile().exists()) {
                    storageFile.getParentFile().mkdirs();
                }
                tempStorageFile.renameTo(storageFile);
            }
            // If we get here, then the file was found in cache with the good checksum!
            // Just need to link the storage to file to the final cache destination.
            WharfUtils.linkCacheFileToStorage(storageFile, dest);
            if (!storageFile.setLastModified(resource.getLastModified())) {
                throw new IOException("Could not change the timestamp of " + storageFile.getAbsolutePath());
            }
            return dest.length();
        } finally {
            if (tempStorageFile.exists()) {
                FileUtil.forceDelete(tempStorageFile);
            }
        }
    }

    public static boolean isEmptyString(String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }
}
