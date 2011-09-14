package org.jfrog.wharf.layout.field.maven;

import org.jfrog.wharf.layout.field.provider.BaseFieldProvider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ArtifactFields.artifact;
import static org.jfrog.wharf.layout.field.definition.ArtifactFields.type;
import static org.jfrog.wharf.layout.field.definition.ModuleFields.module;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class MavenArtifactNameFieldProvider extends BaseFieldProvider {

    public MavenArtifactNameFieldProvider() {
        super(artifact);
    }

    @Override
    public void populate(Map<String, String> from) {
        from.put(id(), from.get(module.id()));
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        return super.isValid(from) && from.get(id()).equals(from.get(module.id()));
    }
}
