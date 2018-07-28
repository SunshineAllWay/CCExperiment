package org.apache.lucene.xmlparser.builders;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.apache.lucene.xmlparser.QueryBuilder;
import org.w3c.dom.Element;
import org.apache.lucene.util.Version;
public class UserInputQueryBuilder implements QueryBuilder {
	QueryParser unSafeParser;
	private Analyzer analyzer;
	private String defaultField;
	public UserInputQueryBuilder(QueryParser parser) {
		this.unSafeParser = parser;
	}
	public UserInputQueryBuilder(String defaultField, Analyzer analyzer) {
		this.analyzer = analyzer;
		this.defaultField = defaultField;
	}
	public Query getQuery(Element e) throws ParserException {
		String text=DOMUtils.getText(e);
		try {
			Query q = null;
			if(unSafeParser!=null)
			{
				synchronized (unSafeParser)
				{
					q = unSafeParser.parse(text);
				}
			}
			else
			{
				String fieldName=DOMUtils.getAttribute(e, "fieldName", defaultField);
				QueryParser parser=createQueryParser(fieldName, analyzer);
				q = parser.parse(text);				
			}
			q.setBoost(DOMUtils.getAttribute(e,"boost",1.0f));
			return q;
		} catch (ParseException e1) {
			throw new ParserException(e1.getMessage());
		}
	}
	protected QueryParser createQueryParser(String fieldName, Analyzer analyzer)
	{
		return new QueryParser(Version.LUCENE_CURRENT, fieldName,analyzer);
	}
}
