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

import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;

import java.io.File;
import java.io.IOException;

/**
 * @author Tomer Cohen
 */
public interface WharfResolver {

    long getAndCheck(Resource resource, File dest) throws IOException;

    long get(Resource resource, File dest) throws IOException;

    RepositoryCacheManager getRepositoryCacheManager();

    ResourceDownloader getDownloader();

    ArtifactResourceResolver getArtifactResourceResolver();
}
