package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.EnumeratedAttribute;
public final class FixCrLfFilter extends BaseParamFilterReader implements ChainableReader {
    private static final int DEFAULT_TAB_LENGTH = 8;
    private static final int MIN_TAB_LENGTH = 2;
    private static final int MAX_TAB_LENGTH = 80;
    private static final char CTRLZ = '\u001A';
    private int tabLength = DEFAULT_TAB_LENGTH;
    private CrLf eol;
    private AddAsisRemove ctrlz;
    private AddAsisRemove tabs;
    private boolean javafiles = false;
    private boolean fixlast = true;
    private boolean initialized = false;
    public FixCrLfFilter() {
        super();
    }
    public FixCrLfFilter(final Reader in) throws IOException {
        super(in);
    }
    {
        tabs = AddAsisRemove.ASIS;
        if (Os.isFamily("mac") && !Os.isFamily("unix")) {
            ctrlz = AddAsisRemove.REMOVE;
            setEol(CrLf.MAC);
        } else if (Os.isFamily("dos")) {
            ctrlz = AddAsisRemove.ASIS;
            setEol(CrLf.DOS);
        } else {
            ctrlz = AddAsisRemove.REMOVE;
            setEol(CrLf.UNIX);
        }
    }
    public Reader chain(final Reader rdr) {
        try {
            FixCrLfFilter newFilter = new FixCrLfFilter(rdr);
            newFilter.setJavafiles(getJavafiles());
            newFilter.setEol(getEol());
            newFilter.setTab(getTab());
            newFilter.setTablength(getTablength());
            newFilter.setEof(getEof());
            newFilter.setFixlast(getFixlast());
            newFilter.initInternalFilters();
            return newFilter;
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    public AddAsisRemove getEof() {
        return ctrlz.newInstance();
    }
    public CrLf getEol() {
        return eol.newInstance();
    }
    public boolean getFixlast() {
        return fixlast;
    }
    public boolean getJavafiles() {
        return javafiles;
    }
    public AddAsisRemove getTab() {
        return tabs.newInstance();
    }
    public int getTablength() {
        return tabLength;
    }
    private static String calculateEolString(CrLf eol) {
        if (eol == CrLf.ASIS) {
            return System.getProperty("line.separator");
        }
        if (eol == CrLf.CR || eol == CrLf.MAC) {
            return "\r";
        }
        if (eol == CrLf.CRLF || eol == CrLf.DOS) {
            return "\r\n";
        }
        return "\n";
    }
    private void initInternalFilters() {
        in = (ctrlz == AddAsisRemove.REMOVE) ? new RemoveEofFilter(in) : in;
        in = new NormalizeEolFilter(in, calculateEolString(eol), getFixlast());
        if (tabs != AddAsisRemove.ASIS) {
            if (getJavafiles()) {
                in = new MaskJavaTabLiteralsFilter(in);
            }
            in = (tabs == AddAsisRemove.ADD) ? (Reader) new AddTabFilter(in, getTablength())
                    : (Reader) new RemoveTabFilter(in, getTablength());
        }
        in = (ctrlz == AddAsisRemove.ADD) ? new AddEofFilter(in) : in;
        initialized = true;
    }
    public synchronized int read() throws IOException {
        if (!initialized) {
            initInternalFilters();
        }
        return in.read();
    }
    public void setEof(AddAsisRemove attr) {
        ctrlz = attr.resolve();
    }
    public void setEol(CrLf attr) {
        eol = attr.resolve();
    }
    public void setFixlast(boolean fixlast) {
        this.fixlast = fixlast;
    }
    public void setJavafiles(boolean javafiles) {
        this.javafiles = javafiles;
    }
    public void setTab(AddAsisRemove attr) {
        tabs = attr.resolve();
    }
    public void setTablength(int tabLength) throws IOException {
        if (tabLength < MIN_TAB_LENGTH
            || tabLength > MAX_TAB_LENGTH) {
            throw new IOException(
                "tablength must be between " + MIN_TAB_LENGTH
                + " and " + MAX_TAB_LENGTH);
        }
        this.tabLength = tabLength;
    }
    private static class SimpleFilterReader extends Reader {
        private static final int PREEMPT_BUFFER_LENGTH = 16;
        private Reader in;
        private int[] preempt = new int[PREEMPT_BUFFER_LENGTH];
        private int preemptIndex = 0;
        public SimpleFilterReader(Reader in) {
            this.in = in;
        }
        public void push(char c) {
            push((int) c);
        }
        public void push(int c) {
            try {
                preempt[preemptIndex++] = c;
            } catch (ArrayIndexOutOfBoundsException e) {
                int[] p2 = new int[preempt.length * 2];
                System.arraycopy(preempt, 0, p2, 0, preempt.length);
                preempt = p2;
                push(c);
            }
        }
        public void push(char[] cs, int start, int length) {
            for (int i = start + length - 1; i >= start;) {
                push(cs[i--]);
            }
        }
        public void push(char[] cs) {
            push(cs, 0, cs.length);
        }
        public void push(String s) {
            push(s.toCharArray());
        }
        public boolean editsBlocked() {
            return in instanceof SimpleFilterReader && ((SimpleFilterReader) in).editsBlocked();
        }
        public int read() throws java.io.IOException {
            return preemptIndex > 0 ? preempt[--preemptIndex] : in.read();
        }
        public void close() throws java.io.IOException {
            in.close();
        }
        public void reset() throws IOException {
            in.reset();
        }
        public boolean markSupported() {
            return in.markSupported();
        }
        public boolean ready() throws java.io.IOException {
            return in.ready();
        }
        public void mark(int i) throws java.io.IOException {
            in.mark(i);
        }
        public long skip(long i) throws java.io.IOException {
            return in.skip(i);
        }
        public int read(char[] buf) throws java.io.IOException {
            return read(buf, 0, buf.length);
        }
        public int read(char[] buf, int start, int length) throws java.io.IOException {
            int count = 0;
            int c = 0;
            while (length-- > 0 && (c = this.read()) != -1) {
                buf[start++] = (char) c;
                count++;
            }
            return (count == 0 && c == -1) ? -1 : count;
        }
    }
    private static class MaskJavaTabLiteralsFilter extends SimpleFilterReader {
        private boolean editsBlocked = false;
        private static final int JAVA = 1;
        private static final int IN_CHAR_CONST = 2;
        private static final int IN_STR_CONST = 3;
        private static final int IN_SINGLE_COMMENT = 4;
        private static final int IN_MULTI_COMMENT = 5;
        private static final int TRANS_TO_COMMENT = 6;
        private static final int TRANS_FROM_MULTI = 8;
        private int state;
        public MaskJavaTabLiteralsFilter(Reader in) {
            super(in);
            state = JAVA;
        }
        public boolean editsBlocked() {
            return editsBlocked || super.editsBlocked();
        }
        public int read() throws IOException {
            int thisChar = super.read();
            editsBlocked = (state == IN_CHAR_CONST || state == IN_STR_CONST);
            switch (state) {
            case JAVA:
                switch (thisChar) {
                case '\'':
                    state = IN_CHAR_CONST;
                    break;
                case '"':
                    state = IN_STR_CONST;
                    break;
                case '/':
                    state = TRANS_TO_COMMENT;
                    break;
                default:
                }
                break;
            case IN_CHAR_CONST:
                switch (thisChar) {
                case '\'':
                    state = JAVA;
                    break;
                default:
                }
                break;
            case IN_STR_CONST:
                switch (thisChar) {
                case '"':
                    state = JAVA;
                    break;
                default:
                }
                break;
            case IN_SINGLE_COMMENT:
                switch (thisChar) {
                case '\n':
                case '\r': 
                    state = JAVA;
                    break;
                default:
                }
                break;
            case IN_MULTI_COMMENT:
                switch (thisChar) {
                case '*':
                    state = TRANS_FROM_MULTI;
                    break;
                default:
                }
                break;
            case TRANS_TO_COMMENT:
                switch (thisChar) {
                case '*':
                    state = IN_MULTI_COMMENT;
                    break;
                case '/':
                    state = IN_SINGLE_COMMENT;
                    break;
                case '\'':
                    state = IN_CHAR_CONST;
                    break;
                case '"':
                    state = IN_STR_CONST;
                    break;
                default:
                    state = JAVA;
                }
                break;
            case TRANS_FROM_MULTI:
                switch (thisChar) {
                case '/':
                    state = JAVA;
                    break;
                default:
                }
                break;
            default:
            }
            return thisChar;
        }
    }
    private static class NormalizeEolFilter extends SimpleFilterReader {
        private boolean previousWasEOL;
        private boolean fixLast;
        private int normalizedEOL = 0;
        private char[] eol = null;
        public NormalizeEolFilter(Reader in, String eolString, boolean fixLast) {
            super(in);
            eol = eolString.toCharArray();
            this.fixLast = fixLast;
        }
        public int read() throws IOException {
            int thisChar = super.read();
            if (normalizedEOL == 0) {
                int numEOL = 0;
                boolean atEnd = false;
                switch (thisChar) {
                case CTRLZ:
                    int c = super.read();
                    if (c == -1) {
                        atEnd = true;
                        if (fixLast && !previousWasEOL) {
                            numEOL = 1;
                            push(thisChar);
                        }
                    } else {
                        push(c);
                    }
                    break;
                case -1:
                    atEnd = true;
                    if (fixLast && !previousWasEOL) {
                        numEOL = 1;
                    }
                    break;
                case '\n':
                    numEOL = 1;
                    break;
                case '\r':
                    numEOL = 1;
                    int c1 = super.read();
                    int c2 = super.read();
                    if (c1 == '\r' && c2 == '\n') {
                    } else if (c1 == '\r') {
                        numEOL = 2;
                        push(c2);
                    } else if (c1 == '\n') {
                        push(c2);
                    } else {
                        push(c2);
                        push(c1);
                    }
                default:
                }
                if (numEOL > 0) {
                    while (numEOL-- > 0) {
                        push(eol);
                        normalizedEOL += eol.length;
                    }
                    previousWasEOL = true;
                    thisChar = read();
                } else if (!atEnd) {
                    previousWasEOL = false;
                }
            } else {
                normalizedEOL--;
            }
            return thisChar;
        }
    }
    private static class AddEofFilter extends SimpleFilterReader {
        private int lastChar = -1;
        public AddEofFilter(Reader in) {
            super(in);
        }
        public int read() throws IOException {
            int thisChar = super.read();
            if (thisChar == -1) {
                if (lastChar != CTRLZ) {
                    lastChar = CTRLZ;
                    return lastChar;
                }
            } else {
                lastChar = thisChar;
            }
            return thisChar;
        }
    }
    private static class RemoveEofFilter extends SimpleFilterReader {
        private int lookAhead = -1;
        public RemoveEofFilter(Reader in) {
            super(in);
            try {
                lookAhead = in.read();
            } catch (IOException e) {
                lookAhead = -1;
            }
        }
        public int read() throws IOException {
            int lookAhead2 = super.read();
            if (lookAhead2 == -1 && lookAhead == CTRLZ) {
                return -1;
            }
            int i = lookAhead;
            lookAhead = lookAhead2;
            return i;
        }
    }
    private static class AddTabFilter extends SimpleFilterReader {
        private int columnNumber = 0;
        private int tabLength = 0;
        public AddTabFilter(Reader in, int tabLength) {
            super(in);
            this.tabLength = tabLength;
        }
        public int read() throws IOException {
            int c = super.read();
            switch (c) {
            case '\r':
            case '\n':
                columnNumber = 0;
                break;
            case ' ':
                columnNumber++;
                if (!editsBlocked()) {
                    int colNextTab = ((columnNumber + tabLength - 1) / tabLength) * tabLength;
                    int countSpaces = 1;
                    int numTabs = 0;
                    scanWhitespace: while ((c = super.read()) != -1) {
                        switch (c) {
                        case ' ':
                            if (++columnNumber == colNextTab) {
                                numTabs++;
                                countSpaces = 0;
                                colNextTab += tabLength;
                            } else {
                                countSpaces++;
                            }
                            break;
                        case '\t':
                            columnNumber = colNextTab;
                            numTabs++;
                            countSpaces = 0;
                            colNextTab += tabLength;
                            break;
                        default:
                            push(c);
                            break scanWhitespace;
                        }
                    }
                    while (countSpaces-- > 0) {
                        push(' ');
                        columnNumber--;
                    }
                    while (numTabs-- > 0) {
                        push('\t');
                        columnNumber -= tabLength;
                    }
                    c = super.read();
                    switch (c) {
                    case ' ':
                        columnNumber++;
                        break;
                    case '\t':
                        columnNumber += tabLength;
                        break;
                    default:
                    }
                }
                break;
            case '\t':
                columnNumber = ((columnNumber + tabLength - 1) / tabLength) * tabLength;
                break;
            default:
                columnNumber++;
            }
            return c;
        }
    }
    private static class RemoveTabFilter extends SimpleFilterReader {
        private int columnNumber = 0;
        private int tabLength = 0;
        public RemoveTabFilter(Reader in, int tabLength) {
            super(in);
            this.tabLength = tabLength;
        }
        public int read() throws IOException {
            int c = super.read();
            switch (c) {
            case '\r':
            case '\n':
                columnNumber = 0;
                break;
            case '\t':
                int width = tabLength - columnNumber % tabLength;
                if (!editsBlocked()) {
                    for (; width > 1; width--) {
                        push(' ');
                    }
                    c = ' ';
                }
                columnNumber += width;
                break;
            default:
                columnNumber++;
            }
            return c;
        }
    }
    public static class AddAsisRemove extends EnumeratedAttribute {
        private static final AddAsisRemove ASIS = newInstance("asis");
        private static final AddAsisRemove ADD = newInstance("add");
        private static final AddAsisRemove REMOVE = newInstance("remove");
        public String[] getValues() {
            return new String[] {"add", "asis", "remove"};
        }
        public boolean equals(Object other) {
            return other instanceof AddAsisRemove
                    && getIndex() == ((AddAsisRemove) other).getIndex();
        }
        public int hashCode() {
            return getIndex();
        }
        AddAsisRemove resolve() throws IllegalStateException {
            if (this.equals(ASIS)) {
                return ASIS;
            }
            if (this.equals(ADD)) {
                return ADD;
            }
            if (this.equals(REMOVE)) {
                return REMOVE;
            }
            throw new IllegalStateException("No replacement for " + this);
        }
        private AddAsisRemove newInstance() {
            return newInstance(getValue());
        }
        public static AddAsisRemove newInstance(String value) {
            AddAsisRemove a = new AddAsisRemove();
            a.setValue(value);
            return a;
        }
    }
    public static class CrLf extends EnumeratedAttribute {
        private static final CrLf ASIS = newInstance("asis");
        private static final CrLf CR = newInstance("cr");
        private static final CrLf CRLF = newInstance("crlf");
        private static final CrLf DOS = newInstance("dos");
        private static final CrLf LF = newInstance("lf");
        private static final CrLf MAC = newInstance("mac");
        private static final CrLf UNIX = newInstance("unix");
        public String[] getValues() {
            return new String[] {"asis", "cr", "lf", "crlf", "mac", "unix", "dos"};
        }
        public boolean equals(Object other) {
            return other instanceof CrLf && getIndex() == ((CrLf) other).getIndex();
        }
        public int hashCode() {
            return getIndex();
        }
        CrLf resolve() {
            if (this.equals(ASIS)) {
                return ASIS;
            }
            if (this.equals(CR) || this.equals(MAC)) {
                return CR;
            }
            if (this.equals(CRLF) || this.equals(DOS)) {
                return CRLF;
            }
            if (this.equals(LF) || this.equals(UNIX)) {
                return LF;
            }
            throw new IllegalStateException("No replacement for " + this);
        }
        private CrLf newInstance() {
            return newInstance(getValue());
        }
        public static CrLf newInstance(String value) {
            CrLf c = new CrLf();
            c.setValue(value);
            return c;
        }
    }
}
