package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ArtifactFields.type;
import static org.jfrog.wharf.layout.field.ModuleFields.module;
import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class RevisionFieldProvider extends BaseFieldProvider {

    public RevisionFieldProvider() {
        super(revision);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        String baseRevision = from.get(baseRev.id());
        if (StringUtils.isNotBlank(baseRevision)) {
            String fileIntegrationVersion = from.get(fileItegRev.id());
            if (StringUtils.isNotBlank(fileIntegrationVersion)) {
                return baseRevision + "-" + fileIntegrationVersion;
            }
            if ("integration".equals(from.get(status.id()))) {
                return baseRevision + "-SNAPSHOT";
            }
        }
        return null;
    }
}
