package org.jfrog.wharf.layout.field;

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
    revision(new RevisionFieldProvider(), "version"),
    baseRev(new BaseRevisionFieldProvider(), "baseRevision"),
    status(new StatusFieldProvider()),
    folderItegRev("folderIntegrationRevision", "folderIntegRev", "folderIntegrationRev"),
    fileItegRev("fileIntegrationRevision", "fileIntegRev", "fileIntegrationRev");

    final FieldDefinition delegate;

    ModuleRevisionFields(FieldValueProvider converter, String... altNames) {
        delegate = new DefaultFieldDefinition(name(), converter, altNames);
    }

    ModuleRevisionFields(String... altNames) {
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
