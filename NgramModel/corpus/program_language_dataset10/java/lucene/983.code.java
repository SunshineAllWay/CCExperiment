package org.apache.lucene.search.vectorhighlight;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
public class FieldPhraseListTest extends AbstractTestCase {
  public void test1TermIndex() throws Exception {
    make1d1fIndex( "a" );
    FieldQuery fq = new FieldQuery( tq( "a" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "a(1.0)((0,1))", fpl.phraseList.get( 0 ).toString() );
    fq = new FieldQuery( tq( "b" ), true, true );
    stack = new FieldTermStack( reader, 0, F, fq );
    fpl = new FieldPhraseList( stack, fq );
    assertEquals( 0, fpl.phraseList.size() );
  }
  public void test2TermsIndex() throws Exception {
    make1d1fIndex( "a a" );
    FieldQuery fq = new FieldQuery( tq( "a" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 2, fpl.phraseList.size() );
    assertEquals( "a(1.0)((0,1))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( "a(1.0)((2,3))", fpl.phraseList.get( 1 ).toString() );
  }
  public void test1PhraseIndex() throws Exception {
    make1d1fIndex( "a b" );
    FieldQuery fq = new FieldQuery( pqF( "a", "b" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "ab(1.0)((0,3))", fpl.phraseList.get( 0 ).toString() );
    fq = new FieldQuery( tq( "b" ), true, true );
    stack = new FieldTermStack( reader, 0, F, fq );
    fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "b(1.0)((2,3))", fpl.phraseList.get( 0 ).toString() );
  }
  public void test1PhraseIndexB() throws Exception {
    make1d1fIndexB( "bbbacbabc" );
    FieldQuery fq = new FieldQuery( pqF( "ba", "ac" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "baac(1.0)((2,5))", fpl.phraseList.get( 0 ).toString() );
  }
  public void test2ConcatTermsIndexB() throws Exception {
    make1d1fIndexB( "abab" );
    FieldQuery fq = new FieldQuery( tq( "ab" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 2, fpl.phraseList.size() );
    assertEquals( "ab(1.0)((0,2))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( "ab(1.0)((2,4))", fpl.phraseList.get( 1 ).toString() );
  }
  public void test2Terms1PhraseIndex() throws Exception {
    make1d1fIndex( "c a a b" );
    FieldQuery fq = new FieldQuery( pqF( "a", "b" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "ab(1.0)((4,7))", fpl.phraseList.get( 0 ).toString() );
    fq = new FieldQuery( pqF( "a", "b" ), false, true );
    stack = new FieldTermStack( reader, 0, F, fq );
    fpl = new FieldPhraseList( stack, fq );
    assertEquals( 2, fpl.phraseList.size() );
    assertEquals( "a(1.0)((2,3))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( "ab(1.0)((4,7))", fpl.phraseList.get( 1 ).toString() );
  }
  public void testPhraseSlop() throws Exception {
    make1d1fIndex( "c a a b c" );
    FieldQuery fq = new FieldQuery( pqF( 2F, 1, "a", "c" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "ac(2.0)((4,5)(8,9))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( 4, fpl.phraseList.get( 0 ).getStartOffset() );
    assertEquals( 9, fpl.phraseList.get( 0 ).getEndOffset() );
  }
  public void test2PhrasesOverlap() throws Exception {
    make1d1fIndex( "d a b c d" );
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "b" ), Occur.SHOULD );
    query.add( pqF( "b", "c" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "abc(1.0)((2,7))", fpl.phraseList.get( 0 ).toString() );
  }
  public void test3TermsPhrase() throws Exception {
    make1d1fIndex( "d a b a b c d" );
    FieldQuery fq = new FieldQuery( pqF( "a", "b", "c" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "abc(1.0)((6,11))", fpl.phraseList.get( 0 ).toString() );
  }
  public void testSearchLongestPhrase() throws Exception {
    make1d1fIndex( "d a b d c a b c" );
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "b" ), Occur.SHOULD );
    query.add( pqF( "a", "b", "c" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 2, fpl.phraseList.size() );
    assertEquals( "ab(1.0)((2,5))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( "abc(1.0)((10,15))", fpl.phraseList.get( 1 ).toString() );
  }
  public void test1PhraseShortMV() throws Exception {
    makeIndexShortMV();
    FieldQuery fq = new FieldQuery( tq( "d" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "d(1.0)((6,7))", fpl.phraseList.get( 0 ).toString() );
  }
  public void test1PhraseLongMV() throws Exception {
    makeIndexLongMV();
    FieldQuery fq = new FieldQuery( pqF( "search", "engines" ), true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 2, fpl.phraseList.size() );
    assertEquals( "searchengines(1.0)((102,116))", fpl.phraseList.get( 0 ).toString() );
    assertEquals( "searchengines(1.0)((157,171))", fpl.phraseList.get( 1 ).toString() );
  }
  public void test1PhraseLongMVB() throws Exception {
    makeIndexLongMVB();
    FieldQuery fq = new FieldQuery( pqF( "sp", "pe", "ee", "ed" ), true, true ); 
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    assertEquals( 1, fpl.phraseList.size() );
    assertEquals( "sppeeeed(1.0)((88,93))", fpl.phraseList.get( 0 ).toString() );
  }
}
