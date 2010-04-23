// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  UnaryPredicateFactory wraps a set of factory functions which can be used
  to create UnaryPredicate objects.

->*/


public final class UnaryPredicateFactory 
{


	// Create a unary predicate which is always true.

	public static UnaryPredicate always ()
	{
		return new UnaryPredicateAlways ();
	}


	// Create a unary predicate which is never true.

	public static UnaryPredicate never ()
	{
		return new UnaryPredicateNever ();
	}


	// Create a unary predicate which is true iff the argument is
	// identical to the given object (i.e., refers to the same object,
	// or are both null).

	public static UnaryPredicate identity (Object data1)
	{
		return new UnaryPredicateIdentity (data1);
	}


	// Create a unary predicate which is true iff the argument is
	// equal to the given object (according to the equals method), or
	// are both null.

	public static UnaryPredicate equality (Object data1)
	{
		return new UnaryPredicateEquality (data1);
	}


	// Create a unary predicate which is the logical negation of the given
	// predicate.

	public static UnaryPredicate not (UnaryPredicate pred1)
	{
		return new UnaryPredicateNot (pred1);
	}


	// Create a unary predicate which is the logical and of the given
	// predicates.

	public static UnaryPredicate and (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new UnaryPredicateAnd (pred1, pred2);
	}


	// Create a unary predicate which is the logical or of the given
	// predicates.

	public static UnaryPredicate or (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new UnaryPredicateOr (pred1, pred2);
	}


	// Create a unary predicate which is the logical exclusive or of the given
	// predicates.

	public static UnaryPredicate xor (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new UnaryPredicateXor (pred1, pred2);
	}


	// Create a unary predicate which is the trace of the given binary
	// predicate.
	// That is, value(object1) equals pred1.value(object1, object1).

	public static UnaryPredicate trace (BinaryPredicate pred1)
	{
		return new UnaryPredicateTrace (pred1);
	}


	// Create a unary predicate by fixing the first argument of the given
	// binary predicate.
	// That is, value(object1) equals pred1.value(data1, object1).

	public static UnaryPredicate fix1 (BinaryPredicate pred1, Object data1)
	{
		return new UnaryPredicateFix1 (pred1, data1);
	}


	// Create a unary predicate by fixing the second argument of the given
	// binary predicate.
	// That is, value(object1) equals pred1.value(object1, data2).

	public static UnaryPredicate fix2 (BinaryPredicate pred1, Object data2)
	{
		return new UnaryPredicateFix2 (pred1, data2);
	}


}


// The always predicate

final class UnaryPredicateAlways extends UnaryPredicate
{

	UnaryPredicateAlways ()
	{
		super ();
		return;
	}

	public boolean value (Object object1)
	{
		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateAlways)))
		{
			return false;
		}

		UnaryPredicateAlways other = (UnaryPredicateAlways) obj;

		return true;
	}


}


// The never predicate

final class UnaryPredicateNever extends UnaryPredicate
{

	UnaryPredicateNever ()
	{
		super ();
		return;
	}

	public boolean value (Object object1)
	{
		return false;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateNever)))
		{
			return false;
		}

		UnaryPredicateNever other = (UnaryPredicateNever) obj;

		return true;
	}


}


// The identity predicate

final class UnaryPredicateIdentity extends UnaryPredicate
{

	private Object data1;

	UnaryPredicateIdentity (Object data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (Object object1)
	{
		return object1 == data1;
	}


	// Two objects of this class are equal if the contained data items are
	// identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateIdentity)))
		{
			return false;
		}

		UnaryPredicateIdentity other = (UnaryPredicateIdentity) obj;

		return this.data1 == other.data1;
	}


}


// The equality predicate

final class UnaryPredicateEquality extends UnaryPredicate
{

	private Object data1;

	UnaryPredicateEquality (Object data1)
	{
		super ();
		this.data1 = data1;
		return;
	}

	public boolean value (Object object1)
	{
		if ((object1 == null) || (data1 == null))
		{
			return object1 == data1;
		}

		return object1.equals (data1);
	}


	// Two objects of this class are equal if the contained data items are
	// both null or are equal to each other

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateEquality)))
		{
			return false;
		}

		UnaryPredicateEquality other = (UnaryPredicateEquality) obj;

		return (
				   (
					   (this.data1 == null)
				    && (other.data1 == null)
				   )
			    || (
				       (this.data1 != null)
				    && (other.data1 != null)
				    && (this.data1.equals (other.data1))
				   )
			   );
	}


}


// The logical negation predicate

final class UnaryPredicateNot extends UnaryPredicate
{

	private UnaryPredicate pred1;

	UnaryPredicateNot (UnaryPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (Object object1)
	{
		return !pred1.value (object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateNot)))
		{
			return false;
		}

		UnaryPredicateNot other = (UnaryPredicateNot) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The logical and predicate

final class UnaryPredicateAnd extends UnaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	UnaryPredicateAnd (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (object1) && pred2.value (object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateAnd)))
		{
			return false;
		}

		UnaryPredicateAnd other = (UnaryPredicateAnd) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or predicate

final class UnaryPredicateOr extends UnaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	UnaryPredicateOr (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (object1) || pred2.value (object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateOr)))
		{
			return false;
		}

		UnaryPredicateOr other = (UnaryPredicateOr) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or predicate

final class UnaryPredicateXor extends UnaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	UnaryPredicateXor (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (object1) ^ pred2.value (object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateXor)))
		{
			return false;
		}

		UnaryPredicateXor other = (UnaryPredicateXor) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The trace predicate

final class UnaryPredicateTrace extends UnaryPredicate
{

	private BinaryPredicate pred1;

	UnaryPredicateTrace (BinaryPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (object1, object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateTrace)))
		{
			return false;
		}

		UnaryPredicateTrace other = (UnaryPredicateTrace) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The fix1 predicate

final class UnaryPredicateFix1 extends UnaryPredicate
{

	private BinaryPredicate pred1;

	private Object data1;

	UnaryPredicateFix1 (BinaryPredicate pred1, Object data1)
	{
		super ();
		this.pred1 = pred1;
		this.data1 = data1;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (data1, object1);
	}


	// Two objects are equal if their contained predicates are equal and their
	// contained data items are identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateFix1)))
		{
			return false;
		}

		UnaryPredicateFix1 other = (UnaryPredicateFix1) obj;

		return this.pred1.equals (other.pred1) && (this.data1 == other.data1);
	}


}


// The fix2 predicate

final class UnaryPredicateFix2 extends UnaryPredicate
{

	private BinaryPredicate pred1;

	private Object data2;

	UnaryPredicateFix2 (BinaryPredicate pred1, Object data2)
	{
		super ();
		this.pred1 = pred1;
		this.data2 = data2;
		return;
	}

	public boolean value (Object object1)
	{
		return pred1.value (object1, data2);
	}


	// Two objects are equal if their contained predicates are equal and their
	// contained data items are identical

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof UnaryPredicateFix2)))
		{
			return false;
		}

		UnaryPredicateFix2 other = (UnaryPredicateFix2) obj;

		return this.pred1.equals (other.pred1) && (this.data2 == other.data2);
	}


}
