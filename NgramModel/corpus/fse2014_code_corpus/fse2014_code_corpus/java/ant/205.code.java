package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.filters.FixCrLfFilter;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;
public class FixCRLF extends MatchingTask implements ChainableReader {
    private static final String FIXCRLF_ERROR = "<fixcrlf> error: ";
    public static final String ERROR_FILE_AND_SRCDIR
        = FIXCRLF_ERROR + "srcdir and file are mutually exclusive";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean preserveLastModified = false;
    private File srcDir;
    private File destDir = null;
    private File file;
    private FixCrLfFilter filter = new FixCrLfFilter();
    private Vector fcv = null;
    private String encoding = null;
    private String outputEncoding = null;
    public final Reader chain(final Reader rdr) {
        return filter.chain(rdr);
    }
    public void setSrcdir(File srcDir) {
        this.srcDir = srcDir;
    }
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }
    public void setJavafiles(boolean javafiles) {
        filter.setJavafiles(javafiles);
    }
    public void setFile(File file) {
        this.file = file;
    }
    public void setEol(CrLf attr) {
        filter.setEol(FixCrLfFilter.CrLf.newInstance(attr.getValue()));
    }
    public void setCr(AddAsisRemove attr) {
        log("DEPRECATED: The cr attribute has been deprecated,",
            Project.MSG_WARN);
        log("Please use the eol attribute instead", Project.MSG_WARN);
        String option = attr.getValue();
        CrLf c = new CrLf();
        if (option.equals("remove")) {
            c.setValue("lf");
        } else if (option.equals("asis")) {
            c.setValue("asis");
        } else {
            c.setValue("crlf");
        }
        setEol(c);
    }
    public void setTab(AddAsisRemove attr) {
        filter.setTab(FixCrLfFilter.AddAsisRemove.newInstance(attr.getValue()));
    }
    public void setTablength(int tlength) throws BuildException {
        try {
            filter.setTablength(tlength);
        } catch (IOException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
    public void setEof(AddAsisRemove attr) {
        filter.setEof(FixCrLfFilter.AddAsisRemove.newInstance(attr.getValue()));
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
    public void setFixlast(boolean fixlast) {
        filter.setFixlast(fixlast);
    }
    public void setPreserveLastModified(boolean preserve) {
        preserveLastModified = preserve;
    }
    public void execute() throws BuildException {
        validate();
        String enc = encoding == null ? "default" : encoding;
        log("options:"
            + " eol=" + filter.getEol().getValue()
            + " tab=" + filter.getTab().getValue()
            + " eof=" + filter.getEof().getValue()
            + " tablength=" + filter.getTablength()
            + " encoding=" + enc
            + " outputencoding="
            + (outputEncoding == null ? enc : outputEncoding),
            Project.MSG_VERBOSE);
        DirectoryScanner ds = super.getDirectoryScanner(srcDir);
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            processFile(files[i]);
        }
    }
    private void validate() throws BuildException {
        if (file != null) {
            if (srcDir != null) {
                throw new BuildException(ERROR_FILE_AND_SRCDIR);
            }
            fileset.setFile(file);
            srcDir = file.getParentFile();
        }
        if (srcDir == null) {
            throw new BuildException(
                FIXCRLF_ERROR + "srcdir attribute must be set!");
        }
        if (!srcDir.exists()) {
            throw new BuildException(
                FIXCRLF_ERROR + "srcdir does not exist: '" + srcDir + "'");
        }
        if (!srcDir.isDirectory()) {
            throw new BuildException(
                FIXCRLF_ERROR + "srcdir is not a directory: '" + srcDir + "'");
        }
        if (destDir != null) {
            if (!destDir.exists()) {
                throw new BuildException(
                    FIXCRLF_ERROR + "destdir does not exist: '"
                    + destDir + "'");
            }
            if (!destDir.isDirectory()) {
                throw new BuildException(
                    FIXCRLF_ERROR + "destdir is not a directory: '"
                    + destDir + "'");
            }
        }
    }
    private void processFile(String file) throws BuildException {
        File srcFile = new File(srcDir, file);
        long lastModified = srcFile.lastModified();
        File destD = destDir == null ? srcDir : destDir;
        if (fcv == null) {
            FilterChain fc = new FilterChain();
            fc.add(filter);
            fcv = new Vector(1);
            fcv.add(fc);
        }
        File tmpFile = FILE_UTILS.createTempFile("fixcrlf", "", null, true, false);
        try {
            FILE_UTILS.copyFile(srcFile, tmpFile, null, fcv, false, false,
                encoding, outputEncoding == null ? encoding : outputEncoding,
                getProject());
            File destFile = new File(destD, file);
            boolean destIsWrong = true;
            if (destFile.exists()) {
                log("destFile " + destFile + " exists", Project.MSG_DEBUG);
                destIsWrong = !FILE_UTILS.contentEquals(destFile, tmpFile);
                log(destFile + (destIsWrong ? " is being written"
                    : " is not written, as the contents are identical"),
                    Project.MSG_DEBUG);
            }
            if (destIsWrong) {
                FILE_UTILS.rename(tmpFile, destFile);
                if (preserveLastModified) {
                    log("preserved lastModified for " + destFile,
                        Project.MSG_DEBUG);
                    FILE_UTILS.setFileLastModified(destFile, lastModified);
                }
            }
        } catch (IOException e) {
            throw new BuildException("error running fixcrlf on file " + srcFile, e);
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                FILE_UTILS.tryHardToDelete(tmpFile);
            }
        }
    }
    protected class OneLiner implements Enumeration {
        private static final int UNDEF = -1;
        private static final int NOTJAVA = 0;
        private static final int LOOKING = 1;
        private static final int INBUFLEN = 8192;
        private static final int LINEBUFLEN = 200;
        private static final char CTRLZ = '\u001A';
        private int state = filter.getJavafiles() ? LOOKING : NOTJAVA;
        private StringBuffer eolStr = new StringBuffer(LINEBUFLEN);
        private StringBuffer eofStr = new StringBuffer();
        private BufferedReader reader;
        private StringBuffer line = new StringBuffer();
        private boolean reachedEof = false;
        private File srcFile;
        public OneLiner(File srcFile)
            throws BuildException {
            this.srcFile = srcFile;
            try {
                reader = new BufferedReader(
                    ((encoding == null) ? new FileReader(srcFile)
                    : new InputStreamReader(
                    new FileInputStream(srcFile), encoding)), INBUFLEN);
                nextLine();
            } catch (IOException e) {
                throw new BuildException(srcFile + ": " + e.getMessage(),
                                         e, getLocation());
            }
        }
        protected void nextLine()
            throws BuildException {
            int ch = -1;
            int eolcount = 0;
            eolStr = new StringBuffer();
            line = new StringBuffer();
            try {
                ch = reader.read();
                while (ch != -1 && ch != '\r' && ch != '\n') {
                    line.append((char) ch);
                    ch = reader.read();
                }
                if (ch == -1 && line.length() == 0) {
                    reachedEof = true;
                    return;
                }
                switch ((char) ch) {
                case '\r':
                    ++eolcount;
                    eolStr.append('\r');
                    reader.mark(2);
                    ch = reader.read();
                    switch (ch) {
                    case '\r':
                        ch = reader.read();
                        if ((char) (ch) == '\n') {
                            eolcount += 2;
                            eolStr.append("\r\n");
                        } else {
                            reader.reset();
                        }
                        break;
                    case '\n':
                        ++eolcount;
                        eolStr.append('\n');
                        break;
                    case -1:
                        break;
                    default:
                        reader.reset();
                        break;
                    } 
                    break;
                case '\n':
                    ++eolcount;
                    eolStr.append('\n');
                    break;
                default:
                } 
                if (eolcount == 0) {
                    int i = line.length();
                    while (--i >= 0 && line.charAt(i) == CTRLZ) {
                    }
                    if (i < line.length() - 1) {
                        eofStr.append(line.toString().substring(i + 1));
                        if (i < 0) {
                            line.setLength(0);
                            reachedEof = true;
                        } else {
                            line.setLength(i + 1);
                        }
                    }
                } 
            } catch (IOException e) {
                throw new BuildException(srcFile + ": " + e.getMessage(),
                                         e, getLocation());
            }
        }
        public String getEofStr() {
            return eofStr.substring(0);
        }
        public int getState() {
            return state;
        }
        public void setState(int state) {
            this.state = state;
        }
        public boolean hasMoreElements() {
            return !reachedEof;
        }
        public Object nextElement()
            throws NoSuchElementException {
            if (!hasMoreElements()) {
                throw new NoSuchElementException("OneLiner");
            }
            BufferLine tmpLine =
                    new BufferLine(line.toString(), eolStr.substring(0));
            nextLine();
            return tmpLine;
        }
        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }
        class BufferLine {
            private int next = 0;
            private int column = 0;
            private int lookahead = UNDEF;
            private String line;
            private String eolStr;
            public BufferLine(String line, String eolStr)
                throws BuildException {
                next = 0;
                column = 0;
                this.line = line;
                this.eolStr = eolStr;
            }
            public int getNext() {
                return next;
            }
            public void setNext(int next) {
                this.next = next;
            }
            public int getLookahead() {
                return lookahead;
            }
            public void setLookahead(int lookahead) {
                this.lookahead = lookahead;
            }
            public char getChar(int i) {
                return line.charAt(i);
            }
            public char getNextChar() {
                return getChar(next);
            }
            public char getNextCharInc() {
                return getChar(next++);
            }
            public int getColumn() {
                return column;
            }
            public void setColumn(int col) {
                column = col;
            }
            public int incColumn() {
                return column++;
            }
            public int length() {
                return line.length();
            }
            public int getEolLength() {
                return eolStr.length();
            }
            public String getLineString() {
                return line;
            }
            public String getEol() {
                return eolStr;
            }
            public String substring(int begin) {
                return line.substring(begin);
            }
            public String substring(int begin, int end) {
                return line.substring(begin, end);
            }
            public void setState(int state) {
                OneLiner.this.setState(state);
            }
            public int getState() {
                return OneLiner.this.getState();
            }
        }
    }
    public static class AddAsisRemove extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"add", "asis", "remove"};
        }
    }
    public static class CrLf extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"asis", "cr", "lf", "crlf",
                                 "mac", "unix", "dos"};
        }
    }
}
