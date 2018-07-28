package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.solr.common.SolrException;
import org.apache.solr.schema.DateField;
import static org.apache.solr.schema.TrieField.TrieTypes;
import java.io.IOException;
import java.io.Reader;
public class TrieTokenizerFactory extends BaseTokenizerFactory {
  protected final int precisionStep;
  protected final TrieTypes type;
  public TrieTokenizerFactory(TrieTypes type, int precisionStep) {
    this.type = type;
    this.precisionStep = precisionStep;
  }
  public TrieTokenizer create(Reader input) {
    return new TrieTokenizer(input, type, precisionStep, TrieTokenizer.getNumericTokenStream(precisionStep));
  }
}
class TrieTokenizer extends Tokenizer {
  protected static final DateField dateField = new DateField();
  protected final int precisionStep;
  protected final TrieTypes type;
  protected final NumericTokenStream ts;
  static NumericTokenStream getNumericTokenStream(int precisionStep) {
    return new NumericTokenStream(precisionStep);
  }
  public TrieTokenizer(Reader input, TrieTypes type, int precisionStep, NumericTokenStream ts) {
    super(ts);
    this.type = type;
    this.precisionStep = precisionStep;
    this.ts = ts;
   try {
     reset(input);
   } catch (IOException e) {
     throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to create TrieIndexTokenizer", e);
   }
  }
  @Override
  public void reset(Reader input) throws IOException {
   try {
      super.reset(input);
      input = super.input;
      char[] buf = new char[32];
      int len = input.read(buf);
      String v = new String(buf, 0, len);
      switch (type) {
        case INTEGER:
          ts.setIntValue(Integer.parseInt(v));
          break;
        case FLOAT:
          ts.setFloatValue(Float.parseFloat(v));
          break;
        case LONG:
          ts.setLongValue(Long.parseLong(v));
          break;
        case DOUBLE:
          ts.setDoubleValue(Double.parseDouble(v));
          break;
        case DATE:
          ts.setLongValue(dateField.parseMath(null, v).getTime());
          break;
        default:
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field");
      }
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to create TrieIndexTokenizer", e);
    }
    ts.reset();
  }
  @Override
  public boolean incrementToken() throws IOException {
    return ts.incrementToken();
  }
}
