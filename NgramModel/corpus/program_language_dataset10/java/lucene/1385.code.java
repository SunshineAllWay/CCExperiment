package org.apache.lucene.xmlparser.builders;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.search.similar.MoreLikeThisQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.apache.lucene.xmlparser.QueryBuilder;
import org.w3c.dom.Element;
public class LikeThisQueryBuilder implements QueryBuilder {
	private Analyzer analyzer;
	String defaultFieldNames [];
	int defaultMaxQueryTerms=20;
	int defaultMinTermFrequency=1;
	float defaultPercentTermsToMatch=30; 
	public LikeThisQueryBuilder(Analyzer analyzer,String [] defaultFieldNames)
	{
		this.analyzer=analyzer;
		this.defaultFieldNames=defaultFieldNames;
	}
	public Query getQuery(Element e) throws ParserException {
		String fieldsList=e.getAttribute("fieldNames"); 
		String fields[]=defaultFieldNames;
		if((fieldsList!=null)&&(fieldsList.trim().length()>0))
		{
			fields=fieldsList.trim().split(",");
			for (int i = 0; i < fields.length; i++) {
				fields[i]=fields[i].trim();
			}
		}
		String stopWords=e.getAttribute("stopWords");
		Set<String> stopWordsSet=null;
		if((stopWords!=null)&&(fields!=null))
		{
		    stopWordsSet=new HashSet<String>();
		    for (int i = 0; i < fields.length; i++)
            {
                TokenStream ts = analyzer.tokenStream(fields[i],new StringReader(stopWords));
                TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
                try
                {
	                while(ts.incrementToken()) {
	                    stopWordsSet.add(termAtt.term());
	                }
                }
                catch(IOException ioe)
                {
                    throw new ParserException("IoException parsing stop words list in "
                            +getClass().getName()+":"+ioe.getLocalizedMessage());
                }
            }
		}
		MoreLikeThisQuery mlt=new MoreLikeThisQuery(DOMUtils.getText(e),fields,analyzer);
		mlt.setMaxQueryTerms(DOMUtils.getAttribute(e,"maxQueryTerms",defaultMaxQueryTerms));
		mlt.setMinTermFrequency(DOMUtils.getAttribute(e,"minTermFrequency",defaultMinTermFrequency));
		mlt.setPercentTermsToMatch(DOMUtils.getAttribute(e,"percentTermsToMatch",defaultPercentTermsToMatch)/100);
		mlt.setStopWords(stopWordsSet);
		int minDocFreq=DOMUtils.getAttribute(e,"minDocFreq",-1);
		if(minDocFreq>=0)
		{
			mlt.setMinDocFreq(minDocFreq);
		}
		mlt.setBoost(DOMUtils.getAttribute(e,"boost",1.0f));
		return mlt;
	}
}
