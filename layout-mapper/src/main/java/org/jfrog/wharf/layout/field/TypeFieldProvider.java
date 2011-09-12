package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ArtifactFields.artifact;
import static org.jfrog.wharf.layout.field.ArtifactFields.ext;
import static org.jfrog.wharf.layout.field.ArtifactFields.type;
import static org.jfrog.wharf.layout.field.ModuleFields.module;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class TypeFieldProvider extends BaseFieldProvider {

    private static final String IVY = "ivy";

    public TypeFieldProvider() {
        super(type);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        if (IVY.equals(from.get(artifact.id()))) {
            return IVY;
        }
        if ("sources".equals(from.get(ArtifactFields.classifier.id()))) {
            return "source";
        }
        return from.get(ext.id());
    }
}
