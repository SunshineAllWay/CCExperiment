package org.apache.solr.schema;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.*;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.solr.analysis.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.*;
import java.io.IOException;
import java.util.Map;
import java.util.Date;
public class TrieField extends FieldType {
  public static final int DEFAULT_PRECISION_STEP = 8;
  protected int precisionStepArg = TrieField.DEFAULT_PRECISION_STEP;  
  protected int precisionStep;     
  protected TrieTypes type;
  static final DateField dateField = new DateField();
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    String p = args.remove("precisionStep");
    if (p != null) {
       precisionStepArg = Integer.parseInt(p);
    }
    precisionStep = precisionStepArg;
    if (precisionStep<=0 || precisionStep>=64) precisionStep=Integer.MAX_VALUE;
    String t = args.remove("type");
    if (t != null) {
      try {
        type = TrieTypes.valueOf(t.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                "Invalid type specified in schema.xml for field: " + args.get("name"), e);
      }
    }
    CharFilterFactory[] filterFactories = new CharFilterFactory[0];
    TokenFilterFactory[] tokenFilterFactories = new TokenFilterFactory[0];
    analyzer = new TokenizerChain(filterFactories, new TrieTokenizerFactory(type, precisionStep), tokenFilterFactories);
    queryAnalyzer = new TokenizerChain(filterFactories, new TrieTokenizerFactory(type, Integer.MAX_VALUE), tokenFilterFactories);
  }
  @Override
  public Object toObject(Fieldable f) {
    byte[] arr = f.getBinaryValue();
    if (arr==null) return badFieldString(f);
    switch (type) {
      case INTEGER:
        return toInt(arr);
      case FLOAT:
        return toFloat(arr);
      case LONG:
        return toLong(arr);
      case DOUBLE:
        return toDouble(arr);
      case DATE:
        return new Date(toLong(arr));
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + f.name());
    }
  }
  public SortField getSortField(SchemaField field, boolean top) {
    switch (type) {
      case INTEGER:
        return new SortField(field.getName(), FieldCache.NUMERIC_UTILS_INT_PARSER, top);
      case FLOAT:
        return new SortField(field.getName(), FieldCache.NUMERIC_UTILS_FLOAT_PARSER, top);
      case DATE: 
      case LONG:
        return new SortField(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER, top);
      case DOUBLE:
        return new SortField(field.getName(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER, top);
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + field.name);
    }
  }
  public ValueSource getValueSource(SchemaField field) {
    switch (type) {
      case INTEGER:
        return new IntFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_INT_PARSER);
      case FLOAT:
        return new FloatFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_FLOAT_PARSER);
      case DATE:
        return new TrieDateFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER);        
      case LONG:
        return new LongFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER);
      case DOUBLE:
        return new DoubleFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER);
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + field.name);
    }
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    byte[] arr = f.getBinaryValue();
    if (arr==null) {
      xmlWriter.writeStr(name, badFieldString(f));
      return;
    }
    switch (type) {
      case INTEGER:
        xmlWriter.writeInt(name,toInt(arr));
        break;
      case FLOAT:
        xmlWriter.writeFloat(name,toFloat(arr));
        break;
      case LONG:
        xmlWriter.writeLong(name,toLong(arr));
        break;
      case DOUBLE:
        xmlWriter.writeDouble(name,toDouble(arr));
        break;
      case DATE:
        xmlWriter.writeDate(name,new Date(toLong(arr)));
        break;
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + f.name());
    }
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    byte[] arr = f.getBinaryValue();
    if (arr==null) {
      writer.writeStr(name, badFieldString(f),true);
      return;
    }
    switch (type) {
      case INTEGER:
        writer.writeInt(name,toInt(arr));
        break;
      case FLOAT:
        writer.writeFloat(name,toFloat(arr));
        break;
      case LONG:
        writer.writeLong(name,toLong(arr));
        break;
      case DOUBLE:
        writer.writeDouble(name,toDouble(arr));
        break;
      case DATE:
        writer.writeDate(name,new Date(toLong(arr)));
        break;
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + f.name());
    }
  }
  @Override
  public boolean isTokenized() {
    return true;
  }
  @Override
  public boolean multiValuedFieldCache() {
    return false;
  }
  public int getPrecisionStep() {
    return precisionStepArg;
  }
  public TrieTypes getType() {
    return type;
  }
  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String min, String max, boolean minInclusive, boolean maxInclusive) {
    int ps = precisionStep;
    Query query = null;
    switch (type) {
      case INTEGER:
        query = NumericRangeQuery.newIntRange(field.getName(), ps,
                min == null ? null : Integer.parseInt(min),
                max == null ? null : Integer.parseInt(max),
                minInclusive, maxInclusive);
        break;
      case FLOAT:
        query = NumericRangeQuery.newFloatRange(field.getName(), ps,
                min == null ? null : Float.parseFloat(min),
                max == null ? null : Float.parseFloat(max),
                minInclusive, maxInclusive);
        break;
      case LONG:
        query = NumericRangeQuery.newLongRange(field.getName(), ps,
                min == null ? null : Long.parseLong(min),
                max == null ? null : Long.parseLong(max),
                minInclusive, maxInclusive);
        break;
      case DOUBLE:
        query = NumericRangeQuery.newDoubleRange(field.getName(), ps,
                min == null ? null : Double.parseDouble(min),
                max == null ? null : Double.parseDouble(max),
                minInclusive, maxInclusive);
        break;
      case DATE:
        query = NumericRangeQuery.newLongRange(field.getName(), ps,
                min == null ? null : dateField.parseMath(null, min).getTime(),
                max == null ? null : dateField.parseMath(null, max).getTime(),
                minInclusive, maxInclusive);
        break;
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field");
    }
    return query;
  }
  static int toInt(byte[] arr) {
    return (arr[0]<<24) | ((arr[1]&0xff)<<16) | ((arr[2]&0xff)<<8) | (arr[3]&0xff);
  }
  static long toLong(byte[] arr) {
    int high = (arr[0]<<24) | ((arr[1]&0xff)<<16) | ((arr[2]&0xff)<<8) | (arr[3]&0xff);
    int low = (arr[4]<<24) | ((arr[5]&0xff)<<16) | ((arr[6]&0xff)<<8) | (arr[7]&0xff);
    return (((long)high)<<32) | (low&0x0ffffffffL);
  }
  static float toFloat(byte[] arr) {
    return Float.intBitsToFloat(toInt(arr));
  }
  static double toDouble(byte[] arr) {
    return Double.longBitsToDouble(toLong(arr));
  }
  static byte[] toArr(int val) {
    byte[] arr = new byte[4];
    arr[0] = (byte)(val>>>24);
    arr[1] = (byte)(val>>>16);
    arr[2] = (byte)(val>>>8);
    arr[3] = (byte)(val);
    return arr;
  }
  static byte[] toArr(long val) {
    byte[] arr = new byte[8];
    arr[0] = (byte)(val>>>56);
    arr[1] = (byte)(val>>>48);
    arr[2] = (byte)(val>>>40);
    arr[3] = (byte)(val>>>32);
    arr[4] = (byte)(val>>>24);
    arr[5] = (byte)(val>>>16);
    arr[6] = (byte)(val>>>8);
    arr[7] = (byte)(val);
    return arr;
  }
  static byte[] toArr(float val) {
    return toArr(Float.floatToRawIntBits(val));
  }
  static byte[] toArr(double val) {
    return toArr(Double.doubleToRawLongBits(val));
  }
  @Override
  public String storedToReadable(Fieldable f) {
    return toExternal(f);
  }
  @Override
  public String readableToIndexed(String val) {
    switch (type) {
      case INTEGER:
        return NumericUtils.intToPrefixCoded(Integer.parseInt(val));
      case FLOAT:
        return NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(Float.parseFloat(val)));
      case LONG:
        return NumericUtils.longToPrefixCoded(Long.parseLong(val));
      case DOUBLE:
        return NumericUtils.longToPrefixCoded(NumericUtils.doubleToSortableLong(Double.parseDouble(val)));
      case DATE:
        return NumericUtils.longToPrefixCoded(dateField.parseMath(null, val).getTime());
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + type);
    }
  }
  @Override
  public String toInternal(String val) {
    return readableToIndexed(val);
  }
  static String badFieldString(Fieldable f) {
    String s = f.stringValue();
    return "ERROR:SCHEMA-INDEX-MISMATCH,stringValue="+s;
  }
  @Override
  public String toExternal(Fieldable f) {
    byte[] arr = f.getBinaryValue();
    if (arr==null) return badFieldString(f);
    switch (type) {
      case INTEGER:
        return Integer.toString(toInt(arr));
      case FLOAT:
        return Float.toString(toFloat(arr));
      case LONG:
        return Long.toString(toLong(arr));
      case DOUBLE:
        return Double.toString(toDouble(arr));
      case DATE:
        return dateField.formatDate(new Date(toLong(arr)));
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + f.name());
    }
  }
  @Override
  public String indexedToReadable(String indexedForm) {
    switch (type) {
      case INTEGER:
        return Integer.toString( NumericUtils.prefixCodedToInt(indexedForm) );
      case FLOAT:
        return Float.toString( NumericUtils.sortableIntToFloat(NumericUtils.prefixCodedToInt(indexedForm)) );
      case LONG:
        return Long.toString( NumericUtils.prefixCodedToLong(indexedForm) );
      case DOUBLE:
        return Double.toString( NumericUtils.sortableLongToDouble(NumericUtils.prefixCodedToLong(indexedForm)) );
      case DATE:
        return dateField.formatDate( new Date(NumericUtils.prefixCodedToLong(indexedForm)) );
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + type);
    }
  }
  @Override
  public String storedToIndexed(Fieldable f) {
    return readableToIndexed(storedToReadable(f));
  }
  @Override
  public Field createField(SchemaField field, String externalVal, float boost) {
    boolean indexed = field.indexed();
    boolean stored = field.stored();
    if (!indexed && !stored) {
      if (log.isTraceEnabled())
        log.trace("Ignoring unindexed/unstored field: " + field);
      return null;
    }
    int ps = precisionStep;
    byte[] arr=null;
    TokenStream ts=null;
    switch (type) {
      case INTEGER:
        int i = Integer.parseInt(externalVal);
        if (stored) arr = toArr(i);
        if (indexed) ts = new NumericTokenStream(ps).setIntValue(i);
        break;
      case FLOAT:
        float f = Float.parseFloat(externalVal);
        if (stored) arr = toArr(f);
        if (indexed) ts = new NumericTokenStream(ps).setFloatValue(f);
        break;
      case LONG:
        long l = Long.parseLong(externalVal);
        if (stored) arr = toArr(l);
        if (indexed) ts = new NumericTokenStream(ps).setLongValue(l);
        break;
      case DOUBLE:
        double d = Double.parseDouble(externalVal);
        if (stored) arr = toArr(d);
        if (indexed) ts = new NumericTokenStream(ps).setDoubleValue(d);
        break;
      case DATE:
        long time = dateField.parseMath(null, externalVal).getTime();
        if (stored) arr = toArr(time);
        if (indexed) ts = new NumericTokenStream(ps).setLongValue(time);
        break;
      default:
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + type);
    }
    Field f;
    if (stored) {
      f = new Field(field.getName(), arr, Field.Store.YES);
      if (indexed) f.setTokenStream(ts);
    } else {
      f = new Field(field.getName(), ts);
    }
    f.setOmitNorms(field.omitNorms());
    f.setOmitTermFreqAndPositions(field.omitTf());
    f.setBoost(boost);
    return f;
  }
  public enum TrieTypes {
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    DATE
  }
  static final String INT_PREFIX = new String(new char[]{NumericUtils.SHIFT_START_INT});
  static final String LONG_PREFIX = new String(new char[]{NumericUtils.SHIFT_START_LONG});
  public static String getMainValuePrefix(FieldType ft) {
    if (ft instanceof TrieDateField) {
      int step = ((TrieDateField)ft).getPrecisionStep();
      if (step <= 0 || step >=64) return null;
      return LONG_PREFIX;
    } else if (ft instanceof TrieField) {
      TrieField trie = (TrieField)ft;
      if (trie.precisionStep  == Integer.MAX_VALUE) return null;
      switch (trie.type) {
        case INTEGER:
        case FLOAT:
          return INT_PREFIX;
        case LONG:
        case DOUBLE:
        case DATE:
          return LONG_PREFIX;
        default:
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown type for trie field: " + trie.type);
      }
    }
    return null;
  }
}
class TrieDateFieldSource extends LongFieldSource {
  public TrieDateFieldSource(String field, FieldCache.LongParser parser) {
    super(field, parser);
  }
  public TrieDateFieldSource(String field) {
    super(field);
  }
  public String description() {
    return "date(" + field + ')';
  }
  @Override
  public long externalToLong(String extVal) {
    return TrieField.dateField.parseMath(null, extVal).getTime();
  }
}
