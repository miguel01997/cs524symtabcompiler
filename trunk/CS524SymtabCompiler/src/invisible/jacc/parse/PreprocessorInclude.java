// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import invisible.jacc.util.ObjectDeque;

import java.io.IOException;


/*->

  PreprocessorInclude converts a TokenStream into a Preprocessor.  It does
  this by adding the push-back and peek-ahead functions required by a
  Preprocessor.

  In addition, PreprocessorInclude supports stacking of streams.  At any
  time, a new stream can be pushed on the stack.  PreprocessorInclude
  then takes succeeding tokens from the new stream.  When the new stream
  returns end-of-file, PreprocessorInclude closes the new stream, pops the
  stack, and continues reading tokens from the original stream.  This allows
  for processing of include files, transparently to the client.

  Stacking needs to be integrated with the push-back and peek-ahead functions
  to be sure they interact correctly.

  PreprocessorInclude recognizes two special token types:

	The end-of-file token, denoted by Token.EOF.  When an end-of-file token
	is received from any stream other than the original stream, the token is
	deleted and the stream stack is popped.  An end-of-file token obtained
	from the original stream is returned to the client unchanged.

	The insert-stream escape token, denoted by Token.escapeInsertStream.  When
	this token is received, the token's value object is cast to a TokenStream,
	and the stream is pushed onto the stream stack.  The effect is that the
	entire contents of the stream is inserted at the point where the single
	token would have been inserted.

  All other token types are passed through to the client unchanged.

  Tokens that are pushed back are not given any special treatment, even if they
  are end-of-file or insert-stream tokens.  Tokens that are pushed back are
  always returned to the client unchanged.

->*/


public class PreprocessorInclude implements Preprocessor
{

	// The current TokenStream object.

	private TokenStream _stream;


	// A deque containing the buffered tokens.  Each element is a Token
	// object.  The first element is the next to be returned by nextToken().

	private ObjectDeque _bufferedTokens;


	// The number of buffered tokens for the current stream.
	//
	// In _bufferedTokens, elements 0 through _bufferedTokenCount-1 are
	// associated with the current stream.  That means nextToken() returns
	// these elements before reading the next token from the current stream.
	//
	// In _bufferedTokens, elements _bufferedTokenCount and up are associated
	// with stacked streams.  These elements are not returned by nextToken()
	// until the stream stack is popped.

	private int _bufferedTokenCount;


	// The stack of streams.  Each element is a PreprocessorSimpleState
	// object.  The first element is the most recently suspended stream, and
	// the last element is the original stream.
	//
	// An element is pushed on the front of this stack when pushTokenStream()
	// is called.  An element is popped off the front of this stack when the
	// current stream reaches end-of-file.

	private ObjectDeque _streamStack;


	// The last token we returned.

	private Token _token;


	// The number of buffered EOF tokens for the current stream.
	//
	// If the current stream is the original stream, and we have received
	// end-of-file from the original stream, then this value is 1.  Otherwise,
	// it is 0.
	//
	// When we get an end-of-file token from the original stream, we push it
	// onto the token buffer, and we keep it permanently in the token buffer
	// until close() is called.  This allows us to return end-of-file as many
	// times as needed, without calling the original stream more than once.
	//
	// The end-of-file token is counted both in _bufferedTokenCount and in
	// _bufferedEOFCount.  Therefore, _bufferedTokenCount is always greater
	// than or equal to _bufferedEOFCount.

	private int _bufferedEOFCount;




	// The constructor saves the supplied TokenStream object.

	public PreprocessorInclude (TokenStream stream)
	{
		super ();

		// Validate the argument

		if (stream == null)
		{
			throw new NullPointerException ("PreprocessorInclude.PreprocessorInclude");
		}

		// Save the stream

		_stream = stream;

		// Initialize the token buffer

		_bufferedTokens = new ObjectDeque ();
		_bufferedTokenCount = 0;
		_bufferedEOFCount = 0;

		// Initialize the stream stack

		_streamStack = new ObjectDeque ();

		// No token object yet

		_token = null;

		return;
	}




	// Internal routine to pop the stream stack.  The caller must check that
	// the stream stack is nonempty.

	private void popStreamStack ()
	{

		// Close the current stream

		try
		{
			_stream.close();
		}

		// Discard any I/O exception during the close

		catch (IOException e)
		{
		}

		// Get the first element from the stream stack

		PreprocessorSimpleState priorState =
			(PreprocessorSimpleState) _streamStack.popFirst();

		// Restore the prior stream

		_stream = priorState._stream;

		// Add the prior stream's buffered tokens to the token queue

		_bufferedTokenCount += priorState._bufferedTokenCount;

		_bufferedEOFCount += priorState._bufferedEOFCount;

		return;
	}




	// Internal routine to push the stream stack.
	//
	// The tokensToPush argument specifies the number of buffered tokens to
	// push onto the stream stack.  The pushed tokens are taken from the end
	// of the buffered token queue.  The effect is that all the tokens of
	// newStream are inserted before the last tokensToPush buffered tokens.
	//
	// The value of tokensToPush must be between _bufferedEOFCount and
	// _bufferedTokenCount inclusive.

	private void pushStreamStack (TokenStream newStream, int tokensToPush)
	{

		// Save the current state

		_streamStack.pushFirst (
			new PreprocessorSimpleState (_stream, tokensToPush, _bufferedEOFCount) );

		// Adjust the counts of buffered tokens

		_bufferedTokenCount -= tokensToPush;

		_bufferedEOFCount = 0;

		// Establish the new stream

		_stream = newStream;

		return;
	}





	// Get the next token.
	//
	// Implements the nextToken() method of Preprocessor.

	public Token nextToken () throws IOException, SyntaxException
	{

		// Loop until we find a token

		for ( ; ; )
		{

			// If there is a buffered token for the current stream, return it

			if (_bufferedTokenCount != 0)
			{

				// If we have reached end-of-file on the original stream,
				// and this is the last buffered token ...

				if (_bufferedTokenCount == _bufferedEOFCount)
				{

					// Get the end-of-file token, but don't remove it from the queue

					_token = (Token) _bufferedTokens.peekFirst();
				}

				// Otherwise, we're not removing the final end-of-file token ...

				else
				{

					// Pop the first buffered token

					_token = (Token) _bufferedTokens.popFirst();

					// One less buffered token

					--_bufferedTokenCount;
				}

				// Return the token

				return _token;
			}

			// Get next token from the current stream

			_token = _stream.nextToken ();

			// If it's a normal token, return it

			if (_token.number > 0)
			{
				return _token;
			}

			// Otherwise, if it's an end-of-file token ...

			else if (_token.number == Token.EOF)
			{

				// If there is a stacked stream ...

				if (!_streamStack.isEmpty())
				{

					// Pop the stream stack and try again

					popStreamStack ();

					continue;
				}

				// Push the end-of-file token onto the buffer

				_bufferedTokens.pushFirst (_token);

				// One more buffered token for the current stream

				++_bufferedTokenCount;

				// Count a buffered end-of-file token

				++_bufferedEOFCount;
			}

			// Otherwise, if it's an insert-stream escape token ...

			else if (_token.number == Token.escapeInsertStream)
			{

				// Insert the new stream after any buffered tokens.  This means
				// that any tokens currently buffered become associated with the
				// new stream, and the (stacked) old stream has no buffered tokens.
				// However, if there is a buffered end-of-file token, the new
				// stream is inserted just before it.

				pushStreamStack ((TokenStream) _token.value, _bufferedEOFCount);

				// Try again

				continue;
			}

			// In all other cases, return the token

			return _token;
		}

	}




	// Push back a token.
	//
	// Implements the pushBackToken() method of Preprocessor.

	public void pushBackToken (Token token)
	{

		// Copy token and push it onto the front of the deque

		_bufferedTokens.pushFirst (new Token (token));

		// One more buffered token for the current stream

		++_bufferedTokenCount;

		return;
	}




	// Peek ahead a token.
	//
	// Implements the peekAheadToken() method of Preprocessor.

	public Token peekAheadToken (int distance) throws IOException, SyntaxException
	{

		// Validate the argument

		if (distance < 0)
		{
			throw new IllegalArgumentException ("PreprocessorInclude.peekAheadToken");
		}

		// While we don't have enough buffered tokens ...

		while (_bufferedTokenCount <= distance)
		{

			// If the distance is past the original stream's end-of-file token ...

			if (_bufferedEOFCount != 0)
			{

				// Just return the buffered end-of-file token

				_token = (Token) _bufferedTokens.peekFirst(_bufferedTokenCount - 1);

				return _token;
			}

			// Read the next token from the stream

			_token = _stream.nextToken ();

			// If it's an end-of-file token ...

			if (_token.number == Token.EOF)
			{

				// If there is a stacked stream ...

				if (!_streamStack.isEmpty())
				{

					// Pop the stream stack and try again

					popStreamStack ();

					continue;
				}

				// Push the end-of-file token onto the end of the buffer

				_bufferedTokens.pushFirst (_token, _bufferedTokenCount++);

				// Indicate we reached end-of-file on the original stream

				++_bufferedEOFCount;

				// Try again

				continue;
			}

			// Otherwise, if it's an insert-stream escape token ...

			else if (_token.number == Token.escapeInsertStream)
			{

				// Insert the new stream after any buffered tokens.  This means
				// that any tokens currently buffered become associated with the
				// new stream, and the (stacked) old stream has no buffered tokens.
				// However, if there is a buffered end-of-file token, the new
				// stream is inserted just before it.

				pushStreamStack ((TokenStream) _token.value, _bufferedEOFCount);

				// Try again

				continue;
			}

			// Push the new token onto the end of the buffered tokens for
			// the current stream (note we must use pushFirst because there
			// may be additional buffered tokens for stacked streams)

			_bufferedTokens.pushFirst (new Token (_token), _bufferedTokenCount++);
		}

		// Return the buffered token

		return ((Token) _bufferedTokens.peekFirst(distance));
	}




	// Close the source.
	//
	// Implements the close() method of Preprocessor.

	public void close ()
	{

		// While there are stacked streams ...

		while (!_streamStack.isEmpty())
		{

			// Pop the stream stack (this closes the current stream)

			popStreamStack ();
		}

		// Dump buffered tokens

		_bufferedTokens.removeAllElements();

		_bufferedTokenCount = 0;

		_bufferedEOFCount = 0;

		_token = null;

		// Close the original stream

		try
		{
			_stream.close();
		}

		// Discard any I/O exception during the close

		catch (IOException e)
		{
		}

		return;
	}




	// Push the specified stream onto the internal stream stack.
	//
	// The new stream is inserted before any buffered tokens.  This means that
	// nextToken() will return all the tokens in the new stream before it
	// returns any tokens that have been buffered by push-back or peek-ahead
	// operations.  In other words, the new stream is inserted at the client's
	// current position.

	public void pushBackStream (TokenStream stream)
	{

		// Insert the new stream before any buffered tokens.  This means that
		// any tokens currently buffered become associated with the old stream,
		// and the new stream begins life with no buffered tokens.

		pushStreamStack (stream, _bufferedTokenCount);

		return;
	}


}




// PreprocessorSimpleState holds the stacked state of a stream.

final class PreprocessorSimpleState
{
	
	// The stream

	TokenStream _stream;

	// The number of buffered tokens

	int _bufferedTokenCount;

	// The number of buffered end-of-file tokens

	int _bufferedEOFCount;


	// Constructor just saves its arguments.

	PreprocessorSimpleState (TokenStream stream, int bufferedTokenCount,
		int bufferedEOFCount)
	{
		super ();

		_stream = stream;
		_bufferedTokenCount = bufferedTokenCount;
		_bufferedEOFCount = bufferedEOFCount;

		return;
	}


}

