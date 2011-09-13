package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.base.LayoutUtils;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT_SUFFIX;
import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class BaseRevisionFieldProvider extends AbstractRevisionFieldProvider {

    public BaseRevisionFieldProvider() {
        super(baseRev);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        if (!isIntegrationVersion(from)) {
            return from.get(revision.id());
        }
        if (isNotBlank(from.get(baseRev.id()))) {
            return from.get(baseRev.id());
        }
        String currentRevision = from.get(revision.id());
        if (isNotBlank(currentRevision)) {
            String fileIntegrationVersion = from.get(fileItegRev.id());
            if (isNotBlank(fileIntegrationVersion)) {
                if (!currentRevision.endsWith(fileIntegrationVersion)) {
                    throw new IllegalStateException("Module revision object definition based on " +
                            LayoutUtils.mapToString(from) + "\n" +
                            "Due to revision='" + currentRevision + "' does not end with " +
                            "fileItegRev='" + fileIntegrationVersion + "'"
                    );
                }
                return currentRevision.substring(0, currentRevision.length() - (fileIntegrationVersion.length() + 1));
            }
            if (currentRevision.endsWith(SNAPSHOT_SUFFIX)) {
                return currentRevision.substring(0, currentRevision.length() - SNAPSHOT_SUFFIX.length());
            }
            return currentRevision;
        }
        return "";
    }
}
