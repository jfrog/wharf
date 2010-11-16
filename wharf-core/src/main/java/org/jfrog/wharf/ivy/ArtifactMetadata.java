package org.jfrog.wharf.ivy;


/**
 * @author Tomer Cohen
 */
public class ArtifactMetadata {
    public int resolverIdHashCode;
    public String id;
    public String location;
    public boolean local;
    public String md5;
    public String sha1;

    public ArtifactMetadata() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArtifactMetadata metadata = (ArtifactMetadata) o;

        if (resolverIdHashCode != metadata.resolverIdHashCode) {
            return false;
        }
        if (!id.equals(metadata.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = resolverIdHashCode;
        result = 31 * result + id.hashCode();
        return result;
    }
}
