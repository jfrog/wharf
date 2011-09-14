package org.jfrog.wharf.layout.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.field.provider.BaseFieldProvider;
import org.jfrog.wharf.layout.field.FieldDefinition;
import org.jfrog.wharf.layout.field.FieldValueProvider;
import org.jfrog.wharf.layout.field.definition.ArtifactFields;
import org.jfrog.wharf.layout.field.definition.DefaultFieldDefinition;
import org.jfrog.wharf.layout.field.definition.ModuleFields;
import org.jfrog.wharf.layout.field.definition.ModuleRevisionFields;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.jfrog.wharf.layout.base.LayoutUtils.convertToValidField;
import static org.jfrog.wharf.layout.field.DefaultFieldDefinitions.artifactFieldDefinitions;
import static org.jfrog.wharf.layout.field.DefaultFieldDefinitions.mavenValueProviders;

/**
 * Date: 9/11/11
 * Time: 6:02 PM
 *
 * @author Fred Simon
 */
public class MavenArtifactInfo extends BaseArtifactInfo {

    @Override
    protected ImmutableMap<String, FieldValueProvider> getBasicProviders() {
        return mavenValueProviders;
    }

}
