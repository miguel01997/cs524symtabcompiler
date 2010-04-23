// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.ex1;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.ErrorOutputStream;


/*->

  Ex1Main is the main entry point for Jacc example #1.

  The command-line parameters are a series of file names.  Each file name
  in turn is compiled with Ex1Compiler.

->*/


public class Ex1Main 
{

	public static void main (String[] args) throws Exception
	{

		// Create an ErrorOutput object to use as the destination of error
		// messages

		ErrorOutput errOut = new ErrorOutputStream (System.out, null);

		// Create the compiler object

		Ex1Compiler compiler = new Ex1Compiler (System.out, errOut);

		// For each filename listed on the command line ...

		for (int i = 0; i < args.length; ++i)
		{

			// Print the filename

			System.out.println ();
			System.out.println ("Compiling " + args[i] + " ...");

			// Compile the file

			compiler.compile (args[i]);
		}

		return;
	}


}


