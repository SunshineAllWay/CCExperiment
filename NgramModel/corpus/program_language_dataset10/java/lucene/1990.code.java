package org.apache.lucene.search.function;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCaseJ4;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
@Ignore
public class FunctionTestSetup extends LuceneTestCaseJ4 {
  protected static float TEST_SCORE_TOLERANCE_DELTA = 0.001f;
  protected static final int N_DOCS = 17; 
  protected static final String ID_FIELD = "id";
  protected static final String TEXT_FIELD = "text";
  protected static final String INT_FIELD = "iii";
  protected static final String FLOAT_FIELD = "fff";
  private static final String DOC_TEXT_LINES[] = {
          "Well, this is just some plain text we use for creating the ",
          "test documents. It used to be a text from an online collection ",
          "devoted to first aid, but if there was there an (online) lawyers ",
          "first aid collection with legal advices, \"it\" might have quite ",
          "probably advised one not to include \"it\"'s text or the text of ",
          "any other online collection in one's code, unless one has money ",
          "that one don't need and one is happy to donate for lawyers ",
          "charity. Anyhow at some point, rechecking the usage of this text, ",
          "it became uncertain that this text is free to use, because ",
          "the web site in the disclaimer of he eBook containing that text ",
          "was not responding anymore, and at the same time, in projGut, ",
          "searching for first aid no longer found that eBook as well. ",
          "So here we are, with a perhaps much less interesting ",
          "text for the test, but oh much much safer. ",
  };
  protected Directory dir;
  protected Analyzer anlzr;
  private final boolean doMultiSegment;
  public FunctionTestSetup(boolean doMultiSegment) {
    this.doMultiSegment = doMultiSegment;
  }
  public FunctionTestSetup() {
    this(false);
  }
  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    dir = null;
    anlzr = null;
  }
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    super.setUp();
    dir = new RAMDirectory();
    anlzr = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, anlzr));
    int remaining = N_DOCS;
    boolean done[] = new boolean[N_DOCS];
    int i = 0;
    while (remaining > 0) {
      if (done[i]) {
        throw new Exception("to set this test correctly N_DOCS=" + N_DOCS + " must be primary and greater than 2!");
      }
      addDoc(iw, i);
      done[i] = true;
      i = (i + 4) % N_DOCS;
      if (doMultiSegment && remaining % 3 == 0) {
        iw.commit();
      }
      remaining --;
    }
    iw.close();
  }
  private void addDoc(IndexWriter iw, int i) throws Exception {
    Document d = new Document();
    Fieldable f;
    int scoreAndID = i + 1;
    f = new Field(ID_FIELD, id2String(scoreAndID), Field.Store.YES, Field.Index.NOT_ANALYZED); 
    f.setOmitNorms(true);
    d.add(f);
    f = new Field(TEXT_FIELD, "text of doc" + scoreAndID + textLine(i), Field.Store.NO, Field.Index.ANALYZED); 
    f.setOmitNorms(true);
    d.add(f);
    f = new Field(INT_FIELD, "" + scoreAndID, Field.Store.NO, Field.Index.NOT_ANALYZED); 
    f.setOmitNorms(true);
    d.add(f);
    f = new Field(FLOAT_FIELD, scoreAndID + ".000", Field.Store.NO, Field.Index.NOT_ANALYZED); 
    f.setOmitNorms(true);
    d.add(f);
    iw.addDocument(d);
    log("added: " + d);
  }
  protected String id2String(int scoreAndID) {
    String s = "000000000" + scoreAndID;
    int n = ("" + N_DOCS).length() + 3;
    int k = s.length() - n;
    return "ID" + s.substring(k);
  }
  private String textLine(int docNum) {
    return DOC_TEXT_LINES[docNum % DOC_TEXT_LINES.length];
  }
  protected float expectedFieldScore(String docIDFieldVal) {
    return Float.parseFloat(docIDFieldVal.substring(2));
  }
  protected void log(Object o) {
    if (VERBOSE) {
      System.out.println(o.toString());
    }
  }
}
