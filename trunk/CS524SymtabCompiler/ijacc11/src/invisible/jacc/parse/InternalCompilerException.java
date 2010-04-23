// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  InternalCompilerException is a subclass of SyntaxException.  It can be
  thrown when a compiler detects an error that is best described as being
  internal to the compiler.

  For example, InternalCompilerException can be thrown when a nonterminal
  factory receives an invalid production parameter.

->*/


public class InternalCompilerException extends SyntaxException
{


	// Construct an exception without a message.

    public InternalCompilerException ()
	{
		super();
    }


	// Construct an exception with a message.

    public InternalCompilerException (String s)
	{
		super(s);
    }


}

