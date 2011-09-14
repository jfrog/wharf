package org.jfrog.wharf.layout.field.maven;

import org.jfrog.wharf.layout.field.VersionFieldPopulator;
import org.jfrog.wharf.layout.field.provider.AnyRevisionFieldProvider;

import java.util.Map;

import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.folderItegRev;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class MavenFolderIntegrationRevisionFieldProvider extends AnyRevisionFieldProvider {

    public MavenFolderIntegrationRevisionFieldProvider(VersionFieldPopulator versionPopulator) {
        super(folderItegRev, versionPopulator);
    }

    @Override
    public void populate(Map<String, String> from) {
        super.populate(from);
        if (from.get(id()) == null && getVersionFieldPopulator().isIntegration(from)) {
            from.put(id(), SNAPSHOT);
        }
    }
}
