package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
public class SearchTravRetHighlightTask extends SearchTravTask {
  protected int numToHighlight = Integer.MAX_VALUE;
  protected boolean mergeContiguous;
  protected int maxFrags = 2;
  protected Set<String> paramFields = Collections.emptySet();
  protected Highlighter highlighter;
  protected int maxDocCharsToAnalyze;
  public SearchTravRetHighlightTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public void setup() throws Exception {
    super.setup();
    PerfRunData data = getRunData();
    if (data.getConfig().get("doc.stored", false) == false){
      throw new Exception("doc.stored must be set to true");
    }
    maxDocCharsToAnalyze = data.getConfig().get("highlighter.maxDocCharsToAnalyze", Highlighter.DEFAULT_MAX_CHARS_TO_ANALYZE);
  }
  @Override
  public boolean withRetrieve() {
    return true;
  }
  @Override
  public int numToHighlight() {
    return numToHighlight;
  }
  @Override
  protected BenchmarkHighlighter getBenchmarkHighlighter(Query q){
    highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(q));
    highlighter.setMaxDocCharsToAnalyze(maxDocCharsToAnalyze);
    return new BenchmarkHighlighter(){
      @Override
      public int doHighlight(IndexReader reader, int doc, String field,
          Document document, Analyzer analyzer, String text) throws Exception {
        TokenStream ts = TokenSources.getAnyTokenStream(reader, doc, field, document, analyzer);
        TextFragment[] frag = highlighter.getBestTextFragments(ts, text, mergeContiguous, maxFrags);
        return frag != null ? frag.length : 0;
      }
    };
  }
  @Override
  protected Collection<String> getFieldsToHighlight(Document document) {
    Collection<String> result = super.getFieldsToHighlight(document);
    if (paramFields.isEmpty() == false && result.isEmpty() == false) {
      result.retainAll(paramFields);
    } else {
      result = paramFields;
    }
    return result;
  }
  @Override
  public void setParams(String params) {
    String [] splits = params.split(",");
    for (int i = 0; i < splits.length; i++) {
      if (splits[i].startsWith("size[") == true){
        traversalSize = (int)Float.parseFloat(splits[i].substring("size[".length(),splits[i].length() - 1));
      } else if (splits[i].startsWith("highlight[") == true){
        numToHighlight = (int)Float.parseFloat(splits[i].substring("highlight[".length(),splits[i].length() - 1));
      } else if (splits[i].startsWith("maxFrags[") == true){
        maxFrags = (int)Float.parseFloat(splits[i].substring("maxFrags[".length(),splits[i].length() - 1));
      } else if (splits[i].startsWith("mergeContiguous[") == true){
        mergeContiguous = Boolean.valueOf(splits[i].substring("mergeContiguous[".length(),splits[i].length() - 1)).booleanValue();
      } else if (splits[i].startsWith("fields[") == true){
        paramFields = new HashSet<String>();
        String fieldNames = splits[i].substring("fields[".length(), splits[i].length() - 1);
        String [] fieldSplits = fieldNames.split(";");
        for (int j = 0; j < fieldSplits.length; j++) {
          paramFields.add(fieldSplits[j]);          
        }
      }
    }
  }
}