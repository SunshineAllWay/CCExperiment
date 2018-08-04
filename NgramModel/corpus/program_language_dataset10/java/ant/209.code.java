package org.apache.tools.ant.taskdefs;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
public class GZip extends Pack {
    protected void pack() {
        GZIPOutputStream zOut = null;
        try {
            zOut = new GZIPOutputStream(new FileOutputStream(zipFile));
            zipResource(getSrcResource(), zOut);
        } catch (IOException ioe) {
            String msg = "Problem creating gzip " + ioe.getMessage();
            throw new BuildException(msg, ioe, getLocation());
        } finally {
            FileUtils.close(zOut);
        }
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(GZip.class);
    }
}