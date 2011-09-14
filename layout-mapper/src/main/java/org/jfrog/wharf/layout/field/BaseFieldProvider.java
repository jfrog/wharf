package org.jfrog.wharf.layout.field;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Date: 9/11/11
 * Time: 6:37 PM
 *
 * @author Fred Simon
 */
public class BaseFieldProvider implements FieldValueProvider {
    private final FieldDefinition fieldDefinition;

    public BaseFieldProvider(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public final String id() {
        return fieldDefinition.id();
    }

    @Override
    public String[] fieldNames() {
        return fieldDefinition.fieldNames();
    }

    @Override
    public boolean isMandatory() {
        return fieldDefinition.isMandatory();
    }

    @Override
    public boolean isValid(Map<String, String> from) {
        return !isMandatory() || StringUtils.isNotBlank(from.get(id()));
    }

    @Override
    public void populate(Map<String, String> from) {
        // Nothing by default
    }

    @Override
    public String defaultRegex() {
        // By default anything but /
        return "[^/]+";
    }
}
