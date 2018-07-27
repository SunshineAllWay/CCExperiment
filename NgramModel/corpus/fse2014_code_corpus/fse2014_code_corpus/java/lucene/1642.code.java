package org.apache.lucene.search;
public class ComplexExplanation extends Explanation {
  private Boolean match;
  public ComplexExplanation() {
    super();
  }
  public ComplexExplanation(boolean match, float value, String description) {
    super(value, description);
    this.match = Boolean.valueOf(match);
  }
  public Boolean getMatch() { return match; }
  public void setMatch(Boolean match) { this.match = match; }
  @Override
  public boolean isMatch() {
    Boolean m = getMatch();
    return (null != m ? m.booleanValue() : super.isMatch());
  }
  @Override
  protected String getSummary() {
    if (null == getMatch())
      return super.getSummary();
    return getValue() + " = "
      + (isMatch() ? "(MATCH) " : "(NON-MATCH) ")
      + getDescription();
  }
}
