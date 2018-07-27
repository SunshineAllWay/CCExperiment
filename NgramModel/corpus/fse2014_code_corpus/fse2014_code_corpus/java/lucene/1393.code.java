package org.apache.lucene.xmlparser.builders;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.w3c.dom.Element;
public class SpanOrTermsBuilder extends SpanBuilderBase
{
    Analyzer analyzer;
    public SpanOrTermsBuilder(Analyzer analyzer)
    {
        super();
        this.analyzer = analyzer;
    }
	public SpanQuery getSpanQuery(Element e) throws ParserException
	{
 		String fieldName=DOMUtils.getAttributeWithInheritanceOrFail(e,"fieldName");
 		String value=DOMUtils.getNonBlankTextOrFail(e);
		try
		{
			ArrayList<SpanQuery> clausesList=new ArrayList<SpanQuery>();
			TokenStream ts=analyzer.tokenStream(fieldName,new StringReader(value));
			TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
	    while (ts.incrementToken()) {
			    SpanTermQuery stq=new SpanTermQuery(new Term(fieldName, termAtt.term()));
			    clausesList.add(stq);
			}
			SpanOrQuery soq=new SpanOrQuery(clausesList.toArray(new SpanQuery[clausesList.size()]));
			soq.setBoost(DOMUtils.getAttribute(e,"boost",1.0f));
			return soq;
		}
		catch(IOException ioe)
		{
		    throw new ParserException("IOException parsing value:"+value);
		}
	}
}
