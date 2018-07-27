package org.apache.tools.ant.types.selectors.modifiedselector;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.apache.tools.ant.BuildException;
public class DigestAlgorithm implements Algorithm {
    private static final int BYTE_MASK = 0xFF;
    private static final int BUFFER_SIZE = 8192;
    private String algorithm = "MD5";
    private String provider = null;
    private MessageDigest messageDigest = null;
    private int readBufferSize = BUFFER_SIZE;
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm != null
            ? algorithm.toUpperCase(Locale.ENGLISH) : null;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public void initMessageDigest() {
        if (messageDigest != null) {
            return;
        }
        if ((provider != null) && !"".equals(provider) && !"null".equals(provider)) {
            try {
                messageDigest = MessageDigest.getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException noalgo) {
                throw new BuildException(noalgo);
            } catch (NoSuchProviderException noprovider) {
                throw new BuildException(noprovider);
            }
        } else {
            try {
                messageDigest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException noalgo) {
                throw new BuildException(noalgo);
            }
        }
    }
    public boolean isValid() {
        return "SHA".equals(algorithm) || "MD5".equals(algorithm);
    }
    public String getValue(File file) {
        initMessageDigest();
        String checksum = null;
        try {
            if (!file.canRead()) {
                return null;
            }
            FileInputStream fis = null;
            byte[] buf = new byte[readBufferSize];
            try {
                messageDigest.reset();
                fis = new FileInputStream(file);
                DigestInputStream dis = new DigestInputStream(fis,
                                                              messageDigest);
                while (dis.read(buf, 0, readBufferSize) != -1) {
                }
                dis.close();
                fis.close();
                fis = null;
                byte[] fileDigest = messageDigest.digest();
                StringBuffer checksumSb = new StringBuffer();
                for (int i = 0; i < fileDigest.length; i++) {
                    String hexStr
                        = Integer.toHexString(BYTE_MASK & fileDigest[i]);
                    if (hexStr.length() < 2) {
                        checksumSb.append("0");
                    }
                    checksumSb.append(hexStr);
                }
                checksum = checksumSb.toString();
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return checksum;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<DigestAlgorithm:");
        buf.append("algorithm=").append(algorithm);
        buf.append(";provider=").append(provider);
        buf.append(">");
        return buf.toString();
    }
}
