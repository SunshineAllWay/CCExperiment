package org.apache.lucene.analysis.fa;
public class PersianNormalizer {
  public static final char YEH = '\u064A';
  public static final char FARSI_YEH = '\u06CC';
  public static final char YEH_BARREE = '\u06D2';
  public static final char KEHEH = '\u06A9';
  public static final char KAF = '\u0643';
  public static final char HAMZA_ABOVE = '\u0654';
  public static final char HEH_YEH = '\u06C0';
  public static final char HEH_GOAL = '\u06C1';
  public static final char HEH = '\u0647';
  public int normalize(char s[], int len) {
    for (int i = 0; i < len; i++) {
      switch (s[i]) {
      case FARSI_YEH:
      case YEH_BARREE:
        s[i] = YEH;
        break;
      case KEHEH:
        s[i] = KAF;
        break;
      case HEH_YEH:
      case HEH_GOAL:
        s[i] = HEH;
        break;
      case HAMZA_ABOVE: 
        len = delete(s, i, len);
        i--;
        break;
      default:
        break;
      }
    }
    return len;
  }
  protected int delete(char s[], int pos, int len) {
    if (pos < len)
      System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
    return len - 1;
  }
}
