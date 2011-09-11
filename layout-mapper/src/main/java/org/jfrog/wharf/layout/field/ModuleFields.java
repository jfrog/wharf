package org.jfrog.wharf.layout.field;

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
    org(new OrgConverter(), "organisation", "organization", "group", "groupId"),
    orgPath(new OrgPathConverter(), "groupPath"),
    module("moduleName");

    final FieldDefinition delegate;

    ModuleFields(FieldConverter converter, String... altNames) {
        delegate = new DefaultFieldDefinition(name(), converter, altNames);
    }

    ModuleFields(String... altNames) {
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
