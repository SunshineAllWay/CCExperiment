package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.RedirectorElement;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import java.util.Iterator;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
public class VerifyJar extends AbstractJarSignerTask {
    public static final String ERROR_NO_FILE = "Not found :";
    private static final String VERIFIED_TEXT = "jar verified.";
    private boolean certificates = false;
    private BufferingOutputFilter outputCache = new BufferingOutputFilter();
    public static final String ERROR_NO_VERIFY = "Failed to verify ";
    public void setCertificates(boolean certificates) {
        this.certificates = certificates;
    }
    public void execute() throws BuildException {
        final boolean hasJar = jar != null;
        if (!hasJar && !hasResources()) {
            throw new BuildException(ERROR_NO_SOURCE);
        }
        beginExecution();
        RedirectorElement redirector = getRedirector();
        redirector.setAlwaysLog(true);
        FilterChain outputFilterChain = redirector.createOutputFilterChain();
        outputFilterChain.add(outputCache);
        try {
            Path sources = createUnifiedSourcePath();
            Iterator iter = sources.iterator();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                FileProvider fr = (FileProvider) r.as(FileProvider.class);
                verifyOneJar(fr.getFile());
            }
        } finally {
            endExecution();
        }
    }
    private void verifyOneJar(File jar) {
        if (!jar.exists()) {
            throw new BuildException(ERROR_NO_FILE + jar);
        }
        final ExecTask cmd = createJarSigner();
        setCommonOptions(cmd);
        bindToKeystore(cmd);
        addValue(cmd, "-verify");
        if (certificates) {
            addValue(cmd, "-certs");
        }
        addValue(cmd, jar.getPath());
        log("Verifying JAR: " + jar.getAbsolutePath());
        outputCache.clear();
        BuildException ex = null;
        try {
            cmd.execute();
        } catch (BuildException e) {
            ex = e;
        }
        String results = outputCache.toString();
        if (ex != null) {
            if (results.indexOf("zip file closed") >= 0) {
                log("You are running " + JARSIGNER_COMMAND + " against a JVM with"
                    + " a known bug that manifests as an IllegalStateException.",
                    Project.MSG_WARN);
            } else {
                throw ex;
            }
        }
        if (results.indexOf(VERIFIED_TEXT) < 0) {
            throw new BuildException(ERROR_NO_VERIFY + jar);
        }
    }
    private static class BufferingOutputFilter implements ChainableReader {
        private BufferingOutputFilterReader buffer;
        public Reader chain(Reader rdr) {
            buffer = new BufferingOutputFilterReader(rdr);
            return buffer;
        }
        public String toString() {
            return buffer.toString();
        }
        public void clear() {
            if (buffer != null) {
                buffer.clear();
            }
        }
    }
    private static class BufferingOutputFilterReader extends Reader {
        private Reader next;
        private StringBuffer buffer = new StringBuffer();
        public BufferingOutputFilterReader(Reader next) {
            this.next = next;
        }
        public int read(char[] cbuf, int off, int len) throws IOException {
            int result = next.read(cbuf, off, len);
            buffer.append(cbuf, off, len);
            return result;
        }
        public void close() throws IOException {
            next.close();
        }
        public String toString() {
            return buffer.toString();
        }
        public void clear() {
            buffer = new StringBuffer();
        }
    }
}
