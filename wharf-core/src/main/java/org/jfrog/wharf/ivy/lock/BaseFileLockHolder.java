package org.jfrog.wharf.ivy.lock;

import java.io.File;

/**
 * Date: 9/15/11
 * Time: 10:43 AM
 *
 * @author Fred Simon
 */
public abstract class BaseFileLockHolder implements InternalLockHolder {
    protected final WharfLockFactory factory;
    protected final File protectedFile;
    protected final File lockFile;

    protected String lastMessage;

    protected BaseFileLockHolder(WharfLockFactory factory, File protectedFile) {
        this.protectedFile = protectedFile;
        this.factory = factory;
        this.lockFile = new File(protectedFile + WharfLockFactory.LOCK_FILE_SUFFIX);
        File dir = lockFile.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create directory " + dir.getAbsolutePath() +
                        " for file " + protectedFile.getAbsolutePath());
            }
        }
    }

    @Override
    public File getProtectedFile() {
        return protectedFile;
    }

    @Override
    public File getLockFile() {
        return lockFile;
    }

    @Override
    public WharfLockFactory getFactory() {
        return factory;
    }

    @Override
    public LockLogger getLogger() {
        return factory.getLogger();
    }

    @Override
    public String toString() {
        return "LockHolder on '" + lockFile + "'";
    }

    @Override
    public String stateMessage() {
        return "LockHolder on '" + lockFile + "' says " + lastMessage;
    }

    @Override
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public void appendLastMessage(String lastMessage) {
        this.lastMessage += lastMessage;
    }
}
