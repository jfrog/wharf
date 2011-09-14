package org.jfrog.wharf.layout.field;

import java.util.Map;

/**
 * Date: 9/14/11
 * Time: 5:03 PM
 *
 * @author Fred Simon
 */
public interface VersionFieldPopulator {
    boolean isIntegration(Map<String, String> from);

    void populateFromRevision(Map<String, String> from);

    void populateFromBaseRevision(Map<String, String> from);

    String getRevisionFromBaseAndIntegration(Map<String, String> from);
}
