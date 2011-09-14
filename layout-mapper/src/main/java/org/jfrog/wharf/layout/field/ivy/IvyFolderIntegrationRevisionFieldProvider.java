package org.jfrog.wharf.layout.field.ivy;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.field.VersionFieldPopulator;
import org.jfrog.wharf.layout.field.provider.AnyRevisionFieldProvider;

import java.util.Map;

import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.fileItegRev;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.folderItegRev;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class IvyFolderIntegrationRevisionFieldProvider extends AnyRevisionFieldProvider {

    public IvyFolderIntegrationRevisionFieldProvider(VersionFieldPopulator versionPopulator) {
        super(folderItegRev, versionPopulator);
    }

    @Override
    public void populate(Map<String, String> from) {
        super.populate(from);
        if (from.get(id()) == null && getVersionFieldPopulator().isIntegration(from)) {
            from.put(id(), from.get(fileItegRev.id()));
        }
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        return super.isValid(from) && StringUtils.equals(from.get(id()), from.get(fileItegRev.id()));
    }
}
