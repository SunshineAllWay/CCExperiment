package org.apache.solr.response;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.XML;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;
import java.io.Writer;
import java.io.IOException;
import java.util.*;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Document;
final public class XMLWriter {
  public static float CURRENT_VERSION=2.2f;
  private static final char[] XML_START1="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".toCharArray();
  private static final char[] XML_STYLESHEET="<?xml-stylesheet type=\"text/xsl\" href=\"/admin/".toCharArray();
  private static final char[] XML_STYLESHEET_END=".xsl\"?>\n".toCharArray();
  private static final char[] XML_START2_SCHEMA=(
  "<response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
  +" xsi:noNamespaceSchemaLocation=\"http://pi.cnet.com/cnet-search/response.xsd\">\n"
          ).toCharArray();
  private static final char[] XML_START2_NOSCHEMA=(
  "<response>\n"
          ).toCharArray();
  public static void writeResponse(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    String ver = req.getParams().get(CommonParams.VERSION);
    writer.write(XML_START1);
    String stylesheet = req.getParams().get("stylesheet");
    if (stylesheet != null && stylesheet.length() > 0) {
      writer.write(XML_STYLESHEET);
      writer.write(stylesheet);
      writer.write(XML_STYLESHEET_END);
    }
    String noSchema = req.getParams().get("noSchema");
    if (false && noSchema == null)
      writer.write(XML_START2_SCHEMA);
    else
      writer.write(XML_START2_NOSCHEMA);
    XMLWriter xw = new XMLWriter(writer, req.getSchema(), req, ver);
    xw.defaultFieldList = rsp.getReturnFields();
    String indent = req.getParams().get("indent");
    if (indent != null) {
      if ("".equals(indent) || "off".equals(indent)) {
        xw.setIndent(false);
      } else {
        xw.setIndent(true);
      }
    }
    NamedList lst = rsp.getValues();
    Boolean omitHeader = req.getParams().getBool(CommonParams.OMIT_HEADER);
    if(omitHeader != null && omitHeader) lst.remove("responseHeader");
    int sz = lst.size();
    int start=0;
    if (xw.version<=2100 && sz>0) {
      Object header = lst.getVal(0);
      if (header instanceof NamedList && "responseHeader".equals(lst.getName(0))) {
        writer.write("<responseHeader>");
        xw.incLevel();
        NamedList nl = (NamedList)header;
        for (int i=0; i<nl.size(); i++) {
          String name = nl.getName(i);
          Object val = nl.getVal(i);
          if ("status".equals(name) || "QTime".equals(name)) {
            xw.writePrim(name,null,val.toString(),false);
          } else {
            xw.writeVal(name,val);
          }
        }
        xw.decLevel();
        writer.write("</responseHeader>");
        start=1;
      }
    }
    for (int i=start; i<sz; i++) {
      xw.writeVal(lst.getName(i),lst.getVal(i));
    }
    writer.write("\n</response>\n");
  }
  private final Writer writer;
  private final IndexSchema schema; 
  private final SolrQueryRequest request; 
  private int level;
  private boolean defaultIndent=false;
  private boolean doIndent=false;
  private Set<String> defaultFieldList;
  private final int indentThreshold=0;
  final int version;
  private final ArrayList tlst = new ArrayList();
  private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
  private final StringBuilder sb = new StringBuilder();
  public XMLWriter(Writer writer, IndexSchema schema, SolrQueryRequest req, String version) {
    this.writer = writer;
    this.schema = schema;
    this.request = req;
    float ver = version==null? CURRENT_VERSION : Float.parseFloat(version);
    this.version = (int)(ver*1000);
  }
  public void setLevel(int level) { this.level = level; }
  public int level() { return level; }
  public int incLevel() { return ++level; }
  public int decLevel() { return --level; }
  public void setIndent(boolean doIndent) {
    this.doIndent = doIndent;
    defaultIndent = doIndent;
  }
  public void writeAttr(String name, String val) throws IOException {
    writeAttr(name, val, true);
  }
  public void writeAttr(String name, String val, boolean escape) throws IOException{
    if (val != null) {
      writer.write(' ');
      writer.write(name);
      writer.write("=\"");
      if(escape){
        XML.escapeAttributeValue(val, writer);
      } else {
        writer.write(val);
      }
      writer.write('"');
    }
  }
  public void startTag(String tag, Map<String,String> attributes, boolean closeTag, boolean escape) throws IOException {
    if (doIndent) indent();
    writer.write('<');
    writer.write(tag);
    if(!attributes.isEmpty()) {
      for (Map.Entry<String, String> entry : attributes.entrySet()) {
        writeAttr(entry.getKey(), entry.getValue(), escape);
      }
    }
    if (closeTag) {
      writer.write("/>");
    } else {
      writer.write('>');
    }
  }
  public void writeCdataTag(String tag, Map<String,String> attributes, String cdata, boolean escapeCdata, boolean escapeAttr) throws IOException {
    if (doIndent) indent();
    writer.write('<');
    writer.write(tag);
    if (!attributes.isEmpty()) {
      for (Map.Entry<String, String> entry : attributes.entrySet()) {
        writeAttr(entry.getKey(), entry.getValue(), escapeAttr);
      }
    }
    writer.write('>');
    if (cdata != null && cdata.length() > 0) {
      if (escapeCdata) {
        XML.escapeCharData(cdata, writer);
      } else {
        writer.write(cdata, 0, cdata.length());
      }
    }
    writer.write("</");
    writer.write(tag);
    writer.write('>');
  }
  public void startTag(String tag, String name, boolean closeTag) throws IOException {
    if (doIndent) indent();
    writer.write('<');
    writer.write(tag);
    if (name!=null) {
      writeAttr("name", name);
      if (closeTag) {
        writer.write("/>");
      } else {
        writer.write(">");
      }
    } else {
      if (closeTag) {
        writer.write("/>");
      } else {
        writer.write('>');
      }
    }
  }
  private static final String[] indentArr = new String[] {
    "\n",
    "\n ",
    "\n  ",
    "\n\t",
    "\n\t ",
    "\n\t  ",  
    "\n\t\t" };
  public void indent() throws IOException {
     indent(level);
  }
  public void indent(int lev) throws IOException {
    int arrsz = indentArr.length-1;
    String istr = indentArr[ lev > arrsz ? arrsz : lev ];
    writer.write(istr);
  }
  private static final Comparator fieldnameComparator = new Comparator() {
    public int compare(Object o, Object o1) {
      Fieldable f1 = (Fieldable)o; Fieldable f2 = (Fieldable)o1;
      int cmp = f1.name().compareTo(f2.name());
      return cmp;
    }
  };
  public final void writeDoc(String name, Document doc, Set<String> returnFields, float score, boolean includeScore) throws IOException {
    startTag("doc", name, false);
    incLevel();
    if (includeScore) {
      writeFloat("score", score);
    }
    tlst.clear();
    for (Object obj : doc.getFields()) {
      Fieldable ff = (Fieldable)obj;
      if (returnFields!=null && !returnFields.contains(ff.name())) {
        continue;
      }
      tlst.add(ff);
    }
    Collections.sort(tlst, fieldnameComparator);
    int sz = tlst.size();
    int fidx1 = 0, fidx2 = 0;
    while (fidx1 < sz) {
      Fieldable f1 = (Fieldable)tlst.get(fidx1);
      String fname = f1.name();
      fidx2 = fidx1+1;
      while (fidx2 < sz && fname.equals(((Fieldable)tlst.get(fidx2)).name()) ) {
        fidx2++;
      }
      SchemaField sf = schema.getFieldOrNull(fname);
      if( sf == null ) {
        sf = new SchemaField( fname, new TextField() );
      }
      if (fidx1+1 == fidx2) {
        if (version>=2100 && sf.multiValued()) {
          startTag("arr",fname,false);
          doIndent=false;
          sf.write(this, null, f1);
          writer.write("</arr>");
          doIndent=defaultIndent;
        } else {
          sf.write(this, f1.name(), f1);
        }
      } else {
        startTag("arr",fname,false);
        incLevel();
        doIndent=false;
        int cnt=0;
        for (int i=fidx1; i<fidx2; i++) {
          if (defaultIndent && ++cnt==4) { 
            indent();
            cnt=0;
          }
          sf.write(this, null, (Fieldable)tlst.get(i));
        }
        decLevel();
        writer.write("</arr>");
        doIndent=defaultIndent;
      }
      fidx1 = fidx2;
    }
    decLevel();
    if (doIndent) indent();
    writer.write("</doc>");
  }
  final void writeDoc(String name, SolrDocument doc, Set<String> returnFields, boolean includeScore) throws IOException {
    startTag("doc", name, false);
    incLevel();
    if (includeScore && returnFields != null ) {
      returnFields.add( "score" );
    }
    for (String fname : doc.getFieldNames()) {
      if (returnFields!=null && !returnFields.contains(fname)) {
        continue;
      }
      Object val = doc.getFieldValue(fname);
      if (val instanceof Collection) {
        writeVal(fname, val);
      } else {
        SchemaField sf = schema.getFieldOrNull(fname);
        if (version>=2100 && sf!=null && sf.multiValued()) {
          startTag("arr",fname,false);
          doIndent=false;
          writeVal(fname, val);
          writer.write("</arr>");
          doIndent=defaultIndent;          
        } else {
          writeVal(fname, val);          
        }
      }
    }
    decLevel();
    if (doIndent) indent();
    writer.write("</doc>");
  }
  private static interface DocumentListInfo {
    Float getMaxScore();
    int getCount();
    long getNumFound();
    long getStart();
    void writeDocs( boolean includeScore, Set<String> fields ) throws IOException;
  }
  private final void writeDocuments(
      String name, 
      DocumentListInfo docs, 
      Set<String> fields) throws IOException 
  {
    boolean includeScore=false;
    if (fields!=null) {
      includeScore = fields.contains("score");
      if (fields.size()==0 || (fields.size()==1 && includeScore) || fields.contains("*")) {
        fields=null;  
      }
    }
    int sz=docs.getCount();
    if (doIndent) indent();
    writer.write("<result");
    writeAttr("name",name);
    writeAttr("numFound",Long.toString(docs.getNumFound()));  
    writeAttr("start",Long.toString(docs.getStart()));        
    if (includeScore && docs.getMaxScore()!=null) {
      writeAttr("maxScore",Float.toString(docs.getMaxScore()));
    }
    if (sz==0) {
      writer.write("/>");
      return;
    } else {
      writer.write('>');
    }
    incLevel();
    docs.writeDocs(includeScore, fields);
    decLevel();
    if (doIndent) indent();
    writer.write("</result>");
  }
  public final void writeSolrDocumentList(String name, final SolrDocumentList docs, Set<String> fields) throws IOException 
  {
    this.writeDocuments( name, new DocumentListInfo() 
    {  
      public int getCount() {
        return docs.size();
      }
      public Float getMaxScore() {
        return docs.getMaxScore();
      }
      public long getNumFound() {
        return docs.getNumFound();
      }
      public long getStart() {
        return docs.getStart();
      }
      public void writeDocs(boolean includeScore, Set<String> fields) throws IOException {
        for( SolrDocument doc : docs ) {
          writeDoc(null, doc, fields, includeScore);
        }
      }
    }, fields );
  }
  public final void writeDocList(String name, final DocList ids, Set<String> fields) throws IOException 
  {
    this.writeDocuments( name, new DocumentListInfo() 
    {  
      public int getCount() {
        return ids.size();
      }
      public Float getMaxScore() {
        return ids.maxScore();
      }
      public long getNumFound() {
        return ids.matches();
      }
      public long getStart() {
        return ids.offset();
      }
      public void writeDocs(boolean includeScore, Set<String> fields) throws IOException {
        SolrIndexSearcher searcher = request.getSearcher();
        DocIterator iterator = ids.iterator();
        int sz = ids.size();
        includeScore = includeScore && ids.hasScores();
        for (int i=0; i<sz; i++) {
          int id = iterator.nextDoc();
          Document doc = searcher.doc(id, fields);
          writeDoc(null, doc, fields, (includeScore ? iterator.score() : 0.0f), includeScore);
        }
      }
    }, fields );
  }
  public void writeVal(String name, Object val) throws IOException {
    if (val==null) {
      writeNull(name);
    } else if (val instanceof String) {
      writeStr(name, (String)val);
    } else if (val instanceof Integer) {
      writeInt(name, val.toString());
    } else if (val instanceof Boolean) {
      writeBool(name, val.toString());
    } else if (val instanceof Long) {
      writeLong(name, val.toString());
    } else if (val instanceof Date) {
      writeDate(name,(Date)val);
    } else if (val instanceof Float) {
      writeFloat(name, ((Float)val).floatValue());
    } else if (val instanceof Double) {
      writeDouble(name, ((Double)val).doubleValue());
    } else if (val instanceof Document) {
      writeDoc(name, (Document)val, defaultFieldList, 0.0f, false);
    } else if (val instanceof DocList) {
      writeDocList(name, (DocList)val, defaultFieldList);
    }else if (val instanceof SolrDocumentList) {
      writeSolrDocumentList(name, (SolrDocumentList)val, defaultFieldList);  
    }else if (val instanceof DocSet) {
    } else if (val instanceof Map) {
      writeMap(name, (Map)val);
    } else if (val instanceof NamedList) {
      writeNamedList(name, (NamedList)val);
    } else if (val instanceof Iterable) {
      writeArray(name,((Iterable)val).iterator());
    } else if (val instanceof Object[]) {
      writeArray(name,(Object[])val);
    } else if (val instanceof Iterator) {
      writeArray(name,(Iterator)val);
    } else {
      writeStr(name, val.getClass().getName() + ':' + val.toString());
    }
  }
  public void writeNamedList(String name, NamedList val) throws IOException {
    int sz = val.size();
    startTag("lst", name, sz<=0);
    if (sz<indentThreshold) {
      doIndent=false;
    }
    incLevel();
    for (int i=0; i<sz; i++) {
      writeVal(val.getName(i),val.getVal(i));
    }
    decLevel();
    if (sz > 0) {
      if (doIndent) indent();
      writer.write("</lst>");
    }
  }
  public void writeMap(String name, Map val) throws IOException {
    Map map = val;
    int sz = map.size();
    startTag("lst", name, sz<=0);
    incLevel();
    for (Map.Entry entry : (Set<Map.Entry>)map.entrySet()) {
      String k = (String)entry.getKey();
      Object v = entry.getValue();
      writeVal(k,v);
    }
    decLevel();
    if (sz > 0) {
      if (doIndent) indent();
      writer.write("</lst>");
    }
  }
  public void writeArray(String name, Object[] val) throws IOException {
    writeArray(name, Arrays.asList(val).iterator());
  }
  public void writeArray(String name, Iterator iter) throws IOException {
    if( iter.hasNext() ) {
      startTag("arr", name, false );
      incLevel();
      while( iter.hasNext() ) {
        writeVal(null, iter.next());
      }
      decLevel();
      if (doIndent) indent();
      writer.write("</arr>");
    }
    else {
      startTag("arr", name, true );
    }
  }
  public void writeNull(String name) throws IOException {
    writePrim("null",name,"",false);
  }
  public void writeStr(String name, String val) throws IOException {
    writePrim("str",name,val,true);
  }
  public void writeInt(String name, String val) throws IOException {
    writePrim("int",name,val,false);
  }
  public void writeInt(String name, int val) throws IOException {
    writeInt(name,Integer.toString(val));
  }
  public void writeLong(String name, String val) throws IOException {
    writePrim("long",name,val,false);
  }
  public void writeLong(String name, long val) throws IOException {
    writeLong(name,Long.toString(val));
  }
  public void writeBool(String name, String val) throws IOException {
    writePrim("bool",name,val,false);
  }
  public void writeBool(String name, boolean val) throws IOException {
    writeBool(name,Boolean.toString(val));
  }
  public void writeShort(String name, String val) throws IOException {
    writePrim("short",name,val,false);
  }
  public void writeShort(String name, short val) throws IOException {
    writeInt(name,Short.toString(val));
  }
  public void writeByte(String name, String val) throws IOException {
    writePrim("byte",name,val,false);
  }
  public void writeByte(String name, byte val) throws IOException {
    writeInt(name,Byte.toString(val));
  }
  public void writeFloat(String name, String val) throws IOException {
    writePrim("float",name,val,false);
  }
  public void writeFloat(String name, float val) throws IOException {
    writeFloat(name,Float.toString(val));
  }
  public void writeDouble(String name, String val) throws IOException {
    writePrim("double",name,val,false);
  }
  public void writeDouble(String name, double val) throws IOException {
    writeDouble(name,Double.toString(val));
  }
  public void writeDate(String name, Date val) throws IOException {
    cal.setTime(val);
    sb.setLength(0);
    int i = cal.get(Calendar.YEAR);
    sb.append(i);
    sb.append('-');
    i = cal.get(Calendar.MONTH) + 1;  
    if (i<10) sb.append('0');
    sb.append(i);
    sb.append('-');
    i=cal.get(Calendar.DAY_OF_MONTH);
    if (i<10) sb.append('0');
    sb.append(i);
    sb.append('T');
    i=cal.get(Calendar.HOUR_OF_DAY); 
    if (i<10) sb.append('0');
    sb.append(i);
    sb.append(':');
    i=cal.get(Calendar.MINUTE);
    if (i<10) sb.append('0');
    sb.append(i);
    sb.append(':');
    i=cal.get(Calendar.SECOND);
    if (i<10) sb.append('0');
    sb.append(i);
    i=cal.get(Calendar.MILLISECOND);
    if (i != 0) {
      sb.append('.');
      if (i<100) sb.append('0');
      if (i<10) sb.append('0');
      sb.append(i);
      int lastIdx = sb.length()-1;
      if (sb.charAt(lastIdx)=='0') {
        lastIdx--;
        if (sb.charAt(lastIdx)=='0') {
          lastIdx--;
        }
        sb.setLength(lastIdx+1);
      }
    }
    sb.append('Z');
    writeDate(name, sb.toString());
  }
  public void writeDate(String name, String val) throws IOException {
    writePrim("date",name,val,false);
  }
  public void writePrim(String tag, String name, String val, boolean escape) throws IOException {
    int contentLen=val.length();
    startTag(tag, name, contentLen==0);
    if (contentLen==0) return;
    if (escape) {
      XML.escapeCharData(val,writer);
    } else {
      writer.write(val,0,contentLen);
    }
    writer.write("</");
    writer.write(tag);
    writer.write('>');
  }
}
