package org.apache.lucene.search;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.PriorityQueue;
public class FuzzyLikeThisQuery extends Query
{
    static Similarity sim=new DefaultSimilarity();
    Query rewrittenQuery=null;
    ArrayList<FieldVals> fieldVals=new ArrayList<FieldVals>();
    Analyzer analyzer;
    ScoreTermQueue q;
    int MAX_VARIANTS_PER_TERM=50;
    boolean ignoreTF=false;
    private int maxNumTerms;
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((analyzer == null) ? 0 : analyzer.hashCode());
      result = prime * result
          + ((fieldVals == null) ? 0 : fieldVals.hashCode());
      result = prime * result + (ignoreTF ? 1231 : 1237);
      result = prime * result + maxNumTerms;
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FuzzyLikeThisQuery other = (FuzzyLikeThisQuery) obj;
      if (analyzer == null) {
        if (other.analyzer != null)
          return false;
      } else if (!analyzer.equals(other.analyzer))
        return false;
      if (fieldVals == null) {
        if (other.fieldVals != null)
          return false;
      } else if (!fieldVals.equals(other.fieldVals))
        return false;
      if (ignoreTF != other.ignoreTF)
        return false;
      if (maxNumTerms != other.maxNumTerms)
        return false;
      return true;
    }
    public FuzzyLikeThisQuery(int maxNumTerms, Analyzer analyzer)
    {
        q=new ScoreTermQueue(maxNumTerms);
        this.analyzer=analyzer;
        this.maxNumTerms = maxNumTerms;
    }
    class FieldVals
    {
    	String queryString;
    	String fieldName;
    	float minSimilarity;
    	int prefixLength;
		public FieldVals(String name, float similarity, int length, String queryString)
		{
			fieldName = name;
			minSimilarity = similarity;
			prefixLength = length;
			this.queryString = queryString;
		}
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((fieldName == null) ? 0 : fieldName.hashCode());
      result = prime * result + Float.floatToIntBits(minSimilarity);
      result = prime * result + prefixLength;
      result = prime * result
          + ((queryString == null) ? 0 : queryString.hashCode());
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FieldVals other = (FieldVals) obj;
      if (fieldName == null) {
        if (other.fieldName != null)
          return false;
      } else if (!fieldName.equals(other.fieldName))
        return false;
      if (Float.floatToIntBits(minSimilarity) != Float
          .floatToIntBits(other.minSimilarity))
        return false;
      if (prefixLength != other.prefixLength)
        return false;
      if (queryString == null) {
        if (other.queryString != null)
          return false;
      } else if (!queryString.equals(other.queryString))
        return false;
      return true;
    }
    }
    public void addTerms(String queryString, String fieldName,float minSimilarity, int prefixLength) 
    {
    	fieldVals.add(new FieldVals(fieldName,minSimilarity,prefixLength,queryString));
    }
    private void addTerms(IndexReader reader,FieldVals f) throws IOException
    {
        if(f.queryString==null) return;
        TokenStream ts=analyzer.tokenStream(f.fieldName,new StringReader(f.queryString));
        TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
        int corpusNumDocs=reader.numDocs();
        Term internSavingTemplateTerm =new Term(f.fieldName); 
        HashSet<String> processedTerms=new HashSet<String>();
        while (ts.incrementToken()) 
        {
                String term = termAtt.term();
        	if(!processedTerms.contains(term))
        	{
        		processedTerms.add(term);
                ScoreTermQueue variantsQ=new ScoreTermQueue(MAX_VARIANTS_PER_TERM); 
                float minScore=0;
                Term startTerm=internSavingTemplateTerm.createTerm(term);
                FuzzyTermEnum fe=new FuzzyTermEnum(reader,startTerm,f.minSimilarity,f.prefixLength);
                TermEnum origEnum = reader.terms(startTerm);
                int df=0;
                if(startTerm.equals(origEnum.term()))
                {
                    df=origEnum.docFreq(); 
                }
                int numVariants=0;
                int totalVariantDocFreqs=0;
                do
                {
                    Term possibleMatch=fe.term();
                    if(possibleMatch!=null)
                    {
    	                numVariants++;
    	                totalVariantDocFreqs+=fe.docFreq();
    	                float score=fe.difference();
    	                if(variantsQ.size() < MAX_VARIANTS_PER_TERM || score > minScore){
    	                    ScoreTerm st=new ScoreTerm(possibleMatch,score,startTerm);                    
    	                    variantsQ.insertWithOverflow(st);
    	                    minScore = variantsQ.top().score; 
    	                }
                    }
                }
                while(fe.next());
                if(numVariants>0)
                {
	                int avgDf=totalVariantDocFreqs/numVariants;
	                if(df==0)
	                {
	                    df=avgDf; 
	                }
	                int size = variantsQ.size();
	                for(int i = 0; i < size; i++)
	                {
	                  ScoreTerm st = variantsQ.pop();
	                  st.score=(st.score*st.score)*sim.idf(df,corpusNumDocs);
	                  q.insertWithOverflow(st);
	                }                            
                }
        	}
        }     
    }
    @Override
    public Query rewrite(IndexReader reader) throws IOException
    {
        if(rewrittenQuery!=null)
        {
            return rewrittenQuery;
        }
        for (Iterator<FieldVals> iter = fieldVals.iterator(); iter.hasNext();)
		{
			FieldVals f = iter.next();
			addTerms(reader,f);			
		}
        fieldVals.clear();
        BooleanQuery bq=new BooleanQuery();
        HashMap<Term,ArrayList<ScoreTerm>> variantQueries=new HashMap<Term,ArrayList<ScoreTerm>>();
        int size = q.size();
        for(int i = 0; i < size; i++)
        {
          ScoreTerm st = q.pop();
          ArrayList<ScoreTerm> l= variantQueries.get(st.fuzziedSourceTerm);
          if(l==null)
          {
              l=new ArrayList<ScoreTerm>();
              variantQueries.put(st.fuzziedSourceTerm,l);
          }
          l.add(st);
        }
        for (Iterator<ArrayList<ScoreTerm>> iter = variantQueries.values().iterator(); iter.hasNext();)
        {
            ArrayList<ScoreTerm> variants = iter.next();
            if(variants.size()==1)
            {
                ScoreTerm st= variants.get(0);
                TermQuery tq = new FuzzyTermQuery(st.term,ignoreTF);
                tq.setBoost(st.score); 
                bq.add(tq, BooleanClause.Occur.SHOULD); 
            }
            else
            {
                BooleanQuery termVariants=new BooleanQuery(true); 
                for (Iterator<ScoreTerm> iterator2 = variants.iterator(); iterator2
                        .hasNext();)
                {
                    ScoreTerm st = iterator2.next();
                    TermQuery tq = new FuzzyTermQuery(st.term,ignoreTF);      
                    tq.setBoost(st.score); 
                    termVariants.add(tq, BooleanClause.Occur.SHOULD);          
                }
                bq.add(termVariants, BooleanClause.Occur.SHOULD);          
            }
        }
        bq.setBoost(getBoost());
        this.rewrittenQuery=bq;
        return bq;
    }
    private static class ScoreTerm{
        public Term term;
        public float score;
        Term fuzziedSourceTerm;
        public ScoreTerm(Term term, float score, Term fuzziedSourceTerm){
          this.term = term;
          this.score = score;
          this.fuzziedSourceTerm=fuzziedSourceTerm;
        }
      }
      private static class ScoreTermQueue extends PriorityQueue<ScoreTerm> {        
        public ScoreTermQueue(int size){
          initialize(size);
        }
        @Override
        protected boolean lessThan(ScoreTerm termA, ScoreTerm termB) {
          if (termA.score== termB.score)
            return termA.term.compareTo(termB.term) > 0;
          else
            return termA.score < termB.score;
        }
      }
      private static class FuzzyTermQuery extends TermQuery
      {
    	  boolean ignoreTF;
          public FuzzyTermQuery(Term t, boolean ignoreTF)
          {
        	  super(t);
        	  this.ignoreTF=ignoreTF;
          }
          @Override
          public Similarity getSimilarity(Searcher searcher)
          {            
              Similarity result = super.getSimilarity(searcher);
              result = new SimilarityDelegator(result) {
                  @Override
                  public float tf(float freq)
                  {
                	  if(ignoreTF)
                	  {
                          return 1; 
                	  }
            		  return super.tf(freq);
                  }
                  @Override
                  public float idf(int docFreq, int numDocs)
                  {
                      return 1;
                  }               
              };
              return result;
          }        
      }
    @Override
    public String toString(String field)
    {
        return null;
    }
	public boolean isIgnoreTF()
	{
		return ignoreTF;
	}
	public void setIgnoreTF(boolean ignoreTF)
	{
		this.ignoreTF = ignoreTF;
	}   
}
