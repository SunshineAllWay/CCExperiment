package org.apache.solr.common.util;
import java.util.*;
import org.apache.solr.common.SolrException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class DOMUtil {
  public static Map<String,String> toMap(NamedNodeMap attrs) {
    return toMapExcept(attrs);
  }
  public static Map<String,String> toMapExcept(NamedNodeMap attrs, String... exclusions) {
    Map<String,String> args = new HashMap<String,String>();
    outer: for (int j=0; j<attrs.getLength(); j++) {
      Node attr = attrs.item(j);
      String attrName = attr.getNodeName();
      for (String ex : exclusions)
        if (ex.equals(attrName)) continue outer;
      String val = attr.getNodeValue();
      args.put(attrName, val);
    }
    return args;
  }
  public static Node getChild(Node node, String name) {
    if (!node.hasChildNodes()) return null;
    NodeList lst = node.getChildNodes();
    if (lst == null) return null;
    for (int i=0; i<lst.getLength(); i++) {
      Node child = lst.item(i);
      if (name.equals(child.getNodeName())) return child;
    }
    return null;
  }
  public static String getAttr(NamedNodeMap attrs, String name) {
    return getAttr(attrs,name,null);
  }
  public static String getAttr(Node nd, String name) {
    return getAttr(nd.getAttributes(), name);
  }
  public static String getAttr(NamedNodeMap attrs, String name, String missing_err) {
    Node attr = attrs==null? null : attrs.getNamedItem(name);
    if (attr==null) {
      if (missing_err==null) return null;
      throw new RuntimeException(missing_err + ": missing mandatory attribute '" + name + "'");
    }
    String val = attr.getNodeValue();
    return val;
  }
  public static String getAttr(Node node, String name, String missing_err) {
    return getAttr(node.getAttributes(), name, missing_err);
  }
  public static NamedList<Object> childNodesToNamedList(Node nd) {
    return nodesToNamedList(nd.getChildNodes());
  }
  public static List childNodesToList(Node nd) {
    return nodesToList(nd.getChildNodes());
  }
  public static NamedList<Object> nodesToNamedList(NodeList nlst) {
    NamedList<Object> clst = new NamedList<Object>();
    for (int i=0; i<nlst.getLength(); i++) {
      addToNamedList(nlst.item(i), clst, null);
    }
    return clst;
  }
  public static List nodesToList(NodeList nlst) {
    List lst = new ArrayList();
    for (int i=0; i<nlst.getLength(); i++) {
      addToNamedList(nlst.item(i), null, lst);
    }
    return lst;
  }
  @SuppressWarnings("unchecked")
  public static void addToNamedList(Node nd, NamedList nlst, List arr) {
    if (nd.getNodeType() != Node.ELEMENT_NODE) return;
    final String type = nd.getNodeName();
    final String name = getAttr(nd, "name");
    Object val=null;
    if ("lst".equals(type)) {
      val = childNodesToNamedList(nd);
    } else if ("arr".equals(type)) {
      val = childNodesToList(nd);
    } else {
      final String textValue = getText(nd);
      try {
        if ("str".equals(type)) {
          val = textValue;
        } else if ("int".equals(type)) {
          val = Integer.valueOf(textValue);
        } else if ("long".equals(type)) {
          val = Long.valueOf(textValue);
        } else if ("float".equals(type)) {
          val = Float.valueOf(textValue);
        } else if ("double".equals(type)) {
          val = Double.valueOf(textValue);
        } else if ("bool".equals(type)) {
          val = StrUtils.parseBool(textValue);
        }
      } catch (NumberFormatException nfe) {
        throw new SolrException
          (SolrException.ErrorCode.SERVER_ERROR,
           "Value " + (null != name ? ("of '" +name+ "' ") : "") +
           "can not be parsed as '" +type+ "': \"" + textValue + "\"",
           nfe);
      }
    }
    if (nlst != null) nlst.add(name,val);
    if (arr != null) arr.add(val);
  }
  public static String getText(Node nd) {
    short type = nd.getNodeType();
    switch (type) {
    case Node.DOCUMENT_NODE: 
    case Node.DOCUMENT_TYPE_NODE: 
    case Node.NOTATION_NODE: 
      return null;
    }
    StringBuilder sb = new StringBuilder();
    getText(nd, sb);
    return sb.toString();
  }
  private static void getText(Node nd, StringBuilder buf) {
    short type = nd.getNodeType();
    switch (type) {
    case Node.ELEMENT_NODE: 
    case Node.ENTITY_NODE: 
    case Node.ENTITY_REFERENCE_NODE: 
    case Node.DOCUMENT_FRAGMENT_NODE:
      NodeList childs = nd.getChildNodes();
      for (int i = 0; i < childs.getLength(); i++) {
        Node child = childs.item(i);
        short childType = child.getNodeType();
        if (childType != Node.COMMENT_NODE &&
            childType != Node.PROCESSING_INSTRUCTION_NODE) {
          getText(child, buf);
        }
      }
      break;
    case Node.ATTRIBUTE_NODE: 
    case Node.TEXT_NODE: 
    case Node.CDATA_SECTION_NODE: 
    case Node.COMMENT_NODE: 
    case Node.PROCESSING_INSTRUCTION_NODE: 
      buf.append(nd.getNodeValue());
      break;
    case Node.DOCUMENT_NODE: 
    case Node.DOCUMENT_TYPE_NODE: 
    case Node.NOTATION_NODE: 
    default:
    }
  }
  public static void substituteSystemProperties(Node node) {
    substituteProperties(node, null);
  }
  public static void substituteProperties(Node node, Properties properties) {
    Node child;
    Node next = node.getFirstChild();
    while ((child = next) != null) {
      next = child.getNextSibling();
      if (child.getNodeType() == Node.TEXT_NODE) {
        child.setNodeValue(substituteProperty(child.getNodeValue(), properties));
      } else if (child.getNodeType() == Node.ELEMENT_NODE) {
        NamedNodeMap attributes = child.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
          Node attribute = attributes.item(i);
          attribute.setNodeValue(substituteProperty(attribute.getNodeValue(), properties));
        }
        substituteProperties(child, properties);
      }
    }
  }
  private static String substituteProperty(String value, Properties coreProperties) {
    if (value == null || value.indexOf('$') == -1) {
      return value;
    }
    List<String> fragments = new ArrayList<String>();
    List<String> propertyRefs = new ArrayList<String>();
    parsePropertyString(value, fragments, propertyRefs);
    StringBuilder sb = new StringBuilder();
    Iterator<String> i = fragments.iterator();
    Iterator<String> j = propertyRefs.iterator();
    while (i.hasNext()) {
      String fragment = i.next();
      if (fragment == null) {
        String propertyName = j.next();
        String defaultValue = null;
        int colon_index = propertyName.indexOf(':');
        if (colon_index > -1) {
          defaultValue = propertyName.substring(colon_index + 1);
          propertyName = propertyName.substring(0,colon_index);
        }
        if (coreProperties != null) {
          fragment = coreProperties.getProperty(propertyName);
        }
        if (fragment == null) {
          fragment = System.getProperty(propertyName, defaultValue);
        }
        if (fragment == null) {
          throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "No system property or default value specified for " + propertyName);
        }
      }
      sb.append(fragment);
    }
    return sb.toString();
  }
  private static void parsePropertyString(String value, List<String> fragments, List<String> propertyRefs) {
      int prev = 0;
      int pos;
      while ((pos = value.indexOf("$", prev)) >= 0) {
          if (pos > 0) {
              fragments.add(value.substring(prev, pos));
          }
          if (pos == (value.length() - 1)) {
              fragments.add("$");
              prev = pos + 1;
          } else if (value.charAt(pos + 1) != '{') {
              if (value.charAt(pos + 1) == '$') {
                  fragments.add("$");
                  prev = pos + 2;
              } else {
                  fragments.add(value.substring(pos, pos + 2));
                  prev = pos + 2;
              }
          } else {
              int endName = value.indexOf('}', pos);
              if (endName < 0) {
                throw new RuntimeException("Syntax error in property: " + value);
              }
              String propertyName = value.substring(pos + 2, endName);
              fragments.add(null);
              propertyRefs.add(propertyName);
              prev = endName + 1;
          }
      }
      if (prev < value.length()) {
          fragments.add(value.substring(prev));
      }
  }
}
