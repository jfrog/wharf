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

import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class WharfResolverMetadata implements Serializable {

    public String id;

    public String name;

    public String type;

    public boolean m2compatible;

    public String ivyPattern;

    public String artifactPattern;

    public String user;

    public Map<String, String> params;

    public String[] checksumAlgorithms;

    public String authentication;

    public String proxy;

    public WharfResolverMetadata() {
    }

    public WharfResolverMetadata(DependencyResolver resolver) {
        this.name = resolver.getName();
        if (resolver instanceof AbstractResolver) {
            this.type = ((AbstractResolver) resolver).getTypeName();
        } else {
            this.type = resolver.getClass().getName();
        }
        if (resolver instanceof BasicResolver) {
            this.checksumAlgorithms = ((BasicResolver) resolver).getChecksumAlgorithms();
        }
        if (resolver instanceof AbstractPatternsBasedResolver) {
            AbstractPatternsBasedResolver patternsBasedResolver = (AbstractPatternsBasedResolver) resolver;
            this.m2compatible = patternsBasedResolver.isM2compatible();
            List<String> patterns = patternsBasedResolver.getIvyPatterns();
            if (patterns.isEmpty()) {
                this.ivyPattern = "";
            } else if (patterns.size() == 1) {
                this.ivyPattern = patterns.get(0);
            } else {
                StringBuilder builder = new StringBuilder();
                for (String pattern : patterns) {
                    builder.append(pattern).append(",");
                }
                this.ivyPattern = builder.toString();
            }
            patterns = patternsBasedResolver.getArtifactPatterns();
            if (patterns.isEmpty()) {
                this.artifactPattern = "";
            } else if (patterns.size() == 1) {
                this.artifactPattern = patterns.get(0);
            } else {
                StringBuilder builder = new StringBuilder();
                for (String pattern : patterns) {
                    builder.append(pattern).append(",");
                }
                this.artifactPattern = builder.toString();
            }
        }
        // TODO: Find the user
    }

    public String getId() {
        if (WharfUtils.isEmptyString(id)) {
            String idString = type + name + ivyPattern + artifactPattern + params + user;
            id = WharfUtils.computeUUID(idString);
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WharfResolverMetadata that = (WharfResolverMetadata) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
