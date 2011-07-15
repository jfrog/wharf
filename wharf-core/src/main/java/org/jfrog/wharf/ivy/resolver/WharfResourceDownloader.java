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

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;

import java.io.File;
import java.io.IOException;

/**
 * @author Tomer Cohen
 */
public class WharfResourceDownloader implements ResourceDownloader {

    private final WharfResolver resolver;

    public WharfResourceDownloader(WharfResolver resolver) {
        this.resolver = resolver;
    }

    public void download(Artifact artifact, Resource resource, File dest) throws IOException {
        if (!(resource instanceof WharfUrlResource)) {
            throw new IllegalArgumentException("The Wharf Resolver manage only WharfUrlResource");
        }
        //TODO: [by fs] The part is in the wrong place, there should be a part only in storage download not here
        if (dest.exists()) {
            dest.delete();
        }
        File part = new File(dest.getAbsolutePath() + ".part");
        if (resource.getName().equals(String.valueOf(artifact.getUrl()))) {
            //TODO: [by tc] Check why in a wharf env this should not happen => throw exception
            throw new IllegalStateException("Don't know what this means!");
        } else {
            resolver.getAndCheck(resource, part);
        }
        if (!part.renameTo(dest)) {
            throw new IOException("impossible to move part file to definitive one: " + part + " -> " + dest);
        }
    }
}
