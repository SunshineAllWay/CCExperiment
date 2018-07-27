package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.document.Fieldable;
abstract class DocFieldConsumerPerField {
  abstract void processFields(Fieldable[] fields, int count) throws IOException;
  abstract void abort();
}
