package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Intersect;
import org.apache.tools.ant.types.resources.LogOutputResource;
import org.apache.tools.ant.types.resources.Restrict;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.StringResource;
import org.apache.tools.ant.types.resources.selectors.Not;
import org.apache.tools.ant.types.resources.selectors.Exists;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.ConcatResourceInputStream;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ReaderInputStream;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.ant.util.StringUtils;
public class Concat extends Task implements ResourceCollection {
    private static final int BUFFER_SIZE = 8192;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final ResourceSelector EXISTS = new Exists();
    private static final ResourceSelector NOT_EXISTS = new Not(EXISTS);
    public static class TextElement extends ProjectComponent {
        private String   value = "";
        private boolean  trimLeading = false;
        private boolean  trim = false;
        private boolean  filtering = true;
        private String   encoding = null;
        public void setFiltering(boolean filtering) {
            this.filtering = filtering;
        }
        private boolean getFiltering() {
            return filtering;
        }
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
        public void setFile(File file) throws BuildException {
            if (!file.exists()) {
                throw new BuildException("File " + file + " does not exist.");
            }
            BufferedReader reader = null;
            try {
                if (this.encoding == null) {
                    reader = new BufferedReader(new FileReader(file));
                } else {
                    reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file),
                                              this.encoding));
                }
                value = FileUtils.safeReadFully(reader);
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                FileUtils.close(reader);
            }
        }
        public void addText(String value) {
            this.value += getProject().replaceProperties(value);
        }
        public void setTrimLeading(boolean strip) {
            this.trimLeading = strip;
        }
        public void setTrim(boolean trim) {
            this.trim = trim;
        }
        public String getValue() {
            if (value == null) {
                value = "";
            }
            if (value.trim().length() == 0) {
                value = "";
            }
            if (trimLeading) {
                char[] current = value.toCharArray();
                StringBuffer b = new StringBuffer(current.length);
                boolean startOfLine = true;
                int pos = 0;
                while (pos < current.length) {
                    char ch = current[pos++];
                    if (startOfLine) {
                        if (ch == ' ' || ch == '\t') {
                            continue;
                        }
                        startOfLine = false;
                    }
                    b.append(ch);
                    if (ch == '\n' || ch == '\r') {
                        startOfLine = true;
                    }
                }
                value = b.toString();
            }
            if (trim) {
                value = value.trim();
            }
            return value;
        }
    }
    private interface ReaderFactory {
        Reader getReader(Object o) throws IOException;
    }
    private final class MultiReader extends Reader {
        private Reader reader = null;
        private int    lastPos = 0;
        private char[] lastChars = new char[eolString.length()];
        private boolean needAddSeparator = false;
        private Iterator readerSources;
        private ReaderFactory factory;
        private MultiReader(Iterator readerSources, ReaderFactory factory) {
            this.readerSources = readerSources;
            this.factory = factory;
        }
        private Reader getReader() throws IOException {
            if (reader == null && readerSources.hasNext()) {
                reader = factory.getReader(readerSources.next());
                Arrays.fill(lastChars, (char) 0);
            }
            return reader;
        }
        private void nextReader() throws IOException {
            close();
            reader = null;
        }
        public int read() throws IOException {
            if (needAddSeparator) {
                int ret = eolString.charAt(lastPos++);
                if (lastPos >= eolString.length()) {
                    lastPos = 0;
                    needAddSeparator = false;
                }
                return ret;
            }
            while (getReader() != null) {
                int ch = getReader().read();
                if (ch == -1) {
                    nextReader();
                    if (isFixLastLine() && isMissingEndOfLine()) {
                        needAddSeparator = true;
                        lastPos = 0;
                    }
                } else {
                    addLastChar((char) ch);
                    return ch;
                }
            }
            return -1;
        }
        public int read(char[] cbuf, int off, int len)
            throws IOException {
            int amountRead = 0;
            while (getReader() != null || needAddSeparator) {
                if (needAddSeparator) {
                    cbuf[off] = eolString.charAt(lastPos++);
                    if (lastPos >= eolString.length()) {
                        lastPos = 0;
                        needAddSeparator = false;
                    }
                    len--;
                    off++;
                    amountRead++;
                    if (len == 0) {
                        return amountRead;
                    }
                    continue;
                }
                int nRead = getReader().read(cbuf, off, len);
                if (nRead == -1 || nRead == 0) {
                    nextReader();
                    if (isFixLastLine() && isMissingEndOfLine()) {
                        needAddSeparator = true;
                        lastPos = 0;
                    }
                } else {
                    if (isFixLastLine()) {
                        for (int i = nRead;
                                 i > (nRead - lastChars.length);
                                 --i) {
                            if (i <= 0) {
                                break;
                            }
                            addLastChar(cbuf[off + i - 1]);
                        }
                    }
                    len -= nRead;
                    off += nRead;
                    amountRead += nRead;
                    if (len == 0) {
                        return amountRead;
                    }
                }
            }
            if (amountRead == 0) {
                return -1;
            } else {
                return amountRead;
            }
        }
        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }
        private void addLastChar(char ch) {
            for (int i = lastChars.length - 2; i >= 0; --i) {
                lastChars[i] = lastChars[i + 1];
            }
            lastChars[lastChars.length - 1] = ch;
        }
        private boolean isMissingEndOfLine() {
            for (int i = 0; i < lastChars.length; ++i) {
                if (lastChars[i] != eolString.charAt(i)) {
                    return true;
                }
            }
            return false;
        }
        private boolean isFixLastLine() {
            return fixLastLine && textBuffer == null;
        }
    }
    private final class ConcatResource extends Resource {
        private ResourceCollection c;
        private ConcatResource(ResourceCollection c) {
            this.c = c;
        }
        public InputStream getInputStream() throws IOException {
            if (binary) {
                ConcatResourceInputStream result = new ConcatResourceInputStream(c);
                result.setManagingComponent(this);
                return result;
            }
            Reader resourceReader = getFilteredReader(
                    new MultiReader(c.iterator(), resourceReaderFactory));
            Reader rdr;
            if (header == null && footer == null) {
                rdr = resourceReader;
            } else {
                int readerCount = 1;
                if (header != null) {
                    readerCount++;
                }
                if (footer != null) {
                    readerCount++;
                }
                Reader[] readers = new Reader[readerCount];
                int pos = 0;
                if (header != null) {
                    readers[pos] = new StringReader(header.getValue());
                    if (header.getFiltering()) {
                        readers[pos] = getFilteredReader(readers[pos]);
                    }
                    pos++;
                }
                readers[pos++] = resourceReader;
                if (footer != null) {
                    readers[pos] = new StringReader(footer.getValue());
                    if (footer.getFiltering()) {
                        readers[pos] = getFilteredReader(readers[pos]);
                    }
                }
                rdr = new MultiReader(Arrays.asList(readers).iterator(),
                        identityReaderFactory);
            }
            return outputEncoding == null ? new ReaderInputStream(rdr)
                    : new ReaderInputStream(rdr, outputEncoding);
        }
        public String getName() {
            return "concat (" + String.valueOf(c) + ")";
        }
    }
    private Resource dest;
    private boolean append;
    private String encoding;
    private String outputEncoding;
    private boolean binary;
    private StringBuffer textBuffer;
    private Resources rc;
    private Vector filterChains;
    private boolean forceOverwrite = true;
    private boolean force = false;
    private TextElement footer;
    private TextElement header;
    private boolean fixLastLine = false;
    private String eolString;
    private Writer outputWriter = null;
    private boolean ignoreEmpty = true;
    private ReaderFactory resourceReaderFactory  = new ReaderFactory() {
        public Reader getReader(Object o) throws IOException {
            InputStream is = ((Resource) o).getInputStream();
            return new BufferedReader(encoding == null
                ? new InputStreamReader(is)
                : new InputStreamReader(is, encoding));
        }
    };
    private ReaderFactory identityReaderFactory = new ReaderFactory() {
        public Reader getReader(Object o) {
            return (Reader) o;
        }
    };
    public Concat() {
        reset();
    }
    public void reset() {
        append = false;
        forceOverwrite = true;
        dest = null;
        encoding = null;
        outputEncoding = null;
        fixLastLine = false;
        filterChains = null;
        footer = null;
        header = null;
        binary = false;
        outputWriter = null;
        textBuffer = null;
        eolString = StringUtils.LINE_SEP;
        rc = null;
        ignoreEmpty = true;
        force = false;
    }
    public void setDestfile(File destinationFile) {
        setDest(new FileResource(destinationFile));
    }
    public void setDest(Resource dest) {
        this.dest = dest;
    }
    public void setAppend(boolean append) {
        this.append = append;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
        if (outputEncoding == null) {
            outputEncoding = encoding;
        }
    }
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
    public void setForce(boolean forceOverwrite) {
        this.forceOverwrite = forceOverwrite;
    }
    public void setOverwrite(boolean forceOverwrite) {
        setForce(forceOverwrite);
    }
    public void setForceReadOnly(boolean f) {
        force = f;
    }
    public void setIgnoreEmpty(boolean ignoreEmpty) {
        this.ignoreEmpty = ignoreEmpty;
    }
     public Path createPath() {
        Path path = new Path(getProject());
        add(path);
        return path;
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void addFilelist(FileList list) {
        add(list);
    }
    public void add(ResourceCollection c) {
        synchronized (this) {
            if (rc == null) {
                rc = new Resources();
                rc.setProject(getProject());
                rc.setCache(true);
            }
        }
        rc.add(c);
    }
    public void addFilterChain(FilterChain filterChain) {
        if (filterChains == null) {
            filterChains = new Vector();
        }
        filterChains.addElement(filterChain);
    }
    public void addText(String text) {
        if (textBuffer == null) {
            textBuffer = new StringBuffer(text.length());
        }
        textBuffer.append(text);
    }
    public void addHeader(TextElement headerToAdd) {
        this.header = headerToAdd;
    }
    public void addFooter(TextElement footerToAdd) {
        this.footer = footerToAdd;
    }
    public void setFixLastLine(boolean fixLastLine) {
        this.fixLastLine = fixLastLine;
    }
    public void setEol(FixCRLF.CrLf crlf) {
        String s = crlf.getValue();
        if (s.equals("cr") || s.equals("mac")) {
            eolString = "\r";
        } else if (s.equals("lf") || s.equals("unix")) {
            eolString = "\n";
        } else if (s.equals("crlf") || s.equals("dos")) {
            eolString = "\r\n";
        }
    }
    public void setWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }
    public void setBinary(boolean binary) {
        this.binary = binary;
    }
    public void execute() {
        validate();
        if (binary && dest == null) {
            throw new BuildException(
                "dest|destfile attribute is required for binary concatenation");
        }
        ResourceCollection c = getResources();
        if (isUpToDate(c)) {
            log(dest + " is up-to-date.", Project.MSG_VERBOSE);
            return;
        }
        if (c.size() == 0 && ignoreEmpty) {
            return;
        }
        try {
            ResourceUtils.copyResource(new ConcatResource(c), dest == null
                                       ? new LogOutputResource(this, Project.MSG_WARN)
                                       : dest,
                                       null, null, true, false, append, null,
                                       null, getProject(), force);
        } catch (IOException e) {
            throw new BuildException("error concatenating content to " + dest, e);
        }
    }
    public Iterator iterator() {
        validate();
        return Collections.singletonList(new ConcatResource(getResources())).iterator();
    }
    public int size() {
        return 1;
    }
    public boolean isFilesystemOnly() {
        return false;
    }
    private void validate() {
        sanitizeText();
        if (binary) {
            if (textBuffer != null) {
                throw new BuildException(
                    "Nested text is incompatible with binary concatenation");
            }
            if (encoding != null || outputEncoding != null) {
                throw new BuildException(
                    "Setting input or output encoding is incompatible with binary"
                    + " concatenation");
            }
            if (filterChains != null) {
                throw new BuildException(
                    "Setting filters is incompatible with binary concatenation");
            }
            if (fixLastLine) {
                throw new BuildException(
                    "Setting fixlastline is incompatible with binary concatenation");
            }
            if (header != null || footer != null) {
                throw new BuildException(
                    "Nested header or footer is incompatible with binary concatenation");
            }
        }
        if (dest != null && outputWriter != null) {
            throw new BuildException(
                "Cannot specify both a destination resource and an output writer");
        }
        if (rc == null && textBuffer == null) {
            throw new BuildException(
                "At least one resource must be provided, or some text.");
        }
        if (rc != null && textBuffer != null) {
            throw new BuildException(
                "Cannot include inline text when using resources.");
        }
    }
    private ResourceCollection getResources() {
        if (rc == null) {
            return new StringResource(getProject(), textBuffer.toString());
        }
        if (dest != null) {
            Intersect checkDestNotInSources = new Intersect();
            checkDestNotInSources.setProject(getProject());
            checkDestNotInSources.add(rc);
            checkDestNotInSources.add(dest);
            if (checkDestNotInSources.size() > 0) {
                throw new BuildException("Destination resource " + dest
                        + " was specified as an input resource.");
            }
        }
        Restrict noexistRc = new Restrict();
        noexistRc.add(NOT_EXISTS);
        noexistRc.add(rc);
        for (Iterator i = noexistRc.iterator(); i.hasNext();) {
            log(i.next() + " does not exist.", Project.MSG_ERR);
        }
        Restrict result = new Restrict();
        result.add(EXISTS);
        result.add(rc);
        return result;
    }
    private boolean isUpToDate(ResourceCollection c) {
        if (dest == null || forceOverwrite) {
            return false;
        }
        for (Iterator i = c.iterator(); i.hasNext();) {
            Resource r = (Resource) i.next();
            if (SelectorUtils.isOutOfDate(r, dest, FILE_UTILS.getFileTimestampGranularity())) {
                return false;
            }
        }
        return true;
    }
    private void sanitizeText() {
        if (textBuffer != null && "".equals(textBuffer.toString().trim())) {
            textBuffer = null;
        }
    }
    private Reader getFilteredReader(Reader r) {
        if (filterChains == null) {
            return r;
        }
        ChainReaderHelper helper = new ChainReaderHelper();
        helper.setBufferSize(BUFFER_SIZE);
        helper.setPrimaryReader(r);
        helper.setFilterChains(filterChains);
        helper.setProject(getProject());
        return helper.getAssembledReader();
    }
}
