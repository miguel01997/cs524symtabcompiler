// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import invisible.jacc.util.ArrayIO;
import invisible.jacc.util.ArrayRLE;
import invisible.jacc.util.IODataFormatException;
import invisible.jacc.util.JavaSourceOutputStream;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;
import java.io.PrintStream;


/*->

  ScannerTable holds the tables required for Scanner.

  For convenience, all the contained tables are declared public.  This allows
  generators to insert the generated tables, and it allows scanners to get the
  tables.  The alternative would be to provide accessor functions for each
  table, which would involve writing a lot of functions for no real purpose.


  GENERATING TABLES

  The class ParserGenerator contains functions to generate scanner tables.
  Using ParserGenerator, you supply a specification of the tokens, and then
  ParserGenerator creates the resulting ScannerTable object.

  Once you have a ScannerTable object, you will probably want to save it for
  future use.  There are two ways to do this.

  You can use writeToStream() to write the ScannerTable to an output stream.
  This can be used to save the tables into a file.

  Alternatively, you can use writeToJavaSource() to create a subclass of
  ScannerTable that contains all the tables as literals.  This produces a Java
  source file that you can compile.


  USING THE TABLES

  To use the tables, you first have to retrieve them from where they are
  stored.

  If the tables are stored in a file, you can create a new ScannerTable object
  and then call readFromStream() to load the tables from an input stream.

  If the tables are stored as literals in a subclass, you can create a new
  instance of the subclass, and cast the result to ScannerTable.  When you
  create the subclass object, the tables are automatically loaded from the
  literals.

  After retrieving the tables, you need to link in the token factories.  To do
  this, call linkFactory() once for each combination of token name and link
  name that has a token factory.  ScannerTable automatically constructs an
  internal table containing all the token factories.  (If you don't supply
  factories for all token definitions, ScannerTable automatically supplies
  default factories.  If a token definition's parameter is nonzero, the default
  factory assembles a token whose number is the parameter, and whose value is
  null;  otherwise, the default factory discards the token.)

  If you want to enable tracing, call setTrace() and specify the destination
  for tracing output.  If you enable tracing, ScannerTable inserts code to
  write a message every time a token definition is recognized.  This is useful
  for debugging.

  Next, if you are using multiple start conditions, you need to link the start
  conditions.  To do this, use lookupCondition() to get the number of each
  start condition, given its name.  You need to save these numbers.  These
  start condition numbers are the numbers you must pass to Scanner when setting
  the start condition.  (Note that unlike with token factories, ScannerTable
  does not construct and save a start condition linkage table, because Scanner
  doesn't require one.)

  Finally, pass the ScannerTable object to Scanner.


  OTHER FUNCTIONS

  ScannerTable defines other functions that can be used for special purposes.

  You can call clone() to create a copy of a ScannerTable object.  This is a
  shallow copy which contains the same tables.  This is useful if you want to
  create two different ScannerTable objects, with the same scanning tables but
  different sets of token factories.  A multi-pass compiler might need to do
  this, supplying a different set of token factories for each pass.  Note that
  for this to work, you must call clone() before making any calls to
  linkFactory().

  You can call lookupCondition() to get the number of any start condition,
  given its name.  The reverse mapping, getting the name of a start condition
  given its number, can be done by accessing the _conditionNames table.  This
  may be useful for constructing error messages.

  You can call checkInvariant() to make ScannerTable check its tables for
  internal consistency.  This can be used after retrieving the tables from a
  file, if you want to check that the tables were read properly.  It can also
  be used after linking factories to verify that every factory actually
  corresponds to at least one token definition.

->*/


public class ScannerTable implements Cloneable
{

	// Data stream signature

	public static final long streamSignature = 0x4953FF0053543031L;	//IS..ST01


	// ----- Scanner Tables -----


	// The number of different character categories.
	//
	// Categories are numbered from 0 to _categoryCount-1.

	public int _categoryCount;


	// The character set size.
	//
	// Characters can range from 0 to _charSetSize-1.

	public int _charSetSize;


	// The character category table.
	//
	// For input character b, _categoryTable[b] & 0xFF is the category for
	// character b.  (The logical-and with 0xFF is necessary because the Java
	// byte type is signed, but we want to interpret the category as an
	// unsigned integer ranging from 0 to 255.)  Categories are used as input
	// into the DFA transition tables.

	public byte[] _categoryTable;


	// The number of tokens.
	//
	// Tokens are numbered from 0 to _tokenCount-1.

	public int _tokenCount;


	// The token parameters.
	//
	// For token t, _tokenParam[t] is a nonnegative parameter that is passed
	// to the token factory whenever the corresponding token is recognized.

	public int[] _tokenParam;


	// The number of tokens that have a right context.
	//
	// Context numbers can range from 0 to _contextCount-1.  Note that only
	// tokens defined with a right context have context numbers.

	public int _contextCount;


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

	public int[] _contextNumber;


	// The number of start conditions for the forward DFA.
	//
	// Start conditions are numbered from 0 to _conditionCount-1.

	public int _conditionCount;


	// The number of states in the forward DFA.
	//
	// The states of the DFA are numbered from 0 to _fwdStateCount-1.

	public int _fwdStateCount;


	// The number of recognition codes in the forward DFA.
	//
	// Recognition codes are numbered from 0 to _fwdRecognitionCount-1.

	public int _fwdRecognitionCount;


	// Forward DFA initial state table.
	//
	// For start condition x, _fwdInitialState[x] is the initial DFA state.
	// This is guaranteed to be a valid state (a nonnegative integer less than
	// _fwdStateCount).

	public int[] _fwdInitialState;


	// Forward DFA transition table.
	//
	// For state s and category c:  (i) If s has a transition on c, then
	// _fwdTransitionTable[s][c] is the target state number.  (ii) If s does
	// not have a transition on c, then _fwdTransitionTable[s][c] contains the
	// value _fwdStateCount.

	public short[][] _fwdTransitionTable;


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

	public int[] _fwdRecognitionTable;


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

	public int[] _fwdTokenListLength;

	public int[][] _fwdTokenList;


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

	public boolean[][] _fwdContextSplit;


	// The number of states in the reverse DFA.
	//
	// The states of the DFA are numbered from 0 to _revStateCount-1.

	public int _revStateCount;


	// The number of recognition codes in the reverse DFA.
	//
	// Recognition codes are numbered from 0 to _revRecognitionCount-1.

	public int _revRecognitionCount;


	// Reverse DFA transition table.
	//
	// For state s and category c:  (i) If s has a transition on c, then
	// _revTransitionTable[s][c] is the target state number.  (ii) If s does
	// not have a transition on c, then _revTransitionTable[s][c] contains the
	// value _revStateCount.

	public short[][] _revTransitionTable;


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

	public int[] _revRecognitionTable;


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

	public boolean[][] _revContextSplit;


	// ----- Dynamic Linking Tables -----


	// The names of start conditions.
	//
	// For start condition x, _conditionNames[x] is a String giving the name
	// of the condition.  Each condition is required to have a unique name.

	public String[] _conditionNames;


	// The names of tokens.
	//
	// For token t, _tokenNames[t] is a String giving the name of the token.
	// It is not required that each token have a unique name.

	public String[] _tokenNames;


	// The following array of strings gives the link name associated with each
	// token.  If a token is declared without a link name, then the link name
	// is the empty string (not null).

	public String[] _tokenLink;


	// ----- Name Lookup Tables -----


	// Hash table for finding start condition numbers.
	//
	// Each key in this table is a String object containing the name of a start
	// condition.  Each element is an Integer object whose value is the
	// corresponding start condition number.  This table is the inverse of the
	// _conditionNames array.

	private Hashtable _inverseConditionNames;


	// Hash table for linking token factories.
	//
	// Each key in this table is a ScannerTableFactoryKey object containing the
	// name of a token and the production's link name.  Each element is a
	// TokenFactory object whose value is the corresponding token factory.

	private Hashtable _factoryLinkage;


	// The token factories.
	//
	// For token t, _tokenFactories[t] is the TokenFactory object that is used
	// to process that token.

	private TokenFactory[] _tokenFactories;


	// Tracing output.
	//
	// If tracing is enabled, this is an ErrorOutput object that is the
	// destination of trace output.  If tracing is disabled, this is null.

	private ErrorOutput _traceOut;




	// Constructor creates an empty ScannerTable object.

	public ScannerTable ()
	{
		super();

		// Initialize lookup tables

		_inverseConditionNames = null;

		_factoryLinkage = null;

		_tokenFactories = null;

		_traceOut = null;

		return;
	}




	// Given the name of a start condition, this function returns the
	// corresponding start condition number.  If the name is invalid, this
	// function returns -1.
	//
	// If the name lookup table has not been constructed yet, this function
	// constructs it.

	public int lookupCondition (String name)
	{

		// If the name lookup table is not constructed ...

		if (_inverseConditionNames == null)
		{

			// Create the hash table

			_inverseConditionNames = new Hashtable (_conditionCount * 2);

			// For each start condition ...

			for (int i = 0; i < _conditionCount; ++i)
			{

				// Add the condition to the hash table

				_inverseConditionNames.put (_conditionNames[i], new Integer(i));
			}
		}

		// Get the hash table element for this name

		Integer element = (Integer) _inverseConditionNames.get (name);

		// If name not found, return -1

		if (element == null)
		{
			return -1;
		}

		// Return the integer value of the element

		return element.intValue();
	}




	// This function links a token factory into the scanner tables.
	//
	// The parameters are as follows:
	//
	// tokenName - The name of the token.
	//
	// linkName - The token's link name.  If the production does not have
	//	a link name, this should be an empty string (not null).
	//
	// factory - The token factory for the production.  Note that if there is
	//	more than one token with the same name and the same link name, this
	//	factory will be used for all of them.
	//
	// If no factory is provided for a given token, then a default factory is
	// used.  If the token's parameter is zero, the default factory discards
	// the token.  If the token's parameter is nonzero, the default factory
	// assembles a token whose number equals the parameter, and whose value is
	// null.

	public void linkFactory (String tokenName, String linkName,
		TokenFactory factory)
	{

		// If we don't have a factory linkage table, create one

		if (_factoryLinkage == null)
		{
			_factoryLinkage = new Hashtable ();
		}

		// Add our factory to the linkage table

		_factoryLinkage.put (
			new ScannerTableFactoryKey (tokenName, linkName), factory );

		return;
	}




	// This function sets the destination for tracing output.  If the parameter
	// is null, tracing is disabled.
	//
	// If this function is never called, then tracing is disabled.
	//
	// When tracing is enabled, the token name, link name, parameter, position,
	// and text are written out every time a token is recognized.  This lets
	// you view the operation of the scanner.  Caution:  This produces a large
	// amount of output!

	public void setTrace (ErrorOutput traceOut)
	{
		_traceOut = traceOut;

		return;
	}




	// This function returns the array of token factories.  Only Scanner should
	// use this function.
	//
	// When this function is called for the first time, it constructs the array
	// by completing the factory linkage process and creating default token
	// factories.  Subsequent calls simply return the array previously
	// constructed.

	public TokenFactory[] getFactories ()
	{

		// If the factory array is not constructed yet ...

		if (_tokenFactories == null)
		{

			// If we don't have a factory linkage table, create one

			if (_factoryLinkage == null)
			{
				_factoryLinkage = new Hashtable ();
			}

			// Create the token factory array

			_tokenFactories = new TokenFactory[_tokenCount];

			// Create instances of the default factories

			TokenFactory defFactoryDiscard = new ScannerTableKFDiscard ();

			TokenFactory defFactoryAssemble = new ScannerTableKFAssemble ();

			// For each token ...

			for (int t = 0; t < _tokenCount; ++t)
			{

				// Get the token factory from the dictionary

				_tokenFactories[t] = (TokenFactory) _factoryLinkage.get (
					new ScannerTableFactoryKey (
						_tokenNames[t], _tokenLink[t] ) );

				// If we didn't get one, use the default factory

				if (_tokenFactories[t] == null)
				{
					_tokenFactories[t] = 
						(_tokenParam[t] == 0) ? defFactoryDiscard: defFactoryAssemble;
				}

				// If tracing is enabled ...

				if (_traceOut != null)
				{

					// Install a debugging token factory

					_tokenFactories[t] = new ScannerTableKFDebug (
						_tokenFactories[t],
						_tokenNames[t]
							+ " {" + _tokenLink[t] + "}"
							+ " #" + _tokenParam[t],
						_traceOut );
				}
			}
		}

		// Return the token factory array

		return _tokenFactories;
	}


	

	// Write the scanner table to a data stream.

	public void writeToStream (DataOutput stream) throws IOException
	{

		// Write the table signature

		stream.writeLong (streamSignature);

		// Write the number of character categories

		stream.writeInt (_categoryCount);

		// Write the character set size

		stream.writeInt (_charSetSize);

		// Write the number of tokens

		stream.writeInt (_tokenCount);

		// Write the number of contexts

		stream.writeInt (_contextCount);

		// Write the number of initial conditions

		stream.writeInt (_conditionCount);

		// Write the number of states in the forward DFA

		stream.writeInt (_fwdStateCount);

		// Write the number of recognition codes in the forward DFA

		stream.writeInt (_fwdRecognitionCount);

		// Write the number of states in the reverse DFA

		stream.writeInt (_revStateCount);

		// Write the number of recognition codes in the reverse DFA

		stream.writeInt (_revRecognitionCount);

		// Write the character category table

		ArrayIO.writeByte1D (stream, ArrayIO.formatByteRLE, _categoryTable);

		// Write the token parameters

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _tokenParam);

		// Write the context number table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _contextNumber);

		// Write the forward DFA initial state table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _fwdInitialState);

		// Write the forward DFA transition table

		ArrayIO.writeShort2D (stream, ArrayIO.formatShortRLE, _fwdTransitionTable);

		// Write the forward DFA recognition table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _fwdRecognitionTable);

		// Write the forward DFA token list lengths

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _fwdTokenListLength);

		// Write the forward DFA token lists

		ArrayIO.writeInt2D (stream, ArrayIO.formatInt, _fwdTokenList);

		// Write the forward DFA context split table

		ArrayIO.writeBoolean2D (stream, ArrayIO.formatBooleanRLE, _fwdContextSplit);

		// Write the reverse DFA transition table

		ArrayIO.writeShort2D (stream, ArrayIO.formatShortRLE, _revTransitionTable);

		// Write the reverse DFA recognition table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _revRecognitionTable);

		// Write the reverse DFA context split table

		ArrayIO.writeBoolean2D (stream, ArrayIO.formatBooleanRLE, _revContextSplit);

		// Write the names of start conditions

		ArrayIO.writeString1D (stream, ArrayIO.formatStringUTF, _conditionNames);

		// Write the names of tokens

		ArrayIO.writeString1D (stream, ArrayIO.formatStringUTF, _tokenNames);

		// Write the token link names

		ArrayIO.writeString1D (stream, ArrayIO.formatStringUTF, _tokenLink);

		// Done writing stream

		return;
	}



	
	// Read the scanner table from a data stream.

	public void readFromStream (DataInput stream) throws IOException
	{

		// Read the scanner table signature

		long inputSignature = stream.readLong ();

		if (inputSignature != streamSignature)
		{
			throw new IODataFormatException (
				"ScannerTable.readFromStream: Invalid signature");
		}

		// Read the number of character categories

		_categoryCount = stream.readInt();

		// Read the character set size

		_charSetSize = stream.readInt();

		// Read the number of tokens

		_tokenCount = stream.readInt();

		// Read the number of contexts

		_contextCount = stream.readInt();

		// Read the number of initial conditions

		_conditionCount = stream.readInt();

		// Read the number of states in the forward DFA

		_fwdStateCount = stream.readInt();

		// Read the number of recognition codes in the forward DFA

		_fwdRecognitionCount = stream.readInt();

		// Read the number of states in the reverse DFA

		_revStateCount = stream.readInt();

		// Read the number of recognition codes in the reverse DFA

		_revRecognitionCount = stream.readInt();

		// Read the character category table

		_categoryTable = ArrayIO.readByte1D (stream, ArrayIO.formatByteRLE);

		// Read the token parameters

		_tokenParam = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the context number table

		_contextNumber = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the forward DFA initial state table

		_fwdInitialState = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the forward DFA transition table

		_fwdTransitionTable = ArrayIO.readShort2D (stream, ArrayIO.formatShortRLE);

		// Read the forward DFA recognition table

		_fwdRecognitionTable = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the forward DFA token list lengths

		_fwdTokenListLength = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the forward DFA token lists

		_fwdTokenList = ArrayIO.readInt2D (stream, ArrayIO.formatInt);

		// Read the forward DFA context split table

		_fwdContextSplit = ArrayIO.readBoolean2D (stream, ArrayIO.formatBooleanRLE);

		// Read the reverse DFA transition table

		_revTransitionTable = ArrayIO.readShort2D (stream, ArrayIO.formatShortRLE);

		// Read the reverse DFA recognition table

		_revRecognitionTable = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the reverse DFA context split table

		_revContextSplit = ArrayIO.readBoolean2D (stream, ArrayIO.formatBooleanRLE);

		// Read the names of start conditions

		_conditionNames = ArrayIO.readString1D (stream, ArrayIO.formatStringUTF);

		// Read the names of tokens

		_tokenNames = ArrayIO.readString1D (stream, ArrayIO.formatStringUTF);

		// Read the token link names

		_tokenLink = ArrayIO.readString1D (stream, ArrayIO.formatStringUTF);

		// Done reading stream

		return;
	}




	// This function checks the object to make sure that its contents are
	// internally consistent.
	//
	// If the contents are consistent, the function returns null.
	//
	// If any inconsistency is found, the function returns a String describing
	// the problem.

	public String checkInvariant ()
	{

		// Check number of categories

		if ((_categoryCount < 1) || (_categoryCount > 256))
		{
			return "Invalid category count";
		}

		// Check character set size

		if (_charSetSize < 2)
		{
			return "Invalid character set size";
		}

		// Check token count

		if (_tokenCount <= 0)
		{
			return "Invalid token count";
		}

		// Check context count

		if ((_contextCount < 0) || (_contextCount > _tokenCount))
		{
			return "Invalid context count";
		}

		// Check condition count

		if (_conditionCount <= 0)
		{
			return "Invalid condition count";
		}

		// Check forward state count

		if (_fwdStateCount <= 0)
		{
			return "Invalid forward DFA state count";
		}

		// Check forward recognition count

		if ((_fwdRecognitionCount <= 0) || (_fwdRecognitionCount > _fwdStateCount))
		{
			return "Invalid forward DFA recognition count";
		}

		// Check reverse state count

		if (_revStateCount <= 0)
		{
			return "Invalid reverse DFA state count";
		}

		// Check reverse recognition count

		if ((_revRecognitionCount <= 0) || (_revRecognitionCount > _revStateCount))
		{
			return "Invalid reverse DFA recognition count";
		}

		// Check the character category table

		if (_categoryTable == null)
		{
			return "Missing character category table";
		}

		if (_categoryTable.length != _charSetSize)
		{
			return "Invalid character category table length";
		}

		for (int i = 0; i < _charSetSize; ++i)
		{
			if ((_categoryTable[i] & 0xFF) >= _categoryCount)
			{
				return "Invalid character category table entry";
			}
		}

		// Check the token parameters

		if (_tokenParam == null)
		{
			return "Missing token parameter array";
		}

		if (_tokenParam.length != _tokenCount)
		{
			return "Invalid token parameter array length";
		}

		for (int i = 0; i < _tokenCount; ++i)
		{
			if (_tokenParam[i] < 0)
			{
				return "Invalid token parameter";
			}
		}

		// Check the context number table

		if (_contextNumber == null)
		{
			return "Missing context number table";
		}

		if (_contextNumber.length != _tokenCount)
		{
			return "Invalid context number table length";
		}

		for (int i = 0; i < _tokenCount; ++i)
		{
			if ((_contextNumber[i] < 0) || (_contextNumber[i] > _contextCount))
			{
				return "Invalid context number table entry";
			}
		}

		// Check the forward initial state table

		if (_fwdInitialState == null)
		{
			return "Missing forward DFA initial state table";
		}

		if (_fwdInitialState.length != _conditionCount)
		{
			return "Invalid forward DFA initial state table length";
		}

		for (int i = 0; i < _conditionCount; ++i)
		{
			if ((_fwdInitialState[i] < 0) || (_fwdInitialState[i] >= _fwdStateCount))
			{
				return "Invalid forward DFA initial state table entry";
			}
		}

		// Check the forward transition table

		if (_fwdTransitionTable == null)
		{
			return "Missing forward DFA transition table";
		}

		if (_fwdTransitionTable.length != _fwdStateCount)
		{
			return "Invalid forward DFA transition table length";
		}

		for (int i = 0; i < _fwdStateCount; ++i)
		{
			if (_fwdTransitionTable[i] == null)
			{
				return "Missing forward DFA transition table subarray";
			}

			if (_fwdTransitionTable[i].length != _categoryCount)
			{
				return "Invalid forward DFA transition table subarray length";
			}

			for (int j = 0; j < _categoryCount; ++j)
			{
				if ((_fwdTransitionTable[i][j] < 0)
					|| (_fwdTransitionTable[i][j] > _fwdStateCount))
				{
					return "Invalid forward DFA transition table entry";
				}
			}
		}

		// Check the forward recognition table

		if (_fwdRecognitionTable == null)
		{
			return "Missing forward DFA recognition table";
		}

		if (_fwdRecognitionTable.length != _fwdStateCount)
		{
			return "Invalid forward DFA recognition table length";
		}

		for (int i = 0; i < _fwdStateCount; ++i)
		{
			if ((_fwdRecognitionTable[i] < 0)
				|| (_fwdRecognitionTable[i] >= _fwdRecognitionCount))
			{
				return "Invalid forward DFA recognition table entry";
			}
		}

		// Check the forward token list length array

		if (_fwdTokenListLength == null)
		{
			return "Missing forward DFA token list length array";
		}

		if (_fwdTokenListLength.length != _fwdRecognitionCount)
		{
			return "Invalid forward DFA token list length array length";
		}

		for (int i = 0; i < _fwdRecognitionCount; ++i)
		{
			if ((_fwdTokenListLength[i] < 0)
				|| (_fwdTokenListLength[i] > _tokenCount))
			{
				return "Invalid forward DFA token list length array entry";
			}
		}

		// Check the forward token list

		if (_fwdTokenList == null)
		{
			return "Missing forward DFA token list";
		}

		if (_fwdTokenList.length != _fwdRecognitionCount)
		{
			return "Invalid forward DFA token list length";
		}

		for (int i = 0; i < _fwdRecognitionCount; ++i)
		{
			if (_fwdTokenList[i] == null)
			{
				return "Missing forward DFA token list subarray";
			}

			if (_fwdTokenList[i].length != _fwdTokenListLength[i])
			{
				return "Invalid forward DFA token list subarray length";
			}

			for (int j = 0; j < _fwdTokenListLength[i]; ++j)
			{
				if ((_fwdTokenList[i][j] < 0)
					|| (_fwdTokenList[i][j] >= _tokenCount)
					|| ((j > 0) && (_fwdTokenList[i][j] <= _fwdTokenList[i][j-1])) )
				{
					return "Invalid forward DFA token list entry";
				}
			}
		}

		// Check the forward context split table

		if (_fwdContextSplit == null)
		{
			return "Missing forward DFA context split table";
		}

		if (_fwdContextSplit.length != _fwdRecognitionCount)
		{
			return "Invalid forward DFA context split table length";
		}

		for (int i = 0; i < _fwdRecognitionCount; ++i)
		{
			if (_fwdContextSplit[i] == null)
			{
				return "Missing forward DFA context split table subarray";
			}

			if (_fwdContextSplit[i].length != _contextCount)
			{
				return "Invalid forward DFA context split table subarray length";
			}
		}

		// Check the reverse transition table

		if (_revTransitionTable == null)
		{
			return "Missing reverse DFA transition table";
		}

		if (_revTransitionTable.length != _revStateCount)
		{
			return "Invalid reverse DFA transition table length";
		}

		for (int i = 0; i < _revStateCount; ++i)
		{
			if (_revTransitionTable[i] == null)
			{
				return "Missing reverse DFA transition table subarray";
			}

			if (_revTransitionTable[i].length != _categoryCount)
			{
				return "Invalid reverse DFA transition table subarray length";
			}

			for (int j = 0; j < _categoryCount; ++j)
			{
				if ((_revTransitionTable[i][j] < 0)
					|| (_revTransitionTable[i][j] > _revStateCount))
				{
					return "Invalid reverse DFA transition table entry";
				}
			}
		}

		// Check the reverse recognition table

		if (_revRecognitionTable == null)
		{
			return "Missing reverse DFA recognition table";
		}

		if (_revRecognitionTable.length != _revStateCount)
		{
			return "Invalid reverse DFA recognition table length";
		}

		for (int i = 0; i < _revStateCount; ++i)
		{
			if ((_revRecognitionTable[i] < 0)
				|| (_revRecognitionTable[i] >= _revRecognitionCount))
			{
				return "Invalid reverse DFA recognition table entry";
			}
		}

		// Check the reverse context split table

		if (_revContextSplit == null)
		{
			return "Missing reverse DFA context split table";
		}

		if (_revContextSplit.length != _revRecognitionCount)
		{
			return "Invalid reverse DFA context split table length";
		}

		for (int i = 0; i < _revRecognitionCount; ++i)
		{
			if (_revContextSplit[i] == null)
			{
				return "Missing reverse DFA context split table subarray";
			}

			if (_revContextSplit[i].length != _contextCount)
			{
				return "Invalid reverse DFA context split table subarray length";
			}
		}

		// Check the names of start conditions

		if (_conditionNames == null)
		{
			return "Missing condition name array";
		}

		if (_conditionNames.length != _conditionCount)
		{
			return "Invalid condition name array length";
		}

		for (int i = 0; i < _conditionCount; ++i)
		{
			if (_conditionNames[i] == null)
			{
				return "Missing condition name";
			}
		}

		// Check the names of tokens

		if (_tokenNames == null)
		{
			return "Missing token name array";
		}

		if (_tokenNames.length != _tokenCount)
		{
			return "Invalid token name array length";
		}

		for (int i = 0; i < _tokenCount; ++i)
		{
			if (_tokenNames[i] == null)
			{
				return "Missing token name";
			}
		}

		// Check the token link names

		if (_tokenLink == null)
		{
			return "Missing token link name array";
		}

		if (_tokenLink.length != _tokenCount)
		{
			return "Invalid token link name array length";
		}

		for (int i = 0; i < _tokenCount; ++i)
		{
			if (_tokenLink[i] == null)
			{
				return "Missing token link name";
			}
		}

		// Check the factory linkage table

		if (_factoryLinkage != null)
		{
			for (Enumeration e = _factoryLinkage.keys(); e.hasMoreElements(); )
			{
				ScannerTableFactoryKey key = (ScannerTableFactoryKey) e.nextElement();

				checkKey:
				{
					for (int t = 0; t < _tokenCount; ++t)
					{
						if (key._tokenName.equals (_tokenNames[t])
							&& key._linkName.equals (_tokenLink[t]))
						{
							break checkKey;
						}
					}

					return "Unknown token factory '"
						+ key._tokenName + "{" + key._linkName + "}'";
				}
			}
		}

		// Success

		return null;
	}




	// This function writes a Java source file for a subclass of ScannerTable.
	// The subclass contains all the generated scanner tables, written as
	// literal constants.
	//
	// The constructor for the subclass automatically sets all the scanner
	// tables to the literal constants.  This saves the time that would
	// otherwise be required to read the tables in from a file.
	//
	// The parameters to this function specify the stream to which the Java
	// source is to be written, the name to be used for the subclass's
	// package, and the name to be used for the subclass itself.
	//
	// If useRLE is true, the tables are RLE encoded.  The tables are
	// automatically decoded the first time an object of this class is
	// constructed.  This reduces the size of the class file, at the expense
	// of the time required for RLE decoding.  (Note, however, that the Java
	// VM Specification implies a limit of approximately 8000 array initializer
	// elements per class.  Thus, RLE encoding may be mandatory for large
	// tables.)
	//
	// The return value is stream.checkError(), which is true if there was
	// an I/O error.

	public boolean writeToJavaSource (PrintStream stream, String packageName,
		String className, boolean useRLE)
	{

		// Create a Java source output stream object

		JavaSourceOutputStream out = new JavaSourceOutputStream (stream);

		// Write file header

		out.line ("// File generated by " + ProductInfo.product 
				      + " version " + ProductInfo.version + "." );
		out.line ("// " + ProductInfo.product + " is " + ProductInfo.copyright 
					  + ((ProductInfo.copyright.charAt(ProductInfo.copyright.length()-1) == '.') ? "" : ".") );
		out.line ();

		// Write package statement

		if (packageName != null && packageName.length() != 0)
		{
			out.line ("package " + packageName + ";");
			out.line ();
		}

		// Write the import statement

		out.line ("import invisible.jacc.parse.ScannerTable;");
		if (useRLE)
		{
			out.line ("import invisible.jacc.util.ArrayRLE;");
		}
		out.line ();

		// Write the class statement

		out.line ("public class " + className + " extends ScannerTable");
		out.openScope ();
		out.line ();

		// Write the tables

		out.line ("// The number of different character categories.");
		out.line ();
		out.text ("private static final int gen_categoryCount = ");
		out.literal (_categoryCount);
		out.line (";");
		out.line ();

		out.line ("// The character set size.");
		out.line ();
		out.text ("private static final int gen_charSetSize = ");
		out.literal (_charSetSize);
		out.line (";");
		out.line ();

		out.line ("// The character category table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static byte[] gen_categoryTable = null;");
			out.line ("private static short[] rle_categoryTable = ");
			out.literal (ArrayRLE.byte1DToByteRLE (_categoryTable));
		}
		else
		{
			out.line ("private static final byte[] gen_categoryTable = ");
			out.literal (_categoryTable);
		}
		out.line (";");
		out.line ();

		out.line ("// The number of tokens.");
		out.line ();
		out.text ("private static final int gen_tokenCount = ");
		out.literal (_tokenCount);
		out.line (";");
		out.line ();

		out.line ("// The token parameters.");
		out.line ();
		out.line ("private static final int[] gen_tokenParam = ");
		out.literal (_tokenParam);
		out.line (";");
		out.line ();

		out.line ("// The number of tokens that have a right context.");
		out.line ();
		out.text ("private static final int gen_contextCount = ");
		out.literal (_contextCount);
		out.line (";");
		out.line ();

		out.line ("// Context number table.");
		out.line ();
		out.line ("private static final int[] gen_contextNumber = ");
		out.literal (_contextNumber);
		out.line (";");
		out.line ();

		out.line ("// The number of start conditions for the forward DFA.");
		out.line ();
		out.text ("private static final int gen_conditionCount = ");
		out.literal (_conditionCount);
		out.line (";");
		out.line ();

		out.line ("// The number of states in the forward DFA.");
		out.line ();
		out.text ("private static final int gen_fwdStateCount = ");
		out.literal (_fwdStateCount);
		out.line (";");
		out.line ();

		out.line ("// The number of recognition codes in the forward DFA.");
		out.line ();
		out.text ("private static final int gen_fwdRecognitionCount = ");
		out.literal (_fwdRecognitionCount);
		out.line (";");
		out.line ();

		out.line ("// Forward DFA initial state table.");
		out.line ();
		out.line ("private static final int[] gen_fwdInitialState = ");
		out.literal (_fwdInitialState);
		out.line (";");
		out.line ();

		out.line ("// Forward DFA transition table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static short[][] gen_fwdTransitionTable = null;");
			out.line ("private static short[] rle_fwdTransitionTable = ");
			out.literal (ArrayRLE.short2DToShortRLE (_fwdTransitionTable));
		}
		else
		{
			out.line ("private static final short[][] gen_fwdTransitionTable = ");
			out.literal (_fwdTransitionTable);
		}
		out.line (";");
		out.line ();

		out.line ("// Forward DFA recognition table.");
		out.line ();
		out.line ("private static final int[] gen_fwdRecognitionTable = ");
		out.literal (_fwdRecognitionTable);
		out.line (";");
		out.line ();

		out.line ("// Forward DFA token list.");
		out.line ();
		out.line ("private static final int[] gen_fwdTokenListLength = ");
		out.literal (_fwdTokenListLength);
		out.line (";");
		out.line ();
		out.line ("private static final int[][] gen_fwdTokenList = ");
		out.literal (_fwdTokenList);
		out.line (";");
		out.line ();

		out.line ("// Forward DFA context split table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static boolean[][] gen_fwdContextSplit = null;");
			out.line ("private static byte[] rle_fwdContextSplit = ");
			out.literal (ArrayRLE.boolean2DToBooleanRLE (_fwdContextSplit));
		}
		else
		{
			out.line ("private static final boolean[][] gen_fwdContextSplit = ");
			out.literal (_fwdContextSplit);
		}
		out.line (";");
		out.line ();

		out.line ("// The number of states in the reverse DFA.");
		out.line ();
		out.text ("private static final int gen_revStateCount = ");
		out.literal (_revStateCount);
		out.line (";");
		out.line ();

		out.line ("// The number of recognition codes in the reverse DFA.");
		out.line ();
		out.text ("private static final int gen_revRecognitionCount = ");
		out.literal (_revRecognitionCount);
		out.line (";");
		out.line ();

		out.line ("// Reverse DFA transition table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static short[][] gen_revTransitionTable = null;");
			out.line ("private static short[] rle_revTransitionTable = ");
			out.literal (ArrayRLE.short2DToShortRLE (_revTransitionTable));
		}
		else
		{
			out.line ("private static final short[][] gen_revTransitionTable = ");
			out.literal (_revTransitionTable);
		}
		out.line (";");
		out.line ();

		out.line ("// Reverse DFA recognition table.");
		out.line ();
		out.line ("private static final int[] gen_revRecognitionTable = ");
		out.literal (_revRecognitionTable);
		out.line (";");
		out.line ();

		out.line ("// Reverse DFA context split table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static boolean[][] gen_revContextSplit = null;");
			out.line ("private static byte[] rle_revContextSplit = ");
			out.literal (ArrayRLE.boolean2DToBooleanRLE (_revContextSplit));
		}
		else
		{
			out.line ("private static final boolean[][] gen_revContextSplit = ");
			out.literal (_revContextSplit);
		}
		out.line (";");
		out.line ();

		out.line ("// The names of start conditions.");
		out.line ();
		out.line ("private static final String[] gen_conditionNames = ");
		out.literal (_conditionNames);
		out.line (";");
		out.line ();

		out.line ("// The names of tokens.");
		out.line ();
		out.line ("private static final String[] gen_tokenNames = ");
		out.literal (_tokenNames);
		out.line (";");
		out.line ();

		out.line ("// The link name for each token.");
		out.line ();
		out.line ("private static final String[] gen_tokenLink = ");
		out.literal (_tokenLink);
		out.line (";");
		out.line ();

		out.line ();
		out.line ();
		out.line ();

		// Write the RLE decoding function

		if (useRLE)
		{
			out.line ("// RLE decoding function");
			out.line ();

			out.line ("private static synchronized void decodeRLE ()");
			out.openScope ();

			out.line ("if (gen_categoryTable == null)");
			out.openScope ();
			out.line ("gen_categoryTable = ArrayRLE.byteRLEToByte1D (rle_categoryTable);");
			out.line ("rle_categoryTable = null;");
			out.closeScope ();
			out.line ();

			out.line ("if (gen_fwdTransitionTable == null)");
			out.openScope ();
			out.line ("gen_fwdTransitionTable = ArrayRLE.shortRLEToShort2D (rle_fwdTransitionTable);");
			out.line ("rle_fwdTransitionTable = null;");
			out.closeScope ();
			out.line ();

			out.line ("if (gen_fwdContextSplit == null)");
			out.openScope ();
			out.line ("gen_fwdContextSplit = ArrayRLE.booleanRLEToBoolean2D (rle_fwdContextSplit);");
			out.line ("rle_fwdContextSplit = null;");
			out.closeScope ();
			out.line ();

			out.line ("if (gen_revTransitionTable == null)");
			out.openScope ();
			out.line ("gen_revTransitionTable = ArrayRLE.shortRLEToShort2D (rle_revTransitionTable);");
			out.line ("rle_revTransitionTable = null;");
			out.closeScope ();
			out.line ();

			out.line ("if (gen_revContextSplit == null)");
			out.openScope ();
			out.line ("gen_revContextSplit = ArrayRLE.booleanRLEToBoolean2D (rle_revContextSplit);");
			out.line ("rle_revContextSplit = null;");
			out.closeScope ();
			out.line ();

			out.line ("return;");
			out.closeScope ();

			out.line ();
			out.line ();
			out.line ();
			out.line ();
		}

		// Write the constructor

		out.line ("// Constructor installs the generated tables into the ScannerTable");
		out.line ();

		out.line ("public " + className + " ()");
		out.openScope ();

		out.line ("super ();");
		out.line ();

		if (useRLE)
		{
			out.line ("// Decode the run-length-encoded tables");
			out.line ();
			out.line ("decodeRLE ();");
			out.line ();
		}

		out.line ("// Copy scanning tables into the ScannerTable superclass");
		out.line ();
		out.line ("_categoryCount = gen_categoryCount;");
		out.line ("_charSetSize = gen_charSetSize;");
		out.line ("_categoryTable = gen_categoryTable;");
		out.line ("_tokenCount = gen_tokenCount;");
		out.line ("_tokenParam = gen_tokenParam;");
		out.line ("_contextCount = gen_contextCount;");
		out.line ("_contextNumber = gen_contextNumber;");
		out.line ("_conditionCount = gen_conditionCount;");
		out.line ("_fwdStateCount = gen_fwdStateCount;");
		out.line ("_fwdRecognitionCount = gen_fwdRecognitionCount;");
		out.line ("_fwdInitialState = gen_fwdInitialState;");
		out.line ("_fwdTransitionTable = gen_fwdTransitionTable;");
		out.line ("_fwdRecognitionTable = gen_fwdRecognitionTable;");
		out.line ("_fwdTokenListLength = gen_fwdTokenListLength;");
		out.line ("_fwdTokenList = gen_fwdTokenList;");
		out.line ("_fwdContextSplit = gen_fwdContextSplit;");
		out.line ("_revStateCount = gen_revStateCount;");
		out.line ("_revRecognitionCount = gen_revRecognitionCount;");
		out.line ("_revTransitionTable = gen_revTransitionTable;");
		out.line ("_revRecognitionTable = gen_revRecognitionTable;");
		out.line ("_revContextSplit = gen_revContextSplit;");
		out.line ();

		out.line ("// Copy dynamic-link tables into the ScannerTable superclass");
		out.line ();
		out.line ("_conditionNames = gen_conditionNames;");
		out.line ("_tokenNames = gen_tokenNames;");
		out.line ("_tokenLink = gen_tokenLink;");
		out.line ();

		out.line ("return;");
		out.closeScope ();

		out.line ();
		out.line ();

		// End the class

		out.closeScope ();
		out.line ();

		// Return error flag

		return stream.checkError ();
	}



	
	// Creates a copy of the tables.  This is a shallow copy.  The clone object
	// shares its contained tables with the original.

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			ScannerTable cloneTable = (ScannerTable) super.clone();

			// Return the clone

			return cloneTable;
		}
		catch (CloneNotSupportedException e)
		{
		
			// This should never happen, since we are Cloneable

			throw new InternalError();
		}
	}




}




// Key for looking up token factories.

final class ScannerTableFactoryKey
{

	// Name of the token.

	String _tokenName;

	// Token link name.

	String _linkName;


	// Constructor just saves its parameters.

	ScannerTableFactoryKey (String tokenName, String linkName)
	{
		_tokenName = tokenName;
		_linkName = linkName;

		return;
	}


	// Two keys are equal if both strings are equal.

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof ScannerTableFactoryKey)))
		{
			return false;
		}

		ScannerTableFactoryKey other = (ScannerTableFactoryKey) obj;

		return this._tokenName.equals (other._tokenName)
			&& this._linkName.equals (other._linkName);
	}


	// Hash code is the exclusive-or of the hash codes of the two strings.

	public int hashCode ()
	{
		return _tokenName.hashCode() ^ _linkName.hashCode();
	}


}




// A token factory that always discards the token.  This is used as the
// default token factory for any token whose parameter is zero.

final class ScannerTableKFDiscard extends TokenFactory
{

	// Make a token object.
	//
	// Implements the makeToken() method of TokenFactory.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{
		return discard;
	}


	// The constructor does nothing

	public ScannerTableKFDiscard ()
	{
		super ();

		return;
	}


}




// A token factory that always assembles the token.  This is used as the
// default token factory for any token whose parameter is nonzero.

final class ScannerTableKFAssemble extends TokenFactory
{

	// Make a token object.
	//
	// Implements the makeToken() method of TokenFactory.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{
		return assemble;
	}


	// The constructor does nothing

	public ScannerTableKFAssemble ()
	{
		super ();

		return;
	}


}




/*->

  ScannerTableKFDebug is a debugging token factory.  ScannerTableKFDebug
  contains a "real" token factory, a token name, and an ErrorOutput object.

  The makeToken() routine writes the token name, position, and text to the
  ErrorOutput object.  Then it calls the "real" token factory.  Therefore, a
  complete record of all tokens is written out.

  This is useful for debugging a scanner specification, to ensure that tokens
  are being recognized as intended.  Caution:  The amount of output generated
  can be very large.

->*/


final class ScannerTableKFDebug extends TokenFactory
{

	// The contained token factory

	private TokenFactory _factory;

	// The name we use

	private String _tokenName;

	// The destination for our output

	private ErrorOutput _errOut;


	// Make a token object.
	//
	// Implements the makeToken() method of TokenFactory.

	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Write the token to the error output
		
		_errOut.reportError (ErrorOutput.typeInformational, null,
			token.file, token.line, token.column, null,
			_tokenName + " '" + scanner.tokenToString () + "'" );

		// Call the contained token factory

		return _factory.makeToken (scanner, token);
	}


	// The constructor saves its arguments

	public ScannerTableKFDebug (TokenFactory factory, String tokenName, ErrorOutput errOut)
	{
		super ();

		_factory = factory;
		_tokenName = tokenName;
		_errOut = errOut;

		return;
	}


}

