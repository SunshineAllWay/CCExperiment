package org.apache.batik.css.parser;
import java.io.IOException;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
public interface ExtendedParser extends org.w3c.css.sac.Parser {
    void parseStyleDeclaration(String source) 
        throws CSSException, IOException;
    void parseRule(String source) throws CSSException, IOException;
    SelectorList parseSelectors(String source)
        throws CSSException, IOException;
    LexicalUnit parsePropertyValue(String source)
        throws CSSException, IOException;
    SACMediaList parseMedia(String mediaText)
        throws CSSException, IOException;
    boolean parsePriority(String source)
        throws CSSException, IOException;
}
