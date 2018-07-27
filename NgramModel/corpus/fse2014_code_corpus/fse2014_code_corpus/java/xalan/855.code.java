package org.apache.xpath.domapi;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathResult;
class XPathResultImpl implements XPathResult, EventListener {
	final private XObject m_resultObj;
	final private XPath m_xpath;
	final private short m_resultType; 
	private boolean m_isInvalidIteratorState = false;
	final private Node m_contextNode;
	private NodeIterator m_iterator = null;;
	private NodeList m_list = null;
	 XPathResultImpl(short type, XObject result, Node contextNode, XPath xpath) {
		if (!isValidType(type)) {
            String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INVALID_XPATH_TYPE, new Object[] {new Integer(type)});       
            throw new XPathException(XPathException.TYPE_ERR,fmsg); 
		}
        if (null == result) {
            String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_EMPTY_XPATH_RESULT, null);       
            throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,fmsg); 
        }
        this.m_resultObj = result;
        this.m_contextNode = contextNode;
        this.m_xpath = xpath;
        if (type == ANY_TYPE) {
            this.m_resultType = getTypeFromXObject(result);
        } else {
            this.m_resultType = type;
        }
        if (((m_resultType == XPathResult.ORDERED_NODE_ITERATOR_TYPE) ||
            (m_resultType == XPathResult.UNORDERED_NODE_ITERATOR_TYPE))) {
            	addEventListener();
        }
        if ((m_resultType == ORDERED_NODE_ITERATOR_TYPE) ||
            (m_resultType == UNORDERED_NODE_ITERATOR_TYPE) ||
            (m_resultType == ANY_UNORDERED_NODE_TYPE) ||
            (m_resultType == FIRST_ORDERED_NODE_TYPE))  {
            try {
                m_iterator = m_resultObj.nodeset();
            } catch (TransformerException te) {
				String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCOMPATIBLE_TYPES, new Object[] {m_xpath.getPatternString(), getTypeString(getTypeFromXObject(m_resultObj)),getTypeString(m_resultType)});       
  			    throw new XPathException(XPathException.TYPE_ERR, fmsg);  
           }
        } else if ((m_resultType == UNORDERED_NODE_SNAPSHOT_TYPE) ||
                   (m_resultType == ORDERED_NODE_SNAPSHOT_TYPE)) {
            try {       	
        	   m_list = m_resultObj.nodelist();
            } catch (TransformerException te) {
				String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCOMPATIBLE_TYPES, new Object[] {m_xpath.getPatternString(), getTypeString(getTypeFromXObject(m_resultObj)),getTypeString(m_resultType)});       
				throw new XPathException(XPathException.TYPE_ERR, fmsg); 
            }
    	}	    	 
	}
	public short getResultType() {
		return m_resultType;
	}
	public double getNumberValue() throws XPathException {
		if (getResultType() != NUMBER_TYPE) {
			String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});		
			throw new XPathException(XPathException.TYPE_ERR,fmsg);
		} else {
			try {
			   return m_resultObj.num();
			} catch (Exception e) {
				throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
			}
		}		
	}
	public String getStringValue() throws XPathException {
		if (getResultType() != STRING_TYPE) {
			String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_STRING, new Object[] {m_xpath.getPatternString(), m_resultObj.getTypeString()});		
			throw new XPathException(XPathException.TYPE_ERR,fmsg);
		} else {
			try {
			   return m_resultObj.str();
			} catch (Exception e) {
				throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
			}
		}
	}
	public boolean getBooleanValue() throws XPathException {
		if (getResultType() != BOOLEAN_TYPE) {
			String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_BOOLEAN, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});		
			throw new XPathException(XPathException.TYPE_ERR,fmsg);
		} else {
			try {
			   return m_resultObj.bool();
			} catch (TransformerException e) {
				throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
			}
		}
	}
	public Node getSingleNodeValue() throws XPathException {
		if ((m_resultType != ANY_UNORDERED_NODE_TYPE) &&
		    (m_resultType != FIRST_ORDERED_NODE_TYPE)) {
				String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_SINGLENODE, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});                        
				throw new XPathException(XPathException.TYPE_ERR,fmsg);
	    }
		NodeIterator result = null;
		try {
			result = m_resultObj.nodeset();
		} catch (TransformerException te) {
			throw new XPathException(XPathException.TYPE_ERR,te.getMessage());
		}
        if (null == result) return null;
        Node node = result.nextNode();
        if (isNamespaceNode(node)) {
            return new XPathNamespaceImpl(node);
        } else {
            return node;
        }        
	}
	public boolean getInvalidIteratorState() {
		return m_isInvalidIteratorState;
	}
	public int getSnapshotLength() throws XPathException {
		if ((m_resultType != UNORDERED_NODE_SNAPSHOT_TYPE) &&
		    (m_resultType != ORDERED_NODE_SNAPSHOT_TYPE)) {
				String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_GET_SNAPSHOT_LENGTH, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});                        
				throw new XPathException(XPathException.TYPE_ERR,fmsg); 
	    }
		return m_list.getLength();
	}
	public Node iterateNext() throws XPathException, DOMException {
		if ((m_resultType != UNORDERED_NODE_ITERATOR_TYPE) &&
		    (m_resultType != ORDERED_NODE_ITERATOR_TYPE)) {
          String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NON_ITERATOR_TYPE, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});                        
		  throw new XPathException(XPathException.TYPE_ERR, fmsg);  
	    }
		if (getInvalidIteratorState()) {
          String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_DOC_MUTATED, null);                        
		  throw new DOMException(DOMException.INVALID_STATE_ERR,fmsg);  
		}			 
        Node node = m_iterator.nextNode();
        if(null == node)
        	removeEventListener(); 
        if (isNamespaceNode(node)) {
            return new XPathNamespaceImpl(node);
        } else {
            return node;
        }
	}
	public Node snapshotItem(int index) throws XPathException {
		if ((m_resultType != UNORDERED_NODE_SNAPSHOT_TYPE) &&
		    (m_resultType != ORDERED_NODE_SNAPSHOT_TYPE)) {
           String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NON_SNAPSHOT_TYPE, new Object[] {m_xpath.getPatternString(), getTypeString(m_resultType)});                        
           throw new XPathException(XPathException.TYPE_ERR, fmsg); 
	    }		
        Node node = m_list.item(index);
        if (isNamespaceNode(node)) {
            return new XPathNamespaceImpl(node);
        } else {
            return node;
        }
	}
	static boolean isValidType( short type ) {
		switch (type) {
			case ANY_TYPE:
			case NUMBER_TYPE:
			case STRING_TYPE:
			case BOOLEAN_TYPE:
			case UNORDERED_NODE_ITERATOR_TYPE:
			case ORDERED_NODE_ITERATOR_TYPE:
			case UNORDERED_NODE_SNAPSHOT_TYPE:
			case ORDERED_NODE_SNAPSHOT_TYPE:
			case ANY_UNORDERED_NODE_TYPE:
			case FIRST_ORDERED_NODE_TYPE: return true;
			default: return false;
		}
	}
	public void handleEvent(Event event) {
		if (event.getType().equals("DOMSubtreeModified")) {
			m_isInvalidIteratorState = true;
			removeEventListener();
		}
	}
  private String getTypeString(int type)
  {
     switch (type) {
      case ANY_TYPE: return "ANY_TYPE";
      case ANY_UNORDERED_NODE_TYPE: return "ANY_UNORDERED_NODE_TYPE";
      case BOOLEAN_TYPE: return "BOOLEAN";
      case FIRST_ORDERED_NODE_TYPE: return "FIRST_ORDERED_NODE_TYPE";
      case NUMBER_TYPE: return "NUMBER_TYPE";
      case ORDERED_NODE_ITERATOR_TYPE: return "ORDERED_NODE_ITERATOR_TYPE";
      case ORDERED_NODE_SNAPSHOT_TYPE: return "ORDERED_NODE_SNAPSHOT_TYPE";
      case STRING_TYPE: return "STRING_TYPE";
      case UNORDERED_NODE_ITERATOR_TYPE: return "UNORDERED_NODE_ITERATOR_TYPE";
      case UNORDERED_NODE_SNAPSHOT_TYPE: return "UNORDERED_NODE_SNAPSHOT_TYPE";
      default: return "#UNKNOWN";
    }
  }  
  private short getTypeFromXObject(XObject object) {
      switch (object.getType()) {
        case XObject.CLASS_BOOLEAN: return BOOLEAN_TYPE;
        case XObject.CLASS_NODESET: return UNORDERED_NODE_ITERATOR_TYPE; 
        case XObject.CLASS_NUMBER: return NUMBER_TYPE;
        case XObject.CLASS_STRING: return STRING_TYPE;
        case XObject.CLASS_RTREEFRAG: return UNORDERED_NODE_ITERATOR_TYPE; 
        case XObject.CLASS_NULL: return ANY_TYPE; 
        default: return ANY_TYPE; 
    }     
  }  
  private boolean isNamespaceNode(Node node) {
     if ((null != node) && 
         (node.getNodeType() == Node.ATTRIBUTE_NODE) &&
         (node.getNodeName().startsWith("xmlns:") || node.getNodeName().equals("xmlns"))) {
        return true;   
     } else {
        return false;
     }
  }
  private void addEventListener(){
  	if(m_contextNode instanceof EventTarget)
		((EventTarget)m_contextNode).addEventListener("DOMSubtreeModified",this,true);
  }
private void removeEventListener(){
	if(m_contextNode instanceof EventTarget)
		((EventTarget)m_contextNode).removeEventListener("DOMSubtreeModified",this,true);
}
}
