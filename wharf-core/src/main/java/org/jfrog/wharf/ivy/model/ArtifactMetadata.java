package org.jfrog.wharf.ivy.model;


import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class ArtifactMetadata {
    public static final String WHARF_RESOLVER_ID = "wharf:resolverId";
    public int resolverId;
    public int artResolverId;
    public String id;
    public String location;
    public boolean local;
    public String md5;
    public String sha1;

    public ArtifactMetadata() {
    }

    public static int extractResolverId(Artifact artifact, ArtifactOrigin origin) {
        int originResolverId = extractResolverId(origin.getArtifact());
        if (originResolverId == 0) {
            return extractResolverId(artifact);
        }
        return originResolverId;
    }

    public static int extractResolverId(Artifact artifact) {
        String resolverId = (String) artifact.getQualifiedExtraAttributes().get(WHARF_RESOLVER_ID);
        if (resolverId == null || resolverId.length() == 0) {
            return 0;
        }
        return Integer.parseInt(resolverId);
    }

    public static Artifact fillResolverId(Artifact artifact, int resolverId) {
        Map<String, String> extraAttributes = new HashMap<String, String>(artifact.getQualifiedExtraAttributes());
        extraAttributes.put(ArtifactMetadata.WHARF_RESOLVER_ID, String.valueOf(resolverId));
        return new DefaultArtifact(artifact.getModuleRevisionId(), artifact.getPublicationDate(),
                artifact.getName(), artifact.getType(), artifact.getExt(), artifact.getUrl(), extraAttributes);
    }

    public ArtifactMetadata(Artifact artifact, int resolverId) {
        this.id = getArtId(artifact);
        this.resolverId = resolverId;
        this.artResolverId = resolverId;
        URL url = artifact.getUrl();
        if (url == null) {
            this.location = "";
        } else {
            this.location = url.toExternalForm();
        }
        this.local = false;
        check();
    }

    public ArtifactMetadata(Artifact artifact) {
        this(artifact, extractResolverId(artifact));
    }

    public ArtifactMetadata(Artifact artifact, ArtifactOrigin artifactOrigin) {
        this(artifact, extractResolverId(artifact, artifactOrigin));
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
        if (resolverId == 0) {
            throw new IllegalStateException("Resolver id cannot be 0");
        }
    }

    /**
     * Creates the unique prefix key that will reference the artifact within the properties.
     *
     * @param artifact the artifact to create the unique key from. Cannot be null.
     * @return the unique prefix key as a string.
     */
    public static String getArtId(Artifact artifact) {
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
