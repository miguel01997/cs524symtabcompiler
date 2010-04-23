// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  ConstSmallIntSet is an interface to the functions of SmallIntSet that do not
  modify the contents of the set.

->*/


public interface ConstSmallIntSet extends Cloneable
{

    public Object clone ();

    public IntEnumeration elements ();

	public boolean equals (Object obj);

	public int elementCount ();

	public boolean isDisjoint (ConstSmallIntSet constOther);

	public boolean isElement (int element);

	public boolean isEmpty ();

	public String toString ();

}

