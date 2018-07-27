package org.apache.lucene.xmlparser.builders;
import java.util.Map.Entry;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.FilterBuilder;
import org.apache.lucene.xmlparser.FilterBuilderFactory;
import org.apache.lucene.xmlparser.ParserException;
import org.apache.lucene.xmlparser.QueryBuilder;
import org.apache.lucene.xmlparser.QueryBuilderFactory;
import org.w3c.dom.Element;
public class CachedFilterBuilder implements FilterBuilder {
	private QueryBuilderFactory queryFactory;
	private FilterBuilderFactory filterFactory;
    private  LRUCache<Object,Filter> filterCache = null;
	private int cacheSize;
	public CachedFilterBuilder(QueryBuilderFactory queryFactory, 
			FilterBuilderFactory filterFactory,int cacheSize)
	{
		this.queryFactory=queryFactory;
		this.filterFactory=filterFactory;
		this.cacheSize=cacheSize;
	}
	public synchronized Filter getFilter(Element e) throws ParserException
	{
		Element childElement = DOMUtils.getFirstChildOrFail(e);
		if (filterCache == null)
		{
			filterCache = new LRUCache<Object,Filter>(cacheSize);
		}
		QueryBuilder qb = queryFactory.getQueryBuilder(childElement.getNodeName());
		Object cacheKey = null;
		Query q = null;
		Filter f = null;
		if (qb != null)
		{
			q = qb.getQuery(childElement);
			cacheKey = q;
		} else
		{
			f = filterFactory.getFilter(childElement);
			cacheKey = f;
		}
		Filter cachedFilter = filterCache.get(cacheKey);
		if (cachedFilter != null)
		{
			return cachedFilter; 
		}
		if (qb != null)
		{
			cachedFilter = new QueryWrapperFilter(q);
		} else
		{
			cachedFilter = new CachingWrapperFilter(f);
		}
		filterCache.put(cacheKey, cachedFilter);
		return cachedFilter;
	}
	static class LRUCache<K,V> extends java.util.LinkedHashMap<K,V>
	{
	    public LRUCache(int maxsize)
	    {
	        super(maxsize * 4 / 3 + 1, 0.75f, true);
	        this.maxsize = maxsize;
	    }
	    protected int maxsize;
	    @Override
	    protected boolean removeEldestEntry(Entry<K,V> eldest)
	    {
	        return size() > maxsize;
	    }
	}
}
