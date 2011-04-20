package org.jfrog.wharf.ivy.resolver;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;

/**
 * @author Tomer Cohen
 */
public class FileSystemWharfResolver extends FileSystemResolver implements WharfResolver {

    public FileSystemWharfResolver() {
        WharfUtils.hackIvyBasicResolver(this);
        // No checksum verification by default for filesystem
        // File system can recalculate all checksums from the file itself
        getWharfUrlRepository().noChecksumCheck();
    }

    @Override
    public void setRepository(Repository repository) {
        super.setRepository(repository);
    }

    @Override
    public WharfURLRepository getWharfUrlRepository() {
        return (WharfURLRepository) super.getRepository();
    }

    @Override
    public Artifact fromSystem(Artifact artifact) {
        return super.fromSystem(artifact);
    }

    @Override
    public void setChecksums(String checksums) {
        getWharfUrlRepository().setChecksums(checksums);
    }

    @Override
    public boolean supportsWrongSha1() {
        return getWharfUrlRepository().supportsWrongSha1();
    }

    @Override
    public String[] getChecksumAlgorithms() {
        return getWharfUrlRepository().getChecksumAlgorithms();
    }

    @Override
    protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
        ResolvedModuleRevision moduleRevision = super.findModuleInCache(dd, data);
        if (moduleRevision == null) {
            return null;
        }
        ModuleRevisionMetadata metadata = getCacheProperties(dd, moduleRevision);
        if (metadata == null) {
            Message.debug("Dependency descriptor " + dd.getDependencyRevisionId() + " has no descriptor");
            metadata = new ModuleRevisionMetadata();
        }
        updateCachePropertiesToCurrentTime(metadata);
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        cacheManager.getMetadataHandler().saveModuleRevisionMetadata(moduleRevision.getId(), metadata);
        return moduleRevision;
    }

    private ModuleRevisionMetadata getCacheProperties(DependencyDescriptor dd, ResolvedModuleRevision moduleRevision) {
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        return cacheManager.getMetadataHandler().getModuleRevisionMetadata(moduleRevision.getId());
    }

    private void updateCachePropertiesToCurrentTime(ModuleRevisionMetadata cacheProperties) {
        cacheProperties.latestResolvedTime = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public ResolvedResource getArtifactRef(Artifact artifact, Date date) {
        ResolvedResource artifactRef = super.getArtifactRef(artifact, date);
        return WharfUtils.convertToWharfResource(artifactRef);
    }

    @Override
    public long getAndCheck(Resource resource, File dest) throws IOException {
        return WharfUtils.getAndCheck(this, resource, dest);
    }


    @Override
    public long get(Resource resource, File dest) throws IOException {
        return super.get(resource, dest);
    }
}
