package org.apache.lucene.benchmark.byTask.tasks;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class NewLocaleTask extends PerfTask {
  private String language;
  private String country;
  private String variant;
  public NewLocaleTask(PerfRunData runData) {
    super(runData);
  }
  static Locale createLocale(String language, String country, String variant) {
    if (language == null || language.length() == 0) 
      return null;
    String lang = language;
    if (lang.equalsIgnoreCase("ROOT"))
      lang = ""; 
    return new Locale(lang, country, variant);
  }
  @Override
  public int doLogic() throws Exception {
    Locale locale = createLocale(language, country, variant);
    getRunData().setLocale(locale);
    System.out.println("Changed Locale to: " + 
        (locale == null ? "null" : 
        (locale.getDisplayName().length() == 0) ? "root locale" : locale));
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    language = country = variant = "";
    StringTokenizer st = new StringTokenizer(params, ",");
    if (st.hasMoreTokens())
      language = st.nextToken();
    if (st.hasMoreTokens())
      country = st.nextToken();
    if (st.hasMoreTokens())
      variant = st.nextToken();
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
