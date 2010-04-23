// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.parse.InterruptedCompilerException;
import invisible.jacc.util.*;

import java.util.Enumeration;


/*->

  LRMachine holds the configuration finite state machine for an LALR(1)
  context-free grammar.  This is the complete machine, including the dotted
  productions and the lookahead sets.

->*/


public final class LRMachine 
{

	// The following is the context-free grammar

	private ContextFreeGrammar _CFG;

	final ContextFreeGrammar CFG ()
	{
		return _CFG;
	}


	// The following is the set of states for the CFSM.  Each element of the
	// set is an LRMachineState object.

	private ObjectSet _states;

	final boolean addState (LRMachineState state)
	{
		return _states.addElement (state);
	}

	final Enumeration enumerateStates ()
	{
		return _states.elements();
	}


	// The following is the initial state of the CFSM.

	private LRMachineState _initialState;


	// Import constants for accessing production table

	private static final int lhs = ContextFreeGrammar.lhs;
	private static final int rhs = ContextFreeGrammar.rhs;


	// The following table is indexed by symbols.  For each symbol s,
	// _predictionTable[s] is a set that contains all LRMachinePrediction
	// objects predicted by the symbol s.  (If s is a terminal, then
	// _predictionTable[s] is the empty set.)

	private PredicateSet[] _predictionTable;

	final Enumeration enumeratePredictions (int symbol)
	{
		return _predictionTable[symbol].elements();
	}


	// The following table is indexed by productions and dot locations.  For
	// production p and dot location d, _dottedProductionFlyweight[p][d-rhs] is
	// the corresponding LRMachineDottedProduction object.

	private LRMachineDottedProduction[][] _dottedProductionFlyweight;

	final LRMachineDottedProduction dottedProductionFlyweight (int production, int index)
	{
		return _dottedProductionFlyweight[production][index];
	}


	// The following is the number of the goal production.  This is
	// the unique production with the goal symbol as the left hand side.

	private int _goalProduction;

	final int goalProduction ()
	{
		return _goalProduction;
	}


	// The following is the number of the end-of-file symbol.  It is a terminal
	// symbol, which appears as the last symbol in the goal production.

	private int _eofSymbol;

	final int eofSymbol ()
	{
		return _eofSymbol;
	}


	// The following is the minimum number of a terminal symbol.

	private int _minTerminalSymbol;


	// The following is the maximum number of a terminal symbol.

	private int _maxTerminalSymbol;


	// The following contains the machine type

	private int _machineType;

	public static final int LR1 = 0;
	public static final int LALR1 = 1;
	public static final int PLR1 = 2;

	public final int machineType ()
	{
		return _machineType;
	}


	// The following is the predicate used to construct cognates

	private BinaryPredicate _cognatePredicate;


	// The following is the number of dotted productions.

	private int _dottedProductionCount;

	final int dottedProductionCount ()
	{
		return _dottedProductionCount;
	}


	// The following is the array of cognates

	private LRMachineCognate[] _cognateArray;

	public final int cognateCount ()
	{
		return _cognateArray.length;
	}

	public final String cognateToString (int index)
	{
		return _cognateArray[index].toString();
	}

	public final int cognateBasisCount (int index)
	{
		return _cognateArray[index].basisCount();
	}

	public final String cognateBasisToString (int index, int basis)
	{
		return _cognateArray[index].basisToString(basis);
	}

	public final String cognateLookaheadToString (int index, int basis)
	{
		return _cognateArray[index].lookaheadToString(basis);
	}


	// The following is the array of prediction costs.  This can be null.
	// If non-null, the array is indexed by productions.  The value of
	// _productionCost[p] is the cost assigned to production p.
	//
	// If it is non-null, then the machine preserves ordering in basis set
	// elements, and predictions are sorted by cost.  This allows the
	// generation of an LM error repair table.
	//
	// If it is null, then basis set elements are considered to be
	// unordered, and the ordering of predictions is immaterial.  This is the
	// "standard" LR machine.
	//
	// Production costs must obey the following rule:  Suppose that p is the
	// production A->mBn where m and n are (possibly empty) strings of symbols
	// and B is a nonterminal.  Then there must exist a production q of the
	// form B->k, where k is a (possibly empty) string of symbols, such that
	// _productionCost[q] < _productionCost[p].
	//
	// Notice that this rule cannot be satisfied if there is a nonterminal that
	// does not derive terminals.  (If the lhs of a production is a nonterminal
	// that does not derive terminals, then the rhs must contain at least one
	// nonterminal that does not derive terminals.  Assuming the rule is
	// satisified, this lets you find an infinite sequence of productions each
	// of which must have lower cost than the previous one, which is impossible
	// since the total number of productions is finite.)
	//
	// Also notice that the lowest-cost production(s) cannot have a nonterminal
	// on the rhs, since by definition the presence of a nonterminal on the rhs
	// implies the existence of a lower-cost production.
	//
	// Note:  Our rule is stricter than stated in Fischer and LeBlanc (pages
	// 710-719).  They allow _productionCost[q] == _productionCost[p] if m and
	// n derive epsilon.  The problem is, this appears to permit the LM action
	// table to contain a loop of reduce actions for nonterminals that derive
	// epsilon.  For example, consider the three productions A->epsilon, A->B,
	// and B->A.  Since A and B both derive epsilon, Fischer and LeBlanc give
	// all three productions a cost of zero.  But then the cost function cannot
	// force a reduction on A->epsilon rather than A->B;  and reducing by A->B
	// would necessarily lead to a loop.  Further, the analysis is greatly
	// complicated by allowing, as Fischer and LeBlanc do, a production of the
	// form C->D to have the same cost as the least expensive production of
	// the form D->k.
	//
	// A set of production costs is typically created by assigning a positive
	// cost to each terminal symbol.  Then calculate a cost for each
	// nonterminal symbol, and a cost for each production, as follows:  (i) the
	// cost of a production is the sum of the costs of the symbols on its rhs;
	// (ii) the cost of a nonterminal is one plus the minimum cost of any
	// production that has the given nonterminal as its lhs.  (This definition
	// is circular, but can be calculated iteratively.)  We have defined
	// _productionCost as long[] to avoid overflows in this calculation.

	private long[] _productionCost;

	private static final long infiniteProductionCost = 0x7FFFFFFFFFFFFFFFL;

	public final long[] productionCost ()
	{
		return _productionCost;
	}


	// Number of unresolved parser conflicts found.  If this is nonzero, an LR
	// machine could not be constructed for the specified grammar.

	private int _conflictCount;

	public final int conflictCount ()
	{
		return _conflictCount;
	}


	// The following is the predicate used to resolve parser action conflicts.
	// It can be null if no ambiguity resolution is desired.
	//
	// A parser action is encoded into an integer as follows:  Reducing
	// production p is represented by p.  Shifting symbol s is represented by
	// s+PC where PC is the total number of productions in the grammar.
	//
	// _precedencePredicate.value(x,y) returns true if action x has precedence
	// over action y.  On each call to _precedencePredicate.value(x,y), it is
	// guaranteed that x is not equal to y, and that either x and y are both
	// reduce actions, or one is a reduce action and the other is a shift
	// action.
	//
	// When a parser conflict is discovered, the machine constructs a set
	// containing all possible actions.  It then checks every pair of possible
	// actions to determine which actions have precedence over which other
	// actions.  If there is exactly one action that has precedence over every
	// other possible action, then that action is selected.  Otherwise, the
	// conflict is unresolved, and _conflictCount is incremented.
	//
	// Note:  A simple definition of _precedencePredicate could always have
	// _precedencePredicate.value(x,y) return true if x < y.  This suffices
	// to generate left-associative operators.

	private BinaryIntPredicate _precedencePredicate;


	// Parse table.
	//
	// For LR(1) state n and symbol s, _parseTable[n][s] encodes a parser
	// action as follows:  (i) If the action is to reduce production p, the 
	// value is p.  (ii) If the action is to accept, the value is g, where g is
	// the numerical value of the goal production.  (iii) If the action is to
	// shift and go to state m, the value is m+PC, where PC is the total number
	// of productions in the grammar.  (iv) If the action is to signal error,
	// the value is PC.
	//
	// Note that the parser never reduces the goal production or goes to state
	// 0, so the encodings for accept and error do not create ambiguity.
	//
	// If there is an unresolved parsing conflict in state n and symbol s, then
	// _parseTable[n][s] contains the value -1-CS, where CS is the conflict set
	// number.  Thus, all conflicts are tagged with negative values.  The value
	// of _parseTable[n][s] may be passed to function _conflictSet() to obtain
	// the set of actions that comprise the conflict.

	private int[][] _parseTable;

	public final int[][] parseTable ()
	{
		return _parseTable;
	}


	// Unwinding action table.
	//
	// For LR(1) state n, _unwindingTable[n] encodes an unwinding action as
	// follows:  (i) If the unwinding action is to reduce production p, the
	// value is p.  (ii) If the unwinding action is to shift terminal symbol s,
	// the value is s+PC where PC is the total number of productions in the
	// grammar.
	//
	// Note that shifting the end-of-file symbol is equivalent to the accept
	// action.
	//
	// This table is generated even if error repair is not enabled.  However,
	// in this case the table merely generates a legal continuation, not
	// necessarily a continuation that eventually reaches the accept state.

	private int[] _unwindingTable;

	public final int[] unwindingTable ()
	{
		return _unwindingTable;
	}


	// Unwinding parse action table.
	//
	// For LR(1) state n, _unwindingTable[n] encodes an unwinding parse action
	// as follows:  (i) If the unwinding action is to reduce production p, the
	// value is p.  (ii) If the unwinding action is to shift and go to state s,
	// the value is s+PC where PC is the total number of productions in the
	// grammar.
	//
	// For an unambiguous grammar, the contents of this table agree with the
	// parse actions given in _parseTable, using the terminal symbol sequence
	// generated by _unwindingTable.  (Therefore, _unwindingParseTable[n]
	// equals _unwindingTable[n] in case of a reduction, and it equals
	// _parseTable[n][_unwindingTable[n]-PC] in case of a shift.)
	//
	// For an ambiguous grammar, the action in _unwindingParseTable[n] is one
	// of the possible actions in state n, given as lookahead the next terminal
	// symbol generated by _unwindingTable.  In case of a parsing conflict,
	// there is no guarantee that the action in _unwindingParseTable[n] is the
	// same action that was placed in _parseTable when precedence was resolved.
	//
	// Therefore, for ambiguous grammars, _unwindingParseTable must be used
	// instead of _parseTable to manipulate the parse stack when processing
	// error insertions.
	//
	// This table is generated even if error repair is not enabled.  However,
	// in this case the table merely generates a legal continuation, not
	// necessarily a continuation that eventually reaches the accept state.

	private int[] _unwindingParseTable;

	public final int[] unwindingParseTable ()
	{
		return _unwindingParseTable;
	}


	// Conflict set table.
	//
	// Each element of the deque is a SmallIntSet that describes a conflict.
	// If the conflict includes a shift action, the set contains the value PC,
	// where PC is the number of productions in the grammar.  If the conflict
	// includes a reduce on production p, the set contains the value p.
	//
	// Conflict set CS is available as _conflictSetTable.peekFirst(CS).
	//
	// The function conflictSet(-1-CS) returns conflict set CS.  Note that the
	// argument is the value stored in parseTable.
	//
	// The function conflictSetCount() returns the number of conflict sets.
	// Note that conflict entries in parseTable can range in value from
	// -conflictSetCount() to -1.

	private ObjectDeque _conflictSetTable;

	public final ConstSmallIntSet conflictSet (int action)
	{
		return (ConstSmallIntSet) _conflictSetTable.peekFirst (-1 - action);
	}

	public final int conflictSetCount ()
	{
		return _conflictSetTable.elementCount();
	}
	
	
	// Statistics for the LR(0) machine.
	//
	// _LR0StateCount is the number of LR(0) states generated.
	//
	// _LR0PassCount is the number of passes required to generate LR(0) conflict
	// avoidance information (the merge check sets).
	
	private int _LR0StateCount;
	
	private int _LR0PassCount;
	
	public final int LR0StateCount ()
	{
		return _LR0StateCount;
	}
	
	public final int LR0PassCount ()
	{
		return _LR0PassCount;
	}
	
	final void setLR0Statistics (int stateCount, int passCount)
	{
		_LR0StateCount = stateCount;
		_LR0PassCount = passCount;
		return;
	}
	
	
	// Statistics for the LR(1) machine.
	//
	// _LR1CognateCounts is an array that contains one entry for each pass that
	// was used to construct the LR(1) cognates.  The value of each array entry
	// is the number of cognates processed during that pass.
	//
	// _LR1ConflictsResolved is the number of LR(1) parsing conflicts that were
	// successfully resolved.
	
	private int[] _LR1CognateCounts;
	
	private int _LR1ConflictsResolved;
	
	public final int LR1PassCount ()
	{
		return _LR1CognateCounts.length;
	}
	
	public final int LR1StateCount (int pass)
	{
		return _LR1CognateCounts[pass];
	}
	
	public final int LR1ConflictsResolved ()
	{
		return _LR1ConflictsResolved;
	}
	
	final void setLR1Statistics (IntDeque countList)
	{
		_LR1CognateCounts = new int[countList.elementCount()];
		
		IntEnumeration e = countList.elements();
		
		for (int i = 0; i < _LR1CognateCounts.length; ++i)
		{
			_LR1CognateCounts[i] = e.nextElement();
		}
		
		return;
	}
	
	
	// Status reporting for CFSM generation.
	//
	// _generatorStatus is an object that receives status reports and generates
	// interrupt requests.  It can be null.
	//
	// statusWork informs the client that some work has been performed, and
	// checks for an interrupt request.
	
	private GeneratorStatus _generatorStatus;
	
	void statusWork () throws InterruptedCompilerException
	{
		if (_generatorStatus != null)
		{
			_generatorStatus.statusWork ();
		}
		return;
	}
	



	// Create the CFSM for a grammar

	public LRMachine (ContextFreeGrammar grammar, int machineType, long[] productionCost,
		BinaryIntPredicate precedencePredicate, GeneratorStatus generatorStatus)
		 throws InterruptedCompilerException
	{
		super ();

		// Save parameters

		_CFG = grammar;
		_machineType = machineType;
		_productionCost = productionCost;
		_precedencePredicate = precedencePredicate;
		_generatorStatus = generatorStatus;

		// Validate the machine type

		switch (_machineType)
		{

		case LR1:
			_cognatePredicate = LRMachineLR1Predicate.singleton();
			break;

		case LALR1:
			_cognatePredicate = LRMachineLALR1Predicate.singleton();
			break;

		case PLR1:
			_cognatePredicate = LRMachinePLR1Predicate.singleton();
			break;

		default:
			throw new IllegalArgumentException ("LRMachine.LRMachine");
		}
		
		// Report work
		
		statusWork();

		// Validate the grammar

		validateGrammar ();

		// If the production cost is non-null, validate it

		if (_productionCost != null)
		{
			validateProductionCost ();
		}
		
		// Report work
		
		statusWork();

		// Create information tables

		buildDottedProductionFlyweight ();

		buildPredictionTable ();

		if (_productionCost != null)
		{
			sortPredictionTable ();
		}

		// Create the LR(0) machine states

		_states = new ObjectSet ();

		_initialState = LRMachineState.makeStates (
			this, _productionCost != null, _machineType != LR1 );

		_states.compact();

		// Create the LR(1) machine states

		_cognateArray = LRMachineCognate.makeCognates (this, _initialState, _cognatePredicate);
		
		// Report work
		
		statusWork();

		// Create the parse table

		_conflictCount = 0;
		
		_LR1ConflictsResolved = 0;

		_conflictSetTable = new ObjectDeque ();

		_parseTable = new int[_cognateArray.length][];

		for (int i = 0; i < _parseTable.length; ++i)
		{
			_parseTable[i] = _cognateArray[i].makeActionTable ();
		}

		// Create the unwinding action table

		_unwindingTable = new int[_cognateArray.length];

		for (int i = 0; i < _unwindingTable.length; ++i)
		{
			_unwindingTable[i] = _cognateArray[i].getUnwindingAction ();
		}

		// Create the unwinding parse action table

		_unwindingParseTable = new int[_cognateArray.length];

		for (int i = 0; i < _unwindingParseTable.length; ++i)
		{
			_unwindingParseTable[i] = _cognateArray[i].getUnwindingParseAction ();
		}



		return;



	}


	// Validate the grammar, and find the goal production and the
	// end-of-file symbol.  Throws an exception if an error is found.
	// Initializes _goalProduction, _eofSymbol, _minTerminalSymbol, and
	// _maxTerminalSymbol.

	private void validateGrammar ()
	{

		// Look for the goal production

		for (_goalProduction = 0; _goalProduction < _CFG.productionCount(); ++_goalProduction)
		{

			// Check if this is the goal production

			if (_CFG.productionSymbol(_goalProduction, lhs) == _CFG.goalSymbol())
			{
				break;
			}
		}

		// If we didn't find the goal production, error

		if (_goalProduction == _CFG.productionCount())
		{
			throw new IllegalArgumentException ("LRMachine.validateGrammar");
		}

		// End-of-file symbol is the last symbol of the goal production

		if (_CFG.productionLength(_goalProduction) == 1)
		{
			throw new IllegalArgumentException ("LRMachine.validateGrammar");
		}

		_eofSymbol = _CFG.productionSymbol(_goalProduction,
			_CFG.productionLength(_goalProduction) - 1);

		// The end-of-file symbol must be a terminal

		if (!_CFG.isTerminal(_eofSymbol))
		{
			throw new IllegalArgumentException ("LRMachine.validateGrammar");
		}

		// Verify that the goal symbol and end-of-file symbol appear nowhere
		// else in the grammar

		for (int production = 0; production < _CFG.productionCount(); ++production)
		{
			for (int i = 0; i < _CFG.productionLength(production); ++i)
			{

				// Check for goal symbol

				if (   (_CFG.productionSymbol(production, i) == _CFG.goalSymbol())
					&& (
						   (production != _goalProduction)
						|| (i != 0)
					   )
				   )
				{
					throw new IllegalArgumentException ("LRMachine.validateGrammar");
				}

				// Check for end-of-file symbol

				if (   (_CFG.productionSymbol(production, i) == _eofSymbol)
					&& (
						   (production != _goalProduction)
						|| (i != (_CFG.productionLength(production) - 1))
					   )
				   )
				{
					throw new IllegalArgumentException ("LRMachine.validateGrammar");
				}
			}
		}

		// Find the minimum terminal symbol

		for (_minTerminalSymbol = 0; ;++_minTerminalSymbol)
		{
			if (_CFG.isTerminal (_minTerminalSymbol))
			{
				break;
			}
		}

		// Find the maximum terminal symbol

		for (_maxTerminalSymbol = _CFG.symbolCount() - 1; ;--_maxTerminalSymbol)
		{
			if (_CFG.isTerminal (_maxTerminalSymbol))
			{
				break;
			}
		}

		return;
	}


	// Create _dottedProductionFlyweight by allocating one dotted production
	// object for each possible production and dot location.  Also sets up
	// _dottedProductionCount.

	private void buildDottedProductionFlyweight ()
	{

		// No dotted productions so far

		_dottedProductionCount = 0;

		// Allocate the top-level array

		_dottedProductionFlyweight 
			= new LRMachineDottedProduction[_CFG.productionCount()][];

		// Scan the production table

		for (int production = 0; production < _CFG.productionCount(); ++production)
		{

			// Allocate the second-level array

			_dottedProductionFlyweight[production] 
				= new LRMachineDottedProduction[_CFG.productionLength(production)];

			// Scan dot positions

			for (int dot = rhs; dot <= _CFG.productionLength(production); ++dot)
			{

				// Create the dotted production object

				_dottedProductionFlyweight[production][dot-rhs] 
					= new LRMachineDottedProduction (
					this, production, dot, _dottedProductionCount++ );
			}
		}

		return;
	}


	// Creates _predictionTable

	private void buildPredictionTable ()
	{

		// Allocate the table

		_predictionTable = new PredicateSet[_CFG.symbolCount()];

		// Create a set for each symbol

		for (int symbol = 0; symbol < _CFG.symbolCount(); ++symbol)
		{

			// Create an empty set

			_predictionTable[symbol]
				= new PredicateSet (LRMachinePredictionPredicate.singleton());

			// Create an array to indicate which predictions are currently
			// marked.  A prediction is marked if it has changed since the
			// last pass through the set, and the first symbol on the right
			// hand side is a nonterminal.  This array can be indexed by
			// productions, since no two predictions can have the same
			// production.

			boolean[] isMarked = new boolean[_CFG.productionCount()];

			int numberMarked = 0;

			for (int i = 0; i < isMarked.length; ++i)
			{
				isMarked[i] = false;
			}

			// For each production with the current symbol on the left hand side ...

			for (IntEnumeration e = _CFG.productionSet(symbol).elements ();
				e.hasMoreElements (); )
			{

				// Create a new prediction for this production.  The lookahead
				// set is empty, and the propagate flag is true.

				LRMachinePrediction prediction = new LRMachinePrediction (
					_dottedProductionFlyweight[e.nextElement()][0],
					makeLookaheadSet (),
					true );

				// Add it to the prediction table

				_predictionTable[symbol].addElement (prediction);

				// Mark the element if the rhs begins with a nonterminal

				if (prediction.dottedProduction().isSymbolAtDotNonterminal ())
				{
					isMarked[prediction.dottedProduction().production()] = true;
					++numberMarked;
				}
			}

			// Repeat until nothing is marked

			while (numberMarked != 0)
			{

				// Scan the prediction set

				for (Enumeration pe = _predictionTable[symbol].elements ();
					pe.hasMoreElements (); )
				{

					// Get the next prediction to check

					LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement ();

					// If the prediction isn't marked, skip it

					if (!isMarked[prediction.dottedProduction().production()])
					{
						continue;
					}

					// Unmark the prediction

					isMarked[prediction.dottedProduction().production()] = false;
					--numberMarked;

					// For each production whose left hand side is the first symbol
					// on the current production's right hand side ...

					for (IntEnumeration e = _CFG.productionSet (
						prediction.dottedProduction().symbolAtDot()).elements ();
						e.hasMoreElements (); )
					{

						// Create a new prediction for this production.  The lookahead
						// set is the first set of the rhs of the current prediction
						// after the dot, and the propagate flag is false.

						LRMachinePrediction newPrediction = new LRMachinePrediction (
							_dottedProductionFlyweight[e.nextElement()][0],
							prediction.dottedProduction().firstSetAfterDot (),
							false );

						// If the rhs of the current prediction after the dot
						// derives epsilon ...

						if (prediction.dottedProduction().derivesEpsilonAfterDot())
						{

							// Propagate lookaheads from the current prediction
							// into the new prediction

							newPrediction.union (prediction);
						}

						// Add new prediction to the prediction table

						LRMachinePrediction existingPrediction 
							= (LRMachinePrediction) _predictionTable[symbol].findAddElement (
							newPrediction);

						// Assume prediction has changed

						boolean predictionChanged = true;

						// If we found an existing prediction ...

						if (existingPrediction != null)
						{

							// Merge the new prediction into the existing prediction

							predictionChanged = existingPrediction.union (newPrediction);

							// Replace the new prediction with the existing prediction

							newPrediction = existingPrediction;
						}

						// Mark the new element if the rhs begins with a nonterminal, and
						// the prediction has changed, and it is not currently marked

						if (newPrediction.dottedProduction().isSymbolAtDotNonterminal()
							&& predictionChanged
							&& !isMarked[newPrediction.dottedProduction().production()] )

						{
							isMarked[newPrediction.dottedProduction().production()] = true;
							++numberMarked;
						}

					}	// end scan of productions with lhs matching first symbol on rhs

				}	// end scan of prediction set

			}	// end loop until nothing marked

			// Compact the prediction set

			_predictionTable[symbol].compact ();

			// Scan the prediction set

			for (Enumeration pe = _predictionTable[symbol].elements ();
				pe.hasMoreElements (); )
			{

				// Get the next prediction to check

				LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement();

				// Compact the set of spontaneous lookaheads

				prediction.compact();

			}	// end scan of prediction set

		}	// end loop over symbols

		return;
	}


	// Validate the table of production costs.
	// Throws an exception if an error is found.

	private void validateProductionCost ()
	{

		// Create an array which will hold the minimum cost of any production
		// with a given symbol on its left hand side

		long[] minCost = new long[_CFG.symbolCount()];

		// Initialize each element to "infinity"

		for (int i = 0; i < minCost.length; ++i)
		{
			minCost[i] = infiniteProductionCost;
		}

		// Scan the production table to get lowest cost productions

		for (int production = 0; production < _CFG.productionCount(); ++production)
		{

			// Establish minimum

			minCost[_CFG.productionSymbol(production, lhs)] = Math.min (
				minCost[_CFG.productionSymbol(production, lhs)], _productionCost[production] );
		}

		// Scan the production table to verify our production cost rule

		for (int production = 0; production < _CFG.productionCount(); ++production)
		{
			for (int i = rhs; i < _CFG.productionLength(production); ++i)
			{

				// If the current symbol is a nonterminal ...

				if (!_CFG.isTerminal(_CFG.productionSymbol(production, i)))
				{

					// ... It must have a production that is lower cost than
					// the current production

					if (minCost[_CFG.productionSymbol(production, i)]
						>= _productionCost[production])
					{
						throw new IllegalArgumentException ("LRMachine.verifyProductionCost");
					}
				}
			}
		}

		// Rule satisfied

		return;
	}


	// Sort the prediction table into the order required for error repair
	// processing.
	//
	// To sort _predictionTable[A] where A is a nonterminal, we proceed as
	// follows:  (1) mark as "eligible" all predictions which have A as their
	// lhs.  (2) Choose the lowest cost eligible production and add it to the
	// end of list;  in case of a tie, choose the lowest numbered production.
	// (3) If the newly added production has a nonterminal B as the first
	// symbol on the rhs, then mark as "eligible" all predictions which have
	// B as their lhs.  (4) Repeat steps 2 and 3 until done.
	//
	// The critical properties of the sorted prediction table for nonterminal
	// symbol A are as follows:
	//
	//		(1.1) The first prediction is the lowest cost production for A.
	//
	//		(1.2) If the first prediction has a nonterminal B as the first
	//		symbol on its rhs, then the second prediction is the lowest cost
	//		production for B.  In this case, if the second prediction has a
	//		nonterminal C as the first symbol on its rhs, then the third
	//		prediction is the lowest cost production for C.  This applies
	//		repeatedly until eventually there is a prediction that either has a
	//		a terminal as the first symbol on its rhs, or has epsilon as its
	//		rhs.  Note that each of these productions has progressively lower
	//		cost.
	//
	//		(1.3) If the lhs of a prediction is a nonterminal B different from
	//		A, then there is some other prediction earlier in the list which
	//		has B as the first symbol of its rhs.
	//
	// Given an ordered list of basis dotted productions, the ordered closure
	// is formed as follows:  Let the basis elements be BS[i] for i = 0, 1,
	// 2, etc.  Perform the following steps:  (1) Set i = 0.  (2) Add BS[i] to
	// the ordered closure.  (3) If the dot in BS[i] points to a nonterminal A,
	// then add all the elements of _predictionTable[A], in order, to the
	// ordered closure;  but do not add any elements that are already in the
	// ordered closure.  (4) Increment i, then go to step 2.  Exit when all the
	// basis elements have been processed.
	//
	// When generating successor states (as a result of a shift), the ordering
	// of dotted productions in the ordered closure is preserved.  For instance,
	// if A->m*xn is the first element of the ordered closure whose dot points
	// to the symbol x, then A->mx*n is the first basis element of the
	// successor state obtained by shifting x.
	//
	// In the ordered closure, define the "unwinding dotted production" to be 
	// the first dotted production whose dot does not point to a nonterminal.
	//
	// In state s, the "unwinding action" is defined as follows:  (1) If the
	// unwinding dotted production has its dot pointing at a nonterminal b, the
	// unwinding action is to shift b.  (2) Otherwise, the unwinding dotted
	// production has its dot at the extreme right, and the unwinding action is
	// to reduce the unwinding production.
	//
	// Starting at any state s during a parse, if the unwinding action is
	// performed repeatedly then the entire parse stack is unwound back to the
	// initial state.  Furthermore, the nonterminals that are shifted are the
	// succeeding terminals needed to reach the accept state.  In other
	// words, the unwinding actions generate a legal continuation for the
	// parse stack that leads to the accept state.  This allows the automatic
	// generation of an error repair table, because the unwinding actions
	// always provide a legal way to continue the parse.
	//
	// Reference:  Fischer and LeBlanc, pages 710-719.  However, they do not
	// prove that the unwinding actions always reach the initial state (indeed,
	// it appears that their weaker constraint on the cost function allows an
	// infinite loop in the unwinding actions).  Also, they do not address the
	// problem of showing that the symbols generated by shift actions actually
	// belong to the lookahead sets of the productions reduced by reduce
	// actions.  Showing all this seems to require a rather complicated double
	// induction, and so we outline a proof here.
	//
	// The ordered closure has the following properties.  Let OC[i] denote the
	// i-th dotted production in the ordered closure, i = 0, 1, 2, etc.
	//
	//		(2.1) OC[0] is the first basis set element.
	//
	//		(2.2) Suppose OC[0] does not have its dot pointing at a nonterminal.
	//		Then OC[0] is the unwinding dotted production, and we can write
	//			OC[0] = A->m*n
	//		where:  (i) m is a string;  and (ii) n is either the empty string
	//		or a string whose first symbol is a terminal.
	//
	//		(2.3) Suppose OC[0] has its dot pointing at a nonterminal.  Then the
	//		unwinding dotted production is OC[k] for some k > 0.  In this case,
	//		we can write
	//			OC[0] = A->m*B[0]g[0]
	//			OC[i] = B[i-1]->*B[i]g[i]   for 0 < i < k
	//			OC[k] = B[k-1]->*n
	//		where:  (i) m is a string;  (ii) B[0]...B[k-1] are distinct
	//		nonterminals;  (iii) g[0]...g[k-1] are strings;  and (iv) n is
	//		either the empty string or a string whose first symbol is a terminal.
	//		Furthermore, OC[j] is lower cost than OC[j-1] for 1 <= j <= k.
	//
	//		(2.4) Suppose that the dot in OC[i] is at the extreme left position
	//		(i.e., OC[i] is not a basis element).  Let A be the lhs of OC[i].
	//		Then there is some j < i such that the dot in OC[j] points to A.
	//
	// Property 2.1 is true by construction.  Property 2.2 follows directly from
	// the definition of the unwinding dotted production.  Property 2.3 follows
	// from the fact that if the dot in the first basis element points to a
	// nonterminal A, then the entire contents of _predictionTable[A] is inserted
	// into the ordered closure immediately after the first basis element.  OC[1]
	// to OC[k] are the initial elements of the prediction set described in
	// property 1.2.  Property 2.4 follows from property 1.3 plus the fact that
	// prediction sets are added after their corresponding basis elements.
	//
	// We next prove some properties of unwinding actions.
	//
	//		(3.1) Let s be a state, and let A->m*h be its first basis element.
	//		Suppose we perform the unwinding actions beginning at state s.
	//		Then A->mh is eventually reduced, which pops s off the parse stack.
	//		If one or more shift actions occur before A->mh is reduced, then the
	//		first terminal shifted is an element of first(h).  If no shift
	//		actions occur before A->mh is reduced, then h derives epsilon.
	//
	// We prove this by a double induction, first on the cost of A->mh, then on
	// the length of h.  We call h the "tail" of the dotted production A->m*h.
	// Let FB[t] denote the first basis element of state t.
	//
	// Consider first the case where h has length zero.  Then the unwinding
	// action on s is to reduce A->mh, and h derives epsilon.  So 3.1 is true in
	// this case.
	//
	// To perform the induction, assume 3.1 holds for any state t such that either
	// (i) FB[t] is lower cost than A->mh;  or (ii) FB[t] is the same cost as
	// A->mh, and the tail of FB[t] is shorter than h.  There are two cases to
	// consider.
	//
	// Case 1:  Suppose h begins with a terminal symbol b.  Then the unwinding
	// action on s is to shift b.  In this case, b is an element of first(h).
	// We can rewrite A->m*h as A->m*bn.  After shifting b, we reach a successor
	// state t whose first basis element is A->mb*n (because the ordering of
	// dotted productions is preserved in successor states).  Since n is shorter
	// than h, by inductive hypothesis A->mbn = A->mh is eventually reduced.
	//
	// Case 2:  Suppose h begins with a nonterminal.  Then s has the structure
	// described in property 2.3.  In the following, we use the notation of 2.3.
	// We show that the unwinding actions first reduce OC[k], then reduce OC[k-1],
	// and so on until OC[0] is reduced.
	//
	// First we show that OC[k] is reduced.  If n is the empty string, then the
	// unwinding action on s is to reduce OC[k] immediately.  Otherwise, we can
	// write n = cf for a terminal c, and the unwinding action on s is to shift
	// c.  In the successor state, B[k-1]->c*n is the first basis element, and
	// it is lower cost than A->mh.  So by inductive hypothesis, OC[k] is reduced.
	//
	// For 0 < i < k, we assume OC[i+1] has been reduced and show that OC[i] is
	// reduced.  When OC[i+1] is reduced, the parse stack is popped down to s and
	// then B[i] is shifted.  In the successor state, B[i-1]->B[i]*g[i] is the
	// first basis element, and it is lower cost than A->mh.  So by inductive
	// hypothesis, OC[i] is reduced.
	//
	// Finally, we assume that OC[1] has been reduced and show that OC[0] = A->m*h
	// is reduced.  When OC[1] is reduced, the parse stack is popped down to s and
	// then B[0] is shifted.  In the successor state, A->mB[0]*g[0] is the first
	// basis element.  It has the same cost as A->mh, but g[0] is shorter than h.
	// So by inductive hypothesis, OC[0] is reduced.
	//
	// Suppose that no shift actions occured during the reduction of A->mh.  Then:
	// (i) n is the empty string;  (ii) for 0 < i < k, B[i-1]->B[i]*g[i] was
	// reduced with no shift actions, which by inductive hypothesis implies that
	// g[i] derives epsilon;  and (iii) A->mB[0]*g[0] was reduced without shift
	// actions, which implies that g[0] derives epsilon.  Property (i) implies
	// that B[k-1] derives epsilon, while property (ii) implies that if B[i]
	// derives epsilon then B[i-1] derives epsilon.  So B[0] derives epsilon,
	// and then property (iii) implies that h = B[0]g[0] derives epsilon.
	//
	// Suppose that a shift action occurred during the reduction of A->mh.  Let
	// b be the first symbol shifted, and assume that b was shifted during the
	// reduction of OC[j] (in other words, assume OC[j+1]...OC[k] were reduced
	// without shift actions).  Notice that by construction, first(h) contains
	// first(B[0]), which contains first(B[1]), and so on.  We consider three
	// cases.  (i) Suppose j = k.  Then b is the first symbol of n, and so b is
	// an element of first(B[k-1]).  Therefore, b is an element of first(h).
	// (ii) Suppose 0 < j < k.  By the reasoning of the previous paragraph,
	// B[j] derives epsilon.  A shift of b occured during the reduction of
	// B[j-1]->B[j]*g[j], which by inductive hypothesis implies that b is in
	// first(g[j]).  Therefore, b is an element of first(B[j]), and so b is
	// an element of first(h).  (iii) Suppose j = 0.  By the reasoning of the
	// previous paragraph, B[0] derives epsilon.  A shift of b occurred during
	// the reduction of A->mB[0]*g[0], which by inductive hypothesis implies
	// that b is in first(g[0]).  Therefore, b is an element of first(h).
	//
	//		(3.2) Suppose that state s is in the parse stack (not necessarily
	//		at the top of the parse stack).  Suppose that we perform unwinding
	//		actions beginning at the top of the parse stack.  Suppose further
	//		that the unwinding actions eventually reduce a dotted production
	//		C->e*f in state s (not necessarily a basis element).  Let b be the
	//		next terminal shifted after the reduction of C->e*f.  Then b is an
	//		element of the lookahead set of C->e*f in state s.
	//
	// We proceed by induction on the number of unwinding actions between the
	// reduction of C->e*f and the next shift action.  When C->e*f is reduced,
	// the parse stack is popped down to the state t which contains the closure
	// production C->*ef.  (If C->e*f is a closure production, then t is the same
	// as s;  if C->e*f is a basis production then t is lower on the parse stack
	// than s.)  Then symbol C is shifted, yielding a successor state u.  Let
	// A->m*Ch be the first dotted production in t whose dot points at C.  Then
	// A->mC*h is the first basis element of state u.
	//
	// By 3.1, when we perform unwinding actions starting at state u, A->mC*h
	// is eventually reduced (which means that A->m*Ch is reduced).
	//
	// Suppose that a shift operation occurs during the reduction of A->mC*h.
	// By 3.1, b is an element of first(h).  Therefore, in state t, b is a
	// spontaneous lookahead generated by A->m*Ch.  So b is in the lookahead set
	// of C->*ef in state t.  Since lookaheads are propagated to successor
	// states, b is in the lookahead set of C->e*f in state s.
	//
	// Suppose that no shift operation occurs during the reduction of A->mC*h.
	// By 3.1, h derives epsilon.  The number of unwinding actions between the
	// reduction of A->m*Ch and the next shift is less than the number of
	// unwinding actions between the reduction of C->e*f and the next shift.
	// So by inductive hypothesis, b is an element of the lookahead set of
	// A->m*Ch in state t.  Since h derives epsilon, b is propagated into the
	// lookahead set of C->*ef in state t.  Since lookaheads are propagated to
	// successor states, b is in the lookahead set of C->e*f in state s.
	//
	//		(3.3) Suppose that unwinding actions are performed beginning at the
	//		top of the parse stack.  Then the terminals shifted are a legal
	//		continuation to the parse, ending with the end-of-file symbol.  The
	//		final action is to shift the end-of-file symbol.
	//
	// By 3.2, whenever a reduce action occurs, the next continuation symbol is
	// in the reduced production's lookahead set.  This fact, together with the
	// fact that unwinding actions emulate a parser, establish that the
	// continuation sequence is legal.
	//
	// To establish that we reach the end-of-file symbol, we need to show that
	// all states are eventually popped off the parse stack.  Let s be the
	// initial top state.  By 3.1, the first basis element of s is eventually
	// reduced, at which point s is popped off the stack.
	//
	// Whenever the top state is popped off the parse stack, we pop down to some
	// state t, then shift a nonterminal A, and then continue unwinding actions
	// in the successor state.  If we can show that t itself is eventually popped
	// off the parse stack, we'll be done, because then we can pop the entire
	// parse stack by repeatedly performing this operation.
	//
	// Let FE[A] denote the first element in t whose dot points at A.  After
	// shifting A, the shifted FE[A] becomes the first basis element of the
	// successor state.  By 3.1, FE[A] is eventually reduced.  If FE[A] is a
	// basis element of t, we're done.  If not, let B be the lhs of FE[A].
	// Then, when FE[A] is reduced, B is shifted, and FE[B] becomes the first
	// basis element of the successor state.  By 3.1, FE[B] is eventually
	// reduced.  If FE[B] is a basis element of t, we're done.  If not, we let
	// C be the lhs of FE[C], and repeat the process.
	//
	// All we need to do is establish that the sequence FE[A], FE[B], FE[C],
	// etc., eventually reaches a basis element.  But by property 2.4, FE[B]
	// precedes FE[A], and FE[C] precedes FE[B], and so on.  But t has
	// finitely many elements, and the first element of t is a basis element.
	// So the sequence must eventually reach a basis element.

	private void sortPredictionTable ()
	{

		// Scan every possible symbol

		for (int symbol = 0; symbol < _CFG.symbolCount(); ++symbol)
		{

			// If the existing set has fewer than 2 elements, nothing to do

			if (_predictionTable[symbol].elementCount() < 2)
			{
				continue;
			}
		
			// Create an empty set to hold the sorted prediction table

			PredicateSet sortedSet = new PredicateSet (
				LRMachinePredictionPredicate.singleton(),
				_predictionTable[symbol].elementCount() );

			// Create a table of eligible left hand sides

			boolean[] eligibleSymbols = new boolean[_CFG.symbolCount()];

			for (int i = 0; i < eligibleSymbols.length; ++i)
			{
				eligibleSymbols[i] = false;
			}

			// Mark the current symbol eligible

			eligibleSymbols[symbol] = true;

			// Repeat until the original set is empty

			while (!_predictionTable[symbol].isEmpty())
			{

				// No best prediction so far

				LRMachinePrediction bestPrediction = null;

				int bestProduction = -1;

				// Scan the remaining prediction set

				for (Enumeration pe = _predictionTable[symbol].elements();
					pe.hasMoreElements() ; )
				{

					// Get the next prediction to check

					LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement();

					int production = prediction.dottedProduction().production();

					// Skip if this prediction is not eligible

					if (!eligibleSymbols[_CFG.productionSymbol (production, lhs)])
					{
						continue;
					}

					// If this is a new best prediction, save it

					if (   (bestPrediction == null)
						|| (_productionCost[production] < _productionCost[bestProduction])
						|| (
							   (_productionCost[production] == _productionCost[bestProduction])
							&& (production < bestProduction)
						   )
					   )
					{
						bestPrediction = prediction;
						bestProduction = production;
					}
				}

				// Remove the best prediction from the old set
	
				_predictionTable[symbol].removeElement (bestPrediction);

				// Add the best prediction to the new set

				sortedSet.addElement (bestPrediction);

				// Mark the first symbol on the right hand side of the best
				// prediction as eligible

				if (_CFG.productionLength(bestProduction) > rhs)
				{
					eligibleSymbols[_CFG.productionSymbol(bestProduction, rhs)] = true;
				}

			}	// end loop until existing set empty

			// Establish the sorted set as the prediction set

			sortedSet.compact();

			_predictionTable[symbol] = sortedSet;

		}	// end scan of symbols

		// All prediction sets are sorted

		return;
	}


	// Create an empty lookahead set.

	LRMachineLookaheadSet makeLookaheadSet ()
	{
		return LRMachineLookaheadSet.makeSet (
			_minTerminalSymbol, _maxTerminalSymbol );
	}


	// Create a lookahead set and initialize its contents.

	LRMachineLookaheadSet makeLookaheadSet (ConstIntSet initialContents)
	{
		return LRMachineLookaheadSet.makeSet (
			_minTerminalSymbol, _maxTerminalSymbol, initialContents );
	}


	// Attempts to resolve parser action conflicts.
	//
	// On entry, cognateNumber and symbol identify the specific LR(1) state
	// and terminal symbol where a conflict was found, and actionSet is the
	// set of two or more actions that are possible.  Each action is
	// represented by an integer as follows:  Reduction of production p is
	// represented by p;  shift of symbol s is represented by s+PC where
	// PC is the number of productions in the CFG.
	//
	// If the conflict is successfully resolved, the function returns the
	// element of actionSet that is the selected action.  If the conflict
	// cannot be resolved, the function returns a negative value, which is
	// placed unchanged into the parser action table.  If information about
	// unresolved conflicts is to be logged, it is the responsibility of this
	// function to do it.
	//
	// This implementation uses _precedencePredicate to resolve conflicts.  If
	// there is a unique element of actionSet which has precedence over every
	// other element of actionSet, then that element is selected.  Otherwise,
	// the function counts an unresolved conflict and returns -1.

	int resolveConflict (int cognateNumber, int symbol, IntSet actionSet)
	{

		// If the user supplied a precedence predicate ...

		if (_precedencePredicate != null)
		{

			// Best action so far, or -1 if none

			int bestAction = -1;

			// Search for best action

		outerLoop:
			for (IntEnumeration eo = actionSet.elements(); eo.hasMoreElements(); )
			{

				// Get candidate action

				int candidateAction = eo.nextElement();

				// See if the candidate action has precedence over every other action

				for (IntEnumeration ei = actionSet.elements(); ei.hasMoreElements(); )
				{

					// Get comparison action

					int comparisonAction = ei.nextElement();

					// If candidate action doesn't have precedence over comparison
					// action, go on to the next candidate

					if (candidateAction != comparisonAction)
					{
						if (!_precedencePredicate.value (candidateAction, comparisonAction))
						{
							continue outerLoop;
						}
					}
				}

				// If there is already a best action, error

				if (bestAction != -1)
				{
					bestAction = -1;
					break outerLoop;
				}

				// Our candidate is the best action

				bestAction = candidateAction;
			}

			// If we found a best action, return it

			if (bestAction != -1)
			{
				
				// Adjust statistic
				
				++_LR1ConflictsResolved;
				
				// Return the best action
				
				return bestAction;
			}

		}

		// Cannot resolve ambiguity

		++_conflictCount;

		// Create a SmallIntSet containing the conflict set

		SmallIntSet cflSet = new SmallIntSet ();

		for (IntEnumeration eo = actionSet.elements(); eo.hasMoreElements(); )
		{

			// Get candidate action

			int candidateAction = eo.nextElement();

			// All shifts are encoded as PC

			if (candidateAction >= _CFG.productionCount())
			{
				candidateAction = _CFG.productionCount();
			}

			// Add to set

			cflSet.addElement (candidateAction);
		}

		// Find the conflict set that equals ours, if one exists

		int cflNumber =
			_conflictSetTable.firstIndexOf (UnaryPredicateFactory.equality (cflSet));

		// If no existing conflict set equals ours ...

		if (cflNumber < 0)
		{
			
			// We become the last element

			cflNumber = _conflictSetTable.elementCount();

			// Add our set to the table

			_conflictSetTable.pushLast (cflSet);
		}

		// Return action code for this conflict set

		return -1 - cflNumber;
	}






}



// LRMachineLookaheadSet represents a set of terminal symbols.  As the name
// suggests, its intended use is as a set of lookahead symbols.
//
// There are a large number of lookahead sets in an LR machine, and so it is
// expected that the implementation chosen for these sets may have a
// significant impact on time and space requirements for the LR machine.
// Therefore, this class exists to make the implementation details
// transparent to the rest of the LR machine code.  Typically, this set is
// a thin wrapper around a SmallIntSet or IntSet.
//
// Lookahead sets are used primarily in set-wide operations like union and
// isDisjoint.  Individual elements are examined only after the LR machine
// in constructed, to create the parse tables.
//
// The functions required are:
//
//		boolean union (ConstLRMachineLookaheadSet other);
//
//		boolean intersection (ConstLRMachineLookaheadSet other);
//
//		boolean isDisjoint (ConstLRMachineLookaheadSet other);
//
//		boolean compact ();
//
//		boolean equals (Object other);
//
//		Object clone ();
//
//		IntEnumeration elements ();

final class LRMachineLookaheadSet extends SmallIntSet implements LRMachineConstLookaheadSet
{

	// Private constructor to create a SmallIntSet

	private LRMachineLookaheadSet (int minElement, int maxElement)
	{
		super (minElement, maxElement);
	}


	// Function to create an empty lookahead set.  The minimum and
	// maximum symbol values are advisory.

	static LRMachineLookaheadSet makeSet (int minSymbol, int maxSymbol)
	{
		return new LRMachineLookaheadSet (minSymbol, maxSymbol);
	}


	// Function to create a lookahead set and initialize its contents with the
	// elements of an IntSet.  The minimum and maximum symbol values are
	// advisory (and need not reflect the contents of the IntSet).

	static LRMachineLookaheadSet makeSet (int minSymbol, int maxSymbol,
		ConstIntSet initialValues)
	{
		LRMachineLookaheadSet resultSet = new LRMachineLookaheadSet (minSymbol, maxSymbol);
		resultSet.union (initialValues);
		return resultSet;
	}


}



// LRMachineConstLookaheadSet is an interface to the functions of
// LRMachineLookaheadSet that do not modify the set.
//
// The functions required are:
//
//		boolean isDisjoint (LRMachineLookaheadSet other);
//
//		boolean equals (Object other);
//
//		Object clone ();
//
//		IntEnumeration elements ();

interface LRMachineConstLookaheadSet extends ConstSmallIntSet
{

    public Object clone ();

}




// LRMachineDottedProduction represents a dotted production, which is also
// an LR(0) configuration.  A dotted production consists of a production plus
// a "dot" which is a cursor into the right hand side.  The dot can point at
// any symbol in the right hand side, or it can point past the end of the
// right hand side.
//
// LRMachineDottedProduction objects are implemented as flyweights, therefore
// they may be compared by simply comparing their references.
//
// This class attempts to encapsulate all grammar operations, so other classes
// do not have to manipulate the grammar directly.

final class LRMachineDottedProduction
{

	// The LRMachine that this dotted production belongs to.

	private LRMachine _machine;

	final LRMachine machine ()
	{
		return _machine;
	}


	// The production number.

	private int _production;

	final int production ()
	{
		return _production;
	}


	// The dot location.  It can range from rhs (= 1) when the dot points at
	// the first symbol, to machine.CFG().productionLength(production)
	// when the dot points one past the end of the right hand side.

	private int _dot;


	// The index number.  Each dotted production is assigned a unique index
	// number ranging from 0 to one less than the number of dotted productions.
	// This is useful for creating an array indexed by dotted productions.

	private int _index;

	final int index ()
	{
		return _index;
	}


	// Import constants for accessing production table

	private static final int lhs = ContextFreeGrammar.lhs;
	private static final int rhs = ContextFreeGrammar.rhs;


	// This constructor should be used only by LRMachine to create the
	// flyweight dotted production objects.

	LRMachineDottedProduction (LRMachine machine, int production, int dot, int index)
	{
		super ();

		// Save the arguments

		_machine = machine;
		_production = production;
		_dot = dot;
		_index = index;

		return;
	}


	// Returns the symbol pointed to by the dot.  Throws an exception if the
	// dot is past the end of the right hand side.

	public int symbolAtDot ()
	{

		// If at end, throw exception

		if (_dot == _machine.CFG().productionLength(_production))
		{
			throw new IllegalArgumentException ("LRMachineDottedProduction.symbolAtDot");
		}

		// Get the symbol at the dot

		return _machine.CFG().productionSymbol(_production, _dot);
	}


	// Returns true if the symbol pointed to by the dot is a nonterminal.
	// Returns false if the dot points to a terminal, or if the dot points
	// past the end of the right hand side.

	public boolean isSymbolAtDotNonterminal ()
	{
		if (_dot == _machine.CFG().productionLength(_production))
		{
			return false;
		}

		return !_machine.CFG().isTerminal (_machine.CFG().productionSymbol(_production, _dot));
	}


	// Returns the dotted production object that results from shifting the dot
	// one position to the right.  Throws an exception if the dot is already
	// past the right end.

	public LRMachineDottedProduction rightShiftDot ()
	{

		// If at end, throw exception

		if (_dot == _machine.CFG().productionLength(_production))
		{
			throw new IllegalArgumentException ("LRMachineDottedProduction.rightShiftDot");
		}

		// Get the appropriate object from the flyweight table

		return _machine.dottedProductionFlyweight (_production, (_dot+1)-rhs);
	}


	// Returns true if the dot is past the right end.

	public boolean isDotAtEnd ()
	{
		return (_dot == _machine.CFG().productionLength(_production));
	}


	// This function computes the set of all terminal symbols t such that t is
	// the first symbol of a string that can be derived from the portion of
	// the right hand side that lies after the symbol pointed to by the dot.
	//
	// For example, given A->W*XYZ this function returns first(YZ).
	//
	// A new LRMachineLookaheadSet is created for each invocation of this function.

	public LRMachineLookaheadSet firstSetAfterDot ()
	{

		// If at end, throw exception

		if (_dot == _machine.CFG().productionLength(_production))
		{
			throw new IllegalArgumentException ("LRMachineDottedProduction.firstSetAfterDot");
		}

		// Get first set of trailing part of right hand side

		return _machine.makeLookaheadSet (_machine.CFG().firstSet (_production, _dot + 1));
	}


	// This function returns true if the empty string can be derived from the
	// portion of the right hand side that lies after the symbol pointed to by
	// the dot.
	//
	// For example, given A->W*XYZ this function returns true if the empty
	// string can be derived from YZ.

	public boolean derivesEpsilonAfterDot ()
	{

		// If at end, throw exception

		if (_dot == _machine.CFG().productionLength(_production))
		{
			throw new IllegalArgumentException ("LRMachineDottedProduction.derivesEpsilonAfterDot");
		}

		// Determine if trailing part of right hand side derives epsilon

		return _machine.CFG().derivesEpsilon (_production, _dot + 1);
	}


	// This function returns true if the empty string can be derived from the
	// portion of the right hand side that begins with the symbol pointed to by
	// the dot.
	//
	// For example, given A->W*XYZ this function returns true if the empty
	// string can be derived from XYZ.
	//
	// If the dot is at the end of the right hand side, this function returns
	// true.

	public boolean derivesEpsilonAtAndAfterDot ()
	{

		// Determine if trailing part of right hand side derives epsilon

		return _machine.CFG().derivesEpsilon (_production, _dot);
	}


	// This function computes the set of all terminal symbols t such that t
	// appears immediately after A in some string that can be derived from
	// the goal symbol, where A is the left hand side of this production.
	//
	// Note that this function ignores the dot location.
	//
	// For example, given A->W*XYZ this function returns follow(A).

	public ConstIntSet followSetOfLHS ()
	{

		// Get follow set of the left hand side

		return _machine.CFG().followSet (_machine.CFG().productionSymbol(_production, lhs));
	}


	// Converts the dotted production to a string.  Overrides the toString
	// method of class Object.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Scan the production

		for (int i = lhs; i < _machine.CFG().productionLength(_production); ++i)
		{

			// If not the first symbol, append a space

			if (i != lhs)
			{
				buf.append (" ");
			}

			// Append the symbol

			buf.append (_machine.CFG().symbolName (
				_machine.CFG().productionSymbol(_production, i) ));

			// If we just wrote the left hand side, append an arrow

			if (i == lhs)
			{
				buf.append (" ->");
			}

			// If we're at the dot position, append an underscore

			if (i == (_dot-1))
			{
				buf.append (" _");
			}
		}

		// Return resulting string

		return buf.toString ();
	}


}




// LRMachinePrediction represents a dotted production that is predicted by a
// dotted production of the form B->*A for a nonterminal symbol A.
//
// Each prediction contains three pieces of information:  (1) A dotted
// production, with the dot at the beginning of the right hand side.
// (2) A set of terminal symbols which are the spontaneous lookaheads for
// this prediction.  (3) A flag which indicates if lookaheads from the
// original dotted production B->*A should be propagated to this prediction.
//
// Given a nonterminal A, the set of predictions for A can be calculated as
// follows:
//
// (1) Create an empty set to hold the predictions.
//
// (2) For each production of the form A->m (where m is a string of symbols)
// add a prediction containing the dotted production A->*m, no spontaneous
// lookaheads, and a propagate flag of true.
//
// (3) Examine each prediction in the set which contains a dotted production
// of the form D->*Cn where C is a nonterminal and n is a string of symbols.
// Let L be the corresponding set of spontaneous lookaheads, and let P be the
// corresponding propagate flag.  For each production of the form C->k (where
// k is a string of symbols), do the following:
//
//	(3a) Find an existing prediction containing C->*k.  If none exists,
//	create a new prediction containing C->*k, no spontaneous lookaheads, and a
//	propagate flag of false.
//
//	(3b) "Union" firstSet(n) into the spontaneous lookaheads.  In addition, if
//	derivesEpsilon(n) is true then "union" L into the spontaneous lookaheads
//	and "or" F into the propagate flag.
//
// (4) Repeat step 3 until the set stops changing.

final class LRMachinePrediction
{

	// The dotted production for this prediction.

	private LRMachineDottedProduction _dottedProduction;

	LRMachineDottedProduction dottedProduction ()
	{
		return _dottedProduction;
	}


	// The set of spontaneous lookaheads for this prediction.

	private LRMachineLookaheadSet _spontaneousLookaheads;

	LRMachineConstLookaheadSet spontaneousLookaheads ()
	{
		return _spontaneousLookaheads;		// Note set is not cloned
	}


	// The propagate flag for this prediction.

	private boolean _propagateFlag;

	boolean propagateFlag ()
	{
		return _propagateFlag;
	}


	// Create a new prediction with the specified dotted production,
	// spontaneous lookaheads, and propagate flag.
	//
	// This function retains a reference to the lookahead set.

	LRMachinePrediction (LRMachineDottedProduction dottedProduction,
		LRMachineLookaheadSet spontaneousLookaheads, boolean propagateFlag)
	{
		super ();

		// Save the arguments

		_dottedProduction = dottedProduction;
		_spontaneousLookaheads = spontaneousLookaheads;
		_propagateFlag = propagateFlag;

		return;
	}


	// Form the union of this prediction with another prediction.  Returns true
	// if this prediction has changed.

	boolean union (LRMachinePrediction other)
	{

		// Form the union of the lookahead sets

		boolean result = this._spontaneousLookaheads.union (other._spontaneousLookaheads);

		// Form the logical or of the propagate flags

		if (other._propagateFlag && !(this._propagateFlag))
		{
			this._propagateFlag = true;
			result = true;
		}

		return result;
	}


	// Compact the storage used by this prediction.

	void compact ()
	{

		// Compact the set of spontaneous lookaheads 

		_spontaneousLookaheads.compact();

		return;
	}


	// Converts the prediction to a string.  Overrides the toString method of
	// class Object.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left bracket

		buf.append ("[");

		// Append the dotted production

		buf.append (_dottedProduction.toString());

		// Append a space

		buf.append (" ");

		// Append the lookahead set

		buf.append (_dottedProduction.machine().CFG()
			.symbolName(_spontaneousLookaheads).toString());

		// Append a space and the propagate flag

		buf.append (" " + _propagateFlag);

		// End with a right bracket

		buf.append ("]");

		// Return resulting string

		return buf.toString ();
	}


}




// Binary predicate for detemining if two predictions can be merged.  This is
// true if two predictions refer to the same dotted production.

final class LRMachinePredictionPredicate extends BinaryPredicate
{

	// Implement this class as a singleton

	private static final LRMachinePredictionPredicate _singleton
		= new LRMachinePredictionPredicate ();

	static final LRMachinePredictionPredicate singleton ()
	{
		return _singleton;
	}

	private LRMachinePredictionPredicate ()
	{
		super ();
		return;
	}


	// Two predictions can be merged if they have the same dotted production

	public boolean value (Object object1, Object object2)
	{

		// Get the prediction objects

		LRMachinePrediction prediction1 = (LRMachinePrediction) object1;

		LRMachinePrediction prediction2 = (LRMachinePrediction) object2;

		// Compare the dotted productions

		return (prediction1.dottedProduction() == prediction2.dottedProduction());
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof LRMachinePredictionPredicate)))
		{
			return false;
		}

		return true;
	}


}




// LRMachineState represents a state of the LR(0) configuration final state
// machine.  It consists of a set of dotted productions;  no two states
// may contain the same set of dotted productions.

final class LRMachineState
{

	// The LRMachine that this state belongs to.

	private LRMachine _machine;


	// The symbol that is shifted to enter this state.
	//
	// As a special case, for the initial state this is the goal symbol (which
	// can never be shifted because it does not appear on the right hand side
	// of any production).

	private int _shiftSymbol;


	// The basis set of dotted productions for this state.  The basis set
	// is derived from predecessor states by shifting shiftSymbol.  Each
	// dotted production in the basis set has shiftSymbol located immediately
	// before the dot (in particular, the dot cannot be at the start of the
	// production).
	//
	// The basis set is stored as an array (rather than an ObjectSet) 
	// because each entry corresponds to entries in LRMachineCognate
	// with the same index.  Also, the ordering of basis set elements is
	// significant if an LM error repair table is being generated.
	//
	// As a special case, in the initial state the basis set consists of
	// the goal production, with the dot at the start of the production.

	private LRMachineDottedProduction[] _basisSet;


	// The set of successor states.  Note that it is not necessary to store
	// the shift symbols in this table, because they can be obtained from the
	// shiftSymbol field in the successor states.

	private LRMachineState[] _successorStates;


	// The dotted production that is used to unwind this state during error
	// repair.  In the ordered closure of the basis set, the unwinding dotted
	// production is the first dotted production whose dot does not point to
	// a nonterminal.  If the dot is at the end, the unwinding operation is to
	// reduce on this production;  if the dot points at a terminal, the
	// unwinding operation is to shift the terminal.
	//
	// If the error repair table is not being generated, this value has no
	// significance.

	private LRMachineDottedProduction _unwindingDottedProduction;


	// The set of cognates associated with this state.  These are the LR(1)
	// states that have this LR(0) state as their core state.

	private ObjectSet _cognates;

	final boolean addCognate (LRMachineCognate cognate)
	{
		return _cognates.addElement (cognate);
	}

	final boolean removeCognate (LRMachineCognate cognate)
	{
		return _cognates.removeElement (cognate);
	}

	final Enumeration enumerateCognates ()
	{
		return _cognates.elements();
	}

	final boolean compactCognates ()
	{
		return _cognates.compact();
	}


	// This is a set of lookaheads that is used to discriminate among cognates.
	// When considering whether two cognates can be merged, we calculate the
	// intersection of each cognate's lookahead set with mergeCheckSet.  If the
	// intersections are unequal, the cognates are not merged.
	//
	// This can be null if no merge checking is desired.

	private LRMachineLookaheadSet[] _mergeCheckSet;


	// A private constructor is used, so that this class can control the
	// the creation of its own objects.
	//
	// Note:  It is guaranteed that basis set elements appear in the _basisSet
	// array in the same order as they are listed in the basisSet set.

	private LRMachineState (LRMachine machine, int shiftSymbol, ObjectSet basisSet)
	{
		super ();

		// Save the parameters

		_machine = machine;
		_shiftSymbol = shiftSymbol;

		// Convert the basis set into an array

		_basisSet = new LRMachineDottedProduction[basisSet.elementCount()];

		Enumeration e = basisSet.elements();

		for (int i = 0; i < _basisSet.length; ++i)
		{
			_basisSet[i] = (LRMachineDottedProduction) e.nextElement();
		}

		// Initialize set of cognates to empty set

		_cognates = new ObjectSet ();

		// No merge checking yet

		_mergeCheckSet = null;

		// Just to establish a definite initial value

		_successorStates = null;
		_unwindingDottedProduction = null;

		// Add to the machine's set of states

		_machine.addState (this);

		return;
	}


	// Creates all the states for this machine.  Before calling this
	// function, machine._states must be initialized to an empty set.
	// This function returns the initial state.
	//
	// If preserveOrder is true, then the ordering of basis set elements is
	// preserved in successor states.  This may increase the number of states
	// generated.  Refer to buildSuccessorStates for more details.
	//
	// If useMergeCheck is true, then this function builds a merge check set
	// for each state.  This prevents two cognates from being merged if one
	// cognate induces a shift-reduce conflict but the other cognate does not,
	// or if the two cognates induce different shift-reduce conflicts.  A
	// cognate is considered to "induce" a shift-reduce conflict if any of its
	// lookaheads create a shift-reduce conflict in the cognate's core state or
	// any of the core state's (direct or indirect) successor states.
	//
	// The idea of merge checking is to make it safer to use ambiguous grammars.
	// With merge checking, the parser does not enter a state with a conflict
	// unless it is forced to do so.  Note that this is an original extension
	// to Pager's algorithm.  Also note that no similar mechanism is needed for
	// reduce-reduce conflicts, since Pager's algorithm by definition is the
	// mechanism for avoiding unnecessary reduce-reduce conflicts.

	static LRMachineState makeStates (LRMachine machine, boolean preserveOrder,
		boolean useMergeCheck) throws InterruptedCompilerException
	{
		
		// Initialize statistics
		
		int stateCount = 0;
		int passCount = 0;

		// Create a set containing the goal production

		ObjectSet initialBasis = new ObjectSet ();

		initialBasis.addElement (
			machine.dottedProductionFlyweight (machine.goalProduction(), 0) );

		// Create the initial state, and add it to the set of states

		LRMachineState initialState
			= new LRMachineState (machine, machine.CFG().goalSymbol(), initialBasis);

		// Scan the set of states.  This code relies on the fact that an
		// ObjectSet enumerator returns elements added during the enumeration.

		for (Enumeration e = machine.enumerateStates(); e.hasMoreElements(); )
		{
			
			// Report work
			
			machine.statusWork();

			// Tell each state to build its table of successor states

			((LRMachineState) e.nextElement()).buildSuccessorStates(preserveOrder);
			
			// Count this state for statistics
			
			++stateCount;
		}

		// If we want merge checking ...

		if (useMergeCheck)
		{

			// Scan the set of states and initialize the merge check sets

			for (Enumeration e = machine.enumerateStates(); e.hasMoreElements(); )
			{

				// Tell each state to initialize its merge check set

				((LRMachineState) e.nextElement()).initializeMergeCheckSet();
			}

			// Loop until nothing changes

			for (boolean isChanged = true; isChanged; )
			{

				// Nothing changed yet

				isChanged = false;
				
				// Counter to report work for every 20 states processed
				
				int workdiv = 0;

				// Scan the set of states and propagate the merge check sets

				for (Enumeration e = machine.enumerateStates(); e.hasMoreElements(); )
				{
				
					// Report work
				
					if ((workdiv++) % 20 == 0)
					{
						machine.statusWork();
					}

					// Tell each state to propagate its merge check set

					isChanged |= ((LRMachineState) e.nextElement()).propagateMergeCheckSet();
				}
				
				// Count this pass for statistics
				
				++passCount;
			}
		}
		
		// Return statistics
		
		machine.setLR0Statistics (stateCount, passCount);

		// Return the initial state

		return initialState;
	}


	// This function calculates the basis sets for each possible successor
	// state of this state.  Then it searches the set of states, looking for
	// a match to each successor.  Any unmatched basis sets are used to
	// construct new states, which are added to the set of states.  The results
	// are stored in _successorStates.
	//
	// If preserveOrder is true, then the ordering of each successor's basis
	// set is guaranteed to be the same as the order in which dotted productions
	// are generated in this state.  (The order is as follows:  For each b 
	// beginning with b = 0, add _basisSet[b] shifted, then add all shifted 
	// predictions for the symbol at the dot in _basisSet[b] in the order they
	// are stored in the prediction set.)  In other words, the basis set is
	// considered to be an ordered list;  and states are not matched unless the
	// ordering of the basis set elements is the same.  If preserveOrder is
	// false, states are matched regardless of the ordering of their basis sets.

	private void buildSuccessorStates (boolean preserveOrder)
	{

		// Allocate an array of sets.  The value of successorBasis[s] is
		// the basis set of the successor state for shift symbol s.  An empty
		// set indicates no successor for the corresponding symbol.

		ObjectSet[] successorBasis = new ObjectSet[_machine.CFG().symbolCount()];

		for (int i = 0; i < successorBasis.length; ++i)
		{
			successorBasis[i] = new ObjectSet ();
		}

		// No unwinding production so far

		_unwindingDottedProduction = null;

		// Scan our basis set

		for (int b = 0; b < _basisSet.length; ++b)
		{

			// See if we have just found the unwinding dotted production

			if (_unwindingDottedProduction == null)
			{
				if (!_basisSet[b].isSymbolAtDotNonterminal())
				{
					_unwindingDottedProduction = _basisSet[b];
				}
			}

			// If the dot is at extreme right, skip to next basis set element

			if (_basisSet[b].isDotAtEnd())
			{
				continue;
			}

			// Shift dot right, and bin the new dotted production under the bin
			// for the symbol that was shifted

			successorBasis[_basisSet[b].symbolAtDot()].addElement (
				_basisSet[b].rightShiftDot() );

			// Scan the prediction set for the dotted symbol

			for (Enumeration pe
				= _machine.enumeratePredictions(_basisSet[b].symbolAtDot());
				pe.hasMoreElements(); )
			{

				// Get the next closure dotted production to check

				LRMachineDottedProduction closureProduction
					= ((LRMachinePrediction) pe.nextElement()).dottedProduction();

				// See if we have just found the unwinding dotted production

				if (_unwindingDottedProduction == null)
				{
					if (!closureProduction.isSymbolAtDotNonterminal())
					{
						_unwindingDottedProduction = closureProduction;
					}
				}

				// If the dot is at extreme right, skip to next closure set element

				if (closureProduction.isDotAtEnd())
				{
					continue;
				}

				// Shift dot right, and bin the new dotted production under the bin
				// for the symbol that was shifted

				successorBasis[closureProduction.symbolAtDot()].addElement (
					closureProduction.rightShiftDot() );

			}	// end scan of prediction (closure) set

		}	// end scan of basis set

		// Count the number of successors.  There is a successor for each
		// nonempty set of successor basis elements.

		int successorCount = 0;

		for (int i = 0; i < successorBasis.length; ++i)
		{
			if (!successorBasis[i].isEmpty())
			{
				++successorCount;
			}
		}

		// Allocate an array to hold successor states

		_successorStates = new LRMachineState[successorCount];

		// Initialize index into successor states array

		successorCount = 0;

		// Scan the set of states, looking for existing successor states

	scanStates:
		for (Enumeration se = _machine.enumerateStates(); se.hasMoreElements(); )
		{

			// Get the next state to processes

			LRMachineState otherState = (LRMachineState) se.nextElement();

			// Check if the other state's basis set matches our successor basis
			// for the other state's shift symbol

			// Check for same number of basis elements

			if (successorBasis[otherState._shiftSymbol].elementCount()
				!= otherState._basisSet.length)
			{
				continue scanStates;
			}

			// If we want to preserve the ordering of the basis set ...

			if (preserveOrder)
			{

				// Check that the other state's basis set has the same elements,
				// in the same order, as our desired successor basis set

				Enumeration be = successorBasis[otherState._shiftSymbol].elements();

				for (int b = 0; b < otherState._basisSet.length; ++b)
				{
					if (((LRMachineDottedProduction) be.nextElement())
						!= otherState._basisSet[b] )
					{
						continue scanStates;
					}
				}
			}

			// Otherwise, compare basis sets regardless of ordering

			else
			{

				// Check that each element in other state's basis set is a member of
				// our desired successor basis set

				for (int b = 0; b < otherState._basisSet.length; ++b)
				{
					if (!successorBasis[otherState._shiftSymbol].isElement (
						otherState._basisSet[b] ))
					{
						continue scanStates;
					}
				}
			}

			// The other state is one of our successors

			_successorStates[successorCount++] = otherState;

			// Clear the set so we won't check it again

			successorBasis[otherState._shiftSymbol].removeAllElements();

		}	// end scan of states

		// If there are any successor basis sets left, create new states for them

		for (int i = 0; i < successorBasis.length; ++i)
		{

			// If we found a nonempty successor basis set ...

			if (!successorBasis[i].isEmpty())
			{

				// Create a new state and add it to the set of states
				
				_successorStates[successorCount++] 
					= new LRMachineState (_machine, i, successorBasis[i]);
			}
		}

		// Done finding successors

		return;
	}


	// This function calculates the lookahead set for each possible successor
	// state, using the given lookahead set as the lookaheads for this state's
	// basis set.  Then it searches the cognates of each successor state,
	// looking for an existing cognate with which the lookaheads can be merged.
	// If none is found, a new cognate is created.  The resulting array of
	// successor cognates is returned.
	//
	// After each merge, this function calls parentCognate.checkMergeAbort.  If
	// that function returns true, then this function immediately stops 
	// generating successor cognates and returns null.

	LRMachineCognate[] makeSuccessorCognates (LRMachineConstLookaheadSet[] lookaheadSet,
		BinaryPredicate mergePredicate, LRMachineCognate parentCognate)
	{

		// Allocate an array to hold the successor cognates.  The i-th element
		// of this array is a cognate of _successorStates[i].

		LRMachineCognate[] successorCognates = new LRMachineCognate[_successorStates.length];

		for (int i = 0; i < successorCognates.length; ++i)
		{
			successorCognates[i] = null;
		}

		// Allocate an array to hold the lookahead sets of successor dotted
		// productions.  The array is indexed by the index number of the dotted
		// production.  Since a given successor dotted production can appear in
		// only one successor state (determined by the shift symbol), this does
		// not introduce any ambiguity.

		LRMachineLookaheadSet[] successorLookaheads
			= new LRMachineLookaheadSet[_machine.dottedProductionCount()];

		for (int i = 0; i < successorLookaheads.length; ++i)
		{
			successorLookaheads[i] = _machine.makeLookaheadSet();
		}

		// Scan our basis set

		for (int b = 0; b < _basisSet.length; ++b)
		{

			// If the dot is at extreme right, skip to next basis set element

			if (_basisSet[b].isDotAtEnd())
			{
				continue;
			}

			// Shift the dot right, and propagate the supplied lookaheads to
			// the shifted dotted production.

			successorLookaheads[_basisSet[b].rightShiftDot().index()].union (
				lookaheadSet[b] );

			// Calculate the lookaheads that we need to propagate into 
			// predictions.  This consists of spontaneous lookaheads generated
			// by the basis production, plus the supplied lookaheads if the
			// basis tail derives epsilon.

			LRMachineLookaheadSet basisPropagate = _basisSet[b].firstSetAfterDot();

			if (_basisSet[b].derivesEpsilonAfterDot())
			{
				basisPropagate.union (lookaheadSet[b]);
			}

			// Scan the prediction set for the dotted symbol

			for (Enumeration pe
				= _machine.enumeratePredictions(_basisSet[b].symbolAtDot());
				pe.hasMoreElements(); )
			{

				// Get the next closure prediction to check

				LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement();

				// If the dot is at extreme right, skip to next closure set element

				if (prediction.dottedProduction().isDotAtEnd())
				{
					continue;
				}

				// Shift the dot right, and propagate the spontaneous lookaheads to
				// the shifted dotted production.

				successorLookaheads[prediction.dottedProduction().rightShiftDot().index()]
					.union (prediction.spontaneousLookaheads());

				// If the propagate flag is set, also propagate lookaheads from the
				// basis production

				if (prediction.propagateFlag())
				{
					successorLookaheads[prediction.dottedProduction().rightShiftDot().index()]
						.union (basisPropagate);
				}

			}	// end scan of prediction (closure) set

		}	// end scan of basis set

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// Allocate an array to hold lookaheads for this successor

			LRMachineLookaheadSet[] successorLookaheadSet
				= new LRMachineLookaheadSet[_successorStates[s]._basisSet.length];

			// Scan the successor state's basis set, and get the lookahead
			// set for each element of the basis set

			for (int b = 0; b < successorLookaheadSet.length; ++b)
			{
				successorLookaheadSet[b] 
					= successorLookaheads[_successorStates[s]._basisSet[b].index()];
			}

			// Try to merge with an existing cognate of the successor state,
			// and create a new cognate if we can't

			mergeCheck:
			{

				// Scan the cognates of the successor state

				for (Enumeration ce = _successorStates[s]._cognates.elements();
					ce.hasMoreElements(); )
				{

					// Get the next cognate to check

					successorCognates[s] = (LRMachineCognate) ce.nextElement();

					// Try to merge with this cognate

					if (successorCognates[s].merge (successorLookaheadSet, mergePredicate))
					{

						// Successful merge, check for abort

						if (parentCognate.checkMergeAbort (successorCognates))
						{
							return null;
						}

						// Stop searching

						break mergeCheck;
					}
				}

				// Can't merge, create a new cognate of the successor state

				successorCognates[s]
					= new LRMachineCognate (_successorStates[s], successorLookaheadSet);

			}	// end search for cognate merge

		}	// end scan of successor states

		// Return the array of successor cognates

		return successorCognates;
	}


	// Creates the initial cognate.  This function should be called only for
	// the initial state.  It creates a cognate with an empty lookahead set.

	LRMachineCognate makeInitialCognate ()
	{

		// Allocate an array to hold lookaheads

		LRMachineLookaheadSet[] initialLookaheadSet
			= new LRMachineLookaheadSet[_basisSet.length];

		// Initialize each lookahead to the empty set

		for (int b = 0; b < initialLookaheadSet.length; ++b)
		{
			initialLookaheadSet[b] = _machine.makeLookaheadSet(); 
		}

		// Create the new cognate

		return new LRMachineCognate (this, initialLookaheadSet);
	}


	// This function creates the parser action table for this state, given a
	// set of lookaheads.
	//
	// The action table is an array indexed by symbols.  Each element encodes
	// an action as follows:  (i) If the action is to reduce production p, the
	// value is p.  (ii) If the action is to accept, the value is g, where g is
	// the numerical value of the goal production.  (iii) If the action is to
	// shift and go to state s, the value is s+PC, where PC is the total number
	// of productions in the grammar.  (iv) If the action is to signal error,
	// the value is PC.
	//
	// Note that the parser never reduces the goal production or goes to state
	// 0, so the encodings for accept and error do not create ambiguity.

	int[] makeActionTable (LRMachineConstLookaheadSet[] lookaheadSet,
		int[] successorCognateNumbers, int cognateNumber)
	{

		// Allocate an array to hold the action set for each symbol

		IntSet[] actionSet = new IntSet[_machine.CFG().symbolCount()];

		for (int i = 0; i < actionSet.length; ++i)
		{
			actionSet[i] = new IntSet();
		}

		// Scan our basis set

		for (int b = 0; b < _basisSet.length; ++b)
		{

			// If the dot is at extreme right ...

			if (_basisSet[b].isDotAtEnd())
			{

				// For each element of the lookahead set, add a reduce action
				// for this dotted production

				for (IntEnumeration le = lookaheadSet[b].elements(); le.hasMoreElements(); )
				{
					actionSet[le.nextElement()].addElement (_basisSet[b].production());
				}

				// Skip to next basis set element

				continue;
			}

			// Calculate the lookaheads that we need to propagate into 
			// predictions.  This consists of spontaneous lookaheads generated
			// by the basis production, plus the supplied lookaheads if the
			// basis tail derives epsilon.

			LRMachineLookaheadSet basisPropagate = _basisSet[b].firstSetAfterDot();

			if (_basisSet[b].derivesEpsilonAfterDot())
			{
				basisPropagate.union (lookaheadSet[b]);
			}

			// Scan the prediction set for the dotted symbol

			for (Enumeration pe
				= _machine.enumeratePredictions(_basisSet[b].symbolAtDot());
				pe.hasMoreElements(); )
			{

				// Get the next closure prediction to check

				LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement();

				// If the dot is not at extreme right, skip to next closure set element

				if (!prediction.dottedProduction().isDotAtEnd())
				{
					continue;
				}

				// Get the spontaneous lookaheads for this prediction

				LRMachineLookaheadSet predLookaheads
					= (LRMachineLookaheadSet) prediction.spontaneousLookaheads().clone();

				// If the propagate flag is set, propagate lookaheads from the
				// basis production

				if (prediction.propagateFlag())
				{
					predLookaheads.union (basisPropagate);
				}

				// For each element of the lookahead set, add a reduce action
				// for this dotted production

				for (IntEnumeration le = predLookaheads.elements(); le.hasMoreElements(); )
				{
					actionSet[le.nextElement()].addElement (
						prediction.dottedProduction().production() );
				}

			}	// end scan of prediction (closure) set

		}	// end scan of basis set

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// Add a shift action for this successor state

			actionSet[_successorStates[s]._shiftSymbol].addElement (
				_machine.CFG().productionCount() + _successorStates[s]._shiftSymbol );
		}

		// Create the parser action table

		int[] actionTable = new int[_machine.CFG().symbolCount()];

		// Scan the action table

		for (int symbol = 0; symbol < actionTable.length; ++symbol)
		{

			// If the action set is empty ...

			if (actionSet[symbol].isEmpty())
			{

				// Write an error entry into the action table.  We use the code
				// for a shift to state 0 (which is not a valid move).

				actionTable[symbol] = _machine.CFG().productionCount();
			}

			// Else, if the action set contains exactly one element ...

			else if (actionSet[symbol].elementCount() == 1)
			{

				// Write the unique action into the action table

				actionTable[symbol] = actionSet[symbol].elements().nextElement();
			}

			// Otherwise, we need to resolve the parser conflict

			else
			{

				// Attempt to resolve the conflict and save the selected action.

				actionTable[symbol] = _machine.resolveConflict (
					cognateNumber, symbol, actionSet[symbol] );
			}
		}

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// If the action table contains a shift to this successor state ...

			if (actionTable[_successorStates[s]._shiftSymbol]
				== (_machine.CFG().productionCount() + _successorStates[s]._shiftSymbol) )
			{

				// If the shift is the end-of-file symbol ...

				if (_successorStates[s]._shiftSymbol == _machine.eofSymbol())
				{

					// Write an accept entry into the action table.  We use the
					// code for a reduction of the goal production.

					actionTable[_successorStates[s]._shiftSymbol] = _machine.goalProduction();
				}

				// Otherwise, write a shift entry into the action table

				else
				{
					actionTable[_successorStates[s]._shiftSymbol]
						= _machine.CFG().productionCount() + successorCognateNumbers[s];
				}
			}
		}

		// Return the action table

		return actionTable;
	}


	// This function gets the unwinding action for this state.
	//
	// If the unwinding action is to reduce production p, the value returned
	// is p.  If the unwinding action is to shift terminal symbol s, the value
	// returned is s+PC where PC is the total number of productions in the
	// grammar.

	int getUnwindingAction ()
	{

		// If the unwinding dotted production has its dot at the extreme right ...

		if (_unwindingDottedProduction.isDotAtEnd())
		{

			// Reduce the unwinding production

			return _unwindingDottedProduction.production();
		}

		// Otherwise, shift the symbol at the dot

		return _unwindingDottedProduction.symbolAtDot() + _machine.CFG().productionCount();
	}


	// This function gets the unwinding parsing action for this state.
	//
	// If the unwinding action is to reduce production p, the value returned
	// is p.  If the unwinding action is to shift and go to state s, the value
	// returned is s+PC where PC is the total number of productions in the
	// grammar.

	int getUnwindingParseAction (int[] successorCognateNumbers)
	{

		// If the unwinding dotted production has its dot at the extreme right ...

		if (_unwindingDottedProduction.isDotAtEnd())
		{

			// Reduce the unwinding production

			return _unwindingDottedProduction.production();
		}

		// Otherwise, shift the symbol at the dot

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// If the shift is the symbol at the dot ...

			if (_successorStates[s]._shiftSymbol == _unwindingDottedProduction.symbolAtDot())
			{

				// Return the target cognate number for this successor

				return _machine.CFG().productionCount() + successorCognateNumbers[s];
			}
		}

		// Return code for error, a shift to state 0 (this should never happen)

		return _machine.CFG().productionCount();
	}


	// Initialize _mergeCheckSet.
	//
	// This function sets _mergeCheckSet to the set of lookaheads that would
	// cause a shift-reduce conflict in this state.  In other words, if the
	// lookaheads for _basisSet[b] trigger a reduce action, then we set
	// _mergeCheckSet[b] to be the set of all terminal symbols that can be
	// shifted in this state.

	private void initializeMergeCheckSet ()
	{

		// Construct the set of all terminal symbols that we can shift

		LRMachineLookaheadSet shiftTerminals = _machine.makeLookaheadSet();

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// If the shift symbol is a terminal ...

			if (_machine.CFG().isTerminal (_successorStates[s]._shiftSymbol))
			{

				// Add the shift symbol to the set of terminal shift symbols

				shiftTerminals.addElement (_successorStates[s]._shiftSymbol);
			}
		}

		// Allocate an array of lookahead sets

		_mergeCheckSet = new LRMachineLookaheadSet[_basisSet.length];

		// Scan our basis set

		for (int b = 0; b < _basisSet.length; ++b)
		{

			// Create an empty lookahead set for this basis element

			_mergeCheckSet[b] = _machine.makeLookaheadSet();

			// Lookaheads for this basis element cause a reduction if the portion
			// of _basisSet[b] at and after the dot derives epsilon.  (If the dot
			// is at the extreme right, the basis element itself is reduced.  If
			// the dot is not at the extreme right, the lookaheads are propagated
			// to an epsilon production in the closure.)

			if (_basisSet[b].derivesEpsilonAtAndAfterDot())
			{

				// Add all the shift terminals to this set

				_mergeCheckSet[b].union (shiftTerminals);
			}
		}

		// Done

		return;
	}


	// Propagate _mergeCheckSet.
	//
	// For each basis element b, this function determines which successor basis
	// elements receive lookaheads propagated from the lookahead set of b.
	// Then, the merge check set for b is updated to include all symbols in the
	// merge check sets of the successor basis elements.
	//
	// The result is that _mergeCheckSet elements from successor states are
	// propagated backwards into this state.
	//
	// By applying this function repeatedly, eventually _mergeCheckSet contains
	// all terminal symbols that cause a shift-reduce conflict in this state or
	// in any (direct or indirect) successor state.
	//
	// The return value is true if the contents of _mergeCheckSet was changed.

	private boolean propagateMergeCheckSet ()
	{

		// Nothing changed so far

		boolean isChanged = false;

		// Allocate an array to hold a SmallIntSet for each successor dotted
		// production.  The array is indexed by the index number of the dotted
		// production.  Since a given successor dotted production can appear in
		// only one successor state (determined by the shift symbol), this does
		// not introduce any ambiguity.
		//
		// Each SmallIntSet contains the index numbers of the basis elements in
		// this state that contribute lookaheads to the successor basis element.

		SmallIntSet[] basisContributors
			= new SmallIntSet[_machine.dottedProductionCount()];

		for (int i = 0; i < basisContributors.length; ++i)
		{
			basisContributors[i] = new SmallIntSet(0, _basisSet.length);
		}

		// Scan our basis set

		for (int b = 0; b < _basisSet.length; ++b)
		{

			// If the dot is at extreme right, skip to next basis set element

			if (_basisSet[b].isDotAtEnd())
			{
				continue;
			}

			// Shift the dot right, and propagate our lookaheads to
			// the shifted dotted production.

			basisContributors[_basisSet[b].rightShiftDot().index()].addElement (b);

			// If the basis tail does not derive epsilon, skip to the next basis
			// set element because we don't propagate our lookaheads into the
			// closure set elements.

			if (!_basisSet[b].derivesEpsilonAfterDot())
			{
				continue;
			}

			// Scan the prediction set for the dotted symbol

			for (Enumeration pe
				= _machine.enumeratePredictions(_basisSet[b].symbolAtDot());
				pe.hasMoreElements(); )
			{

				// Get the next closure prediction to check

				LRMachinePrediction prediction = (LRMachinePrediction) pe.nextElement();

				// If the dot is at extreme right, skip to next closure set element

				if (prediction.dottedProduction().isDotAtEnd())
				{
					continue;
				}

				// If the propagate flag is set, shift the dot right and propagate
				// our lookaheads to the shifted dotted production

				if (prediction.propagateFlag())
				{
					basisContributors[prediction.dottedProduction().rightShiftDot().index()]
						.addElement (b);
				}

			}	// end scan of prediction (closure) set

		}	// end scan of basis set

		// Scan our successor states

		for (int s = 0; s < _successorStates.length; ++s)
		{

			// Scan the successor state's basis set

			for (int b = 0; b < _successorStates[s]._basisSet.length; ++b)
			{

				// Enumerate the basis elements in this state that propagate
				// lookaheads into the successor basis element

				for (IntEnumeration ie
					= basisContributors[_successorStates[s]._basisSet[b].index()].elements();
					ie.hasMoreElements(); )

				{

					// Combine successor's merge check set into ours

					isChanged |= _mergeCheckSet[ie.nextElement()].union (
						_successorStates[s]._mergeCheckSet[b] );
				}

			}	// end scan of successor's basis set

		}	// end scan of successor states

		// Return the change flag

		return isChanged;
	}


	// Check to see if two lookahead sets can be merged.  This function returns
	// true if the sets can be merged.

	boolean mergeCheck (LRMachineConstLookaheadSet[] lookaheadSet1,
		LRMachineConstLookaheadSet[] lookaheadSet2)
	{

		// If no merge checking, return merge allowed

		if (_mergeCheckSet == null)
		{
			return true;
		}

		// Scan the merge check set

		for (int b = 0; b < _mergeCheckSet.length; ++b)
		{

			// If merge check set is empty, continue

			if (_mergeCheckSet[b].isEmpty())
			{
				continue;
			}

			// Get the intersection of each set with the merge check set

			LRMachineLookaheadSet intersection1 
				= (LRMachineLookaheadSet) _mergeCheckSet[b].clone();

			intersection1.intersection (lookaheadSet1[b]);

			LRMachineLookaheadSet intersection2 
				= (LRMachineLookaheadSet) _mergeCheckSet[b].clone();

			intersection2.intersection (lookaheadSet2[b]);

			// If the intersections are unequal, no merging is allowed

			if (!intersection1.equals(intersection2))
			{
				return false;
			}
		}

		// Return merge allowed

		return true;
	}


	// Converts a basis set element to a string.
	// Used by LRMachineCognate.toString and LRMachineCognate.basisToString.

	final String basisSetToString (int index)
	{
		return _basisSet[index].toString();
	}


	// Converts a lookahead set to a string.
	// Used by LRMachineCognate.toString and LRMachineCognate.lookaheadToString.

	final String lookaheadSetToString (LRMachineConstLookaheadSet set)
	{
		return _machine.CFG().symbolName(set).toString();
	}


	// Converts a successor shift symbol to a string.
	// Used by LRMachineCognate.toString.

	final String successorShiftSymbolToString (int index)
	{
		return _machine.CFG().symbolName (_successorStates[index]._shiftSymbol);
	}


}




// LRMachineCognate represents a state of the LR(1) configuration finite state
// machine.  An LRMachineCognate object is associated with a particular
// LRMachineState object, known as its core state.
//
// A cognate assigns a set of lookahead symbols to each configuration in the
// basis set of its core state.  Thus, LR(1) states with the same core state
// have the same dotted productions, but different lookahead sets.
//
// For LALR(1), there is only one cognate for each core state.  Each lookahead
// set contains all lookahead symbols that are valid when in the core state.
//
// For LR(1), there are multiple cognates for each core state.  Each cognate
// corresponds to a possible set of valid lookahead symbols at a particular
// point in the parse.  In other words, an LR(1) cognate contains exactly the
// lookahead symbols that are valid at a given point in the parse.
//
// For PLR(1), there are multiple cognates for each core state.  PLR(1) is
// obtained from LR(1) by merging cognates when it can be determined that the
// merger does not create a parse conflict.  As a result, a PLR(1) cognate
// contains at least the lookahead symbols that are valid at a particular point
// in the parse, and at most all the lookahead symbols that are valid when in
// the core state.

final class LRMachineCognate
{

	// The LRMachineState associated with this object.

	private LRMachineState _coreState;


	// The table of lookahead symbols.  For each index i, the value of
	// _lookaheadSet[i] is an LRMachineLookaheadSet containing the lookahead
	// symbols for the configuration given in _coreState._basisSet[i].

	private LRMachineLookaheadSet[] _lookaheadSet;


	// A counter associated with this cognate.  During cognate construction,
	// this is a reference counter giving the number of other cognates which
	// have this cognate as a successor.  During parse table construction,
	// this is the state number.

	private int _counter;


	// A link that is used internally when scanning the successor structure.
	// This allows us to avoid recursion.

	private LRMachineCognate _nextCognate;


	// The table of successor cognates.  For each index i, the value of
	// _successorLookaheads[i] is an LRMachineCognate object whose core state
	// is _coreState._successorStates[i].

	private LRMachineCognate[] _successorCognates;


	// Create a new cognate with the specified core state and array of
	// lookahead sets.  This function retains a reference to the lookahead
	// set array.

	LRMachineCognate (LRMachineState coreState, LRMachineLookaheadSet[] lookaheadSet)
	{
		super ();

		// Save the core state and lookahead set array

		_coreState = coreState;
		_lookaheadSet = lookaheadSet;

		// No successor cognates yet

		_successorCognates = null;

		// Initialize reference count to 1

		_counter = 1;

		// Do this just to establish a definite initial value

		_nextCognate = null;

		// Add to core state's set of cognates

		_coreState.addCognate (this);

		return;
	}


	// This function decrements the reference count for each successor, then
	// sets _succesorCognates to null.  Each successor whose reference count
	// is decremented to zero is removed from it's core state's set of
	// cognates, and then its successors are dumped recursively.

	private void dumpSuccessors ()
	{

		// Create a list of cognates to process.  Initially, the list contains
		// just this cognate.

		LRMachineCognate cognateList = this;
		this._nextCognate = null;

		// While the list is nonempty

		while (cognateList != null)
		{

			// Get an element off the list

			LRMachineCognate currentCognate = cognateList;
			cognateList = currentCognate._nextCognate;

			// Null out our forward pointer, so we don't keep a reference to
			// a cognate that may be deleted later

			currentCognate._nextCognate = null;

			// If the element has successors ...

			if (currentCognate._successorCognates != null)
			{

				// Remove reference for all successor cognates

				for (int i = 0; i < currentCognate._successorCognates.length; ++i)
				{

					// Check for a null successor.  This lets us accept partial
					// successor sets such as are passed to checkMergeAbort.

					if (currentCognate._successorCognates[i] == null)
					{
						continue;
					}

					// If reference count is decremented to zero ...

					if ((--currentCognate._successorCognates[i]._counter) == 0)
					{

						// Remove successor cognate from it's core state's set

						currentCognate._successorCognates[i]._coreState.removeCognate (
							currentCognate._successorCognates[i]);

						// Add successor cognate to our list

						currentCognate._successorCognates[i]._nextCognate = cognateList;
						cognateList = currentCognate._successorCognates[i];
					}

					// Break the successor link (strictly speaking this is not
					// necessary, but it may help the garbage collector)

					currentCognate._successorCognates[i] = null;
				}

				// Delete the successor table

				currentCognate._successorCognates = null;

			}	// end if current cognate has successors

		}	// end loop while list is nonempty

		return;
	}


	// Attempts to merge the specified lookahead set into this cognate.  First,
	// the merge predicate is evaluated for the pair of lookahead sets.  If
	// the predicate returns false, then no merge takes place and this function
	// returns false.  Otherwise, the merge is performed by (i) incrementing
	// the reference counter;  (ii) forming the union of this cognate's
	// lookahead set with the specified lookahead set;  (iii) if any lookahead
	// set changed, decrementing the reference count of each successor cognate
	// and clearing the successor table;  and (iv) returning true.

	boolean merge (LRMachineConstLookaheadSet[] otherLookaheadSet,
		BinaryPredicate mergePredicate)
	{

		// Evaluate the predicate to see if merging is allowed

		if (!mergePredicate.value (_lookaheadSet, otherLookaheadSet))
		{
			return false;
		}

		// Check if the core state allows this merge

		if (!_coreState.mergeCheck (_lookaheadSet, otherLookaheadSet))
		{
			return false;
		}

		// Add a reference for this cognate

		++_counter;

		// Form the union of our lookaheads and the other lookaheads

		boolean isChanged = false;

		for (int i = 0; i < _lookaheadSet.length; ++i)
		{
			isChanged |= _lookaheadSet[i].union (otherLookaheadSet[i]);
		}

		// If any lookaheads changed ...

		if (isChanged)
		{

			// Dump our successor table

			dumpSuccessors ();
		}

		// Return successful merge

		return true;
	}


	// LRMachineState.makeSuccessorCognates calls this function for the parent
	// cognate after each successful merge.  This gives the parent cognate the
	// opportunity to abort the process.  If this function returns true, 
	// LRMachineState.makeSuccessorCognates immediately aborts and returns null.
	//
	// The array of partial successors is the set of successor cognates that
	// have been found or created so far.  Unused elements of this array are
	// null.  If this function returns true, then this function is responsible
	// for decrementing the reference counts on the successors.
	//
	// This function returns true if the prior merge caused this cognate (i.e.,
	// the parent cognate) to dump its successor table.  There are two ways
	// this can happen:  (i) This cognate may have been selected to be its own
	// successor.  If the merge changed this cognate's lookahead set, then the
	// successor table was dumped.  (ii) This cognate may have been a direct
	// or indirect successor of the cognate that was merged.  If the merged
	// cognate's lookahead set was changed, the reference counts of its
	// successors were decremented.  If that caused this cognate's reference
	// count to become zero, then the successor table was dumped.

	boolean checkMergeAbort (LRMachineCognate[] partialSuccessors)
	{

		// If we still have our successor table, return false to indicate
		// no abort

		if (_successorCognates != null)
		{
			return false;
		}

		// Take the partial successor table

		_successorCognates = partialSuccessors;

		// Then dump the partial successor table

		dumpSuccessors ();

		// Return true to indicate abort

		return true;
	}


	// This function creates all the cognates for the LR machine.  Before
	// calling this function, all states must be created and all cognate
	// sets must be initialized to the empty set.  The return value is an
	// array containing all cognates in order of LR(1) state number.

	static LRMachineCognate[] makeCognates (LRMachine machine, LRMachineState initialState,
		BinaryPredicate mergePredicate) throws InterruptedCompilerException
	{
		
		// Deque to use for gathering statistics
		
		IntDeque countList = new IntDeque ();

		// Create the initial cognate

		LRMachineCognate initialCognate = initialState.makeInitialCognate ();

		// Loop until nothing changes

		for (boolean isChanged = true; isChanged; )
		{

			// Nothing changed yet

			isChanged = false;

			// Step 1.  Set all reference counts to zero.

			// Scan all cognates and zero out their counters

			for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
			{
				for (Enumeration ce = ((LRMachineState) se.nextElement()).enumerateCognates();
					ce.hasMoreElements(); )
				{
					((LRMachineCognate) ce.nextElement())._counter = 0;
				}
			}

			// Step 2.  Set the reference counts for all cognates that are
			// reachable from the initial cognate.

			// Set the reference count of the initial cognate to 1

			initialCognate._counter = 1;

			// Create a list, and initialize it to contain just the initial cognate

			LRMachineCognate cognateList = initialCognate;
			initialCognate._nextCognate = null;
			
			// Statistics counter
			
			int cognatesInThisPass = 0;

			// Loop until the list is empty

			while (cognateList != null)
			{

				// Remove an element from the list

				LRMachineCognate currentCognate = cognateList;
				cognateList = currentCognate._nextCognate;
				
				// Count it in the statistics
				
				++cognatesInThisPass;

				// Null out our forward pointer, so we don't keep a reference to
				// a cognate that may be deleted later

				currentCognate._nextCognate = null;

				// If it has successors ...

				if (currentCognate._successorCognates != null)
				{

					// Scan the list of successors

					for (int i = 0; i < currentCognate._successorCognates.length; ++i)
					{

						// Increment the successor cognate's reference count, and check
						// if this is the first time we reached the successor cognate

						if ((currentCognate._successorCognates[i]._counter++) == 0)
						{

							// First time here, so add the successor to our list

							currentCognate._successorCognates[i]._nextCognate = cognateList;
							cognateList = currentCognate._successorCognates[i];
						}
					}
				}
			}
			
			// Save the statistics
			
			countList.pushLast (cognatesInThisPass);

			// Step 3.  Remove all cognates whose reference count is still zero.
			// These cognates are not reachable from the initial cognate.  The
			// only way this can happen is if there are unreachable cognates
			// which form a loop through their _successorCognates fields.

			// Scan all cognates

			for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
			{
				for (Enumeration ce = ((LRMachineState) se.nextElement()).enumerateCognates();
					ce.hasMoreElements(); )
				{

					// Get the next cognate to check

					LRMachineCognate currentCognate = (LRMachineCognate) ce.nextElement();

					// If the reference count is zero ...

					if (currentCognate._counter == 0)
					{

						// Remove the cognate from it's core state's set

						currentCognate._coreState.removeCognate (currentCognate);

						// Break all the successor links.  Strictly speaking, this is
						// not necessary, but it may help the garbage collector.  By
						// breaking the successor links, we won't have loops of
						// unreachable objects for the garbage collector to find.

						if (currentCognate._successorCognates != null)
						{
							for (int i = 0; i < currentCognate._successorCognates.length; ++i)
							{
								currentCognate._successorCognates[i] = null;
							}

							currentCognate._successorCognates = null;
						}
					}
				}
			}

			// Step 4.  Compact all the cognate sets.  This needs to be done
			// because on each pass, the number of cognates added and removed
			// may be comparable to the total number of cognates.

			// Scan all states and compact their cognate sets

			for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
			{
				((LRMachineState) se.nextElement()).compactCognates();
			}

			// Step 5.  Create successor cognates for each cognate that does
			// not already have a set of successors.

			// Scan all cognates

			for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
			{
				for (Enumeration ce = ((LRMachineState) se.nextElement()).enumerateCognates();
					ce.hasMoreElements(); )
				{

					// Get the next cognate to check

					LRMachineCognate currentCognate = (LRMachineCognate) ce.nextElement();

					// If it has no successors ...

					if (currentCognate._successorCognates == null)
					{
						
						// Report work
						
						machine.statusWork();

						// Signal that something has changed on this pass

						isChanged = true;

						// Insert a zero-length successor table into this cognate.
						// This allows us to detect (in checkMergeAbort) if this
						// cognate has dumped its successor table during the process
						// of creating its successors.

						currentCognate._successorCognates = new LRMachineCognate[0];

						// Make the successor cognates.  Note that this returns null
						// if an abort occurred.

						currentCognate._successorCognates
							= currentCognate._coreState.makeSuccessorCognates (
							currentCognate._lookaheadSet, mergePredicate, currentCognate );
					}
				}
			}

		}	// end loop until nothing changes

		// Assign a unique number to each cognate.  Note that in order for
		// the initial cognate to be assigned number zero (as desired), we
		// require that the initial state be the first state returned by
		// LRMachine.enumerateStates.

		int cognateCount = 0;

		for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
		{
			for (Enumeration ce = ((LRMachineState) se.nextElement()).enumerateCognates();
				ce.hasMoreElements(); )
			{
				((LRMachineCognate) ce.nextElement())._counter = cognateCount++;
			}
		}

		// Allocate an array to hold the cognates

		LRMachineCognate[] cognateArray = new LRMachineCognate[cognateCount];

		// Insert each cognate into its element of the array

		for (Enumeration se = machine.enumerateStates(); se.hasMoreElements(); )
		{
			for (Enumeration ce = ((LRMachineState) se.nextElement()).enumerateCognates();
				ce.hasMoreElements(); )
			{
				LRMachineCognate nextCognate = (LRMachineCognate) ce.nextElement();
				cognateArray[nextCognate._counter] = nextCognate;
			}
		}

		// Return the statistics
		
		machine.setLR1Statistics (countList);

		// Return the array of cognates

		return cognateArray;
	}


	// This function creates the parser action table for this cognate.
	//
	// The action table is an array indexed by symbols.  Each element encodes
	// an action as follows:  (i) If the action is to reduce production p, the
	// value is p.  (ii) If the action is to accept, the value is g, where g is
	// the numerical value of the goal production.  (iii) If the action is to
	// shift and go to state s, the value is s+PC, where PC is the total number
	// of productions in the grammar.  (iv) If the action is to signal error,
	// the value is PC.
	//
	// Note that the parser never reduces the goal production or goes to state
	// 0, so the encodings for accept and error do not create ambiguity.

	int[] makeActionTable ()
	{

		// Create an array containing the numbers of our successors

		int[] successorCognateNumbers = new int[_successorCognates.length];

		for (int i = 0; i < successorCognateNumbers.length; ++i)
		{
			successorCognateNumbers[i] = _successorCognates[i]._counter;
		}

		// Let the core state calculate the action table

		return _coreState.makeActionTable (_lookaheadSet, successorCognateNumbers, _counter);
	}


	// This function gets the unwinding action for this cognate.
	//
	// If the unwinding action is to reduce production p, the value returned
	// is p.  If the unwinding action is to shift terminal symbol s, the value
	// returned is s+PC where PC is the total number of productions in the
	// grammar.

	int getUnwindingAction ()
	{
		return _coreState.getUnwindingAction ();
	}


	// This function gets the unwinding parse action for this cognate.
	//
	// If the unwinding action is to reduce production p, the value returned
	// is p.  If the unwinding action is to shift and go to state s, the value
	// returned is s+PC where PC is the total number of productions in the
	// grammar.

	int getUnwindingParseAction ()
	{

		// Create an array containing the numbers of our successors

		int[] successorCognateNumbers = new int[_successorCognates.length];

		for (int i = 0; i < successorCognateNumbers.length; ++i)
		{
			successorCognateNumbers[i] = _successorCognates[i]._counter;
		}

		// Let the core state calculate the unwinding parse action

		return _coreState.getUnwindingParseAction (successorCognateNumbers);
	}


	// Converts the cognate to a string.  Overrides the toString method of
	// class Object.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left bracket and the state number

		buf.append ("[" + _counter + ": ");

		// Loop through basis productions

		for (int b = 0; b < _lookaheadSet.length; ++b)
		{

			// If not first time through, append a comma

			if (b != 0)
			{
				buf.append (", ");
			}

			// Append the dotted production

			buf.append (_coreState.basisSetToString(b));

			// Append a space

			buf.append (" ");

			// Append the lookahead set

			buf.append (_coreState.lookaheadSetToString(_lookaheadSet[b]));
		}

		// Append a semicolon and left brace

		buf.append ("; {");

		// Loop through successor cognates

		for (int i = 0; i < _successorCognates.length; ++i)
		{

			// If not first time through, append a comma

			if (i != 0)
			{
				buf.append (", ");
			}

			// Append the transition

			buf.append (_coreState.successorShiftSymbolToString(i) + ":"
				+ _successorCognates[i]._counter);
		}

		// End with a right brace and right bracket

		buf.append ("}]");

		// Return resulting string

		return buf.toString ();
	}


	// Returns the number of elements in the basis set.

	public int basisCount ()
	{
		return _lookaheadSet.length;
	}


	// Returns a String representing one LR(0) basis element.

	public String basisToString (int basis)
	{
		return _coreState.basisSetToString (basis);
	}


	// Returns a String representing the LR(1) lookahead set for one basis element.

	public String lookaheadToString (int basis)
	{
		return _coreState.lookaheadSetToString (_lookaheadSet[basis]);
	}


}




// LALR(1) binary predicate for detemining if two cognates can be merged.
// Since LALR(1) only allows one cognate per core set, this predicate returns
// true if the core states are the same.

final class LRMachineLALR1Predicate extends BinaryPredicate
{

	// Implement this class as a singleton

	private static final LRMachineLALR1Predicate _singleton
		= new LRMachineLALR1Predicate ();

	static final LRMachineLALR1Predicate singleton ()
	{
		return _singleton;
	}

	private LRMachineLALR1Predicate ()
	{
		super ();
		return;
	}


	// Two cognates can be merged if the core state is the same

	public boolean value (Object object1, Object object2)
	{

		// Get the lookahead sets

		LRMachineConstLookaheadSet[] lookaheadSet1 = (LRMachineConstLookaheadSet[]) object1;

		LRMachineConstLookaheadSet[] lookaheadSet2 = (LRMachineConstLookaheadSet[]) object2;

		// Always merge

		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof LRMachineLALR1Predicate)))
		{
			return false;
		}

		return true;
	}


}




// LR(1) binary predicate for detemining if two lookahead sets can be merged.
// Since LR(1) never merges states, this predicate returns true if the
// lookahead sets are the same.

final class LRMachineLR1Predicate extends BinaryPredicate
{

	// Implement this class as a singleton

	private static final LRMachineLR1Predicate _singleton
		= new LRMachineLR1Predicate ();

	static final LRMachineLR1Predicate singleton ()
	{
		return _singleton;
	}

	private LRMachineLR1Predicate ()
	{
		super ();
		return;
	}


	// Two different cognates can never be merged

	public boolean value (Object object1, Object object2)
	{

		// Get the lookahead sets

		LRMachineConstLookaheadSet[] lookaheadSet1 = (LRMachineConstLookaheadSet[]) object1;

		LRMachineConstLookaheadSet[] lookaheadSet2 = (LRMachineConstLookaheadSet[]) object2;

		// Compare the lookahead sets

		for (int i = 0; i < lookaheadSet1.length; ++i)
		{

			// Check for equality

			if (!lookaheadSet1[i].equals(lookaheadSet2[i]))
			{
				return false;
			}
		}

		// All lookahead sets are equal

		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof LRMachineLR1Predicate)))
		{
			return false;
		}

		return true;
	}


}




// PLR(1) binary predicate for detemining if two cognates can be merged.
// PLR(1) allows two cognates to be merged if their core states are the same,
// and their lookahead sets satisfy weak compatibility.
//
// Weak compatibility is as follows:  For every pair of indexes i and j, at
// least one of the following three conditions holds:
//
//	(1) cognate1._lookaheadSet[i] is disjoint from cognate2._lookaheadSet[j],
//	and cognate2._lookaheadSet[i] is disjoint from cognate1._lookaheadSet[j].
//
//	(2) cognate1._lookaheadSet[i] is not disjoint from cognate1._lookaheadSet[j].
//
//	(3) cognate2._lookaheadSet[i] is not disjoint from cognate2._lookaheadSet[j].
//
// The following results can be shown:  (i) Weak compatibility holds for
// the basis set of a cognate if and only if weak compatibility holds for
// the full (closed by adding predictions) set of a cognate.  (ii) If two
// cognates are weakly compatible, and each cognate separately is free of
// parser conflicts, then the merged cognate is free of parser conflicts.
// (iii) If the full (closed by adding predictions) set of a cognate is
// weakly compatible, then the basis sets of its successor cognates are
// weakly compatible.
//
// These results imply that in an LR(1) grammar, weakly compatible cognates can
// be safely merged.  Result "ii" shows that merging does not introduce parser
// conflicts, while results "i" and "iii" show that successor cognates can also
// be safely merged.
//
// Reference:  Fischer and LeBlanc, pages 194-195.

final class LRMachinePLR1Predicate extends BinaryPredicate
{

	// Implement this class as a singleton

	private static final LRMachinePLR1Predicate _singleton
		= new LRMachinePLR1Predicate ();

	static final LRMachinePLR1Predicate singleton ()
	{
		return _singleton;
	}

	private LRMachinePLR1Predicate ()
	{
		super ();
		return;
	}


	// Two cognates can be merged if weak compatibility holds

	public boolean value (Object object1, Object object2)
	{

		// Get the lookahead sets

		LRMachineConstLookaheadSet[] lookaheadSet1 = (LRMachineConstLookaheadSet[]) object1;

		LRMachineConstLookaheadSet[] lookaheadSet2 = (LRMachineConstLookaheadSet[]) object2;

		// Check all pairs of basis set configuration

		for (int i = 1; i < lookaheadSet1.length; ++i)
		{
			for (int j = 0; j < i; ++j)
			{

				// Check for weak compatibility

				if (!(
					     (!lookaheadSet1[i].isDisjoint(lookaheadSet1[j]))
					  || (!lookaheadSet2[i].isDisjoint(lookaheadSet2[j]))
					  || (
						     (lookaheadSet1[i].isDisjoint(lookaheadSet2[j]))
						  && (lookaheadSet2[i].isDisjoint(lookaheadSet1[j]))
						 )
				   ) )
				{
					return false;
				}
			}
		}

		// All pairs satisfy the weak compatibility condition

		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof LRMachinePLR1Predicate)))
		{
			return false;
		}

		return true;
	}


}
