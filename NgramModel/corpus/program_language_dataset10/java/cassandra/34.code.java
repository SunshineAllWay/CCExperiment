package org.apache.cassandra.thrift;
import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;
public enum Compression implements TEnum {
  GZIP(1);
  private final int value;
  private Compression(int value) {
    this.value = value;
  }
  public int getValue() {
    return value;
  }
  public static Compression findByValue(int value) { 
    switch (value) {
      case 1:
        return GZIP;
      default:
        return null;
    }
  }
}
