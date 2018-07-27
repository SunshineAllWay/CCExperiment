package org.apache.tools.ant.taskdefs.optional.i18n;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.LineTokenizer;
public class Translate extends MatchingTask {
    private static final int BUNDLE_SPECIFIED_LANGUAGE_COUNTRY_VARIANT = 0;
    private static final int BUNDLE_SPECIFIED_LANGUAGE_COUNTRY = 1;
    private static final int BUNDLE_SPECIFIED_LANGUAGE = 2;
    private static final int BUNDLE_NOMATCH = 3;
    private static final int BUNDLE_DEFAULT_LANGUAGE_COUNTRY_VARIANT = 4;
    private static final int BUNDLE_DEFAULT_LANGUAGE_COUNTRY = 5;
    private static final int BUNDLE_DEFAULT_LANGUAGE = 6;
     private static final int BUNDLE_MAX_ALTERNATIVES = BUNDLE_DEFAULT_LANGUAGE + 1;
    private String bundle;
    private String bundleLanguage;
    private String bundleCountry;
    private String bundleVariant;
    private File toDir;
    private String srcEncoding;
    private String destEncoding;
    private String bundleEncoding;
    private String startToken;
    private String endToken;
    private boolean forceOverwrite;
    private Vector filesets = new Vector();
    private Hashtable resourceMap = new Hashtable();
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private long[] bundleLastModified = new long[BUNDLE_MAX_ALTERNATIVES];
    private long srcLastModified;
    private long destLastModified;
    private boolean loaded = false;
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }
    public void setBundleLanguage(String bundleLanguage) {
        this.bundleLanguage = bundleLanguage;
    }
    public void setBundleCountry(String bundleCountry) {
        this.bundleCountry = bundleCountry;
    }
    public void setBundleVariant(String bundleVariant) {
        this.bundleVariant = bundleVariant;
    }
    public void setToDir(File toDir) {
        this.toDir = toDir;
    }
    public void setStartToken(String startToken) {
        this.startToken = startToken;
    }
    public void setEndToken(String endToken) {
        this.endToken = endToken;
    }
    public void setSrcEncoding(String srcEncoding) {
        this.srcEncoding = srcEncoding;
    }
    public void setDestEncoding(String destEncoding) {
        this.destEncoding = destEncoding;
    }
    public void setBundleEncoding(String bundleEncoding) {
        this.bundleEncoding = bundleEncoding;
    }
    public void setForceOverwrite(boolean forceOverwrite) {
        this.forceOverwrite = forceOverwrite;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void execute() throws BuildException {
        if (bundle == null) {
            throw new BuildException("The bundle attribute must be set.",
                                     getLocation());
        }
        if (startToken == null) {
            throw new BuildException("The starttoken attribute must be set.",
                                     getLocation());
        }
        if (endToken == null) {
            throw new BuildException("The endtoken attribute must be set.",
                                     getLocation());
        }
        if (bundleLanguage == null) {
            Locale l = Locale.getDefault();
            bundleLanguage  = l.getLanguage();
        }
        if (bundleCountry == null) {
            bundleCountry = Locale.getDefault().getCountry();
        }
        if (bundleVariant == null) {
            Locale l = new Locale(bundleLanguage, bundleCountry);
            bundleVariant = l.getVariant();
        }
        if (toDir == null) {
            throw new BuildException("The todir attribute must be set.",
                                     getLocation());
        }
        if (!toDir.exists()) {
            toDir.mkdirs();
        } else if (toDir.isFile()) {
            throw new BuildException(toDir + " is not a directory");
        }
        if (srcEncoding == null) {
            srcEncoding = System.getProperty("file.encoding");
        }
        if (destEncoding == null) {
            destEncoding = srcEncoding;
        }
        if (bundleEncoding == null) {
            bundleEncoding = srcEncoding;
        }
        loadResourceMaps();
        translate();
    }
    private void loadResourceMaps() throws BuildException {
        Locale locale = new Locale(bundleLanguage,
                                   bundleCountry,
                                   bundleVariant);
        String language = locale.getLanguage().length() > 0
            ? "_" + locale.getLanguage() : "";
        String country = locale.getCountry().length() > 0
            ? "_" + locale.getCountry() : "";
        String variant = locale.getVariant().length() > 0
            ? "_" + locale.getVariant() : "";
        String bundleFile = bundle + language + country + variant;
        processBundle(bundleFile, BUNDLE_SPECIFIED_LANGUAGE_COUNTRY_VARIANT, false);
        bundleFile = bundle + language + country;
        processBundle(bundleFile, BUNDLE_SPECIFIED_LANGUAGE_COUNTRY, false);
        bundleFile = bundle + language;
        processBundle(bundleFile, BUNDLE_SPECIFIED_LANGUAGE, false);
        bundleFile = bundle;
        processBundle(bundleFile, BUNDLE_NOMATCH, false);
        locale = Locale.getDefault();
        language = locale.getLanguage().length() > 0
            ? "_" + locale.getLanguage() : "";
        country = locale.getCountry().length() > 0
            ? "_" + locale.getCountry() : "";
        variant = locale.getVariant().length() > 0
            ? "_" + locale.getVariant() : "";
        bundleEncoding = System.getProperty("file.encoding");
        bundleFile = bundle + language + country + variant;
        processBundle(bundleFile, BUNDLE_DEFAULT_LANGUAGE_COUNTRY_VARIANT, false);
        bundleFile = bundle + language + country;
        processBundle(bundleFile, BUNDLE_DEFAULT_LANGUAGE_COUNTRY, false);
        bundleFile = bundle + language;
        processBundle(bundleFile, BUNDLE_DEFAULT_LANGUAGE, true);
    }
    private void processBundle(final String bundleFile, final int i,
                               final boolean checkLoaded) throws BuildException {
        final File propsFile = getProject().resolveFile(bundleFile + ".properties");
        FileInputStream ins = null;
        try {
            ins = new FileInputStream(propsFile);
            loaded = true;
            bundleLastModified[i] = propsFile.lastModified();
            log("Using " + propsFile, Project.MSG_DEBUG);
            loadResourceMap(ins);
        } catch (IOException ioe) {
            log(propsFile + " not found.", Project.MSG_DEBUG);
            if (!loaded && checkLoaded) {
                throw new BuildException(ioe.getMessage(), getLocation());
            }
        }
    }
    private void loadResourceMap(FileInputStream ins) throws BuildException {
        try {
            BufferedReader in = null;
            InputStreamReader isr = new InputStreamReader(ins, bundleEncoding);
            in = new BufferedReader(isr);
            String line = null;
            while ((line = in.readLine()) != null) {
                if (line.trim().length() > 1 && '#' != line.charAt(0) && '!' != line.charAt(0)) {
                    int sepIndex = line.indexOf('=');
                    if (-1 == sepIndex) {
                        sepIndex = line.indexOf(':');
                    }
                    if (-1 == sepIndex) {
                        for (int k = 0; k < line.length(); k++) {
                            if (Character.isSpaceChar(line.charAt(k))) {
                                sepIndex = k;
                                break;
                            }
                        }
                    }
                    if (-1 != sepIndex) {
                        String key = line.substring(0, sepIndex).trim();
                        String value = line.substring(sepIndex + 1).trim();
                        while (value.endsWith("\\")) {
                            value = value.substring(0, value.length() - 1);
                            line = in.readLine();
                            if (line != null) {
                                value = value + line.trim();
                            } else {
                                break;
                            }
                        }
                        if (key.length() > 0) {
                            if (resourceMap.get(key) == null) {
                                resourceMap.put(key, value);
                            }
                        }
                    }
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe.getMessage(), getLocation());
        }
    }
    private void translate() throws BuildException {
        int filesProcessed = 0;
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int j = 0; j < srcFiles.length; j++) {
                try {
                    File dest = FILE_UTILS.resolveFile(toDir, srcFiles[j]);
                    try {
                        File destDir = new File(dest.getParent());
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                        }
                    } catch (Exception e) {
                        log("Exception occurred while trying to check/create "
                            + " parent directory.  " + e.getMessage(),
                            Project.MSG_DEBUG);
                    }
                    destLastModified = dest.lastModified();
                    File src = FILE_UTILS.resolveFile(ds.getBasedir(), srcFiles[j]);
                    srcLastModified = src.lastModified();
                    boolean needsWork = forceOverwrite
                        || destLastModified < srcLastModified;
                    if (!needsWork) {
                        for (int icounter = 0; icounter < BUNDLE_MAX_ALTERNATIVES; icounter++) {
                            needsWork = (destLastModified < bundleLastModified[icounter]);
                            if (needsWork) {
                                break;
                            }
                        }
                    }
                    if (needsWork) {
                        log("Processing " + srcFiles[j],
                            Project.MSG_DEBUG);
                        translateOneFile(src, dest);
                        ++filesProcessed;
                    } else {
                        log("Skipping " + srcFiles[j]
                            + " as destination file is up to date",
                            Project.MSG_VERBOSE);
                    }
                } catch (IOException ioe) {
                    throw new BuildException(ioe.getMessage(), getLocation());
                }
            }
        }
        log("Translation performed on " + filesProcessed + " file(s).", Project.MSG_DEBUG);
    }
    private void translateOneFile(File src, File dest) throws IOException {
        BufferedWriter out = null;
        BufferedReader in = null;
        try {
            FileOutputStream fos = new FileOutputStream(dest);
            out = new BufferedWriter(new OutputStreamWriter(fos, destEncoding));
            FileInputStream fis = new FileInputStream(src);
            in = new BufferedReader(new InputStreamReader(fis, srcEncoding));
            String line;
            LineTokenizer lineTokenizer = new LineTokenizer();
            lineTokenizer.setIncludeDelims(true);
            line = lineTokenizer.getToken(in);
            while ((line) != null) {
                int startIndex = line.indexOf(startToken);
                while (startIndex >= 0
                       && (startIndex + startToken.length()) <= line.length()) {
                    String replace = null;
                    int endIndex = line.indexOf(endToken, startIndex
                                                + startToken.length());
                    if (endIndex < 0) {
                        startIndex += 1;
                    } else {
                        String token = line.substring(startIndex
                                                      + startToken.length(),
                                                      endIndex);
                        boolean validToken = true;
                        for (int k = 0; k < token.length() && validToken; k++) {
                            char c = token.charAt(k);
                            if (c == ':' || c == '='
                                || Character.isSpaceChar(c)) {
                                validToken = false;
                            }
                        }
                        if (!validToken) {
                            startIndex += 1;
                        } else {
                            if (resourceMap.containsKey(token)) {
                                replace = (String) resourceMap.get(token);
                            } else {
                                log("Replacement string missing for: " + token,
                                    Project.MSG_VERBOSE);
                                replace = startToken + token + endToken;
                            }
                            line = line.substring(0, startIndex) + replace
                                + line.substring(endIndex + endToken.length());
                            startIndex += replace.length();
                        }
                    }
                    startIndex = line.indexOf(startToken, startIndex);
                }
                out.write(line);
                line = lineTokenizer.getToken(in);
            }
        } finally {
            FileUtils.close(in);
            FileUtils.close(out);
        }
    }
}
