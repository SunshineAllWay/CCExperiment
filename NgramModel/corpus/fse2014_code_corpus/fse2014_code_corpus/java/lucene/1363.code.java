package org.apache.lucene.wordnet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
public class Syns2Index
{
	private static final PrintStream o = System.out;
	private static final PrintStream err = System.err;
	public static final String F_SYN = "syn";
	public static final String F_WORD = "word";
    private static final Analyzer ana = new StandardAnalyzer(Version.LUCENE_CURRENT);
    public static void main(String[] args)
        throws Throwable
    {
        String prologFilename = null; 
        String indexDir = null;
        if (args.length == 2)
        {
            prologFilename = args[0];
            indexDir = args[1];
        }
        else
        {
            usage();
            System.exit(1);
        }
        if (! (new File(prologFilename)).canRead())
        {
            err.println("Error: cannot read Prolog file: " + prologFilename);
            System.exit(1);
        }
        if ((new File(indexDir)).isDirectory())
        {
            err.println("Error: index directory already exists: " + indexDir);
            err.println("Please specify a name of a non-existent directory");
            System.exit(1);
        }
        o.println("Opening Prolog file " + prologFilename);
        final FileInputStream fis = new FileInputStream(prologFilename);
        final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        final Map<String,List<String>> word2Nums = new TreeMap<String,List<String>>();
        final Map<String,List<String>> num2Words = new TreeMap<String,List<String>>();
        int ndecent = 0;
        int mod = 1;
        int row = 1;
		o.println( "[1/2] Parsing " + prologFilename);
        while ((line = br.readLine()) != null)
        {
            if ((++row) % mod == 0) 
            {
                mod *= 2;
                o.println("\t" + row + " " + line + " " + word2Nums.size()
                    + " " + num2Words.size() + " ndecent=" + ndecent);
            }
            if (! line.startsWith("s("))
            {
                err.println("OUCH: " + line);
                System.exit(1);
            }
            line = line.substring(2);
            int comma = line.indexOf(',');
            String num = line.substring(0, comma);
            int q1 = line.indexOf('\'');
            line = line.substring(q1 + 1);
            int q2 = line.lastIndexOf('\'');
            String word = line.substring(0, q2).toLowerCase().replace("''", "'");
            if (! isDecent(word))
            {
                ndecent++;
                continue; 
            }
            List<String> lis = word2Nums.get(word);
            if (lis == null)
            {
                lis = new LinkedList<String>();
                lis.add(num);
                word2Nums.put(word, lis);
            }
            else
                lis.add(num);
            lis = num2Words.get(num);
            if (lis == null)
            {
                lis = new LinkedList<String>();
                lis.add(word);
                num2Words.put(num, lis);
            }
            else
                lis.add(word);
        }
        fis.close();
        br.close();
		o.println( "[2/2] Building index to store synonyms, " +
				   " map sizes are " + word2Nums.size() + " and " + num2Words.size());
        index(indexDir, word2Nums, num2Words);
    }
    private static boolean isDecent(String s)
    {
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            if (!Character.isLetter(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
    private static void index(String indexDir, Map<String,List<String>> word2Nums, Map<String,List<String>> num2Words)
        throws Throwable
    {
        int row = 0;
        int mod = 1;
        FSDirectory dir = FSDirectory.open(new File(indexDir));
        try {
          IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
              Version.LUCENE_CURRENT, ana).setOpenMode(OpenMode.CREATE));
          ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(true); 
          ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(true); 
          Iterator<String> i1 = word2Nums.keySet().iterator();
          while (i1.hasNext()) 
          {
              String g = i1.next();
              Document doc = new Document();
              int n = index(word2Nums, num2Words, g, doc);
              if (n > 0)
              {
          doc.add( new Field( F_WORD, g, Field.Store.YES, Field.Index.NOT_ANALYZED));
                  if ((++row % mod) == 0)
                  {
                      o.println("\trow=" + row + "/" + word2Nums.size() + " doc= " + doc);
                      mod *= 2;
                  }
                  writer.addDocument(doc);
              } 
          }
          o.println( "Optimizing..");
          writer.optimize();
          writer.close();
        } finally {
          dir.close();
        }
    }
    private static int index(Map<String,List<String>> word2Nums, Map<String,List<String>> num2Words, String g, Document doc)
        throws Throwable
    {
        List<String> keys = word2Nums.get(g); 
        Iterator<String> i2 = keys.iterator();
        Set<String> already = new TreeSet<String>(); 
        while (i2.hasNext()) 
        {
            already.addAll(num2Words.get(i2.next())); 
        }
        int num = 0;
        already.remove(g); 
        Iterator<String> it = already.iterator();
        while (it.hasNext())
        {
            String cur = it.next();
            if (!isDecent(cur))
            {
                continue;
            }
            num++;
			doc.add( new Field( F_SYN, cur, Field.Store.YES, Field.Index.NO));
        }
        return num;
    }
    private static void usage()
    {
        o.println("\n\n" +
            "java org.apache.lucene.wordnet.Syns2Index <prolog file> <index dir>\n\n");
    }
}
