package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class AnalysisResponseBase extends SolrResponseBase {
  protected List<AnalysisPhase> buildPhases(NamedList<Object> phaseNL) {
    List<AnalysisPhase> phases = new ArrayList<AnalysisPhase>(phaseNL.size());
    for (Map.Entry<String, Object> phaseEntry : phaseNL) {
      AnalysisPhase phase = new AnalysisPhase(phaseEntry.getKey());
      List<NamedList> tokens = (List<NamedList>) phaseEntry.getValue();
      for (NamedList token : tokens) {
        TokenInfo tokenInfo = buildTokenInfo(token);
        phase.addTokenInfo(tokenInfo);
      }
      phases.add(phase);
    }
    return phases;
  }
  protected TokenInfo buildTokenInfo(NamedList tokenNL) {
    String text = (String) tokenNL.get("text");
    String rawText = (String) tokenNL.get("rawText");
    String type = (String) tokenNL.get("type");
    int start = (Integer) tokenNL.get("start");
    int end = (Integer) tokenNL.get("end");
    int position = (Integer) tokenNL.get("position");
    Boolean match = (Boolean) tokenNL.get("match");
    return new TokenInfo(text, rawText, type, start, end, position, (match == null ? false : match));
  }
  public static class AnalysisPhase {
    private final String className;
    private List<TokenInfo> tokens = new ArrayList<TokenInfo>();
    AnalysisPhase(String className) {
      this.className = className;
    }
    public String getClassName() {
      return className;
    }
    private void addTokenInfo(TokenInfo tokenInfo) {
      tokens.add(tokenInfo);
    }
    public List<TokenInfo> getTokens() {
      return tokens;
    }
  }
  public static class TokenInfo {
    private final String text;
    private final String rawText;
    private final String type;
    private final int start;
    private final int end;
    private final int position;
    private final boolean match;
    TokenInfo(String text, String rawText, String type, int start, int end, int position, boolean match) {
      this.text = text;
      this.rawText = rawText;
      this.type = type;
      this.start = start;
      this.end = end;
      this.position = position;
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
    public int getPosition() {
      return position;
    }
    public boolean isMatch() {
      return match;
    }
  }
}
