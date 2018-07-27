package org.apache.tools.ant.taskdefs.optional.jlink;
import org.apache.tools.ant.util.FileUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
public class jlink {
    private static final int BUFFER_SIZE = 8192;
    private static final int VECTOR_INIT_SIZE = 10;
    private String outfile = null;
    private Vector mergefiles = new Vector(VECTOR_INIT_SIZE);
    private Vector addfiles = new Vector(VECTOR_INIT_SIZE);
    private boolean compression = false;
    byte[] buffer = new byte[BUFFER_SIZE];
    public void setOutfile(String outfile) {
        if (outfile == null) {
            return;
        }
        this.outfile = outfile;
    }
    public void addMergeFile(String fileToMerge) {
        if (fileToMerge == null) {
            return;
        }
        mergefiles.addElement(fileToMerge);
    }
    public void addAddFile(String fileToAdd) {
        if (fileToAdd == null) {
            return;
        }
        addfiles.addElement(fileToAdd);
    }
    public void addMergeFiles(String[] filesToMerge) {
        if (filesToMerge == null) {
            return;
        }
        for (int i = 0; i < filesToMerge.length; i++) {
            addMergeFile(filesToMerge[i]);
        }
    }
    public void addAddFiles(String[] filesToAdd) {
        if (filesToAdd == null) {
            return;
        }
        for (int i = 0; i < filesToAdd.length; i++) {
            addAddFile(filesToAdd[i]);
        }
    }
    public void setCompression(boolean compress) {
        this.compression = compress;
    }
    public void link() throws Exception {
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outfile));
        if (compression) {
            output.setMethod(ZipOutputStream.DEFLATED);
            output.setLevel(Deflater.DEFAULT_COMPRESSION);
        } else {
            output.setMethod(ZipOutputStream.STORED);
        }
        Enumeration merges = mergefiles.elements();
        while (merges.hasMoreElements()) {
            String path = (String) merges.nextElement();
            File f = new File(path);
            if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
                mergeZipJarContents(output, f);
            } else {
                addAddFile(path);
            }
        }
        Enumeration adds = addfiles.elements();
        while (adds.hasMoreElements()) {
            String name = (String) adds.nextElement();
            File f = new File(name);
            if (f.isDirectory()) {
                addDirContents(output, f, f.getName() + '/', compression);
            } else {
                addFile(output, f, "", compression);
            }
        }
        FileUtils.close(output);
    }
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: jlink output input1 ... inputN");
            System.exit(1);
        }
        jlink linker = new jlink();
        linker.setOutfile(args[0]);
        for (int i = 1; i < args.length; i++) {
            linker.addMergeFile(args[i]);
        }
        try {
            linker.link();
        } catch (Exception ex) {
            System.err.print(ex.getMessage());
        }
    }
    private void mergeZipJarContents(ZipOutputStream output, File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        ZipFile zipf = new ZipFile(f);
        Enumeration entries = zipf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry inputEntry = (ZipEntry) entries.nextElement();
            String inputEntryName = inputEntry.getName();
            int index = inputEntryName.indexOf("META-INF");
            if (index < 0) {
                try {
                    output.putNextEntry(processEntry(zipf, inputEntry));
                } catch (ZipException ex) {
                    String mess = ex.getMessage();
                    if (mess.indexOf("duplicate") >= 0) {
                        continue;
                    } else {
                        throw ex;
                    }
                }
                InputStream in = zipf.getInputStream(inputEntry);
                int len = buffer.length;
                int count = -1;
                while ((count = in.read(buffer, 0, len)) > 0) {
                    output.write(buffer, 0, count);
                }
                in.close();
                output.closeEntry();
            }
        }
        zipf.close();
    }
    private void addDirContents(ZipOutputStream output, File dir, String prefix,
                                boolean compress) throws IOException {
        String[] contents = dir.list();
        for (int i = 0; i < contents.length; ++i) {
            String name = contents[i];
            File file = new File(dir, name);
            if (file.isDirectory()) {
                addDirContents(output, file, prefix + name + '/', compress);
            } else {
                addFile(output, file, prefix, compress);
            }
        }
    }
    private String getEntryName(File file, String prefix) {
        String name = file.getName();
        if (!name.endsWith(".class")) {
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                String className = ClassNameReader.getClassName(input);
                if (className != null) {
                    return className.replace('.', '/') + ".class";
                }
            } catch (IOException ioe) {
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        System.out.println("From " + file.getPath() + " and prefix " + prefix
                           + ", creating entry " + prefix + name);
        return (prefix + name);
    }
    private void addFile(ZipOutputStream output, File file, String prefix,
                         boolean compress) throws IOException {
        if (!file.exists()) {
            return;
        }
        ZipEntry entry = new ZipEntry(getEntryName(file, prefix));
        entry.setTime(file.lastModified());
        entry.setSize(file.length());
        if (!compress) {
            entry.setCrc(calcChecksum(file));
        }
        FileInputStream input = new FileInputStream(file);
        addToOutputStream(output, input, entry);
    }
    private void addToOutputStream(ZipOutputStream output, InputStream input,
                                   ZipEntry ze) throws IOException {
        try {
            output.putNextEntry(ze);
        } catch (ZipException zipEx) {
            input.close();
            return;
        }
        int numBytes = -1;
        while ((numBytes = input.read(buffer)) > 0) {
            output.write(buffer, 0, numBytes);
        }
        output.closeEntry();
        input.close();
    }
    private ZipEntry processEntry(ZipFile zip, ZipEntry inputEntry) {
        String name = inputEntry.getName();
        if (!(inputEntry.isDirectory() || name.endsWith(".class"))) {
            try {
                InputStream input = zip.getInputStream(zip.getEntry(name));
                String className = ClassNameReader.getClassName(input);
                input.close();
                if (className != null) {
                    name = className.replace('.', '/') + ".class";
                }
            } catch (IOException ioe) {
            }
        }
        ZipEntry outputEntry = new ZipEntry(name);
        outputEntry.setTime(inputEntry.getTime());
        outputEntry.setExtra(inputEntry.getExtra());
        outputEntry.setComment(inputEntry.getComment());
        outputEntry.setTime(inputEntry.getTime());
        if (compression) {
            outputEntry.setMethod(ZipEntry.DEFLATED);
        } else {
            outputEntry.setMethod(ZipEntry.STORED);
            outputEntry.setCrc(inputEntry.getCrc());
            outputEntry.setSize(inputEntry.getSize());
        }
        return outputEntry;
    }
    private long calcChecksum(File f) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        return calcChecksum(in);
    }
    private long calcChecksum(InputStream in) throws IOException {
        CRC32 crc = new CRC32();
        int len = buffer.length;
        int count = -1;
        int haveRead = 0;
        while ((count = in.read(buffer, 0, len)) > 0) {
            haveRead += count;
            crc.update(buffer, 0, count);
        }
        in.close();
        return crc.getValue();
    }
}
