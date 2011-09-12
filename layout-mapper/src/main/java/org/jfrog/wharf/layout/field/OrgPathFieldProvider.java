package org.jfrog.wharf.layout.field;

import java.util.Map;

import static org.jfrog.wharf.layout.field.ModuleFields.org;
import static org.jfrog.wharf.layout.field.ModuleFields.orgPath;

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
    public String extractFromOthers(Map<String, String> from) {
        return convert(from.get(org.id()).replace('.', '/'));
    }
}
