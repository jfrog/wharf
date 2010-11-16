package org.jfrog.wharf.ivy.marshall;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.jfrog.wharf.ivy.model.WharfResolver;

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
public class WharfResolverMarshaller {

    private File resolversFile;
    private Set<WharfResolver> wharfResolvers = new HashSet<WharfResolver>();
    private static final String RESOLVERS_FILE_PATH = ".wharf/resolvers.json";

    public WharfResolverMarshaller(File baseDir) {
        this.resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
        if (baseDir.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(resolversFile);
                JsonParser jsonParser = JacksonFactory.createJsonParser(stream);
                wharfResolvers = jsonParser.readValueAs(Set.class);
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

    public void save() {
        OutputStream stream = null;
        try {
            File dir = resolversFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            stream = new FileOutputStream(resolversFile);
            JsonGenerator generator = JacksonFactory.createJsonGenerator(stream);
            generator.writeObject(wharfResolvers);
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

    public Set<WharfResolver> getWharfResolvers() {
        return wharfResolvers;
    }
}
