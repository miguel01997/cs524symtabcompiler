// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  A TokenStream object represents a stream of tokens.  Each token may be read
  from the stream once and only once.  Reading a token automatically advances
  the internal stream pointer to the next token.

  Each token is packaged in a Token object.

  End-of-stream is indicated by returning token number 0, which is indicated
  symbolically as Token.EOF.

->*/


public interface TokenStream 
{

	// Gets the next token in the stream, and removes it from the stream.
	//
	// When the end of the stream is reached, this function should return with
	// token.number = Token.EOF.  The end-of-stream token may be retrieved
	// only once.  (TokenStream differs from Preprocessor on this point.)  If
	// this function is called again after end-of-stream has been returned, its
	// behavior is undefined.
	//
	// In order to avoid the overhead of creating a Token object for each
	// token, this function may return the same Token object repeatedly, and
	// simply update the fields of the Token object for each call.  Therefore,
	// the caller must treat the returned Token object as read-only, and may
	// use the returned Token object only until the next call to nextToken().

	public Token nextToken () throws IOException, SyntaxException;


	// Closes the token stream.
	//
	// Typically, this function closes the file from which tokens are being
	// obtained.

	public void close () throws IOException;


}

