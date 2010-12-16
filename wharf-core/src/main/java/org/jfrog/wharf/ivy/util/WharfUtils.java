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

package org.jfrog.wharf.ivy.util;

import org.apache.ivy.util.*;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author Tomer Cohen
 */
public class WharfUtils {

    public static final String SHA1_ALGORITHM = "sha1";
    public static final String MD5_ALGORITHM = "md5";

    public static String getChecksumAlgorithm() {
        return SHA1_ALGORITHM;
    }

    private enum OperatingSystem {
        OLD_WINDOWS {
            @Override
            void copyCacheFile(File src, File dest) throws IOException {
                FileUtil.copy(src, dest, new WharfCopyListener(), true);
            }
        },
        NEW_WINDOWS {
            @Override
            void copyCacheFile(File src, File dest) throws IOException {
                WindowsUtils.windowsSymlink(src, dest, new WharfCopyListener(), true);
            }
        },
        OS_X, OTHER;

        void copyCacheFile(File src, File dest) throws IOException {
            FileUtil.symlink(src, dest, new WharfCopyListener(), true);
        }
    }

    private static final OperatingSystem OS;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            if ((!osName.contains("vista") || !osName.contains("7"))) {
                OS = OperatingSystem.OLD_WINDOWS;
            } else {
                OS = OperatingSystem.NEW_WINDOWS;
            }
        } else if (osName.contains("mac os x")) {
            OS = OperatingSystem.OS_X;
        } else {
            OS = OperatingSystem.OTHER;
        }
    }

    public static void copyCacheFile(File src, File dest) throws IOException {
        OS.copyCacheFile(src, dest);
    }

    private static class WharfCopyListener implements CopyProgressListener {

        @Override
        public void start(CopyProgressEvent evt) {
            Message.info(evt.toString());
        }

        @Override
        public void progress(CopyProgressEvent evt) {
        }

        @Override
        public void end(CopyProgressEvent evt) {
        }
    }

    public static String getCleanChecksum(File checksumFile) throws IOException {
        String csFileContent = FileUtil.readEntirely(
                new BufferedReader(new FileReader(checksumFile))).trim().toLowerCase(Locale.US);
        return getCleanChecksum(csFileContent);
    }

    public static String getCleanChecksum(String checksum) {
        String cleanChecksum;
        if (checksum.indexOf(' ') > -1
                && (checksum.startsWith("md") || checksum.startsWith("sha"))) {
            int lastSpaceIndex = checksum.lastIndexOf(' ');
            cleanChecksum = checksum.substring(lastSpaceIndex + 1);
        } else {
            int spaceIndex = checksum.indexOf(' ');
            if (spaceIndex != -1) {
                cleanChecksum = checksum.substring(0, spaceIndex);
                // IVY-1155: support some strange formats like this one:
                // http://repo2.maven.org/maven2/org/apache/pdfbox/fontbox/0.8.0-incubator/fontbox-0.8.0-incubator.jar.md5
                if (cleanChecksum.endsWith(":")) {
                    StringBuffer result = new StringBuffer();
                    char[] chars = checksum.substring(spaceIndex + 1).toCharArray();
                    for (char aChar : chars) {
                        if (!Character.isWhitespace(aChar)) {
                            result.append(aChar);
                        }
                    }
                    cleanChecksum = result.toString();
                }
            } else {
                cleanChecksum = checksum;
            }
        }
        return cleanChecksum;
    }

    public static String computeUUID(String content) {
        String algorithm = MD5_ALGORITHM;
        if (!ChecksumHelper.isKnownAlgorithm(algorithm)) {
            throw new IllegalArgumentException("unknown algorithm " + algorithm);
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();
            byte[] bytes = content.trim().toLowerCase(Locale.US).getBytes("UTF-8");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return ChecksumHelper.byteArrayToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            // Impossible
            throw new IllegalArgumentException("unknown algorithm " + algorithm, e);
        } catch (UnsupportedEncodingException e) {
            // Impossible except with IBM :)
            throw new IllegalArgumentException("unknown charset UTF-8", e);
        }
    }
}
