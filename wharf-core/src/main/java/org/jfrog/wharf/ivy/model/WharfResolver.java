package org.jfrog.wharf.ivy.model;

import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class WharfResolver {

    public String name;

    public String type;

    public String url;

    public String user;

    public Map<String, String> params;

    public String authentication;

    public String proxy;

    public WharfResolver() {
    }

    public WharfResolver(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public WharfResolver(DependencyResolver resolver) {
        this.name = resolver.getName();
        if (resolver instanceof AbstractResolver) {
            this.type = ((AbstractResolver) resolver).getTypeName();
        } else {
            this.type = resolver.getClass().getName();
        }
        if (resolver instanceof URLResolver) {
            URLRepository repository = (URLRepository) ((URLResolver) resolver).getRepository();
            this.url = repository.getName();
        } else if (resolver instanceof FileSystemResolver) {
            FileSystemResolver fileSystemResolver = (FileSystemResolver) resolver;
            this.url = fileSystemResolver.getArtifactPatterns().get(0).toString();
        }
    }

    public int getId() {
        return hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WharfResolver that = (WharfResolver) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (params != null ? !params.equals(that.params) : that.params != null) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
