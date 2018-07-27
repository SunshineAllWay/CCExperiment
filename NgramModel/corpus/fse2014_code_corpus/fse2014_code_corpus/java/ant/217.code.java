package org.apache.tools.ant.taskdefs;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
public abstract class JDBCTask extends Task {
    private static final int HASH_TABLE_SIZE = 3;
    private static Hashtable loaderMap = new Hashtable(HASH_TABLE_SIZE);
    private boolean caching = true;
    private Path classpath;
    private AntClassLoader loader;
    private boolean autocommit = false;
    private String driver = null;
    private String url = null;
    private String userId = null;
    private String password = null;
    private String rdbms = null;
    private String version = null;
    private boolean failOnConnectionError = true;
    private List connectionProperties = new ArrayList();
    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }
    public void setCaching(boolean enable) {
        caching = enable;
    }
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void setDriver(String driver) {
        this.driver = driver.trim();
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }
    public void setRdbms(String rdbms) {
        this.rdbms = rdbms;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setFailOnConnectionError(boolean b) {
        failOnConnectionError = b;
    }
    protected boolean isValidRdbms(Connection conn) {
        if (rdbms == null && version == null) {
            return true;
        }
        try {
            DatabaseMetaData dmd = conn.getMetaData();
            if (rdbms != null) {
                String theVendor = dmd.getDatabaseProductName().toLowerCase();
                log("RDBMS = " + theVendor, Project.MSG_VERBOSE);
                if (theVendor == null || theVendor.indexOf(rdbms) < 0) {
                    log("Not the required RDBMS: " + rdbms, Project.MSG_VERBOSE);
                    return false;
                }
            }
            if (version != null) {
                String theVersion = dmd.getDatabaseProductVersion().toLowerCase(Locale.ENGLISH);
                log("Version = " + theVersion, Project.MSG_VERBOSE);
                if (theVersion == null
                        || !(theVersion.startsWith(version)
                        || theVersion.indexOf(" " + version) >= 0)) {
                    log("Not the required version: \"" + version + "\"", Project.MSG_VERBOSE);
                    return false;
                }
            }
        } catch (SQLException e) {
            log("Failed to obtain required RDBMS information", Project.MSG_ERR);
            return false;
        }
        return true;
    }
    protected static Hashtable getLoaderMap() {
        return loaderMap;
    }
    protected AntClassLoader getLoader() {
        return loader;
    }
    public void addConnectionProperty(Property var) {
        connectionProperties.add(var);
    }
    protected Connection getConnection() throws BuildException {
        if (userId == null) {
            throw new BuildException("UserId attribute must be set!", getLocation());
        }
        if (password == null) {
            throw new BuildException("Password attribute must be set!", getLocation());
        }
        if (url == null) {
            throw new BuildException("Url attribute must be set!", getLocation());
        }
        try {
            log("connecting to " + getUrl(), Project.MSG_VERBOSE);
            Properties info = new Properties();
            info.put("user", getUserId());
            info.put("password", getPassword());
            for (Iterator props = connectionProperties.iterator();
                 props.hasNext(); ) {
                Property p = (Property) props.next();
                String name = p.getName();
                String value = p.getValue();
                if (name == null || value == null) {
                    log("Only name/value pairs are supported as connection"
                        + " properties.", Project.MSG_WARN);
                } else {
                    log("Setting connection property " + name + " to " + value,
                        Project.MSG_VERBOSE);
                    info.put(name, value);
                }
            }
            Connection conn = getDriver().connect(getUrl(), info);
            if (conn == null) {
                throw new SQLException("No suitable Driver for " + url);
            }
            conn.setAutoCommit(autocommit);
            return conn;
        } catch (SQLException e) {
            if (!failOnConnectionError) {
                log("Failed to connect: " + e.getMessage(), Project.MSG_WARN);
                return null;
            } else {
                throw new BuildException(e, getLocation());
            }
        }
    }
    private Driver getDriver() throws BuildException {
        if (driver == null) {
            throw new BuildException("Driver attribute must be set!", getLocation());
        }
        Driver driverInstance = null;
        try {
            Class dc;
            if (classpath != null) {
                synchronized (loaderMap) {
                    if (caching) {
                        loader = (AntClassLoader) loaderMap.get(driver);
                    }
                    if (loader == null) {
                        log("Loading " + driver
                            + " using AntClassLoader with classpath "
                            + classpath, Project.MSG_VERBOSE);
                        loader = getProject().createClassLoader(classpath);
                        if (caching) {
                            loaderMap.put(driver, loader);
                        }
                    } else {
                        log("Loading " + driver
                            + " using a cached AntClassLoader.",
                                Project.MSG_VERBOSE);
                    }
                }
                dc = loader.loadClass(driver);
            } else {
                log("Loading " + driver + " using system loader.",
                    Project.MSG_VERBOSE);
                dc = Class.forName(driver);
            }
            driverInstance = (Driver) dc.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BuildException(
                    "Class Not Found: JDBC driver " + driver + " could not be loaded",
                    e,
                    getLocation());
        } catch (IllegalAccessException e) {
            throw new BuildException(
                    "Illegal Access: JDBC driver " + driver + " could not be loaded",
                    e,
                    getLocation());
        } catch (InstantiationException e) {
            throw new BuildException(
                    "Instantiation Exception: JDBC driver " + driver + " could not be loaded",
                    e,
                    getLocation());
        }
        return driverInstance;
    }
    public void isCaching(boolean value) {
        caching = value;
    }
    public Path getClasspath() {
        return classpath;
    }
    public boolean isAutocommit() {
        return autocommit;
    }
    public String getUrl() {
        return url;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserid(String userId) {
        this.userId = userId;
    }
    public String getPassword() {
        return password;
    }
    public String getRdbms() {
        return rdbms;
    }
    public String getVersion() {
        return version;
    }
}
