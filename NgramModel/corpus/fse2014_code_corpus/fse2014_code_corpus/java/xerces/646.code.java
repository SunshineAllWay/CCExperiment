package org.apache.xerces.xinclude;
import java.io.IOException;
import org.apache.xerces.util.XML11Char;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XInclude11TextReader
    extends XIncludeTextReader {
    public XInclude11TextReader(XMLInputSource source, XIncludeHandler handler, int bufferSize)
        throws IOException {
        super(source, handler, bufferSize);
    }
    protected boolean isValid(int ch) {
        return XML11Char.isXML11Valid(ch);
    }
}
