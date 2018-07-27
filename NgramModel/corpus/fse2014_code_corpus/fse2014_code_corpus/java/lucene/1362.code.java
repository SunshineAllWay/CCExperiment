package org.apache.lucene.wordnet;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
public class SynonymTokenFilter extends TokenFilter {
  public static final String SYNONYM_TOKEN_TYPE = "SYNONYM";
  private final SynonymMap synonyms;
  private final int maxSynonyms;
  private String[] stack = null;
  private int index = 0;
  private AttributeSource.State current = null;
  private int todo = 0;
  private TermAttribute termAtt;
  private TypeAttribute typeAtt;
  private PositionIncrementAttribute posIncrAtt;
  public SynonymTokenFilter(TokenStream input, SynonymMap synonyms, int maxSynonyms) {
    super(input);
    if (input == null)
      throw new IllegalArgumentException("input must not be null");
    if (synonyms == null)
      throw new IllegalArgumentException("synonyms must not be null");
    if (maxSynonyms < 0) 
      throw new IllegalArgumentException("maxSynonyms must not be negative");
    this.synonyms = synonyms;
    this.maxSynonyms = maxSynonyms;
    this.termAtt = addAttribute(TermAttribute.class);
    this.typeAtt = addAttribute(TypeAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    while (todo > 0 && index < stack.length) { 
      if (createToken(stack[index++], current)) {
        todo--;
        return true;
      }
    }
    if (!input.incrementToken()) return false; 
    stack = synonyms.getSynonyms(termAtt.term()); 
    if (stack.length > maxSynonyms) randomize(stack);
    index = 0;
    current = captureState();
    todo = maxSynonyms;
    return true;
  }
  protected boolean createToken(String synonym, AttributeSource.State current) {
    restoreState(current);
    termAtt.setTermBuffer(synonym);
    typeAtt.setType(SYNONYM_TOKEN_TYPE);
    posIncrAtt.setPositionIncrement(0);
    return true;
  }
  private static void randomize(Object[] arr) {
    int seed = 1234567; 
    int randomState = 4*seed + 1;
    int len = arr.length;
    for (int i=0; i < len-1; i++) {
      randomState *= 0x278DDE6D; 
      int r = randomState % (len-i);
      if (r < 0) r = -r; 
      Object tmp = arr[i];
      arr[i] = arr[i + r];
      arr[i + r] = tmp;
    }   
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    stack = null;
    index = 0;
    current = null;
    todo = 0;
  }
}
