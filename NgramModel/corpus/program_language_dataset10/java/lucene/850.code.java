package org.apache.lucene.benchmark.byTask.feeds;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.benchmark.byTask.tasks.NewAnalyzerTask;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class FileBasedQueryMaker extends AbstractQueryMaker implements QueryMaker{
  @Override
  protected Query[] prepareQueries() throws Exception {
    Analyzer anlzr = NewAnalyzerTask.createAnalyzer(config.get("analyzer",
            "org.apache.lucene.analysis.standard.StandardAnalyzer"));
    String defaultField = config.get("file.query.maker.default.field", DocMaker.BODY_FIELD);
    QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, defaultField, anlzr);
    List<Query> qq = new ArrayList<Query>();
    String fileName = config.get("file.query.maker.file", null);
    if (fileName != null)
    {
      File file = new File(fileName);
      Reader reader = null;
      if (file.exists()) {
        reader = new FileReader(file);
      } else {
        InputStream asStream = FileBasedQueryMaker.class.getClassLoader().getResourceAsStream(fileName);
        if (asStream != null) {
          reader = new InputStreamReader(asStream);
        }
      }
      if (reader != null) {
        try {
          BufferedReader buffered = new BufferedReader(reader);
          String line = null;
          int lineNum = 0;
          while ((line = buffered.readLine()) != null)
          {
            line = line.trim();
            if (!line.equals("") && !line.startsWith("#"))
            {
              Query query = null;
              try {
                query = qp.parse(line);
              } catch (ParseException e) {
                System.err.println("Exception: " + e.getMessage() + " occurred while parsing line: " + lineNum + " Text: " + line);
              }
              qq.add(query);
            }
            lineNum++;
          }
        } finally {
          reader.close();
        }
      } else {
        System.err.println("No Reader available for: " + fileName);
      }
    }
    return qq.toArray(new Query[qq.size()]) ;
  }
}
