package org.apache.lucene.spatial.tier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Shape implements Serializable{
  private List<Double> area = new ArrayList<Double>();
  private String tierId;
  public Shape (String tierId){
    this.tierId = tierId;
  }
  public void addBox(double  boxId){
    area.add(boxId);
  }
  public List<Double> getArea(){
    return area;
  }
  public String getTierId(){
    return tierId;
  }
  public boolean isInside(double boxId){
    return area.contains(boxId);
  }
}
