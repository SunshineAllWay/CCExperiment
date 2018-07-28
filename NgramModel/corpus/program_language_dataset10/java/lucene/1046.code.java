package org.apache.lucene.index;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.Version;
public class MultiPassIndexSplitter {
  public void split(IndexReader input, Directory[] outputs, boolean seq) throws IOException {
    if (outputs == null || outputs.length < 2) {
      throw new IOException("Invalid number of outputs.");
    }
    if (input == null || input.numDocs() < 2) {
      throw new IOException("Not enough documents for splitting");
    }
    int numParts = outputs.length;
    input = new FakeDeleteIndexReader(input);
    int maxDoc = input.maxDoc();
    int partLen = maxDoc / numParts;
    for (int i = 0; i < numParts; i++) {
      input.undeleteAll();
      if (seq) { 
        int lo = partLen * i;
        int hi = lo + partLen;
        for (int j = 0; j < lo; j++) {
          input.deleteDocument(j);
        }
        if (i < numParts - 1) {
          for (int j = hi; j < maxDoc; j++) {
            input.deleteDocument(j);
          }
        }
      } else {
        for (int j = 0; j < maxDoc; j++) {
          if ((j + numParts - i) % numParts != 0) {
            input.deleteDocument(j);
          }
        }
      }
      IndexWriter w = new IndexWriter(outputs[i], new IndexWriterConfig(
          Version.LUCENE_CURRENT,
          new WhitespaceAnalyzer(Version.LUCENE_CURRENT))
          .setOpenMode(OpenMode.CREATE));
      System.err.println("Writing part " + (i + 1) + " ...");
      w.addIndexes(new IndexReader[]{input});
      w.close();
    }
    System.err.println("Done.");
  }
  public static void main(String[] args) throws Exception {
    if (args.length < 5) {
      System.err.println("Usage: MultiPassIndexSplitter -out <outputDir> -num <numParts> [-seq] <inputIndex1> [<inputIndex2 ...]");
      System.err.println("\tinputIndex\tpath to input index, multiple values are ok");
      System.err.println("\t-out ouputDir\tpath to output directory to contain partial indexes");
      System.err.println("\t-num numParts\tnumber of parts to produce");
      System.err.println("\t-seq\tsequential docid-range split (default is round-robin)");
      System.exit(-1);
    }
    ArrayList<IndexReader> indexes = new ArrayList<IndexReader>();
    String outDir = null;
    int numParts = -1;
    boolean seq = false;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-out")) {
        outDir = args[++i];
      } else if (args[i].equals("-num")) {
        numParts = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-seq")) {
        seq = true;
      } else {
        File file = new File(args[i]);
        if (!file.exists() || !file.isDirectory()) {
          System.err.println("Invalid input path - skipping: " + file);
          continue;
        }
        Directory dir = FSDirectory.open(new File(args[i]));
        try {
          if (!IndexReader.indexExists(dir)) {
            System.err.println("Invalid input index - skipping: " + file);
            continue;
          }
        } catch (Exception e) {
          System.err.println("Invalid input index - skipping: " + file);
          continue;
        }
        indexes.add(IndexReader.open(dir, true));
      }
    }
    if (outDir == null) {
      throw new Exception("Required argument missing: -out outputDir");
    }
    if (numParts < 2) {
      throw new Exception("Invalid value of required argument: -num numParts");
    }
    if (indexes.size() == 0) {
      throw new Exception("No input indexes to process");
    }
    File out = new File(outDir);
    if (!out.mkdirs()) {
      throw new Exception("Can't create output directory: " + out);
    }
    Directory[] dirs = new Directory[numParts];
    for (int i = 0; i < numParts; i++) {
      dirs[i] = FSDirectory.open(new File(out, "part-" + i));
    }
    MultiPassIndexSplitter splitter = new MultiPassIndexSplitter();
    IndexReader input;
    if (indexes.size() == 1) {
      input = indexes.get(0);
    } else {
      input = new MultiReader(indexes.toArray(new IndexReader[indexes.size()]));
    }
    splitter.split(input, dirs, seq);
  }
  public static class FakeDeleteIndexReader extends FilterIndexReader {
    OpenBitSet dels;
    OpenBitSet oldDels = null;
    public FakeDeleteIndexReader(IndexReader in) {
      super(in);
      dels = new OpenBitSet(in.maxDoc());
      if (in.hasDeletions()) {
        oldDels = new OpenBitSet(in.maxDoc());
        for (int i = 0; i < in.maxDoc(); i++) {
          if (in.isDeleted(i)) oldDels.set(i);
        }
        dels.or(oldDels);
      }
    }
    @Override
    public int numDocs() {
      return in.maxDoc() - (int)dels.cardinality();
    }
    @Override
    protected void doUndeleteAll() throws CorruptIndexException, IOException {
      dels = new OpenBitSet(in.maxDoc());
      if (oldDels != null) {
        dels.or(oldDels);
      }
    }
    @Override
    protected void doDelete(int n) throws CorruptIndexException, IOException {
      dels.set(n);
    }
    @Override
    public boolean hasDeletions() {
      return !dels.isEmpty();
    }
    @Override
    public boolean isDeleted(int n) {
      return dels.get(n);
    }
    @Override
    public TermPositions termPositions() throws IOException {
      return new FilterTermPositions(in.termPositions()) {
        @Override
        public boolean next() throws IOException {
          boolean res;
          while ((res = super.next())) {
            if (!dels.get(doc())) {
              break;
            }
          }
          return res;
        }        
      };
    }
  }
}
