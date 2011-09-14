package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.regex.NamedMatcher;
import org.jfrog.wharf.layout.regex.NamedPattern;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.*;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.*;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.REVISION_PATTERN;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.generateNamedRegexFromLayoutPattern;

/**
 * Date: 9/13/11
 * Time: 5:03 PM
 *
 * @author Fred Simon
 */
public class AnyRevisionFieldProvider extends BaseFieldProvider {
    protected NamedPattern namedPattern;

    public AnyRevisionFieldProvider(FieldDefinition fieldDefinition) {
        super(fieldDefinition);
        String regex = generateNamedRegexFromLayoutPattern(REVISION_PATTERN, true);
        namedPattern = NamedPattern.compile(regex);
    }

    /**
     * Analyzed the fields to find out if it's an integration version.
     * NOTE: The status field is not the only one providing this info!
     * TODO: May be unifying the status field is a good idea? Since status!=integration can still returns integration=true is very confusing
     *
     * @param from the artifact fields
     * @return true if declared integration, false otherwise
     */
    public boolean isIntegration(Map<String, String> from) {
        return STATUS_INTEGRATION.equals(from.get(status.id()))
                || isNotBlank(from.get(fileItegRev.id()))
                || isNotBlank(from.get(folderItegRev.id()));
    }

    @Override
    public void populate(Map<String, String> from) {
        // All the version fields need to be coherent
        String currentRevision = from.get(revision.id());
        String currentBaseRev = from.get(baseRev.id());

        if (isBlank(currentBaseRev) && isBlank(currentRevision)) {
            // log.warn("Could not find version if baseRev and revision are empty");
            return;
        }

        if (isBlank(currentRevision)) {
            populateFromBaseRevision(from);
        } else if (from.get(status.id()) == null || (isIntegration(from) && from.get(fileItegRev.id()) == null)) {
            populateFromRegex(from);
        }
    }

    private void populateFromRegex(Map<String, String> from) {
        String currentRevision = from.get(revision.id());
        String currentBaseRev = from.get(baseRev.id());

        NamedMatcher matcher = namedPattern.matcher(currentRevision);
        if (matcher.matches()) {
            Map<String, String> foundMap = matcher.namedGroups();

            String foundBaseRev = convertToValidField(foundMap.get(baseRev.id()));
            if (currentBaseRev == null) {
                from.put(baseRev.id(), foundBaseRev);
            }

            String fileIntegrationVersion = convertToValidField(foundMap.get(fileItegRev.id()));
            if (from.get(fileItegRev.id()) == null) {
                from.put(fileItegRev.id(), fileIntegrationVersion);
            }

            if (from.get(status.id()) == null) {
                if (isNotBlank(fileIntegrationVersion)) {
                    from.put(status.id(), STATUS_INTEGRATION);
                } else {
                    from.put(status.id(), STATUS_RELEASE);
                }
            }
        }
    }

    private void populateFromBaseRevision(Map<String, String> from) {
        String currentStatus = from.get(status.id());
        if (isIntegration(from)) {
            if (currentStatus == null) {
                from.put(status.id(), STATUS_INTEGRATION);
            }
            if (from.get(folderItegRev.id()) == null) {
                from.put(folderItegRev.id(), SNAPSHOT);
            }
            if (from.get(fileItegRev.id()) == null) {
                from.put(fileItegRev.id(), SNAPSHOT);
            }
            from.put(revision.id(), getRevisionFromBaseAndIntegration(from));
        } else {
            if (currentStatus == null) {
                from.put(status.id(), STATUS_RELEASE);
            }
            from.put(folderItegRev.id(), "");
            from.put(fileItegRev.id(), "");
            from.put(revision.id(), from.get(baseRev.id()));
        }
    }

    protected String getRevisionFromBaseAndIntegration(Map<String, String> from) {
        // TODO: Use the regex to generate the revision
        return from.get(baseRev.id()) + "-" + from.get(fileItegRev.id());
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        if (!super.isValid(from)) {
            return false;
        }
        if (isIntegration(from)) {
            return !STATUS_RELEASE.equals(from.get(status.id()))
                    && isNotBlank(from.get(fileItegRev.id()))
                    && SNAPSHOT.equals(from.get(folderItegRev.id()))
                    && StringUtils.equals(getRevisionFromBaseAndIntegration(from), from.get(revision.id()));
        } else {
            return !STATUS_INTEGRATION.equals(from.get(status.id()))
                    && isBlank(from.get(fileItegRev.id()))
                    && isBlank(from.get(folderItegRev.id()))
                    && StringUtils.equals(from.get(baseRev.id()), from.get(revision.id()));
        }
    }

}
