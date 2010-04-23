// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.parse.InterruptedCompilerException;

import invisible.jacc.util.ArrayHelper;
import invisible.jacc.util.ArrayIO;
import invisible.jacc.util.ConstIntSet;
import invisible.jacc.util.IODataFormatException;
import invisible.jacc.util.IntEnumeration;
import invisible.jacc.util.IntSet;
import invisible.jacc.util.ObjectSet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Enumeration;


/*->

  FiniteAutomaton implements a finite automaton.  It may contain either an
  NFA (nondeterministic finite automaton) or a DFA (deterministic finite
  automaton).  A finite automaton consists of a set of states, and for each
  state a set of transitions.  Each transition is labeled with either a
  character category (which must be a nonnegative integer) or with epsilon
  (which is represented as -1).  A state may have any number of transitions,
  and it may have multiple transitions with the same label.

  If each state has at most one transition with a given label, and no epsilon
  transitions, then the finite automaton is a DFA.  Thus, DFA's are simply a
  subset of the set of possible finite automata.

  One particular state is designated as the start state.  One or more states
  are designated as final states.  We allow each final state to be tagged with
  a nonnegative integer.

  A finite automaton accepts a string if there is a sequence of transitions
  corresponding to the string, beginning with the start state, and ending with
  a final state.  Each transition must be labeled with a character of the
  string, except that epsilon transitions may be inserted at any time.  The
  set of final tags associated with a string consists of the tags of all final
  states that can be reached in this way.

  This is not the most "object oriented" possible implementation of a finite
  automaton.  A more object oriented implementation would define classes
  FiniteAutomatonState and FiniteAutomationTransition.  Then, a
  FiniteAutomaton would contain a set of FiniteAutomatonState, each of which
  would contain a set of FiniteAutomationTransition.  Instead, we represent
  states as integers, and we use arrays to hold the transition table and tag
  table.

->*/


public class FiniteAutomaton implements Cloneable 
{

	// A state is represented as a nonnegative integer ranging from 0 to one
	// less than the number of states.  State 0 is the start state.

	// For each state there is an array of int which specifies its transitions.
	// Each transition is represented by two consecutive array elements;  the
	// first element contains the character category (a nonnegative integer)
	// or the constant "epsilon" (which is -1);  the second element contains
	// the target state.

	private int[][] _transitionTable;

	public int[][] transitionTable ()
	{
		return _transitionTable;
	}


	// For each state there is an int which specifies if the state is final,
	// and if so, its tag value.  Each int can contain a final state tag (a
	// nonnegative integer) or the constant "nonfinal" (which is -1).

	private int[] _tagTable;

	public int[] tagTable ()
	{
		return _tagTable;
	}


	// When an NFA is converted into a DFA, each DFA final state represents
	// the union of one or more NFA final states.  This table is created by
	// toDFA().  For each DFA final state s, _mergeTable[_tagTable[s]] is an
	// array listing the tag values of the NFA final states that were merged
	// to form state s.
	//
	// Furthermore, this table is constructed so that _mergeTable[i] and
	// _mergeTable[j] contain the same set of values if and only if i equals j.
	// As a consequence, _mergeTable.length is less than or equal to the number
	// of final states in the DFA.  Also, given two DFA final states s and t,
	// _tagTable[s] equals _tagTable[t] if and only if s and t represent the
	// same set of NFA final states.

	private int[][] _mergeTable;

	public int[][] mergeTable ()
	{
		return _mergeTable;
	}


	// The following is an expanded form of the transition table which can be
	// used only for DFA's, and only when the categories are small nonnegative
	// integers.  For state s and category c, _expandedTable[s][c] contains the
	// target state number, or -1 if there is no transition.  FiniteAutomaton
	// does not use _expandedTable, but it includes a function to create
	// _expandedTable for the benefit of clients who prefer this form.
	//
	// Note that _expandedTable is not restored when reading the finite
	// automaton from a stream.

	private int[][] _expandedTable;

	public int[][] expandedTable ()
	{
		return _expandedTable;
	}


	// This constant is the "character category" for epsilon transitions.

	public static final int epsilon = -1;

	// This constant is the tag value for nonfinal states.

	public static final int nonfinal = -1;

	// This constant is used internally to indicate that an int variable does
	// not contain a tag value.  It does not occur in the tables.

	private static final int notATag = -2;

	// This constant is used internally to indicate that an int variable does
	// not contain a state.  It does not occur in the tables.

	private static final int notAState = -1;

	// Data stream signature

	public static final long streamSignature = 0x4953FF0046413031L;	//"IS..FA01"




	// This private constructor is used by the factory functions to create new
	// incomplete FiniteAutomaton objects.

	private FiniteAutomaton (int states)
	{
		super ();

		// Allocate the top-level transition table array

		_transitionTable = new int[states][];

		// Allocate the tag table array

		_tagTable = new int[states];

		// No merge table

		_mergeTable = null;

		// No expanded table

		_expandedTable = null;

		return;
	}




	// Creates a finite automaton with one state and no transitions,
	// accepting the empty set

	public FiniteAutomaton ()
	{
		super ();

		// Allocate the top-level transition table array

		_transitionTable = new int[1][];

		// Allocate the tag table array

		_tagTable = new int[1];

		// No merge table

		_mergeTable = null;

		// No expanded table

		_expandedTable = null;

		// The start state is a nonfinal state with no transitions

		_tagTable[0] = nonfinal;

		_transitionTable[0] = new int[0];

		return;
	}




	// Constructs an NFA accepting the empty string.  The final state has the
	// specified tag.

	public static FiniteAutomaton makeNFAEmptyString (int tag)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAEmptyString");
		}

		// Create the new NFA object with one state

		FiniteAutomaton newNFA = new FiniteAutomaton (1);

		// The start state is a final state with no transitions

		newNFA._tagTable[0] = tag;

		newNFA._transitionTable[0] = new int[0];

		return newNFA;
	}




	// Constructs an NFA that does not accept any strings.  Since there is
	// no final state, the tag value is not used.

	public static FiniteAutomaton makeNFAEmptySet (int tag)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAEmptyString");
		}

		// Create the new NFA object with one state

		FiniteAutomaton newNFA = new FiniteAutomaton (1);

		// The start state is a nonfinal state with no transitions

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[0];

		return newNFA;
	}




	// Constructs an NFA accepting a one-character string.  The character may
	// belong to any of the specified categories.  The supplied argument is an
	// IntSet containing the categories.  The final state has the specified
	// tag.

	public static FiniteAutomaton makeNFAOneChar (int tag, ConstIntSet categories)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAOneChar");
		}

		// Create the new NFA object with two states

		FiniteAutomaton newNFA = new FiniteAutomaton (2);

		// The start state is a nonfinal state with a transition to state 1 for
		// each category

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[categories.elementCount () * 2];

		IntEnumeration e = categories.elements ();

		for (int i = 0; i < newNFA._transitionTable[0].length; i += 2)
		{

			// Get a character category

			int category = e.nextElement ();

			if (category < 0)
			{
				throw new IllegalArgumentException ("NFA.makeNFAOneChar");
			}

			// Add a transition to state 1 for the category

			newNFA._transitionTable[0][i] = category;
			newNFA._transitionTable[0][i+1] = 1;
		}

		// State 1 is a final state with no transitions

		newNFA._tagTable[1] = tag;

		newNFA._transitionTable[1] = new int[0];

		return newNFA;
	}




	// Copies all the states of the source NFA to the destination NFA.  The
	// start state of the source NFA becomes state dstStart of the
	// destination NFA.
	//
	// dstFinalTag specifies the tag value to be applied to each final state.
	// If dstFinalTag is not "notATag", then each final state is tagged with
	// the value of dstFinalTag.  In particular, if dstFinalTag is "nonfinal"
	// then each final state is converted into a nonfinal state.  If
	// dstFinalTag is "notATag", then final state tags are copied unchanged.
	//
	// dstFinalLink specifies a successor state to each final state.  If
	// dstFinalLink is not "notAState", then this function adds an epsilon
	// transition from each final state to the state dstFinalLink.  If
	// dstFinalLink is "notAState", then no epsilon transitions are added.

	private static void copyStates (FiniteAutomaton srcNFA, FiniteAutomaton dstNFA,
		int dstStart, int dstFinalTag, int dstFinalLink)
	{

		// Loop over states of the source NFA

		for (int i = 0; i < srcNFA._transitionTable.length; ++i)
		{

			// Set tag for this state

			if ((dstFinalTag != notATag) && (srcNFA._tagTable[i] != nonfinal))
			{

				// If we're retagging final states and this state is final,
				// insert the desired new tag

				dstNFA._tagTable[dstStart+i] = dstFinalTag;
			}
			else
			{
				
				// Otherwise, just copy the existing tag

				dstNFA._tagTable[dstStart+i] = srcNFA._tagTable[i];
			}

			// Create transition array for this state

			if ((dstFinalLink != notAState) && (srcNFA._tagTable[i] != nonfinal))
			{

				// If we're adding final state links and this state is final,
				// allocate two extra entries for the epsilon transition

				dstNFA._transitionTable[dstStart+i]
					= new int[srcNFA._transitionTable[i].length + 2];

				// Insert epsilon transition at the end of the table

				dstNFA._transitionTable[dstStart+i][srcNFA._transitionTable[i].length]
					= epsilon;

				dstNFA._transitionTable[dstStart+i][srcNFA._transitionTable[i].length + 1]
					= dstFinalLink;

			}
			else
			{
				
				// Otherwise, no extra entries are needed

				dstNFA._transitionTable[dstStart+i]
					= new int[srcNFA._transitionTable[i].length];
			}

			// Loop to copy transitions for this state

			for (int j = 0; j < srcNFA._transitionTable[i].length; j += 2)
			{

				// Copy the character category (or -1 for epsilon)

				dstNFA._transitionTable[dstStart+i][j] = srcNFA._transitionTable[i][j];

				// Copy the target state, displacing it by dstStart

				dstNFA._transitionTable[dstStart+i][j+1]
					= srcNFA._transitionTable[i][j+1] + dstStart;
			}

		}	// end loop over states of srcNFA

		return;
	}




	// Constructs an NFA accepting the catenation of two given NFA's.  Each
	// string accepted by the NFA consists of a string accepted by NFA1,
	// followed by (i.e., concatenated with) a string accepted by NFA2.  The
	// final state has the specified tag.

	public static FiniteAutomaton makeNFACatenation (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFACatenation");
		}

		// The number of states in the new NFA equals the sum of the number
		// of states in NFA1 and NFA2

		FiniteAutomaton newNFA = new FiniteAutomaton (
			NFA1._transitionTable.length + NFA2._transitionTable.length);

		// The states of NFA1 are copied beginning at state 0.  The start
		// state of NFA1 becomes the start state of the new NFA.  The final
		// states of NFA1 are changed to non-final states, and are given
		// epsilon transitions to the start state of NFA2.

		copyStates (NFA1, newNFA, 0, nonfinal, NFA1._transitionTable.length);

		// The states of NFA2 are copied after the states of NFA1.  The final
		// states are tagged with the specified tag, and are not given any
		// epsilon transitions.

		copyStates (NFA2, newNFA, NFA1._transitionTable.length, tag, notAState);

		return newNFA;
	}




	// Constructs an NFA accepting the alternation of two given NFA's.  Each
	// string accepted by the NFA consists of a string accepted by NFA1, or
	// a string accepted by NFA2.  The final state has the specified tag.

	public static FiniteAutomaton makeNFAAlternation (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAAlternation");
		}

		// The number of states in the new NFA equals the sum of the number
		// of states in NFA1 and NFA2, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (
			NFA1._transitionTable.length + NFA2._transitionTable.length + 1);

		// The start state has two epsilon transitions, one to the start state
		// of NFA1 and one to the start state of NFA2.  It is nonfinal.

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[4];
		
		newNFA._transitionTable[0][0] = epsilon;
		newNFA._transitionTable[0][1] = 1;
		
		newNFA._transitionTable[0][2] = epsilon;
		newNFA._transitionTable[0][3] = NFA1._transitionTable.length + 1;

		// The states of NFA1 are copied beginning at state 1.  The final
		// states are tagged with the specified tag, and are not given any
		// epsilon transitions.

		copyStates (NFA1, newNFA, 1, tag, notAState);

		// The states of NFA2 are copied after the states of NFA1.  The final
		// states are tagged with the specified tag, and are not given any
		// epsilon transitions.

		copyStates (NFA2, newNFA, NFA1._transitionTable.length + 1, tag, notAState);

		return newNFA;
	}




	// Constructs an NFA accepting the alternation of the given NFA and the
	// empty string.  Each string accepted by the NFA is either a string
	// accepted by NFA1, or the empty string.  The final state has the
	// specified tag.

	public static FiniteAutomaton makeNFAOptional (int tag, FiniteAutomaton NFA1)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAOptional");
		}

		// The number of states in the new NFA equals the the number of states
		// in NFA1, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (NFA1._transitionTable.length + 1);

		// The start state has an epsilon transition to the start state of
		// NFA1.  It is a final state with the specified tag.

		newNFA._tagTable[0] = tag;

		newNFA._transitionTable[0] = new int[2];
		
		newNFA._transitionTable[0][0] = epsilon;
		newNFA._transitionTable[0][1] = 1;

		// The states of NFA1 are copied beginning at state 1.  The final
		// states are tagged with the specified tag, and are not given any
		// epsilon transitions.

		copyStates (NFA1, newNFA, 1, tag, notAState);

		return newNFA;
	}




	// Constructs an NFA accepting the Kleene closure of the given NFA.  Each
	// string accepted by the NFA consists of the concatenation of zero or
	// more strings that are accepted by NFA1.  The final state has the
	// specified tag.

	public static FiniteAutomaton makeNFAKleene (int tag, FiniteAutomaton NFA1)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAKleene");
		}

		// The number of states in the new NFA equals the the number of states
		// in NFA1, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (NFA1._transitionTable.length + 1);

		// The start state has an epsilon transition to the start state of
		// NFA1.  It is a final state with the specified tag.

		newNFA._tagTable[0] = tag;

		newNFA._transitionTable[0] = new int[2];
		
		newNFA._transitionTable[0][0] = epsilon;
		newNFA._transitionTable[0][1] = 1;

		// The states of NFA1 are copied beginning at state 1.  The final
		// states of NFA1 are changed to non-final states, and are given
		// epsilon transitions to state 0.

		copyStates (NFA1, newNFA, 1, nonfinal, 0);

		return newNFA;
	}




	// Constructs an NFA accepting the positive closure of the given NFA.
	// Each string accepted by the NFA consists of the concatenation of one or
	// more strings that are accepted by NFA1.  The final state has the
	// specified tag.

	public static FiniteAutomaton makeNFAPositive (int tag, FiniteAutomaton NFA1)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAPositive");
		}

		// The number of states in the new NFA equals the the number of states
		// in NFA1, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (NFA1._transitionTable.length + 1);

		// The states of NFA1 are copied beginning at state 0.  The start
		// state of NFA1 becomes the start state of the new NFA.  The final
		// states of NFA1 are changed to non-final states, and are given
		// epsilon transitions to the new NFA's final state.

		copyStates (NFA1, newNFA, 0, nonfinal, NFA1._transitionTable.length);

		// The new NFA's final state has an epsilon transition back to the
		// start state.

		newNFA._tagTable[NFA1._transitionTable.length] = tag;

		newNFA._transitionTable[NFA1._transitionTable.length] = new int[2];
		
		newNFA._transitionTable[NFA1._transitionTable.length][0] = epsilon;
		newNFA._transitionTable[NFA1._transitionTable.length][1] = 0;

		return newNFA;
	}




	// Constructs an NFA accepting the alternation of a set of given NFA's.
	// Each string accepted by the NFA consists of a string accepted by one
	// (or more) of the given NFA's.  The resulting NFA has multiple final
	// states, which retain the tags of the given NFA's;  thus, the final
	// state tags encode which of the given NFA's was matched.

	public static FiniteAutomaton makeNFATaggedAlternation (FiniteAutomaton[] NFAArray)
	{

		// Validate the argument

		if (NFAArray.length == 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFATaggedAlternation");
		}

		// The number of states in the new NFA equals the sum of the number
		// of states in the given NFA's, plus 1.

		int totalStates = 0;
		for (int i = 0; i < NFAArray.length; ++i)
		{
			totalStates += NFAArray[i]._transitionTable.length;
		}

		FiniteAutomaton newNFA = new FiniteAutomaton (totalStates + 1);

		// The start state has an epsilon transition to the start state of
		// each of the given NFA's.  It is nonfinal.

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[NFAArray.length * 2];

		// Begin inserting given NFA's at state 1

		int currentState = 1;

		// Process each of the given NFA's

		for (int i = 0; i < NFAArray.length; ++i)
		{

			// Insert an epsilon transition from state 0 to this NFA's
			// start state
			
			newNFA._transitionTable[0][i*2] = epsilon;
			newNFA._transitionTable[0][(i*2) + 1] = currentState;

			// Copy this NFA's state beginning at currentState, without
			// altering the final states

			copyStates (NFAArray[i], newNFA, currentState, notATag, notAState);

			// Advance current state past this NFA's states

			currentState += NFAArray[i]._transitionTable.length;
		}

		return newNFA;
	}




	// Constructs an NFA accepting the catenation of a set of given NFA's.
	// Each string accepted by the NFA consists of the concatenation of strings
	// accepted by the given NFA's, in the given order.  The resulting NFA has
	// multiple final states, which retain the tags of the given NFA's;  thus,
	// the final state tags encode the locations of initial substrings that
	// match the catenation of initial subsequences of the given NFA's.

	public static FiniteAutomaton makeNFATaggedCatenation (FiniteAutomaton[] NFAArray)
	{

		// Validate the argument

		if (NFAArray.length == 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFATaggedCatenation");
		}

		// The number of states in the new NFA equals the sum of the number
		// of states in the given NFA's

		int totalStates = 0;
		for (int i = 0; i < NFAArray.length; ++i)
		{
			totalStates += NFAArray[i]._transitionTable.length;
		}

		FiniteAutomaton newNFA = new FiniteAutomaton (totalStates);

		// Begin inserting given NFA's at state 0

		int currentState = 0;

		// Process each of the given NFA's

		for (int i = 0; i < NFAArray.length; ++i)
		{

			// Copy this NFA's states beginning at currentState, without
			// altering the final states.  If this isn't the last NFA, add
			// epsilon transitions from the final states to the start state
			// of the next NFA.

			copyStates (NFAArray[i], newNFA, currentState, notATag,
				(i == (NFAArray.length - 1))
				? notAState
				: (currentState + NFAArray[i]._transitionTable.length) );

			// Advance current state past this NFA's states

			currentState += NFAArray[i]._transitionTable.length;
		}

		return newNFA;
	}




	// Constructs an NFA accepting the difference of two given NFA's.  Each
	// string accepted by the NFA consists of a string that is accepted by
	// NFA1 and not accepted by NFA2.  The final state has the specified tag.
	//
	// Notes:  Unlike other factory functions which merely link NFA's together,
	// this function must perform extensive work including an NFA-to-DFA
	// conversion.  The resulting finite automaton happens to be a DFA.

	public static FiniteAutomaton makeNFADifference (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFADifference");
		}

		// First we construct a new NFA that represents the alternation of NFA1
		// and NFA2.  NFA1 is tagged 1, and NFA2 is tagged 0.

		// The number of states in the new NFA equals the sum of the number
		// of states in NFA1 and NFA2, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (
			NFA1._transitionTable.length + NFA2._transitionTable.length + 1);

		// The start state has two epsilon transitions, one to the start state
		// of NFA1 and one to the start state of NFA2.  It is nonfinal.

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[4];
		
		newNFA._transitionTable[0][0] = epsilon;
		newNFA._transitionTable[0][1] = 1;
		
		newNFA._transitionTable[0][2] = epsilon;
		newNFA._transitionTable[0][3] = NFA1._transitionTable.length + 1;

		// The states of NFA1 are copied beginning at state 1.  The final
		// states are tagged with the value 1, and are not given any
		// epsilon transitions.

		copyStates (NFA1, newNFA, 1, 1, notAState);

		// The states of NFA2 are copied after the states of NFA1.  The final
		// states are tagged with the value 0, and are not given any
		// epsilon transitions.

		copyStates (NFA2, newNFA, NFA1._transitionTable.length + 1, 0, notAState);

		// Next, convert the new NFA into a DFA

		newNFA.toDFA ();

		// Select the minimum tag values for the DFA

		newNFA.selectMinimumTags ();

		// This DFA accepts every string that is accepted by either NFA1 or
		// NFA2.  The final tag values in the DFA are as follows:
		//
		//		Accepted by NFA1?		Accepted by NFA2?		DFA Tag value
		//			No						No						"nonfinal" or error
		//			No						Yes						0
		//			Yes						No						1
		//			Yes						Yes						0
		//
		// It is seen that a string is in the difference of NFA1 and NFA2 iff
		// the DFA accepts the string with a tag value of 1.  Therefore, we
		// modify the DFA tag table, replacing each 1 with the specified tag,
		// and replacing each 0 with "nonfinal".

		for (int state = 0; state < newNFA._tagTable.length; ++state)
		{
			if (newNFA._tagTable[state] == 1)
			{
				newNFA._tagTable[state] = tag;
			}
			else
			{
				newNFA._tagTable[state] = nonfinal;
			}
		}

		// Now minimize the number of states

		newNFA.minimizeDFAStates ();

		return newNFA;
	}




	// Constructs an NFA accepting the intersection of two given NFA's.  Each
	// string accepted by the NFA consists of a string that is accepted by
	// both NFA1 and NFA2.  The final state has the specified tag.
	//
	// Notes:  Unlike other factory functions which merely link NFA's together,
	// this function must perform extensive work including an NFA-to-DFA
	// conversion.  The resulting finite automaton happens to be a DFA.

	public static FiniteAutomaton makeNFAIntersection (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAIntersection");
		}

		// First we construct a new NFA that represents the alternation of NFA1
		// and NFA2.  NFA1 is tagged 1, and NFA2 is tagged 2.

		// The number of states in the new NFA equals the sum of the number
		// of states in NFA1 and NFA2, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (
			NFA1._transitionTable.length + NFA2._transitionTable.length + 1);

		// The start state has two epsilon transitions, one to the start state
		// of NFA1 and one to the start state of NFA2.  It is nonfinal.

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[4];
		
		newNFA._transitionTable[0][0] = epsilon;
		newNFA._transitionTable[0][1] = 1;
		
		newNFA._transitionTable[0][2] = epsilon;
		newNFA._transitionTable[0][3] = NFA1._transitionTable.length + 1;

		// The states of NFA1 are copied beginning at state 1.  The final
		// states are tagged with the value 1, and are not given any
		// epsilon transitions.

		copyStates (NFA1, newNFA, 1, 1, notAState);

		// The states of NFA2 are copied after the states of NFA1.  The final
		// states are tagged with the value 2, and are not given any
		// epsilon transitions.

		copyStates (NFA2, newNFA, NFA1._transitionTable.length + 1, 2, notAState);

		// Next, convert the new NFA into a DFA

		newNFA.toDFA ();

		// Select the logical-or tag values for the DFA

		newNFA.selectLogicalOrTags ();

		// This DFA accepts every string that is accepted by either NFA1 or
		// NFA2.  The final tag values in the DFA are as follows:
		//
		//		Accepted by NFA1?		Accepted by NFA2?		DFA Tag value
		//			No						No						"nonfinal" or error
		//			No						Yes						2
		//			Yes						No						1
		//			Yes						Yes						3
		//
		// It is seen that a string is in the intersection of NFA1 and NFA2 iff
		// the DFA accepts the string with a tag value of 3.  Therefore, we
		// modify the DFA tag table, replacing each 3 with the specified tag,
		// and replacing every other value with "nonfinal".

		for (int state = 0; state < newNFA._tagTable.length; ++state)
		{
			if (newNFA._tagTable[state] == 3)
			{
				newNFA._tagTable[state] = tag;
			}
			else
			{
				newNFA._tagTable[state] = nonfinal;
			}
		}

		// Now minimize the number of states

		newNFA.minimizeDFAStates ();

		return newNFA;
	}




	// Constructs an NFA accepting strings of NFA1 that include strings of
	// NFA2.  Each string accepted by the NFA consists of a string that is
	// accepted by NFA1 which contains at least one substring accepted by NFA2.
	// The final state has the specified tag.
	//
	// Notes:  Unlike other factory functions which merely link NFA's together,
	// this function must perform extensive work including an NFA-to-DFA
	// conversion.  The resulting finite automaton happens to be a DFA.

	public static FiniteAutomaton makeNFAIncluding (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAIncluding");
		}

		// First we construct an NFA that represents any string in the
		// vocabulary of NFA1.

		FiniteAutomaton V1 = 
			makeNFAKleene (tag,
				makeNFAOneChar (tag, NFA1.getCategories()) );

		// The desired NFA is the intersection of NFA1 with the catenation
		// V1 NFA2 V1.

		return makeNFAIntersection (tag, NFA1,
			makeNFACatenation (tag, makeNFACatenation (tag, V1, NFA2), V1) );
	}




	// Constructs an NFA accepting strings of NFA1 that exclude strings of
	// NFA2.  Each string accepted by the NFA consists of a string that is
	// accepted by NFA1 which does not contain any substring accepted by NFA2.
	// the final state has the specified tag.
	//
	// Notes:  Unlike other factory functions which merely link NFA's together,
	// this function must perform extensive work including an NFA-to-DFA
	// conversion.  The resulting finite automaton happens to be a DFA.

	public static FiniteAutomaton makeNFAExcluding (int tag,
		FiniteAutomaton NFA1, FiniteAutomaton NFA2)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAExcluding");
		}

		// First we construct an NFA that represents any string in the
		// vocabulary of NFA1.

		FiniteAutomaton V1 = 
			makeNFAKleene (tag,
				makeNFAOneChar (tag, NFA1.getCategories()) );

		// The desired NFA is the difference of NFA1 with the catenation
		// V1 NFA2 V1.

		return makeNFADifference (tag, NFA1,
			makeNFACatenation (tag, makeNFACatenation (tag, V1, NFA2), V1) );
	}




	// Constructs an NFA accepting the reverse of the given NFA.  Each string
	// accepted by the NFA consists of the character-reversed form of a string
	// that is accepted by NFA1.  The final state has the specified tag.

	public static FiniteAutomaton makeNFAReverse (int tag, FiniteAutomaton NFA1)
	{

		// Validate the argument

		if (tag < 0)
		{
			throw new IllegalArgumentException ("NFA.makeNFAReverse");
		}

		// The number of states in the new NFA equals the the number of states
		// in NFA1, plus 1

		FiniteAutomaton newNFA = new FiniteAutomaton (NFA1._transitionTable.length + 1);

		// The start state is a nonfinal state with no transitions

		newNFA._tagTable[0] = nonfinal;

		newNFA._transitionTable[0] = new int[0];

		// The states of NFA1 are copied beginning at state 1.  The final
		// states of NFA1 are changed to non-final states, and are given
		// epsilon transitions to state 0.

		copyStates (NFA1, newNFA, 1, nonfinal, 0);

		// State 1, the start state of NFA1, is our final state.

		newNFA._tagTable[1] = tag;

		// Construct a new transition table, where all arrows in the
		// transition graph are reversed.

		int[][] newTransitionTable = new int[newNFA._transitionTable.length][];

		// For each state in the new transition table ...

		for (int state = 0; state < newTransitionTable.length; ++state)
		{

			// Count the number of old transitions to this state

			int tIndex = 0;

			// Loop over states of the existing NFA

			for (int i = 0; i < newNFA._transitionTable.length; ++i)
			{

				// Loop over old transitions for this state

				for (int j = 0; j < newNFA._transitionTable[i].length; j += 2)
				{

					// If target state is our state, count it

					if (newNFA._transitionTable[i][j+1] == state)
					{
						tIndex += 2;
					}
				}
			}

			// Allocate new transition array for this state

			newTransitionTable[state] = new int[tIndex];

			// Index into new transition array

			tIndex = 0;

			// Loop over states of the existing NFA

			for (int i = 0; i < newNFA._transitionTable.length; ++i)
			{

				// Loop over old transitions for this state

				for (int j = 0; j < newNFA._transitionTable[i].length; j += 2)
				{

					// If target state is our state ...

					if (newNFA._transitionTable[i][j+1] == state)
					{

						// Copy the character category

						newTransitionTable[state][tIndex++] = newNFA._transitionTable[i][j];

						// New target state is old source state

						newTransitionTable[state][tIndex++] = i;
					}
				}
			}

		}	// end loop over states in new transition table

		// Insert new transition table into new NFA

		newNFA._transitionTable = newTransitionTable;

		return newNFA;
	}




	// Given a set of NFA states, adds all NFA states reachable by a sequence
	// of one or more epsilon transitions.

	public void epsilonClosure (IntSet NFAStates)
	{

		// Enumerate over the set.  This code uses the fact that an IntSet
		// enumerator returns elements added during the enumeration.

		for (IntEnumeration e = NFAStates.elements (); e.hasMoreElements (); )
		{

			// Get the next state to check

			int state = e.nextElement ();

			// Scan the transition table for this state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If this transition is an epsilon transition ...

				if (_transitionTable[state][i] == epsilon)
				{

					// ... add the target state to the set of states

					NFAStates.addElement (_transitionTable[state][i+1]);
				}
			}
		}

		return;
	}




	// Returns the set of all character categories used in the finite
	// automaton.

	public IntSet getCategories ()
	{

		// Create an empty set

		IntSet categories = new IntSet ();

		// Loop over all states

		for (int state = 0; state < _transitionTable.length; ++state)
		{

			// Scan the transition table for this state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If this transition is not an epsilon transition ...

				if (_transitionTable[state][i] != epsilon)
				{

					// ... add the category to the set of categories

					categories.addElement (_transitionTable[state][i]);
				}
			}
		}

		return categories;
	}




	// Returns the NFAToDFAState object corresponsing to a set of NFA states.
	// This is a subroutine used during DFA construction.
	//
	// NFAStates is a set of NFA states.  This is modified, and it is retained
	// if a new NFAToDFAState object is created.  Therefore, a new IntSet
	// should be created for each call to this function.
	//
	// DFAStates is the set of NFAToDFAState objects already created.  If
	// there is a matching NFAToDFAState in this set, the existing object is
	// returned.  Otherwise, a new object is created and added to the set.
	// If DFAStates is the empty set, this function creates an object that
	// is the start state of the DFA.

	private NFAToDFAState getDFAState (IntSet NFAStates, ObjectSet DFAStates)
	{

		// First get the epsilon closure of the set of NFA states

		epsilonClosure (NFAStates);

		// Accumulate the NFA final state tags.  At the same time, remove all
		// states that do not have any non-epsilon transitions.

		IntSet NFAFinalTags = new IntSet ();

		for (IntEnumeration e = NFAStates.elements (); e.hasMoreElements (); )
		{

			// Get the next state to check

			int state = e.nextElement ();

			// If this is a final state

			if (_tagTable[state] != nonfinal)
			{

				// Accumulate the NFA final tag

				NFAFinalTags.addElement (_tagTable[state]);
			}

			// If the state has no non-epsilon transitions, remove it

			checkEpsilonState:
			{

				// Scan the transition table for this state

				for (int i = 0; i < _transitionTable[state].length; i += 2)
				{

					// If this transition is not an epsilon transition ...

					if (_transitionTable[state][i] != epsilon)
					{

						// ... this isn't an "epsilon state"

						break checkEpsilonState;
					}
				}

				// Remove state from set

				NFAStates.removeElement (state);
			}
		}

		// Scan the set of existing DFA states to determine if this state
		// already exists

		for (Enumeration e = DFAStates.elements (); e.hasMoreElements (); )
		{

			// Get the next state to check

			NFAToDFAState oldState = (NFAToDFAState) e.nextElement ();

			// If the set of final tags matches ...

			if (NFAFinalTags.equals (oldState.NFAFinalTags))
			{

				// ... and the set of NFA states matches ...

				if (NFAStates.equals (oldState.NFAStates))
				{

					// ... then return the existing object

					return oldState;
				}
			}
		}

		// We need to create a new DFA state

		NFAToDFAState newState = new NFAToDFAState (NFAStates,
			DFAStates.elementCount (), NFAFinalTags);

		// Add the new state to the set

		DFAStates.addElement (newState);

		// Return the new state

		return newState;
	}




	// Returns a DFA that accepts the same strings as the NFA.
	//
	// Each DFA final state corresponds to a set of one or more of the NFA's
	// final states.  The NFA's final tag values are preserved in _mergeTable.
	// Each DFA final tag value is an index into _mergeTable, identifying the
	// corresponding set of NFA final state tags.
	//
	// The optional GeneratorStatus parameter can be used to monitor the progress
	// of, and send interrupts to, this function.  If the function is
	// interrupted, the FiniteAutomaton object remains unchanged.

	public void toDFA ()
	{
		try
		{
			toDFA (null);
		}
		catch (InterruptedCompilerException e)
		{
			throw new InternalError();
		}
		return;
	}
	

	public void toDFA (GeneratorStatus generatorStatus) throws InterruptedCompilerException
	{

		// Get the set of character categories used in the NFA

		IntSet categories = getCategories ();

		// Create a set of DFA states and initialize it to the empty set

		ObjectSet DFAStates = new ObjectSet ();

		// An array to use to construct DFA state transition tables.  Its size
		// is the maximum possible.

		int[] DFATransitions = new int[categories.elementCount () * 2];

		// Create a set containing the NFA start states (only state 0)

		IntSet NFAStartStates = new IntSet ();
		NFAStartStates.addElement (0);

		// The DFA start state is the state corresponding to the NFA start states

		getDFAState (NFAStartStates, DFAStates);

		// The set of start states is retained by getDFAState, so we should
		// set our reference to null so we can't access it any more

		NFAStartStates = null;

		// Scan all DFA states and calculate their transitions.  This code
		// uses the fact that an ObjectSet enumerator returns elements added
		// during the enumeration.

		for (Enumeration de = DFAStates.elements (); de.hasMoreElements (); )
		{
			
			// Report progress
			
			if (generatorStatus != null)
			{
				generatorStatus.statusWork ();
			}

			// Get the next DFA state to check

			NFAToDFAState dState = (NFAToDFAState) de.nextElement ();

			// Index into transition table

			int tIndex = 0;

			// Scan all character categories

			for (IntEnumeration ce = categories.elements (); ce.hasMoreElements (); )
			{

				// Get the next category to check

				int category = ce.nextElement ();

				// Create a set to hold the target NFA states

				IntSet targetNFAStates = new IntSet ();

				// Scan all NFA states within this DFA state

				for (IntEnumeration ne = dState.NFAStates.elements (); ne.hasMoreElements (); )
				{

					// Get the next NFA state to check

					int nState = ne.nextElement ();

					// Scan the transition table for this NFA state

					for (int i = 0; i < _transitionTable[nState].length; i += 2)
					{

						// If this transition is for the current category

						if (_transitionTable[nState][i] == category)
						{

							// ... record the target state

							targetNFAStates.addElement (_transitionTable[nState][i+1]);
						}
					}
				}

				// If there is a transition for this category ...

				if (!targetNFAStates.isEmpty ())
				{

					// ... record the transition, creating new DFA state if necessary

					DFATransitions[tIndex++] = category;
					DFATransitions[tIndex++]
						= getDFAState (targetNFAStates, DFAStates).DFAState;
				}

			}	// end scan of character categories

			// Allocate transition table for this DFA state

			dState.DFATransitions = new int[tIndex];

			// Copy transition table from working buffer

			if (tIndex > 0)
			{
				System.arraycopy (DFATransitions, 0, dState.DFATransitions, 0, tIndex);
			}


		}	// end scan of DFA states

		// Allocate arrays for DFA transition and tag tables

		int[] DFATagTable = new int[DFAStates.elementCount ()];

		int[][] DFATransitionTable = new int[DFAStates.elementCount ()][];

		// Allocate an array to hold the merged NFA final tag sets.  Its length
		// is the maximum possible, and mergeCount is the current length.

		IntSet[] NFAFinalTagSets = new IntSet[DFAStates.elementCount ()];

		int mergeCount = 0;

		// Scan all DFA states, copying transitions and constructing final tags

		for (Enumeration de = DFAStates.elements (); de.hasMoreElements (); )
		{

			// Get the next DFA state to check

			NFAToDFAState dState = (NFAToDFAState) de.nextElement ();

			// Copy the transition table

			DFATransitionTable [dState.DFAState] = dState.DFATransitions;

			// If there are no NFA final state tags ...

			if (dState.NFAFinalTags.isEmpty())
			{

				// Set tag for a nonfinal state

				DFATagTable [dState.DFAState] = nonfinal;
			}

			// Otherwise, this is a final state ...

			else
			{

				// Find the DFA tag value for this state

				int tagValue;

				findMatchingSet:
				{

					// Seach array for an existing tag set that matches

					for (tagValue = 0; tagValue < mergeCount; ++tagValue)
					{
						if (dState.NFAFinalTags.equals (NFAFinalTagSets[tagValue]))
						{
							break findMatchingSet;
						}
					}

					// None found, add new set to array

					tagValue = mergeCount++;

					NFAFinalTagSets[tagValue] = dState.NFAFinalTags;
				}

				// Set tag for a final state

				DFATagTable [dState.DFAState] = tagValue;
			}
		}

		// Allocate array for DFA merge table

		int[][] DFAMergeTable = new int[mergeCount][];

		// Convert each final tag set into an array

		for (int m = 0; m < mergeCount; ++m)
		{

			// Allocate an array

			DFAMergeTable[m] = new int[NFAFinalTagSets[m].elementCount()];

			// Enumerate the final tag set

			IntEnumeration me = NFAFinalTagSets[m].elements();

			// Insert elements into array

			for (int i = 0; i < DFAMergeTable[m].length; ++i)
			{
				DFAMergeTable[m][i] = me.nextElement();
			}
		}

		// Become a DFA

		_tagTable = DFATagTable;

		_transitionTable = DFATransitionTable;

		_mergeTable = DFAMergeTable;

		_expandedTable = null;

		return;
	}




	// Checks to see if this finite automaton is a DFA, and returns true
	// if it is.

	public boolean isDFA ()
	{

		// Loop over all states

		for (int state = 0; state < _transitionTable.length; ++state)
		{

			// Scan the transition table for this state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If this is not an epsilon transition, not a DFA

				if (_transitionTable[state][i] == epsilon)
				{
					return false;
				}

				// Scan all prior transitions

				for (int j = 0; j < i; j += 2)
				{

					// If same character category, not a DFA

					if (_transitionTable[state][i] == _transitionTable[state][j])
					{
						return false;
					}
				}
			}
		}

		return true;
	}




	// For each state of the finite automaton, this function sorts the
	// transitions into ascending order by character category, with epsilon
	// transitions listed first.  Transitions with the same character category
	// are sorted by ascending order in target state.  The resulting finite
	// automaton is logically identical to the original.
	//
	// If there is a _mergeTable, then each tag list is sorted into ascending
	// order by tag value.

	public void sortTransitions ()
	{

		// Loop over all states

		for (int state = 0; state < _transitionTable.length; ++state)
		{

			// We just use a simple bubble sort

			for (int i = 0; (i + 2) < _transitionTable[state].length; i += 2)
			{

				// Index of smallest element so far

				int k = i;

				// Check remaining elements, remembering the smallest one

				for (int j = i + 2; j < _transitionTable[state].length; j += 2)
				{

					// Check if this is a new smallest element

					if (
						   (_transitionTable[state][j] < _transitionTable[state][k])
						|| (
							   (_transitionTable[state][j] == _transitionTable[state][k])
							&& (_transitionTable[state][j+1] < _transitionTable[state][k+1])
						   )
					   )
					{
						k = j;
					}
				}

				// Swap the smallest element into position i

				int swapper;

				swapper = _transitionTable[state][k];
				_transitionTable[state][k] = _transitionTable[state][i];
				_transitionTable[state][i] = swapper;

				swapper = _transitionTable[state][k+1];
				_transitionTable[state][k+1] = _transitionTable[state][i+1];
				_transitionTable[state][i+1] = swapper;

			}	// end bubble sort

		}	// end loop over states

		// If there is a merge table ...

		if (_mergeTable != null)
		{

			// Loop over all merged tags

			for (int m = 0; m < _mergeTable.length; ++m)
			{

				// We just use a simple bubble sort

				for (int i = 0; (i + 1) < _mergeTable[m].length; ++i)
				{

					// Index of smallest element so far

					int k = i;

					// Check remaining elements, remembering the smallest one

					for (int j = i + 1; j < _mergeTable[m].length; ++j)
					{

						// Check if this is a new smallest element

						if (_mergeTable[m][j] < _mergeTable[m][k])
						{
							k = j;
						}
					}

					// Swap the smallest element into position i

					int swapper;

					swapper = _mergeTable[m][k];
					_mergeTable[m][k] = _mergeTable[m][i];
					_mergeTable[m][i] = swapper;

				}	// end bubble sort

			}	// end loop over merged tags
		}

		return;
	}




	// Given a partition table and a table of refinement values, this function
	// refines the partition so that any two states in the same partition
	// have the same refinement value.  Returns true if the partition table
	// was modified, false otherwise.  This is an internal subroutine.
	//
	// It is assumed that partitionTable[s] contains the numerical value of
	// the first state (counting from 0) which is in the same partition as
	// state s.  This condition is preserved on return.

	private boolean refinePartition (int[] partitionTable, int[] refinementValues)
	{

		// Flag indicates if anything has changed

		boolean isChanged = false;

		// Scan the partition table from end to beginning

		for (int state = partitionTable.length - 1; state >= 0; --state)
		{

			// Find the first state, in the same partition, with the same
			// refinement value;  this is the new partition

			for (int i = 0; ; ++i)
			{

				// Check for same partition and refinement value;  this will
				// always occur for some i <= state

				if ((partitionTable[i] == partitionTable[state])
					&& (refinementValues[i] == refinementValues[state]))
				{

					// If we need to change partition, record the change

					if (partitionTable[state] != i)
					{
						partitionTable[state] = i;
						isChanged = true;
					}

					// End of search for new partition

					break;
				}
			}
		}

		return isChanged;
	}




	// Given a DFA, this function attempts to minimize the number of states
	// in the DFA by coalescing existing states, and then discarding any
	// unreachable states.  The finite automaton must be a DFA.
	//
	// If the DFA has a merge table, then this function deletes any elements of
	// the merge table that are unreachable.  This can cause the numerical
	// values of the DFA final tags to change.  If the DFA does not have a
	// merge table, then the values of the DFA final tags are unchanged.
	//
	// The optional GeneratorStatus parameter can be used to monitor the progress
	// of, and send interrupts to, this function.  If the function is
	// interrupted, the FiniteAutomaton object remains unchanged.

	public void minimizeDFAStates ()
	{
		try
		{
			minimizeDFAStates (null);
		}
		catch (InterruptedCompilerException e)
		{
			throw new InternalError();
		}
		return;
	}
		

	public void minimizeDFAStates (GeneratorStatus generatorStatus)
		throws InterruptedCompilerException
	{

		// Check that this is a DFA

		if (!isDFA ())
		{
			throw new IllegalArgumentException ("FiniteAutomaton.minimizeDFAStates");
		}

		// Get the set of character categories used in the DFA

		IntSet categories = getCategories ();

		// Allocate an array to use for the partition table.  Two states s1
		// and s2 are in the same partition iff partitionTable[s1] equals
		// partitionTable[s2].  For a given state s, the numerical value of
		// partitionTable[s] is the first state (counting from 0) in the same
		// partition as state s.  If s==partitionTable[s], then state s is
		// said to represent the partition.
		//
		// There is a fictitious "dead state" whose numerical value is
		// _transitionTable.length.  Every state s is considered to transition
		// to the dead state on every character category not explicitly
		// listed in _transitionTable[s].  Also, the dead state is considered
		// to transition to itself on every character category.

		int[] partitionTable = new int[_transitionTable.length + 1];

		// Initially, all states are in partition 0, the partition
		// represented by the start state

		for (int i = 0; i < partitionTable.length; ++i)
		{
			partitionTable[i] = 0;
		}

		// Allocate an array to use for refinement values

		int[] refinementValues = new int[_transitionTable.length + 1];

		// First, we need to refine the partition table according to the
		// final state tags.  This ensures that all states in a partition
		// have the same tag value.  The dead state is considered to have
		// the tag value "nonfinal".

		for (int i = 0; i < _tagTable.length; ++i)
		{
			refinementValues[i] = _tagTable[i];
		}

		refinementValues[_tagTable.length] = nonfinal;

		refinePartition (partitionTable, refinementValues);

		// Do the following repeatedly until the partition table stops changing

		boolean isChanged = true;
		
		while (isChanged)
		{

			// Nothing changed yet

			isChanged = false;
			
			// Report progress
			
			if (generatorStatus != null)
			{
				generatorStatus.statusWork ();
			}

			// Scan all character categories, refining once for each category

			for (IntEnumeration ce = categories.elements (); ce.hasMoreElements (); )
			{

				// Get the next category to check

				int category = ce.nextElement ();

				// For each state, fill in refinementValue[state] with the
				// partition of the target state for the transition by
				// category.  If the partition table is unchanged under this
				// refinement, it means that transition by category is well-
				// defined for partitions (i.e., every state in a given
				// partition transitions into the same target partition).

				// First initialize all the refinement values to the partition
				// of the dead state

				for (int i = 0; i < refinementValues.length; ++i)
				{
					refinementValues[i] = partitionTable[_transitionTable.length];
				}

				// Loop over all states

				for (int state = 0; state < _transitionTable.length; ++state)
				{

					// Scan the transition table for this state

					for (int i = 0; i < _transitionTable[state].length; i += 2)
					{

						// If we found a transition for the current category ...

						if (_transitionTable[state][i] == category)
						{

							// ... Record the partition for the target state

							refinementValues[state]
								= partitionTable[_transitionTable[state][i+1]];

							break;
						}
					}
				}

				// Refine the partition, and remember if anything changed

				isChanged |= refinePartition (partitionTable, refinementValues);

			}	// end scan over categories

		}	// end loop until nothing changes
			
		// Report progress
			
		if (generatorStatus != null)
		{
			generatorStatus.statusWork ();
		}

		// Create a set of reachable partitions

		IntSet reachablePartitions = new IntSet ();

		// Add partition 0 (the partition represented by the start state) to the set

		reachablePartitions.addElement (0);

		// For each partition in the set, add all partitions reachable from it.
		// However, don't add the partition of the dead state.  This code uses
		// the fact that an IntSet enumerator returns elements added during
		// the enumeration.

		for (IntEnumeration e = reachablePartitions.elements (); e.hasMoreElements (); )
		{

			// Get the next state (representing a partition) to check

			int state = e.nextElement ();

			// Scan the transition table for this state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If the transition is not to the dead state's partition ...

				if (partitionTable[_transitionTable[state][i+1]]
					!= partitionTable[_transitionTable.length])
				{

					// ... Add the target partition to the set

					reachablePartitions.addElement (
						partitionTable[_transitionTable[state][i+1]]);
				}
			}
		}
			
		// Report progress
			
		if (generatorStatus != null)
		{
			generatorStatus.statusWork ();
		}

		// The reachable partitions are the states of the new DFA.  Construct
		// an array that, for each reachable partition, gives the new DFA state
		// number assigned to that partition.

		int[] newDFAStates = new int[_transitionTable.length];

		int newDFAStateCount = 0;

		// Scan the set of reachable partitions

		for (IntEnumeration e = reachablePartitions.elements (); e.hasMoreElements (); )
		{

			// Get the next partition

			int partition = e.nextElement ();

			// Assign a new DFA state number to this partition

			newDFAStates[partition] = newDFAStateCount++;
		}
			
		// Report progress
			
		if (generatorStatus != null)
		{
			generatorStatus.statusWork ();
		}

		// Allocate arrays for new DFA transition and tag tables

		int[] newDFATagTable = new int[newDFAStateCount];

		int[][] newDFATransitionTable = new int[newDFAStateCount][];

		// Scan the set of reachable partitions, which is also the set of
		// new DFA states.  For each partition, save the final tag value and
		// create the new DFA transition table.

		for (IntEnumeration e = reachablePartitions.elements (); e.hasMoreElements (); )
		{

			// Get the next state (representing a partition) to check

			int state = e.nextElement ();

			// Save the final tag value

			newDFATagTable[newDFAStates[state]] = _tagTable[state];

			// Scan the transition table for this state, to get the length of
			// the new DFA state's transition table

			int tIndex = 0;

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If the transition is not to the dead state's partition ...

				if (partitionTable[_transitionTable[state][i+1]]
					!= partitionTable[_transitionTable.length])
				{

					// ... Count the transition

					tIndex += 2;
				}
			}

			// Create the transition table for this state

			newDFATransitionTable[newDFAStates[state]] = new int[tIndex];

			// Scan the transition table for this state, and build the new
			// DFA state's transition table

			tIndex = 0;

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If the transition is not to the dead state's partition ...

				if (partitionTable[_transitionTable[state][i+1]]
					!= partitionTable[_transitionTable.length])
				{

					// ... Enter the transition into the table

					newDFATransitionTable[newDFAStates[state]][tIndex++]
						= _transitionTable[state][i];
					newDFATransitionTable[newDFAStates[state]][tIndex++]
						= newDFAStates[partitionTable[_transitionTable[state][i+1]]];
				}
			}

		}
			
		// Report progress
			
		if (generatorStatus != null)
		{
			generatorStatus.statusWork ();
		}

		// If there is a merge table, we need to remove any entries that are
		// no longer referenced in the tag table.  This can occur if the
		// original DFA has an unreachable final state.

		int[][] newDFAMergeTable = null;

		if (_mergeTable != null)
		{

			// Allocate an array to map existing tag values into new tag values

			int[] newTagValues = new int[_mergeTable.length];

			// The number of different tag values we have seen so far

			int mergeCount = 0;

			// Use "nonfinal" to signal an existing tag value not yet encountered

			for (int oldTag = 0; oldTag < newTagValues.length; ++oldTag)
			{
				newTagValues[oldTag] = nonfinal;
			}

			// Scan new DFA states

			for (int state = 0; state < newDFATagTable.length; ++state)
			{

				// If the state is final ...

				if (newDFATagTable[state] != nonfinal)
				{

					// If we haven't seen this final tag yet ...

					if (newTagValues[newDFATagTable[state]] == nonfinal)
					{

						// Assign a new tag value and count it

						newTagValues[newDFATagTable[state]] = mergeCount++;
					}

					// Save the new tag value for this state

					newDFATagTable[state] = newTagValues[newDFATagTable[state]];
				}
			}

			// Allocate the new top-level merge table

			newDFAMergeTable = new int[mergeCount][];

			// Copy NFA tag lists from old merge table to new merge table

			for (int oldTag = 0; oldTag < newTagValues.length; ++oldTag)
			{

				// If we have seen this final tag ...

				if (newTagValues[oldTag] != nonfinal)
				{

					// Insert old tag's list into the new table

					newDFAMergeTable[newTagValues[oldTag]] = _mergeTable[oldTag];
				}
			}
		}
			
		// Report progress
			
		if (generatorStatus != null)
		{
			generatorStatus.statusWork ();
		}

		// Become the new DFA

		_tagTable = newDFATagTable;

		_transitionTable = newDFATransitionTable;

		_mergeTable = newDFAMergeTable;

		_expandedTable = null;

		return;
	}




	// Given a DFA with a merge table, this function replaces each final state
	// tag with the minimum of the NFA final tag values.  Then it deletes the
	// merge table.

	public void selectMinimumTags ()
	{

		// Check that this is a DFA with a merge table

		if ((!isDFA ()) || (_mergeTable == null))
		{
			throw new IllegalArgumentException ("FiniteAutomaton.selectMinimumTags");
		}

		// Create an array to hold the minimum tag values

		int[] minimumTags = new int[_mergeTable.length];

		// For each element of the merge table ...

		for (int m = 0; m < _mergeTable.length; ++m)
		{

			// Find the minimum tag in the list

			minimumTags[m] = _mergeTable[m][0];

			for (int i = 1; i < _mergeTable[m].length; ++i)
			{
				minimumTags[m] = Math.min (minimumTags[m], _mergeTable[m][i]);
			}
		}

		// For each state ...

		for (int state = 0; state < _tagTable.length; ++state)
		{

			// If the state is final ...

			if (_tagTable[state] != nonfinal)
			{

				// Replace its tag with the minimum NFA tag value

				_tagTable[state] = minimumTags[_tagTable[state]];
			}
		}

		// Delete the merge table

		_mergeTable = null;

		return;
	}




	// Given a DFA with a merge table, this function replaces each final state
	// tag with the logical-or of the NFA final tag values.  Then it deletes
	// the merge table.

	public void selectLogicalOrTags ()
	{

		// Check that this is a DFA with a merge table

		if ((!isDFA ()) || (_mergeTable == null))
		{
			throw new IllegalArgumentException ("FiniteAutomaton.selectMinimumTags");
		}

		// Create an array to hold the logical-or tag values

		int[] logicalOrTags = new int[_mergeTable.length];

		// For each element of the merge table ...

		for (int m = 0; m < _mergeTable.length; ++m)
		{

			// Find the logical-or of the tags in the list

			logicalOrTags[m] = _mergeTable[m][0];

			for (int i = 1; i < _mergeTable[m].length; ++i)
			{
				logicalOrTags[m] |= _mergeTable[m][i];
			}
		}

		// For each state ...

		for (int state = 0; state < _tagTable.length; ++state)
		{

			// If the state is final ...

			if (_tagTable[state] != nonfinal)
			{

				// Replace its tag with the logical-or NFA tag value

				_tagTable[state] = logicalOrTags[_tagTable[state]];
			}
		}

		// Delete the merge table

		_mergeTable = null;

		return;
	}


	

	// Creates a copy of the finite automaton.  This is a completely
	// independent copy (i.e., a deep copy of the tables).

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			FiniteAutomaton cloneFA = (FiniteAutomaton) super.clone();

			// Clone the tag table
			
			// Note (04/07/98):  We have changed from deepClone() to deepShallowClone()
			// in order to comply with Java 1.1.  See ArrayHelper.java for an
			// explanation of why this was necessary.

			cloneFA._tagTable = (int[]) ArrayHelper.deepShallowClone (this._tagTable);

			// Clone the transition table

			cloneFA._transitionTable = (int[][]) ArrayHelper.deepShallowClone (this._transitionTable);

			// Clone the merge table

			cloneFA._mergeTable = (int[][]) ArrayHelper.deepShallowClone (this._mergeTable);

			// Clone the expanded table

			cloneFA._expandedTable = (int[][]) ArrayHelper.deepShallowClone (this._expandedTable);

			// Return the clone

			return cloneFA;
		}
		catch (CloneNotSupportedException e)
		{
		
			// This should never happen, since we are Cloneable

			throw new InternalError();
		}
	}


	

	// Write the finite automaton to a data stream

	public void writeToStream (DataOutput stream) throws IOException
	{

		// Write the finite automaton signature

		stream.writeLong (streamSignature);

		// Write the transition table

		ArrayIO.writeInt2D (stream, ArrayIO.formatInt, _transitionTable);

		// Write the tag table

		ArrayIO.writeInt1D (stream, ArrayIO.formatInt, _tagTable);

		// Write the merge table

		ArrayIO.writeInt2D (stream, ArrayIO.formatInt, _mergeTable);

		// Done writing stream

		return;
	}



	
	// Read the finite automaton from a data stream

	public void readFromStream (DataInput stream) throws IOException
	{

		// Read the finite automaton signature

		long inputSignature = stream.readLong ();

		if (inputSignature != streamSignature)
		{
			throw new IODataFormatException (
				"FiniteAutomaton.readFromStream: Invalid signature");
		}

		// Read the transition table

		_transitionTable = ArrayIO.readInt2D (stream, ArrayIO.formatInt);

		// Read the tag table

		_tagTable = ArrayIO.readInt1D (stream, ArrayIO.formatInt);

		// Read the merge table

		_mergeTable = ArrayIO.readInt2D (stream, ArrayIO.formatInt);

		// Expanded table is not restored

		_expandedTable = null;

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

		// Check the transition table

		if (_transitionTable == null)
		{
			return "Missing transition table";
		}

		if (_transitionTable.length < 1)
		{
			return "Invalid transition table length";
		}

		for (int i = 0; i < _transitionTable.length; ++i)
		{
			if (_transitionTable[i] == null)
			{
				return "Missing transition table subarray";
			}

			if ((_transitionTable[i].length & 1) != 0)
			{
				return "Invalid transition table subarray length";
			}

			for (int j = 0; j < _transitionTable[i].length; j += 2)
			{
				if ((_transitionTable[i][j] < epsilon)
					|| (_transitionTable[i][j+1] < 0)
					|| (_transitionTable[i][j+1] >= _transitionTable.length) )
				{
					return "Invalid transition table entry";
				}
			}
		}

		// Check the tag table

		if (_tagTable == null)
		{
			return "Missing tag table";
		}

		if (_tagTable.length != _transitionTable.length)
		{
			return "Invalid tag table length";
		}

		for (int i = 0; i < _tagTable.length; ++i)
		{
			if ((_tagTable[i] < nonfinal)
				|| ((_mergeTable != null) && (_tagTable[i] >= _mergeTable.length)) )
			{
				return "Invalid tag table entry";
			}
		}

		// Check the merge table

		if (_mergeTable != null)
		{
			if (_mergeTable.length > _transitionTable.length)
			{
				return "Invalid merge table length";
			}

			for (int i = 0; i < _mergeTable.length; ++i)
			{
				if (_mergeTable[i] == null)
				{
					return "Missing merge table subarray";
				}

				if (_mergeTable[i].length < 1)
				{
					return "Invalid merge table subarray length";
				}

				for (int j = 0; j < _mergeTable[i].length; ++j)
				{
					if (_mergeTable[i][j] < 0)
					{
						return "Invalid merge table entry";
					}
				}
			}
		}

		// Success

		return null;
	}




	// Given a DFA, this function creates the expanded form of the transition
	// table, storing the result in _expandedTable.  All categories must lie
	// between minCategory and maxCategory inclusive;  however, it is not
	// required that every category between minCategory and maxCategory appear
	// in the DFA.  Note that if minCategory is not 0, this function
	// effectively adjusts the value of each category by subtracting
	// minCategory.

	public void createExpandedTable (int minCategory, int maxCategory)
	{

		// Validate arguments

		if (minCategory > maxCategory)
		{
			throw new IllegalArgumentException ("FiniteAutomaton.createExpandedTable");
		}

		// Check that this is a DFA

		if (!isDFA ())
		{
			throw new IllegalArgumentException ("FiniteAutomaton.createExpandedTable");
		}

		// Create the top-level array

		int[][] newExpandedTable = new int[_transitionTable.length][];

		// Loop over all states

		for (int state = 0; state < _transitionTable.length; ++state)
		{

			// Create a subarray for this state

			newExpandedTable[state] = new int[(maxCategory - minCategory) + 1];

			// Initialize subarray to "no transition"

			for (int i = 0; i < newExpandedTable[state].length; ++i)
			{
				newExpandedTable[state][i] = -1;
			}

			// Scan the transition table for this state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// Check that the category is in range

				if ((_transitionTable[state][i] < minCategory)
					|| (_transitionTable[state][i] > maxCategory))
				{
					throw new IllegalArgumentException ("FiniteAutomaton.createExpandedTable");
				}

				// Record this transition

				newExpandedTable[state][_transitionTable[state][i] - minCategory]
					= _transitionTable[state][i+1];
			}
		}

		// Adopt the new table

		_expandedTable = newExpandedTable;

		return;
	}




	// Converts the finite automaton to a string.  Overrides the toString
	// method of class Object.  This is used mainly for debugging.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left brace

		buf.append ("{");

		// Step through all the NFA states

		for (int state = 0; state < _transitionTable.length; ++state)
		{

			// If not the first state, append a comma

			if (state != 0)
			{
				buf.append (", ");
			}

			// Convert the state to a string an append it

			buf.append (String.valueOf (state));

			// If state is final, append " (f#)" to display the tag

			if (_tagTable[state] != nonfinal)
			{
				buf.append (" (f");
				buf.append (String.valueOf (_tagTable[state]));

				// If there is a merge table, insert " = [...]" to display the merge list

				if (_mergeTable != null)
				{

					// Append the equal sign and opening bracket

					buf.append (" = [");

					// Scan the tag list

					for (int i = 0; i < _mergeTable[_tagTable[state]].length; ++i)
					{

						// If not the first tag, append a comma

						if (i != 0)
						{
							buf.append (", ");
						}

						// Append the tag value

						buf.append (String.valueOf (_mergeTable[_tagTable[state]][i]));
					}

					// Append the closing bracket

					buf.append ("]");
				}

				// Append the closing parenthesis

				buf.append (")");
			}

			// Append a colon and left brace

			buf.append (": {");

			// Scan the transition table for this NFA state

			for (int i = 0; i < _transitionTable[state].length; i += 2)
			{

				// If not the first transition, append a comma

				if (i != 0)
				{
					buf.append (", ");
				}

				// Append the character category, or e for epsilon

				if (_transitionTable[state][i] == epsilon)
				{
					buf.append ("e");
				}
				else
				{
					buf.append (String.valueOf (_transitionTable[state][i]));
				}

				// Append an arrow

				buf.append ("->");

				// Append the target state

				buf.append (String.valueOf (_transitionTable[state][i+1]));

			}	// end loop over transitions

			// Append a right brace

			buf.append ("}");

		}	// end loop over states

		// End with a right brace

		buf.append ("}");

		// Return resulting string

		return buf.toString ();
	}


}




/*->

  NFAToDFAState is used during the conversion from an NFA to a DFA.
  When converting from an NFA to a DFA, each DFA state corresponds to a set
  of NFA states.  NFAToDFAState defines one DFA state, plus the
  corresponding set of NFA states.  With our conversion algorithm, each NFA
  state is a non-final state with at least one non-epsilon transition. 

->*/

class NFAToDFAState
{

	// The set of NFA states

	IntSet NFAStates;

	// The DFA state

	int DFAState;

	// The set of NFA final state tags, or the empty set if this state is
	// non-final

	IntSet NFAFinalTags;

	// The DFA transition table for this state, in the same format as the
	// second-level arrays in FiniteAutomaton._transitionTable.

	int[] DFATransitions;


	// Constructs an object with the specified set of NFA states, DFA state,
	// and set of NFA final tags.  No transition table is allocated.  The
	// object retain references to the supplied sets of NFA states and NFA
	// final tags.

	NFAToDFAState (IntSet NFAStates, int DFAState, IntSet NFAFinalTags)
	{
		super ();

		// Initialize our variables

		this.NFAStates = NFAStates;
		this.DFAState = DFAState;
		this.NFAFinalTags = NFAFinalTags;

		this.DFATransitions = null;

		return;
	}
}
