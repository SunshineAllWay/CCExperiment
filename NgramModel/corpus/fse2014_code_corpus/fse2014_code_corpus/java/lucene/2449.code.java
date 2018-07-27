package org.apache.solr.schema;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.VectorValueSource;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.distance.DistanceUtils;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class PointType extends CoordinateFieldType {
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    SolrParams p = new MapSolrParams(args);
    dimension = p.getInt(DIMENSION, DEFAULT_DIMENSION);
    if (dimension < 1) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "The dimension must be > 0: " + dimension);
    }
    args.remove(DIMENSION);
    this.schema = schema;
    super.init(schema, args);
    createSuffixCache(dimension);
  }
  @Override
  public boolean isPolyField() {
    return true;   
  }
  @Override
  public Fieldable[] createFields(SchemaField field, String externalVal, float boost) {
    String[] point = DistanceUtils.parsePoint(null, externalVal, dimension);
    Fieldable[] f = new Fieldable[ (field.indexed() ? dimension : 0) + (field.stored() ? 1 : 0) ];
    if (field.indexed()) {
      for (int i=0; i<dimension; i++) {
        f[i] = subField(field, i).createField(point[i], boost);
      }
    }
    if (field.stored()) {
      String storedVal = externalVal;  
      f[f.length - 1] = createField(field.getName(), storedVal,
                getFieldStore(field, storedVal), Field.Index.NO, Field.TermVector.NO,
                false, false, boost);
    }
    return f;
  }
  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    ArrayList<ValueSource> vs = new ArrayList<ValueSource>(dimension);
    for (int i=0; i<dimension; i++) {
      SchemaField sub = subField(field, i);
      vs.add(sub.getType().getValueSource(sub, parser));
    }
    return new PointTypeValueSource(field, vs);
  }
  @Override
  public Field createField(SchemaField field, String externalVal, float boost) {
    throw new UnsupportedOperationException("PointType uses multiple fields.  field=" + field.getName());
  }
  @Override
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeStr(name, f.stringValue());
  }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeStr(name, f.stringValue(), false);
  }
  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not suported on PointType " + field.getName());
  }
  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    String[] p1 = DistanceUtils.parsePoint(null, part1, dimension);
    String[] p2 = DistanceUtils.parsePoint(null, part2, dimension);
    BooleanQuery result = new BooleanQuery(true);
    for (int i = 0; i < dimension; i++) {
      SchemaField subSF = subField(field, i);
      result.add(subSF.getType().getRangeQuery(parser, subSF, p1[i], p2[i], minInclusive, maxInclusive), BooleanClause.Occur.MUST);
    }
    return result;
  }
  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    String[] p1 = DistanceUtils.parsePoint(null, externalVal, dimension);
    BooleanQuery bq = new BooleanQuery(true);
    for (int i = 0; i < dimension; i++) {
      SchemaField sf = subField(field, i);
      Query tq = sf.getType().getFieldQuery(parser, sf, p1[i]);
      bq.add(tq, BooleanClause.Occur.MUST);
    }
    return bq;
  }
}
class PointTypeValueSource extends VectorValueSource {
  private final SchemaField sf;
  public PointTypeValueSource(SchemaField sf, List<ValueSource> sources) {
    super(sources);
    this.sf = sf;
  }
  @Override
  public String name() {
    return "point";
  }
  @Override
  public String description() {
    return name()+"("+sf.getName()+")";
  }
}