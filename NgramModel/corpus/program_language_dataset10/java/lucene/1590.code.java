package org.apache.lucene.index;
import java.util.HashSet;
import java.util.Collection;
import org.apache.lucene.store.Directory;
class SegmentWriteState {
  DocumentsWriter docWriter;
  Directory directory;
  String segmentName;
  String docStoreSegmentName;
  int numDocs;
  int termIndexInterval;
  int numDocsInStore;
  Collection<String> flushedFiles;
  public SegmentWriteState(DocumentsWriter docWriter, Directory directory, String segmentName, String docStoreSegmentName, int numDocs,
                           int numDocsInStore, int termIndexInterval) {
    this.docWriter = docWriter;
    this.directory = directory;
    this.segmentName = segmentName;
    this.docStoreSegmentName = docStoreSegmentName;
    this.numDocs = numDocs;
    this.numDocsInStore = numDocsInStore;
    this.termIndexInterval = termIndexInterval;
    flushedFiles = new HashSet<String>();
  }
  public String segmentFileName(String ext) {
    return segmentName + "." + ext;
  }
}
