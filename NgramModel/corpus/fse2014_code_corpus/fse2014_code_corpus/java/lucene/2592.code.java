package org.apache.solr.util;
public class BCDUtils {
  private static int div10(int a) { return (a * 0xcccd) >>> 19; }
  private static int mul10(int a) { return (a*10); }
  private static final char NEG_CHAR=(char)126;
  private static final int ZERO_EXPONENT='a';  
  public static int base10toBase100(char[] arr, int start, int end,
                                    char[] out, int outend
                                    )
  {
    int wpos=outend;  
    boolean neg=false;
    while (--end >= start) {
      int val = arr[end];
      if (val=='+') { break; }
      else if (val=='-') { neg=!neg; break; }
      else {
        val = val - '0';
        if (end > start) {
          int val2 = arr[end-1];
          if (val2=='+') { out[--wpos]=(char)val; break; }
          if (val2=='-') { out[--wpos]=(char)val; neg=!neg; break; }
          end--;
          val = val + (val2 - '0')*10;
        }
        out[--wpos] = (char)val;
      }
    }
    while (wpos<outend && out[wpos]==0) wpos++;
    if (wpos==outend) {
    } else if (neg) {
      out[--wpos]=NEG_CHAR;
    }
    return wpos;  
  }
  public static int base100toBase10(char[] arr, int start, int end,
                                    char[] out, int offset)
  {
    int wpos=offset;  
    boolean firstDigit=true;
    for (int i=start; i<end; i++) {
      int val = arr[i];
      if (val== NEG_CHAR) { out[wpos++]='-'; continue; }
      char tens = (char)(val / 10 + '0');
      if (!firstDigit || tens!='0') {  
        out[wpos++] = (char)(val / 10 + '0');    
      }
      out[wpos++] = (char)(val % 10 + '0');    
      firstDigit=false;
    }
    if (firstDigit) out[wpos++]='0';
    return wpos-offset;
  }
  public static String base10toBase100SortableInt(String val) {
    char[] arr = new char[val.length()+1];
    val.getChars(0,val.length(),arr,0);
    int len = base10toBase100SortableInt(arr,0,val.length(),arr,arr.length);
    return new String(arr,arr.length-len,len);
  }
  public static String base100SortableIntToBase10(String val) {
    int slen = val.length();
    char[] arr = new char[slen<<2];
    val.getChars(0,slen,arr,0);
    int len = base100SortableIntToBase10(arr,0,slen,arr,slen);
    return new String(arr,slen,len);
  }
  public static String base10toBase10kSortableInt(String val) {
    char[] arr = new char[val.length()+1];
    val.getChars(0,val.length(),arr,0);
    int len = base10toBase10kSortableInt(arr,0,val.length(),arr,arr.length);
    return new String(arr,arr.length-len,len);
  }
  public static String base10kSortableIntToBase10(String val) {
    int slen = val.length();
    char[] arr = new char[slen*5]; 
    val.getChars(0,slen,arr,0);
    int len = base10kSortableIntToBase10(arr,0,slen,arr,slen);
    return new String(arr,slen,len);
  }
    public static int base10toBase100SortableInt(char[] arr, int start, int end,
                                                 char[] out, int outend
                                      )
    {
      int wpos=outend;  
      boolean neg=false;
      --end;  
      while (start <= end) {
        char val = arr[start];
        if (val=='-') neg=!neg;
        else if (val>='1' && val<='9') break;
        start++;
      }
      outer: while (start <= end) {
        switch(arr[end]) {
          case ' ':
          case '\t':
          case '\n':
          case '\r': end--; break;
          default: break outer;
        }
      }
      int hundreds=0;
      while (start <= end) {
        int val = arr[end--];
        val = val - '0';
        if (start <= end) {
          int val2 = arr[end--];
          val = val + (val2 - '0')*10;
        }
        out[--wpos] = neg ? (char)(99-val) : (char)val;
      }
      hundreds += outend - wpos;
      out[--wpos] = neg ? (char)(ZERO_EXPONENT - hundreds) : (char)(ZERO_EXPONENT + hundreds);
      return outend-wpos;  
    }
  public static int base100SortableIntToBase10(char[] arr, int start, int end,
                                               char[] out, int offset)
  {
    if (end-start == 1) {
      out[offset]='0';
      return 1;
    }
    int wpos = offset;  
    boolean neg = false;
    int exp = arr[start++];
    if (exp < ZERO_EXPONENT) {
      neg=true;
      exp = ZERO_EXPONENT - exp;
      out[wpos++]='-';
    }
    boolean firstDigit=true;
    while (start < end) {
      int val = arr[start++];
      if (neg) val = 99 - val;
      char tens = (char)(val / 10 + '0');
      if (!firstDigit || tens!='0') {  
        out[wpos++] = tens;      
      }
      out[wpos++] = (char)(val % 10 + '0');    
      firstDigit=false;
    }
    return wpos-offset;
  }
  public static int base10toBase10kSortableInt(char[] arr, int start, int end,
                                               char[] out, int outend
                                    )
  {
    int wpos=outend;  
    boolean neg=false;
    --end;  
    while (start <= end) {
      char val = arr[start];
      if (val=='-') neg=!neg;
      else if (val>='1' && val<='9') break;
      start++;
    }
    outer: while (start <= end) {
      switch(arr[end]) {
        case ' ': 
        case '\t': 
        case '\n': 
        case '\r': end--; break;
        default: break outer;
      }
    }
    int exp=0;
    while (start <= end) {
      int val = arr[end--] - '0';          
      if (start <= end) {
        val += (arr[end--] - '0')*10;      
        if (start <= end) {
          val += (arr[end--] - '0')*100;    
          if (start <= end) {
            val += (arr[end--] - '0')*1000;  
          }
        }
      }
      out[--wpos] = neg ? (char)(9999-val) : (char)val;
    }
    exp += outend - wpos;
    out[--wpos] = neg ? (char)(ZERO_EXPONENT - exp) : (char)(ZERO_EXPONENT + exp);
    return outend-wpos;  
  }
  public static int base10kSortableIntToBase10(char[] arr, int start, int end,
                                               char[] out, int offset)
  {
    if (end-start == 1) {
      out[offset]='0';
      return 1;
    }
    int wpos = offset;  
    boolean neg;
    int exp = arr[start++];
    if (exp < ZERO_EXPONENT) {
      neg=true;
      out[wpos++]='-';
    } else {
      neg=false;
    }
    int val = arr[start++];
    if (neg) val = 9999 - val;
    if (val < 10) {
      out[wpos++] = (char)(val + '0');
    } else if (val < 100) {
      int div = div10(val);
      int ones = val - mul10(div); 
      out[wpos++] = (char)(div + '0');
      out[wpos++] = (char)(ones + '0');
    } else if (val < 1000) {
      int div = div10(val);
      int ones = val - mul10(div); 
      val=div;
      div = div10(val);
      int tens = val - mul10(div); 
      out[wpos++] = (char)(div + '0');
      out[wpos++] = (char)(tens + '0');
      out[wpos++] = (char)(ones + '0');
    } else {
      int div = div10(val);
      int ones = val - mul10(div); 
      val=div;
      div = div10(val);
      int tens = val - mul10(div); 
      val=div;
      div = div10(val);
      int hundreds = val - mul10(div); 
      out[wpos++] = (char)(div + '0');
      out[wpos++] = (char)(hundreds + '0');
      out[wpos++] = (char)(tens + '0');
      out[wpos++] = (char)(ones + '0');
    }
    while (start < end) {
      val = arr[start++];
      if (neg) val = 9999 - val;
      int div = div10(val);
      int ones = val - mul10(div); 
      val=div;
      div = div10(val);
      int tens = val - mul10(div); 
      val=div;
      div = div10(val);
      int hundreds = val - mul10(div); 
      out[wpos++] = (char)(div + '0');
      out[wpos++] = (char)(hundreds + '0');
      out[wpos++] = (char)(tens + '0');
      out[wpos++] = (char)(ones + '0');
    }
    return wpos-offset;
  }
}
