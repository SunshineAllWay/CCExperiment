package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.URLProvider;
import org.apache.tools.ant.types.resources.URLResource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
public class Get extends Task {
    private static final int NUMBER_RETRIES = 3;
    private static final int DOTS_PER_LINE = 50;
    private static final int BIG_BUFFER_SIZE = 100 * 1024;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final int REDIRECT_LIMIT = 25;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private Resources sources = new Resources();
    private File destination; 
    private boolean verbose = false;
    private boolean useTimestamp = false; 
    private boolean ignoreErrors = false;
    private String uname = null;
    private String pword = null;
    private long maxTime = 0;
    private int numberRetries = NUMBER_RETRIES;
    private boolean skipExisting = false;
    private boolean httpUseCaches = true; 
    private Mapper mapperElement = null;
    public void execute() throws BuildException {
        checkAttributes();
        for (Iterator iter = sources.iterator(); iter.hasNext(); ) {
            Resource r = (Resource) iter.next();
            URLProvider up = (URLProvider) r.as(URLProvider.class);
            URL source = up.getURL();
            File dest = destination;
            if (destination.isDirectory()) {
                if (mapperElement == null) {
                    String path = source.getPath();
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    int slash = path.lastIndexOf("/");
                    if (slash > -1) {
                        path = path.substring(slash + 1);
                    }
                    dest = new File(destination, path);
                } else {
                    FileNameMapper mapper = mapperElement.getImplementation();
                    String[] d = mapper.mapFileName(source.toString());
                    if (d == null) {
                        log("skipping " + r + " - mapper can't handle it",
                            Project.MSG_WARN);
                        continue;
                    } else if (d.length == 0) {
                        log("skipping " + r + " - mapper returns no file name",
                            Project.MSG_WARN);
                        continue;
                    } else if (d.length > 1) {
                        log("skipping " + r + " - mapper returns multiple file"
                            + " names", Project.MSG_WARN);
                        continue;
                    }
                    dest = new File(destination, d[0]);
                }
            }
        int logLevel = Project.MSG_INFO;
        DownloadProgress progress = null;
        if (verbose) {
            progress = new VerboseProgress(System.out);
        }
        try {
            doGet(source, dest, logLevel, progress);
        } catch (IOException ioe) {
            log("Error getting " + source + " to " + dest);
            if (!ignoreErrors) {
                throw new BuildException(ioe, getLocation());
            }
        }
        }
    }
    public boolean doGet(int logLevel, DownloadProgress progress)
            throws IOException {
        checkAttributes();
        for (Iterator iter = sources.iterator(); iter.hasNext(); ) {
            Resource r = (Resource) iter.next();
            URLProvider up = (URLProvider) r.as(URLProvider.class);
            URL source = up.getURL();
            return doGet(source, destination, logLevel, progress);
        }
        return false;
    }
    public boolean doGet(URL source, File dest, int logLevel,
                         DownloadProgress progress)
        throws IOException {
        if (dest.exists() && skipExisting) {
            log("Destination already exists (skipping): "
                + dest.getAbsolutePath(), logLevel);
            return true;
        }
        if (progress == null) {
            progress = new NullProgress();
        }
        log("Getting: " + source, logLevel);
        log("To: " + dest.getAbsolutePath(), logLevel);
        long timestamp = 0;
        boolean hasTimestamp = false;
        if (useTimestamp && dest.exists()) {
            timestamp = dest.lastModified();
            if (verbose) {
                Date t = new Date(timestamp);
                log("local file date : " + t.toString(), logLevel);
            }
            hasTimestamp = true;
        }
        GetThread getThread = new GetThread(source, dest,
                                            hasTimestamp, timestamp, progress,
                                            logLevel);
        getThread.setDaemon(true);
        getProject().registerThreadTask(getThread, this);
        getThread.start();
        try {
            getThread.join(maxTime * 1000);
        } catch (InterruptedException ie) {
            log("interrupted waiting for GET to finish",
                Project.MSG_VERBOSE);
        }
        if (getThread.isAlive()) {
            String msg = "The GET operation took longer than " + maxTime
                + " seconds, stopping it.";
            if (ignoreErrors) {
                log(msg);
            }
            getThread.closeStreams();
            if (!ignoreErrors) {
                throw new BuildException(msg);
            }
            return false;
        }
        return getThread.wasSuccessful();
    }
    private void checkAttributes() {
        if (sources.size() == 0) {
            throw new BuildException("at least one source is required",
                                     getLocation());
        }
        for (Iterator iter = sources.iterator(); iter.hasNext(); ) {
            Object up = ((Resource) iter.next()).as(URLProvider.class);
            if (up == null) {
                throw new BuildException("Only URLProvider resources are"
                                         + " supported", getLocation());
            }
        }
        if (destination == null) {
            throw new BuildException("dest attribute is required", getLocation());
        }
        if (destination.exists() && sources.size() > 1
            && !destination.isDirectory()) {
            throw new BuildException("The specified destination is not a"
                                     + " directory",
                                     getLocation());
        }
        if (destination.exists() && !destination.canWrite()) {
            throw new BuildException("Can't write to "
                                     + destination.getAbsolutePath(),
                                     getLocation());
        }
        if (sources.size() > 1 && !destination.exists()) {
            destination.mkdirs();
        }
    }
    public void setSrc(URL u) {
        add(new URLResource(u));
    }
    public void add(ResourceCollection rc) {
        sources.add(rc);
    }
    public void setDest(File dest) {
        this.destination = dest;
    }
    public void setVerbose(boolean v) {
        verbose = v;
    }
    public void setIgnoreErrors(boolean v) {
        ignoreErrors = v;
    }
    public void setUseTimestamp(boolean v) {
        useTimestamp = v;
    }
    public void setUsername(String u) {
        this.uname = u;
    }
    public void setPassword(String p) {
        this.pword = p;
    }
    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }
    public void setRetries(int r) {
        this.numberRetries = r;
    }
    public void setSkipExisting(boolean s) {
        this.skipExisting = s;
    }
    public void setHttpUseCaches(boolean httpUseCache) {
        this.httpUseCaches = httpUseCache;
    }
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    protected static class Base64Converter
        extends org.apache.tools.ant.util.Base64Converter {
    }
    public interface DownloadProgress {
        void beginDownload();
        void onTick();
        void endDownload();
    }
    public static class NullProgress implements DownloadProgress {
        public void beginDownload() {
        }
        public void onTick() {
        }
        public void endDownload() {
        }
    }
    public static class VerboseProgress implements DownloadProgress  {
        private int dots = 0;
        PrintStream out;
        public VerboseProgress(PrintStream out) {
            this.out = out;
        }
        public void beginDownload() {
            dots = 0;
        }
        public void onTick() {
            out.print(".");
            if (dots++ > DOTS_PER_LINE) {
                out.flush();
                dots = 0;
            }
        }
        public void endDownload() {
            out.println();
            out.flush();
        }
    }
    private class GetThread extends Thread {
        private final URL source;
        private final File dest;
        private final boolean hasTimestamp;
        private final long timestamp;
        private final DownloadProgress progress;
        private final int logLevel;
        private boolean success = false;
        private IOException ioexception = null;
        private BuildException exception = null;
        private InputStream is = null;
        private OutputStream os = null;
        private URLConnection connection;
        private int redirections = 0;
        GetThread(URL source, File dest,
                  boolean h, long t, DownloadProgress p, int l) {
            this.source = source;
            this.dest = dest;
            hasTimestamp = h;
            timestamp = t;
            progress = p;
            logLevel = l;
        }
        public void run() {
            try {
                success = get();
            } catch (IOException ioex) {
                ioexception = ioex;
            } catch (BuildException bex) {
                exception = bex;
            }
        }
        private boolean get() throws IOException, BuildException {
            connection = openConnection(source);
            if (connection == null)
            {
                return false;
            }
            boolean downloadSucceeded = downloadFile();
            if (downloadSucceeded && useTimestamp)  {
                updateTimeStamp();
            }
            return downloadSucceeded;
        }
        private boolean redirectionAllowed(URL aSource, URL aDest) {
            if (!(aSource.getProtocol().equals(aDest.getProtocol()) || (HTTP
                    .equals(aSource.getProtocol()) && HTTPS.equals(aDest
                    .getProtocol())))) {
                String message = "Redirection detected from "
                        + aSource.getProtocol() + " to " + aDest.getProtocol()
                        + ". Protocol switch unsafe, not allowed.";
                if (ignoreErrors) {
                    log(message, logLevel);
                    return false;
                } else {
                    throw new BuildException(message);
                }
            }
            redirections++;
            if (redirections > REDIRECT_LIMIT) {
                String message = "More than " + REDIRECT_LIMIT
                        + " times redirected, giving up";
                if (ignoreErrors) {
                    log(message, logLevel);
                    return false;
                } else {
                    throw new BuildException(message);
                }
            }
            return true;
        }
        private URLConnection openConnection(URL aSource) throws IOException {
            URLConnection connection = aSource.openConnection();
            if (hasTimestamp) {
                connection.setIfModifiedSince(timestamp);
            }
            if (uname != null || pword != null) {
                String up = uname + ":" + pword;
                String encoding;
                Base64Converter encoder = new Base64Converter();
                encoding = encoder.encode(up.getBytes());
                connection.setRequestProperty("Authorization", "Basic "
                        + encoding);
            }
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection)
                        .setInstanceFollowRedirects(false);
                ((HttpURLConnection) connection)
                        .setUseCaches(httpUseCaches);
            }
            try {
                connection.connect();
            } catch (NullPointerException e) {
                throw new BuildException("Failed to parse " + source.toString(), e);
            }
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                int responseCode = httpConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                {
                    String newLocation = httpConnection.getHeaderField("Location");
                    String message = aSource
                            + (responseCode == HttpURLConnection.HTTP_MOVED_PERM ? " permanently"
                                    : "") + " moved to " + newLocation;
                    log(message, logLevel);
                    URL newURL = new URL(aSource, newLocation);
                    if (!redirectionAllowed(aSource, newURL))
                    {
                        return null;
                    }
                    return openConnection(newURL);
                }
                long lastModified = httpConnection.getLastModified();
                if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED
                        || (lastModified != 0 && hasTimestamp && timestamp >= lastModified)) {
                    log("Not modified - so not downloaded", logLevel);
                    return null;
                }
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    String message = "HTTP Authorization failure";
                    if (ignoreErrors) {
                        log(message, logLevel);
                        return null;
                    } else {
                        throw new BuildException(message);
                    }
                }
            }
            return connection;
        }
        private boolean downloadFile()
                throws FileNotFoundException, IOException {
            for (int i = 0; i < numberRetries; i++) {
                try {
                    is = connection.getInputStream();
                    break;
                } catch (IOException ex) {
                    log("Error opening connection " + ex, logLevel);
                }
            }
            if (is == null) {
                log("Can't get " + source + " to " + dest, logLevel);
                if (ignoreErrors) {
                    return false;
                }
                throw new BuildException("Can't get " + source + " to " + dest,
                        getLocation());
            }
            os = new FileOutputStream(dest);
            progress.beginDownload();
            boolean finished = false;
            try {
                byte[] buffer = new byte[BIG_BUFFER_SIZE];
                int length;
                while (!isInterrupted() && (length = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, length);
                    progress.onTick();
                }
                finished = !isInterrupted();
            } finally {
                FileUtils.close(os);
                FileUtils.close(is);
                if (!finished) {
                    dest.delete();
                }
            }
            progress.endDownload();
            return true;
        }
        private void updateTimeStamp() {
            long remoteTimestamp = connection.getLastModified();
            if (verbose)  {
                Date t = new Date(remoteTimestamp);
                log("last modified = " + t.toString()
                    + ((remoteTimestamp == 0)
                       ? " - using current time instead"
                       : ""), logLevel);
            }
            if (remoteTimestamp != 0) {
                FILE_UTILS.setFileLastModified(dest, remoteTimestamp);
            }
        }
        boolean wasSuccessful() throws IOException, BuildException {
            if (ioexception != null) {
                throw ioexception;
            }
            if (exception != null) {
                throw exception;
            }
            return success;
        }
        void closeStreams() {
            interrupt();
            FileUtils.close(os);
            FileUtils.close(is);
            if (!success && dest.exists()) {
                dest.delete();
            }
        }
    }
}
