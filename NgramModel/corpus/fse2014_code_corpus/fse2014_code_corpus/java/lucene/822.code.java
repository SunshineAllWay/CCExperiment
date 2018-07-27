package org.apache.lucene.analysis.cn.smart.hhmm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
class SegGraph {
  private Map<Integer,ArrayList<SegToken>> tokenListTable = new HashMap<Integer,ArrayList<SegToken>>();
  private int maxStart = -1;
  public boolean isStartExist(int s) {
    return tokenListTable.get(s) != null;
  }
  public List<SegToken> getStartList(int s) {
    return tokenListTable.get(s);
  }
  public int getMaxStart() {
    return maxStart;
  }
  public List<SegToken> makeIndex() {
    List<SegToken> result = new ArrayList<SegToken>();
    int s = -1, count = 0, size = tokenListTable.size();
    List<SegToken> tokenList;
    short index = 0;
    while (count < size) {
      if (isStartExist(s)) {
        tokenList = tokenListTable.get(s);
        for (SegToken st : tokenList) {
          st.index = index;
          result.add(st);
          index++;
        }
        count++;
      }
      s++;
    }
    return result;
  }
  public void addToken(SegToken token) {
    int s = token.startOffset;
    if (!isStartExist(s)) {
      ArrayList<SegToken> newlist = new ArrayList<SegToken>();
      newlist.add(token);
      tokenListTable.put(s, newlist);
    } else {
      List<SegToken> tokenList = tokenListTable.get(s);
      tokenList.add(token);
    }
    if (s > maxStart)
      maxStart = s;
  }
  public List<SegToken> toTokenList() {
    List<SegToken> result = new ArrayList<SegToken>();
    int s = -1, count = 0, size = tokenListTable.size();
    List<SegToken> tokenList;
    while (count < size) {
      if (isStartExist(s)) {
        tokenList = tokenListTable.get(s);
        for (SegToken st : tokenList) {
          result.add(st);
        }
        count++;
      }
      s++;
    }
    return result;
  }
  @Override
  public String toString() {
    List<SegToken> tokenList = this.toTokenList();
    StringBuilder sb = new StringBuilder();
    for (SegToken t : tokenList) {
      sb.append(t + "\n");
    }
    return sb.toString();
  }
}
