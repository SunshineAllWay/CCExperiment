package org.apache.solr.search.function.distance;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import org.apache.solr.common.SolrException;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.MultiValueSource;
import org.apache.solr.search.function.ValueSource;
import java.io.IOException;
import java.util.Map;
public class VectorDistanceFunction extends ValueSource {
  protected MultiValueSource source1, source2;
  protected float power;
  protected float oneOverPower;
  public VectorDistanceFunction(float power, MultiValueSource source1, MultiValueSource source2) {
    if ((source1.dimension() != source2.dimension())) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Illegal number of sources");
    }
    this.power = power;
    this.oneOverPower = 1 / power;
    this.source1 = source1;
    this.source2 = source2;
  }
  protected String name() {
    return "dist";
  }
  protected double distance(int doc, DocValues dv1, DocValues dv2) {
    double[] vals1 = new double[source1.dimension()];
    double[] vals2 = new double[source1.dimension()];
    dv1.doubleVal(doc, vals1);
    dv2.doubleVal(doc, vals2);
    return DistanceUtils.vectorDistance(vals1, vals2, power, oneOverPower);
  }
  @Override
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues vals1 = source1.getValues(context, reader);
    final DocValues vals2 = source2.getValues(context, reader);
    return new DocValues() {
      @Override
      public byte byteVal(int doc) {
        return (byte) doubleVal(doc);
      }
      @Override
      public short shortVal(int doc) {
        return (short) doubleVal(doc);
      }
      public float floatVal(int doc) {
        return (float) doubleVal(doc);
      }
      public int intVal(int doc) {
        return (int) doubleVal(doc);
      }
      public long longVal(int doc) {
        return (long) doubleVal(doc);
      }
      public double doubleVal(int doc) {
        return distance(doc, vals1, vals2);
      }
      public String strVal(int doc) {
        return Double.toString(doubleVal(doc));
      }
      @Override
      public String toString(int doc) {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append('(').append(power).append(',');
        boolean firstTime = true;
        sb.append(vals1.toString(doc)).append(',');
        sb.append(vals2.toString(doc));
        sb.append(')');
        return sb.toString();
      }
    };
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    source1.createWeight(context, searcher);
    source2.createWeight(context, searcher);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VectorDistanceFunction)) return false;
    VectorDistanceFunction that = (VectorDistanceFunction) o;
    if (Float.compare(that.power, power) != 0) return false;
    if (!source1.equals(that.source1)) return false;
    if (!source2.equals(that.source2)) return false;
    return true;
  }
  @Override
  public int hashCode() {
    int result = source1.hashCode();
    result = 31 * result + source2.hashCode();
    result = 31 * result + Float.floatToRawIntBits(power);
    return result;
  }
  @Override
  public String description() {
    StringBuilder sb = new StringBuilder();
    sb.append(name()).append('(').append(power).append(',');
    sb.append(source1).append(',');
    sb.append(source2);
    sb.append(')');
    return sb.toString();
  }
}
