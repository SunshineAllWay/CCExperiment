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
public class QueryParserTokenManager implements QueryParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_1(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_1(int pos, long active0)
{
   return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 13);
      case 41:
         return jjStopAtPos(0, 14);
      case 44:
         return jjStopAtPos(0, 15);
      case 58:
         return jjStopAtPos(0, 16);
      case 94:
         return jjStopAtPos(0, 17);
      default :
         return jjMoveNfa_1(0, 0);
   }
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_1(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 38;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7bffe8faffffd9ffL & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddStates(0, 4);
                  }
                  else if ((0x100002600L & l) != 0L)
                  {
                     if (kind > 7)
                        kind = 7;
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(5, 7);
                  if ((0x3fc000000000000L & l) != 0L)
                     jjCheckNAddStates(8, 11);
                  else if (curChar == 49)
                     jjCheckNAddTwoStates(20, 21);
                  break;
               case 19:
                  if ((0x3fc000000000000L & l) != 0L)
                     jjCheckNAddStates(8, 11);
                  break;
               case 20:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAdd(17);
                  break;
               case 21:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAdd(18);
                  break;
               case 22:
                  if (curChar == 49)
                     jjCheckNAddTwoStates(20, 21);
                  break;
               case 23:
                  if (curChar == 34)
                     jjCheckNAddStates(5, 7);
                  break;
               case 24:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(24, 25);
                  break;
               case 25:
                  if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 26;
                  break;
               case 26:
                  if (curChar == 42 && kind > 18)
                     kind = 18;
                  break;
               case 27:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(12, 14);
                  break;
               case 29:
                  if (curChar == 34)
                     jjCheckNAddStates(12, 14);
                  break;
               case 30:
                  if (curChar == 34 && kind > 19)
                     kind = 19;
                  break;
               case 31:
                  if ((0x7bffe8faffffd9ffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 4);
                  break;
               case 32:
                  if ((0x7bffe8faffffd9ffL & l) != 0L)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 33:
                  if (curChar == 42 && kind > 20)
                     kind = 20;
                  break;
               case 34:
                  if ((0x7bffe8faffffd9ffL & l) != 0L)
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 35:
                  if ((0x8000040000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(35, 36);
                  break;
               case 36:
                  if ((0xfbffecfaffffd9ffL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(36);
                  break;
               case 37:
                  if ((0x7bffe8faffffd9ffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(37);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xffffffffbfffffffL & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddStates(0, 4);
                  }
                  if ((0x400000004000L & l) != 0L)
                  {
                     if (kind > 12)
                        kind = 12;
                  }
                  else if ((0x80000000800000L & l) != 0L)
                  {
                     if (kind > 11)
                        kind = 11;
                  }
                  else if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 9;
                  else if (curChar == 65)
                     jjstateSet[jjnewStateCnt++] = 6;
                  else if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 3;
                  else if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 1;
                  if (curChar == 110)
                     jjstateSet[jjnewStateCnt++] = 15;
                  else if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 1:
                  if (curChar == 82 && kind > 8)
                     kind = 8;
                  break;
               case 2:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 3:
                  if (curChar == 114 && kind > 8)
                     kind = 8;
                  break;
               case 4:
                  if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 5:
                  if (curChar == 68 && kind > 9)
                     kind = 9;
                  break;
               case 6:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 7:
                  if (curChar == 65)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 8:
                  if (curChar == 100 && kind > 9)
                     kind = 9;
                  break;
               case 9:
                  if (curChar == 110)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 10:
                  if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 84 && kind > 10)
                     kind = 10;
                  break;
               case 12:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 13:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 14:
                  if (curChar == 116 && kind > 10)
                     kind = 10;
                  break;
               case 15:
                  if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 16:
                  if (curChar == 110)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 17:
                  if ((0x80000000800000L & l) != 0L && kind > 11)
                     kind = 11;
                  break;
               case 18:
                  if ((0x400000004000L & l) != 0L && kind > 12)
                     kind = 12;
                  break;
               case 24:
                  jjAddStates(15, 16);
                  break;
               case 27:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(12, 14);
                  break;
               case 28:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 29:
                  if (curChar == 92)
                     jjCheckNAddStates(12, 14);
                  break;
               case 31:
                  if ((0xffffffffbfffffffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 4);
                  break;
               case 32:
                  if ((0xffffffffbfffffffL & l) != 0L)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
                  if ((0xffffffffbfffffffL & l) != 0L)
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 36:
                  if ((0xffffffffbfffffffL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 37:
                  if ((0xffffffffbfffffffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(37);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 4);
                  break;
               case 24:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(15, 16);
                  break;
               case 27:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(12, 14);
                  break;
               case 32:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 36:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 37:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(37);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 38 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_0()
{
   return jjMoveNfa_0(0, 0);
}
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjAddStates(17, 18);
                  break;
               case 1:
                  if (curChar == 46)
                     jjCheckNAdd(2);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   32, 33, 34, 35, 37, 24, 27, 28, 20, 17, 21, 18, 27, 28, 30, 24, 
   25, 0, 1, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
"\50", "\51", "\54", "\72", "\136", null, null, null, null, null, null, };
public static final String[] lexStateNames = {
   "Boost",
   "DEFAULT",
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, 1, 
};
static final long[] jjtoToken = {
   0xffff01L, 
};
static final long[] jjtoSkip = {
   0x80L, 
};
protected CharStream input_stream;
private final int[] jjrounds = new int[38];
private final int[] jjstateSet = new int[76];
protected char curChar;
public QueryParserTokenManager(CharStream stream){
   input_stream = stream;
}
public QueryParserTokenManager(CharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(CharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 38; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(CharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 2 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}
protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);
   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;
   return t;
}
int curLexState = 1;
int defaultLexState = 1;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;
  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }
   switch(curLexState)
   {
     case 0:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}
private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
}