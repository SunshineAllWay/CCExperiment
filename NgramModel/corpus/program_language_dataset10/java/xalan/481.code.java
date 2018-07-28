package org.apache.xalan.xsltc.dom;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.runtime.BasisLibrary;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.ref.DTMAxisIteratorBase;
public abstract class MultiValuedNodeHeapIterator extends DTMAxisIteratorBase {
    public abstract class HeapNode implements Cloneable {
	protected int _node, _markedNode;
	protected boolean _isStartSet = false;
	public abstract int step();
	public HeapNode cloneHeapNode() {
            HeapNode clone;
            try {
                clone = (HeapNode) super.clone();
            } catch (CloneNotSupportedException e) {
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                                          e.toString());
                return null;
            }
	    clone._node = _node;
	    clone._markedNode = _node;
	    return clone;
	}
	public void setMark() {
	    _markedNode = _node;
	}
	public void gotoMark() {
	    _node = _markedNode;
	}
        public abstract boolean isLessThan(HeapNode heapNode);
        public abstract HeapNode setStartNode(int node);
        public abstract HeapNode reset();
    } 
    private static final int InitSize = 8;
    private int        _heapSize = 0;
    private int        _size = InitSize;
    private HeapNode[] _heap = new HeapNode[InitSize];
    private int        _free = 0;
    private int _returnedLast;
    private int _cachedReturnedLast = END;
    private int _cachedHeapSize;
    public DTMAxisIterator cloneIterator() {
	_isRestartable = false;
	final HeapNode[] heapCopy = new HeapNode[_heap.length];
	try {
	    MultiValuedNodeHeapIterator clone =
                    (MultiValuedNodeHeapIterator)super.clone();
            for (int i = 0; i < _free; i++) {
                heapCopy[i] = _heap[i].cloneHeapNode();
            }
	    clone.setRestartable(false);
	    clone._heap = heapCopy;
	    return clone.reset();
	} 
	catch (CloneNotSupportedException e) {
	    BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
				      e.toString());
	    return null;
	}
    }
    protected void addHeapNode(HeapNode node) {
	if (_free == _size) {
	    HeapNode[] newArray = new HeapNode[_size *= 2];
	    System.arraycopy(_heap, 0, newArray, 0, _free);
	    _heap = newArray;
	}
	_heapSize++;
	_heap[_free++] = node;
    }
    public int next() {
	while (_heapSize > 0) {
	    final int smallest = _heap[0]._node;
	    if (smallest == END) { 
		if (_heapSize > 1) {
		    final HeapNode temp = _heap[0];
		    _heap[0] = _heap[--_heapSize];
		    _heap[_heapSize] = temp;
		}
		else {
		    return END;
		}
	    }
	    else if (smallest == _returnedLast) {	
		_heap[0].step(); 
	    }
	    else {
		_heap[0].step(); 
		heapify(0);
		return returnNode(_returnedLast = smallest);
	    }
	    heapify(0);
	}
	return END;
    }
    public DTMAxisIterator setStartNode(int node) {
	if (_isRestartable) {
	    _startNode = node;
	    for (int i = 0; i < _free; i++) {
         	if(!_heap[i]._isStartSet){
        	   _heap[i].setStartNode(node);
        	   _heap[i].step();	
        	   _heap[i]._isStartSet = true;
        	}
	    }
	    for (int i = (_heapSize = _free)/2; i >= 0; i--) {
		heapify(i);
	    }
	    _returnedLast = END;
	    return resetPosition();
	}
	return this;
    }
    protected void init() {
        for (int i =0; i < _free; i++) {
            _heap[i] = null;
        }
        _heapSize = 0;
        _free = 0;
    }
    private void heapify(int i) {
	for (int r, l, smallest;;) {
	    r = (i + 1) << 1; l = r - 1;
	    smallest = l < _heapSize 
		&& _heap[l].isLessThan(_heap[i]) ? l : i;
	    if (r < _heapSize && _heap[r].isLessThan(_heap[smallest])) {
		smallest = r;
	    }
	    if (smallest != i) {
		final HeapNode temp = _heap[smallest];
		_heap[smallest] = _heap[i];
		_heap[i] = temp;
		i = smallest;
	    } else {
		break;
            }
	}
    }
    public void setMark() {
	for (int i = 0; i < _free; i++) {
	    _heap[i].setMark();
	}
	_cachedReturnedLast = _returnedLast;    
	_cachedHeapSize = _heapSize;
    }
    public void gotoMark() {
	for (int i = 0; i < _free; i++) {
	    _heap[i].gotoMark();
	}
	for (int i = (_heapSize = _cachedHeapSize)/2; i >= 0; i--) {
	    heapify(i);
	}
        _returnedLast = _cachedReturnedLast;    
    }
    public DTMAxisIterator reset() {
	for (int i = 0; i < _free; i++) {
	    _heap[i].reset();
	    _heap[i].step();
	}
	for (int i = (_heapSize = _free)/2; i >= 0; i--) {
	    heapify(i);
	}
	_returnedLast = END;
	return resetPosition();
    }
}
