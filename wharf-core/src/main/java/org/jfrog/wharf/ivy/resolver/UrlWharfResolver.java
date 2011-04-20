/*
 *
 *  Copyright (C) 2010 JFrog Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.jfrog.wharf.ivy.resolver;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.plugins.resolver.util.ResourceMDParser;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Tomer Cohen
 */
public class UrlWharfResolver extends URLResolver implements WharfResolver {

    public UrlWharfResolver() {
        WharfUtils.hackIvyBasicResolver(this);
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
    public ResolvedResource getArtifactRef(Artifact artifact, Date date) {
        ResolvedResource artifactRef = super.getArtifactRef(artifact, date);
        return WharfUtils.convertToWharfResource(artifactRef);
    }

    @Override
    protected ResolvedResource findResourceUsingPattern(ModuleRevisionId mrid, String pattern, Artifact artifact,
                                                        ResourceMDParser rmdparser, Date date) {
        return super.findResourceUsingPattern(mrid, pattern, artifact, rmdparser, date);
    }

    @Override
    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        ResolvedResource ivyFileRef = super.findIvyFileRef(dd, data);
        return WharfUtils.convertToWharfResource(ivyFileRef);
    }


    @Override
    protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
        ResolvedModuleRevision moduleRevision = super.findModuleInCache(dd, data);
        if (moduleRevision == null) {
            return null;
        }
        ModuleRevisionMetadata metadata = getCacheProperties(moduleRevision);
        if (metadata == null) {
            Message.debug("Dependency descriptor " + dd.getDependencyRevisionId() + " has no descriptor");
            metadata = new ModuleRevisionMetadata();
        }
        updateCachePropertiesToCurrentTime(metadata);
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        cacheManager.getMetadataHandler().saveModuleRevisionMetadata(moduleRevision.getId(), metadata);
        return moduleRevision;
    }

    @Override
    public long getAndCheck(Resource resource, File dest) throws IOException {
        return WharfUtils.getAndCheck(this, resource, dest);
    }

    @Override
    public long get(Resource resource, File dest) throws IOException {
        return super.get(resource, dest);
    }

    private void updateCachePropertiesToCurrentTime(ModuleRevisionMetadata cacheProperties) {
        cacheProperties.latestResolvedTime = String.valueOf(System.currentTimeMillis());
    }

    private ModuleRevisionMetadata getCacheProperties(ResolvedModuleRevision moduleRevision) {
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        return cacheManager.getMetadataHandler().getModuleRevisionMetadata(moduleRevision.getId());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + (isAlwaysCheckExactRevision() ? 1 : 0);
        result = 31 * result + (isM2compatible() ? 1 : 0);
        result = 31 * result + getIvyPatterns().hashCode();
        result = 31 * result + getArtifactPatterns().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlWharfResolver)) return false;

        UrlWharfResolver that = (UrlWharfResolver) o;

        if (!getName().equals(that.getName())
                || isAlwaysCheckExactRevision() != that.isAlwaysCheckExactRevision()
                || isM2compatible() != that.isM2compatible()
                || !getWharfUrlRepository().getChecksums().equals(that.getWharfUrlRepository().getChecksums()))
            return false;
        List myIvyPatterns = getIvyPatterns();
        List myArtifactsPatterns = getArtifactPatterns();
        List thatIvyPatterns = that.getIvyPatterns();
        List thatArtifactsPatterns = that.getArtifactPatterns();
        if (myIvyPatterns.size() != thatIvyPatterns.size()
                || myArtifactsPatterns.size() != thatArtifactsPatterns.size()
                || !listEquals(myIvyPatterns, thatIvyPatterns)
                || !listEquals(myArtifactsPatterns, thatArtifactsPatterns))
            return false;
        return true;
    }

    private boolean listEquals(List myIvyPatterns, List thatIvyPatterns) {
        for (int i = 0; i < myIvyPatterns.size(); i++) {
            if (!thatIvyPatterns.get(i).equals(myIvyPatterns.get(i))) {
                return false;
            }
        }
        return true;
    }
}
