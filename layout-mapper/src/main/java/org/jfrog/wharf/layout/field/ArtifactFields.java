package org.jfrog.wharf.layout.field;

/**
 * Artifact level fields:<ol>
 * artifact: artifactName (converter from module)
 * classifier:
 * ext: extension
 * type: (converter from ext, artifact)
 * </ol>
 * Date: 9/11/11
 * Time: 6:39 PM
 *
 * @author Fred Simon
 */
public enum ArtifactFields implements FieldDefinition {
    artifact(new ArtifactNameFieldProvider(), "artifactName"),
    classifier(new ClassifierFieldProvider()),
    ext(new ExtensionFieldProvider(), "extension"),
    type(new TypeFieldProvider());

    final FieldDefinition delegate;

    ArtifactFields(FieldValueProvider converter, String... altNames) {
        delegate = new DefaultFieldDefinition(name(), converter, altNames);
    }

    ArtifactFields(String... altNames) {
        this(null, altNames);
    }

    @Override
    public String id() {
        return name();
    }

    @Override
    public String[] fieldNames() {
        return delegate.fieldNames();
    }

    @Override
    public FieldValueProvider provider() {
        return delegate.provider();
    }

    @Override
    public String defaultRegex() {
        return ".*";
    }
}
