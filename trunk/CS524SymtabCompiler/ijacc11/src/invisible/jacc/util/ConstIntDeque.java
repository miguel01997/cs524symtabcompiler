// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  ConstIntDeque is an interface to the functions of IntDeque that do
  not modify the contents of the deque.

->*/


public interface ConstIntDeque extends Cloneable
{

    public Object clone ();

	public int elementAt (int index);

	public int elementCount ();

    public IntEnumeration elements ();

    public IntEnumeration elementsReversed ();

	public int firstIndex ();

	public int firstIndexOf (UnaryIntPredicate pred);

	public int firstIndexOf (UnaryIntPredicate pred, int index);

	public boolean isEmpty ();

	public boolean isIndexValid (int index);

	public int lastIndex ();

	public int lastIndexOf (UnaryIntPredicate pred);

	public int lastIndexOf (UnaryIntPredicate pred, int index);

	public int peekFirst ();

	public int peekFirst (int index);

	public int peekLast ();

	public int peekLast (int index);

	public String toString ();

}


