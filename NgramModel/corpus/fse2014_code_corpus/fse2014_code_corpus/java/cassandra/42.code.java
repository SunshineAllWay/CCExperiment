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
public class CqlResult implements TBase<CqlResult, CqlResult._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("CqlResult");
  private static final TField TYPE_FIELD_DESC = new TField("type", TType.I32, (short)1);
  private static final TField ROWS_FIELD_DESC = new TField("rows", TType.LIST, (short)2);
  private static final TField NUM_FIELD_DESC = new TField("num", TType.I32, (short)3);
  public CqlResultType type;
  public List<CqlRow> rows;
  public int num;
  public enum _Fields implements TFieldIdEnum {
    TYPE((short)1, "type"),
    ROWS((short)2, "rows"),
    NUM((short)3, "num");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return TYPE;
        case 2: 
          return ROWS;
        case 3: 
          return NUM;
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
  private static final int __NUM_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TYPE, new FieldMetaData("type", TFieldRequirementType.REQUIRED, 
        new EnumMetaData(TType.ENUM, CqlResultType.class)));
    tmpMap.put(_Fields.ROWS, new FieldMetaData("rows", TFieldRequirementType.OPTIONAL, 
        new ListMetaData(TType.LIST, 
            new StructMetaData(TType.STRUCT, CqlRow.class))));
    tmpMap.put(_Fields.NUM, new FieldMetaData("num", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(CqlResult.class, metaDataMap);
  }
  public CqlResult() {
  }
  public CqlResult(
    CqlResultType type)
  {
    this();
    this.type = type;
  }
  public CqlResult(CqlResult other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetType()) {
      this.type = other.type;
    }
    if (other.isSetRows()) {
      List<CqlRow> __this__rows = new ArrayList<CqlRow>();
      for (CqlRow other_element : other.rows) {
        __this__rows.add(new CqlRow(other_element));
      }
      this.rows = __this__rows;
    }
    this.num = other.num;
  }
  public CqlResult deepCopy() {
    return new CqlResult(this);
  }
  @Override
  public void clear() {
    this.type = null;
    this.rows = null;
    setNumIsSet(false);
    this.num = 0;
  }
  public CqlResultType getType() {
    return this.type;
  }
  public CqlResult setType(CqlResultType type) {
    this.type = type;
    return this;
  }
  public void unsetType() {
    this.type = null;
  }
  public boolean isSetType() {
    return this.type != null;
  }
  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }
  public int getRowsSize() {
    return (this.rows == null) ? 0 : this.rows.size();
  }
  public java.util.Iterator<CqlRow> getRowsIterator() {
    return (this.rows == null) ? null : this.rows.iterator();
  }
  public void addToRows(CqlRow elem) {
    if (this.rows == null) {
      this.rows = new ArrayList<CqlRow>();
    }
    this.rows.add(elem);
  }
  public List<CqlRow> getRows() {
    return this.rows;
  }
  public CqlResult setRows(List<CqlRow> rows) {
    this.rows = rows;
    return this;
  }
  public void unsetRows() {
    this.rows = null;
  }
  public boolean isSetRows() {
    return this.rows != null;
  }
  public void setRowsIsSet(boolean value) {
    if (!value) {
      this.rows = null;
    }
  }
  public int getNum() {
    return this.num;
  }
  public CqlResult setNum(int num) {
    this.num = num;
    setNumIsSet(true);
    return this;
  }
  public void unsetNum() {
    __isset_bit_vector.clear(__NUM_ISSET_ID);
  }
  public boolean isSetNum() {
    return __isset_bit_vector.get(__NUM_ISSET_ID);
  }
  public void setNumIsSet(boolean value) {
    __isset_bit_vector.set(__NUM_ISSET_ID, value);
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((CqlResultType)value);
      }
      break;
    case ROWS:
      if (value == null) {
        unsetRows();
      } else {
        setRows((List<CqlRow>)value);
      }
      break;
    case NUM:
      if (value == null) {
        unsetNum();
      } else {
        setNum((Integer)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TYPE:
      return getType();
    case ROWS:
      return getRows();
    case NUM:
      return new Integer(getNum());
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case TYPE:
      return isSetType();
    case ROWS:
      return isSetRows();
    case NUM:
      return isSetNum();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CqlResult)
      return this.equals((CqlResult)that);
    return false;
  }
  public boolean equals(CqlResult that) {
    if (that == null)
      return false;
    boolean this_present_type = true && this.isSetType();
    boolean that_present_type = true && that.isSetType();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }
    boolean this_present_rows = true && this.isSetRows();
    boolean that_present_rows = true && that.isSetRows();
    if (this_present_rows || that_present_rows) {
      if (!(this_present_rows && that_present_rows))
        return false;
      if (!this.rows.equals(that.rows))
        return false;
    }
    boolean this_present_num = true && this.isSetNum();
    boolean that_present_num = true && that.isSetNum();
    if (this_present_num || that_present_num) {
      if (!(this_present_num && that_present_num))
        return false;
      if (this.num != that.num)
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_type = true && (isSetType());
    builder.append(present_type);
    if (present_type)
      builder.append(type.getValue());
    boolean present_rows = true && (isSetRows());
    builder.append(present_rows);
    if (present_rows)
      builder.append(rows);
    boolean present_num = true && (isSetNum());
    builder.append(present_num);
    if (present_num)
      builder.append(num);
    return builder.toHashCode();
  }
  public int compareTo(CqlResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    CqlResult typedOther = (CqlResult)other;
    lastComparison = Boolean.valueOf(isSetType()).compareTo(typedOther.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = TBaseHelper.compareTo(this.type, typedOther.type);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRows()).compareTo(typedOther.isSetRows());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRows()) {
      lastComparison = TBaseHelper.compareTo(this.rows, typedOther.rows);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNum()).compareTo(typedOther.isSetNum());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNum()) {
      lastComparison = TBaseHelper.compareTo(this.num, typedOther.num);
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
          if (field.type == TType.I32) {
            this.type = CqlResultType.findByValue(iprot.readI32());
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.LIST) {
            {
              TList _list46 = iprot.readListBegin();
              this.rows = new ArrayList<CqlRow>(_list46.size);
              for (int _i47 = 0; _i47 < _list46.size; ++_i47)
              {
                CqlRow _elem48;
                _elem48 = new CqlRow();
                _elem48.read(iprot);
                this.rows.add(_elem48);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: 
          if (field.type == TType.I32) {
            this.num = iprot.readI32();
            setNumIsSet(true);
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
    if (this.type != null) {
      oprot.writeFieldBegin(TYPE_FIELD_DESC);
      oprot.writeI32(this.type.getValue());
      oprot.writeFieldEnd();
    }
    if (this.rows != null) {
      if (isSetRows()) {
        oprot.writeFieldBegin(ROWS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.rows.size()));
          for (CqlRow _iter49 : this.rows)
          {
            _iter49.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (isSetNum()) {
      oprot.writeFieldBegin(NUM_FIELD_DESC);
      oprot.writeI32(this.num);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CqlResult(");
    boolean first = true;
    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    if (isSetRows()) {
      if (!first) sb.append(", ");
      sb.append("rows:");
      if (this.rows == null) {
        sb.append("null");
      } else {
        sb.append(this.rows);
      }
      first = false;
    }
    if (isSetNum()) {
      if (!first) sb.append(", ");
      sb.append("num:");
      sb.append(this.num);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (type == null) {
      throw new TProtocolException("Required field 'type' was not present! Struct: " + toString());
    }
  }
}
