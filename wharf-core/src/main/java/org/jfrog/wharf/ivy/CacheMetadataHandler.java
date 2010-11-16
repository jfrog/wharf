package org.jfrog.wharf.ivy;


import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.lock.LockStrategy;

import java.io.File;

/**
 * @author Tomer Cohen
 */
public class CacheMetadataHandler implements IvySettingsAware {

    private static final String DEFAULT_DATA_FILE_PATTERN =
            "[organisation]/[module](/[branch])/wharfdata-[revision].json";

    // todo: use Ivy's typedef
    private MrmMarshaller mrmMarshaller = new MrmMarshallerImpl();

    private File baseDir;

    private IvySettings settings;

    private LockStrategy lockStrategy;

    private String lockStrategyName;


    public CacheMetadataHandler(File baseDir) {
        this.baseDir = baseDir;
    }

    public void saveModuleRevisionMetadata(ModuleRevisionId mrid, ModuleRevisionMetadata mrm) {
        File wharfDataFile = getWharfDataFile(mrid);
        mrmMarshaller.save(mrm, wharfDataFile);
    }

    public ModuleRevisionMetadata getModuleRevisionMetadata(ModuleRevisionId mrid) {
        File wharfDataFile = getWharfDataFile(mrid);
        return mrmMarshaller.getModuleRevisionMetadata(wharfDataFile);
    }

    public ArtifactMetadata getArtifactMetadata(Artifact artifact, int resolverId) {
        ArtifactMetadata toFind = new ArtifactMetadata(artifact, resolverId);
        ModuleRevisionMetadata mrm = getModuleRevisionMetadata(artifact.getModuleRevisionId());
        for (ArtifactMetadata artMd : mrm.artifactMetadata) {
            if (artMd.equals(toFind)) {
                return artMd;
            }
        }
        return toFind;
    }

    private File getWharfDataFile(ModuleRevisionId mrid) {
        String wharfDataFileLocation = IvyPatternHelper.substitute(DEFAULT_DATA_FILE_PATTERN, mrid);
        return new File(baseDir, wharfDataFileLocation);
    }

    public void setSettings(IvySettings settings) {
        this.settings = settings;
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
            return getLockStrategy().lockArtifact(artifact, getWharfDataFile(mrid));
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
        // TODO: If lock strategy is from the default ones (noLock or artifact) just return null
        return null;
        // If special lock strategy create the following
        //return new DefaultArtifact(mrid, new Date(), "wharf-metadata", "metadata", "ivy", true);
    }

}
