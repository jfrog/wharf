package org.jfrog.wharf.layout.field.provider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ArtifactFields.artifact;
import static org.jfrog.wharf.layout.field.definition.ModuleFields.module;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class ModuleFieldProvider extends BaseFieldProvider {

    public ModuleFieldProvider() {
        super(module);
    }

    @Override
    public void populate(Map<String, String> from) {
        from.put(id(), from.get(artifact.id()));
    }
}
