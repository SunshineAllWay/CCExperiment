package org.apache.wml;
public interface WMLPostfieldElement extends WMLElement {
  public void setValue(String newValue);
  public String getValue();
  public void setName(String newValue);
  public String getName();
}
