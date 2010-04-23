// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  FunctionNotImplementedException may be thrown by an object that does not
  implement a particular function.

->*/


public class FunctionNotImplementedException extends RuntimeException
{


	// Construct an exception without a message.

    public FunctionNotImplementedException ()
	{
		super();
    }


	// Construct an exception with a message.

    public FunctionNotImplementedException (String s)
	{
		super(s);
    }


}
