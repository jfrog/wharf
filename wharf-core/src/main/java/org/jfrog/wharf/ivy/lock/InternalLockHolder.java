package org.jfrog.wharf.ivy.lock;

import java.io.Closeable;

/**
 * Date: 9/15/11
 * Time: 12:26 PM
 *
 * @author Fred Simon
 */
public interface InternalLockHolder extends LockHolder, Closeable {
    WharfLockFactory getFactory();

    LockLogger getLogger();

    void setLastMessage(String lastMessage);

    void appendLastMessage(String lastMessage);
}
