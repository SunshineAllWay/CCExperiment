package org.apache.lucene.wordnet;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
public class SynLookup {
  final static class CountingCollector extends Collector {
    public int numHits = 0;
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) throws IOException {
      numHits++;
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {}
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }    
  }
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println(
							   "java org.apache.lucene.wordnet.SynLookup <index path> <word>");
		}
		FSDirectory directory = FSDirectory.open(new File(args[0]));
		IndexSearcher searcher = new IndexSearcher(directory, true);
		String word = args[1];
		Query query = new TermQuery(new Term(Syns2Index.F_WORD, word));
		CountingCollector countingCollector = new CountingCollector();
		searcher.search(query, countingCollector);
		if (countingCollector.numHits == 0) {
			System.out.println("No synonyms found for " + word);
		} else {
			System.out.println("Synonyms found for \"" + word + "\":");
		}
		ScoreDoc[] hits = searcher.search(query, countingCollector.numHits).scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			String[] values = doc.getValues(Syns2Index.F_SYN);
			for (int j = 0; j < values.length; j++) {
				System.out.println(values[j]);
			}
		}
		searcher.close();
		directory.close();
	}
	public static Query expand( String query,
								Searcher syns,
								Analyzer a,
								final String field,
								final float boost)
		throws IOException
	{
		final Set<String> already = new HashSet<String>(); 
		List<String> top = new LinkedList<String>(); 
		TokenStream ts = a.tokenStream( field, new StringReader( query));
    TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
		while (ts.incrementToken()) {
			String word = termAtt.term();
			if ( already.add( word))
				top.add( word);
		}
		final BooleanQuery tmp = new BooleanQuery();
		Iterator<String> it = top.iterator();
		while ( it.hasNext())
		{
			String word = it.next();
			TermQuery tq = new TermQuery( new Term( field, word));
			tmp.add( tq, BooleanClause.Occur.SHOULD);
			syns.search(new TermQuery( new Term(Syns2Index.F_WORD, word)), new Collector() {
			  IndexReader reader;
        @Override
        public boolean acceptsDocsOutOfOrder() {
          return true;
        }
        @Override
        public void collect(int doc) throws IOException {
          Document d = reader.document(doc);
          String[] values = d.getValues( Syns2Index.F_SYN);
          for ( int j = 0; j < values.length; j++)
          {
            String syn = values[ j];
            if ( already.add( syn))
            {
              TermQuery tq = new TermQuery( new Term( field, syn));
              if ( boost > 0) 
                tq.setBoost( boost);
              tmp.add( tq, BooleanClause.Occur.SHOULD); 
            }
          }
        }
        @Override
        public void setNextReader(IndexReader reader, int docBase)
            throws IOException {
          this.reader = reader;
        }
        @Override
        public void setScorer(Scorer scorer) throws IOException {}
			});
		}
		return tmp;
	}
}
