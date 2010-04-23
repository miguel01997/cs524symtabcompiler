// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  UnaryIntPredicateFactory wraps a set of factory functions which can be used
  to create UnaryIntPredicate objects.

->*/


public final class UnaryIntPredicateFactory 
{


	// Create a unary predicate which is always true.

	public static UnaryIntPredicate always ()
	{
		return new UnaryIntPredicateAlways ();
	}


	// Create a unary predicate which is never true.

	public static UnaryIntPredicate never ()
	{
		return new UnaryIntPredicateNever ();
	}


	// Create a unary predicate which is true iff the argument is
	// equal to the given data value.

	public static UnaryIntPredicate equal (int data1)
	{
		return new UnaryIntPredicateEqual (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// not equal to the given data value.

	public static UnaryIntPredicate notEqual (int data1)
	{
		return new UnaryIntPredicateNotEqual (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// less than to the given data value.

	public static UnaryIntPredicate less (int data1)
	{
		return new UnaryIntPredicateLess (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// less than or equal to the given data value.

	public static UnaryIntPredicate lessEqual (int data1)
	{
		return new UnaryIntPredicateLessEqual (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// greater than the given data value.

	public static UnaryIntPredicate greater (int data1)
	{
		return new UnaryIntPredicateGreater (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// greater than or equal to the given data value.

	public static UnaryIntPredicate greaterEqual (int data1)
	{
		return new UnaryIntPredicateGreaterEqual (data1);
	}


	// Create a unary predicate which is the logical negation of the given
	// predicate.

	public static UnaryIntPredicate not (UnaryIntPredicate pred1)
	{
		return new UnaryIntPredicateNot (pred1);
	}


	// Create a unary predicate which is the logical and of the given
	// predicates.

	public static UnaryIntPredicate and (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new UnaryIntPredicateAnd (pred1, pred2);
	}


	// Create a unary predicate which is the logical or of the given
	// predicates.

	public static UnaryIntPredicate or (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new UnaryIntPredicateOr (pred1, pred2);
	}


	// Create a unary predicate which is the logical exclusive or of the given
	// predicates.

	public static UnaryIntPredicate xor (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new UnaryIntPredicateXor (pred1, pred2);
	}


	// Create a unary predicate which is the trace of the given binary
	// predicate.
	// That is, value(arg1) equals pred1.value(arg1, arg1).

	public static UnaryIntPredicate trace (BinaryIntPredicate pred1)
	{
		return new UnaryIntPredicateTrace (pred1);
	}


	// Create a unary predicate by fixing the first argument of the given
	// binary predicate.
	// That is, value(arg1) equals pred1.value(data1, arg1).

	public static UnaryIntPredicate fix1 (BinaryIntPredicate pred1, int data1)
	{
		return new UnaryIntPredicateFix1 (pred1, data1);
	}


	// Create a unary predicate by fixing the second argument of the given
	// binary predicate.
	// That is, value(arg1) equals pred1.value(arg1, data2).

	public static UnaryIntPredicate fix2 (BinaryIntPredicate pred1, int data2)
	{
		return new UnaryIntPredicateFix2 (pred1, data2);
	}


	// Create a unary predicate which is true iff the argument is greater
	// than or equal to data1, and less than or equal to data2.

	public static UnaryIntPredicate range (int data1, int data2)
	{
		return new UnaryIntPredicateRange (data1, data2);
	}


	// Create a unary predicate which is true iff the argument is a
	// character in the specified string.

	public static UnaryIntPredicate string (String string1)
	{
		return new UnaryIntPredicateString (string1);
	}


	// Create a unary predicate which is true iff the argument is a defined
	// Unicode character, according to Character.isDefined.

	public static UnaryIntPredicate unicode ()
	{
		return new UnaryIntPredicateUnicode ();
	}


	// Create a unary predicate which is true iff the argument is a lower case
	// Unicode character, according to Character.isLowerCase.

	public static UnaryIntPredicate lowerCase ()
	{
		return new UnaryIntPredicateLowerCase ();
	}


	// Create a unary predicate which is true iff the argument is an upper case
	// Unicode character, according to Character.isUpperCase.

	public static UnaryIntPredicate upperCase ()
	{
		return new UnaryIntPredicateUpperCase ();
	}


	// Create a unary predicate which is true iff the argument is a title case
	// Unicode character, according to Character.isTitleCase.

	public static UnaryIntPredicate titleCase ()
	{
		return new UnaryIntPredicateTitleCase ();
	}


	// Create a unary predicate which is true iff the argument is a
	// Unicode digit, according to Character.isDigit.

	public static UnaryIntPredicate digit ()
	{
		return new UnaryIntPredicateDigit ();
	}


	// Create a unary predicate which is true iff the argument is a
	// Unicode letter, according to Character.isLetter.

	public static UnaryIntPredicate letter ()
	{
		return new UnaryIntPredicateLetter ();
	}


	// Create a unary predicate which is true iff the argument is a
	// Unicode letter or digit, according to Character.isLetterOrDigit.

	public static UnaryIntPredicate letterOrDigit ()
	{
		return new UnaryIntPredicateLetterOrDigit ();
	}


	// Create a unary predicate which is true iff the argument is a Java
	// letter, according to Character.isJavaLetter.

	public static UnaryIntPredicate javaLetter ()
	{
		return new UnaryIntPredicateJavaLetter ();
	}


	// Create a unary predicate which is true iff the argument is a Java
	// letter or digit, according to Character.isJavaLetterOrDigit.

	public static UnaryIntPredicate javaLetterOrDigit ()
	{
		return new UnaryIntPredicateJavaLetterOrDigit ();
	}


	// Create a unary predicate which is true iff the argument is a Java
	// space character, according to Character.isSpaceDefined.

	public static UnaryIntPredicate javaSpace ()
	{
		return new UnaryIntPredicateJavaSpace ();
	}


}


// The always predicate

final class UnaryIntPredicateAlways extends UnaryIntPredicate
{

	UnaryIntPredicateAlways ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateAlways)))
		{
			return false;
		}

		UnaryIntPredicateAlways other = (UnaryIntPredicateAlways) obj;

		return true;
	}


}


// The never predicate

final class UnaryIntPredicateNever extends UnaryIntPredicate
{

	UnaryIntPredicateNever ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return false;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateNever)))
		{
			return false;
		}

		UnaryIntPredicateNever other = (UnaryIntPredicateNever) obj;

		return true;
	}


}


// The equal predicate

final class UnaryIntPredicateEqual extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateEqual (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 == data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateEqual)))
		{
			return false;
		}

		UnaryIntPredicateEqual other = (UnaryIntPredicateEqual) obj;

		return this.data1 == other.data1;
	}


}


// The not equal predicate

final class UnaryIntPredicateNotEqual extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateNotEqual (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 != data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateNotEqual)))
		{
			return false;
		}

		UnaryIntPredicateNotEqual other = (UnaryIntPredicateNotEqual) obj;

		return this.data1 == other.data1;
	}


}


// The less predicate

final class UnaryIntPredicateLess extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateLess (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 < data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateLess)))
		{
			return false;
		}

		UnaryIntPredicateLess other = (UnaryIntPredicateLess) obj;

		return this.data1 == other.data1;
	}


}


// The less equal predicate

final class UnaryIntPredicateLessEqual extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateLessEqual (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 <= data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateLessEqual)))
		{
			return false;
		}

		UnaryIntPredicateLessEqual other = (UnaryIntPredicateLessEqual) obj;

		return this.data1 == other.data1;
	}


}


// The greater predicate

final class UnaryIntPredicateGreater extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateGreater (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 > data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateGreater)))
		{
			return false;
		}

		UnaryIntPredicateGreater other = (UnaryIntPredicateGreater) obj;

		return this.data1 == other.data1;
	}


}


// The greater equal predicate

final class UnaryIntPredicateGreaterEqual extends UnaryIntPredicate
{

	private int data1;

	UnaryIntPredicateGreaterEqual (int data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return arg1 >= data1;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateGreaterEqual)))
		{
			return false;
		}

		UnaryIntPredicateGreaterEqual other = (UnaryIntPredicateGreaterEqual) obj;

		return this.data1 == other.data1;
	}


}


// The logical negation predicate

final class UnaryIntPredicateNot extends UnaryIntPredicate
{

	private UnaryIntPredicate pred1;

	UnaryIntPredicateNot (UnaryIntPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (int arg1)
	{
		return !pred1.value (arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateNot)))
		{
			return false;
		}

		UnaryIntPredicateNot other = (UnaryIntPredicateNot) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The logical and predicate

final class UnaryIntPredicateAnd extends UnaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	UnaryIntPredicateAnd (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (arg1) && pred2.value (arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateAnd)))
		{
			return false;
		}

		UnaryIntPredicateAnd other = (UnaryIntPredicateAnd) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or predicate

final class UnaryIntPredicateOr extends UnaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	UnaryIntPredicateOr (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (arg1) || pred2.value (arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateOr)))
		{
			return false;
		}

		UnaryIntPredicateOr other = (UnaryIntPredicateOr) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or predicate

final class UnaryIntPredicateXor extends UnaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	UnaryIntPredicateXor (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (arg1) ^ pred2.value (arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateXor)))
		{
			return false;
		}

		UnaryIntPredicateXor other = (UnaryIntPredicateXor) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The trace predicate

final class UnaryIntPredicateTrace extends UnaryIntPredicate
{

	private BinaryIntPredicate pred1;

	UnaryIntPredicateTrace (BinaryIntPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (arg1, arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateTrace)))
		{
			return false;
		}

		UnaryIntPredicateTrace other = (UnaryIntPredicateTrace) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The fix1 predicate

final class UnaryIntPredicateFix1 extends UnaryIntPredicate
{

	private BinaryIntPredicate pred1;

	private int data1;

	UnaryIntPredicateFix1 (BinaryIntPredicate pred1, int data1)
	{
		super ();
		this.pred1 = pred1;
		this.data1 = data1;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (data1, arg1);
	}


	// Two objects are equal if their contained predicates are equal and their
	// contained data items are identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateFix1)))
		{
			return false;
		}

		UnaryIntPredicateFix1 other = (UnaryIntPredicateFix1) obj;

		return this.pred1.equals (other.pred1) && (this.data1 == other.data1);
	}


}


// The fix2 predicate

final class UnaryIntPredicateFix2 extends UnaryIntPredicate
{

	private BinaryIntPredicate pred1;

	private int data2;

	UnaryIntPredicateFix2 (BinaryIntPredicate pred1, int data2)
	{
		super ();
		this.pred1 = pred1;
		this.data2 = data2;
		return;
	}

	public boolean value (int arg1)
	{
		return pred1.value (arg1, data2);
	}


	// Two objects are equal if their contained predicates are equal and their
	// contained data items are identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateFix2)))
		{
			return false;
		}

		UnaryIntPredicateFix2 other = (UnaryIntPredicateFix2) obj;

		return this.pred1.equals (other.pred1) && (this.data2 == other.data2);
	}


}


// The range predicate

final class UnaryIntPredicateRange extends UnaryIntPredicate
{

	private int data1;

	private int data2;

	UnaryIntPredicateRange (int data1, int data2)
	{
		super ();
		this.data1 = data1;
		this.data2 = data2;
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= data1) && (arg1 <= data2);
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateRange)))
		{
			return false;
		}

		UnaryIntPredicateRange other = (UnaryIntPredicateRange) obj;

		return (this.data1 == other.data1) && (this.data2 == other.data2);
	}


}


// The string predicate

final class UnaryIntPredicateString extends UnaryIntPredicate
{

	private String string1;

	UnaryIntPredicateString (String string1)
	{
		super ();
		this.string1 = string1;
		return;
	}

	public boolean value (int arg1)
	{
		return string1.indexOf (arg1) >= 0;
	}


	// Two objects of this class are equal if the contained data values are
	// equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateString)))
		{
			return false;
		}

		UnaryIntPredicateString other = (UnaryIntPredicateString) obj;

		return this.string1.equals (other.string1);
	}


}


// The unicode predicate

final class UnaryIntPredicateUnicode extends UnaryIntPredicate
{

	UnaryIntPredicateUnicode ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isDefined ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateUnicode)))
		{
			return false;
		}

		UnaryIntPredicateUnicode other = (UnaryIntPredicateUnicode) obj;

		return true;
	}


}


// The lower case predicate

final class UnaryIntPredicateLowerCase extends UnaryIntPredicate
{

	UnaryIntPredicateLowerCase ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isLowerCase ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateLowerCase)))
		{
			return false;
		}

		UnaryIntPredicateLowerCase other = (UnaryIntPredicateLowerCase) obj;

		return true;
	}


}


// The upper case predicate

final class UnaryIntPredicateUpperCase extends UnaryIntPredicate
{

	UnaryIntPredicateUpperCase ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isUpperCase ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateUpperCase)))
		{
			return false;
		}

		UnaryIntPredicateUpperCase other = (UnaryIntPredicateUpperCase) obj;

		return true;
	}


}


// The title case predicate

final class UnaryIntPredicateTitleCase extends UnaryIntPredicate
{

	UnaryIntPredicateTitleCase ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isTitleCase ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateTitleCase)))
		{
			return false;
		}

		UnaryIntPredicateTitleCase other = (UnaryIntPredicateTitleCase) obj;

		return true;
	}


}


// The digit predicate

final class UnaryIntPredicateDigit extends UnaryIntPredicate
{

	UnaryIntPredicateDigit ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isDigit ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateDigit)))
		{
			return false;
		}

		UnaryIntPredicateDigit other = (UnaryIntPredicateDigit) obj;

		return true;
	}


}


// The letter predicate

final class UnaryIntPredicateLetter extends UnaryIntPredicate
{

	UnaryIntPredicateLetter ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isLetter ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateLetter)))
		{
			return false;
		}

		UnaryIntPredicateLetter other = (UnaryIntPredicateLetter) obj;

		return true;
	}


}


// The letter or digit predicate

final class UnaryIntPredicateLetterOrDigit extends UnaryIntPredicate
{

	UnaryIntPredicateLetterOrDigit ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isLetterOrDigit ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateLetterOrDigit)))
		{
			return false;
		}

		UnaryIntPredicateLetterOrDigit other = (UnaryIntPredicateLetterOrDigit) obj;

		return true;
	}


}


// The java letter predicate

final class UnaryIntPredicateJavaLetter extends UnaryIntPredicate
{

	UnaryIntPredicateJavaLetter ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isJavaLetter ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateJavaLetter)))
		{
			return false;
		}

		UnaryIntPredicateJavaLetter other = (UnaryIntPredicateJavaLetter) obj;

		return true;
	}


}


// The java letter or digit predicate

final class UnaryIntPredicateJavaLetterOrDigit extends UnaryIntPredicate
{

	UnaryIntPredicateJavaLetterOrDigit ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isJavaLetterOrDigit ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateJavaLetterOrDigit)))
		{
			return false;
		}

		UnaryIntPredicateJavaLetterOrDigit other = (UnaryIntPredicateJavaLetterOrDigit) obj;

		return true;
	}


}


// The java space predicate

final class UnaryIntPredicateJavaSpace extends UnaryIntPredicate
{

	UnaryIntPredicateJavaSpace ()
	{
		super ();
		return;
	}

	public boolean value (int arg1)
	{
		return (arg1 >= 0) && (arg1 <= 0xFFFF)
			   && Character.isSpace ((char) arg1);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryIntPredicateJavaSpace)))
		{
			return false;
		}

		UnaryIntPredicateJavaSpace other = (UnaryIntPredicateJavaSpace) obj;

		return true;
	}


}
