package org.apache.batik.apps.svgbrowser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.filechooser.FileSystemView;
class WindowsAltFileSystemView extends FileSystemView {
    public static final String EXCEPTION_CONTAINING_DIR_NULL
        = "AltFileSystemView.exception.containing.dir.null";
    public static final String EXCEPTION_DIRECTORY_ALREADY_EXISTS
        = "AltFileSystemView.exception.directory.already.exists";
    public static final String NEW_FOLDER_NAME =
        " AltFileSystemView.new.folder.name";
    public static final String FLOPPY_DRIVE =
        "AltFileSystemView.floppy.drive";
    public boolean isRoot(File f) {
        if(!f.isAbsolute()) {
            return false;
        }
        String parentPath = f.getParent();
        if(parentPath == null) {
            return true;
        } else {
            File parent = new File(parentPath);
            return parent.equals(f);
        }
    }
    public File createNewFolder(File containingDir) throws
        IOException {
        if(containingDir == null) {
            throw new IOException(Resources.getString(EXCEPTION_CONTAINING_DIR_NULL));
        }
        File newFolder = null;
        newFolder = createFileObject(containingDir,
                                     Resources.getString(NEW_FOLDER_NAME));
        int i = 2;
        while (newFolder.exists() && (i < 100)) {
            newFolder = createFileObject
                (containingDir, Resources.getString(NEW_FOLDER_NAME) + " (" + i + ')' );
            i++;
        }
        if(newFolder.exists()) {
            throw new IOException
                (Resources.formatMessage(EXCEPTION_DIRECTORY_ALREADY_EXISTS,
                                         new Object[]{newFolder.getAbsolutePath()}));
        } else {
            newFolder.mkdirs();
        }
        return newFolder;
    }
    public boolean isHiddenFile(File f) {
        return false;
    }
    public File[] getRoots() {
        List rootsVector = new ArrayList();
        FileSystemRoot floppy = new FileSystemRoot(Resources.getString(FLOPPY_DRIVE)
                                                   + "\\");
        rootsVector.add(floppy);
        for (char c = 'C'; c <= 'Z'; c++) {
            char[] device = {c, ':', '\\'};
            String deviceName = new String(device);
            File deviceFile = new FileSystemRoot(deviceName);
            if (deviceFile != null && deviceFile.exists()) {
                rootsVector.add(deviceFile);
            }
        }
        File[] roots = new File[rootsVector.size()];
        rootsVector.toArray(roots);
        return roots;
    }
    class FileSystemRoot extends File {
        public FileSystemRoot(File f) {
            super(f, "");
        }
        public FileSystemRoot(String s) {
            super(s);
        }
        public boolean isDirectory() {
            return true;
        }
    }
}
