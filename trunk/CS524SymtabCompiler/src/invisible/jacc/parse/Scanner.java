// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  Scanner converts a stream of characters into a stream of tokens.

  Scanner reads input from a Preprocessor object.  It delivers output by
  implementing the TokenStream interface.

  In actuality, Scanner is an abstract class.  This file defines two concrete
  implementations called ScannerByte and ScannerChar, which take their input
  as a stream of byte or a stream of char, respectively.  The factory function
  makeScanner() creates a ScannerByte object if given a PrescannerByte, or
  a ScannerChar object if given a PrescannerChar.  (Note that Prescanner
  is actually an "abstract" superinterface;  an implementation of Prescanner
  must also implement either PrescannerByte or PrescannerChar.)

  Most client code doesn't care if the source text is byte or char.  Therefore,
  the class Scanner abstracts away from the source text type, allowing client
  code to be independent of the source text type.

  The conversion is driven by a pair of deterministic finite automata.  The
  tables that define the finite automata are obtained from a ScannerTable
  object.

->*/


public abstract class Scanner implements TokenStream
{

	// The token object.  This object is created when the scanner is created.
	// All calls to nextToken() return this same object, and all calls to
	// makeToken() pass this same object.
	//
	// _token.number is set to the token parameter before calling makeToken().
	// On return, it contains the token number, or 0 to discard the token.
	//
	// _token.value is set to null before calling makeToken().  On return, it
	// contains the token value.
	//
	// _token.file contains the file name.  It is set when the scanner is
	// created, and is not subsequently changed.
	//
	// _token.line contains the line number.  It is initialized when the
	// scanner is created.  A token factory that recognizes end-of-line may
	// alter it.
	//
	// _token.column contains the column number.  It is initialized when
	// the scanner is created, and is incremented by the length of each
	// recognized token.  A token factory that recognizes end-of-line may
	// alter it.

	protected Token _token;


	// The client.

	protected ScannerClient _client;

	public final ScannerClient client ()
	{
		return _client;
	}


	// The start of the current token.
	//
	// During a call to TokenFactory.makeToken(), the first character of the
	// current token is _dataBuffer[_tokenStart].
	//
	// At other times, _dataBuffer[_tokenStart] is the first character where
	// the scanner will begin scanning for the next token.
	//
	// A token factory or client may use tokenStart() to get the current value
	// of _tokenStart.
	//
	// A token factory or client may use setTokenStart() to change the value of
	// _tokenStart.  The new value must be between _tokenStart and _dataEnd,
	// inclusive.  This allows a token factory or client to discard input
	// without the scanner processing it.  This is useful, e.g., to extract
	// embedded data that does not contain normal language text.
	//
	// If a token factory calls setTokenStart(), it may not reject the token.

	protected int _tokenStart;

	public final int tokenStart ()
	{
		return _tokenStart;
	}

	public final void setTokenStart (int newTokenStart)
	{
		if ((newTokenStart < _tokenStart) || (newTokenStart > _dataEnd))
		{
			throw new IllegalArgumentException ("ScannerByte.setTokenStart");
		}

		_tokenStart = newTokenStart;

		return;
	}


	// The end of buffered data, plus 1.  The last character of data in the
	// buffer is _dataBuffer[_dataEnd-1].
	//
	// A token factory or client is allowed to read data from
	// _dataBuffer[_tokenStart] to _dataBuffer[_dataEnd-1].  Data outside
	// this range is inaccessible.  If a token factory or client needs
	// additional data, it can call readData() to obtain it.

	protected int _dataEnd;

	public final int dataEnd ()
	{
		return _dataEnd;
	}


	// The length of the current token.
	//
	// During a call to TokenFactory.makeToken(), _tokenLength contains the
	// length of the token, in characters.  For tokens that are defined with a
	// right context, this length includes only the token proper, not the
	// context expression.  After makeToken() returns, the scanner advances
	// the input by adding _tokenLength to _tokenStart.
	//
	// At other times, _tokenLength has no meaning.
	//
	// A token factory may use tokenLength() to get the current value of
	// _tokenLength.
	//
	// A token factory may use setTokenLength() to change the value of
	// _tokenLength.  The new value must be a non-negative integer not
	// exceeding _dataEnd-_tokenStart.  After makeToken() returns, scanning
	// resumes at buffer position _tokenStart+_tokenLength.  This allows a
	// token factory to specify where scanning should resume after this token.
	//
	// If a token factory rejects the token (by makeToken() returning false),
	// the value of _tokenLength is ignored.

	protected int _tokenLength;

	public final int tokenLength ()
	{
		return _tokenLength;
	}

	public final void setTokenLength (int newTokenLength)
	{
		if ((newTokenLength < 0) || (newTokenLength > (_dataEnd - _tokenStart)))
		{
			throw new IllegalArgumentException ("ScannerByte.setTokenLength");
		}

		_tokenLength = newTokenLength;

		return;
	}


	// The length of the current context.
	//
	// During a call to TokenFactory.makeToken(), _contextLength contains the
	// length of the context, in characters.  For tokens that are defined with
	// a right context, this length includes both the token proper and the
	// context expression.  Thus, _dataBuffer[_tokenStart+_tokenLength] through
	// _dataBuffer[_tokenStart+_contextLength-1] matches the context
	// expression.  For tokens that are defined without a right context,
	// _contextLength is the same as _tokenLength.
	//
	// At other times, _contextLength has no meaning.
	//
	// A token factory may use contextLength() to get the current value of
	// _contextLength.

	protected int _contextLength;

	public final int contextLength ()
	{
		return _contextLength;
	}


	// The current token index number.
	//
	// During a call to TokenFactory.makeToken(), _tokenIndex contains the
	// index number of the current token.  The index number identifies the
	// particular regular expression that was recognized.
	//
	// At other times, _tokenIndex contains the index number of the last token
	// recognized.
	//
	// A token factory or client may us tokenIndex() to get the current value
	// of _tokenIndex.

	protected int _tokenIndex;

	public final int tokenIndex ()
	{
		return _tokenIndex;
	}


	// The forward DFA recognition sequence.
	//
	// As a token is scanned with the forward DFA, the recognition code of each
	// state is stored in this array.  _recognitionSequence[0] contains the
	// recognition code of the initial state.  (Thus, _contextLength may be
	// used as an index into _recognitionSequence).
	//
	// Note:  Scanner code assumes that _recognitionSequence.length >= 2, so
	// it is always possible to scan at least one character.

	protected int[] _recognitionSequence;


	// The current start condition.
	//
	// When the scanner is created, the start condition is initialized to 0.
	//
	// A token factory or client may use condition() to get the current start
	// condition.
	//
	// A token factory may use setCondition() to change the value of the start
	// condition.  The new value must be a non-negative integer, less than the
	// number of start conditions defined in the scanner table.
	//
	// The value of _condition is sampled only when the scanner begins scanning
	// for a token.  In particular, if makeToken() changes the start condition
	// and then rejects the token, the choice of the next token factory is not
	// affected;  the scanner remembers the original scan.

	protected int _condition;

	public final int condition ()
	{
		return _condition;
	}

	public final void setCondition (int newCondition)
	{
		if ((newCondition < 0) || (newCondition >= _conditionCount))
		{
			throw new IllegalArgumentException ("ScannerByte.setCondition");
		}

		_condition = newCondition;

		return;
	}


	// The scanner table object.
	//
	// A token factory may call scannerTable to retrieve the object.

	protected ScannerTable _scannerTable;

	public final ScannerTable scannerTable ()
	{
		return _scannerTable;
	}


	// This flag is true if we have reached end-of-file on the input.

	protected boolean _reachedEOF;


	// The client parameters.
	//
	// Client code and token factories may use this public variable for
	// any desired purpose.

	public Object clientParams;


	// ----- Dynamic Linking Table -----


	// The array of token factories.

	protected TokenFactory[] _tokenFactories;


	// ----- Scanner Tables -----


	// The number of different character categories.
	//
	// Categories are numbered from 0 to _categoryCount-1.

	protected int _categoryCount;


	// The character set size.
	//
	// Characters can range from 0 to _charSetSize-1.

	protected int _charSetSize;


	// The character category table.
	//
	// For input character b, _categoryTable[b] & 0xFF is the category for
	// character b.  (The logical-and with 0xFF is necessary because the Java
	// byte type is signed, but we want to interpret the category as an
	// unsigned integer ranging from 0 to 255.)  Categories are used as input
	// into the DFA transition tables.

	protected byte[] _categoryTable;


	// The number of tokens.
	//
	// Tokens are numbered from 0 to _tokenCount-1.

	protected int _tokenCount;


	// The token parameters.
	//
	// For token t, _tokenParam[t] is a nonnegative parameter that is passed
	// to the token factory whenever the corresponding token is recognized.

	protected int[] _tokenParam;


	// The number of tokens that have a right context.
	//
	// Context numbers can range from 0 to _contextCount-1.  Note that only
	// tokens defined with a right context have context numbers.

	protected int _contextCount;


	// Context number table.
	//
	// For token t:  (i) If the token is defined with a right context,
	// _contextNumber[t] is the context number for token t.  This is a
	// nonnegative integer less than _contextCount.  (ii) If the token does
	// not have a right context, _contextNumber[t] contains _contextCount.
	//
	// The context number may be used as an index into the context split
	// tables.  Context numbers exist to reduce the size of context split
	// tables, since typically most tokens don't have a right context.

	protected int[] _contextNumber;


	// The number of start conditions for the forward DFA.
	//
	// Start conditions are numbered from 0 to _conditionCount-1.

	protected int _conditionCount;


	// The number of states in the forward DFA.
	//
	// The states of the DFA are numbered from 0 to _fwdStateCount-1.

	protected int _fwdStateCount;


	// The number of recognition codes in the forward DFA.
	//
	// Recognition codes are numbered from 0 to _fwdRecognitionCount-1.

	protected int _fwdRecognitionCount;


	// Forward DFA initial state table.
	//
	// For start condition x, _fwdInitialState[x] is the initial DFA state.
	// This is guaranteed to be a valid state (a nonnegative integer less than
	// _fwdStateCount).

	protected int[] _fwdInitialState;


	// Forward DFA transition table.
	//
	// For state s and category c:  (i) If s has a transition on c, then
	// _fwdTransitionTable[s][c] is the target state number.  (ii) If s does
	// not have a transition on c, then _fwdTransitionTable[s][c] contains the
	// value _fwdStateCount.

	protected short[][] _fwdTransitionTable;


	// Forward DFA recognition table.
	//
	// For state s, _fwdRecognitionTable[s] contains the recognition code for
	// state s.  The recognition code is an integer that encodes the set of
	// regular expressions that are recognized by state s.  The recognition
	// code may be used as an index into the token and context split tables.
	//
	// For states s1 and s2, _fwdRecognitionTable[s1] == _fwdRecognitionTable[s2]
	// if and only if s1 and s2 recognize exactly the same set of regular
	// expressions.  Furthermore, _fwdRecognitionTable[s] == 0 if and only if
	// s does not recognize any regular expressions (i.e., s is not a final
	// state of the DFA).

	protected int[] _fwdRecognitionTable;


	// Forward DFA token list.
	//
	// For recognition code r, _fwdTokenList[r] is an array containing a list
	// of the tokens recognized.  (A token that is defined with a right context
	// is considered to be recognized if the catenation of the token expression
	// and the context expression is recognized.)  The list is sorted by
	// increasing token number.  _fwdTokenListLength[r] is the length of the
	// list (an integer between 0 and _tokenCount inclusive).
	//
	// (In practice, each token list typically has a length of 1 or 2.  So,
	// even though _fwdTokenList is a two-dimensional array, there is no reason
	// to use a data type smaller than int, because its memory consumption is
	// typically no worse than a one-dimensional array.)

	protected int[] _fwdTokenListLength;

	protected int[][] _fwdTokenList;


	// Forward DFA context split table.
	//
	// For recognition code r and context number y, _fwdContextSplit[r][y] is
	// true if a context split is recognized.
	//
	// Note that context numbers are defined only for tokens that have a right
	// context (so that y is less than _contextCount).  For the forward DFA,
	// a context split is recognized if and only if the token expression is
	// recognized.  This is used to split the recognized string into the token
	// part and the right-context part.

	protected boolean[][] _fwdContextSplit;


	// The number of states in the reverse DFA.
	//
	// The states of the DFA are numbered from 0 to _revStateCount-1.

	protected int _revStateCount;


	// The number of recognition codes in the reverse DFA.
	//
	// Recognition codes are numbered from 0 to _revRecognitionCount-1.

	protected int _revRecognitionCount;


	// Reverse DFA transition table.
	//
	// For state s and category c:  (i) If s has a transition on c, then
	// _revTransitionTable[s][c] is the target state number.  (ii) If s does
	// not have a transition on c, then _revTransitionTable[s][c] contains the
	// value _revStateCount.

	protected short[][] _revTransitionTable;


	// Reverse DFA recognition table.
	//
	// For state s, _revRecognitionTable[s] contains the recognition code for
	// state s.  The recognition code is an integer that encodes the set of
	// regular expressions that are recognized by state s.  The recognition
	// code may be used as an index into the context split table.
	//
	// For states s1 and s2, _revRecognitionTable[s1] == _revRecognitionTable[s2]
	// if and only if s1 and s2 recognize exactly the same set of regular
	// expressions.  Furthermore, _revRecognitionTable[s] == 0 if and only if
	// s does not recognize any regular expressions (i.e., s is not a final
	// state of the DFA).

	protected int[] _revRecognitionTable;


	// Reverse DFA context split table.
	//
	// For recognition code r and context number y, _revContextSplit[r][y] is
	// true if a context split is recognized.
	//
	// Note that context numbers are defined only for tokens that have a right
	// context (so that y is less than _contextCount).  For the reverse DFA,
	// a context split is recognized if and only if the reverse of the context
	// expression is recognized.  This is used to split the recognized string
	// into the token part and the right-context part.

	protected boolean[][] _revContextSplit;

	


	// Factory function to create a scanner.
	//
	// client - A ScannerClient object that represents the scanner's client.
	//
	// source - A Prescanner object that supplies the input.
	//
	// scannerTable - A ScannerTable object that supplies the scanning tables
	//	which define the deterministic finite automata.
	//
	// file - A String which specifies the file name.  This file name is
	//	returned in the file field of all Token objects.
	//
	// line - An int that contains the initial line number, typically 1.
	//
	// column - An int that contains the initial column, typically 1.
	//
	// bufSize - The recommended initial buffer size for reading the source.
	//
	// params - Initial value for the clientParams variable, which the client
	//	may use for any purpose.

	public static Scanner makeScanner (ScannerClient client, Prescanner source,
		ScannerTable scannerTable, String file, int line, int column,
		int bufSize, Object params)
	{

		// Validate the source

		if (source == null)
		{
			throw new NullPointerException ("Scanner.makeScanner");
		}

		// If the source is a PrescannerByte ...

		if (source instanceof PrescannerByte)
		{

			// Create a ScannerByte object

			return new ScannerByte (client, (PrescannerByte) source,
				scannerTable, file, line, column, bufSize, params );
		}

		// Otherwise, if the source is a PrescannerChar ...

		if (source instanceof PrescannerChar)
		{

			// Create a ScannerChar object

			return new ScannerChar (client, (PrescannerChar) source,
				scannerTable, file, line, column, bufSize, params );
		}

		// Otherwise, it's an error

		throw new IllegalArgumentException ("Scanner.makeScanner");
	}

	


	// Constructor to create a scanner.
	//
	// Note that this is a protected constructor, so only subclasses can
	// create a Scanner.
	//
	// client - A ScannerClient object that represents the scanner's client.
	//
	// scannerTable - A ScannerTable object that supplies the scanning tables
	//	which define the deterministic finite automata.
	//
	// file - A String which specifies the file name.  This file name is
	//	returned in the file field of all Token objects.
	//
	// line - An int that contains the initial line number, typically 1.
	//
	// column - An int that contains the initial column, typically 1.
	//
	// bufSize - The recommended initial buffer size for reading the source.
	//
	// params - Initial value for the clientParams variable, which the client
	//	may use for any purpose.

	protected Scanner (ScannerClient client, 
		ScannerTable scannerTable, String file, int line, int column,
		int bufSize, Object params)
	{
		super ();

		// Save the client

		if (client == null)
		{
			throw new NullPointerException ("Scanner.Scanner");
		}

		_client = client;

		// Save the parameters

		clientParams = params;

		// Initialize the data pointers

		_tokenStart = 0;
		_dataEnd = 0;

		_tokenLength = 0;
		_contextLength = 0;

		_reachedEOF = false;

		// Initialize the recognition sequence array

		if (bufSize < 0)
		{
			throw new IllegalArgumentException ("Scanner.Scanner");
		}

		_recognitionSequence = new int[Math.min (255, Math.max (32, bufSize)) + 1];

		// Initialize the Token object

		_token = new Token (0, null, file, line, column);

		// Initialize the start condition

		_condition = 0;

		// Save the scanner table object

		if (scannerTable == null)
		{
			throw new NullPointerException ("Scanner.Scanner");
		}

		_scannerTable = scannerTable;

		// Copy tables from the ScannerTable object

		_categoryCount = scannerTable._categoryCount;
		_charSetSize = scannerTable._charSetSize;
		_categoryTable = scannerTable._categoryTable;
		_tokenCount = scannerTable._tokenCount;
		_tokenParam = scannerTable._tokenParam;
		_contextCount = scannerTable._contextCount;
		_contextNumber = scannerTable._contextNumber;
		_conditionCount = scannerTable._conditionCount;
		_fwdStateCount = scannerTable._fwdStateCount;
		_fwdRecognitionCount = scannerTable._fwdRecognitionCount;
		_fwdInitialState = scannerTable._fwdInitialState;
		_fwdTransitionTable = scannerTable._fwdTransitionTable;
		_fwdRecognitionTable = scannerTable._fwdRecognitionTable;
		_fwdTokenListLength = scannerTable._fwdTokenListLength;
		_fwdTokenList = scannerTable._fwdTokenList;
		_fwdContextSplit = scannerTable._fwdContextSplit;
		_revStateCount = scannerTable._revStateCount;
		_revRecognitionCount = scannerTable._revRecognitionCount;
		_revTransitionTable = scannerTable._revTransitionTable;
		_revRecognitionTable = scannerTable._revRecognitionTable;
		_revContextSplit = scannerTable._revContextSplit;

		_tokenFactories = scannerTable.getFactories();

		return;
	}




	// Enlarge the recognition sequence array, preserving its contents.

	protected void enlargeRecognitionSequence ()
	{

		// Allocate a new array, 3/2 the size of the original

		int[] newRecognitionSequence =
			new int[_recognitionSequence.length + ((_recognitionSequence.length + 1) >> 1)];

		// Copy existing class sequence into the new array

		System.arraycopy (
			_recognitionSequence, 0, newRecognitionSequence, 0, _recognitionSequence.length );

		// Adopt the new array

		_recognitionSequence = newRecognitionSequence;

		return;
	}




	// Read additional data from the source.  The return value is the number of
	// characters read, or 0 if end of file.
	//
	// Data from _tokenStart to _dataEnd-1 are preserved, although they may be
	// moved to the start of the buffer.  The buffer is reallocated if a larger
	// buffer is needed.  This function can change _dataBuffer, _tokenStart,
	// and _dataEnd.

	public abstract int readData () throws IOException, SyntaxException;




	// This function returns true if the raw text is an array of byte,
	// false if it is an array of char.

	public abstract boolean isByteText ();




	// If _dataBuffer is of type byte[], then this function returns _dataBuffer.
	// Otherwise, it returns null.

	public byte[] rawByteText ()
	{
		return null;
	}




	// If _dataBuffer is of type char[], then this function returns _dataBuffer.
	// Otherwise, it returns null.

	public char[] rawCharText ()
	{
		return null;
	}




	// Convert raw text to a String object.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// converted to a String object.  If the text is byte, each character is
	// zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.

	public abstract String rawTextToString (int off, int len);




	// Obtain one character from the raw text.
	//
	// The contents of _dataBuffer[off] is returned as a char.  (If _dataBuffer
	// is of type byte[], then the byte is converted to char by zero-extension.)
	//
	// The character must lie between _tokenStart and _dataEnd.

	public abstract char rawTextCharAt (int off);




	// Copy raw text into a char array.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// copied into dst[dstOff] through dst[dstOff+len-1].  If the text is byte,
	// each character is zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.

	public abstract void rawTextToChars (int off, int len, char[] dst, int dstOff);




	// Read the next token.
	//
	// Implements the nextToken() method of TokenStream.

	public abstract Token nextToken () throws IOException, SyntaxException;




	// Close the token stream.
	//
	// Implements the close() method of TokenStream.

	public abstract void close () throws IOException;




	// Convert the token text to a String.
	//
	// A new String object is created, whose contents is the entire matching
	// text of the current token.

	public final String tokenToString ()
	{
		return rawTextToString (_tokenStart, _tokenLength);
	}




	// Convert the token text to a String.
	//
	// A new String object is created, whose contents is a substring of the
	// matching text of the current token.  The substring begins at distance
	// 'off' from the start of the token, and has length 'len' characters.
	//
	// The substring must lie within the matching token text.

	public final String tokenToString (int off, int len)
	{

		// Validate the arguments

		if ((off < 0) || (off > _tokenLength) || (len < 0) || (len > (_tokenLength - off)))
		{
			throw new IllegalArgumentException ("Scanner.tokenToString");
		}

		// Create the String

		return rawTextToString (_tokenStart + off, len);
	}




	// Get one character from the token text.
	//
	// The character at distance 'off' from the start of the token is converted
	// to type char and returned.
	//
	// The character must lie within the matching token text.

	public final char tokenCharAt (int off)
	{

		// Validate the argument

		if ((off < 0) || (off >= _tokenLength))
		{
			throw new IllegalArgumentException ("Scanner.tokenCharAt");
		}

		// Return the character

		return rawTextCharAt (_tokenStart + off);
	}




	// Copy the token text into a char array.
	//
	// Characters are copied from the token text into a char array.  The
	// source characters begin at distance 'off' from the start of the token,
	// and have length 'len' characters.  The characters are stored into array
	// 'dst' beginning at offset 'dstOff'.
	//
	// The characters must lie within the matching token text.

	public final void tokenToChars (int off, int len, char[] dst, int dstOff)
	{

		// Validate the arguments

		if ((off < 0) || (off > _tokenLength) || (len < 0) || (len > (_tokenLength - off))
			|| (dstOff < 0) || (dstOff > dst.length) || (len > (dst.length - dstOff)))
		{
			throw new IllegalArgumentException ("Scanner.tokenToChars");
		}

		// Copy the characters

		rawTextToChars (_tokenStart + off, len, dst, dstOff);

		return;
	}




	// Convert the context text to a String.
	//
	// A new String object is created, whose contents is the entire matching
	// text of the current context.

	public final String contextToString ()
	{
		return rawTextToString (_tokenStart, _contextLength);
	}




	// Convert the context text to a String.
	//
	// A new String object is created, whose contents is a substring of the
	// matching text of the current context.  The substring begins at distance
	// 'off' from the start of the token, and has length 'len' characters.
	//
	// The substring must lie within the matching context text.

	public final String contextToString (int off, int len)
	{

		// Validate the arguments

		if ((off < 0) || (off > _contextLength) || (len < 0) || (len > (_contextLength - off)))
		{
			throw new IllegalArgumentException ("Scanner.contextToString");
		}

		// Create the String

		return rawTextToString (_tokenStart + off, len);
	}




	// Get one character from the context text.
	//
	// The character at distance 'off' from the start of the token is converted
	// to type char and returned.
	//
	// The character must lie within the matching context text.

	public final char contextCharAt (int off)
	{

		// Validate the argument

		if ((off < 0) || (off >= _contextLength))
		{
			throw new IllegalArgumentException ("Scanner.contextCharAt");
		}

		// Return the character

		return rawTextCharAt (_tokenStart + off);
	}




	// Copy the context text into a char array.
	//
	// Characters are copied from the context text into a char array.  The
	// source characters begin at distance 'off' from the start of the token,
	// and have length 'len' characters.  The characters are stored into array
	// 'dst' beginning at offset 'dstOff'.
	//
	// The characters must lie within the matching context text.

	public final void contextToChars (int off, int len, char[] dst, int dstOff)
	{

		// Validate the arguments

		if ((off < 0) || (off > _contextLength) || (len < 0) || (len > (_contextLength - off))
			|| (dstOff < 0) || (dstOff > dst.length) || (len > (dst.length - dstOff)))
		{
			throw new IllegalArgumentException ("Scanner.contextToChars");
		}

		// Copy the characters

		rawTextToChars (_tokenStart + off, len, dst, dstOff);

		return;
	}




	// Return the amount of text currently in the buffer

	public final int textLength ()
	{
		return _dataEnd - _tokenStart;
	}




	// Convert text currently in the buffer to a String.
	//
	// A new String object is created, whose contents is a substring of the
	// text currently available in the buffer.  The substring begins at distance
	// 'off' from the start of the token, and has length 'len' characters.
	//
	// The substring must lie within the buffer.

	public final String textToString (int off, int len)
	{
		return rawTextToString (_tokenStart + off, len);
	}




	// Get one character from the text currently in the buffer.
	//
	// The character at distance 'off' from the start of the token is converted
	// to type char and returned.
	//
	// The character must lie within the buffer.

	public final char textCharAt (int off)
	{
		return rawTextCharAt (_tokenStart + off);
	}




	// Copy text currently in the buffer into a char array.
	//
	// Characters are copied from the currently available text into a char
	// array.  The source characters begin at distance 'off' from the start of
	// the token, and have length 'len' characters.  The characters are stored
	// into array 'dst' beginning at offset 'dstOff'.
	//
	// The characters must lie within the buffer.

	public final void textToChars (int off, int len, char[] dst, int dstOff)
	{
		rawTextToChars (_tokenStart + off, len, dst, dstOff);

		return;
	}




	// Increment the line number.
	//
	// This function can be called by a token factory that detects a line-end.
	// It works by incrementing token.line, and setting token.column
	// to 1-_tokenLength.  As a result, the first character after the end of
	// current token is considered to be in column 1 of a new line.

	public final void countLine ()
	{

		// Increment the line number

		_token.line++;

		// Set column number so after incrementing by _tokenLength, it is column 1

		_token.column = 1 - _tokenLength;

		return;
	}


}




// ScannerByte is the concrete implementation of Scanner that accepts input
// text in byte form.

final class ScannerByte extends Scanner implements TokenStream
{


	// The source.

	private PrescannerByte _source;


	// The data buffer used to hold data from the source.
	//
	// A token factory or client may use rawByteText() to get the data buffer.

	private byte[] _dataBuffer;




	// Retrieves the data buffer.
	//
	// Overrides the rawByteText() method of Scanner.

	public final byte[] rawByteText ()
	{
		return _dataBuffer;
	}




	// Indicates if the data buffer is byte or char.
	//
	// Implements the isByteText() method of Scanner.

	public boolean isByteText ()
	{
		return true;
	}




	// Convert raw text to a String object.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// converted to a String object.  If the text is byte, each character is
	// zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.
	//
	// Implements the rawTextToString() method of Scanner.

	public String rawTextToString (int off, int len)
	{

		// Validate the arguments

		if ((off < _tokenStart) || (off > _dataEnd) || (len < 0) || (len > (_dataEnd - off)))
		{
			throw new IllegalArgumentException ("ScannerByte.rawTextToString");
		}

		// Create the new String

		return new String (_dataBuffer, 0, off, len);
	}




	// Obtain one character from the raw text.
	//
	// The contents of _dataBuffer[off] is returned as a char.  (If _dataBuffer
	// is of type byte[], then the byte is converted to char by zero-extension.)
	//
	// The character must lie between _tokenStart and _dataEnd.

	public char rawTextCharAt (int off)
	{

		// Validate the argument

		if ((off < _tokenStart) || (off >= _dataEnd))
		{
			throw new IllegalArgumentException ("ScannerByte.rawTextCharAt");
		}

		// Return result as a char

		return (char)(_dataBuffer[off] & 0xFF);
	}




	// Copy raw text into a char array.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// copied into dst[dstOff] through dst[dstOff+len-1].  If the text is byte,
	// each character is zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.

	public void rawTextToChars (int off, int len, char[] dst, int dstOff)
	{

		// Validate the arguments

		if ((off < _tokenStart) || (off > _dataEnd) || (len < 0) || (len > (_dataEnd - off))
			|| (dstOff < 0) || (dstOff > dst.length) || (len > (dst.length - dstOff)))
		{
			throw new IllegalArgumentException ("ScannerByte.rawTextToChars");
		}

		// Copy the characters

		int srcIndex = off;

		int dstIndex = dstOff;

		int srcLimit = off + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (char)(_dataBuffer[srcIndex++] & 0xFF);
		}

		return;
	}




	// Constructor to create a scanner.
	//
	// client - A ScannerClient object that represents the scanner's client.
	//
	// source - A PrescannerByte object that supplies the input.
	//
	// scannerTable - A ScannerTable object that supplies the scanning tables
	//	which define the deterministic finite automata.
	//
	// file - A String which specifies the file name.  This file name is
	//	returned in the file field of all Token objects.
	//
	// line - An int that contains the initial line number, typically 1.
	//
	// column - An int that contains the initial column, typically 1.
	//
	// bufSize - The recommended initial buffer size for reading the source.
	//
	// params - Initial value for the clientParams variable, which the client
	//	may use for any purpose.

	ScannerByte (ScannerClient client, PrescannerByte source,
		ScannerTable scannerTable, String file, int line, int column,
		int bufSize, Object params)
	{

		// Pass parameters to the Scanner constructor

		super (client, scannerTable, file, line, column, bufSize, params);

		// Save the source

		if (source == null)
		{
			throw new NullPointerException ("ScannerByte.ScannerByte");
		}

		_source = source;

		// Initialize the data buffer

		if (bufSize < 0)
		{
			throw new IllegalArgumentException ("ScannerByte.ScannerByte");
		}

		_dataBuffer = new byte[Math.max (32, bufSize)];

		return;
	}




	// Read additional data from the source.  The return value is the number of
	// characters read, or 0 if end of file.
	//
	// Data from _tokenStart to _dataEnd-1 are preserved, although they may be
	// moved to the start of the buffer.  The buffer is reallocated if a larger
	// buffer is needed.  This function can change _dataBuffer, _tokenStart,
	// and _dataEnd.
	//
	// Overrides the readData() method of Scanner.

	public int readData () throws IOException, SyntaxException
	{

		// If already reached end-of-file, just return 0

		if (_reachedEOF)
		{
			return 0;
		}

		// Length of existing data

		int dataLength = _dataEnd - _tokenStart;

		// If nonnegative, number of characters read;  if negative, amount of
		// buffer space required

		int charsRead = -1;

		// If the buffer is no more than half full ...

		if (dataLength <= (_dataBuffer.length >> 1))
		{

			// If no data ...

			if (dataLength == 0)
			{

				// Reset pointers to start of buffer

				_dataEnd = 0;
				_tokenStart = 0;
			}

			// Otherwise, there is buffered data ...

			else
			{

				// If data isn't confined to the first half of the buffer (which
				// implies _tokenStart is nonzero) ...

				if (_dataEnd > (_dataBuffer.length >> 1))
				{

					// Copy data to start of buffer

					System.arraycopy (_dataBuffer, _tokenStart, _dataBuffer, 0, dataLength);

					// Adjust pointers

					_dataEnd -= _tokenStart;
					_tokenStart = 0;
				}
			}

			// Read from the source

			_token.column += (_dataEnd - _tokenStart);

			charsRead = _source.read (
				_dataBuffer, _dataEnd, _dataBuffer.length - _dataEnd, _token);

			_token.column -= (_dataEnd - _tokenStart);
		}

		// Loop until successful read or end-of-file

		while (charsRead < 0)
		{

			// Allocate a new buffer with at least the minimum required
			// space, and at least 3/2 the size of the old buffer

			byte[] newDataBuffer = new byte[Math.max (dataLength - charsRead,
				_dataBuffer.length + ((_dataBuffer.length + 1) >> 1) )];

			// Copy data to start of new buffer

			if (dataLength != 0)
			{
				System.arraycopy (_dataBuffer, _tokenStart, newDataBuffer, 0, dataLength);
			}

			// Adjust pointers

			_dataEnd -= _tokenStart;
			_tokenStart = 0;

			// Adopt the new buffer

			_dataBuffer = newDataBuffer;

			// Read from the source

			_token.column += (_dataEnd - _tokenStart);

			charsRead = _source.read (
				_dataBuffer, _dataEnd, _dataBuffer.length - _dataEnd, _token);

			_token.column -= (_dataEnd - _tokenStart);
		}

		// Adjust the end-of-data pointer

		_dataEnd += charsRead;

		// If end-of-file, set the flag

		if (charsRead == 0)
		{
			_reachedEOF = true;
		}

		// Return number of characters read

		return charsRead;
	}




	// Read the next token.
	//
	// Implements the nextToken() method of TokenStream.

	public Token nextToken () throws IOException, SyntaxException
	{

		// Main loop to step past discarded tokens and illegal characters

	mainLoop:
		for ( ; ; )
		{

			// If there is no buffered data ...

			if (_dataEnd == _tokenStart)
			{

				// Read more data and check for end of file

				if (readData() == 0)
				{

					// Set up end-of-file token

					_token.number = Token.EOF;
					_token.value = null;

					// Tell client we are at end of file

					_client.scannerEOF (this, _token);

					// Return our contained Token object

					return _token;
				}
			}

			// The number of characters we have processed successfully

			_contextLength = 0;

			// The maximum number of characters we can process without either
			// reading more data or enlarging the recognition sequence

			int maxContextLength =
				Math.min (_dataEnd - _tokenStart, _recognitionSequence.length - 1);

			// Get the initial state, depending on the start condition.
			// Note that this is guaranteed not to be the invalid state.

			int state = _fwdInitialState[_condition];

			// Save the recognition code of this state

			_recognitionSequence[0] = _fwdRecognitionTable[state];

			// Get the first character from the data buffer, get its category,
			// and then get the target state

			state = _fwdTransitionTable[state][
				_categoryTable[_dataBuffer[_tokenStart] & 0xFF] & 0xFF ];

			// Loop until we reach the invalid state

			while (state != _fwdStateCount)
			{

				// Save the recognition code of this state, and increment the
				// number of characters we have processed successfully

				_recognitionSequence[++_contextLength] = _fwdRecognitionTable[state];

				// If we are at the maximum length ...

				if (_contextLength == maxContextLength)
				{

					// If the recognition sequence array is full, enlarge it

					if (_contextLength == (_recognitionSequence.length - 1))
					{
						enlargeRecognitionSequence ();
					}

					// If we have processed all available data ...

					if (_contextLength == (_dataEnd - _tokenStart))
					{

						// Read more data and check for end of file

						if (readData() == 0)
						{

							// We have reached end-of-file with a partial token
							// text, so break out of loop as if end-of-file forces
							// a transition to the invalid state

							break;
						}
					}

					// Update the maximum length

					maxContextLength =
						Math.min (_dataEnd - _tokenStart, _recognitionSequence.length - 1);

				}	// end if at maximum length

				// Get the next character from the data buffer, get its category,
				// and then get the target state

				state = _fwdTransitionTable[state][
					_categoryTable[_dataBuffer[_tokenStart + _contextLength] & 0xFF] & 0xFF ];

			}	// end loop until invalid state

			// At this point, _contextLength is the length of the longest string
			// that matches any initial substring of any token.  We now need to
			// scan backwards to find recognized tokens.  But we don't recognize
			// zero-length tokens, even if the DFA accepts them.

			// Outer loop over context length ...

			for ( ; _contextLength > 0; --_contextLength)
			{

				// Get the list of recognized tokens

				int[] recList = _fwdTokenList[_recognitionSequence[_contextLength]];

				// Inner loop over recognized tokens ...

				for (int recIndex = 0; recIndex < recList.length; ++recIndex)
				{

					// Get the recognized token number

					_tokenIndex = recList[recIndex];

					// Get the recognized context number

					int recContext = _contextNumber[_tokenIndex];

					// Initial token length is the same as context length

					_tokenLength = _contextLength;

					// If there is no right context ...

					if (recContext == _contextCount)
					{

						// Get the parameter for this token

						_token.number = _tokenParam[_tokenIndex];

						// Initialize the token value

						_token.value = null;

						// Call the token factory, and switch on the result

						switch (_tokenFactories[_tokenIndex].makeToken (this, _token))
						{

						case TokenFactory.assemble:

							// Assembled token, skip past its text

							_token.column += _tokenLength;
							_tokenStart += _tokenLength;

							// Return the token

							return _token;

						case TokenFactory.discard:

							// Discard token, skip past its text

							_token.column += _tokenLength;
							_tokenStart += _tokenLength;

							// Start another scan

							continue mainLoop;

						case TokenFactory.reject:

							// Reject token, do nothing

							break;

						default:

							// Otherwise, we got an illegal command

							throw new IllegalArgumentException ("ScannerByte.nextToken");
						}

						// Search for another token

						continue;

					}	// end if no right context

					// Initial state in reverse DFA

					state = 0;

					// Scan backwards, beginning at the end of the current
					// context, looking for a context split.  The forward and
					// reverse DFA's must agree on the split location.  We
					// don't accept zero-length tokens, but we do accept
					// zero-length right contexts.

					while ((_tokenLength > 0) && (state != _revStateCount))
					{

						// If the forward and reverse DFA's both accept this
						// as a split location ...

						if (_fwdContextSplit[_recognitionSequence[_tokenLength]][recContext]
							&& _revContextSplit[_revRecognitionTable[state]][recContext] )
						{

							// Get the parameter for this token

							_token.number = _tokenParam[_tokenIndex];

							// Initialize the token value

							_token.value = null;

							// Call the token factory, and switch on the result

							switch (_tokenFactories[_tokenIndex].makeToken (this, _token))
							{

							case TokenFactory.assemble:

								// Assembled token, skip past its text

								_token.column += _tokenLength;
								_tokenStart += _tokenLength;

								// Return the token

								return _token;

							case TokenFactory.discard:

								// Discard token, skip past its text

								_token.column += _tokenLength;
								_tokenStart += _tokenLength;

								// Start another scan

								continue mainLoop;

							case TokenFactory.reject:

								// Reject token, do nothing

								break;

							default:

								// Otherwise, we got an illegal command

								throw new IllegalArgumentException ("ScannerByte.nextToken");
							}
						}

						// Back up one character, get the character from the data buffer,
						// get its category, and then get the reverse DFA target state

						state = _revTransitionTable[state][_categoryTable[
							_dataBuffer[_tokenStart + (--_tokenLength)] & 0xFF ] & 0xFF ];

					}	// end loop searching for context split

				}	// end inner loop over recognized tokens

			}	// end outer loop over possible context lengths

			// Didn't find any matching tokens, or all token factories rejected
			// the token, so tell the client

			_token.number = 0;

			_token.value = null;

			_client.scannerUnmatchedToken (this, _token);

			// Step forward one character and try again

			_token.column += 1;
			_tokenStart += 1;

		}	// end main loop searching for a token to match

	}	// end nextToken()




	// Close the token stream.
	//
	// Implements the close() method of TokenStream.

	public void close () throws IOException
	{

		// Close the source

		_source.close ();

		return;
	}



}




// ScannerChar is the concrete implementation of Scanner that accepts input
// text in char form.

final class ScannerChar extends Scanner implements TokenStream
{


	// The source.

	private PrescannerChar _source;


	// The data buffer used to hold data from the source.
	//
	// A token factory or client may use dataBuffer() to get the data buffer.

	private char[] _dataBuffer;




	// Retrieves the data buffer.
	//
	// Overrides the rawCharText() method of Scanner.

	public final char[] rawCharText ()
	{
		return _dataBuffer;
	}




	// Indicates if the data buffer is byte or char.
	//
	// Implements the isByteText() method of Scanner.

	public boolean isByteText ()
	{
		return false;
	}




	// Convert raw text to a String object.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// converted to a String object.  If the text is byte, each character is
	// zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.
	//
	// Implements the rawTextToString() method of Scanner.

	public String rawTextToString (int off, int len)
	{

		// Validate the arguments

		if ((off < _tokenStart) || (off > _dataEnd) || (len < 0) || (len > (_dataEnd - off)))
		{
			throw new IllegalArgumentException ("ScannerChar.rawTextToString");
		}

		// Create the new String

		return new String (_dataBuffer, off, len);
	}




	// Obtain one character from the raw text.
	//
	// The contents of _dataBuffer[off] is returned as a char.  (If _dataBuffer
	// is of type byte[], then the byte is converted to char by zero-extension.)
	//
	// The character must lie between _tokenStart and _dataEnd.

	public char rawTextCharAt (int off)
	{

		// Validate the argument

		if ((off < _tokenStart) || (off >= _dataEnd))
		{
			throw new IllegalArgumentException ("ScannerChar.rawTextCharAt");
		}

		// Return result as a char

		return _dataBuffer[off];
	}




	// Copy raw text into a char array.
	//
	// Characters from _dataBuffer[off] through _dataBuffer[off+len-1] are
	// copied into dst[dstOff] through dst[dstOff+len-1].  If the text is byte,
	// each character is zero-extended to 16 bits.
	//
	// All the characters must lie in the region between _tokenStart and
	// _dataEnd, otherwise an exception is thrown.

	public void rawTextToChars (int off, int len, char[] dst, int dstOff)
	{

		// Validate the arguments

		if ((off < _tokenStart) || (off > _dataEnd) || (len < 0) || (len > (_dataEnd - off))
			|| (dstOff < 0) || (dstOff > dst.length) || (len > (dst.length - dstOff)))
		{
			throw new IllegalArgumentException ("ScannerChar.rawTextToChars");
		}

		// Copy the characters

		if (len > 0)
		{
			System.arraycopy (_dataBuffer, off, dst, dstOff, len);
		}

		return;
	}




	// Constructor to create a scanner.
	//
	// client - A ScannerClient object that represents the scanner's client.
	//
	// source - A PrescannerChar object that supplies the input.
	//
	// scannerTable - A ScannerTable object that supplies the scanning tables
	//	which define the deterministic finite automata.
	//
	// file - A String which specifies the file name.  This file name is
	//	returned in the file field of all Token objects.
	//
	// line - An int that contains the initial line number, typically 1.
	//
	// column - An int that contains the initial column, typically 1.
	//
	// bufSize - The recommended initial buffer size for reading the source.
	//
	// params - Initial value for the clientParams variable, which the client
	//	may use for any purpose.

	ScannerChar (ScannerClient client, PrescannerChar source,
		ScannerTable scannerTable, String file, int line, int column,
		int bufSize, Object params)
	{

		// Pass parameters to the Scanner constructor

		super (client, scannerTable, file, line, column, bufSize, params);

		// Save the source

		if (source == null)
		{
			throw new NullPointerException ("ScannerChar.ScannerChar");
		}

		_source = source;

		// Initialize the data buffer

		if (bufSize < 0)
		{
			throw new IllegalArgumentException ("ScannerChar.ScannerChar");
		}

		_dataBuffer = new char[Math.max (32, bufSize)];

		return;
	}




	// Read additional data from the source.  The return value is the number of
	// characters read, or 0 if end of file.
	//
	// Data from _tokenStart to _dataEnd-1 are preserved, although they may be
	// moved to the start of the buffer.  The buffer is reallocated if a larger
	// buffer is needed.  This function can change _dataBuffer, _tokenStart,
	// and _dataEnd.
	//
	// Overrides the readData() method of Scanner.

	public int readData () throws IOException, SyntaxException
	{

		// If already reached end-of-file, just return 0

		if (_reachedEOF)
		{
			return 0;
		}

		// Length of existing data

		int dataLength = _dataEnd - _tokenStart;

		// If nonnegative, number of characters read;  if negative, amount of
		// buffer space required

		int charsRead = -1;

		// If the buffer is no more than half full ...

		if (dataLength <= (_dataBuffer.length >> 1))
		{

			// If no data ...

			if (dataLength == 0)
			{

				// Reset pointers to start of buffer

				_dataEnd = 0;
				_tokenStart = 0;
			}

			// Otherwise, there is buffered data ...

			else
			{

				// If data isn't confined to the first half of the buffer (which
				// implies _tokenStart is nonzero) ...

				if (_dataEnd > (_dataBuffer.length >> 1))
				{

					// Copy data to start of buffer

					System.arraycopy (_dataBuffer, _tokenStart, _dataBuffer, 0, dataLength);

					// Adjust pointers

					_dataEnd -= _tokenStart;
					_tokenStart = 0;
				}
			}

			// Read from the source

			_token.column += (_dataEnd - _tokenStart);

			charsRead = _source.read (
				_dataBuffer, _dataEnd, _dataBuffer.length - _dataEnd, _token);

			_token.column -= (_dataEnd - _tokenStart);
		}

		// Loop until successful read or end-of-file

		while (charsRead < 0)
		{

			// Allocate a new buffer with at least the minimum required
			// space, and at least 3/2 the size of the old buffer

			char[] newDataBuffer = new char[Math.max (dataLength - charsRead,
				_dataBuffer.length + ((_dataBuffer.length + 1) >> 1) )];

			// Copy data to start of new buffer

			if (dataLength != 0)
			{
				System.arraycopy (_dataBuffer, _tokenStart, newDataBuffer, 0, dataLength);
			}

			// Adjust pointers

			_dataEnd -= _tokenStart;
			_tokenStart = 0;

			// Adopt the new buffer

			_dataBuffer = newDataBuffer;

			// Read from the source

			_token.column += (_dataEnd - _tokenStart);

			charsRead = _source.read (
				_dataBuffer, _dataEnd, _dataBuffer.length - _dataEnd, _token);

			_token.column -= (_dataEnd - _tokenStart);
		}

		// Adjust the end-of-data pointer

		_dataEnd += charsRead;

		// If end-of-file, set the flag

		if (charsRead == 0)
		{
			_reachedEOF = true;
		}

		// Return number of characters read

		return charsRead;
	}




	// Read the next token.
	//
	// Implements the nextToken() method of TokenStream.

	public Token nextToken () throws IOException, SyntaxException
	{

		// Main loop to step past discarded tokens and illegal characters

	mainLoop:
		for ( ; ; )
		{

			// If there is no buffered data ...

			if (_dataEnd == _tokenStart)
			{

				// Read more data and check for end of file

				if (readData() == 0)
				{

					// Set up end-of-file token

					_token.number = Token.EOF;
					_token.value = null;

					// Tell client we are at end of file

					_client.scannerEOF (this, _token);

					// Return our contained Token object

					return _token;
				}
			}

			// The number of characters we have processed successfully

			_contextLength = 0;

			// The maximum number of characters we can process without either
			// reading more data or enlarging the recognition sequence

			int maxContextLength =
				Math.min (_dataEnd - _tokenStart, _recognitionSequence.length - 1);

			// Get the initial state, depending on the start condition.
			// Note that this is guaranteed not to be the invalid state.

			int state = _fwdInitialState[_condition];

			// Save the recognition code of this state

			_recognitionSequence[0] = _fwdRecognitionTable[state];

			// Get the first character from the data buffer, get its category,
			// and then get the target state

			state = _fwdTransitionTable[state][
				_categoryTable[_dataBuffer[_tokenStart]] & 0xFF ];

			// Loop until we reach the invalid state

			while (state != _fwdStateCount)
			{

				// Save the recognition code of this state, and increment the
				// number of characters we have processed successfully

				_recognitionSequence[++_contextLength] = _fwdRecognitionTable[state];

				// If we are at the maximum length ...

				if (_contextLength == maxContextLength)
				{

					// If the recognition sequence array is full, enlarge it

					if (_contextLength == (_recognitionSequence.length - 1))
					{
						enlargeRecognitionSequence ();
					}

					// If we have processed all available data ...

					if (_contextLength == (_dataEnd - _tokenStart))
					{

						// Read more data and check for end of file

						if (readData() == 0)
						{

							// We have reached end-of-file with a partial token
							// text, so break out of loop as if end-of-file forces
							// a transition to the invalid state

							break;
						}
					}

					// Update the maximum length

					maxContextLength =
						Math.min (_dataEnd - _tokenStart, _recognitionSequence.length - 1);

				}	// end if at maximum length

				// Get the next character from the data buffer, get its category,
				// and then get the target state

				state = _fwdTransitionTable[state][
					_categoryTable[_dataBuffer[_tokenStart + _contextLength]] & 0xFF ];

			}	// end loop until invalid state

			// At this point, _contextLength is the length of the longest string
			// that matches any initial substring of any token.  We now need to
			// scan backwards to find recognized tokens.  But we don't recognize
			// zero-length tokens, even if the DFA accepts them.

			// Outer loop over context length ...

			for ( ; _contextLength > 0; --_contextLength)
			{

				// Get the list of recognized tokens

				int[] recList = _fwdTokenList[_recognitionSequence[_contextLength]];

				// Inner loop over recognized tokens ...

				for (int recIndex = 0; recIndex < recList.length; ++recIndex)
				{

					// Get the recognized token number

					_tokenIndex = recList[recIndex];

					// Get the recognized context number

					int recContext = _contextNumber[_tokenIndex];

					// Initial token length is the same as context length

					_tokenLength = _contextLength;

					// If there is no right context ...

					if (recContext == _contextCount)
					{

						// Get the parameter for this token

						_token.number = _tokenParam[_tokenIndex];

						// Initialize the token value

						_token.value = null;

						// Call the token factory, and switch on the result

						switch (_tokenFactories[_tokenIndex].makeToken (this, _token))
						{

						case TokenFactory.assemble:

							// Assembled token, skip past its text

							_token.column += _tokenLength;
							_tokenStart += _tokenLength;

							// Return the token

							return _token;

						case TokenFactory.discard:

							// Discard token, skip past its text

							_token.column += _tokenLength;
							_tokenStart += _tokenLength;

							// Start another scan

							continue mainLoop;

						case TokenFactory.reject:

							// Reject token, do nothing

							break;

						default:

							// Otherwise, we got an illegal command

							throw new IllegalArgumentException ("ScannerChar.nextToken");
						}

						// Search for another token

						continue;

					}	// end if no right context

					// Initial state in reverse DFA

					state = 0;

					// Scan backwards, beginning at the end of the current
					// context, looking for a context split.  The forward and
					// reverse DFA's must agree on the split location.  We
					// don't accept zero-length tokens, but we do accept
					// zero-length right contexts.

					while ((_tokenLength > 0) && (state != _revStateCount))
					{

						// If the forward and reverse DFA's both accept this
						// as a split location ...

						if (_fwdContextSplit[_recognitionSequence[_tokenLength]][recContext]
							&& _revContextSplit[_revRecognitionTable[state]][recContext] )
						{

							// Get the parameter for this token

							_token.number = _tokenParam[_tokenIndex];

							// Initialize the token value

							_token.value = null;

							// Call the token factory, and switch on the result

							switch (_tokenFactories[_tokenIndex].makeToken (this, _token))
							{

							case TokenFactory.assemble:

								// Assembled token, skip past its text

								_token.column += _tokenLength;
								_tokenStart += _tokenLength;

								// Return the token

								return _token;

							case TokenFactory.discard:

								// Discard token, skip past its text

								_token.column += _tokenLength;
								_tokenStart += _tokenLength;

								// Start another scan

								continue mainLoop;

							case TokenFactory.reject:

								// Reject token, do nothing

								break;

							default:

								// Otherwise, we got an illegal command

								throw new IllegalArgumentException ("ScannerChar.nextToken");
							}
						}

						// Back up one character, get the character from the data buffer,
						// get its category, and then get the reverse DFA target state

						state = _revTransitionTable[state][_categoryTable[
							_dataBuffer[_tokenStart + (--_tokenLength)]] & 0xFF ];

					}	// end loop searching for context split

				}	// end inner loop over recognized tokens

			}	// end outer loop over possible context lengths

			// Didn't find any matching tokens, or all token factories rejected
			// the token, so tell the client

			_token.number = 0;

			_token.value = null;

			_client.scannerUnmatchedToken (this, _token);

			// Step forward one character and try again

			_token.column += 1;
			_tokenStart += 1;

		}	// end main loop searching for a token to match

	}	// end nextToken()




	// Close the token stream.
	//
	// Implements the close() method of TokenStream.

	public void close () throws IOException
	{

		// Close the source

		_source.close ();

		return;
	}



}


