package org.apache.batik.test.svg;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.util.XMLResourceDescriptor;
public class SelfContainedSVGOnLoadTest extends AbstractTest {
    public static final String ERROR_CANNOT_LOAD_SVG_DOCUMENT
        = "SelfContainedSVGOnLoadTest.error.cannot.load.svg.document";
    public static final String ERROR_WHILE_PROCESSING_SVG_DOCUMENT
        = "SelfContainedSVGOnLoadTest.error.while.processing.svg.document";
    public static final String ERROR_UNEXPECTED_NUMBER_OF_TEST_RESULT_ELEMENTS
        = "SelfContainedSVGOnLoadTest.error.unexpected.number.of.test.result.elements";
    public static final String ERROR_UNEXPECTED_RESULT_VALUE
        = "SelfContainedSVGOnLoadTest.error.unexpected.result.value";
    public static final String ERROR_MISSING_OR_EMPTY_ERROR_CODE_ON_FAILED_TEST
        = "SelfContainedSVGOnLoadTest.error.missing.or.empty.error.code.on.failed.test";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION 
        = "SelfContainedSVGOnLoadTest.entry.key.error.description";
    public static final String ENTRY_KEY_NUMBER_OF_TEST_RESULT_ELEMENTS
        = "SelfContainedSVGOnLoadTest.entry.key.number.of.test.result.elements";
    public static final String ENTRY_KEY_RESULT_VALUE
        = "SelfContainedSVGOnLoadTest.entry.key.result.value";
    public static final String testNS = "http://xml.apache.org/batik/test";
    public static final String TAG_TEST_RESULT = "testResult";
    public static final String TAG_ERROR_DESCRIPTION_ENTRY = "errorDescriptionEntry";
    public static final String ATTRIBUTE_RESULT = "result";
    public static final String ATTRIBUTE_KEY = "id";
    public static final String ATTRIBUTE_VALUE = "value";
    public static final String TEST_RESULT_PASSED = "passed";
    public static final String TEST_RESULT_FAILED = "failed";
    protected String svgURL;
    public SelfContainedSVGOnLoadTest(String svgURL){
        this.svgURL = resolveURL(svgURL);
    }
    protected SelfContainedSVGOnLoadTest(){
    }
    protected String resolveURL(String url){
        File f = (new File(url)).getAbsoluteFile();
        if(f.getParentFile().exists()){
            try{
                return f.toURL().toString();
            }catch(MalformedURLException e){
                throw new IllegalArgumentException();
            }
        }
        try{
            return (new URL(url)).toString();
        }catch(MalformedURLException e){
            throw new IllegalArgumentException(url);
        }
    }
    public TestReport runImpl() throws Exception{
        DefaultTestReport report 
            = new DefaultTestReport(this);
        String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClassName);
        Document doc = null;
        try {
            doc = f.createDocument(svgURL);
        } catch(IOException e){
            report.setErrorCode(ERROR_CANNOT_LOAD_SVG_DOCUMENT);
            report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                       e.getMessage());
            report.setPassed(false);
            return report;
        } catch(Exception e){
            report.setErrorCode(ERROR_CANNOT_LOAD_SVG_DOCUMENT);
            report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                       e.getMessage());
            report.setPassed(false);
            return report;
        }
        UserAgent userAgent = buildUserAgent();
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        ctx.setDynamic(true);
        try {
            builder.build(ctx, doc);
            BaseScriptingEnvironment scriptEnvironment 
                = new BaseScriptingEnvironment(ctx);
            scriptEnvironment.loadScripts();
            scriptEnvironment.dispatchSVGLoadEvent();
        } catch (BridgeException e){
            e.printStackTrace();
            report.setErrorCode(ERROR_WHILE_PROCESSING_SVG_DOCUMENT);
            report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                       e.getMessage());
            report.setPassed(false);
            return report;
        } catch(Exception e){
            e.printStackTrace();
            report.setErrorCode(ERROR_WHILE_PROCESSING_SVG_DOCUMENT);
            report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                       e.getMessage());
            report.setPassed(false);
            return report;
        }
        NodeList testResultList = doc.getElementsByTagNameNS(testNS,
                                                             TAG_TEST_RESULT);
        if(testResultList.getLength() != 1){
            report.setErrorCode(ERROR_UNEXPECTED_NUMBER_OF_TEST_RESULT_ELEMENTS);
            report.addDescriptionEntry(ENTRY_KEY_NUMBER_OF_TEST_RESULT_ELEMENTS,
                                  "" + testResultList.getLength());
            report.setPassed(false);
            return report;
        }
        Element testResult = (Element)testResultList.item(0);
        String result = testResult.getAttributeNS(null, ATTRIBUTE_RESULT);
        boolean passed = true;
        if(TEST_RESULT_PASSED.equals(result)){
        } else if (TEST_RESULT_FAILED.equals(result)){
            passed = false;
        } else {
            report.setErrorCode(ERROR_UNEXPECTED_RESULT_VALUE);
            report.addDescriptionEntry(ENTRY_KEY_RESULT_VALUE, result);
            report.setPassed(false);
            return report;
        }
        if( !passed ){
            String errorCode = testResult.getAttributeNS(null, "errorCode");
            if("".equals(errorCode)){
                report.setErrorCode(ERROR_MISSING_OR_EMPTY_ERROR_CODE_ON_FAILED_TEST);
                report.setPassed(false);
                return report;
            }
            report.setErrorCode(errorCode);
            NodeList desc = testResult.getElementsByTagNameNS(testNS,
                                                              TAG_ERROR_DESCRIPTION_ENTRY);
            int nDesc = desc.getLength();
            for (int i=0; i<nDesc; i++){
                Element entry = (Element)desc.item(i);
                String key = entry.getAttributeNS(null, ATTRIBUTE_KEY);
                String value = entry.getAttributeNS(null, ATTRIBUTE_VALUE);
                report.addDescriptionEntry(key, value);
            }
            report.setPassed(false);
            return report;
        }
        return report;
    }
    protected UserAgent buildUserAgent(){
        return new UserAgentAdapter();
    }
}
