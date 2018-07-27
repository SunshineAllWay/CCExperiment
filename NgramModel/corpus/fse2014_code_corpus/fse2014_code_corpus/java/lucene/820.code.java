package org.apache.lucene.analysis.cn.smart.hhmm;
import java.util.List;
import org.apache.lucene.analysis.cn.smart.CharType;
import org.apache.lucene.analysis.cn.smart.Utility;
import org.apache.lucene.analysis.cn.smart.WordType;
import org.apache.lucene.analysis.cn.smart.hhmm.SegToken;
public class HHMMSegmenter {
  private static WordDictionary wordDict = WordDictionary.getInstance();
  private SegGraph createSegGraph(String sentence) {
    int i = 0, j;
    int length = sentence.length();
    int foundIndex;
    int[] charTypeArray = getCharTypes(sentence);
    StringBuilder wordBuf = new StringBuilder();
    SegToken token;
    int frequency = 0; 
    boolean hasFullWidth;
    int wordType;
    char[] charArray;
    SegGraph segGraph = new SegGraph();
    while (i < length) {
      hasFullWidth = false;
      switch (charTypeArray[i]) {
        case CharType.SPACE_LIKE:
          i++;
          break;
        case CharType.HANZI:
          j = i + 1;
          wordBuf.delete(0, wordBuf.length());
          wordBuf.append(sentence.charAt(i));
          charArray = new char[] { sentence.charAt(i) };
          frequency = wordDict.getFrequency(charArray);
          token = new SegToken(charArray, i, j, WordType.CHINESE_WORD,
              frequency);
          segGraph.addToken(token);
          foundIndex = wordDict.getPrefixMatch(charArray);
          while (j <= length && foundIndex != -1) {
            if (wordDict.isEqual(charArray, foundIndex) && charArray.length > 1) {
              frequency = wordDict.getFrequency(charArray);
              token = new SegToken(charArray, i, j, WordType.CHINESE_WORD,
                  frequency);
              segGraph.addToken(token);
            }
            while (j < length && charTypeArray[j] == CharType.SPACE_LIKE)
              j++;
            if (j < length && charTypeArray[j] == CharType.HANZI) {
              wordBuf.append(sentence.charAt(j));
              charArray = new char[wordBuf.length()];
              wordBuf.getChars(0, charArray.length, charArray, 0);
              foundIndex = wordDict.getPrefixMatch(charArray, foundIndex);
              j++;
            } else {
              break;
            }
          }
          i++;
          break;
        case CharType.FULLWIDTH_LETTER:
          hasFullWidth = true;
        case CharType.LETTER:
          j = i + 1;
          while (j < length
              && (charTypeArray[j] == CharType.LETTER || charTypeArray[j] == CharType.FULLWIDTH_LETTER)) {
            if (charTypeArray[j] == CharType.FULLWIDTH_LETTER)
              hasFullWidth = true;
            j++;
          }
          charArray = Utility.STRING_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          wordType = hasFullWidth ? WordType.FULLWIDTH_STRING : WordType.STRING;
          token = new SegToken(charArray, i, j, wordType, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        case CharType.FULLWIDTH_DIGIT:
          hasFullWidth = true;
        case CharType.DIGIT:
          j = i + 1;
          while (j < length
              && (charTypeArray[j] == CharType.DIGIT || charTypeArray[j] == CharType.FULLWIDTH_DIGIT)) {
            if (charTypeArray[j] == CharType.FULLWIDTH_DIGIT)
              hasFullWidth = true;
            j++;
          }
          charArray = Utility.NUMBER_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          wordType = hasFullWidth ? WordType.FULLWIDTH_NUMBER : WordType.NUMBER;
          token = new SegToken(charArray, i, j, wordType, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        case CharType.DELIMITER:
          j = i + 1;
          frequency = Utility.MAX_FREQUENCE;
          charArray = new char[] { sentence.charAt(i) };
          token = new SegToken(charArray, i, j, WordType.DELIMITER, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        default:
          j = i + 1;
          charArray = Utility.STRING_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          token = new SegToken(charArray, i, j, WordType.STRING, frequency);
          segGraph.addToken(token);
          i = j;
          break;
      }
    }
    charArray = Utility.START_CHAR_ARRAY;
    frequency = wordDict.getFrequency(charArray);
    token = new SegToken(charArray, -1, 0, WordType.SENTENCE_BEGIN, frequency);
    segGraph.addToken(token);
    charArray = Utility.END_CHAR_ARRAY;
    frequency = wordDict.getFrequency(charArray);
    token = new SegToken(charArray, length, length + 1, WordType.SENTENCE_END,
        frequency);
    segGraph.addToken(token);
    return segGraph;
  }
  private static int[] getCharTypes(String sentence) {
    int length = sentence.length();
    int[] charTypeArray = new int[length];
    for (int i = 0; i < length; i++) {
      charTypeArray[i] = Utility.getCharType(sentence.charAt(i));
    }
    return charTypeArray;
  }
  public List<SegToken> process(String sentence) {
    SegGraph segGraph = createSegGraph(sentence);
    BiSegGraph biSegGraph = new BiSegGraph(segGraph);
    List<SegToken> shortPath = biSegGraph.getShortPath();
    return shortPath;
  }
}
