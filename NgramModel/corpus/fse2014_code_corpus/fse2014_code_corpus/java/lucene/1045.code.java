package org.apache.lucene.index;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.store.FSDirectory;
public class IndexSplitter {
  public SegmentInfos infos;
  FSDirectory fsDir;
  File dir;
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: IndexSplitter <srcDir> -l (list the segments and their sizes)");
      System.err.println("IndexSplitter <srcDir> <destDir> <segments>+");
      System.err
          .println("IndexSplitter <srcDir> -d (delete the following segments)");
      return;
    }
    File srcDir = new File(args[0]);
    IndexSplitter is = new IndexSplitter(srcDir);
    if (!srcDir.exists()) {
      throw new Exception("srcdir:" + srcDir.getAbsolutePath()
          + " doesn't exist");
    }
    if (args[1].equals("-l")) {
      is.listSegments();
    } else if (args[1].equals("-d")) {
      List<String> segs = new ArrayList<String>();
      for (int x = 2; x < args.length; x++) {
        segs.add(args[x]);
      }
      is.remove(segs.toArray(new String[0]));
    } else {
      File targetDir = new File(args[1]);
      List<String> segs = new ArrayList<String>();
      for (int x = 2; x < args.length; x++) {
        segs.add(args[x]);
      }
      is.split(targetDir, segs.toArray(new String[0]));
    }
  }
  public IndexSplitter(File dir) throws IOException {
    this.dir = dir;
    fsDir = FSDirectory.open(dir);
    infos = new SegmentInfos();
    infos.read(fsDir);
  }
  public void listSegments() throws IOException {
    DecimalFormat formatter = new DecimalFormat("###,###.###");
    for (int x = 0; x < infos.size(); x++) {
      SegmentInfo info = infos.info(x);
      String sizeStr = formatter.format(info.sizeInBytes());
      System.out.println(info.name + " " + sizeStr);
    }
  }
  private int getIdx(String name) {
    for (int x = 0; x < infos.size(); x++) {
      if (name.equals(infos.info(x).name))
        return x;
    }
    return -1;
  }
  private SegmentInfo getInfo(String name) {
    for (int x = 0; x < infos.size(); x++) {
      if (name.equals(infos.info(x).name))
        return infos.info(x);
    }
    return null;
  }
  public void remove(String[] segs) throws IOException {
    for (String n : segs) {
      int idx = getIdx(n);
      infos.remove(idx);
    }
    infos.commit(fsDir);
  }
  public void split(File destDir, String[] segs) throws IOException {
    destDir.mkdirs();
    FSDirectory destFSDir = FSDirectory.open(destDir);
    SegmentInfos destInfos = new SegmentInfos();
    for (String n : segs) {
      SegmentInfo info = getInfo(n);
      destInfos.add(info);
      List<String> files = info.files();
      for (final String srcName : files) {
        File srcFile = new File(dir, srcName);
        File destFile = new File(destDir, srcName);
        copyFile(srcFile, destFile);
      }
    }
    destInfos.commit(destFSDir);
  }
  private static final byte[] copyBuffer = new byte[32*1024];
  private static void copyFile(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    int len;
    while ((len = in.read(copyBuffer)) > 0) {
      out.write(copyBuffer, 0, len);
    }
    in.close();
    out.close();
  }
}
