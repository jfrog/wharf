/*
 *
 *  Copyright (C) 2010 JFrog Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.jfrog.wharf.ivy.marshall.jackson;


import org.apache.ivy.util.Message;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.jfrog.wharf.ivy.marshall.api.MrmMarshaller;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.*;

/**
 * @author Tomer Cohen
 */
public class MrmJacksonMarshallerImpl implements MrmMarshaller {

    private static final String DEFAULT_DATA_FILE_PATTERN =
            "[organisation]/[module](/[branch])/wharfdata-[revision].json";

    @Override
    public ModuleRevisionMetadata getModuleRevisionMetadata(File file) {
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                JsonParser jsonParser = JacksonFactory.createJsonParser(inputStream);
                return jsonParser.readValueAs(ModuleRevisionMetadata.class);
            } catch (IOException e) {
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
        } else {
            Message.debug("File: " + file.getAbsolutePath() + " was not found");
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

    @Override
    public String getDataFilePattern() {
        return DEFAULT_DATA_FILE_PATTERN;
    }
}
