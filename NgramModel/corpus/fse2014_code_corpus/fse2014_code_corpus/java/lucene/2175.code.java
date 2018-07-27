package org.apache.solr.common.params;
public interface SpellingParams {
  public static final String SPELLCHECK_PREFIX = "spellcheck.";
  public static final String SPELLCHECK_DICT = SPELLCHECK_PREFIX + "dictionary";
  public static final String SPELLCHECK_COUNT = SPELLCHECK_PREFIX + "count";
  public static final String SPELLCHECK_ONLY_MORE_POPULAR = SPELLCHECK_PREFIX + "onlyMorePopular";
  public static final String SPELLCHECK_EXTENDED_RESULTS = SPELLCHECK_PREFIX + "extendedResults";
  public static final String SPELLCHECK_Q = SPELLCHECK_PREFIX + "q";
  public static final String SPELLCHECK_BUILD = SPELLCHECK_PREFIX + "build";
  public static final String SPELLCHECK_RELOAD = SPELLCHECK_PREFIX + "reload";
  public static final String SPELLCHECK_COLLATE = SPELLCHECK_PREFIX + "collate";
}
