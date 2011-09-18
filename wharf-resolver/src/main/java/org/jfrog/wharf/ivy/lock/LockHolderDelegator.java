package org.jfrog.wharf.ivy.lock;

import java.io.File;

/**
 * Date: 9/15/11
 * Time: 12:28 PM
 *
 * @author Fred Simon
 */
public abstract class LockHolderDelegator implements InternalLockHolder {
    protected final InternalLockHolder delegate;

    public LockHolderDelegator(InternalLockHolder delegate) {
        this.delegate = delegate;
    }

    @Override
    public File getLockFile() {
        return delegate.getLockFile();
    }

    @Override
    public File getProtectedFile() {
        return delegate.getProtectedFile();
    }

    @Override
    public String stateMessage() {
        return delegate.stateMessage();
    }

    @Override
    public LockHolderFactory getFactory() {
        return delegate.getFactory();
    }

    @Override
    public LockLogger getLogger() {
        return delegate.getLogger();
    }

    @Override
    public void setLastMessage(String lastMessage) {
        delegate.setLastMessage(lastMessage);
    }

    @Override
    public void appendLastMessage(String lastMessage) {
        delegate.appendLastMessage(lastMessage);
    }
}
