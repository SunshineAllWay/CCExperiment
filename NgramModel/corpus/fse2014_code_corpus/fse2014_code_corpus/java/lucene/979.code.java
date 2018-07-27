package org.apache.lucene.search.vectorhighlight;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
public class ScoreOrderFragmentsBuilder extends BaseFragmentsBuilder {
  public ScoreOrderFragmentsBuilder(){
    super();
  }
  public ScoreOrderFragmentsBuilder( String[] preTags, String[] postTags ){
    super( preTags, postTags );
  }
  @Override
  public List<WeightedFragInfo> getWeightedFragInfoList( List<WeightedFragInfo> src ) {
    Collections.sort( src, new ScoreComparator() );
    return src;
  }
  public static class ScoreComparator implements Comparator<WeightedFragInfo> {
    public int compare( WeightedFragInfo o1, WeightedFragInfo o2 ) {
      if( o1.totalBoost > o2.totalBoost ) return -1;
      else if( o1.totalBoost < o2.totalBoost ) return 1;
      else{
        if( o1.startOffset < o2.startOffset ) return -1;
        else if( o1.startOffset > o2.startOffset ) return 1;
      }
      return 0;
    }
  }
}
