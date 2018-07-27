package org.apache.xalan.templates;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.functions.FuncCurrent;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.functions.Function;
import org.apache.xpath.operations.Variable;
public class AbsPathChecker extends XPathVisitor
{
	private boolean m_isAbs = true;
	public boolean checkAbsolute(LocPathIterator path)
	{
		m_isAbs = true;
		path.callVisitors(null, this);
		return m_isAbs;
	}
	public boolean visitFunction(ExpressionOwner owner, Function func)
	{
		if((func instanceof FuncCurrent) ||
		   (func instanceof FuncExtFunction))
			m_isAbs = false;
		return true;
	}
	public boolean visitVariableRef(ExpressionOwner owner, Variable var)
	{
		m_isAbs = false;
		return true;
	}
}
