package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.core.settings.IvySettings;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Date: 9/15/11
 * Time: 5:09 PM
 *
 * @author Fred Simon
 */
public abstract class AbstractLockHolderFactory implements LockHolderFactory {

    private static final String LOCK_FILE_SUFFIX = ".lck";
    private static final long DEFAULT_SLEEP_TIME = 400;
    private static final long DEFAULT_TIMEOUT = 15 * 1000;

    // Lock holder per Ivy settings
    private final ConcurrentMap<String, LockHolder> locks = new ConcurrentHashMap<String, LockHolder>();

    private long sleepTimeInMs;
    private long timeoutInMs;
    private LockLogger logger;

    public AbstractLockHolderFactory() {
        sleepTimeInMs = DEFAULT_SLEEP_TIME;
        timeoutInMs = DEFAULT_TIMEOUT;
        logger = new LockLoggerImpl(null);
    }

    @Override
    public void setSettings(IvySettings settings) {
        logger = new LockLoggerImpl(settings);
    }

    @Override
    public LockLogger getLogger() {
        return logger;
    }

    @Override
    public String getLockFileSuffix() {
        return LOCK_FILE_SUFFIX;
    }

    @Override
    public long getTimeoutInMs() {
        return timeoutInMs;
    }

    @Override
    public long getSleepTimeInMs() {
        return sleepTimeInMs;
    }

    public void setSleepTimeInMs(long sleepTimeInMs) {
        this.sleepTimeInMs = sleepTimeInMs;
    }

    public void setTimeoutInMs(long timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }

    public void setLogger(LockLogger logger) {
        this.logger = logger;
    }

    @Override
    public void close() throws IOException {
        try {
            for (LockHolder lockHolder : locks.values()) {
                if (lockHolder instanceof Closeable) {
                    ((Closeable) lockHolder).close();
                }
            }
        } finally {
            locks.clear();
        }
    }

    @Override
    public LockHolder getOrCreateLockHolder(File protectedFile) {
        String path = protectedFile.getAbsolutePath();
        LockHolder lockHolder = locks.get(path);
        if (lockHolder == null) {
            lockHolder = createLockHolder(protectedFile);
            LockHolder oldLockHolder = locks.putIfAbsent(path, lockHolder);
            if (oldLockHolder != null) {
                // Ignore the one created always take the old one
                lockHolder = oldLockHolder;
            }
        }
        return lockHolder;
    }

    @Override
    public LockHolder getLockHolder(File protectedFile) {
        return locks.get(protectedFile.getAbsolutePath());
    }

    protected abstract LockHolder createLockHolder(File protectedFile);
}
