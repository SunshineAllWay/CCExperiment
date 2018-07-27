package org.apache.solr.handler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.*;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.commons.csv.CSVStrategy;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import java.util.regex.Pattern;
import java.util.List;
import java.io.*;
public class CSVRequestHandler extends ContentStreamHandlerBase {
  protected ContentStreamLoader newLoader(SolrQueryRequest req, UpdateRequestProcessor processor) {
    return new SingleThreadedCSVLoader(req, processor);
  }
  @Override
  public String getDescription() {
    return "Add/Update multiple documents with CSV formatted rows";
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: CSVRequestHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/CSVRequestHandler.java $";
  }
}
abstract class CSVLoader extends ContentStreamLoader {
  static String SEPARATOR="separator";
  static String FIELDNAMES="fieldnames";
  static String HEADER="header";
  static String SKIP="skip";
  static String SKIPLINES="skipLines";
  static String MAP="map";
  static String TRIM="trim";
  static String EMPTY="keepEmpty";
  static String SPLIT="split";
  static String ENCAPSULATOR="encapsulator";
  static String ESCAPE="escape";
  static String OVERWRITE="overwrite";
  private static Pattern colonSplit = Pattern.compile(":");
  private static Pattern commaSplit = Pattern.compile(",");
  final IndexSchema schema;
  final SolrParams params;
  final CSVStrategy strategy;
  final UpdateRequestProcessor processor;
  String[] fieldnames;
  SchemaField[] fields;
  CSVLoader.FieldAdder[] adders;
  int skipLines;    
  final AddUpdateCommand templateAdd;
  private class FieldAdder {
    void add(SolrInputDocument doc, int line, int column, String val) {
      if (val.length() > 0) {
        doc.addField(fields[column].getName(),val,1.0f);
      }
    }
  }
  private class FieldAdderEmpty extends CSVLoader.FieldAdder {
    void add(SolrInputDocument doc, int line, int column, String val) {
      doc.addField(fields[column].getName(),val,1.0f);
    }
  }
  private class FieldTrimmer extends CSVLoader.FieldAdder {
    private final CSVLoader.FieldAdder base;
    FieldTrimmer(CSVLoader.FieldAdder base) { this.base=base; }
    void add(SolrInputDocument doc, int line, int column, String val) {
      base.add(doc, line, column, val.trim());
    }
  }
 private class FieldMapperSingle extends CSVLoader.FieldAdder {
   private final String from;
   private final String to;
   private final CSVLoader.FieldAdder base;
   FieldMapperSingle(String from, String to, CSVLoader.FieldAdder base) {
     this.from=from;
     this.to=to;
     this.base=base;
   }
    void add(SolrInputDocument doc, int line, int column, String val) {
      if (from.equals(val)) val=to;
      base.add(doc,line,column,val);
    }
 }
  private class FieldSplitter extends CSVLoader.FieldAdder {
    private final CSVStrategy strategy;
    private final CSVLoader.FieldAdder base;
    FieldSplitter(CSVStrategy strategy, CSVLoader.FieldAdder base) {
      this.strategy = strategy;
      this.base = base;
    }
    void add(SolrInputDocument doc, int line, int column, String val) {
      CSVParser parser = new CSVParser(new StringReader(val), strategy);
      try {
        String[] vals = parser.getLine();
        if (vals!=null) {
          for (String v: vals) base.add(doc,line,column,v);
        } else {
          base.add(doc,line,column,val);
        }
      } catch (IOException e) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,e);
      }
    }
  }
  String errHeader="CSVLoader:";
  CSVLoader(SolrQueryRequest req, UpdateRequestProcessor processor) {
    this.processor = processor;
    this.params = req.getParams();
    schema = req.getSchema();
    templateAdd = new AddUpdateCommand();
    templateAdd.allowDups=false;
    templateAdd.overwriteCommitted=true;
    templateAdd.overwritePending=true;
    if (params.getBool(OVERWRITE,true)) {
      templateAdd.allowDups=false;
      templateAdd.overwriteCommitted=true;
      templateAdd.overwritePending=true;
    } else {
      templateAdd.allowDups=true;
      templateAdd.overwriteCommitted=false;
      templateAdd.overwritePending=false;
    }
    strategy = new CSVStrategy(',', '"', CSVStrategy.COMMENTS_DISABLED, CSVStrategy.ESCAPE_DISABLED, false, false, false, true);
    String sep = params.get(SEPARATOR);
    if (sep!=null) {
      if (sep.length()!=1) throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Invalid separator:'"+sep+"'");
      strategy.setDelimiter(sep.charAt(0));
    }
    String encapsulator = params.get(ENCAPSULATOR);
    if (encapsulator!=null) {
      if (encapsulator.length()!=1) throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Invalid encapsulator:'"+encapsulator+"'");
    }
    String escape = params.get(ESCAPE);
    if (escape!=null) {
      if (escape.length()!=1) throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Invalid escape:'"+escape+"'");
    }
    if (encapsulator == null && escape != null) {
      strategy.setEncapsulator((char)-2);  
      strategy.setEscape(escape.charAt(0));
    } else {
      if (encapsulator != null) {
        strategy.setEncapsulator(encapsulator.charAt(0));
      }
      if (escape != null) {
        char ch = escape.charAt(0);
        strategy.setEscape(ch);
        if (ch == '\\') {
          strategy.setUnicodeEscapeInterpretation(true);
        }
      }
    }
    String fn = params.get(FIELDNAMES);
    fieldnames = fn != null ? commaSplit.split(fn,-1) : null;
    Boolean hasHeader = params.getBool(HEADER);
    skipLines = params.getInt(SKIPLINES,0);
    if (fieldnames==null) {
      if (null == hasHeader) {
        hasHeader=true;
      } else if (!hasHeader) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"CSVLoader: must specify fieldnames=<fields>* or header=true");
      }
    } else {
      if (hasHeader!=null && hasHeader) skipLines++;
      prepareFields();
    }
  }
  void prepareFields() {
    fields = new SchemaField[fieldnames.length];
    adders = new CSVLoader.FieldAdder[fieldnames.length];
    String skipStr = params.get(SKIP);
    List<String> skipFields = skipStr==null ? null : StrUtils.splitSmart(skipStr,',');
    CSVLoader.FieldAdder adder = new CSVLoader.FieldAdder();
    CSVLoader.FieldAdder adderKeepEmpty = new CSVLoader.FieldAdderEmpty();
    for (int i=0; i<fields.length; i++) {
      String fname = fieldnames[i];
      if (fname.length()==0 || (skipFields!=null && skipFields.contains(fname))) continue;
      fields[i] = schema.getField(fname);
      boolean keepEmpty = params.getFieldBool(fname,EMPTY,false);
      adders[i] = keepEmpty ? adderKeepEmpty : adder;
      String[] fmap = params.getFieldParams(fname,MAP);
      if (fmap!=null) {
        for (String mapRule : fmap) {
          String[] mapArgs = colonSplit.split(mapRule,-1);
          if (mapArgs.length!=2)
            throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Map rules must be of the form 'from:to' ,got '"+mapRule+"'");
          adders[i] = new CSVLoader.FieldMapperSingle(mapArgs[0], mapArgs[1], adders[i]);
        }
      }
      if (params.getFieldBool(fname,TRIM,false)) {
        adders[i] = new CSVLoader.FieldTrimmer(adders[i]);
      }
      if (params.getFieldBool(fname,SPLIT,false)) {
        String sepStr = params.getFieldParam(fname,SEPARATOR);
        char fsep = sepStr==null || sepStr.length()==0 ? ',' : sepStr.charAt(0);
        String encStr = params.getFieldParam(fname,ENCAPSULATOR);
        char fenc = encStr==null || encStr.length()==0 ? (char)-2 : encStr.charAt(0);
        String escStr = params.getFieldParam(fname,ESCAPE);
        char fesc = escStr==null || encStr.length()==0 ? CSVStrategy.ESCAPE_DISABLED : escStr.charAt(0);
        CSVStrategy fstrat = new CSVStrategy(fsep,fenc,CSVStrategy.COMMENTS_DISABLED,fesc, false, false, false, false);
        adders[i] = new CSVLoader.FieldSplitter(fstrat, adders[i]);
      }
    }
  }
  private void input_err(String msg, String[] line, int lineno) {
    StringBuilder sb = new StringBuilder();
    sb.append(errHeader+", line="+lineno + ","+msg+"\n\tvalues={");
    for (String val: line) { sb.append("'"+val+"',"); }
    sb.append('}');
    throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,sb.toString());
  }
  public void load(SolrQueryRequest req, SolrQueryResponse rsp, ContentStream stream) throws IOException {
    errHeader = "CSVLoader: input=" + stream.getSourceInfo();
    Reader reader = null;
    try {
      reader = stream.getReader();
      if (skipLines>0) {
        if (!(reader instanceof BufferedReader)) {
          reader = new BufferedReader(reader);
        }
        BufferedReader r = (BufferedReader)reader;
        for (int i=0; i<skipLines; i++) {
          r.readLine();
        }
      }
      CSVParser parser = new CSVParser(reader, strategy);
      if (fieldnames==null) {
        fieldnames = parser.getLine();
        if (fieldnames==null) {
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Expected fieldnames in CSV input");
        }
        prepareFields();
      }
      for(;;) {
        int line = parser.getLineNumber();  
        String[] vals = parser.getLine();
        if (vals==null) break;
        if (vals.length != fields.length) {
          input_err("expected "+fields.length+" values but got "+vals.length, vals, line);
        }
        addDoc(line,vals);
      }
    } finally{
      if (reader != null) {
        IOUtils.closeQuietly(reader);
      }
    }
  }
  abstract void addDoc(int line, String[] vals) throws IOException;
  void doAdd(int line, String[] vals, SolrInputDocument doc, AddUpdateCommand template) throws IOException {
    for (int i=0; i<vals.length; i++) {
      if (fields[i]==null) continue;  
      String val = vals[i];
      adders[i].add(doc, line, i, val);
    }
    template.solrDoc = doc;
    processor.processAdd(template);
  }
}
class SingleThreadedCSVLoader extends CSVLoader {
  SingleThreadedCSVLoader(SolrQueryRequest req, UpdateRequestProcessor processor) {
    super(req, processor);
  }
  void addDoc(int line, String[] vals) throws IOException {
    templateAdd.indexedId = null;
    SolrInputDocument doc = new SolrInputDocument();
    doAdd(line, vals, doc, templateAdd);
  }
}
