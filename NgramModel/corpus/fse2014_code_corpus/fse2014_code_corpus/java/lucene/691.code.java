package org.apache.lucene.analysis.payloads;
import org.apache.lucene.index.Payload;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
public class IdentityEncoder extends AbstractEncoder implements PayloadEncoder{
  protected Charset charset = Charset.forName("UTF-8");
  @Deprecated
  protected String charsetName = charset.name();
  public IdentityEncoder() {
  }
  public IdentityEncoder(Charset charset) {
    this.charset = charset;
    charsetName = charset.name();
  }
  public Payload encode(char[] buffer, int offset, int length) {
    final ByteBuffer bb = charset.encode(CharBuffer.wrap(buffer, offset, length));
    if (bb.hasArray()) {
      return new Payload(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
    } else {
      final byte[] b = new byte[bb.remaining()];
      bb.get(b);
      return new Payload(b);
    }
  }
}
