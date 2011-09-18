package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Message;

/**
 * Date: 9/15/11
 * Time: 10:03 AM
 *
 * @author Fred Simon
 */
public class LockLoggerImpl implements LockLogger {

    private boolean debugLocking;

    public LockLoggerImpl(IvySettings settings) {
        if (settings != null) {
            debugLocking = settings.debugLocking();
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return debugLocking;
    }

    @Override
    public void log(String msg) {
        Message.info(Thread.currentThread() + " " + System.currentTimeMillis() + " " + msg);
    }
}
