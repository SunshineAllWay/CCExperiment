package org.apache.lucene.analysis;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public class TestNumericTokenStream extends BaseTokenStreamTestCase {
  static final long lvalue = 4573245871874382L;
  static final int ivalue = 123456;
  public void testLongStream() throws Exception {
    final NumericTokenStream stream=new NumericTokenStream().setLongValue(lvalue);
    final TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    final TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
    for (int shift=0; shift<64; shift+=NumericUtils.PRECISION_STEP_DEFAULT) {
      assertTrue("New token is available", stream.incrementToken());
      assertEquals("Term is correctly encoded", NumericUtils.longToPrefixCoded(lvalue, shift), termAtt.term());
      assertEquals("Type correct", (shift == 0) ? NumericTokenStream.TOKEN_TYPE_FULL_PREC : NumericTokenStream.TOKEN_TYPE_LOWER_PREC, typeAtt.type());
    }
    assertFalse("No more tokens available", stream.incrementToken());
  }
  public void testIntStream() throws Exception {
    final NumericTokenStream stream=new NumericTokenStream().setIntValue(ivalue);
    final TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    final TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
    for (int shift=0; shift<32; shift+=NumericUtils.PRECISION_STEP_DEFAULT) {
      assertTrue("New token is available", stream.incrementToken());
      assertEquals("Term is correctly encoded", NumericUtils.intToPrefixCoded(ivalue, shift), termAtt.term());
      assertEquals("Type correct", (shift == 0) ? NumericTokenStream.TOKEN_TYPE_FULL_PREC : NumericTokenStream.TOKEN_TYPE_LOWER_PREC, typeAtt.type());
    }
    assertFalse("No more tokens available", stream.incrementToken());
  }
  public void testNotInitialized() throws Exception {
    final NumericTokenStream stream=new NumericTokenStream();
    try {
      stream.reset();
      fail("reset() should not succeed.");
    } catch (IllegalStateException e) {
    }
    try {
      stream.incrementToken();
      fail("incrementToken() should not succeed.");
    } catch (IllegalStateException e) {
    }
  }
}
