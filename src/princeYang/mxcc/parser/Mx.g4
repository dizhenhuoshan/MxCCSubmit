/**
    Mx Grammar File
    Powered by ANTLR 4
    Copyright by princeYang
*/
grammar Mx;

// ===Paser===

// Declaration:
mxprogram
    : (declarations)* EOF
    ;

declarations
    : variableDeclaration | functionDeclaration | classDeclaration
    ;

variableDeclaration
    : nonVoidType Identifier ('=' expression)? ';'
    ;

    withVoidType
        : nonVoidType
        | Void
        ;

    nonVoidType
        : nonVoidType '[' ']'               # ArrayType
        | nonVoidnonArrayType               # NonArrayType
        ;

    nonVoidnonArrayType
        : Int
        | Bool
        | String
        | Identifier
        ;


functionDeclaration
    : withVoidType? Identifier '(' paramentDeclarations? ')' functionBlock
    ;

    paramentDeclarations
        : paramentDeclaration (',' paramentDeclaration)*
        ;

    paramentDeclaration
        : nonVoidType Identifier
        ;

    functionBlock
        : '{' functionStatement* '}'
        ;

classDeclaration
    : Class Identifier '{' classStatement* '}'
    ;

// Statements:
classStatement
    : variableDeclaration
    | functionDeclaration
    ;

functionStatement
    : statement                     # FuncState
    | variableDeclaration           # VarDecl
    ;

statement
    : expression ';'                # ExprState
    | loopStatement                 # LoopState
    | jumpStatement                 # JumpState
    | conditionalStatement          # CondState
    | functionBlock                 # FuncBlockState
    | ';'                           # EmptyState
    ;

    loopStatement
        : For '(' start = expression? ';' stop = expression? ';' step = expression? ')' statement   # ForState
        | While '(' expression ')' statement                                                        # WhileState
        ;

    jumpStatement
        : Return expression ? ';'           # ReturnState
        | Break ';'                         # BreakState
        | Continue ';'                      # ContinueState
        ;

    conditionalStatement
        : If '(' expression ')' thenStatement = statement (Else elseStatement = statement)?
        ;

// expression

expression
    : expression '.' (Identifier | '(' Identifier ')')              # MemeryAccessExpr
    | expression '(' paramentList? ')'                              # FunctionCallExpr
    | array = expression '[' sub = expression ']'                   # ArrayAccessExpr
    | expression op = ('++' | '--')                                 # PostFixExpr

    | New creator                                                   # NewExpr
    | <assoc = right> op = ('++' | '--') expression                 # PreFixExpr
    | <assoc = right> op = ('+' | '-') expression                   # PreFixExpr
    | <assoc = right> op = ('!' | '~') expression                   # PreFixExpr

    | lhs = expression op = ('*' | '/' | '%') rhs = expression      # BinaryExpr
    | lhs = expression op = ('+' | '-') rhs = expression            # BinaryExpr
    | lhs = expression op = ('<<' | '>>') rhs = expression          # BinaryExpr
    | lhs = expression op = ('<=' | '>=') rhs = expression          # BinaryExpr
    | lhs = expression op = ('<' | '>') rhs = expression            # BinaryExpr
    | lhs = expression op = ('==' | '!=') rhs = expression          # BinaryExpr
    | lhs = expression op = '&' rhs = expression                    # BinaryExpr
    | lhs = expression op = '^' rhs = expression                    # BinaryExpr
    | lhs = expression op = '|' rhs = expression                    # BinaryExpr
    | lhs = expression op = '&&' rhs = expression                   # BinaryExpr
    | lhs = expression op = '||' rhs = expression                   # BinaryExpr

    | <assoc = right> lhs = expression '=' rhs = expression         # AssignExpr

    | constant                                                      # ConstantExpr
    | Identifier                                                    # IdentifierExpr
    | This                                                          # ThisExpr
    | '(' expression ')'                                            # SubExpr
    ;

    constant
        : ConstIntenger     # ConstInt
        | ConstBool         # ConstBool
        | ConstString       # ConstStr
        | Null              # ConstNull
        ;

    paramentList
        : expression (',' expression)*
        ;

    creator
        : nonVoidnonArrayType ('[' expression ']')+ (Lbracket Rbracket)*    # ArrayCreator
        | nonVoidnonArrayType ('(' ')')?                                    # NonArrayCreator
        ;
// ===Lexer===

Bool: 'bool';
Int: 'int';
String: 'string';
Null: 'null';
Void: 'void';
fragment True: 'true';
fragment False: 'false';
For: 'for';
If: 'if';
Else: 'else';
While: 'while';
Break: 'break';
Continue: 'continue';
Return: 'return';
New: 'new';
Class: 'class';
This: 'this';
Lbracket: '[';
Rbracket: ']';

ConstBool
    : True
    | False
    ;

ConstIntenger
    : '0'
    | [1-9] Digit*
    ;

ConstString
    : '"' (EscapeCharacter | .)*? '"'
    ;

    fragment EscapeCharacter
        : '\\' [btnr"\\]
        ;



Identifier
    : Character (NonDigit | Digit)*
    ;

    fragment Character
        :   [a-zA-Z]
        ;

    fragment NonDigit
        :   [a-zA-Z_]
        ;

    fragment Digit
        :   [0-9]
        ;


Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;