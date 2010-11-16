package org.jfrog.wharf.ivy.model;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class ModuleRevisionMetadata {

    public String latestResolvedRevision;
    public String latestResolvedTime;

    public Set<ArtifactMetadata> artifactMetadata = new HashSet<ArtifactMetadata>();


    public Set<ArtifactMetadata> getArtifactMetadata() {
        return artifactMetadata;
    }

    public void setArtifactMetadata(Set<ArtifactMetadata> artifactMetadata) {
        this.artifactMetadata = artifactMetadata;
    }

    public String getLatestResolvedRevision() {
        return latestResolvedRevision;
    }

    public void setLatestResolvedRevision(String latestResolvedRevision) {
        this.latestResolvedRevision = latestResolvedRevision;
    }

    public String getLatestResolvedTime() {
        return latestResolvedTime;
    }

    public void setLatestResolvedTime(String latestResolvedTime) {
        this.latestResolvedTime = latestResolvedTime;
    }
}
