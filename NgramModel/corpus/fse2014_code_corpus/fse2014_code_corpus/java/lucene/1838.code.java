package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Random;
import org.apache.lucene.util.Version;
public class TestCharTokenizers extends BaseTokenStreamTestCase {
  public void testReadSupplementaryChars() throws IOException {
    StringBuilder builder = new StringBuilder();
    Random newRandom = newRandom();
    int num = 1024 + newRandom.nextInt(1024);
    for (int i = 1; i < num; i++) {
      builder.append("\ud801\udc1cabc");
      if((i % 10) == 0)
        builder.append(" ");
    }
    builder.insert(1023, "\ud801\udc1c");
    LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(
        TEST_VERSION_CURRENT, new StringReader(builder.toString()));
    assertTokenStreamContents(tokenizer, builder.toString().toLowerCase().split(" "));
  }
  public void testExtendCharBuffer() throws IOException {
    for (int i = 0; i < 40; i++) {
      StringBuilder builder = new StringBuilder();
      for (int j = 0; j < 1+i; j++) {
        builder.append("a");
      }
      builder.append("\ud801\udc1cabc");
      LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(
          TEST_VERSION_CURRENT, new StringReader(builder.toString()));
      assertTokenStreamContents(tokenizer, new String[] {builder.toString().toLowerCase()});
    }
  }
  public void testMaxWordLength() throws IOException {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 255; i++) {
      builder.append("A");
    }
    LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(
        TEST_VERSION_CURRENT, new StringReader(builder.toString() + builder.toString()));
    assertTokenStreamContents(tokenizer, new String[] {builder.toString().toLowerCase(), builder.toString().toLowerCase()});
  }
  public void testMaxWordLengthWithSupplementary() throws IOException {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 254; i++) {
      builder.append("A");
    }
    builder.append("\ud801\udc1c");
    LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(
        TEST_VERSION_CURRENT, new StringReader(builder.toString() + builder.toString()));
    assertTokenStreamContents(tokenizer, new String[] {builder.toString().toLowerCase(), builder.toString().toLowerCase()});
  }
  public void testLowerCaseTokenizer() throws IOException {
    StringReader reader = new StringReader("Tokenizer \ud801\udc1ctest");
    LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(TEST_VERSION_CURRENT,
        reader);
    assertTokenStreamContents(tokenizer, new String[] { "tokenizer",
        "\ud801\udc44test" });
  }
  public void testLowerCaseTokenizerBWCompat() throws IOException {
    StringReader reader = new StringReader("Tokenizer \ud801\udc1ctest");
    LowerCaseTokenizer tokenizer = new LowerCaseTokenizer(Version.LUCENE_30,
        reader);
    assertTokenStreamContents(tokenizer, new String[] { "tokenizer", "test" });
  }
  public void testWhitespaceTokenizer() throws IOException {
    StringReader reader = new StringReader("Tokenizer \ud801\udc1ctest");
    WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT,
        reader);
    assertTokenStreamContents(tokenizer, new String[] { "Tokenizer",
        "\ud801\udc1ctest" });
  }
  public void testWhitespaceTokenizerBWCompat() throws IOException {
    StringReader reader = new StringReader("Tokenizer \ud801\udc1ctest");
    WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(Version.LUCENE_30,
        reader);
    assertTokenStreamContents(tokenizer, new String[] { "Tokenizer",
        "\ud801\udc1ctest" });
  }
  public void testIsTokenCharCharInSubclass() {
    new TestingCharTokenizer(Version.LUCENE_30, new StringReader(""));
    try {
      new TestingCharTokenizer(TEST_VERSION_CURRENT, new StringReader(""));
      fail("version 3.1 is not permitted if char based method is implemented");
    } catch (IllegalArgumentException e) {
    }
  }
  public void testNormalizeCharInSubclass() {
    new TestingCharTokenizerNormalize(Version.LUCENE_30, new StringReader(""));
    try {
      new TestingCharTokenizerNormalize(TEST_VERSION_CURRENT,
          new StringReader(""));
      fail("version 3.1 is not permitted if char based method is implemented");
    } catch (IllegalArgumentException e) {
    }
  }
  public void testNormalizeAndIsTokenCharCharInSubclass() {
    new TestingCharTokenizerNormalizeIsTokenChar(Version.LUCENE_30,
        new StringReader(""));
    try {
      new TestingCharTokenizerNormalizeIsTokenChar(TEST_VERSION_CURRENT,
          new StringReader(""));
      fail("version 3.1 is not permitted if char based method is implemented");
    } catch (IllegalArgumentException e) {
    }
  }
  static class TestingCharTokenizer extends CharTokenizer {
    public TestingCharTokenizer(Version matchVersion, Reader input) {
      super(matchVersion, input);
    }
    @Override
    protected boolean isTokenChar(int c) {
      return Character.isLetter(c);
    }
    @Deprecated @Override
    protected boolean isTokenChar(char c) {
      return Character.isLetter(c);
    }
  }
  static class TestingCharTokenizerNormalize extends CharTokenizer {
    public TestingCharTokenizerNormalize(Version matchVersion, Reader input) {
      super(matchVersion, input);
    }
    @Deprecated @Override
    protected char normalize(char c) {
      return c;
    }
    @Override
    protected int normalize(int c) {
      return c;
    }
  }
  static class TestingCharTokenizerNormalizeIsTokenChar extends CharTokenizer {
    public TestingCharTokenizerNormalizeIsTokenChar(Version matchVersion,
        Reader input) {
      super(matchVersion, input);
    }
    @Deprecated @Override
    protected char normalize(char c) {
      return c;
    }
    @Override
    protected int normalize(int c) {
      return c;
    }
    @Override
    protected boolean isTokenChar(int c) {
      return Character.isLetter(c);
    }
    @Deprecated @Override
    protected boolean isTokenChar(char c) {
      return Character.isLetter(c);
    }
  }
}
