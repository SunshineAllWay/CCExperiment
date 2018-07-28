package org.apache.lucene.queryParser.core.parser;
import java.util.Locale;
public interface EscapeQuerySyntax {
  public enum Type {
    STRING, NORMAL;
  }
  CharSequence escape(CharSequence text, Locale locale, Type type);
}
