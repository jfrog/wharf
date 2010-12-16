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

package org.jfrog.wharf.ivy.model;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class ModuleRevisionMetadata {

    public String latestResolvedRevision;
    public String latestResolvedTime;

    public Set<ArtifactMetadata> artifactMetadata = new HashSet<ArtifactMetadata>();


    public Set<ArtifactMetadata> getArtifactMetadata() {
        return artifactMetadata;
    }

    public void setArtifactMetadata(Set<ArtifactMetadata> artifactMetadata) {
        this.artifactMetadata = artifactMetadata;
    }

    public String getLatestResolvedRevision() {
        return latestResolvedRevision;
    }

    public void setLatestResolvedRevision(String latestResolvedRevision) {
        this.latestResolvedRevision = latestResolvedRevision;
    }

    public String getLatestResolvedTime() {
        return latestResolvedTime;
    }

    public void setLatestResolvedTime(String latestResolvedTime) {
        this.latestResolvedTime = latestResolvedTime;
    }
}
