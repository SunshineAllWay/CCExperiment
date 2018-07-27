package org.apache.lucene.xmlparser;
import java.util.HashMap;
import org.apache.lucene.search.Filter;
import org.w3c.dom.Element;
public class FilterBuilderFactory implements FilterBuilder {
	HashMap<String,FilterBuilder> builders=new HashMap<String,FilterBuilder>();
	public Filter getFilter(Element n) throws ParserException {
		FilterBuilder builder= builders.get(n.getNodeName());
		if(builder==null)
		{
			throw new ParserException("No FilterBuilder defined for node "+n.getNodeName()); 
		}
		return builder.getFilter(n); 
	}
	public void addBuilder(String nodeName,FilterBuilder builder)
	{
		builders.put(nodeName,builder);
	}
	public FilterBuilder getFilterBuilder(String nodeName)
	{
		return builders.get(nodeName);		
	}	
}
