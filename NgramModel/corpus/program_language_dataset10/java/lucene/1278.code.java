package org.apache.lucene.spatial.tier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.tier.DistanceHandler.Precision;
public abstract class DistanceFilter extends Filter {
  final protected Filter startingFilter;
  protected Precision precise;
  protected Map<Integer,Double> distances;
  protected double distance;
  protected int nextDocBase; 
  protected transient WeakHashMap<String,Double> distanceLookupCache;
  public DistanceFilter(Filter startingFilter, double distance) {
    if (startingFilter == null) {
      throw new IllegalArgumentException("please provide a non-null startingFilter; you can use QueryWrapperFilter(MatchAllDocsQuery) as a no-op filter");
    }
    this.startingFilter = startingFilter;
    this.distance = distance;
    distances = new HashMap<Integer,Double>();
    distanceLookupCache = new WeakHashMap<String,Double>();
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    distanceLookupCache = new WeakHashMap<String,Double>();
  }
  public Map<Integer,Double> getDistances(){
    return distances;
  }
  public Double getDistance(int docid){
    return distances.get(docid);
  }
  public void setDistances(Map<Integer, Double> distances) {
    this.distances = distances;
  }
  public void reset() {
    nextDocBase = 0;
  }
  @Override
  public abstract boolean equals(Object o);
  @Override
  public abstract int hashCode();
}
