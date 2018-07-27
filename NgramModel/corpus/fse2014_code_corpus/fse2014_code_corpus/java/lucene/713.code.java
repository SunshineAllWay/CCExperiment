package org.apache.lucene.analysis.sinks;
import org.apache.lucene.analysis.TeeSinkTokenFilter.SinkFilter;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
public class TokenTypeSinkFilter extends SinkFilter {
  private String typeToMatch;
  private TypeAttribute typeAtt;
  public TokenTypeSinkFilter(String typeToMatch) {
    this.typeToMatch = typeToMatch;
  }
  @Override
  public boolean accept(AttributeSource source) {
    if (typeAtt == null) {
      typeAtt = source.addAttribute(TypeAttribute.class);
    }
    return (typeToMatch.equals(typeAtt.type()));
  }
}
