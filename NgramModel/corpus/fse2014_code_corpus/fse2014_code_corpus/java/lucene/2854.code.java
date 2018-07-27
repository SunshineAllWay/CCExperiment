package org.apache.solr.search;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.util.AbstractSolrTestCase;
import java.io.IOException;
import java.util.*;
public class TestSort extends AbstractSolrTestCase {
  public String getSchemaFile() { return null; }
  public String getSolrConfigFile() { return null; }
  Random r = new Random();
  int ndocs = 77;
  int iter = 100;  
  int qiter = 1000;
  int commitCount = ndocs/5 + 1;
  int maxval = ndocs*2;
  static class MyDoc {
    int doc;
    String val;
  }
  public void testSort() throws Exception {
    RAMDirectory dir = new RAMDirectory();
    Document smallDoc = new Document();
    Field f = new Field("f","0", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
    smallDoc.add(f);
    Document emptyDoc = new Document();
    for (int iterCnt = 0; iterCnt<iter; iterCnt++) {
      IndexWriter iw = new IndexWriter(dir, new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
      final MyDoc[] mydocs = new MyDoc[ndocs];
      int commitCountdown = commitCount;
      for (int i=0; i< ndocs; i++) {
        Document doc;
        MyDoc mydoc = new MyDoc();
        mydoc.doc = i;
        mydocs[i] = mydoc;
        if (r.nextInt(3)==0) {
          doc = emptyDoc;
          mydoc.val = null;
        } else {
          mydoc.val = Integer.toString(r.nextInt(maxval));
          f.setValue(mydoc.val);
          doc = smallDoc;
        }
        iw.addDocument(doc);
        if (--commitCountdown <= 0) {
          commitCountdown = commitCount;
          iw.commit();
        }
      }
      iw.close();
      IndexSearcher searcher = new IndexSearcher(dir, true);
      assertTrue(searcher.getIndexReader().getSequentialSubReaders().length > 1);
      for (int i=0; i<qiter; i++) {
        Filter filt = new Filter() {
          @Override
          public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            return randSet(reader.maxDoc());
          }
        };
        int top = r.nextInt((ndocs>>3)+1)+1;
        final boolean sortMissingLast = r.nextBoolean();
        final boolean reverse = !sortMissingLast;
        List<SortField> sfields = new ArrayList<SortField>();
        if (r.nextBoolean()) sfields.add( new SortField(null, SortField.SCORE));
        sfields.add( Sorting.getStringSortField("f", reverse, sortMissingLast, !sortMissingLast) );
        int sortIdx = sfields.size() - 1;
        if (r.nextBoolean()) sfields.add( new SortField(null, SortField.SCORE));
        Sort sort = new Sort(sfields.toArray(new SortField[sfields.size()]));
        final String nullRep = "zzz";
        boolean trackScores = r.nextBoolean();
        boolean trackMaxScores = r.nextBoolean();
        boolean scoreInOrder = r.nextBoolean();
        final TopFieldCollector topCollector = TopFieldCollector.create(sort, top, true, trackScores, trackMaxScores, scoreInOrder);
        final List<MyDoc> collectedDocs = new ArrayList<MyDoc>();
        Collector myCollector = new Collector() {
          int docBase;
          @Override
          public void setScorer(Scorer scorer) throws IOException {
            topCollector.setScorer(scorer);
          }
          @Override
          public void collect(int doc) throws IOException {
            topCollector.collect(doc);
            collectedDocs.add(mydocs[doc + docBase]);
          }
          @Override
          public void setNextReader(IndexReader reader, int docBase) throws IOException {
            topCollector.setNextReader(reader,docBase);
            this.docBase = docBase;
          }
          @Override
          public boolean acceptsDocsOutOfOrder() {
            return topCollector.acceptsDocsOutOfOrder();
          }
        };
        searcher.search(new MatchAllDocsQuery(), filt, myCollector);
        Collections.sort(collectedDocs, new Comparator<MyDoc>() {
          public int compare(MyDoc o1, MyDoc o2) {
            String v1 = o1.val==null ? nullRep : o1.val;
            String v2 = o2.val==null ? nullRep : o2.val;
            int cmp = v1.compareTo(v2);
            if (reverse) cmp = -cmp;
            cmp = cmp==0 ? o1.doc-o2.doc : cmp;
            return cmp;
          }
        });
        TopDocs topDocs = topCollector.topDocs();
        ScoreDoc[] sdocs = topDocs.scoreDocs;
        for (int j=0; j<sdocs.length; j++) {
          int id = sdocs[j].doc;
          String s = (String)((FieldDoc)sdocs[j]).fields[sortIdx];
          if (id != collectedDocs.get(j).doc) {
            log.error("Error at pos " + j);
          }
          assertEquals(id, collectedDocs.get(j).doc);
        }
      }
    }
  }
  public DocIdSet randSet(int sz) {
    OpenBitSet obs = new OpenBitSet(sz);
    int n = r.nextInt(sz);
    for (int i=0; i<n; i++) {
      obs.fastSet(r.nextInt(sz));
    }
    return obs;
  }  
}
