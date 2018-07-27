package org.apache.xml.utils;
public interface PrefixResolver
{
  String getNamespaceForPrefix(String prefix);
  String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context);
  public String getBaseIdentifier();
  public boolean handlesNullPrefixes();
}
