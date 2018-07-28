package org.apache.lucene.search;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestCustomSearcherSort
extends LuceneTestCase
implements Serializable {
    private Directory index = null;
    private Query query = null;
    private final static int INDEX_SIZE = 2000;
  public TestCustomSearcherSort (String name) {
    super (name);
  }
  public static void main (String[] argv) {
      TestRunner.run (suite());
  }
  public static Test suite() {
    return new TestSuite (TestCustomSearcherSort.class);
  }
  private Directory getIndex()
  throws IOException {
          RAMDirectory indexStore = new RAMDirectory ();
          IndexWriter writer = new IndexWriter (indexStore, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
          RandomGen random = new RandomGen(newRandom());
          for (int i=0; i<INDEX_SIZE; ++i) { 
          Document doc = new Document();
              if((i%5)!=0) { 
                  doc.add (new Field("publicationDate_", random.getLuceneDate(), Field.Store.YES, Field.Index.NOT_ANALYZED));
              }
              if((i%7)==0) { 
                  doc.add (new Field("content", "test", Field.Store.YES, Field.Index.ANALYZED));
              }
              doc.add(new Field("mandant", Integer.toString(i%3), Field.Store.YES, Field.Index.NOT_ANALYZED));
              writer.addDocument (doc);
          }
          writer.optimize ();
          writer.close ();
      return indexStore;
  }
  @Override
  protected void setUp() throws Exception {
          super.setUp();
          index = getIndex();
          query = new TermQuery( new Term("content", "test"));
  }
  public void testFieldSortCustomSearcher() throws Exception {
      Sort custSort = new Sort(
              new SortField("publicationDate_", SortField.STRING), 
              SortField.FIELD_SCORE
      );
      Searcher searcher = new CustomSearcher (index, 2);
    matchHits(searcher, custSort);
  }
  public void testFieldSortSingleSearcher() throws Exception {
      Sort custSort = new Sort(
              new SortField("publicationDate_", SortField.STRING), 
              SortField.FIELD_SCORE
      );
      Searcher searcher = new MultiSearcher(new Searcher[] { new CustomSearcher(
        index, 2) });
    matchHits(searcher, custSort);
  }
  public void testFieldSortMultiCustomSearcher() throws Exception {
      Sort custSort = new Sort(
              new SortField("publicationDate_", SortField.STRING), 
              SortField.FIELD_SCORE
      );
      Searcher searcher = 
          new MultiSearcher(new Searchable[] {
                  new CustomSearcher (index, 0),
                  new CustomSearcher (index, 2)});
    matchHits(searcher, custSort);
  }
  private void matchHits (Searcher searcher, Sort sort)
  throws IOException {
    ScoreDoc[] hitsByRank = searcher.search(query, null, 1000).scoreDocs;
    checkHits(hitsByRank, "Sort by rank: "); 
        Map<Integer,Integer> resultMap = new TreeMap<Integer,Integer>();
        for(int hitid=0;hitid<hitsByRank.length; ++hitid) {
            resultMap.put(
                    Integer.valueOf(hitsByRank[hitid].doc),  
                    Integer.valueOf(hitid));				
        }
    ScoreDoc[] resultSort = searcher.search (query, null, 1000, sort).scoreDocs;
    checkHits(resultSort, "Sort by custom criteria: "); 
        for(int hitid=0;hitid<resultSort.length; ++hitid) {
            Integer idHitDate = Integer.valueOf(resultSort[hitid].doc); 
            if(!resultMap.containsKey(idHitDate)) {
                log("ID "+idHitDate+" not found. Possibliy a duplicate.");
            }
            assertTrue(resultMap.containsKey(idHitDate)); 
            resultMap.remove(idHitDate);
        }
        if(resultMap.size()==0) {
        } else {
        log("Couldn't match "+resultMap.size()+" hits.");
        }
        assertEquals(resultMap.size(), 0);
  }
    private void checkHits(ScoreDoc[] hits, String prefix) {
        if(hits!=null) {
            Map<Integer,Integer> idMap = new TreeMap<Integer,Integer>();
            for(int docnum=0;docnum<hits.length;++docnum) {
                Integer luceneId = null;
                luceneId = Integer.valueOf(hits[docnum].doc);
                if(idMap.containsKey(luceneId)) {
                    StringBuilder message = new StringBuilder(prefix);
                    message.append("Duplicate key for hit index = ");
                    message.append(docnum);
                    message.append(", previous index = ");
                    message.append((idMap.get(luceneId)).toString());
                    message.append(", Lucene ID = ");
                    message.append(luceneId);
                    log(message.toString());
                } else { 
                    idMap.put(luceneId, Integer.valueOf(docnum));
                }
            }
        }
    }
    private void log(String message) {
        if (VERBOSE) System.out.println(message);
    }
    public class CustomSearcher extends IndexSearcher {
        private int switcher;
        public CustomSearcher(Directory directory, int switcher) throws IOException {
            super(directory, true);
            this.switcher = switcher;
        }
        public CustomSearcher(IndexReader r, int switcher) {
            super(r);
            this.switcher = switcher;
        }
        @Override
        public TopFieldDocs search(Query query, Filter filter, int nDocs,
                Sort sort) throws IOException {
            BooleanQuery bq = new BooleanQuery();
            bq.add(query, BooleanClause.Occur.MUST);
            bq.add(new TermQuery(new Term("mandant", Integer.toString(switcher))), BooleanClause.Occur.MUST);
            return super.search(bq, filter, nDocs, sort);
        }
        @Override
        public TopDocs search(Query query, Filter filter, int nDocs)
        throws IOException {
            BooleanQuery bq = new BooleanQuery();
            bq.add(query, BooleanClause.Occur.MUST);
            bq.add(new TermQuery(new Term("mandant", Integer.toString(switcher))), BooleanClause.Occur.MUST);
            return super.search(bq, filter, nDocs);
        }
    }
    private class RandomGen {
      RandomGen(Random random) {
        this.random = random;
      }
      private Random random;
      private Calendar base = new GregorianCalendar(1980, 1, 1);
      private String getLuceneDate() {
        return DateTools.timeToString(base.getTimeInMillis() + random.nextInt() - Integer.MIN_VALUE, DateTools.Resolution.DAY);
      }
    }
}
