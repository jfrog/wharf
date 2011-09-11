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
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.lock.ArtifactLockStrategy;
import org.apache.ivy.plugins.lock.LockStrategy;
import org.apache.ivy.plugins.lock.NoLockStrategy;
import org.jfrog.wharf.ivy.marshall.api.MarshallerFactory;
import org.jfrog.wharf.ivy.marshall.api.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;
import java.util.Date;

/**
 * @author Tomer Cohen
 */
public class CacheMetadataHandler {

    private static final ArtifactLockStrategy LOCK_STRATEGY = new ArtifactLockStrategy();

    private final MrmMarshaller mrmMarshaller = MarshallerFactory.createMetadataMarshaller();

    private final File baseDir;
    private final IvySettings settings;

    private LockStrategy lockStrategy;
    private String lockStrategyName;

    public CacheMetadataHandler(File baseDir, IvySettings settings) {
        this.baseDir = baseDir;
        this.settings = settings;
        setLockStrategy(LOCK_STRATEGY);
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

    public LockStrategy getLockStrategy() {
        if (lockStrategy == null) {
            if (lockStrategyName != null) {
                lockStrategy = settings.getLockStrategy(lockStrategyName);
            } else {
                lockStrategy = settings.getDefaultLockStrategy();
            }
        }
        return lockStrategy;
    }

    public void setLockStrategy(LockStrategy lockStrategy) {
        this.lockStrategy = lockStrategy;
    }

    public void setLockStrategy(String lockStrategyName) {
        this.lockStrategyName = lockStrategyName;
    }

    // lock used to lock all metadata related information access
    public boolean lockMetadataArtifact(ModuleRevisionId mrid) {
        Artifact artifact = getDefaultMetadataArtifact(mrid);
        try {
            // we need to provide an artifact origin to be sure we do not end up in a stack overflow
            // if the cache pattern is using original name, and the substitution thus trying to get
            // the saved artifact origin value which in turns calls this method
            File wharfDataFile = getWharfDataFile(mrid);
            if (!wharfDataFile.getParentFile().exists()) {
                wharfDataFile.getParentFile().mkdirs();
            }
            return getLockStrategy().lockArtifact(artifact, wharfDataFile);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // reset interrupt status
            throw new RuntimeException("operation interrupted");
        }
    }

    public void unlockMetadataArtifact(ModuleRevisionId mrid) {
        Artifact artifact = getDefaultMetadataArtifact(mrid);
        getLockStrategy().unlockArtifact(artifact, getWharfDataFile(mrid));
    }

    private Artifact getDefaultMetadataArtifact(ModuleRevisionId mrid) {
        LockStrategy strategy = getLockStrategy();
        if (strategy instanceof ArtifactLockStrategy || strategy instanceof NoLockStrategy) {
            return null;
        }
        // If special lock strategy create the following
        return new DefaultArtifact(mrid, new Date(), "wharf-metadata", "metadata", "ivy", true);
    }
}