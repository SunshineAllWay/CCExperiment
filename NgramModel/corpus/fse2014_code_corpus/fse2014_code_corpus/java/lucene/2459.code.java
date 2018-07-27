package org.apache.solr.schema;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.distance.DistanceUtils;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class SpatialTileField extends AbstractSubTypeFieldType implements ResourceLoaderAware {
  public static final String START_LEVEL = "start";
  public static final String END_LEVEL = "end";
  public static final String PROJECTOR_CLASS = "projector";
  private static final int DEFAULT_END_LEVEL = 15;
  private static final int DEFAULT_START_LEVEL = 4;
  private int start = DEFAULT_START_LEVEL, end = DEFAULT_END_LEVEL;
  private int tileDiff;
  private String projectorName;
  protected List<CartesianTierPlotter> plotters;
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    SolrParams p = new MapSolrParams(args);
    start = p.getInt(START_LEVEL, DEFAULT_START_LEVEL);
    end = p.getInt(END_LEVEL, DEFAULT_END_LEVEL);
    if (end < start) {
      int tmp = start;
      start = end;
      end = tmp;
    }
    args.remove(START_LEVEL);
    args.remove(END_LEVEL);
    projectorName = p.get(PROJECTOR_CLASS, SinusoidalProjector.class.getName());
    args.remove(PROJECTOR_CLASS);
    super.init(schema, args);
    tileDiff = (end - start) + 1;
    createSuffixCache(tileDiff);
  }
  public void inform(ResourceLoader loader) {
    IProjector projector = (IProjector) loader.newInstance(projectorName);
    if (projector != null) {
      plotters = new ArrayList<CartesianTierPlotter>(tileDiff);
      for (int i = start; i <= end; i++) {
        plotters.add(new CartesianTierPlotter(i, projector, ""));
      }
    } else {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Could not instantiate a Projector Instance for: "
              + projectorName + ". Make sure the " + PROJECTOR_CLASS + " attribute is set properly in the schema");
    }
  }
  @Override
  public Fieldable[] createFields(SchemaField field, String externalVal, float boost) {
    Fieldable[] f = new Fieldable[(field.indexed() ? tileDiff : 0) + (field.stored() ? 1 : 0)];
    if (field.indexed()) {
      int i = 0;
      double[] latLon = DistanceUtils.parseLatitudeLongitude(null, externalVal);
      for (CartesianTierPlotter plotter : plotters) {
        double boxId = plotter.getTierBoxId(latLon[0], latLon[1]);
        f[i] = subField(field, i).createField(String.valueOf(boxId), boost);
        i++;
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
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive,
                             boolean maxInclusive) {
    BooleanQuery bq = new BooleanQuery(true);
    for (int i = 0; i < tileDiff; i++) {
      SchemaField sf = subField(field, i);
      Query tq = sf.getType().getRangeQuery(parser, sf, part1, part2, minInclusive, maxInclusive);
      bq.add(tq, BooleanClause.Occur.SHOULD);
    }
    return bq;
  }
  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    BooleanQuery bq = new BooleanQuery(true);
    for (int i = 0; i < tileDiff; i++) {
      SchemaField sf = subField(field, i);
      Query tq = sf.getType().getFieldQuery(parser, sf, externalVal);
      bq.add(tq, BooleanClause.Occur.SHOULD);
    }
    return bq;
  }
  @Override
  public boolean isPolyField() {
    return true;
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
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on SpatialTileField " + field.getName());
  }
  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    throw new UnsupportedOperationException("SpatialTileField uses multiple fields and does not support ValueSource");
  }
  @Override
  public Field createField(SchemaField field, String externalVal, float boost) {
    throw new UnsupportedOperationException("SpatialTileField uses multiple fields.  field=" + field.getName());
  }
}
