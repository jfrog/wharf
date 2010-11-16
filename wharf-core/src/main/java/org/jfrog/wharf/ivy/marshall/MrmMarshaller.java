package org.jfrog.wharf.ivy.marshall;

import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;

/**
 * @author Tomer Cohen
 */
public interface MrmMarshaller {
    ModuleRevisionMetadata getModuleRevisionMetadata(File file);

    void save(ModuleRevisionMetadata mrm, File file);
}
