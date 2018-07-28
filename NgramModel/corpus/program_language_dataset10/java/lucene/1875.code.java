package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.IndexOutput;
import java.io.IOException;
public class TestFieldInfos extends LuceneTestCase {
  private Document testDoc = new Document();
  public TestFieldInfos(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DocHelper.setupDoc(testDoc);
  }
  public void test() throws IOException {
    assertTrue(testDoc != null);
    FieldInfos fieldInfos = new FieldInfos();
    fieldInfos.add(testDoc);
    assertTrue(fieldInfos.size() == DocHelper.all.size()); 
    RAMDirectory dir = new RAMDirectory();
    String name = "testFile";
    IndexOutput output = dir.createOutput(name);
    assertTrue(output != null);
    try {
      fieldInfos.write(output);
      output.close();
      assertTrue(output.length() > 0);
      FieldInfos readIn = new FieldInfos(dir, name);
      assertTrue(fieldInfos.size() == readIn.size());
      FieldInfo info = readIn.fieldInfo("textField1");
      assertTrue(info != null);
      assertTrue(info.storeTermVector == false);
      assertTrue(info.omitNorms == false);
      info = readIn.fieldInfo("textField2");
      assertTrue(info != null);
      assertTrue(info.storeTermVector == true);
      assertTrue(info.omitNorms == false);
      info = readIn.fieldInfo("textField3");
      assertTrue(info != null);
      assertTrue(info.storeTermVector == false);
      assertTrue(info.omitNorms == true);
      info = readIn.fieldInfo("omitNorms");
      assertTrue(info != null);
      assertTrue(info.storeTermVector == false);
      assertTrue(info.omitNorms == true);
      dir.close();
    } catch (IOException e) {
      assertTrue(false);
    }
  }
}
