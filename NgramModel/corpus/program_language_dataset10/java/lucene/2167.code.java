package org.apache.solr.common.params;
public interface HighlightParams {
  public static final String HIGHLIGHT   = "hl";
  public static final String FIELDS      = HIGHLIGHT+".fl";
  public static final String SNIPPETS    = HIGHLIGHT+".snippets";
  public static final String FRAGSIZE    = HIGHLIGHT+".fragsize";
  public static final String INCREMENT   = HIGHLIGHT+".increment";
  public static final String MAX_CHARS   = HIGHLIGHT+".maxAnalyzedChars";
  public static final String FORMATTER   = HIGHLIGHT+".formatter";
  public static final String FRAGMENTER  = HIGHLIGHT+".fragmenter";
  public static final String FRAG_LIST_BUILDER = HIGHLIGHT+".fragListBuilder";
  public static final String FRAGMENTS_BUILDER = HIGHLIGHT+".fragmentsBuilder";
  public static final String FIELD_MATCH = HIGHLIGHT+".requireFieldMatch";
  public static final String ALTERNATE_FIELD = HIGHLIGHT+".alternateField";
  public static final String ALTERNATE_FIELD_LENGTH = HIGHLIGHT+".maxAlternateFieldLength";
  public static final String USE_PHRASE_HIGHLIGHTER = HIGHLIGHT+".usePhraseHighlighter";
  public static final String HIGHLIGHT_MULTI_TERM = HIGHLIGHT+".highlightMultiTerm";
  public static final String MERGE_CONTIGUOUS_FRAGMENTS = HIGHLIGHT + ".mergeContiguous";
  public static final String USE_FVH = HIGHLIGHT + ".useFastVectorHighlighter";
  public static final String SIMPLE = "simple";
  public static final String SIMPLE_PRE  = HIGHLIGHT+"."+SIMPLE+".pre";
  public static final String SIMPLE_POST = HIGHLIGHT+"."+SIMPLE+".post";
  public static final String REGEX = "regex";
  public static final String SLOP  = HIGHLIGHT+"."+REGEX+".slop";
  public static final String PATTERN  = HIGHLIGHT+"."+REGEX+".pattern";
  public static final String MAX_RE_CHARS   = HIGHLIGHT+"."+REGEX+".maxAnalyzedChars";
}
