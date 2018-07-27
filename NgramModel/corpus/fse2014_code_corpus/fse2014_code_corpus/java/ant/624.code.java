package org.apache.tools.ant.types;
import java.io.IOException;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.TarResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
public class TarScanner extends ArchiveScanner {
    protected void fillMapsFromArchive(Resource src, String encoding,
                                       Map fileEntries, Map matchFileEntries,
                                       Map dirEntries, Map matchDirEntries) {
        TarEntry entry = null;
        TarInputStream ti = null;
        try {
            try {
                ti = new TarInputStream(src.getInputStream());
            } catch (IOException ex) {
                throw new BuildException("problem opening " + srcFile, ex);
            }
            while ((entry = ti.getNextEntry()) != null) {
                Resource r = new TarResource(src, entry);
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
        } catch (IOException ex) {
            throw new BuildException("problem reading " + srcFile, ex);
        } finally {
            FileUtils.close(ti);
        }
    }
}