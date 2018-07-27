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
public class SlicePredicate implements TBase<SlicePredicate, SlicePredicate._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("SlicePredicate");
  private static final TField COLUMN_NAMES_FIELD_DESC = new TField("column_names", TType.LIST, (short)1);
  private static final TField SLICE_RANGE_FIELD_DESC = new TField("slice_range", TType.STRUCT, (short)2);
  public List<ByteBuffer> column_names;
  public SliceRange slice_range;
  public enum _Fields implements TFieldIdEnum {
    COLUMN_NAMES((short)1, "column_names"),
    SLICE_RANGE((short)2, "slice_range");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return COLUMN_NAMES;
        case 2: 
          return SLICE_RANGE;
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
    tmpMap.put(_Fields.COLUMN_NAMES, new FieldMetaData("column_names", TFieldRequirementType.OPTIONAL, 
        new ListMetaData(TType.LIST, 
            new FieldValueMetaData(TType.STRING))));
    tmpMap.put(_Fields.SLICE_RANGE, new FieldMetaData("slice_range", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, SliceRange.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(SlicePredicate.class, metaDataMap);
  }
  public SlicePredicate() {
  }
  public SlicePredicate(SlicePredicate other) {
    if (other.isSetColumn_names()) {
      List<ByteBuffer> __this__column_names = new ArrayList<ByteBuffer>();
      for (ByteBuffer other_element : other.column_names) {
        ByteBuffer temp_binary_element = TBaseHelper.copyBinary(other_element);
;
        __this__column_names.add(temp_binary_element);
      }
      this.column_names = __this__column_names;
    }
    if (other.isSetSlice_range()) {
      this.slice_range = new SliceRange(other.slice_range);
    }
  }
  public SlicePredicate deepCopy() {
    return new SlicePredicate(this);
  }
  @Override
  public void clear() {
    this.column_names = null;
    this.slice_range = null;
  }
  public int getColumn_namesSize() {
    return (this.column_names == null) ? 0 : this.column_names.size();
  }
  public java.util.Iterator<ByteBuffer> getColumn_namesIterator() {
    return (this.column_names == null) ? null : this.column_names.iterator();
  }
  public void addToColumn_names(ByteBuffer elem) {
    if (this.column_names == null) {
      this.column_names = new ArrayList<ByteBuffer>();
    }
    this.column_names.add(elem);
  }
  public List<ByteBuffer> getColumn_names() {
    return this.column_names;
  }
  public SlicePredicate setColumn_names(List<ByteBuffer> column_names) {
    this.column_names = column_names;
    return this;
  }
  public void unsetColumn_names() {
    this.column_names = null;
  }
  public boolean isSetColumn_names() {
    return this.column_names != null;
  }
  public void setColumn_namesIsSet(boolean value) {
    if (!value) {
      this.column_names = null;
    }
  }
  public SliceRange getSlice_range() {
    return this.slice_range;
  }
  public SlicePredicate setSlice_range(SliceRange slice_range) {
    this.slice_range = slice_range;
    return this;
  }
  public void unsetSlice_range() {
    this.slice_range = null;
  }
  public boolean isSetSlice_range() {
    return this.slice_range != null;
  }
  public void setSlice_rangeIsSet(boolean value) {
    if (!value) {
      this.slice_range = null;
    }
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case COLUMN_NAMES:
      if (value == null) {
        unsetColumn_names();
      } else {
        setColumn_names((List<ByteBuffer>)value);
      }
      break;
    case SLICE_RANGE:
      if (value == null) {
        unsetSlice_range();
      } else {
        setSlice_range((SliceRange)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case COLUMN_NAMES:
      return getColumn_names();
    case SLICE_RANGE:
      return getSlice_range();
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case COLUMN_NAMES:
      return isSetColumn_names();
    case SLICE_RANGE:
      return isSetSlice_range();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SlicePredicate)
      return this.equals((SlicePredicate)that);
    return false;
  }
  public boolean equals(SlicePredicate that) {
    if (that == null)
      return false;
    boolean this_present_column_names = true && this.isSetColumn_names();
    boolean that_present_column_names = true && that.isSetColumn_names();
    if (this_present_column_names || that_present_column_names) {
      if (!(this_present_column_names && that_present_column_names))
        return false;
      if (!this.column_names.equals(that.column_names))
        return false;
    }
    boolean this_present_slice_range = true && this.isSetSlice_range();
    boolean that_present_slice_range = true && that.isSetSlice_range();
    if (this_present_slice_range || that_present_slice_range) {
      if (!(this_present_slice_range && that_present_slice_range))
        return false;
      if (!this.slice_range.equals(that.slice_range))
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_column_names = true && (isSetColumn_names());
    builder.append(present_column_names);
    if (present_column_names)
      builder.append(column_names);
    boolean present_slice_range = true && (isSetSlice_range());
    builder.append(present_slice_range);
    if (present_slice_range)
      builder.append(slice_range);
    return builder.toHashCode();
  }
  public int compareTo(SlicePredicate other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    SlicePredicate typedOther = (SlicePredicate)other;
    lastComparison = Boolean.valueOf(isSetColumn_names()).compareTo(typedOther.isSetColumn_names());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumn_names()) {
      lastComparison = TBaseHelper.compareTo(this.column_names, typedOther.column_names);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSlice_range()).compareTo(typedOther.isSetSlice_range());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSlice_range()) {
      lastComparison = TBaseHelper.compareTo(this.slice_range, typedOther.slice_range);
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
          if (field.type == TType.LIST) {
            {
              TList _list8 = iprot.readListBegin();
              this.column_names = new ArrayList<ByteBuffer>(_list8.size);
              for (int _i9 = 0; _i9 < _list8.size; ++_i9)
              {
                ByteBuffer _elem10;
                _elem10 = iprot.readBinary();
                this.column_names.add(_elem10);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRUCT) {
            this.slice_range = new SliceRange();
            this.slice_range.read(iprot);
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
    if (this.column_names != null) {
      if (isSetColumn_names()) {
        oprot.writeFieldBegin(COLUMN_NAMES_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.column_names.size()));
          for (ByteBuffer _iter11 : this.column_names)
          {
            oprot.writeBinary(_iter11);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (this.slice_range != null) {
      if (isSetSlice_range()) {
        oprot.writeFieldBegin(SLICE_RANGE_FIELD_DESC);
        this.slice_range.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SlicePredicate(");
    boolean first = true;
    if (isSetColumn_names()) {
      sb.append("column_names:");
      if (this.column_names == null) {
        sb.append("null");
      } else {
        sb.append(this.column_names);
      }
      first = false;
    }
    if (isSetSlice_range()) {
      if (!first) sb.append(", ");
      sb.append("slice_range:");
      if (this.slice_range == null) {
        sb.append("null");
      } else {
        sb.append(this.slice_range);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
  }
}
