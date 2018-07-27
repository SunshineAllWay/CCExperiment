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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
public final class SynExpand {
	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.out.println(
							   "java org.apache.lucene.wordnet.SynExpand <index path> <query>");
		}
		FSDirectory directory = FSDirectory.open(new File(args[0]));
		IndexSearcher searcher = new IndexSearcher(directory, true);
		String query = args[1];
		String field = "contents";
		Query q = expand( query, searcher, new StandardAnalyzer(Version.LUCENE_CURRENT), field, 0.9f);
		System.out.println( "Query: " + q.toString( field));
		searcher.close();
		directory.close();
	}
	public static Query expand( String query,
								Searcher syns,
								Analyzer a,
								String f,
								final float boost)
		throws IOException
	{
		final Set<String> already = new HashSet<String>(); 
		List<String> top = new LinkedList<String>(); 
		final String field = ( f == null) ? "contents" : f;
		if ( a == null) a = new StandardAnalyzer(Version.LUCENE_CURRENT);
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
