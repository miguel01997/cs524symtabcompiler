// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.parse.*;
import invisible.jacc.util.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import java.io.IOException;
import java.io.InputStream;


/*->

  ParserGenerator contains the code to generate scanner and parser tables from
  a grammar specification.  The grammar specification is provided as a text
  file.


  GENERATING TABLES

  Generating the scanner and parser tables is a four-step process.

  First, create a ParserGenerator object.

  Second, call generate() to read the grammar specification.  The generate()
  function creates a set of data tables and stores them internally within the
  ParserGenerator object.  When you call generate(), you pass five parameters:

	- An ErrorOutput object which is used as the destination for all output,
	  including error messages.

	- A boolean which is used to select verbose mode.  In verbose mode,
	  ParserGenerator writes out a complete text representation of all the
	  generated tables.

	- An InputStream object from which ParserGenerator reads the grammar
	  specification.

	- A String object, which is used as the filename in all messages which
	  report errors in the grammar specification.  This can be null.

	- A GeneratorStatus object, which is used for sending progress reports
	  and receiving interrupt requests.  This can be null.

  The return value of generate() is a boolean which is true if an error
  occurred while reading the grammar specification.

  Third, call makeScannerTable() to create the scanner tables.  This function
  returns the resulting ScannerTable object, or null if an error occurred.

  Fourth, call makeParserTable() to create the parser tables.  This function
  returns the resulting ParserTable object, or null if an error occurred.

  Note that makeScannerTable() may be called either before or after
  makeParserTable().  Also, you can call just one of these functions if you are
  only interested in one of the two sets of tables.

  After creating the ScannerTable and ParserTable objects, you will probably
  want to save them for later use.  This can be done either by saving the
  tables to an output stream, or else by generating Java source code which
  contains the tables as array literals.  Refer to classes ScannerTable and
  ParserTable for further information.


  DEFAULT VALUES

  All information for generating the scanner and parser tables is taken from
  the grammar specification.  Some values have defaults, which are used if the
  values are not explicitly included in the grammar specification.  The default
  values are as follows:

	  Character set size: 256.

	  Grammar type: lalr1.

	  Maximum insertions during error repair: 100.

	  Maximum deletions during error repair: 200.

	  Validation length during error repair: 5.

	  Goal symbol: The left hand side of the first production.

	  Insertion cost of each terminal symbol: 1.

	  Deletion cost of each terminal symbol: 1.

	  Start conditions:  A single condition called '%%normal'.


  BOOTSTRAPPING

  ParserGenerator uses Scanner and Parser to read and parse the grammar
  specification.  This raises the question of how ParserGenerator's own scanner
  and parser tables are generated.

  Function ParserGeneratorBootstrap.bootstrap() can be used to create a
  ParserGenerator object with a set of data tables already installed within it.
  These tables contain a subset of the Jacc grammar specification.  This subset
  is sufficient to read the full Jacc grammar specification.

  Therefore, bootstrapping is a two-step process.  First, use bootstrap() to
  create a ParserGenerator object, use makeScannerTable() and makeParserTable()
  to create scanner and parser tables, and write these tables to Java source
  code.  Second, use generate() to read the Jacc grammar specification (which
  is in JaccGrammar.jacc), use makeScannerTable() and makeParserTable() to
  create scanner and parser tables, and write these new tables to Java source
  code.  These are the final tables, which are in JaccGrammarScannerTable
  and JaccGrammarParserTable.

  Class GenBootstrap contains a simple driver to use for bootstrapping.


  GRAMMAR SPECIFICATION LEXICAL STRUCTURE

  A grammar specification is written as an ASCII text file, stored one
  character per byte.  The grammar specification is first broken up into
  tokens.  Some of these token then go on to become the terminal symbols used
  in the grammar specification language.

  The following paragraphs describe the tokens.

  1. White space.  This consists of the characters space, tab, and form feed.
  Also, an ASCII sub character (0x1A) which appears as the last character of
  the file is considered to be white space.  White space is discarded, and has
  no effect except insofar as it serves to separate other tokens.

  2. Line end.  This consists of carriage return, line feed, or the pair
  carriage return followed by line feed.  Line feeds are counted and then
  discarded.

  3. Comment.  There are two forms of comment:
  
	(a) The characters '/' '/', followed by all characters up to the next line
	end.
	
	(b) An arbitrary string of characters introduced by '/' '*' and terminated
	by '*' '/'.
	
  Comments are discarded.

  4. Keyword.  A keyword consists of the character '%' followed by a string of
  letters and digits.  The keywords listed below are valid.  All other keywords
  are invalid and will be flagged as errors.

		%tokens
		%categories
		%conditions
		%terminals
		%productions
		%shift
		%reduce
		%options
		%repair
		%lr1
		%plr1
		%lalr1
		%any
		%none
		%unicode
		%uppercase
		%lowercase
		%titlecase
		%letter
		%digit
		%charsetsize
		%goal
		%java

  5. Operator.  The following character strings are recognized as operators.

		-
		~
		&
		@
		*
		+
		?
		/
		:
		;
		=
		(
		)
		{
		}
		|
		#
		.
		->
		..

  6. Number.  There are two forms of number:

    (a) A series of decimal digits, which are interpreted as a decimal number.
	The decimal digits are: 0 1 2 3 4 5 6 7 8 9.

    (b) The characters '0' 'x' or '0' 'X', followed by one or more hexadecimal
	digits, which are interpreted as a hexadecimal number.  The hexadecimal
	digits are: 0 1 2 3 4 5 6 7 8 9 a b c d e f A B C D E F.

  7. Identifier.  There are two forms of identifier:

    (a) A letter, followed by a series of zero or more letters and digits.
	The characters '_' and '$' count as letters.

    (b) A string of characters, introduced by a single quote and terminated by
	a single quote.  The string may contain any characters except space, tab,
	form feed, carriage return, line feed, and single quote;  except that the
	first and/or last characters of the string may be a single quote.

  If an identifier happens to consist of a letter followed by a string of
  letters and digits, then it can be written either with or without quotes.
  The quoted and unquoted forms refer to the same identifier, and may be used
  interchangeably.  For example, the identifiers AB9C and 'AB9C' are identical
  and may be used interchangeably.

  The rule that allows a single quote as the first and/or last character of an
  identifier lets you create identifiers that occur commonly in programming
  languages.  For example:

		'''    is an identifier consisting of one single quote character.

		''''   is an identifier consisting of two single quote characters.

		'\''   is an identifier consisting of a backslash followed by one
		       single quote character.

		'a'b'  is invalid (because the single quote is neither the first nor
		       the last character of the string).

  Notice that there are no escape sequences for characters within an
  identifier.  This is because it is often necessary to define identifiers
  that are escape sequences in the grammar being defined, and it would be too
  confusing to use Jacc escape sequences to describe the grammar's escape
  sequences.  With these rules, anything appearing between single quotes is
  exactly as it appears in the language text.


  GRAMMAR SPECIFICATION LANGUAGE

  After the grammar specification is broken into tokens, the tokens are then
  parsed according to the Jacc language.  The Jacc language is defined by a set
  of productions, as shown below.

		Goal -> SectionList

		SectionList -> Section

		SectionList -> SectionList Section

		Section -> OptionHeader OptionDefList

		Section -> TerminalHeader TerminalDefList

		Section -> ProductionHeader ProductionDefList

		Section -> CategoryHeader CategoryDefList

		Section -> ConditionHeader ConditionDefList

		Section -> TokenHeader TokenDefList

		OptionHeader -> '%options' ':'

		OptionDefList ->

		OptionDefList -> OptionDefList OptionDefinition

		OptionDefinition -> '%lr1' ';'

		OptionDefinition -> '%plr1' ';'

		OptionDefinition -> '%lalr1' ';'

		OptionDefinition -> '%repair' MaxInsertions MaxDeletions ValidationLength ';'

		OptionDefinition -> '%charsetsize' CharSetSize ';'

		OptionDefinition -> '%goal' Symbol ';'

		OptionDefinition -> '%java' JavaName ';'

		MaxInsertions -> number

		MaxDeletions -> number

		ValidationLength -> number

		CharSetSize -> number

		Symbol -> identifier

		JavaName -> JavaIdentifier

		JavaName -> JavaName '.' JavaIdentifier

		JavaIdentifier -> identifier

		TerminalHeader -> '%terminals' ':'

		TerminalDefList ->

		TerminalDefList -> TerminalDefList TerminalDefinition

		TerminalDefinition -> Symbol ';'

		TerminalDefinition -> Symbol InsertionCost DeletionCost ';'

		InsertionCost -> number

		DeletionCost -> number

		ProductionHeader -> '%productions' ':'

		ProductionDefList ->

		ProductionDefList -> ProductionDefList ProductionDefinition

		ProductionDefinition -> Symbol LinkName Parameter '->' SymbolList ProductionPrec ';'

		LinkName ->

		LinkName -> '{' identifier '}'

		Parameter ->

		Parameter -> '#' number

		SymbolList ->

		SymbolList -> SymbolList Symbol

		ProductionPrec ->

		ProductionPrec -> ProductionPrec '%shift' SymbolSet

		ProductionPrec -> ProductionPrec '%reduce' SymbolSet

		SymbolSet ->

		SymbolSet -> SymbolSet Symbol

		CategoryHeader -> '%categories' ':'

		CategoryDefList ->

		CategoryDefList -> CategoryDefList CategoryDefinition

		CategoryDefinition -> Category '=' CatExp ';'

		Category -> identifier

		CatExp -> number

		CatExp -> identifier

		CatExp -> number '..' number

		CatExp -> identifier '..' identifier

		CatExp -> '%any'

		CatExp -> '%none'

		CatExp -> '%unicode'

		CatExp -> '%uppercase'

		CatExp -> '%lowercase'

		CatExp -> '%titlecase'

		CatExp -> '%letter'

		CatExp -> '%digit'

		CatExp -> '(' CatExp ')'

		CatExp -> CatExp '-' CatExp

		CatExp -> CatExp '&' CatExp

		CatExp -> CatExp '|' CatExp

		ConditionHeader -> '%conditions' ':'

		ConditionDefList ->

		ConditionDefList -> ConditionDefList ConditionDefinition

		ConditionDefinition -> Condition ';'

		Condition -> identifier

		TokenHeader -> '%tokens' ConditionSet ':'

		ConditionSet ->

		ConditionSet -> ConditionSet Condition

		TokenDefList ->

		TokenDefList -> TokenDefList TokenDefinition

		TokenDefinition -> Token LinkName Parameter '=' RegExp ';'

		TokenDefinition -> Token LinkName Parameter '=' RegExp '/' RegExp ';'

		Token -> identifier

		RegExp -> Category

		RegExp -> '(' RegExp ')'

		RegExp -> RegExp '*'

		RegExp -> RegExp '+'

		RegExp -> RegExp '?'

		RegExp -> RegExp RegExp

		RegExp -> RegExp '-' RegExp

		RegExp -> RegExp '&' RegExp

		RegExp -> RegExp '~' RegExp

		RegExp -> RegExp '@' RegExp

		RegExp -> RegExp '|' RegExp


  A grammar specification consists of a series of sections.  Each section is
  introduced by a keyword followed by a colon (except for a %tokens section,
  whose introduction can also contain a list of start conditions).  There are
  six types of sections:

	- An %options section specifies general options.

	- A %terminals section lists the terminal symbols of the grammar.

	- A %productions section lists the productions of the grammar.

	- A %categories section defines character categories for the scanner.

	- A %conditions section lists start conditions of the scanner.

	- A %tokens sections defines the tokens recognized by the scanner.

  Sections may appear in any order, and there can be more than one section of
  a given type.

  In addition to the productions, the following rules and constraints apply:

  1. If there is no %goal statement, then the left hand side of the first
  production is used as the goal symbol.  There can be at most one %goal
  statement.

  2. When generating error repair tables, if two productions have the same cost
  then priority is given to the production that appears earliest.

  3. Except as stated in rules 1 and 2, the order in which productions appear
  is immaterial.

  4. When generating error repair tables, if two terminal symbols have the same
  insertion cost, then priority is given to the symbol which first appears
  earliest.

  5. Except as stated in rule 4, the order in which terminal symbols appear is
  immaterial.

  6. Every production has a link name, which is an identifier.  The link name,
  together with the production's left hand side, is used to select which
  nonterminal factory to use when that production is reduced.  If two
  productions have the same left hand side and the same link name, then the
  same nonterminal factory is used for both productions.  (Notice that it makes
  no difference whether or not the link name is also used as the name of a
  symbol;  in other words, link names live in a separate name space.)

  7. A production's link name may be specified by writing an identifier
  enclosed in braces immediately after the left hand side.  If not specified
  explicitly, a production's link name defaults to an empty string.

  8. Every production has a parameter, which is an integer.  The parameter is
  passed to the nonterminal factory whenever the production is reduced.  The
  meaning of the parameter is client-defined.  One possible use for the
  parameter is to distinguish between different productions that have the same
  nonterminal factory.

  9. A production's parameter may be specified by writing a pound sign followed
  by a number, immediately before the arrow.  (Notice that if both a link name
  and a parameter are specified, the link name must appear first.)  If not
  specified explicitly, a production's parameter defaults to zero.

  10. A production may optionally have one or more %shift or %reduce clauses,
  appearing after the right hand side.  Each %shift or %reduce clause contains
  a (possibly empty) list of symbols, which may include both terminal and
  nonterminal symbols.  Listing a nonterminal symbol X is equivalent to listing
  every terminal symbol that could be the first symbol of an expansion of X.

  11. If a shift-reduce conflict is encountered, it is resolved as follows.
  Let y be the input symbol (the "shift") and let p be the production (the
  "reduce").  If y appears in a %shift clause of p, and does not appear in any
  %reduce clause of p, then the conflict is resolved in favor of shift.  If y
  appears in a %reduce clause of p, and does not appear in any %shift clause of
  p, then the conflict is resolved in favor of reduce.  Otherwise, the conflict
  is not resolved and an error is reported.

  12. It is not an error for a terminal symbol y to appear in both a %shift
  clause and a %reduce clause in the same production, either explicitly or
  implicitly.  ("Explicitly" means that the clause includes y.  "Implicitly"
  means that the clause includes a nonterminal symbol X, and y can be the first
  symbol of an expansion of X.)  However, in this case a shift-reduce conflict
  involving y is not resolved, and an error is reported if such a conflict
  occurs.  The purpose of this rule is to make it easier to use nonterminal
  symbols in %shift and %reduce clauses.

  13. If a reduce-reduce conflict is encountered, an error is reported.

  14. If the default nonterminal factory is used for a production, the value of
  the left hand side is determined as follows:  (a) If the right hand side is
  nonempty, the value is the value of the first symbol on the right hand side.
  (b) If the right hand side is empty, the value is null.

  15. Every terminal symbol must be listed exactly once in a %terminals
  section.

  16. Every nonterminal symbol must appear on the left hand side of at least
  one production.

  17. Every symbol (terminal or nonterminal) must be reachable from the goal
  symbol.

  18. Every nonterminal symbol must have an expansion that produces either a
  string of terminal symbols, or the empty string.

  19. A terminal symbol may optionally have an insertion cost and a deletion
  cost. Each cost can range from 1 to 1000. If the costs are omitted, they each
  default to 1.

  20. The %repair statement can be used to specify the maximum number of
  insertions, maximum number of deletions, and validation length used for error
  repair.  Each of these three parameters can range from 0 to 1000.  If no
  error repair at all is desired, then set all three numbers to 0.  If there is
  no %repair statement, then the default is 100 insertions, 200 deletions, and
  a validation length of 5.  There can be at most one %repair statement.

  21. The %lalr1, %plr1, and %lr1 statements can be used to specify the type of
  grammar to generate.  At most one of these statements can appear.  If none
  appear, the default is LALR(1).

  22. The %charsetsize statement can be used to specify the size of the
  character set recognized by the scanner.  Typically, this is 0x100 for the
  ASCII character set, or 0x10000 for the Unicode character set.  However, any
  value between 2 and 65536 is allowed.  There can be at most one %charsetsize
  statement.  If omitted, it defaults to 256.

  23. The order in which category definitions appear is immaterial.

  24. Each category definition must have a unique name, which appears on the
  left hand side of the equal sign.

  25. A category expression (CatExp in the grammer) represents a set of
  characters.  It may be written in the following forms:

	number						The character whose character code is the given
	                            number.

	identifier					All characters which appear in the given
	                            identifier.

	number .. number			All characters whose character code is greater
	                            than or equal to the first number, and less
								than or equal to the second number.

	identifier .. identifier	All characters greater than or equal to the
	                            first identifier, and less than or equal to the
								second identifier.  Each identifier must be
								exactly one character long.

	%any						Any character.

	%none						No characters.

	%unicode					Any defined Unicode character.

	%uppercase					Any uppercase Unicode letter.

	%lowercase					Any lowercase Unicode letter.

	%titlecase					Any titlecase Unicode letter.

	%letter						Any Unicode letter.

	%digit						Any Unicode digit.

	CatExp - CatExp				Any character which is included in the first
								expression, but not included in the second
								expression.

	CatExp & CatExp				Any character which is included both in the
								first expression, and in the second expression.

	CatExp | CatExp				Any character which is included either in the
								first expression, or in the second expression.

  Category expression operators are organized into two precedence levels as
  follows.  Within a precedence level, operators group from left to right.

	highest:	-  &
	lowest:		|

  Parentheses may be used in category expressions.
  
  26. Conditions are given consecutive numbers starting with 0, in the order
  they appear within the %conditions section.  Therefore, the first condition
  listed in the %conditions section is the initial condition.

  27. The header of a %tokens section may optionally include a list of
  conditions, between the %tokens keyword and the colon.  If the list is
  included, then token definitions appearing within that section are effective
  only for the specified conditions.  If the list is absent, then token
  definitions appearing within that section are effective for all conditions.

  28. The order in which token definitions appear is significant.  If a string
  matches more than one token definition, then priority is given to the token
  definition that appears earliest in the file.

  29. Every token definition has a link name, which is an identifier.  The link
  name, together with the token name, is used to select which token factory to
  use when that token definition is recognized.  If two token definitions have
  the same token name and the same link name, then the same token factory is
  used for both token definitions.  (Notice that it makes no difference whether
  or not the link name is also used as the name of a symbol;  in other words,
  link names live in a separate name space.)

  30. A token definition's link name may be specified by writing an identifier
  enclosed in braces immediately after the token name.  If not specified
  explicitly, a token definition's link name defaults to an empty string.

  32. Every token definition has a parameter, which is an integer.  The
  parameter is passed to the token factory whenever the token definition is
  recognized.  The meaning of the parameter is client-defined.  Typically, the
  parameter is the number of the token that the token factory creates.

  33. If a token name is the same as the name of a terminal symbol, then the
  parameter is automatically set equal to the numerical value of the terminal
  symbol.  In this case, there must not be an explicit parameter in the token
  definition.

  34. If a token name is not the same as the name of a terminal symbol, then
  there may be an explicit parameter in the token defintion.  The parameter
  is written as a pound sign followed by a number, appearing immediately before
  the equal sign.  (Notice that if both a link name and a parameter are
  specified, the link name must appear first.)

  35. If a token name is not the same as the name of a terminal symbol, and
  there is no explicit parameter, then the token definition's parameter
  defaults to zero.

  36. It is an error for a token name to be the same as the name of a
  nonterminal symbol.

  37. The right hand side of a token definition may be written in two forms:

	RegExp						The token consists of a string matching the
								regular expression.  When the scanner
								recognizes this token, both the token text and
								the context text contain the matching string.

	RegExp / RegExp				The token consists of a string matching the
								first regular expression, but only when
								followed by a string matching the second
								regular expression.  When the scanner
								recognizes this token, the token text contains
								the string that matches the first expression,
								and the context text contains the string that
								matches the catenation of the first and second
								expressions.  The second expression is called
								the "right context".

  The right hand side of a token definition never matches the empty string,
  even if the regular expression is written in a way that seems to match the
  empty string.

  38. A regular expression (RegExp in the grammer) represents a set of strings.
  It may be written in the following forms:

	identifier					Matched by any one-character string, whose
								single character belongs to the specified
								category.  The identifier must be the name of a
								character category.

	RegExp *					Kleene closure.  Matched by the catenation of
								zero or more strings, each of which matches the
								specified regular expression.

	RegExp +					Positive closure.  Matched by the catenation of
								one or more strings, each of which matches the
								specified regular expression.

	RegExp ?					Optional closure.  Matched by the empty string,
								and by any string which matches the specified
								regular expression.

	RegExp RegExp				Catenation.  Matched by any string that
								consists of a string that matches the first
								expression, followed by a string that matches
								the second expression.

	RegExp - RegExp				Difference.  Matched by any string which
								matches the first expression, but does not
								match the second expression.

	RegExp & RegExp				Intersection.  Matched by any string which
								matches the first expression, and also matches
								the second expression.

	RegExp ~ RegExp				Exclusion.  Matched by any string which
								matches the first expression, but does not
								contain any substring that matches the second
								expression.

	RegExp @ RegExp				Inclusion.  Matched by any string which
								matches the first expression, and also contains
								at least one substring that matches the second
								expression.

	RegExp | RegExp				Alternation.  Matched by any string which
								either matches the first expression, or matches
								the second expression.

  Regular expression operators are organized into four precedence levels as
  follows.  Within a precedence level, operators group from left to right.

	highest:	*  +  ?
	second:		catenation
	third:		-  &  ~  @
	lowest:		|

  Parentheses may be used in regular expressions.


  JACC GRAMMAR SPECIFICTION

  The following is the complete specification for the Jacc language, written in
  the Jacc language.
  
  A functionally equivalent specification can be found in JaccGrammar.jacc.
  (This version has extra white space and no error correction information, to
  make it more easily adaptable for bootstrapping.)


  %terminals :

  '-' ;
  '~' ;
  '&' ;
  '@' ;
  '*' ;
  '+' ;
  '?' ;
  '/' ;
  ':' ;
  ';' ;
  '=' ;
  '(' ;
  ')' ;
  '{' ;
  '}' ;
  '|' ;
  '#' ;
  '.' ;
  '->' ;
  '..' ;

  '%tokens' ;
  '%categories' ;
  '%conditions' ;
  '%terminals' ;
  '%productions' ;
  '%shift' ;
  '%reduce' ;
  '%options' ;
  '%repair' ;
  '%lr1' ;
  '%plr1' ;
  '%lalr1' ;
  '%any' ;
  '%none' ;
  '%unicode' ;
  '%uppercase' ;
  '%lowercase' ;
  '%titlecase' ;
  '%letter' ;
  '%digit' ;
  '%charsetsize' ;
  '%goal' ;
  '%java' ;

  identifier ;
  number ;

  %productions :

  Goal -> SectionList ;

  SectionList -> Section ;

  SectionList -> SectionList Section ;

  Section -> OptionHeader OptionDefList ;

  Section -> TerminalHeader TerminalDefList ;

  Section -> ProductionHeader ProductionDefList ;

  Section -> CategoryHeader CategoryDefList ;

  Section -> ConditionHeader ConditionDefList ;

  Section -> TokenHeader TokenDefList ;

  OptionHeader -> '%options' ':' ;

  OptionDefList -> ;

  OptionDefList -> OptionDefList OptionDefinition ;

  OptionDefinition { LR1 } -> '%lr1' ';' ;

  OptionDefinition { PLR1 } -> '%plr1' ';' ;

  OptionDefinition { LALR1 } -> '%lalr1' ';' ;

  OptionDefinition { repair } -> '%repair' MaxInsertions MaxDeletions ValidationLength ';' ;

  OptionDefinition { charsetsize } -> '%charsetsize' CharSetSize ';' ;

  OptionDefinition { goal } -> '%goal' Symbol ';' ;

  OptionDefinition { java } -> '%java' JavaName ';' ;

  MaxInsertions -> number ;

  MaxDeletions -> number ;

  ValidationLength -> number ;

  CharSetSize -> number ;

  Symbol -> identifier ;

  JavaName { simple } -> JavaIdentifier ;

  JavaName { qualified } -> JavaName '.' JavaIdentifier ;

  JavaIdentifier -> identifier ;

  TerminalHeader -> '%terminals' ':' ;

  TerminalDefList -> ;

  TerminalDefList -> TerminalDefList TerminalDefinition ;

  TerminalDefinition { defaultCost } -> Symbol ';' ;

  TerminalDefinition { withCost } -> Symbol InsertionCost DeletionCost ';' ;

  InsertionCost -> number ;

  DeletionCost -> number ;

  ProductionHeader -> '%productions' ':' ;

  ProductionDefList -> ;

  ProductionDefList -> ProductionDefList ProductionDefinition ;

  ProductionDefinition -> Symbol LinkName Parameter '->' SymbolList ProductionPrec ';' ;

  LinkName {empty} -> ;

  LinkName {identifier} -> '{' identifier '}' ;

  Parameter {empty} -> ;

  Parameter {number} -> '#' number ;

  SymbolList { empty } -> ;

  SymbolList { append } -> SymbolList Symbol ;

  ProductionPrec { empty } -> ;

  ProductionPrec { appendShift } -> ProductionPrec '%shift' SymbolSet ;

  ProductionPrec { appendReduce } -> ProductionPrec '%reduce' SymbolSet ;

  SymbolSet { empty } -> ;

  SymbolSet { append } -> SymbolSet Symbol ;

  CategoryHeader -> '%categories' ':' ;

  CategoryDefList -> ;

  CategoryDefList -> CategoryDefList CategoryDefinition ;

  CategoryDefinition -> Category '=' CatExp ';' ;

  Category -> identifier ;

  CatExp { number } -> number ;

  CatExp { identifier } -> identifier ;

  CatExp { numberRange } -> number '..' number ;

  CatExp { identifierRange } -> identifier '..' identifier ;

  CatExp { any } -> '%any' ;

  CatExp { none } -> '%none' ;

  CatExp { unicode } -> '%unicode' ;

  CatExp { uppercase } -> '%uppercase' ;

  CatExp { lowercase } -> '%lowercase' ;

  CatExp { titlecase } -> '%titlecase' ;

  CatExp { letter } -> '%letter' ;

  CatExp { digit } -> '%digit' ;

  CatExp { paren } -> '(' CatExp ')' ;

  CatExp { difference } -> CatExp '-' CatExp
	%shift %reduce '-' '&' '|' ;

  CatExp { intersection } -> CatExp '&' CatExp
	%shift %reduce '-' '&' '|' ;

  CatExp { union } -> CatExp '|' CatExp
	%shift '-' '&' %reduce '|' ;

  ConditionHeader -> '%conditions' ':' ;

  ConditionDefList -> ;

  ConditionDefList -> ConditionDefList ConditionDefinition ;

  ConditionDefinition -> Condition ';' ;

  Condition -> identifier ;

  TokenHeader -> '%tokens' ConditionSet ':' ;

  ConditionSet { empty } -> ;

  ConditionSet { append } -> ConditionSet Condition ;

  TokenDefList -> ;

  TokenDefList -> TokenDefList TokenDefinition ;

  TokenDefinition { noContext } -> Token LinkName Parameter '=' RegExp ';' ;

  TokenDefinition { rightContext } -> Token LinkName Parameter '=' RegExp '/' RegExp ';' ;

  Token -> identifier ;

  RegExp { oneChar } -> Category ;

  RegExp { paren } -> '(' RegExp ')' ;

  RegExp { KleeneClosure } -> RegExp '*' ;

  RegExp { positiveClosure } -> RegExp '+' ;

  RegExp { optionalClosure } -> RegExp '?' ;

  RegExp { catenation } -> RegExp RegExp
	%shift '*' '+' '?' %reduce RegExp '-' '&' '~' '@' '|' ;

  RegExp { difference } -> RegExp '-' RegExp
	%shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|' ;

  RegExp { intersection } -> RegExp '&' RegExp
	%shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|' ;

  RegExp { excluding } -> RegExp '~' RegExp
	%shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|' ;

  RegExp { including } -> RegExp '@' RegExp
	%shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|' ;

  RegExp { alternation } -> RegExp '|' RegExp
	%shift '*' '+' '?' RegExp '-' '&' '~' '@' %reduce '|' ;

  %categories :

  a = 'a' ;
  b = 'b' ;
  c = 'c' ;
  d = 'd' ;
  e = 'e' ;
  f = 'f' ;
  g = 'g' ;
  h = 'h' ;
  i = 'i' ;
  j = 'j' ;
  k = 'k' ;
  l = 'l' ;
  m = 'm' ;
  n = 'n' ;
  o = 'o' ;
  p = 'p' ;
  q = 'q' ;
  r = 'r' ;
  s = 's' ;
  t = 't' ;
  u = 'u' ;
  v = 'v' ;
  w = 'w' ;
  x = 'x' ;
  y = 'y' ;
  z = 'z' ;
  '%' = '%' ;
  ':' = ':' ;
  ';' = ';' ;
  '*' = '*' ;
  '+' = '+' ;
  '?' = '?' ;
  '-' = '-' ;
  '>' = '>' ;
  '~' = '~' ;
  '&' = '&' ;
  '@' = '@' ;
  '(' = '(' ;
  ')' = ')' ;
  '{' = '{' ;
  '}' = '}' ;
  ''' = ''' ;
  '/' = '/' ;
  '=' = '=' ;
  '|' = '|' ;
  '#' = '#' ;
  '.' = '.' ;
  '0' = '0' ;
  '1' = '1' ;
  letter = %letter | '_$' ;
  digit = %digit ;
  letterOrDigit = %letter | '_$' | %digit ;
  space = 9 | 12 | 32 ;
  white = 9 | 10 | 12 | 13 | 32 ;
  decDigit = '0' .. '9' ;
  hexDigit = '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' ;
  xX = 'xX' ;
  notQuote = %any - ''' - 9 - 10 - 12 - 13 - 32 ;
  cr = 13 ;
  lf = 10 ;
  sub = 26 ;
  notEol = %any - 10 - 13 ;
  any = %any ;

  %conditions :

  notInComment ;
  inComment ;

  %tokens notInComment :

  '-' = '-' ;
  '~' = '~' ;
  '&' = '&' ;
  '@' = '@' ;
  '*' = '*' ;
  '+' = '+' ;
  '?' = '?' ;
  '/' = '/' ;
  ':' = ':' ;
  ';' = ';' ;
  '=' = '=' ;
  '{' = '{' ;
  '}' = '}' ;
  '|' = '|' ;
  '#' = '#' ;
  '.' = '.' ;
  '->' = '-' '>' ;
  '..' = '.' '.' ;

  '%tokens' = '%' t o k e n s ;
  '%categories' = '%' c a t e g o r i e s ;
  '%conditions' = '%' c o n d i t i o n s ;
  '%terminals' = '%' t e r m i n a l s ;
  '%productions' = '%' p r o d u c t i o n s ;
  '%shift' = '%' s h i f t ;
  '%reduce' = '%' r e d u c e ;
  '%options' = '%' o p t i o n s ;
  '%repair' = '%' r e p a i r ;
  '%lr1' = '%' l r '1' ;
  '%plr1' = '%' p l r '1' ;
  '%lalr1' = '%' l a l r '1' ;
  '%any' = '%' a n y ;
  '%none' = '%' n o n e ;
  '%unicode' = '%' u n i c o d e ;
  '%uppercase' = '%' u p p e r c a s e ;
  '%lowercase' = '%' l o w e r c a s e ;
  '%titlecase' = '%' t i t l e c a s e ;
  '%letter' = '%' l e t t e r ;
  '%digit' = '%' d i g i t ;
  '%charsetsize' = '%' c h a r s e t s i z e ;
  '%goal' = '%' g o a l ;
  '%java' = '%' j a v a ;

  unknownKeyword = '%' letterOrDigit * ;

  number { decimal } = decDigit + ;

  number { hex } = '0' xX hexDigit + ;

  number { illegal } = decDigit + letter letterOrDigit * ;

  identifier { unquoted } = letter letterOrDigit * ;

  identifier { quoted } = ''' ''' ? notQuote * ''' ? ''' ;

  whiteSpace = ( space | '/' '*' ( notEol * ~ '*' '/' ) '*' '/' ) *
               ( '/' '/' notEol * ) ? ;

  beginComment = ( space | '/' '*' ( notEol * ~ '*' '/' ) '*' '/' ) *
				 '/' '*' ( notEol * ~ '*' '/' ) ;

  illegalChar = sub / any ;

  whiteSpace = sub ;

  %tokens inComment :

  endComment = ( notEol * ~ '*' '/' ) '*' '/'
               ( space | '/' '*' ( notEol * ~ '*' '/' ) '*' '/' ) *
               ( '/' '/' notEol * ) ? ;

  whiteSpace = ( ( notEol * ~ '*' '/' ) '*' '/'
			     ( space | '/' '*' ( notEol * ~ '*' '/' ) '*' '/' ) *
			     '/' '*'
			   ) ?
			   ( notEol * ~ '*' '/' ) ;

  %tokens :

  lineEnd = cr | lf | cr lf ;

  illegalChar = any ;


->*/


public class ParserGenerator implements ScannerClient, ParserClient
{

	// This flag enables the use of debug token and nonterminal factories.

	static final boolean _debug = false;


	// ------ Reserved Names and Constants ------


	// The name of the internally-generated end-of-file symbol

	public static final String internalEOFSymbolName = "%%EOF";

	// The name of the internally-generated goal symbol

	public static final String internalGoalSymbolName = "%%Goal";

	// The name of the internally-generated default condition

	public static final String internalDefaultCondition = "%%normal";

	// Internal offset for start conditions

	public static final int internalStartConditionOffset = 0x40000000;

	// Internal offset for context split numbers

	public static final int internalContextSplitOffset = 0x40000000;


	// ------ Symbol Tables ------


	// This hash table maps symbol names to symbol definitions.  Each key is a
	// String object giving the name of a symbol.  Each element is a
	// ParserGeneratorSymbol object giving the definition of the symbol.
	//
	// createTerminal() creates a new terminal symbol.  It returns null if the
	// symbol was already defined as a terminal symbol.  If the symbol was already
	// defined as a nonterminal, it is converted to a terminal.
	//
	// getSymbol() returns an existing symbol, or null if the symbol is not defined.
	//
	// getOrCreateSymbol() returns an existing symbol, or creates a new nonterminal
	// symbol if the symbol is not already defined.

	int _symbolCount;

	private Hashtable _symbolTable;


	ParserGeneratorSymbol createTerminal (String name, int insertionCost, int deletionCost)
	{
		ParserGeneratorSymbol value = (ParserGeneratorSymbol) _symbolTable.get (name);

		if (value != null)
		{
			if (value._isTerminal)
			{
				return null;
			}

			value._isTerminal = true;
			value._insertionCost = insertionCost;
			value._deletionCost = deletionCost;

			return value;
		}

		value = new ParserGeneratorSymbol (
			name, _symbolCount++, true, insertionCost, deletionCost );
		_symbolTable.put (name, value);

		return value;
	}


	ParserGeneratorSymbol getSymbol (String name)
	{
		ParserGeneratorSymbol value = (ParserGeneratorSymbol) _symbolTable.get (name);

		return value;
	}


	ParserGeneratorSymbol getOrCreateSymbol (String name)
	{
		ParserGeneratorSymbol value = (ParserGeneratorSymbol) _symbolTable.get (name);

		if (value == null)
		{
			value = new ParserGeneratorSymbol (name, _symbolCount++, false, 0, 0);
			_symbolTable.put (name, value);
		}

		return value;
	}


	// This set contains the productions.  Each element is a ParserGeneratorProduction
	// object.
	//
	// createProduction() creates a new empty production object.

	int _productionCount;

	private ObjectSet _productionTable;


	ParserGeneratorProduction createProduction ()
	{
		ParserGeneratorProduction value = new ParserGeneratorProduction (_productionCount++);
		_productionTable.addElement (value);

		return value;
	}


	// This hash table maps category names to category definitions for
	// user-defined categories.  Each key is a String object giving the name
	// of a category.  Each element is a ParserGeneratorCategory object giving
	// the definition of the category.
	//
	// createCategory() creates a new empty category object.  It returns null
	// if a category with the specified name already exists.
	//
	// getCategory() returns an existing category, or null if the category is
	// not defined.

	int _categoryCount;

	private Hashtable _categoryTable;


	ParserGeneratorCategory createCategory (String name, UnaryIntPredicate predicate)
	{
		ParserGeneratorCategory value = (ParserGeneratorCategory) _categoryTable.get (name);

		if (value != null)
		{
			return null;
		}

		value = new ParserGeneratorCategory (_categoryCount++, name, predicate);
		_categoryTable.put (name, value);

		return value;
	}


	ParserGeneratorCategory getCategory (String name)
	{
		ParserGeneratorCategory value = (ParserGeneratorCategory) _categoryTable.get (name);

		return value;
	}


	// This set contains the tokens.  Each element is a ParserGeneratorToken
	// object.
	//
	// createToken() creates a new token object.

	int _tokenCount;

	int _contextCount;

	private ObjectSet _tokenTable;


	ParserGeneratorToken createToken (String name, String linkName, Integer parameter,
		ParserGeneratorRegExp tokenExp, ParserGeneratorRegExp contextExp)
	{
		ParserGeneratorToken value = new ParserGeneratorToken (_tokenCount++, _contextCount,
			name, linkName, parameter, tokenExp, contextExp, _currentConditionSet );

		_tokenTable.addElement (value);

		if (contextExp != null)
		{
			++_contextCount;
		}

		return value;
	}


	// This hash table maps condition names to condition definitions for
	// user-defined conditions.  Each key is a String object giving the name
	// of a condition.  Each element is a ParserGeneratorCondition object giving
	// the definition of the condition.
	//
	// createCondition() creates a new condition object.  It returns null
	// if a condition with the specified name already exists.
	//
	// getCondition() returns an existing condition, or null if the condition is
	// not defined.

	int _conditionCount;

	private Hashtable _conditionTable;


	ParserGeneratorCondition createCondition (String name)
	{
		ParserGeneratorCondition value = (ParserGeneratorCondition) _conditionTable.get (name);

		if (value != null)
		{
			return null;
		}

		value = new ParserGeneratorCondition (_conditionCount++, name);
		_conditionTable.put (name, value);

		return value;
	}


	ParserGeneratorCondition getCondition (String name)
	{
		ParserGeneratorCondition value = (ParserGeneratorCondition) _conditionTable.get (name);

		return value;
	}


	// ------ Global variables used during the parse ------


	// This flag is true if we have seen a repair option

	boolean _seenRepairOption;

	// This flag is true if we have seen a grammar option

	boolean _seenGrammarOption;

	// This flag is true if we have seen a character set size option

	boolean _seenCharSetSizeOption;

	// This flag is true if we have seen a goal symbol option

	boolean _seenGoalOption;

	// This flag is true if we have seen a java name option

	boolean _seenJavaNameOption;

	// The internally-generated end-of-file symbol

	ParserGeneratorSymbol _internalEOFSymbol;

	// The internally-generated goal symbol, or null if not created yet

	ParserGeneratorSymbol _internalGoalSymbol;

	// The user's goal symbol, or null if unspecified

	ParserGeneratorSymbol _userGoalSymbol;

	// The numerical value of the end-of-file symbol

	int _eofSymbol;

	// The numerical value of the goal production

	int _goalProduction;

	// The selected grammar type

	int _grammarType;

	static final int defaultGrammarType = LRMachine.LALR1;

	// The selected repair options

	int _maxInsertion;

	int _maxDeletion;

	int _validationLength;

	static final int defaultMaxInsertion = 100;

	static final int defaultMaxDeletion = 200;

	static final int defaultValidationLength = 5;

	// The selected char set size option

	int _charSetSize;

//	static final int defaultCharSetSize = CharCategoryTable.charSetSizeUnicode;
	static final int defaultCharSetSize = CharCategoryTable.charSetSizeASCII;
	
	// The Java name for the grammar, or null if not specified
	
	String _javaName;

	// True if an error has been detected that prevents table generation

	boolean _error;
	
	// The error flags to report to _generatorStatus when an error is detected
	
	int _errorFlags;
	
	// If we're interrupted during the parse, this is the exception object
	
	InterruptedCompilerException _interruptException;

	// A set of String objects that holds the conditions currently in effect
	// for generating tokens, or null to select all conditions

	ObjectSet _currentConditionSet;


	// ----- Generated Parser Tables ------

	
	// Names of symbols, in form required by ContextFreeGrammar

	String[] _symbols;
	
	// Productions, in form required by ContextFreeGrammar

	int[][] _productions;

	// Insertion costs of symbols

	int[] _insertionCost;

	// Deletion costs of symbols

	int[] _deletionCost;

	// Parameters of productions

	int[] _productionParam;

	// Link names of productions

	String[] _productionLink;

	// The left hand side of each production

	int[] _productionLHSSymbol;

	// The length of the right hand side of each production

	int[] _productionRHSLength;

	// The context-free grammar

	ContextFreeGrammar _CFG;

	// The cost of each production

	long[] _productionCost;

	private static final long infiniteProductionCost = 0x7FFFFFFFFFFFFFFFL;

	// The shift set of each production

	IntSet[] _shiftSet;

	// The reduce set of each production

	IntSet[] _reduceSet;

	// The table of single-point insertions

	int _singlePointInsertionCount;

	int[] _singlePointInsertions;

	// The LR(1) machine

	LRMachine _machine;

	// The parser action table

	short[][] _actionTable;

	// The parser unwinding table

	int[] _unwindingTable;

	// The number of parser states

	int _stateCount;

	// Table mapping parser states to cognate numbers

	int _cognateNumber[];
	
	// Number of passes required to calculate production costs
	
	int _productionCostPassCount;


	// ----- Generated Scanner Tables -----


	// The names of character categories

	String[] _categoryNames;

	// The predicate for each category

	UnaryIntPredicate[] _charGroups;

	// The calculated category sets

	IntSet[] _calculatedCategorySets;

	// The character category table

	CharCategoryTable _CCT;

	// The calculated category table, from _CCT.

	byte[] _calculatedCategoryTable;

	// The calculated category count, from _CCT.

	int _calculatedCategoryCount;

	// The names of start conditions

	String[] _conditionNames;

	// The names of tokens

	String[] _tokenNames;

	// The parameters of tokens

	int [] _tokenParam;

	// The link names of tokens

	String [] _tokenLink;

	// Context number table

	int[] _contextNumber;

	// Array of forward token definitions

	FiniteAutomaton[] _fwdTokenDef;

	// Array of reverse token definitions

	FiniteAutomaton[] _revTokenDef;

	// Forward DFA

	FiniteAutomaton _fwdDFA;

	// Reverse DFA

	FiniteAutomaton _revDFA;

	// Forward DFA state count

	int _fwdStateCount;

	// Reverse DFA state count

	int _revStateCount;

	// Forward DFA transition table

	short[][] _fwdTransitionTable;

	// Reverse DFA transition table

	short[][] _revTransitionTable;

	// Forward DFA initial state table

	int[] _fwdInitialState;

	// Forward DFA state recognition count

	int _fwdRecognitionCount;

	// Reverse DFA state recognition count

	int _revRecognitionCount;

	// Forward DFA state recognition table

	int[] _fwdRecognitionTable;

	// Reverse DFA state recognition table

	int[] _revRecognitionTable;

	// Forward DFA context split table

	boolean[][] _fwdContextSplit;

	// Reverse DFA context split table

	boolean[][] _revContextSplit;

	// Forward DFA token list

	int[] _fwdTokenListLength;

	int[][] _fwdTokenList;


	// ----- Interface to the Scanner -----


	// The condition number for "notInComment"

	int conditionNotInComment;

	// The condition number for "inComment"

	int conditionInComment;

	// This flag is true if an illegal character message was printed

	boolean reportedIllegalChar;


	// ------ Parser Generator Parameters ------


	// The error output object

	ErrorOutput _errorOutput;

	// Flag to select verbose mode

	boolean _verbose;
	
	// The client for sending progress reports and receiving interrupts
	
	GeneratorStatus _generatorStatus;




	// Function to report errors at a specified position.  The position
	// information is taken from the Token object.
	//
	// If the type is typeError, then the _error flag is set.

	void reportError (int type, Token token, String code, String message)
	{

		// Set error flag if it's an error

		if (type == ErrorOutput.typeError)
		{
			_error = true;

			if (_generatorStatus != null)
			{
				_generatorStatus.statusError (_errorFlags);
			}
		}

		// Report the error
		
		_errorOutput.reportError (type, null, token.file, token.line,
			token.column, code, message );

		return;
	}




	// Function to report errors, with no position information.
	//
	// If the type is typeError, then the _error flag is set.

	void reportError (int type, String code, String message)
	{

		// Set error flag if it's an error

		if (type == ErrorOutput.typeError)
		{
			_error = true;

			if (_generatorStatus != null)
			{
				_generatorStatus.statusError (_errorFlags);
			}
		}

		// Report the error
		
		_errorOutput.reportError (type, null, null, ErrorOutput.noPosition,
			ErrorOutput.noPosition, code, message );

		return;
	}
	
	
	
	
	// Function to report the start of a new stage.
	
	void statusStage (String stage) throws InterruptedCompilerException
	{
		if (_generatorStatus != null)
		{
			_generatorStatus.statusStage (stage);
		}
		return;
	}
	
	
	
	
	// Function to report an increment of work within a stage.
	
	void statusWork () throws InterruptedCompilerException
	{
		if (_generatorStatus != null)
		{
			_generatorStatus.statusWork ();
		}
		return;
	}




	// The scanner calls this routine when it reaches end-of-file.
	//
	// Implements the scannerEOF() method of ScannerClient.

	public void scannerEOF (Scanner scanner, Token token)
	{

		// If we are in the middle of a comment ...

		if (scanner.condition() == conditionInComment)
		{

			// Report a run-on comment error

			reportError (ErrorOutput.typeError, token, null,
				"Run-on comment." );
		}

		return;
	}




	// The scanner calls this routine when it cannot match a token.
	//
	// Implements the scannerUnmatchedToken() method of ScannerClient.

	public void scannerUnmatchedToken (Scanner scanner, Token token)
	{

		// Report the error

		reportError (ErrorOutput.typeError, token, null,
					 "Illegal character or unrecognized token in input." );

		return;
	}



	// The parser calls this routine when an I/O exception occurs.
	//
	// Implements the parserIOException() method of ParserClient.

	public void parserIOException (Parser parser, IOException e)
	{
		int savedErrorFlags = _errorFlags;
		_errorFlags = GenObserver.efJaccRead;
		
		reportError (ErrorOutput.typeError, "I/O Exception", e.toString());
		
		_errorFlags = savedErrorFlags;
		return;
	}



	// The parser calls this routine when a syntax exception occurs.
	//
	// Implements the parserSyntaxException() method of ParserClient.

	public void parserSyntaxException (Parser parser, SyntaxException e)
	{
		if (e instanceof InterruptedCompilerException)
		{
			_error = true;
			_interruptException = (InterruptedCompilerException) e;
		}
		else
		{
			int savedErrorFlags = _errorFlags;
			_errorFlags = GenObserver.efInternalError;
			
			reportError (ErrorOutput.typeError, "Syntax Exception",
				e.toString() + ".");
			
			_errorFlags = savedErrorFlags;
		}
		return;
	}




	// The parser calls this routine when it repairs an error.
	//
	// Implements the parserErrorRepair() method of ParserClient.

	public void parserErrorRepair (Parser parser, Token errorToken,
		int[] insertions, int insertionLength, int[] deletions, int deletionLength)
	{

		// If it's a "simple" repair...

		if ((insertionLength + deletionLength) <= 6)
		{

			// For each insertion ...

			for (int i = 0; i < insertionLength; ++i)
			{

				// Report the insertion

				reportError (ErrorOutput.typeError, errorToken, null,
					"Expected '" + parser.symbolName(insertions[i]) + "'." );
			}

			// For each deletion ...

			for (int i = 0; i < deletionLength; ++i)
			{

				// Report the deletion

				reportError (ErrorOutput.typeError, errorToken, null,
					"Unexpected '" + parser.symbolName(deletions[i]) + "'." );
			}
		}

		// Otherwise, it's a "complicated" repair ...

		else
		{

			// Report a generic error

			reportError (ErrorOutput.typeError, errorToken, null,
				"Syntax error." );
		}

		return;
	}




	// The parser calls this routine when it is unable to repair an error.
	//
	// Implements the parserErrorFail() method of ParserClient.

	public void parserErrorFail (Parser parser, Token errorToken)
	{

		// Report a generic error

		reportError (ErrorOutput.typeError, errorToken, null,
			"Syntax error - unable to continue." );

		return;
	}




	public ParserGenerator ()
	{
		super ();

		return;
	}




	// This private function initializes variables as required prior to
	// parsing the language definition.

	void initParse ()
	{

		// Initialize symbol table variables

		_symbolCount = 0;

		_symbolTable = new Hashtable ();

		_productionCount = 0;

		_productionTable = new ObjectSet ();

		_categoryCount = 0;

		_categoryTable = new Hashtable ();

		_tokenCount = 0;

		_tokenTable = new ObjectSet ();

		_conditionCount = 0;

		_conditionTable = new Hashtable ();

		// Initialize global parsing variables

		_seenRepairOption = false;

		_seenGrammarOption = false;

		_seenCharSetSizeOption = false;

		_seenGoalOption = false;

		_seenJavaNameOption = false;

		_internalGoalSymbol = null;

		_userGoalSymbol = null;

		_grammarType = defaultGrammarType;

		_maxInsertion = defaultMaxInsertion;

		_maxDeletion = defaultMaxDeletion;

		_validationLength = defaultValidationLength;

		_charSetSize = defaultCharSetSize;
		
		_javaName = null;

		_error = false;
		
		_interruptException = null;

		// Create the end-of-file terminal.  It is always symbol 0.

		_internalEOFSymbol = createTerminal (internalEOFSymbolName, 1, 1);

		_eofSymbol = _internalEOFSymbol._number;

		return;
	}




	// This private function makes the LR(1) machine.

	private void makeLR1 () throws InterruptedCompilerException
	{

		// Write title line

		if (_grammarType == LRMachine.LALR1)
		{
			reportError (ErrorOutput.typeInformational, null,
				"Generating LALR(1) configuration finite state machine ..." );
		}
		else if (_grammarType == LRMachine.PLR1)
		{
			reportError (ErrorOutput.typeInformational, null,
				"Generating PLR(1) configuration finite state machine ..." );
		}
		else
		{
			reportError (ErrorOutput.typeInformational, null,
				"Generating LR(1) configuration finite state machine ..." );
		}

		reportError (ErrorOutput.typeInformational, null,
			"" );
		
		statusStage ("Analyzing productions");

		// Report error if production table is empty

		if (_productionTable.isEmpty())
		{
			reportError (ErrorOutput.typeError, null,
				"No productions specified." );

			return;
		}

		// If the user's goal symbol is unspecified ...

		if (_userGoalSymbol == null)
		{

			// Use the left hand side of the first production

			_userGoalSymbol =
				((ParserGeneratorProduction) _productionTable.elements().nextElement())._lhs;
		}

		// If the internal goal symbol is not created yet ...

		if (_internalGoalSymbol == null)
		{

			// Create the internal goal symbol

			_internalGoalSymbol = getOrCreateSymbol (internalGoalSymbolName);

			// Create the augmenting production

			ParserGeneratorProduction augmentingProduction = createProduction ();

			augmentingProduction._lhs = _internalGoalSymbol;
			augmentingProduction._linkName = "";
			augmentingProduction._parameter = new Integer(0);
			augmentingProduction._rhs = new ObjectDeque ();
			augmentingProduction._rhs.pushLast (_userGoalSymbol);
			augmentingProduction._rhs.pushLast (_internalEOFSymbol);

			_goalProduction = augmentingProduction._number;
		}

		// Dump out the symbol table into arrays of symbol names, insertion
		// costs, and deletion costs

		_symbols = new String[_symbolCount];

		_insertionCost = new int[_symbolCount];

		_deletionCost = new int[_symbolCount];

		for (Enumeration e = _symbolTable.elements(); e.hasMoreElements(); )
		{

			// Get next symbol

			ParserGeneratorSymbol sym = (ParserGeneratorSymbol) e.nextElement();

			// Get symbol name

			_symbols[sym._number] = sym._name;

			// Get symbol insertion cost

			_insertionCost[sym._number] = sym._insertionCost;

			// Get symbol deletion cost

			_deletionCost[sym._number] = sym._deletionCost;
		}

		// Convert production table to array form

		_productions = new int[_productionCount][];

		_productionParam = new int[_productionCount];

		_productionLink = new String[_productionCount];

		_productionLHSSymbol = new int[_productionCount];

		_productionRHSLength = new int[_productionCount];

		for (Enumeration e = _productionTable.elements(); e.hasMoreElements(); )
		{

			// Get next production

			ParserGeneratorProduction prod = (ParserGeneratorProduction) e.nextElement();

			// Allocate the second-level array for this production

			_productions[prod._number] = new int[prod._rhs.elementCount() + 1];

			// Insert the left hand side

			_productions[prod._number][0] = prod._lhs._number;

			// Insert the right hand side

			for (int i = 1; i < _productions[prod._number].length; ++i)
			{
				_productions[prod._number][i] =
					((ParserGeneratorSymbol) prod._rhs.peekFirst (i - 1))._number;
			}

			// Save the parameter

			if (prod._parameter == null)
			{
				_productionParam[prod._number] = 0;
			}
			else
			{
				_productionParam[prod._number] = prod._parameter.intValue();
			}

			// Save the link name

			if (prod._linkName == null)
			{
				_productionLink[prod._number] = "";
			}
			else
			{
				_productionLink[prod._number] = prod._linkName;
			}

			// Save the left hand side symbol

			_productionLHSSymbol[prod._number] = prod._lhs._number;

			// Save the right hand side length

			_productionRHSLength[prod._number] = prod._rhs.elementCount();
		}
			
		// Report progress
			
		statusWork ();

		// Create the context-free grammar

		_CFG = new ContextFreeGrammar (_symbols, _internalGoalSymbol._number , _productions);
			
		// Report progress
			
		statusWork ();

		// Validate all symbols

		for (Enumeration e = _symbolTable.elements(); e.hasMoreElements(); )
		{

			// Get next symbol

			ParserGeneratorSymbol sym = (ParserGeneratorSymbol) e.nextElement();

			// Check for reachability

			if (!_CFG.isReachable (sym._number))
			{

				// Report error

				reportError (ErrorOutput.typeError, null,
					"Symbol '" + sym._name + "' is unreachable." );
			}

			// Check that the symbol derives terminals

			if (!_CFG.derivesTerminals (sym._number))
			{

				// Report error

				reportError (ErrorOutput.typeError, null,
					"Symbol '" + sym._name + "' does not derive terminals." );
			}

			// If this symbol is defined as a terminal ...
			
			if (sym._isTerminal)
			{

				// Check it doesn't appear on the lhs of a production

				if (!_CFG.isTerminal (sym._number))
				{

					// Report error

					reportError (ErrorOutput.typeError, null,
						"Terminal symbol '" + sym._name
						+ "' appears on the left hand side of a production." );
				}
			}

			// Otherwise, this symbol isn't defined as a terminal ...
			
			else
			{

				// Check it appears on the lhs of a production

				if (_CFG.isTerminal (sym._number))
				{

					// Report error

					reportError (ErrorOutput.typeError, null,
						"Nonterminal symbol '" + sym._name
						+ "' does not appear on the left hand side of a production." );
				}
			}

		}	// end symbol validation loop

		// If symbols aren't validated OK, we need to abort

		if (_error)
		{
			return;
		}
			
		// Report progress
			
		statusWork ();

		// Get the shift and reduce sets for each production

		_shiftSet = new IntSet[_productionCount];

		_reduceSet = new IntSet[_productionCount];

		for (int i = 0; i < _productionCount; ++i)
		{
			_shiftSet[i] = new IntSet ();
			_reduceSet[i] = new IntSet ();
		}

		for (Enumeration e = _productionTable.elements(); e.hasMoreElements(); )
		{

			// Get next production

			ParserGeneratorProduction prod = (ParserGeneratorProduction) e.nextElement();

			// Enumerate the shift set

			for (Enumeration se = prod._shift.elements(); se.hasMoreElements(); )
			{

				// Get next symbol

				ParserGeneratorSymbol sym = (ParserGeneratorSymbol) se.nextElement();

				// Add its first set to the shift set

				_shiftSet[prod._number].union (_CFG.firstSet (sym._number));
			}

			// Enumerate the reduce set

			for (Enumeration re = prod._reduce.elements(); re.hasMoreElements(); )
			{

				// Get next symbol

				ParserGeneratorSymbol sym = (ParserGeneratorSymbol) re.nextElement();

				// Add its first set to the reduce set

				_reduceSet[prod._number].union (_CFG.firstSet (sym._number));
			}
		}

		// Set production costs and single point insertion list for no repair

		_productionCost = null;

		_singlePointInsertionCount = 0;

		_singlePointInsertions = new int[0];

		// If insertion repairs are allowed ...

		if (_maxInsertion != 0)
		{

			// Get the production costs

			_productionCost = new long[_productionCount];

			// Allocate a temporary array to hold symbol costs

			long[] symbolCost = new long [_symbolCount];

			// Initialize the cost of a terminal to its insertion cost, and the
			// cost of a nonterminal to infinity

			for (int i = 0; i < _symbolCount; ++i)
			{
				if (_CFG.isTerminal (i))
				{
					symbolCost[i] = (long) _insertionCost[i];
				}
				else
				{
					symbolCost[i] = infiniteProductionCost;
				}
			}

			// The cost of a nonterminal symbol is the cost of the lowest-cost
			// production which has the nonterminal on the left hand side, plus 1.
			// The cost of a production is the sum of the costs of the symbols on
			// the right hand side.  We calculate this iteratively, repeating the
			// calculation until the costs stop changing.  (They always stop
			// eventually because nonterminal costs are only reduced, never
			// increased.)

			for (boolean isChanged = true; isChanged; )
			{

				// Nothing changed yet

				isChanged = false;
			
				// Report progress
			
				statusWork ();

				// For each production ...

				for (int i = 0; i < _productionCount; ++i)
				{

					// Calculate total cost of right hand side.  Take care that the
					// total not exceed infiniteProductionCost-1.

					_productionCost[i] = 0;

					for (int j = 1; j < _productions[i].length; ++j)
					{
						_productionCost[i] += Math.min (symbolCost[_productions[i][j]],
							(infiniteProductionCost - 1L) - _productionCost[i] );
					}

					// If new minimum for the nonterminal symbol on the left hand side ...

					if ((_productionCost[i] + 1L) < symbolCost[_productions[i][0]])
					{

						// Establish a new lower cost for this nonterminal symbol

						symbolCost[_productions[i][0]] = _productionCost[i] + 1L;

						// Indicate that something changed

						isChanged = true;
					}
				}
				
				// Count the number of passes for statistics
				
				++_productionCostPassCount;

			}	// end iteration to calculate production costs

			// In pathological cases, production costs can be exponential in the
			// number of productions.  Detect if this has happened by making sure
			// that the costs of nonterminals are not infinite.

			for (int i = 0; i < _symbolCount; ++i)
			{
				if (symbolCost[i] == infiniteProductionCost)
				{
					reportError (ErrorOutput.typeError, null,
						"Unable to calculate a consistent set of production costs." );

					return;
				}
			}

			// Allocate the single-point insertion list.  Its length is the number
			// of terminal symbols, excluding the end-of-file symbol.

			for (int i = 1; i < _symbolCount; ++i)
			{
				if (_CFG.isTerminal (i))
				{
					++_singlePointInsertionCount;
				}
			}

			_singlePointInsertions = new int[_singlePointInsertionCount];

			// Insert symbols into the list, in order of increasing insertion cost.
			// We use a stable insertion sort, so that symbols with the same cost
			// appear in the order they were declared.

			for (int i = 1, j = 0; i < _symbolCount; ++i)
			{
				if (_CFG.isTerminal (i))
				{
					int k = j++;
					while (k > 0)
					{
						if (_insertionCost[_singlePointInsertions[k-1]] <= _insertionCost[i])
						{
							break;
						}
						_singlePointInsertions[k] = _singlePointInsertions[k-1];
						--k;
					}
					_singlePointInsertions[k] = i;
				}
			}

		}	// end if repair tables are needed

		// Generate the LR(1) machine

		_machine = new LRMachine (_CFG, _grammarType, _productionCost,
			new ParserGeneratorPrecedencePredicate (this), _generatorStatus );

		// Get the original parse table and unwinding tables

		int[][] originalParseTable = _machine.parseTable();

		int[] originalUnwindingTable = _machine.unwindingTable();

		int[] originalUnwindingParseTable = _machine.unwindingParseTable();

		int originalStateCount = originalParseTable.length;

		// Create a table to indicate, for each state, if it is a single-reduce
		// state.  If state n is a single-reduce state, singleReduce[n] contains
		// p+PC where p is the production to reduce and PC is the total number
		// of productions.  If state n is not a single-reduce state, then
		// singleReduce[n] contains m+(PC*2) where m is the new state number
		// assigned to original state n.  Note that singleReduce[0] contains
		// PC*2, which is the error flag.

		int[] singleReduce = new int[originalStateCount];

		// _stateCount is the number of non-single-reduce states

		_stateCount = 0;

		// Check each original state to see if it is single-reduce

	checkSingleReduce:
		for (int i = 0; i < originalStateCount; ++i)
		{

			// Assume state is not single-reduce

			singleReduce[i] = (_stateCount++) + (_productionCount * 2);

			// State 0 is non-single-reduce

			if (i == 0)
			{
				continue checkSingleReduce;
			}

			// Get the unwinding action

			int action = originalUnwindingTable[i];

			// To be single-reduce, the unwinding action must be a reduction, but
			// not a reduction of the goal production

			if ((action >= _productionCount) || (action == _goalProduction))
			{
				continue checkSingleReduce;
			}

			// To be single-reduce, the unwinding parse action must be the same
			// reduction (this should always be true)

			if (action != originalUnwindingParseTable[i])
			{
				continue checkSingleReduce;
			}

			// To be single-reduce, the action must not be to reduce an epsilon
			// production.  This is not strictly necessary, but it makes life
			// easier for the parser because the parser can assume that the
			// reduction part of a shift-reduce action does not increase the size
			// of the parse stack.

			if (_productionRHSLength[action] == 0)
			{
				continue checkSingleReduce;
			}

			// To be single-reduce, each entry in the parse table must be either
			// the same reduce action, or else an error action

			for (int j = 0; j < _symbolCount; ++j)
			{
				if ((originalParseTable[i][j] != action)
					&& (originalParseTable[i][j] != _productionCount))
				{
					continue checkSingleReduce;
				}
			}

			// State is single-reduce

			singleReduce[i] = action + _productionCount;

			--_stateCount;
		}

		// Check to make sure actions can be encoded in a short

		if (((_stateCount + (_productionCount * 2)) > 0x7FFF)
			|| (_machine.conflictSetCount() > 0x7FFF))
		{
			reportError (ErrorOutput.typeError, null,
				"Machine has too many states ("
				+ _stateCount
				+ ") to generate parser action table." );

			return;
		}

		// Create new action tables

		_actionTable = new short[_stateCount][];

		_unwindingTable = new int[_stateCount];

		_cognateNumber = new int[_stateCount];

		// Loop to fill the new tables

		for (int oldState = 0; oldState < originalStateCount; ++oldState)
		{

			// If state is non-single-reduce ...

			if (singleReduce[oldState] >= (_productionCount * 2))
			{
			
				// Report progress
			
				statusWork ();

				// Get the new state number

				int newState = singleReduce[oldState] - (_productionCount * 2);

				// Save cognate number

				_cognateNumber[newState] = oldState;

				// Save unwinding action

				_unwindingTable[newState] = originalUnwindingTable[oldState];

				// Create action table for this new state

				_actionTable[newState] = new short[_symbolCount + 1];

				for (int j = 0; j < _symbolCount; ++j)
				{

					// If the original action is reduce, accept, or conflict ...

					if (originalParseTable[oldState][j] < _productionCount)
					{

						// The new action is the same as the old action

						_actionTable[newState][j] = (short) originalParseTable[oldState][j];
					}

					// Else, the original action is shift or error ...

					else
					{

						// The new action is shift-goto, shift-reduce, or error.  Note
						// that error is correctly mapped because singleReduce[0] 
						// contains _productionCount*2.

						_actionTable[newState][j] = (short) singleReduce[
							originalParseTable[oldState][j] - _productionCount ];
					}
				}

				// If the original unwinding parse action is reduce ...

				if (originalUnwindingParseTable[oldState] < _productionCount)
				{

					// The new action is the same as the old action

					_actionTable[newState][_symbolCount] =
						(short) originalUnwindingParseTable[oldState];
				}

				// Else, the original unwinding parse action is shift ...

				else
				{

					// The new action is shift-goto or shift-reduce

					_actionTable[newState][_symbolCount] = (short) singleReduce[
						originalUnwindingParseTable[oldState] - _productionCount ];
				}
			}
		}

		// Print symbol tables if verbose mode

		if (_verbose)
		{

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Terminal symbols:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of grammar symbols");

			// Write terminal symbols

			for (int sym = 0; sym < _symbolCount; ++sym)
			{

				// If nonterminal, skip it

				if (!_CFG.isTerminal(sym))
				{
					continue;
				}
			
				// Report progress
			
				statusWork ();

				// Write the symbol

				reportError (ErrorOutput.typeInformational, null,
					"  " + _symbols[sym] );

				// Write the insertion cost

				reportError (ErrorOutput.typeInformational, null,
					"    Insertion cost: " + _insertionCost[sym] );

				// Write the deletion cost

				reportError (ErrorOutput.typeInformational, null,
					"    Deletion cost: " + _deletionCost[sym] );

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Nonterminal symbols:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write nonterminal symbols

			for (int sym = 0; sym < _symbolCount; ++sym)
			{

				// If terminal, skip it

				if (_CFG.isTerminal(sym))
				{
					continue;
				}
			
				// Report progress
			
				statusWork ();

				// Write the symbol

				reportError (ErrorOutput.typeInformational, null,
					"  " + _symbols[sym] );

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Productions:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of productions");

			// Write productions

			for (int prod = 0; prod < _productionCount; ++prod)
			{
			
				// Report progress
			
				statusWork ();

				// Write the production number

				reportError (ErrorOutput.typeInformational, null,
					"  Production " + prod );

				// Create a buffer

				StringBuffer buf = new StringBuffer ();

				// Append the left hand side

				buf.append ("    ");
				buf.append (_symbols[_productions[prod][0]]);

				// Append the arrow

				buf.append (" ->");

				// Append the right hand side

				for (int j = 1; j < _productions[prod].length; ++j)
				{
					buf.append (" ");
					buf.append (_symbols[_productions[prod][j]]);
				}

				// Write the production

				reportError (ErrorOutput.typeInformational, null,
					buf.toString() );

				// Write the link name

				if (_productionLink[prod].length() != 0)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    Link name: " + _productionLink[prod] );
				}

				// Write the parameter

				reportError (ErrorOutput.typeInformational, null,
					"    Parameter: " + _productionParam[prod] );

				// If there is a shift set ...

				if (!_shiftSet[prod].isEmpty())
				{

					// Create a buffer and append the start of the line

					StringBuffer buf2 = new StringBuffer ();
					buf2.append ("    Shift terminals:");

					// Append the names of the terminals in the shift set

					for (IntEnumeration e = _shiftSet[prod].elements(); e.hasMoreElements(); )
					{
						buf2.append (" ");
						buf2.append (_symbols[e.nextElement()]);
					}

					// Write the shift set

					reportError (ErrorOutput.typeInformational, null,
						buf2.toString() );
				}

				// If there is a reduce set ...

				if (!_reduceSet[prod].isEmpty())
				{

					// Create a buffer and append the start of the line

					StringBuffer buf2 = new StringBuffer ();
					buf2.append ("    Reduce terminals:");

					// Append the names of the terminals in the reduce set

					for (IntEnumeration e = _reduceSet[prod].elements(); e.hasMoreElements(); )
					{
						buf2.append (" ");
						buf2.append (_symbols[e.nextElement()]);
					}

					// Write the reduce set

					reportError (ErrorOutput.typeInformational, null,
						buf2.toString() );
				}

				// If doing error repair, write the cost

				if (_maxInsertion != 0)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    Cost: " + _productionCost[prod] );
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Error repair options:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write maximum insertions

			reportError (ErrorOutput.typeInformational, null,
				"  Maximum insertions: " + _maxInsertion );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write maximum deletions

			reportError (ErrorOutput.typeInformational, null,
				"  Maximum deletions: " + _maxDeletion );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write validation length

			reportError (ErrorOutput.typeInformational, null,
				"  Validation length: " + _validationLength );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// If doing error repair, write single-point insertion list

			if (_maxInsertion != 0)
			{

				// Write title line

				reportError (ErrorOutput.typeInformational, null,
					"  Single-point insertion list:" );

				reportError (ErrorOutput.typeInformational, null,
					"" );

				// Write single-point insertion list

				for (int i = 0; i < _singlePointInsertionCount; ++i)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    " + _symbols[_singlePointInsertions[i]] );
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Parser generator algorithm statistics:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write number of raw LR(0) machine states

			reportError (ErrorOutput.typeInformational, null,
				"  Number of raw LR(0) states: "
				+ _machine.LR0StateCount() );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write number of raw LR(1) machine states

			reportError (ErrorOutput.typeInformational, null,
				"  Number of raw LR(1) states: "
				+ _machine.LR1StateCount (_machine.LR1PassCount() - 1) );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write number of LR(1) passes

			reportError (ErrorOutput.typeInformational, null,
				"  Number of iterative passes to generate LR(1) states: "
				+ _machine.LR1PassCount() );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write list of cognate counts on each pass

			reportError (ErrorOutput.typeInformational, null,
				"  Number of reachable LR(1) states on each pass:" );

			for (int i = 0; i < _machine.LR1PassCount(); ++i)
			{
				reportError (ErrorOutput.typeInformational, null,
					"    Pass " + i + ": " + _machine.LR1StateCount(i) );
			}

			reportError (ErrorOutput.typeInformational, null,
				"" );
			
			// If doing error repair ...

			if (_maxInsertion != 0)
			{

				// Write number production cost passes

				reportError (ErrorOutput.typeInformational, null,
					"  Number of iterative passes to generate production costs: "
					+ _productionCostPassCount );

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write number of LR(0) passes

			reportError (ErrorOutput.typeInformational, null,
				"  Number of iterative passes to generate conflict avoidance tables: "
				+ _machine.LR0PassCount() );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write number of LR(1) parsing conflicts resolved
			
			reportError (ErrorOutput.typeInformational, null,
				"  Number of parsing conflicts successfully resolved: "
				+ _machine.LR1ConflictsResolved() );

			reportError (ErrorOutput.typeInformational, null,
				"" );

		}	// end if verbose mode

		// We need to print machine states if verbose mode, or if there is a conflict

		if (_verbose || (_machine.conflictCount() != 0))
		{

			// Write title line

			if (_grammarType == LRMachine.LALR1)
			{
				reportError (ErrorOutput.typeInformational, null,
					"LALR(1) configuration finite state machine:" );
			}
			else if (_grammarType == LRMachine.PLR1)
			{
				reportError (ErrorOutput.typeInformational, null,
					"PLR(1) configuration finite state machine:" );
			}
			else
			{
				reportError (ErrorOutput.typeInformational, null,
					"LR(1) configuration finite state machine:" );
			}

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			if (_verbose)
			{
				if (_grammarType == LRMachine.LALR1)
				{
					statusStage ("Writing verbose list of LALR(1) states");
				}
				else if (_grammarType == LRMachine.PLR1)
				{
					statusStage ("Writing verbose list of PLR(1) states");
				}
				else
				{
					statusStage ("Writing verbose list of LR(1) states");
				}
			}

			// Loop over states

		printLRStates:
			for (int state = 0; state < _stateCount; ++state)
			{

				// Determine if this state needs to be printed

				checkLRPrint:
				{

					// If verbose mode, print

					if (_verbose)
					{
						break checkLRPrint;
					}

					// If state contains a conflict, print

					for (int j = 0; j < _symbolCount; ++j)
					{
						if (_actionTable[state][j] < 0)
						{
							break checkLRPrint;
						}
					}

					// Don't need to print this state

					continue printLRStates;
				}
			
				// Report progress
			
				statusWork ();

				// Write the header for this state

				reportError (ErrorOutput.typeInformational, null,
					"  State " + state + ":" );

				// Write the basis set

				for (int b = 0; b < _machine.cognateBasisCount (_cognateNumber[state]); ++b)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    "
						+ _machine.cognateBasisToString (_cognateNumber[state], b)
						+ "  "
						+ _machine.cognateLookaheadToString (_cognateNumber[state], b) );
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );

				// Create a set of actions for this state

				SmallIntSet actionSet = new SmallIntSet ();

				for (int j = 0; j < _symbolCount; ++j)
				{
					actionSet.addElement (_actionTable[state][j]);
				}

				// Write the actions

				for (IntEnumeration ie = actionSet.elements(); ie.hasMoreElements(); )
				{

					// Get the next action

					int action = ie.nextElement();

					// If the action is error, skip it

					if (action == (_productionCount * 2))
					{
						continue;
					}

					// Allocate a buffer

					StringBuffer buf = new StringBuffer ();

					// Append a description of the action

					if (action < 0)
					{
						if (_machine.conflictSet(action).isElement(_productionCount))
						{
							buf.append ("    Conflict (shift/reduce");
						}
						else
						{
							buf.append ("    Conflict (reduce/reduce");
						}

						for (IntEnumeration re = _machine.conflictSet(action).elements();
							re.hasMoreElements(); )
						{
							int reduction = re.nextElement();

							if (reduction < _productionCount)
							{
								buf.append (" ");
								buf.append (reduction);
							}
						}

						buf.append ("):");
					}

					else if (action == _goalProduction)
					{
						buf.append ("    Accept:");
					}
					else if (action < _productionCount)
					{
						buf.append ("    Reduce ");
						buf.append (action);
						buf.append (":");
					}
					else if (action < (_productionCount * 2))
					{
						buf.append ("    Shift-Reduce ");
						buf.append (action - _productionCount);
						buf.append (":");
					}
					else
					{
						buf.append ("    Shift-Goto ");
						buf.append (action - (_productionCount * 2));
						buf.append (":");
					}

					// Append the symbols which have this action

					for (int j = 0; j < _symbolCount; ++j)
					{
						if (_actionTable[state][j] == action)
						{
							buf.append (" ");
							buf.append (_symbols[j]);
						}
					}

					// Write the action

					reportError (ErrorOutput.typeInformational, null,
						buf.toString() );
				}

				// Write the unwinding action

				if (_maxInsertion != 0)
				{
					if (_unwindingTable[state] < _productionCount)
					{
						reportError (ErrorOutput.typeInformational, null,
							"    Unwind: Reduce " + _unwindingTable[state] );
					}
					else if (_unwindingTable[state] == (_productionCount + _eofSymbol))
					{
						reportError (ErrorOutput.typeInformational, null,
							"    Unwind: Accept" );
					}
					else
					{
						if (_actionTable[state][_symbolCount] < (_productionCount * 2))
						{
							reportError (ErrorOutput.typeInformational, null,
								"    Unwind: Shift "
								+ _symbols[_unwindingTable[state] - _productionCount]
								+ " Reduce "
								+ (_actionTable[state][_symbolCount] - _productionCount) );
						}
						else
						{
							reportError (ErrorOutput.typeInformational, null,
								"    Unwind: Shift "
								+ _symbols[_unwindingTable[state] - _productionCount]
								+ " Goto "
								+ (_actionTable[state][_symbolCount] - (_productionCount * 2)) );
						}
					}
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over states

		}	// end if we need to write machine states

		// Write summary data

		reportError (ErrorOutput.typeInformational, null,
			_symbolCount + " symbols." );

		reportError (ErrorOutput.typeInformational, null,
			_productionCount + " productions." );

		if (_grammarType == LRMachine.LALR1)
		{
			reportError (ErrorOutput.typeInformational, null,
				_stateCount + " LALR(1) machine states." );
		}
		else if (_grammarType == LRMachine.PLR1)
		{
			reportError (ErrorOutput.typeInformational, null,
				_stateCount + " PLR(1) machine states." );
		}
		else
		{
			reportError (ErrorOutput.typeInformational, null,
				_stateCount + " LR(1) machine states." );
		}

		reportError (ErrorOutput.typeInformational, null,
			"" );

		// Write error message if unresolved conflicts

		if (_machine.conflictCount() != 0)
		{
			reportError (ErrorOutput.typeError, null,
				_machine.conflictCount() + " unresolved conflicts." );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			return;
		}

		// Successful LR(1) machine construction

		return;
	}




	// This private function makes the character category table.

	private void makeCategories () throws InterruptedCompilerException
	{

		// Write title line

		reportError (ErrorOutput.typeInformational, null,
			"Generating character category table ..." );

		reportError (ErrorOutput.typeInformational, null,
			"" );
		
		statusStage ("Analyzing character categories");

		// Dump out the category table into arrays of category names and predicates

		_categoryNames = new String[_categoryCount];

		_charGroups = new UnaryIntPredicate[_categoryCount];

		for (Enumeration e = _categoryTable.elements(); e.hasMoreElements(); )
		{

			// Get next category

			ParserGeneratorCategory cat = (ParserGeneratorCategory) e.nextElement();

			// Get category name

			_categoryNames[cat._number] = cat._name;

			// Get category predicate

			_charGroups[cat._number] = cat._predicate;
		}

		// Create the character category table

		_CCT = new CharCategoryTable ();

		_calculatedCategorySets = _CCT.calculateCategories (
										_charGroups, _charSetSize, _generatorStatus );

		// Check that the calculation was successful

		if (_calculatedCategorySets == null)
		{
			reportError (ErrorOutput.typeError, null,
				"More than 256 calculated character categories." );

			return;
		}

		// Get the resulting tables

		_calculatedCategoryTable = _CCT.categoryTable();

		_calculatedCategoryCount = _CCT.categoryCount();

		// If verbose mode, print the table ...

		if (_verbose)
		{

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Character category table:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of character categories");

			// Variables for upper and lower limits of range

			int rangeLower = 0;
			int rangeUpper = 0;

			// Loop until entire character set is processed ...

			while (rangeUpper < _charSetSize)
			{
				
				// Report progress
				
				statusWork ();

				// Last upper limit is new lower limit

				rangeLower = rangeUpper;

				// Loop until character value changes

				while ((++rangeUpper) < _charSetSize)
				{
					if (_calculatedCategoryTable[rangeUpper]
						!= _calculatedCategoryTable[rangeLower])
					{
						break;
					}
				}

				// Category number

				int category = _calculatedCategoryTable[rangeLower] & 0xFF;

				// Write the range

				reportError (ErrorOutput.typeInformational, null,
					"  0x"
					+ Integer.toString (rangeLower, 16).toUpperCase()
					+ " - 0x"
					+ Integer.toString (rangeUpper-1, 16).toUpperCase() );

				// Write the category number

				reportError (ErrorOutput.typeInformational, null,
					"    Category number: " + category );

				// Allocate a buffer for the user categories

				StringBuffer buf = new StringBuffer ();
				buf.append ("    User categories:");

				// Append each user category that contains this calculated category

				for (int cat = 0; cat < _categoryCount; ++cat)
				{
					if (_calculatedCategorySets[cat].isElement(category))
					{
						buf.append (" ");
						buf.append (_categoryNames[cat]);
					}
				}

				// Write the user categories

				reportError (ErrorOutput.typeInformational, null,
					buf.toString() );

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over all ranges

		}	// end if verbose

		// Write summary data

		reportError (ErrorOutput.typeInformational, null,
			_categoryCount + " user-defined categories." );

		reportError (ErrorOutput.typeInformational, null,
			_calculatedCategoryCount + " calculated categories." );

		reportError (ErrorOutput.typeInformational, null,
			"" );

		// Successful character category table construction

		return;
	}




	// This private function makes the token finite automata.

	private void makeTokens () throws InterruptedCompilerException
	{

		// Write title line

		reportError (ErrorOutput.typeInformational, null,
			"Generating token deterministic finite automata ..." );

		reportError (ErrorOutput.typeInformational, null,
			"" );
		
		statusStage ("Analyzing tokens");

		// If no conditions are defined ...

		if (_conditionCount == 0)
		{

			// Add a default condition

			createCondition (internalDefaultCondition);
		}

		// Dump out the condition table into an array of condition names

		_conditionNames = new String[_conditionCount];

		for (Enumeration e = _conditionTable.elements(); e.hasMoreElements(); )
		{

			// Get next condition

			ParserGeneratorCondition cond = (ParserGeneratorCondition) e.nextElement();

			// Get condition name

			_conditionNames[cond._number] = cond._name;
		}

		// Dump out the token table into arrays

		_tokenNames = new String[_tokenCount];

		_tokenParam = new int[_tokenCount];

		_tokenLink = new String[_tokenCount];

		_contextNumber = new int[_tokenCount];

		_fwdTokenDef = new FiniteAutomaton[_tokenCount];

		_revTokenDef = new FiniteAutomaton[_tokenCount];

		for (Enumeration e = _tokenTable.elements(); e.hasMoreElements(); )
		{
			
			// Report progress
			
			statusWork ();

			// Get next token

			ParserGeneratorToken tok = (ParserGeneratorToken) e.nextElement();

			// Get token name

			_tokenNames[tok._number] = tok._name;

			// Get token context number

			_contextNumber[tok._number] =
				(tok._contextExp == null) ? _contextCount : tok._ctxNumber;

			// Get token link name

			if (tok._linkName == null)
			{
				_tokenLink[tok._number] = "";
			}
			else
			{
				_tokenLink[tok._number] = tok._linkName;
			}

			// If token has an explicit parameter ...

			if (tok._parameter != null)
			{

				// Save the parameter value

				_tokenParam[tok._number] = tok._parameter.intValue();

				// Token name must not be a symbol

				if (getSymbol (tok._name) != null)
				{
					reportError (ErrorOutput.typeError, null,
						"Token '"
						+ tok._name
						+ "' cannot have an explicit parameter because it is a symbol name." );
				}
			}

			// Otherwise, implicit token parameter ...

			else
			{

				// Parameter is the value of the token's name

				ParserGeneratorSymbol sym = getSymbol (tok._name);

				// If the symbol is defined ...

				if (sym != null)
				{

					// Parameter is symbol number

					_tokenParam[tok._number] = sym._number;

					// Error if symbol is a nonterminal ...

					if (!sym._isTerminal)
					{
						reportError (ErrorOutput.typeError, null,
							"Token name '"
							+ tok._name
							+ "' is also the name of a nonterminal symbol." );
					}
				}

				// If the symbol is undefined ...

				else
				{

					// Parameter defaults to zero

					_tokenParam[tok._number] = 0;
				}
			}

			// Set of start conditions for this token

			IntSet condSet = new IntSet ();

			// If no conditions specified for this token ...

			if (tok._conditionSet == null)
			{

				// Add all conditions to the set

				for (int i = 0; i < _conditionCount; ++i)
				{
					condSet.addElement (i + internalStartConditionOffset);
				}
			}

			// Otherwise, token has associated conditions ...

			else
			{

				// For each condition in the set ...

				for (Enumeration ce = tok._conditionSet.elements(); ce.hasMoreElements(); )
				{

					// Get the name of the condition

					String condName = (String) ce.nextElement();

					// Get the corresponding condition object

					ParserGeneratorCondition cond = getCondition (condName);

					// If the condition name is defined ...

					if (cond != null)
					{

						// Add the condition number to the set

						condSet.addElement (cond._number + internalStartConditionOffset);
					}

					// Otherwise, it's an error

					else
					{
						reportError (ErrorOutput.typeError, null,
							"Undefined start condition name '" + condName + "'." );
					}
				}
			}

			// If there is a context expression ...

			if (tok._contextExp != null)
			{

				// Array for tagged catenation

				FiniteAutomaton[] taggedCat = new FiniteAutomaton[2];

				// The first element is the catenation of the start conditions
				// and the token expression.  Its tag is the token number plus
				// the context split offset.

				taggedCat[0] = FiniteAutomaton.makeNFACatenation (
					tok._number + internalContextSplitOffset,
					FiniteAutomaton.makeNFAOneChar (
						tok._number + internalContextSplitOffset, condSet ),
					tok._tokenExp.makeNFA (tok._number + internalContextSplitOffset) );

				// The second element is the context expression.  Its tag is the
				// token number.

				taggedCat[1] = tok._contextExp.makeNFA (tok._number);

				// The forward token definition is the tagged catenation

				_fwdTokenDef[tok._number] =
					FiniteAutomaton.makeNFATaggedCatenation (taggedCat);

				// The reverse token definition is the reverse of the context element.
				// Its tag is the token number.

				_revTokenDef[tok._number] =
					FiniteAutomaton.makeNFAReverse (tok._number, taggedCat[1]);
			}

			// Otherwise, there is no context expression ...

			else
			{

				// The forward token definition is the catenation of the start
				// conditions and the token expression.  Its tag is the token
				// number.

				_fwdTokenDef[tok._number] = FiniteAutomaton.makeNFACatenation (
					tok._number,
					FiniteAutomaton.makeNFAOneChar (tok._number, condSet),
					tok._tokenExp.makeNFA (tok._number) );

				// The reverse token definition is the empty set

				_revTokenDef[tok._number] =
					FiniteAutomaton.makeNFAEmptySet (tok._number);
			}

		}	// end loop over token definitions

		// If error, return

		if (_error)
		{
			return;
		}

		// Make the forward DFA

		_fwdDFA = FiniteAutomaton.makeNFATaggedAlternation (_fwdTokenDef);
				
		_fwdDFA.toDFA(_generatorStatus);

		_fwdDFA.minimizeDFAStates(_generatorStatus);

		_fwdDFA.sortTransitions();

		// Make the reverse DFA

		_revDFA = FiniteAutomaton.makeNFATaggedAlternation (_revTokenDef);

		_revDFA.toDFA(_generatorStatus);

		_revDFA.minimizeDFAStates(_generatorStatus);

		_revDFA.sortTransitions();
				
		// Report progress
					
		statusWork ();

		// Get the forward DFA transition table

		int[][] originalFwdTransitionTable = _fwdDFA.transitionTable();

		// Get the number of states

		_fwdStateCount = originalFwdTransitionTable.length;

		// Check to make sure transitions can be encoded in a short

		if (_fwdStateCount > 0x7FFF)
		{
			reportError (ErrorOutput.typeError, null,
				"Forward DFA has too many states ("
				+ _fwdStateCount
				+ ") to generate scanner transition table." );

			return;
		}

		// Allocate a new table

		_fwdTransitionTable = new short[_fwdStateCount][];

		// Convert orignal table to our form

		for (int i = 0; i < _fwdStateCount; ++i)
		{

			// Allocate second-level table

			_fwdTransitionTable[i] = new short[_calculatedCategoryCount];

			// Initialize to error values

			for (int j = 0; j < _calculatedCategoryCount; ++j)
			{
				_fwdTransitionTable[i][j] = (short) _fwdStateCount;
			}

			// Scan original table

			for (int j = 0; j < originalFwdTransitionTable[i].length; j += 2)
			{

				// If transition is not for a start condition ...

				if (originalFwdTransitionTable[i][j] < internalStartConditionOffset)
				{

					// Insert transition into new table

					_fwdTransitionTable[i][originalFwdTransitionTable[i][j]] = 
						(short) originalFwdTransitionTable[i][j+1];
				}
			}
		}
				
		// Report progress
					
		statusWork ();

		// Allocate initial state table

		_fwdInitialState = new int[_conditionCount];

		// Initialize to error values

		for (int j = 0; j < _conditionCount; ++j)
		{
			_fwdInitialState[j] = _fwdStateCount;
		}

		// Scan original table for state 0

		for (int j = 0; j < originalFwdTransitionTable[0].length; j += 2)
		{

			// If transition is for a start condition ...

			if (originalFwdTransitionTable[0][j] >= internalStartConditionOffset)
			{

				// Insert transition into new table

				_fwdInitialState[
					originalFwdTransitionTable[0][j] - internalStartConditionOffset] = 
					originalFwdTransitionTable[0][j+1];
			}
		}

		// Make sure all conditions have an initial state

		for (int j = 0; j < _conditionCount; ++j)
		{
			if (_fwdInitialState[j] == _fwdStateCount)
			{
				reportError (ErrorOutput.typeError, null,
					"No tokens defined for start condition '" + _conditionNames[j] + "'." );
			}
		}
				
		// Report progress
					
		statusWork ();

		// Get the reverse DFA transition table

		int[][] originalRevTransitionTable = _revDFA.transitionTable();

		// Get the number of states

		_revStateCount = originalRevTransitionTable.length;

		// Check to make sure transitions can be encoded in a short

		if (_revStateCount > 0x7FFF)
		{
			reportError (ErrorOutput.typeError, null,
				"Reverse DFA has too many states ("
				+ _revStateCount
				+ ") to generate scanner transition table." );

			return;
		}

		// Allocate a new table

		_revTransitionTable = new short[_revStateCount][];

		// Convert orignal table to our form

		for (int i = 0; i < _revStateCount; ++i)
		{

			// Allocate second-level table

			_revTransitionTable[i] = new short[_calculatedCategoryCount];

			// Initialize to error values

			for (int j = 0; j < _calculatedCategoryCount; ++j)
			{
				_revTransitionTable[i][j] = (short) _revStateCount;
			}

			// Scan original table

			for (int j = 0; j < originalRevTransitionTable[i].length; j += 2)
			{

				// Insert transition into new table

				_revTransitionTable[i][originalRevTransitionTable[i][j]] = 
					(short) originalRevTransitionTable[i][j+1];
			}
		}
				
		// Report progress
					
		statusWork ();

		// Get the forward final state tables

		int[] fwdTagTable = _fwdDFA.tagTable();

		int[][] fwdMergeTable = _fwdDFA.mergeTable();

		// The number of recognition codes is the length of the merge table, plus 1

		_fwdRecognitionCount = fwdMergeTable.length + 1;

		// Our state recognition code is the tag table value, plus 1

		_fwdRecognitionTable = new int[_fwdStateCount];

		for (int i = 0; i < _fwdStateCount; ++i)
		{
			_fwdRecognitionTable[i] = fwdTagTable[i] + 1;
		}

		// Allocate per-recognition-code tables

		_fwdContextSplit = new boolean[_fwdRecognitionCount][];

		_fwdTokenList = new int[_fwdRecognitionCount][];

		_fwdTokenListLength = new int[_fwdRecognitionCount];

		// Loop over recognition codes ...

		for (int i = 0; i < _fwdRecognitionCount; ++i)
		{

			// Allocate second-level context split table

			_fwdContextSplit[i] = new boolean[_contextCount];

			// Initialize all entries to context split not recognized

			for (int j = 0; j < _contextCount; ++j)
			{
				_fwdContextSplit[i][j] = false;
			}

			// Count the number of tokens recognized

			int tokIndex = 0;

			if (i != 0)
			{
				for (int j = 0; j < fwdMergeTable[i-1].length; ++j)
				{
					if (fwdMergeTable[i-1][j] < internalContextSplitOffset)
					{
						++tokIndex;
					}
				}
			}

			// Allocate the token recognition list

			_fwdTokenListLength[i] = tokIndex;

			_fwdTokenList[i] = new int[tokIndex];

			// Transfer merge table entries into the per-recognition-code tables

			tokIndex = 0;

			if (i != 0)
			{
				for (int j = 0; j < fwdMergeTable[i-1].length; ++j)
				{
					if (fwdMergeTable[i-1][j] < internalContextSplitOffset)
					{
						_fwdTokenList[i][tokIndex++] = fwdMergeTable[i-1][j];
					}
					else
					{
						_fwdContextSplit[i][_contextNumber[
							fwdMergeTable[i-1][j] - internalContextSplitOffset ]] = true;
					}
				}
			}

		}	// end loop over recognition codes
				
		// Report progress
					
		statusWork ();

		// Get the reverse final state tables

		int[] revTagTable = _revDFA.tagTable();

		int[][] revMergeTable = _revDFA.mergeTable();

		// The number of recognition codes is the length of the merge table, plus 1

		_revRecognitionCount = revMergeTable.length + 1;

		// Our state recognition code is the tag table value, plus 1

		_revRecognitionTable = new int[_revStateCount];

		for (int i = 0; i < _revStateCount; ++i)
		{
			_revRecognitionTable[i] = revTagTable[i] + 1;
		}

		// Allocate per-recognition-code tables

		_revContextSplit = new boolean[_revRecognitionCount][];

		// Loop over recognition codes ...

		for (int i = 0; i < _revRecognitionCount; ++i)
		{

			// Allocate second-level context split table

			_revContextSplit[i] = new boolean[_contextCount];

			// Initialize all entries to token not recognized

			for (int j = 0; j < _contextCount; ++j)
			{
				_revContextSplit[i][j] = false;
			}

			// Transfer merge table entries into the per-recognition-code tables

			if (i != 0)
			{
				for (int j = 0; j < revMergeTable[i-1].length; ++j)
				{
					_revContextSplit[i][_contextNumber[revMergeTable[i-1][j]]] = true;
				}
			}

		}	// end loop over recognition codes

		// If error, return

		if (_error)
		{
			return;
		}

		// If verbose mode, print the table ...

		if (_verbose)
		{

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Start conditions:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of start conditions");

			// Write condition entries

			for (int i = 0; i < _conditionCount; ++i)
			{
				
				// Report progress
				
				statusWork ();

				// Write condition name

				reportError (ErrorOutput.typeInformational, null,
					"  " + _conditionNames[i] );

				// Write initial DFA state

				reportError (ErrorOutput.typeInformational, null,
					"    Initial forward DFA state: " + _fwdInitialState[i] );

				reportError (ErrorOutput.typeInformational, null,
					"" );
			}

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Tokens:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of tokens");

			// Enumerate tokens

			for (Enumeration e = _tokenTable.elements(); e.hasMoreElements(); )
			{
				
				// Report progress
				
				statusWork ();

				// Next token to print

				ParserGeneratorToken tok = (ParserGeneratorToken) e.nextElement();

				// Write token number

				reportError (ErrorOutput.typeInformational, null,
					"  Token " + tok._number );

				// Allocate buffer to build the token definition

				StringBuffer buf = new StringBuffer ();

				// Append the token name

				buf.append ("    ");
				buf.append (tok._name);
				buf.append (" = ");

				// Append the token expression

				tok._tokenExp.getDescription (buf);

				// If there is a context expression, append it

				if (tok._contextExp != null)
				{
					buf.append (" / ");
					tok._contextExp.getDescription (buf);
				}

				// Write the token definition

				reportError (ErrorOutput.typeInformational, null,
					buf.toString() );

				// If there is a context expression, write the context number

				if (tok._contextExp != null)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    Context number: " + tok._ctxNumber );
				}

				// If there is a link name, write it

				if (tok._linkName != null)
				{
					reportError (ErrorOutput.typeInformational, null,
						"    Link name: '" + tok._linkName + "'" );
				}

				// Write the parameter

				reportError (ErrorOutput.typeInformational, null,
					"    Parameter: " + _tokenParam[tok._number] );

				// If there is a condition set, write it

				if (tok._conditionSet != null)
				{

					// Allocate a buffer to build the set

					StringBuffer buf2 = new StringBuffer ();

					buf2.append ("    Conditions:");

					// For each element in the set, append the string

					for (Enumeration ce = tok._conditionSet.elements(); ce.hasMoreElements(); )
					{
						buf2.append (" ");
						buf2.append ((String) ce.nextElement());
					}

					// Write the condition list

					reportError (ErrorOutput.typeInformational, null,
						buf2.toString() );
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over tokens

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Forward DFA States:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of forward DFA states");

			// Write states

			for (int i = 0; i < _fwdStateCount; ++i)
			{
				
				// Report progress
				
				statusWork ();

				// Write the state number

				reportError (ErrorOutput.typeInformational, null,
					"  State " + i );

				// Set of target states

				SmallIntSet targetStates = new SmallIntSet ();

				// Scan transition table and accumulate target states

				for (int j = 0; j < _calculatedCategoryCount; ++j)
				{
					if (_fwdTransitionTable[i][j] != _fwdStateCount)
					{
						targetStates.addElement (_fwdTransitionTable[i][j]);
					}
				}

				// Enumerate target states

				for (IntEnumeration ie = targetStates.elements(); ie.hasMoreElements(); )
				{

					// Get target state

					int target = ie.nextElement();

					// Buffer to accumulate categories

					StringBuffer buf = new StringBuffer ();

					buf.append ("    Goto ");
					buf.append (target);
					buf.append (":");

					// Scan transition table and accumulate categories for this target state

					for (int j = 0; j < _calculatedCategoryCount; ++j)
					{
						if (_fwdTransitionTable[i][j] == target)
						{
							buf.append (" ");
							buf.append (j);
						}
					}

					// Write categories for this target state

					reportError (ErrorOutput.typeInformational, null,
						buf.toString() );
				}

				// Write recognition code

				reportError (ErrorOutput.typeInformational, null,
					"    Recognition code: " + _fwdRecognitionTable[i] );

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over forward DFA states

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Forward DFA Recognition Codes:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write recognition codes

			for (int i = 0; i < _fwdRecognitionCount; ++i)
			{
				
				// Report progress
				
				statusWork ();

				// Write the recognition code number

				reportError (ErrorOutput.typeInformational, null,
					"  Recognition code " + i );

				// If there are tokens recognized ...

				if (_fwdTokenListLength[i] != 0)
				{

					// Buffer to accumulate tokens

					StringBuffer buf = new StringBuffer ();

					buf.append ("    Tokens: ");

					// Append the token numbers

					for (int j = 0; j < _fwdTokenListLength[i]; ++j)
					{
						buf.append (" ");
						buf.append (_fwdTokenList[i][j]);
					}

					// Write tokens for this recognition code

					reportError (ErrorOutput.typeInformational, null,
						buf.toString() );
				}

				// Scan for context splits

				for (int j = 0; j < _contextCount; ++j)
				{

					// If we found a context split ...

					if (_fwdContextSplit[i][j])
					{

						// Buffer to accumulate context splits

						StringBuffer buf = new StringBuffer ();

						buf.append ("    Context splits: ");

						// Append the context splits

						for ( ; j < _contextCount; ++j)
						{
							if (_fwdContextSplit[i][j])
							{
								buf.append (" ");
								buf.append (j);
							}
						}

						// Write context splits for this recognition code

						reportError (ErrorOutput.typeInformational, null,
							buf.toString() );

						break;
					}
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over forward recognition codes

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Reverse DFA States:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );
		
			statusStage ("Writing verbose list of reverse DFA states");

			// Write states

			for (int i = 0; i < _revStateCount; ++i)
			{
				
				// Report progress
				
				statusWork ();

				// Write the state number

				reportError (ErrorOutput.typeInformational, null,
					"  State " + i );

				// Set of target states

				SmallIntSet targetStates = new SmallIntSet ();

				// Scan transition table and accumulate target states

				for (int j = 0; j < _calculatedCategoryCount; ++j)
				{
					if (_revTransitionTable[i][j] != _revStateCount)
					{
						targetStates.addElement (_revTransitionTable[i][j]);
					}
				}

				// Enumerate target states

				for (IntEnumeration ie = targetStates.elements(); ie.hasMoreElements(); )
				{

					// Get target state

					int target = ie.nextElement();

					// Buffer to accumulate categories

					StringBuffer buf = new StringBuffer ();

					buf.append ("    Goto ");
					buf.append (target);
					buf.append (":");

					// Scan transition table and accumulate categories for this target state

					for (int j = 0; j < _calculatedCategoryCount; ++j)
					{
						if (_revTransitionTable[i][j] == target)
						{
							buf.append (" ");
							buf.append (j);
						}
					}

					// Write categories for this target state

					reportError (ErrorOutput.typeInformational, null,
						buf.toString() );
				}

				// Write recognition code

				reportError (ErrorOutput.typeInformational, null,
					"    Recognition code: " + _revRecognitionTable[i] );

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over reverse DFA states

			// Write title line

			reportError (ErrorOutput.typeInformational, null,
				"Reverse DFA Recognition Codes:" );

			reportError (ErrorOutput.typeInformational, null,
				"" );

			// Write recognition codes

			for (int i = 0; i < _revRecognitionCount; ++i)
			{
				
				// Report progress
				
				statusWork ();

				// Write the recognition code number

				reportError (ErrorOutput.typeInformational, null,
					"  Recognition code " + i );

				// Scan for context splits

				for (int j = 0; j < _contextCount; ++j)
				{

					// If we found a context split ...

					if (_revContextSplit[i][j])
					{

						// Buffer to accumulate context splits

						StringBuffer buf = new StringBuffer ();

						buf.append ("    Context splits: ");

						// Append the context splits

						for ( ; j < _contextCount; ++j)
						{
							if (_revContextSplit[i][j])
							{
								buf.append (" ");
								buf.append (j);
							}
						}

						// Write context splits for this recognition code

						reportError (ErrorOutput.typeInformational, null,
							buf.toString() );

						break;
					}
				}

				reportError (ErrorOutput.typeInformational, null,
					"" );

			}	// end loop over reverse recognition codes

		}	// end if verbose

		// Write summary data

		reportError (ErrorOutput.typeInformational, null,
			_conditionCount + " start conditions." );

		reportError (ErrorOutput.typeInformational, null,
			_tokenCount + " tokens." );

		reportError (ErrorOutput.typeInformational, null,
			_fwdStateCount + " states in the forward DFA." );

		reportError (ErrorOutput.typeInformational, null,
			_fwdRecognitionCount + " final state recognition codes in the forward DFA." );

		reportError (ErrorOutput.typeInformational, null,
			_revStateCount + " states in the reverse DFA." );

		reportError (ErrorOutput.typeInformational, null,
			_revRecognitionCount + " final state recognition codes in the reverse DFA." );

		reportError (ErrorOutput.typeInformational, null,
			"" );

		// Successful DFA construction

		return;
	}




	// Read the grammar specification and store it within the ParserGenerator
	// object.  After calling this function, you can call makeScannerTable()
	// and makeParserTable() to create the scanner and parser tables.
	//
	// The parameters are as follows:
	//
	// errorOutput - Destination for all output, including error messages.
	//
	// verbose - true to enable verbose mode, where ParserGenerator writes
	//	out a text description of all the generated tables.  (Warning:  this
	//	produces a lot of output.)
	//
	// stream - Input source, from which ParserGenerator reads the grammar
	//	description.  It is expected to be byte-oriented, that is, one
	//	character per byte.
	//
	// filename - A string that is used in all error messages to identify
	//	the source file.  This can be null.
	//
	// generatorStatus - Destination of progress reports and error summary
	//	information, and source of interrupts.  This can be null.
	//
	// The return value is true if there was an error in the grammar
	// specification.
	//
	// On return, the stream is always closed.

	public boolean generate (ErrorOutput errorOutput, boolean verbose,
		InputStream stream, String filename, GeneratorStatus generatorStatus)
		throws InterruptedCompilerException
	{
		
		// Validate parameters
		
		if (errorOutput == null)
		{
			throw new IllegalArgumentException ("ParserGenerator.generate");
		}
		
		if (stream == null)
		{
			throw new IllegalArgumentException ("ParserGenerator.generate");
		}

		// Save parameters

		_errorOutput = errorOutput;
		_verbose = verbose;
		_generatorStatus = generatorStatus;
		
		// Error flags for this operation
		
		_errorFlags = GenObserver.efGrammarSpec;

		// Initialize

		initParse ();
		
		// Report that we're starting
		
		reportError (ErrorOutput.typeInformational, null,
			"Reading grammar specification ..." );
		
		try
		{
			statusStage ("Reading grammar specification");
		}
		
		// In case of interrupt, close the input stream
		
		catch (InterruptedCompilerException e)
		{
			_error = true;
		
			try
			{
				stream.close();
			}
			catch (Exception e2)
			{
			}
			
			throw e;
		}

		// Get our input source

		PrescannerByte scanSource = new PrescannerByteStream (stream);

		// Get our scanner table

		ScannerTable scanTable = new JaccGrammarScannerTable ();

		// Link the token factories

		scanTable.linkFactory ("unknownKeyword", "", new PGKFUnknownKeyword (this));

		scanTable.linkFactory ("number", "decimal", new PGKFNumberDecimal (this));
		scanTable.linkFactory ("number", "hex", new PGKFNumberHex (this));
		scanTable.linkFactory ("number", "illegal", new PGKFNumberIllegal (this));

		scanTable.linkFactory ("identifier", "unquoted", new PGKFIdentifierUnquoted (this));
		scanTable.linkFactory ("identifier", "quoted", new PGKFIdentifierQuoted (this));

		scanTable.linkFactory ("beginComment", "", new PGKFBeginComment (this));

		scanTable.linkFactory ("endComment", "", new PGKFEndComment (this));

		scanTable.linkFactory ("lineEnd", "", new PGKFLineEnd (this));

		scanTable.linkFactory ("illegalChar", "", new PGKFIllegalChar (this));

		// Enable tracing if debug mode

		if (_debug)
		{
			scanTable.setTrace (_errorOutput);
		}

		// If debug mode, check the invariant

		if (_debug)
		{
			String inv = scanTable.checkInvariant();

			if (inv != null)
			{
				_errorFlags = GenObserver.efInternalError;
					
				reportError (ErrorOutput.typeError, null,
					"Internal scanner table invariant violation: " + inv + "." );
				
				reportError (ErrorOutput.typeInformational, null,
					"" );
		
				try
				{
					stream.close();
				}
				catch (Exception e2)
				{
				}

				return _error;
			}
		}

		// Link condition numbers

		conditionNotInComment = scanTable.lookupCondition ("notInComment");
		conditionInComment = scanTable.lookupCondition ("inComment");

		// Create our scanner

		Scanner scanner = 
			Scanner.makeScanner (this, scanSource, scanTable, filename, 1, 1, 4000, null);

		// Read all the input

		/*->
		try
		{
			for ( ; ; )
			{
				Token token = scanner.nextToken();
				if (token.number == Token.EOF)
				{
					break;
				}
			}
		}

		catch (IOException e)
		{
			reportError (ErrorOutput.typeError, null,
				e.toString() + "." );
		}

		catch (SyntaxException e)
		{
			reportError (ErrorOutput.typeError, null,
				e.toString() + "." );
		}
		->*/

		// Create our preprocessor

		Preprocessor parseSource = new PreprocessorInclude (scanner);

		// Get our parser table

		ParserTable parseTable = new JaccGrammarParserTable ();

		// Link the nonterminal factories

		parseTable.linkFactory ("OptionDefinition", "LR1", new PGNFOptionDefinitionLR1 (this));
		parseTable.linkFactory ("OptionDefinition", "PLR1", new PGNFOptionDefinitionPLR1 (this));
		parseTable.linkFactory ("OptionDefinition", "LALR1", new PGNFOptionDefinitionLALR1 (this));
		parseTable.linkFactory ("OptionDefinition", "repair", new PGNFOptionDefinitionRepair (this));
		parseTable.linkFactory ("OptionDefinition", "charsetsize", new PGNFOptionDefinitionCharsetsize (this));
		parseTable.linkFactory ("OptionDefinition", "goal", new PGNFOptionDefinitionGoal (this));
		parseTable.linkFactory ("OptionDefinition", "java", new PGNFOptionDefinitionJava (this));

		parseTable.linkFactory ("JavaName", "simple", new PGNFJavaNameSimple (this));
		parseTable.linkFactory ("JavaName", "qualified", new PGNFJavaNameQualified (this));

		parseTable.linkFactory ("JavaIdentifier", "", new PGNFJavaIdentifier (this));

		parseTable.linkFactory ("TerminalDefinition", "defaultCost", new PGNFTerminalDefinitionDefaultCost (this));
		parseTable.linkFactory ("TerminalDefinition", "withCost", new PGNFTerminalDefinitionWithCost (this));

		parseTable.linkFactory ("ProductionDefinition", "", new PGNFProductionDefinition (this));

		parseTable.linkFactory ("LinkName", "empty", new PGNFLinkNameEmpty (this));
		parseTable.linkFactory ("LinkName", "identifier", new PGNFLinkNameIdentifier (this));

		parseTable.linkFactory ("Parameter", "empty", new PGNFParameterEmpty (this));
		parseTable.linkFactory ("Parameter", "number", new PGNFParameterNumber (this));

		parseTable.linkFactory ("SymbolList", "empty", new PGNFSymbolListEmpty (this));
		parseTable.linkFactory ("SymbolList", "append", new PGNFSymbolListAppend (this));
		
		parseTable.linkFactory ("ProductionPrec", "empty", new PGNFProductionPrecEmpty (this));
		parseTable.linkFactory ("ProductionPrec", "appendShift", new PGNFProductionPrecAppendShift (this));
		parseTable.linkFactory ("ProductionPrec", "appendReduce", new PGNFProductionPrecAppendReduce (this));
		
		parseTable.linkFactory ("SymbolSet", "empty", new PGNFSymbolSetEmpty (this));
		parseTable.linkFactory ("SymbolSet", "append", new PGNFSymbolSetAppend (this));
		
		parseTable.linkFactory ("CategoryDefinition", "", new PGNFCategoryDefinition (this));
		
		parseTable.linkFactory ("CatExp", "number", new PGNFCatExpNumber (this));
		parseTable.linkFactory ("CatExp", "identifier", new PGNFCatExpIdentifier (this));
		parseTable.linkFactory ("CatExp", "numberRange", new PGNFCatExpNumberRange (this));
		parseTable.linkFactory ("CatExp", "identifierRange", new PGNFCatExpIdentifierRange (this));
		parseTable.linkFactory ("CatExp", "any", new PGNFCatExpAny (this));
		parseTable.linkFactory ("CatExp", "none", new PGNFCatExpNone (this));
		parseTable.linkFactory ("CatExp", "unicode", new PGNFCatExpUnicode (this));
		parseTable.linkFactory ("CatExp", "uppercase", new PGNFCatExpUppercase (this));
		parseTable.linkFactory ("CatExp", "lowercase", new PGNFCatExpLowercase (this));
		parseTable.linkFactory ("CatExp", "titlecase", new PGNFCatExpTitlecase (this));
		parseTable.linkFactory ("CatExp", "letter", new PGNFCatExpLetter (this));
		parseTable.linkFactory ("CatExp", "digit", new PGNFCatExpDigit (this));
		parseTable.linkFactory ("CatExp", "paren", new PGNFCatExpParen (this));
		parseTable.linkFactory ("CatExp", "difference", new PGNFCatExpDifference (this));
		parseTable.linkFactory ("CatExp", "intersection", new PGNFCatExpIntersection (this));
		parseTable.linkFactory ("CatExp", "union", new PGNFCatExpUnion (this));
		
		parseTable.linkFactory ("ConditionDefinition", "", new PGNFConditionDefinition (this));
		
		parseTable.linkFactory ("TokenHeader", "", new PGNFTokenHeader (this));
		
		parseTable.linkFactory ("ConditionSet", "empty", new PGNFConditionSetEmpty (this));
		parseTable.linkFactory ("ConditionSet", "append", new PGNFConditionSetAppend (this));
		
		parseTable.linkFactory ("TokenDefinition", "noContext", new PGNFTokenDefinitionNoContext (this));
		parseTable.linkFactory ("TokenDefinition", "rightContext", new PGNFTokenDefinitionRightContext (this));
		
		parseTable.linkFactory ("RegExp", "oneChar", new PGNFRegExpOneChar (this));
		parseTable.linkFactory ("RegExp", "paren", new PGNFRegExpParen (this));
		parseTable.linkFactory ("RegExp", "KleeneClosure", new PGNFRegExpKleeneClosure (this));
		parseTable.linkFactory ("RegExp", "positiveClosure", new PGNFRegExpPositiveClosure (this));
		parseTable.linkFactory ("RegExp", "optionalClosure", new PGNFRegExpOptionalClosure (this));
		parseTable.linkFactory ("RegExp", "catenation", new PGNFRegExpCatenation (this));
		parseTable.linkFactory ("RegExp", "difference", new PGNFRegExpDifference (this));
		parseTable.linkFactory ("RegExp", "intersection", new PGNFRegExpIntersection (this));
		parseTable.linkFactory ("RegExp", "excluding", new PGNFRegExpExcluding (this));
		parseTable.linkFactory ("RegExp", "including", new PGNFRegExpIncluding (this));
		parseTable.linkFactory ("RegExp", "alternation", new PGNFRegExpAlternation (this));

		// Enable tracing if debug mode

		if (_debug)
		{
			parseTable.setTrace (_errorOutput);
		}

		// If debug mode, check the invariant

		if (_debug)
		{
			String inv = parseTable.checkInvariant();

			if (inv != null)
			{
				_errorFlags = GenObserver.efInternalError;
					
				reportError (ErrorOutput.typeError, null,
					"Internal parser table invariant violation: " + inv + "." );
				
				reportError (ErrorOutput.typeInformational, null,
					"" );
		
				try
				{
					stream.close();
				}
				catch (Exception e2)
				{
				}

				return _error;
			}
		}

		// Create our parser

		Parser parser = new Parser (this, parseTable, null);

		// Parse the source

		parser.parse (parseSource);
		
		// If we were interrupted, re-throw the exception
		
		if (_interruptException != null)
		{
			throw _interruptException;
		}
		
		// Final line of output
		
		reportError (ErrorOutput.typeInformational, null,
			"" );
		
		// Return result

		return _error;
	}




	// This private function clears all the generated scanner tables

	private void clearScannerTable()
	{
		_categoryNames = null;
		_charGroups = null;
		_calculatedCategorySets = null;
		_CCT = null;
		_calculatedCategoryTable = null;
		_calculatedCategoryCount = 0;
		_conditionNames = null;
		_tokenNames = null;
		_tokenParam = null;
		_tokenLink = null;
		_contextNumber = null;
		_fwdTokenDef = null;
		_revTokenDef = null;
		_fwdDFA = null;
		_revDFA = null;
		_fwdStateCount = 0;
		_revStateCount = 0;
		_fwdTransitionTable = null;
		_revTransitionTable = null;
		_fwdInitialState = null;
		_fwdRecognitionCount = 0;
		_revRecognitionCount = 0;
		_fwdRecognitionTable = null;
		_revRecognitionTable = null;
		_fwdContextSplit = null;
		_revContextSplit = null;
		_fwdTokenListLength = null;
		_fwdTokenList = null;

		return;
	}




	// This function constructs a ScannerTable object containing the tables
	// we generated.
	//
	// If an error is encountered, the function returns null.

	public ScannerTable makeScannerTable () throws InterruptedCompilerException
	{

		// If error, just return

		if (_error)
		{
			return null;
		}
		
		// Error flags for this operation
		
		_errorFlags = GenObserver.efScannerTable;

		// Clear scanner tables

		clearScannerTable();

		// Make the character category table

		makeCategories();

		if (_error)
		{
			_error = false;
			clearScannerTable();

			return null;
		}

		// Make the token DFA's

		makeTokens();

		if (_error)
		{
			_error = false;
			clearScannerTable();

			return null;
		}

		// Create an empty ScannerTable object

		ScannerTable scannerTable = new ScannerTable ();

		// Copy scanner tables

		scannerTable._categoryCount = _calculatedCategoryCount;
		scannerTable._charSetSize = _charSetSize;
		scannerTable._categoryTable = _calculatedCategoryTable;
		scannerTable._tokenCount = _tokenCount;
		scannerTable._tokenParam = _tokenParam;
		scannerTable._contextCount = _contextCount;
		scannerTable._contextNumber = _contextNumber;
		scannerTable._conditionCount = _conditionCount;
		scannerTable._fwdStateCount = _fwdStateCount;
		scannerTable._fwdRecognitionCount = _fwdRecognitionCount;
		scannerTable._fwdInitialState = _fwdInitialState;
		scannerTable._fwdTransitionTable = _fwdTransitionTable;
		scannerTable._fwdRecognitionTable = _fwdRecognitionTable;
		scannerTable._fwdTokenListLength = _fwdTokenListLength;
		scannerTable._fwdTokenList = _fwdTokenList;
		scannerTable._fwdContextSplit = _fwdContextSplit;
		scannerTable._revStateCount = _revStateCount;
		scannerTable._revRecognitionCount = _revRecognitionCount;
		scannerTable._revTransitionTable = _revTransitionTable;
		scannerTable._revRecognitionTable = _revRecognitionTable;
		scannerTable._revContextSplit = _revContextSplit;

		// Copy the dynamic-link tables

		scannerTable._conditionNames = _conditionNames;
		scannerTable._tokenNames = _tokenNames;
		scannerTable._tokenLink = _tokenLink;

		// Clear scanner tables

		clearScannerTable();

		// As a final check, check the invariant

		String inv = scannerTable.checkInvariant();

		if (inv != null)
		{
			_errorFlags = GenObserver.efInternalError | GenObserver.efScannerTable;
				
			reportError (ErrorOutput.typeError, null,
				"Scanner table invariant violation: " + inv + "." );
		
			reportError (ErrorOutput.typeInformational, null,
				"" );

			_error = false;

			return null;
		}

		// Return the scanner table object

		return scannerTable;
	}




	// This private function clears all the generated parser tables

	private void clearParserTable()
	{
		_symbols = null;
		_productions = null;
		_insertionCost = null;
		_deletionCost = null;
		_productionParam = null;
		_productionLink = null;
		_productionLHSSymbol = null;
		_productionRHSLength = null;
		_CFG = null;
		_productionCost = null;
		_shiftSet = null;
		_reduceSet = null;
		_singlePointInsertionCount = 0;
		_singlePointInsertions = null;
		_machine = null;
		_actionTable = null;
		_unwindingTable = null;
		_stateCount = 0;
		_cognateNumber = null;
		_productionCostPassCount = 0;

		return;
	}




	// This function constructs a ParserTable object containing the tables
	// we generated.
	//
	// If an error is encountered, the function returns null.

	public ParserTable makeParserTable () throws InterruptedCompilerException
	{

		// If error, just return

		if (_error)
		{
			return null;
		}
		
		// Error flags for this operation
		
		_errorFlags = GenObserver.efParserTable;

		// Clear parser tables

		clearParserTable();

		// Make the LR(1) machine

		makeLR1();

		if (_error)
		{
			_error = false;
			clearParserTable();

			return null;
		}

		// Create an empty ParserTable object

		ParserTable parserTable = new ParserTable ();

		// Copy parser tables

		parserTable._symbolCount = _symbolCount;
		parserTable._productionCount = _productionCount;
		parserTable._productionLHSSymbol = _productionLHSSymbol;
		parserTable._productionRHSLength = _productionRHSLength;
		parserTable._productionParam = _productionParam;
		parserTable._maxInsertion = _maxInsertion;
		parserTable._maxDeletion = _maxDeletion;
		parserTable._validationLength = _validationLength;
		parserTable._singlePointInsertionCount = _singlePointInsertionCount;
		parserTable._singlePointInsertions = _singlePointInsertions;
		parserTable._goalProduction = _goalProduction;
		parserTable._eofSymbol = _eofSymbol;
		parserTable._insertionCost = _insertionCost;
		parserTable._deletionCost = _deletionCost;
		parserTable._stateCount = _stateCount;
		parserTable._actionTable = _actionTable;
		parserTable._unwindingTable = _unwindingTable;

		// Copy the dynamic-link tables

		parserTable._symbols = _symbols;
		parserTable._productionLink = _productionLink;

		// Clear parser tables

		clearParserTable();

		// As a final check, check the invariant

		String inv = parserTable.checkInvariant();

		if (inv != null)
		{
			_errorFlags = GenObserver.efInternalError | GenObserver.efParserTable;
				
			reportError (ErrorOutput.typeError, null,
				"Parser table invariant violation: " + inv + "." );
		
			reportError (ErrorOutput.typeInformational, null,
				"" );

			_error = false;

			return null;
		}

		// Return the parser table object

		return parserTable;
	}




	// This function retrieves the Java name from the grammar specification,
	// or null if there was no %java statement in the grammar specification.
	//
	// If an error is encountered, the function returns null.

	public String getJavaName () throws InterruptedCompilerException
	{

		// If error, just return

		if (_error)
		{
			return null;
		}
		
		// Return the Java name, or null if no %java statement
		
		return _javaName;
	}




}




// ParserGeneratorBootstrap wraps a set of functions that are used to create
// bootstrap parsing and scanning tables.  The bootstrap tables are sufficient
// to parse the Jacc language specification, which in turn creates the full
// parsing and scanning tables.

final class ParserGeneratorBootstrap
{


	// This function creates a ParserGenerator object that contains a language
	// definition sufficient to parse the Jacc language specification.

	public static ParserGenerator bootstrap (ErrorOutput errorOutput, boolean verbose)
	{

		// Create the parser generator object

		ParserGenerator generator = new ParserGenerator();

		// Save parameters

		generator._errorOutput = errorOutput;
		generator._verbose = verbose;
		generator._generatorStatus = null;

		// Initialize

		generator.initParse ();

		// Generate all options

		bootstrapMakeOptions (generator);

		// Generate all terminal symbols

		bootstrapMakeTerminals (generator);

		// Generate all productions

		bootstrapMakeProductions (generator);

		// Generate all character categories

		bootstrapMakeCategories (generator);

		// Generate all conditions

		bootstrapMakeConditions (generator);

		// Generate all tokens

		bootstrapMakeTokens (generator);

		// Successful construction

		return generator;
	}




	// This function is used in bootstrapping the parser.  It creates the
	// options.

	static void bootstrapMakeOptions (ParserGenerator generator)
	{

		// Shrink the character set size

		generator._charSetSize = CharCategoryTable.charSetSizeASCII;
		
		// Set the Java name
		
		generator._javaName = "invisible.jacc.gen.JaccGrammar";
		
		return;
	}




	// This function is used in bootstrapping the parser.  It creates the
	// terminal symbols and adds them to the symbol table.

	static void bootstrapMakeTerminals (ParserGenerator generator)
	{

		// Loop over definitions

		for (int def = 0; def < bootstrapTerminals.length; ++def)
		{

			// Create a string tokenizer for this definition

			StringTokenizer tokenizer = new StringTokenizer (bootstrapTerminals[def]);

			// Get token

			String token = tokenizer.nextToken();

			// If the token is quoted, remove the quotes

			if ((token.length() >= 2)
				&& (token.charAt (0) == '\'')
				&& (token.charAt (token.length() - 1) == '\'') )
			{
				token = token.substring (1, token.length() - 1);
			}

			// Create the terminal symbol

			ParserGeneratorSymbol sym = generator.createTerminal (token, 1, 1);

			// Report any error

			if (sym == null)
			{
				generator.reportError (ErrorOutput.typeError, null,
					"Duplicate terminal symbol '" + token + "'." );
			}

		}	// end loop over definitions

		return;
	}


	// Terminal definitions for bootstrapping.
	//
	// A definition must consist of a symbol name, which may be quoted or unquoted.

	static String[] bootstrapTerminals = 
		{
			"'-'",
			"'~'",
			"'&'",
			"'@'",
			"'*'",
			"'+'",
			"'?'",
			"'/'",
			"':'",
			"';'",
			"'='",
			"'('",
			"')'",
			"'{'",
			"'}'",
			"'|'",
			"'#'",
			"'.'",
			"'->'",
			"'..'",

			"'%tokens'",
			"'%categories'",
			"'%conditions'",
			"'%terminals'",
			"'%productions'",
			"'%shift'",
			"'%reduce'",
			"'%options'",
			"'%repair'",
			"'%lr1'",
			"'%plr1'",
			"'%lalr1'",
			"'%any'",
			"'%none'",
			"'%unicode'",
			"'%uppercase'",
			"'%lowercase'",
			"'%titlecase'",
			"'%letter'",
			"'%digit'",
			"'%charsetsize'",
			"'%goal'",
			"'%java'",

			"identifier",
			"number"
		};




	// This function is used in bootstrapping the parser.  It creates the
	// productions and adds them to the production table.

	static void bootstrapMakeProductions (ParserGenerator generator)
	{

		// Loop over definitions

		for (int def = 0; def < bootstrapProductions.length; ++def)
		{

			// Create a string tokenizer for this definition

			StringTokenizer tokenizer = new StringTokenizer (bootstrapProductions[def]);

			// Create a production for this definition

			ParserGeneratorProduction production = generator.createProduction ();

			production._linkName = null;
			production._parameter = null;
			production._rhs = new ObjectDeque ();

			// State variable:
			// 0 = reading lhs
			// 1 = reading link name
			// 2 = reading rhs
			// 3 = reading shift list
			// 4 = reading reduce list

			int state = 0;

			// Loop until tokenizer runs out of tokens

			while (tokenizer.hasMoreTokens())
			{

				// Get token

				String token = tokenizer.nextToken();

				// Process '{'

				if (token.equals ("{"))
				{
					state = 1;
				}

				// Process '}'

				else if (token.equals ("}"))
				{
					state = 2;
				}

				// Process '->'

				else if (token.equals ("->"))
				{
					state = 2;
				}

				// Process '%shift'

				else if (token.equals ("%shift"))
				{
					state = 3;
				}

				// Process '%reduce'

				else if (token.equals ("%reduce"))
				{
					state = 4;
				}

				// Otherwise, it must be a symbol or number

				else
				{

					// If the token is quoted, remove the quotes

					if ((token.length() >= 2)
						&& (token.charAt (0) == '\'')
						&& (token.charAt (token.length() - 1) == '\'') )
					{
						token = token.substring (1, token.length() - 1);
					}

					// Switch on state

					switch (state)
					{

					case 0:

						// Token is left hand side

						production._lhs = generator.getOrCreateSymbol (token);
						break;

					case 1:

						// Token is link name

						production._linkName = token;
						break;

					case 2:

						// Token is in the right hand side

						production._rhs.pushLast (generator.getOrCreateSymbol (token));
						break;

					case 3:

						// Token is in the shift set

						production._shift.addElement (generator.getOrCreateSymbol (token));
						break;

					case 4:

						// Token is in the reduce set

						production._reduce.addElement (generator.getOrCreateSymbol (token));
						break;
					}
				}

			}	// end loop until tokenizer runs out of tokens

		}	// end loop over definitions

		return;
	}


	// Production definitions for bootstrapping.
	//
	// The tokens within each definition must be separated by white space.
	// No integer parameters are allowed.

	static String[] bootstrapProductions = 
		{

			"Goal -> SectionList",

			"SectionList -> Section",

			"SectionList -> SectionList Section",

			"Section -> OptionHeader OptionDefList",

			"Section -> TerminalHeader TerminalDefList",

			"Section -> ProductionHeader ProductionDefList",

			"Section -> CategoryHeader CategoryDefList",

			"Section -> ConditionHeader ConditionDefList",

			"Section -> TokenHeader TokenDefList",

			"OptionHeader -> '%options' ':'",

			"OptionDefList ->",

			"OptionDefList -> OptionDefList OptionDefinition",

			"OptionDefinition { LR1 } -> '%lr1' ';'",

			"OptionDefinition { PLR1 } -> '%plr1' ';'",

			"OptionDefinition { LALR1 } -> '%lalr1' ';'",

			"OptionDefinition { repair } -> '%repair' MaxInsertions MaxDeletions ValidationLength ';'",

			"OptionDefinition { charsetsize } -> '%charsetsize' CharSetSize ';'",

			"OptionDefinition { goal } -> '%goal' Symbol ';'",

			"OptionDefinition { java } -> '%java' JavaName ';'",

			"MaxInsertions -> number",

			"MaxDeletions -> number",

			"ValidationLength -> number",

			"CharSetSize -> number",

			"Symbol -> identifier",

			"JavaName { simple } -> JavaIdentifier",

			"JavaName { qualified } -> JavaName '.' JavaIdentifier",

			"JavaIdentifier -> identifier",

			"TerminalHeader -> '%terminals' ':'",

			"TerminalDefList ->",

			"TerminalDefList -> TerminalDefList TerminalDefinition",

			"TerminalDefinition { defaultCost } -> Symbol ';'",

			"TerminalDefinition { withCost } -> Symbol InsertionCost DeletionCost ';'",

			"InsertionCost -> number",

			"DeletionCost -> number",

			"ProductionHeader -> '%productions' ':'",

			"ProductionDefList ->",

			"ProductionDefList -> ProductionDefList ProductionDefinition",

			"ProductionDefinition -> Symbol LinkName Parameter '->' SymbolList ProductionPrec ';'",

			"LinkName { empty } ->",

			"LinkName { identifier } -> '{' identifier '}'",

			"Parameter { empty } ->",

			"Parameter { number } -> '#' number",

			"SymbolList { empty } ->",

			"SymbolList { append } -> SymbolList Symbol",

			"ProductionPrec { empty } ->",

			"ProductionPrec { appendShift } -> ProductionPrec '%shift' SymbolSet",

			"ProductionPrec { appendReduce } -> ProductionPrec '%reduce' SymbolSet",

			"SymbolSet { empty } ->",

			"SymbolSet { append } -> SymbolSet Symbol",

			"CategoryHeader -> '%categories' ':'",

			"CategoryDefList ->",

			"CategoryDefList -> CategoryDefList CategoryDefinition",

			"CategoryDefinition -> Category '=' CatExp ';'",

			"Category -> identifier",

			"CatExp { number } -> number",

			"CatExp { identifier } -> identifier",

			"CatExp { numberRange } -> number '..' number",

			"CatExp { identifierRange } -> identifier '..' identifier",

			"CatExp { any } -> '%any'",

			"CatExp { none } -> '%none'",

			"CatExp { unicode } -> '%unicode'",

			"CatExp { uppercase } -> '%uppercase'",

			"CatExp { lowercase } -> '%lowercase'",

			"CatExp { titlecase } -> '%titlecase'",

			"CatExp { letter } -> '%letter'",

			"CatExp { digit } -> '%digit'",

			"CatExp { paren } -> '(' CatExp ')'",

			"CatExp { difference } -> CatExp '-' CatExp %shift %reduce '-' '&' '|'",

			"CatExp { intersection } -> CatExp '&' CatExp %shift %reduce '-' '&' '|'",

			"CatExp { union } -> CatExp '|' CatExp %shift '-' '&' %reduce '|'",

			"ConditionHeader -> '%conditions' ':'",

			"ConditionDefList ->",

			"ConditionDefList -> ConditionDefList ConditionDefinition",

			"ConditionDefinition -> Condition ';'",

			"Condition -> identifier",

			"TokenHeader -> '%tokens' ConditionSet ':'",

			"ConditionSet { empty } ->",

			"ConditionSet { append } -> ConditionSet Condition",

			"TokenDefList ->",

			"TokenDefList -> TokenDefList TokenDefinition",

			"TokenDefinition { noContext } -> Token LinkName Parameter '=' RegExp ';'",

			"TokenDefinition { rightContext } -> Token LinkName Parameter '=' RegExp '/' RegExp ';'",

			"Token -> identifier",

			"RegExp { oneChar } -> Category",

			"RegExp { paren } -> '(' RegExp ')'",

			"RegExp { KleeneClosure } -> RegExp '*'",

			"RegExp { positiveClosure } -> RegExp '+'",

			"RegExp { optionalClosure } -> RegExp '?'",

			"RegExp { catenation } -> RegExp RegExp %shift '*' '+' '?' %reduce RegExp '-' '&' '~' '@' '|'",

			"RegExp { difference } -> RegExp '-' RegExp %shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|'",

			"RegExp { intersection } -> RegExp '&' RegExp %shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|'",

			"RegExp { excluding } -> RegExp '~' RegExp %shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|'",

			"RegExp { including } -> RegExp '@' RegExp %shift '*' '+' '?' RegExp %reduce '-' '&' '~' '@' '|'",

			"RegExp { alternation } -> RegExp '|' RegExp %shift '*' '+' '?' RegExp '-' '&' '~' '@' %reduce '|'"

		};




	// This function is used in bootstrapping the parser.  It creates the
	// categories and adds them to the category table.

	static void bootstrapMakeCategories (ParserGenerator generator)
	{

		// Loop over definitions

		for (int def = 0; def < bootstrapCategories.length; ++def)
		{

			// Create a string tokenizer for this definition

			StringTokenizer tokenizer = new StringTokenizer (bootstrapCategories[def]);

			// Predicate stack for this definition

			UnaryIntPredicate predicate1 = null;

			UnaryIntPredicate predicate2 = null;

			// Name for this definition

			String name = null;

			// State variable:
			// 0 = reading name
			// 1 = reading identifier or number after '='
			// 2 = reading identifier or number after '|'
			// 3 = reading identifier or number after '-'

			int state = 0;

			// Loop until tokenizer runs out of tokens

			while (tokenizer.hasMoreTokens())
			{

				// Get token

				String token = tokenizer.nextToken();

				// Process '='

				if (token.equals ("="))
				{
					state = 1;
				}

				// Process '|'

				else if (token.equals ("|"))
				{
					state = 2;
				}

				// Process '-'

				else if (token.equals ("-"))
				{
					state = 3;
				}

				// Process '%any'

				else if (token.equals ("%any"))
				{
					predicate2 = UnaryIntPredicateFactory.always ();
				}

				// Process '%letter'

				else if (token.equals ("%letter"))
				{
					predicate2 = UnaryIntPredicateFactory.letter ();
				}

				// Process '%digit'

				else if (token.equals ("%digit"))
				{
					predicate2 = UnaryIntPredicateFactory.digit ();
				}

				// Process a number

				else if ((token.length() >= 1)
						 && (token.charAt(0) >= '0')
						 && (token.charAt(0) <= '9') )
				{
					predicate2 = UnaryIntPredicateFactory.equal (Integer.parseInt (token));
				}

				// Otherwise, it must be a symbol

				else
				{

					// If the token is quoted, remove the quotes

					if ((token.length() >= 2)
						&& (token.charAt (0) == '\'')
						&& (token.charAt (token.length() - 1) == '\'') )
					{
						token = token.substring (1, token.length() - 1);
					}

					// Create a predicate which accepts any character in the string

					predicate2 = UnaryIntPredicateFactory.string (token);
				}

				// If there is something on top of the stack ...

				if (predicate2 != null)
				{

					// Switch on state

					switch (state)
					{

					case 0:

						// This is really the name

						name = token;
						break;

					case 1:

						// After '='

						predicate1 = predicate2;
						break;

					case 2:

						// After '|'

						predicate1 = UnaryIntPredicateFactory.or (predicate1, predicate2);
						break;

					case 3:

						// After '-'

						predicate1 = UnaryIntPredicateFactory.and (
							predicate1, UnaryIntPredicateFactory.not (predicate2) );
						break;
					}

					// Clear stack top

					predicate2 = null;
				}

			}	// end loop until tokenizer runs out of tokens

			// Create the category

			ParserGeneratorCategory cat = generator.createCategory (name, predicate1);

			// Report any error

			if (cat == null)
			{
				generator.reportError (ErrorOutput.typeError, null,
					"Duplicate category name '" + name + "'." );
			}

		}	// end loop over definitions

		return;
	}


	// Category definitions for bootstrapping.
	//
	// The tokens within each definition must be separated by white space.
	// The only allowed operators are '=', '|', and '-'.
	// The only allowed special symbols are '%any', '%letter', and '%digit'.
	// Numbers must be in decimal.

	static String[] bootstrapCategories = 
		{
			"a = 'a'",
			"b = 'b'",
			"c = 'c'",
			"d = 'd'",
			"e = 'e'",
			"f = 'f'",
			"g = 'g'",
			"h = 'h'",
			"i = 'i'",
			"j = 'j'",
			"k = 'k'",
			"l = 'l'",
			"m = 'm'",
			"n = 'n'",
			"o = 'o'",
			"p = 'p'",
			"q = 'q'",
			"r = 'r'",
			"s = 's'",
			"t = 't'",
			"u = 'u'",
			"v = 'v'",
			"w = 'w'",
			"x = 'x'",
			"y = 'y'",
			"z = 'z'",
			"'%' = '%'",
			"':' = ':'",
			"';' = ';'",
			"'*' = '*'",
			"'+' = '+'",
			"'?' = '?'",
			"'-' = '-'",
			"'>' = '>'",
			"'~' = '~'",
			"'&' = '&'",
			"'@' = '@'",
			"'(' = '('",
			"')' = ')'",
			"'{' = '{'",
			"'}' = '}'",
			"''' = '''",
			"'/' = '/'",
			"'=' = '='",
			"'|' = '|'",
			"'#' = '#'",
			"'.' = '.'",
			"'0' = '0'",
			"'1' = '1'",
			"letter = %letter | '_$'",
			"digit = %digit",
			"letterOrDigit = %letter | '_$' | %digit",
			"space = 9 | 12 | 32",
			"white = 9 | 10 | 12 | 13 | 32",
			"decDigit = '0123456789'",
			"hexDigit = '0123456789ABCDEFabcdef'",
			"xX = 'xX'",
			"notQuote = %any - ''' - 9 - 10 - 12 - 13 - 32",
			"cr = 13",
			"lf = 10",
			"sub = 26",
			"notEol = %any - 10 - 13",
			"any = %any"
		};




	// This function is used in bootstrapping the parser.  It creates the
	// conditions and adds them to the condition table.

	static void bootstrapMakeConditions (ParserGenerator generator)
	{

		// Loop over definitions

		for (int def = 0; def < bootstrapConditions.length; ++def)
		{

			// Create the condition

			ParserGeneratorCondition cond =
				generator.createCondition (bootstrapConditions[def]);

		}	// end loop over definitions

		return;
	}


	// Condition definitions for bootstrapping.
	//
	// Each definition must be the name of a condition, with no white space.

	static String[] bootstrapConditions = 
		{
			"notInComment",
			"inComment"
		};




	// This function is used in bootstrapping the parser.  It creates the
	// tokens and adds them to the token table.

	static void bootstrapMakeTokens (ParserGenerator generator)
	{

		// No conditions

		generator._currentConditionSet = null;

		// Loop over definitions

		for (int def = 0; def < bootstrapTokens.length; ++def)
		{

			// Create a string tokenizer for this definition

			StringTokenizer tokenizer = new StringTokenizer (bootstrapTokens[def]);

			// Regular expression stack for this definition

			ParserGeneratorRegExp exp1 = null;

			ParserGeneratorRegExp exp2 = null;

			// Name for this definition

			String name = null;

			// Link name for this definition

			String linkName = null;

			// State variable:
			// 0 = reading name
			// 1 = reading link name
			// 2 = reading regular expression

			int state = 0;

			// Loop until tokenizer runs out of tokens

			while (tokenizer.hasMoreTokens())
			{

				// Get token

				String token = tokenizer.nextToken();

				// Process '{'

				if (token.equals ("{"))
				{
					state = 1;
				}

				// Process '}'

				else if (token.equals ("}"))
				{
					state = 2;
				}

				// Process '='

				else if (token.equals ("="))
				{
					state = 2;
				}

				// Process '*'

				else if (token.equals ("*"))
				{
					if (exp2 != null)
					{
						exp2 = new ParserGeneratorREKleene (exp2);
					}
					else
					{
						exp1 = new ParserGeneratorREKleene (exp1);
					}
				}

				// Process '+'

				else if (token.equals ("+"))
				{
					if (exp2 != null)
					{
						exp2 = new ParserGeneratorREPositive (exp2);
					}
					else
					{
						exp1 = new ParserGeneratorREPositive (exp1);
					}
				}

				// Process '?'

				else if (token.equals ("?"))
				{
					if (exp2 != null)
					{
						exp2 = new ParserGeneratorREOptional (exp2);
					}
					else
					{
						exp1 = new ParserGeneratorREOptional (exp1);
					}
				}

				// Otherwise, it's a symbol or number

				else
				{

					// Switch on state

					switch (state)
					{

					case 0:

						// If the token is quoted, remove the quotes

						if ((token.length() >= 2)
							&& (token.charAt (0) == '\'')
							&& (token.charAt (token.length() - 1) == '\'') )
						{
							token = token.substring (1, token.length() - 1);
						}

						// This is the name

						name = token;
						break;

					case 1:

						// If the token is quoted, remove the quotes

						if ((token.length() >= 2)
							&& (token.charAt (0) == '\'')
							&& (token.charAt (token.length() - 1) == '\'') )
						{
							token = token.substring (1, token.length() - 1);
						}

						// It is the link name

						linkName = token;

						break;

					case 2:

						// If the token is quoted, remove the quotes

						if ((token.length() >= 2)
							&& (token.charAt (0) == '\'')
							&& (token.charAt (token.length() - 1) == '\'') )
						{
							token = token.substring (1, token.length() - 1);
						}

						// If there is a stacked expression, catenate it

						if (exp2 != null)
						{
							exp1 = new ParserGeneratorRECatenation (exp1, exp2);
							exp2 = null;
						}

						// If there is an existing expression, stack the new expression

						if (exp1 != null)
						{
							exp2 = new ParserGeneratorREOneChar (token, generator);
						}

						// Otherwise, the new expression is not stacked

						else
						{
							exp1 = new ParserGeneratorREOneChar (token, generator);
						}

						break;
					}
				}

			}	// end loop until tokenizer runs out of tokens

			// If there is a stacked expression, catenate it

			if (exp2 != null)
			{
				exp1 = new ParserGeneratorRECatenation (exp1, exp2);
				exp2 = null;
			}

			// Create the token

			ParserGeneratorToken tok = 
				generator.createToken (name, linkName, null, exp1, null);

		}	// end loop over definitions

		return;
	}


	// Token definitions for bootstrapping.
	//
	// The tokens within each definition must be separated by white space.
	// The only allowed operators are '*', '+', '?', and catenation.
	// No parentheses are allowed.
	// Numbers must be in decimal.
	// No integer parameters are allowed.

	static String[] bootstrapTokens = 
		{

			"'-' = '-'",
			"'~' = '~'",
			"'&' = '&'",
			"'@' = '@'",
			"'*' = '*'",
			"'+' = '+'",
			"'?' = '?'",
			"'/' = '/'",
			"':' = ':'",
			"';' = ';'",
			"'=' = '='",
			"'(' = '('",
			"')' = ')'",
			"'{' = '{'",
			"'}' = '}'",
			"'|' = '|'",
			"'#' = '#'",
			"'.' = '.'",
			"'->' = '-' '>'",
			"'..' = '.' '.'",

			"'%tokens' = '%' t o k e n s",
			"'%categories' = '%' c a t e g o r i e s",
			"'%conditions' = '%' c o n d i t i o n s",
			"'%terminals' = '%' t e r m i n a l s",
			"'%productions' = '%' p r o d u c t i o n s",
			"'%shift' = '%' s h i f t",
			"'%reduce' = '%' r e d u c e",
			"'%options' = '%' o p t i o n s",
			"'%repair' = '%' r e p a i r",
			"'%lr1' = '%' l r '1'",
			"'%plr1' = '%' p l r '1'",
			"'%lalr1' = '%' l a l r '1'",
			"'%any' = '%' a n y",
			"'%none' = '%' n o n e",
			"'%unicode' = '%' u n i c o d e",
			"'%uppercase' = '%' u p p e r c a s e",
			"'%lowercase' = '%' l o w e r c a s e",
			"'%titlecase' = '%' t i t l e c a s e",
			"'%letter' = '%' l e t t e r",
			"'%digit' = '%' d i g i t",
			"'%charsetsize' = '%' c h a r s e t s i z e",
			"'%goal' = '%' g o a l",
			"'%java' = '%' j a v a",

			"unknownKeyword = '%' letterOrDigit *",

			"number { decimal } = decDigit +",

			"number { hex } = '0' xX hexDigit +",

			"number { illegal } = decDigit + letter letterOrDigit *",

			"identifier { unquoted } = letter letterOrDigit *",

			"identifier { quoted } = ''' ''' ? notQuote * ''' ? '''",

			"whiteSpace = space *",

			"whiteSpace = space * '/' '/' notEol *",

			"lineEnd = cr",

			"lineEnd = lf",

			"lineEnd = cr lf",

			"whiteSpace = sub",

			"illegalChar = any",

			"beginComment = sub",

			"endComment = sub"

		};


}




// ParserGeneratorSymbol gives the definition of a symbol.

final class ParserGeneratorSymbol
{

	// The symbol name

	String _name;

	// The symbol number

	int _number;

	// True if declared in a %term statement

	boolean _isTerminal;

	// The insertion cost, if a terminal

	int _insertionCost;

	// The deletion cost, if a terminal

	int _deletionCost;


	// Constructor just saves its arguments

	ParserGeneratorSymbol (String name, int number,
		boolean isTerminal, int insertionCost, int deletionCost)
	{
		super ();

		_name = name;
		_number = number;
		_isTerminal = isTerminal;
		_insertionCost = insertionCost;
		_deletionCost = deletionCost;

		return;
	}


}




// ParserGeneratorProduction gives the definition of a production.

final class ParserGeneratorProduction
{

	// The production number

	int _number;

	// The production left hand side

	ParserGeneratorSymbol _lhs;

	// The production link name, or null if not explicitly specified

	String _linkName;

	// The production parameter, or null if not explicitly specified

	Integer _parameter;

	// The right hand side, which is a list of ParserGeneratorSymbol objects

	ObjectDeque _rhs;

	// The shift precedence set, which is a set of ParserGeneratorSymbol objects

	ObjectSet _shift;

	// The reduce precedence set, which is a set of ParserGeneratorSymbol objects

	ObjectSet _reduce;


	// Constructor just saves its argument

	ParserGeneratorProduction (int number)
	{
		super ();

		_number = number;

		_shift = new ObjectSet ();
		_reduce = new ObjectSet ();

		_lhs = null;
		_rhs = null;
		_linkName = null;
		_parameter = null;

		return;
	}


}




// ParserGeneratorCategory gives the definition of a user-defined category.

final class ParserGeneratorCategory
{

	// The category number

	int _number;

	// The category name

	String _name;

	// The unary predicate that describes the category

	UnaryIntPredicate _predicate;


	// Constructor just saves its arguments

	ParserGeneratorCategory (int number, String name, UnaryIntPredicate predicate)
	{
		super ();

		_number = number;
		_name = name;
		_predicate = predicate;

		return;
	}


}




// ParserGeneratorToken gives the definition of a token

final class ParserGeneratorToken
{

	// The token number

	int _number;

	// The context number, if _contextExp is not null

	int _ctxNumber;

	// The token name

	String _name;

	// The token link name, or null if not explicitly specified

	String _linkName;

	// The token parameter, or null if not explicitly specified

	Integer _parameter;

	// The token regular expression

	ParserGeneratorRegExp _tokenExp;

	// The context regular expression, or null

	ParserGeneratorRegExp _contextExp;

	// A set of String objects giving the conditions, or null

	ObjectSet _conditionSet;


	// Constructor just saves its arguments

	ParserGeneratorToken (int number, int ctxNumber, String name, String linkName,
		Integer parameter, ParserGeneratorRegExp tokenExp, ParserGeneratorRegExp contextExp,
		ObjectSet conditionSet)
	{
		super ();

		_number = number;
		_ctxNumber = ctxNumber;
		_name = name;
		_linkName = linkName;
		_parameter = parameter;
		_tokenExp = tokenExp;
		_contextExp = contextExp;
		_conditionSet = conditionSet;

		return;
	}


}




// ParserGeneratorCondition gives the definition of a start condition.

final class ParserGeneratorCondition
{

	// The condition number

	int _number;

	// The condition name

	String _name;


	// Constructor just saves its arguments

	ParserGeneratorCondition (int number, String name)
	{
		super ();

		_number = number;
		_name = name;

		return;
	}


}




// The precedence predicate

final class ParserGeneratorPrecedencePredicate extends BinaryIntPredicate
{

	private ParserGenerator _generator;

	ParserGeneratorPrecedencePredicate (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Two objects are equal if their contained generators are identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof ParserGeneratorPrecedencePredicate)))
		{
			return false;
		}

		ParserGeneratorPrecedencePredicate other = (ParserGeneratorPrecedencePredicate) obj;

		return this._generator == other._generator;
	}


	// A parser action is encoded into an integer as follows:  Reducing
	// production p is represented by p.  Shifting symbol s is represented by
	// s+PC where PC is the total number of productions in the grammar.
	//
	// _precedencePredicate.value(x,y) returns true if action x has precedence
	// over action y.  On each call to _precedencePredicate.value(x,y), it is
	// guaranteed that x is not equal to y, and that either x and y are both
	// reduce actions, or one is a reduce action and the other is a shift
	// action.
	//
	// When a parser conflict is discovered, the machine constructs a set
	// containing all possible actions.  It then checks every pair of possible
	// actions to determine which actions have precedence over which other
	// actions.  If there is exactly one action that has precedence over every
	// other possible action, then that action is selected.  Otherwise, the
	// conflict is unresolved.

	public boolean value (int arg1, int arg2)
	{

		// If arg1 is a shift and arg2 is a reduction ...

		if ((arg1 >= _generator._productionCount) && (arg2 < _generator._productionCount))
		{

			// The shift has precedence if the symbol is in the shift set

			return _generator._shiftSet[arg2].isElement (arg1 -  _generator._productionCount);
		}

		// If arg1 is a reduction and arg2 is a shift ...

		if ((arg1 < _generator._productionCount) && (arg2 >= _generator._productionCount))
		{

			// The reduction has precedence if the symbol is in the reduce set

			return _generator._reduceSet[arg1].isElement (arg2 -  _generator._productionCount);
		}

		// Otherwise, it's a reduce-reduce conflict, and we return no precedence

		return false;
	}


}




// Abstract class for regular expressions.

abstract class ParserGeneratorRegExp
{

	// Creates the NFA for this regular expression

	abstract FiniteAutomaton makeNFA (int tag);

	// Appends a description of this regular expression to the StringBuffer

	abstract void getDescription (StringBuffer buf);

	// Gets the precedence level of this regular expression

	abstract int precedence ();

	static final int precAlternation = 1;		// '|'
	static final int precIntersection = 2;		// '-' '&' '~' '@'
	static final int precCatenation = 3;		// catenation
	static final int precPostfix = 4;			// '*' '+' '?'
	static final int precPrimary = 5;			// category

}




// Regular expression parse tree element for alternation

final class ParserGeneratorREAlternation extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorREAlternation (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAAlternation (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" | ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precAlternation;
	}

}




// Regular expression parse tree element for catenation

final class ParserGeneratorRECatenation extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorRECatenation (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFACatenation (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precCatenation;
	}

}




// Regular expression parse tree element for difference

final class ParserGeneratorREDifference extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorREDifference (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFADifference (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" - ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precIntersection;
	}

}




// Regular expression parse tree element for excluding

final class ParserGeneratorREExcluding extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorREExcluding (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAExcluding (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" ~ ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precIntersection;
	}

}




// Regular expression parse tree element for including

final class ParserGeneratorREIncluding extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorREIncluding (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAIncluding (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" @ ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precIntersection;
	}

}




// Regular expression parse tree element for intersection

final class ParserGeneratorREIntersection extends ParserGeneratorRegExp
{

	// Contained regular expressions

	private ParserGeneratorRegExp _regExp1;

	private ParserGeneratorRegExp _regExp2;

	// Constructor saves its arguments

	ParserGeneratorREIntersection (ParserGeneratorRegExp regExp1, ParserGeneratorRegExp regExp2)
	{
		super ();

		_regExp1 = regExp1;
		_regExp2 = regExp2;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAIntersection (
			tag, _regExp1.makeNFA(tag), _regExp2.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append (" & ");

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append ("(");
		}

		_regExp2.getDescription (buf);

		if (_regExp2.precedence() <= this.precedence())
		{
			buf.append (")");
		}

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precIntersection;
	}

}




// Regular expression parse tree element for Kleene closure

final class ParserGeneratorREKleene extends ParserGeneratorRegExp
{

	// Contained regular expression

	private ParserGeneratorRegExp _regExp1;

	// Constructor saves its argument

	ParserGeneratorREKleene (ParserGeneratorRegExp regExp1)
	{
		super ();

		_regExp1 = regExp1;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAKleene (
			tag, _regExp1.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append ("*");

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precPostfix;
	}

}




// Regular expression parse tree element for positive closure

final class ParserGeneratorREPositive extends ParserGeneratorRegExp
{

	// Contained regular expression

	private ParserGeneratorRegExp _regExp1;

	// Constructor saves its argument

	ParserGeneratorREPositive (ParserGeneratorRegExp regExp1)
	{
		super ();

		_regExp1 = regExp1;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAPositive (
			tag, _regExp1.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append ("+");

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precPostfix;
	}

}




// Regular expression parse tree element for optional closure

final class ParserGeneratorREOptional extends ParserGeneratorRegExp
{

	// Contained regular expression

	private ParserGeneratorRegExp _regExp1;

	// Constructor saves its argument

	ParserGeneratorREOptional (ParserGeneratorRegExp regExp1)
	{
		super ();

		_regExp1 = regExp1;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{
		return FiniteAutomaton.makeNFAOptional (
			tag, _regExp1.makeNFA(tag) );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_regExp1.precedence() < this.precedence())
		{
			buf.append ("(");
		}

		_regExp1.getDescription (buf);

		if (_regExp1.precedence() < this.precedence())
		{
			buf.append (")");
		}

		buf.append ("?");

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precPostfix;
	}

}




// Regular expression parse tree element for one character

final class ParserGeneratorREOneChar extends ParserGeneratorRegExp
{

	// Category name

	private String _category;

	private ParserGenerator _generator;

	// Constructor saves its arguments

	ParserGeneratorREOneChar (String category, ParserGenerator generator)
	{
		super ();

		_category = category;
		_generator = generator;

		return;
	}

	// Creates the NFA for this regular expression

	FiniteAutomaton makeNFA (int tag)
	{

		// If the category name was an error insertion, just return empty set

		if (_category == null)
		{
			return FiniteAutomaton.makeNFAEmptySet (tag);
		}

		// Get the category symbol table entry

		ParserGeneratorCategory cat = _generator.getCategory (_category);

		// If category is undefined, report the error and return empty set

		if (cat == null)
		{
			_generator.reportError (ErrorOutput.typeError, null,
				"Undefined category name '" + _category + "'." );

			return FiniteAutomaton.makeNFAEmptySet (tag);
		}

		// Generate NFA for one character

		return FiniteAutomaton.makeNFAOneChar (
			tag, _generator._calculatedCategorySets[cat._number] );
	}

	// Appends a description of this regular expression to the StringBuffer

	void getDescription (StringBuffer buf)
	{
		if (_category == null)
		{
			buf.append ("%%error");
			return;
		}

		buf.append ("'");
		buf.append (_category);
		buf.append ("'");

		return;
	}

	// Gets the precedence level of this regular expression

	int precedence ()
	{
		return ParserGeneratorRegExp.precPrimary;
	}

}




// Token factory class for unknownKeyword.
//
// An unknownKeyword is discarded after printing an error message.


final class PGKFUnknownKeyword extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFUnknownKeyword (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Invalid keyword.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{
		_generator.reportError (ErrorOutput.typeError, token, null,
			"Invalid keyword '"
			+ scanner.tokenToString ()
			+ "'." );

		// Discard token

		return discard;
	}


}




// Token factory classes for number.
//
// The value of a number is an Integer object.  It is null if the number
// was incorrectly formatted, could not be converted to int, or was created
// by an error insertion.


final class PGKFNumberDecimal extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFNumberDecimal (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Decimal number.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the number as a string

		String number = scanner.tokenToString ();

		// Convert it to an Integer object

		try
		{
			token.value = new Integer (Integer.parseInt (number, 10));
		}

		// If conversion error, print error message and return null

		catch (NumberFormatException e)
		{
			_generator.reportError (ErrorOutput.typeError, token, null,
				"Invalid decimal number '" + number + "'." );

			token.value = null;
		}

		// Assembled token

		return assemble;
	}


}


final class PGKFNumberHex extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFNumberHex (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Hex number.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the number as a string

		String number = scanner.tokenToString (2, scanner.tokenLength() - 2);

		// Convert it to an Integer object

		try
		{
			token.value = new Integer (Integer.parseInt (number, 16));
		}

		// If conversion error, print error message and return null

		catch (NumberFormatException e)
		{
			_generator.reportError (ErrorOutput.typeError, token, null,
				"Invalid hexadecimal number '0x" + number + "'." );

			token.value = null;
		}

		// Assembled token

		return assemble;
	}


}


final class PGKFNumberIllegal extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFNumberIllegal (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Invalid number format.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{
		_generator.reportError (ErrorOutput.typeError, token, null,
			"Invalid number '"
			+ scanner.tokenToString ()
			+ "'." );

		// Assembled token

		return assemble;
	}


}




// Token factory classes for identifier.
//
// The value of an identifier is a String object.  It is null if the identifier
// was created as an error insertion.


final class PGKFIdentifierUnquoted extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFIdentifierUnquoted (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Unquoted identifier.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		token.value = scanner.tokenToString ();

		// Assembled token

		return assemble;
	}


}


final class PGKFIdentifierQuoted extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFIdentifierQuoted (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Quoted identifier.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		token.value = scanner.tokenToString (1, scanner.tokenLength() - 2);

		// Assembled token

		return assemble;
	}


}




// Token factory class for lineEnd.
//
// A lineEnd is discarded after counting the line.


final class PGKFLineEnd extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFLineEnd (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// End of line.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Bump the line number

		scanner.countLine ();
		
		// Progress report
		
		_generator.statusWork ();

		// Discard token

		return discard;
	}


}




// Token factory class for beginComment.
//
// A beginComment is discarded after setting the start condition.


final class PGKFBeginComment extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFBeginComment (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Beginning of a multi-line comment.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Set start condition to "inComment"

		scanner.setCondition (_generator.conditionInComment);

		// Discard token

		return discard;
	}


}




// Token factory class for endComment.
//
// An endComment is discarded after setting the start condition.


final class PGKFEndComment extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFEndComment (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// End of a multi-line comment.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Set start condition to "notInComment"

		scanner.setCondition (_generator.conditionNotInComment);

		// Discard token

		return discard;
	}


}




// Token factory class for illegalChar.
//
// An illegalChar is discarded after printing an error message.
// To avoid a cascade of error messages, this message is printed only once.


final class PGKFIllegalChar extends TokenFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGKFIllegalChar (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Illegal character in input.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Print message if not already printed

		if (!_generator.reportedIllegalChar)
		{
			_generator.reportError (ErrorOutput.typeError, token, null,
				"Illegal character in input." );

			_generator.reportedIllegalChar = true;
		}

		// Discard token

		return discard;
	}


}




// Nonterminal factory classes for OptionDefinition.
//
// An OptionDefinition has null value.


final class PGNFOptionDefinitionLR1 extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionLR1 (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// LR(1) grammar option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a grammar option ...

		if (_generator._seenGrammarOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate grammar type option." );

			return null;
		}

		// Set the flag

		_generator._seenGrammarOption = true;

		// Establish LR(1) grammar type

		_generator._grammarType = LRMachine.LR1;

		return null;
	}


}


final class PGNFOptionDefinitionPLR1 extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionPLR1 (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// PLR(1) grammar option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a grammar option ...

		if (_generator._seenGrammarOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate grammar type option." );

			return null;
		}

		// Set the flag

		_generator._seenGrammarOption = true;

		// Establish PLR(1) grammar type

		_generator._grammarType = LRMachine.PLR1;

		return null;
	}


}


final class PGNFOptionDefinitionLALR1 extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionLALR1 (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// LALR(1) grammar option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a grammar option ...

		if (_generator._seenGrammarOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate grammar type option." );

			return null;
		}

		// Set the flag

		_generator._seenGrammarOption = true;

		// Establish LALR(1) grammar type

		_generator._grammarType = LRMachine.LALR1;

		return null;
	}


}


final class PGNFOptionDefinitionRepair extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionRepair (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Error repair option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen an error repair option ...

		if (_generator._seenRepairOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate error repair option." );

			return null;
		}

		// Set the flag

		_generator._seenRepairOption = true;

		// Get items from value stack

		Integer maxInsertions = (Integer) parser.rhsValue (1);

		Integer maxDeletions = (Integer) parser.rhsValue (2);

		Integer validationLength = (Integer) parser.rhsValue (3);

		// If max insertions is an error insertion, replace it with default

		if (maxInsertions == null)
		{
			maxInsertions = new Integer (_generator.defaultMaxInsertion);
		}

		// If max deletions is an error insertion, replace it with default

		if (maxDeletions == null)
		{
			maxDeletions = new Integer (_generator.defaultMaxDeletion);
		}

		// If validation length is an error insertion, replace it with default

		if (validationLength == null)
		{
			validationLength = new Integer (_generator.defaultValidationLength);
		}

		// If max insertions is out of range ...

		if ((maxInsertions.intValue() < 0) || (maxInsertions.intValue() > 1000))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Maximum insertions must be between 0 and 1000.");

			// Replace with default value

			maxInsertions = new Integer (_generator.defaultMaxInsertion);
		}

		// If max deletions is out of range ...

		if ((maxDeletions.intValue() < 0) || (maxDeletions.intValue() > 1000))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Maximum deletions must be between 0 and 1000.");

			// Replace with default value

			maxDeletions = new Integer (_generator.defaultMaxDeletion);
		}

		// If validation length is out of range ...

		if ((validationLength.intValue() < 0) || (validationLength.intValue() > 100))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Validation length must be between 0 and 100.");

			// Replace with default value

			validationLength = new Integer (_generator.defaultValidationLength);
		}

		// Set the error repair options

		_generator._maxInsertion = maxInsertions.intValue();

		_generator._maxDeletion = maxDeletions.intValue();

		_generator._validationLength = validationLength.intValue();

		return null;
	}


}


final class PGNFOptionDefinitionCharsetsize extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionCharsetsize (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Character set size option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a character set size option ...

		if (_generator._seenCharSetSizeOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate character set size option." );

			return null;
		}

		// Set the flag

		_generator._seenCharSetSizeOption = true;

		// Get item from value stack

		Integer charSetSize = (Integer) parser.rhsValue (1);

		// If character set size is an error insertion, replace it with default

		if (charSetSize == null)
		{
			charSetSize = new Integer (_generator.defaultCharSetSize);
		}

		// If character set size is out of range ...

		if ((charSetSize.intValue() < 2) || (charSetSize.intValue() > 0x10000))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Character set size must be between 2 and 65536.");

			// Replace with default value

			charSetSize = new Integer (_generator.defaultCharSetSize);
		}

		// Set the character set size option

		_generator._charSetSize = charSetSize.intValue();

		return null;
	}


}


final class PGNFOptionDefinitionGoal extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionGoal (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Goal symbol option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a goal symbol option ...

		if (_generator._seenGoalOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate goal symbol option." );

			return null;
		}

		// Set the flag

		_generator._seenGoalOption = true;

		// Get item from value stack

		String goalSymbol = (String) parser.rhsValue (1);

		// If goal symbol is an error insertion, do nothing

		if (goalSymbol == null)
		{
			return null;
		}

		// Set the goal symbol option

		_generator._userGoalSymbol = _generator.getOrCreateSymbol (goalSymbol);

		return null;
	}


}


final class PGNFOptionDefinitionJava extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFOptionDefinitionJava (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Java name option

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// If already seen a java name option ...

		if (_generator._seenJavaNameOption)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate Java name option." );

			return null;
		}

		// Set the flag

		_generator._seenJavaNameOption = true;

		// Get item from value stack

		String javaName = (String) parser.rhsValue (1);

		// If java name is invalid, or an error insertion, do nothing

		if (javaName == null)
		{
			return null;
		}

		// Set the java name option

		_generator._javaName = javaName;

		return null;
	}


}



// Nonterminal factory classes for JavaName.
//
// The value of a JavaName is a String containing a possibly-qualified
// Java name, or null if the supplied name was invalid.


final class PGNFJavaNameSimple extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFJavaNameSimple (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Simple name

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get item from value stack

		String identifier = (String) parser.rhsValue (0);

		// Return the simple name, or null if it was invalid

		return identifier;
	}


}


final class PGNFJavaNameQualified extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFJavaNameQualified (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Qualified name

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String qualifier = (String) parser.rhsValue (0);

		String identifier = (String) parser.rhsValue (2);

		// If either item is invalid, or an error insertion, return error

		if (qualifier == null || identifier == null)
		{
			return null;
		}

		// Return the qualified name

		return qualifier + "." + identifier;
	}


}



// Nonterminal factory classes for JavaIdentifier.
//
// The value of a JavaIdentifier is a String containing an unqualified
// Java name, or null if the supplied name was invalid.


final class PGNFJavaIdentifier extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFJavaIdentifier (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Java identifier

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get item from value stack

		String identifier = (String) parser.rhsValue (0);

		// If item is an error insertion, return error

		if (identifier == null)
		{
			return null;
		}
		
		// If identifier is zero-length ...
		
		if (identifier.length() == 0)
		{

			// Report the error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Invalid Java identifier." );

			return null;
		}
		
		// If identifier contains an invalid character ...
		
		for (int i = 0; i < identifier.length(); ++i)
		{
			if ((i == 0 && !Character.isJavaLetter (identifier.charAt(i)))
				 || (i > 0 && !Character.isJavaLetterOrDigit (identifier.charAt(i))) )
			{

				// Report the error

				_generator.reportError (ErrorOutput.typeError, parser.token(), null,
					"Invalid Java identifier '" + identifier + "'." );

				return null;
			}
		}

		// Return the simple name

		return identifier;
	}


}




// Nonterminal factory classes for TerminalDefinition.
//
// A TerminalDefinition has null value.


final class PGNFTerminalDefinitionDefaultCost extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFTerminalDefinitionDefaultCost (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Terminal definition with default insertion and deletion costs

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the terminal symbol

		ParserGeneratorSymbol terminal = _generator.createTerminal (
			symbol, 1, 1 );

		// Report error

		if (terminal == null)
		{
			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate definition for terminal symbol '" + symbol + "'.");
		}

		// Return null value

		return null;
	}


}


final class PGNFTerminalDefinitionWithCost extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFTerminalDefinitionWithCost (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Terminal definition with explicit insertion and deletion costs

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		Integer insertionCost = (Integer) parser.rhsValue (1);

		Integer deletionCost = (Integer) parser.rhsValue (2);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// If insertion cost is an error insertion, replace it with default

		if (insertionCost == null)
		{
			insertionCost = new Integer (1);
		}

		// If deletion cost is an error insertion, replace it with default

		if (deletionCost == null)
		{
			deletionCost = new Integer (1);
		}

		// If insertion cost is out of range ...

		if ((insertionCost.intValue() < 1) || (insertionCost.intValue() > 1000))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Insertion cost must be between 1 and 1000.");

			// Replace with default value

			insertionCost = new Integer (1);
		}

		// If deletion cost is out of range ...

		if ((deletionCost.intValue() < 1) || (deletionCost.intValue() > 1000))
		{

			// Produce an error message

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Deletion cost must be between 1 and 1000.");

			// Replace with default value

			deletionCost = new Integer (1);
		}

		// Create the terminal symbol

		ParserGeneratorSymbol terminal = _generator.createTerminal (
			symbol, insertionCost.intValue(), deletionCost.intValue() );

		// Report error

		if (terminal == null)
		{
			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate definition for terminal symbol '" + symbol + "'.");
		}

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for ProductionDefinition.
//
// A ProductionDefinition has null value.


final class PGNFProductionDefinition extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFProductionDefinition (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Production definition

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		String linkName = (String) parser.rhsValue (1);

		Integer parameter = (Integer) parser.rhsValue (2);

		ObjectDeque rhs = (ObjectDeque) parser.rhsValue (4);

		ObjectSet[] prec = (ObjectSet[]) parser.rhsValue (5);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the production

		ParserGeneratorProduction production = _generator.createProduction ();

		// Fill in the production

		production._lhs = _generator.getOrCreateSymbol (symbol);
		production._linkName = linkName;
		production._parameter = parameter;
		production._rhs = rhs;
		production._shift = prec[0];
		production._reduce = prec[1];

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for LinkName.
//
// The value of a LinkName is a String containing the link name value.  It
// is null if the link name was not specified, or if the identifier was an
// error insertion.


final class PGNFLinkNameEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFLinkNameEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Link name not specified

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return null for link name not specified

		return null;
	}


}


final class PGNFLinkNameIdentifier extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFLinkNameIdentifier (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Explicit link name

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String linkName = (String) parser.rhsValue (1);

		// Return link name, or null if the identifier was an error insertion

		return linkName;
	}


}




// Nonterminal factory classes for Parameter.
//
// The value of a Parameter is an Integer containing the parameter value.  It
// is null if the parameter was not specified, or if the number was an error
// insertion.


final class PGNFParameterEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFParameterEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Parameter not specified

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return null for parameter not specified

		return null;
	}


}


final class PGNFParameterNumber extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFParameterNumber (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Explicit parameter

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer parameter = (Integer) parser.rhsValue (1);

		// Return parameter, or null if the number was an error insertion

		return parameter;
	}


}



// Nonterminal factory classes for SymbolList.
//
// The value of a SymbolList is an ObjectDeque containing a list of
// ParserGeneratorSymbol objects.


final class PGNFSymbolListEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFSymbolListEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Empty list

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return empty list

		return new ObjectDeque();
	}


}


final class PGNFSymbolListAppend extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFSymbolListAppend (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Add symbol to list

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectDeque list = (ObjectDeque) parser.rhsValue (0);

		String symbol = (String) parser.rhsValue (1);

		// If symbol is an error insertion, return list unchanged

		if (symbol == null)
		{
			return list;
		}

		// Add symbol to end of list

		list.pushLast (_generator.getOrCreateSymbol (symbol));

		// Return the list

		return list;
	}


}




// Nonterminal factory class for ProductionPrec.
//
// The value of a ProductionPrec is a two-element array of ObjectSet.
// The first element is a set of ParserGeneratorSymbol objects that represent
// the shift set.  The second element is a set of ParserGeneratorSymbol
// objects that represent the reduce set.


final class PGNFProductionPrecEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFProductionPrecEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Empty set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Create empty sets

		ObjectSet[] prec = new ObjectSet[2];

		prec[0] = new ObjectSet();
		prec[1] = new ObjectSet();

		// Return the sets

		return prec;
	}


}


final class PGNFProductionPrecAppendShift extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFProductionPrecAppendShift (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Add symbol set to shift set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectSet[] prec = (ObjectSet[]) parser.rhsValue (0);

		ObjectSet symbolSet = (ObjectSet) parser.rhsValue (2);

		// Add symbols to shift set

		prec[0].union (symbolSet);

		// Return the sets

		return prec;
	}


}


final class PGNFProductionPrecAppendReduce extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFProductionPrecAppendReduce (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Add symbol set to reduce set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectSet[] prec = (ObjectSet[]) parser.rhsValue (0);

		ObjectSet symbolSet = (ObjectSet) parser.rhsValue (2);

		// Add symbols to reduce set

		prec[1].union (symbolSet);

		// Return the sets

		return prec;
	}


}




// Nonterminal factory classes for SymbolSet.
//
// The value of a SymbolSet is an ObjectSet containing a set of
// ParserGeneratorSymbol objects.


final class PGNFSymbolSetEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFSymbolSetEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Empty set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return empty set

		return new ObjectSet();
	}


}


final class PGNFSymbolSetAppend extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFSymbolSetAppend (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Add symbol to set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectSet set = (ObjectSet) parser.rhsValue (0);

		String symbol = (String) parser.rhsValue (1);

		// If symbol is an error insertion, return set unchanged

		if (symbol == null)
		{
			return set;
		}

		// Add symbol to set

		set.addElement (_generator.getOrCreateSymbol (symbol));

		// Return the set

		return set;
	}


}




// Nonterminal factory classes for CategoryDefinition.
//
// A CategoryDefinition has null value.


final class PGNFCategoryDefinition extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCategoryDefinition (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Category definition

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		UnaryIntPredicate pred = (UnaryIntPredicate) parser.rhsValue (2);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the category

		ParserGeneratorCategory category = _generator.createCategory (
			symbol, pred );

		// Report error

		if (category == null)
		{
			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate definition for character category '" + symbol + "'.");
		}

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for CatExp.
//
// The value of a CatExp is a UnaryIntPredicate;  it is not null.


final class PGNFCatExpNumber extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpNumber (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A single number

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer number = (Integer) parser.rhsValue (0);

		// If number is an error insertion, return empty predicate

		if (number == null)
		{
			return UnaryIntPredicateFactory.never ();
		}

		// Return a predicate that recognizes a single value

		return UnaryIntPredicateFactory.equal (number.intValue());
	}


}


final class PGNFCatExpIdentifier extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpIdentifier (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A single identifier

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String identifier = (String) parser.rhsValue (0);

		// If identifier is an error insertion, return empty predicate

		if (identifier == null)
		{
			return UnaryIntPredicateFactory.never ();
		}

		// Return a predicate that recognizes any character in a string

		return UnaryIntPredicateFactory.string (identifier);
	}


}


final class PGNFCatExpNumberRange extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpNumberRange (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A range of numbers

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer number1 = (Integer) parser.rhsValue (0);

		Integer number2 = (Integer) parser.rhsValue (2);

		// If either number is an error insertion, return empty predicate

		if ((number1 == null) || (number2 == null))
		{
			return UnaryIntPredicateFactory.never ();
		}

		// If ordering is reversed ...

		if (number1.intValue() > number2.intValue())
		{

			// Report error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Number range operator '..' must have smaller number first." );

			// Return empty predicate

			return UnaryIntPredicateFactory.never ();
		}

		// Return a predicate that recognizes a range of values

		return UnaryIntPredicateFactory.range (
			number1.intValue(), number2.intValue() );
	}


}


final class PGNFCatExpIdentifierRange extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpIdentifierRange (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A range of characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String identifier1 = (String) parser.rhsValue (0);

		String identifier2 = (String) parser.rhsValue (2);

		// If either identifier is an error insertion, return empty predicate

		if ((identifier1 == null) || (identifier2 == null))
		{
			return UnaryIntPredicateFactory.never ();
		}

		// If either identifier has length not equal to 1 ...

		if ((identifier1.length() != 1) || (identifier2.length() != 1))
		{

			// Report error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Character range operator '..' requires single-character operands." );

			// Return empty predicate

			return UnaryIntPredicateFactory.never ();
		}

		// If ordering is reversed ...

		if (identifier1.charAt(0) > identifier2.charAt(0))
		{

			// Report error

			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Character range operator '..' must have earlier character first." );

			// Return empty predicate

			return UnaryIntPredicateFactory.never ();
		}

		// Return a predicate that recognizes a range of values

		return UnaryIntPredicateFactory.range (
			identifier1.charAt(0), identifier2.charAt(0) );
	}


}


final class PGNFCatExpAny extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpAny (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// All characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that is always true

		return UnaryIntPredicateFactory.always ();
	}


}


final class PGNFCatExpNone extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpNone (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// No characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that is never true

		return UnaryIntPredicateFactory.never ();
	}


}


final class PGNFCatExpUnicode extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpUnicode (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Unicode characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes Unicode characters

		return UnaryIntPredicateFactory.unicode ();
	}


}


final class PGNFCatExpUppercase extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpUppercase (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Uppercase characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes uppercase characters

		return UnaryIntPredicateFactory.upperCase ();
	}


}


final class PGNFCatExpLowercase extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpLowercase (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Lowercase characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes lowercase characters

		return UnaryIntPredicateFactory.lowerCase ();
	}


}


final class PGNFCatExpTitlecase extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpTitlecase (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Titlecase characters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes titlecase characters

		return UnaryIntPredicateFactory.titleCase ();
	}


}


final class PGNFCatExpLetter extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpLetter (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Letters

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes letters

		return UnaryIntPredicateFactory.letter ();
	}


}


final class PGNFCatExpDigit extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpDigit (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Digits

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return a predicate that recognizes digits

		return UnaryIntPredicateFactory.digit ();
	}


}


final class PGNFCatExpParen extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpParen (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A parenthesized expression

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return the item in parentheses

		return parser.rhsValue(1);
	}


}


final class PGNFCatExpDifference extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpDifference (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Difference

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		UnaryIntPredicate pred1 = (UnaryIntPredicate) parser.rhsValue (0);

		UnaryIntPredicate pred2 = (UnaryIntPredicate) parser.rhsValue (2);

		// Return a predicate that recognizes the difference

		return UnaryIntPredicateFactory.and (
			pred1, UnaryIntPredicateFactory.not (pred2) );
	}


}


final class PGNFCatExpIntersection extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpIntersection (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Intersection

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		UnaryIntPredicate pred1 = (UnaryIntPredicate) parser.rhsValue (0);

		UnaryIntPredicate pred2 = (UnaryIntPredicate) parser.rhsValue (2);

		// Return a predicate that recognizes the intersection

		return UnaryIntPredicateFactory.and (pred1, pred2);
	}


}


final class PGNFCatExpUnion extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFCatExpUnion (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Alternation

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		UnaryIntPredicate pred1 = (UnaryIntPredicate) parser.rhsValue (0);

		UnaryIntPredicate pred2 = (UnaryIntPredicate) parser.rhsValue (2);

		// Return a predicate that recognizes the alternation

		return UnaryIntPredicateFactory.or (pred1, pred2);
	}


}




// Nonterminal factory classes for ConditionDefinition.
//
// A ConditionDefinition has null value.


final class PGNFConditionDefinition extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFConditionDefinition (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Condition definition

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the condition

		ParserGeneratorCondition condition = _generator.createCondition (symbol);

		// Report error

		if (condition == null)
		{
			_generator.reportError (ErrorOutput.typeError, parser.token(), null,
				"Duplicate definition for start condition '" + symbol + "'.");
		}

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for TokenHeader.
//
// A TokenHeader has null value.


final class PGNFTokenHeader extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFTokenHeader (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Token header

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectSet condSet = (ObjectSet) parser.rhsValue (1);

		// If condition set is empty ...

		if (condSet.isEmpty())
		{

			// Select all conditions

			_generator._currentConditionSet = null;
		}

		// Otherwise ...

		else
		{

			// Select conditions in the set

			_generator._currentConditionSet = condSet;
		}

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for ConditionSet.
//
// The value of a ConditionSet is an ObjectSet containing a set of
// String objects.


final class PGNFConditionSetEmpty extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFConditionSetEmpty (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Empty set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return empty set

		return new ObjectSet();
	}


}


final class PGNFConditionSetAppend extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFConditionSetAppend (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Add condition to set

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ObjectSet set = (ObjectSet) parser.rhsValue (0);

		String condition = (String) parser.rhsValue (1);

		// If condition is an error insertion, return set unchanged

		if (condition == null)
		{
			return set;
		}

		// Add symbol to set

		set.addElement (condition);

		// Return the set

		return set;
	}


}




// Nonterminal factory classes for TokenDefinition.
//
// A TokenDefinition has null value.


final class PGNFTokenDefinitionNoContext extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFTokenDefinitionNoContext (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Token definition without right context

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		String linkName = (String) parser.rhsValue (1);

		Integer parameter = (Integer) parser.rhsValue (2);

		ParserGeneratorRegExp tokenExp = (ParserGeneratorRegExp) parser.rhsValue (4);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the token

		ParserGeneratorToken token = _generator.createToken (
			symbol, linkName, parameter, tokenExp, null );

		// Return null value

		return null;
	}


}


final class PGNFTokenDefinitionRightContext extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFTokenDefinitionRightContext (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Token definition with right context

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String symbol = (String) parser.rhsValue (0);

		String linkName = (String) parser.rhsValue (1);

		Integer parameter = (Integer) parser.rhsValue (2);

		ParserGeneratorRegExp tokenExp = (ParserGeneratorRegExp) parser.rhsValue (4);

		ParserGeneratorRegExp contextExp = (ParserGeneratorRegExp) parser.rhsValue (6);

		// If symbol is an error insertion, do nothing

		if (symbol == null)
		{
			return null;
		}

		// Create the token

		ParserGeneratorToken token = _generator.createToken (
			symbol, linkName, parameter, tokenExp, contextExp );

		// Return null value

		return null;
	}


}




// Nonterminal factory classes for RegExp.
//
// The value of a RegExp is a ParserGeneratorRegExp;  it is not null.


final class PGNFRegExpOneChar extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpOneChar (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A category

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		String category = (String) parser.rhsValue (0);

		// Return a parse tree for one character
		// Note:  ParserGeneratorREOneChar contains code to handle
		// the case category==null.

		return new ParserGeneratorREOneChar (category, _generator);
	}


}


final class PGNFRegExpParen extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpParen (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// A parenthesized expression

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Return the item in parentheses

		return parser.rhsValue(1);
	}


}


final class PGNFRegExpKleeneClosure extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpKleeneClosure (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Kleene closure

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		// Return a parse tree for Kleene closure

		return new ParserGeneratorREKleene (regExp1);
	}


}


final class PGNFRegExpPositiveClosure extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpPositiveClosure (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Positive closure

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		// Return a parse tree for positive closure

		return new ParserGeneratorREPositive (regExp1);
	}


}


final class PGNFRegExpOptionalClosure extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpOptionalClosure (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Optional closure

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		// Return a parse tree for optional closure

		return new ParserGeneratorREOptional (regExp1);
	}


}


final class PGNFRegExpCatenation extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpCatenation (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Catenation

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (1);

		// Return a parse tree for catenation

		return new ParserGeneratorRECatenation (regExp1, regExp2);
	}


}


final class PGNFRegExpDifference extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpDifference (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Difference

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (2);

		// Return a parse tree for difference

		return new ParserGeneratorREDifference (regExp1, regExp2);
	}


}


final class PGNFRegExpIntersection extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpIntersection (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Intersection

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (2);

		// Return a parse tree for intersection

		return new ParserGeneratorREIntersection (regExp1, regExp2);
	}


}


final class PGNFRegExpExcluding extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpExcluding (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Excluding

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (2);

		// Return a parse tree for excluding

		return new ParserGeneratorREExcluding (regExp1, regExp2);
	}


}


final class PGNFRegExpIncluding extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpIncluding (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Including

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (2);

		// Return a parse tree for including

		return new ParserGeneratorREIncluding (regExp1, regExp2);
	}


}


final class PGNFRegExpAlternation extends NonterminalFactory
{

	// The parser generator object

	private ParserGenerator _generator;

	public PGNFRegExpAlternation (ParserGenerator generator)
	{
		super ();
		_generator = generator;
		return;
	}


	// Alternation

	public Object makeNonterminal (Parser parser, int param) throws IOException, SyntaxException
	{

		// Get items from value stack

		ParserGeneratorRegExp regExp1 = (ParserGeneratorRegExp) parser.rhsValue (0);

		ParserGeneratorRegExp regExp2 = (ParserGeneratorRegExp) parser.rhsValue (2);

		// Return a parse tree for alternation

		return new ParserGeneratorREAlternation (regExp1, regExp2);
	}


}



