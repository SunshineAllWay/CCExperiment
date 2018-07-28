package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
public class BoostingQuery extends Query {
    private float boost;                            
    private Query match;                            
    private Query context;                          
    public BoostingQuery(Query match, Query context, float boost) {
      this.match = match;
      this.context = (Query)context.clone();        
      this.boost = boost;
      this.context.setBoost(0.0f);                      
    }
    @Override
    public Query rewrite(IndexReader reader) throws IOException {
      BooleanQuery result = new BooleanQuery() {
        @Override
        public Similarity getSimilarity(Searcher searcher) {
          return new DefaultSimilarity() {
            @Override
            public float coord(int overlap, int max) {
              switch (overlap) {
              case 1:                               
                return 1.0f;                        
              case 2:                               
                return boost;                       
              default:
                return 0.0f;
              }
            }
          };
        }
      };
      result.add(match, BooleanClause.Occur.MUST);
      result.add(context, BooleanClause.Occur.SHOULD);
      return result;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Float.floatToIntBits(boost);
      result = prime * result + ((context == null) ? 0 : context.hashCode());
      result = prime * result + ((match == null) ? 0 : match.hashCode());
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
      BoostingQuery other = (BoostingQuery) obj;
      if (Float.floatToIntBits(boost) != Float.floatToIntBits(other.boost))
        return false;
      if (context == null) {
        if (other.context != null)
          return false;
      } else if (!context.equals(other.context))
        return false;
      if (match == null) {
        if (other.match != null)
          return false;
      } else if (!match.equals(other.match))
        return false;
      return true;
    }
    @Override
    public String toString(String field) {
      return match.toString(field) + "/" + context.toString(field);
    }
  }
