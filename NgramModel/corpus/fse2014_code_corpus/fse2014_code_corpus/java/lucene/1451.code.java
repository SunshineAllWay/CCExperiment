package org.apache.lucene.analysis;
import org.apache.lucene.analysis.tokenattributes.TermAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.Payload;
import org.apache.lucene.index.TermPositions;     
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeImpl;
public class Token extends TermAttributeImpl 
                   implements TypeAttribute, PositionIncrementAttribute,
                              FlagsAttribute, OffsetAttribute, PayloadAttribute {
  private int startOffset,endOffset;
  private String type = DEFAULT_TYPE;
  private int flags;
  private Payload payload;
  private int positionIncrement = 1;
  public Token() {
  }
  public Token(int start, int end) {
    startOffset = start;
    endOffset = end;
  }
  public Token(int start, int end, String typ) {
    startOffset = start;
    endOffset = end;
    type = typ;
  }
  public Token(int start, int end, int flags) {
    startOffset = start;
    endOffset = end;
    this.flags = flags;
  }
  public Token(String text, int start, int end) {
    setTermBuffer(text);
    startOffset = start;
    endOffset = end;
  }
  public Token(String text, int start, int end, String typ) {
    setTermBuffer(text);
    startOffset = start;
    endOffset = end;
    type = typ;
  }
  public Token(String text, int start, int end, int flags) {
    setTermBuffer(text);
    startOffset = start;
    endOffset = end;
    this.flags = flags;
  }
  public Token(char[] startTermBuffer, int termBufferOffset, int termBufferLength, int start, int end) {
    setTermBuffer(startTermBuffer, termBufferOffset, termBufferLength);
    startOffset = start;
    endOffset = end;
  }
  public void setPositionIncrement(int positionIncrement) {
    if (positionIncrement < 0)
      throw new IllegalArgumentException
        ("Increment must be zero or greater: " + positionIncrement);
    this.positionIncrement = positionIncrement;
  }
  public int getPositionIncrement() {
    return positionIncrement;
  }
  public final int startOffset() {
    return startOffset;
  }
  public void setStartOffset(int offset) {
    this.startOffset = offset;
  }
  public final int endOffset() {
    return endOffset;
  }
  public void setEndOffset(int offset) {
    this.endOffset = offset;
  }
  public void setOffset(int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }
  public final String type() {
    return type;
  }
  public final void setType(String type) {
    this.type = type;
  }
  public int getFlags() {
    return flags;
  }
  public void setFlags(int flags) {
    this.flags = flags;
  }
  public Payload getPayload() {
    return this.payload;
  }
  public void setPayload(Payload payload) {
    this.payload = payload;
  }
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('(').append(term()).append(',')
      .append(startOffset).append(',').append(endOffset);
    if (!"word".equals(type))
      sb.append(",type=").append(type);
    if (positionIncrement != 1)
      sb.append(",posIncr=").append(positionIncrement);
    sb.append(')');
    return sb.toString();
  }
  @Override
  public void clear() {
    super.clear();
    payload = null;
    positionIncrement = 1;
    flags = 0;
    startOffset = endOffset = 0;
    type = DEFAULT_TYPE;
  }
  @Override
  public Object clone() {
    Token t = (Token)super.clone();
    if (payload != null) {
      t.payload = (Payload) payload.clone();
    }
    return t;
  }
  public Token clone(char[] newTermBuffer, int newTermOffset, int newTermLength, int newStartOffset, int newEndOffset) {
    final Token t = new Token(newTermBuffer, newTermOffset, newTermLength, newStartOffset, newEndOffset);
    t.positionIncrement = positionIncrement;
    t.flags = flags;
    t.type = type;
    if (payload != null)
      t.payload = (Payload) payload.clone();
    return t;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj instanceof Token) {
      final Token other = (Token) obj;
      return (startOffset == other.startOffset &&
          endOffset == other.endOffset && 
          flags == other.flags &&
          positionIncrement == other.positionIncrement &&
          (type == null ? other.type == null : type.equals(other.type)) &&
          (payload == null ? other.payload == null : payload.equals(other.payload)) &&
          super.equals(obj)
      );
    } else
      return false;
  }
  @Override
  public int hashCode() {
    int code = super.hashCode();
    code = code * 31 + startOffset;
    code = code * 31 + endOffset;
    code = code * 31 + flags;
    code = code * 31 + positionIncrement;
    if (type != null)
      code = code * 31 + type.hashCode();
    if (payload != null)
      code = code * 31 + payload.hashCode();
    return code;
  }
  private void clearNoTermBuffer() {
    payload = null;
    positionIncrement = 1;
    flags = 0;
    startOffset = endOffset = 0;
    type = DEFAULT_TYPE;
  }
  public Token reinit(char[] newTermBuffer, int newTermOffset, int newTermLength, int newStartOffset, int newEndOffset, String newType) {
    clearNoTermBuffer();
    payload = null;
    positionIncrement = 1;
    setTermBuffer(newTermBuffer, newTermOffset, newTermLength);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = newType;
    return this;
  }
  public Token reinit(char[] newTermBuffer, int newTermOffset, int newTermLength, int newStartOffset, int newEndOffset) {
    clearNoTermBuffer();
    setTermBuffer(newTermBuffer, newTermOffset, newTermLength);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = DEFAULT_TYPE;
    return this;
  }
  public Token reinit(String newTerm, int newStartOffset, int newEndOffset, String newType) {
    clearNoTermBuffer();
    setTermBuffer(newTerm);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = newType;
    return this;
  }
  public Token reinit(String newTerm, int newTermOffset, int newTermLength, int newStartOffset, int newEndOffset, String newType) {
    clearNoTermBuffer();
    setTermBuffer(newTerm, newTermOffset, newTermLength);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = newType;
    return this;
  }
  public Token reinit(String newTerm, int newStartOffset, int newEndOffset) {
    clearNoTermBuffer();
    setTermBuffer(newTerm);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = DEFAULT_TYPE;
    return this;
  }
  public Token reinit(String newTerm, int newTermOffset, int newTermLength, int newStartOffset, int newEndOffset) {
    clearNoTermBuffer();
    setTermBuffer(newTerm, newTermOffset, newTermLength);
    startOffset = newStartOffset;
    endOffset = newEndOffset;
    type = DEFAULT_TYPE;
    return this;
  }
  public void reinit(Token prototype) {
    setTermBuffer(prototype.termBuffer(), 0, prototype.termLength());
    positionIncrement = prototype.positionIncrement;
    flags = prototype.flags;
    startOffset = prototype.startOffset;
    endOffset = prototype.endOffset;
    type = prototype.type;
    payload =  prototype.payload;
  }
  public void reinit(Token prototype, String newTerm) {
    setTermBuffer(newTerm);
    positionIncrement = prototype.positionIncrement;
    flags = prototype.flags;
    startOffset = prototype.startOffset;
    endOffset = prototype.endOffset;
    type = prototype.type;
    payload =  prototype.payload;
  }
  public void reinit(Token prototype, char[] newTermBuffer, int offset, int length) {
    setTermBuffer(newTermBuffer, offset, length);
    positionIncrement = prototype.positionIncrement;
    flags = prototype.flags;
    startOffset = prototype.startOffset;
    endOffset = prototype.endOffset;
    type = prototype.type;
    payload =  prototype.payload;
  }
  @Override
  public void copyTo(AttributeImpl target) {
    if (target instanceof Token) {
      final Token to = (Token) target;
      to.reinit(this);
      if (payload !=null) {
        to.payload = (Payload) payload.clone();
      }
    } else {
      super.copyTo(target);
      ((OffsetAttribute) target).setOffset(startOffset, endOffset);
      ((PositionIncrementAttribute) target).setPositionIncrement(positionIncrement);
      ((PayloadAttribute) target).setPayload((payload == null) ? null : (Payload) payload.clone());
      ((FlagsAttribute) target).setFlags(flags);
      ((TypeAttribute) target).setType(type);
    }
  }
  public static final AttributeSource.AttributeFactory TOKEN_ATTRIBUTE_FACTORY =
    new TokenAttributeFactory(AttributeSource.AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
  public static final class TokenAttributeFactory extends AttributeSource.AttributeFactory {
    private final AttributeSource.AttributeFactory delegate;
    public TokenAttributeFactory(AttributeSource.AttributeFactory delegate) {
      this.delegate = delegate;
    }
    @Override
    public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
      return attClass.isAssignableFrom(Token.class)
        ? new Token() : delegate.createAttributeInstance(attClass);
    }
    @Override
    public boolean equals(Object other) {
      if (this == other) return true;
      if (other instanceof TokenAttributeFactory) {
        final TokenAttributeFactory af = (TokenAttributeFactory) other;
        return this.delegate.equals(af.delegate);
      }
      return false;
    }
    @Override
    public int hashCode() {
      return delegate.hashCode() ^ 0x0a45aa31;
    }
  }
}
