package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ArtifactFields.classifier;
import static org.jfrog.wharf.layout.field.ArtifactFields.type;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class ClassifierFieldProvider extends BaseFieldProvider {

    public ClassifierFieldProvider() {
        super(classifier);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        if ("source".equals(from.get(type.id()))) {
            return "sources";
        }
        return "";
    }
}
