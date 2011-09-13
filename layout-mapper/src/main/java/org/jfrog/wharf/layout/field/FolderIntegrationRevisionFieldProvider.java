package org.jfrog.wharf.layout.field;

import org.jfrog.wharf.layout.base.LayoutUtils;
import org.jfrog.wharf.layout.regex.NamedMatcher;
import org.jfrog.wharf.layout.regex.NamedPattern;
import org.jfrog.wharf.layout.regex.RepoLayoutPatterns;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.SNAPSHOT;
import static org.jfrog.wharf.layout.base.LayoutUtils.STATUS_INTEGRATION;
import static org.jfrog.wharf.layout.field.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class FolderIntegrationRevisionFieldProvider extends AbstractRevisionFieldProvider {

    public FolderIntegrationRevisionFieldProvider() {
        super(folderItegRev);
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        if (isIntegrationVersion(from)) {
            return SNAPSHOT;
        }
        return "";
    }
}
