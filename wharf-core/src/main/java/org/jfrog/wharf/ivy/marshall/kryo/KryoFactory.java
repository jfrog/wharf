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
    private static Kryo kryoMridMetadata = null;
    private static Kryo kryoResolver = null;

    private KryoFactory() {
    }

    public static ObjectBuffer createWharfResolverObjectBuffer() {
        return new ObjectBuffer(getKryoResolver());
    }

    private static Kryo getKryoResolver() {
        if (kryoResolver == null) {
            kryoResolver = new Kryo();
            kryoResolver.register(WharfResolverMetadata.class);
            kryoResolver.register(HashSet.class);
            kryoResolver.register(Map.class);
            kryoResolver.register(String[].class);
        }
        return kryoResolver;
    }

    public static ObjectBuffer createModuleRevisionMetadataObjectBuffer() {
        return new ObjectBuffer(getKryoMridMetadata());
    }

    private static Kryo getKryoMridMetadata() {
        if (kryoMridMetadata == null) {
            kryoMridMetadata = new Kryo();
            kryoMridMetadata.register(ModuleRevisionMetadata.class);
            kryoMridMetadata.register(ArtifactMetadata.class);
            kryoMridMetadata.register(HashSet.class);
        }
        return kryoMridMetadata;
    }
}
