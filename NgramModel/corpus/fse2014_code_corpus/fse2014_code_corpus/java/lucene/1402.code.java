package org.apache.lucene.xmlparser;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
public class TestQueryTemplateManager extends LuceneTestCase {
	CoreParser builder;
	Analyzer analyzer=new StandardAnalyzer(TEST_VERSION_CURRENT);
	private IndexSearcher searcher;
	String docFieldValues []=
	{
			"artist=Jeff Buckley \talbum=Grace \treleaseDate=1999 \tgenre=rock",
			"artist=Fugazi \talbum=Repeater \treleaseDate=1990 \tgenre=alternative",
			"artist=Fugazi \talbum=Red Medicine \treleaseDate=1995 \tgenre=alternative",
			"artist=Peeping Tom \talbum=Peeping Tom \treleaseDate=2006 \tgenre=rock",
			"artist=Red Snapper \talbum=Prince Blimey \treleaseDate=1996 \tgenre=electronic"
	};
	String queryForms[]=
	{
			"artist=Fugazi \texpectedMatches=2 \ttemplate=albumBooleanQuery",
			"artist=Fugazi \treleaseDate=1990 \texpectedMatches=1 \ttemplate=albumBooleanQuery",
			"artist=Buckley \tgenre=rock \texpectedMatches=1 \ttemplate=albumFilteredQuery",
			"artist=Buckley \tgenre=electronic \texpectedMatches=0 \ttemplate=albumFilteredQuery",
			"queryString=artist:buckly~ NOT genre:electronic \texpectedMatches=1 \ttemplate=albumLuceneClassicQuery"
	};
	public void testFormTransforms() throws SAXException, IOException, ParserConfigurationException, TransformerException, ParserException 
	{
		QueryTemplateManager qtm=new QueryTemplateManager();
		qtm.addQueryTemplate("albumBooleanQuery", getClass().getResourceAsStream("albumBooleanQuery.xsl"));
		qtm.addQueryTemplate("albumFilteredQuery", getClass().getResourceAsStream("albumFilteredQuery.xsl"));
		qtm.addQueryTemplate("albumLuceneClassicQuery", getClass().getResourceAsStream("albumLuceneClassicQuery.xsl"));
		for (int i = 0; i < queryForms.length; i++)
		{
			Properties queryFormProperties=getPropsFromString(queryForms[i]);
			Document doc=qtm.getQueryAsDOM(queryFormProperties,queryFormProperties.getProperty("template"));
			Query q=builder.getQuery(doc.getDocumentElement());
			int h=searcher.search(q, null, 1000).totalHits;
			int expectedHits=Integer.parseInt(queryFormProperties.getProperty("expectedMatches"));
			assertEquals("Number of results should match for query "+queryForms[i],expectedHits,h);
		}
	}
	Properties getPropsFromString(String nameValuePairs)
	{
		Properties result=new Properties();
		StringTokenizer st=new StringTokenizer(nameValuePairs,"\t=");
		while(st.hasMoreTokens())
		{
			String name=st.nextToken().trim();
			if(st.hasMoreTokens())
			{
				String value=st.nextToken().trim();
				result.setProperty(name,value);
			}
		}
		return result;
	}
	org.apache.lucene.document.Document getDocumentFromString(String nameValuePairs)
	{
		org.apache.lucene.document.Document result=new org.apache.lucene.document.Document();
		StringTokenizer st=new StringTokenizer(nameValuePairs,"\t=");
		while(st.hasMoreTokens())
		{
			String name=st.nextToken().trim();
			if(st.hasMoreTokens())
			{
				String value=st.nextToken().trim();
				result.add(new Field(name,value,Field.Store.YES,Field.Index.ANALYZED));
			}
		}
		return result;
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RAMDirectory dir=new RAMDirectory();
		IndexWriter w=new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
		for (int i = 0; i < docFieldValues.length; i++)
		{
			w.addDocument(getDocumentFromString(docFieldValues[i]));
		}
		w.optimize();
		w.close();
		searcher=new IndexSearcher(dir, true);
		builder=new CorePlusExtensionsParser("artist", analyzer);
	}
	@Override
	protected void tearDown() throws Exception {
		searcher.close();
    super.tearDown();
	}
}
