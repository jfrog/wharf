package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.util.Message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import static org.jfrog.wharf.ivy.util.WharfUtils.closeQuietly;

/**
 * Locks a file using the {@link java.nio.channels.FileLock} mechanism.
 */
public class NioFileLockHolder extends BaseFileLockHolder {

    private FileLock lock;
    private RandomAccessFile raf;
    private FileChannel channel;

    public NioFileLockHolder(LockHolderFactory factory, File protectedFile) {
        super(factory, protectedFile);
    }

    private void initChannel() throws IOException {
        if (channel != null && channel.isOpen()) {
            return;
        }
        verifyParentDir();
        if (lockFile.createNewFile()) {
            lockFile.deleteOnExit();
        }
        raf = new RandomAccessFile(lockFile, "rw");
        channel = raf.getChannel();
    }

    public void close() {
        closeQuietly(raf);
        raf = null;
        channel = null;
        lock = null;
    }

    @Override
    public synchronized boolean acquireLock() {
        try {
            initChannel();
            if (lock != null) {
                setLastMessage("NIO lock already taken");
                if (getLogger().isDebugEnabled()) {
                    getLogger().log(stateMessage());
                }
                return false;
            }
            lock = channel.tryLock();
            if (lock != null) {
                return true;
            } else {
                setLastMessage("Failed to acquire NIO file lock on " + lockFile.getAbsolutePath());
                if (getLogger().isDebugEnabled()) {
                    getLogger().log(stateMessage());
                }
            }
        } catch (OverlappingFileLockException e) {
            setLastMessage("Trying to acquire a file lock already acquired in the same JVM: " + e.getMessage());
            Message.verbose(stateMessage());
            close();
        } catch (IOException e) {
            setLastMessage("File lock failed due to an exception: " + e.getMessage());
            Message.verbose(stateMessage());
            close();
        }
        return false;
    }

    @Override
    public synchronized void releaseLock() {
        if (lock == null) {
            setLastMessage(" file not previously locked: " + lockFile);
            return;
        }
        try {
            lock.release();
        } catch (IOException e) {
            setLastMessage("File lock release failed due to an exception: " + e.getMessage());
            Message.error(stateMessage());
            close();
        } finally {
            lock = null;
        }
    }

}
