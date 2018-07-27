package org.apache.solr.search;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public abstract class QParserPlugin implements NamedListInitializedPlugin {
  public static String DEFAULT_QTYPE = LuceneQParserPlugin.NAME;
  public static final Object[] standardPlugins = {
    LuceneQParserPlugin.NAME, LuceneQParserPlugin.class,
    OldLuceneQParserPlugin.NAME, OldLuceneQParserPlugin.class,
    FunctionQParserPlugin.NAME, FunctionQParserPlugin.class,
    PrefixQParserPlugin.NAME, PrefixQParserPlugin.class,
    BoostQParserPlugin.NAME, BoostQParserPlugin.class,
    DisMaxQParserPlugin.NAME, DisMaxQParserPlugin.class,
    ExtendedDismaxQParserPlugin.NAME, ExtendedDismaxQParserPlugin.class,
    FieldQParserPlugin.NAME, FieldQParserPlugin.class,
    RawQParserPlugin.NAME, RawQParserPlugin.class,
    NestedQParserPlugin.NAME, NestedQParserPlugin.class,
    FunctionRangeQParserPlugin.NAME, FunctionRangeQParserPlugin.class,
  };
  public abstract QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req);
}
