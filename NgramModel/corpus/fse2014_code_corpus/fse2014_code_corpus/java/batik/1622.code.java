package org.apache.batik.test.xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.batik.test.TestReport;
import org.apache.batik.test.TestReportProcessor;
import org.apache.batik.test.TestSuite;
import org.apache.batik.test.TestException;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMImplementation;
public class XMLTestReportProcessor
    implements TestReportProcessor,
               XTRConstants, XMLConstants {
    public static interface XMLReportConsumer {
        void onNewReport(File xmlReport,
                                File reportDirectory) throws Exception ;
    }
    public static final String ERROR_REPORT_DIRECTORY_UNUSABLE
        = "xml.XMLTestReportProcessor.error.report.directory.unusable";
    public static final String ERROR_REPORT_RESOURCES_DIRECTORY_UNUSABLE
        = "xml.XMLTestReportProcessor.error.report.resources.directory.unusable";
    public static final String XML_TEST_REPORT_DEFAULT_DIRECTORY
        = Messages.formatMessage("XMLTestReportProcessor.config.xml.test.report.default.directory", null);
    public static final String XML_REPORT_DIRECTORY
        = Messages.formatMessage("XMLTestReportProcessor.xml.report.directory", null);
    public static final String XML_RESOURCES_DIRECTORY
        = Messages.formatMessage("XMLTestReportProcessor.xml.resources.directory", null);
    public static final String XML_TEST_REPORT_NAME
        = Messages.formatMessage("XMLTestReportProcessor.config.xml.test.report.name", null);
    protected XMLReportConsumer consumer;
    protected String reportDate;
    protected File reportDirectory;
    protected File xmlDirectory;
    protected File xmlResourcesDirectory;
    public XMLTestReportProcessor(){
    }
    public XMLTestReportProcessor(XMLTestReportProcessor.XMLReportConsumer consumer){
        this.consumer = consumer;
    }
    public void processReport(TestReport report)
        throws TestException {
        initializeReportDirectories();
        try {
            DocumentBuilder docBuilder
                = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            DOMImplementation impl
                = docBuilder.getDOMImplementation();
            Document document = null;
            if(report.getTest() instanceof TestSuite){
                document = impl.createDocument(XTR_NAMESPACE_URI,
                                               XTR_TEST_SUITE_REPORT_TAG, null);
            }
            else {
                document = impl.createDocument(XTR_NAMESPACE_URI,
                                               XTR_TEST_REPORT_TAG, null);
            }
            Element root = document.getDocumentElement();
            root.setAttribute(XTR_DATE_ATTRIBUTE,
                                reportDate);
            processReport(report, root, document);
            File xmlReport = serializeReport(root);
            if(consumer != null){
                consumer.onNewReport(xmlReport, getReportDirectory());
            }
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new TestException(INTERNAL_ERROR,
                                    new Object[] { e.getClass().getName(),
                                                   e.getMessage(),
                                                   sw.toString() },
                                    e);
        }
    }
    public void checkDirectory(File dir,
                               String errorCode)
        throws TestException {
        boolean dirOK = false;
        try{
            if(!dir.exists()){
                dirOK = dir.mkdir();
            }
            else if(dir.isDirectory()){
                dirOK = true;
            }
        }finally{
            if(!dirOK){
                throw new TestException(errorCode,
                                        new Object[] {dir.getAbsolutePath()},
                                        null);
            }
        }
    }
    public void initializeReportDirectories() throws TestException {
        File baseReportDir = new File(XML_TEST_REPORT_DEFAULT_DIRECTORY);
        checkDirectory(baseReportDir, ERROR_REPORT_DIRECTORY_UNUSABLE);
        Calendar c = Calendar.getInstance();
        String dirName = "" + c.get(Calendar.YEAR) + "."
            + makeTwoDigits  (c.get(Calendar.MONTH)+1) + "."
            + makeTwoDigits  (c.get(Calendar.DAY_OF_MONTH)) + "-"
            + makeTwoDigits  (c.get(Calendar.HOUR_OF_DAY)) + "h"
            + makeTwoDigits  (c.get(Calendar.MINUTE)) + "m"
            + makeTwoDigits  (c.get(Calendar.SECOND)) + "s";
        reportDate = dirName;
        reportDirectory = new File(baseReportDir, dirName);
        checkDirectory(reportDirectory, ERROR_REPORT_DIRECTORY_UNUSABLE);
        xmlDirectory = new File(reportDirectory, XML_REPORT_DIRECTORY);
        checkDirectory(xmlDirectory, ERROR_REPORT_DIRECTORY_UNUSABLE);
        xmlResourcesDirectory = new File(xmlDirectory, XML_RESOURCES_DIRECTORY);
        checkDirectory(xmlResourcesDirectory, ERROR_REPORT_DIRECTORY_UNUSABLE);
    }
    protected String makeTwoDigits(int i){
        if(i > 9){
            return "" + i;
        }
        else{
            return "0" + i;
        }
    }
    public File getReportDirectory(){
        return reportDirectory;
    }
    public File getReportResourcesDirectory() {
        return xmlResourcesDirectory;
    }
    protected void processReport(TestReport report,
                                 Element reportElement,
                                 Document reportDocument) throws IOException {
        if(report == null){
            throw new IllegalArgumentException();
        }
        reportElement.setAttribute(XTR_TEST_NAME_ATTRIBUTE,
                                   report.getTest().getName());
        String id = report.getTest().getQualifiedId();
        if( !"".equals(id) ){
            reportElement.setAttribute(XTR_ID_ATTRIBUTE,
                                       id
                                       );
        }
        String status = report.hasPassed()
            ? XTR_PASSED_VALUE
            : XTR_FAILED_VALUE;
        reportElement.setAttribute(XTR_STATUS_ATTRIBUTE,
                                   status);
        String className = report.getTest().getClass().getName();
        reportElement.setAttribute(XTR_CLASS_ATTRIBUTE,
                                   className);
        if(!report.hasPassed()){
            reportElement.setAttribute(XTR_ERROR_CODE_ATTRIBUTE,
                                       report.getErrorCode());
        }
        TestReport.Entry[] entries = report.getDescription();
        int n = entries != null ? entries.length : 0;
        if (n>0) {
            Element descriptionElement
                = reportDocument.createElementNS(null,
                                                 XTR_DESCRIPTION_TAG);
            reportElement.appendChild(descriptionElement);
            for(int i=0; i<n; i++){
                processEntry(entries[i],
                             descriptionElement,
                             reportDocument);
            }
        }
    }
    protected void processEntry(TestReport.Entry entry,
                                Element descriptionElement,
                                Document reportDocument) throws IOException {
        Object value = entry.getValue();
        String key   = entry.getKey();
        if(value instanceof TestReport){
            TestReport report = (TestReport)value;
            Element reportElement = null;
            if(report.getTest() instanceof TestSuite){
                reportElement
                    = reportDocument.createElementNS(XTR_NAMESPACE_URI,
                                                     XTR_TEST_SUITE_REPORT_TAG);
            }
            else{
                reportElement
                    = reportDocument.createElementNS(XTR_NAMESPACE_URI,
                                                     XTR_TEST_REPORT_TAG);
            }
            descriptionElement.appendChild(reportElement);
            processReport((TestReport)entry.getValue(),
                          reportElement,
                          reportDocument);
        }
        else if(value instanceof URL){
            Element entryElement
                = reportDocument.createElementNS(XTR_NAMESPACE_URI,
                                                 XTR_URI_ENTRY_TAG);
            descriptionElement.appendChild(entryElement);
            entryElement.setAttribute(XTR_KEY_ATTRIBUTE,
                                      key.toString());
            entryElement.setAttribute(XTR_VALUE_ATTRIBUTE,
                                      value.toString());
        }
        else if(value instanceof File){
            File tmpFile = (File)value;
            File tmpFileCopy = createResourceFileForName(tmpFile.getName());
            copy(tmpFile, tmpFileCopy);
            Element entryElement
                = reportDocument.createElementNS(XTR_NAMESPACE_URI,
                                                 XTR_FILE_ENTRY_TAG);
            descriptionElement.appendChild(entryElement);
            entryElement.setAttribute(XTR_KEY_ATTRIBUTE,
                                      key.toString());
            entryElement.setAttribute(XTR_VALUE_ATTRIBUTE,
                                      tmpFileCopy.toURL().toString());
        }
        else {
            Element entryElement
                = reportDocument.createElementNS(XTR_NAMESPACE_URI,
                                                 XTR_GENERIC_ENTRY_TAG);
            descriptionElement.appendChild(entryElement);
            entryElement.setAttribute(XTR_KEY_ATTRIBUTE,
                                      key.toString());
            Attr a = reportDocument.createAttribute(XTR_VALUE_ATTRIBUTE);
            a.setValue(value!=null?value.toString():"null");
            entryElement.setAttributeNode(a);
        }
    }
    protected File createResourceFileForName(String fileName){
        File r = new File(xmlResourcesDirectory, fileName);
        if(!r.exists()){
            return r;
        }
        else{
            return createResourceFileForName(fileName, 1);
        }
    }
    protected File createResourceFileForName(String fileName,
                                             int instance){
        int n = fileName.lastIndexOf('.');
        String iFileName = fileName + instance;
        if(n != -1){
            iFileName = fileName.substring(0, n) + instance
                + fileName.substring(n, fileName.length());
        }
        File r = new File(xmlResourcesDirectory, iFileName);
        if(!r.exists()){
            return r;
        }
        else{
            return createResourceFileForName(fileName,
                                             instance + 1);
        }
    }
    protected void copy(File in, File out) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(in));
        OutputStream os = new BufferedOutputStream(new FileOutputStream(out));
        final byte[] b = new byte[1024];
        int n = -1;
        while( (n = is.read(b)) != -1 ){
            os.write(b, 0, n);
        }
        is.close();
        os.close();
    }
    protected File serializeReport(Element reportElement) throws IOException {
        File reportFile = new File(xmlDirectory,
                                   XML_TEST_REPORT_NAME);
        FileWriter fw = new FileWriter(reportFile);
        serializeElement(reportElement,
                         "",
                         fw);
        fw.close();
        return reportFile;
    }
    private static String EOL;
    private static String PROPERTY_LINE_SEPARATOR = "line.separator";
    private static String PROPERTY_LINE_SEPARATOR_DEFAULT = "\n";
    static {
        String  temp;
        try {
            temp = System.getProperty (PROPERTY_LINE_SEPARATOR,
                                       PROPERTY_LINE_SEPARATOR_DEFAULT);
        } catch (SecurityException e) {
            temp = PROPERTY_LINE_SEPARATOR_DEFAULT;
        }
        EOL = temp;
    }
    protected void serializeElement(Element element,
                                    String  prefix,
                                    Writer  writer) throws IOException {
        writer.write(prefix);
        writer.write(XML_OPEN_TAG_START);
        writer.write(element.getTagName());
        serializeAttributes(element,
                            writer);
        NodeList children = element.getChildNodes();
        if(children != null && children.getLength() > 0){
            writer.write(XML_OPEN_TAG_END_CHILDREN);
            writer.write(EOL);
            int n = children.getLength();
            for(int i=0; i<n; i++){
                Node child = children.item(i);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    serializeElement((Element)child,
                                     prefix + XML_TAB,
                                     writer);
                }
            }
            writer.write(prefix);
            writer.write(XML_CLOSE_TAG_START);
            writer.write(element.getTagName());
            writer.write(XML_CLOSE_TAG_END);
        }
        else{
            writer.write(XML_OPEN_TAG_END_NO_CHILDREN);
        }
        writer.write(EOL);
    }
    protected void serializeAttributes(Element element,
                                       Writer  writer) throws IOException{
        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null){
            int nAttr = attributes.getLength();
            for(int i=0; i<nAttr; i++){
                Attr attr = (Attr)attributes.item(i);
                writer.write(XML_SPACE);
                writer.write(attr.getName());
                writer.write(XML_EQUAL_SIGN);
                writer.write(XML_DOUBLE_QUOTE);
                writer.write(encode(attr.getValue()));
                writer.write(XML_DOUBLE_QUOTE);
            }
        }
    }
    protected String encode(String attrValue){
        StringBuffer sb = new StringBuffer(attrValue);
        replace(sb, XML_CHAR_AMP, XML_ENTITY_AMP);
        replace(sb, XML_CHAR_LT, XML_ENTITY_LT);
        replace(sb, XML_CHAR_GT, XML_ENTITY_GT);
        replace(sb, XML_CHAR_QUOT, XML_ENTITY_QUOT);
        replace(sb, XML_CHAR_APOS, XML_ENTITY_APOS);
        return sb.toString();
    }
    protected void replace(StringBuffer s,
                             char c,
                             String r){
        String v = s.toString() + 1;
        int i = v.length();
        while( (i=v.lastIndexOf(c, --i)) != -1 ){
            s.deleteCharAt(i);
            s.insert(i, r);
        }
    }
}
