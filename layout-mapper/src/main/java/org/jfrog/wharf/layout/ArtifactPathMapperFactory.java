package org.jfrog.wharf.layout;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 4:18 PM
 *
 * @author Fred Simon
 */
public interface ArtifactPathMapperFactory {
    ArtifactPathMapper create(Map<String, String> configParams);

    ArtifactPathMapper createMavenMapper(String rootPath);
}
