// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  ParserClient is an interface that represents a client of a Parser object.

  The parser uses this interface to call back to its client to inform the
  client of errors.

->*/


public interface ParserClient 
{

	// Report an I/O exception.
	//
	// The parser calls this function to inform the client that an I/O
	// exception has occurred.  This function is called immediately before
	// Parser.parse() returns.  Typically, this function would generate an
	// error message.
	//
	// The IOException object is the exception object that was thrown.

	public void parserIOException (Parser parser, IOException e);


	// Report a syntax exception.
	//
	// The parser calls this function to inform the client that a syntax
	// exception has occurred.  This function is called immediately before
	// Parser.parse() returns.  Typically, this function would generate an
	// error message.
	//
	// The SyntaxException object is the exception object that was thrown.

	public void parserSyntaxException (Parser parser, SyntaxException e);


	// Report an error repair.
	//
	// The parser calls this function to inform the client that a parser error
	// was detected and repaired.  An error repair consists of first deleting
	// zero or more terminal symbols from the front of the input, then
	// inserting zero or more terminal symbols onto the front of the input.
	//
	// The number of symbols inserted is insertionLength.  The symbols inserted
	// are in insertions[0] thru insertions[insertionLength-1].  Note that
	// insertions[0] is the last symbol pushed back onto the input, so it will
	// be the first symbol processed when parsing resumes.
	//
	// The number of symbols deleted is deletionLength.  The symbols deleted
	// are in deletions[0] thru deletions[deletionLength-1].  Note that
	// deletions[0] appeared first in the original input.
	//
	// Note that insertionLength may be less than insertions.length.  Likewise,
	// deletionLength may be less than deletions.length.
	//
	// It is guaranteed that at least one of insertionLength and deletionLength
	// will be nonzero.
	//
	// In the function parameters, terminal symbols are identified by their
	// symbol numbers.  The client can call parser.symbolName() to get the name
	// of any symbol, given its number.
	//
	// The input token that caused the error is errorToken.  This function may
	// take source file name, line, and column information from errorToken.
	// Note that if deletionLength is nonzero then deletions[0] contains
	// errorToken.number.

	public void parserErrorRepair (Parser parser, Token errorToken,
		int[] insertions, int insertionLength, int[] deletions, int deletionLength);


	// Report an error that the parser was not able to repair.
	//
	// The parser calls this function to inform the client that a parser error
	// occurred, and the parser was not able to repair it.  This may happen
	// either because the parser table has error repairs disabled, or because
	// the parser tried all possible repairs and was unable to validate any of
	// them.
	//
	// The input token that caused the error is errorToken.  This function may
	// take source file name, line, and column information from errorToken.

	public void parserErrorFail (Parser parser, Token errorToken);


}

