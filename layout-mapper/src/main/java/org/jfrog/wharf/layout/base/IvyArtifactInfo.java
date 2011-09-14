package org.jfrog.wharf.layout.base;

import com.google.common.collect.ImmutableMap;
import org.jfrog.wharf.layout.field.DefaultFieldDefinitions;
import org.jfrog.wharf.layout.field.FieldValueProvider;

/**
 * Date: 9/14/11
 * Time: 3:06 PM
 *
 * @author Fred Simon
 */
public class IvyArtifactInfo extends BaseArtifactInfo {
    @Override
    protected ImmutableMap<String, FieldValueProvider> getBasicProviders() {
        return DefaultFieldDefinitions.ivyValueProviders;
    }
}
