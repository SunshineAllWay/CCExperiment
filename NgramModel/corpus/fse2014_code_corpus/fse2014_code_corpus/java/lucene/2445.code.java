package org.apache.solr.schema;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.Config;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.analysis.CharFilterFactory;
import org.apache.solr.analysis.TokenFilterFactory;
import org.apache.solr.analysis.TokenizerChain;
import org.apache.solr.analysis.TokenizerFactory;
import org.apache.solr.search.SolrQueryParser;
import org.apache.solr.util.plugin.AbstractPluginLoader;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.w3c.dom.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class IndexSchema {
  public static final String DEFAULT_SCHEMA_FILE = "schema.xml";
  public static final String LUCENE_MATCH_VERSION_PARAM = "luceneMatchVersion";
  final static Logger log = LoggerFactory.getLogger(IndexSchema.class);
  private final SolrConfig solrConfig;
  private final String resourceName;
  private String name;
  private float version;
  private final SolrResourceLoader loader;
  private final HashMap<String, SchemaField> fields = new HashMap<String,SchemaField>();
  private final HashMap<String, FieldType> fieldTypes = new HashMap<String,FieldType>();
  private final List<SchemaField> fieldsWithDefaultValue = new ArrayList<SchemaField>();
  private final Collection<SchemaField> requiredFields = new HashSet<SchemaField>();
  private DynamicField[] dynamicFields;
  private Analyzer analyzer;
  private Analyzer queryAnalyzer;
  private String defaultSearchFieldName=null;
  private String queryParserDefaultOperator = "OR";
  private final Map<String, List<CopyField>> copyFieldsMap = new HashMap<String, List<CopyField>>();
  private DynamicCopy[] dynamicCopyFields;
  private Map<SchemaField, Integer> copyFieldTargetCounts
    = new HashMap<SchemaField, Integer>();
  @Deprecated
  public IndexSchema(SolrConfig solrConfig, String name) {
    this(solrConfig, name, null);
  }
  public IndexSchema(SolrConfig solrConfig, String name, InputStream is) {
    this.solrConfig = solrConfig;
    if (name == null)
      name = DEFAULT_SCHEMA_FILE;
    this.resourceName = name;
    loader = solrConfig.getResourceLoader();
    InputStream lis = is;
    if (lis == null)
      lis = loader.openSchema(name);
    readSchema(lis);
    if (lis != is) {
      try {
        lis.close();
      }
      catch(IOException xio) {} 
    }
    loader.inform( loader );
  }
  public SolrConfig getSolrConfig() {
    return solrConfig;
  }
  public SolrResourceLoader getResourceLoader()
  {
    return loader;
  }
  public String getResourceName() {
    return resourceName;
  }
  public String getSchemaName() {
    return name;
  }
  float getVersion() {
    return version;
  }
  @Deprecated
  public InputStream getInputStream() {
    return loader.openResource(resourceName);
  }
  @Deprecated
  public String getSchemaFile() {
    return resourceName;
  }
  @Deprecated
  public String getName() { return name; }
  public Map<String,SchemaField> getFields() { return fields; }
  public Map<String,FieldType> getFieldTypes() { return fieldTypes; }
  public List<SchemaField> getFieldsWithDefaultValue() { return fieldsWithDefaultValue; }
  public Collection<SchemaField> getRequiredFields() { return requiredFields; }
  private SimilarityFactory similarityFactory;
  public Similarity getSimilarity() { return similarityFactory.getSimilarity(); }
  public SimilarityFactory getSimilarityFactory() { return similarityFactory; }
  public Analyzer getAnalyzer() { return analyzer; }
  public Analyzer getQueryAnalyzer() { return queryAnalyzer; }
  public SolrQueryParser getSolrQueryParser(String defaultField) {
    SolrQueryParser qp = new SolrQueryParser(this,defaultField);
    String operator = getQueryParserDefaultOperator();
    qp.setDefaultOperator("AND".equals(operator) ?
                          QueryParser.Operator.AND : QueryParser.Operator.OR);
    return qp;
  }
  public String getDefaultSearchFieldName() {
    return defaultSearchFieldName;
  }
  @Deprecated
  public String getQueryParserDefaultOperator() {
    return queryParserDefaultOperator;
  }
  private SchemaField uniqueKeyField;
  public SchemaField getUniqueKeyField() { return uniqueKeyField; }
  private String uniqueKeyFieldName;
  private FieldType uniqueKeyFieldType;
  public Fieldable getUniqueKeyField(org.apache.lucene.document.Document doc) {
    return doc.getFieldable(uniqueKeyFieldName);  
  }
  public String printableUniqueKey(org.apache.lucene.document.Document doc) {
     Fieldable f = doc.getFieldable(uniqueKeyFieldName);
     return f==null ? null : uniqueKeyFieldType.toExternal(f);
  }
  private SchemaField getIndexedField(String fname) {
    SchemaField f = getFields().get(fname);
    if (f==null) {
      throw new RuntimeException("unknown field '" + fname + "'");
    }
    if (!f.indexed()) {
      throw new RuntimeException("'"+fname+"' is not an indexed field:" + f);
    }
    return f;
  }
  public void refreshAnalyzers()
  {
    analyzer = new SolrIndexAnalyzer();
    queryAnalyzer = new SolrQueryAnalyzer();
  }
  private class SolrIndexAnalyzer extends Analyzer {
    protected final HashMap<String,Analyzer> analyzers;
    SolrIndexAnalyzer() {
      analyzers = analyzerCache();
    }
    protected HashMap<String,Analyzer> analyzerCache() {
      HashMap<String,Analyzer> cache = new HashMap<String,Analyzer>();
       for (SchemaField f : getFields().values()) {
        Analyzer analyzer = f.getType().getAnalyzer();
        cache.put(f.getName(), analyzer);
      }
      return cache;
    }
    protected Analyzer getAnalyzer(String fieldName)
    {
      Analyzer analyzer = analyzers.get(fieldName);
      return analyzer!=null ? analyzer : getDynamicFieldType(fieldName).getAnalyzer();
    }
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
      return getAnalyzer(fieldName).tokenStream(fieldName,reader);
    }
    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
      return getAnalyzer(fieldName).reusableTokenStream(fieldName,reader);
    }
    @Override
    public int getPositionIncrementGap(String fieldName) {
      return getAnalyzer(fieldName).getPositionIncrementGap(fieldName);
    }
  }
  private class SolrQueryAnalyzer extends SolrIndexAnalyzer {
    @Override
    protected HashMap<String,Analyzer> analyzerCache() {
      HashMap<String,Analyzer> cache = new HashMap<String,Analyzer>();
       for (SchemaField f : getFields().values()) {
        Analyzer analyzer = f.getType().getQueryAnalyzer();
        cache.put(f.getName(), analyzer);
      }
      return cache;
    }
    @Override
    protected Analyzer getAnalyzer(String fieldName)
    {
      Analyzer analyzer = analyzers.get(fieldName);
      return analyzer!=null ? analyzer : getDynamicFieldType(fieldName).getQueryAnalyzer();
    }
  }
  private void readSchema(InputStream is) {
    log.info("Reading Solr Schema");
    try {
      Config schemaConf = new Config(loader, "schema", is, "/schema/");
      Document document = schemaConf.getDocument();
      final XPath xpath = schemaConf.getXPath();
      final List<SchemaAware> schemaAware = new ArrayList<SchemaAware>();
      Node nd = (Node) xpath.evaluate("/schema/@name", document, XPathConstants.NODE);
      if (nd==null) {
        log.warn("schema has no name!");
      } else {
        name = nd.getNodeValue();
        log.info("Schema name=" + name);
      }
      version = schemaConf.getFloat("/schema/@version", 1.0f);
      final IndexSchema schema = this;
      AbstractPluginLoader<FieldType> fieldLoader = new AbstractPluginLoader<FieldType>( "[schema.xml] fieldType", true, true) {
        @Override
        protected FieldType create( ResourceLoader loader, String name, String className, Node node ) throws Exception
        {
          FieldType ft = (FieldType)loader.newInstance(className);
          ft.setTypeName(name);
          String expression = "./analyzer[@type='query']";
          Node anode = (Node)xpath.evaluate(expression, node, XPathConstants.NODE);
          Analyzer queryAnalyzer = readAnalyzer(anode);
          expression = "./analyzer[not(@type)] | ./analyzer[@type='index']";
          anode = (Node)xpath.evaluate(expression, node, XPathConstants.NODE);
          Analyzer analyzer = readAnalyzer(anode);
          if (queryAnalyzer==null) queryAnalyzer=analyzer;
          if (analyzer==null) analyzer=queryAnalyzer;
          if (analyzer!=null) {
            ft.setAnalyzer(analyzer);
            ft.setQueryAnalyzer(queryAnalyzer);
          }
          if (ft instanceof SchemaAware){
            schemaAware.add((SchemaAware) ft);
          }
          return ft;
        }
        @Override
        protected void init(FieldType plugin, Node node) throws Exception {
          Map<String,String> params = DOMUtil.toMapExcept( node.getAttributes(), "name","class" );
          plugin.setArgs(schema, params );
        }
        @Override
        protected FieldType register(String name, FieldType plugin) throws Exception {
          log.trace("fieldtype defined: " + plugin );
          return fieldTypes.put( name, plugin );
        }
      };
      String expression = "/schema/types/fieldtype | /schema/types/fieldType";
      NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
      fieldLoader.load( loader, nodes );
      Map<String,Boolean> explicitRequiredProp = new HashMap<String, Boolean>();
      ArrayList<DynamicField> dFields = new ArrayList<DynamicField>();
      expression = "/schema/fields/field | /schema/fields/dynamicField";
      nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
      for (int i=0; i<nodes.getLength(); i++) {
        Node node = nodes.item(i);
        NamedNodeMap attrs = node.getAttributes();
        String name = DOMUtil.getAttr(attrs,"name","field definition");
        log.trace("reading field def "+name);
        String type = DOMUtil.getAttr(attrs,"type","field " + name);
        FieldType ft = fieldTypes.get(type);
        if (ft==null) {
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Unknown fieldtype '" + type + "' specified on field " + name,false);
        }
        Map<String,String> args = DOMUtil.toMapExcept(attrs, "name", "type");
        if( args.get( "required" ) != null ) {
          explicitRequiredProp.put( name, Boolean.valueOf( args.get( "required" ) ) );
        }
        SchemaField f = SchemaField.create(name,ft,args);
        if (node.getNodeName().equals("field")) {
          SchemaField old = fields.put(f.getName(),f);
          if( old != null ) {
            String msg = "[schema.xml] Duplicate field definition for '"
              + f.getName() + "' ignoring: "+old.toString();
            Throwable t = new SolrException( SolrException.ErrorCode.SERVER_ERROR, msg );
            SolrException.logOnce(log,null,t);
            SolrConfig.severeErrors.add( t );
          }
          log.debug("field defined: " + f);
          if( f.getDefaultValue() != null ) {
            log.debug(name+" contains default value: " + f.getDefaultValue());
            fieldsWithDefaultValue.add( f );
          }
          if (f.isRequired()) {
            log.debug(name+" is required in this schema");
            requiredFields.add(f);
          }
        } else if (node.getNodeName().equals("dynamicField")) {
          addDynamicField(dFields, f);
        } else {
          throw new RuntimeException("Unknown field type");
        }
      }
    requiredFields.addAll(getFieldsWithDefaultValue());
    Collections.sort(dFields);
    log.trace("Dynamic Field Ordering:" + dFields);
    dynamicFields = (DynamicField[])dFields.toArray(new DynamicField[dFields.size()]);
    Node node = (Node) xpath.evaluate("/schema/similarity", document, XPathConstants.NODE);
    if (node==null) {
      similarityFactory = new SimilarityFactory() {
        public Similarity getSimilarity() {
          return Similarity.getDefault();
        }
      };
      log.debug("using default similarity");
    } else {
      final Object obj = loader.newInstance(((Element) node).getAttribute("class"));
      if (obj instanceof SimilarityFactory) {
        SolrParams params = SolrParams.toSolrParams(DOMUtil.childNodesToNamedList(node));
        similarityFactory = (SimilarityFactory)obj;
        similarityFactory.init(params);
      } else {
        similarityFactory = new SimilarityFactory() {
          public Similarity getSimilarity() {
            return (Similarity) obj;
          }
        };
      }
      if (similarityFactory instanceof SchemaAware){
        schemaAware.add((SchemaAware) similarityFactory);
      }
      log.debug("using similarity factory" + similarityFactory.getClass().getName());
    }
    node = (Node) xpath.evaluate("/schema/defaultSearchField/text()", document, XPathConstants.NODE);
    if (node==null) {
      log.warn("no default search field specified in schema.");
    } else {
      defaultSearchFieldName=node.getNodeValue().trim();
      if (defaultSearchFieldName!=null) {
        SchemaField defaultSearchField = getFields().get(defaultSearchFieldName);
        if ((defaultSearchField == null) || !defaultSearchField.indexed()) {
          String msg =  "default search field '" + defaultSearchFieldName + "' not defined or not indexed" ;
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, msg );
        }
      }
      log.info("default search field is "+defaultSearchFieldName);
    }
    node = (Node) xpath.evaluate("/schema/solrQueryParser/@defaultOperator", document, XPathConstants.NODE);
    if (node==null) {
      log.debug("using default query parser operator (OR)");
    } else {
      queryParserDefaultOperator=node.getNodeValue().trim();
      log.info("query parser default operator is "+queryParserDefaultOperator);
    }
    node = (Node) xpath.evaluate("/schema/uniqueKey/text()", document, XPathConstants.NODE);
    if (node==null) {
      log.warn("no uniqueKey specified in schema.");
    } else {
      uniqueKeyField=getIndexedField(node.getNodeValue().trim());
      if (!uniqueKeyField.stored()) {
        log.error("uniqueKey is not stored - distributed search will not work");
      }
      if (uniqueKeyField.multiValued()) {
        log.error("uniqueKey should not be multivalued");
      }
      uniqueKeyFieldName=uniqueKeyField.getName();
      uniqueKeyFieldType=uniqueKeyField.getType();
      log.info("unique key field: "+uniqueKeyFieldName);
      if( Boolean.FALSE != explicitRequiredProp.get( uniqueKeyFieldName ) ) {
        uniqueKeyField.required = true;
        requiredFields.add(uniqueKeyField);
      }
    }
    dynamicCopyFields = new DynamicCopy[] {};
    expression = "//copyField";
    nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
      for (int i=0; i<nodes.getLength(); i++) {
        node = nodes.item(i);
        NamedNodeMap attrs = node.getAttributes();
        String source = DOMUtil.getAttr(attrs,"source","copyField definition");
        String dest   = DOMUtil.getAttr(attrs,"dest",  "copyField definition");
        String maxChars = DOMUtil.getAttr(attrs, "maxChars");
        int maxCharsInt = CopyField.UNLIMITED;
        if (maxChars != null) {
          try {
            maxCharsInt = Integer.parseInt(maxChars);
          } catch (NumberFormatException e) {
            log.warn("Couldn't parse maxChars attribute for copyField from "
                    + source + " to " + dest + " as integer. The whole field will be copied.");
          }
        }
        registerCopyField(source, dest, maxCharsInt);
     }
      for (Map.Entry<SchemaField, Integer> entry : copyFieldTargetCounts.entrySet())    {
        if (entry.getValue() > 1 && !entry.getKey().multiValued())  {
          log.warn("Field " + entry.getKey().name + " is not multivalued "+
                      "and destination for multiple copyFields ("+
                      entry.getValue()+")");
        }
      }
      for (SchemaAware aware : schemaAware) {
        aware.inform(this);
      }
    } catch (SolrException e) {
      SolrConfig.severeErrors.add( e );
      throw e;
    } catch(Exception e) {
      SolrConfig.severeErrors.add( e );
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Schema Parsing Failed",e,false);
    }
    refreshAnalyzers();
  }
  private void addDynamicField(List<DynamicField> dFields, SchemaField f) {
    boolean dup = isDuplicateDynField(dFields, f);
    if( !dup ) {
      addDynamicFieldNoDupCheck(dFields, f);
    } else {
      String msg = "[schema.xml] Duplicate DynamicField definition for '"
              + f.getName() + "' ignoring: " + f.toString();
      Throwable t = new SolrException(SolrException.ErrorCode.SERVER_ERROR, msg);
      SolrException.logOnce(log, null, t);
      SolrConfig.severeErrors.add(t);
    }
  }
  public void registerDynamicField(SchemaField ... f) {
    List<DynamicField> dynFields = new ArrayList<DynamicField>(Arrays.asList(dynamicFields));
    for (SchemaField field : f) {
      if (isDuplicateDynField(dynFields, field) == false) {
        log.debug("dynamic field creation for schema field: " + field.getName());
        addDynamicFieldNoDupCheck(dynFields, field);
      } else {
        log.debug("dynamic field already exists: dynamic field: [" + field.getName() + "]");
      }
    }
    Collections.sort(dynFields);
    dynamicFields = dynFields.toArray(new DynamicField[dynFields.size()]);
  }
  private void addDynamicFieldNoDupCheck(List<DynamicField> dFields, SchemaField f) {
    dFields.add(new DynamicField(f));
    log.debug("dynamic field defined: " + f);
  }
  private boolean isDuplicateDynField(List<DynamicField> dFields, SchemaField f) {
    for( DynamicField df : dFields ) {
      if( df.regex.equals( f.name ) ) return true;
    }
    return false;
  }
  public void registerCopyField( String source, String dest )
  {
    registerCopyField(source, dest, CopyField.UNLIMITED);
  }
  public void registerCopyField( String source, String dest, int maxChars )
  {
    boolean sourceIsPattern = isWildCard(source);
    boolean destIsPattern   = isWildCard(dest);
    log.debug("copyField source='"+source+"' dest='"+dest+"' maxChars='"+maxChars);
    SchemaField d = getFieldOrNull(dest);
    if(d == null){
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "copyField destination :'"+dest+"' does not exist" );
    }
    if(sourceIsPattern) {
      if( destIsPattern ) {
        DynamicField df = null;
        for( DynamicField dd : dynamicFields ) {
          if( dd.regex.equals( dest ) ) {
            df = dd;
            break;
          }
        }
        if( df == null ) {
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "copyField dynamic destination must match a dynamicField." );
        }
        registerDynamicCopyField(new DynamicDestCopy(source, df, maxChars ));
      }
      else {
        registerDynamicCopyField(new DynamicCopy(source, d, maxChars));
      }
    } 
    else if( destIsPattern ) {
      String msg =  "copyField only supports a dynamic destination if the source is also dynamic" ;
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, msg );
    }
    else {
      SchemaField f = getField(source);
      List<CopyField> copyFieldList = copyFieldsMap.get(source);
      if (copyFieldList == null) {
        copyFieldList = new ArrayList<CopyField>();
        copyFieldsMap.put(source, copyFieldList);
      }
      copyFieldList.add(new CopyField(f, d, maxChars));
      copyFieldTargetCounts.put(d, (copyFieldTargetCounts.containsKey(d) ? copyFieldTargetCounts.get(d) + 1 : 1));
    }
  }
  private void registerDynamicCopyField( DynamicCopy dcopy )
  {
    if( dynamicCopyFields == null ) {
      dynamicCopyFields = new DynamicCopy[] {dcopy};
    }
    else {
      DynamicCopy[] temp = new DynamicCopy[dynamicCopyFields.length+1];
      System.arraycopy(dynamicCopyFields,0,temp,0,dynamicCopyFields.length);
      temp[temp.length -1] = dcopy;
      dynamicCopyFields = temp;
    }
    log.trace("Dynamic Copy Field:" + dcopy );
  }
  private static Object[] append(Object[] orig, Object item) {
    Object[] newArr = (Object[])java.lang.reflect.Array.newInstance(orig.getClass().getComponentType(), orig.length+1);
    System.arraycopy(orig, 0, newArr, 0, orig.length);
    newArr[orig.length] = item;
    return newArr;
  }
  private Analyzer readAnalyzer(Node node) throws XPathExpressionException {
    if (node == null) return null;
    NamedNodeMap attrs = node.getAttributes();
    String analyzerName = DOMUtil.getAttr(attrs,"class");
    if (analyzerName != null) {
      final Class<? extends Analyzer> clazz = loader.findClass(analyzerName).asSubclass(Analyzer.class);
      try {
        try {
          Constructor<? extends Analyzer> cnstr = clazz.getConstructor(Version.class);
          final String matchVersionStr = DOMUtil.getAttr(attrs, LUCENE_MATCH_VERSION_PARAM);
          final Version luceneMatchVersion = (matchVersionStr == null) ?
            solrConfig.luceneMatchVersion : Config.parseLuceneVersionString(matchVersionStr);
          if (luceneMatchVersion == null) {
            throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
              "Configuration Error: Analyzer '" + clazz.getName() +
              "' needs a 'luceneMatchVersion' parameter");
          }
          return cnstr.newInstance(luceneMatchVersion);
        } catch (NoSuchMethodException nsme) {
          return clazz.newInstance();
        }
      } catch (Exception e) {
        throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
              "Cannot load analyzer: "+analyzerName );
      }
    }
    XPath xpath = XPathFactory.newInstance().newXPath();
    final ArrayList<CharFilterFactory> charFilters = new ArrayList<CharFilterFactory>();
    AbstractPluginLoader<CharFilterFactory> charFilterLoader =
      new AbstractPluginLoader<CharFilterFactory>( "[schema.xml] analyzer/charFilter", false, false )
    {
      @Override
      protected void init(CharFilterFactory plugin, Node node) throws Exception {
        if( plugin != null ) {
          final Map<String,String> params = DOMUtil.toMapExcept(node.getAttributes(),"class");
          if (!params.containsKey(LUCENE_MATCH_VERSION_PARAM))
            params.put(LUCENE_MATCH_VERSION_PARAM, solrConfig.luceneMatchVersion.toString());
          plugin.init( params );
          charFilters.add( plugin );
        }
      }
      @Override
      protected CharFilterFactory register(String name, CharFilterFactory plugin) throws Exception {
        return null; 
      }
    };
    charFilterLoader.load( solrConfig.getResourceLoader(), (NodeList)xpath.evaluate("./charFilter", node, XPathConstants.NODESET) );
    final ArrayList<TokenizerFactory> tokenizers = new ArrayList<TokenizerFactory>(1);
    AbstractPluginLoader<TokenizerFactory> tokenizerLoader =
      new AbstractPluginLoader<TokenizerFactory>( "[schema.xml] analyzer/tokenizer", false, false )
    {
      @Override
      protected void init(TokenizerFactory plugin, Node node) throws Exception {
        if( !tokenizers.isEmpty() ) {
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
              "The schema defines multiple tokenizers for: "+node );
        }
        final Map<String,String> params = DOMUtil.toMapExcept(node.getAttributes(),"class");
        if (!params.containsKey(LUCENE_MATCH_VERSION_PARAM))
          params.put(LUCENE_MATCH_VERSION_PARAM, solrConfig.luceneMatchVersion.toString());
        plugin.init( params );
        tokenizers.add( plugin );
      }
      @Override
      protected TokenizerFactory register(String name, TokenizerFactory plugin) throws Exception {
        return null; 
      }
    };
    tokenizerLoader.load( loader, (NodeList)xpath.evaluate("./tokenizer", node, XPathConstants.NODESET) );
    if( tokenizers.isEmpty() ) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"analyzer without class or tokenizer & filter list");
    }
    final ArrayList<TokenFilterFactory> filters = new ArrayList<TokenFilterFactory>();
    AbstractPluginLoader<TokenFilterFactory> filterLoader = 
      new AbstractPluginLoader<TokenFilterFactory>( "[schema.xml] analyzer/filter", false, false )
    {
      @Override
      protected void init(TokenFilterFactory plugin, Node node) throws Exception {
        if( plugin != null ) {
          final Map<String,String> params = DOMUtil.toMapExcept(node.getAttributes(),"class");
          if (!params.containsKey(LUCENE_MATCH_VERSION_PARAM))
            params.put(LUCENE_MATCH_VERSION_PARAM, solrConfig.luceneMatchVersion.toString());
          plugin.init( params );
          filters.add( plugin );
        }
      }
      @Override
      protected TokenFilterFactory register(String name, TokenFilterFactory plugin) throws Exception {
        return null; 
      }
    };
    filterLoader.load( loader, (NodeList)xpath.evaluate("./filter", node, XPathConstants.NODESET) );
    return new TokenizerChain(charFilters.toArray(new CharFilterFactory[charFilters.size()]),
        tokenizers.get(0), filters.toArray(new TokenFilterFactory[filters.size()]));
  };
  static abstract class DynamicReplacement implements Comparable<DynamicReplacement> {
    final static int STARTS_WITH=1;
    final static int ENDS_WITH=2;
    final String regex;
    final int type;
    final String str;
    protected DynamicReplacement(String regex) {
      this.regex = regex;
      if (regex.startsWith("*")) {
        type=ENDS_WITH;
        str=regex.substring(1);
      }
      else if (regex.endsWith("*")) {
        type=STARTS_WITH;
        str=regex.substring(0,regex.length()-1);
      }
      else {
        throw new RuntimeException("dynamic field name must start or end with *");
      }
    }
    public boolean matches(String name) {
      if (type==STARTS_WITH && name.startsWith(str)) return true;
      else if (type==ENDS_WITH && name.endsWith(str)) return true;
      else return false;
    }
    public int compareTo(DynamicReplacement other) {
      return other.regex.length() - regex.length();
    }
  }
  final static class DynamicField extends DynamicReplacement {
    final SchemaField prototype;
    DynamicField(SchemaField prototype) {
      super(prototype.name);
      this.prototype=prototype;
    }
    SchemaField makeSchemaField(String name) {
      return new SchemaField(prototype, name);
    }
    public String toString() {
      return prototype.toString();
    }
  }
  static class DynamicCopy extends DynamicReplacement {
    final SchemaField targetField;
    final int maxChars;
    DynamicCopy(String regex, SchemaField targetField) {
      this(regex, targetField, CopyField.UNLIMITED);
    }
    DynamicCopy(String regex, SchemaField targetField, int maxChars) {
      super(regex);
      this.targetField = targetField;
      this.maxChars = maxChars;
    }
    public SchemaField getTargetField( String sourceField )
    {
      return targetField;
    }
    @Override
    public String toString() {
      return targetField.toString();
    }
  }
  static class DynamicDestCopy extends DynamicCopy 
  {
    final DynamicField dynamic;
    final int dtype;
    final String dstr;
    DynamicDestCopy(String source, DynamicField dynamic) {
      this(source, dynamic, CopyField.UNLIMITED);
    }
    DynamicDestCopy(String source, DynamicField dynamic, int maxChars) {
      super(source, dynamic.prototype, maxChars);
      this.dynamic = dynamic;
      String dest = dynamic.regex;
      if (dest.startsWith("*")) {
        dtype=ENDS_WITH;
        dstr=dest.substring(1);
      }
      else if (dest.endsWith("*")) {
        dtype=STARTS_WITH;
        dstr=dest.substring(0,dest.length()-1);
      }
      else {
        throw new RuntimeException("dynamic copyField destination name must start or end with *");
      }
    }
    @Override
    public SchemaField getTargetField( String sourceField )
    {
      String dyn = ( type==STARTS_WITH ) 
        ? sourceField.substring( str.length() )
        : sourceField.substring( 0, sourceField.length()-str.length() );
      String name = (dtype==STARTS_WITH) ? (dstr+dyn) : (dyn+dstr);
      return dynamic.makeSchemaField( name );
    }
    @Override
    public String toString() {
      return targetField.toString();
    }
  }
  public SchemaField[] getDynamicFieldPrototypes() {
    SchemaField[] df = new SchemaField[dynamicFields.length];
    for (int i=0;i<dynamicFields.length;i++) {
      df[i] = dynamicFields[i].prototype;
    }
    return df;
  }
  public String getDynamicPattern(String fieldName) {
   for (DynamicField df : dynamicFields) {
     if (df.matches(fieldName)) return df.regex;
   }
   return  null; 
  }
  public boolean hasExplicitField(String fieldName) {
    if(fields.containsKey(fieldName)) {
      return true;
    }
    for (DynamicField df : dynamicFields) {
      if (df.matches(fieldName)) return true;
    }
    return false;
  }
  public boolean isDynamicField(String fieldName) {
    if(fields.containsKey(fieldName)) {
      return false;
    }
    for (DynamicField df : dynamicFields) {
      if (df.matches(fieldName)) return true;
    }
    return false;
  }   
  public SchemaField getFieldOrNull(String fieldName) {
    SchemaField f = fields.get(fieldName);
    if (f != null) return f;
    for (DynamicField df : dynamicFields) {
      if (df.matches(fieldName)) return df.makeSchemaField(fieldName);
    }
    return f;
  }
  public SchemaField getField(String fieldName) {
    SchemaField f = getFieldOrNull(fieldName);
    if (f != null) return f;
    throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"undefined field "+fieldName);
  }
  public FieldType getFieldType(String fieldName) {
    SchemaField f = fields.get(fieldName);
    if (f != null) return f.getType();
    return getDynamicFieldType(fieldName);
  }
  public FieldType getFieldTypeByName(String fieldTypeName){
    return fieldTypes.get(fieldTypeName);
  }
  public FieldType getFieldTypeNoEx(String fieldName) {
    SchemaField f = fields.get(fieldName);
    if (f != null) return f.getType();
    return dynFieldType(fieldName);
  }
  public FieldType getDynamicFieldType(String fieldName) {
     for (DynamicField df : dynamicFields) {
      if (df.matches(fieldName)) return df.prototype.getType();
    }
    throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"undefined field "+fieldName);
  }
  private FieldType dynFieldType(String fieldName) {
     for (DynamicField df : dynamicFields) {
      if (df.matches(fieldName)) return df.prototype.getType();
    }
    return null;
  };
  public SchemaField[] getCopySources(String destField) {
    SchemaField f = getField(destField);
    if (!isCopyFieldTarget(f)) {
      return new SchemaField[0];
    }
    List<SchemaField> sf = new ArrayList<SchemaField>();
    for (Map.Entry<String, List<CopyField>> cfs : copyFieldsMap.entrySet()) {
      for (CopyField copyField : cfs.getValue()) {
        if (copyField.getDestination().getName().equals(destField)) {
          sf.add(copyField.getSource());
        }
      }
    }
    return sf.toArray(new SchemaField[sf.size()]);
  }
  @Deprecated
  public SchemaField[] getCopyFields(String sourceField) {
    List<SchemaField> matchCopyFields = new ArrayList<SchemaField>();
    for(DynamicCopy dynamicCopy : dynamicCopyFields) {
      if(dynamicCopy.matches(sourceField)) {
        matchCopyFields.add(dynamicCopy.getTargetField(sourceField));
      }
    }
    final List<CopyField> copyFields = copyFieldsMap.get(sourceField);
    if (copyFields!=null) {
      final Iterator<CopyField> it = copyFields.iterator();
      while (it.hasNext()) {
        matchCopyFields.add(it.next().getDestination());
      }
    }
    return matchCopyFields.toArray(new SchemaField[matchCopyFields.size()]);
  }
  public List<CopyField> getCopyFieldsList(final String sourceField){
    final List<CopyField> result = new ArrayList<CopyField>();
    for (DynamicCopy dynamicCopy : dynamicCopyFields) {
      if (dynamicCopy.matches(sourceField)) {
        result.add(new CopyField(getField(sourceField), dynamicCopy.getTargetField(sourceField), dynamicCopy.maxChars));
      }
    }
    List<CopyField> fixedCopyFields = copyFieldsMap.get(sourceField);
    if (fixedCopyFields != null)
    {
      result.addAll(fixedCopyFields);
    }
    return result;
  }
  public boolean isCopyFieldTarget( SchemaField f )
  {
    return copyFieldTargetCounts.containsKey( f );
  }
  private static boolean isWildCard(String name) {
    return  name.startsWith("*") || name.endsWith("*");
  }
}
