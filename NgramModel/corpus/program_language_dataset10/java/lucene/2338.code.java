package org.apache.solr.handler;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.HighFrequencyDictionary;
import org.apache.solr.util.plugin.SolrCoreAware;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Deprecated
public class SpellCheckerRequestHandler extends RequestHandlerBase implements SolrCoreAware {
  private static Logger log = LoggerFactory.getLogger(SpellCheckerRequestHandler.class);
  private SpellChecker spellChecker;
  protected Directory spellcheckerIndexDir = new RAMDirectory();
  protected String dirDescription = "(ramdir)";
  protected String termSourceField;
  protected static final String PREFIX = "sp.";
  protected static final String QUERY_PREFIX = PREFIX + "query.";
  protected static final String DICTIONARY_PREFIX = PREFIX + "dictionary.";
  protected static final String SOURCE_FIELD = DICTIONARY_PREFIX + "termSourceField";
  protected static final String INDEX_DIR = DICTIONARY_PREFIX + "indexDir";
  protected static final String THRESHOLD = DICTIONARY_PREFIX + "threshold";
  protected static final String ACCURACY = QUERY_PREFIX + "accuracy";
  protected static final String SUGGESTIONS = QUERY_PREFIX + "suggestionCount";
  protected static final String POPULAR = QUERY_PREFIX + "onlyMorePopular";
  protected static final String EXTENDED = QUERY_PREFIX + "extendedResults";
  protected static final float DEFAULT_ACCURACY = 0.5f;
  protected static final int DEFAULT_SUGGESTION_COUNT = 1;
  protected static final boolean DEFAULT_MORE_POPULAR = false;
  protected static final boolean DEFAULT_EXTENDED_RESULTS = false;
  protected static final float DEFAULT_DICTIONARY_THRESHOLD = 0.0f;
  protected SolrParams args = null;
  @Override
  public void init(NamedList args) {
    super.init(args);
    this.args = SolrParams.toSolrParams(args);
  }
  public void inform(SolrCore core) 
  {
    termSourceField = args.get(SOURCE_FIELD, args.get("termSourceField"));
    try {
      String dir = args.get(INDEX_DIR, args.get("spellcheckerIndexDir"));
      if (null != dir) {
        File f = new File(dir);
        if ( ! f.isAbsolute() ) {
          f = new File(core.getDataDir(), dir);
        }
        dirDescription = f.getAbsolutePath();
        log.info("using spell directory: " + dirDescription);
        spellcheckerIndexDir = FSDirectory.open(f);
      } else {
        log.info("using RAM based spell directory");
      }
      spellChecker = new SpellChecker(spellcheckerIndexDir);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open SpellChecker index", e);
    }
  }
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
    throws Exception {
    SolrParams p = req.getParams();
    String words = p.get("q");
    String cmd = p.get("cmd");
    if (cmd != null) {
      cmd = cmd.trim();
      if (cmd.equals("rebuild")) {
        rebuild(req);
        rsp.add("cmdExecuted","rebuild");
      } else if (cmd.equals("reopen")) {
        reopen();
        rsp.add("cmdExecuted","reopen");
      } else {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unrecognized Command: " + cmd);
      }
    }
    if (null == words || "".equals(words.trim())) {
      return;
    }
    IndexReader indexReader = null;
    String suggestionField = null;
    Float accuracy;
    int numSug;
    boolean onlyMorePopular;
    boolean extendedResults;
    try {
      accuracy = p.getFloat(ACCURACY, p.getFloat("accuracy", DEFAULT_ACCURACY));
      spellChecker.setAccuracy(accuracy);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Accuracy must be a valid positive float", e);
    }
    try {
      numSug = p.getInt(SUGGESTIONS, p.getInt("suggestionCount", DEFAULT_SUGGESTION_COUNT));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Spelling suggestion count must be a valid positive integer", e);
    }
    try {
      onlyMorePopular = p.getBool(POPULAR, DEFAULT_MORE_POPULAR);
    } catch (SolrException e) {
      throw new RuntimeException("'Only more popular' must be a valid boolean", e);
    }
    try {
      extendedResults = p.getBool(EXTENDED, DEFAULT_EXTENDED_RESULTS);
    } catch (SolrException e) {
      throw new RuntimeException("'Extended results' must be a valid boolean", e);
    }
    if (onlyMorePopular || extendedResults) {
      indexReader = req.getSearcher().getReader();
      suggestionField = termSourceField;
    }
    if (extendedResults) {
      rsp.add("numDocs", indexReader.numDocs());
      SimpleOrderedMap<Object> results = new SimpleOrderedMap<Object>();
      String[] wordz = words.split(" ");
      for (String word : wordz)
      {
        SimpleOrderedMap<Object> nl = new SimpleOrderedMap<Object>();
        nl.add("frequency", indexReader.docFreq(new Term(suggestionField, word)));
        String[] suggestions =
          spellChecker.suggestSimilar(word, numSug,
          indexReader, suggestionField, onlyMorePopular);
        NamedList<Object> sa = new NamedList<Object>();
        for (int i=0; i<suggestions.length; i++) {
          SimpleOrderedMap<Object> si = new SimpleOrderedMap<Object>();
          si.add("frequency", indexReader.docFreq(new Term(termSourceField, suggestions[i])));
          sa.add(suggestions[i], si);
        }
        nl.add("suggestions", sa);
        results.add(word, nl);
      }
      rsp.add( "result", results );
    } else {
      rsp.add("words", words);
      if (spellChecker.exist(words)) {
        rsp.add("exist","true");
      } else {
        rsp.add("exist","false");
      }
      String[] suggestions =
        spellChecker.suggestSimilar(words, numSug,
                                    indexReader, suggestionField,
                                    onlyMorePopular);
      rsp.add("suggestions", Arrays.asList(suggestions));
    }
  }
  protected Dictionary getDictionary(SolrQueryRequest req) {
    float threshold;
    try {
      threshold = req.getParams().getFloat(THRESHOLD, DEFAULT_DICTIONARY_THRESHOLD);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Threshold must be a valid positive float", e);
    }
    IndexReader indexReader = req.getSearcher().getReader();
    return new HighFrequencyDictionary(indexReader, termSourceField, threshold);
  }
  private void rebuild(SolrQueryRequest req) throws IOException, SolrException {
    if (null == termSourceField) {
      throw new SolrException
        (SolrException.ErrorCode.SERVER_ERROR, "can't rebuild spellchecker index without termSourceField configured");
    }
    Dictionary dictionary = getDictionary(req);
    spellChecker.clearIndex();
    spellChecker.indexDictionary(dictionary);
    reopen();
  }
  private void reopen() throws IOException {
    spellChecker.setSpellIndex(spellcheckerIndexDir);
  }
  public String getVersion() {
    return "$Revision: 922957 $";
  }
  public String getDescription() {
    return "The SpellChecker Solr request handler for SpellChecker index: " + dirDescription;
  }
  public String getSourceId() {
    return "$Id: SpellCheckerRequestHandler.java 922957 2010-03-14 20:58:32Z markrmiller $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/SpellCheckerRequestHandler.java $";
  }
  public URL[] getDocs() {
    return null;
  }
}
