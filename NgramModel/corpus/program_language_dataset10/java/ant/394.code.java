package org.apache.tools.ant.taskdefs.optional.depend;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
public class DirectoryIterator implements ClassFileIterator {
    private Stack enumStack;
    private Enumeration currentEnum;
    public DirectoryIterator(File rootDirectory, boolean changeInto)
         throws IOException {
        super();
        enumStack = new Stack();
        Vector filesInRoot = getDirectoryEntries(rootDirectory);
        currentEnum = filesInRoot.elements();
    }
    private Vector getDirectoryEntries(File directory) {
        Vector files = new Vector();
        String[] filesInDir = directory.list();
        if (filesInDir != null) {
            int length = filesInDir.length;
            for (int i = 0; i < length; ++i) {
                files.addElement(new File(directory, filesInDir[i]));
            }
        }
        return files;
    }
    public ClassFile getNextClassFile() {
        ClassFile nextElement = null;
        try {
            while (nextElement == null) {
                if (currentEnum.hasMoreElements()) {
                    File element = (File) currentEnum.nextElement();
                    if (element.isDirectory()) {
                        enumStack.push(currentEnum);
                        Vector files = getDirectoryEntries(element);
                        currentEnum = files.elements();
                    } else {
                        FileInputStream inFileStream
                            = new FileInputStream(element);
                        if (element.getName().endsWith(".class")) {
                            ClassFile javaClass = new ClassFile();
                            javaClass.read(inFileStream);
                            nextElement = javaClass;
                        }
                    }
                } else {
                    if (enumStack.empty()) {
                        break;
                    } else {
                        currentEnum = (Enumeration) enumStack.pop();
                    }
                }
            }
        } catch (IOException e) {
            nextElement = null;
        }
        return nextElement;
    }
}