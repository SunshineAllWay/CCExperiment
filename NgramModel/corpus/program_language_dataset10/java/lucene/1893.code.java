package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestLazyProxSkipping extends LuceneTestCase {
    private Searcher searcher;
    private int seeksCounter = 0;
    private String field = "tokens";
    private String term1 = "xx";
    private String term2 = "yy";
    private String term3 = "zz";
    private class SeekCountingDirectory extends RAMDirectory {
      @Override
      public IndexInput openInput(String name) throws IOException {
        IndexInput ii = super.openInput(name);
        if (name.endsWith(".prx")) {
          ii = new SeeksCountingStream(ii);
        }
        return ii;
      }
    }
    private void createIndex(int numHits) throws IOException {
        int numDocs = 500;
        Directory directory = new SeekCountingDirectory();
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
        ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false);
        ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false);
        for (int i = 0; i < numDocs; i++) {
            Document doc = new Document();
            String content;
            if (i % (numDocs / numHits) == 0) {
                content = this.term1 + " " + this.term2;
            } else if (i % 15 == 0) {
                content = this.term1 + " " + this.term1;
            } else {
                content = this.term3 + " " + this.term2;
            }
            doc.add(new Field(this.field, content, Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.optimize();
        writer.close();
        SegmentReader reader = SegmentReader.getOnlySegmentReader(directory);
        this.searcher = new IndexSearcher(reader);        
    }
    private ScoreDoc[] search() throws IOException {
        PhraseQuery pq = new PhraseQuery();
        pq.add(new Term(this.field, this.term1));
        pq.add(new Term(this.field, this.term2));
        return this.searcher.search(pq, null, 1000).scoreDocs;        
    }
    private void performTest(int numHits) throws IOException {
        createIndex(numHits);
        this.seeksCounter = 0;
        ScoreDoc[] hits = search();
        assertEquals(numHits, hits.length);
        assertTrue(this.seeksCounter > 0);
        assertTrue(this.seeksCounter <= numHits + 1);
    }
    public void testLazySkipping() throws IOException {
        performTest(5);
        performTest(10);
    }
    public void testSeek() throws IOException {
        Directory directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < 10; i++) {
            Document doc = new Document();
            doc.add(new Field(this.field, "a b", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();
        IndexReader reader = IndexReader.open(directory, true);
        TermPositions tp = reader.termPositions();
        tp.seek(new Term(this.field, "b"));
        for (int i = 0; i < 10; i++) {
            tp.next();
            assertEquals(tp.doc(), i);
            assertEquals(tp.nextPosition(), 1);
        }
        tp.seek(new Term(this.field, "a"));
        for (int i = 0; i < 10; i++) {
            tp.next();
            assertEquals(tp.doc(), i);
            assertEquals(tp.nextPosition(), 0);
        }
    }
    class SeeksCountingStream extends IndexInput {
          private IndexInput input;      
          SeeksCountingStream(IndexInput input) {
              this.input = input;
          }      
          @Override
          public byte readByte() throws IOException {
              return this.input.readByte();
          }
          @Override
          public void readBytes(byte[] b, int offset, int len) throws IOException {
              this.input.readBytes(b, offset, len);        
          }
          @Override
          public void close() throws IOException {
              this.input.close();
          }
          @Override
          public long getFilePointer() {
              return this.input.getFilePointer();
          }
          @Override
          public void seek(long pos) throws IOException {
              TestLazyProxSkipping.this.seeksCounter++;
              this.input.seek(pos);
          }
          @Override
          public long length() {
              return this.input.length();
          }
          @Override
          public Object clone() {
              return new SeeksCountingStream((IndexInput) this.input.clone());
          }
    }
}
