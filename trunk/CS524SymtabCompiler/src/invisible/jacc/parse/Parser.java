// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  Parser is an LR(1) parser with LM error repair.


  CREATION

  When a Parser object is created, it is passed the following:

	An object of type ParserClient.  This provides callback functions that the
	parser can use to inform the client of significant events.  These include
	parsing errors (whether repaired or not), I/O exceptions, and syntax
	exceptions.

	An object of type ParserTable.  This contains all the tables required for
	the LR(1) parsing algorithm and the LM error repair algorithm.  In addition,
	it contains pointers to the nonterminal factories that are called during
	the parse.

	An Object which is used to initialize the public variable clientParams.
	This can be null.

  After creating a Parser object, the client should not alter the ParserTable
  object that was used to create it.

  The clientParams variable may be used by the client and the nonterminal
  factories for any desired purpose.  It is provided as a convenient place for
  the client to store per-parser tables that need to be accessed during the
  parse.


  INVOKING THE PARSER

  Once the Parser object is created, it is invoked by calling the parse() entry
  point.  The parse() entry point is passed the following:

	An object of type Preprocessor.  This provides the stream of tokens that the
	parser is to parse.

  The parse() function automatically reads all the tokens from the source.  It
  returns when one of the following occurs:

	(1) The end-of-file token is shifted onto the parse stack.  In this case,
	parse() returns false.

	(2) An unrepairable parser error occurs.  In this case, parse() calls
	ParserClient.parserErrorFail() and then returns true.

	(3) An I/O exception occurs.  In this case, parse() calls
	ParserClient.parserIOException() and then returns true.

	(4) A syntax I/O exception occurs.  In this case, parse() calls
	ParserClient.parserSyntaxException() and then returns true.

  Note that parse() returns true if any error occurs that prevents the entire
  input from being parsed.  On the other hand, if errors occur but are repaired,
  parse() returns false.  Also, note that parse() catches all checked exceptions,
  so the client doesn't have to do it.

  After parse() returns, you can call parse() again with another token source,
  to parse another source using the same parser tables.  It is not necessary to
  create a new Parser object for each source.


  PARSER OPERATION

  The parser operates using the LR(1) parsing algorithm.  A description of the
  algorithm can be found in "Crafting a Compiler" by Fischer and LeBlanc.

  The parser maintains a value stack that runs in parallel to the parser stack.
  Each entry in the value stack can store an object of type Object, or null.

  When a terminal symbol is pushed onto the parser stack, the corresponding
  token value is saved on the value stack.  (If the input source is coming from
  a Scanner object, then the token value is the value supplied by the token
  factory.)

  When a production is reduced, the parser calls the nonterminal factory for
  the symbol on the production's left hand side.  The parser passes the integer
  parameter associated with the production.  There is one entry on the value
  stack for each symbol on the production's right hand side;  the nonterminal
  factory can call rhsValue() to retrieve these values.  The nonterminal
  factory can also call token() to get the current source position.  Refer to
  the class NonterminalFactory for additional documentation.

  After the nonterminal factory returns, the values of the right hand side are
  popped off the value stack, and the value returned by the nonterminal factory
  is pushed onto the value stack.  Thus, the nonterminal factory returns the
  value of the production's left hand side.

  The last action performed by the parser is the reduction of a production
  whose left hand side is the client's goal symbol.


  ERROR REPAIR

  A parsing error occurs when the parser encounters an input symbol that cannot
  be shifted onto the parser stack.  When this happens, the parser uses the LM
  error repair algorithm to attempt to repair the error and continue parsing,
  provided that error repair is enabled in the ParserTable object.  A
  description of the algorithm can be found in "Crafting a Compiler" by Fischer
  and LeBlanc.  (We have made two alterations in the algorithm, one to ensure
  that it cannot enter an infinite loop, and one to make it work with ambiguous
  grammars.)

  An error repair consists of deleting zero or more terminal symbols from the
  front of the input, and then inserting zero or more terminal symbols onto the
  front of the input.  All repairs are validated;  that is, the parser checks
  to make sure that after the repair is done, several additional input symbols
  can be shifted.
  
  When an error is repaired, the parser calls ParserClient.parserErrorRepair()
  to inform the client.

  When a terminal symbol is inserted during error repair, the value is set to
  null, and the position information is set to the same position as the symbol
  that originally caused the error.  Therefore, if error repair is enabled,
  then every nonterminal factory must be prepared to accept a null value for
  any terminal symbol on the right hand side.

  There is one special consideration when LM error repair is used with an
  ambiguous grammar.  When the inserted terminal symbols are processed, the
  resulting reductions always respect the grammar, but may not respect the
  precedence rules that were used to resolve parsing conflicts.  It appears
  that this rarely occurs in practice.  Nonetheless, nonterminal factories
  should be designed so that they at least don't crash if productions are
  reduced differently than specified in the precedence rules.  (In practice,
  this is unlikely to require any special code in the nonterminal factories.)
 

->*/


public class Parser 
{


	// ----- Parser Tables -----


	// Symbols are represented by nonnegative integers ranging from 0 to one
	// less than the number of symbols.  Symbols may be terminal or
	// nonterminal.  There is no special significance to symbol 0, nor are
	// symbols assumed to be in any particular order.

	// Likewise, productions are identified by nonnegative integers ranging
	// from 0 to one less than the number of productions.  There is no
	// special significance to production 0, nor are productions assumed to
	// be in any particular order.


	// The number of symbols.

	private int _symbolCount;


	// The number of productions.

	private int _productionCount;


	// The following array of int gives the symbol that appears on the left
	// hand side of each production.

	private int[] _productionLHSSymbol;


	// The following array of int gives the length of the right hand side of
	// each production.  A value of 0 denotes an epsilon production, that is,
	// a production whose right hand side is epsilon.

	private int[] _productionRHSLength;


	// The following array of int gives the nonterminal factory parameter
	// associated with each production.

	private int[] _productionParam;


	// The following int is the maximum length of a continuation string that
	// will be considered during LM error repair.  This can be 0 to indicate
	// that LM continuation strings are not considered.

	private int _maxInsertion;


	// The following int is the maximum number of terminal symbols that can be
	// deleted during error repair.  This can be 0 to indicate that deletions
	// are not considered.

	private int _maxDeletion;


	// The following int is the number of terminal symbols that are considered
	// when validating an error repair.  This can be 0 to indicate that
	// validation is not performed.

	private int _validationLength;


	// The number of single point insertion symbols.

	private int _singlePointInsertionCount;


	// The following array contains a list of terminal symbols that are
	// considered for single-point-insertion error repairs.  This can have
	// length 0 to indicate that single-point-insertion is not considered.

	private int[] _singlePointInsertions;


	// The following int is the numerical value of the goal production.

	private int _goalProduction;


	// The following int is the numerical value of the end-of-file symbol.

	private int _eofSymbol;


	// The following array contains the insertion cost for each symbol.

	private int[] _insertionCost;


	// The following array contains the deletion cost for each symbol.

	private int[] _deletionCost;


	// The number of LR(1) states

	private int _stateCount;


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

	private short[][] _actionTable;


	// Unwinding action table.
	//
	// For LR(1) state n, _unwindingTable[n] encodes an unwinding action as
	// follows:  (i) If the unwinding action is to reduce production p, the
	// value is p.  (ii) If the unwinding action is to shift terminal symbol s,
	// the value is s+PC where PC is the total number of productions in the
	// grammar.  (iii) If the unwinding action is to accept, the value is e+PC
	// where e is the numerical value of the end-of-file terminal symbol.

	private int[] _unwindingTable;


	// ----- Dynamic Linking Table -----


	// The nonterminal factories.
	//
	// For production p, _nonterminalFactories[p] is the NonterminalFactory
	// object that is used to reduce that production.

	public NonterminalFactory[] _nonterminalFactories;


	// ----- Derived Information -----


	// Action table error value

	private int _productionCountTimesTwo;


	// ----- Parser state variables -----


	// The last token received from the source.

	private Token _token;


	// The parse stack.  Since the parser begins in state 0, _parseStack[0]
	// always contains 0.

	private int[] _parseStack;


	// The value stack.

	private Object[] _valueStack;


	// The stack top pointer.  Since the parser begins by pushing state 0 onto
	// the stack, the initial value of _stackTop is 0.

	private int _stackTop;


	// The initial size and incremental size of the parse and value stacks.
	// The stacks are increased in size automatically whenever needed.

	private static final int _initialStackSize = 200;

	private static final int _incrementalStackSize = 100;


	// The current size of the parse and value stacks.

	private int _currentStackSize;


	// The preprocessor we are using as our input source.

	private Preprocessor _source;


	// The parser table object.
	//
	// A nonterminal factory may call parserTable() to retrieve the object.

	private ParserTable _parserTable;

	public final ParserTable parserTable ()
	{
		return _parserTable;
	}


	// The parser's client.
	//
	// A nonterminal factory may call client() to retrieve the client.

	private ParserClient _client;

	public final ParserClient client ()
	{
		return _client;
	}


	// The client parameters.
	//
	// Client code and nonterminal factories may use this public variable for
	// any desired purpose.

	public Object clientParams;


	// ----- Error Repair Variables -----


	// Insertion working buffer.
	//
	// During error repair, this array is used to construct a list of terminal
	// symbols to insert.  The length of the array is _maxInsertion, except
	// that if _maxInsertion is zero and _singlePointInsertionCount is nonzero
	// then the length of the array is 1.

	private int[] _insertions;


	// Deletion working buffer.
	//
	// During error repair, this array is used to construct a list of terminal
	// symbols to delete.  The length of the array is _maxDeletion.

	private int[] _deletions;


	// The number of insertions in the current error repair.

	private int _insertionLength;


	// The number of deletions in the current error repair.

	private int _deletionLength;


	// Error token.
	//
	// During error repair, this holds a copy of the token that originally
	// caused the error.

	private Token _errorToken;


	// The error parse stack.

	private int[] _errorParseStack;


	// The error parse stack top pointer.

	private int _errorStackTop;


	// The current size of the error parse stack.

	private int _currentErrorStackSize;


	// The maximum allowed repair cost, plus 1.

	private static final int repairCostCeiling = 0x7FFFFFFF;


	// The cost of the current insertion

	private int _currentInsertionCost;




	// Read a value from the right hand side of a production.
	//
	// A nonterminal factory may call this function to retrieve a value from
	// the value stack.
	//
	// Assume that a nonterminal factory is being called to reduce the
	// production Y -> X1 ... Xn.  Then, rhsValue(0) thru rhsValue(n-1) return
	// the values of X1 thru Xn respectively.
	//
	// Negative offsets are permitted to access values of the right hand sides
	// of containing productions that are not yet reduced.  Doing this requires
	// detailed knowledge of the properties of the grammar, and is not
	// recommended.

	public final Object rhsValue (int offset)
	{
		return _valueStack[_stackTop + offset];
	}




	// Obtain the most recently processed token.
	//
	// A nonterminal factory may call this function to get location and
	// position information.  This is typically used for error reporting.
	//
	// The nonterminal factory may not modify the returned Token object.  The
	// contents of the Token object remain valid only until makeNonterminal()
	// returns (after that, the fields in the Token object may be overwritten).

	public final Token token ()
	{
		return _token;
	}




	// Obtain the name of a symbol, given its number.
	//
	// This function may be called by a nonterminal factory or by a client.
	// This is typically used for error reporting.

	public final String symbolName (int symbolNumber)
	{
		return _parserTable._symbols[symbolNumber];
	}




	// Constructor for Parser.  Refer to the documentation at the top of the
	// file for information on parameters.
	//
	// The function of the constructor is to copy information from the parser
	// table into our instance variables.  Additional data that can be
	// computed from the parser table alone is computed here.

	public Parser (ParserClient client, ParserTable parserTable, Object params)
	{
		super ();

		// Save the client

		if (client == null)
		{
			throw new NullPointerException ("Parser.Parser");
		}

		_client = client;

		// Save the parameters

		clientParams = params;

		// Save the parser table

		if (parserTable == null)
		{
			throw new NullPointerException ("Parser.Parser");
		}

		_parserTable = parserTable;

		// Copy tables from the ParserTable object

		_symbolCount = parserTable._symbolCount;
		_productionCount = parserTable._productionCount;
		_productionLHSSymbol = parserTable._productionLHSSymbol;
		_productionRHSLength = parserTable._productionRHSLength;
		_productionParam = parserTable._productionParam;
		_maxInsertion = parserTable._maxInsertion;
		_maxDeletion = parserTable._maxDeletion;
		_validationLength = parserTable._validationLength;
		_singlePointInsertionCount = parserTable._singlePointInsertionCount;
		_singlePointInsertions = parserTable._singlePointInsertions;
		_goalProduction = parserTable._goalProduction;
		_eofSymbol = parserTable._eofSymbol;
		_insertionCost = parserTable._insertionCost;
		_deletionCost = parserTable._deletionCost;
		_stateCount = parserTable._stateCount;
		_actionTable = parserTable._actionTable;
		_unwindingTable = parserTable._unwindingTable;

		_nonterminalFactories = parserTable.getFactories();

		// Get the action table error value

		_productionCountTimesTwo = _productionCount * 2;

		// Allocate the initial parse and value stacks

		_parseStack = new int[_initialStackSize];

		_valueStack = new Object[_initialStackSize];

		_currentStackSize = _initialStackSize;

		// Allocate the error repair working buffers

		_insertions = new int[
			Math.max (_maxInsertion, Math.min (1, _singlePointInsertionCount)) ];

		_deletions = new int[_maxDeletion];

		_errorToken = null;

		_errorParseStack = null;

		_currentErrorStackSize = 0;

		// Done with creation

		return;
	}




	// This private function enlarges the parse and value stacks

	private void enlargeParseStack ()
	{

		// Allocate new stacks

		int[] newParseStack = new int[_currentStackSize + _incrementalStackSize];

		Object[] newValueStack = new Object[_currentStackSize + _incrementalStackSize];

		// Copy existing stack contents into the new stacks

		System.arraycopy (_parseStack, 0, newParseStack, 0, _currentStackSize);

		System.arraycopy (_valueStack, 0, newValueStack, 0, _currentStackSize);

		// Adopt the new stacks

		_parseStack = newParseStack;

		_valueStack = newValueStack;

		_currentStackSize += _incrementalStackSize;

		return;
	}





	// Parses the input.

	public boolean parse (Preprocessor source)
	{

		// This flag is set if the function aborts due to exception or
		// unrepaired error.

		boolean abort = false;

		// Validate the argument

		if (source == null)
		{
			throw new NullPointerException ("Parser.parse");
		}

		// Save the source

		_source = source;

		// Enclosing try block (no indentation)

		try
		{

		// Initialize the parse stack by pushing state 0

		_stackTop = 0;

		_parseStack[_stackTop] = 0;

		_valueStack[_stackTop] = null;

		// Get the first terminal

		_token = _source.nextToken();

		// Loop until exception or break

	ParseLoop:
		for ( ; ; )
		{

			// Save the current stack top

			int oldStackTop = _stackTop;

			// Get the action from the parse table, and advance the stack
			// top in preparation for the next shift

			int action = _actionTable[_parseStack[_stackTop++]][_token.number];

			// Enlarge the stack if necessary

			if (_stackTop == _currentStackSize)
			{
				enlargeParseStack ();
			}

			// If the action is reduce or accept ...

			if (action < _productionCount)
			{

				// If the action is accept, break out of the parse loop

				if (action == _goalProduction)
				{
					break ParseLoop;
				}

				// The action is the number of the production to reduce

				// Adjust stack top to start of production's right hand side

				_stackTop -= _productionRHSLength[action];

				// Call the corresponding nonterminal factory, and push the
				// nonterminal's value onto the value stack

				_valueStack[_stackTop] = _nonterminalFactories[action].makeNonterminal (
					this, _productionParam[action] );

				// Get the action for the nonterminal on the production's left hand side.
				// By construction of the action table, this is either a shift or a
				// shift-and-reduce.

				action =
					_actionTable[_parseStack[_stackTop-1]][_productionLHSSymbol[action]];

				// While the action is shift-and-reduce ...

				while (action < _productionCountTimesTwo)
				{

					// The action is the number of the production to reduce, plus
					// _productionCount

					// Get the number of the production to reduce

					action -= _productionCount;

					// Adjust stack top to start of production's right hand side.  By
					// construction of the action table, the rhs length is at least 1.

					_stackTop -= (_productionRHSLength[action] - 1);

					// Call the corresponding nonterminal factory, and push the
					// nonterminal's value onto the value stack

					_valueStack[_stackTop] = _nonterminalFactories[action].makeNonterminal (
						this, _productionParam[action] );

					// Get the action for the nonterminal on the production's left hand side.
					// By construction of the action table, this is either a shift or a
					// shift-and-reduce.

					action =
						_actionTable[_parseStack[_stackTop-1]][_productionLHSSymbol[action]];
				}

				// The action is the target state number, plus _productionCountTimesTwo

				// Push the state that results from shifting the nonterminal on the
				// production's left hand side

				_parseStack[_stackTop] = action - _productionCountTimesTwo;
			}

			// Otherwise, the action is shift, shift-and-reduce, or error ...

			else
			{

				// If the action is error ...

				if (action == _productionCountTimesTwo)
				{

					// Save a copy of the error token

					_errorToken = new Token (_token);

					// Push back the current token

					_source.pushBackToken (_token);

					// Restore the stack top pointer

					--_stackTop;

					// Invoke the error repair handler

					if (errorRepair ())
					{

						// Discard the copy of the error token

						_token = _errorToken;

						_errorToken = null;

						// Error repaired, inform the client

						_client.parserErrorRepair (this, _token,
							_insertions, _insertionLength, _deletions, _deletionLength );
					}

					// Otherwise, unable to repair error ...

					else
					{

						// Discard the copy of the error token

						_token = _errorToken;

						_errorToken = null;

						// Set the abort flag

						abort = true;

						// Error repair failed, inform the client

						_client.parserErrorFail (this, _token);

						// Break out of the parse loop

						break ParseLoop;
					}
				}

				// Otherwise, the action is shift or shift-and-reduce ...

				else
				{

					// Push the terminal's value onto the value stack

					_valueStack[_stackTop] = _token.value;

					// While the action is shift-and-reduce ...

					while (action < _productionCountTimesTwo)
					{

						// The action is the number of the production to reduce, plus
						// _productionCount

						// Get the number of the production to reduce

						action -= _productionCount;

						// Adjust stack top to start of production's right hand side.  By
						// construction of the action table, the rhs length is at least 1.

						_stackTop -= (_productionRHSLength[action] - 1);

						// Call the corresponding nonterminal factory, and push the
						// nonterminal's value onto the value stack

						_valueStack[_stackTop] = _nonterminalFactories[action].makeNonterminal (
							this, _productionParam[action] );

						// Get the action for the nonterminal on the production's left hand side.
						// By construction of the action table, this is either a shift or a
						// shift-and-reduce.

						action =
							_actionTable[_parseStack[_stackTop-1]][_productionLHSSymbol[action]];
					}

					// The action is the target state number, plus _productionCountTimesTwo

					// Push the state that results from shifting the nonterminal on the last
					// production's left hand side, or from shifting the original terminal

					_parseStack[_stackTop] = action - _productionCountTimesTwo;
				}

				// Get the next terminal

				_token = _source.nextToken();
			}

			// Clear any pointers on the value stack that point to objects
			// we no longer need

			while (oldStackTop > _stackTop)
			{
				_valueStack[oldStackTop--] = null;
			}

		}	// end parse loop

		}	// end try block

		// Inform client of I/O exception

		catch (IOException e)
		{

			// Set abort flag

			abort = true;

			// Tell the client

			_client.parserIOException (this, e);
		}

		// Inform client of syntax exception

		catch (SyntaxException e)
		{

			// Set abort flag

			abort = true;

			// Tell the client

			_client.parserSyntaxException (this, e);
		}

		// Final cleanup

		finally
		{

			// Clear variables that may contain pointers to leftover objects

			for (int i = 0; i < _valueStack.length; ++i)
			{
				_valueStack[i] = null;
			}

			_token = null;

			_errorToken = null;

			_source = null;

			// Close the source

			source.close();
		}

		// Return abort flag

		return abort;
	}




	// This private function is called when a parser error is detected.  On
	// entry, the error terminal has been pushed back onto the token source,
	// and _errorToken contains a copy of the error terminal.
	//
	// This function must adjust the token source so that parsing can resume.
	// This function can remove and discard tokens from the source, and it can
	// create new tokens and push them onto the source.  It can also peek ahead
	// at upcoming tokens.
	//
	// If the error is repaired, the function returns true.  In this case, the
	// function must set _insertions, _insertionLength, _deletions, and
	// _deletionLength to indicate what repair was done.
	//
	// If the error cannot be repaired, the function returns false.

	private boolean errorRepair () throws IOException, SyntaxException
	{

		// The cost of the best repair found so far

		int bestRepairCost = repairCostCeiling;

		// The deletion distance of the best repair found so far, or -1 if no
		// repair is found so far.

		int bestDelDistance = -1;

		// If the best repair is a single-point insertion, this is the symbol
		// inserted;  otherwise it is -1

		int bestSinglePointInsertion = -1;

		// If the best repair is a continuation insertion, this is the length
		// of the continuation;  otherwise it is -1

		int bestContinuationLength = -1;

		// Loop over deletion distance, stopping when deletion cost reaches
		// best repair cost ...

		for (int delDistance = 0, delCost = 0; delCost < bestRepairCost; ++delDistance)
		{

			// If deletion distance is nonzero, try a zero-length insertion ...

			if (delDistance != 0)
			{

				// Initialize the error parse stack

				initErrorParseStack();

				// If a zero-length insertion works ...

				if (validateRepair (delDistance))
				{

					// This is the best repair

					bestRepairCost = delCost;
					bestDelDistance = delDistance;
					bestSinglePointInsertion = -1;
					bestContinuationLength = -1;

					// Break out of loop since we can't do better

					break;
				}
			}

			// Try a single point insertion

			int currentSinglePointInsertion =
				generateSinglePointInsertion (delDistance, bestRepairCost - delCost);

			// If it worked, it's the best repair so far

			if (currentSinglePointInsertion >= 0)
			{
				bestRepairCost = delCost + _currentInsertionCost;
				bestDelDistance = delDistance;
				bestSinglePointInsertion = currentSinglePointInsertion;
				bestContinuationLength = -1;
			}

			// Try a continuation insertion

			int currentContinuationLength =
				generateContinuationInsertion (delDistance, bestRepairCost - delCost);

			// If it worked, it's the best repair so far

			if (currentContinuationLength >= 0)
			{
				bestRepairCost = delCost + _currentInsertionCost;
				bestDelDistance = delDistance;
				bestSinglePointInsertion = -1;
				bestContinuationLength = currentContinuationLength;
			}

			// If we're at maximum deletion distance, break out of loop

			if (delDistance == _maxDeletion)
			{
				break;
			}

			// Get the next terminal symbol from the input

			_deletions[delDistance] = _source.peekAheadToken(delDistance).number;

			// If it's the end of file symbol, break out of loop

			if (_deletions[delDistance] == _eofSymbol)
			{
				break;
			}

			// If it's a continuation insertion, break out of loop

			if (_deletions[delDistance] == _symbolCount)
			{
				break;
			}

			// Otherwise, add its cost to the deletion cost

			delCost += _deletionCost[_deletions[delDistance]];
		}

		// If we didn't find a repair, return failure

		if (bestDelDistance == -1)
		{
			return false;
		}

		// Set the deletion length for return

		_deletionLength = bestDelDistance;

		// Pop the required number of tokens off the source

		for (int i = 0; i < _deletionLength; ++i)
		{
			_source.nextToken();
		}

		// If we got a single-point insertion ...

		if (bestSinglePointInsertion != -1)
		{

			// Set the insertion length for return

			_insertionLength = 1;

			// Put the symbol into the _insertions array

			_insertions[0] = bestSinglePointInsertion;

			// Push the insertion token onto the source, copying position
			// information from the error token

			_token = new Token (_errorToken);
			_token.number = bestSinglePointInsertion;
			_token.value = null;

			_source.pushBackToken (_token);
		}

		// Otherwise, if we got a continuation insertion ...

		else if (bestContinuationLength != -1)
		{

			// Set the insertion length for return

			_insertionLength = bestContinuationLength;

			// Push the insertion tokens onto the source, copying position
			// information from the error token

			_token = new Token (_errorToken);
			_token.number = _symbolCount;
			_token.value = null;

			for (int i = 0; i < _insertionLength; ++i)
			{
				_source.pushBackToken (_token);
			}
		}

		// Otherwise, we got a zero-length insertion ...

		else
		{

			// Set the insertion length for return

			_insertionLength = 0;
		}

		// Return success

		return true;
	}




	// This private function initializes the error parse stack.  It does this
	// by copying the entire parse stack to the error parse stack.

	private void initErrorParseStack ()
	{

		// If the error parse stack is too small ...

		if (_currentErrorStackSize < (_stackTop + 1))
		{

			// Reallocate the error parse stack

			_errorParseStack = new int[_currentStackSize];

			_currentErrorStackSize = _currentStackSize;
		}

		// Copy the parse stack to the error parse stack

		System.arraycopy (_parseStack, 0, _errorParseStack, 0, _stackTop + 1);

		// Copy the stack top pointer

		_errorStackTop = _stackTop;

		return;
	}




	// This private function enlarges the error parse stack.

	private void enlargeErrorParseStack ()
	{

		// Allocate new stack

		int[] newErrorParseStack = new int[_currentErrorStackSize + _incrementalStackSize];

		// Copy existing stack contents into the new stack

		System.arraycopy (_errorParseStack, 0, newErrorParseStack, 0, _currentErrorStackSize);

		// Adopt the new stack

		_errorParseStack = newErrorParseStack;

		_currentErrorStackSize += _incrementalStackSize;

		return;
	}




	// This private function parses a symbol on the error parse stack.
	//
	// The function performs repeated transitions until it performs a shift,
	// accept, or error action.
	//
	// If the last action is shift or accept, the function returns the parser
	// state which performed the shift or accept.  If the last action is error,
	// the function returns -1.
	//
	// If the last action is shift, the error parse stack is in the state
	// following the shift action.  (If the shift action is a shift-reduce,
	// then any required reductions are performed.)  Otherwise, the error parse
	// stack is in the state which performed the accept or error action.
	//
	// Note that the symbol can be _symbolCount to force the unwinding parser
	// action.  In this case, the last action is always shift, because there is
	// a shift or reduce unwinding action defined for every state.

	private int parseErrorSymbol (int symbol)
	{

		// Loop until return

		for ( ; ; )
		{

			// Get the action from the parse table, and advance the stack
			// top in preparation for the next shift

			int oldState = _errorParseStack[_errorStackTop++];

			int action = _actionTable[oldState][symbol];

			// Enlarge the stack if necessary

			if (_errorStackTop == _currentErrorStackSize)
			{
				enlargeErrorParseStack ();
			}

			// If the action is reduce or accept ...

			if (action < _productionCount)
			{

				// If the action is accept, return success

				if (action == _goalProduction)
				{

					// Restore stack top pointer

					--_errorStackTop;

					// Return success

					return oldState;
				}

				// The action is the number of the production to reduce

				// Adjust stack top to start of production's right hand side

				_errorStackTop -= _productionRHSLength[action];

				// Get the action for the nonterminal on the production's left hand side.
				// By construction of the action table, this is either a shift or a
				// shift-and-reduce.

				action =
					_actionTable[_errorParseStack[_errorStackTop-1]][_productionLHSSymbol[action]];

				// While the action is shift-and-reduce ...

				while (action < _productionCountTimesTwo)
				{

					// The action is the number of the production to reduce, plus
					// _productionCount

					// Get the number of the production to reduce

					action -= _productionCount;

					// Adjust stack top to start of production's right hand side.  By
					// construction of the action table, the rhs length is at least 1.

					_errorStackTop -= (_productionRHSLength[action] - 1);

					// Get the action for the nonterminal on the production's left hand side.
					// By construction of the action table, this is either a shift or a
					// shift-and-reduce.

					action =
						_actionTable[_errorParseStack[_errorStackTop-1]][_productionLHSSymbol[action]];
				}

				// The action is the target state number, plus _productionCountTimesTwo

				// Push the state that results from shifting the nonterminal on the
				// production's left hand side

				_errorParseStack[_errorStackTop] = action - _productionCountTimesTwo;
			}

			// Otherwise, the action is shift, shift-and-reduce, or error ...

			else
			{

				// If the action is error ...

				if (action == _productionCountTimesTwo)
				{

					// Restore the stack top pointer

					--_errorStackTop;

					// Return failure

					return -1;
				}

				// Otherwise, the action is shift or shift-and-reduce ...

				// While the action is shift-and-reduce ...

				while (action < _productionCountTimesTwo)
				{

					// The action is the number of the production to reduce, plus
					// _productionCount

					// Get the number of the production to reduce

					action -= _productionCount;

					// Adjust stack top to start of production's right hand side.  By
					// construction of the action table, the rhs length is at least 1.

					_errorStackTop -= (_productionRHSLength[action] - 1);

					// Get the action for the nonterminal on the production's left hand side.
					// By construction of the action table, this is either a shift or a
					// shift-and-reduce.

					action =
						_actionTable[_errorParseStack[_errorStackTop-1]][_productionLHSSymbol[action]];
				}

				// The action is the target state number, plus _productionCountTimesTwo

				// Push the state that results from shifting the nonterminal on the last
				// production's left hand side, or from shifting the original terminal

				_errorParseStack[_errorStackTop] = action - _productionCountTimesTwo;

				// We did a shift, so return success

				return oldState;
			}

		}	// end parse loop

	}




	// This private function validates an error repair.
	//
	// On entry, delDistance is the number of input symbols to skip, and the
	// error parse stack is set up for parsing.  The delDistance parameter may
	// not point past the end-of-file token (though it may point at the
	// end-of-file token).
	//
	// The return value is true if the repair is validated.  The repair is
	// considered validated if it can successfully shift _validationLength
	// input symbols, or shift the end-of-file symbol.

	private boolean validateRepair (int delDistance) throws IOException, SyntaxException
	{

		// Loop over input symbols we must validate

		for (int valDistance = 0; valDistance < _validationLength; ++valDistance)
		{

			// Get the next input token

			_token = _source.peekAheadToken (delDistance + valDistance);

			// If the token is a continuation insertion ...

			if (_token.number == _symbolCount)
			{

				// Return failure

				return false;
			}

			// Try to shift the symbol.  If we can't shift it ...

			if (parseErrorSymbol (_token.number) < 0)
			{

				// Return failure

				return false;
			}

			// If we just shifted the end-of-file symbol ...

			if (_token.number == _eofSymbol)
			{

				// Return success

				return true;
			}
		}

		// We shifted all symbols, return success

		return true;
	}




	// This private function attempts to find and validate a single point
	// insertion repair.
	//
	// On entry, delDistance is the number of input symbols to skip when
	// performing validation.  Only repairs that cost less than costCeiling
	// are considered.
	//
	// If the function finds a repair, it returns the insertion symbol and sets
	// _currentInsertionCost to the cost.
	//
	// Otherwise, it returns -1 and destroys _currentInsertionCost.

	private int generateSinglePointInsertion (int delDistance, int costCeiling)
		throws IOException, SyntaxException
	{

		// Loop over single point insertion list

		for (int index = 0; index < _singlePointInsertionCount; ++index)
		{

			// Get the symbol to try

			int symbol = _singlePointInsertions[index];

			// Get the cost

			_currentInsertionCost = _insertionCost[symbol];

			// If the cost is too high, return failure

			if (_currentInsertionCost >= costCeiling)
			{
				return -1;
			}

			// Initialize the error parse stack

			initErrorParseStack ();

			// Try to shift the symbol, and skip it if we can't

			if (parseErrorSymbol (symbol) < 0)
			{
				continue;
			}

			// If we can validate the repair, return the symbol

			if (validateRepair (delDistance))
			{
				return symbol;
			}
		}

		// Failed to find a suitable symbol, return failure

		return -1;
	}




	// This private function attempts to find and validate a continuation
	// insertion repair.
	//
	// On entry, delDistance is the number of input symbols to skip when
	// performing validation.  Only repairs that cost less than costCeiling
	// are considered.
	//
	// If the function finds a repair, it returns the continuation length and
	// sets _currentInsertionCost to the cost.
	//
	// Otherwise, it returns -1 and destroys _currentInsertionCost.
	//
	// In any case, this function fills the _insertions array with the symbols
	// that are generated by continuation.

	private int generateContinuationInsertion (int delDistance, int costCeiling)
		throws IOException, SyntaxException
	{

		// Loop over continuation length

		for (int cont = 1; cont <= _maxInsertion; ++cont)
		{

			// Cost so far

			_currentInsertionCost = 0;

			// Initialize the error parse stack

			initErrorParseStack ();

			// Loop until we've generated cont symbols

			for (int index = 0; index < cont; ++index)
			{

				// Perform the unwinding actions until an unwinding symbol is
				// shifted, then get the symbol

				_insertions[index] =
					_unwindingTable[parseErrorSymbol (_symbolCount)] - _productionCount;

				// If we shifted the end-of-file symbol, return failure

				if (_insertions[index] == _eofSymbol)
				{
					return -1;
				}

				// Add to the insertion cost

				_currentInsertionCost += _insertionCost[_insertions[index]];

				// If the cost is too high, return failure

				if (_currentInsertionCost >= costCeiling)
				{
					return -1;
				}
			}

			// If we can validate the repair, return the continuation length

			if (validateRepair (delDistance))
			{
				return cont;
			}
		}

		// Failed to find a suitable continuation, return failure

		return -1;
	}


}


