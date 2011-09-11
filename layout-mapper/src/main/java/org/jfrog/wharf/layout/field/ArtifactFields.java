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
    artifact("artifactName"),
    classifier,
    ext("extension"),
    type;

    final FieldDefinition delegate;

    ArtifactFields(FieldConverter converter, String... altNames) {
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
    public FieldConverter converter() {
        return delegate.converter();
    }
}
