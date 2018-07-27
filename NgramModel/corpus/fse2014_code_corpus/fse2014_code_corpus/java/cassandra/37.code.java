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
public class Counter implements TBase<Counter, Counter._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("Counter");
  private static final TField COLUMN_FIELD_DESC = new TField("column", TType.STRUCT, (short)1);
  private static final TField SUPER_COLUMN_FIELD_DESC = new TField("super_column", TType.STRUCT, (short)2);
  public CounterColumn column;
  public CounterSuperColumn super_column;
  public enum _Fields implements TFieldIdEnum {
    COLUMN((short)1, "column"),
    SUPER_COLUMN((short)2, "super_column");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return COLUMN;
        case 2: 
          return SUPER_COLUMN;
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
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.COLUMN, new FieldMetaData("column", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, CounterColumn.class)));
    tmpMap.put(_Fields.SUPER_COLUMN, new FieldMetaData("super_column", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, CounterSuperColumn.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(Counter.class, metaDataMap);
  }
  public Counter() {
  }
  public Counter(Counter other) {
    if (other.isSetColumn()) {
      this.column = new CounterColumn(other.column);
    }
    if (other.isSetSuper_column()) {
      this.super_column = new CounterSuperColumn(other.super_column);
    }
  }
  public Counter deepCopy() {
    return new Counter(this);
  }
  @Override
  public void clear() {
    this.column = null;
    this.super_column = null;
  }
  public CounterColumn getColumn() {
    return this.column;
  }
  public Counter setColumn(CounterColumn column) {
    this.column = column;
    return this;
  }
  public void unsetColumn() {
    this.column = null;
  }
  public boolean isSetColumn() {
    return this.column != null;
  }
  public void setColumnIsSet(boolean value) {
    if (!value) {
      this.column = null;
    }
  }
  public CounterSuperColumn getSuper_column() {
    return this.super_column;
  }
  public Counter setSuper_column(CounterSuperColumn super_column) {
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
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case COLUMN:
      if (value == null) {
        unsetColumn();
      } else {
        setColumn((CounterColumn)value);
      }
      break;
    case SUPER_COLUMN:
      if (value == null) {
        unsetSuper_column();
      } else {
        setSuper_column((CounterSuperColumn)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case COLUMN:
      return getColumn();
    case SUPER_COLUMN:
      return getSuper_column();
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case COLUMN:
      return isSetColumn();
    case SUPER_COLUMN:
      return isSetSuper_column();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Counter)
      return this.equals((Counter)that);
    return false;
  }
  public boolean equals(Counter that) {
    if (that == null)
      return false;
    boolean this_present_column = true && this.isSetColumn();
    boolean that_present_column = true && that.isSetColumn();
    if (this_present_column || that_present_column) {
      if (!(this_present_column && that_present_column))
        return false;
      if (!this.column.equals(that.column))
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
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_column = true && (isSetColumn());
    builder.append(present_column);
    if (present_column)
      builder.append(column);
    boolean present_super_column = true && (isSetSuper_column());
    builder.append(present_super_column);
    if (present_super_column)
      builder.append(super_column);
    return builder.toHashCode();
  }
  public int compareTo(Counter other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    Counter typedOther = (Counter)other;
    lastComparison = Boolean.valueOf(isSetColumn()).compareTo(typedOther.isSetColumn());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumn()) {
      lastComparison = TBaseHelper.compareTo(this.column, typedOther.column);
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
          if (field.type == TType.STRUCT) {
            this.column = new CounterColumn();
            this.column.read(iprot);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRUCT) {
            this.super_column = new CounterSuperColumn();
            this.super_column.read(iprot);
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
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.column != null) {
      if (isSetColumn()) {
        oprot.writeFieldBegin(COLUMN_FIELD_DESC);
        this.column.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    if (this.super_column != null) {
      if (isSetSuper_column()) {
        oprot.writeFieldBegin(SUPER_COLUMN_FIELD_DESC);
        this.super_column.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Counter(");
    boolean first = true;
    if (isSetColumn()) {
      sb.append("column:");
      if (this.column == null) {
        sb.append("null");
      } else {
        sb.append(this.column);
      }
      first = false;
    }
    if (isSetSuper_column()) {
      if (!first) sb.append(", ");
      sb.append("super_column:");
      if (this.super_column == null) {
        sb.append("null");
      } else {
        sb.append(this.super_column);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
  }
}
