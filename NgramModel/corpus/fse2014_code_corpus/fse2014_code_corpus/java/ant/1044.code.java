package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.apache.tools.ant.taskdefs.XSLTLiaison;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
public abstract class AbstractXSLTLiaisonTest extends TestCase {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    protected XSLTLiaison liaison;
    protected  AbstractXSLTLiaisonTest(String name){
        super(name);
    }
    protected void setUp() throws Exception {
        liaison = createLiaison();
    }
    protected abstract XSLTLiaison createLiaison() throws Exception ;
    protected File getFile(String name) throws FileNotFoundException {
        URL url = getClass().getResource(name);
        if (url == null){
          throw new FileNotFoundException("Unable to load '" + name + "' from classpath");
        }
        return new File(FILE_UTILS.fromURI(url.toExternalForm()));
    }
    public void testTransform() throws Exception {
        File xsl = getFile("/taskdefs/optional/xsltliaison-in.xsl");
        liaison.setStylesheet(xsl);
        liaison.addParam("param", "value");
        File in = getFile("/taskdefs/optional/xsltliaison-in.xml");
        File out = new File("xsltliaison.tmp");
        out.deleteOnExit(); 
        try {
            liaison.transform(in, out);
        } finally {
            out.delete();
        }
    }
    public void testEncoding() throws Exception {
        File xsl = getFile("/taskdefs/optional/xsltliaison-encoding-in.xsl");
        liaison.setStylesheet(xsl);
        File in = getFile("/taskdefs/optional/xsltliaison-encoding-in.xml");
        File out = new File("xsltliaison-encoding.tmp");
        out.deleteOnExit(); 
        try {
            liaison.transform(in, out);
            Document doc = parseXML(out);
            assertEquals("root",doc.getDocumentElement().getNodeName());
            assertEquals("message",doc.getDocumentElement().getFirstChild().getNodeName());
            assertEquals("\u00E9\u00E0\u00E8\u00EF\u00F9",doc.getDocumentElement().getFirstChild().getFirstChild().getNodeValue());
        } finally {
            out.delete();
        }
    }
    public Document parseXML(File file) throws Exception {
        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbuilder = dbfactory.newDocumentBuilder();
        return dbuilder.parse(file);
    }
}
