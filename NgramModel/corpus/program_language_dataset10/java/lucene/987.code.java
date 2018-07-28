package org.apache.lucene.search.vectorhighlight;
import org.apache.lucene.search.Query;
public class ScoreOrderFragmentsBuilderTest extends AbstractTestCase {
  public void test3Frags() throws Exception {
    FieldFragList ffl = ffl( "a c", "a b b b b b b b b b b b a b a b b b b b c a a b b" );
    ScoreOrderFragmentsBuilder sofb = new ScoreOrderFragmentsBuilder();
    String[] f = sofb.createFragments( reader, 0, F, ffl, 3 );
    assertEquals( 3, f.length );
    assertEquals( "<b>c</b> <b>a</b> <b>a</b> b b", f[0] );
    assertEquals( "b b <b>a</b> b <b>a</b> b b b b b ", f[1] );
    assertEquals( "<b>a</b> b b b b b b b b b ", f[2] );
  }
  private FieldFragList ffl( String queryValue, String indexValue ) throws Exception {
    make1d1fIndex( indexValue );
    Query query = paW.parse( queryValue );
    FieldQuery fq = new FieldQuery( query, true, true );
    FieldTermStack stack = new FieldTermStack( reader, 0, F, fq );
    FieldPhraseList fpl = new FieldPhraseList( stack, fq );
    return new SimpleFragListBuilder().createFieldFragList( fpl, 20 );
  }
}
