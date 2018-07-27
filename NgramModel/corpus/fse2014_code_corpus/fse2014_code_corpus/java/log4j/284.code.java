package org.apache.log4j.util;
public class MDCOrderFilter implements Filter {
  private static final String[] patterns =
          new String[] {
                  "{key2,va12}{key1,va11}",
                  "{key2,value2}{key1,value1}"
          };
  private static final String[] replacements =
            new String[] {
                    "{key1,va11}{key2,va12}",
                    "{key1,value1}{key2,value2}"
            };
  public String filter(final String in) {
    if (in == null) {
      return null;
    }
    for(int i = 0; i < patterns.length; i++) {
        int ipos = in.indexOf(patterns[i]);
        if (ipos >= 1) {
            return in.substring(0, ipos)
                    + replacements[i]
                    + in.substring(ipos + patterns[i].length());
        }
    }
    return in;
  }
}
