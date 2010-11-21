package org.jfrog.wharf.ivy;


import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveEngine;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.sort.SortEngine;
import org.apache.ivy.plugins.lock.ArtifactLockStrategy;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.FileUtil;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.util.CacheCleaner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 * Test the following scenario, 3 resolvers, 2 of them are {@link FileSystemResolver} and one of them is a {@link
 * ChainResolver} which contains the file system resolvers, with 3 different {@link IvySettings}. Resolve a file from
 * resolver A, and check that it has downloaded, then check it from resolver B and check that it is downloaded but they
 * are different (downloaded once by each resolver), then test that when invoking the chain resolver, the files are
 * <b>NOT</b> being re-downloaded.
 *
 * @author Tomer Cohen
 */
public class WharfCacheManagerMultipleResolverTest {
    private FileSystemResolver resolverA;
    private FileSystemResolver resolverB;
    private ChainResolver chainResolver;
    private ResolveData resolveData;
    private File cache;

    private static final String FS = System.getProperty("file.separator");
    private static final String REL_IVY_PATTERN = "test" + FS + "repositories" + FS + "1" + FS
            + "[organisation]" + FS + "[module]" + FS + "ivys" + FS + "ivy-[revision].xml";
    private static final String IVY_PATTERN =
            new File(new File(".").getParentFile(), "src").getAbsolutePath() + FS + REL_IVY_PATTERN;

    @Before
    public void setup() {
        cache = new File("build/test/cache");
        FileUtil.forceDelete(cache);
        IvySettings settings1 = new IvySettings();
        settings1.setDefaultCache(cache);
        ResolveEngine engine = new ResolveEngine(settings1, new EventManager(), new SortEngine(settings1));
        resolveData = new ResolveData(engine, new ResolveOptions());
        resolverA = new FileSystemResolver();
        resolverA.setRepositoryCacheManager(newCacheManager(settings1));
        resolverA.setName("testA");
        resolverA.setSettings(settings1);
        File baseDir = new File(settings1.getBaseDir().getParentFile(), "src");
        resolverA.addIvyPattern(IVY_PATTERN);
        resolverA.addArtifactPattern(baseDir +
                "/test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        settings1.addResolver(resolverA);

        IvySettings settings2 = new IvySettings();
        settings2.setDefaultCache(cache);
        resolverB = new FileSystemResolver();
        resolverB.setRepositoryCacheManager(newCacheManager(settings2));
        resolverB.setName("testB");
        resolverA.setSettings(settings2);
        baseDir = new File(settings1.getBaseDir().getParentFile(), "src");
        resolverB.addIvyPattern(IVY_PATTERN);
        resolverB.addArtifactPattern(baseDir +
                "/test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        settings2.addResolver(resolverB);

        IvySettings settings3 = new IvySettings();
        chainResolver = new ChainResolver();
        chainResolver.add(resolverA);
        chainResolver.add(resolverB);
        chainResolver.setSettings(settings3);
    }

    @After
    public void tearDown() throws Exception {
        CacheCleaner.deleteDir(cache);
    }

    @Test
    public void downloadFromResolversAndGetFromCache() throws Exception {
        // Resolve from first resolver.
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmrA =
                resolverA.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveData);
        assertNotNull(rmrA);
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmrA.getPublicationDate());

        // Resolve from the second resolver.
        ResolvedModuleRevision rmrB =
                resolverB.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveData);
        assertNotNull(rmrB);

        // make sure that the resolvers from the rmr's aren't equal (hence redownloaded).
        assertFalse(rmrA.getArtifactResolver().equals(rmrB.getArtifactResolver()));

        assertNotSame(rmrA, rmrB);


        ResolvedModuleRevision chainDep =
                chainResolver.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveData);
        // make sure that the resolver of the chain is the one that did the resolution
        assertEquals(chainDep.getArtifactResolver(), resolverA);

        // make sure that the dependency resources name (URL) are equal, and thus taken from the cache.
        assertEquals(chainDep, rmrA);
    }

    private RepositoryCacheManager newCacheManager(IvySettings settings) {
        WharfCacheManager cacheManager = new WharfCacheManager("cache", settings, new File("build/test/cache"));
        cacheManager.getMetadataHandler().setLockStrategy(new ArtifactLockStrategy());
        return cacheManager;
    }

}
