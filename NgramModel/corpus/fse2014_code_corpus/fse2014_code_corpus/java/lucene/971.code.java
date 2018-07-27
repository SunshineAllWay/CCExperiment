package org.apache.lucene.search.vectorhighlight;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo.SubInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo.Toffs;
public abstract class BaseFragmentsBuilder implements FragmentsBuilder {
  protected String[] preTags, postTags;
  public static final String[] COLORED_PRE_TAGS = {
    "<b style=\"background:yellow\">", "<b style=\"background:lawngreen\">", "<b style=\"background:aquamarine\">",
    "<b style=\"background:magenta\">", "<b style=\"background:palegreen\">", "<b style=\"background:coral\">",
    "<b style=\"background:wheat\">", "<b style=\"background:khaki\">", "<b style=\"background:lime\">",
    "<b style=\"background:deepskyblue\">"
  };
  public static final String[] COLORED_POST_TAGS = { "</b>" };
  protected BaseFragmentsBuilder(){
    this( new String[]{ "<b>" }, new String[]{ "</b>" } );
  }
  protected BaseFragmentsBuilder( String[] preTags, String[] postTags ){
    this.preTags = preTags;
    this.postTags = postTags;
  }
  static Object checkTagsArgument( Object tags ){
    if( tags instanceof String ) return tags;
    else if( tags instanceof String[] ) return tags;
    throw new IllegalArgumentException( "type of preTags/postTags must be a String or String[]" );
  }
  public abstract List<WeightedFragInfo> getWeightedFragInfoList( List<WeightedFragInfo> src );
  public String createFragment( IndexReader reader, int docId,
      String fieldName, FieldFragList fieldFragList ) throws IOException {
    String[] fragments = createFragments( reader, docId, fieldName, fieldFragList, 1 );
    if( fragments == null || fragments.length == 0 ) return null;
    return fragments[0];
  }
  public String[] createFragments( IndexReader reader, int docId,
      String fieldName, FieldFragList fieldFragList, int maxNumFragments )
      throws IOException {
    if( maxNumFragments < 0 )
      throw new IllegalArgumentException( "maxNumFragments(" + maxNumFragments + ") must be positive number." );
    List<WeightedFragInfo> fragInfos = getWeightedFragInfoList( fieldFragList.fragInfos );
    List<String> fragments = new ArrayList<String>( maxNumFragments );
    Field[] values = getFields( reader, docId, fieldName );
    if( values.length == 0 ) return null;
    StringBuilder buffer = new StringBuilder();
    int[] nextValueIndex = { 0 };
    for( int n = 0; n < maxNumFragments && n < fragInfos.size(); n++ ){
      WeightedFragInfo fragInfo = fragInfos.get( n );
      fragments.add( makeFragment( buffer, nextValueIndex, values, fragInfo ) );
    }
    return fragments.toArray( new String[fragments.size()] );
  }
  @Deprecated
  protected String[] getFieldValues( IndexReader reader, int docId, String fieldName) throws IOException {
    Document doc = reader.document( docId, new MapFieldSelector( new String[]{ fieldName } ) );
    return doc.getValues( fieldName ); 
  }
  protected Field[] getFields( IndexReader reader, int docId, String fieldName) throws IOException {
    Document doc = reader.document( docId, new MapFieldSelector( new String[]{ fieldName } ) );
    return doc.getFields( fieldName ); 
  }
  @Deprecated
  protected String makeFragment( StringBuilder buffer, int[] index, String[] values, WeightedFragInfo fragInfo ){
    final int s = fragInfo.startOffset;
    return makeFragment( fragInfo, getFragmentSource( buffer, index, values, s, fragInfo.endOffset ), s );
  }
  protected String makeFragment( StringBuilder buffer, int[] index, Field[] values, WeightedFragInfo fragInfo ){
    final int s = fragInfo.startOffset;
    return makeFragment( fragInfo, getFragmentSource( buffer, index, values, s, fragInfo.endOffset ), s );
  }
  private String makeFragment( WeightedFragInfo fragInfo, String src, int s ){
    StringBuilder fragment = new StringBuilder();
    int srcIndex = 0;
    for( SubInfo subInfo : fragInfo.subInfos ){
      for( Toffs to : subInfo.termsOffsets ){
        fragment.append( src.substring( srcIndex, to.startOffset - s ) ).append( getPreTag( subInfo.seqnum ) )
          .append( src.substring( to.startOffset - s, to.endOffset - s ) ).append( getPostTag( subInfo.seqnum ) );
        srcIndex = to.endOffset - s;
      }
    }
    fragment.append( src.substring( srcIndex ) );
    return fragment.toString();
  }
  @Deprecated
  protected String getFragmentSource( StringBuilder buffer, int[] index, String[] values,
      int startOffset, int endOffset ){
    while( buffer.length() < endOffset && index[0] < values.length ){
      if( index[0] > 0 && values[index[0]].length() > 0 )
        buffer.append( ' ' );
      buffer.append( values[index[0]++] );
    }
    int eo = buffer.length() < endOffset ? buffer.length() : endOffset;
    return buffer.substring( startOffset, eo );
  }
  protected String getFragmentSource( StringBuilder buffer, int[] index, Field[] values,
      int startOffset, int endOffset ){
    while( buffer.length() < endOffset && index[0] < values.length ){
      if( index[0] > 0 && values[index[0]].isTokenized() && values[index[0]].stringValue().length() > 0 )
        buffer.append( ' ' );
      buffer.append( values[index[0]++].stringValue() );
    }
    int eo = buffer.length() < endOffset ? buffer.length() : endOffset;
    return buffer.substring( startOffset, eo );
  }
  protected String getPreTag( int num ){
    return preTags.length > num ? preTags[num] : preTags[0];
  }
  protected String getPostTag( int num ){
    return postTags.length > num ? postTags[num] : postTags[0];
  }
}
