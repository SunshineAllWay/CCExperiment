package org.apache.xalan.xsltc.compiler;
import org.xml.sax.InputSource;
public interface SourceLoader {
    public InputSource loadSource(String href, String context, XSLTC xsltc);
}
