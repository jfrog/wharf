package org.jfrog.wharf.ivy.resolver;

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
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.model.ArtifactMetadata;
import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;

/**
 * @author Tomer Cohen
 */
public class IvyWharfResolver extends IBiblioResolver {
    @SuppressWarnings({"UnusedDeclaration"})
    protected static final String SHA1_ALGORITHM = "sha1";
    protected static final String MD5_ALGORITHM = "md5";
    private final WharfResourceDownloader DOWNLOADER = new WharfResourceDownloader(this);
    private final ArtifactResourceResolver artifactResourceResolver
            = new ArtifactResourceResolver() {
        @Override
        public ResolvedResource resolve(Artifact artifact) {
            artifact = fromSystem(artifact);
            return getArtifactRef(artifact, null);
        }
    };


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
            Resource csRes = resource.clone(resource.getName() + "." + SHA1_ALGORITHM);
            String sha1;
            if (csRes.exists()) {
                File tempChecksum = File.createTempFile("temp", ".tmp");
                get(csRes, tempChecksum);
                try {
                    sha1 = FileUtil.readEntirely(new BufferedReader(new FileReader(tempChecksum))).trim().
                            toLowerCase(Locale.US);
                } finally {
                    FileUtil.forceDelete(tempChecksum);
                }
            } else {
                get(resource, dest);
                sha1 = ChecksumHelper.computeAsString(dest, SHA1_ALGORITHM);
            }
            int id = getResolverIdBySha1(mrm, sha1);
            if (id == 0) {
                if (!dest.exists()) {
                    get(resource, dest);
                }
                return dest.length();
            }
            File tempChecksum = File.createTempFile("temp", "." + SHA1_ALGORITHM);
            FileUtils.writeStringToFile(tempChecksum, sha1);
            Artifact newArtifact = new DefaultArtifact(artifact.getModuleRevisionId(), artifact.getPublicationDate(),
                    artifact.getName(), artifact.getType(), artifact.getExt(), artifact.getExtraAttributes());
            newArtifact = ArtifactMetadata.fillResolverId(newArtifact, id);
            File fileInCache = cacheManager.getArchiveFileInCache(newArtifact);
            try {
                ChecksumHelper.check(fileInCache, tempChecksum, SHA1_ALGORITHM);
                FileUtils.copyFile(fileInCache, dest);
                artMd.sha1 = sha1;
            } catch (IOException e) {
                FileUtil.forceDelete(fileInCache);
            } finally {
                FileUtil.forceDelete(tempChecksum);
            }
            mrm.artifactMetadata.remove(artMd);
            mrm.artifactMetadata.add(artMd);
            cacheManager.getMetadataHandler().saveModuleRevisionMetadata(artifact.getModuleRevisionId(), mrm);
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
}
