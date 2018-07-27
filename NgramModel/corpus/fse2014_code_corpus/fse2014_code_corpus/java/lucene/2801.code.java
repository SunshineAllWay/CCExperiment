package org.apache.solr.handler;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.AbstractSolrTestCase;
public abstract class AnalysisRequestHandlerTestBase extends AbstractSolrTestCase {
  protected void assertToken(NamedList token, TokenInfo info) {
    assertEquals(info.getText(), token.get("text"));
    if (info.getRawText() != null) {
      assertEquals(info.getRawText(), token.get("raw_text"));
    }
    assertEquals(info.getType(), token.get("type"));
    assertEquals(new Integer(info.getStart()), token.get("start"));
    assertEquals(new Integer(info.getEnd()), token.get("end"));
    assertEquals(new Integer(info.getPosition()), token.get("position"));
    if (info.isMatch()) {
      assertEquals(Boolean.TRUE, token.get("match"));
    }
    if (info.getPayload() != null) {
      assertEquals(info.getPayload(), token.get("payload"));
    }
  }
  protected class TokenInfo {
    private String text;
    private String rawText;
    private String type;
    private int start;
    private int end;
    private String payload;
    private int position;
    private boolean match;
    public TokenInfo(
            String text,
            String rawText,
            String type,
            int start,
            int end,
            int position,
            String payload,
            boolean match) {
      this.text = text;
      this.rawText = rawText;
      this.type = type;
      this.start = start;
      this.end = end;
      this.position = position;
      this.payload = payload;
      this.match = match;
    }
    public String getText() {
      return text;
    }
    public String getRawText() {
      return rawText;
    }
    public String getType() {
      return type;
    }
    public int getStart() {
      return start;
    }
    public int getEnd() {
      return end;
    }
    public String getPayload() {
      return payload;
    }
    public int getPosition() {
      return position;
    }
    public boolean isMatch() {
      return match;
    }
  }
}
