package org.apache.batik.test.xml;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.batik.test.DefaultTestSuite;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.test.TestSuite;
import org.apache.batik.test.Test;
import org.apache.batik.test.TestFilter;
import org.apache.batik.test.TestException;
import org.apache.batik.test.TestReportProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XMLTestSuiteRunner implements XTRunConstants, XTSConstants{
    public static final String MESSAGE_UNMATCHED_TEST_IDS
        = "XMLTestSuiteRunner.messages.unmatched.test.ids";
    public static final String CANNOT_CREATE_TEST_REPORT_PROCESSOR
        = "xml.XMLTestSuiteRunner.error.cannot.create.test.report.processor";
    public static final String TEST_SUITE_EXCEPTION
        = "xml.XMLTestSuiteRunner.test.suite.exception";
    public static final String TEST_REPORT_PROCESSING_EXCEPTION
        = "xml.XMLTestSuiteRunner.error.test.report.processing.exception";
    public static class AcceptAllTestsFilter implements TestFilter{
        public Test filter(Test t){
            return t;
        }
    }
    public static class IdBasedTestFilter implements TestFilter {
        protected String[] ids;
        protected Set unmatchedIds = new HashSet();
        public IdBasedTestFilter(String[] ids){
            this.ids = ids;
            for(int i=0; i<ids.length; i++){
                unmatchedIds.add(ids[i]);
            }
        }
        public String traceUnusedIds(){
            Object[] ui = unmatchedIds.toArray();
            StringBuffer sb = null;
            if(ui != null && ui.length > 0){
                sb = new StringBuffer();
                sb.append(ui[0].toString());
                for(int i=1; i<ui.length; i++){
                    sb.append(", ");
                    sb.append(ui[i].toString());
                }
            }
            return sb != null ? sb.toString() : null;
        }
        public void filterTestSuite(TestSuite ts){
            Test[] t = ts.getChildrenTests();
            int nTests = t != null ? t.length : 0;
            for(int i=0; i<nTests; i++){
                if(filter(t[i]) == null){
                    ts.removeTest(t[i]);
                }
            }
        }
        public Test filter(Test t){
            String id = t.getQualifiedId();
            boolean isRequested = isRequestedId(id);
            if(t instanceof TestSuite){
                TestSuite ts = (TestSuite)t;
                filterTestSuite(ts);
                if(ts.getChildrenCount() > 0){
                    return t;
                }
                return null;
            }
            if(isRequested){
                return t;
            }
            return null;
        }
        protected boolean isRequestedId(String id){
            for(int i=0; i<ids.length; i++){
                if(ids[i].lastIndexOf(id) == 0){
                    unmatchedIds.remove(ids[i]);
                    return true;
                }
                if(id.lastIndexOf(ids[i]) != -1){
                    unmatchedIds.remove(ids[i]);
                    return true;
                }
            }
            return false;
        }
    }
    protected TestReportProcessor[] extractTestReportProcessor(Element element)
        throws TestException
    {
        List processors = new ArrayList();
        NodeList children = element.getChildNodes();
        if(children != null && children.getLength() > 0){
            int n = children.getLength();
            for(int i=0; i<n; i++){
                Node child = children.item(i);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    Element childElement = (Element)child;
                    String tagName = childElement.getTagName().intern();
                    if(tagName == XTRun_TEST_REPORT_PROCESSOR_TAG){
                        processors.add(buildProcessor(childElement));
                    }
                }
            }
        }
        TestReportProcessor[] p = null;
        if(processors.size() > 0){
            p = new TestReportProcessor[processors.size()];
            processors.toArray(p);
        }
        return p;
    }
    protected TestReportProcessor buildProcessor(Element element)
        throws TestException {
        try{
            return (TestReportProcessor)XMLReflect.buildObject(element);
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(CANNOT_CREATE_TEST_REPORT_PROCESSOR,
                                    new Object[] { element.getAttribute(XR_CLASS_ATTRIBUTE),
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
    protected DefaultTestSuite buildTestRunTestSuite(Element element)
        throws TestException {
        DefaultTestSuite testSuite = new DefaultTestSuite();
        String name = element.getAttribute(XTRun_NAME_ATTRIBUTE);
        testSuite.setName(name);
        String id = element.getAttribute(XTRun_ID_ATTRIBUTE);
        testSuite.setId(id);
        Element[] testSuites
            = getChildrenByTagName(element, XTRun_TEST_SUITE_TAG);
        int n = testSuites != null ? testSuites.length : 0;
        for(int i=0; i<n; i++){
            String suiteHref =
                testSuites[i].getAttribute(XTRun_HREF_ATTRIBUTE);
            Test test = XMLTestSuiteLoader.loadTestSuite(suiteHref, testSuite);
            if(test != null){
                testSuite.addTest(test);
            }
        }
        return testSuite;
    }
    protected Element[] getChildrenByTagName(Element element,
                                             String tagName)
    {
        tagName = tagName.intern();
        List childrenWithTagName = new ArrayList();
        NodeList children = element.getChildNodes();
        if(children != null && children.getLength() > 0){
            int n = children.getLength();
            for(int i=0; i<n; i++){
                Node child = children.item(i);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    Element childElement = (Element)child;
                    String childTagName = childElement.getTagName().intern();
                    if(childTagName == tagName){
                        childrenWithTagName.add(childElement);
                    }
                }
            }
        }
        Element[] a = null;
        if(childrenWithTagName.size() > 0){
            a = new Element[childrenWithTagName.size()];
            childrenWithTagName.toArray(a);
        }
        return a;
    }
    public TestReport run(Document doc, String[] ids)
        throws TestException {
        Element root = doc.getDocumentElement();
        return run(root, ids);
    }
    protected TestReport runTest(Test test)
        throws TestException {
        try{
            return test.run();
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(TEST_SUITE_EXCEPTION,
                                    new Object[] { test.getName(),
                                                   test.getClass().getName(),
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
    protected void processReport(TestReport report,
                                 TestReportProcessor[] processors)
        throws TestException {
        int n = processors.length;
        int i=0;
        try{
            for(; i<n; i++){
                processors[i].processReport(report);
            }
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(TEST_REPORT_PROCESSING_EXCEPTION,
                                    new Object[] { processors[i].getClass().getName(),
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
    protected TestReport run(Element testRunElement, String[] ids)
        throws TestException{
        Test testRun
            = buildTestRunTestSuite(testRunElement);
        Test filteredTestRun = testRun;
        if(ids != null && ids.length > 0){
            IdBasedTestFilter filter = new IdBasedTestFilter(ids);
            filteredTestRun = filter.filter(testRun);
            String unusedIds = filter.traceUnusedIds();
            if(unusedIds != null){
                System.err.println(Messages.formatMessage(MESSAGE_UNMATCHED_TEST_IDS,
                                                          new Object[]{unusedIds}));
            }
        }
        if(filteredTestRun == null){
            DefaultTestReport report
                = new DefaultTestReport(testRun);
            report.setPassed(true);
            return report;
        }
        TestReportProcessor[] processors
            = extractTestReportProcessor(testRunElement);
        TestReport report = runTest(testRun);
        if(processors != null){
            processReport(report, processors);
        }
        return report;
    }
    public static final String USAGE
        = "XMLTestSuiteRunner.messages.error.usage";
    public static final String NOT_A_FILE_TRY_URI
        = "XMLTestSuiteRunner.messages.error.not.a.file.try.uri";
    public static final String COULD_NOT_CONVERT_FILE_NAME_TO_URI
        = "XMLTestSuiteRunner.messages.error.could.not.convert.file.name.to.uri";
    public static final String INVALID_URI
        = "XMLTestSuiteRunner.messages.error.invalid.uri";
    public static final String INVALID_DOCUMENT
        = "XMLTestSuiteRunner.messages.error.invalid.document";
    public static final String ERROR_RUNNING_TEST_SUITE
        = "XMLTestSuiteRunner.messages.error.running.test.suite";
    public static void main(String[] args) {
        if(args.length < 1){
            System.err.println(Messages.formatMessage(USAGE, null));
            System.exit(0);
        }
        String uriStr = args[0];
        String[] ids = new String[args.length - 1];
        System.arraycopy(args, 1, ids, 0, args.length-1);
        File file = new File(uriStr);
        URL url = null;
        if(file.exists()){
            try {
                url = file.toURL();
            }catch(MalformedURLException e){
                System.err.println(Messages.formatMessage(COULD_NOT_CONVERT_FILE_NAME_TO_URI,
                                                            new Object[]{uriStr}));
                System.exit(0);
            }
        }
        else {
            System.err.println(Messages.formatMessage(NOT_A_FILE_TRY_URI,
                                                        new Object[]{uriStr}));
            try{
                url = new URL(uriStr);
            }catch(MalformedURLException e){
                System.err.println(Messages.formatMessage(INVALID_URI,
                                                          new Object[]{uriStr}));
                System.exit(0);
            }
        }
        Document doc = null;
        try{
            System.err.println("Loading document ...");
            DocumentBuilder docBuilder
                = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(url.toString());
        }catch(Exception e){
            e.printStackTrace();
            System.err.println(Messages.formatMessage(INVALID_DOCUMENT,
                                                      new Object[] { uriStr,
                                                                     e.getClass().getName(),
                                                                     e.getMessage() }));
            System.exit(0);
        }
        try{
            System.err.println("Running test run...");
            XMLTestSuiteRunner r = new XMLTestSuiteRunner();
            r.run(doc, ids);
        }catch(TestException e){
            System.err.println(Messages.formatMessage(ERROR_RUNNING_TEST_SUITE,
                                                      new Object[] { e.getMessage() }));
            System.exit(0);
        }
        System.exit(1);
    }
}
