package org.jfrog.wharf.layout.field.definition;

import org.jfrog.wharf.layout.field.FieldDefinition;
import org.jfrog.wharf.layout.field.FieldValueProvider;
import org.jfrog.wharf.layout.field.OrgFieldProvider;
import org.jfrog.wharf.layout.field.OrgPathFieldProvider;
import org.jfrog.wharf.layout.field.definition.DefaultFieldDefinition;

/**
 * Module level fields:<ol>
 * <li>org: organisation, organization, group, groupId (converter from orgPath)</li>
 * <li>orgPath: groupIdPath (converter from org)</li>
 * <li>module: moduleName</li>
 * </ol>
 * Date: 9/11/11
 * Time: 6:39 PM
 *
 * @author Fred Simon
 */
public enum ModuleFields implements FieldDefinition {
    org("organisation", "organization", "group", "groupId"),
    orgPath("groupPath"),
    module("moduleName", "artifactId");

    final FieldDefinition delegate;

    ModuleFields(String... altNames) {
        // All fields here are mandatory
        delegate = new DefaultFieldDefinition(true, name(), altNames);
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
