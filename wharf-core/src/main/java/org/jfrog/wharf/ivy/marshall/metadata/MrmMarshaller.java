package org.jfrog.wharf.ivy.marshall.metadata;

import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;

/**
 * @author Tomer Cohen
 */
public interface MrmMarshaller {
    ModuleRevisionMetadata getModuleRevisionMetadata(File file);

    void save(ModuleRevisionMetadata mrm, File file);

    public String getDataFilePattern();
}
