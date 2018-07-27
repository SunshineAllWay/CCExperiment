package org.apache.lucene.search;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Assert;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
public class CheckHits {
  public static float EXPLAIN_SCORE_TOLERANCE_DELTA = 0.00005f;
  public static void checkNoMatchExplanations(Query q, String defaultFieldName,
                                              Searcher searcher, int[] results)
    throws IOException {
    String d = q.toString(defaultFieldName);
    Set<Integer> ignore = new TreeSet<Integer>();
    for (int i = 0; i < results.length; i++) {
      ignore.add(Integer.valueOf(results[i]));
    }
    int maxDoc = searcher.maxDoc();
    for (int doc = 0; doc < maxDoc; doc++) {
      if (ignore.contains(Integer.valueOf(doc))) continue;
      Explanation exp = searcher.explain(q, doc);
      Assert.assertNotNull("Explanation of [["+d+"]] for #"+doc+" is null",
                             exp);
      Assert.assertEquals("Explanation of [["+d+"]] for #"+doc+
                            " doesn't indicate non-match: " + exp.toString(),
                            0.0f, exp.getValue(), 0.0f);
    }
  }
  public static void checkHitCollector(Query query, String defaultFieldName,
                                       Searcher searcher, int[] results)
    throws IOException {
    QueryUtils.check(query,searcher);
    Set<Integer> correct = new TreeSet<Integer>();
    for (int i = 0; i < results.length; i++) {
      correct.add(Integer.valueOf(results[i]));
    }
    final Set<Integer> actual = new TreeSet<Integer>();
    final Collector c = new SetCollector(actual);
    searcher.search(query, c);
    Assert.assertEquals("Simple: " + query.toString(defaultFieldName), 
                        correct, actual);
    for (int i = -1; i < 2; i++) {
      actual.clear();
      QueryUtils.wrapSearcher(searcher, i).search(query, c);
      Assert.assertEquals("Wrap Searcher " + i + ": " +
                          query.toString(defaultFieldName),
                          correct, actual);
    }
    if ( ! ( searcher instanceof IndexSearcher ) ) return;
    for (int i = -1; i < 2; i++) {
      actual.clear();
      QueryUtils.wrapUnderlyingReader
        ((IndexSearcher)searcher, i).search(query, c);
      Assert.assertEquals("Wrap Reader " + i + ": " +
                          query.toString(defaultFieldName),
                          correct, actual);
    }
  }
  public static class SetCollector extends Collector {
    final Set<Integer> bag;
    public SetCollector(Set<Integer> bag) {
      this.bag = bag;
    }
    private int base = 0;
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) {
      bag.add(Integer.valueOf(doc + base));
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      base = docBase;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
  public static void checkHits(
        Query query,
        String defaultFieldName,
        Searcher searcher,
        int[] results)
          throws IOException {
    ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
    Set<Integer> correct = new TreeSet<Integer>();
    for (int i = 0; i < results.length; i++) {
      correct.add(Integer.valueOf(results[i]));
    }
    Set<Integer> actual = new TreeSet<Integer>();
    for (int i = 0; i < hits.length; i++) {
      actual.add(Integer.valueOf(hits[i].doc));
    }
    Assert.assertEquals(query.toString(defaultFieldName), correct, actual);
    QueryUtils.check(query,searcher);
  }
  public static void checkDocIds(String mes, int[] results, ScoreDoc[] hits)
  throws IOException {
    Assert.assertEquals(mes + " nr of hits", hits.length, results.length);
    for (int i = 0; i < results.length; i++) {
      Assert.assertEquals(mes + " doc nrs for hit " + i, results[i], hits[i].doc);
    }
  }
  public static void checkHitsQuery(
        Query query,
        ScoreDoc[] hits1,
        ScoreDoc[] hits2,
        int[] results)
          throws IOException {
    checkDocIds("hits1", results, hits1);
    checkDocIds("hits2", results, hits2);
    checkEqual(query, hits1, hits2);
  }
  public static void checkEqual(Query query, ScoreDoc[] hits1, ScoreDoc[] hits2) throws IOException {
     final float scoreTolerance = 1.0e-6f;
     if (hits1.length != hits2.length) {
       Assert.fail("Unequal lengths: hits1="+hits1.length+",hits2="+hits2.length);
     }
    for (int i = 0; i < hits1.length; i++) {
      if (hits1[i].doc != hits2[i].doc) {
        Assert.fail("Hit " + i + " docnumbers don't match\n"
                + hits2str(hits1, hits2,0,0)
                + "for query:" + query.toString());
      }
      if ((hits1[i].doc != hits2[i].doc)
          || Math.abs(hits1[i].score -  hits2[i].score) > scoreTolerance)
      {
        Assert.fail("Hit " + i + ", doc nrs " + hits1[i].doc + " and " + hits2[i].doc
                      + "\nunequal       : " + hits1[i].score
                      + "\n           and: " + hits2[i].score
                      + "\nfor query:" + query.toString());
      }
    }
  }
  public static String hits2str(ScoreDoc[] hits1, ScoreDoc[] hits2, int start, int end) throws IOException {
    StringBuilder sb = new StringBuilder();
    int len1=hits1==null ? 0 : hits1.length;
    int len2=hits2==null ? 0 : hits2.length;
    if (end<=0) {
      end = Math.max(len1,len2);
    }
      sb.append("Hits length1=").append(len1).append("\tlength2=").append(len2);
    sb.append('\n');
    for (int i=start; i<end; i++) {
        sb.append("hit=").append(i).append(':');
      if (i<len1) {
          sb.append(" doc").append(hits1[i].doc).append('=').append(hits1[i].score);
      } else {
        sb.append("               ");
      }
      sb.append(",\t");
      if (i<len2) {
        sb.append(" doc").append(hits2[i].doc).append('=').append(hits2[i].score);
      }
      sb.append('\n');
    }
    return sb.toString();
  }
  public static String topdocsString(TopDocs docs, int start, int end) {
    StringBuilder sb = new StringBuilder();
      sb.append("TopDocs totalHits=").append(docs.totalHits).append(" top=").append(docs.scoreDocs.length).append('\n');
    if (end<=0) end=docs.scoreDocs.length;
    else end=Math.min(end,docs.scoreDocs.length);
    for (int i=start; i<end; i++) {
      sb.append('\t');
      sb.append(i);
      sb.append(") doc=");
      sb.append(docs.scoreDocs[i].doc);
      sb.append("\tscore=");
      sb.append(docs.scoreDocs[i].score);
      sb.append('\n');
    }
    return sb.toString();
  }
  public static void checkExplanations(Query query,
                                       String defaultFieldName,
                                       Searcher searcher) throws IOException {
    checkExplanations(query, defaultFieldName, searcher, false);
  }
  public static void checkExplanations(Query query,
                                       String defaultFieldName,
                                       Searcher searcher, 
                                       boolean deep) throws IOException {
    searcher.search(query,
                    new ExplanationAsserter
                    (query, defaultFieldName, searcher, deep));
  }
  public static void verifyExplanation(String q, 
                                       int doc, 
                                       float score,
                                       boolean deep,
                                       Explanation expl) {
    float value = expl.getValue();
    Assert.assertEquals(q+": score(doc="+doc+")="+score+
        " != explanationScore="+value+" Explanation: "+expl,
        score,value,EXPLAIN_SCORE_TOLERANCE_DELTA);
    if (!deep) return;
    Explanation detail[] = expl.getDetails();
    if (detail!=null) {
      if (detail.length==1) {
        verifyExplanation(q,doc,score,deep,detail[0]);
      } else {
        float x = 0;
        String descr = expl.getDescription().toLowerCase();
        boolean productOf = descr.endsWith("product of:");
        boolean sumOf = descr.endsWith("sum of:");
        boolean maxOf = descr.endsWith("max of:");
        boolean maxTimesOthers = false;
        if (!(productOf || sumOf || maxOf)) {
          int k1 = descr.indexOf("max plus ");
          if (k1>=0) {
            k1 += "max plus ".length();
            int k2 = descr.indexOf(" ",k1);
            try {
              x = Float.parseFloat(descr.substring(k1,k2).trim());
              if (descr.substring(k2).trim().equals("times others of:")) {
                maxTimesOthers = true;
              }
            } catch (NumberFormatException e) {
            }
          }
        }
        Assert.assertTrue(
            q+": multi valued explanation description=\""+descr
            +"\" must be 'max of plus x times others' or end with 'product of'"
            +" or 'sum of:' or 'max of:' - "+expl,
            productOf || sumOf || maxOf || maxTimesOthers);
        float sum = 0;
        float product = 1;
        float max = 0;
        for (int i=0; i<detail.length; i++) {
          float dval = detail[i].getValue();
          verifyExplanation(q,doc,dval,deep,detail[i]);
          product *= dval;
          sum += dval;
          max = Math.max(max,dval);
        }
        float combined = 0;
        if (productOf) {
          combined = product;
        } else if (sumOf) {
          combined = sum;
        } else if (maxOf) {
          combined = max;
        } else if (maxTimesOthers) {
          combined = max + x * (sum - max);
        } else {
            Assert.assertTrue("should never get here!",false);
        }
        Assert.assertEquals(q+": actual subDetails combined=="+combined+
            " != value="+value+" Explanation: "+expl,
            combined,value,EXPLAIN_SCORE_TOLERANCE_DELTA);
      }
    }
  }
  public static class ExplanationAssertingSearcher extends IndexSearcher {
    public ExplanationAssertingSearcher(Directory d) throws IOException {
      super(d, true);
    }
    public ExplanationAssertingSearcher(IndexReader r) throws IOException {
      super(r);
    }
    protected void checkExplanations(Query q) throws IOException {
      super.search(q, null,
                   new ExplanationAsserter
                   (q, null, this));
    }
    @Override
    public TopFieldDocs search(Query query,
                               Filter filter,
                               int n,
                               Sort sort) throws IOException {
      checkExplanations(query);
      return super.search(query,filter,n,sort);
    }
    @Override
    public void search(Query query, Collector results) throws IOException {
      checkExplanations(query);
      super.search(query, results);
    }
    @Override
    public void search(Query query, Filter filter, Collector results) throws IOException {
      checkExplanations(query);
      super.search(query, filter, results);
    }
    @Override
    public TopDocs search(Query query, Filter filter,
                          int n) throws IOException {
      checkExplanations(query);
      return super.search(query,filter, n);
    }
  }
  public static class ExplanationAsserter extends Collector {
    @Deprecated
    public static float SCORE_TOLERANCE_DELTA = 0.00005f;
    Query q;
    Searcher s;
    String d;
    boolean deep;
    Scorer scorer;
    private int base = 0;
    public ExplanationAsserter(Query q, String defaultFieldName, Searcher s) {
      this(q,defaultFieldName,s,false);
    }      
    public ExplanationAsserter(Query q, String defaultFieldName, Searcher s, boolean deep) {
      this.q=q;
      this.s=s;
      this.d = q.toString(defaultFieldName);
      this.deep=deep;
    }      
    @Override
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;     
    }
    @Override
    public void collect(int doc) throws IOException {
      Explanation exp = null;
      doc = doc + base;
      try {
        exp = s.explain(q, doc);
      } catch (IOException e) {
        throw new RuntimeException
          ("exception in hitcollector of [["+d+"]] for #"+doc, e);
      }
      Assert.assertNotNull("Explanation of [["+d+"]] for #"+doc+" is null", exp);
      verifyExplanation(d,doc,scorer.score(),deep,exp);
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      base = docBase;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
}
