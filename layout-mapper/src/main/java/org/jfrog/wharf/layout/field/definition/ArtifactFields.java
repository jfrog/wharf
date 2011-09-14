package org.jfrog.wharf.layout.field.definition;

import org.jfrog.wharf.layout.field.FieldDefinition;

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
    artifact(true, "artifactName"),
    classifier(false),
    ext(true, "extension"),
    type(true);

    final FieldDefinition delegate;

    ArtifactFields(boolean mandatory, String... altNames) {
        delegate = new DefaultFieldDefinition(mandatory, name(), altNames);
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
    public boolean isMandatory() {
        return delegate.isMandatory();
    }
}
