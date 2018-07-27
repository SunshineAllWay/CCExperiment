package org.apache.tools.ant.taskdefs;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
public class Untar extends Expand {
    private UntarCompressionMethod compression = new UntarCompressionMethod();
    public void setCompression(UntarCompressionMethod method) {
        compression = method;
    }
    public void setEncoding(String encoding) {
        throw new BuildException("The " + getTaskName()
                                 + " task doesn't support the encoding"
                                 + " attribute", getLocation());
    }
    public void setScanForUnicodeExtraFields(boolean b) {
        throw new BuildException("The " + getTaskName()
                                 + " task doesn't support the encoding"
                                 + " attribute", getLocation());
    }
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        FileInputStream fis = null;
        if (!srcF.exists()) {
            throw new BuildException("Unable to untar "
                    + srcF
                    + " as the file does not exist",
                    getLocation());
        }
        try {
            fis = new FileInputStream(srcF);
            expandStream(srcF.getPath(), fis, dir);
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcF.getPath()
                                     + "\n" + ioe.toString(),
                                     ioe, getLocation());
        } finally {
            FileUtils.close(fis);
        }
    }
    protected void expandResource(Resource srcR, File dir) {
        if (!srcR.isExists()) {
            throw new BuildException("Unable to untar "
                                     + srcR.getName()
                                     + " as the it does not exist",
                                     getLocation());
        }
        InputStream i = null;
        try {
            i = srcR.getInputStream();
            expandStream(srcR.getName(), i, dir);
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcR.getName(),
                                     ioe, getLocation());
        } finally {
            FileUtils.close(i);
        }
    }
    private void expandStream(String name, InputStream stream, File dir)
        throws IOException {
        TarInputStream tis = null;
        try {
            tis =
                new TarInputStream(compression.decompress(name,
                                                          new BufferedInputStream(stream)));
            log("Expanding: " + name + " into " + dir, Project.MSG_INFO);
            TarEntry te = null;
            boolean empty = true;
            FileNameMapper mapper = getMapper();
            while ((te = tis.getNextEntry()) != null) {
                empty = false;
                extractFile(FileUtils.getFileUtils(), null, dir, tis,
                            te.getName(), te.getModTime(),
                            te.isDirectory(), mapper);
            }
            if (empty && getFailOnEmptyArchive()) {
                throw new BuildException("archive '" + name + "' is empty");
            }
            log("expand complete", Project.MSG_VERBOSE);
        } finally {
            FileUtils.close(tis);
        }
    }
    public static final class UntarCompressionMethod
        extends EnumeratedAttribute {
        private static final String NONE = "none";
        private static final String GZIP = "gzip";
        private static final String BZIP2 = "bzip2";
        public UntarCompressionMethod() {
            super();
            setValue(NONE);
        }
        public String[] getValues() {
            return new String[] {NONE, GZIP, BZIP2};
        }
        public InputStream decompress(final String name,
                                       final InputStream istream)
            throws IOException, BuildException {
            final String v = getValue();
            if (GZIP.equals(v)) {
                return new GZIPInputStream(istream);
            } else {
                if (BZIP2.equals(v)) {
                    final char[] magic = new char[] {'B', 'Z'};
                    for (int i = 0; i < magic.length; i++) {
                        if (istream.read() != magic[i]) {
                            throw new BuildException(
                                                     "Invalid bz2 file." + name);
                        }
                    }
                    return new CBZip2InputStream(istream);
                }
            }
            return istream;
        }
    }
}
