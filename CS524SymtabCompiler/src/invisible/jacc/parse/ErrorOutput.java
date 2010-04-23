// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  ErrorOutput is an abstract class that represents a destination for error
  messages.

  ErrorOutput provides base functionality which includes converting an error
  message into a text string, and maintaining counts of the number of error
  and warning messages.

  The public interface of ErrorOutput consists primarily of a method called
  reportError() which is used to report error messages, warning messages, and
  informational messages.  It also includes routines to read and reset the
  message counters.

  The protected interface of ErrorOutput consists primarily of an abstract
  method called handleError() which a concrete subclass overrides to define
  how a message is handled.  It also provides a method called formatText()
  which provides a default method for converting an error message into a text
  string.

  See ErrorOutputStream for an example of a concrete subclass of ErrorOutput.
  ErrorOutputStream just prints each error message on an output stream.  A more
  sophisticated subclass might store all the error messages in a table, and use
  the table to display the source code where each error occurred.
  
->*/


public abstract class ErrorOutput 
{



	// ----- Private variables -----




	// Number of error messages.

	private int _errorCount;


	// Number of warning messages.

	private int _warningCount;


	// Program name.  This can be null.

	private String _program;




	// ----- Public interface -----




	// Constants for types of error messages.

	public static final int typeInformational = 0;
	public static final int typeWarning = 1;
	public static final int typeError = 2;


	// Constant that defines an unknown or unspecified line or column.

	public static final int noPosition = 0x80000000;




	// Function for reporting an error.  Parameters are as follows:
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

	public final void reportError (int type, String module, String file, int line,
		int column, String code, String message)
	{

		// Advance the error counters, and validate the type

		switch (type)
		{

		case typeInformational:
			break;

		case typeWarning:
			++_warningCount;
			break;

		case typeError:
			++_errorCount;
			break;

		default:
			throw new IllegalArgumentException ("ErrorOutput.reportError");
		}

		// Let the concrete subclass process the error

		handleError (type, module, file, line, column, code, message);

		return;
	}




	// Returns the number of error messages issued.

	public final int errorCount ()
	{
		return _errorCount;
	}




	// Returns the number of warning messages issued.

	public final int warningCount ()
	{
		return _warningCount;
	}




	// Resets the error and warning counters to zero.

	public final void resetCounters ()
	{
		_errorCount = 0;
		_warningCount = 0;

		return;
	}




	// Flushes any buffered data to the output sink.

	public void flush ()
	{
		return;
	}




	// ----- Protected interface -----




	// Constructor.
	//
	// The constructor specifies a program name which is prepended to each
	// output string.  The program name can be null.

	protected ErrorOutput (String program)
	{
		super ();

		// Save the program

		_program = program;

		// Initialize our counters

		_errorCount = 0;
		_warningCount = 0;

		return;
	}




	// Returns the program name.

	protected final String program ()
	{
		return _program;
	}




	// Create a String that represents an error message.
	//
	// A subclass can override this method to change the way that messages
	// are formatted.
	//
	// Parameters are as follows:
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

	protected String formatMessage (int type, String module, String file, int line,
		int column, String code, String message)
	{

		// Allocate a buffer to build the string

		StringBuffer buf = new StringBuffer ();

		// Flag indicates if we need a separator before appending something

		boolean needSeparator = false;

		// If a program is specified ...

		if ((_program != null) && (_program.length() != 0))
		{

			// Append the program

			buf.append (_program);
			needSeparator = true;
		}

		// If a module is specified ...

		if ((module != null) && (module.length() != 0))
		{

			// Append a separator if needed

			if (needSeparator)
			{
				buf.append (": ");
			}

			// Append the module

			buf.append (module);
			needSeparator = true;
		}

		// If a file is specified ...

		if ((file != null) && (file.length() != 0))
		{

			// Append a separator if needed

			if (needSeparator)
			{
				buf.append (": ");
			}

			// Append the file

			buf.append (file);
			needSeparator = true;
		}

		// If a line is specified ...

		if (line != noPosition)
		{

			// Append left parenthesis and line

			buf.append ("(");
			buf.append (line);

			// If a column is also specified ...

			if (column != noPosition)
			{

				// Append comma and column

				buf.append (",");
				buf.append (column);
			}

			// Append right parenthesis

			buf.append (")");

			// If no separator is needed, append a space

			if (!needSeparator)
			{
				buf.append (" ");
			}
		}

		// If it's a warning or error, or if there's a code ...

		if (
			   (type == typeWarning)
			|| (type == typeError)
			|| ((code != null) && (code.length() != 0))
		   )
		{

			// Append a separator if needed

			if (needSeparator)
			{
				buf.append (": ");
			}

			needSeparator = false;
		}

		// Identify the type of message

		switch (type)
		{

		case typeWarning:
			buf.append ("warning");
			needSeparator = true;
			break;

		case typeError:
			buf.append ("error");
			needSeparator = true;
			break;
		}

		// If a code is specified ...

		if ((code != null) && (code.length() != 0))
		{

			// Append a separator if needed

			if (needSeparator)
			{
				buf.append (" ");
			}

			// Append the code

			buf.append (code);
			needSeparator = true;
		}

		// If a message is specified ...

		if ((message != null) && (message.length() != 0))
		{

			// Append a separator if needed

			if (needSeparator)
			{
				buf.append (": ");
			}

			// Append the message

			buf.append (message);
			needSeparator = true;
		}

		// Return the result

		return buf.toString();
	}




	// Handle an error message.
	//
	// This is an abstract method.  A subclass must override this method to
	// provide an implementation for handling error messages.
	//
	// The public function reportError() calls this method after validating
	// the type parameter and incrementing the appropriate counter.
	//
	// Parameters are as follows:
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

	protected abstract void handleError (int type, String module, String file, int line,
		int column, String code, String message);


}

