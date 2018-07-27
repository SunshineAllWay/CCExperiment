package org.apache.solr.common.util;
import java.io.File;
import java.io.FileFilter;
import java.util.regex.*;
public final class RegexFileFilter implements FileFilter {
  final Pattern pattern;
  public RegexFileFilter(String regex) {
    this(Pattern.compile(regex));
  }
  public RegexFileFilter(Pattern regex) {
    pattern = regex;
  }
  public boolean accept(File f) {
    return pattern.matcher(f.getName()).matches();
  }
  public String toString() {
    return "regex:" + pattern.toString();
  }
}
