package org.apache.lucene.xmlparser;
import org.apache.lucene.search.Filter;
import org.w3c.dom.Element;
public interface FilterBuilder {
	 public Filter getFilter(Element e) throws ParserException;
}
