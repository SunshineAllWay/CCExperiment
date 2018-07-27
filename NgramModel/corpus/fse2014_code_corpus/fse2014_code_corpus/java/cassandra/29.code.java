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
public class Column implements TBase<Column, Column._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("Column");
  private static final TField NAME_FIELD_DESC = new TField("name", TType.STRING, (short)1);
  private static final TField VALUE_FIELD_DESC = new TField("value", TType.STRING, (short)2);
  private static final TField TIMESTAMP_FIELD_DESC = new TField("timestamp", TType.I64, (short)3);
  private static final TField TTL_FIELD_DESC = new TField("ttl", TType.I32, (short)4);
  public ByteBuffer name;
  public ByteBuffer value;
  public long timestamp;
  public int ttl;
  public enum _Fields implements TFieldIdEnum {
    NAME((short)1, "name"),
    VALUE((short)2, "value"),
    TIMESTAMP((short)3, "timestamp"),
    TTL((short)4, "ttl");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return NAME;
        case 2: 
          return VALUE;
        case 3: 
          return TIMESTAMP;
        case 4: 
          return TTL;
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
  private static final int __TIMESTAMP_ISSET_ID = 0;
  private static final int __TTL_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NAME, new FieldMetaData("name", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.VALUE, new FieldMetaData("value", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.TIMESTAMP, new FieldMetaData("timestamp", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.I64)));
    tmpMap.put(_Fields.TTL, new FieldMetaData("ttl", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(Column.class, metaDataMap);
  }
  public Column() {
  }
  public Column(
    ByteBuffer name,
    ByteBuffer value,
    long timestamp)
  {
    this();
    this.name = name;
    this.value = value;
    this.timestamp = timestamp;
    setTimestampIsSet(true);
  }
  public Column(Column other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetName()) {
      this.name = TBaseHelper.copyBinary(other.name);
;
    }
    if (other.isSetValue()) {
      this.value = TBaseHelper.copyBinary(other.value);
;
    }
    this.timestamp = other.timestamp;
    this.ttl = other.ttl;
  }
  public Column deepCopy() {
    return new Column(this);
  }
  @Override
  public void clear() {
    this.name = null;
    this.value = null;
    setTimestampIsSet(false);
    this.timestamp = 0;
    setTtlIsSet(false);
    this.ttl = 0;
  }
  public byte[] getName() {
    setName(TBaseHelper.rightSize(name));
    return name.array();
  }
  public ByteBuffer BufferForName() {
    return name;
  }
  public Column setName(byte[] name) {
    setName(ByteBuffer.wrap(name));
    return this;
  }
  public Column setName(ByteBuffer name) {
    this.name = name;
    return this;
  }
  public void unsetName() {
    this.name = null;
  }
  public boolean isSetName() {
    return this.name != null;
  }
  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }
  public byte[] getValue() {
    setValue(TBaseHelper.rightSize(value));
    return value.array();
  }
  public ByteBuffer BufferForValue() {
    return value;
  }
  public Column setValue(byte[] value) {
    setValue(ByteBuffer.wrap(value));
    return this;
  }
  public Column setValue(ByteBuffer value) {
    this.value = value;
    return this;
  }
  public void unsetValue() {
    this.value = null;
  }
  public boolean isSetValue() {
    return this.value != null;
  }
  public void setValueIsSet(boolean value) {
    if (!value) {
      this.value = null;
    }
  }
  public long getTimestamp() {
    return this.timestamp;
  }
  public Column setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    setTimestampIsSet(true);
    return this;
  }
  public void unsetTimestamp() {
    __isset_bit_vector.clear(__TIMESTAMP_ISSET_ID);
  }
  public boolean isSetTimestamp() {
    return __isset_bit_vector.get(__TIMESTAMP_ISSET_ID);
  }
  public void setTimestampIsSet(boolean value) {
    __isset_bit_vector.set(__TIMESTAMP_ISSET_ID, value);
  }
  public int getTtl() {
    return this.ttl;
  }
  public Column setTtl(int ttl) {
    this.ttl = ttl;
    setTtlIsSet(true);
    return this;
  }
  public void unsetTtl() {
    __isset_bit_vector.clear(__TTL_ISSET_ID);
  }
  public boolean isSetTtl() {
    return __isset_bit_vector.get(__TTL_ISSET_ID);
  }
  public void setTtlIsSet(boolean value) {
    __isset_bit_vector.set(__TTL_ISSET_ID, value);
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((ByteBuffer)value);
      }
      break;
    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((ByteBuffer)value);
      }
      break;
    case TIMESTAMP:
      if (value == null) {
        unsetTimestamp();
      } else {
        setTimestamp((Long)value);
      }
      break;
    case TTL:
      if (value == null) {
        unsetTtl();
      } else {
        setTtl((Integer)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();
    case VALUE:
      return getValue();
    case TIMESTAMP:
      return new Long(getTimestamp());
    case TTL:
      return new Integer(getTtl());
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case NAME:
      return isSetName();
    case VALUE:
      return isSetValue();
    case TIMESTAMP:
      return isSetTimestamp();
    case TTL:
      return isSetTtl();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Column)
      return this.equals((Column)that);
    return false;
  }
  public boolean equals(Column that) {
    if (that == null)
      return false;
    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }
    boolean this_present_value = true && this.isSetValue();
    boolean that_present_value = true && that.isSetValue();
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (!this.value.equals(that.value))
        return false;
    }
    boolean this_present_timestamp = true;
    boolean that_present_timestamp = true;
    if (this_present_timestamp || that_present_timestamp) {
      if (!(this_present_timestamp && that_present_timestamp))
        return false;
      if (this.timestamp != that.timestamp)
        return false;
    }
    boolean this_present_ttl = true && this.isSetTtl();
    boolean that_present_ttl = true && that.isSetTtl();
    if (this_present_ttl || that_present_ttl) {
      if (!(this_present_ttl && that_present_ttl))
        return false;
      if (this.ttl != that.ttl)
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_name = true && (isSetName());
    builder.append(present_name);
    if (present_name)
      builder.append(name);
    boolean present_value = true && (isSetValue());
    builder.append(present_value);
    if (present_value)
      builder.append(value);
    boolean present_timestamp = true;
    builder.append(present_timestamp);
    if (present_timestamp)
      builder.append(timestamp);
    boolean present_ttl = true && (isSetTtl());
    builder.append(present_ttl);
    if (present_ttl)
      builder.append(ttl);
    return builder.toHashCode();
  }
  public int compareTo(Column other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    Column typedOther = (Column)other;
    lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = TBaseHelper.compareTo(this.name, typedOther.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetValue()).compareTo(typedOther.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = TBaseHelper.compareTo(this.value, typedOther.value);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTimestamp()).compareTo(typedOther.isSetTimestamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimestamp()) {
      lastComparison = TBaseHelper.compareTo(this.timestamp, typedOther.timestamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTtl()).compareTo(typedOther.isSetTtl());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTtl()) {
      lastComparison = TBaseHelper.compareTo(this.ttl, typedOther.ttl);
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
            this.name = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRING) {
            this.value = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: 
          if (field.type == TType.I64) {
            this.timestamp = iprot.readI64();
            setTimestampIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: 
          if (field.type == TType.I32) {
            this.ttl = iprot.readI32();
            setTtlIsSet(true);
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
    if (!isSetTimestamp()) {
      throw new TProtocolException("Required field 'timestamp' was not found in serialized data! Struct: " + toString());
    }
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.name != null) {
      oprot.writeFieldBegin(NAME_FIELD_DESC);
      oprot.writeBinary(this.name);
      oprot.writeFieldEnd();
    }
    if (this.value != null) {
      oprot.writeFieldBegin(VALUE_FIELD_DESC);
      oprot.writeBinary(this.value);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
    oprot.writeI64(this.timestamp);
    oprot.writeFieldEnd();
    if (isSetTtl()) {
      oprot.writeFieldBegin(TTL_FIELD_DESC);
      oprot.writeI32(this.ttl);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Column(");
    boolean first = true;
    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.name, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("value:");
    if (this.value == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.value, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("timestamp:");
    sb.append(this.timestamp);
    first = false;
    if (isSetTtl()) {
      if (!first) sb.append(", ");
      sb.append("ttl:");
      sb.append(this.ttl);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (name == null) {
      throw new TProtocolException("Required field 'name' was not present! Struct: " + toString());
    }
    if (value == null) {
      throw new TProtocolException("Required field 'value' was not present! Struct: " + toString());
    }
  }
}
