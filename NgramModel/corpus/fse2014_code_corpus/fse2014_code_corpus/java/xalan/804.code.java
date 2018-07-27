package org.apache.xpath;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;
public interface WhitespaceStrippingElementMatcher
{
  public boolean shouldStripWhiteSpace(
          XPathContext support, Element targetElement) throws TransformerException;
  public boolean canStripWhiteSpace();
}
