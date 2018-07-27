package org.apache.lucene.search;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.LuceneTestCase;
import java.util.Random;
import java.util.BitSet;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class TestScorerPerf extends LuceneTestCase {
  Random r;
  boolean validate = true;  
  BitSet[] sets;
  Term[] terms;
  IndexSearcher s;
  public void createDummySearcher() throws Exception {
    RAMDirectory rd = new RAMDirectory();
    IndexWriter iw = new IndexWriter(rd, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    iw.addDocument(new Document());
    iw.close();
    s = new IndexSearcher(rd, true);
  }
  public void createRandomTerms(int nDocs, int nTerms, double power, Directory dir) throws Exception {
    int[] freq = new int[nTerms];
    terms = new Term[nTerms];
    for (int i=0; i<nTerms; i++) {
      int f = (nTerms+1)-i;  
      freq[i] = (int)Math.ceil(Math.pow(f,power));
      terms[i] = new Term("f",Character.toString((char)('A'+i)));
    }
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    for (int i=0; i<nDocs; i++) {
      Document d = new Document();
      for (int j=0; j<nTerms; j++) {
        if (r.nextInt(freq[j]) == 0) {
          d.add(new Field("f", terms[j].text(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
      }
      iw.addDocument(d);
    }
    iw.optimize();
    iw.close();
  }
  public BitSet randBitSet(int sz, int numBitsToSet) {
    BitSet set = new BitSet(sz);
    for (int i=0; i<numBitsToSet; i++) {
      set.set(r.nextInt(sz));
    }
    return set;
  }
  public BitSet[] randBitSets(int numSets, int setSize) {
    BitSet[] sets = new BitSet[numSets];
    for (int i=0; i<sets.length; i++) {
      sets[i] = randBitSet(setSize, r.nextInt(setSize));
    }
    return sets;
  }
  public static class CountingHitCollector extends Collector {
    int count=0;
    int sum=0;
    protected int docBase = 0;
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) {
      count++;
      sum += docBase + doc;  
    }
    public int getCount() { return count; }
    public int getSum() { return sum; }
    @Override
    public void setNextReader(IndexReader reader, int base) {
      docBase = base;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
  public static class MatchingHitCollector extends CountingHitCollector {
    BitSet answer;
    int pos=-1;
    public MatchingHitCollector(BitSet answer) {
      this.answer = answer;
    }
    public void collect(int doc, float score) {
      pos = answer.nextSetBit(pos+1);
      if (pos != doc + docBase) {
        throw new RuntimeException("Expected doc " + pos + " but got " + doc + docBase);
      }
      super.collect(doc);
    }
  }
  BitSet addClause(BooleanQuery bq, BitSet result) {
    final BitSet rnd = sets[r.nextInt(sets.length)];
    Query q = new ConstantScoreQuery(new Filter() {
      @Override
      public DocIdSet getDocIdSet(IndexReader reader) {
        return new DocIdBitSet(rnd);
      }
    });
    bq.add(q, BooleanClause.Occur.MUST);
    if (validate) {
      if (result==null) result = (BitSet)rnd.clone();
      else result.and(rnd);
    }
    return result;
  }
  public int doConjunctions(int iter, int maxClauses) throws IOException {
    int ret=0;
    for (int i=0; i<iter; i++) {
      int nClauses = r.nextInt(maxClauses-1)+2; 
      BooleanQuery bq = new BooleanQuery();
      BitSet result=null;
      for (int j=0; j<nClauses; j++) {
        result = addClause(bq,result);
      }
      CountingHitCollector hc = validate ? new MatchingHitCollector(result)
                                         : new CountingHitCollector();
      s.search(bq, hc);
      ret += hc.getSum();
      if (validate) assertEquals(result.cardinality(), hc.getCount());
    }
    return ret;
  }
  public int doNestedConjunctions(int iter, int maxOuterClauses, int maxClauses) throws IOException {
    int ret=0;
    long nMatches=0;
    for (int i=0; i<iter; i++) {
      int oClauses = r.nextInt(maxOuterClauses-1)+2;
      BooleanQuery oq = new BooleanQuery();
      BitSet result=null;
      for (int o=0; o<oClauses; o++) {
      int nClauses = r.nextInt(maxClauses-1)+2; 
      BooleanQuery bq = new BooleanQuery();
      for (int j=0; j<nClauses; j++) {
        result = addClause(bq,result);
      }
      oq.add(bq, BooleanClause.Occur.MUST);
      } 
      CountingHitCollector hc = validate ? new MatchingHitCollector(result)
                                         : new CountingHitCollector();
      s.search(oq, hc);
      nMatches += hc.getCount();
      ret += hc.getSum();
      if (validate) assertEquals(result.cardinality(), hc.getCount());
    }
    if (VERBOSE) System.out.println("Average number of matches="+(nMatches/iter));
    return ret;
  }
  public int doTermConjunctions(IndexSearcher s,
                                int termsInIndex,
                                int maxClauses,
                                int iter
  ) throws IOException {
    int ret=0;
    long nMatches=0;
    for (int i=0; i<iter; i++) {
      int nClauses = r.nextInt(maxClauses-1)+2; 
      BooleanQuery bq = new BooleanQuery();
      BitSet termflag = new BitSet(termsInIndex);
      for (int j=0; j<nClauses; j++) {
        int tnum;
        tnum = r.nextInt(termsInIndex);
        if (termflag.get(tnum)) tnum=termflag.nextClearBit(tnum);
        if (tnum<0 || tnum>=termsInIndex) tnum=termflag.nextClearBit(0);
        termflag.set(tnum);
        Query tq = new TermQuery(terms[tnum]);
        bq.add(tq, BooleanClause.Occur.MUST);
      }
      CountingHitCollector hc = new CountingHitCollector();
      s.search(bq, hc);
      nMatches += hc.getCount();
      ret += hc.getSum();
    }
    if (VERBOSE) System.out.println("Average number of matches="+(nMatches/iter));
    return ret;
  }
  public int doNestedTermConjunctions(IndexSearcher s,
                                int termsInIndex,
                                int maxOuterClauses,
                                int maxClauses,
                                int iter
  ) throws IOException {
    int ret=0;
    long nMatches=0;
    for (int i=0; i<iter; i++) {
      int oClauses = r.nextInt(maxOuterClauses-1)+2;
      BooleanQuery oq = new BooleanQuery();
      for (int o=0; o<oClauses; o++) {
      int nClauses = r.nextInt(maxClauses-1)+2; 
      BooleanQuery bq = new BooleanQuery();
      BitSet termflag = new BitSet(termsInIndex);
      for (int j=0; j<nClauses; j++) {
        int tnum;
        tnum = r.nextInt(termsInIndex);
        if (termflag.get(tnum)) tnum=termflag.nextClearBit(tnum);
        if (tnum<0 || tnum>=25) tnum=termflag.nextClearBit(0);
        termflag.set(tnum);
        Query tq = new TermQuery(terms[tnum]);
        bq.add(tq, BooleanClause.Occur.MUST);
      } 
      oq.add(bq, BooleanClause.Occur.MUST);
      } 
      CountingHitCollector hc = new CountingHitCollector();
      s.search(oq, hc);
      nMatches += hc.getCount();     
      ret += hc.getSum();
    }
    if (VERBOSE) System.out.println("Average number of matches="+(nMatches/iter));
    return ret;
  }
    public int doSloppyPhrase(IndexSearcher s,
                                int termsInIndex,
                                int maxClauses,
                                int iter
  ) throws IOException {
    int ret=0;
    for (int i=0; i<iter; i++) {
      int nClauses = r.nextInt(maxClauses-1)+2; 
      PhraseQuery q = new PhraseQuery();
      for (int j=0; j<nClauses; j++) {
        int tnum = r.nextInt(termsInIndex);
        q.add(new Term("f",Character.toString((char)(tnum+'A'))), j);
      }
      q.setSlop(termsInIndex);  
      CountingHitCollector hc = new CountingHitCollector();
      s.search(q, hc);
      ret += hc.getSum();
    }
    return ret;
  }
  public void testConjunctions() throws Exception {
    r = newRandom();
    createDummySearcher();
    validate=true;
    sets=randBitSets(1000,10);
    doConjunctions(10000,5);
    doNestedConjunctions(10000,3,3);
    s.close();
  }
}
