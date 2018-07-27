package org.apache.lucene.misc;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;
public class IndexMergeTool {
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.err.println("Usage: IndexMergeTool <mergedIndex> <index1> <index2> [index3] ...");
      System.exit(1);
    }
    FSDirectory mergedIndex = FSDirectory.open(new File(args[0]));
    IndexWriter writer = new IndexWriter(mergedIndex, new IndexWriterConfig(
        Version.LUCENE_CURRENT, new WhitespaceAnalyzer(Version.LUCENE_CURRENT))
        .setOpenMode(OpenMode.CREATE));
    Directory[] indexes = new Directory[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      indexes[i  - 1] = FSDirectory.open(new File(args[i]));
    }
    System.out.println("Merging...");
    writer.addIndexesNoOptimize(indexes);
    System.out.println("Optimizing...");
    writer.optimize();
    writer.close();
    System.out.println("Done.");
  }
}
