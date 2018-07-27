package org.apache.xalan.xsltc;
import org.apache.xml.dtm.DTM;
public interface NodeIterator extends Cloneable {
    public static final int END = DTM.NULL;
    public int next();
    public NodeIterator reset();
    public int getLast();
    public int getPosition();
    public void setMark();
    public void gotoMark();
    public NodeIterator setStartNode(int node);
    public boolean isReverse();
    public NodeIterator cloneIterator();
    public void setRestartable(boolean isRestartable);
}
