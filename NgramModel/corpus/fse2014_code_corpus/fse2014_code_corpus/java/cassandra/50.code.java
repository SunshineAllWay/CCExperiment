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
public class InvalidRequestException extends Exception implements TBase<InvalidRequestException, InvalidRequestException._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("InvalidRequestException");
  private static final TField WHY_FIELD_DESC = new TField("why", TType.STRING, (short)1);
  public String why;
  public enum _Fields implements TFieldIdEnum {
    WHY((short)1, "why");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return WHY;
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
    tmpMap.put(_Fields.WHY, new FieldMetaData("why", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(InvalidRequestException.class, metaDataMap);
  }
  public InvalidRequestException() {
  }
  public InvalidRequestException(
    String why)
  {
    this();
    this.why = why;
  }
  public InvalidRequestException(InvalidRequestException other) {
    if (other.isSetWhy()) {
      this.why = other.why;
    }
  }
  public InvalidRequestException deepCopy() {
    return new InvalidRequestException(this);
  }
  @Override
  public void clear() {
    this.why = null;
  }
  public String getWhy() {
    return this.why;
  }
  public InvalidRequestException setWhy(String why) {
    this.why = why;
    return this;
  }
  public void unsetWhy() {
    this.why = null;
  }
  public boolean isSetWhy() {
    return this.why != null;
  }
  public void setWhyIsSet(boolean value) {
    if (!value) {
      this.why = null;
    }
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case WHY:
      if (value == null) {
        unsetWhy();
      } else {
        setWhy((String)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case WHY:
      return getWhy();
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case WHY:
      return isSetWhy();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof InvalidRequestException)
      return this.equals((InvalidRequestException)that);
    return false;
  }
  public boolean equals(InvalidRequestException that) {
    if (that == null)
      return false;
    boolean this_present_why = true && this.isSetWhy();
    boolean that_present_why = true && that.isSetWhy();
    if (this_present_why || that_present_why) {
      if (!(this_present_why && that_present_why))
        return false;
      if (!this.why.equals(that.why))
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_why = true && (isSetWhy());
    builder.append(present_why);
    if (present_why)
      builder.append(why);
    return builder.toHashCode();
  }
  public int compareTo(InvalidRequestException other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    InvalidRequestException typedOther = (InvalidRequestException)other;
    lastComparison = Boolean.valueOf(isSetWhy()).compareTo(typedOther.isSetWhy());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWhy()) {
      lastComparison = TBaseHelper.compareTo(this.why, typedOther.why);
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
            this.why = iprot.readString();
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
    if (this.why != null) {
      oprot.writeFieldBegin(WHY_FIELD_DESC);
      oprot.writeString(this.why);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("InvalidRequestException(");
    boolean first = true;
    sb.append("why:");
    if (this.why == null) {
      sb.append("null");
    } else {
      sb.append(this.why);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (why == null) {
      throw new TProtocolException("Required field 'why' was not present! Struct: " + toString());
    }
  }
}
