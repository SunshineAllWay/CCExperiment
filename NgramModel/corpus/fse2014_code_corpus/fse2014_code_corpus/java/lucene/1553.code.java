package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.document.Fieldable;
abstract class InvertedDocConsumerPerField {
  abstract boolean start(Fieldable[] fields, int count) throws IOException;
  abstract void start(Fieldable field);
  abstract void add() throws IOException;
  abstract void finish() throws IOException;
  abstract void abort();
}
