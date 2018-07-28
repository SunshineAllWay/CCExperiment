package org.apache.cassandra.thrift;
import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;
public enum ConsistencyLevel implements TEnum {
  ONE(1),
  QUORUM(2),
  LOCAL_QUORUM(3),
  EACH_QUORUM(4),
  ALL(5),
  ANY(6);
  private final int value;
  private ConsistencyLevel(int value) {
    this.value = value;
  }
  public int getValue() {
    return value;
  }
  public static ConsistencyLevel findByValue(int value) { 
    switch (value) {
      case 1:
        return ONE;
      case 2:
        return QUORUM;
      case 3:
        return LOCAL_QUORUM;
      case 4:
        return EACH_QUORUM;
      case 5:
        return ALL;
      case 6:
        return ANY;
      default:
        return null;
    }
  }
}
