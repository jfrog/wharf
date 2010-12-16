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

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson generator factory class
 *
 * @author Noam Tenne
 */
abstract class JacksonFactory {
    private JacksonFactory() {
        // utility class
    }

    /**
     * Creates a JsonGenerator using the given output stream as a writer
     *
     * @param outputStream Stream to write to
     * @return Json Generator
     * @throws IOException
     */
    public static JsonGenerator createJsonGenerator(OutputStream outputStream) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
        updateGenerator(jsonFactory, jsonGenerator);
        return jsonGenerator;
    }

    /**
     * Creates a JsonParser using the given input stream as a reader.
     *
     * @param inputStream Stream to read from
     * @return Json Parser
     * @throws IOException
     */
    public static JsonParser createJsonParser(InputStream inputStream) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
        updateParser(jsonFactory, jsonParser);
        return jsonParser;
    }


    /**
     * Create the JSON factory
     *
     * @return JSON factory
     */
    private static JsonFactory getFactory() {
        JsonFactory jsonFactory = new JsonFactory();
        //Do not auto-close target output when writing completes
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        //Do not auto-close source output when reading completes
        jsonFactory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        return jsonFactory;
    }

    /**
     * Update the generator with a default codec and pretty printer
     *
     * @param jsonFactory   Factory to set as codec
     * @param jsonGenerator Generator to configure
     */
    private static void updateGenerator(JsonFactory jsonFactory, JsonGenerator jsonGenerator) {
        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        //Update the annotation interceptor to also include jaxb annotations as a second choice
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        mapper.getSerializationConfig().setAnnotationIntrospector(primary);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        jsonGenerator.setCodec(mapper);
        jsonGenerator.useDefaultPrettyPrinter();
    }

    /**
     * Update the parser with a default codec
     *
     * @param jsonFactory Factory to set as codec
     * @param jsonParser  Parser to configure
     */
    private static void updateParser(JsonFactory jsonFactory, JsonParser jsonParser) {
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.getSerializationConfig().setAnnotationIntrospector(primary);
        mapper.getDeserializationConfig().setAnnotationIntrospector(primary);
        mapper.getDeserializationConfig().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonParser.setCodec(mapper);
    }
}
