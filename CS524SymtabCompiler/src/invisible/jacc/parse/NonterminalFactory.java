// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  NonterminalFactory is used by Parser to construct nonterminal symbols.

  Whenever the parser reduces a production, it calls the makeNonterminal()
  entry point for the corresponding nonterminal factory.  The parser makes
  available the production's parameter, and the portion of the value stack that
  contains the values for the right hand side of the production.  The
  makeNonterminal() function returns the value of the nonterminal on the left
  hand side of the production;  this value is stored on the value stack.

  It is recommended that a NonterminalFactory object should not store any state
  information.  State information should be stored either in a "global" client
  object, or in the parser's clientParams variable.  In Java 1.1, a
  NonterminalFactory could be declared as an inner class nested inside the
  global client, which would give the NonterminalFactory convenient access to
  the global client's variables and methods.

->*/


public abstract class NonterminalFactory 
{


	// Factory function.
	//
	// Assume that this NonterminalFactory corresponds to the production
	// Y -> X1 ... Xn.  Whenever the parser reduces this production, it calls
	// makeNonterminal().  During this call, parser.rhsValue(0) through
	// parser.rhsValue(n-1) return the values of X1 through Xn respectively.
	// Note that it is allowed for values to be null.
	//
	// The integer parameter associated with this production is passed in
	// param.  If there is more than one production with Y on the left hand
	// side that has the same link name, the integer parameter could be used
	// to indicate which production is being reduced.
	//
	// The function returns the value of Y.  This value is saved on the value
	// stack.  Note that the return value is allowed to be null.
	//
	// During this call, parser.token() returns the most recently shifted
	// Token object.  The nonterminal factory may examine the file,
	// line, and column fields of this Token object.  This can be used
	// to relate semantic errors to a specific position within the source.
	//
	// Also, parser.client() may be used to retrieve the ParserClient object.
	//
	// In addition, the nonterminal factory may access the public variable
	// parser.clientParams, which it may use for any purpose.
	//
	// Note 1:  If error repair is enabled, then any terminal symbol on the
	// right hand side can be the result of an error insertion.  An error
	// insertion symbol has null value;  that is, parser.rhsValue() returns
	// null if the terminal symbol was inserted during error repair.  Therefore,
	// if error repair is enabled, then the nonterminal factory must be
	// prepared to accept a null value for any terminal symbol on the right
	// hand side.
	//
	// Note 2:  If error repair is enabled, and the grammar is ambiguous, then
	// error insertions may cause productions to be reduced differently than
	// specified by the precedence rules.  Therefore, the nonterminal factory
	// should not contain code that crashes if the precedence rules are not
	// followed.  In practice, this is unlikely to require any special code.

	public abstract Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException;


}

