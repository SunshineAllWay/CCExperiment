package org.apache.solr.update.processor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.solr.common.params.SolrParams;
public class TextProfileSignature extends MD5Signature {
  private float quantRate;
  private float minTokenLen;
  public void init(SolrParams params) {
    quantRate = params.getFloat("quantRate", 0.01f);
    minTokenLen = params.getInt("minTokenLen", 2);
  }
  public byte[] getSignature() {
    return super.getSignature();
  }
  @Override
  public void add(String content) {
    HashMap<String, Token> tokens = new HashMap<String, Token>();
    StringBuilder curToken = new StringBuilder();
    int maxFreq = 0;
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        curToken.append(Character.toLowerCase(c));
      } else {
        if (curToken.length() > 0) {
          if (curToken.length() > minTokenLen) {
            String s = curToken.toString();
            Token tok = tokens.get(s);
            if (tok == null) {
              tok = new Token(0, s);
              tokens.put(s, tok);
            }
            tok.cnt++;
            if (tok.cnt > maxFreq)
              maxFreq = tok.cnt;
          }
          curToken.setLength(0);
        }
      }
    }
    if (curToken.length() > minTokenLen) {
      String s = curToken.toString();
      Token tok = tokens.get(s);
      if (tok == null) {
        tok = new Token(0, s);
        tokens.put(s, tok);
      }
      tok.cnt++;
      if (tok.cnt > maxFreq)
        maxFreq = tok.cnt;
    }
    Iterator<Token> it = tokens.values().iterator();
    ArrayList<Token> profile = new ArrayList<Token>();
    int quant = Math.round(maxFreq * quantRate);
    if (quant < 2) {
      if (maxFreq > 1)
        quant = 2;
      else
        quant = 1;
    }
    while (it.hasNext()) {
      Token t = it.next();
      t.cnt = (t.cnt / quant) * quant;
      if (t.cnt < quant) {
        continue;
      }
      profile.add(t);
    }
    Collections.sort(profile, new TokenComparator());
    StringBuilder newText = new StringBuilder();
    it = profile.iterator();
    while (it.hasNext()) {
      Token t = it.next();
      if (newText.length() > 0)
        newText.append("\n");
      newText.append(t.toString());
    }
    super.add(newText.toString());
  }
  private static class Token {
    public int cnt;
    public String val;
    public Token(int cnt, String val) {
      this.cnt = cnt;
      this.val = val;
    }
    public String toString() {
      return val + " " + cnt;
    }
  }
  private static class TokenComparator implements Comparator<Token> {
    public int compare(Token t1, Token t2) {
      return t2.cnt - t1.cnt;
    }
  }
}
