// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/*->

  PredicateSet implements a set of objects.  Storage is automatically
  allocated as objects are added.

  A BinaryPredicate is supplied which is true when two objects are
  considered equal.  The set never contains two equal objects.  The binary
  _predicate is assumed to be symmetric.

->*/


public class PredicateSet implements ConstPredicateSet, Cloneable 
{

	// The buffer to hold the elements of the set

	protected Object[] _elementData;

	// The index into elements where the next element can be added

	protected int _highWaterMark;

	// The number of (non-null) elements

	protected int _elementCount;

	// Default value for initial capacity

	protected static final int defaultCapacity = 10;

	// Number of times set has been compacted

	protected int _compactionCount;

	// The binary _predicate used to test for equality

	protected BinaryPredicate _predicate;


	// Creates a new set with the specified initial capacity

    public PredicateSet (BinaryPredicate equalityPredicate, int initialCapacity)
	{
		super();

		// Validate the arguments

		if ((equalityPredicate == null) || (initialCapacity < 0))
		{
			throw new IllegalArgumentException ("PredicateSet.PredicateSet");
		}

		// Allocate the array

		_elementData = new Object[initialCapacity];

		// Save the equality _predicate

		_predicate = equalityPredicate;

		// Initialize to empty set

		_elementCount = 0;
		_highWaterMark = 0;
		_compactionCount = 0;

		return;
    }

	
	// Creates a new set with default initial capacity

    public PredicateSet (BinaryPredicate equalityPredicate)
	{
		this (equalityPredicate, defaultCapacity);
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
			throw new IllegalArgumentException ("PredicateSet.compact");
		}

		// If we are already compact, do nothing

		if ((_elementCount == _highWaterMark)
			&& ((_highWaterMark + additionalCapacity) == _elementData.length))
		{
			return false;
		}

		// Allocate a new array of the required size

		Object[] newArray = new Object[_elementCount + additionalCapacity];

		// Copy elements, discarding nulls

		for (int i = 0, j = 0; j < _elementCount; ++i)
		{
			if (_elementData[i] != null)
			{
				newArray[j++] = _elementData[i];
			}
		}

		// Establish the new array
		 
		_elementData = newArray;
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
			throw new IllegalArgumentException ("PredicateSet.ensureCapacity");
		}

		// If we have enough capacity, do nothing

		if ((_elementData.length - _highWaterMark) >= additionalCapacity)
		{
			return false;
		}

		// Allocate a new array of the required size

		Object[] newArray = new Object[_highWaterMark + additionalCapacity];

		// Copy array below the high water mark

		if (_highWaterMark > 0)
		{
			System.arraycopy (_elementData, 0, newArray, 0, _highWaterMark);
		}

		// Establish the new array
		 
		_elementData = newArray;

		return true;
	}


	// Searches the set for an element equal to the argument.  If the set
	// contains an element equal to the argument, the element is returned.
	// Otherwise, null is returned.

	public Object findElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.addElement");
		}

		// If element is in the set, return it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], element))
			{
				return _elementData[i];
			}
		}

		// Otherwise, return null

		return null;
	}


	// Adds the specified element to the set.  If the set already contains an
	// element equal to the argument, the set is unchanged and the function
	// returns false.  Otherwise, the argument is added to the set and the
	// function returns true.
	//
	// This function does not invalidate enumerators.  Any active enumerator
	// will see the new element.

	public boolean addElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.addElement");
		}

		// If element is already in the set, do nothing

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], element))
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


	// Adds the specified element to the set.  If the set already contains an
	// element equal to the argument, the set is unchanged and the existing
	// element is returned.  Otherwise, the argument is added to the set and
	// null is returned.
	//
	// This function does not invalidate enumerators.  Any active enumerator
	// will see the new element.

	public Object findAddElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.addElement");
		}

		// If element is already in the set, return it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], element))
			{
				return _elementData[i];
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

		return null;
	}


	// Removes the specified element from the set.  If the set contains an
	// element equal to the argument, the existing element is removed and
	// the function returns true.  Otherwise, the set is unchanged and the
	// function returns false.
	//
	// This function does not invalidate enumerators;  any active enumerator
	// will not see the removed element.  Storage occupied by the removed
	// element is never reused or released until the set is compacted.

	public boolean removeElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.removeElement");
		}

		// If element is in the set, remove it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], element))
			{
				
				// Remove the element from the set

				_elementData[i] = null;
				--_elementCount;

				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Removes the specified element from the set.  If the set contains an
	// element equal to the argument, the existing element is removed and
	// the function returns the existing element.  Otherwise, the set is
	// unchanged and the function returns null.
	//
	// This function does not invalidate enumerators;  any active enumerator
	// will not see the removed element.  Storage occupied by the removed
	// element is never reused or released until the set is compacted.

	public Object findRemoveElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.removeElement");
		}

		// If element is in the set, remove it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], element))
			{
				
				// Remove the element from the set

				Object existing = _elementData[i];

				_elementData[i] = null;
				--_elementCount;

				return existing;
			}
		}

		// Element not found in set

		return null;
	}


	// Removes oldElement from the set and replaces it with newElement.
	// The effect is the same as first removing oldElement and then adding
	// newElement, except that if oldElement was in the set and newElement
	// was not in the set then newElement is placed in the position previously
	// occupied by oldElement (instead of being placed at the end of the
	// set).  Returns true if the contents of the set is modified, false
	// otherwise.
	//
	// This function does not invalidate enumerators;  any active
	// enumerator that has not yet seen oldElement will see newElement.
	// Storage is never reused or released until the set is compacted. 

	public boolean replaceElement (Object oldElement, Object newElement)
	{

		// Validate the arguments

		if ((oldElement == null) || (newElement == null))
		{
			throw new NullPointerException ("PredicateSet.replaceElement");
		}

		// If old element is in the set, remove it

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if ((_elementData[i] != null) && _predicate.value (_elementData[i], oldElement))
			{

				// If the old and new elements are the same, nothing to do

				if (oldElement == newElement)	// Need object identity here
				{
					return false;
				}

				// If new element is already in the set, just remove the old element

				for (int j = 0; j < _highWaterMark; ++j)
				{
					if ((j != i) && ((_elementData[j] != null)
						             && _predicate.value (_elementData[j], newElement)))
					{

						_elementData[i] = null;
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
			_elementData[i] = null;
		}

		_elementCount = 0;

		return;
	}


	// Returns true if the specified object is an element of the set.
	//
	// Note:  This function returns true only if the identical object is
	// in the set.  Use findElement to determine if an equal object is in
	// the set.

	public boolean isElement (Object element)
	{

		// Validate the argument

		if (element == null)
		{
			throw new NullPointerException ("PredicateSet.isElement");
		}

		// Search set to find element

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] == element)	// Need object identity here
			{
				return true;
			}
		}

		// Element not found in set

		return false;
	}


	// Compares two sets to see if they are equal.  Returns true if the
	// argument is a PredicateSet whose elements are equal to the elements
	// of this set;  otherwise returns false.
	//
	// Note:  In order for two PredicateSet objects to be equal, the
	// contained BinaryPredicate objects must be equal (as determined by the
	// equals method).
	//
	// Overrides the equals method of Object.

	public boolean equals (Object obj)
	{

		// Check that we've been given a PredicateSet

		if ((obj == null) || (!(obj instanceof PredicateSet)))
		{
			return false;
		}

		// Convert argument to PredicateSet

		PredicateSet other = (PredicateSet) obj;

		// Check the other set has an equal _predicate

		if (!this._predicate.equals (other._predicate))
		{
			return false;
		}

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

			Object element = other._elementData[i];
			if (element != null)
			{

				// Check if element belongs to this set

				for (int j = 0; j < this._highWaterMark; ++j)
				{
					if ((this._elementData[j] != null)
						&& this._predicate.value (this._elementData[j], element))
					{
						continue outerLoop;
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


    public Enumeration elements ()
	{
		return new PredicateSetEnumerator (this);
    }

	
	// Creates a shallow copy of the set.

    public Object clone ()
	{
		try
		{

			// Invoke the superclass (Object) clone method, which creates
			// a new object of this class and copies all the instance
			// variables

			PredicateSet cloneSet = (PredicateSet) super.clone();

			// Allocate a new array for the clone

			cloneSet._elementData = new Object[this._elementData.length];

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

		Enumeration e = elements ();
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

			buf.append (e.nextElement ().toString ());
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

	public boolean union (ConstPredicateSet constOther)
	{

		// Get access to internals of the other set

		PredicateSet other = (PredicateSet) constOther;

		// Validate the argument

		if (other == null)
		{
			throw new NullPointerException ("PredicateSet.union");
		}

		// Return value

		boolean returnValue = false;

		// Add each element in the other set

		for (int i = 0; i < other._highWaterMark; ++i)
		{
			if (other._elementData[i] != null)
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

	public boolean intersection (ConstPredicateSet constOther)
	{

		// Get access to internals of the other set

		PredicateSet other = (PredicateSet) constOther;

		// Validate the argument

		if (other == null)
		{
			throw new NullPointerException ("PredicateSet.intersection");
		}

		// Return value

		boolean returnValue = false;

		// Remove each element not in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != null)
			{
				if (!other.isElement (_elementData[i]))
				{
				
					// Remove the element from this set

					_elementData[i] = null;
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

	public boolean difference (ConstPredicateSet constOther)
	{

		// Get access to internals of the other set

		PredicateSet other = (PredicateSet) constOther;

		// Validate the argument

		if (other == null)
		{
			throw new NullPointerException ("PredicateSet.difference");
		}

		// Return value

		boolean returnValue = false;

		// Remove each element in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != null)
			{
				if (other.isElement (_elementData[i]))
				{
				
					// Remove the element from this set

					_elementData[i] = null;
					--_elementCount;
					returnValue = true;
				}
			}
		}

		return returnValue;
	}


	// Returns true if this set and the other set are disjoint, that is, if
	// their intersection is empty.

	public boolean isDisjoint (ConstPredicateSet constOther)
	{

		// Get access to internals of the other set

		PredicateSet other = (PredicateSet) constOther;

		// Validate the argument

		if (other == null)
		{
			throw new NullPointerException ("PredicateSet.isDisjoint");
		}

		// Check each element to see if it is in the other set

		for (int i = 0; i < _highWaterMark; ++i)
		{
			if (_elementData[i] != null)
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

final class PredicateSetEnumerator implements Enumeration
{

	// The set we are enumerating

	protected PredicateSet set;

	// The compaction count when this enumerator was created

	protected int _compactionCount;

	// Index into element data array
	
	protected int _elementIndex;


	// Create a new enumerator for the specified set.

	PredicateSetEnumerator (PredicateSet set)
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
			throw new InvalidEnumeratorException ("PredicateSetEnumerator.hasMoreElements");
		}

		// Scan past empty slots
		
		while (_elementIndex < set._highWaterMark)
		{

			// If nonempty slot, we can return an element

			if (set._elementData[_elementIndex] != null)
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

	public Object nextElement ()
	{

		// Check for valid enumerator

		if (this._compactionCount != set._compactionCount)
		{
			throw new InvalidEnumeratorException ("PredicateSetEnumerator.nextElement");
		}

		// Scan past empty slots
		
		while (_elementIndex < set._highWaterMark)
		{

			// Get element in next slot

			Object element = set._elementData[_elementIndex++];

			// If slot nonempty, return the element

			if (element != null)
			{
				return element;
			}
		}

		// No nonempty slots, we can't return an element

		throw new NoSuchElementException("PredicateSetEnumerator.nextElement");
	}


}

