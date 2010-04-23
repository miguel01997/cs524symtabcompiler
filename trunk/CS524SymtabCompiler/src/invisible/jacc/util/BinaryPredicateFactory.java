// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  BinaryPredicateFactory wraps a set of factory functions which can be used
  to create BinaryPredicate objects.

->*/


public final class BinaryPredicateFactory 
{


	// Create a binary predicate which is always true.

	public static BinaryPredicate always ()
	{
		return new BinaryPredicateAlways ();
	}


	// Create a binary predicate which is never true.

	public static BinaryPredicate never ()
	{
		return new BinaryPredicateNever ();
	}


	// Create a binary predicate which is true iff the two arguments are
	// identical (i.e., refer to the same object, or are both null).

	public static BinaryPredicate identity ()
	{
		return new BinaryPredicateIdentity ();
	}


	// Create a binary predicate which is true iff the two arguments are
	// equal (according to the equals method) or both null.

	public static BinaryPredicate equality ()
	{
		return new BinaryPredicateEquality ();
	}


	// Create a binary predicate which is the logical negation of the given
	// predicate.

	public static BinaryPredicate not (BinaryPredicate pred1)
	{
		return new BinaryPredicateNot (pred1);
	}


	// Create a binary predicate which is the transpose of the given
	// predicate.

	public static BinaryPredicate transpose (BinaryPredicate pred1)
	{
		return new BinaryPredicateTranspose (pred1);
	}


	// Create a binary predicate which is the logical and of the given
	// predicates.

	public static BinaryPredicate and (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		return new BinaryPredicateAnd (pred1, pred2);
	}


	// Create a binary predicate which is the logical or of the given
	// predicates.

	public static BinaryPredicate or (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		return new BinaryPredicateOr (pred1, pred2);
	}


	// Create a binary predicate which is the logical exclusive or of the given
	// predicates.

	public static BinaryPredicate xor (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		return new BinaryPredicateXor (pred1, pred2);
	}


	// Create a binary predicate which is the logical and of the given
	// unary predicates.
	// That is, value(object1, object2) equals the logical and of
	// pred1.value(object1) and pred2.value(object2).

	public static BinaryPredicate and (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new BinaryPredicateAndU (pred1, pred2);
	}


	// Create a binary predicate which is the logical or of the given
	// unary predicates.
	// That is, value(object1, object2) equals the logical or of
	// pred1.value(object1) and pred2.value(object2).

	public static BinaryPredicate or (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new BinaryPredicateOrU (pred1, pred2);
	}


	// Create a binary predicate which is the logical exclusive or of the given
	// unary predicates.
	// That is, value(object1, object2) equals the logical exclusive or of
	// pred1.value(object1) and pred2.value(object2).

	public static BinaryPredicate xor (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		return new BinaryPredicateXorU (pred1, pred2);
	}


}


// The always predicate

final class BinaryPredicateAlways extends BinaryPredicate
{

	BinaryPredicateAlways ()
	{
		super ();
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateAlways)))
		{
			return false;
		}

		BinaryPredicateAlways other = (BinaryPredicateAlways) obj;

		return true;
	}


}


// The never predicate

final class BinaryPredicateNever extends BinaryPredicate
{

	BinaryPredicateNever ()
	{
		super ();
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return false;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateNever)))
		{
			return false;
		}

		BinaryPredicateNever other = (BinaryPredicateNever) obj;

		return true;
	}


}


// The identity predicate

final class BinaryPredicateIdentity extends BinaryPredicate
{

	BinaryPredicateIdentity ()
	{
		super ();
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return object1 == object2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateIdentity)))
		{
			return false;
		}

		BinaryPredicateIdentity other = (BinaryPredicateIdentity) obj;

		return true;
	}


}


// The equality predicate

final class BinaryPredicateEquality extends BinaryPredicate
{

	BinaryPredicateEquality ()
	{
		super ();
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		if ((object1 == null) || (object2 == null))
		{
			return object1 == object2;
		}

		return object1.equals (object2);
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateEquality)))
		{
			return false;
		}

		BinaryPredicateEquality other = (BinaryPredicateEquality) obj;

		return true;
	}


}


// The logical negation predicate

final class BinaryPredicateNot extends BinaryPredicate
{

	private BinaryPredicate pred1;

	BinaryPredicateNot (BinaryPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return !pred1.value (object1, object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateNot)))
		{
			return false;
		}

		BinaryPredicateNot other = (BinaryPredicateNot) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The transpose predicate

final class BinaryPredicateTranspose extends BinaryPredicate
{

	private BinaryPredicate pred1;

	BinaryPredicateTranspose (BinaryPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object2, object1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateTranspose)))
		{
			return false;
		}

		BinaryPredicateTranspose other = (BinaryPredicateTranspose) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The logical and predicate

final class BinaryPredicateAnd extends BinaryPredicate
{

	private BinaryPredicate pred1;

	private BinaryPredicate pred2;

	BinaryPredicateAnd (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1, object2) && pred2.value (object1, object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateAnd)))
		{
			return false;
		}

		BinaryPredicateAnd other = (BinaryPredicateAnd) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or predicate

final class BinaryPredicateOr extends BinaryPredicate
{

	private BinaryPredicate pred1;

	private BinaryPredicate pred2;

	BinaryPredicateOr (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1, object2) || pred2.value (object1, object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateOr)))
		{
			return false;
		}

		BinaryPredicateOr other = (BinaryPredicateOr) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or predicate

final class BinaryPredicateXor extends BinaryPredicate
{

	private BinaryPredicate pred1;

	private BinaryPredicate pred2;

	BinaryPredicateXor (BinaryPredicate pred1, BinaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1, object2) ^ pred2.value (object1, object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateXor)))
		{
			return false;
		}

		BinaryPredicateXor other = (BinaryPredicateXor) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical and-unary predicate

final class BinaryPredicateAndU extends BinaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	BinaryPredicateAndU (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1) && pred2.value (object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateAndU)))
		{
			return false;
		}

		BinaryPredicateAndU other = (BinaryPredicateAndU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or-unary predicate

final class BinaryPredicateOrU extends BinaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	BinaryPredicateOrU (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1) || pred2.value (object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateOrU)))
		{
			return false;
		}

		BinaryPredicateOrU other = (BinaryPredicateOrU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or-unary predicate

final class BinaryPredicateXorU extends BinaryPredicate
{

	private UnaryPredicate pred1;

	private UnaryPredicate pred2;

	BinaryPredicateXorU (UnaryPredicate pred1, UnaryPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (Object object1, Object object2)
	{
		return pred1.value (object1) ^ pred2.value (object2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryPredicateXorU)))
		{
			return false;
		}

		BinaryPredicateXorU other = (BinaryPredicateXorU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}
