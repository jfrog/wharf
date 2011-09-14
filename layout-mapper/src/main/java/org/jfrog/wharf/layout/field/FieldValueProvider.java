package org.jfrog.wharf.layout.field;

import java.util.Map;

/**
* Date: 9/11/11
* Time: 6:36 PM
*
* @author Fred Simon
*/
public interface FieldValueProvider extends FieldDefinition {
    /**
     * Called after populating all fields of an ArtifactInfo object.
     * Called only if the field value for this id is null.
     *
     * @param from the map of fields to populate
     */
    void populate(Map<String, String> from);

    /**
     * Verify that the field value is valid.
     * This method should not modify the map.
     *
     * @param from the map of fields of the artifact
     * @return true if the field value (and surrounding field values) are valid and coherent, false otherwise.
     */
    boolean isValid(Map<String, String> from);

    String regex();
}
