package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
public class ReversedWildcardFilterFactory extends BaseTokenFilterFactory {
  private char markerChar = ReverseStringFilter.START_OF_HEADING_MARKER;
  private boolean withOriginal;
  private int maxPosAsterisk;
  private int maxPosQuestion;
  private int minTrailing;
  private float maxFractionAsterisk;
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    withOriginal = getBoolean("withOriginal", true);
    maxPosAsterisk = getInt("maxPosAsterisk", 2);
    maxPosQuestion = getInt("maxPosQuestion", 1);
    minTrailing = getInt("minTrailing", 2);
    maxFractionAsterisk = getFloat("maxFractionAsterisk", 0.0f);
  }
  public TokenStream create(TokenStream input) {
    return new ReversedWildcardFilter(input, withOriginal, markerChar);
  }
  public boolean shouldReverse(String token) {
    int posQ = token.indexOf('?');
    int posA = token.indexOf('*');
    if (posQ == -1 && posA == -1) { 
      return false;
    }
    int pos;
    int lastPos;
    int len = token.length();
    lastPos = token.lastIndexOf('?');
    pos = token.lastIndexOf('*');
    if (pos > lastPos) lastPos = pos;
    if (posQ != -1) {
      pos = posQ;
      if (posA != -1) {
        pos = Math.min(posQ, posA);
      }
    } else {
      pos = posA;
    }
    if (len - lastPos < minTrailing)  { 
      return false;
    }
    if (posQ != -1 && posQ < maxPosQuestion) {  
      return true;
    }
    if (posA != -1 && posA < maxPosAsterisk) { 
      return true;
    }
    if (maxFractionAsterisk > 0.0f && pos < (float)token.length() * maxFractionAsterisk) {
      return true;
    }
    return false;
  }
  public char getMarkerChar() {
    return markerChar;
  }
  protected float getFloat(String name, float defValue) {
    String val = args.get(name);
    if (val == null) {
      return defValue;
    } else {
      return Float.parseFloat(val);
    }
  }
}
