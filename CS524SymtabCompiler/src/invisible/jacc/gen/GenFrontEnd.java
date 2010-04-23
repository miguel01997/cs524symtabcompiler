// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.gen;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.ErrorOutputMulticaster;
import invisible.jacc.parse.ErrorOutputStream;
import invisible.jacc.parse.InterruptedCompilerException;
import invisible.jacc.parse.ParserTable;
import invisible.jacc.parse.ProductInfo;
import invisible.jacc.parse.ScannerTable;


/*->

GenFrontEnd is the front end to the parser generator.  It provides a convenient
programming interface for reading a grammar specification, generating scanner
and parser tables, and writing the results into disk files.  It also lets you
run the parser generator in a separate thread, receive notifications of the
parser generator's progress, and interrupt the parser generator.

To start the parser generator, you call one of the following two entry points.
The first entry point reads the grammar specification from an input stream and
generates scanner and parser tables;  this entry point is recommended if you
are going to use the tables immediately instead of saving them into files.
The second entry point reads the grammar specification from a file, generates
scanner and parser tables, and then saves the tables into files.

	public void generate (GenObserver genObserver, boolean async,
		ErrorOutput errOut, boolean verbose, InputStream inStream,
		String filename, boolean makeScan, boolean makeParse)
	
	public void generate (GenObserver genObserver, boolean async,
		ErrorOutput errOut, boolean verbose, String filename,
		boolean makeScan, boolean makeParse,
		boolean makeOut, boolean makeGen, boolean makeJava)

While the parser generator is running, you can use the following functions
to interrupt it, wait for it to finish, or check if it is finished.
	
	public synchronized void requestInterrupt ()
	
	public synchronized void waitUntilDone ()
	
	public synchronized boolean isDone ()

After the parser generator is finished, you can call the following functions
to get the results.
	
	public int errorFlags ()
	
	public String summary ()
	
	public int errorCount ()
	
	public int warningCount ()
	
	public ScannerTable scannerTable ()
	
	public ParserTable parserTable ()
	
	public String javaName ()
	
	public String jaccFilename ()
	
	public String shortFilename ()
	
	public String outFilename ()
	
	public String scannerJavaFilename ()
	
	public String parserJavaFilename ()
	
	public String genFilename ()

->*/


public class GenFrontEnd implements Runnable, GeneratorStatus
{
	
	// ----- Error return values -----
	
	// These variables may be interrogated after generation is complete
	
	// Error flags. See interface GenObserver for bit definitions.
	
	private int _errorFlags = 0;
	
	public int errorFlags ()
	{
		return _errorFlags;
	}
	
	// A one-line summary of the result.  See GenObserver for additional info.
	
	private String _summary = null;
	
	public String summary ()
	{
		return _summary;
	}
	
	// The number of error messages.
	
	private int _errorCount = 0;
	
	public int errorCount ()
	{
		return _errorCount;
	}
	
	// The number of warning messages.
	
	private int _warningCount = 0;
	
	public int warningCount ()
	{
		return _warningCount;
	}
	
	
	
	
	// ----- Generated tables -----
	
	// These variables may be interrogated after generation is complete.
	
	// The generated scanner table.
	
	private ScannerTable _scannerTable = null;
	
	public ScannerTable scannerTable ()
	{
		return _scannerTable;
	}
	
	// The generated parser table.
	
	private ParserTable _parserTable = null;
	
	public ParserTable parserTable ()
	{
		return _parserTable;
	}
	
	// The Java name from the grammar specification, or null if none.
	
	private String _javaName = null;
	
	public String javaName ()
	{
		return _javaName;
	}
	
	
	
	
	// ----- Filenames we used -----
	
	// These variables may be interrogated after generation is complete.
	
	// The Jacc filename, or null if unknown.
	
	private String _jaccFilename = null;
	
	public String jaccFilename ()
	{
		return _jaccFilename;
	}
	
	// The shortened form of the Jacc filename, or null if unknown.
	
	private String _shortFilename = null;
	
	public String shortFilename ()
	{
		return _shortFilename;
	}
	
	// The output filename, or null if not creating output file.
	
	private String _outFilename = null;
	
	public String outFilename ()
	{
		return _outFilename;
	}
	
	// Name of scanner table Java file, or null if not creating Java source.
	
	private String _scannerJavaFilename = null;
	
	public String scannerJavaFilename ()
	{
		return _scannerJavaFilename;
	}
	
	// Name of parser table Java file, or null if not creating Java source.
	
	private String _parserJavaFilename = null;
	
	public String parserJavaFilename ()
	{
		return _parserJavaFilename;
	}
	
	// Name of generated file, or null if not creating generated file.
	
	private String _genFilename = null;
	
	public String genFilename ()
	{
		return _genFilename;
	}
	
	
	
	
	// ----- Internal variables -----
	
	// The input stream for reading the grammar specification
	
	private InputStream _inStream;
	
	// Verbose output flag
	
	private boolean _verbose;
	
	// Filename to use in error messages, or null if none
	
	private String _filename;
	
	// Flag to make scanner table
	
	private boolean _makeScan;
	
	// Flag to make parser table
	
	private boolean _makeParse;
	
	// Destination for output messages
	
	private ErrorOutputMulticaster _errOut;
	
	// Flag to make output file
	
	private boolean _makeOut;
	
	// Flag to make generated file
	
	private boolean _makeGen;
	
	// Flag to make Java source
	
	private boolean _makeJava;
	
	// This is our status observer, it can be null
	
	private GenObserver _genObserver = null;
	
	// Flag that is true if the parser generator is done.
	// Access to the variable must be protected by synchronization.
	
	private boolean _isDone = true;
	
	// Flag to indicate an interrupt request is pending.
	// Access to the variable must be protected by synchronization.
	
	private boolean _interruptRequested = false;
	
	// The amount of work done in the current stage.
	
	private int _amount = 0;
	
	// The output stream for writing the output file
	
	private PrintStream _outStream = null;
	
	// The parsed filename, as generated by parseFilename()
	
	private String[] _fval = null;
	
	
	
	
	// ----- Functions for interrupting and progress monitoring -----
	
	
	// Request that the parser generator be interrupted.
	
	public synchronized void requestInterrupt ()
	{
		_interruptRequested = true;
		return;
	}
	
	
	// Wait until the parser generator is done.
	
	public synchronized void waitUntilDone ()
	{
		while (!_isDone)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
			}
		}
		
		return;
	}
	
	
	// Check if the parser generator is done.
	
	public synchronized boolean isDone ()
	{
		return _isDone;
	}
	
	
	
	
	// ----- Implementation of the GeneratorStatus interface -----
	
	
	// Signal that the parser generator is beginning a new stage.
	
	public synchronized void statusStage (String stage) throws InterruptedCompilerException
	{
		_amount = 0;
		
		if (_genObserver != null)
		{
			_genObserver.generatorStage (stage);
		}
		
		if (_interruptRequested)
		{
			throw new InterruptedCompilerException();
		}

		return;
	}
	
	
	// Signal that some work has been performed in the current stage.

	public synchronized void statusWork () throws InterruptedCompilerException
	{
		_amount++;
		
		if (_genObserver != null)
		{
			_genObserver.generatorWork (_amount);
		}
		
		if (_interruptRequested)
		{
			throw new InterruptedCompilerException();
		}

		return;
	}
	
	
	// Set bits in the error flags.
	
	public void statusError (int errorFlags)
	{
		_errorFlags |= errorFlags;
		return;
	}
	
	
	
	
	// ----- Internal subroutines for use by entry points -----
	
	
	// Reset all our variables to their default values.
	
	private synchronized void reset ()
	{
		
		// Error return values
		
		_errorFlags = 0;
		_summary = null;
		_errorCount = 0;
		_warningCount = 0;
		
		// Generated tables
		
		_scannerTable = null;
		_parserTable = null;
		_javaName = null;
		
		// File names
		
		_jaccFilename = null;
		_shortFilename = null;
		_outFilename = null;
		_scannerJavaFilename = null;
		_parserJavaFilename = null;
		_genFilename = null;

		// Internal variables
		
		_inStream = null;
		_verbose = false;
		_filename = null;
		_makeScan = false;
		_makeParse = false;
		_errOut = null;
		_makeOut = false;
		_makeGen = false;
		_makeJava = false;
		_genObserver = null;
		_isDone = false;
		_interruptRequested = false;
		_amount = 0;
		_outStream = null;
		_fval = null;
		
		return;
	}
	
	
	

	// ----- Main entry points -----
	
	
	// Run the parser generator.
	//
	// Parameters:
	//	genObserver = an object to receive notifications of the parser
	//		generator's progress.  Can be null.
	//	async = true to run the parser generator asynchronously in a
	//		separate thread;  false to run it synchronously in the
	//		calling thread.
	//	errOut = Destination for error messages and other parser generator
	//		output messages.  Can be null.
	//	verbose = true to generate verbose output.
	//	inStream = Input source for grammar specification.  Cannot be null.
	//	filename = A filename to use in error messages.  This is not used to
	//		open any files.  Can be null.
	//	makeScan = true to generate the scanner table.
	//	makeParse = true to generate the parser table.
	//
	// If the async parameter is true, this function creates a separate thread
	// and returns immediately;  you must rely on calls to the genObserver
	// object, or the isDone() and waitUntilDone() methods, to know when the
	// parser generator has finished execution.
	//
	// If the async parameter is false, this function executes the parser
	// generator in the current thread, and does not return until the parser
	// generator has finished.
	
	public void generate (GenObserver genObserver, boolean async,
		ErrorOutput errOut, boolean verbose, InputStream inStream,
		String filename, boolean makeScan, boolean makeParse)
	{
		
		// If bad arguments, throw exception
		
		if (inStream == null)
		{
			throw new IllegalArgumentException ("GenFrontEnd.generate");
		}
		
		synchronized (this)
		{
		
			// If we're already active, throw exception
		
			if (!_isDone)
			{
				throw new IllegalThreadStateException ("GenFrontEnd.generate");
			}
		
			// Reset all our variables
		
			reset();
		
			// Save the arguments
		
			_errOut = new ErrorOutputMulticaster();
			_errOut.add (errOut);
		
			_verbose = verbose;
			_inStream = inStream;
			_filename = filename;
			_makeScan = makeScan;
			_makeParse = makeParse;
		
			_genObserver = genObserver;
		}
		
		// For asynchronous execution, start the generator thread
		
		if (async)
		{
			(new Thread (this, ProductInfo.product + " - Generator")).start();
		}
		
		// For synchronous execution, just call the run method directly
		
		else
		{
			this.run();
		}
		
		// Successful start
		
		return;
	}
	
	
	
	
	// Run the parser generator.
	//
	// Parameters:
	//	genObserver = an object to receive notifications of the parser
	//		generator's progress.  Can be null.
	//	async = true to run the parser generator asynchronously in a
	//		separate thread;  false to run it synchronously in the
	//		calling thread.
	//	errOut = Destination for error messages and other parser generator
	//		output messages.  Can be null.
	//	verbose = true to generate verbose output.
	//	filename = The name of the grammar specification file.  Cannot be
	//		null.
	//	makeScan = true to generate the scanner table.
	//	makeParse = true to generate the parser table.
	//	makeOut = true to write an output file.
	//	makeGen = true to write a generated file.
	//	makeJava = true to write Java source files.
	//
	// If the async parameter is true, this function creates a separate thread
	// and returns immediately;  you must rely on calls to the genObserver
	// object, or the isDone() and waitUntilDone() methods, to know when the
	// parser generator has finished execution.
	//
	// If the async parameter is false, this function executes the parser
	// generator in the current thread, and does not return until the parser
	// generator has finished.
	//
	// If the supplied filename does not have an extension, then the extension
	// ".jacc" is appended automatically.  If the supplied filename has an
	// extension, then the supplied extension is used.  Additionally, the
	// parser generator reads the disk directory and attempts to correct the
	// case of the supplied filename;  thus, in most cases the supplied
	// filename is case-insensitive.
	//
	// To create a filename to use for the output file or the generated file,
	// the parser generator strips off the supplied extension (if any) and
	// appends the extension ".out" or ".gen" respectively.  The filenames for
	// the Java source files are derived from the %java option in the grammar
	// specification file.
	
	public void generate (GenObserver genObserver, boolean async,
		ErrorOutput errOut, boolean verbose, String filename,
		boolean makeScan, boolean makeParse,
		boolean makeOut, boolean makeGen, boolean makeJava)
	{
		
		// If bad arguments, throw exception
		
		if (filename == null)
		{
			throw new IllegalArgumentException ("GenFrontEnd.start");
		}

		synchronized (this)
		{
		
			// If we're already active, throw exception
		
			if (!_isDone)
			{
				throw new IllegalThreadStateException ("GenFrontEnd.start");
			}
		
			// Reset all our variables
		
			reset();
		
			// Save the arguments
		
			_errOut = new ErrorOutputMulticaster();
			_errOut.add (errOut);
		
			_verbose = verbose;
			_filename = filename;
			_makeScan = makeScan;
			_makeParse = makeParse;
			_makeOut = makeOut;
			_makeGen = makeGen;
			_makeJava = makeJava;
		
			_genObserver = genObserver;
		}
		
		// For asynchronous execution, start the generator thread
		
		if (async)
		{
			(new Thread (this, ProductInfo.product + " - Generator")).start();
		}
		
		// For synchronous execution, just call the run method directly
		
		else
		{
			this.run();
		}
		
		// Successful start
		
		return;
	}
	
	
	

	// ----- Internal functions to run the parser generator -----
	
	
	// Generate the scanner and parser tables.
	
	public void run ()
	{
		
		// Get the filename we're using
		
		setupFilename ();
		
		// Signal that we're beginning
		
		if (_genObserver != null)
		{
			_genObserver.generatorBegin (_jaccFilename, _shortFilename);
		}
		
		// If there was no error so far ...
		
		if (_errorFlags == 0)
		{
		
			// Reset the error counters
		
			_errOut.resetCounters();
		
			// Run the parser generator
		
			try
			{
				runGenerator();
			}
			
			// If it was interrupted, write error message and set error flag
			
			catch (InterruptedCompilerException e)
			{
				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "Interrupted." );
			
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "" );
				
				statusError (GenObserver.efInterrupted);
			}
		}
		
		// Get error counters, create summary, and close files
		
		cleanupGenerator();
		
		// Signal that we're done
		
		if (_genObserver != null)
		{
			_genObserver.generatorEnd (_summary, _errorFlags);
		}
		
		// Discard the observer object
		
		_genObserver = null;
		
		// Wake up anyone waiting for us to complete
		
		synchronized (this)
		{
			_isDone = true;
			this.notifyAll();
		}
		
		// Return to terminate the thread
		
		return;
	}
	
	
	
	
	// This function sets up _jaccFilename to contain the filename used for
	// this run of the parser generator.  This filename is reported to the
	// observer, and is used in error messages.
	//
	// This function also sets up _shortFilename.
	//
	// If we're not opening the input stream ourselves, we just use the
	// filename that the user supplied to us.
	//
	// If we're opening the input stream, we take the user-supplied filename
	// and modify it by appending the .jacc extension if necessary and
	// attempting to case-map it.  In this case, _jaccFilename is set to null
	// if we cannot generate a valid filename.
	//
	// The error flags are set if there is some error in parsing the filename.
	
	private void setupFilename ()
	{
		
		// By default, just use the user-supplied filename
		
		_jaccFilename = _filename;
		
		_shortFilename = _filename;
		
		// If we need a parsed filename ...
		
		if (_inStream == null || _makeOut || _makeGen || _makeJava)
		{
			
			// Parse the filename into its components, apply the .jacc
			// extension, and case-map it
						
			_fval = parseFilename (_filename, "jacc", true);
					
			// If the filename is empty ...
					
			if (_fval[2].length() == 0)
			{
				
				// Signal the error by using a null filename
				
				_jaccFilename = null;
				
				_shortFilename = null;
					
				// Set the error flags
				
				statusError (GenObserver.efBadFilename | GenObserver.efAborted);				   
				
				// Construct the error message
				
				_summary = "The grammar specification filename is missing or invalid";
				
				// Done
				
				return;
				
			}
					
			// Construct the filename
		
			_jaccFilename = _fval[0] + _fval[1] + _fval[2] + _fval[3] + _fval[4];
		
			_shortFilename = _fval[2] + _fval[3] + _fval[4];
		}
		
		// Done
		
		return;
	}
	
	
	
	
	// This function runs the parser generator.
	//
	// This function performs the following steps:
	// - Open the input file, if required.
	// - Open the output file, if required.
	// - Read and analyze the grammar specification.
	// - Generate the scanner table, if required.
	// - Generate the parser table, if required.
	// - Write Java source for the scanner table, if required.
	// - Write Java source for the parser table, if required.
	// - Write generated file, if required.
	//
	// If there is any error, this function sets the error flags, and in some
	// cases also sets the error summary.
	//
	// If a pending interrupt request is detected, this function throws
	// InterruptedCompilerException.
	//
	// No matter how this function terminates, _inStream and _outStream are
	// left non-null if and only if the input and output streams, respectively,
	// are still open.
	
	private void runGenerator () throws InterruptedCompilerException
	{
				
		// If we need to open the input stream ...
				
		if (_inStream == null)
		{
						
			// Open the input stream
						
			try
			{
				_inStream = new FileInputStream (_jaccFilename);
			}
		
			// Note we need to catch all exceptions so we get SecurityException
			// as well as IOException
		
			catch (Exception e)
			{
						
				// No input stream
						
				_inStream = null;
						
				// Set the error flags
					
				statusError (GenObserver.efJaccOpen | GenObserver.efAborted);
					
				// Construct the error message
					
				_summary = "Cannot open grammar file " + _jaccFilename;
					
				// Done
					
				return;
			}
		}
			
		// If we need to create an output file ...
			
		if (_makeOut)
		{
					
			// Construct the filename
		
			_outFilename = _fval[0] + _fval[1] + _fval[2] + ".out";
						
			// Open the output stream
						
			try
			{
				_outStream = new PrintStream (new FileOutputStream (_outFilename));
			}
		
			// Note we need to catch all exceptions so we get SecurityException
			// as well as IOException
		
			catch (Exception e)
			{
						
				// No outut stream
						
				_outStream = null;
						
				// Set the error flags
					
				statusError (GenObserver.efOutWrite | GenObserver.efAborted);
					
				// Construct the error message
					
				_summary = "Cannot create output file " + _outFilename;
					
				// Done
					
				return;
			}
			
			// Create the ErrorOutput object for the output stream
			
			_errOut.add (new ErrorOutputStream (_outStream, null));
		}
		
		// Create a ParserGenerator object to use in generating our scanner
		// and parser tables.

		ParserGenerator PG = new ParserGenerator ();
			
		// PG.generate() always closes the input stream
			
		InputStream inStream = _inStream;
		_inStream = null;

		// Read the grammar specification.  The first argument is the
		// destination for error messages;  the second argument is a boolean
		// that selects verbose mode;  the third argument is the input;  the
		// fourth argument is a string used in error messages;  and the fifth
		// argument is the destination for status and interrupts.  The return
		// value is true if an error occurred.

		boolean error = PG.generate (_errOut, _verbose, inStream, _jaccFilename, this);
			
		// PG.generate() always closes the input stream
			
		inStream = null;

		// If there was an error, print a final message and return.

		if (error)
		{
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "***** Error reading grammar specification. *****" );
			
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "" );
			
			return;
		}

		// If making scanner tables ...

		if (_makeScan)
		{

			// Generate our scanner table.

			_scannerTable = PG.makeScannerTable ();

			// If error, print an error message.

			if (_scannerTable == null)
			{
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "***** Error generating scanner table. *****" );
			
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "" );
			}
		}

		// If making parser tables ...

		if (_makeParse)
		{

			// Generate our parser table.

			_parserTable = PG.makeParserTable ();

			// If error, print an error message.

			if (_parserTable == null)
			{
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "***** Error generating parser table. *****" );
			
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "" );
			}
		}
			
		// Get the Java name from the grammar specification
			
		_javaName = PG.getJavaName();
		
		// If there is a Java name, split it into the Java package and class
		
		String javaClass = null;
		String javaPackage = null;
		
		if (_javaName != null)
		{
			int javaQual = _javaName.lastIndexOf ('.');
		
			javaClass = _javaName.substring (javaQual + 1);
		
			javaPackage = ((javaQual < 0) ? "" : _javaName.substring (0, javaQual));
		}
		
		// Flags for error summary reporting
		
		boolean javaSourceError = false;
		boolean genFileError = false;
		
		// If we're making Java source, get the Java package and class
		
		if (_makeJava)
		{
			
			// If Java name wasn't specified, report an error
		
			if (_javaName == null)
			{
				
				// Write an error message
				
				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "No Java name in grammar specification (%java option is missing)." );
			
				javaSourceError = true;
					
				// Set error flags
				
				statusError (GenObserver.efNoJavaName);
			}
			
			// Otherwise, issue a warning if the Java class name is different
			// from the filename
			
			else
			{
				if (!javaClass.equals(_fval[2]))
				{
				_errOut.reportError (ErrorOutput.typeWarning, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "Java class name '" + javaClass
									       + "' is different from file name '"
									       + _fval[2] + "'." );
				}
			}
		}
			
		// If we made a scanner table, and we're writing Java source ...
			
		if (_makeJava && _scannerTable != null && _javaName != null)
		{
		
			// Report stage
			
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "Writing scanner table Java source ..." );
				
			statusStage ("Writing scanner table Java source");
						
			// The destination stream
				
			PrintStream javaStream = null;
						
			try
			{
					
				// Construct the filename
		
				_scannerJavaFilename = _fval[0] + _fval[1] + javaClass + "ScannerTable.java";
					
				// Open the stream
					
				PrintStream ps = new PrintStream (new FileOutputStream (_scannerJavaFilename));
				javaStream = ps;	// Pass PrintStream to catch clause

				// Create the Java source file.  In the call to writeToJavaSource,
				// the first argument is a PrintStream to which the source is
				// written, the second argument is the package name, the third
				// argument is the class name, and the fourth argument is a boolean
				// that is true to enable run-length-encoding of the tables.

				_scannerTable.writeToJavaSource (
					ps,
					javaPackage,
					javaClass + "ScannerTable",
					true );
					
				// Close the stream
					
				ps.flush();
				javaStream = null;	// Tell catch clause that we closed it
				ps.close();
					
				// Check for error
					
				if (ps.checkError())
				{
					throw new IOException();
				}
			}
		
			// Note we need to catch all exceptions so we get SecurityException
			// as well as IOException
		
			catch (Exception e)
			{
					
				// If the output stream is open, close it
					
				if (javaStream != null)
				{
					try
					{
						javaStream.close();
					}
					catch (Exception e2)
					{
					}
					javaStream = null;
				}
					
				// Write an error message

				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "Cannot write scanner table Java source file "
										   + _scannerJavaFilename );
				
				javaSourceError = true;
					
				// Set error flags
				
				statusError (GenObserver.efScannerJavaWrite);
			}
		}
			
		// If we made a parser table, and we're writing Java source ...
			
		if (_makeJava && _parserTable != null && _javaName != null)
		{
		
			// Report stage
			
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "Writing parser table Java source ..." );
				
			statusStage ("Writing parser table Java source");
						
			// The destination stream
				
			PrintStream javaStream = null;
						
			try
			{
					
				// Construct the filename
		
				_parserJavaFilename = _fval[0] + _fval[1] + javaClass + "ParserTable.java";
					
				// Open the stream
					
				PrintStream ps = new PrintStream (new FileOutputStream (_parserJavaFilename));
				javaStream = ps;	// Pass PrintStream to catch clause

				// Create the Java source file.  In the call to writeToJavaSource,
				// the first argument is a PrintStream to which the source is
				// written, the second argument is the package name, the third
				// argument is the class name, and the fourth argument is a boolean
				// that is true to enable run-length-encoding of the tables.

				_parserTable.writeToJavaSource (
					ps,
					javaPackage,
					javaClass + "ParserTable",
					true );
					
				// Close the stream
					
				ps.flush();
				javaStream = null;	// Tell catch clause that we closed it
				ps.close();
					
				// Check for error
					
				if (ps.checkError())
				{
					throw new IOException();
				}
			}
		
			// Note we need to catch all exceptions so we get SecurityException
			// as well as IOException
		
			catch (Exception e)
			{
					
				// If the output stream is open, close it
					
				if (javaStream != null)
				{
					try
					{
						javaStream.close();
					}
					catch (Exception e2)
					{
					}
					javaStream = null;
				}
					
				// Write an error message

				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "Cannot write parser table Java source file "
										   + _parserJavaFilename );
				
				javaSourceError = true;
					
				// Set error flags
				
				statusError (GenObserver.efParserJavaWrite);
			}
		}
		
		// If there was an error writing Java source, print another message
		
		if (javaSourceError)
		{
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "***** Error producing Java source. *****" );

			if (_makeGen
				&& (_scannerTable != null || _parserTable != null)
				&& (_scannerTable != null || _makeScan == false)
				&& (_parserTable != null || _makeParse == false) )
			{
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "" );
			}
		}
			
		// If we made at least one table, and we're writing a generated file ...
			
		if (_makeGen
			&& (_scannerTable != null || _parserTable != null)
			&& (_scannerTable != null || _makeScan == false)
			&& (_parserTable != null || _makeParse == false) )
		{
		
			// Report stage
			
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "Writing generated file ..." );
				
			statusStage ("Writing generated file");
						
			// The destination stream
				
			DataOutputStream genStream = null;
						
			try
			{
					
				// Construct the filename
		
				_genFilename = _fval[0] + _fval[1] + _fval[2] + ".gen";
					
				// Open the stream
					
				DataOutputStream dos = new DataOutputStream (new FileOutputStream (_genFilename));
				genStream = dos;	// Pass DataOutputStream to catch clause
					
				// Write the scanner table to the generated stream
					
				if (_scannerTable != null)
				{
					_scannerTable.writeToStream (dos);
				}
					
				// Write the parser table to the generated stream
					
				if (_parserTable != null)
				{
					_parserTable.writeToStream (dos);
				}
					
				// Close the stream
					
				dos.flush();
				genStream = null;	// Tell catch clause that we closed it
				dos.close();
			}
		
			// Note we need to catch all exceptions so we get SecurityException
			// as well as IOException
		
			catch (Exception e)
			{
					
				// If the output stream is open, close it
					
				if (genStream != null)
				{
					try
					{
						genStream.close();
					}
					catch (Exception e2)
					{
					}
					genStream = null;
				}
					
				// Write an error message

				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "Cannot write generated file "
										   + _genFilename );
				
				genFileError = true;
					
				// Set error flags
				
				statusError (GenObserver.efGenWrite);
			}
		}
		
		// If there was an error writing generated file, print another message
		
		if (genFileError)
		{
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "***** Error producing generated file. *****" );
		}
		
		// Print an extra blank line if needed
		
		if (_makeJava || _makeGen)
		{
			_errOut.reportError (ErrorOutput.typeInformational, null, null,
								 ErrorOutput.noPosition, ErrorOutput.noPosition,
								 null, "" );
		}

		// All done

		return;
		
	}
	
	
	
	
	// Cleanup after running the parser generator.
	//
	// This function sets up these variables:
	//	_errorCount
	//	_warningCount
	//	_interrupted
	
	public void cleanupGenerator ()
	{
		
		// Flush the ErrorOutput
		
		_errOut.flush();
		
		// If there is an output stream, check it for errors
		
		if (_outStream != null)
		{
			_outStream.flush();
			
			if (_outStream.checkError())
			{
				
				// Write the error message
				
				_errOut.reportError (ErrorOutput.typeError, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "I/O error writing output file "
										   + _outFilename );
			
				_errOut.reportError (ErrorOutput.typeInformational, null, null,
									 ErrorOutput.noPosition, ErrorOutput.noPosition,
									 null, "" );
						
				// Set the error flags
					
				statusError (GenObserver.efOutWrite);
			}
		}
		
		// Read the error counters
		
		_errorCount = _errOut.errorCount();
		_warningCount = _errOut.warningCount();
		
		// If we don't have a result summary yet ...
		
		if (_summary == null)
		{
		
			// If we were interrupted, tell the user
		
			if ((_errorFlags & GenObserver.efInterrupted) != 0)
			{
				_summary = "***** Interrupted. *****";
			}

			// Otherwise, if there was an error or warning, tell the user.

			else if (_errorCount != 0 || _warningCount != 0)
			{
				_summary = "There were "
						   + _errorCount
						   + ((_errorCount == 1) ? " error and " : " errors and ")
						   + _warningCount
						   + ((_warningCount == 1) ? " warning." : " warnings.");
			}

			// Otherwise, if an error flag is set (should never happen)

			else if (_errorFlags != 0)
			{
				_summary = "Table generation failed.";
			}

			// Otherwise, tell the user that everything was successful.

			else
			{
				_summary = "All tables generated successfully.";
			}
		}
		
		// Write the result summary to the output
		
		_errOut.reportError (ErrorOutput.typeInformational, null, null,
							 ErrorOutput.noPosition, ErrorOutput.noPosition,
							 null, _summary);
		
		// Flush the ErrorOutput for a final time
		
		_errOut.flush();
		
		// Discard the ErrorOutput
		
		_errOut = null;
		
		// If the input stream is still open, close it
		
		if (_inStream != null)
		{
			try
			{
				_inStream.close();
			}
			catch (Exception e)
			{
			}
			_inStream = null;
		}
		
		// If the output stream is still open, close it
		
		if (_outStream != null)
		{
			try
			{
				_outStream.close();
			}
			catch (Exception e)
			{
			}
			_outStream = null;
		}
		
		return;
	}
	
	
	
	
	// Parse a filename into its components.
	//
	// Parameters:
	//	name = name of file, may optionally include a directory path.
	//	ext = default extension, or null.
	//	caseMap = true to correct the case of the base filename.
	//
	// The return value is an array of five strings.  If the return value is
	// called val, then the elements of val are:
	//	val[0] = name of parent directory.
	//	val[1] = separator that goes between directory and base filename.
	//	val[2] = primary portion of base filename.
	//	val[3] = separator that goes between primary and extension portions.
	//	val[4] = extension portion of base filename.
	//
	// For example, given "dir1\dir2\myfile.txt" the return value is:
	//	val[0] = "dir1\dir2"
	//	val[1] = "\"
	//	val[2] = "myfile"
	//	val[3] = "."
	//	val[4] = "txt"
	//
	// Any of the five returned strings can be empty if the corresponding part
	// of the filename is not supplied.  However, the five returned strings are
	// always non-null.
	//
	// The function removes leading and trailing white space surrounding the
	// directory name and the base filename.  White space consists of space
	// characters (0x20) and tab characters (0x09).
	//
	// If the ext parameter is non-null, then the ext parameter is a default
	// extension.  If the supplied base filename does not have an extension,
	// then the value of ext is used as the extension (i.e., val[3] = "." and
	// val[4] = ext).  If the supplied base filename has an extension, then the
	// supplied extension is used and the value of ext is ignored.  As a
	// special case, if the supplied base filename has an empty extension
	// (i.e., ends with a dot), then the trailing dot is removed and no
	// extension is added;  thus, a trailing dot can be used to indicate
	// explicitly that no extension is desired.
	//
	// If caseMap is true, then this function attempts to correct the case of
	// the base filename.  It does this by listing the disk directory and
	// searching for the entry that matches the base filename (after appending
	// the default extension, if required).  If a matching directory entry is
	// found, then this function sets the base filename (i.e., the values of
	// val[2], val[3], and val[4]) to the string obtained by reading the disk
	// directory.  If no matching directory entry is found, then the supplied
	// base filename (and default extension, if used) are returned unchanged.
	
	private static String[] parseFilename (String name, String ext, boolean caseMap)
	{
		
		// Return value
		
		String[] val = new String[5];
		
		// Handle null name
		
		if (name == null)
		{
			val[0] = "";
			val[1] = "";
			val[2] = "";
			val[3] = "";
			val[4] = "";
			return val;
		}
		
		// Determine end of the filename by stripping trailing spaces and tabs
		
		int iEnd;		// Index of last non-space character, plus 1
	
		for (iEnd = name.length(); iEnd > 0; --iEnd)
		{
			char c = name.charAt (iEnd - 1);
			if (c != ' ' && c != '\t')
			{
				break;
			}
		}
		
		// The separator character
		
		char cSepChar = File.separatorChar;
		
		// Horrible hack.  Microsoft Java doesn't know that Windows accepts
		// forward slash as a separator character.  So, if the separator
		// character is backslash (which almost surely indicates that this is
		// Windows), we also accept forward slash as a separator.
		
		char cSepChar2 = (cSepChar == '\\') ? '/' : cSepChar;
		
		// Find the path separator by scanning for the last occurrence of a
		// separator character
		
		int iStart;		// Index of character in string
	
		for (iStart = iEnd - 1; iStart >= 0; --iStart)
		{
			char c = name.charAt (iStart);
			if (c == cSepChar || c == cSepChar2)
			{
				break;
			}
		}
		
		// If we found a separator character ...
		
		if (iStart >= 0)
		{
		
			// Strip trailing spaces and tabs on the directory name
		
			int iEndDir;		// Index of last non-space character, plus 1
	
			for (iEndDir = iStart; iEndDir > 0; --iEndDir)
			{
				char c = name.charAt (iEndDir - 1);
				if (c != ' ' && c != '\t')
				{
					break;
				}
			}
		
			// Strip leading spaces and tabs on the base filename
		
			int iStartDir;		// Index of character in string
	
			for (iStartDir = 0; iStartDir < iEndDir; ++iStartDir)
			{
				char c = name.charAt (iStartDir);
				if (c != ' ' && c != '\t')
				{
					break;
				}
			}
			
			// This is the directory name
			
			val[0] = name.substring (iStartDir, iEndDir);
			
			// Supply the separator string
			
			val[1] = name.substring (iStart, iStart + 1);
		
			// Strip leading spaces and tabs on the base filename
	
			for (++iStart; iStart < iEnd; ++iStart)
			{
				char c = name.charAt (iStart);
				if (c != ' ' && c != '\t')
				{
					break;
				}
			}
		}
		
		// Otherwise, there is no separator character ...
		
		else
		{
			
			// Supply empty directory name and separator
			
			val[0] = "";
			val[1] = "";
		
			// Strip leading spaces and tabs on the base filename
	
			for (iStart = 0; iStart < iEnd; ++iStart)
			{
				char c = name.charAt (iStart);
				if (c != ' ' && c != '\t')
				{
					break;
				}
			}
		
			// Another horrible hack.  For Windows, we need to worry about
			// something like "x:file", that is, a drive letter and filename with
			// no separators at all.  In this case, we split the path after the
			// colon, specifying an empty separator string.  But note, we must
			// not split paths like "x:." or "x:..".
			
			if (cSepChar == '\\'
				&& (iEnd - iStart) >= 3
				&& name.charAt(iStart+1) == ':')
			{
				
				// Exclude names like "x:." or "x:.."
				
				for (int i = iStart + 2; i < iEnd; ++i)
				{
					char c = name.charAt (i);
					if (c != '.' && c != ' ' && c != '\t')
					{
				
						// Use drive letter and colon as directory
				
						val[0] = name.substring (iStart, iStart + 2);
		
						// Strip leading spaces and tabs on the base filename
	
						for (iStart = iStart + 2; iStart < iEnd; ++iStart)
						{
							char c2 = name.charAt (iStart);
							if (c2 != ' ' && c2 != '\t')
							{
								break;
							}
						}
						
						break;
					}
				}
			}
		}
		
		// Scan for '.' to find the start of the extension
		
		int iDot;		// Index of last dot, or iStart-1 if none found
		
		for (iDot = iEnd - 1; iDot >= iStart; --iDot)
		{
			if (name.charAt (iDot) == '.')
			{
				break;
			}
		}
		
		// Check that the following condition is satisfied:  There is a dot,
		// and there is at least one character prior to the dot that is not
		// a dot, space, or tab.
			
		checkdots:
		{
			for (int i = iStart; i < iDot; ++i)
			{
				char c = name.charAt (i);
				if (c != '.' && c != ' ' && c != '\t')
				{
					
					// Come here if the condition is satisfied.  In this
					// case, we consider the filename to have an extension.
					
					// If the extension is empty (i.e., the dot is the last
					// character of the filename), and there is a default
					// extension, then remove the extension.  This supports
					// the convention that a final dot can be used to indicate
					// that no extension is desired.
					
					if (ext != null && iDot == (iEnd - 1))
					{
						
						// Record empty extension
						
						val[3] = "";
						val[4] = "";
						
						// Strip trailing spaces and tabs prior to the dot
	
						for ( ; iDot > iStart; --iDot)
						{
							char c2 = name.charAt (iDot - 1);
							if (c2 != ' ' && c2 != '\t')
							{
								break;
							}
						}
					}
					
					// Otherwise, we retain the existing extension
					
					else
					{
						
						// Record existing extension
						
						val[3] = name.substring (iDot, iDot + 1);
						val[4] = name.substring (iDot + 1, iEnd);
					}
					
					// This is the primary filename.  It is nonempty.
					
					val[2] = name.substring (iStart, iDot);
						
					break checkdots;
				}
			}
			
			// Come here if the condition is not satisfied.  In this case,
			// we consider the filename to have no extension.  This includes
			// empty names, names with no dots, names such as "." and ".." that
			// consist entirely of dots, and names such as ".myfile" that begin
			// with a string of dots but have no succeeding dots.
			
			// If there is a nonempty default extension, and there is either
			// (a) a name with no dots (iDot==iStart-1) that is nonempty, or
			// (b) a name with a dot (iDot>=iStart) that has at least one
			// character after the dot, then append the default extension.
			
			if (ext != null && ext.length() != 0 && iDot != (iEnd - 1))
			{
				val[3] = ".";
				val[4] = ext;
			}
			
			// Otherwise, the filename is empty or it consists entirely of
			// dots, spaces, and tabs, or there is no non-empty default
			// extension.  There is no extension in these cases.  (Thus, we
			// never add extensions to "." or "..", or to empty filenames.)
			
			else
			{
				val[3] = "";
				val[4] = "";
			}
					
			// This is the primary filename.  It might be empty, but only if
			// the extension is also empty.
					
			val[2] = name.substring (iStart, iEnd);
		}
		
		// If we want case-mapping ...
		
		if (caseMap && val[2].length() != 0)
		{
			
			// The full path to our file
			
			String fullpath = val[0] + val[1] + val[2] + val[3] + val[4];
			
			// The directory we need to list
			
			String listdir = val[0];
			
			// Another hack.  If there is a list directory but no separator,
			// append '.' to the list directory.  This will occur if we are
			// given a name of the form "x:file" under Windows, in which case
			// we need to pass "x:." to File.list() in order to list the
			// current directory of drive x:.  Note that passing "x:" to
			// File.list() lists the root directory of drive x:.
			
			if (listdir.length() != 0 && val[1].length() == 0)
			{
				listdir = listdir + ".";
			}
			
			// If the directory is blank, try to use File.getAbsolutePath() to
			// get the parent directory
			
			if (listdir.length() == 0)
			{
				listdir = (new File ((new File (fullpath)).getAbsolutePath())).getParent();
			}
			
			// Another hack.  If we still haven't got a directory, just insert
			// '.' in place of the filename.  This will occur if we are given a
			// name of the form "\file" under Windows.
			
			if (listdir == null || listdir.length() == 0)
			{
				listdir = val[0] + val[1] + ".";
			}
			
			// If we have a parent directory and our file exists ...
			
			if (listdir != null && listdir.length() != 0
				&& (new File (fullpath)).exists() )
			{
				
				// List the directory
				
				String[] list = (new File (listdir)).list();
				
				// Search the list ...
					
				searchlist:
				{
					
					// If the list is empty, nothing to search
					
					if (list == null || list.length == 0)
					{
						break searchlist;
					}
					
					// The filename to search for
					
					String searchname = val[2] + val[3] + val[4];
						
					// First check for an exact match
					
					for (int i = 0; i < list.length; ++i)
					{
						if (searchname.equals (list[i]))
						{
							break searchlist;
						}
					}
						
					// Next, check for a match ignoring case
						
					for (int i = 0; i < list.length; ++i)
					{
						if (searchname.equalsIgnoreCase (list[i]))
						{
							val[4] = list[i].substring (
									val[2].length() + val[3].length());
							val[3] = list[i].substring (val[2].length(),
									val[2].length() + val[3].length());
							val[2] = list[i].substring (0, val[2].length());
							break searchlist;
						}
					}
						
					// Now, if there is an empty extension (i.e., a dot
					// but nothing after the dot), try stripping off
					// trailing dots to find a match
						
					if (val[3].length() != 0 && val[4].length() == 0)
					{
							
						// Loop over possible lengths
							
						for (int len = searchname.length() - 1; len > 1; --len)
						{
							char c = searchname.charAt (len);
							if (c == ' ' || c == '\t')
							{
								continue;
							}
							if (c != '.')
							{
								break;
							}
								
							// Get substring to compare with
								
							String subsearch = searchname.substring (0, len);
						
							// First check for an exact match
					
							for (int i = 0; i < list.length; ++i)
							{
								if (subsearch.equals (list[i]))
								{
									val[4] = "";
									val[3] = "";
									val[2] = list[i];
									break searchlist;
								}
							}
						
							// Next, check for a match ignoring case
						
							for (int i = 0; i < list.length; ++i)
							{
								if (subsearch.equalsIgnoreCase (list[i]))
								{
									val[4] = "";
									val[3] = "";
									val[2] = list[i];
									break searchlist;
								}
							}
						}
					}
				}
			}
		}
		
		// Return our result
		
		return val;
	}
	
	
}
