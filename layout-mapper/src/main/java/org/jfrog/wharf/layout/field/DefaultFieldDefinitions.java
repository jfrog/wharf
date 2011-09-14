package org.jfrog.wharf.layout.field;

import com.google.common.collect.ImmutableMap;
import org.jfrog.wharf.layout.field.definition.ArtifactFields;
import org.jfrog.wharf.layout.field.definition.ModuleFields;
import org.jfrog.wharf.layout.field.definition.ModuleRevisionFields;
import org.jfrog.wharf.layout.field.ivy.IvyArtifactNameFieldProvider;
import org.jfrog.wharf.layout.field.ivy.IvyFileIntegrationRevisionFieldProvider;
import org.jfrog.wharf.layout.field.ivy.IvyFolderIntegrationRevisionFieldProvider;
import org.jfrog.wharf.layout.field.ivy.IvyVersionPopulator;
import org.jfrog.wharf.layout.field.maven.MavenFileIntegrationRevisionFieldProvider;
import org.jfrog.wharf.layout.field.maven.MavenArtifactNameFieldProvider;
import org.jfrog.wharf.layout.field.maven.MavenFolderIntegrationRevisionFieldProvider;
import org.jfrog.wharf.layout.field.maven.MavenVersionPopulator;
import org.jfrog.wharf.layout.field.provider.*;

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
    public static final ImmutableMap<String, FieldValueProvider> ivyValueProviders;

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

        ProvidersBuilder mavenProvidersBuilder = new ProvidersBuilder();
        MavenVersionPopulator mavenVersionPopulator = new MavenVersionPopulator(null, null);

        mavenProvidersBuilder.add(new OrgFieldProvider());
        mavenProvidersBuilder.add(new OrgPathFieldProvider());
        mavenProvidersBuilder.add(new ModuleFieldProvider());
        mavenProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.revision, mavenVersionPopulator));
        mavenProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.baseRev, mavenVersionPopulator));
        mavenProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.status, mavenVersionPopulator));
        mavenProvidersBuilder.add(new MavenFolderIntegrationRevisionFieldProvider(mavenVersionPopulator));
        mavenProvidersBuilder.add(new MavenFileIntegrationRevisionFieldProvider(mavenVersionPopulator));
        mavenProvidersBuilder.add(new MavenArtifactNameFieldProvider());
        mavenProvidersBuilder.add(new ClassifierFieldProvider());
        mavenProvidersBuilder.add(new ExtensionFieldProvider());
        mavenProvidersBuilder.add(new TypeFieldProvider());

        mavenValueProviders = mavenProvidersBuilder.build();

        ProvidersBuilder ivyProvidersBuilder = new ProvidersBuilder();
        IvyVersionPopulator ivyVersionPopulator = new IvyVersionPopulator(null, null);

        ivyProvidersBuilder.add(new OrgFieldProvider());
        ivyProvidersBuilder.add(new OrgPathFieldProvider());
        ivyProvidersBuilder.add(new ModuleFieldProvider());
        ivyProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.revision, ivyVersionPopulator));
        ivyProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.baseRev, ivyVersionPopulator));
        ivyProvidersBuilder.add(new AnyRevisionFieldProvider(ModuleRevisionFields.status, ivyVersionPopulator));
        ivyProvidersBuilder.add(new IvyFolderIntegrationRevisionFieldProvider(ivyVersionPopulator));
        ivyProvidersBuilder.add(new IvyFileIntegrationRevisionFieldProvider(ivyVersionPopulator));
        ivyProvidersBuilder.add(new IvyArtifactNameFieldProvider());
        ivyProvidersBuilder.add(new ClassifierFieldProvider());
        ivyProvidersBuilder.add(new ExtensionFieldProvider());
        ivyProvidersBuilder.add(new TypeFieldProvider());

        ivyValueProviders = ivyProvidersBuilder.build();
    }

    static class ProvidersBuilder {
        ImmutableMap.Builder<String, FieldValueProvider> builder = ImmutableMap.builder();

        void add(FieldValueProvider provider) {
            builder.put(provider.id(), provider);
        }

        public ImmutableMap<String, FieldValueProvider> build() {
            return builder.build();
        }
    }
}
