package org.apache.wml;
public interface WMLSetvarElement extends WMLElement {
  public void setValue(String newValue);
  public String getValue();
  public void setName(String newValue);
  public String getName();
}
