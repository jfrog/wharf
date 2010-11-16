package org.jfrog.wharf.ivy;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.parser.ParserSettings;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Don't know why this is here
 *
 * @author Tomer Cohen
 */
public interface ModuleDescriptorProvider {

    public ModuleDescriptor provideModule(ParserSettings ivySettings, File descriptorFile,
            boolean validate) throws ParseException, IOException;

}
