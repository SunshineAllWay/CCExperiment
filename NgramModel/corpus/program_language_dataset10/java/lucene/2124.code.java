package org.apache.solr.handler.dataimport;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
public class TestLineEntityProcessor {
  @Test
  public void simple() throws IOException {
    Map attrs = AbstractDataImportHandlerTest.createMap(
            LineEntityProcessor.URL, "dummy.lis",
            LineEntityProcessor.ACCEPT_LINE_REGEX, null,
            LineEntityProcessor.SKIP_LINE_REGEX, null
    );
    Context c = AbstractDataImportHandlerTest.getContext(
            null,                          
            new VariableResolverImpl(),  
            getDataSource(filecontents),   
            Context.FULL_DUMP,                             
            Collections.EMPTY_LIST,        
            attrs                          
    );
    LineEntityProcessor ep = new LineEntityProcessor();
    ep.init(c);
    System.out.print("\n");
    List<String> fList = new ArrayList<String>();
    while (true) {
      Map<String, Object> f = ep.nextRow();
      if (f == null) break;
      fList.add((String) f.get("rawLine"));
      System.out.print("     rawLine='" + f.get("rawLine") + "'\n");
    }
    Assert.assertEquals(24, fList.size());
  }
  @Test
  public void only_xml_files() throws IOException {
    Map attrs = AbstractDataImportHandlerTest.createMap(
            LineEntityProcessor.URL, "dummy.lis",
            LineEntityProcessor.ACCEPT_LINE_REGEX, "xml",
            LineEntityProcessor.SKIP_LINE_REGEX, null
    );
    Context c = AbstractDataImportHandlerTest.getContext(
            null,                          
            new VariableResolverImpl(),  
            getDataSource(filecontents),   
            Context.FULL_DUMP,                             
            Collections.EMPTY_LIST,        
            attrs                          
    );
    LineEntityProcessor ep = new LineEntityProcessor();
    ep.init(c);
    List<String> fList = new ArrayList<String>();
    while (true) {
      Map<String, Object> f = ep.nextRow();
      if (f == null) break;
      fList.add((String) f.get("rawLine"));
    }
    Assert.assertEquals(5, fList.size());
  }
  @Test
  public void only_xml_files_no_xsd() throws IOException {
    Map attrs = AbstractDataImportHandlerTest.createMap(
            LineEntityProcessor.URL, "dummy.lis",
            LineEntityProcessor.ACCEPT_LINE_REGEX, "\\.xml",
            LineEntityProcessor.SKIP_LINE_REGEX, "\\.xsd"
    );
    Context c = AbstractDataImportHandlerTest.getContext(
            null,                          
            new VariableResolverImpl(),  
            getDataSource(filecontents),   
            Context.FULL_DUMP,                             
            Collections.EMPTY_LIST,        
            attrs                          
    );
    LineEntityProcessor ep = new LineEntityProcessor();
    ep.init(c);
    List<String> fList = new ArrayList<String>();
    while (true) {
      Map<String, Object> f = ep.nextRow();
      if (f == null) break;
      fList.add((String) f.get("rawLine"));
    }
    Assert.assertEquals(4, fList.size());
  }
  @Test
  public void no_xsd_files() throws IOException {
    Map attrs = AbstractDataImportHandlerTest.createMap(
            LineEntityProcessor.URL, "dummy.lis",
            LineEntityProcessor.SKIP_LINE_REGEX, "\\.xsd"
    );
    Context c = AbstractDataImportHandlerTest.getContext(
            null,                          
            new VariableResolverImpl(),  
            getDataSource(filecontents),   
            Context.FULL_DUMP,                             
            Collections.EMPTY_LIST,        
            attrs                          
    );
    LineEntityProcessor ep = new LineEntityProcessor();
    ep.init(c);
    List<String> fList = new ArrayList<String>();
    while (true) {
      Map<String, Object> f = ep.nextRow();
      if (f == null) break;
      fList.add((String) f.get("rawLine"));
    }
    Assert.assertEquals(18, fList.size());
  }
  public static Map<String, String> createField(
          String col,   
          String type,  
          String srcCol,  
          String re,  
          String rw,  
          String gn    
  ) {
    HashMap<String, String> vals = new HashMap<String, String>();
    vals.put("column", col);
    vals.put("type", type);
    vals.put("sourceColName", srcCol);
    vals.put("regex", re);
    vals.put("replaceWith", rw);
    vals.put("groupNames", gn);
    return vals;
  }
  private DataSource<Reader> getDataSource(final String xml) {
    return new DataSource<Reader>() {
      public void init(Context context, Properties initProps) {
      }
      public void close() {
      }
      public Reader getData(String query) {
        return new StringReader(xml);
      }
    };
  }
  private static final String filecontents =
          "\n" +
                  "# this is what the output from 'find . -ls; looks like, athough the format\n" +
                  "# of the time stamp varies depending on the age of the file and your LANG \n" +
                  "# env setting\n" +
                  "412577   0 drwxr-xr-x  6 user group    204 1 Apr 10:53 /Volumes/spare/ts\n" +
                  "412582   0 drwxr-xr-x 13 user group    442 1 Apr 10:18 /Volumes/spare/ts/config\n" +
                  "412583  24 -rwxr-xr-x  1 user group   8318 1 Apr 11:10 /Volumes/spare/ts/config/dc.xsd\n" +
                  "412584  32 -rwxr-xr-x  1 user group  12847 1 Apr 11:10 /Volumes/spare/ts/config/dcterms.xsd\n" +
                  "412585   8 -rwxr-xr-x  1 user group   3156 1 Apr 11:10 /Volumes/spare/ts/config/s-deliver.css\n" +
                  "412586 192 -rwxr-xr-x  1 user group  97764 1 Apr 11:10 /Volumes/spare/ts/config/s-deliver.xsl\n" +
                  "412587 224 -rwxr-xr-x  1 user group 112700 1 Apr 11:10 /Volumes/spare/ts/config/sml-delivery-2.1.xsd\n" +
                  "412588 208 -rwxr-xr-x  1 user group 103419 1 Apr 11:10 /Volumes/spare/ts/config/sml-delivery-norm-2.0.dtd\n" +
                  "412589 248 -rwxr-xr-x  1 user group 125296 1 Apr 11:10 /Volumes/spare/ts/config/sml-delivery-norm-2.1.dtd\n" +
                  "412590  72 -rwxr-xr-x  1 user group  36256 1 Apr 11:10 /Volumes/spare/ts/config/jm.xsd\n" +
                  "412591   8 -rwxr-xr-x  1 user group    990 1 Apr 11:10 /Volumes/spare/ts/config/video.gif\n" +
                  "412592   8 -rwxr-xr-x  1 user group   1498 1 Apr 11:10 /Volumes/spare/ts/config/xlink.xsd\n" +
                  "412593   8 -rwxr-xr-x  1 user group   1155 1 Apr 11:10 /Volumes/spare/ts/config/xml.xsd\n" +
                  "412594   0 drwxr-xr-x  4 user group    136 1 Apr 10:18 /Volumes/spare/ts/acm19\n" +
                  "412621   0 drwxr-xr-x 57 user group   1938 1 Apr 10:18 /Volumes/spare/ts/acm19/data\n" +
                  "412622  24 -rwxr-xr-x  1 user group   8894 1 Apr 11:09 /Volumes/spare/ts/acm19/data/00000510.xml\n" +
                  "412623  32 -rwxr-xr-x  1 user group  14124 1 Apr 11:09 /Volumes/spare/ts/acm19/data/00000603.xml\n" +
                  "412624  24 -rwxr-xr-x  1 user group  11976 1 Apr 11:09 /Volumes/spare/ts/acm19/data/00001292.xml\n" +
                  "# tacked on an extra line to cause a file to be deleted.\n" +
                  "DELETE /Volumes/spare/ts/acm19/data/00001292old.xml\n" +
                  "";
}
