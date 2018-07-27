package org.apache.lucene.search;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
public class BooleanFilter extends Filter
{
  ArrayList<Filter> shouldFilters = null;
  ArrayList<Filter> notFilters = null;
  ArrayList<Filter> mustFilters = null;
  private DocIdSetIterator getDISI(ArrayList<Filter> filters, int index, IndexReader reader)
  throws IOException
  {
    return filters.get(index).getDocIdSet(reader).iterator();
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException
  {
    OpenBitSetDISI res = null;
    if (shouldFilters != null) {
      for (int i = 0; i < shouldFilters.size(); i++) {
        if (res == null) {
          res = new OpenBitSetDISI(getDISI(shouldFilters, i, reader), reader.maxDoc());
        } else { 
          DocIdSet dis = shouldFilters.get(i).getDocIdSet(reader);
          if(dis instanceof OpenBitSet) {
            res.or((OpenBitSet) dis);
          } else {
            res.inPlaceOr(getDISI(shouldFilters, i, reader));
          }
        }
      }
    }
    if (notFilters!=null) {
      for (int i = 0; i < notFilters.size(); i++) {
        if (res == null) {
          res = new OpenBitSetDISI(getDISI(notFilters, i, reader), reader.maxDoc());
          res.flip(0, reader.maxDoc()); 
        } else {
          DocIdSet dis = notFilters.get(i).getDocIdSet(reader);
          if(dis instanceof OpenBitSet) {
            res.andNot((OpenBitSet) dis);
          } else {
            res.inPlaceNot(getDISI(notFilters, i, reader));
          }
        }
      }
    }
    if (mustFilters!=null) {
      for (int i = 0; i < mustFilters.size(); i++) {
        if (res == null) {
          res = new OpenBitSetDISI(getDISI(mustFilters, i, reader), reader.maxDoc());
        } else {
          DocIdSet dis = mustFilters.get(i).getDocIdSet(reader);
          if(dis instanceof OpenBitSet) {
            res.and((OpenBitSet) dis);
          } else {
            res.inPlaceAnd(getDISI(mustFilters, i, reader));
          }
        }
      }
    }
    if (res !=null)
      return finalResult(res, reader.maxDoc());
    return DocIdSet.EMPTY_DOCIDSET;
  }
  @Deprecated
  protected final DocIdSet finalResult(OpenBitSetDISI result, int maxDocs) {
    return result;
  }
  public void add(FilterClause filterClause)
  {
    if (filterClause.getOccur().equals(Occur.MUST)) {
      if (mustFilters==null) {
        mustFilters=new ArrayList<Filter>();
      }
      mustFilters.add(filterClause.getFilter());
    }
    if (filterClause.getOccur().equals(Occur.SHOULD)) {
      if (shouldFilters==null) {
        shouldFilters=new ArrayList<Filter>();
      }
      shouldFilters.add(filterClause.getFilter());
    }
    if (filterClause.getOccur().equals(Occur.MUST_NOT)) {
      if (notFilters==null) {
        notFilters=new ArrayList<Filter>();
      }
      notFilters.add(filterClause.getFilter());
    }
  }
  private boolean equalFilters(ArrayList<Filter> filters1, ArrayList<Filter> filters2)
  {
     return (filters1 == filters2) ||
              ((filters1 != null) && filters1.equals(filters2));
  }
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;
    BooleanFilter other = (BooleanFilter)obj;
    return equalFilters(notFilters, other.notFilters)
        && equalFilters(mustFilters, other.mustFilters)
        && equalFilters(shouldFilters, other.shouldFilters);
  }
  @Override
  public int hashCode()
  {
    int hash=7;
    hash = 31 * hash + (null == mustFilters ? 0 : mustFilters.hashCode());
    hash = 31 * hash + (null == notFilters ? 0 : notFilters.hashCode());
    hash = 31 * hash + (null == shouldFilters ? 0 : shouldFilters.hashCode());
    return hash;
  }
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("BooleanFilter(");
    appendFilters(shouldFilters, "", buffer);
    appendFilters(mustFilters, "+", buffer);
    appendFilters(notFilters, "-", buffer);
    buffer.append(")");
    return buffer.toString();
  }
  private void appendFilters(ArrayList<Filter> filters, String occurString, StringBuilder buffer)
  {
    if (filters != null) {
      for (int i = 0; i < filters.size(); i++) {
        buffer.append(' ');
        buffer.append(occurString);
        buffer.append(filters.get(i).toString());
      }
    }
  }    
}
