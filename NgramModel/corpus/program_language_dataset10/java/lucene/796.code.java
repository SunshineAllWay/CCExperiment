package org.apache.lucene.analysis.ru;
import org.apache.lucene.util.LuceneTestCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
@Deprecated
public class TestRussianStem extends LuceneTestCase
{
    private ArrayList<String> words = new ArrayList<String>();
    private ArrayList<String> stems = new ArrayList<String>();
    public TestRussianStem(String name)
    {
        super(name);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String str;
        BufferedReader inWords =
            new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("wordsUTF8.txt"),
                    "UTF-8"));
        while ((str = inWords.readLine()) != null)
        {
            words.add(str);
        }
        inWords.close();
        BufferedReader inStems =
            new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("stemsUTF8.txt"),
                    "UTF-8"));
        while ((str = inStems.readLine()) != null)
        {
            stems.add(str);
        }
        inStems.close();
    }
    public void testStem()
    {
        for (int i = 0; i < words.size(); i++)
        {
            String realStem =
                RussianStemmer.stemWord(
                    words.get(i));
            assertEquals("unicode", stems.get(i), realStem);
        }
    }
}
