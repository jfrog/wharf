package org.jfrog.wharf.layout;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 3:36 PM
 *
 * @author Fred Simon
 */
public interface ModuleRevisionInfo extends ModuleInfo {

    String getRevision();

    String getBaseRevision();

    String getStatus();
}
