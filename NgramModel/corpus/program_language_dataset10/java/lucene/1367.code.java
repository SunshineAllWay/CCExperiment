package org.apache.lucene.xmlparser;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.xmlparser.builders.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class CoreParser implements QueryBuilder
{
	protected Analyzer analyzer;
	protected QueryParser parser;
	protected QueryBuilderFactory queryFactory;
	protected FilterBuilderFactory filterFactory;
	public static int maxNumCachedFilters=20;
	public CoreParser(Analyzer analyzer, QueryParser parser)
	{
		this(null,analyzer,parser);
	}
	public CoreParser(String defaultField, Analyzer analyzer)
	{
		this(defaultField,analyzer,null);
	}	
	protected CoreParser(String defaultField,Analyzer analyzer, QueryParser parser)
	{
		this.analyzer=analyzer;
		this.parser=parser;
		filterFactory = new FilterBuilderFactory();
		filterFactory.addBuilder("RangeFilter",new RangeFilterBuilder());
		queryFactory = new QueryBuilderFactory();
		queryFactory.addBuilder("TermQuery",new TermQueryBuilder());
		queryFactory.addBuilder("TermsQuery",new TermsQueryBuilder(analyzer));
		queryFactory.addBuilder("MatchAllDocsQuery",new MatchAllDocsQueryBuilder());
		queryFactory.addBuilder("BooleanQuery",new BooleanQueryBuilder(queryFactory));
		if(parser!=null)
		{
			queryFactory.addBuilder("UserQuery",new UserInputQueryBuilder(parser));
		}
		else
		{
			queryFactory.addBuilder("UserQuery",new UserInputQueryBuilder(defaultField,analyzer));			
		}
		queryFactory.addBuilder("FilteredQuery",new FilteredQueryBuilder(filterFactory,queryFactory));
		queryFactory.addBuilder("ConstantScoreQuery",new ConstantScoreQueryBuilder(filterFactory));
		filterFactory.addBuilder("CachedFilter",new CachedFilterBuilder(queryFactory,
							filterFactory, maxNumCachedFilters));
		SpanQueryBuilderFactory sqof=new SpanQueryBuilderFactory();
		SpanNearBuilder snb=new SpanNearBuilder(sqof);
		sqof.addBuilder("SpanNear",snb);
		queryFactory.addBuilder("SpanNear",snb);
    BoostingTermBuilder btb=new BoostingTermBuilder();
    sqof.addBuilder("BoostingTermQuery",btb);
    queryFactory.addBuilder("BoostingTermQuery",btb);        
    SpanTermBuilder snt=new SpanTermBuilder();
		sqof.addBuilder("SpanTerm",snt);
		queryFactory.addBuilder("SpanTerm",snt);
		SpanOrBuilder sot=new SpanOrBuilder(sqof);
		sqof.addBuilder("SpanOr",sot);
		queryFactory.addBuilder("SpanOr",sot);
		SpanOrTermsBuilder sots=new SpanOrTermsBuilder(analyzer);
		sqof.addBuilder("SpanOrTerms",sots);
		queryFactory.addBuilder("SpanOrTerms",sots);		
		SpanFirstBuilder sft=new SpanFirstBuilder(sqof);
		sqof.addBuilder("SpanFirst",sft);
		queryFactory.addBuilder("SpanFirst",sft);
		SpanNotBuilder snot=new SpanNotBuilder(sqof);
		sqof.addBuilder("SpanNot",snot);
		queryFactory.addBuilder("SpanNot",snot);	
	}
	public Query parse(InputStream xmlStream) throws ParserException
	{
		return getQuery(parseXML(xmlStream).getDocumentElement());
	}
	public void addQueryBuilder(String nodeName,QueryBuilder builder)
	{
		queryFactory.addBuilder(nodeName,builder);
	}
	public void addFilterBuilder(String nodeName,FilterBuilder builder)
	{
		filterFactory.addBuilder(nodeName,builder);
	}
	private static Document parseXML(InputStream pXmlFile) throws ParserException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch (Exception se)
		{
			throw new ParserException("XML Parser configuration error", se);
		}
		org.w3c.dom.Document doc = null;
		try
		{
			doc = db.parse(pXmlFile);
		}
		catch (Exception se)
		{
			throw new ParserException("Error parsing XML stream:" + se, se);
		}
		return doc;
	}
	public Query getQuery(Element e) throws ParserException
	{
		return queryFactory.getQuery(e);
	}
}
