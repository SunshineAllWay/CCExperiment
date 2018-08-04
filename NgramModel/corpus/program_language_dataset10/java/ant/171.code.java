package org.apache.tools.ant.taskdefs;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
import java.util.Arrays;
import java.text.MessageFormat;
import java.text.ParseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.resources.Restrict;
import org.apache.tools.ant.types.resources.selectors.Type;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
public class Checksum extends MatchingTask implements Condition {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final int NIBBLE = 4;
    private static final int WORD = 16;
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final int BYTE_MASK = 0xFF;
    private static class FileUnion extends Restrict {
        private Union u;
        FileUnion() {
            u = new Union();
            super.add(u);
            super.add(Type.FILE);
        }
        public void add(ResourceCollection rc) {
            u.add(rc);
        }
    }
    private File file = null;
    private File todir;
    private String algorithm = "MD5";
    private String provider = null;
    private String fileext;
    private String property;
    private Map allDigests = new HashMap();
    private Map relativeFilePaths = new HashMap();
    private String totalproperty;
    private boolean forceOverwrite;
    private String verifyProperty;
    private FileUnion resources = null;
    private Hashtable includeFileMap = new Hashtable();
    private MessageDigest messageDigest;
    private boolean isCondition;
    private int readBufferSize = BUFFER_SIZE;
    private MessageFormat format = FormatElement.getDefault().getFormat();
    public void setFile(File file) {
        this.file = file;
    }
    public void setTodir(File todir) {
        this.todir = todir;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public void setFileext(String fileext) {
        this.fileext = fileext;
    }
    public void setProperty(String property) {
        this.property = property;
    }
    public void setTotalproperty(String totalproperty) {
        this.totalproperty = totalproperty;
    }
    public void setVerifyproperty(String verifyProperty) {
        this.verifyProperty = verifyProperty;
    }
    public void setForceOverwrite(boolean forceOverwrite) {
        this.forceOverwrite = forceOverwrite;
    }
    public void setReadBufferSize(int size) {
        this.readBufferSize = size;
    }
    public void setFormat(FormatElement e) {
        format = e.getFormat();
    }
    public void setPattern(String p) {
        format = new MessageFormat(p);
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void add(ResourceCollection rc) {
        if (rc == null) {
            return;
        }
        resources = (resources == null) ? new FileUnion() : resources;
        resources.add(rc);
    }
    public void execute() throws BuildException {
        isCondition = false;
        boolean value = validateAndExecute();
        if (verifyProperty != null) {
            getProject().setNewProperty(
                verifyProperty,
                (value ? Boolean.TRUE.toString() : Boolean.FALSE.toString()));
        }
    }
    public boolean eval() throws BuildException {
        isCondition = true;
        return validateAndExecute();
    }
    private boolean validateAndExecute() throws BuildException {
        String savedFileExt = fileext;
        if (file == null && (resources == null || resources.size() == 0)) {
            throw new BuildException(
                "Specify at least one source - a file or a resource collection.");
        }
        if (!(resources == null || resources.isFilesystemOnly())) {
            throw new BuildException("Can only calculate checksums for file-based resources.");
        }
        if (file != null && file.exists() && file.isDirectory()) {
            throw new BuildException("Checksum cannot be generated for directories");
        }
        if (file != null && totalproperty != null) {
            throw new BuildException("File and Totalproperty cannot co-exist.");
        }
        if (property != null && fileext != null) {
            throw new BuildException("Property and FileExt cannot co-exist.");
        }
        if (property != null) {
            if (forceOverwrite) {
                throw new BuildException(
                    "ForceOverwrite cannot be used when Property is specified");
            }
            int ct = 0;
            if (resources != null) {
                ct += resources.size();
            }
            if (file != null) {
                ct++;
            }
            if (ct > 1) {
                throw new BuildException(
                    "Multiple files cannot be used when Property is specified");
            }
        }
        if (verifyProperty != null) {
            isCondition = true;
        }
        if (verifyProperty != null && forceOverwrite) {
            throw new BuildException("VerifyProperty and ForceOverwrite cannot co-exist.");
        }
        if (isCondition && forceOverwrite) {
            throw new BuildException(
                "ForceOverwrite cannot be used when conditions are being used.");
        }
        messageDigest = null;
        if (provider != null) {
            try {
                messageDigest = MessageDigest.getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException noalgo) {
                throw new BuildException(noalgo, getLocation());
            } catch (NoSuchProviderException noprovider) {
                throw new BuildException(noprovider, getLocation());
            }
        } else {
            try {
                messageDigest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException noalgo) {
                throw new BuildException(noalgo, getLocation());
            }
        }
        if (messageDigest == null) {
            throw new BuildException("Unable to create Message Digest", getLocation());
        }
        if (fileext == null) {
            fileext = "." + algorithm;
        } else if (fileext.trim().length() == 0) {
            throw new BuildException("File extension when specified must not be an empty string");
        }
        try {
            if (resources != null) {
                for (Iterator i = resources.iterator(); i.hasNext();) {
                    Resource r = (Resource) i.next();
                    File src = ((FileProvider) r.as(FileProvider.class))
                        .getFile();
                    if (totalproperty != null || todir != null) {
                        relativeFilePaths.put(src, r.getName().replace(File.separatorChar, '/'));
                    }
                    addToIncludeFileMap(src);
                }
            }
            if (file != null) {
                if (totalproperty != null || todir != null) {
                    relativeFilePaths.put(
                        file, file.getName().replace(File.separatorChar, '/'));
                }
                addToIncludeFileMap(file);
            }
            return generateChecksums();
        } finally {
            fileext = savedFileExt;
            includeFileMap.clear();
        }
    }
    private void addToIncludeFileMap(File file) throws BuildException {
        if (file.exists()) {
            if (property == null) {
                File checksumFile = getChecksumFile(file);
                if (forceOverwrite || isCondition
                    || (file.lastModified() > checksumFile.lastModified())) {
                    includeFileMap.put(file, checksumFile);
                } else {
                    log(file + " omitted as " + checksumFile + " is up to date.",
                        Project.MSG_VERBOSE);
                    if (totalproperty != null) {
                        String checksum = readChecksum(checksumFile);
                        byte[] digest = decodeHex(checksum.toCharArray());
                        allDigests.put(file, digest);
                    }
                }
            } else {
                includeFileMap.put(file, property);
            }
        } else {
            String message = "Could not find file "
                + file.getAbsolutePath()
                + " to generate checksum for.";
            log(message);
            throw new BuildException(message, getLocation());
        }
    }
    private File getChecksumFile(File file) {
        File directory;
        if (todir != null) {
            String path = getRelativeFilePath(file);
            directory = new File(todir, path).getParentFile();
            directory.mkdirs();
        } else {
            directory = file.getParentFile();
        }
        File checksumFile = new File(directory, file.getName() + fileext);
        return checksumFile;
    }
    private boolean generateChecksums() throws BuildException {
        boolean checksumMatches = true;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] buf = new byte[readBufferSize];
        try {
            for (Enumeration e = includeFileMap.keys(); e.hasMoreElements();) {
                messageDigest.reset();
                File src = (File) e.nextElement();
                if (!isCondition) {
                    log("Calculating " + algorithm + " checksum for " + src, Project.MSG_VERBOSE);
                }
                fis = new FileInputStream(src);
                DigestInputStream dis = new DigestInputStream(fis,
                                                              messageDigest);
                while (dis.read(buf, 0, readBufferSize) != -1) {
                }
                dis.close();
                fis.close();
                fis = null;
                byte[] fileDigest = messageDigest.digest ();
                if (totalproperty != null) {
                    allDigests.put(src, fileDigest);
                }
                String checksum = createDigestString(fileDigest);
                Object destination = includeFileMap.get(src);
                if (destination instanceof java.lang.String) {
                    String prop = (String) destination;
                    if (isCondition) {
                        checksumMatches
                            = checksumMatches && checksum.equals(property);
                    } else {
                        getProject().setNewProperty(prop, checksum);
                    }
                } else if (destination instanceof java.io.File) {
                    if (isCondition) {
                        File existingFile = (File) destination;
                        if (existingFile.exists()) {
                            try {
                                String suppliedChecksum =
                                    readChecksum(existingFile);
                                checksumMatches = checksumMatches
                                    && checksum.equals(suppliedChecksum);
                            } catch (BuildException be) {
                                checksumMatches = false;
                            }
                        } else {
                            checksumMatches = false;
                        }
                    } else {
                        File dest = (File) destination;
                        fos = new FileOutputStream(dest);
                        fos.write(format.format(new Object[] {
                                                    checksum,
                                                    src.getName(),
                                                    FileUtils
                                                    .getRelativePath(dest
                                                                     .getParentFile(),
                                                                     src),
                                                    FileUtils
                                                    .getRelativePath(getProject()
                                                                     .getBaseDir(),
                                                                     src),
                                                    src.getAbsolutePath()
                                                }).getBytes());
                        fos.write(StringUtils.LINE_SEP.getBytes());
                        fos.close();
                        fos = null;
                    }
                }
            }
            if (totalproperty != null) {
                Set keys = allDigests.keySet();
                Object[] keyArray = keys.toArray();
                Arrays.sort(keyArray, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            File f1 = (File) o1;
                            File f2 = (File) o2;
                            return f1 == null ? (f2 == null ? 0 : -1)
                                : (f2 == null ? 1
                                   : getRelativeFilePath(f1)
                                   .compareTo(getRelativeFilePath(f2)));
                        }
                    });
                messageDigest.reset();
                for (int i = 0; i < keyArray.length; i++) {
                    File src = (File) keyArray[i];
                    byte[] digest = (byte[]) allDigests.get(src);
                    messageDigest.update(digest);
                    String fileName = getRelativeFilePath(src);
                    messageDigest.update(fileName.getBytes());
                }
                String totalChecksum = createDigestString(messageDigest.digest());
                getProject().setNewProperty(totalproperty, totalChecksum);
            }
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        } finally {
            FileUtils.close(fis);
            FileUtils.close(fos);
        }
        return checksumMatches;
    }
    private String createDigestString(byte[] fileDigest) {
        StringBuffer checksumSb = new StringBuffer();
        for (int i = 0; i < fileDigest.length; i++) {
            String hexStr = Integer.toHexString(BYTE_MASK & fileDigest[i]);
            if (hexStr.length() < 2) {
                checksumSb.append("0");
            }
            checksumSb.append(hexStr);
        }
        return checksumSb.toString();
    }
    public static byte[] decodeHex(char[] data) throws BuildException {
        int l = data.length;
        if ((l & 0x01) != 0) {
            throw new BuildException("odd number of characters.");
        }
        byte[] out = new byte[l >> 1];
        for (int i = 0, j = 0; j < l; i++) {
            int f = Character.digit(data[j++], WORD) << NIBBLE;
            f = f | Character.digit(data[j++], WORD);
            out[i] = (byte) (f & BYTE_MASK);
        }
        return out;
    }
    private String readChecksum(File f) {
        BufferedReader diskChecksumReader = null;
        try {
            diskChecksumReader = new BufferedReader(new FileReader(f));
            Object[] result = format.parse(diskChecksumReader.readLine());
            if (result == null || result.length == 0 || result[0] == null) {
                throw new BuildException("failed to find a checksum");
            }
            return (String) result[0];
        } catch (IOException e) {
            throw new BuildException("Couldn't read checksum file " + f, e);
        } catch (ParseException e) {
            throw new BuildException("Couldn't read checksum file " + f, e);
        } finally {
            FileUtils.close(diskChecksumReader);
        }
    }
    private String getRelativeFilePath(File f) {
        String path = (String) relativeFilePaths.get(f);
        if (path == null) {
            throw new BuildException("Internal error: "
                                     + "relativeFilePaths could not match file "
                                     + f + "\n"
                                     + "please file a bug report on this");
        }
        return path;
    }
    public static class FormatElement extends EnumeratedAttribute {
        private static HashMap formatMap = new HashMap();
        private static final String CHECKSUM = "CHECKSUM";
        private static final String MD5SUM = "MD5SUM";
        private static final String SVF = "SVF";
        static {
            formatMap.put(CHECKSUM, new MessageFormat("{0}"));
            formatMap.put(MD5SUM, new MessageFormat("{0} *{1}"));
            formatMap.put(SVF, new MessageFormat("MD5 ({1}) = {0}"));
        }
        public FormatElement() {
            super();
        }
        public static FormatElement getDefault() {
            FormatElement e = new FormatElement();
            e.setValue(CHECKSUM);
            return e;
        }
        public MessageFormat getFormat() {
            return (MessageFormat) formatMap.get(getValue());
        }
        public String[] getValues() {
            return new String[] {CHECKSUM, MD5SUM, SVF};
        }
    }
}