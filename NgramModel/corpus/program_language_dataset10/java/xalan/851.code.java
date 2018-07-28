package org.apache.xpath.domapi;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNSResolver;
public final class XPathEvaluatorImpl implements XPathEvaluator {
	private class DummyPrefixResolver implements PrefixResolver {
		DummyPrefixResolver() {}
		public String getNamespaceForPrefix(String prefix, Node context) {
            String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_RESOLVER, null);       
            throw new DOMException(DOMException.NAMESPACE_ERR, fmsg);   
		}
		public String getNamespaceForPrefix(String prefix) {
			return getNamespaceForPrefix(prefix,null);
		}
		public boolean handlesNullPrefixes() {
			return false;
		}
		public String getBaseIdentifier() {
			return null;
		}
	}
    private final Document m_doc;
    public XPathEvaluatorImpl(Document doc) {
        m_doc = doc;
    }
    public XPathEvaluatorImpl() {
            m_doc = null;
    }
	public XPathExpression createExpression(
		String expression,
		XPathNSResolver resolver)
		throws XPathException, DOMException {
		try {
			XPath xpath =  new XPath(expression,null,
			     ((null == resolver) ? new DummyPrefixResolver() : ((PrefixResolver)resolver)), 
			      XPath.SELECT);
            return new XPathExpressionImpl(xpath, m_doc);
		} catch (TransformerException e) {
			if(e instanceof XPathStylesheetDOM3Exception)
				throw new DOMException(DOMException.NAMESPACE_ERR,e.getMessageAndLocation());
			else
				throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,e.getMessageAndLocation());
		}
	}
	public XPathNSResolver createNSResolver(Node nodeResolver) {
		return new XPathNSResolverImpl((nodeResolver.getNodeType() == Node.DOCUMENT_NODE)
	           ? ((Document) nodeResolver).getDocumentElement() : nodeResolver);
	}
	public Object evaluate(
		String expression,
		Node contextNode,
		XPathNSResolver resolver,
		short type,
		Object result)
		throws XPathException, DOMException {
		XPathExpression xpathExpression = createExpression(expression, resolver);
		return	xpathExpression.evaluate(contextNode, type, result);
	}
}
