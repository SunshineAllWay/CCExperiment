package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import java.io.*;
import java.util.*;
public class TestIndexFileDeleter extends LuceneTestCase {
  public void testDeleteLeftoverFiles() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setMaxBufferedDocs(10));
    int i;
    for(i=0;i<35;i++) {
      addDoc(writer, i);
    }
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false);
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false);
    for(;i<45;i++) {
      addDoc(writer, i);
    }
    writer.close();
    IndexReader reader = IndexReader.open(dir, false);
    Term searchTerm = new Term("id", "7");
    int delCount = reader.deleteDocuments(searchTerm);
    assertEquals("didn't delete the right number of documents", 1, delCount);
    reader.setNorm(21, "content", (float) 1.5);
    reader.close();
    String[] files = dir.listAll();
    CompoundFileReader cfsReader = new CompoundFileReader(dir, "_2.cfs");
    FieldInfos fieldInfos = new FieldInfos(cfsReader, "_2.fnm");
    int contentFieldIndex = -1;
    for(i=0;i<fieldInfos.size();i++) {
      FieldInfo fi = fieldInfos.fieldInfo(i);
      if (fi.name.equals("content")) {
        contentFieldIndex = i;
        break;
      }
    }
    cfsReader.close();
    assertTrue("could not locate the 'content' field number in the _2.cfs segment", contentFieldIndex != -1);
    String normSuffix = "s" + contentFieldIndex;
    copyFile(dir, "_2_1." + normSuffix, "_2_2." + normSuffix);
    copyFile(dir, "_2_1." + normSuffix, "_2_2.f" + contentFieldIndex);
    copyFile(dir, "_2_1." + normSuffix, "_1_1." + normSuffix);
    copyFile(dir, "_2_1." + normSuffix, "_1_1.f" + contentFieldIndex);
    copyFile(dir, "_0_1.del", "_0_2.del");
    copyFile(dir, "_0_1.del", "_1_1.del");
    copyFile(dir, "_0_1.del", "_188_1.del");
    copyFile(dir, "_0.cfs", "_188.cfs");
    copyFile(dir, "_0.cfs", "_0.fnm");
    copyFile(dir, "_0.cfs", "deletable");
    copyFile(dir, "segments_3", "segments");
    copyFile(dir, "segments_3", "segments_2");
    copyFile(dir, "_2.cfs", "_3.cfs");
    String[] filesPre = dir.listAll();
    writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
    writer.close();
    String[] files2 = dir.listAll();
    dir.close();
    Arrays.sort(files);
    Arrays.sort(files2);
    Set<String> dif = difFiles(files, files2);
    if (!Arrays.equals(files, files2)) {
      fail("IndexFileDeleter failed to delete unreferenced extra files: should have deleted " + (filesPre.length-files.length) + " files but only deleted " + (filesPre.length - files2.length) + "; expected files:\n    " + asString(files) + "\n  actual files:\n    " + asString(files2)+"\ndif: "+dif);
    }
  }
  private static Set<String> difFiles(String[] files1, String[] files2) {
    Set<String> set1 = new HashSet<String>();
    Set<String> set2 = new HashSet<String>();
    Set<String> extra = new HashSet<String>();
    for (int x=0; x < files1.length; x++) {
      set1.add(files1[x]);
    }
    for (int x=0; x < files2.length; x++) {
      set2.add(files2[x]);
    }
    Iterator<String> i1 = set1.iterator();
    while (i1.hasNext()) {
      String o = i1.next();
      if (!set2.contains(o)) {
        extra.add(o);
      }
    }
    Iterator<String> i2 = set2.iterator();
    while (i2.hasNext()) {
      String o = i2.next();
      if (!set1.contains(o)) {
        extra.add(o);
      }
    }
    return extra;
  }
  private String asString(String[] l) {
    String s = "";
    for(int i=0;i<l.length;i++) {
      if (i > 0) {
        s += "\n    ";
      }
      s += l[i];
    }
    return s;
  }
  public void copyFile(Directory dir, String src, String dest) throws IOException {
    IndexInput in = dir.openInput(src);
    IndexOutput out = dir.createOutput(dest);
    byte[] b = new byte[1024];
    long remainder = in.length();
    while(remainder > 0) {
      int len = (int) Math.min(b.length, remainder);
      in.readBytes(b, 0, len);
      out.writeBytes(b, len);
      remainder -= len;
    }
    in.close();
    out.close();
  }
  private void addDoc(IndexWriter writer, int id) throws IOException
  {
    Document doc = new Document();
    doc.add(new Field("content", "aaa", Field.Store.NO, Field.Index.ANALYZED));
    doc.add(new Field("id", Integer.toString(id), Field.Store.YES, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
  }
}
