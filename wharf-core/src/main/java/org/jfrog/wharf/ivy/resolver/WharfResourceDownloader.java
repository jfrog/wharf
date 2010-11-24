package org.jfrog.wharf.ivy.resolver;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;

/**
 * @author Tomer Cohen
 */
public class WharfResourceDownloader implements ResourceDownloader {

    private URLRepository extartifactrep = new URLRepository(); // used only to download

    private final IvyWharfResolver resolver;

    public WharfResourceDownloader(IvyWharfResolver resolver) {
        this.resolver = resolver;
        URLHandlerRegistry.setDefault(URLHandlerRegistry.getHttp());
    }

    @Override
    public void download(Artifact artifact, Resource resource, File dest) throws IOException {
        if (dest.exists()) {
            dest.delete();
        }
        File part = new File(dest.getAbsolutePath() + ".part");
        if (resource.getName().equals(
                String.valueOf(artifact.getUrl()))) {
            if (part.getParentFile() != null) {
                part.getParentFile().mkdirs();
            }
            extartifactrep.get(resource.getName(), part);
        } else {
            resolver.getAndCheck(resource, part);
        }
        if (!part.renameTo(dest)) {
            throw new IOException("impossible to move part file to definitive one: " + part + " -> " + dest);
        }
    }
}
