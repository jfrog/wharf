package org.jfrog.wharf.ivy;

import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.resolver.DependencyResolver;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class ResolverHandler implements IvySettingsAware{

    /**
     * This is the directory where you have the right to put all of the needed files for the handler. Default is
     * cacheDir/.wharf
     */
    private final File baseDir;

    private final Map<Integer, WharfResolver> resolvers = new HashMap<Integer, WharfResolver>();
    private final Map<Integer, WharfResolver> resolverFromDependencyResolverHash =
            new HashMap<Integer, WharfResolver>();
    private CachedResolversFile cachedResolversFile;
    private IvySettings settings;

    public ResolverHandler(File baseDir) {
        this.baseDir = baseDir;
        // populate the set of resolvers from the baseDir/resolvers.json file
        if (cachedResolversFile == null) {
            cachedResolversFile = new CachedResolversFile(baseDir);
            Set<WharfResolver> resolverIds = cachedResolversFile.getWharfResolvers();
            for (WharfResolver wharfResolver : resolverIds) {
                resolvers.put(wharfResolver.hashCode(), wharfResolver);
            }
        }
    }

    /**
     * @return Get a resolver ID according
     */
    public WharfResolver getResolver(DependencyResolver resolver) {
        // find in shortcut
        int hash = resolver.hashCode();
        if (resolverFromDependencyResolverHash.containsKey(hash)) {
            return resolverFromDependencyResolverHash.get(hash);
        }
        // Need to find if in my cache then save to json file and shortcut
        WharfResolver wharfResolver = new WharfResolver(resolver);
        resolverFromDependencyResolverHash.put(hash, wharfResolver);
        saveCacheResolverFile();
        return wharfResolver;
    }

    /**
     * @return set of all resolvers Ids ever used in this cache
     */
    public Collection<WharfResolver> getAllResolvers() {
        return resolvers.values();
    }

    public WharfResolver getResolver(int hash) {
        return resolvers.get(hash);
    }

    public void saveCacheResolverFile() {
        cachedResolversFile.save();
    }

    public void setSettings(IvySettings settings) {
        this.settings = settings;
    }

    public boolean contains(int resolverIdHashCode) {
        return resolvers.containsKey(resolverIdHashCode);
    }

    public void removeResolver(int resolverIdHashCode) {
        if (resolvers.containsKey(resolverIdHashCode)) {
            resolvers.remove(resolverIdHashCode);
            resolverFromDependencyResolverHash.remove(resolverIdHashCode);
            saveCacheResolverFile();
        }
    }
}
