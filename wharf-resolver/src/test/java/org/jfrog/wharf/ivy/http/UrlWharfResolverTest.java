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

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
import org.jfrog.wharf.ivy.resolver.UrlWharfResolver;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Tomer Cohen
 */
public class UrlWharfResolverTest extends AbstractDependencyResolverTest {

    public static final String RESOLVER_NAME = "test";

    @Test
    public void testUrlWharfResolver() throws Exception {
        UrlWharfResolver resolver1 = setResolver();
        UrlWharfResolver resolver2 = setResolver();
        assertEquals(resolver1, resolver2);
        assertEquals(resolver1.hashCode(), resolver2.hashCode());
        resolver2.addArtifactPattern("toto");
        assertNotSame(resolver1, resolver2);
        assertNotSame(resolver1.hashCode(), resolver2.hashCode());
    }

    private UrlWharfResolver setResolver() throws MalformedURLException {
        UrlWharfResolver resolver = new UrlWharfResolver();
        resolver.setName(RESOLVER_NAME);
        File sourceDir = new File(repoTestRoot, "checksums");
        resolver.addIvyPattern(sourceDir.toURI().toURL().toExternalForm() + "/[module]/ivy-[revision].xml");
        resolver.addArtifactPattern(sourceDir.toURI().toURL().toExternalForm() + "/[module]/[artifact]-[revision].[ext]");
        resolver.addArtifactPattern(sourceDir.toURI().toURL().toExternalForm() + "/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);
        defaultSettings.settings.setDefaultResolver(RESOLVER_NAME);
        return resolver;
    }

    @Test
    public void testBasicWharfResolver() throws Exception {
        UrlWharfResolver resolver = setResolver();

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("test", "allright", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        DownloadReport dr = resolver.download(rmr.getDescriptor().getAllArtifacts(), getDownloadOptions());
        assertEquals(4, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);

        Collection<File> filesInFileStore = getFilesInFileStore();
        //there should be only 1 ivy.xml and 1 jar in the filestore since all the jars have the same checksum
        assertEquals(2, filesInFileStore.size());

        mrid = ModuleRevisionId.newInstance("test", "badivycs", "1.0");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNull(rmr);

        mrid = ModuleRevisionId.newInstance("test", "badartcs", "1.0");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        dr = resolver.download(new Artifact[]{new DefaultArtifact(mrid, rmr.getPublicationDate(),
                mrid.getName(), "jar", "jar")}, getDownloadOptions());
        assertEquals(1, dr.getArtifactsReports(DownloadStatus.FAILED).length);
    }
}
