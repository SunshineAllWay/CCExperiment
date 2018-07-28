package org.apache.lucene.util;
public final class ArrayUtil {
  @Deprecated
  public ArrayUtil() {} 
  public static int parseInt(char[] chars) throws NumberFormatException {
    return parseInt(chars, 0, chars.length, 10);
  }
  public static int parseInt(char[] chars, int offset, int len) throws NumberFormatException {
    return parseInt(chars, offset, len, 10);
  }
  public static int parseInt(char[] chars, int offset, int len, int radix)
          throws NumberFormatException {
    if (chars == null || radix < Character.MIN_RADIX
            || radix > Character.MAX_RADIX) {
      throw new NumberFormatException();
    }
    int  i = 0;
    if (len == 0) {
      throw new NumberFormatException("chars length is 0");
    }
    boolean negative = chars[offset + i] == '-';
    if (negative && ++i == len) {
      throw new NumberFormatException("can't convert to an int");
    }
    if (negative == true){
      offset++;
      len--;
    }
    return parse(chars, offset, len, radix, negative);
  }
  private static int parse(char[] chars, int offset, int len, int radix,
                           boolean negative) throws NumberFormatException {
    int max = Integer.MIN_VALUE / radix;
    int result = 0;
    for (int i = 0; i < len; i++){
      int digit = Character.digit(chars[i + offset], radix);
      if (digit == -1) {
        throw new NumberFormatException("Unable to parse");
      }
      if (max > result) {
        throw new NumberFormatException("Unable to parse");
      }
      int next = result * radix - digit;
      if (next > result) {
        throw new NumberFormatException("Unable to parse");
      }
      result = next;
    }
    if (!negative) {
      result = -result;
      if (result < 0) {
        throw new NumberFormatException("Unable to parse");
      }
    }
    return result;
  }
  public static int oversize(int minTargetSize, int bytesPerElement) {
    if (minTargetSize < 0) {
      throw new IllegalArgumentException("invalid array size " + minTargetSize);
    }
    if (minTargetSize == 0) {
      return 0;
    }
    int extra = minTargetSize >> 3;
    if (extra < 3) {
      extra = 3;
    }
    int newSize = minTargetSize + extra;
    if (newSize+7 < 0) {
      return Integer.MAX_VALUE;
    }
    if (Constants.JRE_IS_64BIT) {
      switch(bytesPerElement) {
      case 4:
        return (newSize + 1) & 0x7ffffffe;
      case 2:
        return (newSize + 3) & 0x7ffffffc;
      case 1:
        return (newSize + 7) & 0x7ffffff8;
      case 8:
      default:
        return newSize;
      }
    } else {
      switch(bytesPerElement) {
      case 2:
        return (newSize + 1) & 0x7ffffffe;
      case 1:
        return (newSize + 3) & 0x7ffffffc;
      case 4:
      case 8:
      default:
        return newSize;
      }
    }
  }
  public static int getShrinkSize(int currentSize, int targetSize, int bytesPerElement) {
    final int newSize = oversize(targetSize, bytesPerElement);
    if (newSize < currentSize / 2)
      return newSize;
    else
      return currentSize;
  }
  public static int[] grow(int[] array, int minSize) {
    if (array.length < minSize) {
      int[] newArray = new int[oversize(minSize, RamUsageEstimator.NUM_BYTES_INT)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }
  public static int[] grow(int[] array) {
    return grow(array, 1 + array.length);
  }
  public static int[] shrink(int[] array, int targetSize) {
    final int newSize = getShrinkSize(array.length, targetSize, RamUsageEstimator.NUM_BYTES_INT);
    if (newSize != array.length) {
      int[] newArray = new int[newSize];
      System.arraycopy(array, 0, newArray, 0, newSize);
      return newArray;
    } else
      return array;
  }
  public static long[] grow(long[] array, int minSize) {
    if (array.length < minSize) {
      long[] newArray = new long[oversize(minSize, RamUsageEstimator.NUM_BYTES_LONG)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }
  public static long[] grow(long[] array) {
    return grow(array, 1 + array.length);
  }
  public static long[] shrink(long[] array, int targetSize) {
    final int newSize = getShrinkSize(array.length, targetSize, RamUsageEstimator.NUM_BYTES_LONG);
    if (newSize != array.length) {
      long[] newArray = new long[newSize];
      System.arraycopy(array, 0, newArray, 0, newSize);
      return newArray;
    } else
      return array;
  }
  public static byte[] grow(byte[] array, int minSize) {
    if (array.length < minSize) {
      byte[] newArray = new byte[oversize(minSize, 1)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }
  public static byte[] grow(byte[] array) {
    return grow(array, 1 + array.length);
  }
  public static byte[] shrink(byte[] array, int targetSize) {
    final int newSize = getShrinkSize(array.length, targetSize, 1);
    if (newSize != array.length) {
      byte[] newArray = new byte[newSize];
      System.arraycopy(array, 0, newArray, 0, newSize);
      return newArray;
    } else
      return array;
  }
  public static char[] grow(char[] array, int minSize) {
    if (array.length < minSize) {
      char[] newArray = new char[oversize(minSize, RamUsageEstimator.NUM_BYTES_CHAR)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }
  public static char[] grow(char[] array) {
    return grow(array, 1 + array.length);
  }
  public static char[] shrink(char[] array, int targetSize) {
    final int newSize = getShrinkSize(array.length, targetSize, RamUsageEstimator.NUM_BYTES_CHAR);
    if (newSize != array.length) {
      char[] newArray = new char[newSize];
      System.arraycopy(array, 0, newArray, 0, newSize);
      return newArray;
    } else
      return array;
  }
  public static int hashCode(char[] array, int start, int end) {
    int code = 0;
    for (int i = end - 1; i >= start; i--)
      code = code * 31 + array[i];
    return code;
  }
  public static int hashCode(byte[] array, int start, int end) {
    int code = 0;
    for (int i = end - 1; i >= start; i--)
      code = code * 31 + array[i];
    return code;
  }
}
