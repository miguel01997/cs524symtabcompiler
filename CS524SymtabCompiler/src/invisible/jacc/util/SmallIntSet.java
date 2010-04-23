// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.NoSuchElementException;


/*->

  SmallIntSet implements a set of int.  Storage is automatically
  allocated as elements are added.  A given int can appear in the
  set at most once.

  A SmallIntSet is for use where the elements are drawn from a narrow
  range.  The set is implemented as a bit array.  This allows linear
  time performance for set-wide operations (like union and intersection),
  and constant time performance for element operations (like add and
  remove).  Note, however, that the enumeration semantics are inferior
  to IntSet.

->*/


public class SmallIntSet implements ConstSmallIntSet, Cloneable 
{

	// The buffer to hold bit flags for each possible element of the set.  If
	// this array has length 0, it means that the range is unspecified (in
	// which case the value of _arrayOrigin has no meaning).

	protected int[] _elementBits;

	// Constants used for splitting element values into word and bit values

	protected static final int shiftCount = 5;
	protected static final int bit0 = 0x00000001;
	protected static final int bitMask = 0x0000001F;
	protected static final int bitsPerWord = 32;

	// The origin of the bit array.  Element v is represented by the bit
	// _elementBits[(v >> shiftCount) - _arrayOrigin] & (bit0 << (v & bitMask)).
	// If _elementBits has zero length, then _arrayOrigin should be 0.

	protected int _arrayOrigin;

	// The number of elements

	protected int _elementCount;

	// Number of times enumerators have been invalidated

	protected int _invalidationCount;


	// A table indicating how many bits are set in each byte

	protected static int[] oneBitsInByte = new int[256];

	static
	{
		for (int i = 0; i < 256; ++i)
		{
			oneBitsInByte[i]
				= (i & 0x01)
				+ ((i >> 1) & 0x01)
				+ ((i >> 2) & 0x01)
				+ ((i >> 3) & 0x01)
				+ ((i >> 4) & 0x01)
				+ ((i >> 5) & 0x01)
				+ ((i >> 6) & 0x01)
				+ ((i >> 7) & 0x01);
		}
	}


	// Calculates the number of one bits in a word

	protected static int oneBitsInWord (int word)
	{
		return oneBitsInByte[word & 0xFF]
			+ oneBitsInByte[(word >> 8) & 0xFF]
			+ oneBitsInByte[(word >> 16) & 0xFF]
			+ oneBitsInByte[(word >> 24) & 0xFF];
	}


	// Creates a new set spanning the specified range.

    public SmallIntSet (int minElement, int maxElement)
	{
		super();

		// Validate the arguments

		if (minElement > maxElement)
		{
			throw new IllegalArgumentException ("SmallIntSet.SmallIntSet");
		}

		// Allocate the array

		_elementBits = new int[((maxElement >> shiftCount) - (minElement >> shiftCount)) + 1];

		for (int i = 0; i < _elementBits.length; ++i)
		{
			_elementBits[i] = 0;
		}

		_arrayOrigin = minElement >> shiftCount;

		// Initialize to empty set

		_elementCount = 0;
		_invalidationCount = 0;

		return;
    }


	// Creates a new set with an unspecified range.

    public SmallIntSet ()
	{
		super();

		// Allocate a zero-length array to signal an unspecified range

		_elementBits = new int[0];

		_arrayOrigin = 0;

		// Initialize to empty set

		_elementCount = 0;
		_invalidationCount = 0;

		return;
    }


	// Releases all memory used by the set, except that needed for
	// the set's current contents plus the specified range.  This function
	// does not invalidate enumerators.  Returns true if the set's memory
	// was reallocated, false otherwise.

	public boolean compact (int minElement, int maxElement)
	{

		// Validate the arguments

		if (minElement > maxElement)
		{
			throw new IllegalArgumentException ("SmallIntSet.compact");
		}

		// Calculate the minimum and maximum word index needed

		int minIndex = minElement >> shiftCount;

		int maxIndex = maxElement >> shiftCount;
		
		// Reduce minIndex to the index of the lowest non-zero word

		for (int i = _arrayOrigin,
			iLimit = Math.min (_elementBits.length + _arrayOrigin, minIndex);
			i < iLimit; ++i)
		{
			if (_elementBits[i - _arrayOrigin] != 0)
			{
				minIndex = i;
				break;
			}
		}
		
		// Increase maxIndex to the index of the highest non-zero word

		for (int i = _elementBits.length + _arrayOrigin - 1,
			iLimit = Math.max (_arrayOrigin, maxIndex);
			i > iLimit; --i)
		{
			if (_elementBits[i - _arrayOrigin] != 0)
			{
				maxIndex = i;
				break;
			}
		}

		// If we are already compact, do nothing

		if ((_elementBits.length == ((maxIndex - minIndex) + 1)) && (_arrayOrigin == minIndex))
		{
			return false;
		}

		// Allocate new array of the required size

		int[] newBitArray = new int[(maxIndex - minIndex) + 1];

		for (int i = 0; i < newBitArray.length; ++i)
		{
			newBitArray[i] = 0;
		}

		// Copy elements from the overlap part of the two arrays

		for (int i = Math.max (_arrayOrigin, minIndex),
			iLimit = Math.min (_arrayOrigin + _elementBits.length, minIndex + newBitArray.length);
			i < iLimit; ++i)
		{
			newBitArray[i - minIndex] = _elementBits[i - _arrayOrigin];
		}

		// Establish the new array
		 
		_elementBits = newBitArray;
		_arrayOrigin = minIndex;

		return true;
	}


	// Releases all memory used by the set, except that needed for
	// the set's current contents.  This function does not invalidate
	// enumerators.  Returns true if memory was released, false
	// otherwise.

	public boolean compact ()
	{

		// If set is nonempty, compact around one of the set's elements

		for (int i = _arrayOrigin; i < _elementBits.length + _arrayOrigin; ++i)
		{
			if (_elementBits[i - _arrayOrigin] != 0)
			{
				return compact (i << shiftCount, (i << shiftCount) + bitMask);
			}
		}

		// If range is unspecified, do nothing

		if (_elementBits.length == 0)
		{
			return false;
		}

		// Set is empty, make range unspecified

		_elementBits = new int[0];

		_arrayOrigin = 0;

		return true;
	}


	// Ensures that the set has enough memory for at least the specified
	// range of elements.  This function does not invalidate enumerators.
	// Returns true if additional memory was allocated, false otherwise.

	public boolean ensureCapacity (int minElement, int maxElement)
	{

		// Validate the arguments

		if (minElement > maxElement)
		{
			throw new IllegalArgumentException ("SmallIntSet.ensureCapacity");
		}

		// Calculate the minimum and maximum word index needed

		int minIndex = minElement >> shiftCount;

		int maxIndex = maxElement >> shiftCount;

		// If we have enough capacity, do nothing

		if ((_arrayOrigin <= minIndex) && (maxIndex <= (_elementBits.length + _arrayOrigin - 1)))
		{
			return false;
		}

		// If there is a range currently specified ...

		if (_elementBits.length != 0)
		{

			// Adjust indexes to encompass the existing range

			minIndex = Math.min (minIndex, _arrayOrigin);

			maxIndex = Math.max (maxIndex, _elementBits.length + _arrayOrigin - 1);
		}

		// Allocate new array of the required size

		int[] newBitArray = new int[(maxIndex - minIndex) + 1];

		for (int i = 0; i < newBitArray.length; ++i)
		{
			newBitArray[i] = 0;
		}

		// Copy elements from the overlap part of the two arrays

		for (int i = Math.max (_arrayOrigin, minIndex),
			iLimit = Math.min (_arrayOrigin + _elementBits.length, minIndex + newBitArray.length);
			i < iLimit; ++i)
		{
			newBitArray[i - minIndex] = _elementBits[i - _arrayOrigin];
		}

		// Establish the new array
		 
		_elementBits = newBitArray;
		_arrayOrigin = minIndex;

		return true;
	}


	// Adds the specified element to the set.  Returns true if the element
	// was not already in the set, false otherwise.  This function invalidates
	// enumerators if the set is modified.

	public boolean addElement (int element)
	{

		// Split element into index and mask

		int index = element >> shiftCount;

		int mask = bit0 << (element & bitMask);

		// If the element is within the existing range ...

		if ((_arrayOrigin <= index) && (index < (_arrayOrigin + _elementBits.length)))
		{

			// If element is already in the set, do nothing

			if ((_elementBits[index - _arrayOrigin] & mask) != 0)
			{
				return false;
			}
		}

		// Otherwise, element is outside existing range ...

		else
		{

			// Expand range to include the element

			ensureCapacity (element, element);
		}

		// Add the element to the set
			
		_elementBits[index - _arrayOrigin] |= mask;

		// Count one more element

		++_elementCount;

		// Invalidate enumerators

		++_invalidationCount;

		return true;
	}


	// Removes the specified element from the set.  Returns true if the
	// element was in the set, false otherwise.  This function invalidates
	// enumerators if the set is modified.
	//
	// Storage is never released until the set is compacted.

	public boolean removeElement (int element)
	{

		// Split element into index and mask

		int index = element >> shiftCount;

		int mask = bit0 << (element & bitMask);

		// If the element is within the existing range ...

		if ((_arrayOrigin <= index) && (index < (_arrayOrigin + _elementBits.length)))
		{

			// If element is in the set ...

			if ((_elementBits[index - _arrayOrigin] & mask) != 0)
			{

				// Remove the element from the set
					
				_elementBits[index - _arrayOrigin] &= ~mask;

				// Count one less element

				--_elementCount;

				// Invalidate enumerators

				++_invalidationCount;

				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Returns the number of elements in the set.

	public int elementCount ()
	{

		// Return the number of elements

		return _elementCount;
	}


	// Returns true if the set is empty.

	public boolean isEmpty ()
	{
		return (_elementCount == 0);
	}


	// Removes all the elements from the set.  This function invalidates
	// enumerators.
	//
	// Storage is never released until the set is compacted.

	public void removeAllElements ()
	{

		// Remove all elements

		for (int index = _arrayOrigin; index < _arrayOrigin + _elementBits.length; ++index)
		{
			_elementBits[index - _arrayOrigin] = 0;
		}

		_elementCount = 0;

		// Invalidate enumerators

		++_invalidationCount;

		return;
	}


	// Returns true if the specified object is an element of the set.

	public boolean isElement (int element)
	{

		// Split element into index and mask

		int index = element >> shiftCount;

		int mask = bit0 << (element & bitMask);

		// If the element is within the existing range ...

		if ((_arrayOrigin <= index) && (index < (_arrayOrigin + _elementBits.length)))
		{

			// If element is in the set ...

			if ((_elementBits[index - _arrayOrigin] & mask) != 0)
			{

				// Element found

				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Compares two sets to see if they are equal.  Returns true if the
	// argument is a SmallIntSet with the same elements as this;  otherwise,
	// returns false.  Overrides the equals method of Object.

	public boolean equals (Object obj)
	{

		// Check that we've been given an SmallIntSet

		if ((obj == null) || (!(obj instanceof SmallIntSet)))
		{
			return false;
		}

		// Convert argument to SmallIntSet

		SmallIntSet other = (SmallIntSet) obj;

		// Check the other set has the same number of elements

		if (this._elementCount != other._elementCount)
		{
			return false;
		}

		// Check if the other set is the same set, or both sets are empty

		if ((this == other) || ((this._elementCount == 0) && (other._elementCount == 0)))
		{
			return true;
		}

		// Scan every index in the combined range

		for (int index = Math.min (this._arrayOrigin, other._arrayOrigin),
			indexLimit = Math.max (this._arrayOrigin + this._elementBits.length,
			other._arrayOrigin + other._elementBits.length);
			index < indexLimit; ++index)
		{

			// Get word from this set's array

			int thisWord = 0;

			if ((this._arrayOrigin <= index)
				&& (index < (this._arrayOrigin + this._elementBits.length)))
			{
				thisWord = this._elementBits[index - this._arrayOrigin];
			}

			// Get word from other set's array

			int otherWord = 0;

			if ((other._arrayOrigin <= index)
				&& (index < (other._arrayOrigin + other._elementBits.length)))
			{
				otherWord = other._elementBits[index - other._arrayOrigin];
			}

			// Compare the words

			if (thisWord != otherWord)
			{
				return false;
			}
		}

		// All elements checked OK

		return true;
	}


	// Returns an enumerator for the set.  The enumerator obeys the
	// following semantics:
	//
	// 1. Elements are enumerated in increasing (sorted) order.
	//
	// 2. The enumerator remains valid until the next time the set is
	//    modified, at which point the enumerator becomes invalid.


    public IntEnumeration elements ()
	{
		return new SmallIntSetEnumerator (this);
    }

	
	// Creates a copy of the set.

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			SmallIntSet cloneSet = (SmallIntSet) super.clone();

			// Clone the bit array

			cloneSet._elementBits = (int[]) this._elementBits.clone();

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
	// were added, false otherwise.  This function invalidates enumerators
	// if the set is modified.

	public boolean union (ConstSmallIntSet constOther)
	{

		// Get access to internals of the other set

		SmallIntSet other = (SmallIntSet) constOther;

		// If other set is empty, nothing to do

		if (other._elementCount == 0)
		{
			return false;
		}

		// Return value

		boolean returnValue = false;

		// Ensure we span the range of the other set

		this.ensureCapacity (other._arrayOrigin << shiftCount,
			((other._arrayOrigin + other._elementBits.length - 1) << shiftCount) + bitMask);

		// Scan every index in our (expanded) range

		for (int index = this._arrayOrigin;
			index < this._arrayOrigin + this._elementBits.length;
			++index)
		{

			// Get word from other set's array

			int otherWord = 0;

			if ((other._arrayOrigin <= index)
				&& (index < (other._arrayOrigin + other._elementBits.length)))
			{
				otherWord = other._elementBits[index - other._arrayOrigin];
			}

			// Get bits that need to change

			int changedBits = otherWord & ~this._elementBits[index - this._arrayOrigin];

			// If any bits are changing ...

			if (changedBits != 0)
			{

				// Change the bits

				this._elementBits[index - this._arrayOrigin] ^= changedBits;

				// Adjust the element count

				this._elementCount += oneBitsInWord (changedBits);

				// Invalidate enumerators

				++this._invalidationCount;

				// Set modified

				returnValue = true;
			}
		}

		return returnValue;
	}


	// Forms the intersection of this set and the other set.  Removes from
	// this set all elements that are not in the other set.  Returns true
	// if any elements were removed, false otherwise.  This function invalidates
	// enumerators if the set is modified.
	//
	// Storage is never released until the set is compacted.

	public boolean intersection (ConstSmallIntSet constOther)
	{

		// Get access to internals of the other set

		SmallIntSet other = (SmallIntSet) constOther;

		// Return value

		boolean returnValue = false;

		// Scan every index in our range

		for (int index = this._arrayOrigin;
			index < this._arrayOrigin + this._elementBits.length;
			++index)
		{

			// Get word from other set's array

			int otherWord = 0;

			if ((other._arrayOrigin <= index)
				&& (index < (other._arrayOrigin + other._elementBits.length)))
			{
				otherWord = other._elementBits[index - other._arrayOrigin];
			}

			// Get bits that need to change

			int changedBits = (~otherWord) & this._elementBits[index - this._arrayOrigin];

			// If any bits are changing ...

			if (changedBits != 0)
			{

				// Change the bits

				this._elementBits[index - this._arrayOrigin] ^= changedBits;

				// Adjust the element count

				this._elementCount -= oneBitsInWord (changedBits);

				// Invalidate enumerators

				++this._invalidationCount;

				// Set modified

				returnValue = true;
			}
		}

		return returnValue;
	}


	// Forms the difference of this set and the other set.  Removes from
	// this set all elements that are in the other set.  Returns true
	// if any elements were removed, false otherwise.  This function invalidates
	// enumerators if the set is modified.
	//
	// Storage is never released until the set is compacted.

	public boolean difference (ConstSmallIntSet constOther)
	{

		// Get access to internals of the other set

		SmallIntSet other = (SmallIntSet) constOther;

		// Return value

		boolean returnValue = false;

		// Scan every index the common range

		for (int index = Math.max (this._arrayOrigin, other._arrayOrigin),
			indexLimit = Math.min (this._arrayOrigin + this._elementBits.length,
			other._arrayOrigin + other._elementBits.length);
			index < indexLimit; ++index)
		{

			// Get word from other set's array

			int otherWord = other._elementBits[index - other._arrayOrigin];

			// Get bits that need to change

			int changedBits = otherWord & this._elementBits[index - this._arrayOrigin];

			// If any bits are changing ...

			if (changedBits != 0)
			{

				// Change the bits

				this._elementBits[index - this._arrayOrigin] ^= changedBits;

				// Adjust the element count

				this._elementCount -= oneBitsInWord (changedBits);

				// Invalidate enumerators

				++this._invalidationCount;

				// Set modified

				returnValue = true;
			}
		}

		return returnValue;
	}


	// Returns true if this set and the other set are disjoint, that is, if
	// their intersection is empty.

	public boolean isDisjoint (ConstSmallIntSet constOther)
	{

		// Get access to internals of the other set

		SmallIntSet other = (SmallIntSet) constOther;

		// Scan every index the common range

		for (int index = Math.max (this._arrayOrigin, other._arrayOrigin),
			indexLimit = Math.min (this._arrayOrigin + this._elementBits.length,
			other._arrayOrigin + other._elementBits.length);
			index < indexLimit; ++index)
		{

			// If the words have bits set in the same position

			if ((this._elementBits[index - this._arrayOrigin] 
				& other._elementBits[index - other._arrayOrigin]) != 0)
			{

				// ... Then there is an element in common

				return false;
			}
		}

		// Found no elements in common

		return true;
	}


	// Forms the union of this set and the specified IntSet.  All the elements
	// in the IntSet are added to this set.  Returns true if any elements
	// were added, false otherwise.  This function invalidates enumerators
	// if the set is modified.

	public boolean union (ConstIntSet other)
	{

		// Return value

		boolean returnValue = false;

		// Enumerate the IntSet

		for (IntEnumeration e = other.elements(); e.hasMoreElements(); )
		{

			// Add the next element

			returnValue |= this.addElement (e.nextElement());
		}

		return returnValue;
	}


}




// Enumerator class

final class SmallIntSetEnumerator implements IntEnumeration
{

	// The set we are enumerating

	protected SmallIntSet set;

	// The invalidation count when this enumerator was created

	protected int _invalidationCount;

	// Index into element bit array
	
	protected int _index;

	// Current bit index

	protected int _bitIndex;

	// Constants used for splitting element values into word and bit values

	protected static final int shiftCount = 5;
	protected static final int bit0 = 0x00000001;
	protected static final int bitMask = 0x0000001F;
	protected static final int bitsPerWord = 32;


	// Create a new enumerator for the specified set.

	SmallIntSetEnumerator (SmallIntSet set)
	{

		// Save the set

		this.set = set;
		this._invalidationCount = set._invalidationCount;

		// Initialize index and bit index to lowest possible values

		this._index = 0x80000000 >> shiftCount;
		this._bitIndex = 0;

		return;
	}


	// Return true if the enumerator can return more elements.

	public boolean hasMoreElements ()
	{

		// Check for valid enumerator

		if (this._invalidationCount != set._invalidationCount)
		{
			throw new InvalidEnumeratorException ("SmallIntSetEnumerator.hasMoreElements");
		}

		// If we're prior to current range, advance to start of range

		if (_index < set._arrayOrigin)
		{
			_index = set._arrayOrigin;
			_bitIndex = 0;
		}

		// Scan past not-present elements

		while (_index < set._arrayOrigin + set._elementBits.length)
		{

			// Get current word

			int word = set._elementBits[_index - set._arrayOrigin];

			// If there are bits set in this word ...

			if (word != 0)
			{

				// Scan past zero bits

				while (_bitIndex < bitsPerWord)
				{

					// If bit set, we can return an element

					if ((word & (bit0 << _bitIndex)) != 0)
					{
						return true;
					}

					// Advance bit index

					++_bitIndex;
				}
			}

			// No remaining bits in this word, advance to next word

			++_index;
			_bitIndex = 0;
		}

		// No bits set, we can't return an element

		return false;
	}


	// Returns the next element in the enumeration.

	public int nextElement ()
	{

		// Check for valid enumerator

		if (this._invalidationCount != set._invalidationCount)
		{
			throw new InvalidEnumeratorException ("SmallIntSetEnumerator.hasMoreElements");
		}

		// If we're prior to current range, advance to start of range

		if (_index < set._arrayOrigin)
		{
			_index = set._arrayOrigin;
			_bitIndex = 0;
		}

		// Scan past not-present elements

		while (_index < set._arrayOrigin + set._elementBits.length)
		{

			// Get current word

			int word = set._elementBits[_index - set._arrayOrigin];

			// If there are bits set in this word ...

			if (word != 0)
			{

				// Scan past zero bits

				while (_bitIndex < bitsPerWord)
				{

					// If bit set, we can return an element

					if ((word & (bit0 << _bitIndex)) != 0)
					{

						// Element to return

						int element = (_index << shiftCount) + _bitIndex;

						// Advance bit index

						++_bitIndex;

						if (_bitIndex == bitsPerWord)
						{
							++_index;
							_bitIndex = 0;
						}

						return element;
					}
				}
			}

			// No remaining bits in this word, advance to next word

			++_index;
			_bitIndex = 0;
		}

		// No bits set, we can't return an element

		throw new NoSuchElementException ("SmallIntSetEnumerator.nextElement");
	}


}

