package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.RAMDirectory;
import java.io.IOException;
import java.util.LinkedList;
public class TestPhrasePrefixQuery extends LuceneTestCase {
  public TestPhrasePrefixQuery(String name) {
    super(name);
  }
    public void testPhrasePrefix()
        throws IOException
    {
        RAMDirectory indexStore = new RAMDirectory();
        IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
        Document doc1 = new Document();
        Document doc2 = new Document();
        Document doc3 = new Document();
        Document doc4 = new Document();
        Document doc5 = new Document();
        doc1.add(new Field("body", "blueberry pie", Field.Store.YES, Field.Index.ANALYZED));
        doc2.add(new Field("body", "blueberry strudel", Field.Store.YES, Field.Index.ANALYZED));
        doc3.add(new Field("body", "blueberry pizza", Field.Store.YES, Field.Index.ANALYZED));
        doc4.add(new Field("body", "blueberry chewing gum", Field.Store.YES, Field.Index.ANALYZED));
        doc5.add(new Field("body", "piccadilly circus", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc1);
        writer.addDocument(doc2);
        writer.addDocument(doc3);
        writer.addDocument(doc4);
        writer.addDocument(doc5);
        writer.optimize();
        writer.close();
        IndexSearcher searcher = new IndexSearcher(indexStore, true);
        MultiPhraseQuery query1 = new MultiPhraseQuery();
        MultiPhraseQuery query2 = new MultiPhraseQuery();
        query1.add(new Term("body", "blueberry"));
        query2.add(new Term("body", "strawberry"));
        LinkedList<Term> termsWithPrefix = new LinkedList<Term>();
        IndexReader ir = IndexReader.open(indexStore, true);
        String prefix = "pi";
        TermEnum te = ir.terms(new Term("body", prefix + "*"));
        do {
            if (te.term().text().startsWith(prefix))
            {
                termsWithPrefix.add(te.term());
            }
        } while (te.next());
        query1.add(termsWithPrefix.toArray(new Term[0]));
        query2.add(termsWithPrefix.toArray(new Term[0]));
        ScoreDoc[] result;
        result = searcher.search(query1, null, 1000).scoreDocs;
        assertEquals(2, result.length);
        result = searcher.search(query2, null, 1000).scoreDocs;
        assertEquals(0, result.length);
    }
}
