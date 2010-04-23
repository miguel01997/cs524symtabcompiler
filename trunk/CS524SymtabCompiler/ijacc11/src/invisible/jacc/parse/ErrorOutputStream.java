// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.PrintStream;


/*->

  ErrorOutputStream is a simple implementation of the ErrorOutput abstract
  class that immediately writes each message to the specified output stream.

->*/


public class ErrorOutputStream extends ErrorOutput
{


	// Destination stream

	private PrintStream _stream;
	
	// Line separator, or null to use the PrintStream default
	
	private String _separator;




	// Constructor specifies the destination stream.
	//
	// In addition, the constructor specifies a program name which is prepended
	// to each output string.  The program name can be null.

	public ErrorOutputStream (PrintStream stream, String program)
	{

		// Pass the program name to the ErrorOutput superclass

		super (program);

		// Save the stream

		_stream = stream;
		
		// Use default line separator
		
		_separator = null;

		return;
	}




	// Constructor specifies the destination stream.
	//
	// In addition, the constructor specifies a program name which is prepended
	// to each output string.  The program name can be null.
	//
	// Also, this constructor specifies the string to use as a line separator.
	// If the separator string is null, then the PrintStream default line
	// separator is used.
	//
	// Note: In Java 1.0, the PrintStream default line separator is "\n".
	// In Java 1.1, the PrintStream default line separator is the system property
	// System.getProperty("line.separator"), which is "\r\n" on Windows.

	public ErrorOutputStream (PrintStream stream, String program, String separator)
	{

		// Pass the program name to the ErrorOutput superclass

		super (program);

		// Save the stream

		_stream = stream;
		
		// Save the line separator
		
		_separator = separator;

		return;
	}




	// Handle an error message.  Parameters are as follows:
	//
	// type - Type of message.  It can be typeInformational, typeWarning,
	//	or typeError.
	//
	// module - A string identifying the program module that issued the
	//	message.  This can be null.
	//
	// file - A string identifying the source file.  This can be null.
	//
	// line - An integer identifying the line within the specified source file
	//	where the error occurred.  A value of noPosition indicates that the
	//	line number is unknown or unspecified.
	//
	// column - A integer identifying the column within the specified line
	//	where error occurred.  A value of noPosition indicates that the column
	//	number is unknown or unspecified.  It is expected that column numbers
	//	(and sometimes line numbers as well) are approximations.
	//
	// code - A string containing an error code.  This can be null.
	//
	// message - The text of the message.  This can be null.

	protected void handleError (int type, String module, String file, int line,
		int column, String code, String message)
	{

		// Print the error message on the stream

		if (_separator == null)
		{
			_stream.println (formatMessage (type, module, file, line, column, code, message));
		}
		else
		{
			_stream.print (formatMessage (type, module, file, line, column, code, message));
			_stream.print (_separator);
		}

		return;
	}




	// Flushes any buffered data to the output sink.

	public void flush ()
	{
		super.flush();
		
		// Flush the output stream
		
		_stream.flush();
		return;
	}


}

