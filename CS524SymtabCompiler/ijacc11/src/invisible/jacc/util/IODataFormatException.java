// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.io.IOException;


/*->

  IODataFormatException may be thrown when input from a stream, or output to a
  stream, cannot be completed because of improperly formatted data.

->*/


public class IODataFormatException extends IOException
{


	// Construct an exception without a message.

    public IODataFormatException ()
	{
		super();
    }


	// Construct an exception with a message.

    public IODataFormatException (String s)
	{
		super(s);
    }


}

