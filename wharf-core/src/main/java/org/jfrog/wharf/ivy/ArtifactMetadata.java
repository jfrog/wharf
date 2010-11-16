package org.jfrog.wharf.ivy;


import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;

import java.net.URL;

/**
 * @author Tomer Cohen
 */
public class ArtifactMetadata {
    public int resolverId;
    public int artResolverId;
    public String id;
    public String location;
    public boolean local;
    public String md5;
    public String sha1;

    public ArtifactMetadata() {
    }

    public ArtifactMetadata(Artifact artifact, int resolverId) {
        this.id = getArtId(artifact);
        this.artResolverId = resolverId;
        this.resolverId = resolverId;
        URL url = artifact.getUrl();
        if (url == null) {
            this.location = "";
        } else {
            this.location = url.toExternalForm();
        }
        this.local = false;
        check();
    }

    public ArtifactMetadata(Artifact artifact, ArtifactOrigin artifactOrigin, int resolverId) {
        this(artifact, resolverId);
        this.location = artifactOrigin.getLocation();
        this.local = artifactOrigin.isLocal();
        check();
    }

    public void check() {
        if (id == null) {
            throw new NullPointerException("ArtifactMetadata ID cannot be null!");
        }
        if (artResolverId == 0) {
            artResolverId = resolverId;
        }
    }

    /**
     * Creates the unique prefix key that will reference the artifact within the properties.
     *
     * @param artifact the artifact to create the unique key from. Cannot be null.
     * @return the unique prefix key as a string.
     */
    static String getArtId(Artifact artifact) {
        // use the hashcode as a uuid for the artifact (fingers crossed)
        int hashCode = artifact.getId().hashCode();
        // use just some visual cue
        return "artifact:" + artifact.getName() + "#" + artifact.getType() + "#"
                + artifact.getExt() + "#" + hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArtifactMetadata metadata = (ArtifactMetadata) o;
        return resolverId == metadata.resolverId && id.equals(metadata.id);
    }

    @Override
    public int hashCode() {
        int result = resolverId;
        result = 31 * result + id.hashCode();
        return result;
    }
}
