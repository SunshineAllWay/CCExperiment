package org.apache.solr.schema;
import org.apache.solr.common.SolrException;
import org.apache.solr.analysis.CharFilterFactory;
import org.apache.solr.analysis.TokenFilterFactory;
import org.apache.solr.analysis.TokenizerChain;
import org.apache.solr.analysis.TrieTokenizerFactory;
import org.apache.solr.search.function.*;
import org.apache.solr.search.QParser;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.NumericTokenStream;
import java.util.Map;
import java.util.Date;
import java.io.IOException;
public class TrieDateField extends DateField {
  protected int precisionStepArg = TrieField.DEFAULT_PRECISION_STEP;  
  protected int precisionStep = precisionStepArg;     
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    String p = args.remove("precisionStep");
    if (p != null) {
       precisionStepArg = Integer.parseInt(p);
    }
    precisionStep = precisionStepArg;
    if (precisionStep<=0 || precisionStep>=64) precisionStep=Integer.MAX_VALUE;
    CharFilterFactory[] filterFactories = new CharFilterFactory[0];
    TokenFilterFactory[] tokenFilterFactories = new TokenFilterFactory[0];
    analyzer = new TokenizerChain(filterFactories, new TrieTokenizerFactory(TrieField.TrieTypes.DATE, precisionStep), tokenFilterFactories);
    queryAnalyzer = new TokenizerChain(filterFactories, new TrieTokenizerFactory(TrieField.TrieTypes.DATE, Integer.MAX_VALUE), tokenFilterFactories);
  }
  @Override
  public Date toObject(Fieldable f) {
    byte[] arr = f.getBinaryValue();
    if (arr==null) throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,TrieField.badFieldString(f));
    return new Date(TrieField.toLong(arr));
  }
  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    return new SortField(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER, top);
  }
  @Override
  public ValueSource getValueSource(SchemaField field) {
    return new TrieDateFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER);
  }
  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    return new TrieDateFieldSource(field.getName(), FieldCache.NUMERIC_UTILS_LONG_PARSER);
  }
  @Override
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    byte[] arr = f.getBinaryValue();
    if (arr==null) {
      xmlWriter.writeStr(name, TrieField.badFieldString(f));
      return;
    }
    xmlWriter.writeDate(name,new Date(TrieField.toLong(arr)));
  }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    byte[] arr = f.getBinaryValue();
    if (arr==null) {
      writer.writeStr(name, TrieField.badFieldString(f),true);
      return;
    }
    writer.writeDate(name,new Date(TrieField.toLong(arr)));
  }
  @Override
  public boolean isTokenized() {
    return true;
  }
  public int getPrecisionStep() {
    return precisionStepArg;
  }
  @Override
  public String storedToReadable(Fieldable f) {
    return toExternal(f);
  }
  @Override
  public String readableToIndexed(String val) {  
    return NumericUtils.longToPrefixCoded(super.parseMath(null, val).getTime());
  }
  @Override
  public String toInternal(String val) {
    return readableToIndexed(val);
  }
  @Override
  public String toExternal(Fieldable f) {
    byte[] arr = f.getBinaryValue();
    if (arr==null) return TrieField.badFieldString(f);
     return super.toExternal(new Date(TrieField.toLong(arr)));
  }
  @Override
  public String indexedToReadable(String indexedForm) {
    return super.toExternal( new Date(NumericUtils.prefixCodedToLong(indexedForm)) );
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
    long time = super.parseMath(null, externalVal).getTime();
    if (stored) arr = TrieField.toArr(time);
    if (indexed) ts = new NumericTokenStream(ps).setLongValue(time);
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
  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String min, String max, boolean minInclusive, boolean maxInclusive) {
    return getRangeQuery(parser, field,
            min==null ? null : super.parseMath(null,min),
            max==null ? null : super.parseMath(null,max),
            minInclusive, maxInclusive);
  }
  @Override
  public Query getRangeQuery(QParser parser, SchemaField sf, Date min, Date max, boolean minInclusive, boolean maxInclusive) {
    int ps = precisionStep;
    Query query = NumericRangeQuery.newLongRange(sf.getName(), ps,
              min == null ? null : min.getTime(),
              max == null ? null : max.getTime(),
              minInclusive, maxInclusive);
    return query;
  }
}
