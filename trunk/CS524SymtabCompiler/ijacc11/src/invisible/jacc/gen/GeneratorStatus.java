package invisible.jacc.gen;

import invisible.jacc.parse.InterruptedCompilerException;


/*->

GeneratorStatus is an interface that the parser generator uses to report
its current status to the client.  It also allows the client to interrupt
the parser generator.

The definition of GeneratorStatus is based on the following model of how the
parser generator works:  The generation process proceeds through a series of
stages.  At the beginning of each stage, the parser generator calls
statusStage() to signal that it is beginning a new stage.  The argument to
statusStage() is a string that can be displayed to the user as an indication
of what the parser generator is doing.

If a stage is lengthy, the parser generator can call statusWork() repeatedly
during the stage.  This can be used to generate an indication to the user that
the parser generator is running.  There is no particular significance to the
number of times that statusWork() is called during a given stage.

If the client wishes to interrupt the parser generator, then statusStage() and
statusWork() may throw InterruptedCompilerException.  This exception will be
propagated completely out of the parser generator, returning control to the
client.  Note that the parser generator does not set the efInterrupted error
flag;  it is the responsibility of the client to do this.

When the parser generator detects an error, it calls statusError() to indicate
which error occurred.  The argument to statusError() is an int which contains
one or more error flag bits.  Calls to statusError() are cumulative;  that is,
the client is expected to maintain an internal error flags value, and form the
logical-or of the internal error flags with the argument to statusError().
Thus, the parser generator can call statusError() several times, to report
various errors.  The error flag bits are defined in interface GenObserver.

->*/


public interface GeneratorStatus
{
	
	// Signal the start of a new stage.
	//
	// The stage parameter is a one-line description of the new stage.  It must
	// be non-null.
	//
	// This function throws InterruptedCompilerException if there is an
	// interrupt request pending.
	
	public void statusStage (String stage) throws InterruptedCompilerException;
	
	
	// Signal that some work has been performed in the current stage.
	//
	// If a stage is lengthy, the parser generator can call this function to
	// update the status and check for interrupt request.
	//
	// There is no particular significance to the number of times that this
	// function is called during a given stage.  However, it is expected that
	// the total number of calls is fixed as a function of the grammar
	// specification and generation options (i.e., the number of calls does
	// not vary from run-to-run).
	//
	// This function throws InterruptedCompilerException if there is an
	// interrupt request pending.

	public void statusWork () throws InterruptedCompilerException;
	
	
	// Set bits in the error flags.
	//
	// This function computes the logical-or of the parameter with the internal
	// error flags value, and saves the result in the internal error flags
	// value.
	//
	// The error flag bits are defined in interface GenObserver.
	
	public void statusError (int errorFlags);
	
}
