package org.apache.xerces.dom;
import org.apache.xerces.util.URI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;
public class NotationImpl 
    extends NodeImpl 
    implements Notation {
    static final long serialVersionUID = -764632195890658402L;
    protected String name;
    protected String publicId;
    protected String systemId;
    protected String baseURI;
    public NotationImpl(CoreDocumentImpl ownerDoc, String name) {
    	super(ownerDoc);
        this.name = name;
    }
    public short getNodeType() {
        return Node.NOTATION_NODE;
    }
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }
    public String getPublicId() {
        if (needsSyncData()) {
            synchronizeData();
        }
    	return publicId;
    } 
    public String getSystemId() {
        if (needsSyncData()) {
            synchronizeData();
        }
    	return systemId;
    } 
    public void setPublicId(String id) {
    	if (isReadOnly()) {
    		throw new DOMException(
    		DOMException.NO_MODIFICATION_ALLOWED_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        publicId = id;
    } 
    public void setSystemId(String id) {
    	if(isReadOnly()) {
    		throw new DOMException(
    		DOMException.NO_MODIFICATION_ALLOWED_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }
        if (needsSyncData()) {
            synchronizeData();
        }
    	systemId = id;
    } 
    public String getBaseURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (baseURI != null && baseURI.length() != 0 ) {
            try {
                return new URI(baseURI).toString();
            }
            catch (org.apache.xerces.util.URI.MalformedURIException e){
                return null;
            }
        }
        return baseURI;
    }
    public void setBaseURI(String uri){
        if (needsSyncData()) {
            synchronizeData();
        }
        baseURI = uri;
    }
} 
