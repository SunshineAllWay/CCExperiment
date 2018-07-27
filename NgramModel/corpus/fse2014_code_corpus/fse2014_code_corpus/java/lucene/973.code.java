package org.apache.lucene.search.vectorhighlight;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo.Toffs;
public class FieldFragList {
  List<WeightedFragInfo> fragInfos = new ArrayList<WeightedFragInfo>();
  public FieldFragList( int fragCharSize ){
  }
  public void add( int startOffset, int endOffset, List<WeightedPhraseInfo> phraseInfoList ){
    fragInfos.add( new WeightedFragInfo( startOffset, endOffset, phraseInfoList ) );
  }
  public static class WeightedFragInfo {
    List<SubInfo> subInfos;
    float totalBoost;
    int startOffset;
    int endOffset;
    public WeightedFragInfo( int startOffset, int endOffset, List<WeightedPhraseInfo> phraseInfoList ){
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      subInfos = new ArrayList<SubInfo>();
      for( WeightedPhraseInfo phraseInfo : phraseInfoList ){
        SubInfo subInfo = new SubInfo( phraseInfo.text, phraseInfo.termsOffsets, phraseInfo.seqnum );
        subInfos.add( subInfo );
        totalBoost += phraseInfo.boost;
      }
    }
    public List<SubInfo> getSubInfos(){
      return subInfos;
    }
    public float getTotalBoost(){
      return totalBoost;
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
      sb.append( "subInfos=(" );
      for( SubInfo si : subInfos )
        sb.append( si.toString() );
      sb.append( ")/" ).append( totalBoost ).append( '(' ).append( startOffset ).append( ',' ).append( endOffset ).append( ')' );
      return sb.toString();
    }
    public static class SubInfo {
      final String text;  
      final List<Toffs> termsOffsets;   
      int seqnum;
      SubInfo( String text, List<Toffs> termsOffsets, int seqnum ){
        this.text = text;
        this.termsOffsets = termsOffsets;
        this.seqnum = seqnum;
      }
      public List<Toffs> getTermsOffsets(){
        return termsOffsets;
      }
      public int getSeqnum(){
        return seqnum;
      }
      @Override
      public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append( text ).append( '(' );
        for( Toffs to : termsOffsets )
          sb.append( to.toString() );
        sb.append( ')' );
        return sb.toString();
      }
    }
  }
}
