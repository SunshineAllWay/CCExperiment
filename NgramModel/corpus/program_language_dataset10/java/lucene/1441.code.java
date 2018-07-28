package org.apache.lucene.analysis;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.document.NumericField; 
import org.apache.lucene.search.NumericRangeQuery; 
import org.apache.lucene.search.NumericRangeFilter; 
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
public final class NumericTokenStream extends TokenStream {
  public static final String TOKEN_TYPE_FULL_PREC  = "fullPrecNumeric";
  public static final String TOKEN_TYPE_LOWER_PREC = "lowerPrecNumeric";
  public NumericTokenStream() {
    this(NumericUtils.PRECISION_STEP_DEFAULT);
  }
  public NumericTokenStream(final int precisionStep) {
    super();
    this.precisionStep = precisionStep;
    if (precisionStep < 1)
      throw new IllegalArgumentException("precisionStep must be >=1");
  }
  public NumericTokenStream(AttributeSource source, final int precisionStep) {
    super(source);
    this.precisionStep = precisionStep;
    if (precisionStep < 1)
      throw new IllegalArgumentException("precisionStep must be >=1");
  }
  public NumericTokenStream(AttributeFactory factory, final int precisionStep) {
    super(factory);
    this.precisionStep = precisionStep;
    if (precisionStep < 1)
      throw new IllegalArgumentException("precisionStep must be >=1");
  }
  public NumericTokenStream setLongValue(final long value) {
    this.value = value;
    valSize = 64;
    shift = 0;
    return this;
  }
  public NumericTokenStream setIntValue(final int value) {
    this.value = value;
    valSize = 32;
    shift = 0;
    return this;
  }
  public NumericTokenStream setDoubleValue(final double value) {
    this.value = NumericUtils.doubleToSortableLong(value);
    valSize = 64;
    shift = 0;
    return this;
  }
  public NumericTokenStream setFloatValue(final float value) {
    this.value = NumericUtils.floatToSortableInt(value);
    valSize = 32;
    shift = 0;
    return this;
  }
  @Override
  public void reset() {
    if (valSize == 0)
      throw new IllegalStateException("call set???Value() before usage");
    shift = 0;
  }
  @Override
  public boolean incrementToken() {
    if (valSize == 0)
      throw new IllegalStateException("call set???Value() before usage");
    if (shift >= valSize)
      return false;
    clearAttributes();
    final char[] buffer;
    switch (valSize) {
      case 64:
        buffer = termAtt.resizeTermBuffer(NumericUtils.BUF_SIZE_LONG);
        termAtt.setTermLength(NumericUtils.longToPrefixCoded(value, shift, buffer));
        break;
      case 32:
        buffer = termAtt.resizeTermBuffer(NumericUtils.BUF_SIZE_INT);
        termAtt.setTermLength(NumericUtils.intToPrefixCoded((int) value, shift, buffer));
        break;
      default:
        throw new IllegalArgumentException("valSize must be 32 or 64");
    }
    typeAtt.setType((shift == 0) ? TOKEN_TYPE_FULL_PREC : TOKEN_TYPE_LOWER_PREC);
    posIncrAtt.setPositionIncrement((shift == 0) ? 1 : 0);
    shift += precisionStep;
    return true;
  }
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(numeric,valSize=").append(valSize);
    sb.append(",precisionStep=").append(precisionStep).append(')');
    return sb.toString();
  }
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private int shift = 0, valSize = 0; 
  private final int precisionStep;
  private long value = 0L;
}
