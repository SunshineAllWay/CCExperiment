package org.apache.xalan.xsltc.dom;
import java.util.StringTokenizer;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.DOMEnhancedForDTM;
import org.apache.xalan.xsltc.runtime.BasisLibrary;
import org.apache.xalan.xsltc.runtime.Hashtable;
import org.apache.xalan.xsltc.util.IntegerArray;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.ref.DTMAxisIteratorBase;
public class KeyIndex extends DTMAxisIteratorBase {
    private Hashtable _index;
    private int _currentDocumentNode = DTM.NULL;
    private Hashtable _rootToIndexMap = new Hashtable();
    private IntegerArray _nodes = null;
    private DOM        _dom;
    private DOMEnhancedForDTM    _enhancedDOM;
    private int _markedPosition = 0;
    public KeyIndex(int dummy) {
    }
    public void setRestartable(boolean flag) {
    }
    public void add(Object value, int node, int rootNode) {
        if (_currentDocumentNode != rootNode) {
            _currentDocumentNode = rootNode;
            _index = new Hashtable();
            _rootToIndexMap.put(new Integer(rootNode), _index);
        }
        IntegerArray nodes = (IntegerArray) _index.get(value);
        if (nodes == null) {
             nodes = new IntegerArray();
            _index.put(value, nodes);
            nodes.add(node);
        } else if (node != nodes.at(nodes.cardinality() - 1)) {
            nodes.add(node);
        }
    }
    public void merge(KeyIndex other) {
	if (other == null) return;
	if (other._nodes != null) {
	    if (_nodes == null) {
		_nodes = (IntegerArray)other._nodes.clone();
	    }
	    else {
		_nodes.merge(other._nodes);
	    }
	}
    }
    public void lookupId(Object value) {
	_nodes = null;
	final StringTokenizer values = new StringTokenizer((String) value,
                                                           " \n\t");
	while (values.hasMoreElements()) {
            final String token = (String) values.nextElement();
	    IntegerArray nodes = (IntegerArray) _index.get(token);
            if (nodes == null && _enhancedDOM != null
                && _enhancedDOM.hasDOMSource()) {
                nodes = getDOMNodeById(token);
            }
	    if (nodes == null) continue;
	    if (_nodes == null) {
		 nodes = (IntegerArray)nodes.clone();
		_nodes = nodes;
	    }
	    else {
		_nodes.merge(nodes);
	    }
	}
    }
    public IntegerArray getDOMNodeById(String id) {
        IntegerArray nodes = null;
        if (_enhancedDOM != null) {
            int ident = _enhancedDOM.getElementById(id);
            if (ident != DTM.NULL) {
                Integer root = new Integer(_enhancedDOM.getDocument());
                Hashtable index = (Hashtable) _rootToIndexMap.get(root);
                if (index == null) {
                    index = new Hashtable();
                    _rootToIndexMap.put(root, index);
                } else {
                    nodes = (IntegerArray) index.get(id);
                }
                if (nodes == null) {
                    nodes = new IntegerArray();
                    index.put(id, nodes);
                }
                nodes.add(_enhancedDOM.getNodeHandle(ident));
            }
        }
        return nodes;
    }
    public void lookupKey(Object value) {
        IntegerArray nodes = (IntegerArray) _index.get(value);
        _nodes = (nodes != null) ? (IntegerArray) nodes.clone() : null;
        _position = 0;
    }
    public int next() {
	if (_nodes == null) return DTMAxisIterator.END;
	return (_position < _nodes.cardinality()) ? 
	    _dom.getNodeHandle(_nodes.at(_position++)) : DTMAxisIterator.END;
    }
    public int containsID(int node, Object value) {
        final String string = (String)value;
        int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                 .setStartNode(node).next();
        Hashtable index =
            (Hashtable) _rootToIndexMap.get(new Integer(rootHandle));
        final StringTokenizer values = new StringTokenizer(string, " \n\t");
        while (values.hasMoreElements()) {
            final String token = (String) values.nextElement();
            IntegerArray nodes = null;
            if (index != null) {
                nodes = (IntegerArray) index.get(token);
            }
            if (nodes == null && _enhancedDOM != null
                && _enhancedDOM.hasDOMSource()) {
                nodes = getDOMNodeById(token);	
            }
            if (nodes != null && nodes.indexOf(node) >= 0) {
                return 1;
            }
        }
        return 0;
    }
    public int containsKey(int node, Object value) {
        int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                 .setStartNode(node).next();
        Hashtable index =
                    (Hashtable) _rootToIndexMap.get(new Integer(rootHandle));
        if (index != null) {
            final IntegerArray nodes = (IntegerArray) index.get(value);
            return (nodes != null && nodes.indexOf(node) >= 0) ? 1 : 0;
        }
        return 0;
    }
    public DTMAxisIterator reset() {
	_position = 0;
	return this;
    }
    public int getLast() {
	return (_nodes == null) ? 0 : _nodes.cardinality();
    }
    public int getPosition() {
	return _position;
    }
    public void setMark() {
	_markedPosition = _position;
    }
    public void gotoMark() {
	_position = _markedPosition;
    }
    public DTMAxisIterator setStartNode(int start) {
	if (start == DTMAxisIterator.END) {
	    _nodes = null;
	}
	else if (_nodes != null) {
	    _position = 0;
	}
	return (DTMAxisIterator) this;
    }
    public int getStartNode() {      
        return 0;
    }
    public boolean isReverse() {
	return(false);
    }
    public DTMAxisIterator cloneIterator() {
	KeyIndex other = new KeyIndex(0);
	other._index = _index;
        other._rootToIndexMap = _rootToIndexMap;
	other._nodes = _nodes;
	other._position = _position;
	return (DTMAxisIterator) other;
    }
    public void setDom(DOM dom) {
    	_dom = dom;
    	if (dom instanceof DOMEnhancedForDTM) {
    	    _enhancedDOM = (DOMEnhancedForDTM)dom;
    	}
    	else if (dom instanceof DOMAdapter) {
    	    DOM idom = ((DOMAdapter)dom).getDOMImpl();
    	    if (idom instanceof DOMEnhancedForDTM) {
    	        _enhancedDOM = (DOMEnhancedForDTM)idom;
    	    }
    	}
    }
    public KeyIndexIterator getKeyIndexIterator(Object keyValue,
                                                boolean isKeyCall) {
        if (keyValue instanceof DTMAxisIterator) {
            return getKeyIndexIterator((DTMAxisIterator) keyValue, isKeyCall);
        } else {
            return getKeyIndexIterator(BasisLibrary.stringF(keyValue, _dom),
                                       isKeyCall);
        }
    }
    public KeyIndexIterator getKeyIndexIterator(String keyValue,
                                                boolean isKeyCall) {
        return new KeyIndexIterator(keyValue, isKeyCall);
    }
    public KeyIndexIterator getKeyIndexIterator(DTMAxisIterator keyValue,
                                                boolean isKeyCall) {
        return new KeyIndexIterator(keyValue, isKeyCall);
    }
    final private static IntegerArray EMPTY_NODES = new IntegerArray(0);
    public class KeyIndexIterator extends MultiValuedNodeHeapIterator {
        private IntegerArray _nodes;
        private DTMAxisIterator _keyValueIterator;
        private String _keyValue;
        private boolean _isKeyIterator;
        protected class KeyIndexHeapNode
                extends MultiValuedNodeHeapIterator.HeapNode
        {
            private IntegerArray _nodes;
            private int _position = 0;
            private int _markPosition = -1;
            KeyIndexHeapNode(IntegerArray nodes) {
                _nodes = nodes;
            }
            public int step() {
                if (_position < _nodes.cardinality()) {
                    _node = _nodes.at(_position);
                    _position++;
                } else {
                    _node = DTMAxisIterator.END;
                }
                return _node;
            }
            public HeapNode cloneHeapNode() {
                KeyIndexHeapNode clone =
                        (KeyIndexHeapNode) super.cloneHeapNode();
                clone._nodes = _nodes;
                clone._position = _position;
                clone._markPosition = _markPosition;
                return clone;
            }
            public void setMark() {
                _markPosition = _position;
            }
            public void gotoMark() {
                _position = _markPosition;
            }
            public boolean isLessThan(HeapNode heapNode) {
                return _node < heapNode._node;
            }
            public HeapNode setStartNode(int node) {
                return this;
            }
            public HeapNode reset() {
                _position = 0;
                return this;
            }
        }
        KeyIndexIterator(String keyValue, boolean isKeyIterator) {
            _isKeyIterator = isKeyIterator;
            _keyValue = keyValue;
        }
        KeyIndexIterator(DTMAxisIterator keyValues, boolean isKeyIterator) {
            _keyValueIterator = keyValues;
            _isKeyIterator = isKeyIterator;
        }
        protected IntegerArray lookupNodes(int root, String keyValue) {
            IntegerArray result = null;
            Hashtable index = (Hashtable)_rootToIndexMap.get(new Integer(root));
            if (!_isKeyIterator) {
                final StringTokenizer values =
                        new StringTokenizer(keyValue, " \n\t");
                while (values.hasMoreElements()) {
                    final String token = (String) values.nextElement();
                    IntegerArray nodes = null;
                    if (index != null) {
                        nodes = (IntegerArray) index.get(token);
                    }
                    if (nodes == null && _enhancedDOM != null
                            && _enhancedDOM.hasDOMSource()) {
                        nodes = getDOMNodeById(token);
                    }
                    if (nodes != null) {
                        if (result == null) {
                            result = (IntegerArray)nodes.clone();
                        } else {
                            result.merge(nodes);
                        }
                    }
                }
            } else if (index != null) {
                result = (IntegerArray) index.get(keyValue);
            }
            return result;
        }
        public DTMAxisIterator setStartNode(int node) {
            _startNode = node;
            if (_keyValueIterator != null) {
                _keyValueIterator = _keyValueIterator.setStartNode(node);
            }
            init();
            return super.setStartNode(node);
        }
        public int next() {
            int nodeHandle;
            if (_nodes != null) {
                if (_position < _nodes.cardinality()) {
                    nodeHandle = returnNode(_nodes.at(_position));
                } else {
                    nodeHandle = DTMAxisIterator.END;
                }
            } else {
                nodeHandle = super.next();
            }
            return nodeHandle;
        }
        public DTMAxisIterator reset() {
            if (_nodes == null) {
                init();
            } else {
                super.reset();
            }
            return resetPosition();
        }
        protected void init() {
            super.init();
            _position = 0;
            int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                      .setStartNode(_startNode).next();
            if (_keyValueIterator == null) {
                _nodes = lookupNodes(rootHandle, _keyValue);
                if (_nodes == null) {
                    _nodes = EMPTY_NODES;
                }
            } else {
                DTMAxisIterator keyValues = _keyValueIterator.reset();
                int retrievedKeyValueIdx = 0;
                boolean foundNodes = false;
                _nodes = null;
                for (int keyValueNode = keyValues.next();
                     keyValueNode != DTMAxisIterator.END;
                     keyValueNode = keyValues.next()) {
                    String keyValue = BasisLibrary.stringF(keyValueNode, _dom);
                    IntegerArray nodes = lookupNodes(rootHandle, keyValue);
                    if (nodes != null) {
                        if (!foundNodes) {
                            _nodes = nodes;
                            foundNodes = true;
                        } else {
                            if (_nodes != null) {
                                addHeapNode(new KeyIndexHeapNode(_nodes));
                                _nodes = null;
                            }
                            addHeapNode(new KeyIndexHeapNode(nodes));
                        }
                    }
                }
                if (!foundNodes) {
                    _nodes = EMPTY_NODES;
                }
            }
        }
        public int getLast() {
            return (_nodes != null) ? _nodes.cardinality() : super.getLast();
        }
        public int getNodeByPosition(int position) {
            int node = DTMAxisIterator.END;
            if (_nodes != null) {
                if (position > 0) {
                    if (position <= _nodes.cardinality()) {
                        _position = position;
                        node = _nodes.at(position-1);
                    } else {
                        _position = _nodes.cardinality();
                    }
                }
            } else {
                node = super.getNodeByPosition(position);
            }
            return node;
        }
    }
}
