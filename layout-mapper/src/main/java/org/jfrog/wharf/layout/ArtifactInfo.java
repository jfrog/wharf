package org.jfrog.wharf.layout;

/**
 * Date: 9/11/11
 * Time: 3:33 PM
 *
 * @author Fred Simon
 */
public interface ArtifactInfo extends ModuleRevisionInfo {
    String getArtifactName();

    String getExtension();

    String getType();

    String getClassifier();
}
