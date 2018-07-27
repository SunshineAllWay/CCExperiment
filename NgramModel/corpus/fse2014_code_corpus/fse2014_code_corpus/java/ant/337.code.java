package org.apache.tools.ant.taskdefs.cvslib;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.AbstractCvsTask;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.DOMUtils;
import org.apache.tools.ant.util.CollectionUtils;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class CvsTagDiff extends AbstractCvsTask {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final DOMElementWriter DOM_WRITER = new DOMElementWriter();
    static final String FILE_STRING = "File ";
    static final int FILE_STRING_LENGTH = FILE_STRING.length();
    static final String TO_STRING = " to ";
    static final String FILE_IS_NEW = " is new;";
    static final String REVISION = "revision ";
    static final String FILE_HAS_CHANGED = " changed from revision ";
    static final String FILE_WAS_REMOVED = " is removed";
    private String mypackage;
    private String mystartTag;
    private String myendTag;
    private String mystartDate;
    private String myendDate;
    private File mydestfile;
    private boolean ignoreRemoved = false;
    private List packageNames = new ArrayList();
    private String[] packageNamePrefixes = null;
    private int[] packageNamePrefixLengths = null;
    public void setPackage(String p) {
        mypackage = p;
    }
    public void setStartTag(String s) {
        mystartTag = s;
    }
    public void setStartDate(String s) {
        mystartDate = s;
    }
    public void setEndTag(String s) {
        myendTag = s;
    }
    public void setEndDate(String s) {
        myendDate = s;
    }
    public void setDestFile(File f) {
        mydestfile = f;
    }
    public void setIgnoreRemoved(boolean b) {
        ignoreRemoved = b;
    }
    public void execute() throws BuildException {
        validate();
        addCommandArgument("rdiff");
        addCommandArgument("-s");
        if (mystartTag != null) {
            addCommandArgument("-r");
            addCommandArgument(mystartTag);
        } else {
            addCommandArgument("-D");
            addCommandArgument(mystartDate);
        }
        if (myendTag != null) {
            addCommandArgument("-r");
            addCommandArgument(myendTag);
        } else {
            addCommandArgument("-D");
            addCommandArgument(myendDate);
        }
        setCommand("");
        File tmpFile = null;
        try {
            handlePackageNames();
            tmpFile = FILE_UTILS.createTempFile("cvstagdiff", ".log", null,
                                                true, true);
            setOutput(tmpFile);
            super.execute();
            CvsTagEntry[] entries = parseRDiff(tmpFile);
            writeTagDiff(entries);
        } finally {
            packageNamePrefixes = null;
            packageNamePrefixLengths = null;
            packageNames.clear();
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
    private CvsTagEntry[] parseRDiff(File tmpFile) throws BuildException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(tmpFile));
            Vector entries = new Vector();
            String line = reader.readLine();
            while (null != line) {
                line = removePackageName(line, packageNamePrefixes,
                                         packageNamePrefixLengths);
                if (line != null) {
                    boolean processed
                        =  doFileIsNew(entries, line)
                        || doFileHasChanged(entries, line)
                        || doFileWasRemoved(entries, line);
                }
                line = reader.readLine();
            }
            CvsTagEntry[] array = new CvsTagEntry[entries.size()];
            entries.copyInto(array);
            return array;
        } catch (IOException e) {
            throw new BuildException("Error in parsing", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log(e.toString(), Project.MSG_ERR);
                }
            }
        }
    }
    private boolean doFileIsNew(Vector entries, String line) {
        int index = line.indexOf(FILE_IS_NEW);
        if (index == -1) {
            return false;
        }
        String filename = line.substring(0, index);
        String rev = null;
        int indexrev = line.indexOf(REVISION, index);
        if (indexrev != -1) {
            rev = line.substring(indexrev + REVISION.length());
        }
        CvsTagEntry entry = new CvsTagEntry(filename, rev);
        entries.addElement(entry);
        log(entry.toString(), Project.MSG_VERBOSE);
        return true;
    }
    private boolean doFileHasChanged(Vector entries, String line) {
        int index = line.indexOf(FILE_HAS_CHANGED);
        if (index == -1) {
            return false;
        }
        String filename = line.substring(0, index);
        int revSeparator = line.indexOf(" to ", index);
        String prevRevision =
            line.substring(index + FILE_HAS_CHANGED.length(),
                           revSeparator);
        String revision = line.substring(revSeparator + TO_STRING.length());
        CvsTagEntry entry = new CvsTagEntry(filename,
                                            revision,
                                            prevRevision);
        entries.addElement(entry);
        log(entry.toString(), Project.MSG_VERBOSE);
        return true;
    }
    private boolean doFileWasRemoved(Vector entries, String line) {
        if (ignoreRemoved) {
            return false;
        }
        int index = line.indexOf(FILE_WAS_REMOVED);
        if (index == -1) {
            return false;
        }
        String filename = line.substring(0, index);
        String rev = null;
        int indexrev = line.indexOf(REVISION, index);
        if (indexrev != -1) {
            rev = line.substring(indexrev + REVISION.length());
        }
        CvsTagEntry entry = new CvsTagEntry(filename, null, rev);
        entries.addElement(entry);
        log(entry.toString(), Project.MSG_VERBOSE);
        return true;
    }
    private void writeTagDiff(CvsTagEntry[] entries) throws BuildException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mydestfile);
            PrintWriter writer = new PrintWriter(
                                     new OutputStreamWriter(output, "UTF-8"));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            Document doc = DOMUtils.newDocument();
            Element root = doc.createElement("tagdiff");
            if (mystartTag != null) {
                root.setAttribute("startTag", mystartTag);
            } else {
                root.setAttribute("startDate", mystartDate);
            }
            if (myendTag != null) {
                root.setAttribute("endTag", myendTag);
            } else {
                root.setAttribute("endDate", myendDate);
            }
            root.setAttribute("cvsroot", getCvsRoot());
            root.setAttribute("package",
                              CollectionUtils.flattenToString(packageNames));
            DOM_WRITER.openElement(root, writer, 0, "\t");
            writer.println();
            for (int i = 0, c = entries.length; i < c; i++) {
                writeTagEntry(doc, writer, entries[i]);
            }
            DOM_WRITER.closeElement(root, writer, 0, "\t", true);
            writer.flush();
            if (writer.checkError()) {
                throw new IOException("Encountered an error writing tagdiff");
            }
            writer.close();
        } catch (UnsupportedEncodingException uee) {
            log(uee.toString(), Project.MSG_ERR);
        } catch (IOException ioe) {
            throw new BuildException(ioe.toString(), ioe);
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException ioe) {
                    log(ioe.toString(), Project.MSG_ERR);
                }
            }
        }
    }
    private void writeTagEntry(Document doc, PrintWriter writer,
                               CvsTagEntry entry)
        throws IOException {
        Element ent = doc.createElement("entry");
        Element f = DOMUtils.createChildElement(ent, "file");
        DOMUtils.appendCDATAElement(f, "name", entry.getFile());
        if (entry.getRevision() != null) {
            DOMUtils.appendTextElement(f, "revision", entry.getRevision());
        }
        if (entry.getPreviousRevision() != null) {
            DOMUtils.appendTextElement(f, "prevrevision",
                                       entry.getPreviousRevision());
        }
        DOM_WRITER.write(ent, writer, 1, "\t");
    }
    private void validate() throws BuildException {
        if (null == mypackage && getModules().size() == 0) {
            throw new BuildException("Package/module must be set.");
        }
        if (null == mydestfile) {
            throw new BuildException("Destfile must be set.");
        }
        if (null == mystartTag && null == mystartDate) {
            throw new BuildException("Start tag or start date must be set.");
        }
        if (null != mystartTag && null != mystartDate) {
            throw new BuildException("Only one of start tag and start date "
                                     + "must be set.");
        }
        if (null == myendTag && null == myendDate) {
            throw new BuildException("End tag or end date must be set.");
        }
        if (null != myendTag && null != myendDate) {
            throw new BuildException("Only one of end tag and end date must "
                                     + "be set.");
        }
    }
    private void handlePackageNames() {
        if (mypackage != null) {
            StringTokenizer myTokenizer = new StringTokenizer(mypackage);
            while (myTokenizer.hasMoreTokens()) {
                String pack = myTokenizer.nextToken();
                packageNames.add(pack);
                addCommandArgument(pack);
            }
        }
        for (Iterator iter = getModules().iterator(); iter.hasNext();) {
            AbstractCvsTask.Module m = (AbstractCvsTask.Module) iter.next();
            packageNames.add(m.getName());
        }
        packageNamePrefixes = new String[packageNames.size()];
        packageNamePrefixLengths = new int[packageNames.size()];
        for (int i = 0; i < packageNamePrefixes.length; i++) {
            packageNamePrefixes[i] = FILE_STRING + packageNames.get(i) + "/";
            packageNamePrefixLengths[i] = packageNamePrefixes[i].length();
        }
    }
    private static String removePackageName(String line,
                                            String[] packagePrefixes,
                                            int[] prefixLengths) {
        if (line.length() < FILE_STRING_LENGTH) {
            return null;
        }
        boolean matched = false;
        for (int i = 0; i < packagePrefixes.length; i++) {
            if (line.startsWith(packagePrefixes[i])) {
                matched = true;
                line = line.substring(prefixLengths[i]);
                break;
            }
        }
        if (!matched) {
            line = line.substring(FILE_STRING_LENGTH);
        }
        return line;
    }
}
