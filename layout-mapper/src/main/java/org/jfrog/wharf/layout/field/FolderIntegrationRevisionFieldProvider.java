package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.folderItegRev;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class FolderIntegrationRevisionFieldProvider extends AnyRevisionFieldProvider {

    public FolderIntegrationRevisionFieldProvider() {
        super(folderItegRev);
    }

    @Override
    public void populate(Map<String, String> from) {
        super.populate(from);
        if (from.get(id()) == null && isIntegration(from)) {
            from.put(id(), SNAPSHOT);
        }
    }
}
