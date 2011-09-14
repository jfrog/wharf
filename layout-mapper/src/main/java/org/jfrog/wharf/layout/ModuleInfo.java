package org.jfrog.wharf.layout;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 6:16 PM
 *
 * @author Fred Simon
 */
public interface ModuleInfo extends Map<String, String> {

    String[] getSerializableFields();

    void populate();

    boolean isValid();

    String getGroup();

    String getGroupPath();

    String getModuleName();
}
