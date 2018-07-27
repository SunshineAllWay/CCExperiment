package org.apache.xml.serializer;
import javax.xml.transform.Transformer;
import org.w3c.dom.Node;
public interface TransformStateSetter
{
  void setCurrentNode(Node n);
  void resetState(Transformer transformer);
}
