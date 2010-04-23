// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.Enumeration;


/*->

  ConstPredicateSet is an interface to the functions of PredicateSet that do
  not modify the contents of the set.

->*/


public interface ConstPredicateSet extends Cloneable
{

    public Object clone ();

    public Enumeration elements ();

	public boolean equals (Object obj);

	public Object findElement (Object element);

	public int elementCount ();

	public boolean isDisjoint (ConstPredicateSet constOther);

	public boolean isElement (Object element);

	public boolean isEmpty ();

	public String toString ();

}


