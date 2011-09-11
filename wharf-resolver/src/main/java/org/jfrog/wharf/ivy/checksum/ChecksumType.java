package org.jfrog.wharf.ivy.checksum;

/**
 * @author Fred Simon
 * Date: 4/13/11
 * Time: 1:47 PM
 */
public enum ChecksumType {
    sha1("SHA-1", ".sha1", 40),
    md5("MD5", ".md5", 32);

    private final String alg;
    private final String ext;
    private final int length;    // length of the hexadecimal string representation of the checksum

    ChecksumType(String alg, String ext, int length) {
        this.alg = alg;
        this.ext = ext;
        this.length = length;
    }

    public String alg() {
        return alg;
    }

    /**
     * @return The filename extension of the checksum, including the dot prefix.
     */
    public String ext() {
        return ext;
    }

    /**
     * @return The length of a valid checksum for this checksum type.
     */
    public int length() {
        return length;
    }

    /**
     * @param candidate Checksum candidate
     * @return True if this string is a checksum value for this type
     */
    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isValid(String candidate) {
        if (candidate == null || candidate.length() != length) {
            return false;
        }
        return candidate.matches("[a-fA-F0-9]{" + length + "}");
    }

    /**
     * @param ext The checksum filename extension assumed to start with '.' for example '.sha1'.
     * @return Checksum type for the given extension. Null if not found.
     */
    public static ChecksumType forExtension(String ext) {
        if (sha1.ext.equals(ext)) {
            return sha1;
        } else if (md5.ext.equals(ext)) {
            return md5;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return alg;
    }
}
