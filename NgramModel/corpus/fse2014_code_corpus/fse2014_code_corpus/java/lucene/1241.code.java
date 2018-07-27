package org.apache.lucene.search.regex;
import org.apache.regexp.RE;
import org.apache.regexp.RegexpTunnel;
public class JakartaRegexpCapabilities implements RegexCapabilities {
  private RE regexp;
  private int flags = RE.MATCH_NORMAL;
  public static final int FLAG_MATCH_NORMAL = RE.MATCH_NORMAL;
  public static final int FLAG_MATCH_CASEINDEPENDENT = RE.MATCH_CASEINDEPENDENT;
  public JakartaRegexpCapabilities() {}
  public JakartaRegexpCapabilities(int flags)
  {
    this.flags = flags;
  }
  public void compile(String pattern) {
    regexp = new RE(pattern, this.flags);
  }
  public boolean match(String string) {
    return regexp.match(string);
  }
  public String prefix() {
    char[] prefix = RegexpTunnel.getPrefix(regexp);
    return prefix == null ? null : new String(prefix);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final JakartaRegexpCapabilities that = (JakartaRegexpCapabilities) o;
    if (regexp != null ? !regexp.equals(that.regexp) : that.regexp != null) return false;
    return true;
  }
  @Override
  public int hashCode() {
    return (regexp != null ? regexp.hashCode() : 0);
  }
}
