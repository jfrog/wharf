package org.jfrog.wharf.layout.field;

import java.util.Map;

/**
* Date: 9/11/11
* Time: 6:37 PM
*
* @author Fred Simon
*/
public class EqualConverter implements FieldConverter {
    final FieldDefinition fieldDefinition;

    public EqualConverter(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public String value(Map<String, String> from) {
        return from.get(fieldDefinition.id());
    }
}
