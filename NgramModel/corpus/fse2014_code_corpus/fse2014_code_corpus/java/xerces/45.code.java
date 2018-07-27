package xni.parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
public class CSVConfiguration
    extends AbstractConfiguration {
    protected static final QName CSV = new QName(null, null, "csv", null);
    protected static final QName ROW = new QName(null, null, "row", null);
    protected static final QName COL = new QName(null, null, "col", null);
    protected static final XMLAttributes EMPTY_ATTRS = new XMLAttributesImpl();
    private final XMLString NEWLINE = new XMLStringBuffer("\n");
    private final XMLString NEWLINE_ONE_SPACE = new XMLStringBuffer("\n ");
    private final XMLString NEWLINE_TWO_SPACES = new XMLStringBuffer("\n  ");
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    public void parse(XMLInputSource source) 
        throws IOException, XNIException {
        openInputSourceStream(source);
        Reader reader = source.getCharacterStream();
        if (reader == null) {
            InputStream stream = source.getByteStream();
            reader = new InputStreamReader(stream);
        }
        BufferedReader bufferedReader = new BufferedReader(reader);
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(null, "UTF-8", new NamespaceSupport(), null);
            fDocumentHandler.xmlDecl("1.0", "UTF-8", "true", null);
            fDocumentHandler.doctypeDecl("csv", null, null, null);
        }
        if (fDTDHandler != null) {
            fDTDHandler.startDTD(null, null);
            fDTDHandler.elementDecl("csv", "(row)*", null);
            fDTDHandler.elementDecl("row", "(col)*", null);
            fDTDHandler.elementDecl("col", "(#PCDATA)", null);
        }
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.startContentModel("csv", null);
            fDTDContentModelHandler.startGroup(null);
            fDTDContentModelHandler.element("row", null);
            fDTDContentModelHandler.endGroup(null);
            short csvOccurs = XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE;
            fDTDContentModelHandler.occurrence(csvOccurs, null);
            fDTDContentModelHandler.endContentModel(null);
            fDTDContentModelHandler.startContentModel("row", null);
            fDTDContentModelHandler.startGroup(null);
            fDTDContentModelHandler.element("col", null);
            fDTDContentModelHandler.endGroup(null);
            short rowOccurs = XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE;
            fDTDContentModelHandler.occurrence(rowOccurs, null);
            fDTDContentModelHandler.endContentModel(null);
            fDTDContentModelHandler.startContentModel("col", null);
            fDTDContentModelHandler.startGroup(null);
            fDTDContentModelHandler.pcdata(null);
            fDTDContentModelHandler.endGroup(null);
            fDTDContentModelHandler.endContentModel(null);
        }
        if (fDTDHandler != null) {
            fDTDHandler.endDTD(null);
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(CSV, EMPTY_ATTRS, null);
        }
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (fDocumentHandler != null) {
                fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
                fDocumentHandler.startElement(ROW, EMPTY_ATTRS, null);
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                while (tokenizer.hasMoreTokens()) {
                    fDocumentHandler.ignorableWhitespace(NEWLINE_TWO_SPACES, null);
                    fDocumentHandler.startElement(COL, EMPTY_ATTRS, null);
                    String token = tokenizer.nextToken();
                    fStringBuffer.clear();
                    fStringBuffer.append(token);
                    fDocumentHandler.characters(fStringBuffer, null);
                    fDocumentHandler.endElement(COL, null);
                }
                fDocumentHandler.ignorableWhitespace(NEWLINE_ONE_SPACE, null);
                fDocumentHandler.endElement(ROW, null);
            }
        }
        bufferedReader.close();
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(NEWLINE, null);
            fDocumentHandler.endElement(CSV, null);
            fDocumentHandler.endDocument(null);
        }
    } 
    public void setFeature(String featureId, boolean state) {}
    public boolean getFeature(String featureId) { return false; }
    public void setProperty(String propertyId, Object value) {}
    public Object getProperty(String propertyId) { return null; }
} 
