package org.apache.solr.schema;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.OrdFieldSource;
import org.apache.solr.search.Sorting;
import org.apache.solr.search.QParser;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.analysis.SolrAnalyzer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.MapSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.IOException;
public abstract class FieldType extends FieldProperties {
  public static final Logger log = LoggerFactory.getLogger(FieldType.class);
  public static final String POLY_FIELD_SEPARATOR = "___";
  protected String typeName;
  protected Map<String,String> args;
  protected int trueProperties;
  protected int falseProperties;
  int properties;
  public boolean isTokenized() {
    return (properties & TOKENIZED) != 0;
  }
  public boolean isMultiValued() {
    return (properties & MULTIVALUED) != 0;
  }
  public boolean isPolyField(){
    return false;
  }
  public boolean multiValuedFieldCache() {
    return isTokenized();
  }
  protected void init(IndexSchema schema, Map<String, String> args) {
  }
  protected String getArg(String n, Map<String,String> args) {
    String s = args.remove(n);
    if (s == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Missing parameter '"+n+"' for FieldType=" + typeName +args);
    }
    return s;
  }
  void setArgs(IndexSchema schema, Map<String,String> args) {
    properties = (STORED | INDEXED);
    float schemaVersion = schema.getVersion();
    if (schemaVersion < 1.1f) properties |= MULTIVALUED;
    if (schemaVersion > 1.1f) properties |= OMIT_TF_POSITIONS;
    if (schemaVersion < 1.3) {
      args.remove("compressThreshold");
    }
    this.args=args;
    Map<String,String> initArgs = new HashMap<String,String>(args);
    trueProperties = FieldProperties.parseProperties(initArgs,true);
    falseProperties = FieldProperties.parseProperties(initArgs,false);
    properties &= ~falseProperties;
    properties |= trueProperties;
    for (String prop : FieldProperties.propertyNames) initArgs.remove(prop);
    init(schema, initArgs);
    String positionInc = initArgs.get("positionIncrementGap");
    if (positionInc != null) {
      Analyzer analyzer = getAnalyzer();
      if (analyzer instanceof SolrAnalyzer) {
        ((SolrAnalyzer)analyzer).setPositionIncrementGap(Integer.parseInt(positionInc));
      } else {
        throw new RuntimeException("Can't set positionIncrementGap on custom analyzer " + analyzer.getClass());
      }
      analyzer = getQueryAnalyzer();
      if (analyzer instanceof SolrAnalyzer) {
        ((SolrAnalyzer)analyzer).setPositionIncrementGap(Integer.parseInt(positionInc));
      } else {
        throw new RuntimeException("Can't set positionIncrementGap on custom analyzer " + analyzer.getClass());
      }
      initArgs.remove("positionIncrementGap");
    }
    if (initArgs.size() > 0) {
      throw new RuntimeException("schema fieldtype " + typeName
              + "("+ this.getClass().getName() + ")"
              + " invalid arguments:" + initArgs);
    }
  }
  protected void restrictProps(int props) {
    if ((properties & props) != 0) {
      throw new RuntimeException("schema fieldtype " + typeName
              + "("+ this.getClass().getName() + ")"
              + " invalid properties:" + propertiesToString(properties & props));
    }
  }
  public String getTypeName() {
    return typeName;
  }
  void setTypeName(String typeName) {
    this.typeName = typeName;
  }
  public String toString() {
    return typeName + "{class=" + this.getClass().getName()
            + (analyzer != null ? ",analyzer=" + analyzer.getClass().getName() : "")
            + ",args=" + args
            +"}";
  }
  public Field createField(SchemaField field, String externalVal, float boost) {
    if (!field.indexed() && !field.stored()) {
      if (log.isTraceEnabled())
        log.trace("Ignoring unindexed/unstored field: " + field);
      return null;
    }
    String val;
    try {
      val = toInternal(externalVal);
    } catch (RuntimeException e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "Error while creating field '" + field + "' from value '" + externalVal + "'", e, false);
    }
    if (val==null) return null;
    return createField(field.getName(), val, getFieldStore(field, val),
            getFieldIndex(field, val), getFieldTermVec(field, val), field.omitNorms(),
            field.omitTf(), boost);
  }
  protected Field createField(String name, String val, Field.Store storage, Field.Index index,
                                    Field.TermVector vec, boolean omitNorms, boolean omitTFPos, float boost){
    Field f = new Field(name,
                        val,
                        storage,
                        index,
                        vec);
    f.setOmitNorms(omitNorms);
    f.setOmitTermFreqAndPositions(omitTFPos);
    f.setBoost(boost);
    return f;
  }
  public Fieldable[] createFields(SchemaField field, String externalVal, float boost) {
    Field f = createField( field, externalVal, boost);
    return f==null ? new Fieldable[]{} : new Fieldable[]{f};
  }
  protected Field.TermVector getFieldTermVec(SchemaField field,
                                             String internalVal) {
    Field.TermVector ftv = Field.TermVector.NO;
    if (field.storeTermPositions() && field.storeTermOffsets())
      ftv = Field.TermVector.WITH_POSITIONS_OFFSETS;
    else if (field.storeTermPositions())
      ftv = Field.TermVector.WITH_POSITIONS;
    else if (field.storeTermOffsets())
      ftv = Field.TermVector.WITH_OFFSETS;
    else if (field.storeTermVector())
      ftv = Field.TermVector.YES;
    return ftv;
  }
  protected Field.Store getFieldStore(SchemaField field,
                                      String internalVal) {
    return field.stored() ? Field.Store.YES : Field.Store.NO;
  }
  protected Field.Index getFieldIndex(SchemaField field,
                                      String internalVal) {
    return field.indexed() ? (isTokenized() ? Field.Index.ANALYZED :
                              Field.Index.NOT_ANALYZED) : Field.Index.NO;
  }
  public String toInternal(String val) {
    return val;
  }
  public String toExternal(Fieldable f) {
    return f.stringValue();
  }
  public Object toObject(Fieldable f) {
    return toExternal(f); 
  }
  public String indexedToReadable(String indexedForm) {
    return indexedForm;
  }
  public String storedToReadable(Fieldable f) {
    return toExternal(f);
  }
  public String storedToIndexed(Fieldable f) {
    return f.stringValue();
  }
  public String readableToIndexed(String val) {
    return toInternal(val);
  }
  protected class DefaultAnalyzer extends SolrAnalyzer {
    final int maxChars;
    DefaultAnalyzer(int maxChars) {
      this.maxChars=maxChars;
    }
    public TokenStreamInfo getStream(String fieldName, Reader reader) {
      Tokenizer ts = new Tokenizer(reader) {
        final char[] cbuf = new char[maxChars];
        final TermAttribute termAtt = (TermAttribute) addAttribute(TermAttribute.class);
        final OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
        @Override
        public boolean incrementToken() throws IOException {
          clearAttributes();
          int n = input.read(cbuf,0,maxChars);
          if (n<=0) return false;
          String s = toInternal(new String(cbuf,0,n));
          termAtt.setTermBuffer(s);
          offsetAtt.setOffset(correctOffset(0),correctOffset(n));
          return true;
        }
      };
      return new TokenStreamInfo(ts, ts);
    }
  }
  protected Analyzer analyzer=new DefaultAnalyzer(256);
  protected Analyzer queryAnalyzer=analyzer;
  public Analyzer getAnalyzer() {
    return analyzer;
  }
  public Analyzer getQueryAnalyzer() {
    return queryAnalyzer;
  }
  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
    log.trace("FieldType: " + typeName + ".setAnalyzer(" + analyzer.getClass().getName() + ")" );
  }
  public void setQueryAnalyzer(Analyzer analyzer) {
    this.queryAnalyzer = analyzer;
    log.trace("FieldType: " + typeName + ".setQueryAnalyzer(" + analyzer.getClass().getName() + ")" );
  }
  public abstract void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException;
  public abstract void write(TextResponseWriter writer, String name, Fieldable f) throws IOException;
  public abstract SortField getSortField(SchemaField field, boolean top);
  protected SortField getStringSort(SchemaField field, boolean reverse) {
    return Sorting.getStringSortField(field.name, reverse, field.sortMissingLast(),field.sortMissingFirst());
  }
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    return getValueSource(field);
  }
  @Deprecated
  public ValueSource getValueSource(SchemaField field) {
    return new OrdFieldSource(field.name);
  }
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    return new TermRangeQuery(
            field.getName(),
            part1 == null ? null : toInternal(part1),
            part2 == null ? null : toInternal(part2),
            minInclusive, maxInclusive);
  }
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    return new TermQuery(new Term(field.getName(), toInternal(externalVal)));
  }
}
