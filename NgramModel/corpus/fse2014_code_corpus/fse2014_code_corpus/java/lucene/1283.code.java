package org.apache.lucene.spatial.tier;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FilteredDocIdSet;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSet;
public class LatLongDistanceFilter extends DistanceFilter {
  private static final long serialVersionUID = 1L;
  double lat;
  double lng;
  String latField;
  String lngField;
  int nextOffset = 0;
  public LatLongDistanceFilter(Filter startingFilter, double lat, double lng, double miles, String latField, String lngField) {
    super(startingFilter, miles);
    this.lat = lat;
    this.lng = lng;
    this.latField = latField;
    this.lngField = lngField;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    final double[] latIndex = FieldCache.DEFAULT.getDoubles(reader, latField);
    final double[] lngIndex = FieldCache.DEFAULT.getDoubles(reader, lngField);
    final int docBase = nextDocBase;
    nextDocBase += reader.maxDoc();
    return new FilteredDocIdSet(startingFilter.getDocIdSet(reader)) {
      @Override
      protected boolean match(int doc) {
        double x = latIndex[doc];
        double y = lngIndex[doc];
        String ck = Double.toString(x)+","+Double.toString(y);
        Double cachedDistance = distanceLookupCache.get(ck);
        double d;
        if (cachedDistance != null){
          d = cachedDistance.doubleValue();
        } else {
          d = DistanceUtils.getInstance().getDistanceMi(lat, lng, x, y);
          distanceLookupCache.put(ck, d);
        }
        if (d < distance) {
          distances.put(doc+docBase, d);
          return true;
        } else {
          return false;
        }
      }
    };
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LatLongDistanceFilter)) return false;
    LatLongDistanceFilter other = (LatLongDistanceFilter) o;
    if (!this.startingFilter.equals(other.startingFilter) ||
        this.distance != other.distance ||
        this.lat != other.lat ||
        this.lng != other.lng ||
        !this.latField.equals(other.latField) ||
        !this.lngField.equals(other.lngField)) {
      return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    int h = Double.valueOf(distance).hashCode();
    h ^= startingFilter.hashCode();
    h ^= Double.valueOf(lat).hashCode();
    h ^= Double.valueOf(lng).hashCode();
    h ^= latField.hashCode();
    h ^= lngField.hashCode();
    return h;
  }
}
