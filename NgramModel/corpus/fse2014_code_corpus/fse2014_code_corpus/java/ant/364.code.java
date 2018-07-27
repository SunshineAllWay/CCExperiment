package org.apache.tools.ant.taskdefs.optional;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.XSLTLiaison3;
import org.apache.tools.ant.taskdefs.XSLTLogger;
import org.apache.tools.ant.taskdefs.XSLTLoggerAware;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLProvider;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
public class TraXLiaison implements XSLTLiaison3, ErrorListener, XSLTLoggerAware {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Project project;
    private String factoryName = null;
    private TransformerFactory tfactory = null;
    private Resource stylesheet;
    private XSLTLogger logger;
    private EntityResolver entityResolver;
    private Transformer transformer;
    private Templates templates;
    private long templatesModTime;
    private URIResolver uriResolver;
    private Vector outputProperties = new Vector();
    private Hashtable params = new Hashtable();
    private Vector attributes = new Vector();
    private boolean suppressWarnings = false;
    private XSLTProcess.TraceConfiguration traceConfiguration = null;
    public TraXLiaison() throws Exception {
    }
    public void setStylesheet(File stylesheet) throws Exception {
        FileResource fr = new FileResource();
        fr.setProject(project);
        fr.setFile(stylesheet);
        setStylesheet(fr);
    }
    public void setStylesheet(Resource stylesheet) throws Exception {
        if (this.stylesheet != null) {
            transformer = null;
            if (!this.stylesheet.equals(stylesheet)
                || (stylesheet.getLastModified() != templatesModTime)) {
                templates = null;
            }
        }
        this.stylesheet = stylesheet;
    }
    public void transform(File infile, File outfile) throws Exception {
        if (transformer == null) {
            createTransformer();
        }
        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(infile));
            fos = new BufferedOutputStream(new FileOutputStream(outfile));
            StreamResult res = new StreamResult(fos);
            res.setSystemId(JAXPUtils.getSystemId(outfile));
            Source src = getSource(fis, infile);
            setTransformationParameters();
            transformer.transform(src, res);
        } finally {
            FileUtils.close(fis);
            FileUtils.close(fos);
        }
    }
    private Source getSource(InputStream is, File infile)
        throws ParserConfigurationException, SAXException {
        Source src = null;
        if (entityResolver != null) {
            if (getFactory().getFeature(SAXSource.FEATURE)) {
                SAXParserFactory spFactory = SAXParserFactory.newInstance();
                spFactory.setNamespaceAware(true);
                XMLReader reader = spFactory.newSAXParser().getXMLReader();
                reader.setEntityResolver(entityResolver);
                src = new SAXSource(reader, new InputSource(is));
            } else {
                throw new IllegalStateException("xcatalog specified, but "
                    + "parser doesn't support SAX");
            }
        } else {
            src = new StreamSource(is);
        }
        src.setSystemId(JAXPUtils.getSystemId(infile));
        return src;
    }
    private Source getSource(InputStream is, Resource resource)
        throws ParserConfigurationException, SAXException {
        Source src = null;
        if (entityResolver != null) {
            if (getFactory().getFeature(SAXSource.FEATURE)) {
                SAXParserFactory spFactory = SAXParserFactory.newInstance();
                spFactory.setNamespaceAware(true);
                XMLReader reader = spFactory.newSAXParser().getXMLReader();
                reader.setEntityResolver(entityResolver);
                src = new SAXSource(reader, new InputSource(is));
            } else {
                throw new IllegalStateException("xcatalog specified, but "
                    + "parser doesn't support SAX");
            }
        } else {
            src = new StreamSource(is);
        }
        src.setSystemId(resourceToURI(resource));
        return src;
    }
    private String resourceToURI(Resource resource) {
        FileProvider fp = (FileProvider) resource.as(FileProvider.class);
        if (fp != null) {
            return FILE_UTILS.toURI(fp.getFile().getAbsolutePath());
        }
        URLProvider up = (URLProvider) resource.as(URLProvider.class);
        if (up != null) {
            URL u = up.getURL();
            return String.valueOf(u);
        } else {
            return resource.getName();
        }
    }
    private void readTemplates()
        throws IOException, TransformerConfigurationException,
               ParserConfigurationException, SAXException {
        InputStream xslStream = null;
        try {
            xslStream
                = new BufferedInputStream(stylesheet.getInputStream());
            templatesModTime = stylesheet.getLastModified();
            Source src = getSource(xslStream, stylesheet);
            templates = getFactory().newTemplates(src);
        } finally {
            if (xslStream != null) {
                xslStream.close();
            }
        }
    }
    private void createTransformer() throws Exception {
        if (templates == null) {
            readTemplates();
        }
        transformer = templates.newTransformer();
        transformer.setErrorListener(this);
        if (uriResolver != null) {
            transformer.setURIResolver(uriResolver);
        }
        for (int i = 0; i < outputProperties.size(); i++) {
            final String[] pair = (String[]) outputProperties.elementAt(i);
            transformer.setOutputProperty(pair[0], pair[1]);
        }
        if (traceConfiguration != null) {
            if ("org.apache.xalan.transformer.TransformerImpl"
                .equals(transformer.getClass().getName())) {
                try {
                    Class traceSupport =
                        Class.forName("org.apache.tools.ant.taskdefs.optional."
                                      + "Xalan2TraceSupport", true,
                                      Thread.currentThread()
                                      .getContextClassLoader());
                    XSLTTraceSupport ts =
                        (XSLTTraceSupport) traceSupport.newInstance();
                    ts.configureTrace(transformer, traceConfiguration);
                } catch (Exception e) {
                    String msg = "Failed to enable tracing because of " + e;
                    if (project != null) {
                        project.log(msg, Project.MSG_WARN);
                    } else {
                        System.err.println(msg);
                    }
                }
            } else {
                String msg = "Not enabling trace support for transformer"
                    + " implementation" + transformer.getClass().getName();
                if (project != null) {
                    project.log(msg, Project.MSG_WARN);
                } else {
                    System.err.println(msg);
                }
            }
        }
    }
    private void setTransformationParameters() {
        for (final Enumeration enumeration = params.keys();
             enumeration.hasMoreElements();) {
            final String name = (String) enumeration.nextElement();
            final String value = (String) params.get(name);
            transformer.setParameter(name, value);
        }
    }
    private TransformerFactory getFactory() throws BuildException {
        if (tfactory != null) {
            return tfactory;
        }
        if (factoryName == null) {
            tfactory = TransformerFactory.newInstance();
        } else {
            try {
                Class clazz = null;
                try {
                    clazz =
                        Class.forName(factoryName, true,
                                      Thread.currentThread()
                                      .getContextClassLoader());
                } catch (ClassNotFoundException cnfe) {
                    String msg = "Failed to load " + factoryName
                        + " via the configured classpath, will try"
                        + " Ant's classpath instead.";
                    if (logger != null) {
                        logger.log(msg);
                    } else if (project != null) {
                        project.log(msg, Project.MSG_WARN);
                    } else {
                        System.err.println(msg);
                    }
                }
                if (clazz == null) {
                    clazz = Class.forName(factoryName);
                }
                tfactory = (TransformerFactory) clazz.newInstance();
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
        tfactory.setErrorListener(this);
        for (int i = 0; i < attributes.size(); i++) {
            final Object[] pair = (Object[]) attributes.elementAt(i);
            tfactory.setAttribute((String) pair[0], pair[1]);
        }
        if (uriResolver != null) {
            tfactory.setURIResolver(uriResolver);
        }
        return tfactory;
    }
    public void setFactory(String name) {
        factoryName = name;
    }
    public void setAttribute(String name, Object value) {
        final Object[] pair = new Object[]{name, value};
        attributes.addElement(pair);
    }
    public void setOutputProperty(String name, String value) {
        final String[] pair = new String[]{name, value};
        outputProperties.addElement(pair);
    }
    public void setEntityResolver(EntityResolver aResolver) {
        entityResolver = aResolver;
    }
    public void setURIResolver(URIResolver aResolver) {
        uriResolver = aResolver;
    }
    public void addParam(String name, String value) {
        params.put(name, value);
    }
    public void setLogger(XSLTLogger l) {
        logger = l;
    }
    public void error(TransformerException e) {
        logError(e, "Error");
    }
    public void fatalError(TransformerException e) {
        logError(e, "Fatal Error");
        throw new BuildException("Fatal error during transformation", e);
    }
    public void warning(TransformerException e) {
        if (!suppressWarnings) {
            logError(e, "Warning");
        }
    }
    private void logError(TransformerException e, String type) {
        if (logger == null) {
            return;
        }
        StringBuffer msg = new StringBuffer();
        SourceLocator locator = e.getLocator();
        if (locator != null) {
            String systemid = locator.getSystemId();
            if (systemid != null) {
                String url = systemid;
                if (url.startsWith("file:")) {
                    url = FileUtils.getFileUtils().fromURI(url);
                }
                msg.append(url);
            } else {
                msg.append("Unknown file");
            }
            int line = locator.getLineNumber();
            if (line != -1) {
                msg.append(":");
                msg.append(line);
                int column = locator.getColumnNumber();
                if (column != -1) {
                    msg.append(":");
                    msg.append(column);
                }
            }
        }
        msg.append(": ");
        msg.append(type);
        msg.append("! ");
        msg.append(e.getMessage());
        if (e.getCause() != null) {
            msg.append(" Cause: ");
            msg.append(e.getCause());
        }
        logger.log(msg.toString());
    }
    protected String getSystemId(File file) {
        return JAXPUtils.getSystemId(file);
    }
    public void configure(XSLTProcess xsltTask) {
        project = xsltTask.getProject();
        XSLTProcess.Factory factory = xsltTask.getFactory();
        if (factory != null) {
            setFactory(factory.getName());
            for (Enumeration attrs = factory.getAttributes();
                    attrs.hasMoreElements();) {
                XSLTProcess.Factory.Attribute attr =
                        (XSLTProcess.Factory.Attribute) attrs.nextElement();
                setAttribute(attr.getName(), attr.getValue());
            }
        }
        XMLCatalog xmlCatalog = xsltTask.getXMLCatalog();
        if (xmlCatalog != null) {
            setEntityResolver(xmlCatalog);
            setURIResolver(xmlCatalog);
        }
        for (Enumeration props = xsltTask.getOutputProperties();
                props.hasMoreElements();) {
            XSLTProcess.OutputProperty prop
                = (XSLTProcess.OutputProperty) props.nextElement();
            setOutputProperty(prop.getName(), prop.getValue());
        }
        suppressWarnings = xsltTask.getSuppressWarnings();
        traceConfiguration = xsltTask.getTraceConfiguration();
    }
}
