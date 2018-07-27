package org.apache.lucene.queryParser.standard.parser;
import java.io.StringReader;
import java.util.Vector;
import org.apache.lucene.messages.Message;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeParseException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.AndQueryNode;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.BoostQueryNode;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.OrQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricRangeQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.SlopQueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
@SuppressWarnings("all")
public class StandardSyntaxParser implements SyntaxParser, StandardSyntaxParserConstants {
        private static final int CONJ_NONE =0;
        private static final int CONJ_AND =2;
        private static final int CONJ_OR =2;
   public StandardSyntaxParser() {
        this(new StringReader(""));
  }
    public QueryNode parse(CharSequence query, CharSequence field) throws QueryNodeParseException {
      ReInit(new StringReader(query.toString()));
      try {
        QueryNode querynode = TopLevelQuery(field);
        return querynode;
      }
      catch (ParseException tme) {
            tme.setQuery(query);
            throw tme;
      }
      catch (Error tme) {
          Message message = new MessageImpl(QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, tme.getMessage());
          QueryNodeParseException e = new QueryNodeParseException(tme);
            e.setQuery(query);
            e.setNonLocalizedMessage(message);
            throw e;
      }
    }
  final public int Conjunction() throws ParseException {
  int ret = CONJ_NONE;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case AND:
    case OR:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        jj_consume_token(AND);
            ret = CONJ_AND;
        break;
      case OR:
        jj_consume_token(OR);
              ret = CONJ_OR;
        break;
      default:
        jj_la1[0] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }
  final public ModifierQueryNode.Modifier Modifiers() throws ParseException {
  ModifierQueryNode.Modifier ret = ModifierQueryNode.Modifier.MOD_NONE;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
    case PLUS:
    case MINUS:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
              ret = ModifierQueryNode.Modifier.MOD_REQ;
        break;
      case MINUS:
        jj_consume_token(MINUS);
                 ret = ModifierQueryNode.Modifier.MOD_NOT;
        break;
      case NOT:
        jj_consume_token(NOT);
               ret = ModifierQueryNode.Modifier.MOD_NOT;
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }
  final public QueryNode TopLevelQuery(CharSequence field) throws ParseException {
        QueryNode q;
    q = Query(field);
    jj_consume_token(0);
                {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public QueryNode Query(CharSequence field) throws ParseException {
  Vector clauses = null;
  QueryNode c, first=null;
    first = DisjQuery(field);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
      case PLUS:
      case MINUS:
      case LPAREN:
      case QUOTED:
      case TERM:
      case RANGEIN_START:
      case RANGEEX_START:
      case NUMBER:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_1;
      }
      c = DisjQuery(field);
             if (clauses == null) {
                 clauses = new Vector();
                 clauses.addElement(first);
             }
         clauses.addElement(c);
    }
        if (clauses != null) {
                {if (true) return new BooleanQueryNode(clauses);}
        } else {
                {if (true) return first;}
        }
    throw new Error("Missing return statement in function");
  }
  final public QueryNode DisjQuery(CharSequence field) throws ParseException {
        QueryNode first, c;
        Vector clauses = null;
    first = ConjQuery(field);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_2;
      }
      jj_consume_token(OR);
      c = ConjQuery(field);
     if (clauses == null) {
         clauses = new Vector();
         clauses.addElement(first);
     }
     clauses.addElement(c);
    }
    if (clauses != null) {
            {if (true) return new OrQueryNode(clauses);}
    } else {
        {if (true) return first;}
    }
    throw new Error("Missing return statement in function");
  }
  final public QueryNode ConjQuery(CharSequence field) throws ParseException {
        QueryNode first, c;
        Vector clauses = null;
    first = ModClause(field);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_3;
      }
      jj_consume_token(AND);
      c = ModClause(field);
     if (clauses == null) {
         clauses = new Vector();
         clauses.addElement(first);
     }
     clauses.addElement(c);
    }
    if (clauses != null) {
            {if (true) return new AndQueryNode(clauses);}
    } else {
        {if (true) return first;}
    }
    throw new Error("Missing return statement in function");
  }
  final public QueryNode ModClause(CharSequence field) throws ParseException {
  QueryNode q;
  ModifierQueryNode.Modifier mods;
    mods = Modifiers();
    q = Clause(field);
                if (mods != ModifierQueryNode.Modifier.MOD_NONE) {
                        q = new ModifierQueryNode(q, mods);
                }
                {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public QueryNode Clause(CharSequence field) throws ParseException {
  QueryNode q;
  Token fieldToken=null, boost=null;
  boolean group = false;
    if (jj_2_1(2)) {
      fieldToken = jj_consume_token(TERM);
      jj_consume_token(COLON);
                               field=EscapeQuerySyntaxImpl.discardEscapeChar(fieldToken.image);
    } else {
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case QUOTED:
    case TERM:
    case RANGEIN_START:
    case RANGEEX_START:
    case NUMBER:
      q = Term(field);
      break;
    case LPAREN:
      jj_consume_token(LPAREN);
      q = Query(field);
      jj_consume_token(RPAREN);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        jj_consume_token(CARAT);
        boost = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[7] = jj_gen;
        ;
      }
                                                                 group=true;
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      if (boost != null) {
                  float f = (float)1.0;
                  try {
                    f = Float.valueOf(boost.image).floatValue();
                if (q != null) {
                        q = new BoostQueryNode(q, f);
                }
                  } catch (Exception ignored) {
                  }
      }
      if (group) { q = new GroupQueryNode(q);}
      {if (true) return q;}
    throw new Error("Missing return statement in function");
  }
  final public QueryNode Term(CharSequence field) throws ParseException {
  Token term, boost=null, fuzzySlop=null, goop1, goop2;
  boolean fuzzy = false;
  QueryNode q =null;
  ParametricQueryNode qLower, qUpper;
  float defaultMinSimilarity = 0.5f;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TERM:
    case NUMBER:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TERM:
        term = jj_consume_token(TERM);
                         q = new FieldQueryNode(field, EscapeQuerySyntaxImpl.discardEscapeChar(term.image), term.beginColumn, term.endColumn);
        break;
      case NUMBER:
        term = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FUZZY_SLOP:
        fuzzySlop = jj_consume_token(FUZZY_SLOP);
                                fuzzy=true;
        break;
      default:
        jj_la1[10] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        jj_consume_token(CARAT);
        boost = jj_consume_token(NUMBER);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case FUZZY_SLOP:
          fuzzySlop = jj_consume_token(FUZZY_SLOP);
                                                         fuzzy=true;
          break;
        default:
          jj_la1[11] = jj_gen;
          ;
        }
        break;
      default:
        jj_la1[12] = jj_gen;
        ;
      }
       if (fuzzy) {
          float fms = defaultMinSimilarity;
          try {
            fms = Float.valueOf(fuzzySlop.image.substring(1)).floatValue();
          } catch (Exception ignored) { }
         if(fms < 0.0f || fms > 1.0f){
           {if (true) throw new ParseException(new MessageImpl(QueryParserMessages.INVALID_SYNTAX_FUZZY_LIMITS));}
         }
         q = new FuzzyQueryNode(field, EscapeQuerySyntaxImpl.discardEscapeChar(term.image), fms, term.beginColumn, term.endColumn);
       }
      break;
    case RANGEIN_START:
      jj_consume_token(RANGEIN_START);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEIN_GOOP:
        goop1 = jj_consume_token(RANGEIN_GOOP);
        break;
      case RANGEIN_QUOTED:
        goop1 = jj_consume_token(RANGEIN_QUOTED);
        break;
      default:
        jj_la1[13] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEIN_TO:
        jj_consume_token(RANGEIN_TO);
        break;
      default:
        jj_la1[14] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEIN_GOOP:
        goop2 = jj_consume_token(RANGEIN_GOOP);
        break;
      case RANGEIN_QUOTED:
        goop2 = jj_consume_token(RANGEIN_QUOTED);
        break;
      default:
        jj_la1[15] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(RANGEIN_END);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        jj_consume_token(CARAT);
        boost = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[16] = jj_gen;
        ;
      }
          if (goop1.kind == RANGEIN_QUOTED) {
            goop1.image = goop1.image.substring(1, goop1.image.length()-1);
          }
          if (goop2.kind == RANGEIN_QUOTED) {
            goop2.image = goop2.image.substring(1, goop2.image.length()-1);
          }
          qLower = new ParametricQueryNode(field, ParametricQueryNode.CompareOperator.GE,
                                               EscapeQuerySyntaxImpl.discardEscapeChar(goop1.image), goop1.beginColumn, goop1.endColumn);
                  qUpper = new ParametricQueryNode(field, ParametricQueryNode.CompareOperator.LE,
                                               EscapeQuerySyntaxImpl.discardEscapeChar(goop2.image), goop2.beginColumn, goop2.endColumn);
          q = new ParametricRangeQueryNode(qLower, qUpper);
      break;
    case RANGEEX_START:
      jj_consume_token(RANGEEX_START);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEEX_GOOP:
        goop1 = jj_consume_token(RANGEEX_GOOP);
        break;
      case RANGEEX_QUOTED:
        goop1 = jj_consume_token(RANGEEX_QUOTED);
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEEX_TO:
        jj_consume_token(RANGEEX_TO);
        break;
      default:
        jj_la1[18] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RANGEEX_GOOP:
        goop2 = jj_consume_token(RANGEEX_GOOP);
        break;
      case RANGEEX_QUOTED:
        goop2 = jj_consume_token(RANGEEX_QUOTED);
        break;
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(RANGEEX_END);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        jj_consume_token(CARAT);
        boost = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[20] = jj_gen;
        ;
      }
          if (goop1.kind == RANGEEX_QUOTED) {
            goop1.image = goop1.image.substring(1, goop1.image.length()-1);
          }
          if (goop2.kind == RANGEEX_QUOTED) {
            goop2.image = goop2.image.substring(1, goop2.image.length()-1);
          }
          qLower = new ParametricQueryNode(field, ParametricQueryNode.CompareOperator.GT,
                                               EscapeQuerySyntaxImpl.discardEscapeChar(goop1.image), goop1.beginColumn, goop1.endColumn);
                  qUpper = new ParametricQueryNode(field, ParametricQueryNode.CompareOperator.LT,
                                               EscapeQuerySyntaxImpl.discardEscapeChar(goop2.image), goop2.beginColumn, goop2.endColumn);
          q = new ParametricRangeQueryNode(qLower, qUpper);
      break;
    case QUOTED:
      term = jj_consume_token(QUOTED);
                      q = new QuotedFieldQueryNode(field, EscapeQuerySyntaxImpl.discardEscapeChar(term.image.substring(1, term.image.length()-1)), term.beginColumn + 1, term.endColumn - 1);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FUZZY_SLOP:
        fuzzySlop = jj_consume_token(FUZZY_SLOP);
        break;
      default:
        jj_la1[21] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CARAT:
        jj_consume_token(CARAT);
        boost = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[22] = jj_gen;
        ;
      }
         int phraseSlop = 0;
         if (fuzzySlop != null) {
           try {
             phraseSlop = Float.valueOf(fuzzySlop.image.substring(1)).intValue();
             q = new SlopQueryNode(q, phraseSlop);
           }
           catch (Exception ignored) {
           }
         }
      break;
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          if (boost != null) {
                  float f = (float)1.0;
                  try {
                    f = Float.valueOf(boost.image).floatValue();
                if (q != null) {
                        q = new BoostQueryNode(q, f);
                }
                  } catch (Exception ignored) {
                  }
          }
      {if (true) return q;}
    throw new Error("Missing return statement in function");
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
  public StandardSyntaxParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  public Token token;
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[24];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x300,0x300,0x1c00,0x1c00,0x763c00,0x200,0x100,0x10000,0x762000,0x440000,0x80000,0x80000,0x10000,0x6000000,0x800000,0x6000000,0x10000,0x60000000,0x8000000,0x60000000,0x10000,0x80000,0x10000,0x760000,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;
  public StandardSyntaxParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public StandardSyntaxParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new StandardSyntaxParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public StandardSyntaxParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new StandardSyntaxParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public StandardSyntaxParser(StandardSyntaxParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }
  public void ReInit(StandardSyntaxParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 24; i++) jj_la1[i] = -1;
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
    boolean[] la1tokens = new boolean[31];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 24; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 31; i++) {
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
