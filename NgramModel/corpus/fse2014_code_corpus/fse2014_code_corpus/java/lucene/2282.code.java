package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.solr.util.CharArrayMap;
import java.util.*;
public class SynonymMap {
  CharArrayMap<SynonymMap> submap; 
  Token[] synonyms;
  int flags;
  static final int INCLUDE_ORIG=0x01;
  static final int IGNORE_CASE=0x02;
  public SynonymMap() {}
  public SynonymMap(boolean ignoreCase) {
    if (ignoreCase) flags |= IGNORE_CASE;
  }
  public boolean includeOrig() { return (flags & INCLUDE_ORIG) != 0; }
  public boolean ignoreCase() { return (flags & IGNORE_CASE) != 0; }
  public void add(List<String> singleMatch, List<Token> replacement, boolean includeOrig, boolean mergeExisting) {
    SynonymMap currMap = this;
    for (String str : singleMatch) {
      if (currMap.submap==null) {
        currMap.submap = new CharArrayMap<SynonymMap>(1, ignoreCase());
      }
      SynonymMap map = currMap.submap.get(str);
      if (map==null) {
        map = new SynonymMap();
        map.flags |= flags & IGNORE_CASE;
        currMap.submap.put(str, map);
      }
      currMap = map;
    }
    if (currMap.synonyms != null && !mergeExisting) {
      throw new RuntimeException("SynonymFilter: there is already a mapping for " + singleMatch);
    }
    List superset = currMap.synonyms==null ? replacement :
          mergeTokens(Arrays.asList(currMap.synonyms), replacement);
    currMap.synonyms = (Token[])superset.toArray(new Token[superset.size()]);
    if (includeOrig) currMap.flags |= INCLUDE_ORIG;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder("<");
    if (synonyms!=null) {
      sb.append("[");
      for (int i=0; i<synonyms.length; i++) {
        if (i!=0) sb.append(',');
        sb.append(synonyms[i]);
      }
      if ((flags & INCLUDE_ORIG)!=0) {
        sb.append(",ORIG");
      }
      sb.append("],");
    }
    sb.append(submap);
    sb.append(">");
    return sb.toString();
  }
  public static List<Token> makeTokens(List<String> strings) {
    List<Token> ret = new ArrayList<Token>(strings.size());
    for (String str : strings) {
      Token newTok = new Token(0,0,"SYNONYM");
      newTok.setTermBuffer(str.toCharArray(), 0, str.length());
      ret.add(newTok);
    }
    return ret;
  }
  public static List<Token> mergeTokens(List<Token> lst1, List<Token> lst2) {
    ArrayList<Token> result = new ArrayList<Token>();
    if (lst1 ==null || lst2 ==null) {
      if (lst2 != null) result.addAll(lst2);
      if (lst1 != null) result.addAll(lst1);
      return result;
    }
    int pos=0;
    Iterator<Token> iter1=lst1.iterator();
    Iterator<Token> iter2=lst2.iterator();
    Token tok1 = iter1.hasNext() ? iter1.next() : null;
    Token tok2 = iter2.hasNext() ? iter2.next() : null;
    int pos1 = tok1!=null ? tok1.getPositionIncrement() : 0;
    int pos2 = tok2!=null ? tok2.getPositionIncrement() : 0;
    while(tok1!=null || tok2!=null) {
      while (tok1 != null && (pos1 <= pos2 || tok2==null)) {
        Token tok = new Token(tok1.startOffset(), tok1.endOffset(), tok1.type());
        tok.setTermBuffer(tok1.termBuffer(), 0, tok1.termLength());
        tok.setPositionIncrement(pos1-pos);
        result.add(tok);
        pos=pos1;
        tok1 = iter1.hasNext() ? iter1.next() : null;
        pos1 += tok1!=null ? tok1.getPositionIncrement() : 0;
      }
      while (tok2 != null && (pos2 <= pos1 || tok1==null)) {
        Token tok = new Token(tok2.startOffset(), tok2.endOffset(), tok2.type());
        tok.setTermBuffer(tok2.termBuffer(), 0, tok2.termLength());
        tok.setPositionIncrement(pos2-pos);
        result.add(tok);
        pos=pos2;
        tok2 = iter2.hasNext() ? iter2.next() : null;
        pos2 += tok2!=null ? tok2.getPositionIncrement() : 0;
      }
    }
    return result;
  }
}
