package org.jfrog.wharf.ivy.file;


import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
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
        FileSystemWharfResolver resolver = createFileSystemResolver("test", "1");

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
