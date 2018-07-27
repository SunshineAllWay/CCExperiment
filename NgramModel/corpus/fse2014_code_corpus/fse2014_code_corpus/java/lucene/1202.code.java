package org.apache.lucene.queryParser.standard.parser;
import org.apache.lucene.messages.Message;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeParseException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
public class ParseException extends QueryNodeParseException {
  private static final long serialVersionUID = 1L;
  public ParseException(Token currentTokenVal,
      int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
    super(new MessageImpl(QueryParserMessages.INVALID_SYNTAX, initialise(
        currentTokenVal, expectedTokenSequencesVal, tokenImageVal)));
    this.currentToken = currentTokenVal;
    this.expectedTokenSequences = expectedTokenSequencesVal;
    this.tokenImage = tokenImageVal;
  }
  public ParseException(Message message) {
    super(message);
  }
  public ParseException() {
    super(new MessageImpl(QueryParserMessages.INVALID_SYNTAX, "Error"));
  }
  @SuppressWarnings("unused")
  private Token currentToken;
  @SuppressWarnings("unused")
  private int[][] expectedTokenSequences;
  @SuppressWarnings("unused")
  private String[] tokenImage;
  private static String initialise(Token currentToken,
      int[][] expectedTokenSequences, String[] tokenImage) {
    String eol = System.getProperty("line.separator", "\n");
    StringBuffer expected = new StringBuffer();
    int maxSize = 0;
    for (int i = 0; i < expectedTokenSequences.length; i++) {
      if (maxSize < expectedTokenSequences[i].length) {
        maxSize = expectedTokenSequences[i].length;
      }
      for (int j = 0; j < expectedTokenSequences[i].length; j++) {
        expected.append(tokenImage[expectedTokenSequences[i][j]]).append(' ');
      }
      if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {
        expected.append("...");
      }
      expected.append(eol).append("    ");
    }
    String retval = "Encountered \"";
    Token tok = currentToken.next;
    for (int i = 0; i < maxSize; i++) {
      if (i != 0)
        retval += " ";
      if (tok.kind == 0) {
        retval += tokenImage[0];
        break;
      }
      retval += " " + tokenImage[tok.kind];
      retval += " \"";
      retval += add_escapes(tok.image);
      retval += " \"";
      tok = tok.next;
    }
    retval += "\" at line " + currentToken.next.beginLine + ", column "
        + currentToken.next.beginColumn;
    retval += "." + eol;
    if (expectedTokenSequences.length == 1) {
      retval += "Was expecting:" + eol + "    ";
    } else {
      retval += "Was expecting one of:" + eol + "    ";
    }
    retval += expected.toString();
    return retval;
  }
  @SuppressWarnings("unused")
  private String eol = System.getProperty("line.separator", "\n");
  static private String add_escapes(String str) {
    StringBuffer retval = new StringBuffer();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i)) {
      case 0:
        continue;
      case '\b':
        retval.append("\\b");
        continue;
      case '\t':
        retval.append("\\t");
        continue;
      case '\n':
        retval.append("\\n");
        continue;
      case '\f':
        retval.append("\\f");
        continue;
      case '\r':
        retval.append("\\r");
        continue;
      case '\"':
        retval.append("\\\"");
        continue;
      case '\'':
        retval.append("\\\'");
        continue;
      case '\\':
        retval.append("\\\\");
        continue;
      default:
        if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
          String s = "0000" + Integer.toString(ch, 16);
          retval.append("\\u" + s.substring(s.length() - 4, s.length()));
        } else {
          retval.append(ch);
        }
        continue;
      }
    }
    return retval.toString();
  }
}
