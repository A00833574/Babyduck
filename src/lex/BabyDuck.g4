
grammar BabyDuck;

@header {
package lex;
}

options {
  language = Java;
}

// ----------------------------
// PARSER RULES
// ----------------------------
programa     : PROGRAM ID SEMICOLON vars funcs MAIN body END ;

vars         : var_decl* ;

var_decl     : VAR tipo ID (COMMA ID)* SEMICOLON ;

tipo         : INT
             | FLOAT ;

funcs        : funcion* ;

funcion      : tipo ID LPAREN parametros? RPAREN COLON vars body SEMICOLON ;

parametros   : tipo ID (COMMA tipo ID)* ;

body         : LBRACE statements RBRACE ;

statements   : statement* ;

statement    : assign SEMICOLON
             | condition
             | cycle
             | print
             | f_call SEMICOLON ;

assign       : ID ASSIGN expresion ;

print        : PRINT LPAREN (expresion | CTE_STRING) ( COMMA (expresion | CTE_STRING) )* RPAREN SEMICOLON;

condition    : IF LPAREN expresion RPAREN body (ELSE body)? ;

cycle        : WHILE LPAREN expresion RPAREN DO body ;

expresion    : exp (relop exp)? ;

relop        : EQ
             | NEQ
             | LT
             | LEQ
             | GT
             | GEQ ;

exp          : termino ((PLUS | MINUS) termino)* ;

termino      : factor ((MULT | DIV) factor)* ;

factor       : LPAREN expresion RPAREN
             | CTE_INT
             | CTE_FLOAT
             | ID
             | f_call ;

f_call       : ID LPAREN (expresion (COMMA expresion)*)? RPAREN ;

// ----------------------------
// LEXER RULES
// ----------------------------
PROGRAM      : 'program' ;
MAIN         : 'main' ;
END          : 'end' ;
VAR          : 'var' ;
INT          : 'int' ;
FLOAT        : 'float' ;
VOID         : 'void' ;
PRINT        : 'print' ;
IF           : 'if' ;
ELSE         : 'else' ;
WHILE        : 'while' ;
DO           : 'do' ;

PLUS         : '+' ;
MINUS        : '-' ;
MULT         : '*' ;
DIV          : '/' ;
ASSIGN       : '=' ;
EQ           : '==' ;
NEQ          : '!=' ;
LT           : '<' ;
LEQ          : '<=' ;
GT           : '>' ;
GEQ          : '>=' ;

SEMICOLON    : ';' ;
COLON        : ':' ;
COMMA        : ',' ;
LPAREN       : '(' ;
RPAREN       : ')' ;
LBRACE       : '{' ;
RBRACE       : '}' ;

ID           : [a-zA-Z_][a-zA-Z_0-9]* ;
CTE_INT      : [0-9]+ ;
CTE_FLOAT    : [0-9]+ '.' [0-9]+ ;
CTE_STRING   : '"' (~["\\])* '"' ;

WS           : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;