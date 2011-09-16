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

package org.jfrog.wharf.ivy.file;


import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
import org.jfrog.wharf.ivy.IvySettingsTestHolder;
import org.junit.Before;
import org.junit.Test;

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
public class WharfCacheManagerMultipleResolverTest extends AbstractDependencyResolverTest {
    private FileSystemResolver resolverA;
    private FileSystemResolver resolverB;
    private ChainResolver chainResolver;
    private ResolveData resolveDataA;
    private ResolveData resolveDataB;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        IvySettingsTestHolder holder1 = createNewSettings();
        resolverA = new FileSystemResolver();
        resolverA.setRepositoryCacheManager(holder1.cacheManager);
        resolverA.setName("testA");
        resolverA.setSettings(holder1.settings);
        resolverA.addIvyPattern(repoTestRoot.getAbsolutePath() + FS + REL_IVY_PATTERN);
        resolverA.addArtifactPattern(repoTestRoot.getAbsolutePath() +
                "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        holder1.settings.addResolver(resolverA);
        resolveDataA = holder1.data;

        IvySettingsTestHolder holder2 = createNewSettings();
        resolverB = new FileSystemResolver();
        resolverB.setRepositoryCacheManager(holder2.cacheManager);
        resolverB.setName("testB");
        resolverA.setSettings(holder2.settings);
        resolverB.addIvyPattern(repoTestRoot.getAbsolutePath() + FS + REL_IVY_PATTERN);
        resolverB.addArtifactPattern(repoTestRoot.getAbsolutePath() +
                "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        holder2.settings.addResolver(resolverB);
        resolveDataB = holder2.data;

        chainResolver = new ChainResolver();
        chainResolver.add(resolverA);
        // TODO: Check if this is possible???? Chaining from separate settings???
        //        chainResolver.add(resolverB);
        chainResolver.setSettings(holder1.settings);
        holder1.settings.addResolver(chainResolver);
    }

    @Test
    public void downloadFromResolversAndGetFromCache() throws Exception {
        // Resolve from first resolver.
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmrA =
                resolverA.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveDataA);
        assertNotNull(rmrA);
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmrA.getPublicationDate());

        // Resolve from the second resolver.
        ResolvedModuleRevision rmrB =
                resolverB.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveDataB);
        assertNotNull(rmrB);

        // make sure that the resolvers from the rmr's aren't equal (hence redownloaded).
        assertFalse(rmrA.getArtifactResolver().equals(rmrB.getArtifactResolver()));

        assertNotSame(rmrA, rmrB);


        ResolvedModuleRevision chainDep =
                chainResolver.getDependency(new DefaultDependencyDescriptor(mrid, false), resolveDataA);
        // make sure that the resolver of the chain is the one that did the resolution
        assertEquals(chainDep.getArtifactResolver(), resolverA);

        // make sure that the dependency resources name (URL) are equal, and thus taken from the cache.
        assertEquals(chainDep, rmrA);
    }
}
