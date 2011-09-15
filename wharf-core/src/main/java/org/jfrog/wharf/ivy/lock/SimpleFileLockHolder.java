package org.jfrog.wharf.ivy.lock;

import java.io.File;
import java.io.IOException;

/**
 * Date: 9/15/11
 * Time: 10:13 AM
 *
 * @author Fred Simon
 */
public class SimpleFileLockHolder extends BaseFileLockHolder {

    private boolean lockAcquired = false;

    public SimpleFileLockHolder(WharfLockFactory factory, File protectedFile) {
        super(factory, protectedFile);
    }

    @Override
    public void releaseLock() {
        if (lockAcquired) {
            if (!lockFile.exists()) {
                appendLastMessage(" acquired lock file " + lockFile.getAbsolutePath() + " but not present on release!");
            }
            if (!lockFile.delete()) {
                appendLastMessage(" could not delete lock file!");
            }
        }
        lockAcquired = false;
    }

    @Override
    public boolean acquireLock() {
        try {
            lockAcquired = lockFile.createNewFile();
            if (lockAcquired) {
                lockFile.deleteOnExit();
            }
        } catch (IOException e) {
            lockAcquired = false;
            appendLastMessage(" IOException while acquire lock file:" + e.getMessage());
        }
        return lockAcquired;
    }

    @Override
    public void close() throws IOException {
        releaseLock();
    }
}
