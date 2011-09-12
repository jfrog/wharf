package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.base.LayoutUtils;

import java.util.Map;

import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_INTEGRATION;
import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_RELEASE;
import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class StatusFieldProvider extends BaseFieldProvider {

    public StatusFieldProvider() {
        super(status);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        String fileIntegrationVersion = from.get(fileItegRev.id());
        if (StringUtils.isNotBlank(fileIntegrationVersion)) {
            return STATUS_INTEGRATION;
        }
        String currentRevision = from.get(revision.id());
        if (StringUtils.isNotBlank(currentRevision)) {
            if (currentRevision.endsWith(LayoutUtils.SNAPSHOT_SUFFIX)) {
                return STATUS_INTEGRATION;
            }
        }
        return STATUS_RELEASE;
    }
}
