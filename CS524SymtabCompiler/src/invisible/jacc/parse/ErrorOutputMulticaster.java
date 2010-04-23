package invisible.jacc.parse;

import invisible.jacc.util.ObjectSet;

import java.util.Enumeration;


/*->

  ErrorOutputMulticaster is an implementation of the ErrorOutput abstract class
  that sends each message to a set of ErrorOutput objects.  This is used when
  error messages need to go to more than one destination.  As a special case,
  this class also can create a "null" ErrorOutput object that drops each
  message into the bit bucket.

->*/


public class ErrorOutputMulticaster extends ErrorOutput
{
	
	// The set of ErrorOutput objects to which we send messages
	
	private ObjectSet _targetSet = new ObjectSet();


	
	
	// Constructor creates an ErrorOutputMulticaster with no targets.

	public ErrorOutputMulticaster ()
	{
		super (null);
		return;
	}
	
	
	
	
	// This function adds an ErrorOutput object to the set of targets.
	//
	// The function does nothing if the argument is null, or if the argument
	// is already in the set of target.
	
	public void add (ErrorOutput target)
	{
		if (target != null)
		{
			_targetSet.addElement (target);
		}
		return;
	}
	
	
	
	
	// This function removes an ErrorOutput object from the set of targets.
	//
	// The function does nothing if the argument is null, or if the argument
	// is not in the set of target.
	
	public void remove (ErrorOutput target)
	{
		if (target != null)
		{
			_targetSet.removeElement (target);
		}
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
		
		// Forward the message to the other ErrorOutput objects
		
		for (Enumeration e = _targetSet.elements(); e.hasMoreElements(); )
		{
			((ErrorOutput) e.nextElement()).reportError (
								type, module, file, line, column, code, message );
		}

		return;
	}




	// Flushes any buffered data to the output sink.

	public void flush ()
	{
		super.flush();
		
		// Flush all the other ErrorOutput objects
		
		for (Enumeration e = _targetSet.elements(); e.hasMoreElements(); )
		{
			((ErrorOutput) e.nextElement()).flush();
		}

		return;
	}


}
