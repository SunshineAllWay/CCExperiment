package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.document.Fieldable;
abstract class TermsHashConsumerPerField {
  abstract boolean start(Fieldable[] fields, int count) throws IOException;
  abstract void finish() throws IOException;
  abstract void skippingLongTerm() throws IOException;
  abstract void start(Fieldable field);
  abstract void newTerm(RawPostingList p) throws IOException;
  abstract void addTerm(RawPostingList p) throws IOException;
  abstract int getStreamCount();
}
