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

package org.jfrog.wharf.ivy.file;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
import org.jfrog.wharf.ivy.resolver.IvyWharfResolver;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Tomer Cohen
 */
public class IvyWharfResolverTest extends AbstractDependencyResolverTest {

    @Test
    public void testBasicWharfResolver() throws Exception {
        IvyWharfResolver resolver = new IvyWharfResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);

        File sourceDir = new File(repoTestRoot, "checksums");

        resolver.setArtroot(sourceDir.toURI().toURL().toExternalForm());
        resolver.setArtpattern("[module]/[artifact]-[revision].[ext]");

        resolver.setIvyroot(sourceDir.toURI().toURL().toExternalForm());
        resolver.setIvypattern("[module]/ivy-[revision].xml");

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
