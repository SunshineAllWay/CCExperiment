package org.apache.lucene.xmlparser;
import org.apache.lucene.search.Query;
import org.w3c.dom.Element;
public interface QueryBuilder {
	public Query getQuery(Element e) throws ParserException;
}
