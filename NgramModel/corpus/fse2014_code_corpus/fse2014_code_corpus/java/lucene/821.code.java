package org.apache.lucene.analysis.cn.smart.hhmm;
class PathNode implements Comparable<PathNode> {
  public double weight;
  public int preNode;
  public int compareTo(PathNode pn) {
    if (weight < pn.weight)
      return -1;
    else if (weight == pn.weight)
      return 0;
    else
      return 1;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + preNode;
    long temp;
    temp = Double.doubleToLongBits(weight);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PathNode other = (PathNode) obj;
    if (preNode != other.preNode)
      return false;
    if (Double.doubleToLongBits(weight) != Double
        .doubleToLongBits(other.weight))
      return false;
    return true;
  }
}
