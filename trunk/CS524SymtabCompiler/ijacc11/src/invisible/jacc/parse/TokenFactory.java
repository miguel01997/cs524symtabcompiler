// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  TokenFactory is used by the scanner to create token objects.

  The scanner is passed an array of TokenFactory objects, each of which
  corresponds to one regular expression recognized by the scanner.  When the
  scanner recognizes a regular expression, it calls the makeToken() routine of
  the corresponding TokenFactory object.

  The makeToken() routine can do one of three things:  (1) It can assemble a
  token.  In this case, the scanner returns the assembled token to its client.
  (2) It can discard the token.  In this case, the scanner skips past the
  matching text and then attempts to recognize another token.  (3) It can
  reject the token.  In this case, the scanner passes the text to the next
  candidate token factory.

  It is recommended that a TokenFactory object should not store any state
  information.  State information should be stored either in a "global" client
  object, or in the scanner's clientParams variable.  In Java 1.1, a
  TokenFactory could be declared as an inner class nested inside the global
  client, which would give the TokenFactory convenient access to the global
  client's variables and methods.

->*/


public abstract class TokenFactory 
{

	// Make a token object.
	//
	//
	// PARAMETERS
	//
	// The parameters are as follows:
	//
	// scanner - The scanner object.  The token factory can use this parameter
	//		to call on certain routines in the scanner, as described below.
	//
	// token - A Token object into which the function should assemble the
	//		token.  Its fields are initialized by the scanner as follows:
	//
	//		token.number - The token parameter from the scanner tables.  This
	//			is a nonnegative integer.
	//
	//		token.value - null.
	//
	//		token.file - The file name.
	//
	//		token.line - The current line number.  This is the line number
	//			for the first character in the token.
	//
	//		token.column - The current column number.  This is the column
	//			number for the first character in the token.
	//
	//
	// ASSEMBLING A TOKEN
	//
	// If makeToken() assembles a token, it must return 'assemble'.  In addition,
	// it must set token.number to the token number, and it must set token.value
	// to the token value.
	//
	// After makeToken() returns, the scanner adds its internal _tokenLength
	// value to token.column and to its internal _tokenStart value.  This
	// has the effect of moving past the token text.  Then, the scanner returns
	// the assembled token to its client.
	//
	// There is no restriction on the type of token that can be assembled.  The
	// token can be a normal token, an end-of-file token, or an escape token.
	// However, normally a token factory would not return an end-of-file token,
	// since the scanner generates one automatically when it reaches the end of
	// its input.
	//
	//
	// DISCARDING A TOKEN
	//
	// If makeToken() discards a token, it must return 'discard'.
	//
	// After makeToken() returns, the scanner adds its internal _tokenLength
	// value to token.column and to its internal _tokenStart value.  This
	// has the effect of moving past the token text.  Then, the scanner
	// searches for another token.
	//
	//
	// REJECTING A TOKEN
	//
	// If makeToken() rejects a token, it must return 'reject'.
	//
	// After makeToken() returns, the scanner calls the next candidate token
	// factory for the current token text.  This is useful if token recognition
	// cannot be accomplished with regular expressions alone.
	//
	// Note that rejecting a token does not cause the scanner to re-scan the
	// text.  Rather, the scanner remembers the results of the prior scan.  For
	// this reason, makeToken() must not call scanner.setTokenStart() or
	// scanner.setTokenLength() if it rejects the token.
	//
	// Also note that rejecting a token may cause the same token factory to be
	// called again, with a different (usually smaller) tokenLength.
	//
	//
	// GETTING TOKEN TEXT AND LENGTH
	//
	// The scanner provides functions for the token factory to obtain the
	// matching text of a token.
	//
	// The token factory can call scanner.tokenToString() to obtain a String
	// object which contains all or part of the matching token text.  It can
	// call scanner.tokenCharAt() to obtain a single character of the matching
	// token text.  It can call scanner.tokenToChars() to copy the matching
	// token text into a char array.
	//
	// The function scanner.tokenLength() returns the length of the matching
	// token text.
	//
	//
	// GETTING CONTEXT TEXT AND LENGTH
	//
	// A token that is defined with a right context can obtain the text that
	// matches the context.  This text includes both the token expression and
	// the right-context expression.  (For example, given the expression
	// 'a'+/'b'+, if the input is "aaabbc", the token text is "aaa" and the
	// context text is "aaabb".)  If a token is defined without a right context,
	// the context text is the same as the token text.
	//
	// The token factory can call scanner.contextToString() to obtain a String
	// object which contains all or part of the matching context text.  It can
	// call scanner.contextCharAt() to obtain a single character of the matching
	// context text.  It can call scanner.contextToChars() to copy the matching
	// context text into a char array.
	//
	// The function scanner.contextLength() returns the length of the matching
	// context text.
	//
	//
	// ALTERING LINE AND COLUMN POSITION
	//
	// Optionally, makeToken() can alter the values of token.line and/or
	// token.column.  This is typically done if the token text contains
	// an end-of-line.  For example, a token that recognizes a single
	// end-of-line could increment token.line and set token.column to
	// 1-scanner.tokenLength().
	//
	// The token factory can call scanner.countLine() to increment token.line
	// and set token.column to 1-scanner.tokenLength().  The function
	// scanner.countLine() is provided as a convenience to the token factory;
	// there is nothing wrong with the token factory modifying token.line
	// and token.column directly.
	//
	//
	// ALTERING TOKEN LENGTH
	//
	// The scanner's internal _tokenLength variable can be altered by calling
	// scanner.setTokenLength().  This is useful if the actual token length
	// differs from the length recognized by the regular expression.  Note that
	// it is legal to specify a length of zero, however caution must be used
	// in this case not to create an infinite loop.
	//
	// Note that scanner.tokenLength() returns the value of the scanner's
	// internal _tokenLength variable, so calling scanner.setTokenLength()
	// alters the value returned by scanner.tokenLength().
	//
	//
	// SETTING THE START CONDITION
	//
	// The start condition can be changed by calling scanner.setCondition().
	// The new start condition takes effect beginning with the next token scan.
	//
	// Additionally, scanner.condition() can be called to retrieve the current
	// start condition.
	//
	//
	// GETTING THE TOKEN INDEX NUMBER
	//
	// The token factory can call scanner.tokenIndex() to get the current token
	// index number.  Each token definition is assigned an index number,
	// beginning with 0, in the order they appear in the specification file.
	//
	//
	// ACCESSING CLIENT PARAMETERS
	//
	// The token factory can access the public variable scanner.clientParams.
	// This variable is dedicated to the client's use.  The token factory and
	// client can use it for any desired purpose.  For a client that supports
	// include files, scanner.clientParams would be the appropriate place to
	// store any per-file state information.
	//
	//
	// GETTING ADDITIONAL INPUT TEXT
	//
	// The scanner maintains an internal text buffer.  Conceptually, the buffer
	// begins at the start of the current token text, and it includes at least
	// the token text and context text.  Typically, it also includes additional
	// input beyond the context text.
	//
	// The function scanner.textLength() returns the number of characters
	// currently available in the text buffer.
	//
	// The token factory can call scanner.textToString() to obtain a String
	// object which contains part of the currently available text.  It can call
	// scanner.textCharAt() to obtain any single character of the currently
	// available text.  It can call scanner.textToChars() to copy part of the
	// currently available text into a char array.
	//
	// If the token factory needs to read beyond the currently available text,
	// it can call scanner.readData(), which causes the scanner to read some
	// amount of additional input data into the text buffer.
	//
	//
	// DIRECT ACCESS TO THE INPUT BUFFER
	//
	// In unusual cases, it may be necessary for the token factory to have
	// direct access to the scanner's input buffer.  The following functions
	// allow this.
	//
	// The scanner's input buffer may have type byte[] or char[].  Therefore, a
	// token factory that wants to access the buffer directly must be able to
	// handle either type of buffer.  First, call scanner.isByteText() to
	// determine which type of buffer is in use.  If the buffer type is byte[],
	// call scanner.rawByteText() to obtain the internal text buffer.  If the
	// buffer type is char[], call scanner.rawCharText() to obtain the internal
	// text buffer.
	//
	// The function scanner.tokenStart() returns the offset within the text
	// buffer where the current token text begins.  The function scanner.dataEnd()
	// returns the offset of the last available character in the text buffer,
	// plus 1.  The token factory may only access the part of the buffer between
	// these two offsets.
	//
	// The token factory may call scanner.readData() to read additional data into
	// the buffer.  Since scanner.readData() can reallocate the buffer, any values
	// previously obtained from scanner.rawByteText(), scanner.rawCharText(),
	// scanner.tokenStart(), and scanner.dataEnd() become invalid.  After calling
	// scanner.readData(), the token factory must call these other functions again
	// to obtain updated values.
	//
	// The token factory can call scanner.setTokenStart() to discard input text.
	// This function sets the scanner's internal _tokenStart variable.  Then, the
	// next call to scanner.readData() can overwrite any text prior to the new
	// value of _tokenStart.  The token factory can use this function to scan ahead
	// an arbitrary distance in the input, perhaps to discard comments or embedded
	// data blocks.  If the token factory does this, it will almost certainly want
	// to call scanner.setTokenLength() to specify where scanning is to resume.


	public abstract int makeToken (Scanner scanner, Token token)
		throws IOException, SyntaxException;


	// Return values from makeToken().

	public static final int assemble = 0;
	public static final int discard = 1;
	public static final int reject = 2;


}

