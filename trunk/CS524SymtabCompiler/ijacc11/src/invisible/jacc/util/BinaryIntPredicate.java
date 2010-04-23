// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  BinaryIntPredicate is a functor class that abstracts the idea of a binary
  predicate.  A binary predicate is a function which, given two arguments,
  returns a boolean value.

->*/


public abstract class BinaryIntPredicate 
{

	// This function returns the value of the binary predicate for two
	// given arguments.

	public abstract boolean value (int arg1, int arg2);


	// The equals method should return true only if it is guaranteed that the
	// two binary predicates return the same value on all inputs.  If this
	// method is not overridden, it returns true only if the two predicates
	// are the same object.

	public boolean equals (Object obj)
	{
		return this == obj;
	}


}

