package org.apache.solr.handler.dataimport;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class XPathRecordReader {
  private Node rootNode = new Node("/", null);
  public static final int FLATTEN = 1;
  public XPathRecordReader(String forEachXpath) {
    String[] splits = forEachXpath.split("\\|");
    for (String split : splits) {
      split = split.trim();
      if (split.startsWith("//"))
         throw new RuntimeException("forEach cannot start with '//': " + split);
      if (split.length() == 0)
        continue;
      addField0(split, split, false, true, 0);
    }
  }
  public synchronized XPathRecordReader addField(String name, String xpath, boolean multiValued) {
    addField0(xpath, name, multiValued, false, 0);
    return this;
  }
  public synchronized XPathRecordReader addField(String name, String xpath, boolean multiValued, int flags) {
    addField0(xpath, name, multiValued, false, flags);
    return this;
  }
  private void addField0(String xpath, String name, boolean multiValued,
                         boolean isRecord, int flags) {
    if (!xpath.startsWith("/"))
      throw new RuntimeException("xpath must start with '/' : " + xpath);
    List<String> paths = splitEscapeQuote(xpath);
    if ("".equals(paths.get(0).trim()))
      paths.remove(0);
    rootNode.build(paths, name, multiValued, isRecord, flags);
    rootNode.buildOptimise(null);
  }
  public List<Map<String, Object>> getAllRecords(Reader r) {
    final List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    streamRecords(r, new Handler() {
      public void handle(Map<String, Object> record, String s) {
        results.add(record);
      }
    });
    return results;
  }
  public void streamRecords(Reader r, Handler handler) {
    try {
      XMLStreamReader parser = factory.createXMLStreamReader(r);
      rootNode.parse(parser, handler, new HashMap<String, Object>(),
              new Stack<Set<String>>(), false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private static class Node {
    String name;      
    String fieldName; 
    String xpathName; 
    String forEachPath; 
    List<Node> attributes; 
    List<Node> childNodes; 
    List<Node> wildCardNodes; 
    List<Map.Entry<String, String>> attribAndValues;
    Node wildAncestor; 
    Node parent; 
    boolean hasText=false; 
    boolean multiValued=false; 
    boolean isRecord=false; 
    private boolean flatten; 
    public Node(String name, Node p) {
      xpathName = this.name = name;
      parent = p;
    }
    public Node(String name, String fieldName, boolean multiValued) {
      this.name = name;               
      this.fieldName = fieldName;     
      this.multiValued = multiValued; 
    }
    private void parse(XMLStreamReader parser, 
                       Handler handler,
                       Map<String, Object> values, 
                       Stack<Set<String>> stack, 
                       boolean recordStarted
                       ) throws IOException, XMLStreamException {
      Set<String> valuesAddedinThisFrame = null;
      if (isRecord) {
        recordStarted = true;
        valuesAddedinThisFrame = new HashSet<String>();
        stack.push(valuesAddedinThisFrame);
      } else if (recordStarted) {
        valuesAddedinThisFrame = stack.peek();
      }
      try {
        if (attributes != null) {
          for (Node node : attributes) {
            String value = parser.getAttributeValue(null, node.name);
            if (value != null || (recordStarted && !isRecord)) {
              putText(values, value, node.fieldName, node.multiValued);
              valuesAddedinThisFrame.add(node.fieldName);
            }
          }
        }
        Set<Node> childrenFound = new HashSet<Node>();
        int event = -1;
        int flattenedStarts=0; 
        StringBuilder text = new StringBuilder();
        while (true) {  
          event = parser.next();
          if (event == END_ELEMENT) {
            if (flattenedStarts > 0) flattenedStarts--;
            else {
              if (text.length() > 0 && valuesAddedinThisFrame != null) {
                valuesAddedinThisFrame.add(fieldName);
                putText(values, text.toString(), fieldName, multiValued);
              }
              if (isRecord) handler.handle(getDeepCopy(values), forEachPath);
              if (childNodes != null && recordStarted && !isRecord && !childrenFound.containsAll(childNodes)) {
                for (Node n : childNodes) {
                  if (!childrenFound.contains(n)) n.putNulls(values);
                }
              }
              return;
            }
          }
          else if (hasText && (event==CDATA || event==CHARACTERS || event==SPACE)) {
            text.append(parser.getText());
          } 
          else if (event == START_ELEMENT) {
            if ( flatten ) 
               flattenedStarts++;
            else 
               handleStartElement(parser, childrenFound, handler, values, stack, recordStarted);
          }
          else if (event == END_DOCUMENT) return;
          }
        }finally {
        if ((isRecord || !recordStarted) && !stack.empty()) {
          Set<String> cleanThis = stack.pop();
          if (cleanThis != null) {
            for (String fld : cleanThis) values.remove(fld);
          }
        }
      }
    }
    private void handleStartElement(XMLStreamReader parser, Set<Node> childrenFound,
                                    Handler handler, Map<String, Object> values,
                                    Stack<Set<String>> stack, boolean recordStarted)
            throws IOException, XMLStreamException {
      Node n = getMatchingNode(parser,childNodes);
      Map<String, Object> decends=new HashMap<String, Object>();
      if (n != null) {
        childrenFound.add(n);
        n.parse(parser, handler, values, stack, recordStarted);
        return;
        }
      Node dn = this; 
      do {
        if (dn.wildCardNodes != null) {
          n = getMatchingNode(parser, dn.wildCardNodes);
          if (n != null) {
            childrenFound.add(n);
            n.parse(parser, handler, values, stack, recordStarted);
            break;
          }
          for (Node nn : dn.wildCardNodes) decends.put(nn.name, nn);
        }
        dn = dn.wildAncestor; 
      } while (dn != null) ;
      if (n == null) {
        int count = 1; 
        while (count != 0) {
          int token = parser.next();
          if (token == START_ELEMENT) {
            Node nn = (Node) decends.get(parser.getLocalName());
            if (nn != null) {
              childrenFound.add(nn);
              nn.parse(parser, handler, values, stack, recordStarted);
            } 
            else count++;
          } 
          else if (token == END_ELEMENT) count--;
        }
      }
    }
    private Node getMatchingNode(XMLStreamReader parser,List<Node> searchL){
      if (searchL == null)
        return null;
      String localName = parser.getLocalName();
      for (Node n : searchL) {
        if (n.name.equals(localName)) {
          if (n.attribAndValues == null)
            return n;
          if (checkForAttributes(parser, n.attribAndValues))
            return n;
        }
      }
      return null;
    }
    private boolean checkForAttributes(XMLStreamReader parser,
                                       List<Map.Entry<String, String>> attrs) {
      for (Map.Entry<String, String> e : attrs) {
        String val = parser.getAttributeValue(null, e.getKey());
        if (val == null)
          return false;
        if (e.getValue() != null && !e.getValue().equals(val))
          return false;
      }
      return true;
    }
    private void putNulls(Map<String, Object> values) {
      if (attributes != null) {
        for (Node n : attributes) {
          if (n.multiValued)
            putText(values, null, n.fieldName, true);
        }
      }
      if (hasText && multiValued)
        putText(values, null, fieldName, true);
      if (childNodes != null) {
        for (Node childNode : childNodes)
          childNode.putNulls(values);
      }
    }
    @SuppressWarnings("unchecked")
    private void putText(Map<String, Object> values, String value,
                         String fieldName, boolean multiValued) {
      if (multiValued) {
        List<String> v = (List<String>) values.get(fieldName);
        if (v == null) {
          v = new ArrayList<String>();
          values.put(fieldName, v);
        }
        v.add(value);
      } else {
        values.put(fieldName, value);
      }
    }
    private void buildOptimise(Node wa) {
     wildAncestor=wa;
     if ( wildCardNodes != null ) wa = this;
     if ( childNodes != null )
       for ( Node n : childNodes ) n.buildOptimise(wa);
     }
    private void build(
        List<String> paths,   
        String fieldName,     
        boolean multiValued,  
        boolean record,       
        int flags             
        ) {
      String xpseg = paths.remove(0); 
      if (paths.isEmpty() && xpseg.startsWith("@")) {
        if (attributes == null) {
          attributes = new ArrayList<Node>();
        }
        xpseg = xpseg.substring(1); 
        attributes.add(new Node(xpseg, fieldName, multiValued));
      }
      else if ( xpseg.length() == 0) {
        xpseg = paths.remove(0); 
        if (wildCardNodes == null) wildCardNodes = new ArrayList<Node>();
        Node n = getOrAddNode(xpseg, wildCardNodes);
        if (paths.isEmpty()) {
          n.hasText = true;        
          n.fieldName = fieldName; 
          n.multiValued = multiValued; 
          n.flatten = flags == FLATTEN; 
        }
        else {
          n.build(paths, fieldName, multiValued, record, flags);
        }
      }
      else {
        if (childNodes == null)
          childNodes = new ArrayList<Node>();
        Node n = getOrAddNode(xpseg,childNodes);
        if (paths.isEmpty()) {
          if (record) {
            n.isRecord = true; 
            n.forEachPath = fieldName; 
          } else {
            n.hasText = true;        
            n.fieldName = fieldName; 
            n.multiValued = multiValued; 
            n.flatten = flags == FLATTEN; 
          }
        } else {
          n.build(paths, fieldName, multiValued, record, flags);
        }
      }
    }
    private Node getOrAddNode(String xpathName, List<Node> searchList ) {
      for (Node n : searchList)
        if (n.xpathName.equals(xpathName)) return n;
      Node n = new Node(xpathName, this); 
      Matcher m = ATTRIB_PRESENT_WITHVAL.matcher(xpathName);
      if (m.find()) {
        n.name = m.group(1);
        int start = m.start(2);
        while (true) {
          HashMap<String, String> attribs = new HashMap<String, String>();
          if (!m.find(start))
            break;
          attribs.put(m.group(3), m.group(5));
          start = m.end(6);
          if (n.attribAndValues == null)
            n.attribAndValues = new ArrayList<Map.Entry<String, String>>();
          n.attribAndValues.addAll(attribs.entrySet());
        }
      }
      searchList.add(n);
      return n;
    }
    private static Map<String, Object> getDeepCopy(Map<String, Object> values) {
      Map<String, Object> result = new HashMap<String, Object>();
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        if (entry.getValue() instanceof List) {
          result.put(entry.getKey(), new ArrayList((List) entry.getValue()));
        } else {
          result.put(entry.getKey(), entry.getValue());
        }
      }
      return result;
    }
  } 
  private static List<String> splitEscapeQuote(String str) {
    List<String> result = new LinkedList<String>();
    String[] ss = str.split("/");
    for (int i=0; i<ss.length; i++) { 
      StringBuilder sb = new StringBuilder();
      int quoteCount = 0;
      while (true) {
        sb.append(ss[i]);
        for (int j=0; j<ss[i].length(); j++)
            if (ss[i].charAt(j) == '\'') quoteCount++;
        if ((quoteCount % 2) == 0) break;
        i++;
        sb.append("/");
      }
      result.add(sb.toString());
    }
    return result;
  }
  static XMLInputFactory factory = XMLInputFactory.newInstance();
  static{
    factory.setProperty(XMLInputFactory.IS_VALIDATING , Boolean.FALSE); 
    factory.setProperty(XMLInputFactory.SUPPORT_DTD , Boolean.FALSE);
  }
  public static interface Handler {
    public void handle(Map<String, Object> record, String xpath);
  }
  private static final Pattern ATTRIB_PRESENT_WITHVAL = Pattern
          .compile("(\\S*?)?(\\[@)(\\S*?)(='(.*?)')?(\\])");
}
