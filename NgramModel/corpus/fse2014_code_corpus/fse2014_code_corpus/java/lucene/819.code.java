package org.apache.lucene.analysis.cn.smart.hhmm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.cn.smart.Utility;
class BiSegGraph {
  private Map<Integer,ArrayList<SegTokenPair>> tokenPairListTable = new HashMap<Integer,ArrayList<SegTokenPair>>();
  private List<SegToken> segTokenList;
  private static BigramDictionary bigramDict = BigramDictionary.getInstance();
  public BiSegGraph(SegGraph segGraph) {
    segTokenList = segGraph.makeIndex();
    generateBiSegGraph(segGraph);
  }
  private void generateBiSegGraph(SegGraph segGraph) {
    double smooth = 0.1;
    int wordPairFreq = 0;
    int maxStart = segGraph.getMaxStart();
    double oneWordFreq, weight, tinyDouble = 1.0 / Utility.MAX_FREQUENCE;
    int next;
    char[] idBuffer;
    segTokenList = segGraph.makeIndex();
    int key = -1;
    List<SegToken> nextTokens = null;
    while (key < maxStart) {
      if (segGraph.isStartExist(key)) {
        List<SegToken> tokenList = segGraph.getStartList(key);
        for (SegToken t1 : tokenList) {
          oneWordFreq = t1.weight;
          next = t1.endOffset;
          nextTokens = null;
          while (next <= maxStart) {
            if (segGraph.isStartExist(next)) {
              nextTokens = segGraph.getStartList(next);
              break;
            }
            next++;
          }
          if (nextTokens == null) {
            break;
          }
          for (SegToken t2 : nextTokens) {
            idBuffer = new char[t1.charArray.length + t2.charArray.length + 1];
            System.arraycopy(t1.charArray, 0, idBuffer, 0, t1.charArray.length);
            idBuffer[t1.charArray.length] = BigramDictionary.WORD_SEGMENT_CHAR;
            System.arraycopy(t2.charArray, 0, idBuffer,
                t1.charArray.length + 1, t2.charArray.length);
            wordPairFreq = bigramDict.getFrequency(idBuffer);
            weight = -Math
                .log(smooth
                    * (1.0 + oneWordFreq)
                    / (Utility.MAX_FREQUENCE + 0.0)
                    + (1.0 - smooth)
                    * ((1.0 - tinyDouble) * wordPairFreq / (1.0 + oneWordFreq) + tinyDouble));
            SegTokenPair tokenPair = new SegTokenPair(idBuffer, t1.index,
                t2.index, weight);
            this.addSegTokenPair(tokenPair);
          }
        }
      }
      key++;
    }
  }
  public boolean isToExist(int to) {
    return tokenPairListTable.get(Integer.valueOf(to)) != null;
  }
  public List<SegTokenPair> getToList(int to) {
    return tokenPairListTable.get(to);
  }
  public void addSegTokenPair(SegTokenPair tokenPair) {
    int to = tokenPair.to;
    if (!isToExist(to)) {
      ArrayList<SegTokenPair> newlist = new ArrayList<SegTokenPair>();
      newlist.add(tokenPair);
      tokenPairListTable.put(to, newlist);
    } else {
      List<SegTokenPair> tokenPairList = tokenPairListTable.get(to);
      tokenPairList.add(tokenPair);
    }
  }
  public int getToCount() {
    return tokenPairListTable.size();
  }
  public List<SegToken> getShortPath() {
    int current;
    int nodeCount = getToCount();
    List<PathNode> path = new ArrayList<PathNode>();
    PathNode zeroPath = new PathNode();
    zeroPath.weight = 0;
    zeroPath.preNode = 0;
    path.add(zeroPath);
    for (current = 1; current <= nodeCount; current++) {
      double weight;
      List<SegTokenPair> edges = getToList(current);
      double minWeight = Double.MAX_VALUE;
      SegTokenPair minEdge = null;
      for (SegTokenPair edge : edges) {
        weight = edge.weight;
        PathNode preNode = path.get(edge.from);
        if (preNode.weight + weight < minWeight) {
          minWeight = preNode.weight + weight;
          minEdge = edge;
        }
      }
      PathNode newNode = new PathNode();
      newNode.weight = minWeight;
      newNode.preNode = minEdge.from;
      path.add(newNode);
    }
    int preNode, lastNode;
    lastNode = path.size() - 1;
    current = lastNode;
    List<Integer> rpath = new ArrayList<Integer>();
    List<SegToken> resultPath = new ArrayList<SegToken>();
    rpath.add(current);
    while (current != 0) {
      PathNode currentPathNode = path.get(current);
      preNode = currentPathNode.preNode;
      rpath.add(Integer.valueOf(preNode));
      current = preNode;
    }
    for (int j = rpath.size() - 1; j >= 0; j--) {
      Integer idInteger = (Integer) rpath.get(j);
      int id = idInteger.intValue();
      SegToken t = segTokenList.get(id);
      resultPath.add(t);
    }
    return resultPath;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Collection<ArrayList<SegTokenPair>>  values = tokenPairListTable.values();
    for (ArrayList<SegTokenPair> segList : values) {
      for (SegTokenPair pair : segList) {
        sb.append(pair + "\n");
      }
    }
    return sb.toString();
  }
}
