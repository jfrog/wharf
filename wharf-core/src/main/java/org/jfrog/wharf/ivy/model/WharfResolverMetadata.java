package org.jfrog.wharf.ivy.model;

import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class WharfResolverMetadata {

    public String id;

    public String name;

    public String type;

    public boolean m2compatible;

    public String ivyPattern;

    public String artifactPattern;

    public String user;

    public Map<String, String> params;

    public String[] checksumAlgorithms;

    public String authentication;

    public String proxy;

    public WharfResolverMetadata() {
    }

    public WharfResolverMetadata(DependencyResolver resolver) {
        this.name = resolver.getName();
        if (resolver instanceof AbstractResolver) {
            this.type = ((AbstractResolver) resolver).getTypeName();
        } else {
            this.type = resolver.getClass().getName();
        }
        if (resolver instanceof BasicResolver) {
            this.checksumAlgorithms = ((BasicResolver) resolver).getChecksumAlgorithms();
        }
        if (resolver instanceof AbstractPatternsBasedResolver) {
            AbstractPatternsBasedResolver patternsBasedResolver = (AbstractPatternsBasedResolver) resolver;
            this.m2compatible = patternsBasedResolver.isM2compatible();
            List<String> patterns = patternsBasedResolver.getIvyPatterns();
            if (patterns.isEmpty()) {
                this.ivyPattern = "";
            } else {
                this.ivyPattern = patterns.get(0);
            }
            patterns = patternsBasedResolver.getArtifactPatterns();
            if (patterns.isEmpty()) {
                this.artifactPattern = "";
            } else {
                this.artifactPattern = patterns.get(0);
            }
        }
        // TODO: Find the user
    }

    public String getId() {
        if (id == null || id.isEmpty()) {
            String idString = type + name + ivyPattern + artifactPattern + params + user;
            id = WharfUtils.computeUUID(idString);
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WharfResolverMetadata that = (WharfResolverMetadata) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
