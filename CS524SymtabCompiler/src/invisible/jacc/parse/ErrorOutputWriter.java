// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.PrintWriter;


/*->

  ErrorOutputWriter is a simple implementation of the ErrorOutput abstract
  class that immediately writes each message to the specified output writer.

  Caution:  This class uses code specific to Java 1.1.  If you use this class,
  then your program will not run on Java 1.0.

->*/


public class ErrorOutputWriter extends ErrorOutput
{


	// Destination writer

	private PrintWriter _writer;
	
	// Line separator, or null to use the PrintWriter default
	
	private String _separator;




	// Constructor specifies the destination writer.
	//
	// In addition, the constructor specifies a program name which is prepended
	// to each output string.  The program name can be null.

	public ErrorOutputWriter (PrintWriter writer, String program)
	{

		// Pass the program name to the ErrorOutput superclass

		super (program);

		// Save the writer

		_writer = writer;
		
		// Use default line separator
		
		_separator = null;

		return;
	}




	// Constructor specifies the destination writer.
	//
	// In addition, the constructor specifies a program name which is prepended
	// to each output string.  The program name can be null.
	//
	// Also, this constructor specifies the string to use as a line separator.
	// If the separator string is null, then the PrintWriter default line
	// separator is used.
	//
	// Note:  The PrintWriter default line separator is the system property
	// System.getProperty("line.separator"), which is "\r\n" on Windows.

	public ErrorOutputWriter (PrintWriter writer, String program, String separator)
	{

		// Pass the program name to the ErrorOutput superclass

		super (program);

		// Save the writer

		_writer = writer;
		
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

		// Print the error message on the writer

		if (_separator == null)
		{
			_writer.println (formatMessage (type, module, file, line, column, code, message));
		}
		else
		{
			_writer.print (formatMessage (type, module, file, line, column, code, message));
			_writer.print (_separator);
		}

		return;
	}




	// Flushes any buffered data to the output sink.

	public void flush ()
	{
		super.flush();
		
		// Flush the output writer
		
		_writer.flush();
		return;
	}


}

