package org.jfrog.wharf.ivy.lock;

import java.io.IOException;

/**
 * Date: 9/15/11
 * Time: 11:18 AM
 *
 * @author Fred Simon
 */
public class WaitingLockHolder extends LockHolderDelegator {

    public WaitingLockHolder(InternalLockHolder delegate) {
        super(delegate);
    }

    @Override
    public void releaseLock() {
        delegate.releaseLock();
    }

    @Override
    public boolean acquireLock() {
        long start = System.currentTimeMillis();
        do {
            if (delegate.acquireLock()) {
                delegate.appendLastMessage(" waiting lock acquired in " + (System.currentTimeMillis() - start) + "ms");
                if (delegate.getLogger().isDebugEnabled()) {
                    delegate.getLogger().log(stateMessage());
                }
                return true;
            }
            try {
                Thread.sleep(delegate.getFactory().getSleepTimeInMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // reset interrupt status
                String message = stateMessage() + " waiting lock interrupted after waiting for " +
                        (System.currentTimeMillis() - start) + "ms :" + e.getMessage();
                delegate.setLastMessage(message);
                throw new RuntimeException(stateMessage(), e);
            }
        } while (System.currentTimeMillis() - start < delegate.getFactory().getTimeoutInMs());
        delegate.appendLastMessage(" waiting lock timeout waiting for " +
                (System.currentTimeMillis() - start) + "ms");
        return false;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
