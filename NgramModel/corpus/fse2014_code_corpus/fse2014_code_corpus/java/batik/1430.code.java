package org.apache.batik.util.gui.xmleditor;
import java.awt.Graphics;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
public class XMLView extends PlainView {
    protected XMLContext context = null;
    protected XMLScanner lexer = new XMLScanner();
    protected int tabSize = 4;
    public XMLView(XMLContext context, Element elem) {
        super(elem);
        this.context = context;
    }
    public int getTabSize() {
        return tabSize;
    }
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1)
            throws BadLocationException {
        XMLDocument doc = (XMLDocument)getDocument();
        XMLToken token = doc.getScannerStart(p0);
        String str = doc.getText(token.getStartOffset(),
                                 (p1-token.getStartOffset()) + 1);
        lexer.setString(str);
        lexer.reset();
        int pos = token.getStartOffset();
        int ctx = token.getContext();
        int lastCtx = ctx;
        while (pos < p0) {
            pos = lexer.scan(ctx) + token.getStartOffset();
            lastCtx = ctx;
            ctx = lexer.getScanValue();
        }
        int mark = p0;
        while (pos < p1) {
            if (lastCtx != ctx) {
                g.setColor(context.getSyntaxForeground(lastCtx));
                g.setFont(context.getSyntaxFont(lastCtx));
                Segment text = getLineBuffer();
                doc.getText(mark, pos - mark, text);
                x = Utilities.drawTabbedText(text, x, y, g, this, mark);
                mark = pos;
            }
            pos = lexer.scan(ctx) + token.getStartOffset();
            lastCtx = ctx;
            ctx = lexer.getScanValue();
        }
        g.setColor(context.getSyntaxForeground(lastCtx));
        g.setFont(context.getSyntaxFont(lastCtx));
        Segment text = getLineBuffer();
        doc.getText(mark, p1 - mark, text);
        x = Utilities.drawTabbedText(text, x, y, g, this, mark);
        return x;
    }
}
