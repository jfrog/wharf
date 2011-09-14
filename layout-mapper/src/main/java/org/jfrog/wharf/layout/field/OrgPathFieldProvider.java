package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jfrog.wharf.layout.base.LayoutUtils.convertToValidField;
import static org.jfrog.wharf.layout.field.definition.ModuleFields.org;
import static org.jfrog.wharf.layout.field.definition.ModuleFields.orgPath;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class OrgPathFieldProvider extends BaseFieldProvider {
    public OrgPathFieldProvider() {
        super(orgPath);
    }

    @Override
    public void populate(Map<String, String> from) {
        String orgValue = from.get(org.id());
        if (isNotBlank(orgValue)) {
            from.put(id(), convertToValidField(orgValue.replace('.', '/')));
        }
    }
}
