package org.apache.lucene.benchmark.quality.trec;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.lucene.benchmark.quality.QualityQuery;
public class TrecTopicsReader {
  private static final String newline = System.getProperty("line.separator");
  public TrecTopicsReader() {
    super();
  }
  public QualityQuery[] readQueries(BufferedReader reader) throws IOException {
    ArrayList<QualityQuery> res = new ArrayList<QualityQuery>();
    StringBuffer sb;
    try {
      while (null!=(sb=read(reader,"<top>",null,false,false))) {
        HashMap<String,String> fields = new HashMap<String,String>();
        sb = read(reader,"<num>",null,true,false);
        int k = sb.indexOf(":");
        String id = sb.substring(k+1).trim();
        sb = read(reader,"<title>",null,true,false);
        k = sb.indexOf(">");
        String title = sb.substring(k+1).trim();
        read(reader,"<desc>",null,false,false);
        sb.setLength(0);
        String line = null;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("<narr>"))
            break;
          if (sb.length() > 0) sb.append(' ');
          sb.append(line);
        }
        String description = sb.toString().trim();
        sb.setLength(0);
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("</top>"))
            break;
          if (sb.length() > 0) sb.append(' ');
          sb.append(line);
        }
        String narrative = sb.toString().trim();
        fields.put("title",title);
        fields.put("description",description);
        fields.put("narrative", narrative);
        QualityQuery topic = new QualityQuery(id,fields);
        res.add(topic);
      }
    } finally {
      reader.close();
    }
    QualityQuery qq[] = res.toArray(new QualityQuery[0]);
    Arrays.sort(qq);
    return qq;
  }
  private StringBuffer read (BufferedReader reader, String prefix, StringBuffer sb, boolean collectMatchLine, boolean collectAll) throws IOException {
    sb = (sb==null ? new StringBuffer() : sb);
    String sep = "";
    while (true) {
      String line = reader.readLine();
      if (line==null) {
        return null;
      }
      if (line.startsWith(prefix)) {
        if (collectMatchLine) {
          sb.append(sep+line);
          sep = newline;
        }
        break;
      }
      if (collectAll) {
        sb.append(sep+line);
        sep = newline;
      }
    }
    return sb;
  }
}
