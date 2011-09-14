package org.jfrog.wharf.layout.field.provider;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.field.FieldDefinition;
import org.jfrog.wharf.layout.field.VersionFieldPopulator;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.*;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.*;

/**
 * Date: 9/13/11
 * Time: 5:03 PM
 *
 * @author Fred Simon
 */
public class AnyRevisionFieldProvider extends BaseFieldProvider {
    private final VersionFieldPopulator versionFieldPopulator;

    public AnyRevisionFieldProvider(FieldDefinition fieldDefinition, VersionFieldPopulator versionFieldPopulator) {
        super(fieldDefinition);
        this.versionFieldPopulator = versionFieldPopulator;
    }

    public VersionFieldPopulator getVersionFieldPopulator() {
        return versionFieldPopulator;
    }

    @Override
    public void populate(Map<String, String> from) {
        if (isBlank(from.get(baseRev.id())) && isBlank(from.get(revision.id()))) {
            // log.warn("Could not find version if baseRev and revision are empty");
            return;
        }

        if (isNotBlank(from.get(revision.id()))) {
            versionFieldPopulator.populateFromRevision(from);
        } else {
            versionFieldPopulator.populateFromBaseRevision(from);
        }
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        if (!super.isValid(from)) {
            return false;
        }
        if (versionFieldPopulator.isIntegration(from)) {
            return !STATUS_RELEASE.equals(from.get(status.id()))
                    && isNotBlank(from.get(fileItegRev.id()))
                    && SNAPSHOT.equals(from.get(folderItegRev.id()))
                    && StringUtils.equals(versionFieldPopulator.getRevisionFromBaseAndIntegration(from), from.get(revision.id()));
        } else {
            return !STATUS_INTEGRATION.equals(from.get(status.id()))
                    && isBlank(from.get(fileItegRev.id()))
                    && isBlank(from.get(folderItegRev.id()))
                    && StringUtils.equals(from.get(baseRev.id()), from.get(revision.id()));
        }
    }

}
