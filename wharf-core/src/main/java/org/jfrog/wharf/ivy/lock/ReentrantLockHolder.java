package org.jfrog.wharf.ivy.lock;

import java.io.IOException;

/**
 * Date: 9/15/11
 * Time: 11:05 AM
 *
 * @author Fred Simon
 */
public class ReentrantLockHolder extends LockHolderDelegator {
    private int counter;

    public ReentrantLockHolder(InternalLockHolder delegate) {
        super(delegate);
        this.counter = 0;
    }

    @Override
    public synchronized void releaseLock() {
        if (--counter == 0) {
            delegate.releaseLock();
        }
    }

    @Override
    public synchronized boolean acquireLock() {
        if (counter < 0) {
            // Dead lock holder
            delegate.appendLastMessage(" reentrant lock already closed!");
            return false;
        }
        boolean acquired = true;
        if (++counter == 1) {
            acquired = delegate.acquireLock();
        }
        if (acquired) {
            delegate.appendLastMessage(" reentrant lock holds = " + counter);
        } else {
            delegate.appendLastMessage(" reentrant lock was not acquired!");
            counter--;
        }
        if (delegate.getLogger().isDebugEnabled()) {
            delegate.getLogger().log(delegate.stateMessage());
        }
        return acquired;
    }

    @Override
    public synchronized void close() throws IOException {
        counter = -1;
        delegate.close();
    }
}
