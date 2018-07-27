package org.apache.lucene.xmlparser.builders;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.w3c.dom.Element;
public class BoostingTermBuilder extends SpanBuilderBase
{
	public SpanQuery getSpanQuery(Element e) throws ParserException
	{
 		String fieldName=DOMUtils.getAttributeWithInheritanceOrFail(e,"fieldName");
 		String value=DOMUtils.getNonBlankTextOrFail(e);
  		PayloadTermQuery btq = new PayloadTermQuery(new Term(fieldName,value), new AveragePayloadFunction());
  		btq.setBoost(DOMUtils.getAttribute(e,"boost",1.0f));
		return btq;
	}
}