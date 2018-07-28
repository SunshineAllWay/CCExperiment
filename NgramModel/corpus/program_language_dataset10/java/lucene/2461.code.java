package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;
public class TextField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
    properties |= TOKENIZED;
    if (schema.getVersion()> 1.1f) properties &= ~OMIT_TF_POSITIONS;
    super.init(schema, args);    
  }
  public SortField getSortField(SchemaField field, boolean reverse) {
    return getStringSort(field, reverse);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeStr(name, f.stringValue());
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeStr(name, f.stringValue(), true);
  }
  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    return parseFieldQuery(parser, getQueryAnalyzer(), field.getName(), externalVal);
  }
  static Query parseFieldQuery(QParser parser, Analyzer analyzer, String field, String queryText) {
    int phraseSlop = 0;
    boolean enablePositionIncrements = true;
    TokenStream source;
    try {
      source = analyzer.reusableTokenStream(field, new StringReader(queryText));
      source.reset();
    } catch (IOException e) {
      source = analyzer.tokenStream(field, new StringReader(queryText));
    }
    CachingTokenFilter buffer = new CachingTokenFilter(source);
    TermAttribute termAtt = null;
    PositionIncrementAttribute posIncrAtt = null;
    int numTokens = 0;
    boolean success = false;
    try {
      buffer.reset();
      success = true;
    } catch (IOException e) {
    }
    if (success) {
      if (buffer.hasAttribute(TermAttribute.class)) {
        termAtt = (TermAttribute) buffer.getAttribute(TermAttribute.class);
      }
      if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
        posIncrAtt = (PositionIncrementAttribute) buffer.getAttribute(PositionIncrementAttribute.class);
      }
    }
    int positionCount = 0;
    boolean severalTokensAtSamePosition = false;
    boolean hasMoreTokens = false;
    if (termAtt != null) {
      try {
        hasMoreTokens = buffer.incrementToken();
        while (hasMoreTokens) {
          numTokens++;
          int positionIncrement = (posIncrAtt != null) ? posIncrAtt.getPositionIncrement() : 1;
          if (positionIncrement != 0) {
            positionCount += positionIncrement;
          } else {
            severalTokensAtSamePosition = true;
          }
          hasMoreTokens = buffer.incrementToken();
        }
      } catch (IOException e) {
      }
    }
    try {
      buffer.reset();
      source.close();
    }
    catch (IOException e) {
    }
    if (numTokens == 0)
      return null;
    else if (numTokens == 1) {
      String term = null;
      try {
        boolean hasNext = buffer.incrementToken();
        assert hasNext == true;
        term = termAtt.term();
      } catch (IOException e) {
      }
      return new TermQuery(new Term(field, term));
    } else {
      if (severalTokensAtSamePosition) {
        if (positionCount == 1) {
          BooleanQuery q = new BooleanQuery(true);
          for (int i = 0; i < numTokens; i++) {
            String term = null;
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.term();
            } catch (IOException e) {
            }
            Query currentQuery = new TermQuery(new Term(field, term));
            q.add(currentQuery, BooleanClause.Occur.SHOULD);
          }
          return q;
        }
        else {
          MultiPhraseQuery mpq = new MultiPhraseQuery();
          mpq.setSlop(phraseSlop);
          List multiTerms = new ArrayList();
          int position = -1;
          for (int i = 0; i < numTokens; i++) {
            String term = null;
            int positionIncrement = 1;
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.term();
              if (posIncrAtt != null) {
                positionIncrement = posIncrAtt.getPositionIncrement();
              }
            } catch (IOException e) {
            }
            if (positionIncrement > 0 && multiTerms.size() > 0) {
              if (enablePositionIncrements) {
                mpq.add((Term[])multiTerms.toArray(new Term[0]),position);
              } else {
                mpq.add((Term[])multiTerms.toArray(new Term[0]));
              }
              multiTerms.clear();
            }
            position += positionIncrement;
            multiTerms.add(new Term(field, term));
          }
          if (enablePositionIncrements) {
            mpq.add((Term[])multiTerms.toArray(new Term[0]),position);
          } else {
            mpq.add((Term[])multiTerms.toArray(new Term[0]));
          }
          return mpq;
        }
      }
      else {
        PhraseQuery pq = new PhraseQuery();
        pq.setSlop(phraseSlop);
        int position = -1;
        for (int i = 0; i < numTokens; i++) {
          String term = null;
          int positionIncrement = 1;
          try {
            boolean hasNext = buffer.incrementToken();
            assert hasNext == true;
            term = termAtt.term();
            if (posIncrAtt != null) {
              positionIncrement = posIncrAtt.getPositionIncrement();
            }
          } catch (IOException e) {
          }
          if (enablePositionIncrements) {
            position += positionIncrement;
            pq.add(new Term(field, term),position);
          } else {
            pq.add(new Term(field, term));
          }
        }
        return pq;
      }
    }
  }
}
