package org.apache.batik.svggen;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
class XmlWriter implements SVGConstants {
    private static String EOL;
    private static final String TAG_END = "/>";
    private static final String TAG_START = "</";
    private static final char[] SPACES =
    { ' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',
      ' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ' };
    private static final int    SPACES_LEN = SPACES.length;
    static {
        String  temp;
        try { temp = System.getProperty ("line.separator", "\n"); }
        catch (SecurityException e) { temp = "\n"; }
        EOL = temp;
    }
    static class IndentWriter extends Writer {
        protected Writer proxied;
        protected int    indentLevel;
        protected int    column;
        public IndentWriter(Writer proxied){
            if (proxied == null)
                throw new SVGGraphics2DRuntimeException
                    (ErrorConstants.ERR_PROXY);
            this.proxied = proxied;
        }
        public void setIndentLevel(int indentLevel){
            this.indentLevel = indentLevel;
        }
        public int getIndentLevel(){
            return indentLevel;
        }
        public void printIndent() throws IOException{
            proxied.write(EOL);
            int temp = indentLevel;
            while(temp > 0){
                if (temp > SPACES_LEN) {
                    proxied.write(SPACES, 0, SPACES_LEN);
                    temp -= SPACES_LEN;
                } else {
                    proxied.write(SPACES, 0, temp);
                    break;
                }
            }
            column = indentLevel;
        }
        public Writer getProxied(){
            return proxied;
        }
        public int getColumn() { return column; }
        public void write(int c) throws IOException {
            column++;
            proxied.write(c);
        }
        public void write(char[] cbuf) throws IOException {
            column+=cbuf.length;
            proxied.write(cbuf);
        }
        public void write(char[] cbuf, int off, int len) throws IOException{
            column+=len;
            proxied.write(cbuf, off, len);
        }
        public void write(String str) throws IOException {
            column+=str.length();
            proxied.write(str);
        }
        public void write(String str, int off, int len) throws IOException {
            column+=len;
            proxied.write(str, off, len);
        }
        public void flush() throws IOException{
            proxied.flush();
        }
        public void close() throws IOException{
            column = -1;
            proxied.close();
        }
    }
    private static void writeXml(Attr attr, IndentWriter out,
                                 boolean escaped)
        throws IOException{
        String name = attr.getName();
        out.write (name);
        out.write ("=\"");
        writeChildrenXml(attr, out, escaped);
        out.write ('"');
    }
    private static void writeChildrenXml(Attr attr, IndentWriter out,
                                         boolean escaped)
        throws IOException {
        char[] data = attr.getValue().toCharArray();
        if (data == null) return;
        int         length = data.length;
        int         start=0, last=0;
        while (last < length) {
            char c = data[last];
            switch (c) {
            case '<':
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&lt;");
                break;
            case '>':
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&gt;");
                break;
            case '&':
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&amp;");
                break;
            case '"':
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&quot;");
                break;
            default: 
                if (escaped && (c > 0x007F)) {
                    out.write (data, start, last - start);
                    String hex = "0000"+Integer.toHexString(c);
                    out.write("&#x"+hex.substring(hex.length()-4)+";");
                    start = last + 1;
                }
                break;
            }
            last++;
        }
        out.write (data, start, last - start);
    }
    private static void writeXml(Comment comment, IndentWriter out,
                                 boolean escaped)
        throws IOException {
        char[] data = comment.getData().toCharArray();
        if (data == null) {
            out.write("<!---->");
            return;
        }
        out.write ("<!--");
        boolean     sawDash = false;
        int         length = data.length;
        int         start=0, last=0;
        while (last < length) {
            char c = data[last];
            if (c == '-') {
                if (sawDash) {
                    out.write (data, start, last - start);
                    start = last;
                    out.write (' ');
                }
                sawDash = true;
            } else {
                sawDash = false;
            }
            last++;
        }
        out.write (data, start, last - start);
        if (sawDash)
            out.write (' ');
        out.write ("-->");
    }
    private static void writeXml(Text text, IndentWriter out, boolean escaped)
        throws IOException {
        writeXml(text, out, false, escaped);
    }
    private static void writeXml(Text text, IndentWriter out, boolean trimWS,
                                 boolean escaped)
        throws IOException {
        char[] data = text.getData().toCharArray();
        if (data == null)
            { System.err.println ("Null text data??"); return; }
        int length = data.length;
        int start = 0, last = 0;
        if (trimWS) {
            while (last < length) {
                char c = data[last];
                switch (c) {
                case ' ': case '\t': case '\n': case '\r': last++; continue;
                default: break;
                }
                break;
            }
            start = last;
        }
        while (last < length) {
            char c = data [last];
            switch(c) {
            case ' ': case '\t': case '\n': case '\r':
                if (trimWS) {
                    int wsStart = last; last++;
                    while (last < length) {
                        switch(data[last]) {
                        case ' ': case '\t': case '\n': case '\r':
                            last++; continue;
                        default: break;
                        }
                        break;
                    }
                    if (last == length) {
                        out.write(data, start, wsStart-start);
                        return;
                    } else {
                        continue;
                    }
                }
                break;
            case '<':                     
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&lt;");
                break;
            case '>':                     
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&gt;");
                break;
            case '&':                    
                out.write (data, start, last - start);
                start = last + 1;
                out.write ("&amp;");
                break;
            default: 
                if (escaped && (c > 0x007F)) {
                    out.write (data, start, last - start);
                    String hex = "0000"+Integer.toHexString(c);
                    out.write("&#x"+hex.substring(hex.length()-4)+";");
                    start = last + 1;
                }
                break;
            }
            last++;
        }
        out.write (data, start, last - start);
    }
    private static void writeXml(CDATASection cdataSection, IndentWriter out,
                                 boolean escaped)
        throws IOException {
        char[] data = cdataSection.getData().toCharArray();
        if (data == null) {
            out.write ("<![CDATA[]]>");
            return;
        }
        out.write ("<![CDATA[");
        int length = data.length;
        int  start = 0, last = 0;
        while (last < length) {
            char c = data [last];
            if (c == ']') {
                if (((last + 2) < data.length) &&
                    (data [last + 1] == ']')   &&
                    (data [last + 2] == '>')) {
                    out.write (data, start, last - start);
                    start = last + 1;
                    out.write ("]]]]><![CDATA[>");
                    continue;
                }
            }
            last++;
        }
        out.write (data, start, last - start);
        out.write ("]]>");
    }
    private static void writeXml(Element element, IndentWriter out,
                                 boolean escaped)
        throws IOException, SVGGraphics2DIOException {
        out.write (TAG_START, 0, 1);    
        out.write (element.getTagName());
        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null){
            int nAttr = attributes.getLength();
            for(int i=0; i<nAttr; i++){
                Attr attr = (Attr)attributes.item(i);
                out.write(' ');
                writeXml(attr, out, escaped);
            }
        }
        boolean lastElem = (element.getParentNode().getLastChild()==element);
        if (!element.hasChildNodes()) {
            if (lastElem)
                out.setIndentLevel(out.getIndentLevel()-2);
            out.printIndent ();
            out.write(TAG_END, 0, 2);   
            return;
        }
        Node child = element.getFirstChild();
        out.printIndent ();
        out.write(TAG_END, 1, 1);   
        if ((child.getNodeType() != Node.TEXT_NODE) ||
            (element.getLastChild() != child)) { 
            out.setIndentLevel(out.getIndentLevel()+2);
        }
        writeChildrenXml(element, out, escaped);
        out.write (TAG_START, 0, 2);        
        out.write (element.getTagName());
        if (lastElem)
            out.setIndentLevel(out.getIndentLevel()-2);
        out.printIndent ();
        out.write (TAG_END, 1, 1);  
    }
    private static void writeChildrenXml(Element element, IndentWriter out,
                                         boolean escaped)
        throws IOException, SVGGraphics2DIOException {
        Node child = element.getFirstChild();
        while (child != null) {
            writeXml(child, out, escaped);
            child = child.getNextSibling();
        }
    }
    private static void writeDocumentHeader(IndentWriter out)
        throws IOException {
        String  encoding = null;
        if (out.getProxied() instanceof OutputStreamWriter) {
            OutputStreamWriter osw = (OutputStreamWriter)out.getProxied();
            encoding = java2std(osw.getEncoding());
        }
        out.write ("<?xml version=\"1.0\"");
        if (encoding != null) {
            out.write (" encoding=\"");
            out.write (encoding);
            out.write ('\"');
        }
        out.write ("?>");
        out.write (EOL);
        out.write ("<!DOCTYPE svg PUBLIC '");
        out.write (SVG_PUBLIC_ID);
        out.write ("'"); out.write (EOL);
        out.write ("          '");
        out.write (SVG_SYSTEM_ID);
        out.write ("'"); out.write (">"); out.write (EOL);
    }
    private static void writeXml(Document document, IndentWriter out,
                                 boolean escaped)
        throws IOException, SVGGraphics2DIOException {
        writeDocumentHeader(out);
        NodeList childList = document.getChildNodes();
        writeXml(childList, out, escaped);
    }
    private static void writeXml(NodeList childList, IndentWriter out,
                                 boolean escaped)
        throws IOException, SVGGraphics2DIOException {
        int     length = childList.getLength ();
        if (length == 0)
            return;
        for (int i = 0; i < length; i++) {
            Node child = childList.item(i);
            writeXml(child, out, escaped);
            out.write (EOL);
        }
    }
    static String java2std(String encodingName) {
        if (encodingName == null)
            return null;
        if (encodingName.startsWith ("ISO8859_"))       
            return "ISO-8859-" + encodingName.substring (8);
        if (encodingName.startsWith ("8859_"))          
            return "ISO-8859-" + encodingName.substring (5);
        if ("ASCII7".equalsIgnoreCase (encodingName)
            || "ASCII".equalsIgnoreCase (encodingName))
            return "US-ASCII";
        if ("UTF8".equalsIgnoreCase (encodingName))
            return "UTF-8";
        if (encodingName.startsWith ("Unicode"))
            return "UTF-16";
        if ("SJIS".equalsIgnoreCase (encodingName))
            return "Shift_JIS";
        if ("JIS".equalsIgnoreCase (encodingName))
            return "ISO-2022-JP";
        if ("EUCJIS".equalsIgnoreCase (encodingName))
            return "EUC-JP";
        return "UTF-8";
    }
    public static void writeXml(Node node, Writer writer, boolean escaped)
        throws SVGGraphics2DIOException {
        try {
            IndentWriter out = null;
            if (writer instanceof IndentWriter)
                out = (IndentWriter)writer;
            else
                out = new IndentWriter(writer);
            switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                writeXml((Attr)node, out, escaped);
                break;
            case Node.COMMENT_NODE:
                writeXml((Comment)node, out, escaped);
                break;
            case Node.TEXT_NODE:
                writeXml((Text)node, out, escaped);
                break;
            case Node.CDATA_SECTION_NODE:
                writeXml((CDATASection)node, out, escaped);
                break;
            case Node.DOCUMENT_NODE:
                writeXml((Document)node, out, escaped);
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                writeDocumentHeader(out);
                NodeList childList = node.getChildNodes();
                writeXml(childList, out, escaped);
                break;
            case Node.ELEMENT_NODE:
                writeXml((Element)node, out, escaped);
                break;
            default:
                throw new SVGGraphics2DRuntimeException
                    (ErrorConstants.INVALID_NODE+node.getClass().getName());
            }
        } catch (IOException io) {
            throw new SVGGraphics2DIOException(io);
        }
    }
}
