package org.apache.lucene.xmlparser.builders;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.FilterBuilder;
import org.apache.lucene.xmlparser.ParserException;
import org.w3c.dom.Element;
public class RangeFilterBuilder implements FilterBuilder {
	public Filter getFilter(Element e) throws ParserException {
		String fieldName=DOMUtils.getAttributeWithInheritance(e,"fieldName");
		String lowerTerm=e.getAttribute("lowerTerm");
		String upperTerm=e.getAttribute("upperTerm");
		boolean includeLower=DOMUtils.getAttribute(e,"includeLower",true);
		boolean includeUpper=DOMUtils.getAttribute(e,"includeUpper",true);
		return new TermRangeFilter(fieldName,lowerTerm,upperTerm,includeLower,includeUpper);
	}
}
