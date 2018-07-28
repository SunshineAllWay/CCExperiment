package org.apache.lucene.search;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.PriorityQueue;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser; 
public abstract class MultiTermQuery extends Query {
  protected RewriteMethod rewriteMethod = CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
  transient int numberOfTerms = 0;
  public static abstract class RewriteMethod implements Serializable {
    public abstract Query rewrite(IndexReader reader, MultiTermQuery query) throws IOException;
  }
  private static final class ConstantScoreFilterRewrite extends RewriteMethod {
    @Override
    public Query rewrite(IndexReader reader, MultiTermQuery query) {
      Query result = new ConstantScoreQuery(new MultiTermQueryWrapperFilter<MultiTermQuery>(query));
      result.setBoost(query.getBoost());
      return result;
    }
    protected Object readResolve() {
      return CONSTANT_SCORE_FILTER_REWRITE;
    }
  }
  public final static RewriteMethod CONSTANT_SCORE_FILTER_REWRITE = new ConstantScoreFilterRewrite();
  private abstract static class BooleanQueryRewrite extends RewriteMethod {
    protected final int collectTerms(IndexReader reader, MultiTermQuery query, TermCollector collector) throws IOException {
      final FilteredTermEnum enumerator = query.getEnum(reader);
      int count = 0;
      try {
        do {
          Term t = enumerator.term();
          if (t != null) {
            if (collector.collect(t, enumerator.difference())) {
              count++;
            } else {
              break;
            }
          }
        } while (enumerator.next());    
      } finally {
        enumerator.close();
      }
      return count;
    }
    protected interface TermCollector {
      boolean collect(Term t, float boost) throws IOException;
    }
  }
  private static class ScoringBooleanQueryRewrite extends BooleanQueryRewrite {
    @Override
    public Query rewrite(final IndexReader reader, final MultiTermQuery query) throws IOException {
      final BooleanQuery result = new BooleanQuery(true);
      query.incTotalNumberOfTerms(collectTerms(reader, query, new TermCollector() {
        public boolean collect(Term t, float boost) {
          TermQuery tq = new TermQuery(t); 
          tq.setBoost(query.getBoost() * boost); 
          result.add(tq, BooleanClause.Occur.SHOULD); 
          return true;
        }
      }));
      return result;
    }
    protected Object readResolve() {
      return SCORING_BOOLEAN_QUERY_REWRITE;
    }
  }
  public final static RewriteMethod SCORING_BOOLEAN_QUERY_REWRITE = new ScoringBooleanQueryRewrite();
  public static abstract class TopTermsBooleanQueryRewrite extends BooleanQueryRewrite {
    private final int size;
    public TopTermsBooleanQueryRewrite(int size) {
      this.size = size;
    }
    public TopTermsBooleanQueryRewrite() {
      this(Integer.MAX_VALUE);
    }
    protected abstract Query getQuery(Term term);
    @Override
    public Query rewrite(IndexReader reader, MultiTermQuery query) throws IOException {
      final int maxSize = Math.min(size, BooleanQuery.getMaxClauseCount());
      final PriorityQueue<ScoreTerm> stQueue = new PriorityQueue<ScoreTerm>();
      collectTerms(reader, query, new TermCollector() {
        public boolean collect(Term t, float boost) {
          if (stQueue.size() >= maxSize && boost <= stQueue.peek().boost)
            return true;
          st.term = t;
          st.boost = boost;
          stQueue.offer(st);
          st = (stQueue.size() > maxSize) ? stQueue.poll() : new ScoreTerm();
          return true;
        }
        private ScoreTerm st = new ScoreTerm();
      });
      final BooleanQuery bq = new BooleanQuery(true);
      for (final ScoreTerm st : stQueue) {
        Query tq = getQuery(st.term);    
        tq.setBoost(query.getBoost() * st.boost); 
        bq.add(tq, BooleanClause.Occur.SHOULD);   
      }
      query.incTotalNumberOfTerms(bq.clauses().size());
      return bq;
    }
    @Override
    public int hashCode() {
      final int prime = 17;
      int result = 1;
      result = prime * result + size;
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      TopTermsBooleanQueryRewrite other = (TopTermsBooleanQueryRewrite) obj;
      if (size != other.size) return false;
      return true;
    }
    private static class ScoreTerm implements Comparable<ScoreTerm> {
      public Term term;
      public float boost;
      public int compareTo(ScoreTerm other) {
        if (this.boost == other.boost)
          return other.term.compareTo(this.term);
        else
          return Float.compare(this.boost, other.boost);
      }
    }
  }
  public static final class TopTermsScoringBooleanQueryRewrite extends
      TopTermsBooleanQueryRewrite {
    public TopTermsScoringBooleanQueryRewrite() {
      super();
    }
    public TopTermsScoringBooleanQueryRewrite(int size) {
      super(size);
    }
    @Override
    protected Query getQuery(Term term) {
      return new TermQuery(term);
    }
  }
  public static final class TopTermsBoostOnlyBooleanQueryRewrite extends
      TopTermsBooleanQueryRewrite {
    public TopTermsBoostOnlyBooleanQueryRewrite() {
      super();
    }
    public TopTermsBoostOnlyBooleanQueryRewrite(int size) {
      super(size);
    }
    @Override
    protected Query getQuery(Term term) {
      return new ConstantScoreQuery(new QueryWrapperFilter(new TermQuery(term)));
    }
  }
  private static class ConstantScoreBooleanQueryRewrite extends ScoringBooleanQueryRewrite implements Serializable {
    @Override
    public Query rewrite(IndexReader reader, MultiTermQuery query) throws IOException {
      Query result = super.rewrite(reader, query);
      assert result instanceof BooleanQuery;
      if (!((BooleanQuery) result).clauses().isEmpty()) {
        result = new ConstantScoreQuery(new QueryWrapperFilter(result));
        result.setBoost(query.getBoost());
      }
      return result;
    }
    @Override
    protected Object readResolve() {
      return CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE;
    }
  }
  public final static RewriteMethod CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE = new ConstantScoreBooleanQueryRewrite();
  public static class ConstantScoreAutoRewrite extends BooleanQueryRewrite {
    public static int DEFAULT_TERM_COUNT_CUTOFF = 350;
    public static double DEFAULT_DOC_COUNT_PERCENT = 0.1;
    private int termCountCutoff = DEFAULT_TERM_COUNT_CUTOFF;
    private double docCountPercent = DEFAULT_DOC_COUNT_PERCENT;
    public void setTermCountCutoff(int count) {
      termCountCutoff = count;
    }
    public int getTermCountCutoff() {
      return termCountCutoff;
    }
    public void setDocCountPercent(double percent) {
      docCountPercent = percent;
    }
    public double getDocCountPercent() {
      return docCountPercent;
    }
    @Override
    public Query rewrite(final IndexReader reader, final MultiTermQuery query) throws IOException {
      final int docCountCutoff = (int) ((docCountPercent / 100.) * reader.maxDoc());
      final int termCountLimit = Math.min(BooleanQuery.getMaxClauseCount(), termCountCutoff);
      final CutOffTermCollector col = new CutOffTermCollector(reader, docCountCutoff, termCountLimit);
      collectTerms(reader, query, col);
      if (col.hasCutOff) {
        return CONSTANT_SCORE_FILTER_REWRITE.rewrite(reader, query);
      } else {
        final Query result;
        if (col.pendingTerms.isEmpty()) {
          result = new BooleanQuery(true);
        } else {
          BooleanQuery bq = new BooleanQuery(true);
          for(Term term : col.pendingTerms) {
            TermQuery tq = new TermQuery(term);
            bq.add(tq, BooleanClause.Occur.SHOULD);
          }
          result = new ConstantScoreQuery(new QueryWrapperFilter(bq));
          result.setBoost(query.getBoost());
        }
        query.incTotalNumberOfTerms(col.pendingTerms.size());
        return result;
      }
    }
    private static final class CutOffTermCollector implements TermCollector {
      CutOffTermCollector(IndexReader reader, int docCountCutoff, int termCountLimit) {
        this.reader = reader;
        this.docCountCutoff = docCountCutoff;
        this.termCountLimit = termCountLimit;
      }
      public boolean collect(Term t, float boost) throws IOException {
        pendingTerms.add(t);
        if (pendingTerms.size() >= termCountLimit || docVisitCount >= docCountCutoff) {
          hasCutOff = true;
          return false;
        }
        docVisitCount += reader.docFreq(t);
        return true;
      }
      int docVisitCount = 0;
      boolean hasCutOff = false;
      final IndexReader reader;
      final int docCountCutoff, termCountLimit;
      final ArrayList<Term> pendingTerms = new ArrayList<Term>();
    }
    @Override
    public int hashCode() {
      final int prime = 1279;
      return (int) (prime * termCountCutoff + Double.doubleToLongBits(docCountPercent));
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ConstantScoreAutoRewrite other = (ConstantScoreAutoRewrite) obj;
      if (other.termCountCutoff != termCountCutoff) {
        return false;
      }
      if (Double.doubleToLongBits(other.docCountPercent) != Double.doubleToLongBits(docCountPercent)) {
        return false;
      }
      return true;
    }
  }
  public final static RewriteMethod CONSTANT_SCORE_AUTO_REWRITE_DEFAULT = new ConstantScoreAutoRewrite() {
    @Override
    public void setTermCountCutoff(int count) {
      throw new UnsupportedOperationException("Please create a private instance");
    }
    @Override
    public void setDocCountPercent(double percent) {
      throw new UnsupportedOperationException("Please create a private instance");
    }
    protected Object readResolve() {
      return CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
    }
  };
  public MultiTermQuery() {
  }
  protected abstract FilteredTermEnum getEnum(IndexReader reader)
      throws IOException;
  public int getTotalNumberOfTerms() {
    return numberOfTerms;
  }
  public void clearTotalNumberOfTerms() {
    numberOfTerms = 0;
  }
  protected void incTotalNumberOfTerms(int inc) {
    numberOfTerms += inc;
  }
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    return rewriteMethod.rewrite(reader, this);
  }
  public RewriteMethod getRewriteMethod() {
    return rewriteMethod;
  }
  public void setRewriteMethod(RewriteMethod method) {
    rewriteMethod = method;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(getBoost());
    result = prime * result;
    result += rewriteMethod.hashCode();
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
    MultiTermQuery other = (MultiTermQuery) obj;
    if (Float.floatToIntBits(getBoost()) != Float.floatToIntBits(other.getBoost()))
      return false;
    if (!rewriteMethod.equals(other.rewriteMethod)) {
      return false;
    }
    return true;
  }
}
