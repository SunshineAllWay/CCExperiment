package org.apache.solr.spelling;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.util.HighFrequencyDictionary;
import org.apache.solr.search.SolrIndexSearcher;
public class FileBasedSpellChecker extends AbstractLuceneSpellChecker {
  private static final Logger log = LoggerFactory.getLogger(FileBasedSpellChecker.class);
  public static final String SOURCE_FILE_CHAR_ENCODING = "characterEncoding";
  private String characterEncoding;
  public static final String WORD_FIELD_NAME = "word";
  public String init(NamedList config, SolrCore core) {
    super.init(config, core);
    characterEncoding = (String) config.get(SOURCE_FILE_CHAR_ENCODING);
    return name;
  }
  public void build(SolrCore core, SolrIndexSearcher searcher) {
    try {
      loadExternalFileDictionary(core.getSchema(), core.getResourceLoader());
      spellChecker.clearIndex();
      spellChecker.indexDictionary(dictionary);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  protected IndexReader determineReader(IndexReader reader) {
    return null;
  }
  @SuppressWarnings("unchecked")
  private void loadExternalFileDictionary(IndexSchema schema, SolrResourceLoader loader) {
    try {
      if (fieldTypeName != null
              && schema.getFieldTypeNoEx(fieldTypeName) != null) {
        FieldType fieldType = schema.getFieldTypes()
                .get(fieldTypeName);
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, fieldType.getAnalyzer(),
                true, IndexWriter.MaxFieldLength.UNLIMITED);
        writer.setMergeFactor(300);
        writer.setMaxBufferedDocs(150);
        List<String> lines = loader.getLines(sourceLocation, characterEncoding);
        for (String s : lines) {
          Document d = new Document();
          d.add(new Field(WORD_FIELD_NAME, s, Field.Store.NO, Field.Index.ANALYZED));
          writer.addDocument(d);
        }
        writer.optimize();
        writer.close();
        dictionary = new HighFrequencyDictionary(IndexReader.open(ramDir),
                WORD_FIELD_NAME, 0.0f);
      } else {
        if (characterEncoding == null) {
          dictionary = new PlainTextDictionary(loader.openResource(sourceLocation));
        } else {
          dictionary = new PlainTextDictionary(new InputStreamReader(loader.openResource(sourceLocation), characterEncoding));
        }
      }
    } catch (IOException e) {
      log.error( "Unable to load spellings", e);
    }
  }
  public String getCharacterEncoding() {
    return characterEncoding;
  }
}
