// Copyright 1998 Invisible Software, Inc.

package invisible.jacc.ex5;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.ErrorOutputStream;


/*->

Ex5Main is the main entry point for Jacc example #5.

This example is a Java class summarizer.  It reads one or more Java source
files, and writes a summary of each file to the standard output.  The summary
consists of the class or interface headers, method signatures, constructor
signatures, and field types.  These summaries are similar to the Java class
summaries you can find in the back of the Java Language Specification.

The command-line format is:

	[option]  java-file  [java-file]...

The option can be one of the following:

	-1	Include only public classes and members.
	-2	Include only public and protected classes and members.
	-3	Include all non-private classes and members.
	-4	Include all classes and members.

If the option is omitted, it defaults to -2 (which displays public and
protected classes and members).

The java-file is the name of a Java source file.  You can include more than
one Java source file name on the command line, in which case a summary is
produced for each file.

->*/


public class Ex5Main 
{

	public static void main (String[] args) throws Exception
	{

		// Create an ErrorOutput object to use as the destination of error
		// messages

		ErrorOutput errOut = new ErrorOutputStream (System.out, null);

		// Create the compiler object

		Ex5Compiler compiler = new Ex5Compiler (System.out, errOut);

		// For each filename listed on the command line ...

		for (int i = 0; i < args.length; ++i)
		{
			
			// Check for access level switch
			
			if (args[i].equalsIgnoreCase ("-1"))
			{
				compiler.access = 1;
				continue;
			}
			
			if (args[i].equalsIgnoreCase ("-2"))
			{
				compiler.access = 2;
				continue;
			}
			
			if (args[i].equalsIgnoreCase ("-3"))
			{
				compiler.access = 3;
				continue;
			}
			
			if (args[i].equalsIgnoreCase ("-4"))
			{
				compiler.access = 4;
				continue;
			}

			// Print the filename

			System.out.println ();
			System.out.println ("Summary for " + args[i] + " ...");

			// Compile the file

			compiler.compile (args[i]);
		}

		return;
	}


}
