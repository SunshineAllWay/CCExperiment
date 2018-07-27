package org.apache.solr.common.params;
public interface MoreLikeThisParams 
{
  public final static String MLT = "mlt";
  public final static String PREFIX = "mlt.";
  public final static String SIMILARITY_FIELDS     = PREFIX + "fl";
  public final static String MIN_TERM_FREQ         = PREFIX + "mintf";
  public final static String MIN_DOC_FREQ          = PREFIX + "mindf";
  public final static String MIN_WORD_LEN          = PREFIX + "minwl";
  public final static String MAX_WORD_LEN          = PREFIX + "maxwl";
  public final static String MAX_QUERY_TERMS       = PREFIX + "maxqt";
  public final static String MAX_NUM_TOKENS_PARSED = PREFIX + "maxntp";
  public final static String BOOST                 = PREFIX + "boost"; 
  public final static String QF                    = PREFIX + "qf"; 
  public final static String DOC_COUNT = PREFIX + "count";
  public final static String MATCH_INCLUDE = PREFIX + "match.include";
  public final static String MATCH_OFFSET  = PREFIX + "match.offset";
  public final static String INTERESTING_TERMS = PREFIX + "interestingTerms";  
  public enum TermStyle {
    NONE,
    LIST,
    DETAILS;
    public static TermStyle get( String p )
    {
      if( p != null ) {
        p = p.toUpperCase();
        if( p.equals( "DETAILS" ) ) {
          return DETAILS;
        }
        else if( p.equals( "LIST" ) ) {
          return LIST;
        }
      }
      return NONE; 
    }
  }
}
