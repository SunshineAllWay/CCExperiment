package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;
abstract class FormatPostingsTermsConsumer {
  abstract FormatPostingsDocsConsumer addTerm(char[] text, int start) throws IOException;
  char[] termBuffer;
  FormatPostingsDocsConsumer addTerm(String text) throws IOException {
    final int len = text.length();
    if (termBuffer == null || termBuffer.length < 1+len)
      termBuffer = new char[ArrayUtil.oversize(1+len, RamUsageEstimator.NUM_BYTES_CHAR)];
    text.getChars(0, len, termBuffer, 0);
    termBuffer[len] = 0xffff;
    return addTerm(termBuffer, 0);
  }
  abstract void finish() throws IOException;
}
