package org.apache.batik.bridge;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
public class ExternalResourcesTest extends AbstractTest
    implements ErrorConstants {
    public static final String ERROR_CANNOT_LOAD_SVG_DOCUMENT
        = "ExternalResourcesTest.error.cannot.load.svg.document";
    public static final String ERROR_WHILE_PROCESSING_SVG_DOCUMENT
        = "ExternalResourcesTest.error.while.processing.svg.document";
    public static final String ERROR_UNTHROWN_SECURITY_EXCEPTIONS
        = "ExternalResourcesTest.error.unthrown.security.exceptions";
    public static final String ERROR_THROWN_SECURITY_EXCEPTIONS
        = "ExternalResourcesTest.error.thrown.security.exceptions";
    public static final String ERROR_NO_INSERTION_POINT_IN_DOCUMENT
        = "ExternalResourceTest.error.no.insertion.point.in.document";
    public static final String ERROR_NO_ID_LIST
        = "ExternalResourceTest.error.no.id.list";
    public static final String ERROR_TARGET_ID_NOT_FOUND
        = "ExternalResourcesTest.error.target.id.not.found";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "ExternalResourcesTest.entry.key.error.description";
    public static final String ENTRY_KEY_INSERTION_POINT_ID
        = "ExternalResourcesTest.entry.key.insertion.point.id";
    public static final String ENTRY_KEY_TARGET_ID
        = "ExternalResourcesTest.entry.target.id";
    public static final String ENTRY_KEY_EXPECTED_EXCEPTION_ON
        = "ExternalResourcesTest.entry.key.expected.exception.on";
    public static final String ENTRY_KEY_UNEXPECTED_EXCEPTION_ON
        = "ExternalResourcesTest.entry.key.unexpected.exception.on";
    public static final String EXTERNAL_STYLESHEET_ID
        = "external-stylesheet";
    public static final String testNS = "http://xml.apache.org/batik/test";
    public static final String INSERTION_POINT_ID = "insertionPoint";
    public static final String FILE_DIR =
        "test-resources/org/apache/batik/bridge/";
    protected boolean secure = true;
    String svgURL;
    public void setId(String id){
        super.setId(id);
        String file = id;
        int idx = file.indexOf('.');
        if (idx != -1) {
            file = file.substring(0,idx);
        }
        svgURL = resolveURL(FILE_DIR + file + ".svg");
    }
    public Boolean getSecure(){
        return secure ? Boolean.TRUE : Boolean.FALSE;
    }
    public void setSecure(Boolean secure) {
        this.secure = secure.booleanValue();
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
        List failures = new ArrayList();
        MyUserAgent userAgent = buildUserAgent();
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        ctx.setDynamic(true);
        Throwable th = null;
        try {
            GraphicsNode gn = builder.build(ctx, doc);
            gn.getBounds();
            th = userAgent.getDisplayError();
        } catch (BridgeException e){
            th = e;
        } catch (SecurityException e) {
            th = e;
        } catch (Throwable t) {
            th = t;
        }
        if (th == null) {
            if (secure)
                failures.add(EXTERNAL_STYLESHEET_ID);
        } else if (th instanceof SecurityException) {
            if (!secure)
                failures.add(EXTERNAL_STYLESHEET_ID);
        } else if (th instanceof BridgeException) {
            BridgeException be = (BridgeException)th;
            if (!secure  ||
                (secure && !ERR_URI_UNSECURE.equals(be.getCode()))) {
                report.setErrorCode(ERROR_WHILE_PROCESSING_SVG_DOCUMENT);
                report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                           be.getMessage());
                report.setPassed(false);
                return report;
            }
        }
        Node child = doc.getFirstChild();
        Node next = null;
        while (child != null) {
            next = child.getNextSibling();
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                doc.removeChild(child);
            }
            child = next;
        }
        Element root = doc.getDocumentElement();
        String idList = root.getAttributeNS(testNS, "targetids");
        if (idList == null || "".equals(idList)) {
            report.setErrorCode(ERROR_NO_ID_LIST);
            report.setPassed(false);
            return report;
        }
        StringTokenizer st = new StringTokenizer(idList, ",");
        String[] ids = new String[st.countTokens()];
        for (int i=0; i<ids.length; i++) {
            ids[i] = st.nextToken().trim();
        }
        for (int i=0; i<ids.length; i++) {
            String id = ids[i];
            userAgent = buildUserAgent();
            builder = new GVTBuilder();
            ctx = new BridgeContext(userAgent);
            ctx.setDynamic(true);
            Document cloneDoc = (Document)doc.cloneNode(true);
            Element insertionPoint = cloneDoc.getElementById(INSERTION_POINT_ID);
            if (insertionPoint == null) {
                report.setErrorCode(ERROR_NO_INSERTION_POINT_IN_DOCUMENT);
                report.addDescriptionEntry(ENTRY_KEY_INSERTION_POINT_ID,
                                           INSERTION_POINT_ID);
                report.setPassed(false);
                return report;
            }
            Element target = cloneDoc.getElementById(id);
            if (target == null) {
                report.setErrorCode(ERROR_TARGET_ID_NOT_FOUND);
                report.addDescriptionEntry(ENTRY_KEY_TARGET_ID,
                                           id);
                report.setPassed(false);
                return report;
            }
            insertionPoint.appendChild(target);
            th = null;
            try {
                GraphicsNode gn = builder.build(ctx, cloneDoc);
                gn.getBounds();
                th = userAgent.getDisplayError();
            } catch (BridgeException e){
                th = e;
            } catch (SecurityException e) {
                th = e;
            } catch (Throwable t) {
                th = t;
            }
            if (th == null) {
                if (secure)
                    failures.add(id);
            } else if (th instanceof SecurityException) {
                if (!secure)
                    failures.add(id);
            } else if (th instanceof BridgeException) {
                BridgeException be = (BridgeException)th;
                if (!secure  ||
                    (secure && !ERR_URI_UNSECURE.equals(be.getCode()))) {
                    report.setErrorCode(ERROR_WHILE_PROCESSING_SVG_DOCUMENT);
                    report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                               be.getMessage());
                    report.setPassed(false);
                    return report;
                }
            } else {
                report.setErrorCode(ERROR_WHILE_PROCESSING_SVG_DOCUMENT);
                report.addDescriptionEntry(ENTRY_KEY_ERROR_DESCRIPTION,
                                           th.getMessage());
                report.setPassed(false);
                return report;
            }
        }
        if (failures.size() == 0) {
            return reportSuccess();
        }
        if (secure) {
            report.setErrorCode(ERROR_UNTHROWN_SECURITY_EXCEPTIONS);
            for (int i=0; i<failures.size(); i++) {
                report.addDescriptionEntry(ENTRY_KEY_EXPECTED_EXCEPTION_ON,
                                           failures.get(i));
            }
        } else {
            report.setErrorCode(ERROR_THROWN_SECURITY_EXCEPTIONS);
            for (int i=0; i<failures.size(); i++) {
                report.addDescriptionEntry(ENTRY_KEY_UNEXPECTED_EXCEPTION_ON,
                                           failures.get(i));
            }
        }
        report.setPassed(false);
        return report;
    }
    protected interface MyUserAgent extends UserAgent {
        Exception getDisplayError();
    }
    protected MyUserAgent buildUserAgent(){
        if (secure) {
            return new SecureUserAgent();
        } else {
            return new RelaxedUserAgent();
        }
    }
    class MyUserAgentAdapter extends UserAgentAdapter implements MyUserAgent {
        Exception ex = null;
        public void displayError(Exception ex) {
            this.ex = ex;
            super.displayError(ex);
        }
        public Exception getDisplayError() { return ex; }
    }
    class SecureUserAgent extends MyUserAgentAdapter {
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourcePURL,
                                        ParsedURL docPURL){
            return new NoLoadExternalResourceSecurity();
        }
    }
    class RelaxedUserAgent extends MyUserAgentAdapter {
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourcePURL,
                                        ParsedURL docPURL){
            return new RelaxedExternalResourceSecurity(resourcePURL,
                                                       docPURL);
        }
    }
}
