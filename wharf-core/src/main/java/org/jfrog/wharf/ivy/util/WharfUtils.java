package org.jfrog.wharf.ivy.util;

import org.apache.ivy.util.ChecksumHelper;
import org.apache.ivy.util.CopyProgressEvent;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author Tomer Cohen
 */
public class WharfUtils {

    public static final String SHA1_ALGORITHM = "sha1";
    public static final String MD5_ALGORITHM = "md5";

    public static final String getChecksumAlgoritm() {
        return SHA1_ALGORITHM;
    }

    private enum OperatingSystem {
        WINDOWS {
            @Override
            void copyCacheFile(File src, File dest) throws IOException {
                FileUtil.copy(src, dest, new WharfCopyListener(), true);
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
            OS = OperatingSystem.WINDOWS;
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
