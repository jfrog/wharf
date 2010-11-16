package org.jfrog.wharf.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Tomer Cohen
 */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


/**
 * @see org.apache.ivy.core.cache.DefaultResolutionCacheManager
 */
public class WharfCacheManagerTest {
    private WharfCacheManager cacheManager;

    private Artifact artifact;

    private ArtifactOrigin origin;
    private IvySettings settings;

    @Before
    public void setUp() throws Exception {
        File f = File.createTempFile("ivycache", ".dir");
        Ivy ivy = new Ivy();
        ivy.configureDefault();
        settings = ivy.getSettings();
        f.delete(); // we want to use the file as a directory, so we delete the file itself
        cacheManager = new WharfCacheManager();
        cacheManager.setSettings(settings);
        cacheManager.setBasedir(f);
        artifact = createArtifact("org", "module", "rev", "name", "type", "ext");
        origin = new ArtifactOrigin(artifact, true, "/some/where");
        cacheManager.saveArtifactOrigin(artifact, origin, settings.getDefaultResolver());
    }

    @After
    public void tearDown() throws Exception {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(cacheManager.getRepositoryCacheRoot());
        del.execute();
    }

    @Test
    public void testArtifactOrigin() {
        ArtifactOrigin found = cacheManager.getSavedArtifactOrigin(artifact);
        assertEquals(origin, found);

        artifact = createArtifact("org", "module", "rev", "name", "type2", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));
    }

    @Test
    public void testUniqueness() {
        cacheManager.saveArtifactOrigin(artifact, origin, settings.getDefaultResolver());

        artifact = createArtifact("org1", "module", "rev", "name", "type", "ext");
        ArtifactOrigin found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module1", "rev", "name", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev1", "name", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name1", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name", "type1", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name", "type", "ext1");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));
    }

    protected Artifact createArtifact(String org, String module, String rev, String name,
            String type, String ext) {
        ModuleId mid = new ModuleId(org, module);
        ModuleRevisionId mrid = new ModuleRevisionId(mid, rev);
        return new DefaultArtifact(mrid, new Date(), name, type, ext);
    }

}



