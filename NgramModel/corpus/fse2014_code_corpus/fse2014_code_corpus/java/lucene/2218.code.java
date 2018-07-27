package org.apache.solr.analysis;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.util.plugin.ResourceLoaderAware;
public class CommonGramsFilterFactory extends BaseTokenFilterFactory implements
    ResourceLoaderAware {
  public void inform(ResourceLoader loader) {
    String commonWordFiles = args.get("words");
    ignoreCase = getBoolean("ignoreCase", false);
    if (commonWordFiles != null) {
      try {
        List<String> files = StrUtils.splitFileNames(commonWordFiles);
          if (commonWords == null && files.size() > 0){
            commonWords = new CharArraySet(files.size() * 10, ignoreCase);
          }
          for (String file : files) {
            List<String> wlist = loader.getLines(file.trim());
            commonWords.addAll(CommonGramsFilter.makeCommonSet((String[])wlist.toArray(new String[0]), ignoreCase));
          }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      commonWords = (CharArraySet) StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }
  }
    private CharArraySet commonWords;
    private boolean ignoreCase;
  public boolean isIgnoreCase() {
    return ignoreCase;
  }
  public Set getCommonWords() {
    return commonWords;
  }
  public CommonGramsFilter create(TokenStream input) {
    CommonGramsFilter commonGrams = new CommonGramsFilter(input, commonWords, ignoreCase);
    return commonGrams;
  }
}
