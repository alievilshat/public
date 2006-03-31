/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others) 
 * E-mail: degenerl@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/pdt 
 * Copyright (C): 2004-2006, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * In addition, you may at your option use, modify and redistribute any
 * part of this program under the terms of the GNU Lesser General Public
 * License (LGPL), version 2.1 or, at your option, any later version of the
 * same license, as long as
 * 
 * 1) The program part in question does not depend, either directly or
 *   indirectly, on parts of the Eclipse framework and
 *   
 * 2) the program part in question does not include files that contain or
 *   are derived from third-party work and are therefor covered by special
 *   license agreements.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *   
 * ad 1: A program part is said to "depend, either directly or indirectly,
 *   on parts of the Eclipse framework", if it cannot be compiled or cannot
 *   be run without the help or presence of some part of the Eclipse
 *   framework. All java classes in packages containing the "pdt" package
 *   fragment in their name fall into this category.
 *   
 * ad 2: "Third-party code" means any code that was originaly written as
 *   part of a project other than the PDT. Files that contain or are based on
 *   such code contain a notice telling you so, and telling you the
 *   particular conditions under which they may be used, modified and/or
 *   distributed.
 ****************************************************************************/

/* Generated By:JJTree&JavaCC: Do not edit this line. CanonicalTermParserTokenManager.java */
package org.cs3.pl.cterm.internal.parser;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CanonicalTermParserTokenManager implements CanonicalTermParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         return jjStopAtPos(0, 13);
      case 40:
         return jjStopAtPos(0, 7);
      case 41:
         return jjStopAtPos(0, 8);
      case 44:
         return jjStopAtPos(0, 9);
      case 46:
         return jjStartNfaWithStates_0(0, 10, 1);
      case 91:
         return jjStopAtPos(0, 11);
      case 93:
         return jjStopAtPos(0, 12);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffefffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0x7fffffff00ffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xfffffffe00000000L, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0xffffffffffff0000L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec4 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffL
};
static final long[] jjbitVec5 = {
   0xfffe0000fffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0x7fffffff00ffffffL
};
static final long[] jjbitVec6 = {
   0x0L, 0x0L, 0x0L, 0xffffffffffffffffL
};
static final long[] jjbitVec7 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec8 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec9 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 55;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfc00ec5800000000L & l) != 0L)
                  {
                     if (kind > 14)
                        kind = 14;
                     jjCheckNAdd(1);
                  }
                  else if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddStates(0, 9);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(10, 13);
                  else if (curChar == 39)
                     jjCheckNAddStates(14, 17);
                  if (curChar == 47)
                     jjAddStates(18, 19);
                  else if (curChar == 48)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 1:
                  if ((0xffffec5800000000L & l) == 0L)
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(1);
                  break;
               case 3:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 4:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 5:
                  if ((0xffffec5800000000L & l) != 0L && kind > 22)
                     kind = 22;
                  break;
               case 6:
                  if (curChar == 48)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 7:
               case 11:
                  if (curChar == 39)
                     jjCheckNAddStates(14, 17);
                  break;
               case 8:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAddStates(14, 17);
                  break;
               case 10:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(14, 17);
                  break;
               case 12:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 13:
                  if (curChar == 39 && kind > 28)
                     kind = 28;
                  break;
               case 14:
                  if (curChar == 34)
                     jjCheckNAddStates(10, 13);
                  break;
               case 15:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(10, 13);
                  break;
               case 17:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(10, 13);
                  break;
               case 18:
                  if (curChar == 39)
                     jjCheckNAddStates(10, 13);
                  break;
               case 19:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 20:
                  if (curChar == 34 && kind > 29)
                     kind = 29;
                  break;
               case 21:
                  if (curChar == 47)
                     jjAddStates(18, 19);
                  break;
               case 22:
                  if (curChar == 47)
                     jjCheckNAddStates(20, 22);
                  break;
               case 23:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(20, 22);
                  break;
               case 24:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 25:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 26:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 27:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 28:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if (curChar == 42)
                     jjAddStates(23, 24);
                  break;
               case 30:
                  if ((0xffff7fffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(31, 29);
                  break;
               case 31:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(31, 29);
                  break;
               case 32:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 9);
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(34);
                  break;
               case 36:
                  if ((0x3000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 38:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjstateSet[jjnewStateCnt++] = 38;
                  break;
               case 40:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 25)
                     kind = 25;
                  jjstateSet[jjnewStateCnt++] = 40;
                  break;
               case 41:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 42:
                  if (curChar == 46)
                     jjCheckNAdd(43);
                  break;
               case 43:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAddTwoStates(43, 44);
                  break;
               case 45:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(46);
                  break;
               case 46:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(46);
                  break;
               case 47:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(47, 48);
                  break;
               case 49:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(50);
                  break;
               case 50:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(50);
                  break;
               case 51:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAddTwoStates(51, 52);
                  break;
               case 53:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(54);
                  break;
               case 54:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(54);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x47fffffe50000001L & l) != 0L)
                  {
                     if (kind > 14)
                        kind = 14;
                     jjCheckNAdd(1);
                  }
                  else if ((0x87fffffeL & l) != 0L)
                  {
                     if (kind > 15)
                        kind = 15;
                     jjCheckNAdd(3);
                  }
                  break;
               case 1:
                  if ((0x47fffffed7ffffffL & l) == 0L)
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(1);
                  break;
               case 2:
                  if ((0x87fffffeL & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAdd(3);
                  break;
               case 3:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAdd(3);
                  break;
               case 5:
                  if ((0x47fffffed7ffffffL & l) != 0L && kind > 22)
                     kind = 22;
                  break;
               case 8:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(14, 17);
                  break;
               case 9:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 10:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(14, 17);
                  break;
               case 15:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(10, 13);
                  break;
               case 16:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 17:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(10, 13);
                  break;
               case 23:
                  jjAddStates(20, 22);
                  break;
               case 28:
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
               case 31:
                  jjCheckNAddTwoStates(31, 29);
                  break;
               case 35:
                  if (curChar == 98)
                     jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 37:
                  if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 38;
                  break;
               case 39:
                  if (curChar == 120)
                     jjCheckNAdd(40);
                  break;
               case 40:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 25)
                     kind = 25;
                  jjCheckNAdd(40);
                  break;
               case 44:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(25, 26);
                  break;
               case 48:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(27, 28);
                  break;
               case 52:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(29, 30);
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
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 1:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(1);
                  break;
               case 3:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 5:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2) && kind > 22)
                     kind = 22;
                  break;
               case 8:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjAddStates(14, 17);
                  break;
               case 15:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjAddStates(10, 13);
                  break;
               case 23:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(20, 22);
                  break;
               case 28:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
               case 31:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(31, 29);
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
      if ((i = jjnewStateCnt) == (startsAt = 55 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   34, 35, 37, 39, 41, 42, 47, 48, 51, 52, 15, 16, 19, 20, 8, 9, 
   12, 13, 22, 27, 23, 24, 26, 30, 32, 45, 46, 49, 50, 53, 54, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      case 32:
         return ((jjbitVec3[i2] & l2) != 0L);
      case 255:
         return ((jjbitVec4[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 255:
         return ((jjbitVec4[i2] & l2) != 0L);
      default : 
         if ((jjbitVec5[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec9[i2] & l2) != 0L);
      default : 
         if ((jjbitVec8[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_3(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec9[i2] & l2) != 0L);
      default : 
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\50", "\51", "\54", "\56", "\133", 
"\135", "\41", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x37c0ff81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[55];
private final int[] jjstateSet = new int[110];
protected char curChar;
public CanonicalTermParserTokenManager(SimpleCharStream stream)
{
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public CanonicalTermParserTokenManager(SimpleCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 55; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
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

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
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

}
