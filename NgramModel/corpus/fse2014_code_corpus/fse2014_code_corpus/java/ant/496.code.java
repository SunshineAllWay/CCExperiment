package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class XMLResultAggregator extends Task implements XMLConstants {
    protected Vector filesets = new Vector();
    protected String toFile;
    protected File toDir;
    protected Vector transformers = new Vector();
    public static final String DEFAULT_DIR = ".";
    public static final String DEFAULT_FILENAME = "TESTS-TestSuites.xml";
    protected int generatedId = 0;
    static final String WARNING_IS_POSSIBLY_CORRUPTED
        = " is not a valid XML document. It is possibly corrupted.";
    static final String WARNING_INVALID_ROOT_ELEMENT
        = " is not a valid testsuite XML document";
    static final String WARNING_EMPTY_FILE
        = " is empty.\nThis can be caused by the test JVM exiting unexpectedly";
    public AggregateTransformer createReport() {
        AggregateTransformer transformer = new AggregateTransformer(this);
        transformers.addElement(transformer);
        return transformer;
    }
    public void setTofile(String value) {
        toFile = value;
    }
    public void setTodir(File value) {
        toDir = value;
    }
    public void addFileSet(FileSet fs) {
        filesets.addElement(fs);
    }
    public void execute() throws BuildException {
        Element rootElement = createDocument();
        File destFile = getDestinationFile();
        try {
            writeDOMTree(rootElement.getOwnerDocument(), destFile);
        } catch (IOException e) {
            throw new BuildException("Unable to write test aggregate to '" + destFile + "'", e);
        }
        Enumeration e = transformers.elements();
        while (e.hasMoreElements()) {
            AggregateTransformer transformer =
                (AggregateTransformer) e.nextElement();
            transformer.setXmlDocument(rootElement.getOwnerDocument());
            transformer.transform();
        }
    }
    public File getDestinationFile() {
        if (toFile == null) {
            toFile = DEFAULT_FILENAME;
        }
        if (toDir == null) {
            toDir = getProject().resolveFile(DEFAULT_DIR);
        }
        return new File(toDir, toFile);
    }
    protected File[] getFiles() {
        Vector v = new Vector();
        final int size = filesets.size();
        for (int i = 0; i < size; i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();
            String[] f = ds.getIncludedFiles();
            for (int j = 0; j < f.length; j++) {
                String pathname = f[j];
                if (pathname.endsWith(".xml")) {
                    File file = new File(ds.getBasedir(), pathname);
                    file = getProject().resolveFile(file.getPath());
                    v.addElement(file);
                }
            }
        }
        File[] files = new File[v.size()];
        v.copyInto(files);
        return files;
    }
    protected void writeDOMTree(Document doc, File file) throws IOException {
        OutputStream out = null;
        PrintWriter wri = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            wri = new PrintWriter(new OutputStreamWriter(out, "UTF8"));
            wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            (new DOMElementWriter()).write(doc.getDocumentElement(), wri, 0, "  ");
            wri.flush();
            if (wri.checkError()) {
                throw new IOException("Error while writing DOM content");
            }
        } finally {
            FileUtils.close(wri);
            FileUtils.close(out);
        }
    }
    protected Element createDocument() {
        DocumentBuilder builder = getDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement(TESTSUITES);
        doc.appendChild(rootElement);
        generatedId = 0;
        File[] files = getFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                log("Parsing file: '" + file + "'", Project.MSG_VERBOSE);
                if (file.length() > 0) {
                    Document testsuiteDoc
                            = builder.parse(
                                FileUtils.getFileUtils().toURI(files[i].getAbsolutePath()));
                    Element elem = testsuiteDoc.getDocumentElement();
                    if (TESTSUITE.equals(elem.getNodeName())) {
                        addTestSuite(rootElement, elem);
                        generatedId++;
                    } else {
                        log("the file " + file
                                + WARNING_INVALID_ROOT_ELEMENT,
                                Project.MSG_WARN);
                    }
                } else {
                    log("the file " + file
                            + WARNING_EMPTY_FILE,
                            Project.MSG_WARN);
                }
            } catch (SAXException e) {
                log("The file " + file + WARNING_IS_POSSIBLY_CORRUPTED, Project.MSG_WARN);
                log(StringUtils.getStackTrace(e), Project.MSG_DEBUG);
            } catch (IOException e) {
                log("Error while accessing file " + file + ": "
                    + e.getMessage(), Project.MSG_ERR);
                log("Error while accessing file " + file + ": "
                    + e.getMessage(), e, Project.MSG_VERBOSE);
            }
        }
        return rootElement;
    }
    protected void addTestSuite(Element root, Element testsuite) {
        String fullclassname = testsuite.getAttribute(ATTR_NAME);
        int pos = fullclassname.lastIndexOf('.');
        String pkgName = (pos == -1) ? "" : fullclassname.substring(0, pos);
        String classname = (pos == -1) ? fullclassname : fullclassname.substring(pos + 1);
        Element copy = (Element) DOMUtil.importNode(root, testsuite);
        copy.setAttribute(ATTR_NAME, classname);
        copy.setAttribute(ATTR_PACKAGE, pkgName);
        copy.setAttribute(ATTR_ID, Integer.toString(generatedId));
    }
    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }
}
