package org.apache.wml;
public interface WMLGoElement extends WMLElement {
  public void setSendreferer(String newValue);
  public String getSendreferer();
  public void setAcceptCharset(String newValue);
  public String getAcceptCharset();
  public void setHref(String newValue);
  public String getHref();
  public void setMethod(String newValue);
  public String getMethod();
}
