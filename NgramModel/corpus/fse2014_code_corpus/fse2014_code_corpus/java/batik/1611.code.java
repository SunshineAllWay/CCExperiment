package org.apache.batik.test.svg;
import java.io.File;
import java.io.FilePermission;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Enumeration;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.EmbededExternalResourceSecurity;
import org.apache.batik.bridge.EmbededScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.NoLoadExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.TestReport;
import org.apache.batik.util.ApplicationSecurityEnforcer;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGOnLoadExceptionTest extends AbstractTest {
    public static final String RAN = "ran";
    public static final String ERROR_EXCEPTION_DID_NOT_OCCUR
        = "SVGOnLoadExceptionTest.error.exception.did.not.occur";
    public static final String ERROR_UNEXPECTED_EXCEPTION
        = "SVGOnLoadExceptionTest.error.unexpected.exception";
    public static final String ERROR_UNEXPECTED_ERROR_CODE
        = "SVGOnLoadExceptionTest.error.unexpected.error.code";
    public static final String ERROR_SCRIPT_DID_NOT_RUN
        = "SVGOnLoadExceptionTest.error.script.did.not.run";
    public static final String ENTRY_KEY_UNEXPECTED_EXCEPTION
        = "SVGOnLoadExceptionTest.entry.key.unexpected.exception";
    public static final String ENTRY_KEY_UNEXPECTED_ERROR_CODE
        = "SVGOnLoadExceptionTest.entry.key.unexpected.error.code";
    public static final String ENTRY_KEY_EXPECTED_ERROR_CODE
        = "SVGOnLoadExceptionTest.entry.key.expected.error.code";
    public static final String ENTRY_KEY_EXPECTED_EXCEPTION
        = "SVGOnLoadExceptionTest.entry.key.expected.exception";
    public static final String ENTRY_KEY_UNEXPECTED_RESULT
        = "SVGOnLoadExceptionTest.entry.key.unexpected.result";
    public static final String ERROR_CODE_NO_CHECK
        = "noCheck";
    public static final String testNS = "http://xml.apache.org/batik/test";
    protected String svgURL;
    protected String scripts = "text/ecmascript, application/java-archive";
    protected String expectedExceptionClass = "org.apache.batik.bridge.Exception";
    protected String expectedErrorCode = "none";
    protected String scriptOrigin = "ANY";
    protected String resourceOrigin = "ANY";
    protected boolean secure = false;
    protected Boolean validate = Boolean.FALSE;
    protected String fileName;
    protected boolean restricted = false;
    public boolean getRestricted() {
        return restricted;
    }
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
    public void setScripts(String scripts){
        this.scripts = scripts;
    }
    public String getScripts(){
        return scripts;
    }
    public void setScriptOrigin(String scriptOrigin){
        this.scriptOrigin = scriptOrigin;
    }
    public String getScriptOrigin(){
        return this.scriptOrigin;
    }
    public void setResourceOrigin(String resourceOrigin){
        this.resourceOrigin = resourceOrigin;
    }
    public String getResourceOrigin(){
        return this.resourceOrigin;
    }
    public void setSecure(boolean secure){
        this.secure = secure;
    }
    public boolean getSecure(){
        return secure;
    }
    public void setExpectedExceptionClass(String expectedExceptionClass){
        this.expectedExceptionClass = expectedExceptionClass;
    }
    public String getExpectedExceptionClass(){
        return this.expectedExceptionClass;
    }
    public void setExpectedErrorCode(String expectedErrorCode){
        this.expectedErrorCode = expectedErrorCode;
    }
    public String getExpectedErrorCode(){
        return this.expectedErrorCode;
    }
    public Boolean getValidate() {
        return validate;
    }
    public void setValidate(Boolean validate) {
        this.validate = validate;
        if (this.validate == null) {
            this.validate = Boolean.FALSE;
        }
    }
    public SVGOnLoadExceptionTest(){
    }
    public void setId(String id){
        super.setId(id);
        if (id != null) {
            int i = id.indexOf("(");
            if (i != -1) {
                id = id.substring(0, i);
            }
            fileName = "test-resources/org/apache/batik/" + id + ".svg";
            svgURL = resolveURL(fileName);
        }
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
        ApplicationSecurityEnforcer ase
            = new ApplicationSecurityEnforcer(this.getClass(),
                                              "org/apache/batik/apps/svgbrowser/resources/svgbrowser.policy");
        if (secure) {
            ase.enforceSecurity(true);
        }
        try {
            if (!restricted) {
                return testImpl();
            } else {
                Policy policy = Policy.getPolicy();
                URL classesURL = (new File("classes")).toURL();
                CodeSource cs = new CodeSource(classesURL, (Certificate[])null);
                PermissionCollection permissionsOrig
                    = policy.getPermissions(cs);
                Permissions permissions = new Permissions();
                Enumeration iter = permissionsOrig.elements();
                while (iter.hasMoreElements()) {
                    Permission p = (Permission)iter.nextElement();
                    if (!(p instanceof RuntimePermission)) {
                        if (!(p instanceof java.security.AllPermission)) {
                            permissions.add(p);
                        }
                    } else {
                        if (!"createClassLoader".equals(p.getName())) {
                            permissions.add(p);
                        }
                    }
                }
                permissions.add(new FilePermission(fileName, "read"));
                permissions.add(new RuntimePermission("accessDeclaredMembers"));
                ProtectionDomain domain;
                AccessControlContext ctx;
                domain = new ProtectionDomain(null, permissions);
                ctx = new AccessControlContext(new ProtectionDomain[]{domain});
                try {
                    return (TestReport)AccessController.doPrivileged
                        (new PrivilegedExceptionAction() {
                                public Object run() throws Exception {
                                    return testImpl();
                                }
                            }, ctx);
                } catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            }
        } finally {
            ase.enforceSecurity(false);
        }
    }
    protected TestReport testImpl() {
        String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClassName);
        f.setValidating(validate.booleanValue());
        Document doc = null;
        try {
            doc = f.createDocument(svgURL);
        } catch(Exception e){
            e.printStackTrace();
            return handleException(e);
        }
        TestUserAgent userAgent = buildUserAgent();
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        ctx.setDynamic(true);
        Exception e = null;
        try {
            builder.build(ctx, doc);
            BaseScriptingEnvironment scriptEnvironment
                = new BaseScriptingEnvironment(ctx);
            scriptEnvironment.loadScripts();
            scriptEnvironment.dispatchSVGLoadEvent();
        } catch (Exception ex){
            e = ex;
        } finally {
            if (e == null && userAgent.e != null) {
                e = userAgent.e;
            }
            if (e != null) {
                return handleException(e);
            }
        }
        TestReport report = null;
        if (expectedExceptionClass == null) {
            Element elem = doc.getElementById("testResult");
            String s = elem.getAttributeNS(null, "result");
            if (RAN.equals(s)) {
                report = reportSuccess();
            } else {
                report = reportError(ERROR_SCRIPT_DID_NOT_RUN);
                report.addDescriptionEntry(ENTRY_KEY_UNEXPECTED_RESULT,
                                           s);
            }
        }
        if (report == null) {
            report = reportError(ERROR_EXCEPTION_DID_NOT_OCCUR);
            report.addDescriptionEntry(ENTRY_KEY_EXPECTED_EXCEPTION,
                                       expectedExceptionClass);
        }
        return report;
    }
    protected TestReport handleException(Exception e) {
        if (!isMatch(e.getClass(), expectedExceptionClass)) {
            TestReport report = reportError(ERROR_UNEXPECTED_EXCEPTION);
            report.addDescriptionEntry(ENTRY_KEY_UNEXPECTED_EXCEPTION,
                                       e.getClass().getName());
            report.addDescriptionEntry(ENTRY_KEY_EXPECTED_EXCEPTION,
                                       expectedExceptionClass);
            return report;
        } else {
            if (!ERROR_CODE_NO_CHECK.equals(expectedErrorCode)
                && e instanceof BridgeException) {
                if ( !expectedErrorCode.equals(((BridgeException)e).getCode()) ) {
                    TestReport report = reportError(ERROR_UNEXPECTED_ERROR_CODE);
                    report.addDescriptionEntry(ENTRY_KEY_UNEXPECTED_ERROR_CODE,
                                               ((BridgeException)e).getCode());
                    report.addDescriptionEntry(ENTRY_KEY_EXPECTED_ERROR_CODE,
                                               expectedErrorCode);
                    return report;
                }
            }
            return reportSuccess();
        }
    }
    protected boolean isMatch(final Class cl, final String name) {
        if (cl == null) {
            return false;
        } else if (cl.getName().equals(name)) {
            return true;
        } else {
            return isMatch(cl.getSuperclass(), name);
        }
    }
    protected TestUserAgent buildUserAgent(){
        return new TestUserAgent();
    }
    class TestUserAgent extends UserAgentAdapter {
        Exception e;
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourceURL,
                                        ParsedURL docURL) {
            if ("ANY".equals(resourceOrigin)) {
                return new RelaxedExternalResourceSecurity(resourceURL,
                                                           docURL);
            } else if ("DOCUMENT".equals(resourceOrigin)) {
                return new DefaultExternalResourceSecurity(resourceURL,
                                                           docURL);
            } else if ("EMBEDED".equals(resourceOrigin)) {
                return new EmbededExternalResourceSecurity(resourceURL);
            } else {
                return new NoLoadExternalResourceSecurity();
            }
        }
        public ScriptSecurity
            getScriptSecurity(String scriptType,
                              ParsedURL scriptURL,
                              ParsedURL docURL) {
            ScriptSecurity result = null;
            if (scripts.indexOf(scriptType) == -1) {
                result = new NoLoadScriptSecurity(scriptType);
            } else {
                if ("ANY".equals(scriptOrigin)) {
                    result = new RelaxedScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                } else if ("DOCUMENT".equals(scriptOrigin)) {
                    result = new DefaultScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                } else if ("EMBEDED".equals(scriptOrigin)) {
                    result = new EmbededScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                } else {
                    result = new NoLoadScriptSecurity(scriptType);
                }
            }
            return result;
        }
        public void displayError(Exception e) {
            this.e = e;
        }
    }
}
