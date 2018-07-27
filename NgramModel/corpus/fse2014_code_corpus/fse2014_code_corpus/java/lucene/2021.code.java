package org.apache.lucene.util;
import java.util.Locale;
import java.util.Set;
public abstract class LocalizedTestCase extends LuceneTestCase {
  private final Locale defaultLocale = Locale.getDefault();
  private Locale locale;
  private final Set<String> testWithDifferentLocales;
  public LocalizedTestCase() {
    super();
    testWithDifferentLocales = null;
  }
  public LocalizedTestCase(String name) {
    super(name);
    testWithDifferentLocales = null;
  }
  public LocalizedTestCase(Set<String> testWithDifferentLocales) {
    super();
    this.testWithDifferentLocales = testWithDifferentLocales;
  }
  public LocalizedTestCase(String name, Set<String> testWithDifferentLocales) {
    super(name);
    this.testWithDifferentLocales = testWithDifferentLocales;
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(locale);
  }
  @Override
  protected void tearDown() throws Exception {
    assertEquals("default locale unexpectedly changed:", locale, Locale
        .getDefault());
    Locale.setDefault(defaultLocale);
    super.tearDown();
  }
  @Override
  public void runBare() throws Throwable {
    try {
      locale = defaultLocale;
      super.runBare();
    } catch (Throwable e) {
      System.out.println("Test failure of '" + getName()
          + "' occurred with the default Locale " + locale);
      throw e;
    }
    if (testWithDifferentLocales == null
        || testWithDifferentLocales.contains(getName())) {
      Locale systemLocales[] = Locale.getAvailableLocales();
      for (int i = 0; i < systemLocales.length; i++) {
        try {
          locale = systemLocales[i];
          super.runBare();
        } catch (Throwable e) {
          System.out.println("Test failure of '" + getName()
              + "' occurred under a different Locale " + locale);
          throw e;
        }
      }
    }
  }
}
