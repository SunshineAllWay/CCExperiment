package org.apache.lucene.queryParser.ext;
import org.apache.lucene.util.LuceneTestCase;
public class TestExtensions extends LuceneTestCase {
  private Extensions ext;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.ext = new Extensions();
  }
  public void testBuildExtensionField() {
    assertEquals("field\\:key", ext.buildExtensionField("key", "field"));
    assertEquals("\\:key", ext.buildExtensionField("key"));
    ext = new Extensions('.');
    assertEquals("field.key", ext.buildExtensionField("key", "field"));
    assertEquals(".key", ext.buildExtensionField("key"));
  }
  public void testSplitExtensionField() {
    assertEquals("field\\:key", ext.buildExtensionField("key", "field"));
    assertEquals("\\:key", ext.buildExtensionField("key"));
    ext = new Extensions('.');
    assertEquals("field.key", ext.buildExtensionField("key", "field"));
    assertEquals(".key", ext.buildExtensionField("key"));
  }
  public void testAddGetExtension() {
    ParserExtension extension = new ExtensionStub();
    assertNull(ext.getExtension("foo"));
    ext.add("foo", extension);
    assertSame(extension, ext.getExtension("foo"));
    ext.add("foo", null);
    assertNull(ext.getExtension("foo"));
  }
  public void testGetExtDelimiter() {
    assertEquals(Extensions.DEFAULT_EXTENSION_FIELD_DELIMITER, this.ext
        .getExtensionFieldDelimiter());
    ext = new Extensions('?');
    assertEquals('?', this.ext.getExtensionFieldDelimiter());
  }
  public void testEscapeExtension() {
    assertEquals("abc\\:\\?\\{\\}\\[\\]\\\\\\(\\)\\+\\-\\!\\~", ext
        .escapeExtensionField("abc:?{}[]\\()+-!~"));
    try {
      ext.escapeExtensionField(null);
      fail("should throw NPE - escape string is null");
    } catch (NullPointerException e) {
    }
  }
}
