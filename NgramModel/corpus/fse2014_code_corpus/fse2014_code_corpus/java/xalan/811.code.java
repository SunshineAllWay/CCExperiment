package org.apache.xpath;
public interface XPathVisitable
{
	public void callVisitors(ExpressionOwner owner, XPathVisitor visitor);
}
