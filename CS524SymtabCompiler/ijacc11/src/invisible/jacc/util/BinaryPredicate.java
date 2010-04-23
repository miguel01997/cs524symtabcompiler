// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  BinaryPredicate is a functor class that abstracts the idea of a binary
  predicate.  A binary predicate is a function which, given two arguments,
  returns a boolean value.

->*/


public abstract class BinaryPredicate 
{

	// This function returns the value of the binary predicate for two
	// given arguments.

	public abstract boolean value (Object object1, Object object2);


	// The equals method should return true only if it is guaranteed that the
	// two binary predicates return the same value on all inputs.  If this
	// method is not overridden, it returns true only if the two predicates
	// are the same object.

	public boolean equals (Object obj)
	{
		return this == obj;
	}


}

