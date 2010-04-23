// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  InterruptedCompilerException is a subclass of SyntaxException.  It can be
  thrown when a compiler detects that some external source has requested that
  the compiler be interrupted.

  For example, InterruptedCompilerException can be thrown when the user
  want to abort the compiler run.

->*/


public class InterruptedCompilerException extends SyntaxException
{


	// Construct an exception without a message.

    public InterruptedCompilerException ()
	{
		super();
    }


	// Construct an exception with a message.

    public InterruptedCompilerException (String s)
	{
		super(s);
    }


}

