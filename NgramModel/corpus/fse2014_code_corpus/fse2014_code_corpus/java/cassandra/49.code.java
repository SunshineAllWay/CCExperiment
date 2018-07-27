package org.apache.cassandra.thrift;
import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;
public enum IndexType implements TEnum {
  KEYS(0);
  private final int value;
  private IndexType(int value) {
    this.value = value;
  }
  public int getValue() {
    return value;
  }
  public static IndexType findByValue(int value) { 
    switch (value) {
      case 0:
        return KEYS;
      default:
        return null;
    }
  }
}
