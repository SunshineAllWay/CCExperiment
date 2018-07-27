package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class BooleanFilterTest extends LuceneTestCase {
	private RAMDirectory directory;
	private IndexReader reader;
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
		directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
		addDoc(writer, "admin guest", "010", "20040101","Y");
		addDoc(writer, "guest", "020", "20040101","Y");
		addDoc(writer, "guest", "020", "20050101","Y");
		addDoc(writer, "admin", "020", "20050101","Maybe");
		addDoc(writer, "admin guest", "030", "20050101","N");
		writer.close();
		reader=IndexReader.open(directory, true);			
	}
	private void addDoc(IndexWriter writer, String accessRights, String price, String date, String inStock) throws IOException
	{
		Document doc=new Document();
		doc.add(new Field("accessRights",accessRights,Field.Store.YES,Field.Index.ANALYZED));
		doc.add(new Field("price",price,Field.Store.YES,Field.Index.ANALYZED));
		doc.add(new Field("date",date,Field.Store.YES,Field.Index.ANALYZED));
		doc.add(new Field("inStock",inStock,Field.Store.YES,Field.Index.ANALYZED));
		writer.addDocument(doc);
	}
  private Filter getRangeFilter(String field,String lowerPrice, String upperPrice)
	{
    Filter f = new TermRangeFilter(field,lowerPrice,upperPrice,true,true);
    return f;
	}
  private Filter getTermsFilter(String field,String text)
	{
		TermsFilter tf=new TermsFilter();
		tf.addTerm(new Term(field,text));
		return tf;
	}
        private void tstFilterCard(String mes, int expected, Filter filt)
        throws Throwable
        {
          DocIdSetIterator disi = filt.getDocIdSet(reader).iterator();
          int actual = 0;
          while (disi.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
            actual++;
          }
          assertEquals(mes, expected, actual);
        }
	public void testShould() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getTermsFilter("price","030"),BooleanClause.Occur.SHOULD));
    tstFilterCard("Should retrieves only 1 doc",1,booleanFilter);
	}
	public void testShoulds() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getRangeFilter("price","010", "020"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getRangeFilter("price","020", "030"),BooleanClause.Occur.SHOULD));
    tstFilterCard("Shoulds are Ored together",5,booleanFilter);
	}
	public void testShouldsAndMustNot() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getRangeFilter("price","010", "020"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getRangeFilter("price","020", "030"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getTermsFilter("inStock", "N"),BooleanClause.Occur.MUST_NOT));
    tstFilterCard("Shoulds Ored but AndNot",4,booleanFilter);
    booleanFilter.add(new FilterClause(getTermsFilter("inStock", "Maybe"),BooleanClause.Occur.MUST_NOT));
    tstFilterCard("Shoulds Ored but AndNots",3,booleanFilter);
	}
	public void testShouldsAndMust() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getRangeFilter("price","010", "020"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getRangeFilter("price","020", "030"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getTermsFilter("accessRights", "admin"),BooleanClause.Occur.MUST));
    tstFilterCard("Shoulds Ored but MUST",3,booleanFilter);
	}
	public void testShouldsAndMusts() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getRangeFilter("price","010", "020"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getRangeFilter("price","020", "030"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getTermsFilter("accessRights", "admin"),BooleanClause.Occur.MUST));
    booleanFilter.add(new FilterClause(getRangeFilter("date","20040101", "20041231"),BooleanClause.Occur.MUST));
    tstFilterCard("Shoulds Ored but MUSTs ANDED",1,booleanFilter);
	}
	public void testShouldsAndMustsAndMustNot() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getRangeFilter("price","030", "040"),BooleanClause.Occur.SHOULD));
    booleanFilter.add(new FilterClause(getTermsFilter("accessRights", "admin"),BooleanClause.Occur.MUST));
    booleanFilter.add(new FilterClause(getRangeFilter("date","20050101", "20051231"),BooleanClause.Occur.MUST));
    booleanFilter.add(new FilterClause(getTermsFilter("inStock","N"),BooleanClause.Occur.MUST_NOT));
    tstFilterCard("Shoulds Ored but MUSTs ANDED and MustNot",0,booleanFilter);
	}
	public void testJustMust() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getTermsFilter("accessRights", "admin"),BooleanClause.Occur.MUST));
    tstFilterCard("MUST",3,booleanFilter);
	}
	public void testJustMustNot() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getTermsFilter("inStock","N"),BooleanClause.Occur.MUST_NOT));
    tstFilterCard("MUST_NOT",4,booleanFilter);
	}
	public void testMustAndMustNot() throws Throwable
	{
    BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(getTermsFilter("inStock","N"),BooleanClause.Occur.MUST));
    booleanFilter.add(new FilterClause(getTermsFilter("price","030"),BooleanClause.Occur.MUST_NOT));
    tstFilterCard("MUST_NOT wins over MUST for same docs",0,booleanFilter);
	}
}
