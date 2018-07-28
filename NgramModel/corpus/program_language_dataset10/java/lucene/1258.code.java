package org.apache.lucene.search;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
public class TestRemoteSort extends LuceneTestCase implements Serializable {
  private Searcher full;
  private Query queryX;
  private Query queryY;
  private Query queryA;
  private Query queryF;
  private Sort sort;
  public TestRemoteSort (String name) {
    super (name);
  }
  public static void main (String[] argv) {
    if (argv == null || argv.length < 1)
      TestRunner.run (suite());
    else if ("server".equals (argv[0])) {
      TestRemoteSort test = new TestRemoteSort (null);
      try {
        test.startServer();
        Thread.sleep (500000);
      } catch (Exception e) {
        System.out.println (e);
        e.printStackTrace();
      }
    }
  }
  public static Test suite() {
    return new TestSuite (TestRemoteSort.class);
  }
  private String[][] data = new String[][] {
  {   "A",   "x a",           "5",           "4f",           "c",     "A-3",   "p\u00EAche",      "10",           "-4.0", "3", "126", "J"},
  {   "B",   "y a",           "5",           "3.4028235E38", "i",     "B-10",  "HAT",             "1000000000", "40.0", "24", "1", "I"},
  {   "C",   "x a b c",       "2147483647",  "1.0",          "j",     "A-2",   "p\u00E9ch\u00E9", "99999999",   "40.00002343", "125", "15", "H"},
  {   "D",   "y a b c",       "-1",          "0.0f",         "a",     "C-0",   "HUT",             String.valueOf(Long.MAX_VALUE),           String.valueOf(Double.MIN_VALUE), String.valueOf(Short.MIN_VALUE), String.valueOf(Byte.MIN_VALUE), "G"},
  {   "E",   "x a b c d",     "5",           "2f",           "h",     "B-8",   "peach",           String.valueOf(Long.MIN_VALUE),           String.valueOf(Double.MAX_VALUE), String.valueOf(Short.MAX_VALUE),           String.valueOf(Byte.MAX_VALUE), "F"},
  {   "F",   "y a b c d",     "2",           "3.14159f",     "g",     "B-1",   "H\u00C5T",        "-44",           "343.034435444", "-3", "0", "E"},
  {   "G",   "x a b c d",     "3",           "-1.0",         "f",     "C-100", "sin",             "323254543543", "4.043544", "5", "100", "D"},
  {   "H",   "y a b c d",     "0",           "1.4E-45",      "e",     "C-88",  "H\u00D8T",        "1023423423005","4.043545", "10", "-50", "C"},
  {   "I",   "x a b c d e f", "-2147483648", "1.0e+0",       "d",     "A-10",  "s\u00EDn",        "332422459999", "4.043546", "-340", "51", "B"},
  {   "J",   "y a b c d e f", "4",           ".5",           "b",     "C-7",   "HOT",             "34334543543",  "4.0000220343", "300", "2", "A"},
  {   "W",   "g",             "1",           null,           null,    null,    null,              null,           null, null, null, null},
  {   "X",   "g",             "1",           "0.1",          null,    null,    null,              null,           null, null, null, null},
  {   "Y",   "g",             "1",           "0.2",          null,    null,    null,              null,           null, null, null, null},
  {   "Z",   "f g",           null,          null,           null,    null,    null,              null,           null, null, null, null}
  };
  private Searcher getIndex (boolean even, boolean odd)
  throws IOException {
    RAMDirectory indexStore = new RAMDirectory ();
    IndexWriter writer = new IndexWriter(indexStore, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT))
        .setMaxBufferedDocs(2));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(1000);
    for (int i=0; i<data.length; ++i) {
      if (((i%2)==0 && even) || ((i%2)==1 && odd)) {
        Document doc = new Document();
        doc.add (new Field ("tracer",   data[i][0], Field.Store.YES, Field.Index.NO));
        doc.add (new Field ("contents", data[i][1], Field.Store.NO, Field.Index.ANALYZED));
        if (data[i][2] != null) doc.add (new Field ("int",      data[i][2], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][3] != null) doc.add (new Field ("float",    data[i][3], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][4] != null) doc.add (new Field ("string",   data[i][4], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][5] != null) doc.add (new Field ("custom",   data[i][5], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][6] != null) doc.add (new Field ("i18n",     data[i][6], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][7] != null) doc.add (new Field ("long",     data[i][7], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][8] != null) doc.add (new Field ("double",     data[i][8], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][9] != null) doc.add (new Field ("short",     data[i][9], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][10] != null) doc.add (new Field ("byte",     data[i][10], Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (data[i][11] != null) doc.add (new Field ("parser",     data[i][11], Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.setBoost(2);  
        writer.addDocument (doc);
      }
    }
    writer.close ();
    IndexSearcher s = new IndexSearcher (indexStore, false);
    s.setDefaultFieldSortScoring(true, true);
    return s;
  }
  private Searcher getFullIndex()
  throws IOException {
    return getIndex (true, true);
  }
  public String getRandomNumberString(int num, int low, int high) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < num; i++) {
      sb.append(getRandomNumber(low, high));
    }
    return sb.toString();
  }
  public String getRandomCharString(int num) {
    return getRandomCharString(num, 48, 122);
  }
  public String getRandomCharString(int num, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < num; i++) {
      sb.append(new Character((char) getRandomNumber(start, end)));
    }
    return sb.toString();
  }
  Random r;
  public int getRandomNumber(final int low, final int high) {
    int randInt = (Math.abs(r.nextInt()) % (high - low)) + low;
    return randInt;
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    full = getFullIndex();
    queryX = new TermQuery (new Term ("contents", "x"));
    queryY = new TermQuery (new Term ("contents", "y"));
    queryA = new TermQuery (new Term ("contents", "a"));
    queryF = new TermQuery (new Term ("contents", "f"));
    sort = new Sort();
  }
  static class MyFieldComparator extends FieldComparator {
    int[] docValues;
    int[] slotValues;
    int bottomValue;
    MyFieldComparator(int numHits) {
      slotValues = new int[numHits];
    }
    @Override
    public void copy(int slot, int doc) {
      slotValues[slot] = docValues[doc];
    }
    @Override
    public int compare(int slot1, int slot2) {
      return slotValues[slot1] - slotValues[slot2];
    }
    @Override
    public int compareBottom(int doc) {
      return bottomValue - docValues[doc];
    }
    @Override
    public void setBottom(int bottom) {
      bottomValue = slotValues[bottom];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      docValues = FieldCache.DEFAULT.getInts(reader, "parser", new FieldCache.IntParser() {
          public final int parseInt(final String val) {
            return (val.charAt(0)-'A') * 123456;
          }
        });
    }
    @Override
    public Comparable<?> value(int slot) {
      return Integer.valueOf(slotValues[slot]);
    }
  }
  static class MyFieldComparatorSource extends FieldComparatorSource {
    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) {
      return new MyFieldComparator(numHits);
    }
  }
  public void testRemoteSort() throws Exception {
    Searchable searcher = getRemote();
    MultiSearcher multi = new MultiSearcher (new Searchable[] { searcher });
    runMultiSorts(multi, true); 
  }
  public void testNormalizedScores() throws Exception {
    HashMap<String,Float> scoresX = getScores (full.search (queryX, null, 1000).scoreDocs, full);
    HashMap<String,Float> scoresY = getScores (full.search (queryY, null, 1000).scoreDocs, full);
    HashMap<String,Float> scoresA = getScores (full.search (queryA, null, 1000).scoreDocs, full);
    MultiSearcher remote = new MultiSearcher (new Searchable[] { getRemote() });
    sort = new Sort();
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort(SortField.FIELD_DOC);
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField("int", SortField.INT));
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField("float", SortField.FLOAT));
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField("string", SortField.STRING));
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField("int", SortField.INT), new SortField("float", SortField.FLOAT));
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField ("int", SortField.INT, true), new SortField (null, SortField.DOC, true) );
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
    sort.setSort (new SortField("float", SortField.FLOAT), new SortField("string", SortField.STRING));
    assertSameValues (scoresX, getScores (remote.search (queryX, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresY, getScores (remote.search (queryY, null, 1000, sort).scoreDocs, remote));
    assertSameValues (scoresA, getScores (remote.search (queryA, null, 1000, sort).scoreDocs, remote));
  }
  private void runMultiSorts(Searcher multi, boolean isFull) throws Exception {
    sort.setSort(SortField.FIELD_DOC);
    String expected = isFull ? "ABCDEFGHIJ" : "ACEGIBDFHJ";
    assertMatches(multi, queryA, sort, expected);
    sort.setSort(new SortField ("int", SortField.INT));
    expected = isFull ? "IDHFGJABEC" : "IDHFGJAEBC";
    assertMatches(multi, queryA, sort, expected);
    sort.setSort(new SortField ("int", SortField.INT), SortField.FIELD_DOC);
    expected = isFull ? "IDHFGJABEC" : "IDHFGJAEBC";
    assertMatches(multi, queryA, sort, expected);
    sort.setSort(new SortField ("float", SortField.FLOAT), SortField.FIELD_DOC);
    assertMatches(multi, queryA, sort, "GDHJCIEFAB");
    sort.setSort(new SortField("float", SortField.FLOAT));
    assertMatches(multi, queryA, sort, "GDHJCIEFAB");
    sort.setSort(new SortField("string", SortField.STRING));
    assertMatches(multi, queryA, sort, "DJAIHGFEBC");
    sort.setSort(new SortField ("int", SortField.INT, true));
    expected = isFull ? "CABEJGFHDI" : "CAEBJGFHDI";
    assertMatches(multi, queryA, sort, expected);
    sort.setSort(new SortField ("float", SortField.FLOAT, true));
    assertMatches(multi, queryA, sort, "BAFECIJHDG");
    sort.setSort(new SortField ("string", SortField.STRING, true));
    assertMatches(multi, queryA, sort, "CBEFGHIAJD");
    sort.setSort(new SortField ("int", SortField.INT), new SortField ("float", SortField.FLOAT));
    assertMatches(multi, queryA, sort, "IDHFGJEABC");
    sort.setSort(new SortField ("float", SortField.FLOAT), new SortField ("string", SortField.STRING));
    assertMatches(multi, queryA, sort, "GDHJICEFAB");
    sort.setSort(new SortField ("int", SortField.INT));
    assertMatches(multi, queryF, sort, "IZJ");
    sort.setSort(new SortField ("int", SortField.INT, true));
    assertMatches(multi, queryF, sort, "JZI");
    sort.setSort(new SortField ("float", SortField.FLOAT));
    assertMatches(multi, queryF, sort, "ZJI");
    sort.setSort(new SortField ("string", SortField.STRING));
    assertMatches(multi, queryF, sort, "ZJI");
    sort.setSort(new SortField ("string", SortField.STRING, true));
    assertMatches(multi, queryF, sort, "IJZ");
    assertSaneFieldCaches(getName() + " Basics");
    FieldCache.DEFAULT.purgeAllCaches();
    sort.setSort(new SortField ("string", Locale.US) );
    assertMatches(multi, queryA, sort, "DJAIHGFEBC");
    sort.setSort(new SortField ("string", Locale.US, true));
    assertMatches(multi, queryA, sort, "CBEFGHIAJD");
    assertSaneFieldCaches(getName() + " Locale.US");
    FieldCache.DEFAULT.purgeAllCaches();
  }
  private void assertMatches(Searcher searcher, Query query, Sort sort,
      String expectedResult) throws IOException {
    TopDocs hits = searcher.search (query, null, expectedResult.length(), sort);
    ScoreDoc[] result = hits.scoreDocs;
    assertEquals(hits.totalHits, expectedResult.length());
    StringBuilder buff = new StringBuilder(10);
    int n = result.length;
    for (int i=0; i<n; ++i) {
      Document doc = searcher.doc(result[i].doc);
      String[] v = doc.getValues("tracer");
      for (int j=0; j<v.length; ++j) {
        buff.append (v[j]);
      }
    }
    assertEquals (expectedResult, buff.toString());
  }
  private HashMap<String, Float> getScores (ScoreDoc[] hits, Searcher searcher)
  throws IOException {
    HashMap<String, Float> scoreMap = new HashMap<String, Float>();
    int n = hits.length;
    for (int i=0; i<n; ++i) {
      Document doc = searcher.doc(hits[i].doc);
      String[] v = doc.getValues("tracer");
      assertEquals (v.length, 1);
      scoreMap.put (v[0], Float.valueOf(hits[i].score));
    }
    return scoreMap;
  }
  private void assertSameValues (HashMap<?, ?> m1, HashMap<?, ?> m2) {
    int n = m1.size();
    int m = m2.size();
    assertEquals (n, m);
    Iterator<?> iter = m1.keySet().iterator();
    while (iter.hasNext()) {
      Object key = iter.next();
      Object o1 = m1.get(key);
      Object o2 = m2.get(key);
      if (o1 instanceof Float) {
        assertEquals(((Float)o1).floatValue(), ((Float)o2).floatValue(), 1e-6);
      } else {
        assertEquals (m1.get(key), m2.get(key));
      }
    }
  }
  private Searchable getRemote () throws Exception {
    try {
      return lookupRemote ();
    } catch (Throwable e) {
      startServer ();
      return lookupRemote ();
    }
  }
  private Searchable lookupRemote () throws Exception {
    return (Searchable) Naming.lookup ("//localhost:" + port + "/SortedSearchable");
  }
  private int port = -1;
  private void startServer () throws Exception {
    port = _TestUtil.getRandomSocketPort();
    Searcher local = getFullIndex();
    LocateRegistry.createRegistry (port);
    RemoteSearchable impl = new RemoteSearchable (local);
    Naming.rebind ("//localhost:" + port + "/SortedSearchable", impl);
  }
}
