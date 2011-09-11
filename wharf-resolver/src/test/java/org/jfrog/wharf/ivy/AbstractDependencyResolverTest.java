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
package org.jfrog.wharf.ivy;

import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.*;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.sort.SortEngine;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.util.FileUtil;
import org.apache.tools.ant.util.FileUtils;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.resolver.FileSystemWharfResolver;
import org.jfrog.wharf.ivy.resolver.IBiblioWharfResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbstractDependencyResolverTest {

    public static final String SRC_TEST_REPOSITORIES = "src/test/repositories";
    protected static final String FS = System.getProperty("file.separator");
    protected static final String DEFAULT_IVY_PATTERN = "[organisation]/[module]/ivys/ivy-[revision].xml";
    protected static final String REL_IVY_PATTERN = "1" + FS + DEFAULT_IVY_PATTERN;
    protected static final String DEFAULT_ARTIFACT_PATTERN = "[organisation]/[module]/[type]s/[artifact]-[revision].[type]";

    protected IvySettingsTestHolder defaultSettings;

    protected IBiblioWharfResolver createIBiblioResolver(String name, String root) {
        IBiblioWharfResolver resolver = new IBiblioWharfResolver();
        resolver.setName(name);
        resolver.setRoot(root);
        resolver.setM2compatible(true);
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        defaultSettings.settings.setDefaultResolver(name);
        return resolver;
    }

    protected void downloadNoDescriptor(ModuleRevisionId mrid, DependencyResolver resolver, int nbDownload) throws ParseException {
        DownloadReport dr = resolver.download(new Artifact[] {new DefaultArtifact(mrid, null, mrid.getModuleId().getName(), "jar", "jar")}, getDownloadOptions());
        assertEquals(nbDownload, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);
    }

    protected void downloadSources(ModuleRevisionId mrid, DependencyResolver resolver, int nbDownload) throws ParseException {
        HashMap attributes = new HashMap();
        attributes.put("m:classifier", "sources");
        DefaultArtifact defaultArtifact = new DefaultArtifact(mrid, null, mrid.getModuleId().getName(), "jar", "jar", attributes);
        DownloadReport dr = resolver.download(new Artifact[] {defaultArtifact}, getDownloadOptions());
        assertEquals(nbDownload, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);
    }

    protected void downloadAndCheck(ModuleRevisionId mrid, DependencyResolver resolver, int min, int max) throws ParseException {
        ResolvedModuleRevision rmr;
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        DownloadReport dr = resolver.download(rmr.getDescriptor().getAllArtifacts(), getDownloadOptions());
        int nbDownload = dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length;
        assertTrue("The number of downloads "+nbDownload+" should be at least "+min, nbDownload >= min);
        assertTrue("The number of downloads "+nbDownload+" should be at most "+max, nbDownload <= max);
    }

    protected void downloadAndCheck(ModuleRevisionId mrid, DependencyResolver resolver, int nbDownload) throws ParseException {
        downloadAndCheck(mrid, resolver, nbDownload, nbDownload);
    }

    protected FileSystemWharfResolver createFileSystemResolver(String resolverName, String repoName) {
        return createFileSystemResolver(resolverName, repoName, DEFAULT_IVY_PATTERN, DEFAULT_ARTIFACT_PATTERN);
    }

    protected FileSystemWharfResolver createFileSystemResolver(String resolverName, String repoName,
                                                               String ivyPattern,
                                                               String artifactPattern) {
        FileSystemWharfResolver resolver = new FileSystemWharfResolver();
        resolver.setName(resolverName);
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        StringBuilder builder = new StringBuilder(repoTestRoot.getAbsolutePath());
        if (builder.charAt(builder.length()-1) != '/') {
            builder.append('/');
        }
        builder.append(repoName).append('/');
        String rootPattern = builder.toString();
        if (ivyPattern != null) {
            resolver.addIvyPattern(rootPattern + ivyPattern);
        }
        if (artifactPattern != null) {
            resolver.addArtifactPattern(rootPattern + artifactPattern);
        }
        return resolver;
    }

    protected class IvySettingsTestHolder {
        protected IvySettings settings;
        protected ResolveEngine engine;
        protected ResolveData data;
        protected WharfCacheManager cacheManager;

        protected void init(File baseDir) {
            settings = new IvySettings();
            settings.setBaseDir(baseDir);
            settings.setDefaultCache(cacheFolder);
            cacheManager = WharfCacheManager.newInstance(settings);
            settings.setDefaultRepositoryCacheManager(cacheManager);
            engine = new ResolveEngine(settings, new EventManager(), new SortEngine(settings));
            data = new ResolveData(engine, new ResolveOptions());
        }

    }

    protected File cacheFolder;
    protected File repoTestRoot;

    @Before
    public void setUp() throws Exception {
        // Find the baseDir based on where test repositories are located
        File baseDir = new File(".").getCanonicalFile();
        repoTestRoot = new File(baseDir, SRC_TEST_REPOSITORIES);
        if (!repoTestRoot.exists()) {
            baseDir = new File(baseDir, "wharf-resolver");
            repoTestRoot = new File(baseDir, SRC_TEST_REPOSITORIES);
        }
        assertTrue(repoTestRoot.exists());

        // Create empty test cache folder
        cacheFolder = new File(baseDir, "build/test/cache");
        deleteCacheFolder(cacheFolder);
        cacheFolder.mkdirs();

        // Configure the ivy settings
        defaultSettings = new IvySettingsTestHolder();
        defaultSettings.init(baseDir);
        setupLastModified();
    }

    protected IvySettingsTestHolder createNewSettings() {
        IvySettingsTestHolder holder = new IvySettingsTestHolder();
        holder.init(defaultSettings.settings.getBaseDir());
        return holder;
    }

    public String getIvyPattern() {
        return repoTestRoot + FS + REL_IVY_PATTERN;
    }

    public static void deleteCacheFolder(File toDelete) {
        if (toDelete.exists()) {
            // First delete the symlinks then the filestore
            File[] files = toDelete.listFiles();
            for (File file : files) {
                if (!"filestore".equals(file.getName())) {
                    assertTrue("Could not delete " + file.getAbsolutePath(), deleteFile(file));
                }
            }
            assertTrue("Could not delete " + toDelete.getAbsolutePath(), deleteFile(toDelete));
        }
    }

    private static boolean deleteFolder(File del) {
        File[] files = del.listFiles();
        if (files == null || files.length == 0) {
            return true;
        }
        for (File file : files) {
            if (!deleteFile(file)) {
                return false;
            }
        }
        return true;
    }

    private static boolean deleteFile(File del) {
        boolean result = true;
        if (del.isDirectory()) {
            result = deleteFolder(del);
        } else {
            // It may be a symlink delete and recheck
            FileUtils.getFileUtils().tryHardToDelete(del);
            result = !del.exists();
        }
        if (!result) {
            Assert.fail("Could not delete " + del.getAbsolutePath());
        }
        return result;
    }

    private void setupLastModified() {
        // change important last modified dates cause svn doesn't keep them
        long minute = 60 * 1000;
        long time = new GregorianCalendar().getTimeInMillis() - (4 * minute);
        new File(repoTestRoot, "1/org1/mod1.1/ivys/ivy-1.0.xml").setLastModified(time);
        time += minute;
        new File(repoTestRoot, "1/org1/mod1.1/ivys/ivy-1.0.1.xml").setLastModified(time);
        time += minute;
        new File(repoTestRoot, "1/org1/mod1.1/ivys/ivy-1.1.xml").setLastModified(time);
        time += minute;
        new File(repoTestRoot, "1/org1/mod1.1/ivys/ivy-2.0.xml").setLastModified(time);
    }

    @After()
    public void tearDown() throws Exception {
        deleteCacheFolder(cacheFolder);
    }

    protected DownloadOptions getDownloadOptions() {
        return new DownloadOptions();
    }

    protected Collection<File> getFilesInFileStore() {
        Collection<File> filestore = FileUtil.listAll(new File(cacheFolder, "filestore"), Collections.EMPTY_SET);
        Collection<File> filesInFileStore = new HashSet<File>();
        for (File file : filestore) {
            if (file.isFile()) {
                filesInFileStore.add(file);
            }
        }
        return filesInFileStore;
    }
}
