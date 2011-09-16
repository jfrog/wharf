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

package org.jfrog.wharf.ivy.marshall.api;


import org.jfrog.wharf.ivy.lock.LockHolderFactory;
import org.jfrog.wharf.ivy.marshall.jackson.JacksonMarshallerProvider;
import org.jfrog.wharf.ivy.marshall.kryo.KryoMarshallerProvider;

/**
 * @author Tomer Cohen
 */
public abstract class MarshallerFactory {

    private static final String WHARF_MARSHALL_TYPE = "wharf.marshallType";

    private MarshallerFactory() {
        // utility class
    }

    /**
     * @return kryo or jackson depending on wharf.marshallType property, the default is kryo.
     */
    public static String getMarshallerType() {
        return System.getProperty(WHARF_MARSHALL_TYPE, "kryo");
    }

    public static MarshallerProvider getMarshallerProvider(LockHolderFactory lockFactory) {
        String marshallerType = getMarshallerType();
        if ("kryo".equals(marshallerType)) {
            return new KryoMarshallerProvider(lockFactory);
        } else if ("jackson".equals(marshallerType)) {
            return new JacksonMarshallerProvider();
        } else {
            // The marshallerType is the full class name of the MarshallerProvider implementation
            try {
                return ((Class<? extends MarshallerProvider>) Class.forName(marshallerType)).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static MrmMarshaller createMetadataMarshaller(LockHolderFactory lockFactory) {
        return getMarshallerProvider(lockFactory).getMetadataMarshaller();
    }

    public static WharfResolverMarshaller createWharfResolverMarshaller(LockHolderFactory lockFactory) {
        return getMarshallerProvider(lockFactory).getWharfResolverMarshaller();
    }
}
