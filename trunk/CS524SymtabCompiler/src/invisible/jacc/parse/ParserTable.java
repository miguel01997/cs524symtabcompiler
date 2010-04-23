// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import invisible.jacc.util.ArrayIO;
import invisible.jacc.util.ArrayRLE;
import invisible.jacc.util.IODataFormatException;
import invisible.jacc.util.JavaSourceOutputStream;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.Hashtable;


/*->

  ParserTable holds the tables required for Parser.

  For convenience, all the contained tables are declared public.  This allows
  generators to insert the generated tables, and it allows parsers to get the
  tables.  The alternative would be to provide accessor functions for each
  table, which would involve writing a lot of functions for no real purpose.


  GENERATING TABLES

  The class ParserGenerator contains functions to generate parser tables.
  Using ParserGenerator, you supply a specification of the grammar, and then
  ParserGenerator creates the resulting ParserTable object.

  Once you have a ParserTable object, you will probably want to save it for
  future use.  There are two ways to do this.

  You can use writeToStream() to write the ParserTable to an output stream.
  This can be used to save the tables into a file.

  Alternatively, you can use writeToJavaSource() to create a subclass of
  ParserTable that contains all the tables as literals.  This produces a Java
  source file that you can compile.


  USING THE TABLES

  To use the tables, you first have to retrieve them from where they are
  stored.

  If the tables are stored in a file, you can create a new ParserTable object
  and then call readFromStream() to load the tables from an input stream.

  If the tables are stored as literals in a subclass, you can create a new
  instance of the subclass, and cast the result to ParserTable.  When you
  create the subclass object, the tables are automatically loaded from the
  literals.

  After retrieving the tables, you need to link in the nonterminal factories.
  To do this, call linkFactory() once for each combination of left hand side
  and link name that has a nonterminal factory.  ParserTable automatically
  constructs an internal table containing all the nonterminal factories.  (If
  you don't supply factories for all productions, ParserTable automatically
  supplies default factories.  If a production's right hand side is nonempty,
  the default factory returns the value of the first symbol on the right hand
  side;  otherwise, the default factory returns null.)

  If you want to enable tracing, call setTrace() and specify the destination
  for tracing output.  If you enable tracing, ParserTable inserts code to write
  a message every time a production is reduced.  This is useful for debugging.

  Finally, pass the ParserTable object to Parser.


  OTHER FUNCTIONS

  ParserTable defines other functions that can be used for special purposes.

  You can call clone() to create a copy of a ParserTable object.  This is a
  shallow copy which contains the same tables.  This is useful if you want to
  create two different ParserTable objects, with the same parsing tables but
  different sets of nonterminal factories.  A multi-pass compiler might need to
  do this, supplying a different set of nonterminal factories for each pass.
  Note that for this to work, you must call clone() before making any calls to
  linkFactory().

  You can call lookupSymbol() to get the number of any symbol in the grammar,
  given its name.  You can use this to link token factories to their
  corresponding token numbers.  Normally, this linkage is done automatically by
  ParserGenerator;  each token factory is passed a parameter which is normally
  the token number.  In some cases you may have to perform the linkage
  yourself, for example, if a given token factory can return two or more
  different types of tokens.  Also, if the tokens are coming from a source
  other than Scanner, you should use this function to get the token numbers to
  use.

  You can call checkInvariant() to make ParserTable check its tables for
  internal consistency.  This can be used after retrieving the tables from a
  file, if you want to check that the tables were read properly.  It can also
  be used after linking factories to verify that every factory actually
  corresponds to at least one production.

->*/


public class ParserTable implements Cloneable
{

	// Data stream signature

	public static final long streamSignature = 0x4953FF0050543031L;	//IS..PT01


	// ----- Parser Tables -----


	// Symbols are represented by nonnegative integers ranging from 0 to one
	// less than the number of symbols.  Symbols may be terminal or
	// nonterminal.  There is no special significance to symbol 0, nor are
	// symbols assumed to be in any particular order.

	// Likewise, productions are identified by nonnegative integers ranging
	// from 0 to one less than the number of productions.  There is no
	// special significance to production 0, nor are productions assumed to
	// be in any particular order.


	// The number of symbols

	public int _symbolCount;


	// The number of productions

	public int _productionCount;


	// The following array of int gives the symbol that appears on the left
	// hand side of each production.

	public int[] _productionLHSSymbol;


	// The following array of int gives the length of the right hand side of
	// each production.  A value of 0 denotes an epsilon production, that is,
	// a production whose right hand side is epsilon.

	public int[] _productionRHSLength;


	// The following array of int gives the nonterminal factory parameter
	// associated with each production.

	public int[] _productionParam;


	// The following int is the maximum length of a continuation string that
	// will be considered during LM error repair.  This can be 0 to indicate
	// that LM continuation strings are not considered.

	public int _maxInsertion;


	// The following int is the maximum number of terminal symbols that can be
	// deleted during error repair.  This can be 0 to indicate that deletions
	// are not considered.

	public int _maxDeletion;


	// The following int is the number of terminal symbols that are considered
	// when validating an error repair.  This can be 0 to indicate that
	// validation is not performed.

	public int _validationLength;


	// The number of single point insertion symbols.

	public int _singlePointInsertionCount;


	// The following array contains a list of terminal symbols that are
	// considered for single-point-insertion error repairs.  This can have
	// length 0 to indicate that single-point-insertion is not considered.

	public int[] _singlePointInsertions;


	// The following int is the numerical value of the goal production.

	public int _goalProduction;


	// The following int is the numerical value of the end-of-file symbol.

	public int _eofSymbol;


	// The following array contains the insertion cost for each symbol.

	public int[] _insertionCost;


	// The following array contains the deletion cost for each symbol.

	public int[] _deletionCost;


	// The number of LR(1) states.

	public int _stateCount;


	// Parsing action table.
	//
	// For LR(1) state n and symbol s, _actionTable[n][s] encodes a parser
	// action as follows:  (i) If the action is to reduce production p, the 
	// value is p.  (ii) If the action is to accept, the value is g, where g is
	// the numerical value of the goal production.  (iii) If the action is to
	// shift and go to state m, the value is m+(PC*2), where PC is the total
	// number of productions in the grammar.  (iv) If the action is to shift
	// and then reduce production p, the value is p+PC.  In this case, it is
	// guaranteed that the rhs of production p is nonempty.  (v) If the action
	// is to signal error, the value is PC*2.
	//
	// Note that the parser never reduces the goal production or goes to state
	// 0, so the encodings for accept and error do not create ambiguity.
	//
	// In addition, _actionTable[n][_symbolCount] encodes a parser action used
	// for unwinding the stack, with the same encoding described above.  In
	// effect, there is a fictitious terminal symbol, with numerical value
	// _symbolCount, that is used for all error insertions generated by
	// _unwindingTable.
	//
	// Note that _actionTable[n].length equals _symbolCount+1.

	public short[][] _actionTable;



	// Unwinding action table.
	//
	// For LR(1) state n, _unwindingTable[n] encodes an unwinding action as
	// follows:  (i) If the unwinding action is to reduce production p, the
	// value is p.  (ii) If the unwinding action is to shift terminal symbol s,
	// the value is s+PC where PC is the total number of productions in the
	// grammar.  (iii) If the unwinding action is to accept, the value is e+PC
	// where e is the numerical value of the end-of-file terminal symbol.

	public int[] _unwindingTable;


	// ----- Dynamic Linking Tables -----


	// The following array of strings gives the names of the symbols.
	//
	// For symbol s, _symbols[s] is a String giving the name of the symbol.
	// Each symbol is required to have a unique name.  The name of a
	// nonterminal symbol determines which nonterminal factory is used to
	// process productions with that symbol on the left hand side.

	public String[] _symbols;


	// The following array of strings gives the link name associated with each
	// production.  If a production is declared without a link name, then the
	// link name is the empty string (not null).

	public String[] _productionLink;


	// ----- Name Lookup Tables -----


	// Hash table for finding symbol numbers.
	//
	// Each key in this table is a String object containing the name of a
	// symbol.  Each element is an Integer object whose value is the
	// corresponding symbol number.  This table is the inverse of the _symbols
	// array.

	private Hashtable _inverseSymbolNames;


	// Hash table for linking nonterminal factories.
	//
	// Each key in this table is a ParserTableFactoryKey object containing the
	// name of a nonterminal symbol and a production link name.  Each element
	// is a NonterminalFactory object whose value is the corresponding
	// nonterminal factory.

	private Hashtable _factoryLinkage;


	// The nonterminal factories.
	//
	// For production p, _nonterminalFactories[p] is the NonterminalFactory
	// object that is used to reduce that production.

	private NonterminalFactory[] _nonterminalFactories;


	// Tracing output.
	//
	// If tracing is enabled, this is an ErrorOutput object that is the
	// destination of trace output.  If tracing is disabled, this is null.

	private ErrorOutput _traceOut;




	// Constructor creates an empty ParserTable object.

	public ParserTable ()
	{
		super();

		// Initialize lookup tables

		_inverseSymbolNames = null;

		_factoryLinkage = null;

		_nonterminalFactories = null;

		_traceOut = null;

		return;
	}




	// Given the name of a symbol, this function returns the corresponding
	// symbol number.  If the name is invalid, this function returns -1.
	//
	// If the name lookup table has not been constructed yet, this function
	// constructs it.

	public int lookupSymbol (String name)
	{

		// If the name lookup table is not constructed ...

		if (_inverseSymbolNames == null)
		{

			// Create the hash table

			_inverseSymbolNames = new Hashtable (_symbolCount * 2);

			// For each symbol ...

			for (int i = 0; i < _symbolCount; ++i)
			{

				// Add the symbol to the hash table

				_inverseSymbolNames.put (_symbols[i], new Integer(i));
			}
		}

		// Get the hash table element for this name

		Integer element = (Integer) _inverseSymbolNames.get (name);

		// If name not found, return -1

		if (element == null)
		{
			return -1;
		}

		// Return the integer value of the element

		return element.intValue();
	}




	// This function links a nonterminal factory into the parser tables.
	//
	// The parameters are as follows:
	//
	// nonterminalName - The name of the nonterminal on the production's right
	//	hand side.
	//
	// linkName - The production's link name.  If the production does not have
	//	a link name, this should be an empty string (not null).
	//
	// factory - The nonterminal factory for the production.  Note that if
	//	there is more than one production with the same left hand side and
	//	link name, this factory will be used for all of them.
	//
	// If no factory is provided for a given production, then a default factory
	// is used.  If the production's right hand side is nonempty, the default
	// factory returns the value of the first symbol on the right hand side.
	// If the right hand side is empty, the default factory returns null.

	public void linkFactory (String nonterminalName, String linkName,
		NonterminalFactory factory)
	{

		// If we don't have a factory linkage table, create one

		if (_factoryLinkage == null)
		{
			_factoryLinkage = new Hashtable ();
		}

		// Add our factory to the linkage table

		_factoryLinkage.put (
			new ParserTableFactoryKey (nonterminalName, linkName), factory );

		return;
	}




	// This function sets the destination for tracing output.  If the parameter
	// is null, tracing is disabled.
	//
	// If this function is never called, then tracing is disabled.
	//
	// When tracing is enabled, the production's left hand side, link name,
	// parameter, and position are written out every time a production is
	// reduced.  This lets you view the operation of the parser.  Caution:
	// This produces a large amount of output!

	public void setTrace (ErrorOutput traceOut)
	{
		_traceOut = traceOut;

		return;
	}




	// This function returns the array of nonterminal factories.  Only Parser
	// should use this function.
	//
	// When this function is called for the first time, it constructs the array
	// by completing the factory linkage process and creating default
	// nonterminal factories.  Subsequent calls simply return the array
	// previously constructed.

	public NonterminalFactory[] getFactories ()
	{

		// If the factory array is not constructed yet ...

		if (_nonterminalFactories == null)
		{

			// If we don't have a factory linkage table, create one

			if (_factoryLinkage == null)
			{
				_factoryLinkage = new Hashtable ();
			}

			// Create the nonterminal factory array

			_nonterminalFactories = new NonterminalFactory[_productionCount];

			// Create instances of the default factories

			NonterminalFactory defFactoryNull = new ParserTableNFNull ();

			NonterminalFactory defFactoryCopy = new ParserTableNFCopy ();

			// For each production ...

			for (int p = 0; p < _productionCount; ++p)
			{

				// Get the nonterminal factory from the dictionary

				_nonterminalFactories[p] = (NonterminalFactory) _factoryLinkage.get (
					new ParserTableFactoryKey (
						_symbols[_productionLHSSymbol[p]], _productionLink[p] ) );

				// If we didn't get one, use the default factory

				if (_nonterminalFactories[p] == null)
				{
					_nonterminalFactories[p] = 
						(_productionRHSLength[p] == 0) ? defFactoryNull: defFactoryCopy;
				}

				// If tracing is enabled ...

				if (_traceOut != null)
				{

					// Install a debugging nonterminal factory

					_nonterminalFactories[p] = new ParserTableNFDebug (
						_nonterminalFactories[p],
						_symbols[_productionLHSSymbol[p]]
							+ " {" + _productionLink[p] + "}"
							+ " #" + _productionParam[p],
						_traceOut );
				}
			}
		}

		// Return the nonterminal factory array

		return _nonterminalFactories;
	}


	

	// Write the parser table to a data stream.

	public void writeToStream (DataOutput stream) throws IOException
	{

		// Write the table signature

		stream.writeLong (streamSignature);

		// Write the number of symbols

		stream.writeInt (_symbolCount);

		// Write the number of productions

		stream.writeInt (_productionCount);

		// Write the maximum continuation length

		stream.writeInt (_maxInsertion);

		// Write the maximum error repair deletion

		stream.writeInt (_maxDeletion);

		// Write the validation length

		stream.writeInt (_validationLength);

		// Write the number of single point insertions

		stream.writeInt (_singlePointInsertionCount);

		// Write the goal production

		stream.writeInt (_goalProduction);

		// Write the end-of-file symbol

		stream.writeInt (_eofSymbol);

		// Write the number of LR(1) states

		stream.writeInt (_stateCount);

		// Write the production left hand side symbol numbers

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _productionLHSSymbol);

		// Write the production right hand side lengths

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _productionRHSLength);

		// Write the production parameters

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _productionParam);

		// Write the single point insertion list

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _singlePointInsertions);

		// Write the insertion costs

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _insertionCost);

		// Write the deletion costs

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _deletionCost);

		// Write the parse table

		ArrayIO.writeShort2D (stream, ArrayIO.formatShortRLE, _actionTable);

		// Write the unwinding table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _unwindingTable);

		// Write the symbol names

		ArrayIO.writeString1D (stream, ArrayIO.formatStringUTF, _symbols);

		// Write the production link names

		ArrayIO.writeString1D (stream, ArrayIO.formatStringUTF, _productionLink);

		// Done writing stream

		return;
	}



	
	// Read the parser table from a data stream.

	public void readFromStream (DataInput stream) throws IOException
	{

		// Read the parser table signature

		long inputSignature = stream.readLong ();

		if (inputSignature != streamSignature)
		{
			throw new IODataFormatException (
				"ParserTable.readFromStream: Invalid signature");
		}

		// Read the number of symbols

		_symbolCount = stream.readInt ();

		// Read the number of productions

		_productionCount = stream.readInt ();

		// Read the maximum continuation length

		_maxInsertion = stream.readInt ();

		// Read the maximum error repair deletion length

		_maxDeletion = stream.readInt ();

		// Read the validation length

		_validationLength = stream.readInt ();

		// Read the number of single point insertions

		_singlePointInsertionCount = stream.readInt ();

		// Read the goal production

		_goalProduction = stream.readInt ();

		// Read the end-of-file symbol

		_eofSymbol = stream.readInt ();

		// Read the number of LR(1) states

		_stateCount = stream.readInt ();

		// Read the production left hand side symbol numbers

		_productionLHSSymbol = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the production right hand side symbol lengths

		_productionRHSLength = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the production parameters

		_productionParam = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the list of single point insertions

		_singlePointInsertions = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the insertion costs

		_insertionCost = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the deletion costs

		_deletionCost = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the parse table

		_actionTable = ArrayIO.readShort2D (stream, ArrayIO.formatShortRLE);

		// Read the unwinding action table

		_unwindingTable = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the names of the symbols

		_symbols = ArrayIO.readString1D (stream, ArrayIO.formatStringUTF);

		// Read the production link names

		_productionLink = ArrayIO.readString1D (stream, ArrayIO.formatStringUTF);

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

		// Check number of symbols

		if (_symbolCount <= 0)
		{
			return "Invalid symbol count";
		}

		// Check number of productions

		if (_productionCount <= 0)
		{
			return "Invalid production count";
		}

		// Check maximum insertions

		if (_maxInsertion < 0)
		{
			return "Invalid maximum insertion length";
		}

		// Check maximum deletions

		if (_maxDeletion < 0)
		{
			return "Invalid maximum deletion length";
		}

		// Check validation length

		if (_validationLength < 0)
		{
			return "Invalid validation length";
		}

		// Check single point insertion count

		if (_singlePointInsertionCount < 0)
		{
			return "Invalid single point insertion count";
		}

		// Check the goal production

		if ((_goalProduction < 0) || (_goalProduction >= _productionCount))
		{
			return "Invalid goal production";
		}

		// Check the end-of-file symbol

		if ((_eofSymbol < 0) || (_eofSymbol >= _symbolCount))
		{
			return "Invalid end-of-file symbol";
		}

		// Check the number of LR(1) states

		if (_stateCount <= 0)
		{
			return "Invalid LR(1) state count";
		}

		// Check the production left hand side symbol numbers

		if (_productionLHSSymbol == null)
		{
			return "Missing production LHS symbol array";
		}

		if (_productionLHSSymbol.length != _productionCount)
		{
			return "Invalid production LHS symbol array length";
		}

		for (int i = 0; i < _productionCount; ++i)
		{
			if ((_productionLHSSymbol[i] < 0)
				|| (_productionLHSSymbol[i] >= _symbolCount))
			{
				return "Invalid production LHS symbol value";
			}
		}

		// Check the production right hand side symbol lengths

		if (_productionRHSLength == null)
		{
			return "Missing production RHS length array";
		}

		if (_productionRHSLength.length != _productionCount)
		{
			return "Invalid production RHS length array length";
		}

		for (int i = 0; i < _productionCount; ++i)
		{
			if (_productionRHSLength[i] < 0)
			{
				return "Invalid production RHS length";
			}
		}

		// Check the production parameters

		if (_productionParam == null)
		{
			return "Missing production parameter array";
		}

		if (_productionParam.length != _productionCount)
		{
			return "Invalid production parameter array length";
		}

		for (int i = 0; i < _productionCount; ++i)
		{
			if (_productionParam[i] < 0)
			{
				return "Invalid production parameter";
			}
		}

		// Check the list of single point insertions

		if (_singlePointInsertions == null)
		{
			return "Missing single point insertion array";
		}

		if (_singlePointInsertions.length != _singlePointInsertionCount)
		{
			return "Invalid single point insertion array length";
		}

		for (int i = 0; i < _singlePointInsertionCount; ++i)
		{
			if ((_singlePointInsertions[i] < 0)
				|| (_singlePointInsertions[i] >= _symbolCount))
			{
				return "Invalid single point insertion symbol value";
			}
		}

		// Check the insertion costs

		if (_insertionCost == null)
		{
			return "Missing insertion cost array";
		}

		if (_insertionCost.length != _symbolCount)
		{
			return "Invalid insertion cost array length";
		}

		for (int i = 0; i < _symbolCount; ++i)
		{
			if (_insertionCost[i] < 0)
			{
				return "Invalid insertion cost";
			}
		}

		// Check the deletion costs

		if (_deletionCost == null)
		{
			return "Missing deletion cost array";
		}

		if (_deletionCost.length != _symbolCount)
		{
			return "Invalid deletion cost array length";
		}

		for (int i = 0; i < _symbolCount; ++i)
		{
			if (_deletionCost[i] < 0)
			{
				return "Invalid deletion cost";
			}
		}

		// Check the parse action table

		if (_actionTable == null)
		{
			return "Missing parse action table";
		}

		if (_actionTable.length != _stateCount)
		{
			return "Invalid parse action table length";
		}

		for (int i = 0; i < _stateCount; ++i)
		{
			if (_actionTable[i] == null)
			{
				return "Missing parse action table subarray";
			}

			if (_actionTable[i].length != _symbolCount + 1)
			{
				return "Invalid parse action table subarray length";
			}

			for (int j = 0; j < _symbolCount + 1; ++j)
			{
				if ((_actionTable[i][j] < 0)
					|| (_actionTable[i][j] >= (_productionCount * 2) + _stateCount))
				{
					return "Invalid parse action table entry";
				}
			}
		}

		// Check the unwinding action table

		if (_unwindingTable == null)
		{
			return "Missing unwinding action table";
		}

		if (_unwindingTable.length != _stateCount)
		{
			return "Invalid unwinding action table length";
		}

		for (int i = 0; i < _stateCount; ++i)
		{
			if ((_unwindingTable[i] < 0)
				|| (_unwindingTable[i] >= _productionCount + _symbolCount))
			{
				return "Invalid unwinding action table entry";
			}
		}

		// Check the names of symbols

		if (_symbols == null)
		{
			return "Missing symbol name array";
		}

		if (_symbols.length != _symbolCount)
		{
			return "Invalid symbol name array length";
		}

		for (int i = 0; i < _symbolCount; ++i)
		{
			if (_symbols[i] == null)
			{
				return "Missing symbol name";
			}
		}

		// Check the production link names

		if (_productionLink == null)
		{
			return "Missing production link name array";
		}

		if (_productionLink.length != _productionCount)
		{
			return "Invalid production link name array length";
		}

		for (int i = 0; i < _productionCount; ++i)
		{
			if (_productionLink[i] == null)
			{
				return "Missing production link name";
			}
		}

		// Check the factory linkage table

		if (_factoryLinkage != null)
		{
			for (Enumeration e = _factoryLinkage.keys(); e.hasMoreElements(); )
			{
				ParserTableFactoryKey key = (ParserTableFactoryKey) e.nextElement();

				checkKey:
				{
					for (int p = 0; p < _productionCount; ++p)
					{
						if (key._nonterminalName.equals (_symbols[_productionLHSSymbol[p]])
							&& key._linkName.equals (_productionLink[p]))
						{
							break checkKey;
						}
					}

					return "Unknown nonterminal factory '"
						+ key._nonterminalName + "{" + key._linkName + "}'";
				}
			}
		}

		// Success

		return null;
	}




	// This function writes a Java source file for a subclass of ParserTable.
	// The subclass contains all the generated parser tables, written as
	// literal constants.
	//
	// The constructor for the subclass automatically sets all the parser
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

		out.line ("import invisible.jacc.parse.ParserTable;");
		if (useRLE)
		{
			out.line ("import invisible.jacc.util.ArrayRLE;");
		}
		out.line ();

		// Write the class statement

		out.line ("public class " + className + " extends ParserTable");
		out.openScope ();
		out.line ();

		// Write the tables

		out.line ("// The number of symbols.");
		out.line ();
		out.text ("private static final int gen_symbolCount = ");
		out.literal (_symbolCount);
		out.line (";");
		out.line ();

		out.line ("// The number of productions.");
		out.line ();
		out.text ("private static final int gen_productionCount = ");
		out.literal (_productionCount);
		out.line (";");
		out.line ();

		out.line ("// The symbol on the left hand side of each production.");
		out.line ();
		out.line ("private static final int[] gen_productionLHSSymbol = ");
		out.literal (_productionLHSSymbol);
		out.line (";");
		out.line ();

		out.line ("// The length of the right hand side of each production.");
		out.line ();
		out.line ("private static final int[] gen_productionRHSLength = ");
		out.literal (_productionRHSLength);
		out.line (";");
		out.line ();

		out.line ("// The parameter for each production.");
		out.line ();
		out.line ("private static final int[] gen_productionParam = ");
		out.literal (_productionParam);
		out.line (";");
		out.line ();

		out.line ("// The maximum number of insertions during error repair.");
		out.line ();
		out.text ("private static final int gen_maxInsertion = ");
		out.literal (_maxInsertion);
		out.line (";");
		out.line ();

		out.line ("// The maximum number of deletions during error repair.");
		out.line ();
		out.text ("private static final int gen_maxDeletion = ");
		out.literal (_maxDeletion);
		out.line (";");
		out.line ();

		out.line ("// The validation length for error repair.");
		out.line ();
		out.text ("private static final int gen_validationLength = ");
		out.literal (_validationLength);
		out.line (";");
		out.line ();

		out.line ("// The number of single-point insertions for error repair.");
		out.line ();
		out.text ("private static final int gen_singlePointInsertionCount = ");
		out.literal (_singlePointInsertionCount);
		out.line (";");
		out.line ();

		out.line ("// The list of symbols for single-point insertions.");
		out.line ();
		out.line ("private static final int[] gen_singlePointInsertions = ");
		out.literal (_singlePointInsertions);
		out.line (";");
		out.line ();

		out.line ("// The goal production.");
		out.line ();
		out.text ("private static final int gen_goalProduction = ");
		out.literal (_goalProduction);
		out.line (";");
		out.line ();

		out.line ("// The end-of-file symbol.");
		out.line ();
		out.text ("private static final int gen_eofSymbol = ");
		out.literal (_eofSymbol);
		out.line (";");
		out.line ();

		out.line ("// Insertion cost of each symbol for error repair.");
		out.line ();
		out.line ("private static final int[] gen_insertionCost = ");
		out.literal (_insertionCost);
		out.line (";");
		out.line ();

		out.line ("// Deletion cost of each symbol for error repair.");
		out.line ();
		out.line ("private static final int[] gen_deletionCost = ");
		out.literal (_deletionCost);
		out.line (";");
		out.line ();

		out.line ("// The number of LR(1) states.");
		out.line ();
		out.text ("private static final int gen_stateCount = ");
		out.literal (_stateCount);
		out.line (";");
		out.line ();

		out.line ("// Parsing action table.");
		out.line ();
		if (useRLE)
		{
			out.line ("private static short[][] gen_actionTable = null;");
			out.line ("private static short[] rle_actionTable = ");
			out.literal (ArrayRLE.short2DToShortRLE (_actionTable));
		}
		else
		{
			out.line ("private static final short[][] gen_actionTable = ");
			out.literal (_actionTable);
		}
		out.line (";");
		out.line ();

		out.line ("// Unwinding action table for error repair.");
		out.line ();
		out.line ("private static final int[] gen_unwindingTable = ");
		out.literal (_unwindingTable);
		out.line (";");
		out.line ();

		out.line ("// The names of symbols.");
		out.line ();
		out.line ("private static final String[] gen_symbols = ");
		out.literal (_symbols);
		out.line (";");
		out.line ();

		out.line ("// The link name for each production.");
		out.line ();
		out.line ("private static final String[] gen_productionLink = ");
		out.literal (_productionLink);
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

			out.line ("if (gen_actionTable == null)");
			out.openScope ();
			out.line ("gen_actionTable = ArrayRLE.shortRLEToShort2D (rle_actionTable);");
			out.line ("rle_actionTable = null;");
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

		out.line ("// Constructor installs the generated tables into the ParserTable");
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

		out.line ("// Copy parsing tables into the ParserTable superclass");
		out.line ();
		out.line ("_symbolCount = gen_symbolCount;");
		out.line ("_productionCount = gen_productionCount;");
		out.line ("_productionLHSSymbol = gen_productionLHSSymbol;");
		out.line ("_productionRHSLength = gen_productionRHSLength;");
		out.line ("_productionParam = gen_productionParam;");
		out.line ("_maxInsertion = gen_maxInsertion;");
		out.line ("_maxDeletion = gen_maxDeletion;");
		out.line ("_validationLength = gen_validationLength;");
		out.line ("_singlePointInsertionCount = gen_singlePointInsertionCount;");
		out.line ("_singlePointInsertions = gen_singlePointInsertions;");
		out.line ("_goalProduction = gen_goalProduction;");
		out.line ("_eofSymbol = gen_eofSymbol;");
		out.line ("_insertionCost = gen_insertionCost;");
		out.line ("_deletionCost = gen_deletionCost;");
		out.line ("_stateCount = gen_stateCount;");
		out.line ("_actionTable = gen_actionTable;");
		out.line ("_unwindingTable = gen_unwindingTable;");
		out.line ();

		out.line ("// Copy dynamic-link tables into the ParserTable superclass");
		out.line ();
		out.line ("_symbols = gen_symbols;");
		out.line ("_productionLink = gen_productionLink;");
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

			ParserTable cloneTable = (ParserTable) super.clone();

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




// Key for looking up nonterminal factories.

final class ParserTableFactoryKey
{

	// Name of the nonterminal symbol.

	String _nonterminalName;

	// Production link name.

	String _linkName;


	// Constructor just saves its parameters.

	ParserTableFactoryKey (String nonterminalName, String linkName)
	{
		_nonterminalName = nonterminalName;
		_linkName = linkName;

		return;
	}


	// Two keys are equal if both strings are equal.

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof ParserTableFactoryKey)))
		{
			return false;
		}

		ParserTableFactoryKey other = (ParserTableFactoryKey) obj;

		return this._nonterminalName.equals (other._nonterminalName)
			&& this._linkName.equals (other._linkName);
	}


	// Hash code is the exclusive-or of the hash codes of the two strings.

	public int hashCode ()
	{
		return _nonterminalName.hashCode() ^ _linkName.hashCode();
	}


}




// A nonterminal factory that always returns null.  This is used as the
// default nonterminal factory for any production whose right hand side is
// empty.

final class ParserTableNFNull extends NonterminalFactory
{

	// Make a nonterminal object.
	//
	// Implements the makeNonterminal() method of NonterminalFactory.

	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{
		return null;
	}


	// The constructor does nothing.

	public ParserTableNFNull ()
	{
		super ();

		return;
	}


}




// A nonterminal factory that copies the first symbol on the right hand side.
// This is used as the default nonterminal factory for any production whose
// right hand side is nonempty.

final class ParserTableNFCopy extends NonterminalFactory
{

	// Make a nonterminal object.
	//
	// Implements the makeNonterminal() method of NonterminalFactory.

	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{
		return parser.rhsValue (0);
	}


	// The constructor does nothing.

	public ParserTableNFCopy ()
	{
		super ();

		return;
	}


}




/*->

  ParserTableNFDebug is a debugging nonterminal factory.  It contains
  a "real" nonterminal factory, a production name, and an ErrorOutput object.

  The makeNonterminal() routine writes the production name and position to the
  ErrorOutput object.  Then it calls the "real" nonterminal factory.  Therefore,
  a complete record of all reductions is written out.

  This is useful for debugging a grammar specification, to ensure that productions
  are being recognized as intended.  Caution:  The amount of output generated
  can be very large.

->*/


final class ParserTableNFDebug extends NonterminalFactory
{

	// The contained nonterminal factory

	private NonterminalFactory _factory;

	// The name we use

	private String _productionName;

	// The destination for our output

	private ErrorOutput _errOut;


	// Make a nonterminal object.
	//
	// Implements the makeNonterminal() method of NonterminalFactory.

	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get the parser's current token

		Token token = parser.token();

		// Write the production name to the error output
		
		_errOut.reportError (ErrorOutput.typeInformational, null,
			token.file, token.line, token.column, null,
			_productionName );

		// Call the contained nonterminal factory

		return _factory.makeNonterminal (parser, param);
	}


	// The constructor saves its arguments.

	public ParserTableNFDebug (NonterminalFactory factory, String productionName,
		ErrorOutput errOut)
	{
		super ();

		_factory = factory;
		_productionName = productionName;
		_errOut = errOut;

		return;
	}


}


