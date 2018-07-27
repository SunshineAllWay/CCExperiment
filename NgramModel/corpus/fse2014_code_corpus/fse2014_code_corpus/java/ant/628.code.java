package org.apache.tools.ant.types;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
public class ZipScanner extends ArchiveScanner {
    protected void fillMapsFromArchive(Resource src, String encoding,
                                       Map fileEntries, Map matchFileEntries,
                                       Map dirEntries, Map matchDirEntries) {
        ZipEntry entry = null;
        ZipFile zf = null;
        File srcFile = null;
        FileProvider fp = (FileProvider) src.as(FileProvider.class);
        if (fp != null) {
            srcFile = fp.getFile();
        } else {
            throw new BuildException("Only file provider resources are supported");
        }
        try {
            try {
                zf = new ZipFile(srcFile, encoding);
            } catch (ZipException ex) {
                throw new BuildException("Problem reading " + srcFile, ex);
            } catch (IOException ex) {
                throw new BuildException("Problem opening " + srcFile, ex);
            }
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                Resource r = new ZipResource(srcFile, encoding, entry);
                String name = entry.getName();
                if (entry.isDirectory()) {
                    name = trimSeparator(name);
                    dirEntries.put(name, r);
                    if (match(name)) {
                        matchDirEntries.put(name, r);
                    }
                } else {
                    fileEntries.put(name, r);
                    if (match(name)) {
                        matchFileEntries.put(name, r);
                    }
                }
            }
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }
}