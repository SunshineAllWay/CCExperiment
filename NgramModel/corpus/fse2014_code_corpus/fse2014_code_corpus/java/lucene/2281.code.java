package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class SynonymFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  public void inform(ResourceLoader loader) {
    String synonyms = args.get("synonyms");
    boolean ignoreCase = getBoolean("ignoreCase", false);
    boolean expand = getBoolean("expand", true);
    String tf = args.get("tokenizerFactory");
    TokenizerFactory tokFactory = null;
    if( tf != null ){
      tokFactory = loadTokenizerFactory( loader, tf, args );
    }
    if (synonyms != null) {
      List<String> wlist=null;
      try {
        File synonymFile = new File(synonyms);
        if (synonymFile.exists()) {
          wlist = loader.getLines(synonyms);
        } else  {
          List<String> files = StrUtils.splitFileNames(synonyms);
          wlist = new ArrayList<String>();
          for (String file : files) {
            List<String> lines = loader.getLines(file.trim());
            wlist.addAll(lines);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      synMap = new SynonymMap(ignoreCase);
      parseRules(wlist, synMap, "=>", ",", expand,tokFactory);
    }
  }
  private SynonymMap synMap;
  static void parseRules(List<String> rules, SynonymMap map, String mappingSep,
    String synSep, boolean expansion, TokenizerFactory tokFactory) {
    int count=0;
    for (String rule : rules) {
      List<String> mapping = StrUtils.splitSmart(rule, mappingSep, false);
      List<List<String>> source;
      List<List<String>> target;
      if (mapping.size() > 2) {
        throw new RuntimeException("Invalid Synonym Rule:" + rule);
      } else if (mapping.size()==2) {
        source = getSynList(mapping.get(0), synSep, tokFactory);
        target = getSynList(mapping.get(1), synSep, tokFactory);
      } else {
        source = getSynList(mapping.get(0), synSep, tokFactory);
        if (expansion) {
          target = source;
        } else {
          target = new ArrayList<List<String>>(1);
          target.add(source.get(0));
        }
      }
      boolean includeOrig=false;
      for (List<String> fromToks : source) {
        count++;
        for (List<String> toToks : target) {
          map.add(fromToks,
                  SynonymMap.makeTokens(toToks),
                  includeOrig,
                  true
          );
        }
      }
    }
  }
  private static List<List<String>> getSynList(String str, String separator, TokenizerFactory tokFactory) {
    List<String> strList = StrUtils.splitSmart(str, separator, false);
    List<List<String>> synList = new ArrayList<List<String>>();
    for (String toks : strList) {
      List<String> tokList = tokFactory == null ?
        StrUtils.splitWS(toks, true) : splitByTokenizer(toks, tokFactory);
      synList.add(tokList);
    }
    return synList;
  }
  private static List<String> splitByTokenizer(String source, TokenizerFactory tokFactory){
    StringReader reader = new StringReader( source );
    TokenStream ts = loadTokenizer(tokFactory, reader);
    List<String> tokList = new ArrayList<String>();
    try {
      TermAttribute termAtt = (TermAttribute) ts.addAttribute(TermAttribute.class);
      while (ts.incrementToken()){
        String text = new String(termAtt.termBuffer(), 0, termAtt.termLength());
        if( text.length() > 0 )
          tokList.add( text );
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally{
      reader.close();
    }
    return tokList;
  }
  private static TokenizerFactory loadTokenizerFactory(ResourceLoader loader, String cname, Map<String,String> args){
    TokenizerFactory tokFactory = (TokenizerFactory)loader.newInstance( cname );
    tokFactory.init( args );
    return tokFactory;
  }
  private static TokenStream loadTokenizer(TokenizerFactory tokFactory, Reader reader){
    return tokFactory.create( reader );
  }
  public SynonymMap getSynonymMap() {
    return synMap;
  }
  public SynonymFilter create(TokenStream input) {
    return new SynonymFilter(input,synMap);
  }
}
