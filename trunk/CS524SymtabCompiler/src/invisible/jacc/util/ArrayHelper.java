// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  ArrayHelper is a class that defines a set of utility functions for arrays.

  Since all functions are static, there is no need to ever create an instance
  of this class.

->*/


public class ArrayHelper 
{




	// ----- Deep Cloning -----


	// The following functions make a deep clone of an array.  In contrast,
	// the Java function Object.clone(), when applied to an array, makes a
	// shallow clone;  i.e., it copies only the top level of the array
	// hierarchy, and does not clone array elements.
	//
	// Two forms of this function are provided:
	//
	// 1. Deep Clone.  A deep clone of an array copies the entire array
	//    hierarchy down any number of levels.  In addition, for each array
	//    element of reference type that is not itself an array:  (i) it makes
	//    a clone of the element (using Object.clone()) if the element is
	//    cloneable;  and (ii) copies the reference if the element is not
	//    cloneable.
	//
	//    That is, the array is cloned, and array elements of reference type
	//    are also cloned if they are cloneable.
	//
	//    A deep clone of an array should compare deep equal to the original
	//    array (however, this depends on the behavior of the clone() function
	//    applied to array elements).
	//
	//    Note:  Sadly, the deep clone function has been removed to comply with
	//    Java 1.1.  See the note preceding deepClone() below.
	//
	// 2. Deep Shallow Clone.  A deep shallow clone of an array copies the
	//    entire array hierarchy down any number of levels.  In addition, for
	//    each array element of reference type that is not itself an array, it
	//    copies the reference.
	//
	//    That is, the array is cloned, but array elements of reference type
	//    are not cloned.  The original and clone arrays share references to
	//    the same reference-type elements.
	//
	//    A deep shallow clone of an array will compare deep shallow equal to
	//    the original array.
	//
	// These functions can walk down arrays of any depth, regardless of their
	// declared types, even if different paths down the array have different
	// depths.
	//
	// For arrays of primitive type, the two forms of cloning produce the same
	// result.




	// Deep clone the specified object.
	//
	// This function clones the specified object.  If the object is an array,
	// each array element is deep-cloned recursively.
	//
	// If the object is not cloneable, the function returns the original
	// object.  If the object is an array and any array element is not
	// cloneable, then that array element (recursively) is unchanged.
	//
	// This function never throws CloneNotSupportedException.
	//
	// If the argument is null, the function returns null.
	
	/* ->
	
	Note (04/07/98): We have been forced to remove deepClone() to comply
	with Java 1.1.  The problem is that "Object c = p.clone();" won't compile
	under Java 1.1 because class Object declares clone() as protected, which
	means that we can't access it through an explicit reference.  In Java 1.1,
	a class C can access a protected member through an explicit reference only
	if the type of the reference is C or a subclass of C, or if the reference
	is super.  Actually, this has always been the rule in Java (see Java Language
	Specification sections 6.6.2 and 6.6.7), but it is only in Java 1.1 that
	the compiler is enforcing the rule.
	
	There appears to be no satisfactory way to clone an arbitrary Cloneable
	object.  (One possibility would be to use the Java 1.1 Introspection API
	to check if the clone() method is public, and if so, execute it.  But this
	feels like a completely inappropriate use of the Introspection API, and in
	any event it wouldn't work on Java 1.0.)

	public static Object deepClone (Object p)
	{

		// Try block so compiler doesn't complain about CloneNotSupportedException

		try
		{

			// If the object is null, return null

			if (p == null)
			{
				return null;
			}

			// If the object is not cloneable, return the object

			if (!(p instanceof Cloneable))
			{
				return p;
			}

			// Clone the object

			Object c = p.clone();

			// If the object is an array of references ...

			if (c instanceof Object[])
			{

				// Cast the clone object to array type

				Object[] a = (Object[]) c;

				// Clone all the array elements

				for (int i = 0; i < a.length; ++i)
				{
					a[i] = deepClone (a[i]);
				}
			}

			// Return the clone

			return c;
		}

		// We should never reach this exception handler

		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}

	-> */




	// Deep shallow clone the specified object.
	//
	// If the specified object is an array, the function clones it.  If the
	// object is an array of references, the function (recursively) clones
	// each array element that is itself an array.
	//
	// If the specified object is not an array, the function returns the
	// original object.
	//
	// This function never throws CloneNotSupportedException.
	//
	// If the argument is null, the function returns null.

	public static Object deepShallowClone (Object p)
	{

		// If the object is null, return null

		if (p == null)
		{
			return null;
		}
		
		// We need this try block to placate some compilers (see the note
		// preceding the catch clause)
		
		try
		{

			// Clone boolean array

			if (p instanceof boolean[])
			{
				return ((boolean[]) p).clone();
			}

			// Clone byte array

			if (p instanceof byte[])
			{
				return ((byte[]) p).clone();
			}

			// Clone char array

			if (p instanceof char[])
			{
				return ((char[]) p).clone();
			}

			// Clone short array

			if (p instanceof short[])
			{
				return ((short[]) p).clone();
			}

			// Clone int array

			if (p instanceof int[])
			{
				return ((int[]) p).clone();
			}

			// Clone long array

			if (p instanceof long[])
			{
				return ((long[]) p).clone();
			}

			// Clone float array

			if (p instanceof float[])
			{
				return ((float[]) p).clone();
			}

			// Clone double array

			if (p instanceof double[])
			{
				return ((double[]) p).clone();
			}

			// Clone reference array

			if (p instanceof Object[])
			{
				Object[] c = (Object[]) ((Object[]) p).clone();

				for (int i = 0; i < c.length; ++i)
				{
					c[i] = deepShallowClone (c[i]);
				}

				return c;
			}
		}
		
		// This catch clause should never be executed.  It exists for the
		// following reason:  Some Java compilers think that executing clone()
		// on an array can throw CloneNotSupportedException;  such compilers
		// will complain if there isn't a catch clause.  Other Java compilers
		// know that executing clone() on an array cannot throw
		// CloneNotSupportedException;  on such compilers, a catch clause for
		// CloneNotSupportedException would make the compiler complain that
		// the catch clause is unreachable.  To make this code compile
		// properly on both types of compilers, we use a catch clause for any
		// Exception.
		
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
			{
				RuntimeException e2 = (RuntimeException) e;
				throw e2;
			}
			throw new InternalError();
		}

		// Not an array

		return p;
	}




	// ----- Deep Equality -----


	// The following functions compare two arrays for deep equality.  There is
	// no comparable Java function.
	//
	// Two forms of this function are provided:
	//
	// 1. Deep Equal.  Two arrays are deep equal if (i) the array hierarchies
	//    are the same down any number of levels;  and (ii) corresponding
	//    array elements of primitive type are equal to each other;  and (iii)
	//    corresponding array elements of reference type, which are not
	//    themselves arrays, are equal according to Object.equals().
	//
	//    A deep clone of an array should compare deep equal to the original
	//    array (however, this depends on the behavior of the clone() function
	//    applied to array elements).
	//
	// 2. Deep Shallow Equal.  Two arrays are deep shallow equal if (i) the
	//    array hierarchies are the same down any number of levels;  and (ii)
	//    corresponding array elements of primitive type are equal to each
	//    other;  and (iii) corresponding array elements of reference type,
	//    which are not themselves arrays, are equal as references (i.e, refer
	//    to the same object, according to the equality operator ==).
	//
	//    A deep shallow clone of an array will compare deep shallow equal to
	//    the original array.
	//
	// These functions can walk down arrays of any depth, regardless of their
	// declared types, even if different paths down the array have different
	// depths.
	//
	// For arrays of primitive type, the two forms of equality produce the same
	// result.




	// Deep equal function for the specified objects.
	//
	// First, if the two object are reference-equal, or both objects are null,
	// the function returns true.
	//
	// Then, if the objects are of different classes, or one is null and the
	// other is non-null, the function returns false.
	//
	// Then, if the objects are arrays of primitive type, the arrays are
	// compared element-by-element.  If the arrays are the same length and all
	// elements compare equal, the function returns true;  otherwise, it
	// returns false.
	//
	// Then, if the objects are arrays of reference type, the arrays are
	// recursively compared element-by-element for deep equality.  If the
	// arrays are the same length and all elements compare deep equal, the
	// function returns true;  otherwise, it returns false.
	//
	// Finally, if the objects are not arrays, they are compared using the
	// Object.equals() method, and the result is returned.

	public static boolean deepEqual (Object p, Object q)
	{

		// If p and q refer to the same object, or both are null, return equal

		if (p == q)
		{
			return true;
		}

		// If one object is null and the other is non-null, or if the two objects
		// are of different classes, return not-equal

		if ((p == null) || (q == null) || (p.getClass() != q.getClass()))
		{
			return false;
		}

		// Compare boolean array

		if (p instanceof boolean[])
		{

			// Cast to array type

			boolean[] x = (boolean[]) p;
			boolean[] y = (boolean[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare byte array

		if (p instanceof byte[])
		{

			// Cast to array type

			byte[] x = (byte[]) p;
			byte[] y = (byte[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare char array

		if (p instanceof char[])
		{

			// Cast to array type

			char[] x = (char[]) p;
			char[] y = (char[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare short array

		if (p instanceof short[])
		{

			// Cast to array type

			short[] x = (short[]) p;
			short[] y = (short[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare int array

		if (p instanceof int[])
		{

			// Cast to array type

			int[] x = (int[]) p;
			int[] y = (int[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare long array

		if (p instanceof long[])
		{

			// Cast to array type

			long[] x = (long[]) p;
			long[] y = (long[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare float array

		if (p instanceof float[])
		{

			// Cast to array type

			float[] x = (float[]) p;
			float[] y = (float[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare double array

		if (p instanceof double[])
		{

			// Cast to array type

			double[] x = (double[]) p;
			double[] y = (double[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare reference array

		if (p instanceof Object[])
		{

			// Cast to array type

			Object[] x = (Object[]) p;
			Object[] y = (Object[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (!deepEqual (x[i], y[i]))
				{
					return false;
				}
			}

			return true;
		}

		// Not an array

		return p.equals(q);
	}




	// Deep shallow equal function for the specified objects.
	//
	// First, if the two object are reference-equal, or both objects are null,
	// the function returns true.
	//
	// Then, if the objects are of different classes, or one is null and the
	// other is non-null, the function returns false.
	//
	// Then, if the objects are arrays of primitive type, the arrays are
	// compared element-by-element.  If the arrays are the same length and all
	// elements compare equal, the function returns true;  otherwise, it
	// returns false.
	//
	// Then, if the objects are arrays of reference type, the arrays are
	// recursively compared element-by-element for deep shallow equality.  If
	// the arrays are the same length and all elements compare deep shallow
	// equal, the function returns true;  otherwise, it returns false.
	//
	// Finally, if the objects are not arrays, the function returns false.

	public static boolean deepShallowEqual (Object p, Object q)
	{

		// If p and q refer to the same object, or both are null, return equal

		if (p == q)
		{
			return true;
		}

		// If one object is null and the other is non-null, or if the two objects
		// are of different classes, return not-equal

		if ((p == null) || (q == null) || (p.getClass() != q.getClass()))
		{
			return false;
		}

		// Compare boolean array

		if (p instanceof boolean[])
		{

			// Cast to array type

			boolean[] x = (boolean[]) p;
			boolean[] y = (boolean[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare byte array

		if (p instanceof byte[])
		{

			// Cast to array type

			byte[] x = (byte[]) p;
			byte[] y = (byte[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare char array

		if (p instanceof char[])
		{

			// Cast to array type

			char[] x = (char[]) p;
			char[] y = (char[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare short array

		if (p instanceof short[])
		{

			// Cast to array type

			short[] x = (short[]) p;
			short[] y = (short[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare int array

		if (p instanceof int[])
		{

			// Cast to array type

			int[] x = (int[]) p;
			int[] y = (int[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare long array

		if (p instanceof long[])
		{

			// Cast to array type

			long[] x = (long[]) p;
			long[] y = (long[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare float array

		if (p instanceof float[])
		{

			// Cast to array type

			float[] x = (float[]) p;
			float[] y = (float[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare double array

		if (p instanceof double[])
		{

			// Cast to array type

			double[] x = (double[]) p;
			double[] y = (double[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (x[i] != y[i])
				{
					return false;
				}
			}

			return true;
		}

		// Compare reference array

		if (p instanceof Object[])
		{

			// Cast to array type

			Object[] x = (Object[]) p;
			Object[] y = (Object[]) q;

			// Compare lengths

			if (x.length != y.length)
			{
				return false;
			}

			// Compare array elements

			for (int i = 0; i < x.length; ++i)
			{
				if (!deepShallowEqual (x[i], y[i]))
				{
					return false;
				}
			}

			return true;
		}

		// Not an array

		return false;
	}




}

