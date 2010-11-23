package org.jfrog.wharf.ivy.marshall.metadata.kryo;


import com.esotericsoftware.kryo.ObjectBuffer;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.marshall.factory.KryoFactory;
import org.jfrog.wharf.ivy.marshall.metadata.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Tomer Cohen
 */
public class MrmKryoMarshallerImpl implements MrmMarshaller {

    private static final String DEFAULT_DATA_FILE_PATTERN =
            "[organisation]/[module](/[branch])/wharfdata-[revision].kryo";

    @Override
    public ModuleRevisionMetadata getModuleRevisionMetadata(File file) {
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                ObjectBuffer buffer =
                        KryoFactory.createModuleRevisionMetadataObjectBuffer(ModuleRevisionMetadata.class);
                return buffer.readObject(inputStream, ModuleRevisionMetadata.class);
            } catch (IOException ioe) {
                Message.error("Error loading module revision metadata file: " + file.getAbsolutePath());
                // Delete the file (send exception if delete impossible) and returns null
                file.delete();
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

    @Override
    public void save(ModuleRevisionMetadata mrm, File file) {
        OutputStream stream = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            stream = new FileOutputStream(file);
            ObjectBuffer buffer = KryoFactory.createModuleRevisionMetadataObjectBuffer(ModuleRevisionMetadata.class);
            buffer.writeObject(stream, mrm);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
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

    @Override
    public String getDataFilePattern() {
        return DEFAULT_DATA_FILE_PATTERN;
    }
}
