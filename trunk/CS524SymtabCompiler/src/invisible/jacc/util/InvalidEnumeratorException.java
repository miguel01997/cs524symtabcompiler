// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  InvalidEnumeratorException may be thrown by an enumerator when it detects
  that the enumerator has become invalid, usually because the underlying
  collection has been altered in a way that makes it impossible to continue
  the enumeration.  This is an unchecked exception.

->*/


public class InvalidEnumeratorException extends RuntimeException
{


	// Construct an exception without a message.

    public InvalidEnumeratorException ()
	{
		super();
    }


	// Construct an exception with a message.

    public InvalidEnumeratorException (String s)
	{
		super(s);
    }


}
