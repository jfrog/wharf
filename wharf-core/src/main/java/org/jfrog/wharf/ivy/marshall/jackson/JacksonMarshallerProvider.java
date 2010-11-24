package org.jfrog.wharf.ivy.marshall.jackson;

import org.jfrog.wharf.ivy.marshall.api.MarshallerProvider;
import org.jfrog.wharf.ivy.marshall.api.MrmMarshaller;
import org.jfrog.wharf.ivy.marshall.api.WharfResolverMarshaller;

/**
 * @author Tomer Cohen
 */
public class JacksonMarshallerProvider implements MarshallerProvider {

    @Override
    public MrmMarshaller getMetadataMarshaller() {
        return new MrmJacksonMarshallerImpl();
    }

    @Override
    public WharfResolverMarshaller getWharfResolverMarshaller() {
        return new WharfJacksonResolverMarshallerImpl();
    }
}
