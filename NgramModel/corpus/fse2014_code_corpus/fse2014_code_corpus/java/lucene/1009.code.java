package org.apache.lucene.search.highlight;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
public class TokenSources {
  public static TokenStream getAnyTokenStream(IndexReader reader, int docId,
      String field, Document doc, Analyzer analyzer) throws IOException {
    TokenStream ts = null;
    TermFreqVector tfv = reader.getTermFreqVector(docId, field);
    if (tfv != null) {
      if (tfv instanceof TermPositionVector) {
        ts = getTokenStream((TermPositionVector) tfv);
      }
    }
    if (ts == null) {
      ts = getTokenStream(doc, field, analyzer);
    }
    return ts;
  }
  public static TokenStream getAnyTokenStream(IndexReader reader, int docId,
      String field, Analyzer analyzer) throws IOException {
    TokenStream ts = null;
    TermFreqVector tfv = reader.getTermFreqVector(docId, field);
    if (tfv != null) {
      if (tfv instanceof TermPositionVector) {
        ts = getTokenStream((TermPositionVector) tfv);
      }
    }
    if (ts == null) {
      ts = getTokenStream(reader, docId, field, analyzer);
    }
    return ts;
  }
  public static TokenStream getTokenStream(TermPositionVector tpv) {
    return getTokenStream(tpv, false);
  }
  public static TokenStream getTokenStream(TermPositionVector tpv,
      boolean tokenPositionsGuaranteedContiguous) {
    if (!tokenPositionsGuaranteedContiguous && tpv.getTermPositions(0) != null) {
      return new TokenStreamFromTermPositionVector(tpv);
    }
    class StoredTokenStream extends TokenStream {
      Token tokens[];
      int currentToken = 0;
      TermAttribute termAtt;
      OffsetAttribute offsetAtt;
      StoredTokenStream(Token tokens[]) {
        this.tokens = tokens;
        termAtt = addAttribute(TermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
      }
      @Override
      public boolean incrementToken() throws IOException {
        if (currentToken >= tokens.length) {
          return false;
        }
        Token token = tokens[currentToken++];
        clearAttributes();
        termAtt.setTermBuffer(token.term());
        offsetAtt.setOffset(token.startOffset(), token.endOffset());
        return true;
      }
    }
    String[] terms = tpv.getTerms();
    int[] freq = tpv.getTermFrequencies();
    int totalTokens = 0;
    for (int t = 0; t < freq.length; t++) {
      totalTokens += freq[t];
    }
    Token tokensInOriginalOrder[] = new Token[totalTokens];
    ArrayList<Token> unsortedTokens = null;
    for (int t = 0; t < freq.length; t++) {
      TermVectorOffsetInfo[] offsets = tpv.getOffsets(t);
      if (offsets == null) {
        throw new IllegalArgumentException("Required TermVector Offset information was not found");
      }
      int[] pos = null;
      if (tokenPositionsGuaranteedContiguous) {
        pos = tpv.getTermPositions(t);
      }
      if (pos == null) {
        if (unsortedTokens == null) {
          unsortedTokens = new ArrayList<Token>();
        }
        for (int tp = 0; tp < offsets.length; tp++) {
          Token token = new Token(offsets[tp].getStartOffset(), offsets[tp]
              .getEndOffset());
          token.setTermBuffer(terms[t]);
          unsortedTokens.add(token);
        }
      } else {
        for (int tp = 0; tp < pos.length; tp++) {
          Token token = new Token(terms[t], offsets[tp].getStartOffset(),
              offsets[tp].getEndOffset());
          tokensInOriginalOrder[pos[tp]] = token;
        }
      }
    }
    if (unsortedTokens != null) {
      tokensInOriginalOrder = unsortedTokens.toArray(new Token[unsortedTokens
          .size()]);
      Arrays.sort(tokensInOriginalOrder, new Comparator<Token>() {
        public int compare(Token t1, Token t2) {
          if (t1.startOffset() > t2.endOffset())
            return 1;
          if (t1.startOffset() < t2.startOffset())
            return -1;
          return 0;
        }
      });
    }
    return new StoredTokenStream(tokensInOriginalOrder);
  }
  public static TokenStream getTokenStream(IndexReader reader, int docId,
      String field) throws IOException {
    TermFreqVector tfv = reader.getTermFreqVector(docId, field);
    if (tfv == null) {
      throw new IllegalArgumentException(field + " in doc #" + docId
          + "does not have any term position data stored");
    }
    if (tfv instanceof TermPositionVector) {
      TermPositionVector tpv = (TermPositionVector) reader.getTermFreqVector(
          docId, field);
      return getTokenStream(tpv);
    }
    throw new IllegalArgumentException(field + " in doc #" + docId
        + "does not have any term position data stored");
  }
  public static TokenStream getTokenStream(IndexReader reader, int docId,
      String field, Analyzer analyzer) throws IOException {
    Document doc = reader.document(docId);
    return getTokenStream(doc, field, analyzer);
  }
  public static TokenStream getTokenStream(Document doc, String field,
      Analyzer analyzer) {
    String contents = doc.get(field);
    if (contents == null) {
      throw new IllegalArgumentException("Field " + field
          + " in document is not stored and cannot be analyzed");
    }
    return getTokenStream(field, contents, analyzer);
  }
  public static TokenStream getTokenStream(String field, String contents,
      Analyzer analyzer) {
    return analyzer.tokenStream(field, new StringReader(contents));
  }
}
