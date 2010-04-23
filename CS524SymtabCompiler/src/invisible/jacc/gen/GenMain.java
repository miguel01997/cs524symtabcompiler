// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.parse.ErrorOutputStream;


/*->

  GenMain holds the main command-line processor for the parser generator.
  It reads parameters from the command line and passes them directly to
  GenFrontEnd.  It is also acceptable for clients to call GenFrontEnd directly.

  The command line is as follows:

	[options] jacc-file

  The options may include the following:

	-v		Selects verbose mode.

	-o		Creates an output file.  The name of the output file is the base
			name of the jacc-file with extension ".out".  If this option is not
			included, the standard output is used.

	-g		Creates a generated file.  The name of generated file is the base
			name of the jacc-file with extension ".gen".  The generated file
			contains first the scanner table, and then the parser table.

	-j		Creates Java source code for the scanner and parser tables.  The
			Java package and class names are given by the %java option in the
			grammar specification.  The class names for the scanner and
			parser tables are created by concatenating "ScannerTable" and
			"ParserTable", respectively, to the grammar's class name.

	-s		Generates only scanner tables.

	-p		Generates only parser tables.

  The jacc-file is the name of the file containing the grammar specification.
  It may optionally be written without an extension, in which case the
  extension ".jacc" is automatically added.  The name is not case-sensitive;
  the parser generator reads the disk directory to obtain the correct case.

->*/


public class GenMain implements GenObserver
{

	// Generate scanner and parser tables.
	//
	// This function only needs to be run once, unless the grammar
	// specification is changed.

	public static void main (String[] args)
	{

		// Assume not verbose mode

		boolean verbose = false;

		// Assume not making output file

		boolean makeOut = false;

		// Assume not making generated file

		boolean makeGen = false;

		// Assume not making Java files

		boolean makeJava = false;

		// Assume making scanner table

		boolean makeScan = true;

		// Assume making parser table

		boolean makeParse = true;

		// Jacc file name

		String jaccFile = null;

		// Scan for command-line parameters

		for (int i = 0; i < args.length; ++i)
		{

			// If verbose mode ...

			if (args[i].equalsIgnoreCase ("-v"))
			{
				verbose = true;
			}

			// If making output file ...

			else if (args[i].equalsIgnoreCase ("-o"))
			{
				makeOut = true;
			}

			// If making generated file ...

			else if (args[i].equalsIgnoreCase ("-g"))
			{
				makeGen = true;
			}

			// If making Java files ...

			else if (args[i].equalsIgnoreCase ("-j"))
			{
				makeJava = true;
			}

			// If making scanner table only ...

			else if (args[i].equalsIgnoreCase ("-s"))
			{
				makeParse = false;
			}

			// If making parser table only ...

			else if (args[i].equalsIgnoreCase ("-p"))
			{
				makeScan = false;
			}

			// If unrecognized option ...

			else if (args[i].charAt(0) == '-')
			{
				System.out.println ("Invalid command-line option '" + args[i] + "'.");
				return;
			}

			// Otherwise, its the jacc filename ...

			else
			{
				if (jaccFile != null)
				{
					System.out.println ("Too many parameters.");
					return;
				}

				jaccFile = args[i];
			}
		}

		// Check that we got a jacc file

		if (jaccFile == null)
		{
			System.out.println ("No Jacc file specified.");
			return;
		}
		
		// Check that we didn't get both -p and -s
		
		if (makeScan == false && makeParse == false)
		{
			System.out.println ("Cannot specify both -p and -s.");
			return;
		}
			
		// Set up the parser generator front end
			
		GenFrontEnd genFrontEnd = new GenFrontEnd ();
		
		// Pass it all to the front end
			
		genFrontEnd.generate (makeOut ? (new GenMain()) : null,
							  false,
							  makeOut ? null : (new ErrorOutputStream (System.out, null)),
							  verbose,
							  jaccFile,
							  makeScan,
							  makeParse,
							  makeOut,
							  makeGen,
							  makeJava );

		// All done

		return;
	}
	
	
	
	
	// ----- Progress reporting -----
	
	
	// The constructor is protected so only main() can instantiate GenMain.
	//
	// If we are making an output file, we instantiate GenMain and use it as a
	// GenObserver.  This lets us produce progress messages to the standard
	// output.
	//
	// If we are not making an output file, we supply a null GenObserver, since
	// the standard output is used for output messages.
	
	protected GenMain ()
	{
		super();
		return;
	}
	
	
	// This function is called when the parser generator begins execution.
	
	public void generatorBegin (String filename, String shortFilename)
	{
		System.out.print ("Processing " + filename + " ...");
		System.out.flush();
		return;
	}
	
	
	
	
	// This function is called when the parser generator ends execution.
	
	public void generatorEnd (String summary, int errorFlags)
	{
		System.out.println ();
		System.out.println (summary);
		System.out.flush();
		return;
	}
	
	
	
	
	// This function is called when the parser generator begins a new stage.
	
	public void generatorStage (String stage)
	{
		System.out.println ();
		System.out.print (stage + " ...");
		System.out.flush();
		return;
	}
	
	
	
	
	// This function is called when the parser generator has performed some
	// work within a stage.
	
	public void generatorWork (int amount)
	{
		if ((amount % 20) == 0)
		{
			System.out.print (".");
			System.out.flush();
		}
		return;
	}


}

