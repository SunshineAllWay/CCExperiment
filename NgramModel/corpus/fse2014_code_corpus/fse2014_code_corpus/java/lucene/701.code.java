package org.apache.lucene.analysis.reverse;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import java.io.IOException;
public final class ReverseStringFilter extends TokenFilter {
  private TermAttribute termAtt;
  private final char marker;
  private final Version matchVersion;
  private static final char NOMARKER = '\uFFFF';
  public static final char START_OF_HEADING_MARKER = '\u0001';
  public static final char INFORMATION_SEPARATOR_MARKER = '\u001F';
  public static final char PUA_EC00_MARKER = '\uEC00';
  public static final char RTL_DIRECTION_MARKER = '\u200F';
  @Deprecated
  public ReverseStringFilter(TokenStream in) {
    this(in, NOMARKER);
  }
  @Deprecated
  public ReverseStringFilter(TokenStream in, char marker) {
    this(Version.LUCENE_30, in, marker);
  }
  public ReverseStringFilter(Version matchVersion, TokenStream in) {
    this(matchVersion, in, NOMARKER);
  }
  public ReverseStringFilter(Version matchVersion, TokenStream in, char marker) {
    super(in);
    this.matchVersion = matchVersion;
    this.marker = marker;
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      int len = termAtt.termLength();
      if (marker != NOMARKER) {
        len++;
        termAtt.resizeTermBuffer(len);
        termAtt.termBuffer()[len - 1] = marker;
      }
      reverse( matchVersion, termAtt.termBuffer(), 0, len );
      termAtt.setTermLength(len);
      return true;
    } else {
      return false;
    }
  }
  @Deprecated
  public static String reverse( final String input ){
    return reverse(Version.LUCENE_30, input);
  }
  public static String reverse( Version matchVersion, final String input ){
    final char[] charInput = input.toCharArray();
    reverse( matchVersion, charInput, 0, charInput.length );
    return new String( charInput );
  }
  @Deprecated
  public static void reverse( final char[] buffer ){
    reverse( buffer, 0, buffer.length );
  }
  public static void reverse(Version matchVersion, final char[] buffer) {
    reverse(matchVersion, buffer, 0, buffer.length);
  }
  @Deprecated
  public static void reverse( final char[] buffer, final int len ){
    reverse( buffer, 0, len );
  }
  public static void reverse(Version matchVersion, final char[] buffer,
      final int len) {
    reverse( matchVersion, buffer, 0, len );
  }
  @Deprecated
  public static void reverse(char[] buffer, int start, int len ) {
    reverseUnicode3(buffer, start, len);
  }
  @Deprecated
  private static void reverseUnicode3( char[] buffer, int start, int len ){
    if( len <= 1 ) return;
    int num = len>>1;
    for( int i = start; i < ( start + num ); i++ ){
      char c = buffer[i];
      buffer[i] = buffer[start * 2 + len - i - 1];
      buffer[start * 2 + len - i - 1] = c;
    }
  }
  public static void reverse(Version matchVersion, final char[] buffer,
      final int start, final int len) {
    if (!matchVersion.onOrAfter(Version.LUCENE_31)) {
      reverseUnicode3(buffer, start, len);
      return;
    }
    if (len < 2)
      return;
    int end = (start + len) - 1;
    char frontHigh = buffer[start];
    char endLow = buffer[end];
    boolean allowFrontSur = true, allowEndSur = true;
    final int mid = start + (len >> 1);
    for (int i = start; i < mid; ++i, --end) {
      final char frontLow = buffer[i + 1];
      final char endHigh = buffer[end - 1];
      final boolean surAtFront = allowFrontSur
          && Character.isSurrogatePair(frontHigh, frontLow);
      if (surAtFront && (len < 3)) {
        return;
      }
      final boolean surAtEnd = allowEndSur
          && Character.isSurrogatePair(endHigh, endLow);
      allowFrontSur = allowEndSur = true;
      if (surAtFront == surAtEnd) {
        if (surAtFront) {
          buffer[end] = frontLow;
          buffer[--end] = frontHigh;
          buffer[i] = endHigh;
          buffer[++i] = endLow;
          frontHigh = buffer[i + 1];
          endLow = buffer[end - 1];
        } else {
          buffer[end] = frontHigh;
          buffer[i] = endLow;
          frontHigh = frontLow;
          endLow = endHigh;
        }
      } else {
        if (surAtFront) {
          buffer[end] = frontLow;
          buffer[i] = endLow;
          endLow = endHigh;
          allowFrontSur = false;
        } else {
          buffer[end] = frontHigh;
          buffer[i] = endHigh;
          frontHigh = frontLow;
          allowEndSur = false;
        }
      }
    }
    if ((len & 0x01) == 1 && !(allowFrontSur && allowEndSur)) {
      buffer[end] = allowFrontSur ? endLow : frontHigh;
    }
  }
}
