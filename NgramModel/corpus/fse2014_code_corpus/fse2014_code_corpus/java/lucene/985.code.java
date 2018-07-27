package org.apache.lucene.search.vectorhighlight;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
public class FieldTermStackTest extends AbstractTestCase {
  public void test1Term() throws Exception {
    makeIndex();
    FieldQuery fq = new FieldQuery( tq( "a" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 6, stack.termList.size() );
    assertEquals( "a(0,1,0)", stack.pop().toString() );
    assertEquals( "a(2,3,1)", stack.pop().toString() );
    assertEquals( "a(4,5,2)", stack.pop().toString() );
    assertEquals( "a(12,13,6)", stack.pop().toString() );
    assertEquals( "a(28,29,14)", stack.pop().toString() );
    assertEquals( "a(32,33,16)", stack.pop().toString() );
  }
  public void test2Terms() throws Exception {
    makeIndex();
    BooleanQuery query = new BooleanQuery();
    query.add( tq( "b" ), Occur.SHOULD );
    query.add( tq( "c" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 8, stack.termList.size() );
    assertEquals( "b(6,7,3)", stack.pop().toString() );
    assertEquals( "b(8,9,4)", stack.pop().toString() );
    assertEquals( "c(10,11,5)", stack.pop().toString() );
    assertEquals( "b(14,15,7)", stack.pop().toString() );
    assertEquals( "b(16,17,8)", stack.pop().toString() );
    assertEquals( "c(18,19,9)", stack.pop().toString() );
    assertEquals( "b(26,27,13)", stack.pop().toString() );
    assertEquals( "b(30,31,15)", stack.pop().toString() );
  }
  public void test1Phrase() throws Exception {
    makeIndex();
    FieldQuery fq = new FieldQuery( pqF( "c", "d" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 3, stack.termList.size() );
    assertEquals( "c(10,11,5)", stack.pop().toString() );
    assertEquals( "c(18,19,9)", stack.pop().toString() );
    assertEquals( "d(20,21,10)", stack.pop().toString() );
  }
  private void makeIndex() throws Exception {
    String value1 = "a a a b b c a b b c d e f";
    String value2 = "b a b a f";
    make1dmfIndex( value1, value2 );
  }
  public void test1TermB() throws Exception {
    makeIndexB();
    FieldQuery fq = new FieldQuery( tq( "ab" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 2, stack.termList.size() );
    assertEquals( "ab(2,4,2)", stack.pop().toString() );
    assertEquals( "ab(6,8,6)", stack.pop().toString() );
  }
  public void test2TermsB() throws Exception {
    makeIndexB();
    BooleanQuery query = new BooleanQuery();
    query.add( tq( "bc" ), Occur.SHOULD );
    query.add( tq( "ef" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 3, stack.termList.size() );
    assertEquals( "bc(4,6,4)", stack.pop().toString() );
    assertEquals( "bc(8,10,8)", stack.pop().toString() );
    assertEquals( "ef(11,13,11)", stack.pop().toString() );
  }
  public void test1PhraseB() throws Exception {
    makeIndexB();
    FieldQuery fq = new FieldQuery( pqF( "ab", "bb" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 4, stack.termList.size() );
    assertEquals( "ab(2,4,2)", stack.pop().toString() );
    assertEquals( "bb(3,5,3)", stack.pop().toString() );
    assertEquals( "ab(6,8,6)", stack.pop().toString() );
    assertEquals( "bb(7,9,7)", stack.pop().toString() );
  }
  private void makeIndexB() throws Exception {
    String value = "aaabbcabbcdef";
    make1dmfIndexB( value );
  }
  public void test1PhraseShortMV() throws Exception {
    makeIndexShortMV();
    FieldQuery fq = new FieldQuery( tq( "d" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 1, stack.termList.size() );
    assertEquals( "d(6,7,3)", stack.pop().toString() );
  }
  public void test1PhraseLongMV() throws Exception {
    makeIndexLongMV();
    FieldQuery fq = new FieldQuery( pqF( "search", "engines" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 4, stack.termList.size() );
    assertEquals( "search(102,108,14)", stack.pop().toString() );
    assertEquals( "engines(109,116,15)", stack.pop().toString() );
    assertEquals( "search(157,163,24)", stack.pop().toString() );
    assertEquals( "engines(164,171,25)", stack.pop().toString() );
  }
  public void test1PhraseMVB() throws Exception {
    makeIndexLongMVB();
    FieldQuery fq = new FieldQuery( pqF( "sp", "pe", "ee", "ed" ), true, true ); 
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    assertEquals( 4, stack.termList.size() );
    assertEquals( "sp(88,90,61)", stack.pop().toString() );
    assertEquals( "pe(89,91,62)", stack.pop().toString() );
    assertEquals( "ee(90,92,63)", stack.pop().toString() );
    assertEquals( "ed(91,93,64)", stack.pop().toString() );
  }
}
