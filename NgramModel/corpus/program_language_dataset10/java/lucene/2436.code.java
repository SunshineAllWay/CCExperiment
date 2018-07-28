package org.apache.solr.schema;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.MapSolrParams;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
public abstract class CoordinateFieldType extends AbstractSubTypeFieldType {
  protected int dimension;
  public static final int DEFAULT_DIMENSION = 2;
  public static final String DIMENSION = "dimension";
  public int getDimension() {
    return dimension;
  }
}
