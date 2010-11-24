package org.jfrog.wharf.ivy.util;

import org.apache.ivy.util.CopyProgressEvent;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;

import java.io.File;
import java.io.IOException;

/**
 * @author Tomer Cohen
 */
public class WharfUtils {

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

}
