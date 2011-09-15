package org.jfrog.wharf.ivy.lock;

/**
 * Date: 9/15/11
 * Time: 9:57 AM
 *
 * @author Fred Simon
 */
public interface LockLogger {
    boolean isDebugEnabled();

    void log(String msg);
}
