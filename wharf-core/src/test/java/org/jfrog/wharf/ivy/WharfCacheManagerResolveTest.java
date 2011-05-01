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

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.latest.LatestRevisionStrategy;
import org.apache.ivy.plugins.latest.LatestTimeStrategy;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.FileUtil;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;
import org.jfrog.wharf.ivy.resolver.FileSystemWharfResolver;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 *
 */
public class WharfCacheManagerResolveTest extends AbstractDependencyResolverTest {

    public WharfCacheManagerResolveTest() {
    }

    private FileSystemWharfResolver createFileSystemResolver() {
        // TODO: Check also standard FS resolver with WharfCacheManager?
        return new FileSystemWharfResolver();
    }

    @Test
    public void testFixedRevision() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(getIvyPattern());
        resolver.addArtifactPattern(repoTestRoot.getAbsolutePath() + "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid,
                false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());

        // test to ask to download
        DefaultArtifact artifact = new DefaultArtifact(mrid, pubdate, "mod1.1", "jar", "jar");
        DownloadReport report = resolver.download(new Artifact[]{artifact},
                getDownloadOptions());
        assertNotNull(report);

        assertEquals(1, report.getArtifactsReports().length);

        ArtifactDownloadReport ar = report.getArtifactReport(artifact);
        assertNotNull(ar);

        assertEquals(artifact, ar.getArtifact());
        assertEquals(DownloadStatus.SUCCESSFUL, ar.getDownloadStatus());

        // test to ask to download again, should use cache
        report = resolver.download(new Artifact[]{artifact}, getDownloadOptions());
        assertNotNull(report);

        assertEquals(1, report.getArtifactsReports().length);

        ar = report.getArtifactReport(artifact);
        assertNotNull(ar);

        assertEquals(artifact, ar.getArtifact());
        assertEquals(DownloadStatus.NO, ar.getDownloadStatus());
    }

    @Test
    public void testChecksum() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);

        resolver.addIvyPattern(repoTestRoot + "/checksums/[module]/[artifact]-[revision].[ext]");
        resolver.addArtifactPattern(repoTestRoot + "/checksums/[module]/[artifact]-[revision].[ext]");

        resolver.setChecksums("sha1, md5");
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("test", "allright", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        DownloadReport dr = resolver.download(rmr.getDescriptor().getAllArtifacts(), getDownloadOptions());
        assertEquals(4, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);

        resolver.setChecksums("md5");
        mrid = ModuleRevisionId.newInstance("test", "badivycs", "1.0");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNull(rmr);
        resolver.setChecksums("none");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        dr = resolver.download(new Artifact[]{new DefaultArtifact(mrid, rmr.getPublicationDate(),
                mrid.getName(), "jar", "jar")}, getDownloadOptions());
        assertEquals(1, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);

        resolver.setChecksums("md5");
        mrid = ModuleRevisionId.newInstance("test", "badartcs", "1.0");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        dr = resolver.download(new Artifact[]{new DefaultArtifact(mrid, rmr.getPublicationDate(),
                mrid.getName(), "jar", "jar")}, getDownloadOptions());
        assertEquals(1, dr.getArtifactsReports(DownloadStatus.FAILED).length);

        resolver.setChecksums("");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        dr = resolver.download(new Artifact[]{new DefaultArtifact(mrid, rmr.getPublicationDate(),
                mrid.getName(), "jar", "jar")}, getDownloadOptions());
        assertEquals(1, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);
    }

    @Test
    public void testCheckModified() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(repoTestRoot + FS + "checkmodified" + FS + "ivy-[revision].xml");
        File modify = new File(repoTestRoot, "checkmodified/ivy-1.0.xml");
        FileUtil.copy(new File(repoTestRoot, "checkmodified/ivy-1.0-before.xml"), modify, null, true);
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());

        // updates ivy file in repository
        FileUtil.copy(new File(repoTestRoot, "checkmodified/ivy-1.0-after.xml"), modify, null, true);
        pubdate = new GregorianCalendar(2005, 4, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());

        // should not get the new version
        resolver.setCheckmodified(false);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime(), rmr.getPublicationDate());

        // should now get the new version
        resolver.setCheckmodified(true);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    @Test
    public void testNoRevision() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());
        resolver.addIvyPattern(repoTestRoot + FS + "norevision" + FS
                + "ivy-[module].xml");
        resolver.addArtifactPattern(repoTestRoot + FS + "norevision" + FS
                + "[artifact].[ext]");
        File modify = new File(repoTestRoot, "norevision/ivy-mod1.1.xml");
        File artifact = new File(repoTestRoot, "norevision/mod1.1.jar");

        // 'publish' 'before' version
        FileUtil.copy(new File(repoTestRoot, "norevision/ivy-mod1.1-before.xml"), modify, null,
                true);
        FileUtil.copy(new File(repoTestRoot, "norevision/mod1.1-before.jar"), artifact, null,
                true);
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "latest.integration");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(ModuleRevisionId.newInstance("org1", "mod1.1", "1.0"), rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());

        Artifact[] artifacts = rmr.getDescriptor().getArtifacts("default");
        WharfResolverMetadata wharfResolverMetadata = defaultSettings.cacheManager.getResolverHandler().getResolver(resolver);
        artifacts[0] = ArtifactMetadata.fillResolverId(artifacts[0], wharfResolverMetadata.getId());
        File archiveFileInCache = defaultSettings.cacheManager.getArchiveFileInCache(artifacts[0]);
        resolver.download(artifacts, getDownloadOptions());
        assertTrue(archiveFileInCache.exists());
        BufferedReader r = new BufferedReader(new FileReader(archiveFileInCache));
        assertEquals("before", r.readLine());
        r.close();

        // updates ivy file and artifact in repository
        FileUtil.copy(new File(repoTestRoot, "norevision/ivy-mod1.1-after.xml"), modify, null,
                true);
        FileUtil.copy(new File(repoTestRoot, "norevision/mod1.1-after.jar"), artifact, null,
                true);
        pubdate = new GregorianCalendar(2005, 4, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());
        // no need to update new artifact timestamp cause it isn't used

        // should get the new version even if checkModified is false, because we ask a
        // latest.integration
        resolver.setCheckmodified(false);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(ModuleRevisionId.newInstance("org1", "mod1.1", "1.1"), rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());

        artifacts = rmr.getDescriptor().getArtifacts("default");
        artifacts[0] = ArtifactMetadata.fillResolverId(artifacts[0], wharfResolverMetadata.getId());
        archiveFileInCache = defaultSettings.cacheManager.getArchiveFileInCache(artifacts[0]);

        assertFalse(archiveFileInCache.exists());

        // should download the new artifact
        artifacts = rmr.getDescriptor().getArtifacts("default");
        resolver.download(artifacts, getDownloadOptions());
        assertTrue(archiveFileInCache.exists());
        r = new BufferedReader(new FileReader(archiveFileInCache));
        assertEquals("after", r.readLine());
        r.close();
    }

    @Test
    public void testChanging() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(repoTestRoot + FS + "checkmodified" + FS
                + "ivy-[revision].xml");
        resolver.addArtifactPattern(repoTestRoot + FS + "checkmodified" + FS
                + "[artifact]-[revision].[ext]");
        File modify = new File(repoTestRoot, "checkmodified/ivy-1.0.xml");
        File artifact = new File(repoTestRoot, "checkmodified/mod1.1-1.0.jar");

        // 'publish' 'before' version
        FileUtil.copy(new File(repoTestRoot, "checkmodified/ivy-1.0-before.xml"), modify, null,
                true);
        FileUtil.copy(new File(repoTestRoot, "checkmodified/mod1.1-1.0-before.jar"), artifact,
                null, true);
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid,
                false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());

        Artifact[] artifacts = rmr.getDescriptor().getArtifacts("default");
        resolver.download(artifacts, getDownloadOptions());
        WharfResolverMetadata wharfResolverMetadata = defaultSettings.cacheManager.getResolverHandler().getResolver(resolver);
        artifacts[0] = ArtifactMetadata.fillResolverId(artifacts[0], wharfResolverMetadata.getId());
        File archiveFileInCache = defaultSettings.cacheManager.getArchiveFileInCache(artifacts[0]);
        assertTrue(archiveFileInCache.exists());
        BufferedReader r = new BufferedReader(new FileReader(archiveFileInCache));
        assertEquals("before", r.readLine());
        r.close();

        // updates ivy file and artifact in repository
        FileUtil.copy(new File(repoTestRoot, "checkmodified/ivy-1.0-after.xml"), modify, null,
                true);
        FileUtil.copy(new File(repoTestRoot, "checkmodified/mod1.1-1.0-after.jar"), artifact,
                null, true);
        pubdate = new GregorianCalendar(2005, 4, 1, 11, 0, 0).getTime();
        modify.setLastModified(pubdate.getTime());
        // no need to update new artifact timestamp cause it isn't used

        // should not get the new version: checkmodified is false and dependency is not told to be a
        // changing one
        resolver.setCheckmodified(false);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime(), rmr
                .getPublicationDate());

        assertTrue(archiveFileInCache.exists());
        r = new BufferedReader(new FileReader(archiveFileInCache));
        assertEquals("before", r.readLine());
        r.close();

        // should now get the new version cause we say it's a changing one
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false, true), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        assertEquals(pubdate, rmr.getPublicationDate());

        assertFalse(archiveFileInCache.exists());

        artifacts = rmr.getDescriptor().getArtifacts("default");
        resolver.download(artifacts, getDownloadOptions());
        assertTrue(archiveFileInCache.exists());
        r = new BufferedReader(new FileReader(archiveFileInCache));
        assertEquals("after", r.readLine());
        r.close();
    }

    @Test
    public void testRelativePath() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(getIvyPattern());
        resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");

        resolver.setLatestStrategy(new LatestRevisionStrategy());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "2.0");
        ResolvedModuleRevision rmr = resolver
                .getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1",
                        "mod1.1", "latest.integration"), false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2005, 1, 15, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    @Test
    public void testFormattedLatestTime() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(getIvyPattern());
        resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");

        resolver.setLatestStrategy(new LatestTimeStrategy());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.1");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(
                ModuleRevisionId.newInstance("org1", "mod1.1", "1+"), false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2005, 0, 2, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    @Test
    public void testFormattedLatestRevision() throws Exception {
        FileSystemResolver resolver = createFileSystemResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        assertEquals("test", resolver.getName());

        resolver.addIvyPattern(getIvyPattern());
        resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");

        resolver.setLatestStrategy(new LatestRevisionStrategy());

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.1");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(
                ModuleRevisionId.newInstance("org1", "mod1.1", "1+"), false), defaultSettings.data);
        assertNotNull(rmr);

        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2005, 0, 2, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }


    @Test
    public void testUnsupportedTransaction() throws Exception {
        try {
            FileSystemResolver resolver = createFileSystemResolver();
            resolver.setName("test");
            resolver.setSettings(defaultSettings.settings);
            resolver.setTransactional("true");

            resolver.addArtifactPattern(
                    // this pattern is not supported for transaction publish
                    repoTestRoot + "/1/[organisation]/[module]/[artifact]-[revision].[ext]");

            ModuleRevisionId mrid = ModuleRevisionId.newInstance("myorg", "mymodule", "myrevision");
            Artifact artifact = new DefaultArtifact(mrid, new Date(), "myartifact", "mytype",
                    "myext");
            File src = new File("test/repositories/ivysettings.xml");
            try {
                resolver.beginPublishTransaction(mrid, false);

                resolver.publish(artifact, src, false);
                fail("publishing with transaction=true and an unsupported pattern should raise an exception");
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().indexOf("transactional") != -1);
            }
        } finally {
            FileUtil.forceDelete(new File("test/repositories/1/myorg"));
        }
    }

    @Test
    public void testUnsupportedTransaction2() throws Exception {
        try {
            FileSystemResolver resolver = createFileSystemResolver();
            resolver.setName("test");
            resolver.setSettings(defaultSettings.settings);
            resolver.setTransactional("true");

            // the two patterns are inconsistent and thus not supported for transactions
            resolver.addIvyPattern(repoTestRoot + "/1/[organisation]-[module]/[revision]/[artifact]-[revision].[ext]");
            resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]");

            ModuleRevisionId mrid = ModuleRevisionId.newInstance("myorg", "mymodule", "myrevision");
            Artifact ivyArtifact = new DefaultArtifact(mrid, new Date(), "ivy", "ivy", "xml");
            Artifact artifact = new DefaultArtifact(mrid, new Date(), "myartifact", "mytype",
                    "myext");
            File src = new File(repoTestRoot, "ivysettings.xml");
            try {
                resolver.beginPublishTransaction(mrid, false);
                resolver.publish(ivyArtifact, src, false);
                resolver.publish(artifact, src, false);
                fail("publishing with transaction=true and an unsupported combination of patterns should raise an exception");
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().indexOf("transactional") != -1);
            }
        } finally {
            FileUtil.forceDelete(new File("test/repositories/1/myorg"));
        }
    }

    @Test
    public void testUnsupportedTransaction3() throws Exception {
        try {
            FileSystemResolver resolver = createFileSystemResolver();
            resolver.setName("test");
            resolver.setSettings(defaultSettings.settings);
            resolver.setTransactional("true");

            resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]");

            ModuleRevisionId mrid = ModuleRevisionId.newInstance("myorg", "mymodule", "myrevision");
            Artifact artifact = new DefaultArtifact(mrid, new Date(), "myartifact", "mytype",
                    "myext");
            File src = new File(repoTestRoot, "ivysettings.xml");
            try {
                // overwrite transaction not supported
                resolver.beginPublishTransaction(mrid, true);

                resolver.publish(artifact, src, true);
                fail("publishing with transaction=true and overwrite mode should raise an exception");
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().indexOf("transactional") != -1);
            }
        } finally {
            FileUtil.forceDelete(new File(repoTestRoot, "1/myorg"));
        }
    }

}
