package org.apache.solr.common.util;
public class Hash {
  @SuppressWarnings("fallthrough")
  public static int lookup3(int[] k, int offset, int length, int initval) {
    int a,b,c;
    a = b = c = 0xdeadbeef + (length<<2) + initval;
    int i=offset;
    while (length > 3)
    {
      a += k[i];
      b += k[i+1];
      c += k[i+2];
      {
        a -= c;  a ^= (c<<4)|(c>>>-4);   c += b;
        b -= a;  b ^= (a<<6)|(a>>>-6);   a += c;
        c -= b;  c ^= (b<<8)|(b>>>-8);   b += a;
        a -= c;  a ^= (c<<16)|(c>>>-16); c += b;
        b -= a;  b ^= (a<<19)|(a>>>-19); a += c;
        c -= b;  c ^= (b<<4)|(b>>>-4);   b += a;
      }
      length -= 3;
      i += 3;
    }
    switch(length) {
      case 3 : c+=k[i+2];  
      case 2 : b+=k[i+1];  
      case 1 : a+=k[i+0];  
      {
        c ^= b; c -= (b<<14)|(b>>>-14);
        a ^= c; a -= (c<<11)|(c>>>-11);
        b ^= a; b -= (a<<25)|(a>>>-25);
        c ^= b; c -= (b<<16)|(b>>>-16);
        a ^= c; a -= (c<<4)|(c>>>-4);
        b ^= a; b -= (a<<14)|(a>>>-14);
        c ^= b; c -= (b<<24)|(b>>>-24);
      }
      case 0:
        break;
    }
    return c;
  }
  public static int lookup3ycs(int[] k, int offset, int length, int initval) {
    return lookup3(k, offset, length, initval-(length<<2));
  }
  public static int lookup3ycs(CharSequence s, int start, int end, int initval) {
    int a,b,c;
    a = b = c = 0xdeadbeef + initval;
    int i=start;
    boolean mixed=true;  
    for(;;) {
      if (i>= end) break;
      mixed=false;
      char ch;
      ch = s.charAt(i++);
      a += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      ch = s.charAt(i++);
      b += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      ch = s.charAt(i++);
      c += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      {
        a -= c;  a ^= (c<<4)|(c>>>-4);   c += b;
        b -= a;  b ^= (a<<6)|(a>>>-6);   a += c;
        c -= b;  c ^= (b<<8)|(b>>>-8);   b += a;
        a -= c;  a ^= (c<<16)|(c>>>-16); c += b;
        b -= a;  b ^= (a<<19)|(a>>>-19); a += c;
        c -= b;  c ^= (b<<4)|(b>>>-4);   b += a;
      }
      mixed=true;
    }
    if (!mixed) {
        c ^= b; c -= (b<<14)|(b>>>-14);
        a ^= c; a -= (c<<11)|(c>>>-11);
        b ^= a; b -= (a<<25)|(a>>>-25);
        c ^= b; c -= (b<<16)|(b>>>-16);
        a ^= c; a -= (c<<4)|(c>>>-4);
        b ^= a; b -= (a<<14)|(a>>>-14);
        c ^= b; c -= (b<<24)|(b>>>-24);
    }
    return c;
  }
  public static long lookup3ycs64(CharSequence s, int start, int end, long initval) {
    int a,b,c;
    a = b = c = 0xdeadbeef + (int)initval;
    c += (int)(initval>>>32);
    int i=start;
    boolean mixed=true;  
    for(;;) {
      if (i>= end) break;
      mixed=false;
      char ch;
      ch = s.charAt(i++);
      a += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      ch = s.charAt(i++);
      b += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      ch = s.charAt(i++);
      c += Character.isHighSurrogate(ch) && i< end ? Character.toCodePoint(ch, s.charAt(i++)) : ch;
      if (i>= end) break;
      {
        a -= c;  a ^= (c<<4)|(c>>>-4);   c += b;
        b -= a;  b ^= (a<<6)|(a>>>-6);   a += c;
        c -= b;  c ^= (b<<8)|(b>>>-8);   b += a;
        a -= c;  a ^= (c<<16)|(c>>>-16); c += b;
        b -= a;  b ^= (a<<19)|(a>>>-19); a += c;
        c -= b;  c ^= (b<<4)|(b>>>-4);   b += a;
      }
      mixed=true;
    }
    if (!mixed) {
        c ^= b; c -= (b<<14)|(b>>>-14);
        a ^= c; a -= (c<<11)|(c>>>-11);
        b ^= a; b -= (a<<25)|(a>>>-25);
        c ^= b; c -= (b<<16)|(b>>>-16);
        a ^= c; a -= (c<<4)|(c>>>-4);
        b ^= a; b -= (a<<14)|(a>>>-14);
        c ^= b; c -= (b<<24)|(b>>>-24);
    }
    return c + (((long)b) << 32);
  }
}
