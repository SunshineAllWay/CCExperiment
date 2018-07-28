package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.MockRAMDirectory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class TestMultiSearcher extends LuceneTestCase
{
    public TestMultiSearcher(String name)
    {
        super(name);
    }
	protected MultiSearcher getMultiSearcherInstance(Searcher[] searchers) throws IOException {
		return new MultiSearcher(searchers);
	}
    public void testEmptyIndex() throws Exception {
        Directory indexStoreA = new MockRAMDirectory();
        Directory indexStoreB = new MockRAMDirectory();
        Document lDoc = new Document();
        lDoc.add(new Field("fulltext", "Once upon a time.....", Field.Store.YES, Field.Index.ANALYZED));
        lDoc.add(new Field("id", "doc1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        lDoc.add(new Field("handle", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        Document lDoc2 = new Document();
        lDoc2.add(new Field("fulltext", "in a galaxy far far away.....",
            Field.Store.YES, Field.Index.ANALYZED));
        lDoc2.add(new Field("id", "doc2", Field.Store.YES, Field.Index.NOT_ANALYZED));
        lDoc2.add(new Field("handle", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        Document lDoc3 = new Document();
        lDoc3.add(new Field("fulltext", "a bizarre bug manifested itself....",
            Field.Store.YES, Field.Index.ANALYZED));
        lDoc3.add(new Field("id", "doc3", Field.Store.YES, Field.Index.NOT_ANALYZED));
        lDoc3.add(new Field("handle", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        IndexWriter writerA = new IndexWriter(indexStoreA, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
        IndexWriter writerB = new IndexWriter(indexStoreB, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
        writerA.addDocument(lDoc);
        writerA.addDocument(lDoc2);
        writerA.addDocument(lDoc3);
        writerA.optimize();
        writerA.close();
        writerB.close();
        QueryParser parser = new QueryParser(TEST_VERSION_CURRENT, "fulltext", new StandardAnalyzer(TEST_VERSION_CURRENT));
        Query query = parser.parse("handle:1");
        Searcher[] searchers = new Searcher[2];
        searchers[0] = new IndexSearcher(indexStoreB, true);
        searchers[1] = new IndexSearcher(indexStoreA, true);
        Searcher mSearcher = getMultiSearcherInstance(searchers);
        ScoreDoc[] hits = mSearcher.search(query, null, 1000).scoreDocs;
        assertEquals(3, hits.length);
        for (int i = 0; i < hits.length; i++) {
          mSearcher.doc(hits[i].doc);
        }
        mSearcher.close();
        writerB = new IndexWriter(indexStoreB, new IndexWriterConfig(
            TEST_VERSION_CURRENT, 
                new StandardAnalyzer(TEST_VERSION_CURRENT))
                .setOpenMode(OpenMode.APPEND));
        writerB.addDocument(lDoc);
        writerB.optimize();
        writerB.close();
        Searcher[] searchers2 = new Searcher[2];
        searchers2[0] = new IndexSearcher(indexStoreB, true);
        searchers2[1] = new IndexSearcher(indexStoreA, true);
        MultiSearcher mSearcher2 = getMultiSearcherInstance(searchers2);
        ScoreDoc[] hits2 = mSearcher2.search(query, null, 1000).scoreDocs;
        assertEquals(4, hits2.length);
        for (int i = 0; i < hits2.length; i++) {
          mSearcher2.doc(hits2[i].doc);
        }
        Query subSearcherQuery = parser.parse("id:doc1");
        hits2 = mSearcher2.search(subSearcherQuery, null, 1000).scoreDocs;
        assertEquals(2, hits2.length);
        assertEquals(0, mSearcher2.subSearcher(hits2[0].doc));   
        assertEquals(1, mSearcher2.subSearcher(hits2[1].doc));   
        subSearcherQuery = parser.parse("id:doc2");
        hits2 = mSearcher2.search(subSearcherQuery, null, 1000).scoreDocs;
        assertEquals(1, hits2.length);
        assertEquals(1, mSearcher2.subSearcher(hits2[0].doc));   
        mSearcher2.close();
        Term term = new Term("id", "doc1");
        IndexReader readerB = IndexReader.open(indexStoreB, false);
        readerB.deleteDocuments(term);
        readerB.close();
        writerB = new IndexWriter(indexStoreB, new IndexWriterConfig(
            TEST_VERSION_CURRENT, 
                new StandardAnalyzer(TEST_VERSION_CURRENT))
                .setOpenMode(OpenMode.APPEND));
        writerB.optimize();
        writerB.close();
        Searcher[] searchers3 = new Searcher[2];
        searchers3[0] = new IndexSearcher(indexStoreB, true);
        searchers3[1] = new IndexSearcher(indexStoreA, true);
        Searcher mSearcher3 = getMultiSearcherInstance(searchers3);
        ScoreDoc[] hits3 = mSearcher3.search(query, null, 1000).scoreDocs;
        assertEquals(3, hits3.length);
        for (int i = 0; i < hits3.length; i++) {
          mSearcher3.doc(hits3[i].doc);
        }
        mSearcher3.close();
        indexStoreA.close();
        indexStoreB.close();
    }
    private static Document createDocument(String contents1, String contents2) {
        Document document=new Document();
        document.add(new Field("contents", contents1, Field.Store.YES, Field.Index.NOT_ANALYZED));
      document.add(new Field("other", "other contents", Field.Store.YES, Field.Index.NOT_ANALYZED));
        if (contents2!=null) {
            document.add(new Field("contents", contents2, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        return document;
    }
    private static void initIndex(Directory directory, int nDocs, boolean create, String contents2) throws IOException {
        IndexWriter indexWriter=null;
        try {
          indexWriter = new IndexWriter(directory, new IndexWriterConfig(
              TEST_VERSION_CURRENT, new KeywordAnalyzer()).setOpenMode(
                  create ? OpenMode.CREATE : OpenMode.APPEND));
            for (int i=0; i<nDocs; i++) {
                indexWriter.addDocument(createDocument("doc" + i, contents2));
            }
        } finally {
            if (indexWriter!=null) {
                indexWriter.close();
            }
        }
    }
  public void testFieldSelector() throws Exception {
    RAMDirectory ramDirectory1, ramDirectory2;
    IndexSearcher indexSearcher1, indexSearcher2;
    ramDirectory1 = new RAMDirectory();
    ramDirectory2 = new RAMDirectory();
    Query query = new TermQuery(new Term("contents", "doc0"));
    initIndex(ramDirectory1, 10, true, null); 
    initIndex(ramDirectory2, 10, true, "x"); 
    indexSearcher1 = new IndexSearcher(ramDirectory1, true);
    indexSearcher2 = new IndexSearcher(ramDirectory2, true);
    MultiSearcher searcher = getMultiSearcherInstance(new Searcher[]{indexSearcher1, indexSearcher2});
    assertTrue("searcher is null and it shouldn't be", searcher != null);
    ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue(hits.length + " does not equal: " + 2, hits.length == 2);
    Document document = searcher.doc(hits[0].doc);
    assertTrue("document is null and it shouldn't be", document != null);
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 2, document.getFields().size() == 2);
    Set<String> ftl = new HashSet<String>();
    ftl.add("other");
    SetBasedFieldSelector fs = new SetBasedFieldSelector(ftl, Collections. <String> emptySet());
    document = searcher.doc(hits[0].doc, fs);
    assertTrue("document is null and it shouldn't be", document != null);
    assertTrue("document.getFields() Size: " + document.getFields().size() + " is not: " + 1, document.getFields().size() == 1);
    String value = document.get("contents");
    assertTrue("value is not null and it should be", value == null);
    value = document.get("other");
    assertTrue("value is null and it shouldn't be", value != null);
    ftl.clear();
    ftl.add("contents");
    fs = new SetBasedFieldSelector(ftl, Collections. <String> emptySet());
    document = searcher.doc(hits[1].doc, fs);
    value = document.get("contents");
    assertTrue("value is null and it shouldn't be", value != null);    
    value = document.get("other");
    assertTrue("value is not null and it should be", value == null);
  }
    public void testNormalization10() throws IOException {
        testNormalization(10, "Using 10 documents per index:");
    }
    private void testNormalization(int nDocs, String message) throws IOException {
        Query query=new TermQuery(new Term("contents", "doc0"));
        RAMDirectory ramDirectory1;
        IndexSearcher indexSearcher1;
        ScoreDoc[] hits;
        ramDirectory1=new MockRAMDirectory();
        initIndex(ramDirectory1, nDocs, true, null); 
        initIndex(ramDirectory1, nDocs, false, "x"); 
        indexSearcher1=new IndexSearcher(ramDirectory1, true);
        indexSearcher1.setDefaultFieldSortScoring(true, true);
        hits=indexSearcher1.search(query, null, 1000).scoreDocs;
        assertEquals(message, 2, hits.length);
        float[] scores={ hits[0].score, hits[1].score };
        assertTrue(message, scores[0] > scores[1]);
        indexSearcher1.close();
        ramDirectory1.close();
        hits=null;
        RAMDirectory ramDirectory2;
        IndexSearcher indexSearcher2;
        ramDirectory1=new MockRAMDirectory();
        ramDirectory2=new MockRAMDirectory();
        initIndex(ramDirectory1, nDocs, true, null); 
        initIndex(ramDirectory2, nDocs, true, "x"); 
        indexSearcher1=new IndexSearcher(ramDirectory1, true);
        indexSearcher1.setDefaultFieldSortScoring(true, true);
        indexSearcher2=new IndexSearcher(ramDirectory2, true);
        indexSearcher2.setDefaultFieldSortScoring(true, true);
        Searcher searcher=getMultiSearcherInstance(new Searcher[] { indexSearcher1, indexSearcher2 });
        hits=searcher.search(query, null, 1000).scoreDocs;
        assertEquals(message, 2, hits.length);
        assertEquals(message, scores[0], hits[0].score, 1e-6); 
        assertEquals(message, scores[1], hits[1].score, 1e-6); 
        hits=searcher.search(query, null, 1000, Sort.RELEVANCE).scoreDocs;
        assertEquals(message, 2, hits.length);
        assertEquals(message, scores[0], hits[0].score, 1e-6); 
        assertEquals(message, scores[1], hits[1].score, 1e-6); 
        searcher.close();
        ramDirectory1.close();
        ramDirectory2.close();
    }
    public void testCustomSimilarity () throws IOException {
        RAMDirectory dir = new RAMDirectory();
        initIndex(dir, 10, true, "x"); 
        IndexSearcher srchr = new IndexSearcher(dir, true);
        MultiSearcher msrchr = getMultiSearcherInstance(new Searcher[]{srchr});
        Similarity customSimilarity = new DefaultSimilarity() {
            @Override
            public float idf(int docFreq, int numDocs) { return 100.0f; }
            @Override
            public float coord(int overlap, int maxOverlap) { return 1.0f; }
            @Override
            public float lengthNorm(String fieldName, int numTokens) { return 1.0f; }
            @Override
            public float queryNorm(float sumOfSquaredWeights) { return 1.0f; }
            @Override
            public float sloppyFreq(int distance) { return 1.0f; }
            @Override
            public float tf(float freq) { return 1.0f; }
        };
        srchr.setSimilarity(customSimilarity);
        msrchr.setSimilarity(customSimilarity);
        Query query=new TermQuery(new Term("contents", "doc0"));
        TopDocs topDocs = srchr.search(query, null, 1);
        float score1 = topDocs.getMaxScore();
        topDocs = msrchr.search(query, null, 1);
        float scoreN = topDocs.getMaxScore();
        assertEquals("MultiSearcher score must be equal to single searcher score!", score1, scoreN, 1e-6);
    }
    public void testDocFreq() throws IOException{
      RAMDirectory dir1 = new RAMDirectory();
      RAMDirectory dir2 = new RAMDirectory();
      initIndex(dir1, 10, true, "x"); 
      initIndex(dir2, 5, true, "x"); 
      IndexSearcher searcher1 = new IndexSearcher(dir1, true);
      IndexSearcher searcher2 = new IndexSearcher(dir2, true);
      MultiSearcher multiSearcher = getMultiSearcherInstance(new Searcher[]{searcher1, searcher2});
      assertEquals(15, multiSearcher.docFreq(new Term("contents","x")));
    }
    public void testCreateDocFrequencyMap() throws IOException{
      RAMDirectory dir1 = new RAMDirectory();
      RAMDirectory dir2 = new RAMDirectory();
      Term template = new Term("contents") ;
      String[] contents  = {"a", "b", "c"};
      HashSet<Term> termsSet = new HashSet<Term>();
      for (int i = 0; i < contents.length; i++) {
        initIndex(dir1, i+10, i==0, contents[i]); 
        initIndex(dir2, i+5, i==0, contents[i]);
        termsSet.add(template.createTerm(contents[i]));
      }
      IndexSearcher searcher1 = new IndexSearcher(dir1, true);
      IndexSearcher searcher2 = new IndexSearcher(dir2, true);
      MultiSearcher multiSearcher = getMultiSearcherInstance(new Searcher[]{searcher1, searcher2});
      Map<Term,Integer> docFrequencyMap = multiSearcher.createDocFrequencyMap(termsSet);
      assertEquals(3, docFrequencyMap.size());
      for (int i = 0; i < contents.length; i++) {
        assertEquals(Integer.valueOf((i*2) +15), docFrequencyMap.get(template.createTerm(contents[i])));
      }
    }
}
