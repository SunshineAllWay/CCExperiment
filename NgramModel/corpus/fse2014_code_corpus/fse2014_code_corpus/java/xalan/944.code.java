package org.apache.xpath.operations;
import org.apache.xml.dtm.DTMManager;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
public class VariableSafeAbsRef extends Variable
{
    static final long serialVersionUID = -9174661990819967452L;
  public XObject execute(XPathContext xctxt, boolean destructiveOK) 
  	throws javax.xml.transform.TransformerException
  {
  	XNodeSet xns = (XNodeSet)super.execute(xctxt, destructiveOK);
  	DTMManager dtmMgr = xctxt.getDTMManager();
  	int context = xctxt.getContextNode();
  	if(dtmMgr.getDTM(xns.getRoot()).getDocument() != 
  	   dtmMgr.getDTM(context).getDocument())
  	{
  		Expression expr = (Expression)xns.getContainedIter();
  		xns = (XNodeSet)expr.asIterator(xctxt, context);
  	}
  	return xns;
  }
}
