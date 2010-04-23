// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  BinaryIntPredicateFactory wraps a set of factory functions which can be used
  to create BinaryIntPredicate objects.

->*/


public final class BinaryIntPredicateFactory 
{


	// Create a binary predicate which is always true.

	public static BinaryIntPredicate always ()
	{
		return new BinaryIntPredicateAlways ();
	}


	// Create a binary predicate which is never true.

	public static BinaryIntPredicate never ()
	{
		return new BinaryIntPredicateNever ();
	}


	// Create a binary predicate which is true iff the first argument is
	// equal to the second argument

	public static BinaryIntPredicate equal ()
	{
		return new BinaryIntPredicateEqual ();
	}


	// Create a binary predicate which is true iff the first argument is
	// not equal to the second argument

	public static BinaryIntPredicate notEqual ()
	{
		return new BinaryIntPredicateNotEqual ();
	}


	// Create a binary predicate which is true iff the first argument is
	// less than the second argument

	public static BinaryIntPredicate less ()
	{
		return new BinaryIntPredicateLess ();
	}


	// Create a binary predicate which is true iff the first argument is
	// less than or equal to the second argument

	public static BinaryIntPredicate lessEqual ()
	{
		return new BinaryIntPredicateLessEqual ();
	}


	// Create a binary predicate which is true iff the first argument is
	// greater than the second argument

	public static BinaryIntPredicate greater ()
	{
		return new BinaryIntPredicateGreater ();
	}


	// Create a binary predicate which is true iff the first argument is
	// greater than or equal to the second argument

	public static BinaryIntPredicate greaterEqual ()
	{
		return new BinaryIntPredicateGreaterEqual ();
	}


	// Create a binary predicate which is the logical negation of the given
	// predicate.

	public static BinaryIntPredicate not (BinaryIntPredicate pred1)
	{
		return new BinaryIntPredicateNot (pred1);
	}


	// Create a binary predicate which is the transpose of the given
	// predicate.

	public static BinaryIntPredicate transpose (BinaryIntPredicate pred1)
	{
		return new BinaryIntPredicateTranspose (pred1);
	}


	// Create a binary predicate which is the logical and of the given
	// predicates.

	public static BinaryIntPredicate and (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		return new BinaryIntPredicateAnd (pred1, pred2);
	}


	// Create a binary predicate which is the logical or of the given
	// predicates.

	public static BinaryIntPredicate or (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		return new BinaryIntPredicateOr (pred1, pred2);
	}


	// Create a binary predicate which is the logical exclusive or of the given
	// predicates.

	public static BinaryIntPredicate xor (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		return new BinaryIntPredicateXor (pred1, pred2);
	}


	// Create a binary predicate which is the logical and of the given
	// unary predicates.
	// That is, value(arg1, arg2) equals the logical and of
	// pred1.value(arg1) and pred2.value(arg2).

	public static BinaryIntPredicate and (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new BinaryIntPredicateAndU (pred1, pred2);
	}


	// Create a binary predicate which is the logical or of the given
	// unary predicates.
	// That is, value(arg1, arg2) equals the logical or of
	// pred1.value(arg1) and pred2.value(arg2).

	public static BinaryIntPredicate or (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new BinaryIntPredicateOrU (pred1, pred2);
	}


	// Create a binary predicate which is the logical exclusive or of the given
	// unary predicates.
	// That is, value(arg1, arg2) equals the logical exclusive or of
	// pred1.value(arg1) and pred2.value(arg2).

	public static BinaryIntPredicate xor (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		return new BinaryIntPredicateXorU (pred1, pred2);
	}


}


// The always predicate

final class BinaryIntPredicateAlways extends BinaryIntPredicate
{

	BinaryIntPredicateAlways ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return true;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateAlways)))
		{
			return false;
		}

		BinaryIntPredicateAlways other = (BinaryIntPredicateAlways) obj;

		return true;
	}


}


// The never predicate

final class BinaryIntPredicateNever extends BinaryIntPredicate
{

	BinaryIntPredicateNever ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return false;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateNever)))
		{
			return false;
		}

		BinaryIntPredicateNever other = (BinaryIntPredicateNever) obj;

		return true;
	}


}


// The equal predicate

final class BinaryIntPredicateEqual extends BinaryIntPredicate
{

	BinaryIntPredicateEqual ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 == arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateEqual)))
		{
			return false;
		}

		BinaryIntPredicateEqual other = (BinaryIntPredicateEqual) obj;

		return true;
	}


}


// The not equal predicate

final class BinaryIntPredicateNotEqual extends BinaryIntPredicate
{

	BinaryIntPredicateNotEqual ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 != arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateNotEqual)))
		{
			return false;
		}

		BinaryIntPredicateNotEqual other = (BinaryIntPredicateNotEqual) obj;

		return true;
	}


}


// The less predicate

final class BinaryIntPredicateLess extends BinaryIntPredicate
{

	BinaryIntPredicateLess ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 < arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateLess)))
		{
			return false;
		}

		BinaryIntPredicateLess other = (BinaryIntPredicateLess) obj;

		return true;
	}


}


// The less or equal predicate

final class BinaryIntPredicateLessEqual extends BinaryIntPredicate
{

	BinaryIntPredicateLessEqual ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 <= arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateLessEqual)))
		{
			return false;
		}

		BinaryIntPredicateLessEqual other = (BinaryIntPredicateLessEqual) obj;

		return true;
	}


}


// The greater predicate

final class BinaryIntPredicateGreater extends BinaryIntPredicate
{

	BinaryIntPredicateGreater ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 > arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateGreater)))
		{
			return false;
		}

		BinaryIntPredicateGreater other = (BinaryIntPredicateGreater) obj;

		return true;
	}


}


// The greater or equal predicate

final class BinaryIntPredicateGreaterEqual extends BinaryIntPredicate
{

	BinaryIntPredicateGreaterEqual ()
	{
		super ();
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return arg1 >= arg2;
	}


	// Any two objects of this class are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateGreaterEqual)))
		{
			return false;
		}

		BinaryIntPredicateGreaterEqual other = (BinaryIntPredicateGreaterEqual) obj;

		return true;
	}


}


// The logical negation predicate

final class BinaryIntPredicateNot extends BinaryIntPredicate
{

	private BinaryIntPredicate pred1;

	BinaryIntPredicateNot (BinaryIntPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return !pred1.value (arg1, arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateNot)))
		{
			return false;
		}

		BinaryIntPredicateNot other = (BinaryIntPredicateNot) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The transpose predicate

final class BinaryIntPredicateTranspose extends BinaryIntPredicate
{

	private BinaryIntPredicate pred1;

	BinaryIntPredicateTranspose (BinaryIntPredicate pred1)
	{
		super ();
		this.pred1 = pred1;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg2, arg1);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateTranspose)))
		{
			return false;
		}

		BinaryIntPredicateTranspose other = (BinaryIntPredicateTranspose) obj;

		return this.pred1.equals (other.pred1);
	}


}


// The logical and predicate

final class BinaryIntPredicateAnd extends BinaryIntPredicate
{

	private BinaryIntPredicate pred1;

	private BinaryIntPredicate pred2;

	BinaryIntPredicateAnd (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1, arg2) && pred2.value (arg1, arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateAnd)))
		{
			return false;
		}

		BinaryIntPredicateAnd other = (BinaryIntPredicateAnd) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or predicate

final class BinaryIntPredicateOr extends BinaryIntPredicate
{

	private BinaryIntPredicate pred1;

	private BinaryIntPredicate pred2;

	BinaryIntPredicateOr (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1, arg2) || pred2.value (arg1, arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateOr)))
		{
			return false;
		}

		BinaryIntPredicateOr other = (BinaryIntPredicateOr) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or predicate

final class BinaryIntPredicateXor extends BinaryIntPredicate
{

	private BinaryIntPredicate pred1;

	private BinaryIntPredicate pred2;

	BinaryIntPredicateXor (BinaryIntPredicate pred1, BinaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1, arg2) ^ pred2.value (arg1, arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateXor)))
		{
			return false;
		}

		BinaryIntPredicateXor other = (BinaryIntPredicateXor) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical and-unary predicate

final class BinaryIntPredicateAndU extends BinaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	BinaryIntPredicateAndU (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1) && pred2.value (arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateAndU)))
		{
			return false;
		}

		BinaryIntPredicateAndU other = (BinaryIntPredicateAndU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical or-unary predicate

final class BinaryIntPredicateOrU extends BinaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	BinaryIntPredicateOrU (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1) || pred2.value (arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateOrU)))
		{
			return false;
		}

		BinaryIntPredicateOrU other = (BinaryIntPredicateOrU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}


// The logical exclusive or-unary predicate

final class BinaryIntPredicateXorU extends BinaryIntPredicate
{

	private UnaryIntPredicate pred1;

	private UnaryIntPredicate pred2;

	BinaryIntPredicateXorU (UnaryIntPredicate pred1, UnaryIntPredicate pred2)
	{
		super ();
		this.pred1 = pred1;
		this.pred2 = pred2;
		return;
	}

	public boolean value (int arg1, int arg2)
	{
		return pred1.value (arg1) ^ pred2.value (arg2);
	}


	// Two objects are equal if their contained predicates are equal

	public boolean equals (Object obj)
	{
		if ((obj == null) || (!(obj instanceof BinaryIntPredicateXorU)))
		{
			return false;
		}

		BinaryIntPredicateXorU other = (BinaryIntPredicateXorU) obj;

		return this.pred1.equals (other.pred1) && this.pred2.equals (other.pred2);
	}


}

