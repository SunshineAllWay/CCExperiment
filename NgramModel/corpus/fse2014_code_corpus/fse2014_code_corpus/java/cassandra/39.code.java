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
public class CounterDeletion implements TBase<CounterDeletion, CounterDeletion._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("CounterDeletion");
  private static final TField SUPER_COLUMN_FIELD_DESC = new TField("super_column", TType.STRING, (short)1);
  private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)2);
  public ByteBuffer super_column;
  public SlicePredicate predicate;
  public enum _Fields implements TFieldIdEnum {
    SUPER_COLUMN((short)1, "super_column"),
    PREDICATE((short)2, "predicate");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return SUPER_COLUMN;
        case 2: 
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
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SUPER_COLUMN, new FieldMetaData("super_column", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, SlicePredicate.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(CounterDeletion.class, metaDataMap);
  }
  public CounterDeletion() {
  }
  public CounterDeletion(CounterDeletion other) {
    if (other.isSetSuper_column()) {
      this.super_column = TBaseHelper.copyBinary(other.super_column);
;
    }
    if (other.isSetPredicate()) {
      this.predicate = new SlicePredicate(other.predicate);
    }
  }
  public CounterDeletion deepCopy() {
    return new CounterDeletion(this);
  }
  @Override
  public void clear() {
    this.super_column = null;
    this.predicate = null;
  }
  public byte[] getSuper_column() {
    setSuper_column(TBaseHelper.rightSize(super_column));
    return super_column.array();
  }
  public ByteBuffer BufferForSuper_column() {
    return super_column;
  }
  public CounterDeletion setSuper_column(byte[] super_column) {
    setSuper_column(ByteBuffer.wrap(super_column));
    return this;
  }
  public CounterDeletion setSuper_column(ByteBuffer super_column) {
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
  public CounterDeletion setPredicate(SlicePredicate predicate) {
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
    if (that instanceof CounterDeletion)
      return this.equals((CounterDeletion)that);
    return false;
  }
  public boolean equals(CounterDeletion that) {
    if (that == null)
      return false;
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
  public int compareTo(CounterDeletion other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    CounterDeletion typedOther = (CounterDeletion)other;
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
          if (field.type == TType.STRING) {
            this.super_column = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
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
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
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
    StringBuilder sb = new StringBuilder("CounterDeletion(");
    boolean first = true;
    if (isSetSuper_column()) {
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
