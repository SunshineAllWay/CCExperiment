package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
abstract class TermsHashConsumer {
  abstract int bytesPerPosting();
  abstract void createPostings(RawPostingList[] postings, int start, int count);
  abstract TermsHashConsumerPerThread addThread(TermsHashPerThread perThread);
  abstract void flush(Map<TermsHashConsumerPerThread,Collection<TermsHashConsumerPerField>> threadsAndFields, final SegmentWriteState state) throws IOException;
  abstract void abort();
  abstract void closeDocStore(SegmentWriteState state) throws IOException;
  FieldInfos fieldInfos;
  void setFieldInfos(FieldInfos fieldInfos) {
    this.fieldInfos = fieldInfos;
  }
}
