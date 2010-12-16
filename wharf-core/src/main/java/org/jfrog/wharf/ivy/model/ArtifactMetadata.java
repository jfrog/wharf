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
    public String resolverId;
    public String artResolverId;
    public String id;
    public String location;
    public boolean local;
    public String md5;
    public String sha1;

    public ArtifactMetadata() {
    }

    public static String extractResolverId(Artifact artifact, ArtifactOrigin origin) {
        String artifactResolverId = extractResolverId(artifact);
        if (artifactResolverId == null || artifactResolverId.isEmpty()) {
            // Get the resolver id from the origin
            return extractResolverId(origin.getArtifact());
        }
        return artifactResolverId;
    }

    public static String extractResolverId(Artifact artifact) {
        return (String) artifact.getQualifiedExtraAttributes().get(WHARF_RESOLVER_ID);
    }

    public static Artifact fillResolverId(Artifact artifact, String resolverId) {
        Map<String, String> extraAttributes = new HashMap<String, String>(artifact.getQualifiedExtraAttributes());
        extraAttributes.put(ArtifactMetadata.WHARF_RESOLVER_ID, String.valueOf(resolverId));
        return new DefaultArtifact(artifact.getModuleRevisionId(), artifact.getPublicationDate(),
                artifact.getName(), artifact.getType(), artifact.getExt(), artifact.getUrl(), extraAttributes);
    }

    private ArtifactMetadata(Artifact artifact, String resolverId) {
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
        if (artResolverId == null || artResolverId.isEmpty()) {
            artResolverId = resolverId;
        }
        if (resolverId == null || resolverId.isEmpty()) {
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
        return resolverId.equals(metadata.resolverId) && id.equals(metadata.id);
    }

    @Override
    public int hashCode() {
        int result = resolverId.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
