package org.apache.batik.util.gui.xmleditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
public class XMLDocument extends PlainDocument {
    protected XMLScanner lexer;
    protected XMLContext context;
    protected XMLToken cacheToken = null;
    public XMLDocument() {
        this(new XMLContext());
    }
    public XMLDocument(XMLContext context) {
        this.context = context;
        lexer = new XMLScanner();
    }
    public XMLToken getScannerStart(int pos) throws BadLocationException {
        int ctx = XMLScanner.CHARACTER_DATA_CONTEXT;
        int offset = 0;
        int tokenOffset = 0;
        if (cacheToken != null) {
            if (cacheToken.getStartOffset() > pos) {
                cacheToken = null;
            } else {
                ctx = cacheToken.getContext();
                offset = cacheToken.getStartOffset();
                tokenOffset = offset;
                Element element = getDefaultRootElement();
                int line1 = element.getElementIndex(pos);
                int line2 = element.getElementIndex(offset);
                if (line1 - line2 < 50) {
                    return cacheToken;
                }
            }
        }
        String str = getText(offset, pos - offset);
        lexer.setString(str);
        lexer.reset();
        int lastCtx = ctx;
        int lastOffset = offset;
        while (offset < pos) {
            lastOffset = offset;
            lastCtx = ctx;
            offset = lexer.scan(ctx) + tokenOffset;
            ctx = lexer.getScanValue();
        }
        cacheToken = new XMLToken(lastCtx, lastOffset, offset);
        return cacheToken;
    }
    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException {
        super.insertString(offset, str, a);
        if (cacheToken != null) {
            if (cacheToken.getStartOffset() >= offset) {
                cacheToken = null;
            }
        }
    }
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        if (cacheToken != null) {
            if (cacheToken.getStartOffset() >= offs) {
                cacheToken = null;
            }
        }
    }
    public int find(String str, int fromIndex, boolean caseSensitive)
            throws BadLocationException {
        int offset = -1;
        int startOffset = -1;
        int len = 0;
        int charIndex = 0;
        Element rootElement = getDefaultRootElement();
        int elementIndex = rootElement.getElementIndex(fromIndex);
        if (elementIndex < 0) { return offset; }
        charIndex = fromIndex -
            rootElement.getElement(elementIndex).getStartOffset();
        for (int i = elementIndex; i < rootElement.getElementCount(); i++) {
            Element element = rootElement.getElement(i);
            startOffset = element.getStartOffset();
            if (element.getEndOffset() > getLength()) {
               len = getLength() - startOffset;
            } else {
                len = element.getEndOffset() - startOffset;
            }
            String text = getText(startOffset, len);
            if (!caseSensitive) {
                text = text.toLowerCase();
                str = str.toLowerCase();
            }
            charIndex = text.indexOf(str, charIndex);
            if (charIndex != -1) {
                offset = startOffset + charIndex;
                break;
            }
            charIndex = 0;  
        }
        return offset;
    }
}
