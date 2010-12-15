package org.jfrog.wharf.ivy.util;

import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for windows
 *
 * @author Tomer Cohen
 */
public abstract class WindowsUtils {

    // utility class
    private WindowsUtils() {
    }

    /**
     * Create a windows style symbolic link, which is slightly different than the linux symbolic links. The commands
     * that can be run in an mklink can be seen <a href="http://technet.microsoft.com/en-us/library/cc753194(WS.10).aspx">here</a>.
     * This operation will only work on Windows Vista and up, attempting to use this on Windows XP and below will result
     * in an exception and will perform a regular copy.
     *
     * @param src       The source file
     * @param dest      The destination file
     * @param l         A copy progress listener to be used to listen to operations during the mklink/copy
     * @param overwrite A flag that if set to true, and the destination file already exists, then the operation will
     *                  stop and the destination file will not be deleted.
     * @throws IOException Will be thrown should anything happen during the mklink process on the filesystem.
     */
    public static void windowsSymlink(File src, File dest, CopyProgressListener l, boolean overwrite)
            throws IOException {
        try {
            if (dest.exists()) {
                if (!dest.isFile()) {
                    throw new IOException("impossible to copy: destination is not a file: " + dest);
                }
                if (!overwrite) {
                    Message.verbose(dest + " already exists, nothing done");
                    return;
                }
            }
            if (dest.getParentFile() != null) {
                dest.getParentFile().mkdirs();
            }

            Runtime runtime = Runtime.getRuntime();
            Message.verbose("executing 'mklink " + src.getAbsolutePath() + " " + dest.getPath()
                    + "'");
            Process process = runtime.exec(new String[]{"mklink", src.getAbsolutePath(),
                    dest.getPath()});

            if (process.waitFor() != 0) {
                InputStream errorStream = process.getErrorStream();
                InputStreamReader isr = new InputStreamReader(errorStream);
                BufferedReader br = new BufferedReader(isr);

                StringBuffer error = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                    error.append('\n');
                }

                throw new IOException("error performing mklink " + src + " to " + dest + ":\n" + error);
            }

            // check if the creation of the symbolic link was successful
            if (!dest.exists()) {
                throw new IOException("error performing mklink: " + dest + " doesn't exists");
            }

            // check if the result is a true symbolic link
            if (dest.getAbsolutePath().equals(dest.getCanonicalPath())) {
                dest.delete(); // just make sure we do delete the invalid symlink!
                throw new IOException("error mklink: " + dest + " isn't a symlink");
            }
        } catch (IOException x) {
            Message.verbose("mklink failed; falling back to copy");
            StringWriter buffer = new StringWriter();
            x.printStackTrace(new PrintWriter(buffer));
            Message.debug(buffer.toString());
            FileUtil.copy(src, dest, l, overwrite);
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
    }
}
