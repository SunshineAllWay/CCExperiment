package org.apache.lucene.xmlparser.builders;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.FilterBuilder;
import org.apache.lucene.xmlparser.ParserException;
import org.w3c.dom.Element;
public class TermsFilterBuilder implements FilterBuilder
{
	Analyzer analyzer;
	public TermsFilterBuilder(Analyzer analyzer)
	{
		this.analyzer = analyzer;
	}
	public Filter getFilter(Element e) throws ParserException
	{
		TermsFilter tf = new TermsFilter();
		String text = DOMUtils.getNonBlankTextOrFail(e);
		String fieldName = DOMUtils.getAttributeWithInheritanceOrFail(e, "fieldName");
		TokenStream ts = analyzer.tokenStream(fieldName, new StringReader(text));
    TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
		try
		{
			Term term = null;
	      while (ts.incrementToken()) {
				if (term == null)
				{
					term = new Term(fieldName, termAtt.term());
				} else
				{
					term = term.createTerm(termAtt.term()); 
				}
				tf.addTerm(term);
			}
		} 
		catch (IOException ioe)
		{
			throw new RuntimeException("Error constructing terms from index:"
					+ ioe);
		}
		return tf;
	}
}
