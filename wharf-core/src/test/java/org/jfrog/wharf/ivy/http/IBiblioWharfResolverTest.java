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

package org.jfrog.wharf.ivy.http;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
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

    public static final String RJO_ROOT = "http://repo-demo.jfrog.org/artifactory/jboss-releases";
    public static final String RJO_NAME = "rjo";
    public static final String RJO2_ROOT = "http://repo-demo.jfrog.org/artifactory/libs-releases";
    public static final String RJO2_NAME = "rjo2";
    public static final String RGO_ROOT = "http://repo.gradle.org/gradle/libs";
    public static final String RGO_NAME = "rgo";
    public static final String RJO_SNAPSHOTS_ROOT = "http://repo-demo.jfrog.org/artifactory/jboss-snapshots";
    public static final String RJO_SNAPSHOTS_NAME = "rjo-snapshots";

    @Test
    public void testSnapshotsMetadata() throws Exception {

        ChainResolver chainResolver = createChainResolver();

        ModuleRevisionId mridRelease = ModuleRevisionId.newInstance("org.hibernate", "hibernate-annotations", "3.5.5-Final");
        ModuleRevisionId mridSnapshot = ModuleRevisionId.newInstance("org.hibernate", "hibernate-annotations", "3.5.5-SNAPSHOT");

        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridRelease, chainResolver, 2);
        downloadAndCheck(mridSnapshot, chainResolver, 1);
        myTracer.check();
        // TODO: Reduce this number WHARF-30
        assertEquals(37, myTracer.counter.size());

        chainResolver = createChainResolver();

        myTracer = new MyTracer(true, true);
        WharfUrlHandler.tracer = myTracer;
        downloadAndCheck(mridRelease, chainResolver, 0);
        downloadAndCheck(mridSnapshot, chainResolver, 0);
        myTracer.check();
        // TODO: Remove the 4 head requests mainly due to WHARF-31 + WHARF-30
        assertEquals(4, myTracer.counter.size());

        Thread.sleep(1000);

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
        assertEquals(4, myTracer.counter.size());
    }

    private ChainResolver createChainResolver() {
        defaultSettings = createNewSettings();
        FileSystemWharfResolver fileTest = createFileSystemResolver("fileTest", "1");
        IBiblioWharfResolver central = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        IBiblioWharfResolver resolver2 = createIBiblioResolver(RJO_SNAPSHOTS_NAME, RJO_SNAPSHOTS_ROOT);
        central.setSnapshotTimeout(1000);
        resolver2.setSnapshotTimeout(1000);
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
        defaultSettings = createNewSettings();
        IBiblioWharfResolver central = createIBiblioResolver(RGO_NAME, RGO_ROOT);

        WharfUrlHandler.tracer = null;
        downloadAndCheck(ModuleRevisionId.newInstance("org.apache.ant", "ant-parent", "1.7.1"), central, 0);
        ModuleRevisionId mridPMaven = ModuleRevisionId.newInstance("org.sonatype.pmaven", "pmaven-common", "0.8-20100325");
        downloadSources(mridPMaven, central, 0);
        downloadAndCheck(mridPMaven, central, 1);

        defaultSettings = createNewSettings();
        central = createIBiblioResolver(RGO_NAME, RGO_ROOT);
        downloadNoDescriptor(mridPMaven, central, 0);
        downloadAndCheck(mridPMaven, central, 0);
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

    @Test
    public void testBasicWharfResolver() throws Exception {
        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver = createIBiblioResolver(RGO_NAME, RGO_ROOT);
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("junit", "junit", "4.8.2");
        downloadAndCheck(mrid, resolver, 2, 3);
        myTracer.check();
        assertEquals(8, myTracer.counter.size());
        downloadAndCheck(mrid, resolver, 0);
        assertEquals(8, myTracer.counter.size());
    }

    @Test
    public void testFullWharfResolver() throws Exception {
        defaultSettings = createNewSettings();
        MyTracer myTracer = new MyTracer(false);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver = createIBiblioResolver(RJO_NAME, RJO_ROOT);
        fullDownloadAndCheck(resolver, true, 2);
        myTracer.check();
        assertEquals(10, myTracer.counter.size());
        myTracer = new MyTracer(true);
        WharfUrlHandler.tracer = myTracer;
        IBiblioWharfResolver resolver2 = createIBiblioResolver(RJO2_NAME, RJO2_ROOT);
        fullDownloadAndCheck(resolver2, true, 2);
        myTracer.check();
        assertEquals(6, myTracer.counter.size());
        fullDownloadAndCheck(resolver, false, 0);
        assertEquals(6, myTracer.counter.size());
        fullDownloadAndCheck(resolver2, false, 0);
        assertEquals(6, myTracer.counter.size());
    }

    private void fullDownloadAndCheck(IBiblioWharfResolver resolver, boolean shouldDownload, int junitDownloads) throws ParseException {
        downloadAndCheck(ModuleRevisionId.newInstance("org.hibernate", "hibernate-annotations", "3.5.5-Final"), resolver, shouldDownload ? junitDownloads : 0);

        Collection<File> filesInFileStore = getFilesInFileStore();
        assertEquals(4, filesInFileStore.size());
    }

}
