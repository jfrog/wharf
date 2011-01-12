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

package org.jfrog.wharf.marahsller.jackson;

import org.jfrog.wharf.ivy.AbstractDependencyResolverTest;
import org.jfrog.wharf.ivy.marshall.jackson.WharfJacksonResolverMarshallerImpl;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfJacksonResolverMarshallerTest {

    WharfJacksonResolverMarshallerImpl wharfJacksonResolverMarshaller = new WharfJacksonResolverMarshallerImpl();
    private File cacheDir;

    @Before
    public void setup() {
        cacheDir = new File("build/test/cache");
    }

    @After
    public void tearDown() {
        AbstractDependencyResolverTest.deleteCacheFolder(cacheDir);
    }

    @Test
    public void saveAndRead() {
        Set<WharfResolverMetadata> wharfResolverMetadatas = new HashSet<WharfResolverMetadata>();
        WharfResolverMetadata metadataA = new WharfResolverMetadata();
        metadataA.name = "a";
        metadataA.type = "typeA";
        metadataA.authentication = "auth";
        metadataA.proxy = "proxy";
        wharfResolverMetadatas.add(metadataA);

        WharfResolverMetadata metadataB = new WharfResolverMetadata();
        metadataB.name = "b";
        metadataB.type = "typeB";
        metadataB.authentication = "authB";
        metadataB.proxy = "proxyB";
        wharfResolverMetadatas.add(metadataB);
        wharfJacksonResolverMarshaller.save(cacheDir, wharfResolverMetadatas);

        Set<WharfResolverMetadata> metadatas = wharfJacksonResolverMarshaller.getWharfMetadatas(cacheDir);
        Iterator<WharfResolverMetadata> iterator = metadatas.iterator();
        Assert.assertEquals(2, metadatas.size());
        Assert.assertEquals(metadataA, iterator.next());
        Assert.assertEquals(metadataB, iterator.next());
    }
}
