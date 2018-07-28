package org.apache.lucene.search.payloads;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Payload;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.English;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import static org.apache.lucene.util.LuceneTestCaseJ4.TEST_VERSION_CURRENT;
import java.io.Reader;
import java.io.IOException;
public class PayloadHelper {
  private byte[] payloadField = new byte[]{1};
  private byte[] payloadMultiField1 = new byte[]{2};
  private byte[] payloadMultiField2 = new byte[]{4};
  public static final String NO_PAYLOAD_FIELD = "noPayloadField";
  public static final String MULTI_FIELD = "multiField";
  public static final String FIELD = "field";
  public class PayloadAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream result = new LowerCaseTokenizer(TEST_VERSION_CURRENT, reader);
      result = new PayloadFilter(result, fieldName);
      return result;
    }
  }
  public class PayloadFilter extends TokenFilter {
    String fieldName;
    int numSeen = 0;
    PayloadAttribute payloadAtt;
    public PayloadFilter(TokenStream input, String fieldName) {
      super(input);
      this.fieldName = fieldName;
      payloadAtt = addAttribute(PayloadAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        if (fieldName.equals(FIELD))
        {
          payloadAtt.setPayload(new Payload(payloadField));
        }
        else if (fieldName.equals(MULTI_FIELD))
        {
          if (numSeen  % 2 == 0)
          {
            payloadAtt.setPayload(new Payload(payloadMultiField1));
          }
          else
          {
            payloadAtt.setPayload(new Payload(payloadMultiField2));
          }
          numSeen++;
        }
        return true;
      }
      return false;
    }
  }
  public IndexSearcher setUp(Similarity similarity, int numDocs) throws IOException {
    RAMDirectory directory = new RAMDirectory();
    PayloadAnalyzer analyzer = new PayloadAnalyzer();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setSimilarity(similarity));
    for (int i = 0; i < numDocs; i++) {
      Document doc = new Document();
      doc.add(new Field(FIELD, English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field(MULTI_FIELD, English.intToEnglish(i) + "  " + English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field(NO_PAYLOAD_FIELD, English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    IndexSearcher searcher = new IndexSearcher(directory, true);
    searcher.setSimilarity(similarity);
    return searcher;
  }
}
