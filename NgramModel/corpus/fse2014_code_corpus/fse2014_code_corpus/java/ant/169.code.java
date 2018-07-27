package org.apache.tools.ant.taskdefs;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2OutputStream;
public class BZip2 extends Pack {
    protected void pack() {
        CBZip2OutputStream zOut = null;
        try {
            BufferedOutputStream bos =
                new BufferedOutputStream(new FileOutputStream(zipFile));
            bos.write('B');
            bos.write('Z');
            zOut = new CBZip2OutputStream(bos);
            zipResource(getSrcResource(), zOut);
        } catch (IOException ioe) {
            String msg = "Problem creating bzip2 " + ioe.getMessage();
            throw new BuildException(msg, ioe, getLocation());
        } finally {
            FileUtils.close(zOut);
        }
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(BZip2.class);
    }
}
