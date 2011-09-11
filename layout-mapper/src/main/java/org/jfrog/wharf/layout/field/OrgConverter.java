package org.jfrog.wharf.layout.field;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class OrgConverter implements FieldConverter {
    @Override
    public String value(Map<String, String> from) {
        return from.get(ModuleFields.orgPath.id()).replace('/', '.').replace('\\', '.');
    }
}
