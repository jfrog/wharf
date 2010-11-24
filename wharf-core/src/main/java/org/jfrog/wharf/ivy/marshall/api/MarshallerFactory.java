package org.jfrog.wharf.ivy.marshall.api;


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

    public static MarshallerProvider getMarshallerProvider() {
        String marshallerType = getMarshallerType();
        if ("kryo".equals(marshallerType)) {
            return new KryoMarshallerProvider();
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

    public static MrmMarshaller createMetadataMarshaller() {
        return getMarshallerProvider().getMetadataMarshaller();
    }

    public static WharfResolverMarshaller createWharfResolverMarshaller() {
        return getMarshallerProvider().getWharfResolverMarshaller();
    }
}
