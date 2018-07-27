package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
public class PatternReplaceFilterFactory extends BaseTokenFilterFactory {
  Pattern p;
  String replacement;
  boolean all = true;
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    try {
      p = Pattern.compile(args.get("pattern"));
    } catch (PatternSyntaxException e) {
      throw new RuntimeException
        ("Configuration Error: 'pattern' can not be parsed in " +
         this.getClass().getName(), e);
    }
    replacement = args.get("replacement");
    String r = args.get("replace");
    if (null != r) {
      if (r.equals("all")) {
        all = true;
      } else {
        if (r.equals("first")) {
          all = false;
        } else {
          throw new RuntimeException
            ("Configuration Error: 'replace' must be 'first' or 'all' in "
             + this.getClass().getName());
        }
      }
    }
  }
  public PatternReplaceFilter create(TokenStream input) {
    return new PatternReplaceFilter(input, p, replacement, all);
  }
}
