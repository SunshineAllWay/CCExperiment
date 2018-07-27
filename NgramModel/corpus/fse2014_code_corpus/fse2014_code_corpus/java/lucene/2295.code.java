package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.util.StrUtils;
import java.util.Map;
import java.io.File;
import java.util.List;
import java.io.IOException;
public class WordDelimiterFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
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
  int generateWordParts=0;
  int generateNumberParts=0;
  int catenateWords=0;
  int catenateNumbers=0;
  int catenateAll=0;
  int splitOnCaseChange=0;
  int splitOnNumerics=0;
  int preserveOriginal=0;
  int stemEnglishPossessive=0;
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    generateWordParts = getInt("generateWordParts", 1);
    generateNumberParts = getInt("generateNumberParts", 1);
    catenateWords = getInt("catenateWords", 0);
    catenateNumbers = getInt("catenateNumbers", 0);
    catenateAll = getInt("catenateAll", 0);
    splitOnCaseChange = getInt("splitOnCaseChange", 1);
    splitOnNumerics = getInt("splitOnNumerics", 1);
    preserveOriginal = getInt("preserveOriginal", 0);
    stemEnglishPossessive = getInt("stemEnglishPossessive", 1);
  }
  public WordDelimiterFilter create(TokenStream input) {
    return new WordDelimiterFilter(input,
                                   generateWordParts, generateNumberParts,
                                   catenateWords, catenateNumbers, catenateAll,
                                   splitOnCaseChange, preserveOriginal,
                                   splitOnNumerics, stemEnglishPossessive, protectedWords);
  }
}
