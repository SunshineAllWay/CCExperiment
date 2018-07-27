package org.apache.lucene.analysis.cn.smart;
import org.apache.lucene.analysis.cn.smart.hhmm.SegTokenFilter; 
public class Utility {
  public static final char[] STRING_CHAR_ARRAY = new String("未##串")
      .toCharArray();
  public static final char[] NUMBER_CHAR_ARRAY = new String("未##数")
      .toCharArray();
  public static final char[] START_CHAR_ARRAY = new String("始##始")
      .toCharArray();
  public static final char[] END_CHAR_ARRAY = new String("末##末").toCharArray();
  public static final char[] COMMON_DELIMITER = new char[] { ',' };
  public static final String SPACES = " 　\t\r\n";
  public static final int MAX_FREQUENCE = 2079997 + 80000;
  public static int compareArray(char[] larray, int lstartIndex, char[] rarray,
      int rstartIndex) {
    if (larray == null) {
      if (rarray == null || rstartIndex >= rarray.length)
        return 0;
      else
        return -1;
    } else {
      if (rarray == null) {
        if (lstartIndex >= larray.length)
          return 0;
        else
          return 1;
      }
    }
    int li = lstartIndex, ri = rstartIndex;
    while (li < larray.length && ri < rarray.length && larray[li] == rarray[ri]) {
      li++;
      ri++;
    }
    if (li == larray.length) {
      if (ri == rarray.length) {
        return 0;
      } else {
        return -1;
      }
    } else {
      if (ri == rarray.length) {
        return 1;
      } else {
        if (larray[li] > rarray[ri])
          return 1;
        else
          return -1;
      }
    }
  }
  public static int compareArrayByPrefix(char[] shortArray, int shortIndex,
      char[] longArray, int longIndex) {
    if (shortArray == null)
      return 0;
    else if (longArray == null)
      return (shortIndex < shortArray.length) ? 1 : 0;
    int si = shortIndex, li = longIndex;
    while (si < shortArray.length && li < longArray.length
        && shortArray[si] == longArray[li]) {
      si++;
      li++;
    }
    if (si == shortArray.length) {
      return 0;
    } else {
      if (li == longArray.length)
        return 1;
      else
        return (shortArray[si] > longArray[li]) ? 1 : -1;
    }
  }
  public static int getCharType(char ch) {
    if (ch >= 0x4E00 && ch <= 0x9FA5)
      return CharType.HANZI;
    if ((ch >= 0x0041 && ch <= 0x005A) || (ch >= 0x0061 && ch <= 0x007A))
      return CharType.LETTER;
    if (ch >= 0x0030 && ch <= 0x0039)
      return CharType.DIGIT;
    if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '　')
      return CharType.SPACE_LIKE;
    if ((ch >= 0x0021 && ch <= 0x00BB) || (ch >= 0x2010 && ch <= 0x2642)
        || (ch >= 0x3001 && ch <= 0x301E))
      return CharType.DELIMITER;
    if ((ch >= 0xFF21 && ch <= 0xFF3A) || (ch >= 0xFF41 && ch <= 0xFF5A))
      return CharType.FULLWIDTH_LETTER;
    if (ch >= 0xFF10 && ch <= 0xFF19)
      return CharType.FULLWIDTH_DIGIT;
    if (ch >= 0xFE30 && ch <= 0xFF63)
      return CharType.DELIMITER;
    return CharType.OTHER;
  }
}
