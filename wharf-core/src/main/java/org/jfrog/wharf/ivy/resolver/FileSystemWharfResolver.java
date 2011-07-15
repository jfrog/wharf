package org.jfrog.wharf.ivy.resolver;

import org.apache.ivy.core.cache.CacheMetadataOptions;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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

    public boolean supportsWrongSha1() {
        return getWharfUrlRepository().supportsWrongSha1();
    }

    @Override
    public String[] getChecksumAlgorithms() {
        return getWharfUrlRepository().getChecksumAlgorithms();
    }

    public ResolvedModuleRevision basicFindModuleInCache(DependencyDescriptor dd, ResolveData data, boolean anyResolver) {
        return super.findModuleInCache(dd, data, anyResolver);
    }

    @Override
    protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
        return WharfUtils.findModuleInCache(this, dd, data);
    }

    @Override
    public CacheMetadataOptions getCacheOptions(ResolveData data) {
        return super.getCacheOptions(data);
    }

    public ModuleRevisionMetadata getCacheProperties(ModuleRevisionId mrid) {
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        return cacheManager.getMetadataHandler().getModuleRevisionMetadata(mrid);
    }

    @Override
    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        ResolvedResource ivyFileRef = super.findIvyFileRef(dd, data);
        return WharfUtils.convertToWharfResource(ivyFileRef);
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
