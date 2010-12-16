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

package org.jfrog.wharf.ivy.marshall.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.util.HashSet;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
abstract class KryoFactory {

    private KryoFactory() {
    }

    public static ObjectBuffer createWharfResolverObjectBuffer(Class<WharfResolverMetadata> wharfResolverClazz) {
        Kryo kryo = new Kryo();
        kryo.register(wharfResolverClazz);
        kryo.register(HashSet.class);
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
