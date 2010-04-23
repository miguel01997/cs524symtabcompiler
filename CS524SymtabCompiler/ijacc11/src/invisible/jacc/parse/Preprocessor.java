// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  A Preprocessor object represents a stream of tokens, with push-back and
  peek-ahead capability.  Reading a token automatically advances the internal
  stream pointer to the next token.  Pushing back a token inserts it into the
  stream at the current position, so it can later be read.  Peeking ahead
  examines upcoming tokens without removing them from the stream.

  Each token is packaged in a Token object.

  End-of-stream is indicated by returning token number 0, which is indicated
  symbolically as Token.EOF.

  Note that a Preprocessor can also be used as a TokenStream.

->*/


public interface Preprocessor extends TokenStream
{

	// Gets the next token in the stream, and removes it from the stream.
	//
	// If any tokens were pushed back, this function retrieves the token most
	// recently pushed back, and removes it from the push-back queue.
	//
	// When the end of the stream is reached, this function should return with
	// token.number = Token.EOF.  The end-of-stream token may be retrieved
	// an arbitrary number of times.
	//
	// In order to avoid the overhead of creating a Token object for each
	// token, this function may return the same Token object repeatedly, and
	// simply update the fields of the Token object for each call.  Therefore,
	// the caller must treat the returned Token object as read-only, and may
	// use the returned Token object only until the next call to nextToken()
	// or peekAheadToken().

	public Token nextToken () throws IOException, SyntaxException;


	// Pushes back the specified token, so that the next call to nextToken()
	// retrieves the pushed-back token.
	//
	// An implementation of Preprocessor is required to support an unlimited
	// number of push-backs.  A typical implementation would copy the Token
	// object for each pushed-back token, and queue the copies in an
	// ObjectDeque.
	//
	// This function is not allowed to retain a reference to the supplied
	// Token object.  It is permitted to pass in the Token object that was
	// returned from the last call to nextToken() or peekAheadToken().
	//
	// Implementations may assume that calls to this function are rare, perhaps
	// occuring only during error recovery.  In particular, a parser should not
	// use this function to push back lookahead tokens.

	public void pushBackToken (Token token);


	// Retrieves a token from the stream, without removing it from the stream.
	//
	// The distance argument specifies how far ahead in the stream to look.  If
	// distance is 0, the function returns the token that will be returned by
	// the next call to nextToken().  The effect of this function is the same
	// as making distance+1 calls to nextToken(), saving the last token, and
	// then making distance+1 calls to pushBackToken().
	//
	// If distance is past the end of the stream, this function should return
	// with token.number = Token.EOF.  If distance is negative, this function
	// should throw IllegalArgumentException.
	//
	// An implementation of Preprocessor is required to support peeking ahead an
	// unlimited number of tokens.  A typical implementation would create a
	// Token object for each peek-ahead token, and queue the Token objects in
	// an ObjectDeque.
	//
	// In order to avoid the overhead of creating a Token object for each
	// call, this function may return a Token object that is also stored
	// internally.  Therefore, the caller must treat the returned Token object
	// as read-only, and may use the returned Token object only until the next
	// call to nextToken() or peekAheadToken().
	//
	// Implementations may assume that calls to this function are rare, perhaps
	// occuring only during error recovery.  In particular, a parser should not
	// use this function to retrieve lookahead tokens.

	public Token peekAheadToken (int distance) throws IOException, SyntaxException;


	// Closes the token source.
	//
	// Typically, this function closes the file from which tokens are being
	// obtained.
	//
	// Note that this function is not allowed to throw IOException.

	public void close ();


}

