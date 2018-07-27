package org.apache.cassandra.thrift;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;
public class KeyCount implements TBase<KeyCount, KeyCount._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("KeyCount");
  private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
  private static final TField COUNT_FIELD_DESC = new TField("count", TType.I32, (short)2);
  public ByteBuffer key;
  public int count;
  public enum _Fields implements TFieldIdEnum {
    KEY((short)1, "key"),
    COUNT((short)2, "count");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return KEY;
        case 2: 
          return COUNT;
        default:
          return null;
      }
    }
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }
    public static _Fields findByName(String name) {
      return byName.get(name);
    }
    private final short _thriftId;
    private final String _fieldName;
    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }
    public short getThriftFieldId() {
      return _thriftId;
    }
    public String getFieldName() {
      return _fieldName;
    }
  }
  private static final int __COUNT_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.COUNT, new FieldMetaData("count", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(KeyCount.class, metaDataMap);
  }
  public KeyCount() {
  }
  public KeyCount(
    ByteBuffer key,
    int count)
  {
    this();
    this.key = key;
    this.count = count;
    setCountIsSet(true);
  }
  public KeyCount(KeyCount other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetKey()) {
      this.key = TBaseHelper.copyBinary(other.key);
;
    }
    this.count = other.count;
  }
  public KeyCount deepCopy() {
    return new KeyCount(this);
  }
  @Override
  public void clear() {
    this.key = null;
    setCountIsSet(false);
    this.count = 0;
  }
  public byte[] getKey() {
    setKey(TBaseHelper.rightSize(key));
    return key.array();
  }
  public ByteBuffer BufferForKey() {
    return key;
  }
  public KeyCount setKey(byte[] key) {
    setKey(ByteBuffer.wrap(key));
    return this;
  }
  public KeyCount setKey(ByteBuffer key) {
    this.key = key;
    return this;
  }
  public void unsetKey() {
    this.key = null;
  }
  public boolean isSetKey() {
    return this.key != null;
  }
  public void setKeyIsSet(boolean value) {
    if (!value) {
      this.key = null;
    }
  }
  public int getCount() {
    return this.count;
  }
  public KeyCount setCount(int count) {
    this.count = count;
    setCountIsSet(true);
    return this;
  }
  public void unsetCount() {
    __isset_bit_vector.clear(__COUNT_ISSET_ID);
  }
  public boolean isSetCount() {
    return __isset_bit_vector.get(__COUNT_ISSET_ID);
  }
  public void setCountIsSet(boolean value) {
    __isset_bit_vector.set(__COUNT_ISSET_ID, value);
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case KEY:
      if (value == null) {
        unsetKey();
      } else {
        setKey((ByteBuffer)value);
      }
      break;
    case COUNT:
      if (value == null) {
        unsetCount();
      } else {
        setCount((Integer)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case KEY:
      return getKey();
    case COUNT:
      return new Integer(getCount());
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case KEY:
      return isSetKey();
    case COUNT:
      return isSetCount();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof KeyCount)
      return this.equals((KeyCount)that);
    return false;
  }
  public boolean equals(KeyCount that) {
    if (that == null)
      return false;
    boolean this_present_key = true && this.isSetKey();
    boolean that_present_key = true && that.isSetKey();
    if (this_present_key || that_present_key) {
      if (!(this_present_key && that_present_key))
        return false;
      if (!this.key.equals(that.key))
        return false;
    }
    boolean this_present_count = true;
    boolean that_present_count = true;
    if (this_present_count || that_present_count) {
      if (!(this_present_count && that_present_count))
        return false;
      if (this.count != that.count)
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_key = true && (isSetKey());
    builder.append(present_key);
    if (present_key)
      builder.append(key);
    boolean present_count = true;
    builder.append(present_count);
    if (present_count)
      builder.append(count);
    return builder.toHashCode();
  }
  public int compareTo(KeyCount other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    KeyCount typedOther = (KeyCount)other;
    lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetKey()) {
      lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCount()).compareTo(typedOther.isSetCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCount()) {
      lastComparison = TBaseHelper.compareTo(this.count, typedOther.count);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }
  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: 
          if (field.type == TType.STRING) {
            this.key = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.I32) {
            this.count = iprot.readI32();
            setCountIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    if (!isSetCount()) {
      throw new TProtocolException("Required field 'count' was not found in serialized data! Struct: " + toString());
    }
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.key != null) {
      oprot.writeFieldBegin(KEY_FIELD_DESC);
      oprot.writeBinary(this.key);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(COUNT_FIELD_DESC);
    oprot.writeI32(this.count);
    oprot.writeFieldEnd();
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("KeyCount(");
    boolean first = true;
    sb.append("key:");
    if (this.key == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.key, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("count:");
    sb.append(this.count);
    first = false;
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (key == null) {
      throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
    }
  }
}
