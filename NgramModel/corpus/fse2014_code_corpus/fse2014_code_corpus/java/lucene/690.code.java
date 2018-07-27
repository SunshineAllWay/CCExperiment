package org.apache.lucene.analysis.payloads;
import org.apache.lucene.index.Payload;
public class FloatEncoder extends AbstractEncoder implements PayloadEncoder {
  public Payload encode(char[] buffer, int offset, int length) {
    Payload result = new Payload();
    float payload = Float.parseFloat(new String(buffer, offset, length));
    byte[] bytes = PayloadHelper.encodeFloat(payload);
    result.setData(bytes);
    return result;
  }
}
