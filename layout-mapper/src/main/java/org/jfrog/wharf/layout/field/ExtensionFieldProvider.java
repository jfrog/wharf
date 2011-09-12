package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ArtifactFields.*;

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
    public String extractFromOthers(Map<String, String> from) {
        if (POM.equals(from.get(type.id()))) {
            return POM;
        }
        if (IVY.equals(from.get(type.id())) || IVY.equals(from.get(artifact.id()))) {
            return "xml";
        }
        return "jar";
    }
}
