package org.jfrog.wharf.ivy.marshall.api;

import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.File;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public interface WharfResolverMarshaller {

    /**
     * The default path where to store <b>ALL</b> resolvers that were ever used by this cache.
     */
    public String getResolversFilePath();


    /**
     * Save the wharf resolver meatadatas as a serialized object.
     *
     * @param baseDir                The basedir of where to store the serialized file.
     * @param wharfResolverMetadatas The object to store.
     */
    public void save(File baseDir, Set<WharfResolverMetadata> wharfResolverMetadatas);

    /**
     * Get the wharf metadatas as a de-serialized object.
     *
     * @param baseDir The base dir that contains the serialized file that represents the object.
     * @return The de-serialised object.
     */
    public Set<WharfResolverMetadata> getWharfMetadatas(File baseDir);
}
