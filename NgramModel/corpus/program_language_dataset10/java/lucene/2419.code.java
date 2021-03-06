package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
public class PHPSerializedResponseWriter implements QueryResponseWriter {
  static String CONTENT_TYPE_PHP_UTF8="text/x-php-serialized;charset=UTF-8";
  boolean CESU8 = false;
  public void init(NamedList n) {
    String cesu8Setting = System.getProperty("solr.phps.cesu8");
    if (cesu8Setting != null) {
      CESU8="true".equals(cesu8Setting);
    } else {
      CESU8 = System.getProperty("jetty.home") != null;
    }
  }
 public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    PHPSerializedWriter w = new PHPSerializedWriter(writer, req, rsp, CESU8);
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
class PHPSerializedWriter extends JSONWriter {
  final private boolean CESU8;
  final UnicodeUtil.UTF8Result utf8;
  public PHPSerializedWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp, boolean CESU8) {
    super(writer, req, rsp);
    this.CESU8 = CESU8;
    this.utf8 = CESU8 ? null : new UnicodeUtil.UTF8Result();
    doIndent = false;
  }
  public void writeResponse() throws IOException {
    Boolean omitHeader = req.getParams().getBool(CommonParams.OMIT_HEADER);
    if(omitHeader != null && omitHeader) rsp.getValues().remove("responseHeader");
    writeNamedList(null, rsp.getValues());
  }
  @Override
  public void writeNamedList(String name, NamedList val) throws IOException {
    writeNamedListAsMapMangled(name,val);
  }
  @Override
  public void writeDoc(String name, Collection<Fieldable> fields, Set<String> returnFields, Map pseudoFields) throws IOException {
    ArrayList<Fieldable> single = new ArrayList<Fieldable>();
    HashMap<String, MultiValueField> multi = new HashMap<String, MultiValueField>();
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
        single.add(ff);
      }
    }
    writeArrayOpener(single.size() + multi.size() + ((pseudoFields!=null) ? pseudoFields.size() : 0));
    for(Fieldable ff : single) {
      SchemaField sf = schema.getField(ff.name());
      writeKey(ff.name(),true);
      sf.write(this, ff.name(), ff);
    }
    for(MultiValueField mvf : multi.values()) {
      writeKey(mvf.sfield.getName(), true);
      writeArrayOpener(mvf.fields.size());
      int i = 0;
      for (Fieldable ff : mvf.fields) {
        writeKey(i++, false);
        mvf.sfield.write(this, null, ff);
      }
      writeArrayCloser();
    }
    if (pseudoFields !=null && pseudoFields.size()>0) {
      writeMap(null,pseudoFields,true,false);
    }
    writeArrayCloser();
  }
  @Override
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
    writeKey("numFound",false);
    writeInt(null,ids.matches());
    writeKey("start",false);
    writeInt(null,ids.offset());
    if (includeScore) {
      writeKey("maxScore",false);
      writeFloat(null,ids.maxScore());
    }
    writeKey("docs",false);
    writeArrayOpener(sz);
    SolrIndexSearcher searcher = req.getSearcher();
    DocIterator iterator = ids.iterator();
    for (int i=0; i<sz; i++) {
      int id = iterator.nextDoc();
      Document doc = searcher.doc(id, fields);
      writeKey(i, false);
      writeDoc(null, doc, fields, (includeScore ? iterator.score() : 0.0f), includeScore);
    }
    writeMapCloser();
    if (otherFields !=null) {
      writeMap(null, otherFields, true, false);
    }
    writeMapCloser();
  }
  @Override
  public void writeArray(String name, Object[] val) throws IOException {
    writeMapOpener(val.length);
    for(int i=0; i < val.length; i++) {
      writeKey(i, false);
      writeVal(String.valueOf(i), val[i]);
    }
    writeMapCloser();
  }
  @Override
  public void writeArray(String name, Iterator val) throws IOException {
    ArrayList vals = new ArrayList();
    while( val.hasNext() ) {
      vals.add(val.next());
    }
    writeArray(name, vals.toArray());
  }
  @Override
  public void writeMapOpener(int size) throws IOException, IllegalArgumentException {
  	if (size < 0) {
  		throw new IllegalArgumentException("Map size must not be negative");
  	}
    writer.write("a:"+size+":{");
  }
  @Override
  public void writeMapSeparator() throws IOException {
  }
  @Override
  public void writeMapCloser() throws IOException {
    writer.write('}');
  }
  @Override
  public void writeArrayOpener(int size) throws IOException, IllegalArgumentException {
  	if (size < 0) {
  		throw new IllegalArgumentException("Array size must not be negative");
  	}
    writer.write("a:"+size+":{");
  }
  @Override  
  public void writeArraySeparator() throws IOException {
  }
  @Override
  public void writeArrayCloser() throws IOException {
    writer.write('}');
  }
  @Override
  public void writeNull(String name) throws IOException {
    writer.write("N;");
  }
  @Override
  protected void writeKey(String fname, boolean needsEscaping) throws IOException {
    writeStr(null, fname, needsEscaping);
  }
  void writeKey(int val, boolean needsEscaping) throws IOException {
    writeInt(null, String.valueOf(val));
  }
  @Override
  public void writeBool(String name, boolean val) throws IOException {
    writer.write(val ? "b:1;" : "b:0;");
  }
  @Override
  public void writeBool(String name, String val) throws IOException {
    writeBool(name, val.charAt(0) == 't');
  }
  @Override
  public void writeInt(String name, String val) throws IOException {
    writer.write("i:"+val+";");
  }
  @Override
  public void writeLong(String name, String val) throws IOException {
    writeInt(name,val);
  }
  @Override
  public void writeFloat(String name, String val) throws IOException {
    writeDouble(name,val);
  }
  @Override
  public void writeDouble(String name, String val) throws IOException {
    writer.write("d:"+val+";");
  }
  @Override
  public void writeStr(String name, String val, boolean needsEscaping) throws IOException {
    int nBytes;
    if (CESU8) {
      nBytes = 0;
      for (int i=0; i<val.length(); i++) {
        char ch = val.charAt(i);
        if (ch<='\u007f') {
          nBytes += 1;
        } else if (ch<='\u07ff') {
          nBytes += 2;
        } else {
          nBytes += 3;
        }
      }
    } else {
      UnicodeUtil.UTF16toUTF8(val, 0, val.length(), utf8);
      nBytes = utf8.length;
    }
    writer.write("s:");
    writer.write(Integer.toString(nBytes));
    writer.write(":\"");
    writer.write(val);
    writer.write("\";");
  }
}
