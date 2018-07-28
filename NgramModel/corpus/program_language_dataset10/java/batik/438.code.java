package org.apache.batik.css.parser;
public class ScannerUtilities {
    protected static final int[] IDENTIFIER_START =
    { 0x0, 0x0, 0x87FFFFFE, 0x7FFFFFE };
    protected static final int[] NAME =
    { 0, 0x3FF2000, 0x87FFFFFE, 0x7FFFFFE };
    protected static final int[] HEXADECIMAL =
    { 0, 0x3FF0000, 0x7E, 0x7E };
    protected static final int[] STRING =
    { 0x200, 0xFFFFFF7B, 0xFFFFFFFF, 0x7FFFFFFF };
    protected static final int[] URI =
    { 0x0, 0xFFFFFC7A, 0xFFFFFFFF, 0x7FFFFFFF };
    protected ScannerUtilities() {
    }
    public static boolean isCSSSpace(char c) {
      return (c <= 0x0020) &&
             (((((1L << '\t') |
                 (1L << '\n') |
                 (1L << '\r') |
                 (1L << '\f') |
                 (1L << 0x0020)) >> c) & 1L) != 0);
    }
    public static boolean isCSSIdentifierStartCharacter(char c) {
        return c >= 128 || ((IDENTIFIER_START[c>>5] & (1 << (c &0x1F))) != 0);
    }
    public static boolean isCSSNameCharacter(char c) {
        return c >= 128 || ((NAME[c >>5] & (1 << (c &0x1F))) != 0);
    }
    public static boolean isCSSHexadecimalCharacter(char c) {
        return c < 128 && ((HEXADECIMAL[c>>5] & (1 << (c&0x1F))) != 0);
    }
    public static boolean isCSSStringCharacter(char c) {
        return c >= 128 || ((STRING[c>>5] & (1 << (c&0x1F))) != 0);
    }
    public static boolean isCSSURICharacter(char c) {
        return c >= 128 || ((URI[c>>5] & (1 << (c&0x1F))) != 0);
    }
}
