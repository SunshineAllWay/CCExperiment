package org.apache.xml.dtm;
public interface DTMAxisIterator extends Cloneable
{
  public static final int END = DTM.NULL;
  public int next();  
  public DTMAxisIterator reset();
  public int getLast();
  public int getPosition();
  public void setMark();
  public void gotoMark();
  public DTMAxisIterator setStartNode(int node);
  public int getStartNode();
  public boolean isReverse();
  public DTMAxisIterator cloneIterator();
  public void setRestartable(boolean isRestartable);
  public int getNodeByPosition(int position);
}
