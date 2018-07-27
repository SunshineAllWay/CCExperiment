package org.apache.solr.handler.clustering.carrot2;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.common.params.HighlightParams;
public interface CarrotParams {
  String CARROT_PREFIX = "carrot.";
  String ALGORITHM = CARROT_PREFIX + "algorithm";
  String TITLE_FIELD_NAME = CARROT_PREFIX + "title";
  String URL_FIELD_NAME = CARROT_PREFIX + "url";
  String SNIPPET_FIELD_NAME = CARROT_PREFIX + "snippet";
  String PRODUCE_SUMMARY = CARROT_PREFIX + "produceSummary";
  String NUM_DESCRIPTIONS = CARROT_PREFIX + "numDescriptions";
  String OUTPUT_SUB_CLUSTERS = CARROT_PREFIX + "outputSubClusters";
  String SUMMARY_FRAGSIZE = CARROT_PREFIX + "fragzise";
  public static final Set<String> CARROT_PARAM_NAMES = ImmutableSet.of(
          ALGORITHM, TITLE_FIELD_NAME, URL_FIELD_NAME, SNIPPET_FIELD_NAME,
          PRODUCE_SUMMARY, NUM_DESCRIPTIONS, OUTPUT_SUB_CLUSTERS, SUMMARY_FRAGSIZE);
}
