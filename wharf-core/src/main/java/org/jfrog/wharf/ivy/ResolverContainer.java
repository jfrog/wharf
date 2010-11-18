package org.jfrog.wharf.ivy;


import org.apache.ivy.plugins.resolver.DependencyResolver;

/**
 * @author Tomer Cohen
 */
public class ResolverContainer {

    private static DependencyResolver containedResolver;

    public static void setResolver(DependencyResolver dependencyResolver) {
        containedResolver = dependencyResolver;
    }

    public static DependencyResolver getResolver() {
        return containedResolver;
    }

    public static void clear() {
        if (containedResolver != null) {
            containedResolver = null;
        }
    }
}
