package org.jfrog.wharf.ivy.checksum;

import org.apache.ivy.util.Message;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Fred Simon
 * Date: 4/13/11
 * Time: 1:54 PM
 */
public class ChecksumInputStream extends BufferedInputStream {
    private final Checksum[] checksums;
    private boolean closed;
    private long totalBytesRead;

    public ChecksumInputStream(InputStream is, Checksum... checksums) {
        super(is);
        this.checksums = checksums;
    }

    public Checksum[] getChecksums() {
        return checksums;
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        byte b[] = new byte[1];
        return read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        if (bytesRead != -1) {
            totalBytesRead += bytesRead;
            for (Checksum checksum : checksums) {
                checksum.update(b, off, bytesRead);
            }
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!closed) {
            Message.debug("ChecksumInputStream total bytes read: " + totalBytesRead);
            for (Checksum checksum : checksums) {
                checksum.calc();
                Message.debug("Calculated checksum: '"+checksum.getType()+":"+checksum.getChecksum()+"'");
            }
            closed = true;
        }
    }
}
