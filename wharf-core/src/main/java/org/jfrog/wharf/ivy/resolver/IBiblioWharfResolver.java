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
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Tomer Cohen
 */
public class IBiblioWharfResolver extends IBiblioResolver implements WharfResolver {

    private final WharfResourceDownloader downloader = new WharfResourceDownloader(this);
    private final ArtifactResourceResolver artifactResourceResolver = new ArtifactResourceResolver() {
        @Override
        public ResolvedResource resolve(Artifact artifact) {
            artifact = fromSystem(artifact);
            return getArtifactRef(artifact, null);
        }
    };
    protected CacheTimeoutStrategy snapshotTimeout = DAILY;

    public IBiblioWharfResolver() {
        super.setChecksums(WharfUtils.SHA1_ALGORITHM);
        WharfUtils.hackIvyBasicResolver(this);
        setRepository(new WharfURLRepository());
    }

    @Override
    public void setChecksums(String checksums) {
        throw new UnsupportedOperationException("Wharf resolvers enforce the usage of SHA1 checksums only!");
    }


    @Override
    protected ResolvedResource getArtifactRef(Artifact artifact, Date date) {
        ResolvedResource artifactRef = super.getArtifactRef(artifact, date);
        return WharfUtils.convertToWharfResource(artifactRef);
    }

    @Override
    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        ResolvedResource ivyFileRef = super.findIvyFileRef(dd, data);
        return WharfUtils.convertToWharfResource(ivyFileRef);
    }

    /**
     * Returns the timeout strategy for a Maven Snapshot in the cache
     */
    public CacheTimeoutStrategy getSnapshotTimeout() {
        return snapshotTimeout;
    }

    /**
     * Sets the time in ms a Maven Snapshot in the cache is not checked for a newer version
     *
     * @param snapshotLifetime The lifetime in ms
     */
    public void setSnapshotTimeout(long snapshotLifetime) {
        this.snapshotTimeout = new Interval(snapshotLifetime);
    }

    /**
     * Sets a timeout strategy for a Maven Snapshot in the cache
     *
     * @param cacheTimeoutStrategy The strategy
     */
    public void setSnapshotTimeout(CacheTimeoutStrategy cacheTimeoutStrategy) {
        this.snapshotTimeout = cacheTimeoutStrategy;
    }

    @Override
    public long get(Resource resource, File dest) throws IOException {
        return super.get(resource, dest);
    }

    @Override
    public ResourceDownloader getDownloader() {
        return downloader;
    }

    @Override
    public ArtifactResourceResolver getArtifactResourceResolver() {
        return artifactResourceResolver;
    }

    @Override
    protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
        setChangingPattern(null);
        ResolvedModuleRevision moduleRevision = super.findModuleInCache(dd, data);
        if (moduleRevision == null) {
            setChangingPattern(".*-SNAPSHOT");
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
        if (snapshotTimeout.isCacheTimedOut(getLastResolvedTime(metadata))) {
            setChangingPattern(".*-SNAPSHOT");
            return null;
        } else {
            return moduleRevision;
        }
    }

    @Override
    public long getAndCheck(Resource resource, File dest) throws IOException {
        return WharfUtils.getAndCheck(this, resource, dest);
    }


    private void updateCachePropertiesToCurrentTime(ModuleRevisionMetadata cacheProperties) {
        cacheProperties.latestResolvedTime = String.valueOf(System.currentTimeMillis());
    }

    private Long getLastResolvedTime(ModuleRevisionMetadata cacheProperties) {
        String lastResolvedProp = cacheProperties.latestResolvedTime;
        Long lastResolvedTime = lastResolvedProp != null ? Long.parseLong(lastResolvedProp) : 0;
        return lastResolvedTime;
    }

    private ModuleRevisionMetadata getCacheProperties(DependencyDescriptor dd, ResolvedModuleRevision moduleRevision) {
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        return cacheManager.getMetadataHandler().getModuleRevisionMetadata(moduleRevision.getId());
    }

    @Override
    public void setRoot(String root) {
        super.setRoot(root);

        URI rootUri;
        try {
            rootUri = new URI(root);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (rootUri.getScheme().equalsIgnoreCase("file")) {
            setSnapshotTimeout(ALWAYS);
        } else {
            setSnapshotTimeout(DAILY);
        }
    }

    public interface CacheTimeoutStrategy {
        boolean isCacheTimedOut(long lastResolvedTime);
    }

    public static class Interval implements CacheTimeoutStrategy {
        private long interval;

        public Interval(long interval) {
            this.interval = interval;
        }

        @Override
        public boolean isCacheTimedOut(long lastResolvedTime) {
            return System.currentTimeMillis() - lastResolvedTime > interval;
        }
    }

    public static final CacheTimeoutStrategy NEVER = new CacheTimeoutStrategy() {
        @Override
        public boolean isCacheTimedOut(long lastResolvedTime) {
            return false;
        }
    };

    public static final CacheTimeoutStrategy ALWAYS = new CacheTimeoutStrategy() {
        @Override
        public boolean isCacheTimedOut(long lastResolvedTime) {
            return true;
        }
    };

    public static final CacheTimeoutStrategy DAILY = new CacheTimeoutStrategy() {
        @Override
        public boolean isCacheTimedOut(long lastResolvedTime) {
            Calendar calendarCurrent = Calendar.getInstance();
            calendarCurrent.setTime(new Date());
            int dayOfYear = calendarCurrent.get(Calendar.DAY_OF_YEAR);
            int year = calendarCurrent.get(Calendar.YEAR);

            Calendar calendarLastResolved = Calendar.getInstance();
            calendarLastResolved.setTime(new Date(lastResolvedTime));
            if (calendarLastResolved.get(Calendar.YEAR) == year &&
                    calendarLastResolved.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
                return false;
            }
            return true;
        }
    };

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getRoot().hashCode();
        result = 31 * result + snapshotTimeout.hashCode();
        result = 31 * result + getPattern().hashCode();
        result = 31 * result + (isUsepoms() ? 1 : 0);
        result = 31 * result + (isAlwaysCheckExactRevision() ? 1 : 0);
        result = 31 * result + (isM2compatible() ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IBiblioWharfResolver)) return false;

        IBiblioWharfResolver that = (IBiblioWharfResolver) o;

        if (!getName().equals(that.getName())
                || !getRoot().equals(that.getRoot())
                || !snapshotTimeout.equals(that.snapshotTimeout)
                || !getPattern().equals(that.getPattern())
                || isUsepoms() != that.isUsepoms()
                || isUseMavenMetadata() != that.isUseMavenMetadata()
                || isAlwaysCheckExactRevision() != that.isAlwaysCheckExactRevision()
                || isM2compatible() != that.isM2compatible())
            return false;
        return true;
    }
}
