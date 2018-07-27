package org.apache.tools.ant.taskdefs;
import java.sql.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
public class SQLExecTest extends TestCase {
    public final static int NULL = 0;
    public final static int ORACLE = 1;
    public final static int MYSQL = 2;
    public final static String DRIVER = "driver";
    public final static String USER = "user";
    public final static String PASSWORD = "password";
    public final static String URL = "url";
    public final static String PATH = "path";
    public final static String SQL = "sql";
    public SQLExecTest(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        JDBCTask.getLoaderMap().clear();
    }
    public void testDriverCaching(){
        SQLExec sql = createTask(getProperties(NULL));
        assertTrue(!SQLExec.getLoaderMap().containsKey(NULL_DRIVER));
        try {
            sql.execute();
        } catch (BuildException e){
            assertTrue(e.getCause().getMessage().indexOf("No suitable Driver") != -1);
        }
        assertTrue(SQLExec.getLoaderMap().containsKey(NULL_DRIVER));
        assertSame(sql.getLoader(), JDBCTask.getLoaderMap().get(NULL_DRIVER));
        ClassLoader loader1 = sql.getLoader();
        sql = createTask(getProperties(NULL));
        assertTrue(JDBCTask.getLoaderMap().containsKey(NULL_DRIVER));
        try {
            sql.execute();
        } catch (BuildException e){
            assertTrue(e.getCause().getMessage().indexOf("No suitable Driver") != -1);
        }
        assertTrue(JDBCTask.getLoaderMap().containsKey(NULL_DRIVER));
        assertSame(sql.getLoader(), JDBCTask.getLoaderMap().get(NULL_DRIVER));
        assertSame(loader1, sql.getLoader());
    }
    public void testNull() throws Exception {
        doMultipleCalls(1000, NULL, true, true);
    }
    protected void doMultipleCalls(int calls, int database, boolean caching, boolean catchexception){
        Properties props = getProperties(database);
        for (int i = 0; i < calls; i++){
            SQLExec sql = createTask(props);
            sql.setCaching(caching);
            try  {
                sql.execute();
            } catch (BuildException e){
                if (!catchexception){
                    throw e;
                }
            }
        }
    }
    protected SQLExec createTask(Properties props){
        SQLExec sql = new SQLExec();
        sql.setProject( new Project() );
        sql.setDriver( props.getProperty(DRIVER) );
        sql.setUserid( props.getProperty(USER) );
        sql.setPassword( props.getProperty(PASSWORD) );
        sql.setUrl( props.getProperty(URL) );
        sql.createClasspath().setLocation( new File(props.getProperty(PATH)) );
        sql.addText( props.getProperty(SQL) );
        return sql;
    }
    protected String findResourcePath(String resource){
        resource = resource.replace('.', '/') + ".class";
        URL url = getClass().getClassLoader().getResource(resource);
        if (url == null) {
            return null;
        }
        String u = url.toString();
        if (u.startsWith("jar:file:")) {
            int pling = u.indexOf("!");
            return u.substring("jar:file:".length(), pling);
        } else if (u.startsWith("file:")) {
            int tail = u.indexOf(resource);
            return u.substring("file:".length(), tail);
        }
        return null;
    }
    protected Properties getProperties(int database){
        Properties props = null;
        switch (database){
            case ORACLE:
                props = getProperties("oracle.jdbc.driver.OracleDriver", "test", "test", "jdbc:oracle:thin:@127.0.0.1:1521:orcl");
                break;
            case MYSQL:
                props = getProperties("org.gjt.mm.mysql.Driver", "test", "test", "jdbc:mysql://127.0.0.1:3306/test");
                break;
            case NULL:
            default:
                props = getProperties(NULL_DRIVER, "test", "test", "jdbc:database://hostname:port/name");
        }
        String path = findResourcePath(props.getProperty(DRIVER));
        props.put(PATH, path);
        props.put(SQL, "create table OOME_TEST(X INTEGER NOT NULL);\ndrop table if exists OOME_TEST;");
        return props;
    }
    protected Properties getProperties(String driver, String user, String pwd, String url){
        Properties props = new Properties();
        props.put(DRIVER, driver);
        props.put(USER, user);
        props.put(PASSWORD, pwd);
        props.put(URL, url);
        return props;
    }
    public final static String NULL_DRIVER = NullDriver.class.getName();
    public static class NullDriver implements Driver {
        public Connection connect(String url, Properties info)
                throws SQLException {
            return null;
        }
        public boolean acceptsURL(String url) throws SQLException {
            return false;
        }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
                throws SQLException {
            return new DriverPropertyInfo[0];
        }
        public int getMajorVersion() {
            return 0;
        }
        public int getMinorVersion() {
            return 0;
        }
        public boolean jdbcCompliant() {
            return false;
        }
        public Logger getParentLogger()  {
            return Logger.getAnonymousLogger();
        }
    }
    public void testLastDelimiterPositionNormalModeStrict() {
        SQLExec s = new SQLExec();
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer(), null));
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer("GO"), null));
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer("; "), null));
        assertEquals(2,
                     s.lastDelimiterPosition(new StringBuffer("ab;"), null));
        s.setDelimiter("GO");
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer("GO "), null));
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer("go"), null));
        assertEquals(0,
                     s.lastDelimiterPosition(new StringBuffer("GO"), null));
    }
    public void testLastDelimiterPositionNormalModeNonStrict() {
        SQLExec s = new SQLExec();
        s.setStrictDelimiterMatching(false);
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer(), null));
        assertEquals(-1,
                     s.lastDelimiterPosition(new StringBuffer("GO"), null));
        assertEquals(0,
                     s.lastDelimiterPosition(new StringBuffer("; "), null));
        assertEquals(2,
                     s.lastDelimiterPosition(new StringBuffer("ab;"), null));
        s.setDelimiter("GO");
        assertEquals(0,
                     s.lastDelimiterPosition(new StringBuffer("GO "), null));
        assertEquals(0,
                     s.lastDelimiterPosition(new StringBuffer("go"), null));
        assertEquals(0,
                     s.lastDelimiterPosition(new StringBuffer("GO"), null));
    }
    public void testLastDelimiterPositionRowModeStrict() {
        SQLExec s = new SQLExec();
        SQLExec.DelimiterType t = new SQLExec.DelimiterType();
        t.setValue("row");
        s.setDelimiterType(t);
        assertEquals(-1, s.lastDelimiterPosition(null, ""));
        assertEquals(-1, s.lastDelimiterPosition(null, "GO"));
        assertEquals(-1, s.lastDelimiterPosition(null, "; "));
        assertEquals(1, s.lastDelimiterPosition(new StringBuffer("ab"), ";"));
        s.setDelimiter("GO");
        assertEquals(-1, s.lastDelimiterPosition(null, "GO "));
        assertEquals(-1, s.lastDelimiterPosition(null, "go"));
        assertEquals(0, s.lastDelimiterPosition(new StringBuffer("ab"), "GO"));
    }
    public void testLastDelimiterPositionRowModeNonStrict() {
        SQLExec s = new SQLExec();
        SQLExec.DelimiterType t = new SQLExec.DelimiterType();
        t.setValue("row");
        s.setDelimiterType(t);
        s.setStrictDelimiterMatching(false);
        assertEquals(-1, s.lastDelimiterPosition(null, ""));
        assertEquals(-1, s.lastDelimiterPosition(null, "GO"));
        assertEquals(0, s.lastDelimiterPosition(new StringBuffer("; "), "; "));
        assertEquals(1, s.lastDelimiterPosition(new StringBuffer("ab"), ";"));
        s.setDelimiter("GO");
        assertEquals(1,
                     s.lastDelimiterPosition(new StringBuffer("abcd"), "GO "));
        assertEquals(0, s.lastDelimiterPosition(new StringBuffer("go"), "go"));
        assertEquals(0, s.lastDelimiterPosition(new StringBuffer("ab"), "GO"));
    }
}
