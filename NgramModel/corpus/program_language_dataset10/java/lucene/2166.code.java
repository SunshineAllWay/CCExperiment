package org.apache.solr.common.params;
import org.apache.solr.common.SolrException;
public interface FacetParams {
  public static final String FACET = "facet";
  public static final String FACET_METHOD = FACET + ".method";
  public static final String FACET_METHOD_enum = "enum";
  public static final String FACET_METHOD_fc = "fc";
  public static final String FACET_QUERY = FACET + ".query";
  public static final String FACET_FIELD = FACET + ".field";
  public static final String FACET_OFFSET = FACET + ".offset";
  public static final String FACET_LIMIT = FACET + ".limit";
  public static final String FACET_MINCOUNT = FACET + ".mincount";
  public static final String FACET_ZEROS = FACET + ".zeros";
  public static final String FACET_MISSING = FACET + ".missing";
  public static final String FACET_SORT = FACET + ".sort";
  public static final String FACET_SORT_COUNT = "count";
  public static final String FACET_SORT_COUNT_LEGACY = "true";
  public static final String FACET_SORT_INDEX = "index";
  public static final String FACET_SORT_INDEX_LEGACY = "false";
  public static final String FACET_PREFIX = FACET + ".prefix";
  public static final String FACET_ENUM_CACHE_MINDF = FACET + ".enum.cache.minDf";
  public static final String FACET_DATE = FACET + ".date";
  public static final String FACET_DATE_START = FACET_DATE + ".start";
  public static final String FACET_DATE_END = FACET_DATE + ".end";
  public static final String FACET_DATE_GAP = FACET_DATE + ".gap";
  public static final String FACET_DATE_HARD_END = FACET_DATE + ".hardend";
  public static final String FACET_DATE_OTHER = FACET_DATE + ".other";
  public enum FacetDateOther {
    BEFORE, AFTER, BETWEEN, ALL, NONE;
    public String toString() { return super.toString().toLowerCase(); }
    public static FacetDateOther get(String label) {
      try {
        return valueOf(label.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           label+" is not a valid type of 'other' date facet information",e);
      }
    }
  }
}
