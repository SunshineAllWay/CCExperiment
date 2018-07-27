package org.apache.solr.analysis;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.solr.common.SolrException;
public class PatternTokenizerFactory extends BaseTokenizerFactory 
{
  public static final String PATTERN = "pattern";
  public static final String GROUP = "group";
  protected Map<String,String> args;
  protected Pattern pattern;
  protected int group;
  @Override
  public void init(Map<String,String> args) 
  {
    this.args = args;
    String regex = args.get( PATTERN );
    if( regex == null ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "missing required argument: "+PATTERN );
    }
    int flags = 0; 
    pattern = Pattern.compile( regex, flags );
    group = -1;  
    String g = args.get( GROUP );
    if( g != null ) {
      try {
        group = Integer.parseInt( g );
      }
      catch( Exception ex ) {
        throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "invalid group argument: "+g );
      }
    }
  }
  public Tokenizer create(final Reader in) {
    try {
      return new PatternTokenizer(in, pattern, group);
    } catch( IOException ex ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, ex );
    }
  }
  @Deprecated
  public static List<Token> split( Matcher matcher, String input )
  {
    int index = 0;
    int lastNonEmptySize = Integer.MAX_VALUE;
    ArrayList<Token> matchList = new ArrayList<Token>();
    while(matcher.find()) {
      String match = input.subSequence(index, matcher.start()).toString();
      matchList.add( new Token( match, index, matcher.start()) );
      index = matcher.end();
      if( match.length() > 0 ) {
        lastNonEmptySize = matchList.size();
      }
    }
    if (index == 0) {
      matchList.add( new Token( input, 0, input.length()) );
    }
    else { 
      String match = input.subSequence(index, input.length()).toString();
      matchList.add( new Token( match, index, input.length()) );
      if( match.length() > 0 ) {
        lastNonEmptySize = matchList.size();
      }
    }
    if( lastNonEmptySize < matchList.size() ) {
      return matchList.subList( 0, lastNonEmptySize );
    }
    return matchList;
  }
  @Deprecated
  public static List<Token> group( Matcher matcher, String input, int group )
  {
    ArrayList<Token> matchList = new ArrayList<Token>();
    while(matcher.find()) {
      Token t = new Token( 
        matcher.group(group), 
        matcher.start(group), 
        matcher.end(group) );
      matchList.add( t );
    }
    return matchList;
  }
}
