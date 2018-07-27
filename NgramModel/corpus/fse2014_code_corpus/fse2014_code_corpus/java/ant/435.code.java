package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
public class JarLibDisplayTask extends Task {
    private File libraryFile;
    private final Vector libraryFileSets = new Vector();
    public void setFile(final File file) {
        this.libraryFile = file;
    }
    public void addFileset(final FileSet fileSet) {
        libraryFileSets.addElement(fileSet);
    }
    public void execute() throws BuildException {
        validate();
        final LibraryDisplayer displayer = new LibraryDisplayer();
        if (!libraryFileSets.isEmpty()) {
            final Iterator iterator = libraryFileSets.iterator();
            while (iterator.hasNext()) {
                final FileSet fileSet = (FileSet) iterator.next();
                final DirectoryScanner scanner
                    = fileSet.getDirectoryScanner(getProject());
                final File basedir = scanner.getBasedir();
                final String[] files = scanner.getIncludedFiles();
                for (int i = 0; i < files.length; i++) {
                    final File file = new File(basedir, files[ i ]);
                    displayer.displayLibrary(file);
                }
            }
        } else {
            displayer.displayLibrary(libraryFile);
        }
    }
    private void validate() throws BuildException {
        if (null == libraryFile && libraryFileSets.isEmpty()) {
            final String message = "File attribute not specified.";
            throw new BuildException(message);
        }
        if (null != libraryFile && !libraryFile.exists()) {
            final String message = "File '" + libraryFile + "' does not exist.";
            throw new BuildException(message);
        }
        if (null != libraryFile && !libraryFile.isFile()) {
            final String message = "\'" + libraryFile + "\' is not a file.";
            throw new BuildException(message);
        }
    }
}
