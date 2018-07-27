package org.apache.lucene.index;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MockRAMDirectory;
public class TestTransactionRollback extends LuceneTestCase {
  private static final String FIELD_RECORD_ID = "record_id";
  private Directory dir;
  private void rollBackLast(int id) throws Exception {
    String ids="-"+id;
    IndexCommit last=null;
    Collection<IndexCommit> commits = IndexReader.listCommits(dir);
    for (Iterator<IndexCommit> iterator = commits.iterator(); iterator.hasNext();) {
      IndexCommit commit =  iterator.next();
      Map<String,String> ud=commit.getUserData();
      if (ud.size() > 0)
        if (ud.get("index").endsWith(ids))
          last=commit;
    }
    if (last==null)
      throw new RuntimeException("Couldn't find commit point "+id);
    IndexWriter w = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(
        new RollbackDeletionPolicy(id)).setIndexCommit(last));
    Map<String,String> data = new HashMap<String,String>();
    data.put("index", "Rolled back to 1-"+id);
    w.commit(data);
    w.close();
  }
  public void testRepeatedRollBacks() throws Exception {		
    int expectedLastRecordId=100;
    while (expectedLastRecordId>10) {
      expectedLastRecordId -=10;			
      rollBackLast(expectedLastRecordId);
      BitSet expecteds = new BitSet(100);
      expecteds.set(1,(expectedLastRecordId+1),true);
      checkExpecteds(expecteds);			
    }
  }
  private void checkExpecteds(BitSet expecteds) throws Exception {
    IndexReader r = IndexReader.open(dir, true);
    for (int i = 0; i < r.maxDoc(); i++) {
      if(!r.isDeleted(i)) {
        String sval=r.document(i).get(FIELD_RECORD_ID);
        if(sval!=null) {
          int val=Integer.parseInt(sval);
          assertTrue("Did not expect document #"+val, expecteds.get(val));
          expecteds.set(val,false);
        }
      }
    }
    r.close();
    assertEquals("Should have 0 docs remaining ", 0 ,expecteds.cardinality());
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = new MockRAMDirectory();
    IndexDeletionPolicy sdp=new KeepAllDeletionPolicy();
    IndexWriter w=new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setIndexDeletionPolicy(sdp));
    for(int currentRecordId=1;currentRecordId<=100;currentRecordId++) {
      Document doc=new Document();
      doc.add(new Field(FIELD_RECORD_ID,""+currentRecordId,Field.Store.YES,Field.Index.ANALYZED));
      w.addDocument(doc);
      if (currentRecordId%10 == 0) {
        Map<String,String> data = new HashMap<String,String>();
        data.put("index", "records 1-"+currentRecordId);
        w.commit(data);
      }
    }
    w.close();
  }
  class RollbackDeletionPolicy implements IndexDeletionPolicy {
    private int rollbackPoint;
    public RollbackDeletionPolicy(int rollbackPoint) {
      this.rollbackPoint = rollbackPoint;
    }
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
    }
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      for (final IndexCommit commit : commits) {
        Map<String,String> userData=commit.getUserData();
        if (userData.size() > 0) {
          String x = userData.get("index");
          String lastVal = x.substring(x.lastIndexOf("-")+1);
          int last = Integer.parseInt(lastVal);
          if (last>rollbackPoint) {
            commit.delete();									
          }
        }
      }
    }		
  }
  class DeleteLastCommitPolicy implements IndexDeletionPolicy {
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {}
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
      commits.get(commits.size()-1).delete();
    }
  }
  public void testRollbackDeletionPolicy() throws Exception {		
    for(int i=0;i<2;i++) {
      new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT))
          .setIndexDeletionPolicy(new DeleteLastCommitPolicy())).close();
      IndexReader r = IndexReader.open(dir, true);
      assertEquals(100, r.numDocs());
      r.close();
    }
  }
  class KeepAllDeletionPolicy implements IndexDeletionPolicy {
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {}
    public void onInit(List<? extends IndexCommit> commits) throws IOException {}
  }
}
