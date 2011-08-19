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

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.jfrog.wharf.ivy.handler.WharfUrlHandler;
import org.jfrog.wharf.ivy.resolver.FileSystemWharfResolver;
import org.jfrog.wharf.ivy.resolver.IBiblioWharfResolver;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Fred Simon
 */
public class IBiblioWharfResolverTest extends AbstractDependencyResolverTest {

    public static final String RJO_ROOT = "http://repo.jfrog.org/artifactory/repo1";
    public static final String RJO_NAME = "rjo";
    public static final String RGO_ROOT = "http://repo.gradle.org/repo1";
    public static final String RGO_NAME = "rgo";
    public static final String RJO_SNAPSHOTS_ROOT = "http://repo.jfrog.org/artifactory/gradle-plugins-snapshots";
    public static final String RJO_SNAPSHOTS_NAME = "rjo-snapshots";

    @Test
    public void testSnapshotsMetadata() throws Exception {

        ChainResolver chainResolver = createChainResolver();

        ModuleRevisionId mridRelease = ModuleRevisionId.newInstance("org.jfrog.buildinfo", "build-info-client", "2.0.1");
        ModuleRevisionId mridSnapshot = ModuleRevisionId.newInstance("org.jfrog.buildinfo", "build-info-client", "2.0.x-SNAPSHOT");

        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridRelease, chainResolver, 2);
        downloadAndCheck(mridSnapshot, chainResolver, 2);
        myTracer.check();
        assertEquals(22, myTracer.counter.size());

        chainResolver = createChainResolver();

        myTracer = new MyTracer(true, true);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridRelease, chainResolver, 0);
        downloadAndCheck(mridSnapshot, chainResolver, 0);
        myTracer.check();
        // TODO: Remove the 5 head requests
        assertEquals(5, myTracer.counter.size());

        Thread.sleep(200);

        chainResolver = createChainResolver();

        myTracer = new MyTracer(true, true);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridRelease, chainResolver, 0);
        myTracer.check();
        assertEquals(0, myTracer.counter.size());

        // TODO: Find a way to touch the maven metadata xml file
        myTracer = new MyTracer(true, false);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridSnapshot, chainResolver, 0);
        myTracer.check();
        assertEquals(5, myTracer.counter.size());
    }

    private ChainResolver createChainResolver() {
        IvySettingsTestHolder newSettings = createNewSettings();
        defaultSettings = newSettings;
        FileSystemWharfResolver fileTest = createFileSystemResolver("fileTest", "1");
        IBiblioWharfResolver central = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        IBiblioWharfResolver resolver2 = createIBiblioResolver(RJO_SNAPSHOTS_NAME, RJO_SNAPSHOTS_ROOT);
        central.setSnapshotTimeout(200);
        resolver2.setSnapshotTimeout(200);
        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("chainTest");
        chainResolver.setSettings(defaultSettings.settings);
        chainResolver.add(fileTest);
        chainResolver.add(central);
        chainResolver.add(resolver2);
        defaultSettings.settings.addResolver(chainResolver);
        defaultSettings.settings.setDefaultResolver(chainResolver.getName());
        return chainResolver;
    }

    @Test
    public void testNPENoMetadata() throws Exception {
        ChainResolver chainResolver = createChainResolver();

        WharfUrlHandler.tracer = null;
        downloadAndCheck(ModuleRevisionId.newInstance("org.apache.ant", "ant-parent", "1.7.1"), chainResolver, 0);
        ModuleRevisionId mridPMaven = ModuleRevisionId.newInstance("org.sonatype.pmaven", "pmaven-common", "0.8-20100325");
        downloadSources(mridPMaven, chainResolver, 0);
        downloadAndCheck(mridPMaven, chainResolver, 1);

        chainResolver = createChainResolver();
        downloadNoDescriptor(mridPMaven, chainResolver, 0);
        downloadAndCheck(mridPMaven, chainResolver, 0);
    }

    @Test
    public void testIBiblioWharfResolver() throws Exception {
        IBiblioWharfResolver resolver1 = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        IBiblioWharfResolver resolver2 = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        assertEquals(resolver1, resolver2);
        assertEquals(resolver1.hashCode(), resolver2.hashCode());
        resolver2.setName(RGO_NAME);
        assertNotSame(resolver1, resolver2);
        assertNotSame(resolver1.hashCode(), resolver2.hashCode());
        IBiblioWharfResolver resolver3 = createIBiblioResolver(RJO_NAME, RGO_ROOT);
        assertNotSame(resolver1, resolver3);
        assertNotSame(resolver1.hashCode(), resolver3.hashCode());
    }

    static class MyTracer implements WharfUrlHandler.TraceCounter {
        final boolean shouldNotDownload;
        final boolean shouldNotSendHeads;
        final Map<String, List<Integer>> counter = new HashMap<String, List<Integer>>();

        MyTracer(boolean shouldNotDownload) {
            this.shouldNotDownload = shouldNotDownload;
            this.shouldNotSendHeads = false;
        }

        MyTracer(boolean shouldNotDownload, boolean shouldNotSendHeads) {
            this.shouldNotDownload = shouldNotDownload;
            this.shouldNotSendHeads = shouldNotSendHeads;
        }

        public void add(String query, int status) {
            if (shouldNotSendHeads) {
                // TODO: Maven metadata xml file is directly (and all the time) open by Ivy in org.apache.ivy.plugins.resolver.IBiblioResolver.findSnapshotVersion()
                // TODO: Cache the 404 answers
                if (!query.endsWith("maven-metadata.xml") && status != 404) {
                    fail("Query " + query + " should not happen!");
                } else {
                    System.out.println("Try to avoid head query " + query + " got " + status);
                }
            }
            assertFalse("Query " + query + " should not happen!", shouldNotDownload && query.startsWith("GET") && status == 200);
            List<Integer> count = counter.get(query);
            if (count == null) {
                count = new ArrayList<Integer>();
                counter.put(query, count);
            }
            count.add(status);
        }

        public void check() {
            // There should be only one status per URL, and every HEAD 200 should have a GET 200 in download mode
            for (Map.Entry<String, List<Integer>> entry : counter.entrySet()) {
                assertEquals(1, entry.getValue().size());
                if (!shouldNotDownload) {
                    String query = entry.getKey();
                    if (query.startsWith("HEAD") && entry.getValue().get(0) == 200) {
                        String getQuery = query.replace("HEAD", "GET");
                        // TODO: Maven metadata xml file is directly (and all the time) open by Ivy in org.apache.ivy.plugins.resolver.IBiblioResolver.findSnapshotVersion()
                        // TODO: Find a way to clean resource handling of maven metadata xml
                        if (!getQuery.endsWith("maven-metadata.xml")) {
                            assertTrue("HEAD query " + query + " does not have a GET in " + counter, counter.containsKey(getQuery));
                            assertTrue("GET query for " + getQuery + " did not return 200", counter.get(getQuery).get(0) == 200);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBasicWharfResolver() throws Exception {
        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("junit", "junit", "4.8.2");
        downloadAndCheck(mrid, resolver, 3);
        myTracer.check();
        assertEquals(8, myTracer.counter.size());
        downloadAndCheck(mrid, resolver, 0);
        assertEquals(8, myTracer.counter.size());
    }

    @Test
    public void testFullWharfResolver() throws Exception {
        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        fullDownloadAndCheck(resolver, true, 3);
        myTracer.check();
        assertEquals(19, myTracer.counter.size());
        myTracer = new MyTracer(true);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver2 = createIBiblioResolver(RGO_NAME, RGO_ROOT);
        fullDownloadAndCheck(resolver2, true, 1);
        myTracer.check();
        assertEquals(11, myTracer.counter.size());
        fullDownloadAndCheck(resolver, false, 3);
        assertEquals(11, myTracer.counter.size());
        fullDownloadAndCheck(resolver2, false, 3);
        assertEquals(11, myTracer.counter.size());
    }

    private void fullDownloadAndCheck(IBiblioWharfResolver resolver, boolean shouldDownload, int junitDownloads) throws ParseException {
        downloadAndCheck(ModuleRevisionId.newInstance("org.antlr", "antlr", "3.1.3"), resolver, shouldDownload ? 3 : 0);
        downloadAndCheck(ModuleRevisionId.newInstance("junit", "junit", "4.8"), resolver, shouldDownload ? junitDownloads : 0);

        Collection<File> filesInFileStore = getFilesInFileStore();
        assertEquals(9, filesInFileStore.size());
    }

}
