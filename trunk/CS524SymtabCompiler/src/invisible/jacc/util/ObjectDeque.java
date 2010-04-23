// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/*->

  ObjectDeque implements a deque of objects.  We view a deque as an array which
  is indexed by a range of integers.  The lower and upper limits of the index
  range are both variable, and both can be either positive or negative.  The
  index range can wrap around from the smallest negative integer to the largest
  positive integer.  Storage is allocated automatically as elements are added
  to the deque.

->*/

public class ObjectDeque implements ConstObjectDeque, Cloneable 
{

	// The buffer to hold the elements of the set

	private Object[] _elementData;

	// The number of elements

	private int _elementCount;

	// Default value for initial capacity

	private static final int defaultCapacity = 10;

	// The index of the first element in the deque

	private int _firstIndex;

	// The offset in elementData where the first element is located

	private int _firstOffset;


	// Create a new deque with the specified range.  If the range is nonempty,
	// each element is initialized with the fill value.  Enough storage is 
	// allocated to hold the specified number of additional elements.

    public ObjectDeque (int firstIndex, int elementCount, Object fill, int additionalCapacity)
	{
		super();

		// Validate the arguments

		if ((elementCount < 0)
			|| (additionalCapacity < 0)
			|| ((elementCount + additionalCapacity) < 0))
		{
			throw new IllegalArgumentException ("ObjectDeque.ObjectDeque");
		}

		// Allocate the array

		_elementData = new Object[elementCount + additionalCapacity];

		// Insert the fill value

		for (int i = 0; i < elementCount; ++i)
		{
			_elementData[i] = fill;
		}

		// Initialize indexes

		_firstIndex = firstIndex;
		_elementCount = elementCount;
		_firstOffset = 0;

		return;
    }


	// Create a new deque with the specified range.  If the range is nonempty,
	// each element is initialized with the fill value.

    public ObjectDeque (int firstIndex, int elementCount, Object fill)
	{
		this (firstIndex, elementCount, fill, 0);
	}


	// Create a new empty deque with the specified initial capacity.

    public ObjectDeque (int initialCapacity)
	{
		this (0, 0, null, initialCapacity);
	}


	// Create a new empty deque with the default initial capacity.

    public ObjectDeque ()
	{
		this (0, 0, null, defaultCapacity);
	}


	// Releases all memory used by the deque, except that needed for the
	// deque's current contents plus the specified number of additional
	// elements.  Returns true if memory was released, false otherwise.

	public boolean compact (int additionalCapacity)
	{

		// Validate the argument

		if ((additionalCapacity < 0) || ((_elementCount + additionalCapacity) < 0))
		{
			throw new IllegalArgumentException ("ObjectDeque.compact");
		}

		// If we are already compact, do nothing

		if ((_elementCount + additionalCapacity) == _elementData.length)
		{
			return false;
		}

		// Allocate a new array of the required size

		Object[] newArray = new Object[_elementCount + additionalCapacity];

		// If there are elements to copy ...

		if (_elementCount != 0)
		{

			// Copy pre-wrap data

			int preWrap = Math.min (_elementCount, _elementData.length - _firstOffset);

			System.arraycopy (_elementData, _firstOffset, newArray, 0, preWrap);

			// If there is post-wrap data ...

			if (_elementCount != preWrap)
			{

				// Copy post-wrap data

				System.arraycopy (_elementData, 0, newArray, preWrap, _elementCount - preWrap);
			}
		}

		// Establish the new array

		_elementData = newArray;
		_firstOffset = 0;

		return true;
	}


	// Releases all memory used by the deque, except that needed for the
	// deque's current contents.  Returns true if memory was released, false
	// otherwise.

	public boolean compact ()
	{
		return compact (0);
	}


	// Ensures that the deque has enough storage to hold at least the
	// specified number of additional elements.  Returns true if more memory
	// was allocated, false otherwise.

	public boolean ensureCapacity (int additionalCapacity)
	{

		// Validate the argument

		if ((additionalCapacity < 0) || ((_elementCount + additionalCapacity) < 0))
		{
			throw new IllegalArgumentException ("ObjectDeque.ensureCapacity");
		}

		// If we already have enough storage, do nothing

		if ((_elementCount + additionalCapacity) <= _elementData.length)
		{
			return false;
		}

		// Allocate a new array of the required size

		Object[] newArray = new Object[_elementCount + additionalCapacity];

		// If there are elements to copy ...

		if (_elementCount != 0)
		{

			// Copy pre-wrap data

			int preWrap = Math.min (_elementCount, _elementData.length - _firstOffset);

			System.arraycopy (_elementData, _firstOffset, newArray, 0, preWrap);

			// If there is post-wrap data ...

			if (_elementCount != preWrap)
			{

				// Copy post-wrap data

				System.arraycopy (_elementData, 0, newArray, preWrap, _elementCount - preWrap);
			}
		}

		// Establish the new array

		_elementData = newArray;
		_firstOffset = 0;

		return true;
	}


	// Given an offset and a distance, this function adds the distance to
	// the offset, wrapping around at the end of the array.  The offset must be
	// in the range 0 to _elementData.length-1, while the distance must be in
	// the range 0 to _elementData.length.

	private int circularAdd (int offset, int distance)
	{
		return (distance < (_elementData.length - offset))
			   ? (distance + offset)
			   : (distance - (_elementData.length - offset));
	}


	// Given an offset and a distance, this function subtracts the distance
	// from the offset, wrapping around at the end of the array.  The offset
	// must be in the range 0 to _elementData.length-1, while the distance must
	// be in the range 0 to _elementData.length.

	private int circularSubtract (int offset, int distance)
	{
		return (distance <= offset)
			   ? (offset - distance)
			   : (_elementData.length - (distance - offset));
	}


	// Given a relative index (an index relative to _firstIndex), this
	// function computes the corresponding array offset.  It throws an
	// exception if the index is not within the deque.

	private int arrayOffset (int relativeIndex)
	{

		// Validate the index

		if ((relativeIndex < 0) || (relativeIndex >= _elementCount))
		{
			throw new IndexOutOfBoundsException ("ObjectDeque.arrayOffset");
		}

		// Return the array index

		return circularAdd (_firstOffset, relativeIndex);
	}


	// Given a source offset, source length, and shift distance, this function
	// shifts the source region left (toward lower offsets) the specified
	// distance.  The source and destination regions can wrap around the end
	// of the array.  The source length plus the shift distance must be less
	// than or equal to the length of the array.

	private void circularShiftLeft (int srcOffset, int srcLength, int shiftDistance)
	{

		// Loop while there are elements remaining to shift

		for (int lenRemaining = srcLength; lenRemaining != 0; )
		{

			// First element in current source

			int srcCurrent = circularAdd (srcOffset, srcLength - lenRemaining);

			// First element in current destination

			int dstCurrent = circularSubtract (srcCurrent, shiftDistance);

			// Current length is the length remaining, or the distance to the
			// next wraparound, whichever is less

			int lenCurrent = Math.min (lenRemaining,
				_elementData.length - Math.max (srcCurrent, dstCurrent) );

			// Do the copy

			System.arraycopy (_elementData, srcCurrent, _elementData, dstCurrent, lenCurrent);

			// Adjust the length remaining

			lenRemaining -= lenCurrent;
		}

		return;
	}


	// Given a source offset, source length, and shift distance, this function
	// shifts the source region right (toward higher offsets) the specified
	// distance.  The source and destination regions can wrap around the end
	// of the array.  The source length plus the shift distance must be less
	// than or equal to the length of the array.

	private void circularShiftRight (int srcOffset, int srcLength, int shiftDistance)
	{

		// Loop while there are elements remaining to shift

		for (int lenRemaining = srcLength; lenRemaining != 0; )
		{

			// Last element in current source

			int srcCurrent = circularAdd (srcOffset, lenRemaining - 1);

			// Last element in current destination

			int dstCurrent = circularAdd (srcCurrent, shiftDistance);

			// Current length is the length remaining, or the distance to the
			// next wraparound, whichever is less

			int lenCurrent = Math.min (lenRemaining, 1 + Math.min (srcCurrent, dstCurrent));

			// Do the copy

			System.arraycopy (_elementData, srcCurrent - (lenCurrent - 1),
				_elementData, dstCurrent - (lenCurrent - 1), lenCurrent);

			// Adjust the length remaining

			lenRemaining -= lenCurrent;
		}

		return;
	}


	// Given a destination offset, destination length, and fill value, this
	// function fills the destination region with the specified value.  The
	// source length must be less than or equal to the length of the array.

	private void circularFill (int dstOffset, int dstLength, Object fill)
	{

		// Loop while there are elements remaining to fill

		for (int lenRemaining = dstLength; lenRemaining != 0; )
		{

			// First element in current destination

			int dstCurrent = circularAdd (dstOffset, dstLength - lenRemaining);

			// Current length is the length remaining, or the distance to the
			// next wraparound, whichever is less

			int lenCurrent = Math.min (lenRemaining, _elementData.length - dstCurrent);

			// Do the fill

			for (int i = dstCurrent, iLimit = dstCurrent + lenCurrent; i < iLimit; ++i)
			{
				_elementData[i] = fill;
			}

			// Adjust the length remaining

			lenRemaining -= lenCurrent;
		}

		return;
	}


	// Given a relative index (an index relative to _firstIndex) and a length,
	// this function removes the specified region from the deque.  Elements
	// before the removed region retain their absolute indexes;  elements
	// after the removed region have their absolute indexes reduced by the
	// length of the removed region.  This function throws an exception if
	// the region to remove is not entirely within the deque.

	private void removeRegion (int relIndex, int relLength)
	{

		// Validate the region

		if ((relIndex < 0) || (relIndex > _elementCount)
			|| (relLength < 0) || (relLength > _elementCount - relIndex))
		{
			throw new IndexOutOfBoundsException ("ObjectDeque.removeRegion");
		}

		// If number of elements before region is less than or equal to the
		// number of elements after region ...

		if (relIndex <= (_elementCount - (relIndex + relLength)))
		{

			// Shift the prior elements right

			circularShiftRight (_firstOffset, relIndex, relLength);

			// Fill the vacated area with nulls, so we don't retain pointers
			// to objects not in the deque

			circularFill (_firstOffset, relLength, null);

			// Adjust _firstOffset to follow the shift

			_firstOffset = circularAdd (_firstOffset, relLength);
		}

		// Otherwise, there are more elements before than after the region ...

		else
		{

			// Shift the following elements left

			circularShiftLeft (circularAdd (_firstOffset, relIndex + relLength),
				_elementCount - (relIndex + relLength), relLength );

			// Fill the vacated area with nulls, so we don't retain pointers
			// to objects not in the deque

			circularFill (circularAdd (_firstOffset, _elementCount - relLength),
				relLength, null );
		}

		// Adjust the count of elements in the deque

		_elementCount -= relLength;

		return;
	}


	// Given a relative index (an index relative to _firstIndex), and a length,
	// this function inserts the specified number of slots before the specified
	// element of the deque.  The relative index must point to an element of
	// the deque, or one past the end of the deque.  Elements prior to the
	// specified element retain their absolute indexes;  the specified element
	// and all later elements have their absolute indexes increased by the
	// length of the region inserted.  This function throws an exception if the
	// index is out of range.  The return value is the array index of the first
	// element in the inserted region.

	private int insertRegion (int relIndex, int relLength)
	{

		// Validate the index and length

		if ((relIndex < 0) || (relIndex > _elementCount) || (relLength < 0))
		{
			throw new IndexOutOfBoundsException ("ObjectDeque.arrayOffset");
		}

		// Make sure we have enough capacity for the inserted region

		if (relLength > (_elementData.length - _elementCount))
		{
			ensureCapacity (Math.max (relLength, Math.max (defaultCapacity,
				_elementData.length + (_elementData.length - _elementCount) )));
		}

		// If the number of elements before the insertion point is less than
		// or equal to the number of elements after the insertion point ...

		if (relIndex <= (_elementCount - relIndex))
		{

			// Shift the prior elements left

			circularShiftLeft (_firstOffset, relIndex, relLength);

			// Adjust _firstOffset to follow the shift

			_firstOffset = circularSubtract (_firstOffset, relLength);
		}

		// Otherwise, there are more elements before than after the insertion
		// point ...

		else
		{
			
			// Shift the following elements right

			circularShiftRight (circularAdd (_firstOffset, relIndex),
				_elementCount - relIndex, relLength );
		}

		// Adjust the count of elements in the deque

		_elementCount += relLength;

		// Return the array index of the inserted region

		return circularAdd (_firstOffset, relIndex);
	}


	// Gets the element at the specified distance from the first element.
	// An index of 0 returns the first element.

	public Object peekFirst (int index)
	{
		return _elementData[arrayOffset (index)];
	}


	// Gets the first element.

	public Object peekFirst ()
	{
		return _elementData[arrayOffset (0)];
	}


	// Gets the element at the specified absolute index.

	public Object elementAt (int index)
	{
		return _elementData[arrayOffset (index - _firstIndex)];
	}


	// Gets the element at the specified distance from the last element.
	// An index of 0 returns the last element.  An index of 1 returns
	// the next-to-last element, and so on.

	public Object peekLast (int index)
	{
		return _elementData[arrayOffset ((_elementCount - 1) - index)];
	}


	// Gets the last element.

	public Object peekLast ()
	{
		return _elementData[arrayOffset (_elementCount - 1)];
	}


	// Sets the element at the specified distance from the first element.
	// An index of 0 sets the first element.

	public void setFirst (Object element, int index)
	{
		_elementData[arrayOffset (index)] = element;
		return;
	}


	// Sets the first element.

	public void setFirst (Object element)
	{
		_elementData[arrayOffset (0)] = element;
		return;
	}


	// Sets the element at the specified absolute index.

	public void setElementAt (Object element, int index)
	{
		_elementData[arrayOffset (index - _firstIndex)] = element;
	}


	// Sets the element at the specified distance from the last element.
	// An index of 0 sets the last element.  An index of 1 sets
	// the next-to-last element, and so on.

	public void setLast (Object element, int index)
	{
		_elementData[arrayOffset ((_elementCount - 1) - index)] = element;
		return;
	}


	// Sets the last element.

	public void setLast (Object element)
	{
		_elementData[arrayOffset (_elementCount - 1)] = element;
		return;
	}


	// Gets the element at the specified distance from the first element,
	// and removes it from the deque.  Elements before the removed element
	// have their absolute indexes increased by 1.  An index of 0 removes
	// the first element.

	public Object popFirst (int index)
	{
		Object returnValue = _elementData[arrayOffset (index)];
		removeRegion (index, 1);
		++_firstIndex;
		return returnValue;
	}


	// Gets the first element, and removes it from the deque.  All other
	// elements have their absolute indexes unchanged.

	public Object popFirst ()
	{
		Object returnValue = _elementData[arrayOffset (0)];
		removeRegion (0, 1);
		++_firstIndex;
		return returnValue;
	}


	// Gets the element at the specified distance from the last element, and
	// removes it from the deque.  Elements after the removed element have
	// their absolute indexes decreased by 1.  An index of 0 removes the
	// last element.

	public Object popLast (int index)
	{
		Object returnValue = _elementData[arrayOffset ((_elementCount - 1) - index)];
		removeRegion ((_elementCount - 1) - index, 1);
		return returnValue;
	}


	// Gets the last element, and removes it from the deque.  Elements after
	// the removed element have their absolute indexes decreased by 1.

	public Object popLast ()
	{
		Object returnValue = _elementData[arrayOffset (_elementCount - 1)];
		removeRegion (_elementCount - 1, 1);
		return returnValue;
	}


	// Inserts the element at the specified distance from the start of the
	// deque.  If the index is 0, the element becomes the new first element.
	// If the index is elementCount(), the element becomes the new last
	// element.  Elements before the inserted element have their absolute
	// indexes decreased by 1.

	public void pushFirst (Object element, int index)
	{
		int offset = insertRegion (index, 1);
		_elementData[offset] = element;
		--_firstIndex;
		return;

		// Warning:  Code like the following does not work:
		//
		//		_elementData[insertRegion (index, 1)] = element;
		//
		// This is because the value of _elementData is fetched and remembered
		// before the value inside the brackets is evaluated (Java Language
		// Specification, section 15.12.2).  But insertRegion can change the
		// value of the instance variable _elementData if it needs to allocate
		// additional storage.  In this case, the wrong array would be written.
	}


	// Inserts the element at the start of the deque.  The element becomes the
	// new first element.  All other elements have their absolute indexes
	// unchanged.

	public void pushFirst (Object element)
	{
		int offset = insertRegion (0, 1);
		_elementData[offset] = element;
		--_firstIndex;
		return;
	}


	// Inserts the element at the specified distance from the end of the
	// deque.  If the index is 0, the element becomes the new last element.
	// If the index is elementCount(), the element becomes the new first
	// element.  Elements after the inserted element have their absolute
	// indexes increased by 1.

	public void pushLast (Object element, int index)
	{
		int offset = insertRegion (_elementCount - index, 1);
		_elementData[offset] = element;
		return;
	}


	// Inserts the element at the end of the deque.  The element becomes the
	// new last element.  All other elements have their absolute indexes
	// unchanged.

	public void pushLast (Object element)
	{
		int offset = insertRegion (_elementCount, 1);
		_elementData[offset] = element;
		return;
	}


	// Returns the number of elements in the deque.

	public int elementCount ()
	{
		return _elementCount;
	}


	// Returns true if the deque is empty.

	public boolean isEmpty ()
	{
		return (_elementCount == 0);
	}


	// Removes all the elements from the deque.

	public void removeAllElements ()
	{

		// Remove all elements

		removeRegion (0, _elementCount);

		return;
	}


	// Returns the absolute index of the first element in the deque.

	public int firstIndex ()
	{
		return _firstIndex;
	}


	// Returns the absolute index of the last element in the deque.

	public int lastIndex ()
	{
		return _firstIndex + _elementCount - 1;
	}


	// Sets the absolute index of the first element in the deque.

	public void setFirstIndex (int index)
	{
		_firstIndex = index;
	}


	// Sets the absolute index of the last element in the deque.

	public void setLastIndex (int index)
	{
		_firstIndex = index - (_elementCount - 1);
	}


	// Returns true if the index is a valid absolute index.

	public boolean isIndexValid (int index)
	{
		int relIndex = index - _firstIndex;
		return (relIndex >= 0) && (relIndex < _elementCount);
	}


	// Returns an enumerator for the deque, which returns elements beginning
	// with the first element, and ending with the last element.
	//
	// The enumerator works be maintaining an absolute index, which is
	// incremented during the enumeration.  The enumeration ends when the
	// index becomes invalid.

    public Enumeration elements ()
	{
		return new ObjectDequeEnumerator (this, firstIndex(), 1);
    }


	// Returns an enumerator for the deque, which returns elements beginning
	// with the last element, and ending with the first element.
	//
	// The enumerator works be maintaining an absolute index, which is
	// decremented during the enumeration.  The enumeration ends when the
	// index becomes invalid.

    public Enumeration elementsReversed ()
	{
		return new ObjectDequeEnumerator (this, lastIndex(), -1);
    }


	// Returns the distance from the start of the deque of the first element,
	// at the given position or after, for which the unary predicate is true.
	// Returns -1 if no such element is found.

	public int firstIndexOf (UnaryPredicate pred, int index)
	{

		// Validate the index

		if (index < 0)
		{
			throw new IndexOutOfBoundsException ("ObjectDeque.firstIndexOf");
		}

		// Search for the desired element

		for (int i = index; i < _elementCount; ++i)
		{

			// If this is the element we want ...

			if (pred.value (_elementData[circularAdd (_firstOffset, i)]))
			{

				// Return the index

				return i;
			}
		}

		// Element not found

		return -1;
	}


	// Returns the distance from the start of the deque of the first element
	// for which the unary predicate is true.  Returns -1 if no such element
	// is found.

	public int firstIndexOf (UnaryPredicate pred)
	{
		return firstIndexOf (pred, 0);
	}


	// Returns the distance from the end of the deque of the last element,
	// at the given position or before, for which the unary predicate is true.
	// Returns -1 if no such element is found.

	public int lastIndexOf (UnaryPredicate pred, int index)
	{

		// Validate the index

		if (index < 0)
		{
			throw new IndexOutOfBoundsException ("ObjectDeque.lastIndexOf");
		}

		// Search for the desired element

		for (int i = index; i < _elementCount; ++i)
		{

			// If this is the element we want ...

			if (pred.value (_elementData[circularAdd (_firstOffset, (_elementCount - 1) - i)]))
			{

				// Return the index

				return i;
			}
		}

		// Element not found

		return -1;
	}


	// Returns the distance from the end of the deque of the last element
	// for which the unary predicate is true.  Returns -1 if no such element
	// is found.

	public int lastIndexOf (UnaryPredicate pred)
	{
		return lastIndexOf (pred, 0);
	}


	// Sets the range of absolute indexes for the deque.  Any elements outside
	// the specified range are removed.  Any new elements are initialized with
	// the specified fill value.

	public void setRange (int newFirstIndex, int newElementCount, Object fill)
	{

		// Validate the new element count

		if (newElementCount < 0)
		{
			throw new IllegalArgumentException ("ObjectDeque.setRange");
		}

		// Distance from start of new range to start of old range

		int rangeOffset = _firstIndex - newFirstIndex;

		// If the ranges intersect or abut ...

		if ((rangeOffset >= -_elementCount) && (rangeOffset <= newElementCount))
		{

			// If the start of the old range is prior to the start of
			// the new range ...

			if (rangeOffset < 0)
			{

				// Remove elements prior to the start of the new range

				removeRegion (0, -rangeOffset);

				// Existing range now begins at start of new range

				rangeOffset = 0;
			}

			// If the distance from the start of the old range to the end of the
			// new range is less than the length of the old range ...

			if ((newElementCount - rangeOffset) < _elementCount)
			{

				// Remove elements after the end of the new range

				removeRegion (newElementCount - rangeOffset,
					_elementCount - (newElementCount - rangeOffset) );
			}
		}

		// Otherwise, the ranges are separate ...

		else
		{

			// Remove all elements in the old range

			removeRegion (0, _elementCount);

			// Existing range now begins at start of new range

			rangeOffset = 0;
		}

		// At this point, the existing range is within the new range.
		// Ensure we have capacity for expanding the range.

		ensureCapacity (newElementCount - _elementCount);

		// Insert fill elements before the old range.  The number of elements
		// in the new range prior to the start of the old range is rangeOffset.

		circularFill (insertRegion (0, rangeOffset), rangeOffset, fill);

		// Insert fill elements after the old range.  The number of elements
		// in the new range after the end of the old range is now
		// newElementCount - _elementCount.

		int numberAfter = newElementCount - _elementCount;

		circularFill (insertRegion (_elementCount, numberAfter), numberAfter, fill);

		// Warning:  Code like the following does not work:
		//
		//		circularFill (insertRegion (_elementCount, newElementCount - _elementCount),
		//			newElementCount - _elementCount, fill);
		//
		// This is because insertRegion alters _elementCount, which means that the
		// second argument to circularFill would be incorrect.

		// Establish the new first index

		_firstIndex = newFirstIndex;

		return;
	}

	
	// Creates a shallow copy of the deque.

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			ObjectDeque cloneDeque = (ObjectDeque) super.clone();

			// Clone the data array

			cloneDeque._elementData = (Object[]) this._elementData.clone();

			// Return the clone

			return cloneDeque;
		}
		catch (CloneNotSupportedException e)
		{
		
			// This should never happen, since we are Cloneable

			throw new InternalError();
		}
	}


	// Converts the deque to a string.  Overrides the toString method of
	// class Object.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left brace

		buf.append ("{");

		// Display the absolute index range

		buf.append ("[");
		buf.append (String.valueOf (firstIndex()));
		buf.append ("..");
		buf.append (String.valueOf (lastIndex()));
		buf.append ("]");

		// Enumerate the deque

		Enumeration e = elements ();
		boolean isFirstElement = true;

		while (e.hasMoreElements ())
		{

			// If not the first element, append a comma

			if (isFirstElement)
			{
				isFirstElement = false;
				buf.append (" ");
			}
			else
			{
				buf.append (", ");
			}

			// Convert the element to a string an append it

			buf.append (e.nextElement ().toString ());
		}

		// End with a right brace

		buf.append ("}");

		// Return resulting string

		return buf.toString ();
	}


}




// Enumerator class

final class ObjectDequeEnumerator implements Enumeration
{

	// The deque we are enumerating

	private ObjectDeque _deque;

	// The absolute index of the next element to return

	private int _index;

	// The increment (+1 or -1)

	private int _increment;


	// Create a new enumerator for the specified deque.

	ObjectDequeEnumerator (ObjectDeque deque, int index, int increment)
	{

		// Save the deque

		_deque = deque;
		_index = index;
		_increment = increment;

		return;
	}


	// Return true if the enumerator can return more elements.

	public boolean hasMoreElements ()
	{

		// True if index is valid

		return _deque.isIndexValid(_index);
	}


	// Returns the next element in the enumeration.

	public Object nextElement ()
	{

		// Get the next element to return

		try
		{

			// Get the element at our index position

			Object returnValue = _deque.elementAt(_index);

			// Increment the index

			_index += _increment;

			// Return the next element

			return returnValue;
		}

		// Come here if index was invalid

		catch (IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException ("ObjectDequeEnumerator.nextElement");
		}
	}


}


