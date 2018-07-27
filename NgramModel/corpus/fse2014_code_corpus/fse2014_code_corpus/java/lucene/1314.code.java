package org.apache.lucene.queryParser.surround.parser;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.queryParser.surround.query.SrndQuery;
import org.apache.lucene.queryParser.surround.query.FieldsQuery;
import org.apache.lucene.queryParser.surround.query.OrQuery;
import org.apache.lucene.queryParser.surround.query.AndQuery;
import org.apache.lucene.queryParser.surround.query.NotQuery;
import org.apache.lucene.queryParser.surround.query.DistanceQuery;
import org.apache.lucene.queryParser.surround.query.SrndTermQuery;
import org.apache.lucene.queryParser.surround.query.SrndPrefixQuery;
import org.apache.lucene.queryParser.surround.query.SrndTruncQuery;
public class QueryParser implements QueryParserConstants {
  final int minimumPrefixLength = 3;
  final int minimumCharsInTrunc = 3;
  final String truncationErrorMessage = "Too unrestrictive truncation: ";
  final String boostErrorMessage = "Cannot handle boost value: ";
  final char truncator = '*';
  final char anyChar = '?';
  final char quote = '\"';
  final char fieldOperator = ':';
  final char comma = ','; 
  final char carat = '^'; 
  static public SrndQuery parse(String query) throws ParseException {
    QueryParser parser = new QueryParser();
    return parser.parse2(query);
  }
  public QueryParser() {
    this(new FastCharStream(new StringReader("")));
  }
  public SrndQuery parse2(String query) throws ParseException {
    ReInit(new FastCharStream(new StringReader(query)));
    try {
      return TopSrndQuery();
    } catch (TokenMgrError tme) {
      throw new ParseException(tme.getMessage());
    }
  }
  protected SrndQuery getFieldsQuery(
      SrndQuery q, ArrayList fieldNames) {
    return new FieldsQuery(q, fieldNames, fieldOperator);
  }
  protected SrndQuery getOrQuery(List queries, boolean infix, Token orToken) {
    return new OrQuery(queries, infix, orToken.image);
  }
  protected SrndQuery getAndQuery(List queries, boolean infix, Token andToken) {
    return new AndQuery( queries, infix, andToken.image);
  }
  protected SrndQuery getNotQuery(List queries, Token notToken) {
    return new NotQuery( queries, notToken.image);
  }
  protected static int getOpDistance(String distanceOp) {
    return distanceOp.length() == 1
      ? 1
      : Integer.parseInt( distanceOp.substring( 0, distanceOp.length() - 1));
  }
  protected static void checkDistanceSubQueries(DistanceQuery distq, String opName)
  throws ParseException {
    String m = distq.distanceSubQueryNotAllowed();
    if (m != null) {
      throw new ParseException("Operator " + opName + ": " + m);
    }
  }
  protected SrndQuery getDistanceQuery(
        List queries,
        boolean infix,
        Token dToken,
        boolean ordered) throws ParseException {
    DistanceQuery dq = new DistanceQuery(queries,
                                        infix,
                                        getOpDistance(dToken.image),
                                        dToken.image,
                                        ordered);
    checkDistanceSubQueries(dq, dToken.image);
    return dq;
  }
  protected SrndQuery getTermQuery(
        String term, boolean quoted) {
    return new SrndTermQuery(term, quoted);
  }
  protected boolean allowedSuffix(String suffixed) {
    return (suffixed.length() - 1) >= minimumPrefixLength;
  }
  protected SrndQuery getPrefixQuery(
      String prefix, boolean quoted) {
    return new SrndPrefixQuery(prefix, quoted, truncator);
  }
  protected boolean allowedTruncation(String truncated) {
    int nrNormalChars = 0;
    for (int i = 0; i < truncated.length(); i++) {
      char c = truncated.charAt(i);
      if ((c != truncator) && (c != anyChar)) {
        nrNormalChars++;
      }
    }
    return nrNormalChars >= minimumCharsInTrunc;
  }
  protected SrndQuery getTruncQuery(String truncated) {
    return new SrndTruncQuery(truncated, truncator, anyChar);
  }
  final public SrndQuery TopSrndQuery() throws ParseException {
  SrndQuery q;
    q = FieldsQuery();
    jj_consume_token(0);
   {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery FieldsQuery() throws ParseException {
  SrndQuery q;
  ArrayList fieldNames;
    fieldNames = OptionalFields();
    q = OrQuery();
   {if (true) return (fieldNames == null) ? q : getFieldsQuery(q, fieldNames);}
    throw new Error("Missing return statement in function");
  }
  final public ArrayList OptionalFields() throws ParseException {
  Token fieldName;
  ArrayList fieldNames = null;
    label_1:
    while (true) {
      if (jj_2_1(2)) {
        ;
      } else {
        break label_1;
      }
          fieldName = jj_consume_token(TERM);
      jj_consume_token(COLON);
      if (fieldNames == null) {
        fieldNames = new ArrayList();
      }
      fieldNames.add(fieldName.image);
    }
   {if (true) return fieldNames;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery OrQuery() throws ParseException {
  SrndQuery q;
  ArrayList queries = null;
  Token oprt = null;
    q = AndQuery();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_2;
      }
      oprt = jj_consume_token(OR);
      if (queries == null) {
        queries = new ArrayList();
        queries.add(q);
      }
      q = AndQuery();
      queries.add(q);
    }
   {if (true) return (queries == null) ? q : getOrQuery(queries, true , oprt);}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery AndQuery() throws ParseException {
  SrndQuery q;
  ArrayList queries = null;
  Token oprt = null;
    q = NotQuery();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_3;
      }
      oprt = jj_consume_token(AND);
      if (queries == null) {
        queries = new ArrayList();
        queries.add(q);
      }
      q = NotQuery();
      queries.add(q);
    }
   {if (true) return (queries == null) ? q : getAndQuery(queries, true , oprt);}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery NotQuery() throws ParseException {
  SrndQuery q;
  ArrayList queries = null;
  Token oprt = null;
    q = NQuery();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_4;
      }
      oprt = jj_consume_token(NOT);
      if (queries == null) {
        queries = new ArrayList();
        queries.add(q);
      }
      q = NQuery();
      queries.add(q);
    }
   {if (true) return (queries == null) ? q : getNotQuery(queries, oprt);}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery NQuery() throws ParseException {
  SrndQuery q;
  ArrayList queries;
  Token dt;
    q = WQuery();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case N:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_5;
      }
      dt = jj_consume_token(N);
      queries = new ArrayList();
      queries.add(q); 
      q = WQuery();
      queries.add(q);
      q = getDistanceQuery(queries, true , dt, false );
    }
   {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery WQuery() throws ParseException {
  SrndQuery q;
  ArrayList queries;
  Token wt;
    q = PrimaryQuery();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case W:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_6;
      }
      wt = jj_consume_token(W);
      queries = new ArrayList();
      queries.add(q); 
      q = PrimaryQuery();
      queries.add(q);
      q = getDistanceQuery(queries, true , wt, true );
    }
   {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery PrimaryQuery() throws ParseException {
  SrndQuery q;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LPAREN:
      jj_consume_token(LPAREN);
      q = FieldsQuery();
      jj_consume_token(RPAREN);
      break;
    case OR:
    case AND:
    case W:
    case N:
      q = PrefixOperatorQuery();
      break;
    case TRUNCQUOTED:
    case QUOTED:
    case SUFFIXTERM:
    case TRUNCTERM:
    case TERM:
      q = SimpleTerm();
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    OptionalWeights(q);
   {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery PrefixOperatorQuery() throws ParseException {
  Token oprt;
  List queries;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OR:
      oprt = jj_consume_token(OR);
          queries = FieldsQueryList();
     {if (true) return getOrQuery(queries, false , oprt);}
      break;
    case AND:
      oprt = jj_consume_token(AND);
          queries = FieldsQueryList();
     {if (true) return getAndQuery(queries, false , oprt);}
      break;
    case N:
      oprt = jj_consume_token(N);
          queries = FieldsQueryList();
     {if (true) return getDistanceQuery(queries, false , oprt, false );}
      break;
    case W:
      oprt = jj_consume_token(W);
          queries = FieldsQueryList();
     {if (true) return getDistanceQuery(queries, false  , oprt, true );}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }
  final public List FieldsQueryList() throws ParseException {
  SrndQuery q;
  ArrayList queries = new ArrayList();
    jj_consume_token(LPAREN);
    q = FieldsQuery();
                     queries.add(q);
    label_7:
    while (true) {
      jj_consume_token(COMMA);
      q = FieldsQuery();
                              queries.add(q);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_7;
      }
    }
    jj_consume_token(RPAREN);
   {if (true) return queries;}
    throw new Error("Missing return statement in function");
  }
  final public SrndQuery SimpleTerm() throws ParseException {
  Token term;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TERM:
      term = jj_consume_token(TERM);
     {if (true) return getTermQuery(term.image, false );}
      break;
    case QUOTED:
      term = jj_consume_token(QUOTED);
     {if (true) return getTermQuery(term.image.substring(1, term.image.length()-1), true );}
      break;
    case SUFFIXTERM:
      term = jj_consume_token(SUFFIXTERM);
      if (! allowedSuffix(term.image)) {
        {if (true) throw new ParseException(truncationErrorMessage + term.image);}
      }
      {if (true) return getPrefixQuery(term.image.substring(0, term.image.length()-1), false );}
      break;
    case TRUNCTERM:
      term = jj_consume_token(TRUNCTERM);
      if (! allowedTruncation(term.image)) {
        {if (true) throw new ParseException(truncationErrorMessage + term.image);}
      }
      {if (true) return getTruncQuery(term.image);}
      break;
    case TRUNCQUOTED:
      term = jj_consume_token(TRUNCQUOTED);
      if ((term.image.length() - 3) < minimumPrefixLength) {
        {if (true) throw new ParseException(truncationErrorMessage + term.image);}
      }
      {if (true) return getPrefixQuery(term.image.substring(1, term.image.length()-2), true );}
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }
  final public void OptionalWeights(SrndQuery q) throws ParseException {
  Token weight=null;
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_8;
      }
      jj_consume_token(CARAT);
      weight = jj_consume_token(NUMBER);
      float f;
      try {
        f = Float.valueOf(weight.image).floatValue();
      } catch (Exception floatExc) {
        {if (true) throw new ParseException(boostErrorMessage + weight.image + " (" + floatExc + ")");}
      }
      if (f <= 0.0) {
        {if (true) throw new ParseException(boostErrorMessage + weight.image);}
      }
      q.setWeight(f * q.getWeight()); 
    }
  }
  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }
  private boolean jj_3_1() {
    if (jj_scan_token(TERM)) return true;
    if (jj_scan_token(COLON)) return true;
    return false;
  }
  public QueryParserTokenManager token_source;
  public Token token;
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[10];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x100,0x200,0x400,0x1000,0x800,0x7c3b00,0x1b00,0x8000,0x7c0000,0x20000,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;
  public QueryParser(CharStream stream) {
    token_source = new QueryParserTokenManager(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 10; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public void ReInit(CharStream stream) {
    token_source.ReInit(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 10; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public QueryParser(QueryParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 10; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public void ReInit(QueryParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 10; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }
  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }
  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }
  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;
  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[24];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 10; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }
  final public void enable_tracing() {
  }
  final public void disable_tracing() {
  }
  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }
  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }
  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }
}
