package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class DescriptorHandler extends org.xml.sax.HandlerBase {
    private static final int DEFAULT_HASH_TABLE_SIZE = 10;
    private static final int STATE_LOOKING_EJBJAR = 1;
    private static final int STATE_IN_EJBJAR = 2;
    private static final int STATE_IN_BEANS = 3;
    private static final int STATE_IN_SESSION = 4;
    private static final int STATE_IN_ENTITY = 5;
    private static final int STATE_IN_MESSAGE = 6;
    private Task owningTask;
    private String publicId = null;
    private static final String EJB_REF               = "ejb-ref";
    private static final String EJB_LOCAL_REF         = "ejb-local-ref";
    private static final String HOME_INTERFACE        = "home";
    private static final String REMOTE_INTERFACE      = "remote";
    private static final String LOCAL_HOME_INTERFACE  = "local-home";
    private static final String LOCAL_INTERFACE       = "local";
    private static final String BEAN_CLASS            = "ejb-class";
    private static final String PK_CLASS              = "prim-key-class";
    private static final String EJB_NAME              = "ejb-name";
    private static final String EJB_JAR               = "ejb-jar";
    private static final String ENTERPRISE_BEANS      = "enterprise-beans";
    private static final String ENTITY_BEAN           = "entity";
    private static final String SESSION_BEAN          = "session";
    private static final String MESSAGE_BEAN          = "message-driven";
    private int parseState = STATE_LOOKING_EJBJAR;
    protected String currentElement = null;
    protected String currentText = null;
    protected Hashtable ejbFiles = null;
    protected String ejbName = null;
    private Hashtable fileDTDs = new Hashtable();
    private Hashtable resourceDTDs = new Hashtable();
    private boolean inEJBRef = false;
    private Hashtable urlDTDs = new Hashtable();
    private File srcDir;
    public DescriptorHandler(Task task, File srcDir) {
        this.owningTask = task;
        this.srcDir = srcDir;
    }
    public void registerDTD(String publicId, String location) {
        if (location == null) {
            return;
        }
        File fileDTD = new File(location);
        if (!fileDTD.exists()) {
            fileDTD = owningTask.getProject().resolveFile(location);
        }
        if (fileDTD.exists()) {
            if (publicId != null) {
                fileDTDs.put(publicId, fileDTD);
                owningTask.log("Mapped publicId " + publicId + " to file "
                    + fileDTD, Project.MSG_VERBOSE);
            }
            return;
        }
        if (getClass().getResource(location) != null) {
            if (publicId != null) {
                resourceDTDs.put(publicId, location);
                owningTask.log("Mapped publicId " + publicId + " to resource "
                    + location, Project.MSG_VERBOSE);
            }
        }
        try {
            if (publicId != null) {
                URL urldtd = new URL(location);
                urlDTDs.put(publicId, urldtd);
            }
        } catch (java.net.MalformedURLException e) {
        }
    }
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
        this.publicId = publicId;
        File dtdFile = (File) fileDTDs.get(publicId);
        if (dtdFile != null) {
            try {
                owningTask.log("Resolved " + publicId + " to local file "
                    + dtdFile, Project.MSG_VERBOSE);
                return new InputSource(new FileInputStream(dtdFile));
            } catch (FileNotFoundException ex) {
            }
        }
        String dtdResourceName = (String) resourceDTDs.get(publicId);
        if (dtdResourceName != null) {
            InputStream is = this.getClass().getResourceAsStream(dtdResourceName);
            if (is != null) {
                owningTask.log("Resolved " + publicId + " to local resource "
                    + dtdResourceName, Project.MSG_VERBOSE);
                return new InputSource(is);
            }
        }
        URL dtdUrl = (URL) urlDTDs.get(publicId);
        if (dtdUrl != null) {
            try {
                InputStream is = dtdUrl.openStream();
                owningTask.log("Resolved " + publicId + " to url "
                    + dtdUrl, Project.MSG_VERBOSE);
                return new InputSource(is);
            } catch (IOException ioe) {
            }
        }
        owningTask.log("Could not resolve ( publicId: " + publicId
            + ", systemId: " + systemId + ") to a local entity", Project.MSG_INFO);
        return null;
    }
    public Hashtable getFiles() {
        return (ejbFiles == null) ? new Hashtable() : ejbFiles;
    }
    public String getPublicId() {
        return publicId;
    }
    public String getEjbName() {
        return ejbName;
    }
    public void startDocument() throws SAXException {
        this.ejbFiles = new Hashtable(DEFAULT_HASH_TABLE_SIZE, 1);
        this.currentElement = null;
        inEJBRef = false;
    }
    public void startElement(String name, AttributeList attrs)
        throws SAXException {
        this.currentElement = name;
        currentText = "";
        if (name.equals(EJB_REF) || name.equals(EJB_LOCAL_REF)) {
            inEJBRef = true;
        } else if (parseState == STATE_LOOKING_EJBJAR && name.equals(EJB_JAR)) {
            parseState = STATE_IN_EJBJAR;
        } else if (parseState == STATE_IN_EJBJAR && name.equals(ENTERPRISE_BEANS)) {
            parseState = STATE_IN_BEANS;
        } else if (parseState == STATE_IN_BEANS && name.equals(SESSION_BEAN)) {
            parseState = STATE_IN_SESSION;
        } else if (parseState == STATE_IN_BEANS && name.equals(ENTITY_BEAN)) {
            parseState = STATE_IN_ENTITY;
        } else if (parseState == STATE_IN_BEANS && name.equals(MESSAGE_BEAN)) {
            parseState = STATE_IN_MESSAGE;
        }
    }
    public void endElement(String name) throws SAXException {
        processElement();
        currentText = "";
        this.currentElement = "";
        if (name.equals(EJB_REF) || name.equals(EJB_LOCAL_REF)) {
            inEJBRef = false;
        } else if (parseState == STATE_IN_ENTITY && name.equals(ENTITY_BEAN)) {
            parseState = STATE_IN_BEANS;
        } else if (parseState == STATE_IN_SESSION && name.equals(SESSION_BEAN)) {
            parseState = STATE_IN_BEANS;
        } else if (parseState == STATE_IN_MESSAGE && name.equals(MESSAGE_BEAN)) {
            parseState = STATE_IN_BEANS;
        } else if (parseState == STATE_IN_BEANS && name.equals(ENTERPRISE_BEANS)) {
            parseState = STATE_IN_EJBJAR;
        } else if (parseState == STATE_IN_EJBJAR && name.equals(EJB_JAR)) {
            parseState = STATE_LOOKING_EJBJAR;
        }
    }
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        currentText += new String(ch, start, length);
    }
    protected void processElement() {
        if (inEJBRef
            || (parseState != STATE_IN_ENTITY
                && parseState != STATE_IN_SESSION
                && parseState != STATE_IN_MESSAGE)) {
            return;
        }
        if (currentElement.equals(HOME_INTERFACE)
            || currentElement.equals(REMOTE_INTERFACE)
            || currentElement.equals(LOCAL_INTERFACE)
            || currentElement.equals(LOCAL_HOME_INTERFACE)
            || currentElement.equals(BEAN_CLASS)
            || currentElement.equals(PK_CLASS)) {
            File classFile = null;
            String className = currentText.trim();
            if (!className.startsWith("java.")
                && !className.startsWith("javax.")) {
                className = className.replace('.', File.separatorChar);
                className += ".class";
                classFile = new File(srcDir, className);
                ejbFiles.put(className, classFile);
            }
        }
        if (currentElement.equals(EJB_NAME)) {
            if (ejbName == null) {
                ejbName = currentText.trim();
            }
        }
    }
}
