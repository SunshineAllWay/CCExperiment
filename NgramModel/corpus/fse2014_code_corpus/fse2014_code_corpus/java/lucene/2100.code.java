package org.apache.solr.handler.dataimport;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TemplateString {
  private List<String> variables = new ArrayList<String>();
  private List<String> pcs = new ArrayList<String>();
  private Map<String, TemplateString> cache;
  public TemplateString() {
    cache = new ConcurrentHashMap<String, TemplateString>();
  }
  private TemplateString(String s) {
    Matcher m = WORD_PATTERN.matcher(s);
    int idx = 0;
    while (m.find()) {
      String aparam = s.substring(m.start() + 2, m.end() - 1);
      variables.add(aparam);
      pcs.add(s.substring(idx, m.start()));
      idx = m.end();
    }
    pcs.add(s.substring(idx));
  }
  public String replaceTokens(String string, VariableResolver resolver) {
    if (string == null)
      return null;
    TemplateString ts = cache.get(string);
    if (ts == null) {
      ts = new TemplateString(string);
      cache.put(string, ts);
    }
    return ts.fillTokens(resolver);
  }
  private String fillTokens(VariableResolver resolver) {
    String[] s = new String[variables.size()];
    for (int i = 0; i < variables.size(); i++) {
      Object val = resolver.resolve(variables.get(i));
      s[i] = val == null ? "" : getObjectAsString(val);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pcs.size(); i++) {
      sb.append(pcs.get(i));
      if (i < s.length) {
        sb.append(s[i]);
      }
    }
    return sb.toString();
  }
  private String getObjectAsString(Object val) {
    if (val instanceof Date) {
      Date d = (Date) val;
      return DataImporter.DATE_TIME_FORMAT.get().format(d);
    }
    return val.toString();
  }
  public static List<String> getVariables(String s) {
    return new TemplateString(s).variables;
  }
  static final Pattern WORD_PATTERN = Pattern.compile("(\\$\\{.*?\\})");
}
