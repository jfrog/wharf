package org.jfrog.wharf.layout.field.definition;

import org.jfrog.wharf.layout.field.FieldDefinition;

/**
 * Date: 9/11/11
 * Time: 6:44 PM
 *
 * @author Fred Simon
 */
public class DefaultFieldDefinition implements FieldDefinition {
    private final String[] ids;
    private final boolean mandatory;

    public DefaultFieldDefinition(boolean mandatory, String id, String... altNames) {
        this.mandatory = mandatory;
        this.ids = new String[altNames.length + 1];
        ids[0] = id;
        System.arraycopy(altNames, 0, ids, 1, altNames.length);
    }

    @Override
    public String id() {
        return ids[0];
    }

    @Override
    public String[] fieldNames() {
        return ids;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }
}
