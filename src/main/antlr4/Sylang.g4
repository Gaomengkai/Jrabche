grammar Sylang;

fragment HexPrefix: '0x' | '0X';
fragment OctPrefix: '0';

fragment NonzeroDigit: [1-9];
fragment Digit: [0-9];
fragment HexDigit: [0-9a-fA-F];
fragment OctDegit: [0-7];

DecIntConst: NonzeroDigit Digit*;
OctIntConst: OctPrefix OctDegit*;
HexIntConst: HexPrefix HexDigit+;

fragment Dot: '.';

fragment Sign: '+' | '-';

fragment Exponent: 'e' | 'E';
fragment HexExponent: 'p' | 'P';

fragment DecFloatFrac: Digit* Dot Digit+ | Digit+ Dot;
fragment HexFloatFrac: HexDigit* Dot HexDigit+ | HexDigit+ Dot;

fragment DecFloatExp: Exponent Sign? Digit+;
fragment BinFloatExp: HexExponent Sign? Digit+;

DecFloatConst: DecFloatFrac DecFloatExp? | Digit+ DecFloatExp;
HexFloatConst:
	HexPrefix HexFloatFrac BinFloatExp
	| HexPrefix HexDigit+ BinFloatExp;

fragment Escaped: '\\' ['"?\\abfnrtv];

StringConst: '"' (~['"\\\r\n] | Escaped)* '"';

Int: 'int';
Float: 'float';
Void: 'void';

Const: 'const';

If: 'if';
Else: 'else';
While: 'while';
Break: 'break';
Continue: 'continue';
Return: 'return';
For : 'for';

Assign: '=';

Add: '+';
Sub: '-';
Mul: '*';
Div: '/';
Mod: '%';

DuoAdd: '++';
DuoSub: '--';

Eq: '==';
Neq: '!=';
Lt: '<';
Gt: '>';
Leq: '<=';
Geq: '>=';

Not: '!';
And: '&&';
Or: '||';

Comma: ',';
Semicolon: ';';
Lparen: '(';
Rparen: ')';
Lbracket: '[';
Rbracket: ']';
Lbrace: '{';
Rbrace: '}';



Ident: [A-Za-z_][_0-9A-Za-z]*;

Whitespace: [ \t\r\n]+ -> skip;

LineComment: '//' ~[\r\n]* -> skip;
BlockComment: '/*' .*? '*/' -> skip;

compUnit: compUnitItem* EOF;
compUnitItem: decl | funcDef;

decl: (constDecl | varDecl) Semicolon;

constDecl: Const bType constDef (Comma constDef)*;

bType: Int # int | Float # float;

constDef: Ident (Lbracket exp Rbracket)* Assign initVal;

varDecl: bType varDef (Comma varDef)*;
varDef: Ident (Lbracket exp Rbracket)* (Assign initVal)?;

initVal:
	exp											# initExp
	| Lbrace (initVal (Comma initVal)*)? Rbrace	# initList;

funcDef: funcType Ident Lparen funcFParams? Rparen block;

funcType: bType  | Void;

funcFParams: funcFParam (Comma funcFParam)*;

funcFParam:
	bType Ident													# scalarParam
	| bType Ident Lbracket Rbracket (Lbracket exp Rbracket)*	# arrayParam;

block: Lbrace blockItem* Rbrace;

blockItem: decl | stmt;

forInitClause: exp | varDecl|assignExp;

forIterationExpr:
    assignExp|exp
;

assignExp: lVal Assign exp;

stmt:
	assignExp Semicolon					# assign
	| exp? Semicolon							# exprStmt
	| block										# blockStmt
	| If Lparen cond Rparen stmt (Else stmt)?	# ifElse
	| While Lparen cond Rparen stmt				# while
    | For Lparen forInitClause? Semicolon cond? Semicolon forIterationExpr? Rparen stmt # for
	| Break Semicolon							# break
	| Continue Semicolon						# continue
	| Return exp? Semicolon						# return;

exp: addExp;

cond: lOrExp;

lVal: Ident (Lbracket exp Rbracket)*;

primaryExp:
	Lparen exp Rparen	# primaryExp_exp
	| lVal				# lValExpr
	| number			# primaryExp_number;

intConst:
	DecIntConst		# decIntConst
	| OctIntConst	# octIntConst
	| HexIntConst	# hexIntConst;
floatConst:
	DecFloatConst	# decFloatConst
	| HexFloatConst	# hexFloatConst;
number: intConst | floatConst;

postfixExp:
    primaryExp							# postfixExp_primary
    | primaryExp (DuoAdd|DuoSub)		# duoPostfix;

unaryExp:
	postfixExp							# unaryExp_postfix
	| (DuoAdd|DuoSub) postfixExp        # duoPrefix
	| Ident Lparen funcRParams? Rparen	# call
	| (Add|Sub|Not) unaryExp						# unary_;


stringConst: StringConst;
funcRParam: exp | stringConst;
funcRParams: funcRParam (Comma funcRParam)*;

mulExp:
	unaryExp				# mulExp_unary
	| mulExp Mul unaryExp	# mul_
	| mulExp Div unaryExp	# mul_
	| mulExp Mod unaryExp	# mul_
;

addExp:
	mulExp				# addExp_mul
	| addExp Add mulExp	# add_
	| addExp Sub mulExp	# add_
;

relExp:
	addExp				# relExp_add
	| relExp Lt addExp	    #  relExp_
	| relExp Gt addExp	#  relExp_
	| relExp Leq addExp	#  relExp_
	| relExp Geq addExp	#  relExp_
;

eqExp:
	relExp				# eqExp_rel
	| eqExp Eq relExp	# eq
	| eqExp Neq relExp	# neq;

lAndExp:
    eqExp # lAndExp_eq
    | lAndExp And eqExp # and;

lOrExp:
    lAndExp # lOrExp_lAnd
    | lOrExp Or lAndExp # or;