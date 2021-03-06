package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.HTMLStripTransformer.TRUE;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class ClobTransformer extends Transformer {
  public Object transformRow(Map<String, Object> aRow, Context context) {
    for (Map<String, String> map : context.getAllEntityFields()) {
      if (!TRUE.equals(map.get(CLOB))) continue;
      String column = map.get(DataImporter.COLUMN);
      String srcCol = map.get(RegexTransformer.SRC_COL_NAME);
      if (srcCol == null)
        srcCol = column;
      Object o = aRow.get(srcCol);
      if (o instanceof List) {
        List<Clob> inputs = (List<Clob>) o;
        List<String> results = new ArrayList<String>();
        for (Object input : inputs) {
          if (input instanceof Clob) {
            Clob clob = (Clob) input;
            results.add(readFromClob(clob));
          }
        }
        aRow.put(column, results);
      } else {
        if (o instanceof Clob) {
          Clob clob = (Clob) o;
          aRow.put(column, readFromClob(clob));
        }
      }
    }
    return aRow;
  }
  private String readFromClob(Clob clob) {
    Reader reader = FieldReaderDataSource.readCharStream(clob);
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[1024];
    int len;
    try {
      while ((len = reader.read(buf)) != -1) {
        sb.append(buf, 0, len);
      }
    } catch (IOException e) {
      DataImportHandlerException.wrapAndThrow(DataImportHandlerException.SEVERE, e);
    }
    return sb.toString();
  }
  public static final String CLOB = "clob";
}
