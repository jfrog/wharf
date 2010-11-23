package org.jfrog.wharf.ivy.marshall.factory;


import org.jfrog.wharf.ivy.marshall.metadata.MrmMarshaller;
import org.jfrog.wharf.ivy.marshall.resolver.WharfResolverMarshaller;

/**
 * @author Tomer Cohen
 */
public abstract class MarshallerFactory {

    private MarshallerFactory() {

    }

    public static MrmMarshaller createMetadataMarshaller() {
        Class<? extends MrmMarshaller> marshallerClass;
        try {
            marshallerClass =
                    (Class<? extends MrmMarshaller>) Class
                            .forName("org.jfrog.wharf.ivy.marshall.metadata.Jackson.MrmMarshallerImpl");
        } catch (ClassNotFoundException e) {
            try {
                marshallerClass =
                        (Class<? extends MrmMarshaller>) Class
                                .forName("org.jfrog.wharf.ivy.marshall.metadata.kryo.MrmKryoMarshallerImpl");
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("No metadata marshaller implementation was found in classpath");
            }
        }
        try {
            return marshallerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static WharfResolverMarshaller createWharfResolverMarshaller() {
        Class<? extends WharfResolverMarshaller> marshallerClass;
        try {
            marshallerClass =
                    (Class<? extends WharfResolverMarshaller>) Class.forName(
                            "org.jfrog.wharf.ivy.marshall.resolver.jackson.WharfJacksonResolverMarshallerImpl");
        } catch (ClassNotFoundException e) {
            try {
                marshallerClass =
                        (Class<? extends WharfResolverMarshaller>) Class
                                .forName("org.jfrog.wharf.ivy.marshall.resolver.kryo.WharfKryoResolverMarshaller");
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("No wharf marshaller implementation was found in classpath");

            }
        }
        try {
            return marshallerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
