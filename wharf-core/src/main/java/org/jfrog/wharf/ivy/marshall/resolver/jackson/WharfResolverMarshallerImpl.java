package org.jfrog.wharf.ivy.marshall.resolver.jackson;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.TypeReference;
import org.jfrog.wharf.ivy.marshall.JacksonFactory;
import org.jfrog.wharf.ivy.marshall.resolver.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfResolverMarshallerImpl implements WharfResolverMarshaller {

    @Override
    public Set<WharfResolverMetadata> getWharfMetadatas(File baseDir) {
        File resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
        if (resolversFile.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(resolversFile);
                JsonParser jsonParser = JacksonFactory.createJsonParser(stream);
                return jsonParser.readValueAs(new TypeReference<Set<WharfResolverMetadata>>() {

                });
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
        } else {
            return new HashSet<WharfResolverMetadata>();
        }
    }

    @Override
    public void save(File baseDir, Set<WharfResolverMetadata> wharfResolverMetadatas) {
        File resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
        OutputStream stream = null;
        try {
            File dir = resolversFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            stream = new FileOutputStream(resolversFile);
            JsonGenerator generator = JacksonFactory.createJsonGenerator(stream);
            generator.writeObject(wharfResolverMetadatas);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
