package org.jfrog.wharf.layout.field;

import org.jfrog.wharf.layout.base.LayoutUtils;

import java.util.Map;

/**
* Date: 9/11/11
* Time: 6:37 PM
*
* @author Fred Simon
*/
public class BaseFieldProvider implements FieldValueProvider {
    final FieldDefinition fieldDefinition;

    public BaseFieldProvider(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public boolean validate(Map<String, String> from) {
        return true;
    }

    @Override
    public String extractFromOthers(Map<String, String> from) {
        return null;
    }

    @Override
    public String convert(String value) {
        return LayoutUtils.convertToValidField(value);
    }
}
