package org.apache.solr.spelling;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public class SpellingQueryConverter extends QueryConverter  {
  final static String[] NAMESTARTCHAR_PARTS = {
          "A-Z_a-z", "\\xc0-\\xd6", "\\xd8-\\xf6", "\\xf8-\\u02ff",
          "\\u0370-\\u037d", "\\u037f-\\u1fff",
          "\\u200c-\\u200d", "\\u2070-\\u218f",
          "\\u2c00-\\u2fef", "\\u2001-\\ud7ff",
          "\\uf900-\\ufdcf", "\\ufdf0-\\ufffd"
  };
  final static String[] ADDITIONAL_NAMECHAR_PARTS = {
          "\\-.0-9\\xb7", "\\u0300-\\u036f", "\\u203f-\\u2040"
  };
  final static String SURROGATE_PAIR = "\\p{Cs}{2}";
  final static String NMTOKEN;
  static {
    StringBuilder sb = new StringBuilder();
    for (String part : NAMESTARTCHAR_PARTS)
      sb.append(part);
    for (String part : ADDITIONAL_NAMECHAR_PARTS)
      sb.append(part);
    NMTOKEN = "([" + sb.toString() + "]|" + SURROGATE_PAIR + ")+";
  }
  final static String PATTERN = "(?:(?!(" + NMTOKEN + ":|\\d+)))[\\p{L}_\\-0-9]+";
  protected Pattern QUERY_REGEX = Pattern.compile(PATTERN);
  public Collection<Token> convert(String original) {
    if (original == null) { 
      return Collections.emptyList();
    }
    Collection<Token> result = new ArrayList<Token>();
    Matcher matcher = QUERY_REGEX.matcher(original);
    TokenStream stream;
    while (matcher.find()) {
      String word = matcher.group(0);
      if (word.equals("AND") == false && word.equals("OR") == false) {
        try {
          stream = analyzer.reusableTokenStream("", new StringReader(word));
          TermAttribute termAtt = (TermAttribute) stream.addAttribute(TermAttribute.class);
          FlagsAttribute flagsAtt = (FlagsAttribute) stream.addAttribute(FlagsAttribute.class);
          TypeAttribute typeAtt = (TypeAttribute) stream.addAttribute(TypeAttribute.class);
          PayloadAttribute payloadAtt = (PayloadAttribute) stream.addAttribute(PayloadAttribute.class);
          PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) stream.addAttribute(PositionIncrementAttribute.class);
          stream.reset();
          while (stream.incrementToken()) {
            Token token = new Token();
            token.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
            token.setStartOffset(matcher.start());
            token.setEndOffset(matcher.end());
            token.setFlags(flagsAtt.getFlags());
            token.setType(typeAtt.type());
            token.setPayload(payloadAtt.getPayload());
            token.setPositionIncrement(posIncAtt.getPositionIncrement());
            result.add(token);
          }
        } catch (IOException e) {
        }
      }
    }
    return result;
  }
}
