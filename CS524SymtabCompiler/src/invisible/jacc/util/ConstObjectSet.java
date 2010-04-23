// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.util.Enumeration;


/*->

  ConstObjectSet is an interface to the functions of ObjectSet that do not
  modify the contents of the set.

->*/


public interface ConstObjectSet extends Cloneable
{

    public Object clone ();

    public Enumeration elements ();

	public boolean equals (Object obj);

	public int elementCount ();

	public boolean isDisjoint (ConstObjectSet constOther);

	public boolean isElement (Object element);

	public boolean isEmpty ();

	public String toString ();

}


