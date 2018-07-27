package org.apache.solr.search;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrException;
import java.io.IOException;
import java.util.List;
public final class QueryResultKey {
  final Query query;
  final Sort sort;
  final SortField[] sfields;
  final List<Query> filters;
  final int nc_flags;  
  private final int hc;  
  private static SortField[] defaultSort = new SortField[0];
  public QueryResultKey(Query query, List<Query> filters, Sort sort, int nc_flags) throws IOException {
    this.query = query;
    this.sort = sort;
    this.filters = filters;
    this.nc_flags = nc_flags;
    int h = query.hashCode();
    if (filters != null) h ^= filters.hashCode();
    sfields = (this.sort !=null) ? this.sort.getSort() : defaultSort;
    for (SortField sf : sfields) {
      h = h*29 + sf.hashCode();
    }
    hc = h;
  }
  public int hashCode() {
    return hc;
  }
  public boolean equals(Object o) {
    if (o==this) return true;
    if (!(o instanceof QueryResultKey)) return false;
    QueryResultKey other = (QueryResultKey)o;
    if (this.hc != other.hc) return false;
    if (this.sfields.length != other.sfields.length) return false;
    if (!this.query.equals(other.query)) return false;
    if (!isEqual(this.filters, other.filters)) return false;
    for (int i=0; i<sfields.length; i++) {
      SortField sf1 = this.sfields[i];
      SortField sf2 = other.sfields[i];
      if (!sf1.equals(sf2)) return false;
    }
    return true;
  }
  private static boolean isEqual(Object o1, Object o2) {
    if (o1==o2) return true;  
    if (o1==null || o2==null) return false;
    return o1.equals(o2);
  }
}
