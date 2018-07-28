package org.apache.solr.search;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
public class SortSpec 
{
  Sort sort;
  int num;
  int offset;
  public SortSpec(Sort sort, int num) {
    this(sort,0,num);
  }
  public SortSpec(Sort sort, int offset, int num) {
    this.sort=sort;
    this.offset=offset;
    this.num=num;
  }
  public void setSort( Sort s )
  {
    sort = s;
  }
  public static boolean includesScore(Sort sort) {
    if (sort==null) return true;
    for (SortField sf : sort.getSort()) {
      if (sf.getType() == SortField.SCORE) return true;
    }
    return false;
  }
  public boolean includesScore() {
    return includesScore(sort);
  }
  public Sort getSort() { return sort; }
  public int getOffset() { return offset; }
  public int getCount() { return num; }
  @Override
  public String toString() {
    return "start="+offset+ "&rows="+num + (sort==null ? "" : "&sort="+sort); 
  }
}
