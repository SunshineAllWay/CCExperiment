package org.apache.solr.search.function.distance;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.DocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.spatial.geohash.GeoHashUtils;
import java.util.Map;
import java.io.IOException;
public class GeohashFunction extends ValueSource {
  protected ValueSource lat, lon;
  public GeohashFunction(ValueSource lat, ValueSource lon) {
    this.lat = lat;
    this.lon = lon;
  }
  protected String name() {
    return "geohash";
  }
  @Override
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues latDV = lat.getValues(context, reader);
    final DocValues lonDV = lon.getValues(context, reader);
    return new DocValues() {
      @Override
      public String strVal(int doc) {
        return GeoHashUtils.encode(latDV.doubleVal(doc), lonDV.doubleVal(doc));
      }
      @Override
      public String toString(int doc) {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append('(');
        sb.append(latDV.toString(doc)).append(',').append(lonDV.toString(doc));
        sb.append(')');
        return sb.toString();
      }
    };
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GeohashFunction)) return false;
    GeohashFunction that = (GeohashFunction) o;
    if (!lat.equals(that.lat)) return false;
    if (!lon.equals(that.lon)) return false;
    return true;
  }
  @Override
  public int hashCode() {
    int result = lat.hashCode();
    result = 29 * result - lon.hashCode();
    return result;
  }
  @Override  
  public String description() {
    StringBuilder sb = new StringBuilder();
    sb.append(name()).append('(');
    sb.append(lat).append(',').append(lon);
    sb.append(')');
    return sb.toString();
  }
}
