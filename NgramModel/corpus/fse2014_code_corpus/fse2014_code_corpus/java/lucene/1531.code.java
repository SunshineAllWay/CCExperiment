package org.apache.lucene.index;
import java.io.IOException;
abstract class FormatPostingsDocsConsumer {
  abstract FormatPostingsPositionsConsumer addDoc(int docID, int termDocFreq) throws IOException;
  abstract void finish() throws IOException;
}
