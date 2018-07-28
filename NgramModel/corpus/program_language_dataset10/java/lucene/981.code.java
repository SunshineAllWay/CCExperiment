package org.apache.lucene.search.vectorhighlight;
import java.util.List;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
public class SimpleFragmentsBuilder extends BaseFragmentsBuilder {
  public SimpleFragmentsBuilder() {
    super();
  }
  public SimpleFragmentsBuilder( String[] preTags, String[] postTags ) {
    super( preTags, postTags );
  }
  @Override
  public List<WeightedFragInfo> getWeightedFragInfoList( List<WeightedFragInfo> src ) {
    return src;
  }
}
