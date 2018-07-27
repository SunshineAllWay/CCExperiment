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
public class Deletion implements TBase<Deletion, Deletion._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("Deletion");
  private static final TField TIMESTAMP_FIELD_DESC = new TField("timestamp", TType.I64, (short)1);
  private static final TField SUPER_COLUMN_FIELD_DESC = new TField("super_column", TType.STRING, (short)2);
  private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
  public long timestamp;
  public ByteBuffer super_column;
  public SlicePredicate predicate;
  public enum _Fields implements TFieldIdEnum {
    TIMESTAMP((short)1, "timestamp"),
    SUPER_COLUMN((short)2, "super_column"),
    PREDICATE((short)3, "predicate");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return TIMESTAMP;
        case 2: 
          return SUPER_COLUMN;
        case 3: 
          return PREDICATE;
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
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TIMESTAMP, new FieldMetaData("timestamp", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.I64)));
    tmpMap.put(_Fields.SUPER_COLUMN, new FieldMetaData("super_column", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, SlicePredicate.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(Deletion.class, metaDataMap);
  }
  public Deletion() {
  }
  public Deletion(
    long timestamp)
  {
    this();
    this.timestamp = timestamp;
    setTimestampIsSet(true);
  }
  public Deletion(Deletion other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.timestamp = other.timestamp;
    if (other.isSetSuper_column()) {
      this.super_column = TBaseHelper.copyBinary(other.super_column);
;
    }
    if (other.isSetPredicate()) {
      this.predicate = new SlicePredicate(other.predicate);
    }
  }
  public Deletion deepCopy() {
    return new Deletion(this);
  }
  @Override
  public void clear() {
    setTimestampIsSet(false);
    this.timestamp = 0;
    this.super_column = null;
    this.predicate = null;
  }
  public long getTimestamp() {
    return this.timestamp;
  }
  public Deletion setTimestamp(long timestamp) {
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
  public byte[] getSuper_column() {
    setSuper_column(TBaseHelper.rightSize(super_column));
    return super_column.array();
  }
  public ByteBuffer BufferForSuper_column() {
    return super_column;
  }
  public Deletion setSuper_column(byte[] super_column) {
    setSuper_column(ByteBuffer.wrap(super_column));
    return this;
  }
  public Deletion setSuper_column(ByteBuffer super_column) {
    this.super_column = super_column;
    return this;
  }
  public void unsetSuper_column() {
    this.super_column = null;
  }
  public boolean isSetSuper_column() {
    return this.super_column != null;
  }
  public void setSuper_columnIsSet(boolean value) {
    if (!value) {
      this.super_column = null;
    }
  }
  public SlicePredicate getPredicate() {
    return this.predicate;
  }
  public Deletion setPredicate(SlicePredicate predicate) {
    this.predicate = predicate;
    return this;
  }
  public void unsetPredicate() {
    this.predicate = null;
  }
  public boolean isSetPredicate() {
    return this.predicate != null;
  }
  public void setPredicateIsSet(boolean value) {
    if (!value) {
      this.predicate = null;
    }
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TIMESTAMP:
      if (value == null) {
        unsetTimestamp();
      } else {
        setTimestamp((Long)value);
      }
      break;
    case SUPER_COLUMN:
      if (value == null) {
        unsetSuper_column();
      } else {
        setSuper_column((ByteBuffer)value);
      }
      break;
    case PREDICATE:
      if (value == null) {
        unsetPredicate();
      } else {
        setPredicate((SlicePredicate)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TIMESTAMP:
      return new Long(getTimestamp());
    case SUPER_COLUMN:
      return getSuper_column();
    case PREDICATE:
      return getPredicate();
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case TIMESTAMP:
      return isSetTimestamp();
    case SUPER_COLUMN:
      return isSetSuper_column();
    case PREDICATE:
      return isSetPredicate();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Deletion)
      return this.equals((Deletion)that);
    return false;
  }
  public boolean equals(Deletion that) {
    if (that == null)
      return false;
    boolean this_present_timestamp = true;
    boolean that_present_timestamp = true;
    if (this_present_timestamp || that_present_timestamp) {
      if (!(this_present_timestamp && that_present_timestamp))
        return false;
      if (this.timestamp != that.timestamp)
        return false;
    }
    boolean this_present_super_column = true && this.isSetSuper_column();
    boolean that_present_super_column = true && that.isSetSuper_column();
    if (this_present_super_column || that_present_super_column) {
      if (!(this_present_super_column && that_present_super_column))
        return false;
      if (!this.super_column.equals(that.super_column))
        return false;
    }
    boolean this_present_predicate = true && this.isSetPredicate();
    boolean that_present_predicate = true && that.isSetPredicate();
    if (this_present_predicate || that_present_predicate) {
      if (!(this_present_predicate && that_present_predicate))
        return false;
      if (!this.predicate.equals(that.predicate))
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_timestamp = true;
    builder.append(present_timestamp);
    if (present_timestamp)
      builder.append(timestamp);
    boolean present_super_column = true && (isSetSuper_column());
    builder.append(present_super_column);
    if (present_super_column)
      builder.append(super_column);
    boolean present_predicate = true && (isSetPredicate());
    builder.append(present_predicate);
    if (present_predicate)
      builder.append(predicate);
    return builder.toHashCode();
  }
  public int compareTo(Deletion other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    Deletion typedOther = (Deletion)other;
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
    lastComparison = Boolean.valueOf(isSetSuper_column()).compareTo(typedOther.isSetSuper_column());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSuper_column()) {
      lastComparison = TBaseHelper.compareTo(this.super_column, typedOther.super_column);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPredicate()) {
      lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
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
          if (field.type == TType.I64) {
            this.timestamp = iprot.readI64();
            setTimestampIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRING) {
            this.super_column = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: 
          if (field.type == TType.STRUCT) {
            this.predicate = new SlicePredicate();
            this.predicate.read(iprot);
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
    oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
    oprot.writeI64(this.timestamp);
    oprot.writeFieldEnd();
    if (this.super_column != null) {
      if (isSetSuper_column()) {
        oprot.writeFieldBegin(SUPER_COLUMN_FIELD_DESC);
        oprot.writeBinary(this.super_column);
        oprot.writeFieldEnd();
      }
    }
    if (this.predicate != null) {
      if (isSetPredicate()) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Deletion(");
    boolean first = true;
    sb.append("timestamp:");
    sb.append(this.timestamp);
    first = false;
    if (isSetSuper_column()) {
      if (!first) sb.append(", ");
      sb.append("super_column:");
      if (this.super_column == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.super_column, sb);
      }
      first = false;
    }
    if (isSetPredicate()) {
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
  }
}
