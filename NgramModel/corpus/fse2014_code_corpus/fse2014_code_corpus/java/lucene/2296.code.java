package org.apache.solr.analysis;
import static org.apache.solr.analysis.WordDelimiterFilter.*;
final class WordDelimiterIterator {
  public static final int DONE = -1;
  public static final byte[] DEFAULT_WORD_DELIM_TABLE;
  char text[];
  int length;
  int startBounds;
  int endBounds;
  int current;
  int end;
  private boolean hasFinalPossessive = false;
  final boolean splitOnCaseChange;
  final boolean splitOnNumerics;
  final boolean stemEnglishPossessive;
  private final byte[] charTypeTable;
  private boolean skipPossessive = false;
  static {
    byte[] tab = new byte[256];
    for (int i = 0; i < 256; i++) {
      byte code = 0;
      if (Character.isLowerCase(i)) {
        code |= LOWER;
      }
      else if (Character.isUpperCase(i)) {
        code |= UPPER;
      }
      else if (Character.isDigit(i)) {
        code |= DIGIT;
      }
      if (code == 0) {
        code = SUBWORD_DELIM;
      }
      tab[i] = code;
    }
    DEFAULT_WORD_DELIM_TABLE = tab;
  }
  WordDelimiterIterator(byte[] charTypeTable, boolean splitOnCaseChange, boolean splitOnNumerics, boolean stemEnglishPossessive) {
    this.charTypeTable = charTypeTable;
    this.splitOnCaseChange = splitOnCaseChange;
    this.splitOnNumerics = splitOnNumerics;
    this.stemEnglishPossessive = stemEnglishPossessive;
  }
  int next() {
    current = end;
    if (current == DONE) {
      return DONE;
    }
    if (skipPossessive) {
      current += 2;
      skipPossessive = false;
    }
    int lastType = 0;
    while (current < endBounds && (isSubwordDelim(lastType = charType(text[current])))) {
      current++;
    }
    if (current >= endBounds) {
      return end = DONE;
    }
    for (end = current + 1; end < endBounds; end++) {
      int type = charType(text[end]);
      if (isBreak(lastType, type)) {
        break;
      }
      lastType = type;
    }
    if (end < endBounds - 1 && endsWithPossessive(end + 2)) {
      skipPossessive = true;
    }
    return end;
  }
  int type() {
    if (end == DONE) {
      return 0;
    }
    int type = charType(text[current]);
    switch (type) {
      case LOWER:
      case UPPER:
        return ALPHA;
      default:
        return type;
    }
  }
  void setText(char text[], int length) {
    this.text = text;
    this.length = this.endBounds = length;
    current = startBounds = end = 0;
    skipPossessive = hasFinalPossessive = false;
    setBounds();
  }
  private boolean isBreak(int lastType, int type) {
    if ((type & lastType) != 0) {
      return false;
    }
    if (!splitOnCaseChange && isAlpha(lastType) && isAlpha(type)) {
      return false;
    } else if (isUpper(lastType) && isAlpha(type)) {
      return false;
    } else if (!splitOnNumerics && ((isAlpha(lastType) && isDigit(type)) || (isDigit(lastType) && isAlpha(type)))) {
      return false;
    }
    return true;
  }
  boolean isSingleWord() {
    if (hasFinalPossessive) {
      return current == startBounds && end == endBounds - 2;
    }
    else {
      return current == startBounds && end == endBounds;
    }
  }
  private void setBounds() {
    while (startBounds < length && (isSubwordDelim(charType(text[startBounds])))) {
      startBounds++;
    }
    while (endBounds > startBounds && (isSubwordDelim(charType(text[endBounds - 1])))) {
      endBounds--;
    }
    if (endsWithPossessive(endBounds)) {
      hasFinalPossessive = true;
    }
    current = startBounds;
  }
  private boolean endsWithPossessive(int pos) {
    return (stemEnglishPossessive &&
            pos > 2 &&
            text[pos - 2] == '\'' &&
            (text[pos - 1] == 's' || text[pos - 1] == 'S') &&
            isAlpha(charType(text[pos - 3])) &&
            (pos == endBounds || isSubwordDelim(charType(text[pos]))));
  }
  private int charType(int ch) {
    if (ch < charTypeTable.length) {
      return charTypeTable[ch];
    }
    switch (Character.getType(ch)) {
      case Character.UPPERCASE_LETTER: return UPPER;
      case Character.LOWERCASE_LETTER: return LOWER;
      case Character.TITLECASE_LETTER:
      case Character.MODIFIER_LETTER:
      case Character.OTHER_LETTER:
      case Character.NON_SPACING_MARK:
      case Character.ENCLOSING_MARK:  
      case Character.COMBINING_SPACING_MARK:
        return ALPHA; 
      case Character.DECIMAL_DIGIT_NUMBER:
      case Character.LETTER_NUMBER:
      case Character.OTHER_NUMBER:
        return DIGIT;
      case Character.SURROGATE:  
        return ALPHA|DIGIT;  
      default: return SUBWORD_DELIM;
    }
  }
}