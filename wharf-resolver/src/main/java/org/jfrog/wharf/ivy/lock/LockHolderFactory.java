package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.lock.LockStrategy;

import java.io.Closeable;
import java.io.File;

/**
 * Date: 9/15/11
 * Time: 5:07 PM
 *
 * @author Fred Simon
 */
public interface LockHolderFactory extends IvySettingsAware, Closeable {
    LockLogger getLogger();

    long getTimeoutInMs();

    long getSleepTimeInMs();

    String getLockFileSuffix();

    LockHolder getLockHolder(File protectedFile);

    LockHolder getOrCreateLockHolder(File protectedFile);
}
