// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.NoSuchElementException;
import java.util.Random;


/*->

  IntSet implements a set of int.  Storage is automatically
  allocated as elements are added.  A given int can appear in the
  set at most once.

->*/


public class IntSet implements ConstIntSet, Cloneable 
{

	// The buffer to hold the elements of the set

	protected int[] _elementData;

	// The value used to mark an empty slot

	protected int _emptySlot;

	// The index into elements where the next element can be added

	protected int _highWaterMark;

	// The number of (non-null) elements

	protected int _elementCount;

	// Default value for initial capacity

	protected static final int defaultCapacity = 10;

	// Number of times set has been compacted

	protected int _compactionCount;

	// A random number generator we use to generate values for _emptySlot

	protected static final Random ourRandom = new Random (0x5349FF00L);


	// Creates a new set with the specified initial capacity and empty
	// slot value

    public IntSet (int initialCapacity, int initialEmptySlot)
	{
		super();

		// Validate the argument

		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException ("IntSet.IntSet");
		}

		// Allocate the array

		_elementData = new int[initialCapacity];

		// Save empty slot value

		_emptySlot = initialEmptySlot;

		// Initialize to empty set

		_elementCount = 0;
		_highWaterMark = 0;
		_compactionCount = 0;

		return;
    }


	// Creates a new set with the specified initial capacity

    public IntSet (int initialCapacity)
	{
		this (initialCapacity, ourRandom.nextInt ());
    }

	
	// Creates a new set with default initial capacity

    public IntSet ()
	{
		this (defaultCapacity);
    }


	// Changes the empty slot value.  Upon return, it is guaranteed that
	// the empty slot value has changed.

	protected void changeEmptySlot ()
	{

		// New value

		int newEmptySlot;

		// Loop to generate new values and check to make sure there is no conflict

	conflictLoop:
		for ( ; ; )
		{

			// Generate a new value

			newEmptySlot = ourRandom.nextInt ();

			// If new value is same as old value, reject it

			if (_emptySlot == newEmptySlot)
			{
				continue conflictLoop;
			}

			// Reject new value if it is already in the array

			for (int i = 0; i < _highWaterMark; ++i)
			{
				if (_elementData[i] == newEmptySlot)
				{
					continue conflictLoop;
				}
			}

			// Value OK, break out of loop

			break conflictLoop;
		}

		// Scan the array and change all the empty slot values

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == _emptySlot)
			{
				_elementData[i] = newEmptySlot;
			}
		}

		// Save the new empty slot value

		_emptySlot = newEmptySlot;

		return;
	}


	// Releases all memory used by the set, except that needed for
	// the set's current contents plus the specified number of
	// additional elements.  This function invalidates enumerators.
	// Returns true if memory was released, false otherwise.

	public boolean compact (int additionalCapacity)
	{

		// Validate the argument

		if (additionalCapacity < 0)
		{
			throw new IllegalArgumentException ("IntSet.compact");
		}

		// If we are already compact, do nothing

		if ((_elementCount == _highWaterMark)
			&& ((_highWaterMark + additionalCapacity) == _elementData.length))
		{
			return false;
		}

		// Allocate new array of the required size

		int[] newDataArray = new int[_elementCount + additionalCapacity];

		// Copy elements, discarding invalid ones

		for (int i = 0, j = 0; j < _elementCount; ++i)
		{
			if (_elementData[i] != _emptySlot)
			{
				newDataArray[j++] = _elementData[i];
			}
		}

		// Establish the new arrays
		 
		_elementData = newDataArray;
		_highWaterMark = _elementCount;

		// Advance the compaction count (this invalidates enumerators)

		++_compactionCount;

		return true;
	}


	// Releases all memory used by the set, except that needed for
	// the set's current contents.  This function invalidates
	// enumerators.  Returns true if memory was released, false
	// otherwise.

	public boolean compact ()
	{
		return compact (0);
	}


	// Ensures that the set has enough memory for at least the specified
	// number of additional elements.  This function does not invalidate
	// enumerators.  Returns true if additional memory was allocated,
	// false otherwise.

	public boolean ensureCapacity (int additionalCapacity)
	{

		// Validate the argument

		if (additionalCapacity < 0)
		{
			throw new IllegalArgumentException ("IntSet.ensureCapacity");
		}

		// If we have enough capacity, do nothing

		if ((_elementData.length - _highWaterMark) >= additionalCapacity)
		{
			return false;
		}

		// Allocate new array of the required size

		int[] newDataArray = new int[_highWaterMark + additionalCapacity];

		// Copy array below the high water mark

		if (_highWaterMark > 0)
		{
			System.arraycopy (_elementData, 0, newDataArray, 0, _highWaterMark);
		}

		// Establish the new array
		 
		_elementData = newDataArray;

		return true;
	}


	// Adds the specified element to the set.  Returns true if the element
	// was not already in the set, false otherwise.  This function does not
	// invalidate enumerators;  any active enumerator will see the new
	// element.

	public boolean addElement (int element)
	{

		// If element conflicts with empty slot value, change empty slot value

		if (element == _emptySlot)
		{
			changeEmptySlot ();
		}

		// If element is already in the set, do nothing

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == element)
			{
				return false;
			}
		}

		// Make room for the new element

		if (_highWaterMark == _elementData.length)
		{
			ensureCapacity (Math.max (_elementData.length, defaultCapacity));
		}

		// Add the element to the set

		_elementData[_highWaterMark++] = element;
		++_elementCount;

		return true;
	}


	// Removes the specified element from the set.  Returns true if the
	// element was in the set, false otherwise.  This function does not
	// invalidate enumerators;  any active enumerator will not see the
	// removed element.  Storage occupied by the removed element is never
	// reused or released until the set is compacted.

	public boolean removeElement (int element)
	{

		// If element equals empty slot value, it's not in the set

		if (element == _emptySlot)
		{
			return false;
		}

		// If element is in the set, remove it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == element)
			{
				
				// Remove the element from the set

				_elementData[i] = _emptySlot;
				--_elementCount;

				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Removes oldElement from the set and replaces it with newElement.
	// The effect is the same as first removing oldElement and then adding
	// newElement, except that if oldElement was in the set and newElement
	// was not in the set then newElement is placed in the position previously
	// occupied by oldElement (instead of being placed at the end of the
	// set).  Returns true if the contents of the set is modified, false
	// otherwise.  This function does not invalidate enumerators;  any active
	// enumerator that has not yet seen oldElement will see newElement.
	// Storage is never reused or released until the set is compacted. 

	public boolean replaceElement (int oldElement, int newElement)
	{

		// If old element equals empty slot value, it's not in the set,
		// so just add the new element

		if (oldElement == _emptySlot)
		{
			return addElement (newElement);
		}

		// If old element is in the set, remove it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == oldElement)
			{

				// If the old and new elements are the same, nothing to do

				if (oldElement == newElement)
				{
					return false;
				}

				// If new element conflicts with empty slot value, change empty slot value

				if (newElement == _emptySlot)
				{
					changeEmptySlot ();
				}

				// If new element is already in the set, just remove the old element

				for (int j = 0; j < _highWaterMark; ++j)
				{
					if (_elementData[j] == newElement)
					{
						_elementData[i] = _emptySlot;
						--_elementCount;
						return true;
					}
				}

				// New element is not in the set, insert in place of old element

				_elementData[i] = newElement;
				return true;
			}
		}

		// Old element not found in set, just add the new element

		return addElement (newElement);
	}


	// Returns the number of elements in the set.

	public int elementCount ()
	{
		return _elementCount;
	}


	// Returns true if the set is empty.

	public boolean isEmpty ()
	{
		return (_elementCount == 0);
	}


	// Removes all the elements from the set.  This function does not
	// invalidate enumerators.  Storage is never reused or released until
	// the set is compacted.

	public void removeAllElements ()
	{

		// Remove all elements

		for (int i = 0; i < _highWaterMark; ++i)
		{
			_elementData[i] = _emptySlot;
		}

		_elementCount = 0;

		return;
	}


	// Returns true if the specified object is an element of the set.

	public boolean isElement (int element)
	{

		// If element equals empty slot value, it's not in the set

		if (element == _emptySlot)
		{
			return false;
		}

		// Search set to find element

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == element)
			{
				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Compares two sets to see if they are equal.  Returns true if the
	// argument is an IntSet with the same elements as this;  otherwise,
	// returns false.  Overrides the equals method of Object.

	public boolean equals (Object obj)
	{

		// Check that we've been given an IntSet

		if ((obj == null) || (!(obj instanceof IntSet)))
		{
			return false;
		}

		// Convert argument to IntSet

		IntSet other = (IntSet) obj;

		// Check the other set has the same number of elements

		if (this._elementCount != other._elementCount)
		{
			return false;
		}

		// Check if the other set is the same set

		if (this == other)
		{
			return true;
		}

		// Check that each element of the other set belongs to this set
		
	outerLoop:
		for (int i = 0; i < other._highWaterMark; ++i)
		{

			// Get element of other set

			int element = other._elementData[i];
			if (element != other._emptySlot)
			{

				// Check if element belongs to this set

				if (element != this._emptySlot)
				{
					for (int j = 0; j < this._highWaterMark; ++j)
					{
						if (this._elementData[j] == element)
						{
							continue outerLoop;
						}
					}
				}

				// Didn't find element

				return false;
			}
		}

		// All elements checked OK

		return true;
	}


	// Returns an enumerator for the set.  The enumerator obeys the
	// following semantics:
	//
	// 1. Elements are enumerated in the order they were added to the set.
	//
	// 2. If an element is added during the enumeration, the new element
	//    is returned by the enumerator.
	//
	// 3. If an element is removed during the enumeration, and the
	//    enumerator has not yet returned that element, then that element
	//    is never returned by the enumerator.
	//
	// 4. Two checks for end of enumeration may yield different results if
	//    an element is added or removed between the two checks.
	//
	// 5. The enumerator remains valid until the next time the set is
	//    compacted, at which point the enumerator becomes invalid.
	//
	// 6. When an existing element is replaced with a new element (with
	//    replaceElement), the following applies:  The new element appears
	//    in the enumeration in the position previously occupied by the old
	//    element.  If the replacement occurs during the enumeration then the
	//    new element is returned by the enumerator if and only if the
	//    enumerator has not yet returned the old element.


    public IntEnumeration elements ()
	{
		return new IntSetEnumerator (this);
    }

	
	// Creates a copy of the set.

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			IntSet cloneSet = (IntSet) super.clone();

			// Allocate new array for the clone

			cloneSet._elementData = new int[this._elementData.length];

			if (_highWaterMark > 0)
			{
				System.arraycopy (this._elementData, 0, cloneSet._elementData, 0, _highWaterMark);
			}

			// Return the clone

			return cloneSet;
		}
		catch (CloneNotSupportedException e)
		{
		
			// This should never happen, since we are Cloneable

			throw new InternalError();
		}
	}


	// Converts the set to a string.  Overrides the toString method of
	// class Object.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left brace

		buf.append ("{");

		// Enumerate the set

		IntEnumeration e = elements ();
		boolean isFirstElement = true;

		while (e.hasMoreElements ())
		{

			// If not the first element, append a comma

			if (isFirstElement)
			{
				isFirstElement = false;
			}
			else
			{
				buf.append (", ");
			}

			// Convert the element to a string an append it

			buf.append (String.valueOf (e.nextElement ()));
		}

		// End with a right brace

		buf.append ("}");

		// Return resulting string

		return buf.toString ();
	}


	// Forms the union of this set and the other set.  All the elements in
	// the other set are added to this set.  Returns true if any elements
	// were added, false otherwise.  This function does not invalidate
	// enumerators;  any active enumerator will see the new elements.

	public boolean union (ConstIntSet constOther)
	{

		// Get access to internals of the other set

		IntSet other = (IntSet) constOther;

		// Return value

		boolean returnValue = false;

		// Add each element in the other set

		for (int i = 0; i < other._highWaterMark; ++i)
		{
			if (other._elementData[i] != other._emptySlot)
			{
				returnValue |= addElement (other._elementData[i]);
			}
		}

		return returnValue;
	}


	// Forms the intersection of this set and the other set.  Removes from
	// this set all elements that are not in the other set.  Returns true
	// if any elements were removed, false otherwise.  This function does not
	// invalidate enumerators;  any active enumerator will not see the removed
	// elements.  Storage occupied by the removed elements is never reused or
	// released until the set is compacted.

	public boolean intersection (ConstIntSet constOther)
	{

		// Get access to internals of the other set

		IntSet other = (IntSet) constOther;

		// Return value

		boolean returnValue = false;

		// Remove each element not in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != _emptySlot)
			{
				if (!other.isElement (_elementData[i]))
				{
				
					// Remove the element from this set

					_elementData[i] = _emptySlot;
					--_elementCount;
					returnValue = true;
				}
			}
		}

		return returnValue;
	}


	// Forms the difference of this set and the other set.  Removes from
	// this set all elements that are in the other set.  Returns true
	// if any elements were removed, false otherwise.  This function does not
	// invalidate enumerators;  any active enumerator will not see the removed
	// elements.  Storage occupied by the removed elements is never reused or
	// released until the set is compacted.

	public boolean difference (ConstIntSet constOther)
	{

		// Get access to internals of the other set

		IntSet other = (IntSet) constOther;

		// Return value

		boolean returnValue = false;

		// Remove each element in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != _emptySlot)
			{
				if (other.isElement (_elementData[i]))
				{
				
					// Remove the element from this set

					_elementData[i] = _emptySlot;
					--_elementCount;
					returnValue = true;
				}
			}
		}

		return returnValue;
	}


	// Returns true if this set and the other set are disjoint, that is, if
	// their intersection is empty.

	public boolean isDisjoint (ConstIntSet constOther)
	{

		// Get access to internals of the other set

		IntSet other = (IntSet) constOther;

		// Check each element to see if it is in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != _emptySlot)
			{
				if (other.isElement (_elementData[i]))
				{

					// Found an element in common

					return false;
				}
			}
		}

		// Found no elements in common

		return true;
	}


}




// Enumerator class

final class IntSetEnumerator implements IntEnumeration
{

	// The set we are enumerating

	protected IntSet set;

	// The compaction count when this enumerator was created

	protected int _compactionCount;

	// Index into element data array
	
	protected int _elementIndex;


	// Create a new enumerator for the specified set.

	IntSetEnumerator (IntSet set)
	{

		// Save the set

		this.set = set;
		this._compactionCount = set._compactionCount;

		// Initialize index into element data array
		 
		_elementIndex = 0;

		return;
	}


	// Return true if the enumerator can return more elements.

	public boolean hasMoreElements ()
	{

		// Check for valid enumerator

		if (this._compactionCount != set._compactionCount)
		{
			throw new InvalidEnumeratorException ("IntSetEnumerator.hasMoreElements");
		}

		// Scan past empty slots
		
		while (_elementIndex < set._highWaterMark)
		{

			// If nonempty slot, we can return an element

			if (set._elementData[_elementIndex] != set._emptySlot)
			{
				return true;
			}

			// Skip over empty slot

			++_elementIndex;
		}

		// No nonempty slots, we can't return an element

		return false;
	}


	// Returns the next element in the enumeration.

	public int nextElement ()
	{

		// Check for valid enumerator

		if (this._compactionCount != set._compactionCount)
		{
			throw new InvalidEnumeratorException ("IntSetEnumerator.nextElement");
		}

		// Scan past empty slots
		
		while (_elementIndex < set._highWaterMark)
		{

			// Get element in next slot

			int element = set._elementData[_elementIndex++];

			// If slot nonempty, return the element

			if (element != set._emptySlot)
			{
				return element;
			}
		}

		// No nonempty slots, we can't return an element

		throw new NoSuchElementException ("IntSetEnumerator.nextElement");
	}


}

