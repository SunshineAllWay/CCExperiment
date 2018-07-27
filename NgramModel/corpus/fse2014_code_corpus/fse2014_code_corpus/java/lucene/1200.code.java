package org.apache.lucene.queryParser.standard.parser;
import java.util.Locale;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryParser.core.util.UnescapedCharSequence;
public class EscapeQuerySyntaxImpl implements EscapeQuerySyntax {
  private static final char[] wildcardChars = { '*', '?' };
  private static final String[] escapableTermExtraFirstChars = { "+", "-", "@" };
  private static final String[] escapableTermChars = { "\"", "<", ">", "=",
      "!", "(", ")", "^", "[", "{", ":", "]", "}", "~" };
  private static final String[] escapableQuotedChars = { "\"" };
  private static final String[] escapableWhiteChars = { " ", "\t", "\n", "\r",
      "\f", "\b", "\u3000" };
  private static final String[] escapableWordTokens = { "AND", "OR", "NOT",
      "TO", "WITHIN", "SENTENCE", "PARAGRAPH", "INORDER" };
  private static final CharSequence escapeChar(CharSequence str, Locale locale) {
    if (str == null || str.length() == 0)
      return str;
    CharSequence buffer = str;
    for (int i = 0; i < escapableTermChars.length; i++) {
      buffer = replaceIgnoreCase(buffer, escapableTermChars[i].toLowerCase(),
          "\\", locale);
    }
    for (int i = 0; i < escapableTermExtraFirstChars.length; i++) {
      if (buffer.charAt(0) == escapableTermExtraFirstChars[i].charAt(0)) {
        buffer = "\\" + buffer.charAt(0)
            + buffer.subSequence(1, buffer.length());
        break;
      }
    }
    return buffer;
  }
  private final CharSequence escapeQuoted(CharSequence str, Locale locale) {
    if (str == null || str.length() == 0)
      return str;
    CharSequence buffer = str;
    for (int i = 0; i < escapableQuotedChars.length; i++) {
      buffer = replaceIgnoreCase(buffer, escapableTermChars[i].toLowerCase(),
          "\\", locale);
    }
    return buffer;
  }
  private static final CharSequence escapeTerm(CharSequence term, Locale locale) {
    if (term == null)
      return term;
    term = escapeChar(term, locale);
    term = escapeWhiteChar(term, locale);
    for (int i = 0; i < escapableWordTokens.length; i++) {
      if (escapableWordTokens[i].equalsIgnoreCase(term.toString()))
        return "\\" + term;
    }
    return term;
  }
  private static CharSequence replaceIgnoreCase(CharSequence string,
      CharSequence sequence1, CharSequence escapeChar, Locale locale) {
    if (escapeChar == null || sequence1 == null || string == null)
      throw new NullPointerException();
    int count = string.length();
    int sequence1Length = sequence1.length();
    if (sequence1Length == 0) {
      StringBuilder result = new StringBuilder((count + 1)
          * escapeChar.length());
      result.append(escapeChar);
      for (int i = 0; i < count; i++) {
        result.append(string.charAt(i));
        result.append(escapeChar);
      }
      return result.toString();
    }
    StringBuilder result = new StringBuilder();
    char first = sequence1.charAt(0);
    int start = 0, copyStart = 0, firstIndex;
    while (start < count) {
      if ((firstIndex = string.toString().toLowerCase(locale).indexOf(first,
          start)) == -1)
        break;
      boolean found = true;
      if (sequence1.length() > 1) {
        if (firstIndex + sequence1Length > count)
          break;
        for (int i = 1; i < sequence1Length; i++) {
          if (string.toString().toLowerCase(locale).charAt(firstIndex + i) != sequence1
              .charAt(i)) {
            found = false;
            break;
          }
        }
      }
      if (found) {
        result.append(string.toString().substring(copyStart, firstIndex));
        result.append(escapeChar);
        result.append(string.toString().substring(firstIndex,
            firstIndex + sequence1Length));
        copyStart = start = firstIndex + sequence1Length;
      } else {
        start = firstIndex + 1;
      }
    }
    if (result.length() == 0 && copyStart == 0)
      return string;
    result.append(string.toString().substring(copyStart));
    return result.toString();
  }
  private static final CharSequence escapeWhiteChar(CharSequence str,
      Locale locale) {
    if (str == null || str.length() == 0)
      return str;
    CharSequence buffer = str;
    for (int i = 0; i < escapableWhiteChars.length; i++) {
      buffer = replaceIgnoreCase(buffer, escapableWhiteChars[i].toLowerCase(),
          "\\", locale);
    }
    return buffer;
  }
  public CharSequence escape(CharSequence text, Locale locale, Type type) {
    if (text == null || text.length() == 0)
      return text;
    if (text instanceof UnescapedCharSequence) {
      text = ((UnescapedCharSequence) text).toStringEscaped(wildcardChars);
    } else {
      text = new UnescapedCharSequence(text).toStringEscaped(wildcardChars);
    }
    if (type == Type.STRING) {
      return escapeQuoted(text, locale);
    } else {
      return escapeTerm(text, locale);
    }
  }
  public static UnescapedCharSequence discardEscapeChar(CharSequence input)
      throws ParseException {
    char[] output = new char[input.length()];
    boolean[] wasEscaped = new boolean[input.length()];
    int length = 0;
    boolean lastCharWasEscapeChar = false;
    int codePointMultiplier = 0;
    int codePoint = 0;
    for (int i = 0; i < input.length(); i++) {
      char curChar = input.charAt(i);
      if (codePointMultiplier > 0) {
        codePoint += hexToInt(curChar) * codePointMultiplier;
        codePointMultiplier >>>= 4;
        if (codePointMultiplier == 0) {
          output[length++] = (char) codePoint;
          codePoint = 0;
        }
      } else if (lastCharWasEscapeChar) {
        if (curChar == 'u') {
          codePointMultiplier = 16 * 16 * 16;
        } else {
          output[length] = curChar;
          wasEscaped[length] = true;
          length++;
        }
        lastCharWasEscapeChar = false;
      } else {
        if (curChar == '\\') {
          lastCharWasEscapeChar = true;
        } else {
          output[length] = curChar;
          length++;
        }
      }
    }
    if (codePointMultiplier > 0) {
      throw new ParseException(new MessageImpl(
          QueryParserMessages.INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION));
    }
    if (lastCharWasEscapeChar) {
      throw new ParseException(new MessageImpl(
          QueryParserMessages.INVALID_SYNTAX_ESCAPE_CHARACTER));
    }
    return new UnescapedCharSequence(output, wasEscaped, 0, length);
  }
  private static final int hexToInt(char c) throws ParseException {
    if ('0' <= c && c <= '9') {
      return c - '0';
    } else if ('a' <= c && c <= 'f') {
      return c - 'a' + 10;
    } else if ('A' <= c && c <= 'F') {
      return c - 'A' + 10;
    } else {
      throw new ParseException(new MessageImpl(
          QueryParserMessages.INVALID_SYNTAX_ESCAPE_NONE_HEX_UNICODE, c));
    }
  }
}
