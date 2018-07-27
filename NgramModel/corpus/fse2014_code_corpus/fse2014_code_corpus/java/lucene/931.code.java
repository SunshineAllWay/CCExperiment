package org.apache.lucene.benchmark.quality.utils;
import java.io.IOException;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.Searcher;
public class DocNameExtractor {
  private FieldSelector fldSel;
  private String docNameField;
  public DocNameExtractor (final String docNameField) {
    this.docNameField = docNameField;
    fldSel = new FieldSelector() {
      public FieldSelectorResult accept(String fieldName) {
        return fieldName.equals(docNameField) ? 
            FieldSelectorResult.LOAD_AND_BREAK :
              FieldSelectorResult.NO_LOAD;
      }
    };
  }
  public String docName(Searcher searcher, int docid) throws IOException {
    return searcher.doc(docid,fldSel).get(docNameField);
  }
}
