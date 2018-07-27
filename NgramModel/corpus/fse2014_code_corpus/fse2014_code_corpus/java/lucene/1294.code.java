package org.apache.lucene.spatial.tier;
import java.io.IOException;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.store.RAMDirectory;
public class TestDistance extends LuceneTestCase {
  private RAMDirectory directory;
  private double lat = 38.969398; 
  private double lng= -77.386398;
  private String latField = "lat";
  private String lngField = "lng";
  private IndexWriter writer;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    addData(writer);
  }
  @Override
  protected void tearDown() throws Exception {
    writer.close();
    super.tearDown();
  }
  private void addPoint(IndexWriter writer, String name, double lat, double lng) throws IOException{
    Document doc = new Document();
    doc.add(new Field("name", name,Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field(latField, NumericUtils.doubleToPrefixCoded(lat),Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field(lngField, NumericUtils.doubleToPrefixCoded(lng),Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
  }
  private void addData(IndexWriter writer) throws IOException {
    addPoint(writer,"McCormick &amp; Schmick's Seafood Restaurant",38.9579000,-77.3572000);
    addPoint(writer,"Jimmy's Old Town Tavern",38.9690000,-77.3862000);
    addPoint(writer,"Ned Devine's",38.9510000,-77.4107000);
    addPoint(writer,"Old Brogue Irish Pub",38.9955000,-77.2884000);
    addPoint(writer,"Alf Laylah Wa Laylah",38.8956000,-77.4258000);
    addPoint(writer,"Sully's Restaurant &amp; Supper",38.9003000,-77.4467000);
    addPoint(writer,"TGIFriday",38.8725000,-77.3829000);
    addPoint(writer,"Potomac Swing Dance Club",38.9027000,-77.2639000);
    addPoint(writer,"White Tiger Restaurant",38.9027000,-77.2638000);
    addPoint(writer,"Jammin' Java",38.9039000,-77.2622000);
    addPoint(writer,"Potomac Swing Dance Club",38.9027000,-77.2639000);
    addPoint(writer,"WiseAcres Comedy Club",38.9248000,-77.2344000);
    addPoint(writer,"Glen Echo Spanish Ballroom",38.9691000,-77.1400000);
    addPoint(writer,"Whitlow's on Wilson",38.8889000,-77.0926000);
    addPoint(writer,"Iota Club and Cafe",38.8890000,-77.0923000);
    addPoint(writer,"Hilton Washington Embassy Row",38.9103000,-77.0451000);
    addPoint(writer,"HorseFeathers, Bar & Grill", 39.01220000000001, -77.3942);
    writer.commit();
  }
  public void testLatLongFilterOnDeletedDocs() throws Exception {
    writer.deleteDocuments(new Term("name", "Potomac"));
    IndexReader r = writer.getReader();
    LatLongDistanceFilter f = new LatLongDistanceFilter(new QueryWrapperFilter(new MatchAllDocsQuery()),
                                                        lat, lng, 1.0, latField, lngField);
    IndexReader[] readers = r.getSequentialSubReaders();
    for(int i=0;i<readers.length;i++) {
      f.getDocIdSet(readers[i]);
    }
  }
}
