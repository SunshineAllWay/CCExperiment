package org.apache.lucene.analysis.cz;
public class CzechStemmer {
  public int stem(char s[], int len) {
    len = removeCase(s, len);
    len = removePossessives(s, len);
    len = normalize(s, len);
    return len;
  }
  private int removeCase(char s[], int len) {  
    if (len > 7 && endsWith(s, len, "atech"))
      return len - 5;
    if (len > 6 && 
        (endsWith(s, len,"ětem") ||
        endsWith(s, len,"etem") ||
        endsWith(s, len,"atům")))
      return len - 4;
    if (len > 5 && 
        (endsWith(s, len, "ech") ||
        endsWith(s, len, "ich") ||
        endsWith(s, len, "ích") ||
        endsWith(s, len, "ého") ||
        endsWith(s, len, "ěmi") ||
        endsWith(s, len, "emi") ||
        endsWith(s, len, "ému") ||
        endsWith(s, len, "ěte") ||
        endsWith(s, len, "ete") ||
        endsWith(s, len, "ěti") ||
        endsWith(s, len, "eti") ||
        endsWith(s, len, "ího") ||
        endsWith(s, len, "iho") ||
        endsWith(s, len, "ími") ||
        endsWith(s, len, "ímu") ||
        endsWith(s, len, "imu") ||
        endsWith(s, len, "ách") ||
        endsWith(s, len, "ata") ||
        endsWith(s, len, "aty") ||
        endsWith(s, len, "ých") ||
        endsWith(s, len, "ama") ||
        endsWith(s, len, "ami") ||
        endsWith(s, len, "ové") ||
        endsWith(s, len, "ovi") ||
        endsWith(s, len, "ými")))
      return len - 3;
    if (len > 4 && 
        (endsWith(s, len, "em") ||
        endsWith(s, len, "es") ||
        endsWith(s, len, "ém") ||
        endsWith(s, len, "ím") ||
        endsWith(s, len, "ům") ||
        endsWith(s, len, "at") ||
        endsWith(s, len, "ám") ||
        endsWith(s, len, "os") ||
        endsWith(s, len, "us") ||
        endsWith(s, len, "ým") ||
        endsWith(s, len, "mi") ||
        endsWith(s, len, "ou")))
      return len - 2;
    if (len > 3) {
      switch (s[len - 1]) {
        case 'a':
        case 'e':
        case 'i':
        case 'o':
        case 'u':
        case 'ů':
        case 'y':
        case 'á':
        case 'é':
        case 'í':
        case 'ý':
        case 'ě':
          return len - 1;
      }
    }
    return len;
  }
  private int removePossessives(char s[], int len) {
    if (len > 5 &&
        (endsWith(s, len, "ov") ||
        endsWith(s, len, "in") ||
        endsWith(s, len, "ův")))
      return len - 2;
    return len;
  }
  private int normalize(char s[], int len) {
    if (endsWith(s, len, "čt")) { 
      s[len - 2] = 'c';
      s[len - 1] = 'k';
      return len;
    }
    if (endsWith(s, len, "št")) { 
      s[len - 2] = 's';
      s[len - 1] = 'k';
      return len;
    }
    switch(s[len - 1]) {
      case 'c': 
      case 'č':
        s[len - 1] = 'k';
        return len;
      case 'z': 
      case 'ž':
        s[len - 1] = 'h';
        return len;
    }
    if (len > 1 && s[len - 2] == 'e') {
      s[len - 2] = s[len - 1]; 
      return len - 1;
    }
    if (len > 2 && s[len - 2] == 'ů') {
      s[len - 2] = 'o'; 
      return len;
    }
    return len;
  }
  private boolean endsWith(char s[], int len, String suffix) {
    int suffixLen = suffix.length();
    if (suffixLen > len)
      return false;
    for (int i = suffixLen - 1; i >= 0; i--)
      if (s[len - (suffixLen - i)] != suffix.charAt(i))
        return false;
    return true;
  }
}
