package org.jfrog.wharf.layout.field.provider;

import org.jfrog.wharf.layout.field.provider.BaseFieldProvider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ArtifactFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class ExtensionFieldProvider extends BaseFieldProvider {

    private static final String POM = "pom";
    private static final String IVY = "ivy";

    public ExtensionFieldProvider() {
        super(ext);
    }

    @Override
    public void populate(Map<String, String> from) {
        if (POM.equals(from.get(type.id()))) {
            from.put(id(), POM);
        } else if (IVY.equals(from.get(type.id())) || IVY.equals(from.get(artifact.id()))) {
            from.put(id(), "xml");
        } else {
            from.put(id(), "jar");
        }
    }
}
