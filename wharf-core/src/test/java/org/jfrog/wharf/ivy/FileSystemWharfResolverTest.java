package org.jfrog.wharf.ivy;


import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.jfrog.wharf.ivy.resolver.FileSystemWharfResolver;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Tomer Cohen
 */
public class FileSystemWharfResolverTest extends AbstractDependencyResolverTest {

    @Test
    public void testBasicWharfResolver() throws Exception {
        FileSystemWharfResolver resolver = new FileSystemWharfResolver();
        resolver.setName("test");
        resolver.setSettings(defaultSettings.settings);
        defaultSettings.settings.addResolver(resolver);

        resolver.addIvyPattern(getIvyPattern());
        resolver.addArtifactPattern(repoTestRoot + "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "2.0");
        ResolvedModuleRevision
                rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNotNull(rmr);
        DownloadReport dr = resolver.download(rmr.getDescriptor().getAllArtifacts(), getDownloadOptions());
        assertEquals(1, dr.getArtifactsReports(DownloadStatus.SUCCESSFUL).length);

        Collection<File> filesInFileStore = getFilesInFileStore();
        //there should be only 1 ivy.xml and 1 jar in the filestore since all the jars have the same checksum
        assertEquals(2, filesInFileStore.size());

        mrid = ModuleRevisionId.newInstance("test", "badivycs", "1.0");
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), defaultSettings.data);
        assertNull(rmr);
    }
}
