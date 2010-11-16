package org.jfrog.wharf.ivy;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class ModuleRevisionMetadata {

    public String latestResolvedRevision;
    public String latestResolvedTime;

    public Set<ArtifactMetadata> artifactMetadata = new HashSet<ArtifactMetadata>();


}
