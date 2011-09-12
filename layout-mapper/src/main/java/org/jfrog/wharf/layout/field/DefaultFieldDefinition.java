package org.jfrog.wharf.layout.field;

/**
 * Date: 9/11/11
 * Time: 6:44 PM
 *
 * @author Fred Simon
 */
public class DefaultFieldDefinition implements FieldDefinition {
    final String[] ids;
    final FieldValueProvider converter;

    public DefaultFieldDefinition(String id, FieldValueProvider converter, String... altNames) {
        if (converter != null) {
            this.converter = converter;
        } else {
            this.converter = new BaseFieldProvider(this);
        }
        ids = new String[altNames.length + 1];
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
    public FieldValueProvider provider() {
        return converter;
    }

    @Override
    public String defaultRegex() {
        return ".*";
    }
}
