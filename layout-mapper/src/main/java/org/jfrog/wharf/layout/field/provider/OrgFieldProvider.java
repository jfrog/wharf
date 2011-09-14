package org.jfrog.wharf.layout.field.provider;

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
public class OrgFieldProvider extends BaseFieldProvider {
    public OrgFieldProvider() {
        super(org);
    }

    @Override
    public void populate(Map<String, String> from) {
        String orgPathValue = from.get(orgPath.id());
        if (isNotBlank(orgPathValue)) {
            from.put(id(), convertToValidField(orgPathValue.replace('/', '.').replace('\\', '.')));
        }
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        String myVal = from.get(id());
        return isNotBlank(myVal) && !myVal.contains("/");
    }
}
