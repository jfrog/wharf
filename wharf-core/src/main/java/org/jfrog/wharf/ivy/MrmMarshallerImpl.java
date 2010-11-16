package org.jfrog.wharf.ivy;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class MrmMarshallerImpl implements MrmMarshaller {

    public ModuleRevisionMetadata getModuleRevisionMetadata(File file) {
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                JsonParser jsonParser = JacksonFactory.createJsonParser(inputStream);
                return jsonParser.readValueAs(ModuleRevisionMetadata.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

    public void save(ModuleRevisionMetadata mrm, File file) {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            JsonGenerator generator = JacksonFactory.createJsonGenerator(stream);
            generator.writeObject(mrm);
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
