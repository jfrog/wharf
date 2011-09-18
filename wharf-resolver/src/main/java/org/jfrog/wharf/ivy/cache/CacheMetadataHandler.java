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

package org.jfrog.wharf.ivy.cache;


import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.jfrog.wharf.ivy.lock.LockHolder;
import org.jfrog.wharf.ivy.lock.LockHolderFactory;
import org.jfrog.wharf.ivy.marshall.api.MarshallerFactory;
import org.jfrog.wharf.ivy.marshall.api.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;

/**
 * @author Tomer Cohen
 */
public class CacheMetadataHandler {

    private final MrmMarshaller mrmMarshaller;
    private final File baseDir;
    private final LockHolderFactory lockFactory;

    public CacheMetadataHandler(File baseDir, LockHolderFactory lockFactory) {
        this.baseDir = baseDir;
        this.mrmMarshaller = MarshallerFactory.createMetadataMarshaller(lockFactory);
        this.lockFactory = lockFactory;
    }

    public void saveModuleRevisionMetadata(ModuleRevisionId mrid, ModuleRevisionMetadata mrm) {
        File wharfDataFile = getWharfDataFile(mrid);
        mrmMarshaller.save(mrm, wharfDataFile);
    }

    public ModuleRevisionMetadata getModuleRevisionMetadata(ModuleRevisionId mrid) {
        File wharfDataFile = getWharfDataFile(mrid);
        return mrmMarshaller.getModuleRevisionMetadata(wharfDataFile);
    }

    public ArtifactMetadata getArtifactMetadata(Artifact artifact) {
        ArtifactMetadata toFind = new ArtifactMetadata(artifact);
        ModuleRevisionMetadata mrm = getModuleRevisionMetadata(artifact.getModuleRevisionId());
        if (mrm == null) {
            return null;
        }
        for (ArtifactMetadata artMd : mrm.artifactMetadata) {
            if (artMd.equals(toFind)) {
                return artMd;
            }
        }
        return null;
    }

    private File getWharfDataFile(ModuleRevisionId mrid) {
        String wharfDataFileLocation = IvyPatternHelper.substitute(mrmMarshaller.getDataFilePattern(), mrid);
        return new File(baseDir, wharfDataFileLocation);
    }

    // lock used to lock all metadata related information access
    public boolean lockMetadataArtifact(ModuleRevisionId mrid) {
        return lockFactory.getOrCreateLockHolder(getWharfDataFile(mrid)).acquireLock();
    }

    public void unlockMetadataArtifact(ModuleRevisionId mrid) {
        LockHolder lockHolder = lockFactory.getLockHolder(getWharfDataFile(mrid));
        if (lockHolder != null) {
            lockHolder.releaseLock();
        }
    }
}
