package org.apache.lucene.analysis;
import java.io.StringReader;
public class TestMappingCharFilter extends BaseTokenStreamTestCase {
  NormalizeCharMap normMap;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    normMap = new NormalizeCharMap();
    normMap.add( "aa", "a" );
    normMap.add( "bbb", "b" );
    normMap.add( "cccc", "cc" );
    normMap.add( "h", "i" );
    normMap.add( "j", "jj" );
    normMap.add( "k", "kkk" );
    normMap.add( "ll", "llll" );
    normMap.add( "empty", "" );
  }
  public void testReaderReset() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "x" ) );
    char[] buf = new char[10];
    int len = cs.read(buf, 0, 10);
    assertEquals( 1, len );
    assertEquals( 'x', buf[0]) ;
    len = cs.read(buf, 0, 10);
    assertEquals( -1, len );
    cs.reset();
    len = cs.read(buf, 0, 10);
    assertEquals( 1, len );
    assertEquals( 'x', buf[0]) ;
  }
  public void testNothingChange() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "x" ) );
    TokenStream ts = new WhitespaceTokenizer(TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"x"}, new int[]{0}, new int[]{1});
  }
  public void test1to1() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "h" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"i"}, new int[]{0}, new int[]{1});
  }
  public void test1to2() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "j" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"jj"}, new int[]{0}, new int[]{1});
  }
  public void test1to3() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "k" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"kkk"}, new int[]{0}, new int[]{1});
  }
  public void test2to4() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "ll" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"llll"}, new int[]{0}, new int[]{2});
  }
  public void test2to1() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "aa" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"a"}, new int[]{0}, new int[]{2});
  }
  public void test3to1() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "bbb" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"b"}, new int[]{0}, new int[]{3});
  }
  public void test4to2() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "cccc" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[]{"cc"}, new int[]{0}, new int[]{4});
  }
  public void test5to0() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, new StringReader( "empty" ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts, new String[0]);
  }
  public void testTokenStream() throws Exception {
    CharStream cs = new MappingCharFilter( normMap, CharReader.get( new StringReader( "h i j k ll cccc bbb aa" ) ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts,
      new String[]{"i","i","jj","kkk","llll","cc","b","a"},
      new int[]{0,2,4,6,8,11,16,20},
      new int[]{1,3,5,7,10,15,19,22}
    );
  }
  public void testChained() throws Exception {
    CharStream cs = new MappingCharFilter( normMap,
        new MappingCharFilter( normMap, CharReader.get( new StringReader( "aaaa ll h" ) ) ) );
    TokenStream ts = new WhitespaceTokenizer( TEST_VERSION_CURRENT, cs );
    assertTokenStreamContents(ts,
      new String[]{"a","llllllll","i"},
      new int[]{0,5,8},
      new int[]{4,7,9}
    );
  }
}
