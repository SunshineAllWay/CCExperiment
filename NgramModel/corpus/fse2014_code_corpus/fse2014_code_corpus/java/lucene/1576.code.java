package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.util.Map;
class ReadOnlyDirectoryReader extends DirectoryReader {
  ReadOnlyDirectoryReader(Directory directory, SegmentInfos sis, IndexDeletionPolicy deletionPolicy, int termInfosIndexDivisor) throws IOException {
    super(directory, sis, deletionPolicy, true, termInfosIndexDivisor);
  }
  ReadOnlyDirectoryReader(Directory directory, SegmentInfos infos, SegmentReader[] oldReaders, int[] oldStarts,  Map<String,byte[]> oldNormsCache, boolean doClone,
                          int termInfosIndexDivisor) throws IOException {
    super(directory, infos, oldReaders, oldStarts, oldNormsCache, true, doClone, termInfosIndexDivisor);
  }
  ReadOnlyDirectoryReader(IndexWriter writer, SegmentInfos infos, int termInfosIndexDivisor) throws IOException {
    super(writer, infos, termInfosIndexDivisor);
  }
  @Override
  protected void acquireWriteLock() {
    ReadOnlySegmentReader.noWrite();
  }
}
