package org.apache.lucene.xmlparser;
import java.util.HashMap;
import org.apache.lucene.search.Query;
import org.w3c.dom.Element;
public class QueryBuilderFactory implements QueryBuilder {
	HashMap<String,QueryBuilder> builders=new HashMap<String,QueryBuilder>();
	public Query getQuery(Element n) throws ParserException {
		QueryBuilder builder= builders.get(n.getNodeName());
		if(builder==null)
		{
			throw new ParserException("No QueryObjectBuilder defined for node "+n.getNodeName()); 
		}
		return builder.getQuery(n); 
	}
	public void addBuilder(String nodeName,QueryBuilder builder)
	{
		builders.put(nodeName,builder);
	}
	public QueryBuilder getQueryBuilder(String nodeName)
	{
		return builders.get(nodeName);		
	}
}
