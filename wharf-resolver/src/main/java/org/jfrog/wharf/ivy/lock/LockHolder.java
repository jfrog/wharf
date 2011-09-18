package org.jfrog.wharf.ivy.lock;

import java.io.File;

/**
 * Date: 9/15/11
 * Time: 10:10 AM
 *
 * @author Fred Simon
 */
public interface LockHolder {
    void releaseLock();

    boolean acquireLock();

    File getLockFile();

    File getProtectedFile();

    String stateMessage();
}
