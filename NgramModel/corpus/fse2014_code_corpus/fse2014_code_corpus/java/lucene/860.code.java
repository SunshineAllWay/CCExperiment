package org.apache.lucene.benchmark.byTask.feeds;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
public class SimpleSloppyPhraseQueryMaker extends SimpleQueryMaker {
  @Override
  protected Query[] prepareQueries() throws Exception {
    String words[];
    ArrayList<String> w = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(SingleDocSource.DOC_TEXT);
    while (st.hasMoreTokens() && w.size()<100) {
      w.add(st.nextToken());
    }
    words = w.toArray(new String[0]);
    ArrayList<Query> queries = new ArrayList<Query>(); 
    for (int slop=0; slop<8; slop++) {
      for (int qlen=2; qlen<6; qlen++) {
        for (int wd=0; wd<words.length-qlen-slop; wd++) {
          int remainedSlop = slop;
          PhraseQuery q = new PhraseQuery();
          q.setSlop(slop);
          int wind = wd;
          for (int i=0; i<qlen; i++) {
            q.add(new Term(DocMaker.BODY_FIELD,words[wind++]));
            if (remainedSlop>0) {
              remainedSlop--;
              wind++;
            }
          }
          queries.add(q);
          remainedSlop = slop;
          q = new PhraseQuery();
          q.setSlop(slop+2*qlen);
          wind = wd+qlen+remainedSlop-1;
          for (int i=0; i<qlen; i++) {
            q.add(new Term(DocMaker.BODY_FIELD,words[wind--]));
            if (remainedSlop>0) {
              remainedSlop--;
              wind--;
            }
          }
          queries.add(q);
        }
      }
    }
    return queries.toArray(new Query[0]);
  }
}
