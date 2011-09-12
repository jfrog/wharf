package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.base.LayoutUtils;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class BaseRevisionFieldProvider extends BaseFieldProvider {

    public BaseRevisionFieldProvider() {
        super(baseRev);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        String currentRevision = from.get(revision.id());
        if (StringUtils.isNotBlank(currentRevision)) {
            String fileIntegrationVersion = from.get(fileItegRev.id());
            if (StringUtils.isNotBlank(fileIntegrationVersion)) {
                if (!currentRevision.endsWith(fileIntegrationVersion)) {
                    throw new IllegalStateException("Module revision object definition based on " +
                            LayoutUtils.mapToString(from) + "\n" +
                            "Due to revision='" + currentRevision + "' does not end with " +
                            "fileItegRev='" + fileIntegrationVersion + "'"
                    );
                }
                return currentRevision.substring(0, currentRevision.length() - (fileIntegrationVersion.length() + 1));
            }
            if (currentRevision.endsWith("-SNAPSHOT")) {
                return currentRevision.substring(0, currentRevision.length() - "-SNAPSHOT".length());
            }
            if ("integration".equals(from.get(status.id()))) {
                // TODO: Need to use fileItegRev regular expression
                throw new UnsupportedOperationException();
            }
            return currentRevision;
        }
        return null;
    }
}
