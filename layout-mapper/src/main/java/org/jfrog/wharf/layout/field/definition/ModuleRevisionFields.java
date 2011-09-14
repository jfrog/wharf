package org.jfrog.wharf.layout.field.definition;

import org.jfrog.wharf.layout.field.FieldDefinition;

/**
 * Module Revision level fields:<ol>
 * revision: (converter from baseRev, status, fileItegRev)
 * baseRev: baseRevision (converter from revision)
 * status: (integration, release, other) (converter from revision)
 * folderItegRev: (converter from fileItegRev)
 * fileItegRev: integrationRevision
 * </ol>
 * Date: 9/11/11
 * Time: 6:39 PM
 *
 * @author Fred Simon
 */
public enum ModuleRevisionFields implements FieldDefinition {
    revision(true, "version"),
    baseRev(true, "baseRevision"),
    status(true),
    folderItegRev(false, "folderIntegrationRevision", "folderIntegRev", "folderIntegrationRev"),
    fileItegRev(false, "fileIntegrationRevision", "fileIntegRev", "fileIntegrationRev");

    final FieldDefinition delegate;

    ModuleRevisionFields(boolean mandatory, String... altNames) {
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
