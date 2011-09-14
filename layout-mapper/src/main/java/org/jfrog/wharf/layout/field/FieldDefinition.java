package org.jfrog.wharf.layout.field;

/**
* Date: 9/11/11
* Time: 6:37 PM
*
* @author Fred Simon
*/
public interface FieldDefinition {
    String id();

    String[] fieldNames();

    boolean isMandatory();
}
