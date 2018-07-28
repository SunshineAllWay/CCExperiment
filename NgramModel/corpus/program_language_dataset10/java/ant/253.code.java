package org.apache.tools.ant.taskdefs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
public class Replace extends MatchingTask {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File sourceFile = null;
    private NestedString token = null;
    private NestedString value = new NestedString();
    private Resource propertyResource = null;
    private Resource replaceFilterResource = null;
    private Properties properties = null;
    private ArrayList replacefilters = new ArrayList();
    private File dir = null;
    private int fileCount;
    private int replaceCount;
    private boolean summary = false;
    private String encoding = null;
    private Union resources;
    private boolean preserveLastModified = false;
    private boolean failOnNoReplacements = false;
    public class NestedString {
        private boolean expandProperties = false;
        private StringBuffer buf = new StringBuffer();
        public void setExpandProperties(boolean b) {
            expandProperties = b;
        }
        public void addText(String val) {
            buf.append(val);
        }
        public String getText() {
            String s = buf.toString();
            return expandProperties ? getProject().replaceProperties(s) : s;
        }
    }
    public class Replacefilter {
        private NestedString token;
        private NestedString value;
        private String replaceValue;
        private String property;
        private StringBuffer inputBuffer;
        private StringBuffer outputBuffer = new StringBuffer();
        public void validate() throws BuildException {
            if (token == null) {
                String message = "token is a mandatory for replacefilter.";
                throw new BuildException(message);
            }
            if ("".equals(token.getText())) {
                String message = "The token must not be an empty "
                    + "string.";
                throw new BuildException(message);
            }
            if ((value != null) && (property != null)) {
                String message = "Either value or property "
                    + "can be specified, but a replacefilter "
                    + "element cannot have both.";
                throw new BuildException(message);
            }
            if ((property != null)) {
                if (propertyResource == null) {
                    String message = "The replacefilter's property attribute "
                        + "can only be used with the replacetask's "
                        + "propertyFile/Resource attribute.";
                    throw new BuildException(message);
                }
                if (properties == null
                    || properties.getProperty(property) == null) {
                    String message = "property \"" + property
                        + "\" was not found in " + propertyResource.getName();
                    throw new BuildException(message);
                }
            }
            replaceValue = getReplaceValue();
        }
        public String getReplaceValue() {
            if (property != null) {
                return properties.getProperty(property);
            } else if (value != null) {
                return value.getText();
            } else if (Replace.this.value != null) {
                return Replace.this.value.getText();
            } else {
                return "";
            }
        }
        public void setToken(String t) {
            createReplaceToken().addText(t);
        }
        public String getToken() {
            return token.getText();
        }
        public void setValue(String value) {
            createReplaceValue().addText(value);
        }
        public String getValue() {
            return value.getText();
        }
        public void setProperty(String property) {
            this.property = property;
        }
        public String getProperty() {
            return property;
        }
        public NestedString createReplaceToken() {
            if (token == null) {
                token = new NestedString();
            }
            return token;
        }
        public NestedString createReplaceValue() {
            if (value == null) {
                value = new NestedString();
            }
            return value;
        }
        StringBuffer getOutputBuffer() {
            return outputBuffer;
        }
        void setInputBuffer(StringBuffer input) {
            inputBuffer = input;
        }
        boolean process() {
            String t = getToken();
            if (inputBuffer.length() > t.length()) {
                int pos = replace();
                pos = Math.max((inputBuffer.length() - t.length()), pos);
                outputBuffer.append(inputBuffer.substring(0, pos));
                inputBuffer.delete(0, pos);
                return true;
            }
            return false;
        }
        void flush() {
            replace();
            outputBuffer.append(inputBuffer);
            inputBuffer.delete(0, inputBuffer.length());
        }
        private int replace() {
            String t = getToken();
            int found = inputBuffer.indexOf(t);
            int pos = -1;
            final int tokenLength = t.length();
            final int replaceValueLength = replaceValue.length();
            while (found >= 0) {
                inputBuffer.replace(found, found + tokenLength, replaceValue);
                pos = found + replaceValueLength;
                found = inputBuffer.indexOf(t, pos);
                ++replaceCount;
            }
            return pos;
        }
    }
    private class FileInput {
        private StringBuffer outputBuffer;
        private Reader reader;
        private char[] buffer;
        private static final int BUFF_SIZE = 4096;
        FileInput(File source) throws IOException {
            outputBuffer = new StringBuffer();
            buffer = new char[BUFF_SIZE];
            if (encoding == null) {
                reader = new BufferedReader(new FileReader(source));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(source), encoding));
            }
        }
        StringBuffer getOutputBuffer() {
            return outputBuffer;
        }
        boolean readChunk() throws IOException {
            int bufferLength = 0;
            bufferLength = reader.read(buffer);
            if (bufferLength < 0) {
                return false;
            }
            outputBuffer.append(new String(buffer, 0, bufferLength));
            return true;
        }
        void close() throws IOException {
            reader.close();
        }
        void closeQuietly() {
            FileUtils.close(reader);
        }
    }
    private class FileOutput {
        private StringBuffer inputBuffer;
        private Writer writer;
        FileOutput(File out) throws IOException {
                if (encoding == null) {
                    writer = new BufferedWriter(new FileWriter(out));
                } else {
                    writer = new BufferedWriter(new OutputStreamWriter
                            (new FileOutputStream(out), encoding));
                }
        }
        void setInputBuffer(StringBuffer input) {
            inputBuffer = input;
        }
        boolean process() throws IOException {
            writer.write(inputBuffer.toString());
            inputBuffer.delete(0, inputBuffer.length());
            return false;
        }
        void flush() throws IOException {
            process();
            writer.flush();
        }
        void close() throws IOException {
            writer.close();
        }
        void closeQuietly() {
            FileUtils.close(writer);
        }
    }
    public void execute() throws BuildException {
        ArrayList savedFilters = (ArrayList) replacefilters.clone();
        Properties savedProperties =
            properties == null ? null : (Properties) properties.clone();
        if (token != null) {
            StringBuffer val = new StringBuffer(value.getText());
            stringReplace(val, "\r\n", "\n");
            stringReplace(val, "\n", StringUtils.LINE_SEP);
            StringBuffer tok = new StringBuffer(token.getText());
            stringReplace(tok, "\r\n", "\n");
            stringReplace(tok, "\n", StringUtils.LINE_SEP);
            Replacefilter firstFilter = createPrimaryfilter();
            firstFilter.setToken(tok.toString());
            firstFilter.setValue(val.toString());
        }
        try {
            if (replaceFilterResource != null) {
                Properties props = getProperties(replaceFilterResource);
                Iterator e = props.keySet().iterator();
                while (e.hasNext()) {
                    String tok =  e.next().toString();
                    Replacefilter replaceFilter = createReplacefilter();
                    replaceFilter.setToken(tok);
                    replaceFilter.setValue(props.getProperty(tok));
                }
            }
            validateAttributes();
            if (propertyResource != null) {
                properties = getProperties(propertyResource);
            }
            validateReplacefilters();
            fileCount = 0;
            replaceCount = 0;
            if (sourceFile != null) {
                processFile(sourceFile);
            }
            if (dir != null) {
                DirectoryScanner ds = super.getDirectoryScanner(dir);
                String[] srcs = ds.getIncludedFiles();
                for (int i = 0; i < srcs.length; i++) {
                    File file = new File(dir, srcs[i]);
                    processFile(file);
                }
            }
            if (resources != null) {
                for (Iterator i = resources.iterator(); i.hasNext(); ) {
                    FileProvider fp =
                        (FileProvider) ((Resource) i.next())
                        .as(FileProvider.class);
                    processFile(fp.getFile());
                }
            }
            if (summary) {
                log("Replaced " + replaceCount + " occurrences in "
                    + fileCount + " files.", Project.MSG_INFO);
            }
            if (failOnNoReplacements && replaceCount == 0) {
                throw new BuildException("didn't replace anything");
            }
        } finally {
            replacefilters = savedFilters;
            properties = savedProperties;
        } 
    }
    public void validateAttributes() throws BuildException {
        if (sourceFile == null && dir == null && resources == null) {
            String message = "Either the file or the dir attribute "
                + "or nested resources must be specified";
            throw new BuildException(message, getLocation());
        }
        if (propertyResource != null && !propertyResource.isExists()) {
            String message = "Property file " + propertyResource.getName()
                + " does not exist.";
            throw new BuildException(message, getLocation());
        }
        if (token == null && replacefilters.size() == 0) {
            String message = "Either token or a nested replacefilter "
                + "must be specified";
            throw new BuildException(message, getLocation());
        }
        if (token != null && "".equals(token.getText())) {
            String message = "The token attribute must not be an empty string.";
            throw new BuildException(message, getLocation());
        }
    }
    public void validateReplacefilters()
            throws BuildException {
        for (int i = 0; i < replacefilters.size(); i++) {
            Replacefilter element =
                (Replacefilter) replacefilters.get(i);
            element.validate();
        }
    }
    public Properties getProperties(File propertyFile) throws BuildException {
        return getProperties(new FileResource(getProject(), propertyFile));
    }
    public Properties getProperties(Resource propertyResource)
        throws BuildException {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = propertyResource.getInputStream();
            props.load(in);
        } catch (IOException e) {
            String message = "Property resource (" + propertyResource.getName()
                + ") cannot be loaded.";
            throw new BuildException(message);
        } finally {
            FileUtils.close(in);
        }
        return props;
    }
    private void processFile(File src) throws BuildException {
        if (!src.exists()) {
            throw new BuildException("Replace: source file " + src.getPath()
                                     + " doesn't exist", getLocation());
        }
        File temp = null;
        FileInput in = null;
        FileOutput out = null;
        try {
            in = new FileInput(src);
            temp = FILE_UTILS.createTempFile("rep", ".tmp",
                    src.getParentFile(), false, true);
            out = new FileOutput(temp);
            int repCountStart = replaceCount;
            logFilterChain(src.getPath());
            out.setInputBuffer(buildFilterChain(in.getOutputBuffer()));
            while (in.readChunk()) {
                if (processFilterChain()) {
                    out.process();
                }
            }
            flushFilterChain();
            out.flush();
            in.close();
            in = null;
            out.close();
            out = null;
            boolean changes = (replaceCount != repCountStart);
            if (changes) {
                fileCount++;
                long origLastModified = src.lastModified();
                FILE_UTILS.rename(temp, src);
                if (preserveLastModified) {
                    FILE_UTILS.setFileLastModified(src, origLastModified);
                }
                temp = null;
            }
        } catch (IOException ioe) {
            throw new BuildException("IOException in " + src + " - "
                    + ioe.getClass().getName() + ":"
                    + ioe.getMessage(), ioe, getLocation());
        } finally {
            if (null != in) {
                in.closeQuietly();
            }
            if (null != out) {
                out.closeQuietly();
            }
            if (temp != null) {
                if (!temp.delete()) {
                    temp.deleteOnExit();
                }
            }
        }
    }
    private void flushFilterChain() {
        for (int i = 0; i < replacefilters.size(); i++) {
            Replacefilter filter = (Replacefilter) replacefilters.get(i);
            filter.flush();
        }
    }
    private boolean processFilterChain() {
        for (int i = 0; i < replacefilters.size(); i++) {
            Replacefilter filter = (Replacefilter) replacefilters.get(i);
            if (!filter.process()) {
                return false;
            }
        }
        return true;
    }
    private StringBuffer buildFilterChain(StringBuffer inputBuffer) {
        StringBuffer buf = inputBuffer;
        for (int i = 0; i < replacefilters.size(); i++) {
            Replacefilter filter = (Replacefilter) replacefilters.get(i);
            filter.setInputBuffer(buf);
            buf = filter.getOutputBuffer();
        }
        return buf;
    }
    private void logFilterChain(String filename) {
        for (int i = 0; i < replacefilters.size(); i++) {
            Replacefilter filter = (Replacefilter) replacefilters.get(i);
            log("Replacing in " + filename + ": " + filter.getToken()
                    + " --> " + filter.getReplaceValue(), Project.MSG_VERBOSE);
        }
    }
    public void setFile(File file) {
        this.sourceFile = file;
    }
    public void setSummary(boolean summary) {
        this.summary = summary;
    }
    public void setReplaceFilterFile(File replaceFilterFile) {
        setReplaceFilterResource(new FileResource(getProject(),
                                                  replaceFilterFile));
    }
    public void setReplaceFilterResource(Resource replaceFilter) {
        this.replaceFilterResource = replaceFilter;
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    public void setToken(String token) {
        createReplaceToken().addText(token);
    }
    public void setValue(String value) {
        createReplaceValue().addText(value);
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public NestedString createReplaceToken() {
        if (token == null) {
            token = new NestedString();
        }
        return token;
    }
    public NestedString createReplaceValue() {
        return value;
    }
    public void setPropertyFile(File propertyFile) {
        setPropertyResource(new FileResource(propertyFile));
    }
    public void setPropertyResource(Resource propertyResource) {
        this.propertyResource = propertyResource;
    }
    public Replacefilter createReplacefilter() {
        Replacefilter filter = new Replacefilter();
        replacefilters.add(filter);
        return filter;
    }
    public void addConfigured(ResourceCollection rc) {
        if (!rc.isFilesystemOnly()) {
            throw new BuildException("only filesystem resources are supported");
        }
        if (resources == null) {
            resources = new Union();
        }
        resources.add(rc);
    }
    public void setPreserveLastModified(boolean b) {
        preserveLastModified = b;
    }
    public void setFailOnNoReplacements(boolean b) {
        failOnNoReplacements = b;
    }
    private Replacefilter createPrimaryfilter() {
        Replacefilter filter = new Replacefilter();
        replacefilters.add(0, filter);
        return filter;
    }
    private void stringReplace(StringBuffer str, String str1, String str2) {
        int found = str.indexOf(str1);
        final int str1Length = str1.length();
        final int str2Length = str2.length();
        while (found >= 0) {
            str.replace(found, found + str1Length, str2);
            found = str.indexOf(str1, found + str2Length);
        }
    }
}
