package org.apache.lucene.search.vectorhighlight;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.search.vectorhighlight.FieldQuery.QueryPhraseMap;
import org.apache.lucene.search.vectorhighlight.FieldTermStack.TermInfo;
public class FieldPhraseList {
  LinkedList<WeightedPhraseInfo> phraseList = new LinkedList<WeightedPhraseInfo>();
  public FieldPhraseList( FieldTermStack fieldTermStack, FieldQuery fieldQuery ){
    final String field = fieldTermStack.getFieldName();
    LinkedList<TermInfo> phraseCandidate = new LinkedList<TermInfo>();
    QueryPhraseMap currMap = null;
    QueryPhraseMap nextMap = null;
    while( !fieldTermStack.isEmpty() ){
      phraseCandidate.clear();
      TermInfo ti = fieldTermStack.pop();
      currMap = fieldQuery.getFieldTermMap( field, ti.getText() );
      if( currMap == null ) continue;
      phraseCandidate.add( ti );
      while( true ){
        ti = fieldTermStack.pop();
        nextMap = null;
        if( ti != null )
          nextMap = currMap.getTermMap( ti.getText() );
        if( ti == null || nextMap == null ){
          if( ti != null )
            fieldTermStack.push( ti );
          if( currMap.isValidTermOrPhrase( phraseCandidate ) ){
            addIfNoOverlap( new WeightedPhraseInfo( phraseCandidate, currMap.getBoost(), currMap.getTermOrPhraseNumber() ) );
          }
          else{
            while( phraseCandidate.size() > 1 ){
              fieldTermStack.push( phraseCandidate.removeLast() );
              currMap = fieldQuery.searchPhrase( field, phraseCandidate );
              if( currMap != null ){
                addIfNoOverlap( new WeightedPhraseInfo( phraseCandidate, currMap.getBoost(), currMap.getTermOrPhraseNumber() ) );
                break;
              }
            }
          }
          break;
        }
        else{
          phraseCandidate.add( ti );
          currMap = nextMap;
        }
      }
    }
  }
  void addIfNoOverlap( WeightedPhraseInfo wpi ){
    for( WeightedPhraseInfo existWpi : phraseList ){
      if( existWpi.isOffsetOverlap( wpi ) ) return;
    }
    phraseList.add( wpi );
  }
  public static class WeightedPhraseInfo {
    String text;  
    List<Toffs> termsOffsets;   
    float boost;  
    int seqnum;
    public WeightedPhraseInfo( LinkedList<TermInfo> terms, float boost ){
      this( terms, boost, 0 );
    }
    public WeightedPhraseInfo( LinkedList<TermInfo> terms, float boost, int number ){
      this.boost = boost;
      this.seqnum = number;
      termsOffsets = new ArrayList<Toffs>( terms.size() );
      TermInfo ti = terms.get( 0 );
      termsOffsets.add( new Toffs( ti.getStartOffset(), ti.getEndOffset() ) );
      if( terms.size() == 1 ){
        text = ti.getText();
        return;
      }
      StringBuilder sb = new StringBuilder();
      sb.append( ti.getText() );
      int pos = ti.getPosition();
      for( int i = 1; i < terms.size(); i++ ){
        ti = terms.get( i );
        sb.append( ti.getText() );
        if( ti.getPosition() - pos == 1 ){
          Toffs to = termsOffsets.get( termsOffsets.size() - 1 );
          to.setEndOffset( ti.getEndOffset() );
        }
        else{
          termsOffsets.add( new Toffs( ti.getStartOffset(), ti.getEndOffset() ) );
        }
        pos = ti.getPosition();
      }
      text = sb.toString();
    }
    public int getStartOffset(){
      return termsOffsets.get( 0 ).startOffset;
    }
    public int getEndOffset(){
      return termsOffsets.get( termsOffsets.size() - 1 ).endOffset;
    }
    public boolean isOffsetOverlap( WeightedPhraseInfo other ){
      int so = getStartOffset();
      int eo = getEndOffset();
      int oso = other.getStartOffset();
      int oeo = other.getEndOffset();
      if( so <= oso && oso < eo ) return true;
      if( so < oeo && oeo <= eo ) return true;
      if( oso <= so && so < oeo ) return true;
      if( oso < eo && eo <= oeo ) return true;
      return false;
    }
    @Override
    public String toString(){
      StringBuilder sb = new StringBuilder();
      sb.append( text ).append( '(' ).append( boost ).append( ")(" );
      for( Toffs to : termsOffsets ){
        sb.append( to );
      }
      sb.append( ')' );
      return sb.toString();
    }
    public static class Toffs {
      int startOffset;
      int endOffset;
      public Toffs( int startOffset, int endOffset ){
        this.startOffset = startOffset;
        this.endOffset = endOffset;
      }
      public void setEndOffset( int endOffset ){
        this.endOffset = endOffset;
      }
      public int getStartOffset(){
        return startOffset;
      }
      public int getEndOffset(){
        return endOffset;
      }
      @Override
      public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append( '(' ).append( startOffset ).append( ',' ).append( endOffset ).append( ')' );
        return sb.toString();
      }
    }
  }
}
