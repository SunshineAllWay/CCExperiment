package org.apache.lucene.analysis.compound.hyphenation;
import java.io.Serializable;
public class Hyphen implements Serializable {
  public String preBreak;
  public String noBreak;
  public String postBreak;
  Hyphen(String pre, String no, String post) {
    preBreak = pre;
    noBreak = no;
    postBreak = post;
  }
  Hyphen(String pre) {
    preBreak = pre;
    noBreak = null;
    postBreak = null;
  }
  @Override
  public String toString() {
    if (noBreak == null && postBreak == null && preBreak != null
        && preBreak.equals("-")) {
      return "-";
    }
    StringBuilder res = new StringBuilder("{");
    res.append(preBreak);
    res.append("}{");
    res.append(postBreak);
    res.append("}{");
    res.append(noBreak);
    res.append('}');
    return res.toString();
  }
}
