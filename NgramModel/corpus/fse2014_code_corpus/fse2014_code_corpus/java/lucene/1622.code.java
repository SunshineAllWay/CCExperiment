package org.apache.lucene.messages;
import java.io.Serializable;
import java.util.Locale;
public interface Message extends Serializable {
  public String getKey();
  public Object[] getArguments();
  public String getLocalizedMessage();
  public String getLocalizedMessage(Locale locale);
}
