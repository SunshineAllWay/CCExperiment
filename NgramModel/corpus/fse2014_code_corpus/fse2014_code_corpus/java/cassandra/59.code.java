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
public class SliceRange implements TBase<SliceRange, SliceRange._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("SliceRange");
  private static final TField START_FIELD_DESC = new TField("start", TType.STRING, (short)1);
  private static final TField FINISH_FIELD_DESC = new TField("finish", TType.STRING, (short)2);
  private static final TField REVERSED_FIELD_DESC = new TField("reversed", TType.BOOL, (short)3);
  private static final TField COUNT_FIELD_DESC = new TField("count", TType.I32, (short)4);
  public ByteBuffer start;
  public ByteBuffer finish;
  public boolean reversed;
  public int count;
  public enum _Fields implements TFieldIdEnum {
    START((short)1, "start"),
    FINISH((short)2, "finish"),
    REVERSED((short)3, "reversed"),
    COUNT((short)4, "count");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return START;
        case 2: 
          return FINISH;
        case 3: 
          return REVERSED;
        case 4: 
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
  private static final int __REVERSED_ISSET_ID = 0;
  private static final int __COUNT_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.START, new FieldMetaData("start", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.FINISH, new FieldMetaData("finish", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.REVERSED, new FieldMetaData("reversed", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.BOOL)));
    tmpMap.put(_Fields.COUNT, new FieldMetaData("count", TFieldRequirementType.REQUIRED, 
        new FieldValueMetaData(TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(SliceRange.class, metaDataMap);
  }
  public SliceRange() {
    this.reversed = false;
    this.count = 100;
  }
  public SliceRange(
    ByteBuffer start,
    ByteBuffer finish,
    boolean reversed,
    int count)
  {
    this();
    this.start = start;
    this.finish = finish;
    this.reversed = reversed;
    setReversedIsSet(true);
    this.count = count;
    setCountIsSet(true);
  }
  public SliceRange(SliceRange other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetStart()) {
      this.start = TBaseHelper.copyBinary(other.start);
;
    }
    if (other.isSetFinish()) {
      this.finish = TBaseHelper.copyBinary(other.finish);
;
    }
    this.reversed = other.reversed;
    this.count = other.count;
  }
  public SliceRange deepCopy() {
    return new SliceRange(this);
  }
  @Override
  public void clear() {
    this.start = null;
    this.finish = null;
    this.reversed = false;
    this.count = 100;
  }
  public byte[] getStart() {
    setStart(TBaseHelper.rightSize(start));
    return start.array();
  }
  public ByteBuffer BufferForStart() {
    return start;
  }
  public SliceRange setStart(byte[] start) {
    setStart(ByteBuffer.wrap(start));
    return this;
  }
  public SliceRange setStart(ByteBuffer start) {
    this.start = start;
    return this;
  }
  public void unsetStart() {
    this.start = null;
  }
  public boolean isSetStart() {
    return this.start != null;
  }
  public void setStartIsSet(boolean value) {
    if (!value) {
      this.start = null;
    }
  }
  public byte[] getFinish() {
    setFinish(TBaseHelper.rightSize(finish));
    return finish.array();
  }
  public ByteBuffer BufferForFinish() {
    return finish;
  }
  public SliceRange setFinish(byte[] finish) {
    setFinish(ByteBuffer.wrap(finish));
    return this;
  }
  public SliceRange setFinish(ByteBuffer finish) {
    this.finish = finish;
    return this;
  }
  public void unsetFinish() {
    this.finish = null;
  }
  public boolean isSetFinish() {
    return this.finish != null;
  }
  public void setFinishIsSet(boolean value) {
    if (!value) {
      this.finish = null;
    }
  }
  public boolean isReversed() {
    return this.reversed;
  }
  public SliceRange setReversed(boolean reversed) {
    this.reversed = reversed;
    setReversedIsSet(true);
    return this;
  }
  public void unsetReversed() {
    __isset_bit_vector.clear(__REVERSED_ISSET_ID);
  }
  public boolean isSetReversed() {
    return __isset_bit_vector.get(__REVERSED_ISSET_ID);
  }
  public void setReversedIsSet(boolean value) {
    __isset_bit_vector.set(__REVERSED_ISSET_ID, value);
  }
  public int getCount() {
    return this.count;
  }
  public SliceRange setCount(int count) {
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
    case START:
      if (value == null) {
        unsetStart();
      } else {
        setStart((ByteBuffer)value);
      }
      break;
    case FINISH:
      if (value == null) {
        unsetFinish();
      } else {
        setFinish((ByteBuffer)value);
      }
      break;
    case REVERSED:
      if (value == null) {
        unsetReversed();
      } else {
        setReversed((Boolean)value);
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
    case START:
      return getStart();
    case FINISH:
      return getFinish();
    case REVERSED:
      return new Boolean(isReversed());
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
    case START:
      return isSetStart();
    case FINISH:
      return isSetFinish();
    case REVERSED:
      return isSetReversed();
    case COUNT:
      return isSetCount();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SliceRange)
      return this.equals((SliceRange)that);
    return false;
  }
  public boolean equals(SliceRange that) {
    if (that == null)
      return false;
    boolean this_present_start = true && this.isSetStart();
    boolean that_present_start = true && that.isSetStart();
    if (this_present_start || that_present_start) {
      if (!(this_present_start && that_present_start))
        return false;
      if (!this.start.equals(that.start))
        return false;
    }
    boolean this_present_finish = true && this.isSetFinish();
    boolean that_present_finish = true && that.isSetFinish();
    if (this_present_finish || that_present_finish) {
      if (!(this_present_finish && that_present_finish))
        return false;
      if (!this.finish.equals(that.finish))
        return false;
    }
    boolean this_present_reversed = true;
    boolean that_present_reversed = true;
    if (this_present_reversed || that_present_reversed) {
      if (!(this_present_reversed && that_present_reversed))
        return false;
      if (this.reversed != that.reversed)
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
    boolean present_start = true && (isSetStart());
    builder.append(present_start);
    if (present_start)
      builder.append(start);
    boolean present_finish = true && (isSetFinish());
    builder.append(present_finish);
    if (present_finish)
      builder.append(finish);
    boolean present_reversed = true;
    builder.append(present_reversed);
    if (present_reversed)
      builder.append(reversed);
    boolean present_count = true;
    builder.append(present_count);
    if (present_count)
      builder.append(count);
    return builder.toHashCode();
  }
  public int compareTo(SliceRange other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    SliceRange typedOther = (SliceRange)other;
    lastComparison = Boolean.valueOf(isSetStart()).compareTo(typedOther.isSetStart());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStart()) {
      lastComparison = TBaseHelper.compareTo(this.start, typedOther.start);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFinish()).compareTo(typedOther.isSetFinish());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFinish()) {
      lastComparison = TBaseHelper.compareTo(this.finish, typedOther.finish);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetReversed()).compareTo(typedOther.isSetReversed());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetReversed()) {
      lastComparison = TBaseHelper.compareTo(this.reversed, typedOther.reversed);
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
            this.start = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRING) {
            this.finish = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: 
          if (field.type == TType.BOOL) {
            this.reversed = iprot.readBool();
            setReversedIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: 
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
    if (!isSetReversed()) {
      throw new TProtocolException("Required field 'reversed' was not found in serialized data! Struct: " + toString());
    }
    if (!isSetCount()) {
      throw new TProtocolException("Required field 'count' was not found in serialized data! Struct: " + toString());
    }
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.start != null) {
      oprot.writeFieldBegin(START_FIELD_DESC);
      oprot.writeBinary(this.start);
      oprot.writeFieldEnd();
    }
    if (this.finish != null) {
      oprot.writeFieldBegin(FINISH_FIELD_DESC);
      oprot.writeBinary(this.finish);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(REVERSED_FIELD_DESC);
    oprot.writeBool(this.reversed);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(COUNT_FIELD_DESC);
    oprot.writeI32(this.count);
    oprot.writeFieldEnd();
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SliceRange(");
    boolean first = true;
    sb.append("start:");
    if (this.start == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.start, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("finish:");
    if (this.finish == null) {
      sb.append("null");
    } else {
      TBaseHelper.toString(this.finish, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("reversed:");
    sb.append(this.reversed);
    first = false;
    if (!first) sb.append(", ");
    sb.append("count:");
    sb.append(this.count);
    first = false;
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
    if (start == null) {
      throw new TProtocolException("Required field 'start' was not present! Struct: " + toString());
    }
    if (finish == null) {
      throw new TProtocolException("Required field 'finish' was not present! Struct: " + toString());
    }
  }
}
