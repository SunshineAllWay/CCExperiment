package org.apache.solr.analysis;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.tartarus.snowball.SnowballProgram;
import java.io.IOException;
import java.io.File;
import java.util.List;
public class EnglishPorterFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  public static final String PROTECTED_TOKENS = "protected";
  public void inform(ResourceLoader loader) {
    String wordFiles = args.get(PROTECTED_TOKENS);
    if (wordFiles != null) {
      try {
        File protectedWordFiles = new File(wordFiles);
        if (protectedWordFiles.exists()) {
          List<String> wlist = loader.getLines(wordFiles);
          protectedWords = new CharArraySet(wlist, false);
        } else  {
          List<String> files = StrUtils.splitFileNames(wordFiles);
          for (String file : files) {
            List<String> wlist = loader.getLines(file.trim());
            if (protectedWords == null)
              protectedWords = new CharArraySet(wlist, false);
            else
              protectedWords.addAll(wlist);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  private CharArraySet protectedWords = null;
  public EnglishPorterFilter create(TokenStream input) {
    return new EnglishPorterFilter(input, protectedWords);
  }
}
@Deprecated
class EnglishPorterFilter extends SnowballPorterFilter {
  public EnglishPorterFilter(TokenStream source, CharArraySet protWords) {
    super(source, new org.tartarus.snowball.ext.EnglishStemmer(), protWords);
  }
}
