package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ArtifactFields.artifact;
import static org.jfrog.wharf.layout.field.ArtifactFields.type;
import static org.jfrog.wharf.layout.field.ModuleFields.module;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class ArtifactNameFieldProvider extends BaseFieldProvider {

    private static final String IVY = "ivy";

    public ArtifactNameFieldProvider() {
        super(artifact);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        if (IVY.equals(from.get(type.id()))) {
            return IVY;
        }
        return from.get(module.id());
    }
}
