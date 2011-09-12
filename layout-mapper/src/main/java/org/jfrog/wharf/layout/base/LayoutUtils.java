package org.jfrog.wharf.layout.base;

import java.util.Map;

/**
 * Date: 9/12/11
 * Time: 4:22 PM
 *
 * @author Fred Simon
 */
public abstract class LayoutUtils {
    public static final String STATUS_INTEGRATION = "integration";
    public static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    public static final String STATUS_RELEASE = "release";

    public static String mapToString(Map<String, String> from) {
        StringBuilder builder = new StringBuilder("[");
        for (Map.Entry<String, String> entry : from.entrySet()) {
            builder.append('\'').append(entry.getKey()).append("':'").append(entry.getValue()).append("',");
        }
        builder.append("]");
        return builder.toString();
    }
}
