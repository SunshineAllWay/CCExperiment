package org.apache.lucene.analysis.ar;
public class ArabicStemmer {
  public static final char ALEF = '\u0627';
  public static final char BEH = '\u0628';
  public static final char TEH_MARBUTA = '\u0629';
  public static final char TEH = '\u062A';
  public static final char FEH = '\u0641';
  public static final char KAF = '\u0643';
  public static final char LAM = '\u0644';
  public static final char NOON = '\u0646';
  public static final char HEH = '\u0647';
  public static final char WAW = '\u0648';
  public static final char YEH = '\u064A';
  public static final char prefixes[][] = {
      ("" + ALEF + LAM).toCharArray(), 
      ("" + WAW + ALEF + LAM).toCharArray(), 
      ("" + BEH + ALEF + LAM).toCharArray(),
      ("" + KAF + ALEF + LAM).toCharArray(),
      ("" + FEH + ALEF + LAM).toCharArray(),
      ("" + LAM + LAM).toCharArray(),
      ("" + WAW).toCharArray(),
  };
  public static final char suffixes[][] = {
    ("" + HEH + ALEF).toCharArray(), 
    ("" + ALEF + NOON).toCharArray(), 
    ("" + ALEF + TEH).toCharArray(), 
    ("" + WAW + NOON).toCharArray(), 
    ("" + YEH + NOON).toCharArray(), 
    ("" + YEH + HEH).toCharArray(),
    ("" + YEH + TEH_MARBUTA).toCharArray(),
    ("" + HEH).toCharArray(),
    ("" + TEH_MARBUTA).toCharArray(),
    ("" + YEH).toCharArray(),
};
  public int stem(char s[], int len) {
    len = stemPrefix(s, len);
    len = stemSuffix(s, len);
    return len;
  }
  public int stemPrefix(char s[], int len) {
    for (int i = 0; i < prefixes.length; i++) 
      if (startsWith(s, len, prefixes[i]))
        return deleteN(s, 0, len, prefixes[i].length);
    return len;
  }
  public int stemSuffix(char s[], int len) {
    for (int i = 0; i < suffixes.length; i++) 
      if (endsWith(s, len, suffixes[i]))
        len = deleteN(s, len - suffixes[i].length, len, suffixes[i].length);
    return len;
  }
  boolean startsWith(char s[], int len, char prefix[]) {
    if (prefix.length == 1 && len < 4) { 
      return false;
    } else if (len < prefix.length + 2) { 
      return false;
    } else {
      for (int i = 0; i < prefix.length; i++)
        if (s[i] != prefix[i])
          return false;
      return true;
    }
  }
  boolean endsWith(char s[], int len, char suffix[]) {
    if (len < suffix.length + 2) { 
      return false;
    } else {
      for (int i = 0; i < suffix.length; i++)
        if (s[len - suffix.length + i] != suffix[i])
          return false;
      return true;
    }
  }
  protected int deleteN(char s[], int pos, int len, int nChars) {
    for (int i = 0; i < nChars; i++)
      len = delete(s, pos, len);
    return len;
  }
  protected int delete(char s[], int pos, int len) {
    if (pos < len) 
      System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
    return len - 1;
  }
}
