package org.apache.lucene.analysis.payloads;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.ArrayUtil;
public class IntegerEncoder extends AbstractEncoder implements PayloadEncoder {
  public Payload encode(char[] buffer, int offset, int length) {
    Payload result = new Payload();
    int payload = ArrayUtil.parseInt(buffer, offset, length);
    byte[] bytes = PayloadHelper.encodeInt(payload);
    result.setData(bytes);
    return result;
  }
}