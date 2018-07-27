package org.apache.lucene.queryParser.complexPhrase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;
public class ComplexPhraseQueryParser extends QueryParser {
  private ArrayList<ComplexPhraseQuery> complexPhrases = null;
  private boolean isPass2ResolvingPhrases;
  private ComplexPhraseQuery currentPhraseQuery = null;
  public ComplexPhraseQueryParser(Version matchVersion, String f, Analyzer a) {
    super(matchVersion, f, a);
  }
  @Override
  protected Query getFieldQuery(String field, String queryText, int slop) {
    ComplexPhraseQuery cpq = new ComplexPhraseQuery(field, queryText, slop);
    complexPhrases.add(cpq); 
    return cpq;
  }
  @Override
  public Query parse(String query) throws ParseException {
    if (isPass2ResolvingPhrases) {
      MultiTermQuery.RewriteMethod oldMethod = getMultiTermRewriteMethod();
      try {
        setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
        return super.parse(query);
      } finally {
        setMultiTermRewriteMethod(oldMethod);
      }
    }
    complexPhrases = new ArrayList<ComplexPhraseQuery>();
    Query q = super.parse(query);
    isPass2ResolvingPhrases = true;
    try {
      for (Iterator<ComplexPhraseQuery> iterator = complexPhrases.iterator(); iterator.hasNext();) {
        currentPhraseQuery = iterator.next();
        currentPhraseQuery.parsePhraseElements(this);
      }
    } finally {
      isPass2ResolvingPhrases = false;
    }
    return q;
  }
  @Override
  protected Query newTermQuery(Term term) {
    if (isPass2ResolvingPhrases) {
      try {
        checkPhraseClauseIsForSameField(term.field());
      } catch (ParseException pe) {
        throw new RuntimeException("Error parsing complex phrase", pe);
      }
    }
    return super.newTermQuery(term);
  }
  private void checkPhraseClauseIsForSameField(String field)
      throws ParseException {
    if (!field.equals(currentPhraseQuery.field)) {
      throw new ParseException("Cannot have clause for field \"" + field
          + "\" nested in phrase " + " for field \"" + currentPhraseQuery.field
          + "\"");
    }
  }
  @Override
  protected Query getWildcardQuery(String field, String termStr)
      throws ParseException {
    if (isPass2ResolvingPhrases) {
      checkPhraseClauseIsForSameField(field);
    }
    return super.getWildcardQuery(field, termStr);
  }
  @Override
  protected Query getRangeQuery(String field, String part1, String part2,
      boolean inclusive) throws ParseException {
    if (isPass2ResolvingPhrases) {
      checkPhraseClauseIsForSameField(field);
    }
    return super.getRangeQuery(field, part1, part2, inclusive);
  }
  @Override
  protected Query newRangeQuery(String field, String part1, String part2,
      boolean inclusive) {
    if (isPass2ResolvingPhrases) {
      TermRangeQuery rangeQuery = new TermRangeQuery(field, part1, part2, inclusive, inclusive,
          getRangeCollator());
      rangeQuery.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
      return rangeQuery;
    }
    return super.newRangeQuery(field, part1, part2, inclusive);
  }
  @Override
  protected Query getFuzzyQuery(String field, String termStr,
      float minSimilarity) throws ParseException {
    if (isPass2ResolvingPhrases) {
      checkPhraseClauseIsForSameField(field);
    }
    return super.getFuzzyQuery(field, termStr, minSimilarity);
  }
  static class ComplexPhraseQuery extends Query {
    String field;
    String phrasedQueryStringContents;
    int slopFactor;
    private Query contents;
    public ComplexPhraseQuery(String field, String phrasedQueryStringContents,
        int slopFactor) {
      super();
      this.field = field;
      this.phrasedQueryStringContents = phrasedQueryStringContents;
      this.slopFactor = slopFactor;
    }
    protected void parsePhraseElements(QueryParser qp) throws ParseException {
      contents = qp.parse(phrasedQueryStringContents);
    }
    @Override
    public Query rewrite(IndexReader reader) throws IOException {
      if (contents instanceof TermQuery) {
        return contents;
      }
      int numNegatives = 0;
      if (!(contents instanceof BooleanQuery)) {
        throw new IllegalArgumentException("Unknown query type \""
            + contents.getClass().getName()
            + "\" found in phrase query string \"" + phrasedQueryStringContents
            + "\"");
      }
      BooleanQuery bq = (BooleanQuery) contents;
      BooleanClause[] bclauses = bq.getClauses();
      SpanQuery[] allSpanClauses = new SpanQuery[bclauses.length];
      for (int i = 0; i < bclauses.length; i++) {
        Query qc = bclauses[i].getQuery();
        qc = qc.rewrite(reader);
        if (bclauses[i].getOccur().equals(BooleanClause.Occur.MUST_NOT)) {
          numNegatives++;
        }
        if (qc instanceof BooleanQuery) {
          ArrayList<SpanQuery> sc = new ArrayList<SpanQuery>();
          addComplexPhraseClause(sc, (BooleanQuery) qc);
          if (sc.size() > 0) {
            allSpanClauses[i] = sc.get(0);
          } else {
            allSpanClauses[i] = new SpanTermQuery(new Term(field,
                "Dummy clause because no terms found - must match nothing"));
          }
        } else {
          if (qc instanceof TermQuery) {
            TermQuery tq = (TermQuery) qc;
            allSpanClauses[i] = new SpanTermQuery(tq.getTerm());
          } else {
            throw new IllegalArgumentException("Unknown query type \""
                + qc.getClass().getName()
                + "\" found in phrase query string \""
                + phrasedQueryStringContents + "\"");
          }
        }
      }
      if (numNegatives == 0) {
        return new SpanNearQuery(allSpanClauses, slopFactor, true);
      }
      ArrayList<SpanQuery> positiveClauses = new ArrayList<SpanQuery>();
      for (int j = 0; j < allSpanClauses.length; j++) {
        if (!bclauses[j].getOccur().equals(BooleanClause.Occur.MUST_NOT)) {
          positiveClauses.add(allSpanClauses[j]);
        }
      }
      SpanQuery[] includeClauses = positiveClauses
          .toArray(new SpanQuery[positiveClauses.size()]);
      SpanQuery include = null;
      if (includeClauses.length == 1) {
        include = includeClauses[0]; 
      } else {
        include = new SpanNearQuery(includeClauses, slopFactor + numNegatives,
            true);
      }
      SpanNearQuery exclude = new SpanNearQuery(allSpanClauses, slopFactor,
          true);
      SpanNotQuery snot = new SpanNotQuery(include, exclude);
      return snot;
    }
    private void addComplexPhraseClause(List<SpanQuery> spanClauses, BooleanQuery qc) {
      ArrayList<SpanQuery> ors = new ArrayList<SpanQuery>();
      ArrayList<SpanQuery> nots = new ArrayList<SpanQuery>();
      BooleanClause[] bclauses = qc.getClauses();
      for (int i = 0; i < bclauses.length; i++) {
        Query childQuery = bclauses[i].getQuery();
        ArrayList<SpanQuery> chosenList = ors;
        if (bclauses[i].getOccur() == BooleanClause.Occur.MUST_NOT) {
          chosenList = nots;
        }
        if (childQuery instanceof TermQuery) {
          TermQuery tq = (TermQuery) childQuery;
          SpanTermQuery stq = new SpanTermQuery(tq.getTerm());
          stq.setBoost(tq.getBoost());
          chosenList.add(stq);
        } else if (childQuery instanceof BooleanQuery) {
          BooleanQuery cbq = (BooleanQuery) childQuery;
          addComplexPhraseClause(chosenList, cbq);
        } else {
          throw new IllegalArgumentException("Unknown query type:"
              + childQuery.getClass().getName());
        }
      }
      if (ors.size() == 0) {
        return;
      }
      SpanOrQuery soq = new SpanOrQuery(ors
          .toArray(new SpanQuery[ors.size()]));
      if (nots.size() == 0) {
        spanClauses.add(soq);
      } else {
        SpanOrQuery snqs = new SpanOrQuery(nots
            .toArray(new SpanQuery[nots.size()]));
        SpanNotQuery snq = new SpanNotQuery(soq, snqs);
        spanClauses.add(snq);
      }
    }
    @Override
    public String toString(String field) {
      return "\"" + phrasedQueryStringContents + "\"";
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((field == null) ? 0 : field.hashCode());
      result = prime
          * result
          + ((phrasedQueryStringContents == null) ? 0
              : phrasedQueryStringContents.hashCode());
      result = prime * result + slopFactor;
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ComplexPhraseQuery other = (ComplexPhraseQuery) obj;
      if (field == null) {
        if (other.field != null)
          return false;
      } else if (!field.equals(other.field))
        return false;
      if (phrasedQueryStringContents == null) {
        if (other.phrasedQueryStringContents != null)
          return false;
      } else if (!phrasedQueryStringContents
          .equals(other.phrasedQueryStringContents))
        return false;
      if (slopFactor != other.slopFactor)
        return false;
      return true;
    }
  }
}
