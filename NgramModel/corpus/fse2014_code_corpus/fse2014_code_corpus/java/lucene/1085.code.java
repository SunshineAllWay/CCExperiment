package org.apache.lucene.search.similar;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.Version;
public final class MoreLikeThis {
    public static final int DEFAULT_MAX_NUM_TOKENS_PARSED=5000;
    public static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer(Version.LUCENE_CURRENT);
    public static final int DEFAULT_MIN_TERM_FREQ = 2;
    public static final int DEFAULT_MIN_DOC_FREQ = 5;
    public static final int DEFAULT_MAX_DOC_FREQ = Integer.MAX_VALUE;
    public static final boolean DEFAULT_BOOST = false;
    public static final String[] DEFAULT_FIELD_NAMES = new String[] { "contents"};
    public static final int DEFAULT_MIN_WORD_LENGTH = 0;
    public static final int DEFAULT_MAX_WORD_LENGTH = 0;
	public static final Set<?> DEFAULT_STOP_WORDS = null;
	private Set<?> stopWords = DEFAULT_STOP_WORDS;
    public static final int DEFAULT_MAX_QUERY_TERMS = 25;
    private Analyzer analyzer = DEFAULT_ANALYZER;
    private int minTermFreq = DEFAULT_MIN_TERM_FREQ;
    private int minDocFreq = DEFAULT_MIN_DOC_FREQ;
	private int maxDocFreq = DEFAULT_MAX_DOC_FREQ;
    private boolean boost = DEFAULT_BOOST;
    private String[] fieldNames = DEFAULT_FIELD_NAMES;
	private int maxNumTokensParsed=DEFAULT_MAX_NUM_TOKENS_PARSED;   
    private int minWordLen = DEFAULT_MIN_WORD_LENGTH;
    private int maxWordLen = DEFAULT_MAX_WORD_LENGTH;
    private int maxQueryTerms = DEFAULT_MAX_QUERY_TERMS;
    private Similarity similarity;
    private final IndexReader ir;
    private float boostFactor = 1;
    public float getBoostFactor() {
        return boostFactor;
    }
    public void setBoostFactor(float boostFactor) {
        this.boostFactor = boostFactor;
    }
    public MoreLikeThis(IndexReader ir) {
        this(ir, new DefaultSimilarity());
    }
    public MoreLikeThis(IndexReader ir, Similarity sim){
      this.ir = ir;
      this.similarity = sim;
    }
  public Similarity getSimilarity() {
    return similarity;
  }
  public void setSimilarity(Similarity similarity) {
    this.similarity = similarity;
  }
    public Analyzer getAnalyzer() {
        return analyzer;
    }
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
    public int getMinTermFreq() {
        return minTermFreq;
    }
    public void setMinTermFreq(int minTermFreq) {
        this.minTermFreq = minTermFreq;
    }
    public int getMinDocFreq() {
        return minDocFreq;
    }
    public void setMinDocFreq(int minDocFreq) {
        this.minDocFreq = minDocFreq;
    }
    public int getMaxDocFreq() {
        return maxDocFreq;
    }
	public void setMaxDocFreq(int maxFreq) {
		this.maxDocFreq = maxFreq;
	}
	public void setMaxDocFreqPct(int maxPercentage) {
		this.maxDocFreq = maxPercentage * ir.numDocs() / 100;
	}
    public boolean isBoost() {
        return boost;
    }
    public void setBoost(boolean boost) {
        this.boost = boost;
    }
    public String[] getFieldNames() {
        return fieldNames;
    }
    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }
    public int getMinWordLen() {
        return minWordLen;
    }
    public void setMinWordLen(int minWordLen) {
        this.minWordLen = minWordLen;
    }
    public int getMaxWordLen() {
        return maxWordLen;
    }
    public void setMaxWordLen(int maxWordLen) {
        this.maxWordLen = maxWordLen;
    }
	public void setStopWords(Set<?> stopWords) {
		this.stopWords = stopWords;
	}
	public Set<?> getStopWords() {
		return stopWords;
	}
    public int getMaxQueryTerms() {
        return maxQueryTerms;
    }
    public void setMaxQueryTerms(int maxQueryTerms) {
        this.maxQueryTerms = maxQueryTerms;
    }
	public int getMaxNumTokensParsed()
	{
		return maxNumTokensParsed;
	}
	public void setMaxNumTokensParsed(int i)
	{
		maxNumTokensParsed = i;
	}
    public Query like(int docNum) throws IOException {
        if (fieldNames == null) {
            Collection<String> fields = ir.getFieldNames( IndexReader.FieldOption.INDEXED);
            fieldNames = fields.toArray(new String[fields.size()]);
        }
        return createQuery(retrieveTerms(docNum));
    }
    public Query like(File f) throws IOException {
        if (fieldNames == null) {
            Collection<String> fields = ir.getFieldNames( IndexReader.FieldOption.INDEXED);
            fieldNames = fields.toArray(new String[fields.size()]);
        }
        return like(new FileReader(f));
    }
    public Query like(URL u) throws IOException {
        return like(new InputStreamReader(u.openConnection().getInputStream()));
    }
    public Query like(java.io.InputStream is) throws IOException {
        return like(new InputStreamReader(is));
    }
    public Query like(Reader r) throws IOException {
        return createQuery(retrieveTerms(r));
    }
    private Query createQuery(PriorityQueue<Object[]> q) {
        BooleanQuery query = new BooleanQuery();
        Object cur;
        int qterms = 0;
        float bestScore = 0;
        while (((cur = q.pop()) != null)) {
            Object[] ar = (Object[]) cur;
            TermQuery tq = new TermQuery(new Term((String) ar[1], (String) ar[0]));
            if (boost) {
                if (qterms == 0) {
                    bestScore = ((Float) ar[2]).floatValue();
                }
                float myScore = ((Float) ar[2]).floatValue();
                tq.setBoost(boostFactor * myScore / bestScore);
            }
            try {
                query.add(tq, BooleanClause.Occur.SHOULD);
            }
            catch (BooleanQuery.TooManyClauses ignore) {
                break;
            }
            qterms++;
            if (maxQueryTerms > 0 && qterms >= maxQueryTerms) {
                break;
            }
        }
        return query;
    }
    private PriorityQueue<Object[]> createQueue(Map<String,Int> words) throws IOException {
        int numDocs = ir.numDocs();
        FreqQ res = new FreqQ(words.size()); 
        Iterator<String> it = words.keySet().iterator();
        while (it.hasNext()) { 
            String word = it.next();
            int tf = words.get(word).x; 
            if (minTermFreq > 0 && tf < minTermFreq) {
                continue; 
            }
            String topField = fieldNames[0];
            int docFreq = 0;
            for (int i = 0; i < fieldNames.length; i++) {
                int freq = ir.docFreq(new Term(fieldNames[i], word));
                topField = (freq > docFreq) ? fieldNames[i] : topField;
                docFreq = (freq > docFreq) ? freq : docFreq;
            }
            if (minDocFreq > 0 && docFreq < minDocFreq) {
                continue; 
            }
            if (docFreq > maxDocFreq) {
                continue; 
            }
            if (docFreq == 0) {
                continue; 
            }
            float idf = similarity.idf(docFreq, numDocs);
            float score = tf * idf;
            res.insertWithOverflow(new Object[]{word,                   
                                    topField,               
                                    Float.valueOf(score),       
                                    Float.valueOf(idf),         
                                    Integer.valueOf(docFreq),   
                                    Integer.valueOf(tf)
            });
        }
        return res;
    }
    public String describeParams() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t" + "maxQueryTerms  : " + maxQueryTerms + "\n");
        sb.append("\t" + "minWordLen     : " + minWordLen + "\n");
        sb.append("\t" + "maxWordLen     : " + maxWordLen + "\n");
        sb.append("\t" + "fieldNames     : ");
        String delim = "";
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            sb.append(delim).append(fieldName);
            delim = ", ";
        }
        sb.append("\n");
        sb.append("\t" + "boost          : " + boost + "\n");
        sb.append("\t" + "minTermFreq    : " + minTermFreq + "\n");
        sb.append("\t" + "minDocFreq     : " + minDocFreq + "\n");
        return sb.toString();
    }
    public static void main(String[] a) throws Throwable {
        String indexName = "localhost_index";
        String fn = "c:/Program Files/Apache Group/Apache/htdocs/manual/vhosts/index.html.en";
        URL url = null;
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals("-i")) {
                indexName = a[++i];
            }
            else if (a[i].equals("-f")) {
                fn = a[++i];
            }
            else if (a[i].equals("-url")) {
                url = new URL(a[++i]);
            }
        }
        PrintStream o = System.out;
        FSDirectory dir = FSDirectory.open(new File(indexName));
        IndexReader r = IndexReader.open(dir, true);
        o.println("Open index " + indexName + " which has " + r.numDocs() + " docs");
        MoreLikeThis mlt = new MoreLikeThis(r);
        o.println("Query generation parameters:");
        o.println(mlt.describeParams());
        o.println();
        Query query = null;
        if (url != null) {
            o.println("Parsing URL: " + url);
            query = mlt.like(url);
        }
        else if (fn != null) {
            o.println("Parsing file: " + fn);
            query = mlt.like(new File(fn));
        }
        o.println("q: " + query);
        o.println();
        IndexSearcher searcher = new IndexSearcher(dir, true);
        TopDocs hits = searcher.search(query, null, 25);
        int len = hits.totalHits;
        o.println("found: " + len + " documents matching");
        o.println();
        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (int i = 0; i < Math.min(25, len); i++) {
            Document d = searcher.doc(scoreDocs[i].doc);
			String summary = d.get( "summary");
            o.println("score  : " + scoreDocs[i].score);
            o.println("url    : " + d.get("url"));
            o.println("\ttitle  : " + d.get("title"));
			if ( summary != null)
				o.println("\tsummary: " + d.get("summary"));
            o.println();
        }
    }
    public PriorityQueue<Object[]> retrieveTerms(int docNum) throws IOException {
        Map<String,Int> termFreqMap = new HashMap<String,Int>();
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            TermFreqVector vector = ir.getTermFreqVector(docNum, fieldName);
            if (vector == null) {
            	Document d=ir.document(docNum);
            	String text[]=d.getValues(fieldName);
            	if(text!=null)
            	{
                for (int j = 0; j < text.length; j++) {
                  addTermFrequencies(new StringReader(text[j]), termFreqMap, fieldName);
                }
            	}
            }
            else {
				addTermFrequencies(termFreqMap, vector);
            }
        }
        return createQueue(termFreqMap);
    }
	private void addTermFrequencies(Map<String,Int> termFreqMap, TermFreqVector vector)
	{
		String[] terms = vector.getTerms();
		int freqs[]=vector.getTermFrequencies();
		for (int j = 0; j < terms.length; j++) {
		    String term = terms[j];
			if(isNoiseWord(term)){
				continue;
			}
		    Int cnt = termFreqMap.get(term);
		    if (cnt == null) {
		    	cnt=new Int();
				termFreqMap.put(term, cnt);
				cnt.x=freqs[j];				
		    }
		    else {
		        cnt.x+=freqs[j];
		    }
		}
	}
	private void addTermFrequencies(Reader r, Map<String,Int> termFreqMap, String fieldName)
		throws IOException
	{
		   TokenStream ts = analyzer.tokenStream(fieldName, r);
			int tokenCount=0;
			TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
			while (ts.incrementToken()) {
				String word = termAtt.term();
				tokenCount++;
				if(tokenCount>maxNumTokensParsed)
				{
					break;
				}
				if(isNoiseWord(word)){
					continue;
				}
				Int cnt = termFreqMap.get(word);
				if (cnt == null) {
					termFreqMap.put(word, new Int());
				}
				else {
					cnt.x++;
				}
			}
	}
	private boolean isNoiseWord(String term)
	{
		int len = term.length();
		if (minWordLen > 0 && len < minWordLen) {
			return true;
		}
		if (maxWordLen > 0 && len > maxWordLen) {
			return true;
		}
		if (stopWords != null && stopWords.contains( term)) {
			return true;
		}
		return false;
	}
    public PriorityQueue<Object[]> retrieveTerms(Reader r) throws IOException {
        Map<String,Int> words = new HashMap<String,Int>();
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
			addTermFrequencies(r, words, fieldName);
        }
        return createQueue(words);
    }
  public String [] retrieveInterestingTerms(int docNum) throws IOException{
    ArrayList<Object> al = new ArrayList<Object>( maxQueryTerms);
		PriorityQueue<Object[]> pq = retrieveTerms(docNum);
		Object cur;
		int lim = maxQueryTerms; 
		while (((cur = pq.pop()) != null) && lim-- > 0) {
            Object[] ar = (Object[]) cur;
			al.add( ar[ 0]); 
		}
		String[] res = new String[ al.size()];
		return al.toArray( res);
  }
	public String[] retrieveInterestingTerms( Reader r) throws IOException {
		ArrayList<Object> al = new ArrayList<Object>( maxQueryTerms);
		PriorityQueue<Object[]> pq = retrieveTerms( r);
		Object cur;
		int lim = maxQueryTerms; 
		while (((cur = pq.pop()) != null) && lim-- > 0) {
            Object[] ar = (Object[]) cur;
			al.add( ar[ 0]); 
		}
		String[] res = new String[ al.size()];
		return al.toArray( res);
	}
    private static class FreqQ extends PriorityQueue<Object[]> {
        FreqQ (int s) {
            initialize(s);
        }
        @Override
        protected boolean lessThan(Object[] aa, Object[] bb) {
            Float fa = (Float) aa[2];
            Float fb = (Float) bb[2];
            return fa.floatValue() > fb.floatValue();
        }
    }
    private static class Int {
        int x;
        Int() {
            x = 1;
        }
    }
}
