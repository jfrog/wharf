package org.jfrog.wharf.layout.field.provider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ArtifactFields.*;

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
    public void populate(Map<String, String> from) {
        if (IVY.equals(from.get(artifact.id()))) {
            from.put(id(), IVY);
        } else if ("sources".equals(from.get(classifier.id()))) {
            from.put(id(), "source");
        } else {
            from.put(id(), from.get(ext.id()));
        }
    }
}
