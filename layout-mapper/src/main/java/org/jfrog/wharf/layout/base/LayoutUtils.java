package org.jfrog.wharf.layout.base;

import java.util.Arrays;
import java.util.Map;

/**
 * Date: 9/12/11
 * Time: 4:22 PM
 *
 * @author Fred Simon
 */
public abstract class LayoutUtils {
    public static final String STATUS_INTEGRATION = "integration";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String SNAPSHOT_SUFFIX = "-" + SNAPSHOT;
    public static final String STATUS_RELEASE = "release";

    public static String mapToString(Map<String, String> from) {
        StringBuilder builder = new StringBuilder("[");
        for (Map.Entry<String, String> entry : from.entrySet()) {
            builder.append('\'').append(entry.getKey()).append("':'").append(entry.getValue()).append("',");
        }
        builder.append("]");
        return builder.toString();
    }

    public static String convertToValidField(String value) {
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
        while (value.length() > 0 && partOf(illegals, value.charAt(value.length() - 1))) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static boolean partOf(char[] values, char val) {
        for (char value : values) {
            if (value == val) {
                return true;
            }
        }
        return false;
    }
}
