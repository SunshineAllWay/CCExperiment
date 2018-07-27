package org.apache.xalan.xsltc.runtime;
import java.util.Vector;
public class AttributeList implements org.xml.sax.Attributes {
    private final static String EMPTYSTRING = "";
    private final static String CDATASTRING = "CDATA";
    private Hashtable _attributes;
    private Vector    _names;
    private Vector    _qnames;
    private Vector    _values;
    private Vector    _uris;
    private int       _length;
    public AttributeList() {
	_length = 0;
    }
    public AttributeList(org.xml.sax.Attributes attributes) {
	this();
	if (attributes != null) {
	    final int count = attributes.getLength();
	    for (int i = 0; i < count; i++) {
		add(attributes.getQName(i),attributes.getValue(i));
	    }
	}
    }
    private void alloc() {
	_attributes = new Hashtable();
	_names  = new Vector();
	_values = new Vector();
	_qnames = new Vector();
	_uris   = new Vector();        
    }
    public int getLength() {
	return(_length);
    }
    public String getURI(int index) {
	if (index < _length)
	    return((String)_uris.elementAt(index));
	else
	    return(null);
    }
    public String getLocalName(int index) {
	if (index < _length)
	    return((String)_names.elementAt(index));
	else
	    return(null);
    }
    public String getQName(int pos) {
	if (pos < _length)
	    return((String)_qnames.elementAt(pos));
	else
	    return(null);
    }
    public String getType(int index) {
	return(CDATASTRING);
    }
    public int getIndex(String namespaceURI, String localPart) {
	return(-1);
    }
    public int getIndex(String qname) {
	return(-1);
    }
    public String getType(String uri, String localName) {
	return(CDATASTRING);
    }
    public String getType(String qname) {
	return(CDATASTRING);
    }
    public String getValue(int pos) {
	if (pos < _length)
	    return((String)_values.elementAt(pos));
	else
	    return(null);
    }
    public String getValue(String qname) {
	if (_attributes != null) {
	    final Integer obj = (Integer)_attributes.get(qname);
	    if (obj == null) return null;
	    return(getValue(obj.intValue()));
	}
	else
	    return null;
    }
    public String getValue(String uri, String localName) {
	return(getValue(uri+':'+localName));
    }
    public void add(String qname, String value) {
	if (_attributes == null)
	    alloc();
	Integer obj = (Integer)_attributes.get(qname);
	if (obj == null) {
	    _attributes.put(qname, obj = new Integer(_length++));
	    _qnames.addElement(qname);
	    _values.addElement(value);
	    int col = qname.lastIndexOf(':');
	    if (col > -1) {
		_uris.addElement(qname.substring(0,col));
		_names.addElement(qname.substring(col+1));
	    }
	    else {
		_uris.addElement(EMPTYSTRING);
		_names.addElement(qname);
	    }
	}
	else {
	    final int index = obj.intValue();
	    _values.set(index, value);
	}
    }
    public void clear() {
	_length = 0;
	if (_attributes != null) {
	    _attributes.clear();
	    _names.removeAllElements();
	    _values.removeAllElements();
	    _qnames.removeAllElements();
	    _uris.removeAllElements();
	}
    }
}
