package org.apache.lucene;
public final class LucenePackage {
  private LucenePackage() {}                      
  public static Package get() {
    return LucenePackage.class.getPackage();
  }
}
