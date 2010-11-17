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
package org.jfrog.wharf.ivy;

import junit.framework.Assert;
import org.apache.ivy.core.search.ModuleEntry;
import org.apache.ivy.core.search.OrganisationEntry;
import org.apache.ivy.core.search.RevisionEntry;
import org.apache.ivy.plugins.resolver.DependencyResolver;

import java.util.Arrays;

/**
 *
 */
public class ResolverTestHelper {

    public static void assertOrganisationEntriesContains(DependencyResolver resolver, String[] orgNames,
            OrganisationEntry[] orgs) {
        Assert.assertNotNull(orgs);
        for (String orgName : orgNames) {
            boolean found = false;
            for (OrganisationEntry org : orgs) {
                if (orgName.equals(org.getOrganisation())) {
                    found = true;
                    Assert.assertEquals(resolver, org.getResolver());
                }
            }
            Assert.assertTrue("organisation not found: " + orgName, found);
        }
    }

    public static void assertModuleEntries(DependencyResolver resolver, OrganisationEntry org,
            String[] names, ModuleEntry[] mods) {
        Assert.assertNotNull(mods);
        Assert.assertEquals("invalid module entries: unmatched number: expected: "
                + Arrays.asList(names) + " but was " + Arrays.asList(mods),
                names.length, mods.length);
        assertModuleEntriesContains(resolver, org, names, mods);
    }

    static void assertModuleEntriesContains(DependencyResolver resolver, OrganisationEntry org,
            String[] names, ModuleEntry[] mods) {
        Assert.assertNotNull(mods);
        for (String name : names) {
            boolean found = false;
            for (ModuleEntry mod : mods) {
                if (name.equals(mod.getModule())) {
                    found = true;
                    Assert.assertEquals(resolver, mod.getResolver());
                    Assert.assertEquals(org, mod.getOrganisationEntry());
                }
            }
            Assert.assertTrue("module not found: " + name, found);
        }
    }

    public static void assertRevisionEntries(DependencyResolver resolver, ModuleEntry mod, String[] names,
            RevisionEntry[] revs) {
        Assert.assertNotNull(revs);
        Assert.assertEquals("invalid revision entries: unmatched number: expected: "
                + Arrays.asList(names) + " but was " + Arrays.asList(revs),
                names.length, revs.length);
        assertRevisionEntriesContains(resolver, mod, names, revs);
    }

    static void assertRevisionEntriesContains(DependencyResolver resolver, ModuleEntry mod,
            String[] names, RevisionEntry[] revs) {
        Assert.assertNotNull(revs);
        for (String name : names) {
            boolean found = false;
            for (RevisionEntry rev : revs) {
                if (name.equals(rev.getRevision())) {
                    found = true;
                    Assert.assertEquals(resolver, rev.getResolver());
                    Assert.assertEquals(mod, rev.getModuleEntry());
                }
            }
            Assert.assertTrue("revision not found: " + name, found);
        }
    }

    public static OrganisationEntry getEntry(OrganisationEntry[] orgs, String name) {
        for (OrganisationEntry org : orgs) {
            if (name.equals(org.getOrganisation())) {
                return org;
            }
        }
        Assert.fail("organisation not found: " + name);
        return null; // for compilation only
    }

    public static ModuleEntry getEntry(ModuleEntry[] mods, String name) {
        for (ModuleEntry mod : mods) {
            if (name.equals(mod.getModule())) {
                return mod;
            }
        }
        Assert.fail("module not found: " + name);
        return null; // for compilation only
    }

}
