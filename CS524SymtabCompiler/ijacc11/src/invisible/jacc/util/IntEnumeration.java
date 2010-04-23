// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  IntEnumeration is the same as java.util.Enumeration, except that it
  enumerates a list of int (instead of a list of Object).

->*/


public interface IntEnumeration
{

	// Returns true if the enumeration contains more elements; false
	// if it is empty.

	boolean hasMoreElements ();


	// Returns the next element of the enumeration.  Calls to this
	// method will enumerate successive elements.

	int nextElement ();
}

