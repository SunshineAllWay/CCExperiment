package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QueryUtils;
import org.apache.solr.search.function.BoostedQuery;
import org.apache.solr.search.function.FunctionQuery;
import org.apache.solr.search.function.ProductFloatFunction;
import org.apache.solr.search.function.QueryValueSource;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.util.SolrPluginUtils;
import org.apache.solr.analysis.*;
import java.util.*;
import java.io.Reader;
import java.io.IOException;
public class ExtendedDismaxQParserPlugin extends QParserPlugin {
  public static final String NAME = "edismax";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new ExtendedDismaxQParser(qstr, localParams, params, req);
  }
}
class ExtendedDismaxQParser extends QParser {
  private static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
  private static class U extends SolrPluginUtils {
  }
  private static interface DMP extends DisMaxParams {
  }
  public ExtendedDismaxQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  Map<String,Float> queryFields;
  Query parsedUserQuery;
  private String[] boostParams;
  private String[] multBoosts;
  private List<Query> boostQueries;
  private Query altUserQuery;
  private QParser altQParser;
  public Query parse() throws ParseException {
    SolrParams localParams = getLocalParams();
    SolrParams params = getParams();
    SolrParams solrParams = localParams == null ? params : new DefaultSolrParams(localParams, params);
    queryFields = U.parseFieldBoosts(solrParams.getParams(DMP.QF));
    if (0 == queryFields.size()) {
      queryFields.put(req.getSchema().getDefaultSearchFieldName(), 1.0f);
    }
    Map<String,Float> phraseFields = 
      U.parseFieldBoosts(solrParams.getParams(DMP.PF));
    Map<String,Float> phraseFields2 = 
      U.parseFieldBoosts(solrParams.getParams("pf2"));
    Map<String,Float> phraseFields3 = 
      U.parseFieldBoosts(solrParams.getParams("pf3"));
    float tiebreaker = solrParams.getFloat(DMP.TIE, 0.0f);
    int pslop = solrParams.getInt(DMP.PS, 0);
    int qslop = solrParams.getInt(DMP.QS, 0);
    boolean stopwords = solrParams.getBool("stopwords", true);
    BooleanQuery query = new BooleanQuery(true);
    parsedUserQuery = null;
    String userQuery = getString();
    altUserQuery = null;
    if( userQuery == null || userQuery.length() < 1 ) {
      String altQ = solrParams.get( DMP.ALTQ );
      if (altQ != null) {
        altQParser = subQuery(altQ, null);
        altUserQuery = altQParser.getQuery();
        query.add( altUserQuery , BooleanClause.Occur.MUST );
      } else {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "missing query string" );
      }
    }
    else {     
      boolean lowercaseOperators = solrParams.getBool("lowercaseOperators", true);
      String mainUserQuery = userQuery;
      ExtendedSolrQueryParser up =
        new ExtendedSolrQueryParser(this, IMPOSSIBLE_FIELD_NAME);
      up.addAlias(IMPOSSIBLE_FIELD_NAME,
                tiebreaker, queryFields);
      up.setPhraseSlop(qslop);     
      up.setAllowLeadingWildcard(true);
      List<Clause> clauses = null;
      boolean specialSyntax = false;
      int numPluses = 0;
      int numMinuses = 0;
      int numOptional = 0;
      int numAND = 0;
      int numOR = 0;
      int numNOT = 0;
      boolean sawLowerAnd=false;
      boolean sawLowerOr=false;
      clauses = splitIntoClauses(userQuery, false);
      for (Clause clause : clauses) {
        if (!clause.isPhrase && clause.hasSpecialSyntax) {
          specialSyntax = true;
        }
        if (clause.must == '+') numPluses++;
        if (clause.must == '-') numMinuses++;
        if (clause.isBareWord()) {
          String s = clause.val;
          if ("AND".equals(s)) {
            numAND++;
          } else if ("OR".equals(s)) {
            numOR++;
          } else if ("NOT".equals(s)) {
            numNOT++;
          } else if (lowercaseOperators) {
            if ("and".equals(s)) {
              numAND++;
              sawLowerAnd=true;
            } else if ("or".equals(s)) {
              numOR++;
              sawLowerOr=true;
            }
          }
        }
      }
      numOptional = clauses.size() - (numPluses + numMinuses);
      if (sawLowerAnd || sawLowerOr) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<clauses.size(); i++) {
          Clause clause = clauses.get(i);
          String s = clause.raw;
          if (i>0 && i+1<clauses.size()) {
            if ("AND".equalsIgnoreCase(s)) {
              s="AND";
            } else if ("OR".equalsIgnoreCase(s)) {
              s="OR";
            }
          }
          sb.append(s);
          sb.append(' ');
        }
        mainUserQuery = sb.toString();
      }
      boolean doMinMatched = (numOR + numNOT + numPluses + numMinuses) == 0;
      try {
        up.setRemoveStopFilter(!stopwords);
        parsedUserQuery = up.parse(mainUserQuery);
        if (stopwords && isEmpty(parsedUserQuery)) {
          up.setRemoveStopFilter(true);
          parsedUserQuery = up.parse(mainUserQuery);          
        }
      } catch (Exception e) {
      }
      if (parsedUserQuery != null && doMinMatched) {
        String minShouldMatch = solrParams.get(DMP.MM, "100%");
        if (parsedUserQuery instanceof BooleanQuery) {
          U.setMinShouldMatch((BooleanQuery)parsedUserQuery, minShouldMatch);
        }
      }
      if (parsedUserQuery == null) {
        StringBuilder sb = new StringBuilder();
        for (Clause clause : clauses) {
          boolean doQuote = clause.isPhrase;
          String s=clause.val;
          if (!clause.isPhrase && ("OR".equals(s) || "AND".equals(s) || "NOT".equals(s))) {
            doQuote=true;
          }
          if (clause.must != 0) {
            sb.append(clause.must);
          }
          if (clause.field != null) {
            sb.append(clause.field);
            sb.append(':');
          }
          if (doQuote) {
            sb.append('"');
          }
          sb.append(clause.val);
          if (doQuote) {
            sb.append('"');
          }
          sb.append(' ');
        }
        String escapedUserQuery = sb.toString();
        parsedUserQuery = up.parse(escapedUserQuery);
        String minShouldMatch = solrParams.get(DMP.MM, "100%");
        if (parsedUserQuery instanceof BooleanQuery) {
          BooleanQuery t = new BooleanQuery();
          U.flattenBooleanQuery(t, (BooleanQuery)parsedUserQuery);
          U.setMinShouldMatch(t, minShouldMatch);
          parsedUserQuery = t;
        }
      }
      query.add(parsedUserQuery, BooleanClause.Occur.MUST);
      if (phraseFields.size() > 0 || 
          phraseFields2.size() > 0 ||
          phraseFields3.size() > 0) {
        List<Clause> normalClauses = new ArrayList<Clause>(clauses.size());
        for (Clause clause : clauses) {
          if (clause.field != null || clause.isPhrase) continue;
          if (clause.isBareWord()) {
            String s = clause.val.toString();
            if ("OR".equals(s) || "AND".equals(s) || "NOT".equals(s) || "TO".equals(s)) continue;
          }
          normalClauses.add(clause);
        }
        addShingledPhraseQueries(query, normalClauses, phraseFields, 0, 
                                 tiebreaker, pslop);
        addShingledPhraseQueries(query, normalClauses, phraseFields2, 2,  
                                 tiebreaker, pslop);
        addShingledPhraseQueries(query, normalClauses, phraseFields3, 3,
                                 tiebreaker, pslop);
      }
    }
    boostParams = solrParams.getParams(DMP.BQ);
    boostQueries=null;
    if (boostParams!=null && boostParams.length>0) {
      boostQueries = new ArrayList<Query>();
      for (String qs : boostParams) {
        if (qs.trim().length()==0) continue;
        Query q = subQuery(qs, null).getQuery();
        boostQueries.add(q);
      }
    }
    if (null != boostQueries) {
      for(Query f : boostQueries) {
        query.add(f, BooleanClause.Occur.SHOULD);
      }
    }
    String[] boostFuncs = solrParams.getParams(DMP.BF);
    if (null != boostFuncs && 0 != boostFuncs.length) {
      for (String boostFunc : boostFuncs) {
        if(null == boostFunc || "".equals(boostFunc)) continue;
        Map<String,Float> ff = SolrPluginUtils.parseFieldBoosts(boostFunc);
        for (String f : ff.keySet()) {
          Query fq = subQuery(f, FunctionQParserPlugin.NAME).getQuery();
          Float b = ff.get(f);
          if (null != b) {
            fq.setBoost(b);
          }
          query.add(fq, BooleanClause.Occur.SHOULD);
        }
      }
    }
    Query topQuery = query;
    multBoosts = solrParams.getParams("boost");
    if (multBoosts!=null && multBoosts.length>0) {
      List<ValueSource> boosts = new ArrayList<ValueSource>();
      for (String boostStr : multBoosts) {
        if (boostStr==null || boostStr.length()==0) continue;
        Query boost = subQuery(boostStr, FunctionQParserPlugin.NAME).getQuery();
        ValueSource vs;
        if (boost instanceof FunctionQuery) {
          vs = ((FunctionQuery)boost).getValueSource();
        } else {
          vs = new QueryValueSource(boost, 1.0f);
        }
        boosts.add(vs);
      }
      if (boosts.size()>1) {
        ValueSource prod = new ProductFloatFunction(boosts.toArray(new ValueSource[boosts.size()]));
        topQuery = new BoostedQuery(query, prod);
      } else if (boosts.size() == 1) {
        topQuery = new BoostedQuery(query, boosts.get(0));
      }
    }
    return topQuery;
  }
  private void addShingledPhraseQueries(final BooleanQuery mainQuery, 
                                        final List<Clause> clauses,
                                        final Map<String,Float> fields,
                                        int shingleSize,
                                        final float tiebreaker,
                                        final int slop) 
    throws ParseException {
    if (null == fields || fields.isEmpty() || 
        null == clauses || clauses.size() <= shingleSize ) 
      return;
    if (0 == shingleSize) shingleSize = clauses.size();
    final int goat = shingleSize-1; 
    StringBuilder userPhraseQuery = new StringBuilder();
      for (int i=0; i < clauses.size() - goat; i++) {
        userPhraseQuery.append('"');
        for (int j=0; j <= goat; j++) {
          userPhraseQuery.append(clauses.get(i + j).val);
          userPhraseQuery.append(' ');
        }
        userPhraseQuery.append('"');
        userPhraseQuery.append(' ');
      }
      ExtendedSolrQueryParser pp =
        new ExtendedSolrQueryParser(this, IMPOSSIBLE_FIELD_NAME);
      pp.addAlias(IMPOSSIBLE_FIELD_NAME, tiebreaker, fields);
      pp.setPhraseSlop(slop);
      pp.setRemoveStopFilter(true);  
      pp.makeDismax = true; 
      pp.minClauseSize = 2;  
      Query phrase = pp.parse(userPhraseQuery.toString());
      if (phrase != null) {
        mainQuery.add(phrase, BooleanClause.Occur.SHOULD);
      }
  }
  @Override
  public String[] getDefaultHighlightFields() {
    String[] highFields = queryFields.keySet().toArray(new String[0]);
    return highFields;
  }
  @Override
  public Query getHighlightQuery() throws ParseException {
    return parsedUserQuery;
  }
  public void addDebugInfo(NamedList<Object> debugInfo) {
    super.addDebugInfo(debugInfo);
    debugInfo.add("altquerystring", altUserQuery);
    if (null != boostQueries) {
      debugInfo.add("boost_queries", boostParams);
      debugInfo.add("parsed_boost_queries",
                QueryParsing.toString(boostQueries, getReq().getSchema()));
    }
    debugInfo.add("boostfuncs", getReq().getParams().getParams(DisMaxParams.BF));
  }
  public static CharSequence partialEscape(CharSequence s) {
    StringBuilder sb = new StringBuilder();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == ':') {
        if (i+1<len && i>0) {
          char ch = s.charAt(i+1);
          if (!(Character.isWhitespace(ch) || ch=='+' || ch=='-' || ch==':')) {
            int start, pos;
            for (start=i-1; start>=0; start--) {
              ch = s.charAt(start);
              if (Character.isWhitespace(ch)) break;
            }
            pos = start+1;
            ch = s.charAt(pos);
            if (ch=='+' || ch=='-') {
              pos++;
            }
              ch = s.charAt(pos++);
              if (Character.isJavaIdentifierPart(ch)) {
                for(;;) {
                  ch = s.charAt(pos++);
                  if (!(Character.isJavaIdentifierPart(ch) || ch=='-' || ch=='.')) {
                    break;
                  }
                }
                if (pos<=i) {
                  sb.append(':');
                  continue;  
                }
              }
          }
        }
        sb.append('\\');
      }
      else if (c == '\\' || c == '!' || c == '(' || c == ')' ||
          c == '^' || c == '[' || c == ']' ||
          c == '{'  || c == '}' || c == '~' || c == '*' || c == '?'
          )
      {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb;
  }
  static class Clause {
    boolean isBareWord() {
      return must==0 && !isPhrase;
    }
    String field;
    boolean isPhrase;
    boolean hasWhitespace;
    boolean hasSpecialSyntax;
    boolean syntaxError;
    char must;   
    String val;  
    String raw;  
  }
  public List<Clause> splitIntoClauses(String s, boolean ignoreQuote) {
    ArrayList<Clause> lst = new ArrayList<Clause>(4);
    Clause clause = new Clause();
    int pos=0;
    int end=s.length();
    char ch=0;
    int start;
    outer: while (pos < end) {
      ch = s.charAt(pos);
      while (Character.isWhitespace(ch)) {
        if (++pos >= end) break;
        ch = s.charAt(pos);
      }
      start = pos;      
      if (ch=='+' || ch=='-') {
        clause.must = ch;
        pos++;
      }
      clause.field = getFieldName(s, pos, end);
      if (clause.field != null) {
        pos += clause.field.length(); 
        pos++;  
      }
      if (pos>=end) break;
      char inString=0;
      ch = s.charAt(pos);
      if (!ignoreQuote && ch=='"') {
        clause.isPhrase = true;
        inString = '"';
        pos++;
      }
      StringBuilder sb = new StringBuilder();
      while (pos < end) {
        ch = s.charAt(pos++);
        if (ch=='\\') {    
          sb.append(ch);
          if (pos >= end) {
            sb.append(ch); 
            break;
          }
          ch = s.charAt(pos++);
          sb.append(ch);
          continue;
        } else if (inString != 0 && ch == inString) {
          inString=0;
          break;
        } else if (Character.isWhitespace(ch)) {
          clause.hasWhitespace=true;
          if (inString == 0) {
            pos--;
            break;
          }
        }
        if (inString == 0) {
          switch (ch) {
            case '!':
            case '(':
            case ')':
            case ':':
            case '^':
            case '[':
            case ']':
            case '{':
            case '}':
            case '~':
            case '*':
            case '?':
            case '"':
            case '+':
            case '-':
              clause.hasSpecialSyntax = true;
              sb.append('\\');
          }
        } else if (ch=='"') {
          sb.append('\\');
        }
        sb.append(ch);
      }
      clause.val = sb.toString();
      if (clause.isPhrase) {
        if (inString != 0) {
          return splitIntoClauses(s, true);
        }
        clause.hasSpecialSyntax = false;        
      } else {
        if (clause.val.length() == 0) {
          clause.syntaxError = true;
          if (clause.must != 0) {
            clause.val="\\"+clause.must;
            clause.must = 0;
            clause.hasSpecialSyntax = true;
          } else {
            clause=null;
          }
        }
      }
      if (clause != null) {
        clause.raw = s.substring(start, pos);
        lst.add(clause);
      }
      clause = new Clause();
    }
    return lst;
  }
  public String getFieldName(String s, int pos, int end) {
    if (pos >= end) return null;
    int p=pos;
    int colon = s.indexOf(':',pos);
    if (colon<=pos || colon+1>=end || Character.isWhitespace(s.charAt(colon+1))) return null;
    char ch = s.charAt(p++);
    if (!Character.isJavaIdentifierPart(ch)) return null;
    while (p<colon) {
      ch = s.charAt(p++);
      if (!(Character.isJavaIdentifierPart(ch) || ch=='-' || ch=='.')) return null;
    }
    String fname = s.substring(pos, p);
    return getReq().getSchema().getFieldTypeNoEx(fname) == null ? null : fname;
  }
  public static List<String> split(String s, boolean ignoreQuote) {
    ArrayList<String> lst = new ArrayList<String>(4);
    int pos=0, start=0, end=s.length();
    char inString=0;
    char ch=0;
    while (pos < end) {
      char prevChar=ch;
      ch = s.charAt(pos++);
      if (ch=='\\') {    
        pos++;
      } else if (inString != 0 && ch==inString) {
        inString=0;
      } else if (!ignoreQuote && ch=='"') {
        if (!Character.isLetterOrDigit(prevChar)) {
          inString=ch;
        }
      } else if (Character.isWhitespace(ch) && inString==0) {
        lst.add(s.substring(start,pos-1));
        start=pos;
      }
    }
    if (start < end) {
      lst.add(s.substring(start,end));
    }
    if (inString != 0) {
      return split(s, true);
    }
    return lst;
  }
    enum QType {
      FIELD,
      PHRASE,
      PREFIX,
      WILDCARD,
      FUZZY,
      RANGE
    }
  class ExtendedSolrQueryParser extends SolrQueryParser {
    protected class Alias {
      public float tie;
      public Map<String,Float> fields;
    }
    boolean makeDismax=true;
    boolean disableCoord=true;
    boolean allowWildcard=true;
    int minClauseSize = 0;    
    ExtendedAnalyzer analyzer;
    protected Map<String,Alias> aliases = new HashMap<String,Alias>(3);
    public ExtendedSolrQueryParser(QParser parser, String defaultField) {
      super(parser, defaultField, new ExtendedAnalyzer(parser));
      analyzer = (ExtendedAnalyzer)getAnalyzer();      
      setDefaultOperator(QueryParser.Operator.OR);
    }
    public void setRemoveStopFilter(boolean remove) {
      analyzer.removeStopFilter = remove;
    }
    protected Query getBooleanQuery(List clauses, boolean disableCoord) throws ParseException {
      Query q = super.getBooleanQuery(clauses, disableCoord);
      if (q != null) {
        q = QueryUtils.makeQueryable(q);
      }
      return q;
    }
    protected void addClause(List clauses, int conj, int mods, Query q) {
      super.addClause(clauses, conj, mods, q);
    }
    public void addAlias(String field, float tiebreaker,
                         Map<String,Float> fieldBoosts) {
      Alias a = new Alias();
      a.tie = tiebreaker;
      a.fields = fieldBoosts;
      aliases.put(field, a);
    }
    QType type;
    String field;
    String val;
    String val2;
    boolean bool;
    float flt;
    int slop;
    @Override
    protected Query getFieldQuery(String field, String val) throws ParseException {
      this.type = QType.FIELD;
      this.field = field;
      this.val = val;
      this.slop = getPhraseSlop(); 
      return getAliasedQuery();
    }
    @Override
    protected Query getFieldQuery(String field, String val, int slop) throws ParseException {
      this.type = QType.PHRASE;
      this.field = field;
      this.val = val;
      this.slop = slop;
      return getAliasedQuery();
    }
    @Override
    protected Query getPrefixQuery(String field, String val) throws ParseException {
      if (val.equals("") && field.equals("*")) {
        return new MatchAllDocsQuery();
      }
      this.type = QType.PREFIX;
      this.field = field;
      this.val = val;
      return getAliasedQuery();
    }
    @Override
    protected Query getRangeQuery(String field, String a, String b, boolean inclusive) throws ParseException {
      this.type = QType.RANGE;
      this.field = field;
      this.val = a;
      this.val2 = b;
      this.bool = inclusive;
      return getAliasedQuery();
    }
    @Override
    protected Query getWildcardQuery(String field, String val) throws ParseException {
      if (val.equals("*")) {
        if (field.equals("*")) {
          return new MatchAllDocsQuery();
        } else{
          return getPrefixQuery(field,"");
        }
      }
      this.type = QType.WILDCARD;
      this.field = field;
      this.val = val;
      return getAliasedQuery();
    }
    @Override
    protected Query getFuzzyQuery(String field, String val, float minSimilarity) throws ParseException {
      this.type = QType.FUZZY;
      this.field = field;
      this.val = val;
      this.flt = minSimilarity;
      return getAliasedQuery();
    }
    protected Query getAliasedQuery()
      throws ParseException {
      Alias a = aliases.get(field);
      if (a != null) {
        List<Query> lst = getQueries(a);
        if (lst == null || lst.size()==0)
            return getQuery();
        if (makeDismax) {
          DisjunctionMaxQuery q = new DisjunctionMaxQuery(lst, a.tie);
          return q;
        } else {
          BooleanQuery q = new BooleanQuery(disableCoord);
          for (Query sub : lst) {
            q.add(sub, BooleanClause.Occur.SHOULD);
          }
          return q;
        }
      } else {
        return getQuery();
      }
    }
     protected List<Query> getQueries(Alias a) throws ParseException {
       if (a == null) return null;
       if (a.fields.size()==0) return null;
       List<Query> lst= new ArrayList<Query>(4);
       for (String f : a.fields.keySet()) {
         this.field = f;
         Query sub = getQuery();
         if (sub != null) {
           Float boost = a.fields.get(f);
           if (boost != null) {
              sub.setBoost(boost);
           }
           lst.add(sub);
         }
       }
       return lst;
     }
    private Query getQuery() throws ParseException {
      try {
        switch (type) {
          case FIELD:  
          case PHRASE:
            Query query = super.getFieldQuery(field, val);
            if (query instanceof PhraseQuery) {
              PhraseQuery pq = (PhraseQuery)query;
              if (minClauseSize > 1 && pq.getTerms().length < minClauseSize) return null;
              ((PhraseQuery)query).setSlop(slop);
            } else if (query instanceof MultiPhraseQuery) {
              MultiPhraseQuery pq = (MultiPhraseQuery)query;
              if (minClauseSize > 1 && pq.getTermArrays().size() < minClauseSize) return null;
              ((MultiPhraseQuery)query).setSlop(slop);
            } else if (minClauseSize > 1) {
              return null;
            }
            return query;
          case PREFIX: return super.getPrefixQuery(field, val);
          case WILDCARD: return super.getWildcardQuery(field, val);
          case FUZZY: return super.getFuzzyQuery(field, val, flt);
          case RANGE: return super.getRangeQuery(field, val, val2, bool);
        }
        return null;
      } catch (Exception e) {
        return null;
      }
    }
  }
  static boolean isEmpty(Query q) {
    if (q==null) return true;
    if (q instanceof BooleanQuery && ((BooleanQuery)q).clauses().size()==0) return true;
    return false;
  }
}
class ExtendedAnalyzer extends Analyzer {
  final Map<String, Analyzer> map = new HashMap<String, Analyzer>();
  final QParser parser;
  final Analyzer queryAnalyzer;
  public boolean removeStopFilter = false;
  public static TokenizerChain getQueryTokenizerChain(QParser parser, String fieldName) {
    FieldType ft = parser.getReq().getSchema().getFieldType(fieldName);
    Analyzer qa = ft.getQueryAnalyzer();
    return qa instanceof TokenizerChain ? (TokenizerChain)qa : null;
  }
  public static StopFilterFactory getQueryStopFilter(QParser parser, String fieldName) {
    TokenizerChain tcq = getQueryTokenizerChain(parser, fieldName);
    if (tcq == null) return null;
    TokenFilterFactory[] facs = tcq.getTokenFilterFactories();
    for (int i=0; i<facs.length; i++) {
      TokenFilterFactory tf = facs[i];
      if (tf instanceof StopFilterFactory) {
        return (StopFilterFactory)tf;
      }
    }
    return null;
  }
  public ExtendedAnalyzer(QParser parser) {
    this.parser = parser;
    this.queryAnalyzer = parser.getReq().getSchema().getQueryAnalyzer();
  }
  public TokenStream tokenStream(String fieldName, Reader reader) {
    if (!removeStopFilter) {
      return queryAnalyzer.tokenStream(fieldName, reader);
    }
    Analyzer a = map.get(fieldName);
    if (a != null) {
      return a.tokenStream(fieldName, reader);
    }
    FieldType ft = parser.getReq().getSchema().getFieldType(fieldName);
    Analyzer qa = ft.getQueryAnalyzer();
    if (!(qa instanceof TokenizerChain)) {
      map.put(fieldName, qa);
      return qa.tokenStream(fieldName, reader);
    }
    TokenizerChain tcq = (TokenizerChain)qa;
    Analyzer ia = ft.getAnalyzer();
    if (ia == qa || !(ia instanceof TokenizerChain)) {
      map.put(fieldName, qa);
      return qa.tokenStream(fieldName, reader);
    }
    TokenizerChain tci = (TokenizerChain)ia;
    for (TokenFilterFactory tf : tci.getTokenFilterFactories()) {
      if (tf instanceof StopFilterFactory) {
        map.put(fieldName, qa);
        return qa.tokenStream(fieldName, reader);
      }
    }
    int stopIdx = -1;
    TokenFilterFactory[] facs = tcq.getTokenFilterFactories();
    for (int i=0; i<facs.length; i++) {
      TokenFilterFactory tf = facs[i];
      if (tf instanceof StopFilterFactory) {
        stopIdx = i;
        break;
      }
    }
    if (stopIdx == -1) {
      map.put(fieldName, qa);
      return qa.tokenStream(fieldName, reader);
    }
    TokenFilterFactory[] newtf = new TokenFilterFactory[facs.length-1];
    for (int i=0,j=0; i<facs.length; i++) {
      if (i==stopIdx) continue;
      newtf[j++] = facs[i];
    }
    TokenizerChain newa = new TokenizerChain(tcq.getTokenizerFactory(), newtf);
    newa.setPositionIncrementGap(tcq.getPositionIncrementGap(fieldName));
    map.put(fieldName, newa);
    return newa.tokenStream(fieldName, reader);        
  }
  public int getPositionIncrementGap(String fieldName) {
    return queryAnalyzer.getPositionIncrementGap(fieldName);
  }
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    if (!removeStopFilter) {
      return queryAnalyzer.reusableTokenStream(fieldName, reader);
    }
    return tokenStream(fieldName, reader);
  }
}
