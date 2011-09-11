package org.jfrog.wharf.ivy.cache;

import org.apache.ivy.core.module.id.ModuleRevisionId;

/**
 * Date: 9/1/11
 * Time: 11:32 AM
 *
 * @author Fred Simon
 */
public interface ModuleMetadataManager {
    public long getLastResolvedTime(ModuleRevisionId mrid);
}
