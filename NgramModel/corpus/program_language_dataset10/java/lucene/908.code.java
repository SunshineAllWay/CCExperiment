package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
public class SearchTravRetVectorHighlightTask extends SearchTravTask {
  protected int numToHighlight = Integer.MAX_VALUE;
  protected int maxFrags = 2;
  protected int fragSize = 100;
  protected Set<String> paramFields = Collections.emptySet();
  protected FastVectorHighlighter highlighter;
  public SearchTravRetVectorHighlightTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public void setup() throws Exception {
    super.setup();
    PerfRunData data = getRunData();
    if (data.getConfig().get("doc.stored", false) == false){
      throw new Exception("doc.stored must be set to true");
    }
    if (data.getConfig().get("doc.term.vector.offsets", false) == false){
      throw new Exception("doc.term.vector.offsets must be set to true");
    }
    if (data.getConfig().get("doc.term.vector.positions", false) == false){
      throw new Exception("doc.term.vector.positions must be set to true");
    }
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
    highlighter = new FastVectorHighlighter( false, false );
    final FieldQuery fq = highlighter.getFieldQuery( q );
    return new BenchmarkHighlighter(){
      @Override
      public int doHighlight(IndexReader reader, int doc, String field,
          Document document, Analyzer analyzer, String text) throws Exception {
        String[] fragments = highlighter.getBestFragments(fq, reader, doc, field, fragSize, maxFrags);
        return fragments != null ? fragments.length : 0;
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
      } else if (splits[i].startsWith("fragSize[") == true){
        fragSize = (int)Float.parseFloat(splits[i].substring("fragSize[".length(),splits[i].length() - 1));
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
