package org.jfrog.wharf.layout.field;

import org.jfrog.wharf.layout.base.LayoutUtils;
import org.jfrog.wharf.layout.regex.NamedMatcher;
import org.jfrog.wharf.layout.regex.NamedPattern;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_INTEGRATION;
import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_RELEASE;
import static org.jfrog.wharf.layout.base.LayoutUtils.convertToValidField;
import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.REVISION_PATTERN;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.generateNamedRegexFromLayoutPattern;

/**
 * Date: 9/13/11
 * Time: 5:03 PM
 *
 * @author Fred Simon
 */
public class AbstractRevisionFieldProvider extends BaseFieldProvider {
    protected NamedPattern namedPattern;

    public AbstractRevisionFieldProvider(FieldDefinition fieldDefinition) {
        super(fieldDefinition);
        String regex = generateNamedRegexFromLayoutPattern(REVISION_PATTERN, true);
        namedPattern = NamedPattern.compile(regex);
    }

    protected boolean isIntegrationVersion(Map<String, String> from) {
        String fileIntegrationVersion = from.get(fileItegRev.id());
        String folderIntegrationVersion = from.get(folderItegRev.id());
        if (isNotBlank(fileIntegrationVersion) || isNotBlank(folderIntegrationVersion)) {
            return true;
        }
        String statusValue = from.get(status.id());
        if (isNotBlank(statusValue)) {
            return STATUS_INTEGRATION.equals(statusValue);
        }
        String currentRevision = from.get(revision.id());
        if (isNotBlank(currentRevision)) {
            if (currentRevision.endsWith(LayoutUtils.SNAPSHOT_SUFFIX)) {
                return true;
            }
            NamedMatcher matcher = namedPattern.matcher(currentRevision);
            if (matcher.matches()) {
                Map<String, String> map = matcher.namedGroups();
                if (isBlank(from.get(baseRev.id()))) {
                    from.put(baseRev.id(), convertToValidField(map.get(baseRev.id())));
                }
                fileIntegrationVersion = convertToValidField(map.get(fileItegRev.id()));
                from.put(fileItegRev.id(), fileIntegrationVersion);
                if (isNotBlank(fileIntegrationVersion)) {
                    from.put(status.id(), STATUS_INTEGRATION);
                } else {
                    from.put(status.id(), STATUS_RELEASE);
                }
                return true;
            }
        }
        return false;
    }

}
