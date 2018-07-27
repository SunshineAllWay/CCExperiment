package org.apache.lucene.search.vectorhighlight;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo;
public class SimpleFragListBuilder implements FragListBuilder {
  public static final int MARGIN = 6;
  public static final int MIN_FRAG_CHAR_SIZE = MARGIN * 3;
  public FieldFragList createFieldFragList(FieldPhraseList fieldPhraseList, int fragCharSize) {
    if( fragCharSize < MIN_FRAG_CHAR_SIZE )
      throw new IllegalArgumentException( "fragCharSize(" + fragCharSize + ") is too small. It must be " +
          MIN_FRAG_CHAR_SIZE + " or higher." );
    FieldFragList ffl = new FieldFragList( fragCharSize );
    List<WeightedPhraseInfo> wpil = new ArrayList<WeightedPhraseInfo>();
    Iterator<WeightedPhraseInfo> ite = fieldPhraseList.phraseList.iterator();
    WeightedPhraseInfo phraseInfo = null;
    int startOffset = 0;
    boolean taken = false;
    while( true ){
      if( !taken ){
        if( !ite.hasNext() ) break;
        phraseInfo = ite.next();
      }
      taken = false;
      if( phraseInfo == null ) break;
      if( phraseInfo.getStartOffset() < startOffset ) continue;
      wpil.clear();
      wpil.add( phraseInfo );
      int st = phraseInfo.getStartOffset() - MARGIN < startOffset ?
          startOffset : phraseInfo.getStartOffset() - MARGIN;
      int en = st + fragCharSize;
      if( phraseInfo.getEndOffset() > en )
        en = phraseInfo.getEndOffset();
      startOffset = en;
      while( true ){
        if( ite.hasNext() ){
          phraseInfo = ite.next();
          taken = true;
          if( phraseInfo == null ) break;
        }
        else
          break;
        if( phraseInfo.getEndOffset() <= en )
          wpil.add( phraseInfo );
        else
          break;
      }
      ffl.add( st, en, wpil );
    }
    return ffl;
  }
}
