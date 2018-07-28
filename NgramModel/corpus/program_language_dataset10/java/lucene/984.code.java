package org.apache.lucene.search.vectorhighlight;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.vectorhighlight.FieldQuery.QueryPhraseMap;
import org.apache.lucene.search.vectorhighlight.FieldTermStack.TermInfo;
public class FieldQueryTest extends AbstractTestCase {
  public void testFlattenBoolean() throws Exception {
    Query query = paW.parse( "A AND B OR C NOT (D AND E)" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    fq.flatten( query, flatQueries );
    assertCollectionQueries( flatQueries, tq( "A" ), tq( "B" ), tq( "C" ) );
  }
  public void testFlattenDisjunctionMaxQuery() throws Exception {
    Query query = dmq( tq( "A" ), tq( "B" ), pqF( "C", "D" ) );
    FieldQuery fq = new FieldQuery( query, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    fq.flatten( query, flatQueries );
    assertCollectionQueries( flatQueries, tq( "A" ), tq( "B" ), pqF( "C", "D" ) );
  }
  public void testFlattenTermAndPhrase() throws Exception {
    Query query = paW.parse( "A AND \"B C\"" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    fq.flatten( query, flatQueries );
    assertCollectionQueries( flatQueries, tq( "A" ), pqF( "B", "C" ) );
  }
  public void testFlattenTermAndPhrase2gram() throws Exception {
    Query query = paB.parse( "AA AND BCD OR EFGH" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    fq.flatten( query, flatQueries );
    assertCollectionQueries( flatQueries, tq( "AA" ), pqF( "BC", "CD" ), pqF( "EF", "FG", "GH" ) );
  }
  public void testFlatten1TermPhrase() throws Exception {
    Query query = pqF( "A" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    fq.flatten( query, flatQueries );
    assertCollectionQueries( flatQueries, tq( "A" ) );
  }
  public void testExpand() throws Exception {
    Query dummy = pqF( "DUMMY" );
    FieldQuery fq = new FieldQuery( dummy, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "b", "c" ), pqF( "a", "b", "c" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "b", "c", "d" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "b", "c", "d" ), pqF( "a", "b", "c", "d" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "c" ) );
    flatQueries.add( pqF( "b", "c", "d" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "c" ), pqF( "b", "c", "d" ), pqF( "a", "b", "c", "d" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "c" ) );
    flatQueries.add( pqF( "c", "d", "e" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "c" ), pqF( "c", "d", "e" ), pqF( "a", "b", "c", "d", "e" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "c", "d" ) );
    flatQueries.add( pqF( "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "c", "d" ), pqF( "b", "c" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "b" ) );
    flatQueries.add( pqF( "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "b" ), pqF( "b", "c" ), pqF( "a", "b", "b", "c" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "b", "a" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "b", "a" ), pqF( "a", "b", "a" ), pqF( "b", "a", "b" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "a", "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "a", "b", "c" ) );
  }
  public void testNoExpand() throws Exception {
    Query dummy = pqF( "DUMMY" );
    FieldQuery fq = new FieldQuery( dummy, true, true );
    Set<Query> flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "c", "d" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "c", "d" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( tq( "a" ) );
    flatQueries.add( pqF( "a", "b" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        tq( "a" ), pqF( "a", "b" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( tq( "b" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), tq( "b" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "c" ) );
    flatQueries.add( pqF( "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "c" ), pqF( "b", "c" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b" ) );
    flatQueries.add( pqF( "a", "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b" ), pqF( "a", "b", "c" ) );
    flatQueries = new HashSet<Query>();
    flatQueries.add( pqF( "a", "b", "c" ) );
    flatQueries.add( pqF( "b", "d", "e" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pqF( "a", "b", "c" ), pqF( "b", "d", "e" ) );
  }
  public void testExpandNotFieldMatch() throws Exception {
    Query dummy = pqF( "DUMMY" );
    FieldQuery fq = new FieldQuery( dummy, true, false );
    Set<Query> flatQueries = new HashSet<Query>();
    flatQueries.add( pq( F1, "a", "b" ) );
    flatQueries.add( pq( F2, "b", "c" ) );
    assertCollectionQueries( fq.expand( flatQueries ),
        pq( F1, "a", "b" ), pq( F2, "b", "c" ), pq( F1, "a", "b", "c" ) );
  }
  public void testGetFieldTermMap() throws Exception {
    Query query = tq( "a" );
    FieldQuery fq = new FieldQuery( query, true, true );
    QueryPhraseMap pqm = fq.getFieldTermMap( F, "a" );
    assertNotNull( pqm );
    assertTrue( pqm.isTerminal() );
    pqm = fq.getFieldTermMap( F, "b" );
    assertNull( pqm );
    pqm = fq.getFieldTermMap( F1, "a" );
    assertNull( pqm );
  }
  public void testGetRootMap() throws Exception {
    Query dummy = pqF( "DUMMY" );
    FieldQuery fq = new FieldQuery( dummy, true, true );
    QueryPhraseMap rootMap1 = fq.getRootMap( tq( "a" ) );
    QueryPhraseMap rootMap2 = fq.getRootMap( tq( "a" ) );
    assertTrue( rootMap1 == rootMap2 );
    QueryPhraseMap rootMap3 = fq.getRootMap( tq( "b" ) );
    assertTrue( rootMap1 == rootMap3 );
    QueryPhraseMap rootMap4 = fq.getRootMap( tq( F1, "b" ) );
    assertFalse( rootMap4 == rootMap3 );
  }
  public void testGetRootMapNotFieldMatch() throws Exception {
    Query dummy = pqF( "DUMMY" );
    FieldQuery fq = new FieldQuery( dummy, true, false );
    QueryPhraseMap rootMap1 = fq.getRootMap( tq( "a" ) );
    QueryPhraseMap rootMap2 = fq.getRootMap( tq( "a" ) );
    assertTrue( rootMap1 == rootMap2 );
    QueryPhraseMap rootMap3 = fq.getRootMap( tq( "b" ) );
    assertTrue( rootMap1 == rootMap3 );
    QueryPhraseMap rootMap4 = fq.getRootMap( tq( F1, "b" ) );
    assertTrue( rootMap4 == rootMap3 );
  }
  public void testGetTermSet() throws Exception {
    Query query = paW.parse( "A AND B OR x:C NOT (D AND E)" );
    FieldQuery fq = new FieldQuery( query, true, true );
    assertEquals( 2, fq.termSetMap.size() );
    Set<String> termSet = fq.getTermSet( F );
    assertEquals( 2, termSet.size() );
    assertTrue( termSet.contains( "A" ) );
    assertTrue( termSet.contains( "B" ) );
    termSet = fq.getTermSet( "x" );
    assertEquals( 1, termSet.size() );
    assertTrue( termSet.contains( "C" ) );
    termSet = fq.getTermSet( "y" );
    assertNull( termSet );
  }
  public void testQueryPhraseMap1Term() throws Exception {
    Query query = tq( "a" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertTrue( qpm.subMap.get( "a" ) != null );
    assertTrue( qpm.subMap.get( "a" ).terminal );
    assertEquals( 1F, qpm.subMap.get( "a" ).boost );
    fq = new FieldQuery( query, true, false );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( F ) );
    assertNotNull( map.get( null ) );
    qpm = map.get( null );
    assertEquals( 1, qpm.subMap.size() );
    assertTrue( qpm.subMap.get( "a" ) != null );
    assertTrue( qpm.subMap.get( "a" ).terminal );
    assertEquals( 1F, qpm.subMap.get( "a" ).boost );
    fq = new FieldQuery( query, false, true );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertTrue( qpm.subMap.get( "a" ) != null );
    assertTrue( qpm.subMap.get( "a" ).terminal );
    assertEquals( 1F, qpm.subMap.get( "a" ).boost );
    fq = new FieldQuery( query, false, false );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( F ) );
    assertNotNull( map.get( null ) );
    qpm = map.get( null );
    assertEquals( 1, qpm.subMap.size() );
    assertTrue( qpm.subMap.get( "a" ) != null );
    assertTrue( qpm.subMap.get( "a" ).terminal );
    assertEquals( 1F, qpm.subMap.get( "a" ).boost );
    query = tq( 2, "a" );
    fq = new FieldQuery( query, true, true );
    map = fq.rootMaps;
    qpm = map.get( F );
    assertEquals( 2F, qpm.subMap.get( "a" ).boost );
  }
  public void testQueryPhraseMap1Phrase() throws Exception {
    Query query = pqF( "a", "b" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    fq = new FieldQuery( query, true, false );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( F ) );
    assertNotNull( map.get( null ) );
    qpm = map.get( null );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    fq = new FieldQuery( query, false, true );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    qpm = map.get( F );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    qpm2 = qpm.subMap.get( "a" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( qpm.subMap.get( "b" ) );
    qpm2 = qpm.subMap.get( "b" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    fq = new FieldQuery( query, false, false );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( F ) );
    assertNotNull( map.get( null ) );
    qpm = map.get( null );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    qpm2 = qpm.subMap.get( "a" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( qpm.subMap.get( "b" ) );
    qpm2 = qpm.subMap.get( "b" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    query = pqF( 2, "a", "b" );
    fq = new FieldQuery( query, false, false );
    map = fq.rootMaps;
    qpm = map.get( null );
    qpm2 = qpm.subMap.get( "a" );
    assertEquals( 2F, qpm2.boost );
    qpm3 = qpm2.subMap.get( "b" );
    assertEquals( 2F, qpm3.boost );
    qpm2 = qpm.subMap.get( "b" );
    assertEquals( 2F, qpm2.boost );
  }
  public void testQueryPhraseMap1PhraseAnother() throws Exception {
    Query query = pqF( "search", "engines" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "search" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "search" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "engines" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "engines" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
  }
  public void testQueryPhraseMap2Phrases() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "b" ), Occur.SHOULD );
    query.add( pqF( 2, "c", "d" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( qpm.subMap.get( "c" ) );
    qpm2 = qpm.subMap.get( "c" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "d" ) );
    qpm3 = qpm2.subMap.get( "d" );
    assertTrue( qpm3.terminal );
    assertEquals( 2F, qpm3.boost );
  }
  public void testQueryPhraseMap2PhrasesFields() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add( pq( F1, "a", "b" ), Occur.SHOULD );
    query.add( pq( 2F, F2, "c", "d" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 2, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F1 ) );
    QueryPhraseMap qpm = map.get( F1 );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( map.get( F2 ) );
    qpm = map.get( F2 );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "c" ) );
    qpm2 = qpm.subMap.get( "c" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "d" ) );
    qpm3 = qpm2.subMap.get( "d" );
    assertTrue( qpm3.terminal );
    assertEquals( 2F, qpm3.boost );
    fq = new FieldQuery( query, true, false );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( F1 ) );
    assertNull( map.get( F2 ) );
    assertNotNull( map.get( null ) );
    qpm = map.get( null );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( qpm.subMap.get( "c" ) );
    qpm2 = qpm.subMap.get( "c" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "d" ) );
    qpm3 = qpm2.subMap.get( "d" );
    assertTrue( qpm3.terminal );
    assertEquals( 2F, qpm3.boost );
  }
  public void testQueryPhraseMapOverlapPhrases() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "b", "c" ), Occur.SHOULD );
    query.add( pqF( 2, "b", "c", "d" ), Occur.SHOULD );
    query.add( pqF( 3, "b", "d" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "b" );
    assertFalse( qpm3.terminal );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "c" ) );
    QueryPhraseMap qpm4 = qpm3.subMap.get( "c" );
    assertTrue( qpm4.terminal );
    assertEquals( 1F, qpm4.boost );
    assertNotNull( qpm4.subMap.get( "d" ) );
    QueryPhraseMap qpm5 = qpm4.subMap.get( "d" );
    assertTrue( qpm5.terminal );
    assertEquals( 1F, qpm5.boost );
    assertNotNull( qpm.subMap.get( "b" ) );
    qpm2 = qpm.subMap.get( "b" );
    assertFalse( qpm2.terminal );
    assertEquals( 2, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "c" ) );
    qpm3 = qpm2.subMap.get( "c" );
    assertFalse( qpm3.terminal );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "d" ) );
    qpm4 = qpm3.subMap.get( "d" );
    assertTrue( qpm4.terminal );
    assertEquals( 2F, qpm4.boost );
    assertNotNull( qpm2.subMap.get( "d" ) );
    qpm3 = qpm2.subMap.get( "d" );
    assertTrue( qpm3.terminal );
    assertEquals( 3F, qpm3.boost );
  }
  public void testQueryPhraseMapOverlapPhrases2() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "b" ), Occur.SHOULD );
    query.add( pqF( 2, "a", "b", "c" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "b" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "b" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "c" ) );
    QueryPhraseMap qpm4 = qpm3.subMap.get( "c" );
    assertTrue( qpm4.terminal );
    assertEquals( 2F, qpm4.boost );
  }
  public void testQueryPhraseMapOverlapPhrases3() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add( pqF( "a", "a", "a", "a" ), Occur.SHOULD );
    query.add( pqF( 2, "a", "a", "a" ), Occur.SHOULD );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 1, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "a" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "a" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "a" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "a" );
    assertFalse( qpm3.terminal );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "a" ) );
    QueryPhraseMap qpm4 = qpm3.subMap.get( "a" );
    assertTrue( qpm4.terminal );
    assertEquals( 1, qpm4.subMap.size() );
    assertNotNull( qpm4.subMap.get( "a" ) );
    QueryPhraseMap qpm5 = qpm4.subMap.get( "a" );
    assertTrue( qpm5.terminal );
    assertEquals( 1, qpm5.subMap.size() );
    assertNotNull( qpm5.subMap.get( "a" ) );
    QueryPhraseMap qpm6 = qpm5.subMap.get( "a" );
    assertTrue( qpm6.terminal );
    assertEquals( 1, qpm6.subMap.size() );
    assertNotNull( qpm6.subMap.get( "a" ) );
    QueryPhraseMap qpm7 = qpm6.subMap.get( "a" );
    assertTrue( qpm7.terminal );
  }
  public void testQueryPhraseMapOverlap2gram() throws Exception {
    Query query = paB.parse( "abc AND bcd" );
    FieldQuery fq = new FieldQuery( query, true, true );
    Map<String, QueryPhraseMap> map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    QueryPhraseMap qpm = map.get( F );
    assertEquals( 2, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "ab" ) );
    QueryPhraseMap qpm2 = qpm.subMap.get( "ab" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "bc" ) );
    QueryPhraseMap qpm3 = qpm2.subMap.get( "bc" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "cd" ) );
    QueryPhraseMap qpm4 = qpm3.subMap.get( "cd" );
    assertTrue( qpm4.terminal );
    assertEquals( 1F, qpm4.boost );
    assertNotNull( qpm.subMap.get( "bc" ) );
    qpm2 = qpm.subMap.get( "bc" );
    assertFalse( qpm2.terminal );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "cd" ) );
    qpm3 = qpm2.subMap.get( "cd" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    fq = new FieldQuery( query, false, true );
    map = fq.rootMaps;
    assertEquals( 1, map.size() );
    assertNull( map.get( null ) );
    assertNotNull( map.get( F ) );
    qpm = map.get( F );
    assertEquals( 3, qpm.subMap.size() );
    assertNotNull( qpm.subMap.get( "ab" ) );
    qpm2 = qpm.subMap.get( "ab" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "bc" ) );
    qpm3 = qpm2.subMap.get( "bc" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertEquals( 1, qpm3.subMap.size() );
    assertNotNull( qpm3.subMap.get( "cd" ) );
    qpm4 = qpm3.subMap.get( "cd" );
    assertTrue( qpm4.terminal );
    assertEquals( 1F, qpm4.boost );
    assertNotNull( qpm.subMap.get( "bc" ) );
    qpm2 = qpm.subMap.get( "bc" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    assertEquals( 1, qpm2.subMap.size() );
    assertNotNull( qpm2.subMap.get( "cd" ) );
    qpm3 = qpm2.subMap.get( "cd" );
    assertTrue( qpm3.terminal );
    assertEquals( 1F, qpm3.boost );
    assertNotNull( qpm.subMap.get( "cd" ) );
    qpm2 = qpm.subMap.get( "cd" );
    assertTrue( qpm2.terminal );
    assertEquals( 1F, qpm2.boost );
    assertEquals( 0, qpm2.subMap.size() );
  }
  public void testSearchPhrase() throws Exception {
    Query query = pqF( "a", "b", "c" );
    FieldQuery fq = new FieldQuery( query, true, true );
    List<TermInfo> phraseCandidate = new ArrayList<TermInfo>();
    phraseCandidate.add( new TermInfo( "a", 0, 1, 0 ) );
    assertNull( fq.searchPhrase( F, phraseCandidate ) );
    phraseCandidate.add( new TermInfo( "b", 2, 3, 1 ) );
    assertNull( fq.searchPhrase( F, phraseCandidate ) );
    phraseCandidate.add( new TermInfo( "c", 4, 5, 2 ) );
    assertNotNull( fq.searchPhrase( F, phraseCandidate ) );
    assertNull( fq.searchPhrase( "x", phraseCandidate ) );
    fq = new FieldQuery( query, true, false );
    assertNotNull( fq.searchPhrase( F, phraseCandidate ) );
    assertNotNull( fq.searchPhrase( "x", phraseCandidate ) );
    fq = new FieldQuery( query, false, true );
    phraseCandidate.clear();
    phraseCandidate.add( new TermInfo( "a", 0, 1, 0 ) );
    assertNotNull( fq.searchPhrase( F, phraseCandidate ) );
    phraseCandidate.add( new TermInfo( "b", 2, 3, 1 ) );
    assertNull( fq.searchPhrase( F, phraseCandidate ) );
    phraseCandidate.add( new TermInfo( "c", 4, 5, 2 ) );
    assertNotNull( fq.searchPhrase( F, phraseCandidate ) );
    assertNull( fq.searchPhrase( "x", phraseCandidate ) );
  }
  public void testSearchPhraseSlop() throws Exception {
    Query query = pqF( "a", "b", "c" );
    FieldQuery fq = new FieldQuery( query, true, true );
    List<TermInfo> phraseCandidate = new ArrayList<TermInfo>();
    phraseCandidate.add( new TermInfo( "a", 0, 1, 0 ) );
    phraseCandidate.add( new TermInfo( "b", 2, 3, 2 ) );
    phraseCandidate.add( new TermInfo( "c", 4, 5, 4 ) );
    assertNull( fq.searchPhrase( F, phraseCandidate ) );
    query = pqF( 1F, 1, "a", "b", "c" );
    fq = new FieldQuery( query, true, true );
    assertNotNull( fq.searchPhrase( F, phraseCandidate ) );
    phraseCandidate.clear();
    phraseCandidate.add( new TermInfo( "a", 0, 1, 0 ) );
    phraseCandidate.add( new TermInfo( "b", 2, 3, 3 ) );
    phraseCandidate.add( new TermInfo( "c", 4, 5, 6 ) );
    assertNull( fq.searchPhrase( F, phraseCandidate ) );
  }
}
