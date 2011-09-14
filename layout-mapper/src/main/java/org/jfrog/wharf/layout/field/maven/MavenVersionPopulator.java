package org.jfrog.wharf.layout.field.maven;

import org.jfrog.wharf.layout.field.VersionFieldPopulator;
import org.jfrog.wharf.layout.regex.NamedMatcher;
import org.jfrog.wharf.layout.regex.NamedPattern;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.*;
import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT;
import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_RELEASE;
import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.*;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.MAVEN_REVISION_PATTERN;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.generateNamedRegexFromLayoutPattern;

/**
 * Date: 9/14/11
 * Time: 5:08 PM
 *
 * @author Fred Simon
 */
public class MavenVersionPopulator implements VersionFieldPopulator {
    private final NamedPattern namedPattern;

    public MavenVersionPopulator(String versionPattern, Map<String, String> patterns) {
        namedPattern = NamedPattern.compile(generateNamedRegexFromLayoutPattern(MAVEN_REVISION_PATTERN, true));
    }

    /**
     * Analyzed the fields to find out if it's an integration version.
     * NOTE: The status field is not the only one providing this info!
     * TODO: May be unifying the status field is a good idea?
     * TODO: Since status!=integration can still returns integration=true is very confusing
     *
     * @param from the artifact fields
     * @return true if declared integration, false otherwise
     */
    @Override
    public boolean isIntegration(Map<String, String> from) {
        return STATUS_INTEGRATION.equals(from.get(status.id()))
                || isNotBlank(from.get(fileItegRev.id()))
                || isNotBlank(from.get(folderItegRev.id()));
    }

    @Override
    public void populateFromRevision(Map<String, String> from) {
        String currentRevision = from.get(revision.id());

        NamedMatcher matcher = namedPattern.matcher(currentRevision);
        if (matcher.matches()) {
            Map<String, String> foundMap = matcher.namedGroups();

            String foundBaseRev = convertToValidField(foundMap.get(baseRev.id()));
            if (from.get(baseRev.id()) == null) {
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

    @Override
    public void populateFromBaseRevision(Map<String, String> from) {
        if (isIntegration(from)) {
            if (from.get(status.id()) == null) {
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
            if (from.get(status.id()) == null) {
                from.put(status.id(), STATUS_RELEASE);
            }
            from.put(folderItegRev.id(), "");
            from.put(fileItegRev.id(), "");
            from.put(revision.id(), from.get(baseRev.id()));
        }
    }

    @Override
    public String getRevisionFromBaseAndIntegration(Map<String, String> from) {
        // TODO: Use the regex to generate the revision
        return from.get(baseRev.id()) + "-" + from.get(fileItegRev.id());
    }
}
