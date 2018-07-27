package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
public class MakeUrl extends Task {
    private String property;
    private File file;
    private String separator = " ";
    private List filesets = new LinkedList();
    private List paths = new LinkedList();
    private boolean validate = true;
    public static final String ERROR_MISSING_FILE = "A source file is missing :";
    public static final String ERROR_NO_PROPERTY = "No property defined";
    public static final String ERROR_NO_FILES = "No files defined";
    public void setProperty(String property) {
        this.property = property;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }
    public void setSeparator(String separator) {
        this.separator = separator;
    }
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    public void addPath(Path path) {
        paths.add(path);
    }
    private String filesetsToURL() {
        if (filesets.isEmpty()) {
            return "";
        }
        int count = 0;
        StringBuffer urls = new StringBuffer();
        ListIterator list = filesets.listIterator();
        while (list.hasNext()) {
            FileSet set = (FileSet) list.next();
            DirectoryScanner scanner = set.getDirectoryScanner(getProject());
            String[] files = scanner.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                File f = new File(scanner.getBasedir(), files[i]);
                validateFile(f);
                String asUrl = toURL(f);
                urls.append(asUrl);
                log(asUrl, Project.MSG_DEBUG);
                urls.append(separator);
                count++;
            }
        }
        return stripTrailingSeparator(urls, count);
    }
    private String stripTrailingSeparator(StringBuffer urls,
                                          int count) {
        if (count > 0) {
            urls.delete(urls.length() - separator.length(), urls.length());
            return new String(urls);
        } else {
            return "";
        }
    }
    private String pathsToURL() {
        if (paths.isEmpty()) {
            return "";
        }
        int count = 0;
        StringBuffer urls = new StringBuffer();
        ListIterator list = paths.listIterator();
        while (list.hasNext()) {
            Path path = (Path) list.next();
            String[] elements = path.list();
            for (int i = 0; i < elements.length; i++) {
                File f = new File(elements[i]);
                validateFile(f);
                String asUrl = toURL(f);
                urls.append(asUrl);
                log(asUrl, Project.MSG_DEBUG);
                urls.append(separator);
                count++;
            }
        }
        return stripTrailingSeparator(urls, count);
    }
    private void validateFile(File fileToCheck) {
        if (validate && !fileToCheck.exists()) {
            throw new BuildException(ERROR_MISSING_FILE + fileToCheck.toString());
        }
    }
    public void execute() throws BuildException {
        validate();
        if (getProject().getProperty(property) != null) {
            return;
        }
        String url;
        String filesetURL = filesetsToURL();
        if (file != null) {
            validateFile(file);
            url = toURL(file);
            if (filesetURL.length() > 0) {
                url = url + separator + filesetURL;
            }
        } else {
            url = filesetURL;
        }
        String pathURL = pathsToURL();
        if (pathURL.length() > 0) {
            if (url.length() > 0) {
                url = url + separator + pathURL;
            } else {
                url = pathURL;
            }
        }
        log("Setting " + property + " to URL " + url, Project.MSG_VERBOSE);
        getProject().setNewProperty(property, url);
    }
    private void validate() {
        if (property == null) {
            throw new BuildException(ERROR_NO_PROPERTY);
        }
        if (file == null && filesets.isEmpty() && paths.isEmpty()) {
            throw new BuildException(ERROR_NO_FILES);
        }
    }
    private String toURL(File fileToConvert) {
        String url;
        url = FileUtils.getFileUtils().toURI(fileToConvert.getAbsolutePath());
        return url;
    }
}
