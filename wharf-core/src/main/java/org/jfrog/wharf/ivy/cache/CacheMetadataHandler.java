package org.jfrog.wharf.ivy.cache;


import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.lock.ArtifactLockStrategy;
import org.apache.ivy.plugins.lock.LockStrategy;
import org.apache.ivy.plugins.lock.NoLockStrategy;
import org.jfrog.wharf.ivy.marshall.metadata.Jackson.MrmMarshallerImpl;
import org.jfrog.wharf.ivy.marshall.metadata.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;
import java.util.Date;

/**
 * @author Tomer Cohen
 */
public class CacheMetadataHandler implements IvySettingsAware {

    private static final String DEFAULT_DATA_FILE_PATTERN =
            "[organisation]/[module](/[branch])/wharfdata-[revision].json";

    // todo: use Ivy's typedef
    private final MrmMarshaller mrmMarshaller = new MrmMarshallerImpl();

    private File baseDir;

    private IvySettings settings;

    private LockStrategy lockStrategy;

    private String lockStrategyName;


    public CacheMetadataHandler(File baseDir) {
        this.baseDir = baseDir;
        setLockStrategy(new ArtifactLockStrategy());
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
        String wharfDataFileLocation = IvyPatternHelper.substitute(DEFAULT_DATA_FILE_PATTERN, mrid);
        return new File(baseDir, wharfDataFileLocation);
    }

    @Override
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
