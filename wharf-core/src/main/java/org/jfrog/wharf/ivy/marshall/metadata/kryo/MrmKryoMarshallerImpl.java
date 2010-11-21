package org.jfrog.wharf.ivy.marshall.metadata.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.marshall.metadata.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * @author Tomer Cohen
 */
public class MrmKryoMarshallerImpl implements MrmMarshaller {

    @Override
    public ModuleRevisionMetadata getModuleRevisionMetadata(File file) {
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                Kryo kryo = new Kryo();
                kryo.register(ModuleRevisionMetadata.class);
                kryo.register(HashSet.class);
                kryo.register(ArtifactMetadata.class);
                ObjectBuffer buffer = new ObjectBuffer(kryo);
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
            Kryo kryo = new Kryo();
            kryo.register(ModuleRevisionMetadata.class);
            kryo.register(ArtifactMetadata.class);
            kryo.register(HashSet.class);
            ObjectBuffer buffer = new ObjectBuffer(kryo);
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
}
