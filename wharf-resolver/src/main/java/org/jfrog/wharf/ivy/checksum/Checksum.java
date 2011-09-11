package org.jfrog.wharf.ivy.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Fred Simnon
 * Date: 4/13/11
 * Time: 1:51 PM
 */
public class Checksum {

    private final ChecksumType type;
    private final MessageDigest digest;
    private String checksum;

    /**
     * @param type The checksum type
     */
    public Checksum(ChecksumType type) {
        this.type = type;
        String algorithm = type.alg();
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(
                    "Cannot create a digest for algorithm: " + algorithm);
        }
    }

    public ChecksumType getType() {
        return type;
    }

    public String getChecksum() {
        if (checksum == null) {
            throw new IllegalStateException("Checksum not calculated yet.");
        }
        return checksum;
    }

    void update(byte[] bytes, int off, int length) {
        digest.update(bytes, off, length);
    }

    void calc() {
        if (checksum != null) {
            throw new IllegalStateException("Checksum already calculated.");
        }
        //Encodes a 128 bit or 160-bit byte array into a String
        byte[] bytes = digest.digest();
        if (bytes.length != 16 && bytes.length != 20) {
            int bitLength = bytes.length * 8;
            throw new IllegalArgumentException("Unrecognised length for binary data: " + bitLength + " bits");
        }
        StringBuilder sb = new StringBuilder();
        for (byte aBinaryData : bytes) {
            String t = Integer.toHexString(aBinaryData & 0xff);
            if (t.length() == 1) {
                sb.append("0");
            }
            sb.append(t);
        }
        checksum = sb.toString().trim();
    }
}
