package org.jfrog.wharf.layout.field;

import com.google.common.collect.ImmutableMap;
import org.jfrog.wharf.layout.field.definition.ArtifactFields;
import org.jfrog.wharf.layout.field.definition.ModuleFields;
import org.jfrog.wharf.layout.field.definition.ModuleRevisionFields;

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
    public static final ImmutableMap<String, FieldValueProvider> mavenValueProviders;

    static {
        ImmutableMap.Builder<String, FieldDefinition> moduleBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, FieldDefinition> moduleRevisionBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, FieldDefinition> artifactBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, FieldValueProvider> mavenProvidersBuilder = ImmutableMap.builder();
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

        mavenProvidersBuilder.put(ModuleFields.org.id(), new OrgFieldProvider());
        mavenProvidersBuilder.put(ModuleFields.orgPath.id(), new OrgPathFieldProvider());
        mavenProvidersBuilder.put(ModuleFields.module.id(), new ModuleFieldProvider());
        mavenProvidersBuilder.put(ModuleRevisionFields.revision.id(), new RevisionFieldProvider());
        mavenProvidersBuilder.put(ModuleRevisionFields.baseRev.id(), new BaseRevisionFieldProvider());
        mavenProvidersBuilder.put(ModuleRevisionFields.status.id(), new StatusFieldProvider());
        mavenProvidersBuilder.put(ModuleRevisionFields.folderItegRev.id(), new FolderIntegrationRevisionFieldProvider());
        mavenProvidersBuilder.put(ModuleRevisionFields.fileItegRev.id(), new FileIntegrationRevisionFieldProvider());
        mavenProvidersBuilder.put(ArtifactFields.artifact.id(), new ArtifactNameFieldProvider());
        mavenProvidersBuilder.put(ArtifactFields.classifier.id(), new ClassifierFieldProvider());
        mavenProvidersBuilder.put(ArtifactFields.ext.id(), new ExtensionFieldProvider());
        mavenProvidersBuilder.put(ArtifactFields.type.id(), new TypeFieldProvider());

        mavenValueProviders = mavenProvidersBuilder.build();
    }

}
