package org.apache.tools.ant.taskdefs.optional.depend;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class JarFileIterator implements ClassFileIterator {
    private ZipInputStream jarStream;
    public JarFileIterator(InputStream stream) throws IOException {
        super();
        jarStream = new ZipInputStream(stream);
    }
    public ClassFile getNextClassFile() {
        ZipEntry jarEntry;
        ClassFile nextElement = null;
        try {
            jarEntry = jarStream.getNextEntry();
            while (nextElement == null && jarEntry != null) {
                String entryName = jarEntry.getName();
                if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                    ClassFile javaClass = new ClassFile();
                    javaClass.read(jarStream);
                    nextElement = javaClass;
                } else {
                    jarEntry = jarStream.getNextEntry();
                }
            }
        } catch (IOException e) {
            String message = e.getMessage();
            String text = e.getClass().getName();
            if (message != null) {
                text += ": " + message;
            }
            throw new RuntimeException("Problem reading JAR file: " + text);
        }
        return nextElement;
    }
}
