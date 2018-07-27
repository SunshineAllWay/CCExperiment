package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("unchecked")
public class AnlysisResponseBaseTest {
  @Test
  public void testBuildTokenInfo() throws Exception {
    NamedList tokenNL = new NamedList();
    tokenNL.add("text", "JUMPING");
    tokenNL.add("type", "word");
    tokenNL.add("start", 0);
    tokenNL.add("end", 7);
    tokenNL.add("position", 1);
    AnalysisResponseBase response = new AnalysisResponseBase();
    AnalysisResponseBase.TokenInfo tokenInfo = response.buildTokenInfo(tokenNL);
    assertEquals("JUMPING", tokenInfo.getText());
    assertEquals(null, tokenInfo.getRawText());
    assertEquals("word", tokenInfo.getType());
    assertEquals(0, tokenInfo.getStart());
    assertEquals(7, tokenInfo.getEnd());
    assertEquals(1, tokenInfo.getPosition());
    assertFalse(tokenInfo.isMatch());
    tokenNL.add("rawText", "JUMPING1");
    tokenNL.add("match", true);
    tokenInfo = response.buildTokenInfo(tokenNL);
    assertEquals("JUMPING", tokenInfo.getText());
    assertEquals("JUMPING1", tokenInfo.getRawText());
    assertEquals("word", tokenInfo.getType());
    assertEquals(0, tokenInfo.getStart());
    assertEquals(7, tokenInfo.getEnd());
    assertEquals(1, tokenInfo.getPosition());
    assertTrue(tokenInfo.isMatch());
  }
  @Test
  public void testBuildPhases() throws Exception {
    final AnalysisResponseBase.TokenInfo tokenInfo = new AnalysisResponseBase.TokenInfo("text", null, "type", 0, 3, 1, false);
    NamedList nl = new NamedList();
    nl.add("Tokenizer", buildFakeTokenInfoList(6));
    nl.add("Filter1", buildFakeTokenInfoList(5));
    nl.add("Filter2", buildFakeTokenInfoList(4));
    nl.add("Filter3", buildFakeTokenInfoList(3));
    AnalysisResponseBase response = new AnalysisResponseBase() {
      @Override
      protected TokenInfo buildTokenInfo(NamedList tokenNL) {
        return tokenInfo;
      }
    };
    List<AnalysisResponseBase.AnalysisPhase> phases = response.buildPhases(nl);
    assertEquals(4, phases.size());
    assertPhase(phases.get(0), "Tokenizer", 6, tokenInfo);
    assertPhase(phases.get(1), "Filter1", 5, tokenInfo);
    assertPhase(phases.get(2), "Filter2", 4, tokenInfo);
    assertPhase(phases.get(3), "Filter3", 3, tokenInfo);
  }
  private List<NamedList> buildFakeTokenInfoList(int numberOfTokens) {
    List<NamedList> list = new ArrayList<NamedList>(numberOfTokens);
    for (int i = 0; i < numberOfTokens; i++) {
      list.add(new NamedList());
    }
    return list;
  }
  private void assertPhase(AnalysisResponseBase.AnalysisPhase phase, String expectedClassName, int expectedTokenCount, AnalysisResponseBase.TokenInfo expectedToken) {
    assertEquals(expectedClassName, phase.getClassName());
    List<AnalysisResponseBase.TokenInfo> tokens = phase.getTokens();
    assertEquals(expectedTokenCount, tokens.size());
    for (AnalysisResponseBase.TokenInfo token : tokens) {
      assertSame(expectedToken, token);
    }
  }
}
