package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;
public class ManifestTask extends Task {
    public static final String VALID_ATTRIBUTE_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
    private Manifest nestedManifest = new Manifest();
    private File manifestFile;
    private Mode mode;
    private String encoding;
    private boolean mergeClassPaths = false;
    private boolean flattenClassPaths = false;
    public static class Mode extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"update", "replace"};
        }
    }
    public ManifestTask() {
        mode = new Mode();
        mode.setValue("replace");
    }
    public void addConfiguredSection(Manifest.Section section)
         throws ManifestException {
        Enumeration attributeKeys = section.getAttributeKeys();
        while (attributeKeys.hasMoreElements()) {
            Attribute attribute = section.getAttribute(
                (String) attributeKeys.nextElement());
            checkAttribute(attribute);
        }
        nestedManifest.addConfiguredSection(section);
    }
    public void addConfiguredAttribute(Manifest.Attribute attribute)
         throws ManifestException {
        checkAttribute(attribute);
        nestedManifest.addConfiguredAttribute(attribute);
    }
    private void checkAttribute(Manifest.Attribute attribute) throws BuildException {
        String name = attribute.getName();
        char ch = name.charAt(0);
        if (ch == '-' || ch == '_') {
            throw new BuildException("Manifest attribute names must not start with '" + ch + "'.");
        }
        for (int i = 0; i < name.length(); i++) {
            ch = name.charAt(i);
            if (VALID_ATTRIBUTE_CHARS.indexOf(ch) < 0) {
                throw new BuildException("Manifest attribute names must not contain '" + ch + "'");
            }
        }
    }
    public void setFile(File f) {
        manifestFile = f;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setMode(Mode m) {
        mode = m;
    }
    public void setMergeClassPathAttributes(boolean b) {
        mergeClassPaths = b;
    }
    public void setFlattenAttributes(boolean b) {
        flattenClassPaths = b;
    }
    public void execute() throws BuildException {
        if (manifestFile == null) {
            throw new BuildException("the file attribute is required");
        }
        Manifest toWrite = Manifest.getDefaultManifest();
        Manifest current = null;
        BuildException error = null;
        if (manifestFile.exists()) {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            try {
                fis = new FileInputStream(manifestFile);
                if (encoding == null) {
                    isr = new InputStreamReader(fis, "UTF-8");
                } else {
                    isr = new InputStreamReader(fis, encoding);
                }
                current = new Manifest(isr);
            } catch (ManifestException m) {
                error = new BuildException("Existing manifest " + manifestFile
                                           + " is invalid", m, getLocation());
            } catch (IOException e) {
                error = new BuildException("Failed to read " + manifestFile,
                                           e, getLocation());
            } finally {
                FileUtils.close(isr);
            }
        }
        for (Enumeration e = nestedManifest.getWarnings();
                e.hasMoreElements();) {
            log("Manifest warning: " + (String) e.nextElement(),
                    Project.MSG_WARN);
        }
        try {
            if (mode.getValue().equals("update") && manifestFile.exists()) {
                if (current != null) {
                    toWrite.merge(current, false, mergeClassPaths);
                } else if (error != null) {
                    throw error;
                }
            }
            toWrite.merge(nestedManifest, false, mergeClassPaths);
        } catch (ManifestException m) {
            throw new BuildException("Manifest is invalid", m, getLocation());
        }
        if (toWrite.equals(current)) {
            log("Manifest has not changed, do not recreate",
                Project.MSG_VERBOSE);
            return;
        }
        PrintWriter w = null;
        try {
            FileOutputStream fos = new FileOutputStream(manifestFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, Manifest.JAR_ENCODING);
            w = new PrintWriter(osw);
            toWrite.write(w, flattenClassPaths);
            if (w.checkError()) {
                throw new IOException("Encountered an error writing manifest");
            }
        } catch (IOException e) {
            throw new BuildException("Failed to write " + manifestFile,
                                     e, getLocation());
        } finally {
            FileUtils.close(w);
        }
    }
}
