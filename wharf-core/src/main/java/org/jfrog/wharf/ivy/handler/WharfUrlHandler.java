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

package org.jfrog.wharf.ivy.handler;

import org.apache.ivy.Ivy;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.BasicURLHandler;
import org.apache.ivy.util.url.IvyAuthenticator;
import org.apache.ivy.util.url.URLHandler;
import org.jfrog.wharf.ivy.checksum.Checksum;
import org.jfrog.wharf.ivy.checksum.ChecksumInputStream;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;
import org.jfrog.wharf.ivy.util.WharfCopyListener;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * @author Tomer Cohen
 */
public class WharfUrlHandler extends BasicURLHandler {

    public static interface TraceCounter {
        public void add(String query, int status);
    }

    public static TraceCounter tracer = null;

    private static final int BUFFER_SIZE = 64 * 1024;
    public static final WharfUrlInfo UNAVAILABLE = new WharfUrlInfo(false, 0, 0, "", "");


    private static final class HttpStatus {
        static final int SC_OK = 200;

        static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

        private HttpStatus() {
        }
    }

    @Override
    public WharfUrlInfo getURLInfo(URL url) {
        return getURLInfo(url, 0);
    }

    @Override
    public WharfUrlInfo getURLInfo(URL url, int timeout) {
        // Install the IvyAuthenticator
        if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
            IvyAuthenticator.install();
        }
        URLConnection con = null;
        try {
            url = normalizeToURL(url);
            con = url.openConnection();
            if (con instanceof HttpURLConnection) {
                con.setRequestProperty("User-Agent", "Wharf Ivy/" + Ivy.getIvyVersion());
                HttpURLConnection httpCon = (HttpURLConnection) con;
                if (getRequestMethod() == URLHandler.REQUEST_METHOD_HEAD) {
                    httpCon.setRequestMethod("HEAD");
                }
                if (checkStatusCode(url, httpCon)) {
                    String serverName = httpCon.getHeaderField("Server");
                    String sha1;
                    String md5;
                    if (serverName != null && serverName.startsWith("Artifactory/")) {
                        sha1 = getSha1FromHeader(httpCon);
                        if (sha1 == null) {
                            // force download of the artifact to populate checksums
                            Message.debug("No sha1 tag found");
                            File tempFile = File.createTempFile("temp", "orig");
                            try {
                                FileUtil.copy(url, tempFile, new WharfCopyListener());
                            } finally {
                                FileUtil.forceDelete(tempFile);
                            }
                            // get the checksum using extension to find file
                            sha1 = getSha1WithExtension(url);
                            md5 = getMd5WithExtension(url);
                        } else {
                            Message.debug("Sha1 tag found: " + sha1);
                            md5 = getMd5FromHeader(httpCon);
                        }
                    } else {
                        //For non-artifactory ask for the sha1/md5 files
                        sha1 = getSha1WithExtension(url);
                        md5 = getMd5WithExtension(url);
                    }
                    return new WharfUrlInfo(true, httpCon.getContentLength(), con.getLastModified(), sha1, md5);
                }
            } else {
                int contentLength = con.getContentLength();
                if (contentLength <= 0) {
                    return UNAVAILABLE;
                } else {
                    URL fileUrl = con.getURL();
                    String sha1 = getSha1WithExtension(fileUrl);
                    String md5 = getMd5WithExtension(fileUrl);
                    return new WharfUrlInfo(true, contentLength, con.getLastModified(), sha1, md5);
                }
            }
        } catch (UnknownHostException e) {
            Message.warn("Host " + e.getMessage() + " not found. url=" + url);
            Message.info("You probably access the destination server through "
                    + "a proxy server that is not well configured.");
        } catch (IOException e) {
            Message.error("Server access Error: " + e.getMessage() + " url=" + url);
        } finally {
            disconnect(con);
        }
        return UNAVAILABLE;
    }

    public void download(WharfUrlResource res, File dest, CopyProgressListener l) throws IOException {
        FileWithChecksumStreamHandler handler = new FileWithChecksumStreamHandler(dest, l);
        internalDownload(res.getUrl(), handler);
        for (Checksum checksum : handler.getChecksums()) {
            res.getActual().put(checksum.getType(), checksum.getChecksum());
        }
    }

    @Override
    public void download(URL src, File dest, CopyProgressListener l) throws IOException {
        internalDownload(src, new FileStreamHandler(dest, l));
    }

    private void internalDownload(URL src, StreamHandler handler) throws IOException {
        // Install the IvyAuthenticator
        if ("http".equals(src.getProtocol()) || "https".equals(src.getProtocol())) {
            IvyAuthenticator.install();
        }

        InputStream inStream = null;
        URLConnection srcConn = null;
        try {
            src = normalizeToURL(src);
            srcConn = src.openConnection();
            srcConn.setRequestProperty("User-Agent", "Apache Ivy/" + Ivy.getIvyVersion());
            srcConn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            if (srcConn instanceof HttpURLConnection) {
                HttpURLConnection httpCon = (HttpURLConnection) srcConn;
                if (!checkStatusCode(src, httpCon)) {
                    throw new IOException(
                            "The HTTP response code for " + src + " did not indicate a success."
                                    + " See log for more detail.");
                }
            }

            // do the download
            inStream = getDecodingInputStream(srcConn.getContentEncoding(), srcConn.getInputStream());
            handler.handleStream(srcConn, inStream);
        } finally {
            disconnect(srcConn);
        }
    }

    static interface StreamHandler {
        void handleStream(URLConnection srcConn, InputStream inStream) throws IOException;
    }

    static class StringStreamHandler implements StreamHandler {
        String content;

        public void handleStream(URLConnection srcConn, InputStream inStream) throws IOException {
            content = FileUtil.readEntirely(inStream);
        }

        public String getContent() {
            return content;
        }
    }

    static class FileStreamHandler implements StreamHandler {
        final File destFile;
        final CopyProgressListener progressListener;

        FileStreamHandler(File destFile, CopyProgressListener progressListener) {
            this.destFile = destFile;
            this.progressListener = progressListener;
        }

        public void handleStream(URLConnection srcConn, InputStream inStream) throws IOException {
            FileUtil.copy(inStream, destFile, progressListener);

            // check content length only if content was not encoded
            if (srcConn.getContentEncoding() == null) {
                int contentLength = srcConn.getContentLength();
                if (contentLength != -1 && destFile.length() != contentLength) {
                    destFile.delete();
                    throw new IOException(
                            "Downloaded file size doesn't match expected Content Length for " + srcConn.getURL()
                                    + ". Please retry.");
                }
            }

            // update modification date
            long lastModified = srcConn.getLastModified();
            if (lastModified > 0) {
                destFile.setLastModified(lastModified);
            }
        }
    }

    static class FileWithChecksumStreamHandler extends FileStreamHandler {
        private final Checksum[] checksums;

        FileWithChecksumStreamHandler(File destFile, CopyProgressListener progressListener) {
            super(destFile, progressListener);
            // On download always calculate all checksums
            ChecksumType[] checksumTypes = ChecksumType.values();
            checksums = new Checksum[checksumTypes.length];
            int i = 0;
            for (ChecksumType checksumType : checksumTypes) {
                checksums[i] = new Checksum(checksumType);
                i++;
            }
        }

        @Override
        public void handleStream(URLConnection srcConn, InputStream inStream) throws IOException {
            super.handleStream(srcConn, new ChecksumInputStream(inStream, checksums));
        }

        public Checksum[] getChecksums() {
            return checksums;
        }
    }

    private String getSha1WithExtension(URL url) throws IOException {
        return getChecksumFromExtraFile(ChecksumType.sha1, url);
    }

    private String getMd5WithExtension(URL url) throws IOException {
        return getChecksumFromExtraFile(ChecksumType.md5, url);
    }

    private String getChecksumFromExtraFile(ChecksumType checksumType, URL url) throws IOException {
        String checksumValue = null;
        String checksumUrl = url.toExternalForm() + checksumType.ext();
        Message.debug("Retrieving " + checksumType + " using: '" + checksumUrl + "'");
        URL newChecksumUrl = new URL(checksumUrl);
        if ("file".equals(newChecksumUrl.getProtocol())) {
            try {
                checksumValue = WharfUtils.getCleanChecksum(new File(newChecksumUrl.toURI()));
            } catch (URISyntaxException e) {
                Message.debug(checksumType.alg() + " not found at " + checksumUrl + " due to: " + e.getMessage());
            }
        } else {
            try {
                StringStreamHandler handler = new StringStreamHandler();
                internalDownload(newChecksumUrl, handler);
                checksumValue = WharfUtils.getCleanChecksum(handler.getContent());
            } catch (IOException e) {
                Message.debug(checksumType.alg() + " not found at " + checksumUrl + " due to: " + e.getMessage());
            }
        }
        return checksumValue;
    }

    public static class WharfUrlInfo extends URLInfo {
        private final String sha1;
        private final String md5;

        private WharfUrlInfo(boolean available, long contentLength, long lastModified, String sha1, String md5) {
            super(available, contentLength, lastModified);
            this.sha1 = sha1;
            this.md5 = md5;
        }

        public String getSha1() {
            return sha1;
        }

        public String getMd5() {
            return md5;
        }
    }

    private String getSha1FromHeader(HttpURLConnection httpCon) {
        String sha1 = httpCon.getHeaderField("X-Checksum-Sha1");
        if (sha1 == null) {
            sha1 = httpCon.getHeaderField("ETag");
        }
        return sha1;
    }

    private String getMd5FromHeader(HttpURLConnection httpCon) {
        return httpCon.getHeaderField("X-Checksum-Md5");
    }

    private void disconnect(URLConnection con) {
        if (con instanceof HttpURLConnection) {
            if (!"HEAD".equals(((HttpURLConnection) con).getRequestMethod())) {
                // We must read the response body before disconnecting!
                // Cfr. http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
                // [quote]Do not abandon a connection by ignoring the response body. Doing
                // so may results in idle TCP connections.[/quote]
                readResponseBody((HttpURLConnection) con);
            }

            ((HttpURLConnection) con).disconnect();
        } else if (con != null) {
            try {
                InputStream is = con.getInputStream();
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignored
            }
        }
    }

    /**
     * Read and ignore the response body.
     */
    private void readResponseBody(HttpURLConnection conn) {
        byte[] buffer = new byte[BUFFER_SIZE];

        InputStream inStream = null;
        try {
            inStream = conn.getInputStream();
            while (inStream.read(buffer) > 0) {
                //Skip content
            }
        } catch (IOException e) {
            // ignore
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        InputStream errStream = conn.getErrorStream();
        if (errStream != null) {
            try {
                while (errStream.read(buffer) > 0) {
                    //Skip content
                }
            } catch (IOException e) {
                // ignore
            } finally {
                try {
                    errStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }


    private boolean checkStatusCode(URL url, HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        if (tracer != null) {
            tracer.add(con.getRequestMethod() + " " + url.toExternalForm(), status);
        }
        if (status == HttpStatus.SC_OK) {
            return true;
        }
        Message.debug("HTTP response status: " + status + " url=" + url);
        if (status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
            Message.warn("Your proxy requires authentication.");
        } else if (String.valueOf(status).startsWith("4")) {
            Message.verbose("CLIENT ERROR: " + con.getResponseMessage() + " url=" + url);
        } else if (String.valueOf(status).startsWith("5")) {
            Message.error("SERVER ERROR: " + con.getResponseMessage() + " url=" + url);
        }
        return false;
    }
}
