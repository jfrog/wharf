package org.jfrog.wharf.ivy;


import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class ResolverId {

    private String name;

    private String type;

    private String url;

    private String user;

    private Map<String, String> params;

    public ResolverId() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class ResolverIdBuilder {
        private ResolverId resolverId = new ResolverId();

        public ResolverIdBuilder name(String name) {
            resolverId.name = name;
            return this;
        }

        public ResolverIdBuilder dependencyResolver(DependencyResolver resolver) {
            resolverId.name = resolver.getName();
            if (resolver instanceof AbstractResolver) {
                resolverId.type = ((AbstractResolver) resolver).getTypeName();
            } else {
                resolverId.type = resolver.getClass().getName();
            }
            if (resolver instanceof URLResolver) {
                URLRepository repository = (URLRepository) ((URLResolver) resolver).getRepository();
                resolverId.url = repository.getName();
            } else if (resolver instanceof FileSystemResolver) {
                FileSystemResolver repository = (FileSystemResolver) ((FileSystemResolver) resolver).getRepository();
                resolverId.url = repository.getArtifactPatterns().get(0).toString();
            }
            return this;
        }

        public ResolverId build() {
            return resolverId;
        }
    }
}
