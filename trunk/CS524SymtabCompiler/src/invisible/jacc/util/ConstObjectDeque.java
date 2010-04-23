// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.Enumeration;


/*->

  ConstObjectDeque is an interface to the functions of ObjectDeque that do
  not modify the contents of the deque.

->*/


public interface ConstObjectDeque extends Cloneable
{

    public Object clone ();

	public Object elementAt (int index);

	public int elementCount ();

    public Enumeration elements ();

    public Enumeration elementsReversed ();

	public int firstIndex ();

	public int firstIndexOf (UnaryPredicate pred);

	public int firstIndexOf (UnaryPredicate pred, int index);

	public boolean isEmpty ();

	public boolean isIndexValid (int index);

	public int lastIndex ();

	public int lastIndexOf (UnaryPredicate pred);

	public int lastIndexOf (UnaryPredicate pred, int index);

	public Object peekFirst ();

	public Object peekFirst (int index);

	public Object peekLast ();

	public Object peekLast (int index);

	public String toString ();

}


