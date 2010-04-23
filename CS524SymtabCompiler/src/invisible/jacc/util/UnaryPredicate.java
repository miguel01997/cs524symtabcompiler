// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  UnaryPredicate is a functor class that abstracts the idea of a unary
  predicate.  A unary predicate is a function which, given one argument,
  returns a boolean value.

->*/


public abstract class UnaryPredicate 
{

	// This function returns the value of the unary predicate for one
	// given argument.

	public abstract boolean value (Object object1);


	// The equals method should return true only if it is guaranteed that the
	// two unary predicates return the same value on all inputs.  If this
	// method is not overridden, it returns true only if the two predicates
	// are the same object.

	public boolean equals (Object obj)
	{
		return this == obj;
	}


}

