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

import org.apache.commons.io.FileUtils;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.Checks;
import org.apache.ivy.util.ChecksumHelper;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.downloader.WharfResourceDownloader;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private final WharfResourceDownloader DOWNLOADER = new WharfResourceDownloader(this);

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

    @Override
    public ArtifactDownloadReport download(final ArtifactOrigin origin, DownloadOptions options) {
        Checks.checkNotNull(origin, "origin");
        return getRepositoryCacheManager().download(
                origin.getArtifact(),
                new ArtifactResourceResolver() {
                    @Override
                    public ResolvedResource resolve(Artifact artifact) {
                        try {
                            Resource resource = getResource(origin.getLocation());
                            if (resource == null) {
                                return null;
                            }
                            String revision = origin.getArtifact().getModuleRevisionId().getRevision();
                            return new ResolvedResource(resource, revision);
                        } catch (IOException e) {
                            return null;
                        }
                    }
                },
                DOWNLOADER,
                getCacheDownloadOptions(options));
    }

    @Override
    public ResolvedModuleRevision parse(final ResolvedResource mdRef, DependencyDescriptor dd,
            ResolveData data) throws ParseException {

        DependencyDescriptor nsDd = dd;
        dd = toSystem(nsDd);

        ModuleRevisionId mrid = dd.getDependencyRevisionId();
        ModuleDescriptorParser parser = ModuleDescriptorParserRegistry
                .getInstance().getParser(mdRef.getResource());
        if (parser == null) {
            Message.warn("no module descriptor parser available for " + mdRef.getResource());
            return null;
        }
        Message.verbose("\t" + getName() + ": found md file for " + mrid);
        Message.verbose("\t\t=> " + mdRef);
        Message.debug("\tparser = " + parser);

        ModuleRevisionId resolvedMrid = mrid;

        // first check if this dependency has not yet been resolved
        if (getSettings().getVersionMatcher().isDynamic(mrid)) {
            resolvedMrid = ModuleRevisionId.newInstance(mrid, mdRef.getRevision());
            IvyNode node = data.getNode(resolvedMrid);
            if (node != null && node.getModuleRevision() != null) {
                // this revision has already be resolved : return it
                if (node.getDescriptor() != null && node.getDescriptor().isDefault()) {
                    Message.verbose("\t" + getName() + ": found already resolved revision: "
                            + resolvedMrid
                            + ": but it's a default one, maybe we can find a better one");
                } else {
                    Message.verbose("\t" + getName() + ": revision already resolved: "
                            + resolvedMrid);
                    node.getModuleRevision().getReport().setSearched(true);
                    return node.getModuleRevision();
                }
            }
        }

        Artifact moduleArtifact = parser.getMetadataArtifact(resolvedMrid, mdRef.getResource());
        return getRepositoryCacheManager().cacheModuleDescriptor(this, mdRef, dd, moduleArtifact, DOWNLOADER,
                getCacheOptions(data));
    }

    @Override
    public DownloadReport download(Artifact[] artifacts, DownloadOptions options) {
        RepositoryCacheManager cacheManager = getRepositoryCacheManager();

        clearArtifactAttempts();
        DownloadReport dr = new DownloadReport();
        for (Artifact artifact : artifacts) {
            ArtifactDownloadReport adr = cacheManager.download(
                    artifact, artifactResourceResolver, DOWNLOADER, getCacheDownloadOptions(options));
            if (DownloadStatus.FAILED == adr.getDownloadStatus()) {
                if (!ArtifactDownloadReport.MISSING_ARTIFACT.equals(adr.getDownloadDetails())) {
                    Message.warn("\t" + adr);
                }
            } else if (DownloadStatus.NO == adr.getDownloadStatus()) {
                Message.verbose("\t" + adr);
            } else if (LogOptions.LOG_QUIET.equals(options.getLog())) {
                Message.verbose("\t" + adr);
            } else {
                Message.info("\t" + adr);
            }
            dr.addArtifactReport(adr);
            checkInterrupted();
        }
        return dr;
    }

    private final ArtifactResourceResolver artifactResourceResolver
            = new ArtifactResourceResolver() {
        @Override
        public ResolvedResource resolve(Artifact artifact) {
            artifact = fromSystem(artifact);
            return getArtifactRef(artifact, null);
        }
    };

    @Override
    public long getAndCheck(Resource resource, File dest) throws IOException {
        String[] algorithms = getChecksumAlgorithms();
        WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
        Artifact artifact = DOWNLOADER.getArtifact();
        ModuleRevisionMetadata mrm =
                cacheManager.getMetadataHandler().getModuleRevisionMetadata(artifact.getModuleRevisionId());
        if (mrm == null) {
            get(resource, dest);
            return dest.length();
        }
        if (mrm.artifactMetadata.isEmpty()) {
            get(resource, dest);
            return dest.length();
        }
        ArtifactMetadata artMd = cacheManager.getMetadataHandler().getArtifactMetadata(artifact);
        if (artMd == null) {
            artMd = new ArtifactMetadata(artifact);
            for (String algorithm : algorithms) {
                Resource csRes = resource.clone(resource.getName() + "." + algorithm);
                if (csRes.exists()) {
                    File tempChecksum = File.createTempFile("temp", ".tmp");
                    get(csRes, tempChecksum);
                    try {
                        if ("md5".equals(algorithm)) {

                            String csFileContent = FileUtil.readEntirely(
                                    new BufferedReader(new FileReader(tempChecksum))).trim().toLowerCase(Locale.US);
                            int id = getResolverIdByMd5(mrm, csFileContent);
                            if (id == 0) {
                                get(resource, dest);
                                return dest.length();
                            }
                            Artifact newArtifact = new DefaultArtifact(artifact.getModuleRevisionId(),
                                    artifact.getPublicationDate(), artifact.getName(),
                                    artifact.getType(), artifact.getExt(),
                                    artifact.getExtraAttributes());
                            newArtifact = ArtifactMetadata.fillResolverId(newArtifact, id);
                            File fileInCache = cacheManager.getArchiveFileInCache(newArtifact);
                            try {
                                ChecksumHelper.check(fileInCache, tempChecksum, "md5");
                                FileUtils.copyFile(fileInCache, dest);
                                artMd.md5 = csFileContent;
                            } catch (IOException e) {
                                // recalculate checksum
                            }
                        } else if ("sha1".equals(algorithm)) {
                            String csFileContent = FileUtil.readEntirely(
                                    new BufferedReader(new FileReader(tempChecksum))).trim().toLowerCase(Locale.US);
                            int id = getResolverIdBySha1(mrm, csFileContent);
                            if (id == 0) {
                                get(resource, dest);
                                return dest.length();
                            }
                            Artifact newArtifact = new DefaultArtifact(artifact.getModuleRevisionId(),
                                    artifact.getPublicationDate(), artifact.getName(),
                                    artifact.getType(), artifact.getExt(),
                                    artifact.getExtraAttributes());
                            newArtifact = ArtifactMetadata.fillResolverId(newArtifact, id);
                            File fileInCache = cacheManager.getArchiveFileInCache(newArtifact);
                            try {
                                ChecksumHelper.check(fileInCache, tempChecksum, "sha1");
                                FileUtils.copyFile(fileInCache, dest);
                                artMd.sha1 = csFileContent;
                            } catch (IOException e) {
                                // recalculate checksum
                            }
                        }
                    } finally {
                        FileUtil.forceDelete(tempChecksum);
                    }

                }
            }
            mrm.artifactMetadata.remove(artMd);
            mrm.artifactMetadata.add(artMd);
            cacheManager.getMetadataHandler()
                    .saveModuleRevisionMetadata(artifact.getModuleRevisionId(), mrm);
        }
        return dest.length();
    }

    private int getResolverIdByMd5(ModuleRevisionMetadata metadata, String md5) {
        for (ArtifactMetadata artMd : metadata.artifactMetadata) {
            if (md5.equals(artMd.md5)) {
                return artMd.artResolverId;
            }
        }
        return 0;
    }

    private int getResolverIdBySha1(ModuleRevisionMetadata metadata, String sha1) {
        for (ArtifactMetadata artMd : metadata.artifactMetadata) {
            if (sha1.equals(artMd.sha1)) {
                return artMd.artResolverId;
            }
        }
        return 0;
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


