package org.jfrog.wharf.layout.field.maven;

import org.jfrog.wharf.layout.field.VersionFieldPopulator;
import org.jfrog.wharf.layout.field.provider.AnyRevisionFieldProvider;

import java.util.Map;

import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.fileItegRev;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class MavenFileIntegrationRevisionFieldProvider extends AnyRevisionFieldProvider {

    public MavenFileIntegrationRevisionFieldProvider(VersionFieldPopulator versionPopulator) {
        super(fileItegRev, versionPopulator);
    }

    @Override
    public void populate(Map<String, String> from) {
        super.populate(from);
        if (from.get(id()) == null && getVersionFieldPopulator().isIntegration(from)) {
            from.put(id(), SNAPSHOT);
        }
    }
}
