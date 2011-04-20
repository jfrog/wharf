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
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author Tomer Cohen
 */
public interface WharfResolver {
    String DEFAULT_IVY_PATTERN = "[organisation]/[module]/ivy-[revision].xml";
    String DEFAULT_ART_PATTERN = "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]";

    // Methods from Ivy needed in the Wharf flow
    long getAndCheck(Resource resource, File dest) throws IOException;

    long get(Resource resource, File dest) throws IOException;

    RepositoryCacheManager getRepositoryCacheManager();

    void setRepository(Repository repository);

    Artifact fromSystem(Artifact artifact);

    ResolvedResource getArtifactRef(Artifact artifact, Date date);

    boolean supportsWrongSha1();

    WharfURLRepository getWharfUrlRepository();

    String[] getChecksumAlgorithms();
}
