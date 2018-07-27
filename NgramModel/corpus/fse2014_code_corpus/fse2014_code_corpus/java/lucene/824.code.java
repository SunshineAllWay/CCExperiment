package org.apache.lucene.analysis.cn.smart.hhmm;
import org.apache.lucene.analysis.cn.smart.Utility;
import org.apache.lucene.analysis.cn.smart.WordType;
public class SegTokenFilter {
  public SegToken filter(SegToken token) {
    switch (token.wordType) {
      case WordType.FULLWIDTH_NUMBER:
      case WordType.FULLWIDTH_STRING: 
        for (int i = 0; i < token.charArray.length; i++) {
          if (token.charArray[i] >= 0xFF10)
            token.charArray[i] -= 0xFEE0;
          if (token.charArray[i] >= 0x0041 && token.charArray[i] <= 0x005A) 
            token.charArray[i] += 0x0020;
        }
        break;
      case WordType.STRING:
        for (int i = 0; i < token.charArray.length; i++) {
          if (token.charArray[i] >= 0x0041 && token.charArray[i] <= 0x005A) 
            token.charArray[i] += 0x0020;
        }
        break;
      case WordType.DELIMITER: 
        token.charArray = Utility.COMMON_DELIMITER;
        break;
      default:
        break;
    }
    return token;
  }
}
