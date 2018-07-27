package org.apache.batik.bridge;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
public class BridgeException extends RuntimeException {
    protected Element e;
    protected String code;
    protected String message;
    protected Object [] params;
    protected int line;
    protected GraphicsNode node;
    public BridgeException(BridgeContext ctx, LiveAttributeException ex) {
        switch (ex.getCode()) {
            case LiveAttributeException.ERR_ATTRIBUTE_MISSING:
                this.code = ErrorConstants.ERR_ATTRIBUTE_MISSING;
                break;
            case LiveAttributeException.ERR_ATTRIBUTE_MALFORMED:
                this.code = ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED;
                break;
            case LiveAttributeException.ERR_ATTRIBUTE_NEGATIVE:
                this.code = ErrorConstants.ERR_LENGTH_NEGATIVE;
                break;
            default:
                throw new IllegalStateException
                    ("Unknown LiveAttributeException error code "
                     + ex.getCode());
        }
        this.e = ex.getElement();
        this.params = new Object[] { ex.getAttributeName(), ex.getValue() };
        if (e != null && ctx != null) {
            this.line = ctx.getDocumentLoader().getLineNumber(e);
        }
    }
    public BridgeException(BridgeContext ctx, Element e, String code,
                           Object[] params) {
        this.e = e;
        this.code = code;
        this.params = params;
        if (e != null && ctx != null) {
            this.line = ctx.getDocumentLoader().getLineNumber(e);
        }
    }
    public BridgeException(BridgeContext ctx, Element e, Exception ex, String code,
                           Object[] params) {
        this.e = e;
        message = ex.getMessage();
        this.code = code;
        this.params = params;
        if (e != null && ctx != null) {
            this.line = ctx.getDocumentLoader().getLineNumber(e);
        }
    }
    public BridgeException(BridgeContext ctx, Element e, String message) {
        this.e = e;
        this.message = message;
        if (e != null && ctx != null) {
            this.line = ctx.getDocumentLoader().getLineNumber(e);
        }
    }
    public Element getElement() {
        return e;
    }
    public void setGraphicsNode(GraphicsNode node) {
        this.node = node;
    }
    public GraphicsNode getGraphicsNode() {
        return node;
    }
    public String getMessage() {
        if (message != null) {
            return message;
        }
        String uri;
        String lname = "<Unknown Element>";
        SVGDocument doc = null;
        if (e != null) {
            doc = (SVGDocument)e.getOwnerDocument();
            lname = e.getLocalName();
        }
        if (doc == null)  uri = "<Unknown Document>";
        else              uri = doc.getURL();
        Object [] fullparams = new Object[params.length+3];
        fullparams[0] = uri;
        fullparams[1] = new Integer(line);
        fullparams[2] = lname;
        System.arraycopy( params, 0, fullparams, 3, params.length );
        return Messages.formatMessage(code, fullparams);
    }
    public String getCode() {
        return code;
    }
}
