package org.apache.lucene.queryParser.surround.query;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class SrndTruncQuery extends SimpleTerm {
  public SrndTruncQuery(String truncated, char unlimited, char mask) {
    super(false); 
    this.truncated = truncated;
    this.unlimited = unlimited;
    this.mask = mask;
    truncatedToPrefixAndPattern();
  }
  private final String truncated;
  private final char unlimited;
  private final char mask;
  private String prefix;
  private Pattern pattern;
  public String getTruncated() {return truncated;}
  @Override
  public String toStringUnquoted() {return getTruncated();}
  protected boolean matchingChar(char c) {
    return (c != unlimited) && (c != mask);
  }
  protected void appendRegExpForChar(char c, StringBuilder re) {
    if (c == unlimited)
      re.append(".*");
    else if (c == mask)
      re.append(".");
    else
      re.append(c);
  }
  protected void truncatedToPrefixAndPattern() {
    int i = 0;
    while ((i < truncated.length()) && matchingChar(truncated.charAt(i))) {
      i++;
    }
    prefix = truncated.substring(0, i);
    StringBuilder re = new StringBuilder();
    while (i < truncated.length()) {
      appendRegExpForChar(truncated.charAt(i), re);
      i++;
    }
    pattern = Pattern.compile(re.toString());
  }
  @Override
  public void visitMatchingTerms(
    IndexReader reader,
    String fieldName,
    MatchingTermVisitor mtv) throws IOException
  {
    int prefixLength = prefix.length();
    TermEnum enumerator = reader.terms(new Term(fieldName, prefix));
    Matcher matcher = pattern.matcher("");
    try {
      do {
        Term term = enumerator.term();
        if (term != null) {
          String text = term.text();
          if ((! text.startsWith(prefix)) || (! term.field().equals(fieldName))) {
            break;
          } else {
            matcher.reset( text.substring(prefixLength));
            if (matcher.matches()) {
              mtv.visitMatchingTerm(term);
            }
          }
        }
      } while (enumerator.next());
    } finally {
      enumerator.close();
      matcher.reset();
    }
  }
}
