package org.jfrog.wharf.ivy.marshall.api;

/**
 * @author Tomer Cohen
 */
public interface MarshallerProvider {
    public MrmMarshaller getMetadataMarshaller();

    public WharfResolverMarshaller getWharfResolverMarshaller();
}
