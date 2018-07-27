package org.apache.lucene.xmlparser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.xmlparser.builders.BooleanFilterBuilder;
import org.apache.lucene.xmlparser.builders.BoostingQueryBuilder;
import org.apache.lucene.xmlparser.builders.DuplicateFilterBuilder;
import org.apache.lucene.xmlparser.builders.FuzzyLikeThisQueryBuilder;
import org.apache.lucene.xmlparser.builders.LikeThisQueryBuilder;
import org.apache.lucene.xmlparser.builders.TermsFilterBuilder;
public class CorePlusExtensionsParser extends CoreParser
{
	public CorePlusExtensionsParser(Analyzer analyzer, QueryParser parser)
	{
		this(null,analyzer, parser);
	}
	public CorePlusExtensionsParser(String defaultField,Analyzer analyzer)
	{
		this(defaultField,analyzer, null);
	}
	private CorePlusExtensionsParser(String defaultField,Analyzer analyzer, QueryParser parser)
	{
		super(defaultField,analyzer, parser);
		filterFactory.addBuilder("TermsFilter",new TermsFilterBuilder(analyzer));
		filterFactory.addBuilder("BooleanFilter",new BooleanFilterBuilder(filterFactory));
		filterFactory.addBuilder("DuplicateFilter",new DuplicateFilterBuilder());
		String fields[]={"contents"};
		queryFactory.addBuilder("LikeThisQuery",new LikeThisQueryBuilder(analyzer,fields));
		queryFactory.addBuilder("BoostingQuery", new BoostingQueryBuilder(queryFactory));
		queryFactory.addBuilder("FuzzyLikeThisQuery", new FuzzyLikeThisQueryBuilder(analyzer));
	}
}
