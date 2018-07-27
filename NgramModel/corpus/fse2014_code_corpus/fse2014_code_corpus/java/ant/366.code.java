package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DTDLocation;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.apache.tools.ant.util.XmlConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;
public class XMLValidateTask extends Task {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    protected static final String INIT_FAILED_MSG =
        "Could not start xml validation: ";
    protected boolean failOnError = true;
    protected boolean warn = true;
    protected boolean lenient = false;
    protected String readerClassName = null;
    protected File file = null;
    protected Vector filesets = new Vector();
    protected Path classpath;
    protected XMLReader xmlReader = null;
    protected ValidatorErrorHandler errorHandler = new ValidatorErrorHandler();
    private Vector attributeList = new Vector();
    private final Vector propertyList = new Vector();
    private XMLCatalog xmlCatalog = new XMLCatalog();
    public static final String MESSAGE_FILES_VALIDATED
        = " file(s) have been successfully validated.";
    private AntClassLoader readerLoader = null;
    public void setFailOnError(boolean fail) {
        failOnError = fail;
    }
    public void setWarn(boolean bool) {
        warn = bool;
    }
    public void setLenient(boolean bool) {
        lenient = bool;
    }
    public void setClassName(String className) {
        readerClassName = className;
    }
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
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
    public void setFile(File file) {
        this.file = file;
    }
    public void addConfiguredXMLCatalog(XMLCatalog catalog) {
        xmlCatalog.addConfiguredXMLCatalog(catalog);
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public Attribute createAttribute() {
        final Attribute feature = new Attribute();
        attributeList.addElement(feature);
        return feature;
    }
    public Property createProperty() {
        final Property prop = new Property();
        propertyList.addElement(prop);
        return prop;
    }
    public void init() throws BuildException {
        super.init();
        xmlCatalog.setProject(getProject());
    }
    public DTDLocation createDTD() {
        DTDLocation dtdLocation = new DTDLocation();
        xmlCatalog.addDTD(dtdLocation);
        return dtdLocation;
    }
    protected EntityResolver getEntityResolver() {
        return xmlCatalog;
    }
    protected XMLReader getXmlReader() {
        return xmlReader;
    }
    public void execute() throws BuildException {
        try {
        int fileProcessed = 0;
        if (file == null && (filesets.size() == 0)) {
            throw new BuildException(
                "Specify at least one source - " + "a file or a fileset.");
        }
        if (file != null) {
            if (file.exists() && file.canRead() && file.isFile()) {
                doValidate(file);
                fileProcessed++;
            } else {
                String errorMsg = "File " + file + " cannot be read";
                if (failOnError) {
                    throw new BuildException(errorMsg);
                } else {
                    log(errorMsg, Project.MSG_ERR);
                }
            }
        }
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();
            for (int j = 0; j < files.length; j++) {
                File srcFile = new File(fs.getDir(getProject()), files[j]);
                doValidate(srcFile);
                fileProcessed++;
            }
        }
        onSuccessfulValidation(fileProcessed);
        } finally {
            cleanup();
        }
    }
    protected void onSuccessfulValidation(int fileProcessed) {
        log(fileProcessed + MESSAGE_FILES_VALIDATED);
    }
    protected void initValidator() {
        xmlReader = createXmlReader();
        xmlReader.setEntityResolver(getEntityResolver());
        xmlReader.setErrorHandler(errorHandler);
        if (!isSax1Parser()) {
            if (!lenient) {
                setFeature(XmlConstants.FEATURE_VALIDATION, true);
            }
            for (int i = 0; i < attributeList.size(); i++) {
                Attribute feature = (Attribute) attributeList.elementAt(i);
                setFeature(feature.getName(), feature.getValue());
            }
            for (int i = 0; i < propertyList.size(); i++) {
                final Property prop = (Property) propertyList.elementAt(i);
                setProperty(prop.getName(), prop.getValue());
            }
        }
    }
    protected boolean isSax1Parser() {
        return (xmlReader instanceof ParserAdapter);
    }
    protected XMLReader createXmlReader() {
        Object reader = null;
        if (readerClassName == null) {
            reader = createDefaultReaderOrParser();
        } else {
            Class readerClass = null;
            try {
                if (classpath != null) {
                    readerLoader = getProject().createClassLoader(classpath);
                    readerClass = Class.forName(readerClassName, true,
                                                readerLoader);
                } else {
                    readerClass = Class.forName(readerClassName);
                }
                reader = readerClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new BuildException(INIT_FAILED_MSG + readerClassName, e);
            } catch (InstantiationException e) {
                throw new BuildException(INIT_FAILED_MSG + readerClassName, e);
            } catch (IllegalAccessException e) {
                throw new BuildException(INIT_FAILED_MSG + readerClassName, e);
            }
        }
        XMLReader newReader;
        if (reader instanceof XMLReader) {
            newReader = (XMLReader) reader;
            log(
                "Using SAX2 reader " + reader.getClass().getName(),
                Project.MSG_VERBOSE);
        } else {
            if (reader instanceof Parser) {
                newReader = new ParserAdapter((Parser) reader);
                log(
                    "Using SAX1 parser " + reader.getClass().getName(),
                    Project.MSG_VERBOSE);
            } else {
                throw new BuildException(
                    INIT_FAILED_MSG
                        + reader.getClass().getName()
                        + " implements nor SAX1 Parser nor SAX2 XMLReader.");
            }
        }
        return newReader;
    }
    protected void cleanup() {
        if (readerLoader != null) {
            readerLoader.cleanup();
            readerLoader = null;
        }
    }
    private Object createDefaultReaderOrParser() {
        Object reader;
        try {
            reader = createDefaultReader();
        } catch (BuildException exc) {
            reader = JAXPUtils.getParser();
        }
        return reader;
    }
    protected XMLReader createDefaultReader() {
        return JAXPUtils.getXMLReader();
    }
    protected void setFeature(String feature, boolean value)
        throws BuildException {
        log("Setting feature " + feature + "=" + value, Project.MSG_DEBUG);
        try {
            xmlReader.setFeature(feature, value);
        } catch (SAXNotRecognizedException e) {
            throw new BuildException(
                "Parser "
                    + xmlReader.getClass().getName()
                    + " doesn't recognize feature "
                    + feature,
                e,
                getLocation());
        } catch (SAXNotSupportedException e) {
            throw new BuildException(
                "Parser "
                    + xmlReader.getClass().getName()
                    + " doesn't support feature "
                    + feature,
                e,
                getLocation());
        }
    }
    protected void setProperty(String name, String value) throws BuildException {
        if (name == null || value == null) {
            throw new BuildException("Property name and value must be specified.");
        }
        try {
            xmlReader.setProperty(name, value);
        } catch (SAXNotRecognizedException e) {
            throw new BuildException(
                "Parser "
                    + xmlReader.getClass().getName()
                    + " doesn't recognize property "
                    + name,
                e,
                getLocation());
        } catch (SAXNotSupportedException e) {
            throw new BuildException(
                "Parser "
                    + xmlReader.getClass().getName()
                    + " doesn't support property "
                    + name,
                e,
                getLocation());
        }
    }
    protected boolean doValidate(File afile) {
        initValidator();
        boolean result = true;
        try {
            log("Validating " + afile.getName() + "... ", Project.MSG_VERBOSE);
            errorHandler.init(afile);
            InputSource is = new InputSource(new FileInputStream(afile));
            String uri = FILE_UTILS.toURI(afile.getAbsolutePath());
            is.setSystemId(uri);
            xmlReader.parse(is);
        } catch (SAXException ex) {
            log("Caught when validating: " + ex.toString(), Project.MSG_DEBUG);
            if (failOnError) {
                throw new BuildException(
                    "Could not validate document " + afile);
            }
            log("Could not validate document " + afile + ": " + ex.toString());
            result = false;
        } catch (IOException ex) {
            throw new BuildException(
                "Could not validate document " + afile,
                ex);
        }
        if (errorHandler.getFailure()) {
            if (failOnError) {
                throw new BuildException(
                    afile + " is not a valid XML document.");
            }
            result = false;
            log(afile + " is not a valid XML document", Project.MSG_ERR);
        }
        return result;
    }
    protected class ValidatorErrorHandler implements ErrorHandler {
        protected File currentFile = null;
        protected String lastErrorMessage = null;
        protected boolean failed = false;
        public void init(File file) {
            currentFile = file;
            failed = false;
        }
        public boolean getFailure() {
            return failed;
        }
        public void fatalError(SAXParseException exception) {
            failed = true;
            doLog(exception, Project.MSG_ERR);
        }
        public void error(SAXParseException exception) {
            failed = true;
            doLog(exception, Project.MSG_ERR);
        }
        public void warning(SAXParseException exception) {
            if (warn) {
                doLog(exception, Project.MSG_WARN);
            }
        }
        private void doLog(SAXParseException e, int logLevel) {
            log(getMessage(e), logLevel);
        }
        private String getMessage(SAXParseException e) {
            String sysID = e.getSystemId();
            if (sysID != null) {
                String name = sysID;
                if (sysID.startsWith("file:")) {
                    try {
                        name = FILE_UTILS.fromURI(sysID);
                    } catch (Exception ex) {
                    }
                }
                int line = e.getLineNumber();
                int col = e.getColumnNumber();
                return  name
                    + (line == -1
                       ? ""
                       : (":" + line + (col == -1 ? "" : (":" + col))))
                    + ": "
                    + e.getMessage();
            }
            return e.getMessage();
        }
    }
    public static class Attribute {
        private String attributeName = null;
        private boolean attributeValue;
        public void setName(String name) {
            attributeName = name;
        }
        public void setValue(boolean value) {
            attributeValue = value;
        }
        public String getName() {
            return attributeName;
        }
        public boolean getValue() {
            return attributeValue;
        }
    }
    public static final class Property {
        private String name;
        private String value;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    } 
}
