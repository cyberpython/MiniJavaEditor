/* The following code was generated by JFlex 1.4.1 on 22/3/2009 12:10 πμ */

/*
 * Copyright 2008 Georgios "cyebrpython" Migdos cyberpython@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jsyntaxpane.lexers;

import jsyntaxpane.DefaultLexer;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;
 

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1
 * on 22/3/2009 12:10 πμ from the specification file
 * <tt>/media/MySpace/programming/Java/netbeans projects/JSyntaxPaneMini/trunk/src/main/jflex/jsyntaxpane/lexers/minijava.flex</tt>
 */
public final class MiniJavaLexer extends DefaultLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\7\1\3\1\2\1\0\1\3\1\1\16\7\4\0\1\3\1\47"+
    "\2\0\1\6\1\0\1\50\1\0\1\41\1\42\1\5\1\47\1\47"+
    "\1\47\1\40\1\4\12\10\1\0\1\47\1\47\1\47\3\0\22\6"+
    "\1\27\7\6\1\45\1\0\1\46\1\0\1\6\1\0\1\13\1\17"+
    "\1\11\1\24\1\32\1\34\1\31\1\36\1\20\2\6\1\12\1\25"+
    "\1\26\1\23\1\15\1\6\1\30\1\14\1\21\1\16\1\22\1\35"+
    "\1\33\1\37\1\6\1\43\1\0\1\44\1\0\41\7\2\0\4\6"+
    "\4\0\1\6\2\0\1\7\7\0\1\6\4\0\1\6\5\0\27\6"+
    "\1\0\37\6\1\0\u013f\6\31\0\162\6\4\0\14\6\16\0\5\6"+
    "\11\0\1\6\21\0\130\7\5\0\23\7\12\0\1\6\13\0\1\6"+
    "\1\0\3\6\1\0\1\6\1\0\24\6\1\0\54\6\1\0\46\6"+
    "\1\0\5\6\4\0\202\6\1\0\4\7\3\0\105\6\1\0\46\6"+
    "\2\0\2\6\6\0\20\6\41\0\46\6\2\0\1\6\7\0\47\6"+
    "\11\0\21\7\1\0\27\7\1\0\3\7\1\0\1\7\1\0\2\7"+
    "\1\0\1\7\13\0\33\6\5\0\3\6\15\0\4\7\14\0\6\7"+
    "\13\0\32\6\5\0\13\6\16\7\7\0\12\7\4\0\2\6\1\7"+
    "\143\6\1\0\1\6\10\7\1\0\6\7\2\6\2\7\1\0\4\7"+
    "\2\6\12\7\3\6\2\0\1\6\17\0\1\7\1\6\1\7\36\6"+
    "\33\7\2\0\3\6\60\0\46\6\13\7\1\6\u014f\0\3\7\66\6"+
    "\2\0\1\7\1\6\20\7\2\0\1\6\4\7\3\0\12\6\2\7"+
    "\2\0\12\7\21\0\3\7\1\0\10\6\2\0\2\6\2\0\26\6"+
    "\1\0\7\6\1\0\1\6\3\0\4\6\2\0\1\7\1\6\7\7"+
    "\2\0\2\7\2\0\3\7\11\0\1\7\4\0\2\6\1\0\3\6"+
    "\2\7\2\0\12\7\4\6\15\0\3\7\1\0\6\6\4\0\2\6"+
    "\2\0\26\6\1\0\7\6\1\0\2\6\1\0\2\6\1\0\2\6"+
    "\2\0\1\7\1\0\5\7\4\0\2\7\2\0\3\7\13\0\4\6"+
    "\1\0\1\6\7\0\14\7\3\6\14\0\3\7\1\0\11\6\1\0"+
    "\3\6\1\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6\2\0"+
    "\1\7\1\6\10\7\1\0\3\7\1\0\3\7\2\0\1\6\17\0"+
    "\2\6\2\7\2\0\12\7\1\0\1\6\17\0\3\7\1\0\10\6"+
    "\2\0\2\6\2\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6"+
    "\2\0\1\7\1\6\6\7\3\0\2\7\2\0\3\7\10\0\2\7"+
    "\4\0\2\6\1\0\3\6\4\0\12\7\1\0\1\6\20\0\1\7"+
    "\1\6\1\0\6\6\3\0\3\6\1\0\4\6\3\0\2\6\1\0"+
    "\1\6\1\0\2\6\3\0\2\6\3\0\3\6\3\0\10\6\1\0"+
    "\3\6\4\0\5\7\3\0\3\7\1\0\4\7\11\0\1\7\17\0"+
    "\11\7\11\0\1\6\7\0\3\7\1\0\10\6\1\0\3\6\1\0"+
    "\27\6\1\0\12\6\1\0\5\6\4\0\7\7\1\0\3\7\1\0"+
    "\4\7\7\0\2\7\11\0\2\6\4\0\12\7\22\0\2\7\1\0"+
    "\10\6\1\0\3\6\1\0\27\6\1\0\12\6\1\0\5\6\2\0"+
    "\1\7\1\6\7\7\1\0\3\7\1\0\4\7\7\0\2\7\7\0"+
    "\1\6\1\0\2\6\4\0\12\7\22\0\2\7\1\0\10\6\1\0"+
    "\3\6\1\0\27\6\1\0\20\6\4\0\6\7\2\0\3\7\1\0"+
    "\4\7\11\0\1\7\10\0\2\6\4\0\12\7\22\0\2\7\1\0"+
    "\22\6\3\0\30\6\1\0\11\6\1\0\1\6\2\0\7\6\3\0"+
    "\1\7\4\0\6\7\1\0\1\7\1\0\10\7\22\0\2\7\15\0"+
    "\60\6\1\7\2\6\7\7\4\0\10\6\10\7\1\0\12\7\47\0"+
    "\2\6\1\0\1\6\2\0\2\6\1\0\1\6\2\0\1\6\6\0"+
    "\4\6\1\0\7\6\1\0\3\6\1\0\1\6\1\0\1\6\2\0"+
    "\2\6\1\0\4\6\1\7\2\6\6\7\1\0\2\7\1\6\2\0"+
    "\5\6\1\0\1\6\1\0\6\7\2\0\12\7\2\0\2\6\42\0"+
    "\1\6\27\0\2\7\6\0\12\7\13\0\1\7\1\0\1\7\1\0"+
    "\1\7\4\0\2\7\10\6\1\0\42\6\6\0\24\7\1\0\2\7"+
    "\4\6\4\0\10\7\1\0\44\7\11\0\1\7\71\0\42\6\1\0"+
    "\5\6\1\0\2\6\1\0\7\7\3\0\4\7\6\0\12\7\6\0"+
    "\6\6\4\7\106\0\46\6\12\0\51\6\7\0\132\6\5\0\104\6"+
    "\5\0\122\6\6\0\7\6\1\0\77\6\1\0\1\6\1\0\4\6"+
    "\2\0\7\6\1\0\1\6\1\0\4\6\2\0\47\6\1\0\1\6"+
    "\1\0\4\6\2\0\37\6\1\0\1\6\1\0\4\6\2\0\7\6"+
    "\1\0\1\6\1\0\4\6\2\0\7\6\1\0\7\6\1\0\27\6"+
    "\1\0\37\6\1\0\1\6\1\0\4\6\2\0\7\6\1\0\47\6"+
    "\1\0\23\6\16\0\11\7\56\0\125\6\14\0\u026c\6\2\0\10\6"+
    "\12\0\32\6\5\0\113\6\3\0\3\6\17\0\15\6\1\0\4\6"+
    "\3\7\13\0\22\6\3\7\13\0\22\6\2\7\14\0\15\6\1\0"+
    "\3\6\1\0\2\7\14\0\64\6\40\7\3\0\1\6\3\0\2\6"+
    "\1\7\2\0\12\7\41\0\3\7\2\0\12\7\6\0\130\6\10\0"+
    "\51\6\1\7\126\0\35\6\3\0\14\7\4\0\14\7\12\0\12\7"+
    "\36\6\2\0\5\6\u038b\0\154\6\224\0\234\6\4\0\132\6\6\0"+
    "\26\6\2\0\6\6\2\0\46\6\2\0\6\6\2\0\10\6\1\0"+
    "\1\6\1\0\1\6\1\0\1\6\1\0\37\6\2\0\65\6\1\0"+
    "\7\6\1\0\1\6\3\0\3\6\1\0\7\6\3\0\4\6\2\0"+
    "\6\6\4\0\15\6\5\0\3\6\1\0\7\6\17\0\4\7\32\0"+
    "\5\7\20\0\2\6\23\0\1\6\13\0\4\7\6\0\6\7\1\0"+
    "\1\6\15\0\1\6\40\0\22\6\36\0\15\7\4\0\1\7\3\0"+
    "\6\7\27\0\1\6\4\0\1\6\2\0\12\6\1\0\1\6\3\0"+
    "\5\6\6\0\1\6\1\0\1\6\1\0\1\6\1\0\4\6\1\0"+
    "\3\6\1\0\7\6\3\0\3\6\5\0\5\6\26\0\44\6\u0e81\0"+
    "\3\6\31\0\11\6\6\7\1\0\5\6\2\0\5\6\4\0\126\6"+
    "\2\0\2\7\2\0\3\6\1\0\137\6\5\0\50\6\4\0\136\6"+
    "\21\0\30\6\70\0\20\6\u0200\0\u19b6\6\112\0\u51a6\6\132\0\u048d\6"+
    "\u0773\0\u2ba4\6\u215c\0\u012e\6\2\0\73\6\225\0\7\6\14\0\5\6"+
    "\5\0\1\6\1\7\12\6\1\0\15\6\1\0\5\6\1\0\1\6"+
    "\1\0\2\6\1\0\2\6\1\0\154\6\41\0\u016b\6\22\0\100\6"+
    "\2\0\66\6\50\0\15\6\3\0\20\7\20\0\4\7\17\0\2\6"+
    "\30\0\3\6\31\0\1\6\6\0\5\6\1\0\207\6\2\0\1\7"+
    "\4\0\1\6\13\0\12\7\7\0\32\6\4\0\1\6\1\0\32\6"+
    "\12\0\132\6\3\0\6\6\2\0\6\6\2\0\6\6\2\0\3\6"+
    "\3\0\2\6\3\0\2\6\22\0\3\7\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\4\1\1\2\1\3\1\4\16\3\1\5\1\6"+
    "\1\7\1\10\1\11\1\12\1\1\1\13\1\0\5\3"+
    "\1\14\15\3\2\13\2\0\4\3\1\15\11\3\1\0"+
    "\15\3\13\0\1\14";

  private static int [] zzUnpackAction() {
    int [] result = new int[94];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\51\0\122\0\173\0\244\0\51\0\315\0\366"+
    "\0\u011f\0\u0148\0\u0171\0\u019a\0\u01c3\0\u01ec\0\u0215\0\u023e"+
    "\0\u0267\0\u0290\0\u02b9\0\u02e2\0\u030b\0\u0334\0\51\0\51"+
    "\0\51\0\51\0\51\0\51\0\u035d\0\u0386\0\u03af\0\u03d8"+
    "\0\u0401\0\u042a\0\u0453\0\u047c\0\315\0\u04a5\0\u04ce\0\u04f7"+
    "\0\u0520\0\u0549\0\u0572\0\u059b\0\u05c4\0\u05ed\0\u0616\0\u063f"+
    "\0\u0668\0\u0691\0\u06ba\0\51\0\u06e3\0\u070c\0\u0735\0\u075e"+
    "\0\u0787\0\u07b0\0\315\0\u07d9\0\u0802\0\u082b\0\u0854\0\u087d"+
    "\0\u08a6\0\u08cf\0\u08f8\0\u0921\0\u094a\0\u0973\0\u099c\0\u09c5"+
    "\0\u09ee\0\u0a17\0\u0a40\0\u0a69\0\u0a92\0\u0abb\0\u0ae4\0\u0b0d"+
    "\0\u0b36\0\u0b5f\0\u0b88\0\u0bb1\0\u0bda\0\u0c03\0\u0c2c\0\u0c55"+
    "\0\u0c7e\0\u0ca7\0\u0cd0\0\u0cf9\0\u0d22\0\51";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[94];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\1\2\1\4\1\5\1\6\1\7\1\2"+
    "\1\10\1\11\2\7\1\12\1\13\1\7\1\14\1\15"+
    "\1\16\1\17\2\7\1\20\1\21\1\22\1\23\1\7"+
    "\1\24\1\7\1\25\1\26\2\7\1\6\1\27\1\30"+
    "\1\31\1\32\1\33\1\34\1\6\1\35\53\0\1\2"+
    "\51\0\1\4\51\0\1\36\1\37\51\0\32\7\21\0"+
    "\1\10\46\0\4\7\1\40\25\7\17\0\13\7\1\41"+
    "\16\7\17\0\10\7\1\42\21\7\17\0\15\7\1\43"+
    "\14\7\17\0\20\7\1\44\5\7\1\45\3\7\17\0"+
    "\22\7\1\46\5\7\1\47\1\7\17\0\15\7\1\50"+
    "\14\7\17\0\5\7\1\51\24\7\17\0\24\7\1\52"+
    "\5\7\17\0\13\7\1\53\15\7\1\54\17\0\24\7"+
    "\1\55\5\7\17\0\4\7\1\56\20\7\1\57\4\7"+
    "\17\0\5\7\1\60\7\7\1\61\14\7\17\0\30\7"+
    "\1\62\1\7\61\0\1\6\1\36\1\63\1\64\46\36"+
    "\5\65\1\66\43\65\6\0\5\7\1\67\24\7\17\0"+
    "\5\7\1\70\24\7\17\0\11\7\1\71\20\7\17\0"+
    "\15\7\1\72\14\7\17\0\13\7\1\73\16\7\17\0"+
    "\10\7\1\74\21\7\17\0\12\7\1\75\17\7\17\0"+
    "\12\7\1\76\17\7\17\0\12\7\1\77\17\7\17\0"+
    "\27\7\1\45\2\7\17\0\22\7\1\100\7\7\17\0"+
    "\6\7\1\101\23\7\17\0\13\7\1\102\16\7\17\0"+
    "\6\7\1\74\23\7\17\0\13\7\1\103\16\7\17\0"+
    "\4\7\1\56\25\7\17\0\22\7\1\45\7\7\17\0"+
    "\12\7\1\104\17\7\13\0\1\64\46\0\5\65\1\105"+
    "\43\65\4\0\1\64\1\66\51\0\6\7\1\75\23\7"+
    "\17\0\13\7\1\106\16\7\17\0\4\7\1\106\25\7"+
    "\17\0\4\7\1\107\25\7\17\0\24\7\1\45\5\7"+
    "\17\0\6\7\1\45\23\7\17\0\16\7\1\45\13\7"+
    "\17\0\20\7\1\45\11\7\17\0\12\7\1\110\17\7"+
    "\17\0\13\7\1\111\16\7\17\0\10\7\1\112\21\7"+
    "\17\0\24\7\1\113\5\7\17\0\4\7\1\74\25\7"+
    "\11\0\4\65\1\64\1\105\43\65\6\0\12\7\1\114"+
    "\17\7\17\0\24\7\1\115\5\7\17\0\20\7\1\116"+
    "\11\7\17\0\24\7\1\117\5\7\17\0\22\7\1\77"+
    "\7\7\17\0\20\7\1\120\11\7\17\0\3\7\1\45"+
    "\26\7\17\0\5\7\1\121\24\7\17\0\23\7\1\45"+
    "\6\7\17\0\17\7\1\122\12\7\17\0\16\7\1\75"+
    "\13\7\17\0\20\7\1\73\11\7\17\0\32\7\1\123"+
    "\33\0\1\124\43\0\1\125\53\0\1\126\67\0\1\127"+
    "\25\0\1\130\63\0\1\131\40\0\1\132\56\0\1\133"+
    "\43\0\1\134\41\0\1\135\64\0\1\136\22\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3403];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\3\1\1\11\20\1\6\11\2\1\1\0"+
    "\24\1\1\11\2\0\16\1\1\0\15\1\13\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[94];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public MiniJavaLexer() {
        super();
    }

    private Token token(TokenType type) {
        return new Token(type, yychar, yylength());
    }

    private Token token(TokenType type, int pairValue) {
        return new Token(type, yychar, yylength(), (byte)pairValue);
    }

    private static final byte PARAN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;



  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public MiniJavaLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public MiniJavaLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 1754) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzPushbackPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead < 0) {
      return true;
    }
    else {
      zzEndRead+= numRead;
      return false;
    }
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = zzLexicalState;


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 12: 
          { return token(TokenType.KEYWORD);
          }
        case 14: break;
        case 7: 
          { return token(TokenType.OPERATOR,  CURLY);
          }
        case 15: break;
        case 9: 
          { return token(TokenType.OPERATOR,  BRACKET);
          }
        case 16: break;
        case 2: 
          { return token(TokenType.OPERATOR);
          }
        case 17: break;
        case 6: 
          { return token(TokenType.OPERATOR, -PARAN);
          }
        case 18: break;
        case 3: 
          { return token(TokenType.IDENTIFIER);
          }
        case 19: break;
        case 5: 
          { return token(TokenType.OPERATOR,  PARAN);
          }
        case 20: break;
        case 11: 
          { return token(TokenType.COMMENT);
          }
        case 21: break;
        case 10: 
          { return token(TokenType.OPERATOR, -BRACKET);
          }
        case 22: break;
        case 8: 
          { return token(TokenType.OPERATOR, -CURLY);
          }
        case 23: break;
        case 13: 
          { return token(TokenType.TYPE);
          }
        case 24: break;
        case 4: 
          { return token(TokenType.NUMBER);
          }
        case 25: break;
        case 1: 
          { 
          }
        case 26: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
              {
                return null;
              }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}