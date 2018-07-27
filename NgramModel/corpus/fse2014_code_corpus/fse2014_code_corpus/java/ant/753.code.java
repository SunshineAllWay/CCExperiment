package org.apache.tools.ant.types.selectors.modifiedselector;
import java.util.Locale;
import java.util.zip.Checksum;
import java.util.zip.CRC32;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.security.NoSuchAlgorithmException;
import org.apache.tools.ant.BuildException;
public class ChecksumAlgorithm implements Algorithm {
    private String algorithm = "CRC";
    private Checksum checksum = null;
    public void setAlgorithm(String algorithm) {
        this.algorithm =
            algorithm != null ? algorithm.toUpperCase(Locale.ENGLISH) : null;
    }
    public void initChecksum() {
        if (checksum != null) {
            return;
        }
        if ("CRC".equals(algorithm)) {
            checksum = new CRC32();
        } else if ("ADLER".equals(algorithm)) {
            checksum = new Adler32();
        } else {
            throw new BuildException(new NoSuchAlgorithmException());
        }
    }
    public boolean isValid() {
        return "CRC".equals(algorithm) || "ADLER".equals(algorithm);
    }
    public String getValue(File file) {
        initChecksum();
        String rval = null;
        try {
            if (file.canRead()) {
                 checksum.reset();
                 FileInputStream fis = new FileInputStream(file);
                 CheckedInputStream check = new CheckedInputStream(fis, checksum);
                 BufferedInputStream in = new BufferedInputStream(check);
                 while (in.read() != -1) {
                 }
                 rval = Long.toString(check.getChecksum().getValue());
                 in.close();
            }
        } catch (Exception e) {
            rval = null;
        }
        return rval;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<ChecksumAlgorithm:");
        buf.append("algorithm=").append(algorithm);
        buf.append(">");
        return buf.toString();
    }
}
