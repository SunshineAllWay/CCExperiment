package org.apache.solr.handler;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
public class TestCSVLoader extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema12.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  String filename = "solr_tmp.csv";
  String def_charset = "UTF-8";
  File file = new File(filename);
  public void setUp() throws Exception {
    super.setUp();
  }
  public void tearDown() throws Exception {
    super.tearDown();
    deleteFile();
  }
  void makeFile(String contents) {
    makeFile(contents,def_charset);
  }
  void makeFile(String contents, String charset) {
    try {
      Writer out = new OutputStreamWriter(new FileOutputStream(filename), charset);
      out.write(contents);
      out.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  void deleteFile() {
    file.delete();
  }
  void cleanup() {
    assertU(delQ("id:[100 TO 110]"));
    assertU(commit());
  }
  void loadLocal(String... args) throws Exception {
    LocalSolrQueryRequest req =  (LocalSolrQueryRequest)req(args);
    List<ContentStream> cs = new ArrayList<ContentStream>();
    cs.add(new ContentStreamBase.FileStream(new File(filename)));
    req.setContentStreams(cs);
    h.query("/update/csv",req);
  }
  public void testCSVLoad() throws Exception {
    makeFile("id\n100\n101\n102");
    loadLocal("stream.file",filename);
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='0']");
    assertU(commit());
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='3']");
  }
  public void testCommitFalse() throws Exception {
    makeFile("id\n100\n101\n102");
    loadLocal("stream.file",filename,"commit","false");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='0']");
    assertU(commit());
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='3']");
  }
  public void testCommitTrue() throws Exception {
    makeFile("id\n100\n101\n102");
    loadLocal("stream.file",filename,"commit","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='3']");
  }
  public void testCSV() throws Exception {
    lrf.args.put("version","2.0");
    makeFile("id,str_s\n100,\"quoted\"\n101,\n102,\"\"\n103,");
    loadLocal("stream.file",filename,"commit","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted']");
    assertQ(req("id:101"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:102"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:103"),"count(//str[@name='str_s'])=0");
    loadLocal("stream.file",filename, "commit","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    loadLocal("stream.file",filename, "commit","true","header","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    loadLocal("stream.file",filename, "commit","true", "overwrite","false");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='8']");
    loadLocal("stream.file",filename, "commit","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    loadLocal("stream.file",filename, "commit","true", "map","quoted:QUOTED");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='QUOTED']");
    assertQ(req("id:101"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:102"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:103"),"count(//str[@name='str_s'])=0");
    loadLocal("stream.file",filename, "commit","true", "map","quoted:");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"count(//str[@name='str_s'])=0");
    loadLocal("stream.file",filename, "commit","true", "map",":EMPTY");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted']");
    assertQ(req("id:101"),"//str[@name='str_s'][.='EMPTY']");
    assertQ(req("id:102"),"//str[@name='str_s'][.='EMPTY']");
    assertQ(req("id:103"),"//str[@name='str_s'][.='EMPTY']");
    loadLocal("stream.file",filename, "commit","true", "map",":EMPTY", "map","quoted:QUOTED");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='QUOTED']");
    assertQ(req("id:101"),"//str[@name='str_s'][.='EMPTY']");
    assertQ(req("id:102"),"//str[@name='str_s'][.='EMPTY']");
    assertQ(req("id:103"),"//str[@name='str_s'][.='EMPTY']");
    loadLocal("stream.file",filename, "commit","true", "f.str_s.keepEmpty","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted']");
    assertQ(req("id:101"),"//str[@name='str_s'][.='']");
    assertQ(req("id:102"),"//str[@name='str_s'][.='']");
    assertQ(req("id:103"),"//str[@name='str_s'][.='']");
    loadLocal("stream.file",filename, "commit","true",
             "fieldnames","id,my_s", "header","true",
             "f.my_s.map",":EMPTY");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='my_s'][.='quoted']");
    assertQ(req("id:101"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:102"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:103"),"count(//str[@name='str_s'])=0");
    assertQ(req("id:101"),"//str[@name='my_s'][.='EMPTY']");
    assertQ(req("id:102"),"//str[@name='my_s'][.='EMPTY']");
    assertQ(req("id:103"),"//str[@name='my_s'][.='EMPTY']");
    assertQ(req("id:id"),"//*[@numFound='0']");
    loadLocal("stream.file",filename,"commit","true","keepEmpty","true","skip","str_s");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:[100 TO 110]"),"count(//str[@name='str_s'])=0");
    loadLocal("stream.file",filename,"commit","true","keepEmpty","true","fieldnames","id,");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:[100 TO 110]"),"count(//str[@name='str_s'])=0");
    loadLocal("stream.file",filename, "commit","true",
             "fieldnames","id,my_s", "header","false");
    assertQ(req("id:id"),"//*[@numFound='1']");
    assertQ(req("id:100"),"//str[@name='my_s'][.='quoted']");
    loadLocal("stream.file",filename, "commit","true",
             "fieldnames","id,my_s", "header","false", "skipLines","1");
    assertQ(req("id:id"),"//*[@numFound='1']");
    assertQ(req("id:100"),"//str[@name='my_s'][.='quoted']");
    makeFile("id,str_s\n"
            +"100,\"quoted\"\n"
            +"101,\"a,b,c\"\n"
            +"102,\"a,,b\"\n"
            +"103,\n");
    loadLocal("stream.file",filename, "commit","true",
              "f.str_s.map",":EMPTY",
              "f.str_s.split","true");
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='4']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[1][.='a']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[2][.='b']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[3][.='c']");
    assertQ(req("id:102"),"//arr[@name='str_s']/str[2][.='EMPTY']");
    assertQ(req("id:103"),"//str[@name='str_s'][.='EMPTY']");
    makeFile("id|str_s\n"
            +"100|^quoted^\n"
            +"101|a;'b';c\n"
            +"102|a;;b\n"
            +"103|\n"
            +"104|a\\\\b\n"  
    );
    loadLocal("stream.file",filename, "commit","true",
              "separator","|",
              "encapsulator","^",
              "f.str_s.map",":EMPTY",
              "f.str_s.split","true",
              "f.str_s.separator",";",
              "f.str_s.encapsulator","'"
    );
    assertQ(req("id:[100 TO 110]"),"//*[@numFound='5']");
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[1][.='a']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[2][.='b']");
    assertQ(req("id:101"),"//arr[@name='str_s']/str[3][.='c']");
    assertQ(req("id:102"),"//arr[@name='str_s']/str[2][.='EMPTY']");
    assertQ(req("id:103"),"//str[@name='str_s'][.='EMPTY']");
    assertQ(req("id:104"),"//str[@name='str_s'][.='a\\\\b']");
    makeFile("id,str_s\n"
            +"100,\"quoted \"\" \\ string\"\n"
            +"101,unquoted \"\" \\ string\n"     
            +"102,end quote \\\n"
    );
    loadLocal("stream.file",filename, "commit","true"
    );
    assertQ(req("id:100"),"//str[@name='str_s'][.='quoted \" \\ string']");
    assertQ(req("id:101"),"//str[@name='str_s'][.='unquoted \"\" \\ string']");
    assertQ(req("id:102"),"//str[@name='str_s'][.='end quote \\']");
    makeFile("id,str_s\n"
            +"100,\"quoted \"\" \\\" \\\\ string\"\n"  
            +"101,unquoted \"\" \\\" \\, \\\\ string\n"
    );
    loadLocal("stream.file",filename, "commit","true"
            ,"escape","\\"
    );
    assertQ(req("id:100"),"//str[@name='str_s'][.='\"quoted \"\" \" \\ string\"']");
    assertQ(req("id:101"),"//str[@name='str_s'][.='unquoted \"\" \" , \\ string']");
  }
}
