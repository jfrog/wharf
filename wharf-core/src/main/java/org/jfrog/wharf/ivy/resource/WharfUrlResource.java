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

package org.jfrog.wharf.ivy.resource;

import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.file.FileResource;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.handler.WharfUrlHandler;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;

/**
 * @author Tomer Cohen
 */
public class WharfUrlResource implements Resource {
    private URL url;

    private boolean init = false;

    private long lastModified;

    private long contentLength;

    private boolean exists;

    private final EnumMap<ChecksumType, String> remote;

    private final EnumMap<ChecksumType, String> actual;

    public WharfUrlResource(URL url) {
        this.url = url;
        remote = new EnumMap<ChecksumType, String>(ChecksumType.class);
        actual = new EnumMap<ChecksumType, String>(ChecksumType.class);
    }

    public WharfUrlResource(Resource resource) {
        if (resource instanceof FileResource) {
            try {
                url = ((FileResource) resource).getFile().toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed File URL", e);
            }
            remote = new EnumMap<ChecksumType, String>(ChecksumType.class);
            actual = new EnumMap<ChecksumType, String>(ChecksumType.class);
        } else if (resource instanceof URLResource) {
            url = ((URLResource) resource).getURL();
            remote = new EnumMap<ChecksumType, String>(ChecksumType.class);
            actual = new EnumMap<ChecksumType, String>(ChecksumType.class);
        } else if (resource instanceof WharfUrlResource) {
            WharfUrlResource wharfUrlResource = (WharfUrlResource) resource;
            this.url = wharfUrlResource.url;
            this.init = wharfUrlResource.init;
            this.lastModified = wharfUrlResource.lastModified;
            this.contentLength = wharfUrlResource.contentLength;
            this.exists = wharfUrlResource.exists;
            this.remote = new EnumMap<ChecksumType, String>(wharfUrlResource.remote);
            this.actual = new EnumMap<ChecksumType, String>(wharfUrlResource.actual);
        } else {
            throw new IllegalArgumentException("Wharf Downloader manage only URL and Files");
        }
    }

    public String getName() {
        return url.toExternalForm();
    }

    public Resource clone(String cloneName) {
        try {
            return new WharfUrlResource(new URL(cloneName));
        } catch (MalformedURLException e) {
            try {
                return new WharfUrlResource(new File(cloneName).toURI().toURL());
            } catch (MalformedURLException e1) {
                throw new IllegalArgumentException(
                        "bad clone name provided: not suitable for an URLResource: " + cloneName);
            }
        }
    }

    public long getLastModified() {
        if (!init) {
            init();
        }
        return lastModified;
    }

    private void init() {
        WharfUrlHandler.WharfUrlInfo info = WharfUtils.getWharfUrlHandler().getURLInfo(url);
        contentLength = info.getContentLength();
        lastModified = info.getLastModified();
        exists = info.isReachable();
        if (info.getSha1() != null) {
            remote.put(ChecksumType.sha1, WharfUtils.getCleanChecksum(info.getSha1()));
        }
        if (info.getMd5() != null) {
            remote.put(ChecksumType.md5, WharfUtils.getCleanChecksum(info.getMd5()));
        }
        init = true;
    }

    public long getContentLength() {
        if (!init) {
            init();
        }
        return contentLength;
    }

    public URL getUrl() {
        return url;
    }

    public String getSha1() {
        if (!init) {
            init();
        }
        return remote.get(ChecksumType.sha1);
    }

    public String getMd5() {
        if (!init) {
            init();
        }
        return remote.get(ChecksumType.md5);
    }

    public boolean exists() {
        if (!init) {
            init();
        }
        return exists;
    }

    public String toString() {
        return getName();
    }

    public boolean isLocal() {
        return false;
    }

    public InputStream openStream() throws IOException {
        return URLHandlerRegistry.getDefault().openStream(url);
    }

    public EnumMap<ChecksumType, String> getRemote() {
        return remote;
    }

    public EnumMap<ChecksumType, String> getActual() {
        return actual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WharfUrlResource)) return false;

        WharfUrlResource that = (WharfUrlResource) o;

        if (contentLength != that.contentLength) return false;
        if (exists != that.exists) return false;
        if (init != that.init) return false;
        if (lastModified != that.lastModified) return false;
        if (!remote.equals(that.remote)) return false;
        if (!actual.equals(that.actual)) return false;
        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (init ? 1 : 0);
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (int) (contentLength ^ (contentLength >>> 32));
        result = 31 * result + (exists ? 1 : 0);
        result = 31 * result + remote.hashCode();
        result = 31 * result + actual.hashCode();
        return result;
    }
}
