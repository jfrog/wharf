package org.jfrog.wharf.layout.field;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 9/12/11
 * Time: 1:22 PM
 *
 * @author Fred Simon
 */
public abstract class DefaultFieldDefinitions {
    public static final ImmutableMap<String, FieldDefinition> moduleFieldDefinitions;
    public static final ImmutableMap<String, FieldDefinition> moduleRevisionFieldDefinitions;
    public static final ImmutableMap<String, FieldDefinition> artifactFieldDefinitions;

    static {
        ImmutableMap.Builder<String, FieldDefinition> moduleBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, FieldDefinition> moduleRevisionBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, FieldDefinition> artifactBuilder = ImmutableMap.builder();
        for (ModuleFields fieldDefinition : ModuleFields.values()) {
            for (String fieldName : fieldDefinition.fieldNames()) {
               moduleBuilder.put(fieldName, fieldDefinition);
               moduleRevisionBuilder.put(fieldName, fieldDefinition);
               artifactBuilder.put(fieldName, fieldDefinition);
            }
        }
        for (ModuleRevisionFields fieldDefinition : ModuleRevisionFields.values()) {
            for (String fieldName : fieldDefinition.fieldNames()) {
                moduleRevisionBuilder.put(fieldName, fieldDefinition);
                artifactBuilder.put(fieldName, fieldDefinition);
            }
        }
        for (ArtifactFields fieldDefinition : ArtifactFields.values()) {
            for (String fieldName : fieldDefinition.fieldNames()) {
                artifactBuilder.put(fieldName, fieldDefinition);
            }
        }
        moduleFieldDefinitions = moduleBuilder.build();
        moduleRevisionFieldDefinitions = moduleRevisionBuilder.build();
        artifactFieldDefinitions = artifactBuilder.build();
    }

}
