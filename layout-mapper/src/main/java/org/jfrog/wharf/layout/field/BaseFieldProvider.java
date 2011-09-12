package org.jfrog.wharf.layout.field;

import java.util.Arrays;
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
        if (value == null || value.length() == 0) {
            return "";
        }
        // All values here should start and ends with a valid path character
        // So, remove all starting and trailing / \ . " "
        char[] illegals = {' ', '/', '\\', '.'};
        Arrays.sort(illegals);
        while (partOf(illegals, value.charAt(0))) {
            value = value.substring(1);
        }
        while (value.length() > 0 && partOf(illegals, value.charAt(value.length()-1))) {
            value = value.substring(0, value.length()-1);
        }
        return value;
    }

    boolean partOf(char[] values, char val) {
        for (char value : values) {
            if (value == val) {
                return true;
            }
        }
        return false;
    }
}
