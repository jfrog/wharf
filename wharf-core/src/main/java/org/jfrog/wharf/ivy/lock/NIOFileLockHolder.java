package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.util.Message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static org.jfrog.wharf.ivy.util.WharfUtils.closeQuietly;

/**
 * Locks a file using the {@link java.nio.channels.FileLock} mechanism.
 */
public class NIOFileLockHolder extends BaseFileLockHolder {

    private FileLock lock;
    private RandomAccessFile raf;
    private FileChannel channel;

    public NIOFileLockHolder(WharfLockFactory factory, File protectedFile) {
        super(factory, protectedFile);
    }

    private void initChannel() throws IOException {
        if (channel != null && channel.isOpen()) {
            return;
        }
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
    }

    @Override
    public boolean acquireLock() {
        try {
            initChannel();
            lock = channel.tryLock();
            if (lock != null) {
                return true;
            } else {
                setLastMessage("Failed to acquire NIO file lock on " + lockFile.getAbsolutePath());
                if (getLogger().isDebugEnabled()) {
                    getLogger().log(stateMessage());
                }
            }
        } catch (IOException e) {
            // ignored
            setLastMessage("File lock failed due to an exception: " + e.getMessage());
            Message.verbose(stateMessage());
            close();
        }
        return false;
    }

    @Override
    public void releaseLock() {
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
