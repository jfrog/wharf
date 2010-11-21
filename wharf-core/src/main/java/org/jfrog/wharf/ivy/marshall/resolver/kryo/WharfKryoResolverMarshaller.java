package org.jfrog.wharf.ivy.marshall.resolver.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import org.jfrog.wharf.ivy.marshall.resolver.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfKryoResolverMarshaller implements WharfResolverMarshaller {

    @Override
    public void save(File baseDir, Set<WharfResolverMetadata> wharfResolverMetadatas) {
        File resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
        if (resolversFile.exists()) {
            OutputStream stream = null;
            try {
                stream = new FileOutputStream(resolversFile);
                Kryo kryo = new Kryo();
                kryo.register(WharfResolverMetadata.class);
                kryo.register(Set.class);
                kryo.register(Map.class);
                kryo.register(String[].class);
                ObjectBuffer buffer = new ObjectBuffer(kryo);
                buffer.writeObject(stream, wharfResolverMetadatas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if ((stream != null)) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    @Override
    public Set<WharfResolverMetadata> getWharfMetadatas(File baseDir) {
        File resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
        if (resolversFile.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(resolversFile);
                Kryo kryo = new Kryo();
                kryo.register(WharfResolverMetadata.class);
                kryo.register(Set.class);
                kryo.register(Map.class);
                kryo.register(String[].class);
                ObjectBuffer buffer = new ObjectBuffer(kryo);
                return buffer.readObject(stream, Set.class);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                if ((stream != null)) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return new HashSet<WharfResolverMetadata>();
    }
}
