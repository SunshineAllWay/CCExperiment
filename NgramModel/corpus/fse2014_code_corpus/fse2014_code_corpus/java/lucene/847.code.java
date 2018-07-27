package org.apache.lucene.benchmark.byTask.feeds;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.Format;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
public class DocMaker {
  private static class LeftOver {
    private DocData docdata;
    private int cnt;
  }
  private Random r;
  private int updateDocIDLimit;
  static class DocState {
    private final Map<String,Field> fields;
    private final boolean reuseFields;
    final Document doc;
    DocData docData = new DocData();
    public DocState(boolean reuseFields, Store store, Store bodyStore, Index index, Index bodyIndex, TermVector termVector) {
      this.reuseFields = reuseFields;
      if (reuseFields) {
        fields =  new HashMap<String,Field>();
        fields.put(BODY_FIELD, new Field(BODY_FIELD, "", bodyStore, bodyIndex, termVector));
        fields.put(TITLE_FIELD, new Field(TITLE_FIELD, "", store, index, termVector));
        fields.put(DATE_FIELD, new Field(DATE_FIELD, "", store, index, termVector));
        fields.put(ID_FIELD, new Field(ID_FIELD, "", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        fields.put(NAME_FIELD, new Field(NAME_FIELD, "", store, index, termVector));
        doc = new Document();
      } else {
        fields = null;
        doc = null;
      }
    }
    Field getField(String name, Store store, Index index, TermVector termVector) {
      if (!reuseFields) {
        return new Field(name, "", store, index, termVector);
      }
      Field f = fields.get(name);
      if (f == null) {
        f = new Field(name, "", store, index, termVector);
        fields.put(name, f);
      }
      return f;
    }
  }
  private int numDocsCreated = 0;
  private boolean storeBytes = false;
  private ThreadLocal<LeftOver> leftovr = new ThreadLocal<LeftOver>();
  private ThreadLocal<DocState> docState = new ThreadLocal<DocState>();
  public static final String BODY_FIELD = "body";
  public static final String TITLE_FIELD = "doctitle";
  public static final String DATE_FIELD = "docdate";
  public static final String ID_FIELD = "docid";
  public static final String BYTES_FIELD = "bytes";
  public static final String NAME_FIELD = "docname";
  protected Config config;
  protected Store storeVal = Store.NO;
  protected Store bodyStoreVal = Store.NO;
  protected Index indexVal = Index.ANALYZED_NO_NORMS;
  protected Index bodyIndexVal = Index.ANALYZED;
  protected TermVector termVecVal = TermVector.NO;
  protected ContentSource source;
  protected boolean reuseFields;
  protected boolean indexProperties;
  private int lastPrintedNumUniqueTexts = 0;
  private long lastPrintedNumUniqueBytes = 0;
  private int printNum = 0;
  private Document createDocument(DocData docData, int size, int cnt) throws UnsupportedEncodingException {
    final DocState ds = getDocState();
    final Document doc = reuseFields ? ds.doc : new Document();
    doc.getFields().clear();
    Field idField = ds.getField(ID_FIELD, storeVal, Index.NOT_ANALYZED_NO_NORMS, termVecVal);
    idField.setValue("doc" + (r != null ? r.nextInt(updateDocIDLimit) : incrNumDocsCreated()));
    doc.add(idField);
    String name = docData.getName();
    if (name == null) name = "";
    name = cnt < 0 ? name : name + "_" + cnt;
    Field nameField = ds.getField(NAME_FIELD, storeVal, indexVal, termVecVal);
    nameField.setValue(name);
    doc.add(nameField);
    String date = docData.getDate();
    if (date == null) {
      date = "";
    }
    Field dateField = ds.getField(DATE_FIELD, storeVal, indexVal, termVecVal);
    dateField.setValue(date);
    doc.add(dateField);
    String title = docData.getTitle();
    Field titleField = ds.getField(TITLE_FIELD, storeVal, indexVal, termVecVal);
    titleField.setValue(title == null ? "" : title);
    doc.add(titleField);
    String body = docData.getBody();
    if (body != null && body.length() > 0) {
      String bdy;
      if (size <= 0 || size >= body.length()) {
        bdy = body; 
        docData.setBody(""); 
      } else {
        for (int n = size - 1; n < size + 20 && n < body.length(); n++) {
          if (Character.isWhitespace(body.charAt(n))) {
            size = n;
            break;
          }
        }
        bdy = body.substring(0, size); 
        docData.setBody(body.substring(size)); 
      }
      Field bodyField = ds.getField(BODY_FIELD, bodyStoreVal, bodyIndexVal, termVecVal);
      bodyField.setValue(bdy);
      doc.add(bodyField);
      if (storeBytes) {
        Field bytesField = ds.getField(BYTES_FIELD, Store.YES, Index.NOT_ANALYZED_NO_NORMS, TermVector.NO);
        bytesField.setValue(bdy.getBytes("UTF-8"));
        doc.add(bytesField);
      }
    }
    if (indexProperties) {
      Properties props = docData.getProps();
      if (props != null) {
        for (final Map.Entry<Object,Object> entry : props.entrySet()) {
          Field f = ds.getField((String) entry.getKey(), storeVal, indexVal, termVecVal);
          f.setValue((String) entry.getValue());
          doc.add(f);
        }
        docData.setProps(null);
      }
    }
    return doc;
  }
  private void resetLeftovers() {
    leftovr.set(null);
  }
  protected DocState getDocState() {
    DocState ds = docState.get();
    if (ds == null) {
      ds = new DocState(reuseFields, storeVal, bodyStoreVal, indexVal, bodyIndexVal, termVecVal);
      docState.set(ds);
    }
    return ds;
  }
  protected synchronized int incrNumDocsCreated() {
    return numDocsCreated++;
  }
  public void close() throws IOException {
    source.close();
  }
  public synchronized long getBytesCount() {
    return source.getBytesCount();
  }
  public long getTotalBytesCount() {
    return source.getTotalBytesCount();
  }
  public Document makeDocument() throws Exception {
    resetLeftovers();
    DocData docData = source.getNextDocData(getDocState().docData);
    Document doc = createDocument(docData, 0, -1);
    return doc;
  }
  public Document makeDocument(int size) throws Exception {
    LeftOver lvr = leftovr.get();
    if (lvr == null || lvr.docdata == null || lvr.docdata.getBody() == null
        || lvr.docdata.getBody().length() == 0) {
      resetLeftovers();
    }
    DocData docData = getDocState().docData;
    DocData dd = (lvr == null ? source.getNextDocData(docData) : lvr.docdata);
    int cnt = (lvr == null ? 0 : lvr.cnt);
    while (dd.getBody() == null || dd.getBody().length() < size) {
      DocData dd2 = dd;
      dd = source.getNextDocData(new DocData());
      cnt = 0;
      dd.setBody(dd2.getBody() + dd.getBody());
    }
    Document doc = createDocument(dd, size, cnt);
    if (dd.getBody() == null || dd.getBody().length() == 0) {
      resetLeftovers();
    } else {
      if (lvr == null) {
        lvr = new LeftOver();
        leftovr.set(lvr);
      }
      lvr.docdata = dd;
      lvr.cnt = ++cnt;
    }
    return doc;
  }
  public void printDocStatistics() {
    boolean print = false;
    String col = "                  ";
    StringBuffer sb = new StringBuffer();
    String newline = System.getProperty("line.separator");
    sb.append("------------> ").append(Format.simpleName(getClass())).append(" statistics (").append(printNum).append("): ").append(newline);
    int nut = source.getTotalDocsCount();
    if (nut > lastPrintedNumUniqueTexts) {
      print = true;
      sb.append("total count of unique texts: ").append(Format.format(0,nut,col)).append(newline);
      lastPrintedNumUniqueTexts = nut;
    }
    long nub = getTotalBytesCount();
    if (nub > lastPrintedNumUniqueBytes) {
      print = true;
      sb.append("total bytes of unique texts: ").append(Format.format(0,nub,col)).append(newline);
      lastPrintedNumUniqueBytes = nub;
    }
    if (source.getDocsCount() > 0) {
      print = true;
      sb.append("num docs added since last inputs reset:   ").append(Format.format(0,source.getDocsCount(),col)).append(newline);
      sb.append("total bytes added since last inputs reset: ").append(Format.format(0,getBytesCount(),col)).append(newline);
    }
    if (print) {
      System.out.println(sb.append(newline).toString());
      printNum++;
    }
  }
  public synchronized void resetInputs() throws IOException {
    printDocStatistics();
    setConfig(config);
    source.resetInputs();
    numDocsCreated = 0;
    resetLeftovers();
  }
  public void setConfig(Config config) {
    this.config = config;
    try {
      String sourceClass = config.get("content.source", "org.apache.lucene.benchmark.byTask.feeds.SingleDocSource");
      source = Class.forName(sourceClass).asSubclass(ContentSource.class).newInstance();
      source.setConfig(config);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    boolean stored = config.get("doc.stored", false);
    boolean bodyStored = config.get("doc.body.stored", stored);
    boolean tokenized = config.get("doc.tokenized", true);
    boolean bodyTokenized = config.get("doc.body.tokenized", tokenized);
    boolean norms = config.get("doc.tokenized.norms", false);
    boolean bodyNorms = config.get("doc.body.tokenized.norms", true);
    boolean termVec = config.get("doc.term.vector", false);
    storeVal = (stored ? Field.Store.YES : Field.Store.NO);
    bodyStoreVal = (bodyStored ? Field.Store.YES : Field.Store.NO);
    if (tokenized) {
      indexVal = norms ? Index.ANALYZED : Index.ANALYZED_NO_NORMS;
    } else {
      indexVal = norms ? Index.NOT_ANALYZED : Index.NOT_ANALYZED_NO_NORMS;
    }
    if (bodyTokenized) {
      bodyIndexVal = bodyNorms ? Index.ANALYZED : Index.ANALYZED_NO_NORMS;
    } else {
      bodyIndexVal = bodyNorms ? Index.NOT_ANALYZED : Index.NOT_ANALYZED_NO_NORMS;
    }
    boolean termVecPositions = config.get("doc.term.vector.positions", false);
    boolean termVecOffsets = config.get("doc.term.vector.offsets", false);
    if (termVecPositions && termVecOffsets) {
      termVecVal = TermVector.WITH_POSITIONS_OFFSETS;
    } else if (termVecPositions) {
      termVecVal = TermVector.WITH_POSITIONS;
    } else if (termVecOffsets) {
      termVecVal = TermVector.WITH_OFFSETS;
    } else if (termVec) {
      termVecVal = TermVector.YES;
    } else {
      termVecVal = TermVector.NO;
    }
    storeBytes = config.get("doc.store.body.bytes", false);
    reuseFields = config.get("doc.reuse.fields", true);
    docState = new ThreadLocal<DocState>();
    indexProperties = config.get("doc.index.props", false);
    updateDocIDLimit = config.get("doc.random.id.limit", -1);
    if (updateDocIDLimit != -1) {
      r = new Random(179);
    }
  }
}
