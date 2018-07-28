package org.apache.solr.schema;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.search.QParser;
import org.apache.lucene.search.Query;
import java.util.HashMap;
import java.util.Map;
public abstract class AbstractSubTypeFieldType extends FieldType implements SchemaAware {
  protected FieldType subType;
  public static final String SUB_FIELD_SUFFIX = "subFieldSuffix";
  public static final String SUB_FIELD_TYPE = "subFieldType";
  protected String suffix;
  protected int dynFieldProps;
  protected String[] suffixes;
  protected IndexSchema schema;   
  public FieldType getSubType() {
    return subType;
  }
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    this.schema = schema;
    SolrParams p = new MapSolrParams(args);
    String subFT = p.get(SUB_FIELD_TYPE);
    String subSuffix = p.get(SUB_FIELD_SUFFIX);
    if (subFT != null) {
      args.remove(SUB_FIELD_TYPE);
      subType = schema.getFieldTypeByName(subFT.trim());
      suffix = POLY_FIELD_SEPARATOR + subType.typeName;
    } else if (subSuffix != null) {
      args.remove(SUB_FIELD_SUFFIX);
      suffix = subSuffix;
    } else {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "The field type: " + typeName
              + " must specify the " +
              SUB_FIELD_TYPE + " attribute or the " + SUB_FIELD_SUFFIX + " attribute.");
    }
  }
  static SchemaField registerPolyFieldDynamicPrototype(IndexSchema schema, FieldType type) {
    String name = "*" + FieldType.POLY_FIELD_SEPARATOR + type.typeName;
    Map<String, String> props = new HashMap<String, String>();
    props.put("indexed", "true");
    props.put("stored", "false");
    int p = SchemaField.calcProps(name, type, props);
    SchemaField proto = SchemaField.create(name,
            type, p, null);
    schema.registerDynamicField(proto);
    return proto;
  }
  public void inform(IndexSchema schema) {
    if (subType != null) {
      SchemaField proto = registerPolyFieldDynamicPrototype(schema, subType);
      dynFieldProps = proto.getProperties();
    }
  }
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    throw new UnsupportedOperationException();
  }
  protected void createSuffixCache(int size) {
    suffixes = new String[size];
    for (int i=0; i<size; i++) {
      suffixes[i] = "_" + i + suffix;
    }
  }
  protected SchemaField subField(SchemaField base, int i) {
    return schema.getField(base.getName() + suffixes[i]);
  }
}
