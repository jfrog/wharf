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


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.TypeReference;
import org.jfrog.wharf.ivy.marshall.api.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfJacksonResolverMarshallerImpl implements WharfResolverMarshaller {
    private static final String RESOLVERS_FILE_PATH = ".wharf/resolvers.json";

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
    public String getResolversFilePath() {
        return RESOLVERS_FILE_PATH;
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
