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
public class CqlRow implements TBase<CqlRow, CqlRow._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("CqlRow");
  private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
  private static final TField COLUMNS_FIELD_DESC = new TField("columns", TType.LIST, (short)2);
  public ByteBuffer key;
  public List<Column> columns;
  public enum _Fields implements TFieldIdEnum {
    KEY((short)1, "key"),
    COLUMNS((short)2, "columns");
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
          return COLUMNS;
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
    tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.COLUMNS, new FieldMetaData("columns", TFieldRequirementType.REQUIRED, 
        new ListMetaData(TType.LIST, 
            new StructMetaData(TType.STRUCT, Column.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(CqlRow.class, metaDataMap);
  }
  public CqlRow() {
  }
  public CqlRow(
    ByteBuffer key,
    List<Column> columns)
  {
    this();
    this.key = key;
    this.columns = columns;
  }
  public CqlRow(CqlRow other) {
    if (other.isSetKey()) {
      this.key = TBaseHelper.copyBinary(other.key);
;
    }
    if (other.isSetColumns()) {
      List<Column> __this__columns = new ArrayList<Column>();
      for (Column other_element : other.columns) {
        __this__columns.add(new Column(other_element));
      }
      this.columns = __this__columns;
    }
  }
  public CqlRow deepCopy() {
    return new CqlRow(this);
  }
  @Override
  public void clear() {
    this.key = null;
    this.columns = null;
  }
  public byte[] getKey() {
    setKey(TBaseHelper.rightSize(key));
    return key.array();
  }
  public ByteBuffer BufferForKey() {
    return key;
  }
  public CqlRow setKey(byte[] key) {
    setKey(ByteBuffer.wrap(key));
    return this;
  }
  public CqlRow setKey(ByteBuffer key) {
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
  public int getColumnsSize() {
    return (this.columns == null) ? 0 : this.columns.size();
  }
  public java.util.Iterator<Column> getColumnsIterator() {
    return (this.columns == null) ? null : this.columns.iterator();
  }
  public void addToColumns(Column elem) {
    if (this.columns == null) {
      this.columns = new ArrayList<Column>();
    }
    this.columns.add(elem);
  }
  public List<Column> getColumns() {
    return this.columns;
  }
  public CqlRow setColumns(List<Column> columns) {
    this.columns = columns;
    return this;
  }
  public void unsetColumns() {
    this.columns = null;
  }
  public boolean isSetColumns() {
    return this.columns != null;
  }
  public void setColumnsIsSet(boolean value) {
    if (!value) {
      this.columns = null;
    }
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
    case COLUMNS:
      if (value == null) {
        unsetColumns();
      } else {
        setColumns((List<Column>)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case KEY:
      return getKey();
    case COLUMNS:
      return getColumns();
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
    case COLUMNS:
      return isSetColumns();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CqlRow)
      return this.equals((CqlRow)that);
    return false;
  }
  public boolean equals(CqlRow that) {
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
    boolean this_present_columns = true && this.isSetColumns();
    boolean that_present_columns = true && that.isSetColumns();
    if (this_present_columns || that_present_columns) {
      if (!(this_present_columns && that_present_columns))
        return false;
      if (!this.columns.equals(that.columns))
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
    boolean present_columns = true && (isSetColumns());
    builder.append(present_columns);
    if (present_columns)
      builder.append(columns);
    return builder.toHashCode();
  }
  public int compareTo(CqlRow other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    CqlRow typedOther = (CqlRow)other;
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
    lastComparison = Boolean.valueOf(isSetColumns()).compareTo(typedOther.isSetColumns());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumns()) {
      lastComparison = TBaseHelper.compareTo(this.columns, typedOther.columns);
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
          if (field.type == TType.LIST) {
            {
              TList _list42 = iprot.readListBegin();
              this.columns = new ArrayList<Column>(_list42.size);
              for (int _i43 = 0; _i43 < _list42.size; ++_i43)
              {
                Column _elem44;
                _elem44 = new Column();
                _elem44.read(iprot);
                this.columns.add(_elem44);
              }
              iprot.readListEnd();
            }
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
    if (this.key != null) {
      oprot.writeFieldBegin(KEY_FIELD_DESC);
      oprot.writeBinary(this.key);
      oprot.writeFieldEnd();
    }
    if (this.columns != null) {
      oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.columns.size()));
        for (Column _iter45 : this.columns)
        {
          _iter45.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CqlRow(");
    boolean first = true;
    sb.append("key:");
    if (this.key == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.key, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("columns:");
    if (this.columns == null) {
      sb.append("null");
    } else {
      sb.append(this.columns);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (key == null) {
      throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
    }
    if (columns == null) {
      throw new TProtocolException("Required field 'columns' was not present! Struct: " + toString());
    }
  }
}
