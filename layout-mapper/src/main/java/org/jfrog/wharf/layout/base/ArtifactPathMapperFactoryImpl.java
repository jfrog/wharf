package org.jfrog.wharf.layout.base;

import org.jfrog.wharf.layout.ArtifactPathMapper;
import org.jfrog.wharf.layout.ArtifactPathMapperFactory;

import java.util.Map;

/**
 * Date: 9/12/11
 * Time: 3:10 PM
 *
 * @author Fred Simon
 */
public class ArtifactPathMapperFactoryImpl implements ArtifactPathMapperFactory {
    @Override
    public ArtifactPathMapper create(Map<String, String> configParams) {
        return createMavenMapper(configParams.get("root"));
    }

    @Override
    public ArtifactPathMapper createMavenMapper(String rootPath) {
        return new MavenArtifactPathMapper(rootPath);
    }
}
