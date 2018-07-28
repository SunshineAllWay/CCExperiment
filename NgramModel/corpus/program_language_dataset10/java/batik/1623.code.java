package org.apache.batik.test.xml;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.batik.test.DefaultTestSuite;
import org.apache.batik.test.TestSuite;
import org.apache.batik.test.Test;
import org.apache.batik.test.TestException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XMLTestSuiteLoader implements XTSConstants {
    public static final String TEST_SUITE_LOADING_EXCEPTION
        = "xml.XMLTestSuiteLoader.error.test.suite.loading.exception";
    public static final String CANNOT_CREATE_TEST
        = "xml.XMLTestSuiteLoader.error.cannot.create.test";
    public static TestSuite loadTestSuite(String testSuiteURI, 
                                          TestSuite parent) 
        throws TestException{
        Document testSuiteDocument = loadTestSuiteDocument(testSuiteURI);
        return buildTestSuite(testSuiteDocument.getDocumentElement(), parent);
    }
    protected static Document loadTestSuiteDocument(String testSuiteURI)
        throws TestException{
        Document doc = null;
        try{
            DocumentBuilder docBuilder
                = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(testSuiteURI);
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(TEST_SUITE_LOADING_EXCEPTION,
                                    new Object[] { testSuiteURI,
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);            
        }
        return doc;
    }
    protected static TestSuite buildTestSuite(Element element,
                                              TestSuite parent) 
        throws TestException {
        DefaultTestSuite testSuite 
            = new DefaultTestSuite();
        String suiteId 
            = element.getAttribute(XTS_ID_ATTRIBUTE);
        testSuite.setId(suiteId);
        NodeList children = element.getChildNodes();
        if(children != null && children.getLength() > 0){
            int n = children.getLength();
            for(int i=0; i<n; i++){
                Node child = children.item(i);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    Element childElement = (Element)child;
                    String tagName = childElement.getTagName().intern();
                    if(tagName == XTS_TEST_TAG){
                        Test t = buildTest(childElement);
                        testSuite.addTest(t);
                    }
                    else if(tagName == XTS_TEST_GROUP_TAG){
                        Test t = buildTestSuite(childElement, testSuite);
                        testSuite.addTest(t);
                    }
                }
            }
        }
        return testSuite;
    }
    protected static Test buildTest(Element element) throws TestException {
        try{
            Test t = (Test)XMLReflect.buildObject(element);
            String id 
                = element.getAttribute(XTS_ID_ATTRIBUTE);
            t.setId(id);
            return t;
        }catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(CANNOT_CREATE_TEST,
                                    new Object[] { element.getAttribute(XR_CLASS_ATTRIBUTE),
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
}
