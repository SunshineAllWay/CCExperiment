package org.apache.lucene.analysis.ar;
public class ArabicNormalizer {
  public static final char ALEF = '\u0627';
  public static final char ALEF_MADDA = '\u0622';
  public static final char ALEF_HAMZA_ABOVE = '\u0623';
  public static final char ALEF_HAMZA_BELOW = '\u0625';
  public static final char YEH = '\u064A';
  public static final char DOTLESS_YEH = '\u0649';
  public static final char TEH_MARBUTA = '\u0629';
  public static final char HEH = '\u0647';
  public static final char TATWEEL = '\u0640';
  public static final char FATHATAN = '\u064B';
  public static final char DAMMATAN = '\u064C';
  public static final char KASRATAN = '\u064D';
  public static final char FATHA = '\u064E';
  public static final char DAMMA = '\u064F';
  public static final char KASRA = '\u0650';
  public static final char SHADDA = '\u0651';
  public static final char SUKUN = '\u0652';
  public int normalize(char s[], int len) {
    for (int i = 0; i < len; i++) {
      switch (s[i]) {
      case ALEF_MADDA:
      case ALEF_HAMZA_ABOVE:
      case ALEF_HAMZA_BELOW:
        s[i] = ALEF;
        break;
      case DOTLESS_YEH:
        s[i] = YEH;
        break;
      case TEH_MARBUTA:
        s[i] = HEH;
        break;
      case TATWEEL:
      case KASRATAN:
      case DAMMATAN:
      case FATHATAN:
      case FATHA:
      case DAMMA:
      case KASRA:
      case SHADDA:
      case SUKUN:
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
