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

import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.util.*;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.resolver.WharfResolver;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;

import java.io.*;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author Tomer Cohen
 */
public class WharfUtils {

    public static final String SHA1_ALGORITHM = "sha1";
    public static final String MD5_ALGORITHM = "md5";

    public static String getChecksumAlgorithm() {
        return SHA1_ALGORITHM;
    }

    public static void hackIvyBasicResolver(WharfResolver wharfResolver) {
        try {
            // TODO: The following reflection can be removed once Ivy uses a getDownloader and getArtifactResourceResolver methods
            Field downloaderField = BasicResolver.class.getDeclaredField("downloader");
            downloaderField.setAccessible(true);
            downloaderField.set(wharfResolver, wharfResolver.getDownloader());
            Field artifactResourceResolverField = BasicResolver.class.getDeclaredField("artifactResourceResolver");
            artifactResourceResolverField.setAccessible(true);
            artifactResourceResolverField.set(wharfResolver, wharfResolver.getArtifactResourceResolver());
        } catch (Exception e) {
            throw new RuntimeException("Could not hack Ivy :(", e);
        }
    }

    private enum OperatingSystem {
        OLD_WINDOWS {
            @Override
            void copyCacheFile(File src, File dest) throws IOException {
                FileUtil.copy(src, dest, new WharfCopyListener(), true);
            }
        },
        NEW_WINDOWS {
            @Override
            void copyCacheFile(File src, File dest) throws IOException {
                WindowsUtils.windowsSymlink(src, dest, new WharfCopyListener(), true);
            }
        },
        OS_X, OTHER;

        void copyCacheFile(File src, File dest) throws IOException {
            FileUtil.symlink(src, dest, new WharfCopyListener(), true);
        }
    }

    private static final OperatingSystem OS;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            if ((!osName.contains("vista") || !osName.contains("7"))) {
                OS = OperatingSystem.OLD_WINDOWS;
            } else {
                OS = OperatingSystem.NEW_WINDOWS;
            }
        } else if (osName.contains("mac os x")) {
            OS = OperatingSystem.OS_X;
        } else {
            OS = OperatingSystem.OTHER;
        }
    }

    public static void copyCacheFile(File src, File dest) throws IOException {
        OS.copyCacheFile(src, dest);
    }

    private static class WharfCopyListener implements CopyProgressListener {

        @Override
        public void start(CopyProgressEvent evt) {
            Message.info(evt.toString());
        }

        @Override
        public void progress(CopyProgressEvent evt) {
        }

        @Override
        public void end(CopyProgressEvent evt) {
        }
    }

    public static String getCleanChecksum(File checksumFile) throws IOException {
        String csFileContent = FileUtil.readEntirely(
                new BufferedReader(new FileReader(checksumFile))).trim().toLowerCase(Locale.US);
        return getCleanChecksum(csFileContent);
    }

    public static String getCleanChecksum(String checksum) {
        if (checksum == null) {
            return null;
        }
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
        // First get the checksum for this resource
        String checksumValue = wharfResource.getSha1();
        if (checksumValue == null) {
            // If no checksum found in HEAD request, download the actual .sha1 resource
            // In non Artifactory server will do 2 queries HEAD + GET
            Resource csRes = resource.clone(resource.getName() + "." + WharfUtils.getChecksumAlgorithm());
            if (csRes.exists()) {
                File tempChecksum = File.createTempFile("temp", "." + WharfUtils.getChecksumAlgorithm());
                wharfResolver.get(csRes, tempChecksum);
                try {
                    checksumValue = WharfUtils.getCleanChecksum(tempChecksum);
                } finally {
                    FileUtil.forceDelete(tempChecksum);
                }
            } else {
                // The Wharf system enforce the presence of checksums on the remote repo
                throw new IOException(
                        "invalid " + WharfUtils.getChecksumAlgorithm() + " checksum file " + csRes.getName() +
                                " not found!");
            }
        }
        if (checksumValue == null) {
            // The Wharf system enforce the presence of checksums on the remote repo
            throw new IOException(
                    "invalid " + WharfUtils.getChecksumAlgorithm() + " checksum not found for " + resource.getName());
        }
        WharfCacheManager cacheManager = (WharfCacheManager) wharfResolver.getRepositoryCacheManager();
        File storageFile = cacheManager.getStorageFile(checksumValue);
        if (!storageFile.exists()) {
            // Not in storage cache
            if (!storageFile.getParentFile().exists()) {
                storageFile.getParentFile().mkdirs();
            }
            wharfResolver.get(resource, storageFile);
            String downloadChecksum =
                    ChecksumHelper.computeAsString(storageFile, WharfUtils.getChecksumAlgorithm()).trim()
                            .toLowerCase(Locale.US);
            if (!checksumValue.equals(downloadChecksum)) {
                FileUtil.forceDelete(storageFile);
                throw new IOException(
                        "invalid " + WharfUtils.getChecksumAlgorithm() + ": expected=" + checksumValue + " computed="
                                + downloadChecksum);
            }
        }
        // If we get here, then the file was found in cache with the good checksum! just need to copy it
        // to the destination.
        WharfUtils.copyCacheFile(storageFile, dest);
        return dest.length();
    }
}
