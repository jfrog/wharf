/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jfrog.wharf.resolver;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.ChecksumHelper;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Hans Dockter
 * @author Tomer Cohen
 */
public class WharfResolver extends IBiblioResolver {

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

    private CacheTimeoutStrategy snapshotTimeout = DAILY;

    {
        setChecksums("md5, sha1");
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
    protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
        setChangingPattern(null);
        ResolvedModuleRevision moduleRevision = super.findModuleInCache(dd, data);
        if (moduleRevision == null) {
            setChangingPattern(".*-SNAPSHOT");
            return null;
        }
        ModuleRevisionMetadata metadata = getCacheProperties(dd, moduleRevision);
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        Artifact artifact = moduleRevision.getDescriptor().getMetadataArtifact();
        int id = cacheManager.getResolverHandler().getResolver(moduleRevision.getResolver()).getId();
        artifact = ArtifactMetadata.fillResolverId(artifact, id);
        ArtifactMetadata artMd = cacheManager.getMetadataHandler().getArtifactMetadata(artifact);
        calculateChecksums(moduleRevision, artMd);
        metadata.artifactMetadata.remove(artMd);
        metadata.artifactMetadata.add(artMd);
        updateCachePropertiesToCurrentTime(metadata);
        Long lastResolvedTime = getLastResolvedTime(metadata);
        cacheManager.getMetadataHandler().saveModuleRevisionMetadata(moduleRevision.getId(), metadata);
        if (snapshotTimeout.isCacheTimedOut(lastResolvedTime)) {
            setChangingPattern(".*-SNAPSHOT");
            return null;
        } else {
            return moduleRevision;
        }
    }

    private void calculateChecksums(ResolvedModuleRevision moduleRevision, ArtifactMetadata artMd) {
        String[] algorithms = getChecksumAlgorithms();
        for (String algorithm : algorithms) {
            File localFile = moduleRevision.getReport().getLocalFile();
            try {
                String computedChecksum = ChecksumHelper.computeAsString(localFile, algorithm);
                if ("sha1".equals(algorithm)) {
                    artMd.sha1 = computedChecksum;
                } else if ("md5".equals(algorithm)) {
                    artMd.md5 = computedChecksum;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
}


