package org.jfrog.wharf.layout.field;

import java.util.Map;

/**
* Date: 9/11/11
* Time: 6:36 PM
*
* @author Fred Simon
*/
public interface FieldConverter {
    String value(Map<String, String> from);
}
