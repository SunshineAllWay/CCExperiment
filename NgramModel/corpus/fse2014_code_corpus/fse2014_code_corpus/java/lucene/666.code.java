package org.apache.lucene.analysis.hi;
public class HindiNormalizer {
  public int normalize(char s[], int len) {
    for (int i = 0; i < len; i++) {
      switch (s[i]) {
      case '\u0928':
        if (i + 1 < len && s[i + 1] == '\u094D') {
          s[i] = '\u0902';
          len = delete(s, i + 1, len);
        }
        break;
      case '\u0901':
        s[i] = '\u0902';
        break;
      case '\u093C':
        len = delete(s, i, len);
        i--;
        break;      
      case '\u0929':
        s[i] = '\u0928';
        break;
      case '\u0931':
        s[i] = '\u0930';
        break;
      case '\u0934':
        s[i] = '\u0933';
        break;
      case '\u0958':
        s[i] = '\u0915';
        break;
      case '\u0959':
        s[i] = '\u0916';
        break;
      case '\u095A':
        s[i] = '\u0917';
        break;
      case '\u095B':
        s[i] = '\u091C';
        break;
      case '\u095C':
        s[i] = '\u0921';
        break;
      case '\u095D':
        s[i] = '\u0922';
        break;
      case '\u095E':
        s[i] = '\u092B';
        break;
      case '\u095F':
        s[i] = '\u092F';
        break;
      case '\u200D':
      case '\u200C':
        len = delete(s, i, len);
        i--;
        break;
      case '\u094D':
        len = delete(s, i, len);
        i--;
        break;
      case '\u0945':
      case '\u0946':
        s[i] = '\u0947';
        break;
      case '\u0949':
      case '\u094A':
        s[i] = '\u094B';
        break;
      case '\u090D':
      case '\u090E':
        s[i] = '\u090F';
        break;
      case '\u0911':
      case '\u0912':
        s[i] = '\u0913';
        break;
      case '\u0972':
        s[i] = '\u0905';
        break;
      case '\u0906':
        s[i] = '\u0905';
        break;
      case '\u0908':
        s[i] = '\u0907';
        break;
      case '\u090A':
        s[i] = '\u0909';
        break;
      case '\u0960':
        s[i] = '\u090B';
        break;
      case '\u0961':
        s[i] = '\u090C';
        break;
      case '\u0910':
        s[i] = '\u090F';
        break;
      case '\u0914':
        s[i] = '\u0913';
        break;
      case '\u0940':
        s[i] = '\u093F';
        break;
      case '\u0942':
        s[i] = '\u0941';
        break;
      case '\u0944':
        s[i] = '\u0943';
        break;
      case '\u0963':
        s[i] = '\u0962';
        break;
      case '\u0948':
        s[i] = '\u0947';
        break;
      case '\u094C':
        s[i] = '\u094B';
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
