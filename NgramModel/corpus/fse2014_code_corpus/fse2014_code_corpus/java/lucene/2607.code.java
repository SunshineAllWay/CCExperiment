package org.apache.solr.util;
public class NumberUtils {
  public static String int2sortableStr(int val) {
    char[] arr = new char[3];
    int2sortableStr(val,arr,0);
    return new String(arr,0,3);
  }
  public static String int2sortableStr(String val) {
    return int2sortableStr(Integer.parseInt(val));
  }
  public static String SortableStr2int(String val) {
    int ival = SortableStr2int(val,0,3);
    return Integer.toString(ival);
  }
  public static String long2sortableStr(long val) {
    char[] arr = new char[5];
    long2sortableStr(val,arr,0);
    return new String(arr,0,5);
  }
  public static String long2sortableStr(String val) {
    return long2sortableStr(Long.parseLong(val));
  }
  public static String SortableStr2long(String val) {
    long ival = SortableStr2long(val,0,5);
    return Long.toString(ival);
  }
  public static String float2sortableStr(float val) {
    int f = Float.floatToRawIntBits(val);
    if (f<0) f ^= 0x7fffffff;
    return int2sortableStr(f);
  }
  public static String float2sortableStr(String val) {
    return float2sortableStr(Float.parseFloat(val));
  }
  public static float SortableStr2float(String val) {
    int f = SortableStr2int(val,0,3);
    if (f<0) f ^= 0x7fffffff;
    return Float.intBitsToFloat(f);
  }
  public static String SortableStr2floatStr(String val) {
    return Float.toString(SortableStr2float(val));
  }
  public static String double2sortableStr(double val) {
    long f = Double.doubleToRawLongBits(val);
    if (f<0) f ^= 0x7fffffffffffffffL;
    return long2sortableStr(f);
  }
  public static String double2sortableStr(String val) {
    return double2sortableStr(Double.parseDouble(val));
  }
  public static double SortableStr2double(String val) {
    long f = SortableStr2long(val,0,6);
    if (f<0) f ^= 0x7fffffffffffffffL;
    return Double.longBitsToDouble(f);
  }
  public static String SortableStr2doubleStr(String val) {
    return Double.toString(SortableStr2double(val));
  }
  public static int int2sortableStr(int val, char[] out, int offset) {
    val += Integer.MIN_VALUE;
    out[offset++] = (char)(val >>> 24);
    out[offset++] = (char)((val >>> 12) & 0x0fff);
    out[offset++] = (char)(val & 0x0fff);
    return 3;
  }
  public static int SortableStr2int(String sval, int offset, int len) {
    int val = sval.charAt(offset++) << 24;
    val |= sval.charAt(offset++) << 12;
    val |= sval.charAt(offset++);
    val -= Integer.MIN_VALUE;
    return val;
  }
  public static int long2sortableStr(long val, char[] out, int offset) {
    val += Long.MIN_VALUE;
    out[offset++] = (char)(val >>>60);
    out[offset++] = (char)(val >>>45 & 0x7fff);
    out[offset++] = (char)(val >>>30 & 0x7fff);
    out[offset++] = (char)(val >>>15 & 0x7fff);
    out[offset] = (char)(val & 0x7fff);
    return 5;
  }
  public static long SortableStr2long(String sval, int offset, int len) {
    long val = (long)(sval.charAt(offset++)) << 60;
    val |= ((long)sval.charAt(offset++)) << 45;
    val |= ((long)sval.charAt(offset++)) << 30;
    val |= sval.charAt(offset++) << 15;
    val |= sval.charAt(offset);
    val -= Long.MIN_VALUE;
    return val;
  }
}
