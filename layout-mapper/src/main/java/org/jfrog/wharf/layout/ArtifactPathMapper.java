package org.jfrog.wharf.layout;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 3:32 PM
 *
 * @author Fred Simon
 */
public interface ArtifactPathMapper {
    ArtifactInfo fromMap(Map<String, String> map);

    boolean isValid(ArtifactInfo artifact);

    String toPath(ArtifactInfo artifact);

    ArtifactInfo fromPath(String path);
}
