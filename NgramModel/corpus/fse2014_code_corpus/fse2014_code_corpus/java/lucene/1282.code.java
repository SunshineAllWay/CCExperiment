package org.apache.lucene.spatial.tier;
public class InvalidGeoException extends Exception {
  private static final long serialVersionUID = 1L;
  public InvalidGeoException(String message){
    super(message);
  }
}
