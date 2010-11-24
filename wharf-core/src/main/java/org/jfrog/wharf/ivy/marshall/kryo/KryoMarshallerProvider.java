package org.jfrog.wharf.ivy.marshall.kryo;


import org.jfrog.wharf.ivy.marshall.api.MarshallerProvider;
import org.jfrog.wharf.ivy.marshall.api.MrmMarshaller;
import org.jfrog.wharf.ivy.marshall.api.WharfResolverMarshaller;

/**
 * @author Tomer Cohen
 */
public class KryoMarshallerProvider implements MarshallerProvider {
    @Override
    public MrmMarshaller getMetadataMarshaller() {
        return new MrmKryoMarshallerImpl();
    }

    @Override
    public WharfResolverMarshaller getWharfResolverMarshaller() {
        return new WharfKryoResolverMarshaller();
    }
}
