package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.KeepAliveOutputStream;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Appendable;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Union;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
public class SQLExec extends JDBCTask {
    public static class DelimiterType extends EnumeratedAttribute {
        public static final String NORMAL = "normal", ROW = "row";
        public String[] getValues() {
            return new String[] {NORMAL, ROW};
        }
    }
    private int goodSql = 0;
    private int totalSql = 0;
    private Connection conn = null;
    private Union resources;
    private Statement statement = null;
    private File srcFile = null;
    private String sqlCommand = "";
    private Vector transactions = new Vector();
    private String delimiter = ";";
    private String delimiterType = DelimiterType.NORMAL;
    private boolean print = false;
    private boolean showheaders = true;
    private boolean showtrailers = true;
    private Resource output = null;
    private String onError = "abort";
    private String encoding = null;
    private boolean append = false;
    private boolean keepformat = false;
    private boolean escapeProcessing = true;
    private boolean expandProperties = true;
    private boolean rawBlobs;
    private boolean strictDelimiterMatching = true;
    private boolean showWarnings = false;
    private String csvColumnSep = ",";
    private String csvQuoteChar = null;
    private boolean treatWarningsAsErrors = false;
    private String errorProperty = null;
    private String warningProperty = null;
    private String rowCountProperty = null;
    public void setSrc(File srcFile) {
        this.srcFile = srcFile;
    }
    public void setExpandProperties(boolean expandProperties) {
        this.expandProperties = expandProperties;
    }
    public boolean getExpandProperties() {
        return expandProperties;
    }
    public void addText(String sql) {
        this.sqlCommand += sql;
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void add(ResourceCollection rc) {
        if (rc == null) {
            throw new BuildException("Cannot add null ResourceCollection");
        }
        synchronized (this) {
            if (resources == null) {
                resources = new Union();
            }
        }
        resources.add(rc);
    }
    public Transaction createTransaction() {
        Transaction t = new Transaction();
        transactions.addElement(t);
        return t;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    public void setDelimiterType(DelimiterType delimiterType) {
        this.delimiterType = delimiterType.getValue();
    }
    public void setPrint(boolean print) {
        this.print = print;
    }
    public void setShowheaders(boolean showheaders) {
        this.showheaders = showheaders;
    }
    public void setShowtrailers(boolean showtrailers) {
        this.showtrailers = showtrailers;
    }
    public void setOutput(File output) {
        setOutput(new FileResource(getProject(), output));
    }
    public void setOutput(Resource output) {
        this.output = output;
    }
    public void setAppend(boolean append) {
        this.append = append;
    }
    public void setOnerror(OnError action) {
        this.onError = action.getValue();
    }
    public void setKeepformat(boolean keepformat) {
        this.keepformat = keepformat;
    }
    public void setEscapeProcessing(boolean enable) {
        escapeProcessing = enable;
    }
    public void setRawBlobs(boolean rawBlobs) {
        this.rawBlobs = rawBlobs;
    }
    public void setStrictDelimiterMatching(boolean b) {
        strictDelimiterMatching = b;
    }
    public void setShowWarnings(boolean b) {
        showWarnings = b;
    }
    public void setTreatWarningsAsErrors(boolean b) {
        treatWarningsAsErrors =  b;
    }
    public void setCsvColumnSeparator(String s) {
        csvColumnSep = s;
    }
    public void setCsvQuoteCharacter(String s) {
        if (s != null && s.length() > 1) {
            throw new BuildException("The quote character must be a single"
                                     + " character.");
        }
        csvQuoteChar = s;
    }
    public void setErrorProperty(String errorProperty) {
        this.errorProperty = errorProperty;
    }
    public void setWarningProperty(String warningProperty) {
        this.warningProperty = warningProperty;
    }
    public void setRowCountProperty(String rowCountProperty) {
        this.rowCountProperty = rowCountProperty;
    }
    public void execute() throws BuildException {
        Vector savedTransaction = (Vector) transactions.clone();
        String savedSqlCommand = sqlCommand;
        sqlCommand = sqlCommand.trim();
        try {
            if (srcFile == null && sqlCommand.length() == 0 && resources == null) {
                if (transactions.size() == 0) {
                    throw new BuildException("Source file or resource collection, "
                                             + "transactions or sql statement "
                                             + "must be set!", getLocation());
                }
            }
            if (srcFile != null && !srcFile.isFile()) {
                throw new BuildException("Source file " + srcFile
                        + " is not a file!", getLocation());
            }
            if (resources != null) {
                Iterator iter = resources.iterator();
                while (iter.hasNext()) {
                    Resource r = (Resource) iter.next();
                    Transaction t = createTransaction();
                    t.setSrcResource(r);
                }
            }
            Transaction t = createTransaction();
            t.setSrc(srcFile);
            t.addText(sqlCommand);
            if (getConnection() == null) {
                return;
            }
            try {
                PrintStream out = KeepAliveOutputStream.wrapSystemOut();
                try {
                    if (output != null) {
                        log("Opening PrintStream to output Resource " + output, Project.MSG_VERBOSE);
                        OutputStream os = null;
                        FileProvider fp =
                            (FileProvider) output.as(FileProvider.class);
                        if (fp != null) {
                            os = new FileOutputStream(fp.getFile(), append);
                        } else {
                            if (append) {
                                Appendable a =
                                    (Appendable) output.as(Appendable.class);
                                if (a != null) {
                                    os = a.getAppendOutputStream();
                                }
                            }
                            if (os == null) {
                                os = output.getOutputStream();
                                if (append) {
                                    log("Ignoring append=true for non-appendable"
                                        + " resource " + output,
                                        Project.MSG_WARN);
                                }
                            }
                        }
                        out = new PrintStream(new BufferedOutputStream(os));
                    }
                    for (Enumeration e = transactions.elements();
                         e.hasMoreElements();) {
                        ((Transaction) e.nextElement()).runTransaction(out);
                        if (!isAutocommit()) {
                            log("Committing transaction", Project.MSG_VERBOSE);
                            getConnection().commit();
                        }
                    }
                } finally {
                    FileUtils.close(out);
                }
            } catch (IOException e) {
                closeQuietly();
                setErrorProperty();
                if (onError.equals("abort")) {
                    throw new BuildException(e, getLocation());
                }
            } catch (SQLException e) {
                closeQuietly();
                setErrorProperty();
                if (onError.equals("abort")) {
                    throw new BuildException(e, getLocation());
                }
            } finally {
                try {
                    if (getStatement() != null) {
                        getStatement().close();
                    }
                } catch (SQLException ex) {
                }
                try {
                    if (getConnection() != null) {
                        getConnection().close();
                    }
                } catch (SQLException ex) {
                }
            }
            log(goodSql + " of " + totalSql + " SQL statements executed successfully");
        } finally {
            transactions = savedTransaction;
            sqlCommand = savedSqlCommand;
        }
    }
    protected void runStatements(Reader reader, PrintStream out)
        throws SQLException, IOException {
        StringBuffer sql = new StringBuffer();
        String line;
        BufferedReader in = new BufferedReader(reader);
        while ((line = in.readLine()) != null) {
            if (!keepformat) {
                line = line.trim();
            }
            if (expandProperties) {
                line = getProject().replaceProperties(line);
            }
            if (!keepformat) {
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }
            }
            sql.append(keepformat ? "\n" : " ").append(line);
            if (!keepformat && line.indexOf("--") >= 0) {
                sql.append("\n");
            }
            int lastDelimPos = lastDelimiterPosition(sql, line);
            if (lastDelimPos > -1) {
                execSQL(sql.substring(0, lastDelimPos), out);
                sql.replace(0, sql.length(), "");
            }
        }
        if (sql.length() > 0) {
            execSQL(sql.toString(), out);
        }
    }
    protected void execSQL(String sql, PrintStream out) throws SQLException {
        if ("".equals(sql.trim())) {
            return;
        }
        ResultSet resultSet = null;
        try {
            totalSql++;
            log("SQL: " + sql, Project.MSG_VERBOSE);
            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            ret = getStatement().execute(sql);
            updateCount = getStatement().getUpdateCount();
            do {
                if (updateCount != -1) {
                    updateCountTotal += updateCount;
                }
                if (ret) {
                    resultSet = getStatement().getResultSet();
                    printWarnings(resultSet.getWarnings(), false);
                    resultSet.clearWarnings();
                    if (print) {
                        printResults(resultSet, out);
                    }
                }
                ret = getStatement().getMoreResults();
                updateCount = getStatement().getUpdateCount();
            } while (ret || updateCount != -1);
            printWarnings(getStatement().getWarnings(), false);
            getStatement().clearWarnings();
            log(updateCountTotal + " rows affected", Project.MSG_VERBOSE);
            if (updateCountTotal != -1) {
                setRowCountProperty(updateCountTotal);
            }
            if (print && showtrailers) {
                out.println(updateCountTotal + " rows affected");
            }
            SQLWarning warning = getConnection().getWarnings();
            printWarnings(warning, true);
            getConnection().clearWarnings();
            goodSql++;
        } catch (SQLException e) {
            log("Failed to execute: " + sql, Project.MSG_ERR);
            setErrorProperty();
            if (!onError.equals("abort")) {
                log(e.toString(), Project.MSG_ERR);
            }
            if (!onError.equals("continue")) {
                throw e;
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    protected void printResults(PrintStream out) throws SQLException {
        ResultSet rs = getStatement().getResultSet();
        try {
            printResults(rs, out);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
    protected void printResults(ResultSet rs, PrintStream out) throws SQLException {
        if (rs != null) {
            log("Processing new result set.", Project.MSG_VERBOSE);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (columnCount > 0) {
                if (showheaders) {
                    out.print(md.getColumnName(1));
                    for (int col = 2; col <= columnCount; col++) {
                         out.print(csvColumnSep);
                         out.print(maybeQuote(md.getColumnName(col)));
                    }
                    out.println();
                }
                while (rs.next()) {
                    printValue(rs, 1, out);
                    for (int col = 2; col <= columnCount; col++) {
                        out.print(csvColumnSep);
                        printValue(rs, col, out);
                    }
                    out.println();
                    printWarnings(rs.getWarnings(), false);
                }
            }
        }
        out.println();
    }
    private void printValue(ResultSet rs, int col, PrintStream out)
            throws SQLException {
        if (rawBlobs && rs.getMetaData().getColumnType(col) == Types.BLOB) {
            Blob blob = rs.getBlob(col);
            if (blob != null) {
                new StreamPumper(rs.getBlob(col).getBinaryStream(), out).run();
            }
        } else {
            out.print(maybeQuote(rs.getString(col)));
        }
    }
    private String maybeQuote(String s) {
        if (csvQuoteChar == null || s == null
            || (s.indexOf(csvColumnSep) == -1 && s.indexOf(csvQuoteChar) == -1)
            ) {
            return s;
        }
        StringBuffer sb = new StringBuffer(csvQuoteChar);
        int len = s.length();
        char q = csvQuoteChar.charAt(0);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == q) {
                sb.append(q);
            }
            sb.append(c);
        }
        return sb.append(csvQuoteChar).toString();
    }
    private void closeQuietly() {
        if (!isAutocommit() && getConnection() != null && onError.equals("abort")) {
            try {
                getConnection().rollback();
            } catch (SQLException ex) {
            }
        }
    }
    protected Connection getConnection() {
        if (conn == null) {
            conn = super.getConnection();
            if (!isValidRdbms(conn)) {
                conn = null;
            }
        }
        return conn;
    }
    protected Statement getStatement() throws SQLException {
        if (statement == null) {
            statement = getConnection().createStatement();
            statement.setEscapeProcessing(escapeProcessing);
        }
        return statement;
    }
    public static class OnError extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"continue", "stop", "abort"};
        }
    }
    public class Transaction {
        private Resource tSrcResource = null;
        private String tSqlCommand = "";
        public void setSrc(File src) {
            if (src != null) {
                setSrcResource(new FileResource(src));
            }
        }
        public void setSrcResource(Resource src) {
            if (tSrcResource != null) {
                throw new BuildException("only one resource per transaction");
            }
            tSrcResource = src;
        }
        public void addText(String sql) {
            if (sql != null) {
                this.tSqlCommand += sql;
            }
        }
        public void addConfigured(ResourceCollection a) {
            if (a.size() != 1) {
                throw new BuildException("only single argument resource "
                                         + "collections are supported.");
            }
            setSrcResource((Resource) a.iterator().next());
        }
        private void runTransaction(PrintStream out)
            throws IOException, SQLException {
            if (tSqlCommand.length() != 0) {
                log("Executing commands", Project.MSG_INFO);
                runStatements(new StringReader(tSqlCommand), out);
            }
            if (tSrcResource != null) {
                log("Executing resource: " + tSrcResource.toString(),
                    Project.MSG_INFO);
                InputStream is = null;
                Reader reader = null;
                try {
                    is = tSrcResource.getInputStream();
                    reader = (encoding == null) ? new InputStreamReader(is)
                        : new InputStreamReader(is, encoding);
                    runStatements(reader, out);
                } finally {
                    FileUtils.close(is);
                    FileUtils.close(reader);
                }
            }
        }
    }
    public int lastDelimiterPosition(StringBuffer buf, String currentLine) {
        if (strictDelimiterMatching) {
            if ((delimiterType.equals(DelimiterType.NORMAL)
                 && StringUtils.endsWith(buf, delimiter)) ||
                (delimiterType.equals(DelimiterType.ROW)
                 && currentLine.equals(delimiter))) {
                return buf.length() - delimiter.length();
            }
            return -1;
        } else {
            String d = delimiter.trim().toLowerCase(Locale.ENGLISH);
            if (delimiterType.equals(DelimiterType.NORMAL)) {
                int endIndex = delimiter.length() - 1;
                int bufferIndex = buf.length() - 1;
                while (bufferIndex >= 0
                       && Character.isWhitespace(buf.charAt(bufferIndex))) {
                    --bufferIndex;
                }
                if (bufferIndex < endIndex) {
                    return -1;
                }
                while (endIndex >= 0) {
                    if (buf.substring(bufferIndex, bufferIndex + 1)
                        .toLowerCase(Locale.ENGLISH).charAt(0)
                        != d.charAt(endIndex)) {
                        return -1;
                    }
                    bufferIndex--;
                    endIndex--;
                }
                return bufferIndex + 1;
            } else {
                return currentLine.trim().toLowerCase(Locale.ENGLISH).equals(d)
                    ? buf.length() - currentLine.length() : -1;
            }
        }
    }
    private void printWarnings(SQLWarning warning, boolean force)
        throws SQLException {
        SQLWarning initialWarning = warning;
        if (showWarnings || force) {
            while (warning != null) {
                log(warning + " sql warning",
                    showWarnings ? Project.MSG_WARN : Project.MSG_VERBOSE);
                warning = warning.getNextWarning();
            }
        }
        if (initialWarning != null) {
            setWarningProperty();
        }
        if (treatWarningsAsErrors && initialWarning != null) {
            throw initialWarning;
        }
    }
    protected final void setErrorProperty() {
        setProperty(errorProperty, "true");
    }
    protected final void setWarningProperty() {
        setProperty(warningProperty, "true");
    }
    protected final void setRowCountProperty(int rowCount) {
        setProperty(rowCountProperty, Integer.toString(rowCount));
    }
    private void setProperty(String name, String value) {
        if (name != null) {
            getProject().setNewProperty(name, value);
        }
    }
}
