package org.apache.solr.search.function.distance;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.MultiValueSource;
public class SquaredEuclideanFunction extends VectorDistanceFunction {
  protected String name = "sqedist";
  public SquaredEuclideanFunction(MultiValueSource source1, MultiValueSource source2) {
    super(-1, source1, source2);
  }
  protected String name() {
    return name;
  }
  protected double distance(int doc, DocValues dv1, DocValues dv2) {
    double[] vals1 = new double[source1.dimension()];
    double[] vals2 = new double[source1.dimension()];
    dv1.doubleVal(doc, vals1);
    dv2.doubleVal(doc, vals2);
    return DistanceUtils.squaredEuclideanDistance(vals1, vals2);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SquaredEuclideanFunction)) return false;
    if (!super.equals(o)) return false;
    SquaredEuclideanFunction that = (SquaredEuclideanFunction) o;
    if (!name.equals(that.name)) return false;
    return true;
  }
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
