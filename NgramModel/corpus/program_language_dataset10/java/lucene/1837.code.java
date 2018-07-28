package org.apache.lucene.analysis;
import java.io.StringReader;
import org.apache.lucene.util.LuceneTestCase;
public class TestCharFilter extends LuceneTestCase {
  public void testCharFilter1() throws Exception {
    CharStream cs = new CharFilter1( CharReader.get( new StringReader("") ) );
    assertEquals( "corrected offset is invalid", 1, cs.correctOffset( 0 ) );
  }
  public void testCharFilter2() throws Exception {
    CharStream cs = new CharFilter2( CharReader.get( new StringReader("") ) );
    assertEquals( "corrected offset is invalid", 2, cs.correctOffset( 0 ) );
  }
  public void testCharFilter12() throws Exception {
    CharStream cs = new CharFilter2( new CharFilter1( CharReader.get( new StringReader("") ) ) );
    assertEquals( "corrected offset is invalid", 3, cs.correctOffset( 0 ) );
  }
  public void testCharFilter11() throws Exception {
    CharStream cs = new CharFilter1( new CharFilter1( CharReader.get( new StringReader("") ) ) );
    assertEquals( "corrected offset is invalid", 2, cs.correctOffset( 0 ) );
  }
  static class CharFilter1 extends CharFilter {
    protected CharFilter1(CharStream in) {
      super(in);
    }
    @Override
    protected int correct(int currentOff) {
      return currentOff + 1;
    }
  }
  static class CharFilter2 extends CharFilter {
    protected CharFilter2(CharStream in) {
      super(in);
    }
    @Override
    protected int correct(int currentOff) {
      return currentOff + 2;
    }
  }
}
