package org.apache.batik.util.gui.xmleditor;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
public class XMLEditorKit extends DefaultEditorKit {
    public static final String XML_MIME_TYPE = "text/xml";
    protected XMLContext context;
    protected ViewFactory factory = null;
    public XMLEditorKit() {
        this(null);
    }
    public XMLEditorKit(XMLContext context) {
        super();
        factory = new XMLViewFactory();
        if (context == null) {
            this.context = new XMLContext();
        } else {
            this.context = context;
        }
    }
    public XMLContext getStylePreferences() {
        return context;
    }
    public void install(JEditorPane c) {
        super.install(c);
        Object obj = context.getSyntaxFont(XMLContext.DEFAULT_STYLE);
        if (obj != null) {
            c.setFont((Font)obj);
        }
    }
    public String getContentType() {
        return XML_MIME_TYPE;
    }
    public Object clone() {
        XMLEditorKit kit = new XMLEditorKit();
        kit.context = context;
        return kit;
    }
    public Document createDefaultDocument() {
        XMLDocument doc = new XMLDocument(context);
        return doc;
    }
    public ViewFactory getViewFactory() {
        return factory;
    }
    protected class XMLViewFactory implements ViewFactory {
        public View create(Element elem) {
            return new XMLView(context, elem);
        }
    }
}
