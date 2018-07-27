package org.apache.solr.response;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.util.StringHelper;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
public class JSONResponseWriter implements QueryResponseWriter {
  static String CONTENT_TYPE_JSON_UTF8="text/x-json; charset=UTF-8";
  public void init(NamedList n) {
  }
  public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    JSONWriter w = new JSONWriter(writer, req, rsp);
    try {
      w.writeResponse();
    } finally {
      w.close();
    }
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE_TEXT_UTF8;
  }
}
class JSONWriter extends TextResponseWriter {
  private Calendar cal;
  private String namedListStyle;
  private String wrapperFunction;
  private static final String JSON_NL_STYLE="json.nl";
  private static final String JSON_NL_MAP="map";
  private static final String JSON_NL_FLAT="flat";
  private static final String JSON_NL_ARROFARR="arrarr";
  private static final String JSON_NL_ARROFMAP="arrmap";
  private static final String JSON_WRAPPER_FUNCTION="json.wrf";
  public JSONWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    super(writer, req, rsp);
    namedListStyle = StringHelper.intern(req.getParams().get(JSON_NL_STYLE, JSON_NL_FLAT));
    wrapperFunction = req.getParams().get(JSON_WRAPPER_FUNCTION);
  }
  public void writeResponse() throws IOException {
    if(wrapperFunction!=null) {
        writer.write(wrapperFunction + "(");
    }
    Boolean omitHeader = req.getParams().getBool(CommonParams.OMIT_HEADER);
    if(omitHeader != null && omitHeader) rsp.getValues().remove("responseHeader");
    writeNamedList(null, rsp.getValues());
    if(wrapperFunction!=null) {
        writer.write(')');
    }
  }
  protected void writeKey(String fname, boolean needsEscaping) throws IOException {
    writeStr(null, fname, needsEscaping);
    writer.write(':');
  }
  protected void writeNamedListAsMapMangled(String name, NamedList val) throws IOException {
    int sz = val.size();
    writeMapOpener(sz);
    incLevel();
    HashMap<String,Integer> repeats = new HashMap<String,Integer>(4);
    boolean first=true;
    for (int i=0; i<sz; i++) {
      String key = val.getName(i);
      if (key==null) key="";
      if (first) {
        first=false;
        repeats.put(key,0);
      } else {
        writeMapSeparator();
        Integer repeatCount = repeats.get(key);
        if (repeatCount==null) {
          repeats.put(key,0);
        } else {
          String newKey = key;
          int newCount = repeatCount;
          do {  
            newKey = key + ' ' + (++newCount);
            repeatCount = repeats.get(newKey);
          } while (repeatCount != null);
          repeats.put(key,newCount);
          key = newKey;
        }
      }
      indent();
      writeKey(key, true);
      writeVal(key,val.getVal(i));
    }
    decLevel();
    writeMapCloser();
  }
  protected void writeNamedListAsMapWithDups(String name, NamedList val) throws IOException {
    int sz = val.size();
    writeMapOpener(sz);
    incLevel();
    for (int i=0; i<sz; i++) {
      if (i!=0) {
        writeMapSeparator();
      }
      String key = val.getName(i);
      if (key==null) key="";
      indent();
      writeKey(key, true);
      writeVal(key,val.getVal(i));
    }
    decLevel();
    writeMapCloser();
  }
  protected void writeNamedListAsArrMap(String name, NamedList val) throws IOException {
    int sz = val.size();
    indent();
    writeArrayOpener(sz);
    incLevel();
    boolean first=true;
    for (int i=0; i<sz; i++) {
      String key = val.getName(i);
      if (first) {
        first=false;
      } else {
        writeArraySeparator();
      }
      indent();
      if (key==null) {
        writeVal(null,val.getVal(i));
      } else {
        writeMapOpener(1);
        writeKey(key, true);
        writeVal(key,val.getVal(i));
        writeMapCloser();
      }
    }
    decLevel();
    writeArrayCloser();
  }
  protected void writeNamedListAsArrArr(String name, NamedList val) throws IOException {
    int sz = val.size();
    indent();
    writeArrayOpener(sz);
    incLevel();
    boolean first=true;
    for (int i=0; i<sz; i++) {
      String key = val.getName(i);
      if (first) {
        first=false;
      } else {
        writeArraySeparator();
      }
      indent();
        writeArrayOpener(1);
        incLevel();
        if (key==null) {
          writeNull(null);
        } else {
          writeStr(null, key, true);
        }
        writeArraySeparator();
        writeVal(key,val.getVal(i));
        decLevel();
        writeArrayCloser();
    }
    decLevel();
    writeArrayCloser();
  }
  protected void writeNamedListAsFlat(String name, NamedList val) throws IOException {
    int sz = val.size();
    writeArrayOpener(sz);
    incLevel();
    for (int i=0; i<sz; i++) {
      if (i!=0) {
        writeArraySeparator();
      }
      String key = val.getName(i);
      indent();
      if (key==null) {
        writeNull(null);
      } else {
        writeStr(null, key, true);
      }
      writeArraySeparator();
      writeVal(key, val.getVal(i));
    }
    decLevel();
    writeArrayCloser();
  }
  public void writeNamedList(String name, NamedList val) throws IOException {
    if (val instanceof SimpleOrderedMap) {
      writeNamedListAsMapWithDups(name,val);
    } else if (namedListStyle==JSON_NL_FLAT) {
      writeNamedListAsFlat(name,val);
    } else if (namedListStyle==JSON_NL_MAP){
      writeNamedListAsMapWithDups(name,val);
    } else if (namedListStyle==JSON_NL_ARROFARR) {
      writeNamedListAsArrArr(name,val);
    } else if (namedListStyle==JSON_NL_ARROFMAP) {
      writeNamedListAsArrMap(name,val);
    }
  }
  protected static class MultiValueField {
    final SchemaField sfield;
    final ArrayList<Fieldable> fields;
    MultiValueField(SchemaField sfield, Fieldable firstVal) {
      this.sfield = sfield;
      this.fields = new ArrayList<Fieldable>(4);
      this.fields.add(firstVal);
    }
  }
  public void writeDoc(String name, Collection<Fieldable> fields, Set<String> returnFields, Map pseudoFields) throws IOException {
    writeMapOpener(-1); 
    incLevel();
    HashMap<String, MultiValueField> multi = new HashMap<String, MultiValueField>();
    boolean first=true;
    for (Fieldable ff : fields) {
      String fname = ff.name();
      if (returnFields!=null && !returnFields.contains(fname)) {
        continue;
      }
      SchemaField sf = schema.getField(fname);
      if (sf.multiValued()) {
        MultiValueField mf = multi.get(fname);
        if (mf==null) {
          mf = new MultiValueField(sf, ff);
          multi.put(fname, mf);
        } else {
          mf.fields.add(ff);
        }
      } else {
        if (first) {
          first=false;
        } else {
          writeMapSeparator();
        }
        indent();
        writeKey(fname,true);
        sf.write(this, fname, ff);
      }
    }
    for(MultiValueField mvf : multi.values()) {
      if (first) {
        first=false;
      } else {
        writeMapSeparator();
      }
      indent();
      writeKey(mvf.sfield.getName(), true);
      boolean indentArrElems=false;
      if (doIndent) {
        indentArrElems = (mvf.sfield.getType() instanceof TextField);
      }
      writeArrayOpener(-1); 
      boolean firstArrElem=true;
      incLevel();
      for (Fieldable ff : mvf.fields) {
        if (firstArrElem) {
          firstArrElem=false;
        } else {
          writeArraySeparator();
        }
        if (indentArrElems) indent();
        mvf.sfield.write(this, null, ff);
      }
      writeArrayCloser();
      decLevel();
    }
    if (pseudoFields !=null && pseudoFields.size()>0) {
      writeMap(null,pseudoFields,true,first);
    }
    decLevel();
    writeMapCloser();
  }
  public void writeSolrDocument(String name, SolrDocument doc, Set<String> returnFields, Map pseudoFields) throws IOException {
    writeMapOpener(-1); 
    incLevel();
    boolean first=true;
    for (String fname : doc.getFieldNames()) {
      if (first) {
        first=false;
      }
      else {
        writeMapSeparator();
      }
      indent();
      writeKey(fname, true);
      Object val = doc.getFieldValue(fname);
      if (val instanceof Collection) {
        writeVal(fname, val);
      } else {
        SchemaField sf = schema.getFieldOrNull(fname);
        if (sf != null && sf.multiValued()) {
          writeArrayOpener(-1); 
          writeVal(fname, val);
          writeArrayCloser();
        } else {
          writeVal(fname, val);
        }
      }
      if (pseudoFields !=null && pseudoFields.size()>0) {
        writeMap(null,pseudoFields,true,first);
      }
    }
    decLevel();
    writeMapCloser();
  }
  private final HashMap scoreMap = new HashMap(1);
  public void writeDoc(String name, Document doc, Set<String> returnFields, float score, boolean includeScore) throws IOException {
    Map other = null;
    if (includeScore) {
      other = scoreMap;
      scoreMap.put("score",score);
    }
    writeDoc(name, (List<Fieldable>)(doc.getFields()), returnFields, other);
  }
  public void writeDocList(String name, DocList ids, Set<String> fields, Map otherFields) throws IOException {
    boolean includeScore=false;
    if (fields!=null) {
      includeScore = fields.contains("score");
      if (fields.size()==0 || (fields.size()==1 && includeScore) || fields.contains("*")) {
        fields=null;  
      }
    }
    int sz=ids.size();
    writeMapOpener(includeScore ? 4 : 3);
    incLevel();
    writeKey("numFound",false);
    writeInt(null,ids.matches());
    writeMapSeparator();
    writeKey("start",false);
    writeInt(null,ids.offset());
    if (includeScore) {
      writeMapSeparator();
      writeKey("maxScore",false);
      writeFloat(null,ids.maxScore());
    }
    writeMapSeparator();
    writeKey("docs",false);
    writeArrayOpener(sz);
    incLevel();
    boolean first=true;
    SolrIndexSearcher searcher = req.getSearcher();
    DocIterator iterator = ids.iterator();
    for (int i=0; i<sz; i++) {
      int id = iterator.nextDoc();
      Document doc = searcher.doc(id, fields);
      if (first) {
        first=false;
      } else {
        writeArraySeparator();
      }
      indent();
      writeDoc(null, doc, fields, (includeScore ? iterator.score() : 0.0f), includeScore);
    }
    decLevel();
    writeArrayCloser();
    if (otherFields !=null) {
      writeMap(null, otherFields, true, false);
    }
    decLevel();
    indent();
    writeMapCloser();
  }
  @Override
  public void writeSolrDocumentList(String name, SolrDocumentList docs, Set<String> fields, Map otherFields) throws IOException {
    boolean includeScore=false;
    if (fields!=null) {
      includeScore = fields.contains("score");
      if (fields.size()==0 || (fields.size()==1 && includeScore) || fields.contains("*")) {
        fields=null;  
      }
    }
    int sz=docs.size();
    writeMapOpener(includeScore ? 4 : 3);
    incLevel();
    writeKey("numFound",false);
    writeLong(null,docs.getNumFound());
    writeMapSeparator();
    writeKey("start",false);
    writeLong(null,docs.getStart());
    if (includeScore && docs.getMaxScore() != null) {
      writeMapSeparator();
      writeKey("maxScore",false);
      writeFloat(null,docs.getMaxScore());
    }
    writeMapSeparator();
    writeKey("docs",false);
    writeArrayOpener(sz);
    incLevel();
    boolean first=true;
    SolrIndexSearcher searcher = req.getSearcher();
    for (SolrDocument doc : docs) {
      if (first) {
        first=false;
      } else {
        writeArraySeparator();
      }
      indent();      
      writeSolrDocument(null, doc, fields, otherFields);
    }
    decLevel();
    writeArrayCloser();
    if (otherFields !=null) {
      writeMap(null, otherFields, true, false);
    }
    decLevel();
    indent();
    writeMapCloser();
  }
  public void writeMapOpener(int size) throws IOException, IllegalArgumentException {
    writer.write('{');
  }
  public void writeMapSeparator() throws IOException {
    writer.write(',');
  }
  public void writeMapCloser() throws IOException {
    writer.write('}');
  }
  public void writeArrayOpener(int size) throws IOException, IllegalArgumentException {
    writer.write('[');
  }
  public void writeArraySeparator() throws IOException {
    writer.write(',');
  }
  public void writeArrayCloser() throws IOException {
    writer.write(']');
  }
  public void writeStr(String name, String val, boolean needsEscaping) throws IOException {
    if (needsEscaping) {
      writer.write('"');
      for (int i=0; i<val.length(); i++) {
        char ch = val.charAt(i);
        if ((ch > '#' && ch != '\\') || ch==' ') { 
          writer.write(ch);
          continue;
        }
        switch(ch) {
          case '"':
          case '\\':
            writer.write('\\');
            writer.write(ch);
            break;
          case '\r': writer.write('\\'); writer.write('r'); break;
          case '\n': writer.write('\\'); writer.write('n'); break;
          case '\t': writer.write('\\'); writer.write('t'); break;
          case '\b': writer.write('\\'); writer.write('b'); break;
          case '\f': writer.write('\\'); writer.write('f'); break;
          default: {
            if (ch <= 0x1F) {
              unicodeEscape(writer,ch);
            } else {
              writer.write(ch);
            }
          }
        }
      }
      writer.write('"');
    } else {
      writer.write('"');
      writer.write(val);
      writer.write('"');
    }
  }
  public void writeMap(String name, Map val, boolean excludeOuter, boolean isFirstVal) throws IOException {
    if (!excludeOuter) {
      writeMapOpener(val.size());
      incLevel();
      isFirstVal=true;
    }
    boolean doIndent = excludeOuter || val.size() > 1;
    for (Map.Entry entry : (Set<Map.Entry>)val.entrySet()) {
      Object e = entry.getKey();
      String k = e==null ? "" : e.toString();
      Object v = entry.getValue();
      if (isFirstVal) {
        isFirstVal=false;
      } else {
        writeMapSeparator();
      }
      if (doIndent) indent();
      writeKey(k,true);
      writeVal(k,v);
    }
    if (!excludeOuter) {
      decLevel();
      writeMapCloser();
    }
  }
  public void writeArray(String name, Object[] val) throws IOException {
    writeArray(name, Arrays.asList(val).iterator());
  }
  public void writeArray(String name, Iterator val) throws IOException {
    writeArrayOpener(-1); 
    incLevel();
    boolean first=true;
    while( val.hasNext() ) {
      if( !first ) indent();
      writeVal(null, val.next());
      if( val.hasNext() ) {
        writeArraySeparator();
      }
      first=false;
    }
    decLevel();
    writeArrayCloser();
  }
  public void writeNull(String name) throws IOException {
    writer.write("null");
  }
  public void writeInt(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeLong(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeBool(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeFloat(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeDouble(String name, String val) throws IOException {
    writer.write(val);
  }
   @Override
  public void writeShort(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeByte(String name, String val) throws IOException {
    writer.write(val);
  }
  public void writeDate(String name, Date val) throws IOException {
    StringBuilder sb = new StringBuilder();
    if (cal==null) cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.setTime(val);
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
    writeStr(name, val, false);
  }
  private static char[] hexdigits = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
  protected static void unicodeEscape(Appendable out, int ch) throws IOException {
    out.append('\\');
    out.append('u');
    out.append(hexdigits[(ch>>>12)     ]);
    out.append(hexdigits[(ch>>>8) & 0xf]);
    out.append(hexdigits[(ch>>>4) & 0xf]);
    out.append(hexdigits[(ch)     & 0xf]);
  }
}
abstract class NaNFloatWriter extends JSONWriter {
  abstract protected String getNaN();
  abstract protected String getInf();
  public NaNFloatWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    super(writer, req, rsp);
  }
  @Override
  public void writeFloat(String name, float val) throws IOException {
    if (Float.isNaN(val)) {
      writer.write(getNaN());
    } else if (Float.isInfinite(val)) {
      if (val < 0.0f)
        writer.write('-');
      writer.write(getInf());
    } else {
      writeFloat(name, Float.toString(val));
    }
  }
  @Override
  public void writeDouble(String name, double val) throws IOException {
    if (Double.isNaN(val)) {
      writer.write(getNaN());
    } else if (Double.isInfinite(val)) {
      if (val < 0.0)
        writer.write('-');
      writer.write(getInf());
    } else {
      writeDouble(name, Double.toString(val));
    }
  }
}
