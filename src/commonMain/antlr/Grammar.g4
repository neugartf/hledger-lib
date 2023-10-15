grammar Grammar;

// Parser rules
file_:  transaction* EOF;


date: NUMBER '-' NUMBER '-' NUMBER;
description: (WORD | WS )+;

transaction: date WS? description EOL (WS+  default_posting|WS+  commodity_posting)+;
quantity: NUMBER;
commodity_name: WORD;
commodity:  quantity WS? commodity_name WS? '@' WS? unit_price;
default_posting: account WS? (currency amount)? (EOL|EOF);
commodity_posting: account WS? commodity? (EOL|EOF);
account: (WORD | WS | ':')+;
currency: ('$'|'€');
amount:  DASH? NUMBER;
unit_price: currency amount;

// Lexer rules

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT :      [0-9] ;
DASH: '-';
CHAR: [A-Za-z];
ASTERISK: '*';
WS :   (' ' | '\t')+ ;
EOL
   : [\r\n]+
   ;
WORD: (LOWERCASE | UPPERCASE | '_')+ ;
NUMBER  : DIGIT+ ('.' DIGIT+)? ;