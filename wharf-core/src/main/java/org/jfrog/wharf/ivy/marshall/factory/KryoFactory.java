package org.jfrog.wharf.ivy.marshall.factory;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public abstract class KryoFactory {

    private KryoFactory() {
    }

    public static ObjectBuffer createWharfResolverObjectBuffer(Class<WharfResolverMetadata> wharfResolverClazz) {
        Kryo kryo = new Kryo();
        kryo.register(wharfResolverClazz);
        kryo.register(Set.class);
        kryo.register(Map.class);
        kryo.register(String[].class);
        ObjectBuffer buffer = new ObjectBuffer(kryo);
        return buffer;
    }

    public static ObjectBuffer createModuleRevisionMetadataObjectBuffer(Class<ModuleRevisionMetadata> mrmClazz) {
        Kryo kryo = new Kryo();
        kryo.register(mrmClazz);
        kryo.register(ArtifactMetadata.class);
        kryo.register(HashSet.class);
        ObjectBuffer buffer = new ObjectBuffer(kryo);
        return buffer;
    }
}
