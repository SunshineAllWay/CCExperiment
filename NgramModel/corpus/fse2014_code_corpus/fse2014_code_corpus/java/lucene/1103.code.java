package org.apache.lucene.queryParser.core.messages;
import org.apache.lucene.messages.NLS;
public class QueryParserMessages extends NLS {
  private static final String BUNDLE_NAME = QueryParserMessages.class.getName();
  private QueryParserMessages() {
  }
  static {
    NLS.initializeMessages(BUNDLE_NAME, QueryParserMessages.class);
  }
  public static String INVALID_SYNTAX;
  public static String INVALID_SYNTAX_CANNOT_PARSE;
  public static String INVALID_SYNTAX_FUZZY_LIMITS;
  public static String INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION;
  public static String INVALID_SYNTAX_ESCAPE_CHARACTER;
  public static String INVALID_SYNTAX_ESCAPE_NONE_HEX_UNICODE;
  public static String NODE_ACTION_NOT_SUPPORTED;
  public static String PARAMETER_VALUE_NOT_SUPPORTED;
  public static String LUCENE_QUERY_CONVERSION_ERROR;
  public static String EMPTY_MESSAGE;
  public static String WILDCARD_NOT_SUPPORTED;
  public static String TOO_MANY_BOOLEAN_CLAUSES;
  public static String LEADING_WILDCARD_NOT_ALLOWED;
}
