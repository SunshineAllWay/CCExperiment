package org.apache.xml.utils;
public interface RawCharacterHandler
{
  public void charactersRaw(char ch[], int start, int length)
    throws javax.xml.transform.TransformerException;
}
