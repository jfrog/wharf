package org.jfrog.wharf.ivy.cache;

import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.marshall.resolver.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.marshall.resolver.jackson.WharfResolverMarshallerImpl;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class ResolverHandler implements IvySettingsAware {

    /**
     * This is the directory where you have the right to put all of the needed files for the handler. Default is
     * cacheDir/.wharf
     */
    private final File baseDir;

    private final Map<Integer, WharfResolverMetadata> resolvers = new HashMap<Integer, WharfResolverMetadata>();
    private final Map<Integer, WharfResolverMetadata> resolverFromDependencyResolverHash =
            new HashMap<Integer, WharfResolverMetadata>();
    private WharfResolverMarshaller wharfResolverMarshaller = new WharfResolverMarshallerImpl();
    private IvySettings settings;
    private static final WharfResolverMetadata LOCAL_WHARF_METADATA = new WharfResolverMetadata("local-wharf", "wharf");

    public ResolverHandler(File baseDir) {
        this.baseDir = baseDir;
        // populate the set of resolvers from the baseDir/resolvers.json file
        Set<WharfResolverMetadata> resolverMetadataIds = wharfResolverMarshaller.getWharfMetadatas(baseDir);
        for (WharfResolverMetadata wharfResolverMetadata : resolverMetadataIds) {
            resolvers.put(wharfResolverMetadata.hashCode(), wharfResolverMetadata);
        }
    }

    /**
     * @return Get a resolver ID according
     */
    public WharfResolverMetadata getResolver(DependencyResolver resolver) {
        // find in shortcut
        if (resolver == null) {
            throw new IllegalArgumentException("Cannot find null resolver");
        }
        int hash = resolver.hashCode();
        if (resolverFromDependencyResolverHash.containsKey(hash)) {
            return resolverFromDependencyResolverHash.get(hash);
        }
        // Need to find if in my cache then save to json file and shortcut
        WharfResolverMetadata wharfResolverMetadata = new WharfResolverMetadata(resolver);
        resolverFromDependencyResolverHash.put(hash, wharfResolverMetadata);
        resolvers.put(wharfResolverMetadata.hashCode(), wharfResolverMetadata);
        saveCacheResolverFile();
        return wharfResolverMetadata;
    }

    public WharfResolverMetadata getLocalResolver() {
        if (!resolvers.containsKey(LOCAL_WHARF_METADATA.hashCode())) {
            try {
                LOCAL_WHARF_METADATA.url = baseDir.toURI().toURL().toExternalForm();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            resolvers.put(LOCAL_WHARF_METADATA.hashCode(), LOCAL_WHARF_METADATA);
        }
        return LOCAL_WHARF_METADATA;
    }

    public void cleanResolvers() {
        resolvers.clear();
        getLocalResolver();
        saveCacheResolverFile();
    }

    /**
     * @return set of all resolvers Ids ever used in this cache
     */
    public Collection<WharfResolverMetadata> getAllResolvers() {
        return resolvers.values();
    }

    public WharfResolverMetadata getResolver(int resolverId) {
        return resolvers.get(resolverId);
    }

    public void saveCacheResolverFile() {
        wharfResolverMarshaller.save(baseDir, new HashSet<WharfResolverMetadata>(getAllResolvers()));
    }

    public void setSettings(IvySettings settings) {
        this.settings = settings;
    }

    public boolean isActiveResolver(int resolverId) {
        WharfResolverMetadata resolverMetadata = getResolver(resolverId);
        if (resolverMetadata.equals(getLocalResolver())) {
            return true;
        }
        if (resolverMetadata == null) {
            Message.error("No resolver for " + resolverId + " This cannot happen, please check cache corruption");
            return false;
        }
        if (settings.getResolverNames().contains(resolverMetadata.name)) {
            int currentResolverId = new WharfResolverMetadata(settings.getResolver(resolverMetadata.name)).getId();
            return currentResolverId == resolverId;
        }
        return false;
    }

    public boolean contains(int resolverId) {
        return resolvers.containsKey(resolverId);
    }

    public void removeResolver(int resolverIdHashCode) {
        if (resolvers.containsKey(resolverIdHashCode)) {
            resolvers.remove(resolverIdHashCode);
            resolverFromDependencyResolverHash.remove(resolverIdHashCode);
            saveCacheResolverFile();
        }
    }
}
