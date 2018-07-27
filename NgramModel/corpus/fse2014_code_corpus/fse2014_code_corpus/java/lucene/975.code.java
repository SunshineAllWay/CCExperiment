package org.apache.lucene.search.vectorhighlight;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.vectorhighlight.FieldTermStack.TermInfo;
public class FieldQuery {
  final boolean fieldMatch;
  Map<String, QueryPhraseMap> rootMaps = new HashMap<String, QueryPhraseMap>();
  Map<String, Set<String>> termSetMap = new HashMap<String, Set<String>>();
  int termOrPhraseNumber; 
  FieldQuery( Query query, boolean phraseHighlight, boolean fieldMatch ){
    this.fieldMatch = fieldMatch;
    Set<Query> flatQueries = new HashSet<Query>();
    flatten( query, flatQueries );
    saveTerms( flatQueries );
    Collection<Query> expandQueries = expand( flatQueries );
    for( Query flatQuery : expandQueries ){
      QueryPhraseMap rootMap = getRootMap( flatQuery );
      rootMap.add( flatQuery );
      if( !phraseHighlight && flatQuery instanceof PhraseQuery ){
        PhraseQuery pq = (PhraseQuery)flatQuery;
        if( pq.getTerms().length > 1 ){
          for( Term term : pq.getTerms() )
            rootMap.addTerm( term, flatQuery.getBoost() );
        }
      }
    }
  }
  void flatten( Query sourceQuery, Collection<Query> flatQueries ){
    if( sourceQuery instanceof BooleanQuery ){
      BooleanQuery bq = (BooleanQuery)sourceQuery;
      for( BooleanClause clause : bq.getClauses() ){
        if( !clause.isProhibited() )
          flatten( clause.getQuery(), flatQueries );
      }
    }
    else if( sourceQuery instanceof DisjunctionMaxQuery ){
      DisjunctionMaxQuery dmq = (DisjunctionMaxQuery)sourceQuery;
      for( Query query : dmq ){
        flatten( query, flatQueries );
      }
    }
    else if( sourceQuery instanceof TermQuery ){
      if( !flatQueries.contains( sourceQuery ) )
        flatQueries.add( sourceQuery );
    }
    else if( sourceQuery instanceof PhraseQuery ){
      if( !flatQueries.contains( sourceQuery ) ){
        PhraseQuery pq = (PhraseQuery)sourceQuery;
        if( pq.getTerms().length > 1 )
          flatQueries.add( pq );
        else if( pq.getTerms().length == 1 ){
          flatQueries.add( new TermQuery( pq.getTerms()[0] ) );
        }
      }
    }
  }
  Collection<Query> expand( Collection<Query> flatQueries ){
    Set<Query> expandQueries = new HashSet<Query>();
    for( Iterator<Query> i = flatQueries.iterator(); i.hasNext(); ){
      Query query = i.next();
      i.remove();
      expandQueries.add( query );
      if( !( query instanceof PhraseQuery ) ) continue;
      for( Iterator<Query> j = flatQueries.iterator(); j.hasNext(); ){
        Query qj = j.next();
        if( !( qj instanceof PhraseQuery ) ) continue;
        checkOverlap( expandQueries, (PhraseQuery)query, (PhraseQuery)qj );
      }
    }
    return expandQueries;
  }
  private void checkOverlap( Collection<Query> expandQueries, PhraseQuery a, PhraseQuery b ){
    if( a.getSlop() != b.getSlop() ) return;
    Term[] ats = a.getTerms();
    Term[] bts = b.getTerms();
    if( fieldMatch && !ats[0].field().equals( bts[0].field() ) ) return;
    checkOverlap( expandQueries, ats, bts, a.getSlop(), a.getBoost() );
    checkOverlap( expandQueries, bts, ats, b.getSlop(), b.getBoost() );
  }
  private void checkOverlap( Collection<Query> expandQueries, Term[] src, Term[] dest, int slop, float boost ){
    for( int i = 1; i < src.length; i++ ){
      boolean overlap = true;
      for( int j = i; j < src.length; j++ ){
        if( ( j - i ) < dest.length && !src[j].text().equals( dest[j-i].text() ) ){
          overlap = false;
          break;
        }
      }
      if( overlap && src.length - i < dest.length ){
        PhraseQuery pq = new PhraseQuery();
        for( Term srcTerm : src )
          pq.add( srcTerm );
        for( int k = src.length - i; k < dest.length; k++ ){
          pq.add( new Term( src[0].field(), dest[k].text() ) );
        }
        pq.setSlop( slop );
        pq.setBoost( boost );
        if(!expandQueries.contains( pq ) )
          expandQueries.add( pq );
      }
    }
  }
  QueryPhraseMap getRootMap( Query query ){
    String key = getKey( query );
    QueryPhraseMap map = rootMaps.get( key );
    if( map == null ){
      map = new QueryPhraseMap( this );
      rootMaps.put( key, map );
    }
    return map;
  }
  private String getKey( Query query ){
    if( !fieldMatch ) return null;
    if( query instanceof TermQuery )
      return ((TermQuery)query).getTerm().field();
    else if ( query instanceof PhraseQuery ){
      PhraseQuery pq = (PhraseQuery)query;
      Term[] terms = pq.getTerms();
      return terms[0].field();
    }
    else
      throw new RuntimeException( "query \"" + query.toString() + "\" must be flatten first." );
  }
  void saveTerms( Collection<Query> flatQueries ){
    for( Query query : flatQueries ){
      Set<String> termSet = getTermSet( query );
      if( query instanceof TermQuery )
        termSet.add( ((TermQuery)query).getTerm().text() );
      else if( query instanceof PhraseQuery ){
        for( Term term : ((PhraseQuery)query).getTerms() )
          termSet.add( term.text() );
      }
      else
        throw new RuntimeException( "query \"" + query.toString() + "\" must be flatten first." );
    }
  }
  private Set<String> getTermSet( Query query ){
    String key = getKey( query );
    Set<String> set = termSetMap.get( key );
    if( set == null ){
      set = new HashSet<String>();
      termSetMap.put( key, set );
    }
    return set;
  }
  Set<String> getTermSet( String field ){
    return termSetMap.get( fieldMatch ? field : null );
  }
  public QueryPhraseMap getFieldTermMap( String fieldName, String term ){
    QueryPhraseMap rootMap = getRootMap( fieldName );
    return rootMap == null ? null : rootMap.subMap.get( term );
  }
  public QueryPhraseMap searchPhrase( String fieldName, final List<TermInfo> phraseCandidate ){
    QueryPhraseMap root = getRootMap( fieldName );
    if( root == null ) return null;
    return root.searchPhrase( phraseCandidate );
  }
  private QueryPhraseMap getRootMap( String fieldName ){
    return rootMaps.get( fieldMatch ? fieldName : null );
  }
  int nextTermOrPhraseNumber(){
    return termOrPhraseNumber++;
  }
  public static class QueryPhraseMap {
    boolean terminal;
    int slop;   
    float boost;  
    int termOrPhraseNumber;   
    FieldQuery fieldQuery;
    Map<String, QueryPhraseMap> subMap = new HashMap<String, QueryPhraseMap>();
    public QueryPhraseMap( FieldQuery fieldQuery ){
      this.fieldQuery = fieldQuery;
    }
    void addTerm( Term term, float boost ){
      QueryPhraseMap map = getOrNewMap( subMap, term.text() );
      map.markTerminal( boost );
    }
    private QueryPhraseMap getOrNewMap( Map<String, QueryPhraseMap> subMap, String term ){
      QueryPhraseMap map = subMap.get( term );
      if( map == null ){
        map = new QueryPhraseMap( fieldQuery );
        subMap.put( term, map );
      }
      return map;
    }
    void add( Query query ){
      if( query instanceof TermQuery ){
        addTerm( ((TermQuery)query).getTerm(), query.getBoost() );
      }
      else if( query instanceof PhraseQuery ){
        PhraseQuery pq = (PhraseQuery)query;
        Term[] terms = pq.getTerms();
        Map<String, QueryPhraseMap> map = subMap;
        QueryPhraseMap qpm = null;
        for( Term term : terms ){
          qpm = getOrNewMap( map, term.text() );
          map = qpm.subMap;
        }
        qpm.markTerminal( pq.getSlop(), pq.getBoost() );
      }
      else
        throw new RuntimeException( "query \"" + query.toString() + "\" must be flatten first." );
    }
    public QueryPhraseMap getTermMap( String term ){
      return subMap.get( term );
    }
    private void markTerminal( float boost ){
      markTerminal( 0, boost );
    }
    private void markTerminal( int slop, float boost ){
      this.terminal = true;
      this.slop = slop;
      this.boost = boost;
      this.termOrPhraseNumber = fieldQuery.nextTermOrPhraseNumber();
    }
    public boolean isTerminal(){
      return terminal;
    }
    public int getSlop(){
      return slop;
    }
    public float getBoost(){
      return boost;
    }
    public int getTermOrPhraseNumber(){
      return termOrPhraseNumber;
    }
    public QueryPhraseMap searchPhrase( final List<TermInfo> phraseCandidate ){
      QueryPhraseMap currMap = this;
      for( TermInfo ti : phraseCandidate ){
        currMap = currMap.subMap.get( ti.getText() );
        if( currMap == null ) return null;
      }
      return currMap.isValidTermOrPhrase( phraseCandidate ) ? currMap : null;
    }
    public boolean isValidTermOrPhrase( final List<TermInfo> phraseCandidate ){
      if( !terminal ) return false;
      if( phraseCandidate.size() == 1 ) return true;
      int pos = phraseCandidate.get( 0 ).getPosition();
      for( int i = 1; i < phraseCandidate.size(); i++ ){
        int nextPos = phraseCandidate.get( i ).getPosition();
        if( Math.abs( nextPos - pos - 1 ) > slop ) return false;
        pos = nextPos;
      }
      return true;
    }
  }
}
