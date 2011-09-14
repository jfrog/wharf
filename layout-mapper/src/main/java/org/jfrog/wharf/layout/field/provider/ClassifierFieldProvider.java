package org.jfrog.wharf.layout.field.provider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ArtifactFields.classifier;
import static org.jfrog.wharf.layout.field.definition.ArtifactFields.type;

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
    public void populate(Map<String, String> from) {
        if ("source".equals(from.get(type.id()))) {
            from.put(id(), "sources");
        }
    }
}
