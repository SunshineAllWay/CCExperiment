package org.apache.lucene.search.highlight;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.StringHelper;
public final class QueryTermExtractor
{
	public static final WeightedTerm[] getTerms(Query query) 
	{
		return getTerms(query,false);
	}
	public static final WeightedTerm[] getIdfWeightedTerms(Query query, IndexReader reader, String fieldName) 
	{
	    WeightedTerm[] terms=getTerms(query,false, fieldName);
	    int totalNumDocs=reader.numDocs();
	    for (int i = 0; i < terms.length; i++)
        {
	        try
            {
                int docFreq=reader.docFreq(new Term(fieldName,terms[i].term));
                if(totalNumDocs < docFreq) {
                  docFreq = totalNumDocs;
                }
                float idf=(float)(Math.log((float)totalNumDocs/(double)(docFreq+1)) + 1.0);
                terms[i].weight*=idf;
            } 
	        catch (IOException e)
            {
            }
        }
		return terms;
	}
	public static final WeightedTerm[] getTerms(Query query, boolean prohibited, String fieldName) 
	{
		HashSet<WeightedTerm> terms=new HashSet<WeightedTerm>();
		if(fieldName!=null)
		{
		    fieldName= StringHelper.intern(fieldName);
		}
		getTerms(query,terms,prohibited,fieldName);
		return terms.toArray(new WeightedTerm[0]);
	}
	public static final WeightedTerm[] getTerms(Query query, boolean prohibited) 
	{
	    return getTerms(query,prohibited,null);
	}	
	private static final void getTerms(Query query, HashSet<WeightedTerm> terms,boolean prohibited, String fieldName) 
	{
       	try
       	{
    		if (query instanceof BooleanQuery)
    			getTermsFromBooleanQuery((BooleanQuery) query, terms, prohibited, fieldName);
    		else
    			if(query instanceof FilteredQuery)
    				getTermsFromFilteredQuery((FilteredQuery)query, terms,prohibited, fieldName);
    			else
    		{
	       		HashSet<Term> nonWeightedTerms=new HashSet<Term>();
	       		query.extractTerms(nonWeightedTerms);
	       		for (Iterator<Term> iter = nonWeightedTerms.iterator(); iter.hasNext();)
				{
					Term term = iter.next();
				    if((fieldName==null)||(term.field()==fieldName))
					{
						terms.add(new WeightedTerm(query.getBoost(),term.text()));
					}
				}
    		}
	      }
	      catch(UnsupportedOperationException ignore)
	      {
       	  }		        			        	
	}
	private static final void getTermsFromBooleanQuery(BooleanQuery query, HashSet<WeightedTerm> terms, boolean prohibited, String fieldName)
	{
		BooleanClause[] queryClauses = query.getClauses();
		for (int i = 0; i < queryClauses.length; i++)
		{
			if (prohibited || queryClauses[i].getOccur()!=BooleanClause.Occur.MUST_NOT)
				getTerms(queryClauses[i].getQuery(), terms, prohibited, fieldName);
		}
	}	
	private static void getTermsFromFilteredQuery(FilteredQuery query, HashSet<WeightedTerm> terms, boolean prohibited, String fieldName)
	{
		getTerms(query.getQuery(),terms,prohibited,fieldName);		
	}
}
