// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  SyntaxException is the root exception class for all exceptions that result
  from scanning and parsing.  SyntaxException (or one of its subclasses) is
  thrown when an error is encountered that makes it impossible to continue
  scanning or parsing the input.

->*/


public class SyntaxException extends Exception
{


	// Construct an exception without a message.

    public SyntaxException ()
	{
		super();
    }


	// Construct an exception with a message.

    public SyntaxException (String s)
	{
		super(s);
    }


}
