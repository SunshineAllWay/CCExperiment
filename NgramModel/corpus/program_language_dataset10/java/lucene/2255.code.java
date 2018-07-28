package org.apache.solr.analysis;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.lucene.analysis.CharStream;
public class PatternReplaceCharFilterFactory extends BaseCharFilterFactory {
  private Pattern p;
  private String replacement;
  private int maxBlockChars;
  private String blockDelimiters;
  public void init(Map<String, String> args) {
    super.init( args );
    try {
      p = Pattern.compile(args.get("pattern"));
    } catch (PatternSyntaxException e) {
      throw new RuntimeException
        ("Configuration Error: 'pattern' can not be parsed in " +
         this.getClass().getName(), e);
    }
    replacement = args.get( "replacement" );
    if( replacement == null )
      replacement = "";
    maxBlockChars = getInt( "maxBlockChars", PatternReplaceCharFilter.DEFAULT_MAX_BLOCK_CHARS );
    blockDelimiters = args.get( "blockDelimiters" );
  }
  public CharStream create(CharStream input) {
    return new PatternReplaceCharFilter( p, replacement, maxBlockChars, blockDelimiters, input );
  }
}
