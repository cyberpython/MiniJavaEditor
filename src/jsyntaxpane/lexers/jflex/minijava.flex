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
 
%% 

%public
%class MiniJavaLexer
%extends DefaultLexer
%final
%unicode
%char
%type Token


%{
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

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} 

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
IntegerLiteral = [0-9]+

%%

<YYINITIAL> {

  /* keywords */
  "class"                        |
  "public"                       |
  "static"                       |
  "void"                         |
  "main"                         |
  "String"                       |
  "extends"                      |
  "return"                       |
  "if"                           |
  "else"                         |
  "while"                        |
  "for"                          |
  "System.out.println"           |
  "this"                         |
  "new"                          |
  "true"                         |
  "false"                        { return token(TokenType.KEYWORD); }

  /* Java Built in types and wrappers */
  "boolean"                      |
  "int"                          { return token(TokenType.TYPE); }
  
  /* operators */

  "("                            { return token(TokenType.OPERATOR,  PARAN); }
  ")"                            { return token(TokenType.OPERATOR, -PARAN); }
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }
  ";"                            | 
  ","                            | 
  "."                            | 
  "="                            | 
  "<"                            |
  "!"                            | 
  "&&"                           | 
  "+"                            | 
  "-"                            | 
  "*"                            { return token(TokenType.OPERATOR); } 
  

  /* numeric literals */

  {IntegerLiteral}               { return token(TokenType.NUMBER); }
    
  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }

  /* identifiers */ 
  {Identifier}                   { return token(TokenType.IDENTIFIER); }
}


/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

