// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.util.ConstIntSet;
import invisible.jacc.util.ConstSmallIntSet;
import invisible.jacc.util.IntEnumeration;
import invisible.jacc.util.IntSet;
import invisible.jacc.util.ObjectSet;


/*->

  ContextFreeGrammar provides a common representation of a context-free
  grammar (CFG) in a form convenient for grammar analysis algorithms.  It
  also contains some basic analysis algorithms, and holds some basic
  calculated information about the grammar.

->*/


public class ContextFreeGrammar 
{

	// Symbols are represented by nonnegative integers ranging from 0 to one
	// less than the number of symbols.  Symbols may be terminal or
	// nonterminal.  There is no special significance to symbol 0, nor are
	// symbols assumed to be in any particular order.  However, as a practical
	// matter clients will typically impose some ordering on the symbols.

	// Likewise, productions are identified by nonnegative integers ranging
	// from 0 to one less than the number of productions.  There is no
	// special significance to production 0, nor are productions assumed to
	// be in any particular order.

	// The following array of strings gives the names of the symbols.

	private String[] _symbols;

	public final int symbolCount ()
	{
		return _symbols.length;
	}

	public final String symbolName (int symbol)
	{
		return _symbols[symbol];
	}


	// One symbol is designated as the goal symbol.  It must be a nonterminal.

	private int _goalSymbol;

	public final int goalSymbol ()
	{
		return _goalSymbol;
	}


	// The following table lists the productions of the grammar.  Each
	// production is described by an array of int.  Element 0 of the array
	// is the left hand side, while elements 1 and up are the right hand side.
	// An array of length 1 (containing just a left hand side) denotes an
	// epsilon production, that is, a production whose right hand side is the
	// empty string.

	private int[][] _productions;

	public final int productionCount ()
	{
		return _productions.length;
	}

	public final int productionLength (int production)
	{
		return _productions[production].length;
	}

	public final int productionSymbol (int production, int index)
	{
		return _productions[production][index];
	}


	// This constant is used to access the left hand side of a production

	public static final int lhs = 0;


	// This constant is used to access the right hand side of a production

	public static final int rhs = 1;


	// The following table of boolean is indexed by symbols.  The value is
	// true for terminal symbols, false for nonterminal symbols.  This table
	// is derived by scanning the table of productions;  nonterminals are
	// symbols that appear on the left hand side of a production.

	private boolean[] _isTerminal;

	public final boolean isTerminal (int symbol)
	{
		return _isTerminal[symbol];
	}


	// The following table of IntSet is indexed by symbols.  The value of
	// _productionSet[s] is the set of productions which have s as their left
	// hand side.  (If s is a terminal symbol, then _productionSet[s] is the
	// empty set.)

	private IntSet[] _productionSet;

	public final ConstIntSet productionSet (int symbol)
	{
		return _productionSet[symbol];
	}


	// The following table of boolean is indexed by symbols.  The value of
	// _isReachable[s] is true if there is a string containing s that can
	// be derived from the goal symbol.  The presence of an unreachable
	// symbol probably indicates an error in formulating the grammar.

	private boolean[] _isReachable;

	public final boolean isReachable (int symbol)
	{
		return _isReachable[symbol];
	}


	// The following table of boolean is indexed by symbols.  The value is
	// true if there is a sequence of productions that can convert the
	// symbol to a string that contains only terminals (or the empty string).
	// The presence of a symbol that does not derive terminals probably
	// indicates an error in formulating the grammar.

	private boolean[] _derivesTerminals;

	public final boolean derivesTerminals (int symbol)
	{
		return _derivesTerminals[symbol];
	}


	// The following table of boolean is indexed by symbols.  The value is
	// true if there is a sequence of productions that can convert the
	// symbol to the empty string.

	private boolean[] _derivesEpsilon;

	public final boolean derivesEpsilon (int symbol)
	{
		return _derivesEpsilon[symbol];
	}


	// The following table of IntSet is indexed by symbols.  The value of
	// _firstSet[s] is the set of all terminal symbols t such that t is the
	// first symbol of a string that can be derived from s.  (If s is a
	// terminal symbol, then _firstSet[s] equals the set {s}.)

	private IntSet[] _firstSet;

	public final ConstIntSet firstSet (int symbol)
	{
		return _firstSet[symbol];
	}


	// The following table of boolean is indexed by symbols.  The value of
	// _followEpsilon[s] is true if there is a string ending in s that can
	// be derived from the goal symbol.

	private boolean[] _followEpsilon;

	public final boolean followEpsilon (int symbol)
	{
		return _followEpsilon[symbol];
	}


	// The following table of IntSet is indexed by symbols.  The value of
	// _followSet[s] is the set of all terminal symbols t such that t appears
	// immediately after s in a string that can be derived from the goal
	// symbol.

	private IntSet[] _followSet;

	public final ConstIntSet followSet (int symbol)
	{
		return _followSet[symbol];
	}



	// Create a context-free grammer.  This function creates (deep) copies of
	// the arrays passed in.

	public ContextFreeGrammar (String[] symbols, int goalSymbol, int[][] productions)
	{

		// Validate the symbol table

		if ((symbols == null) || (symbols.length == 0))
		{
			throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
		}

		for (int symbol = 0; symbol < symbols.length; ++symbol)
		{
			if (symbols[symbol] == null)
			{
				throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
			}
		}

		// Validate the goal symbol

		if ((goalSymbol < 0) || (goalSymbol >= symbols.length))
		{
			throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
		}

		// Validate the production table

		if ((productions == null) || (productions.length == 0))
		{
			throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
		}

		for (int production = 0; production < productions.length; ++production)
		{
			if ((productions[production] == null)
				|| (productions[production].length == 0))
			{
				throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
			}

			for (int i = 0; i < productions[production].length; ++i)
			{
				if ((productions[production][i] < 0)
					|| (productions[production][i] >= symbols.length))
				{
					throw new IllegalArgumentException ("ContextFreeGrammer.ContextFreeGrammar");
				}
			}
		}

		// Copy the symbol table

		_symbols = new String[symbols.length];

		for (int symbol = 0; symbol < symbols.length; ++symbol)
		{
			_symbols[symbol] = symbols[symbol];
		}

		// Copy the goal symbol

		_goalSymbol = goalSymbol;

		// Copy the production table

		_productions = new int[productions.length][];

		for (int production = 0; production < productions.length; ++production)
		{
			_productions[production] = new int[productions[production].length];

			for (int i = 0; i < productions[production].length; ++i)
			{
				_productions[production][i] = productions[production][i];
			}
		}

		// Build the grammar information structures

		buildIsTerminal ();
		buildProductionSet ();

		buildIsReachable ();
		buildDerivesTerminals ();
		buildDerivesEpsilon ();
		buildFirstSet ();

		buildFollowEpsilonAndFollowSet ();

		return;
	}


	// Determines which symbols are terminals and which are nonterminals, and
	// stores the result in the _isTerminal array.  Also allocates _isTerminal.

	private void buildIsTerminal ()
	{

		// Allocate an array to hold the results

		_isTerminal = new boolean[_symbols.length];

		// Assume all symbols are terminals

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_isTerminal[symbol] = true;
		}

		// Scan the table of productions

		for (int production = 0; production < _productions.length; ++production)
		{

			// Left hand side of production is a nonterminal

			_isTerminal[_productions[production][lhs]] = false;
		}

		return;
	}


	// Creates the set of productions for each symbol and stores the result in
	// _productionSet.  Also allocates _productionSet.

	private void buildProductionSet ()
	{

		// Allocate the table, plus a set for each symbol

		_productionSet = new IntSet[_symbols.length];

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_productionSet[symbol] = new IntSet ();
		}

		// Scan the table of productions

		for (int production = 0; production < _productions.length; ++production)
		{

			// Add this production to the set for its left hand side

			_productionSet[_productions[production][lhs]].addElement (production);
		}

		// Compact the sets

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_productionSet[symbol].compact ();
		}

		return;
	}


	// Determines which symbols are reachable from the goal symbol, and stores
	// the result in _isReachable.  Also allocates _isReachable.

	private void buildIsReachable ()
	{

		// Allocate an array to hold the results

		_isReachable = new boolean[_symbols.length];

		// Assume all symbols are not reachable

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_isReachable[symbol] = false;
		}

		// Mark the goal symbol reachable

		_isReachable[_goalSymbol] = true;

		// Do this repeatedly until the set of reachable symbols stops changing

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Scan the table of productions

			for (int production = 0; production < _productions.length; ++production)
			{

				// If the left hand side is reachable ...

				if (_isReachable[_productions[production][lhs]])
				{

					// Scan the right hand side

					for (int i = rhs; i < _productions[production].length; ++i)
					{

						// If the symbol isn't reachable yet ...

						if (!_isReachable[_productions[production][i]])
						{

							// ... Mark it reachable 

							_isReachable[_productions[production][i]] = true;

							isChanged = true;
						}
					}
				}

			}	// end scan table of productions

		}	// end loop until nothing changes

		return;
	}


	// Determines which symbols can derive a string of terminals, and stores
	// the result in _derivesTerminals.  Also allocates _derivesTerminals.
	//
	// This function uses _isTerminal.

	private void buildDerivesTerminals ()
	{

		// Allocate an array to hold the results

		_derivesTerminals = new boolean[_symbols.length];

		// Initially, only terminals derive terminals

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_derivesTerminals[symbol] = _isTerminal[symbol];
		}

		// Do this repeatedly until the set of symbols that derive terminals stops changing

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Scan the table of productions

			for (int production = 0; production < _productions.length; ++production)
			{

				// If the left hand side does not derive terminals yet ...

				if (!_derivesTerminals[_productions[production][lhs]])
				{

					// Check if each symbol on the right hand side derives terminals

					checkTerminals:
					{

						// Scan the right hand side

						for (int i = rhs; i < _productions[production].length; ++i)
						{

							// If the symbol doesn't derive terminals, then the lhs doesn't

							if (!_derivesTerminals[_productions[production][i]])
							{
								break checkTerminals;
							}
						}

						// All symbols on rhs derive terminals, so lhs does too

						_derivesTerminals[_productions[production][lhs]] = true;

						isChanged = true;
					}
				}

			}	// end scan table of productions

		}	// end loop until nothing changes

		return;
	}


	// Determines which symbols can derive the empty string, and stores the
	// result in _derivesEpsilon.  Also allocates _derivesEpsilon.

	private void buildDerivesEpsilon ()
	{

		// Allocate an array to hold the results

		_derivesEpsilon = new boolean[_symbols.length];

		// Assume all symbols do not derive epsilon

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_derivesEpsilon[symbol] = false;
		}

		// Do this repeatedly until the set of symbols that derives epsilon stops changing

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Scan the table of productions

			for (int production = 0; production < _productions.length; ++production)
			{

				// If the left hand side does not derive epsilon yet ...

				if (!_derivesEpsilon[_productions[production][lhs]])
				{

					// Check if each symbol on the right hand side derives epsilon

					checkEpsilon:
					{

						// Scan the right hand side

						for (int i = rhs; i < _productions[production].length; ++i)
						{

							// If the symbol doesn't derive epsilon, then the lhs doesn't

							if (!_derivesEpsilon[_productions[production][i]])
							{
								break checkEpsilon;
							}
						}

						// All symbols on rhs derive epsilon, so lhs does too

						_derivesEpsilon[_productions[production][lhs]] = true;

						isChanged = true;
					}
				}

			}	// end scan table of productions

		}	// end loop until nothing changes

		return;
	}


	// For each symbol s, determines which terminal symbols can be the first
	// symbol of a string derived from s.  The result is stored in _firstSet.
	// Also allocates _firstSet.
	//
	// This function uses _isTerminal.

	private void buildFirstSet ()
	{

		// Allocate an array to hold the results

		_firstSet = new IntSet[_symbols.length];

		// Create a set for each symbol

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{

			// Create the set

			_firstSet[symbol] = new IntSet ();

			// If it's a terminal symbol, add the symbol to the set

			if (_isTerminal[symbol])
			{
				_firstSet[symbol].addElement (symbol);
			}
		}

		// Do this repeatedly until nothing changes

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Scan the table of productions

			for (int production = 0; production < _productions.length; ++production)
			{

				// To the first set for the left hand side, add the first set of
				// the right hand side

				isChanged |= _firstSet[_productions[production][lhs]].union (
					firstSet (production, rhs) );

			}	// end scan table of productions

		}	// end loop until nothing changes

		// Compact the sets

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_firstSet[symbol].compact ();
		}

		return;
	}


	// For each symbol s, determines which terminal symbols can appear
	// immediately after s in strings derivable from the goal symbol.  Also
	// determines if s can appear at the end of a string derivable from the
	// goal symbol.  The results are stored in _followEpsilon and _followSet.
	// This function also allocates _followEpsilon and _followSet.
	//
	// This function uses _firstSet, _derivesEpsilon, and _isReachable.

	private void buildFollowEpsilonAndFollowSet ()
	{

		// Allocate an array to hold the resulting epsilon flags

		_followEpsilon = new boolean[_symbols.length];

		// Initially, only the goal symbol can appear at the end of a string

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			if (symbol == _goalSymbol)
			{
				_followEpsilon[symbol] = true;
			}
			else
			{
				_followEpsilon[symbol] = false;
			}
		}

		// Allocate an array to hold the resulting follow sets

		_followSet = new IntSet[_symbols.length];

		// Create an empty set for each symbol

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_followSet[symbol] = new IntSet ();
		}

		// Do this repeatedly until nothing changes

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Scan the table of productions

			for (int production = 0; production < _productions.length; ++production)
			{

				// If the left hand side is reachable ...

				if (_isReachable[_productions[production][lhs]])
				{

					// Scan the right hand side

					for (int i = rhs; i < _productions[production].length; ++i)
					{

						// To the follow set of the symbol indexed by i, add the possible
						// initial terminals derivable from the succeeding substring

						isChanged |= _followSet[_productions[production][i]].union (
							firstSet (production, i + 1) );

						// If the succeeding substring derives epsilon ...

						if (derivesEpsilon (production, i + 1))
						{

							// Also add the follow set of the left hand side

							isChanged |= _followSet[_productions[production][i]].union (
								_followSet[_productions[production][lhs]] );

							// If the symbol indexed by i is not marked in _followEpsilon, and
							// the left hand side is marked in _followEpsilon ...

							if ((!_followEpsilon[_productions[production][i]])
								&& (_followEpsilon[_productions[production][lhs]]))
							{

								// Mark the symbol indexed by i in _followEpsilon

								_followEpsilon[_productions[production][i]] = true;
								isChanged = true;
							}
						}

					}	// end scan of right hand side
				}

			}	// end scan table of productions

		}	// end loop until nothing changes

		// Compact the sets

		for (int symbol = 0; symbol < _symbols.length; ++symbol)
		{
			_followSet[symbol].compact ();
		}

		return;
	}


	// Given a string of symbols, this function computes the set of all
	// terminal symbols t such that t is the first symbol of a string that
	// can be derived from the given string.
	//
	// The string of symbols consists of the symbols symbolArray[i] for i
	// ranging from startOffset to endOffset-1.

	public IntSet firstSet (int[] symbolArray, int startOffset, int endOffset)
	{

		// Allocate a set to hold the result

		IntSet resultSet = new IntSet ();

		// Scan the string

		for (int i = startOffset; i < endOffset; ++i)
		{

			// Add all "firsts" for the current symbol

			resultSet.union (_firstSet[symbolArray[i]]);

			// If this symbol doesn't derive epsilon, can't get to next symbol

			if (!_derivesEpsilon[symbolArray[i]])
			{
				break;
			}
		}

		// Return the result set

		return resultSet;
	}


	// Given a production and an index into the production's right hand side,
	// this function computes the set of all terminal symbols t such that t is
	// the first symbol of a string that can be derived from the tail end of
	// the production.
	//
	// The tail end of the production consists of the string of symbols
	// beginning with _productions[production][index] and extending through
	// the end of the production.

	public IntSet firstSet (int production, int index)
	{

		// Return the first set for the tail end of the production

		return firstSet (
			_productions[production],
			index,
			_productions[production].length );
	}


	// Given a string of symbols, this function returns true if the empty
	// string can be derived from the given string.
	//
	// The string of symbols consists of the symbols symbolArray[i] for i
	// ranging from startOffset to endOffset-1.

	public boolean derivesEpsilon (int[] symbolArray, int startOffset, int endOffset)
	{

		// Scan the string

		for (int i = startOffset; i < endOffset; ++i)
		{

			// If this symbol doesn't derive epsilon, can't get the empty string

			if (!_derivesEpsilon[symbolArray[i]])
			{
				return false;
			}
		}

		// All symbols derive epsilon, so the string does too

		return true;
	}


	// Given a production and an index into the production's right hand side,
	// this function returns true if the empty string can be derived from the
	// tail end of the production.
	//
	// The tail end of the production consists of the string of symbols
	// beginning with _productions[production][index] and extending through
	// the end of the production.

	public boolean derivesEpsilon (int production, int index)
	{

		// Return the derives epsilon result for the tail end of the production

		return derivesEpsilon (
			_productions[production],
			index,
			_productions[production].length );
	}


	// Given a set of symbols, this function returns a set containing the
	// names of the symbols.
	//
	// This function is mainly for debugging.

	public ObjectSet symbolName (ConstIntSet symbols)
	{

		// Allocate a set to hold the result

		ObjectSet resultSet = new ObjectSet (symbols.elementCount());

		// For each symbol in the set ...

		for (IntEnumeration e = symbols.elements(); e.hasMoreElements(); )
		{

			// Add the name of the symbol to the result set

			resultSet.addElement (_symbols[e.nextElement()]);
		}

		// Return the result

		return resultSet;
	}


	// Given a set of symbols, this function returns a set containing the
	// names of the symbols.
	//
	// This function is mainly for debugging.

	public ObjectSet symbolName (ConstSmallIntSet symbols)
	{

		// Allocate a set to hold the result

		ObjectSet resultSet = new ObjectSet (symbols.elementCount());

		// For each symbol in the set ...

		for (IntEnumeration e = symbols.elements(); e.hasMoreElements(); )
		{

			// Add the name of the symbol to the result set

			resultSet.addElement (_symbols[e.nextElement()]);
		}

		// Return the result

		return resultSet;
	}


}

