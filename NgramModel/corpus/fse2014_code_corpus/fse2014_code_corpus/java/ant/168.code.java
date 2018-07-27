package org.apache.tools.ant.taskdefs;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
public class BUnzip2 extends Unpack {
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final String DEFAULT_EXTENSION = ".bz2";
    protected String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }
    protected void extract() {
        if (source.lastModified() > dest.lastModified()) {
            log("Expanding " + source.getAbsolutePath() + " to "
                + dest.getAbsolutePath());
            FileOutputStream out = null;
            CBZip2InputStream zIn = null;
            InputStream fis = null;
            BufferedInputStream bis = null;
            try {
                out = new FileOutputStream(dest);
                fis = srcResource.getInputStream();
                bis = new BufferedInputStream(fis);
                int b = bis.read();
                if (b != 'B') {
                    throw new BuildException("Invalid bz2 file.", getLocation());
                }
                b = bis.read();
                if (b != 'Z') {
                    throw new BuildException("Invalid bz2 file.", getLocation());
                }
                zIn = new CBZip2InputStream(bis);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                do {
                    out.write(buffer, 0, count);
                    count = zIn.read(buffer, 0, buffer.length);
                } while (count != -1);
            } catch (IOException ioe) {
                String msg = "Problem expanding bzip2 " + ioe.getMessage();
                throw new BuildException(msg, ioe, getLocation());
            } finally {
                FileUtils.close(bis);
                FileUtils.close(fis);
                FileUtils.close(out);
                FileUtils.close(zIn);
            }
        }
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(BUnzip2.class);
    }
}
