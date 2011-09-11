package org.jfrog.wharf.ivy.repository;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.jfrog.wharf.ivy.resolver.WharfResolver;

/**
 * Date: 4/13/11
 * Time: 3:37 PM
 *
 * @author Fred Simon
 */
public class WharfArtifactResourceResolver implements ArtifactResourceResolver {
    private final WharfResolver resolver;

    public WharfArtifactResourceResolver(WharfResolver resolver) {
        this.resolver = resolver;
    }

    public ResolvedResource resolve(Artifact artifact) {
        artifact = resolver.fromSystem(artifact);
        return resolver.getArtifactRef(artifact, null);
    }

    public WharfResolver getResolver() {
        return resolver;
    }
}
