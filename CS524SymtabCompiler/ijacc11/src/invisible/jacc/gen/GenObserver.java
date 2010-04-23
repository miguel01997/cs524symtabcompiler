// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.gen;


/*->

GenObserver is an interface that defines an object that can observe the
progress of the parser generator.

When the parser generator begins, it calls generatorBegin(), passing the name
of the grammar specification file.  When the parser generator ends, it calls
generatorEnd(), passing a summary and a set of error flags.  It is guaranteed
that there is exactly one call to generatorBegin() and exactly one call to
generatorEnd().

In between the beginning and the end, the parser generator proceeds through a
series of stages.  At the beginning of each stage, the parser generator calls
generatorStage(), passing a description of the stage.  If a stage is lengthy,
the parser generator calls generatorWork() repeatedly during the stage, passing
the amount of work done since the start of the stage.

->*/


public interface GenObserver
{
	
	// Signal the beginning of parser generation.
	//
	// The filename parameter specifies the name of the grammar specification
	// file.  It can be null.
	//
	// The shortFilename parameter is a possibly-shortened form of the name of
	// the grammar specification file.  For example, if the filename parameter
	// is a complete path, then the shortFilename parameter may be the final
	// component of the path.  However, it is possible that shortFilename is
	// the same as the filename parameter.  This parameter can be null.
	
	public void generatorBegin (String filename, String shortFilename);
	
	
	// Signal the end of parser generation.
	//
	// The summary parameter is a one-line summary of the results.  It must be
	// non-null.
	//
	// The errorFlags parameter is a set of error flags.  If errorFlags is
	// zero, then the parser generation is completely successful.  Otherwise,
	// bits are set in errorFlags to indicate which errors occurred.
	//
	// If the efAborted bit is set in errorFlags, it means that there was an
	// error that made it impossible to execute the parser generator (for
	// example, if the grammar file could not be opened).  In this case, the
	// summary parameter contains an error message describing the error.
	//
	// A client may use efAborted as a hint for how to display the results.
	// For example, if efAborted is zero, a GUI can display the output in a
	// window with the summary appearing in a status line.  If efAborted is
	// one, a GUI can display the summary in a pop-up dialog box.
	
	public void generatorEnd (String summary, int errorFlags);
	
	
	// Signal the start of a new stage.
	//
	// The stage parameter is a one-line description of the new stage.  It must
	// be non-null.
	
	public void generatorStage (String stage);
	
	
	// Signal that some work has been performed in the current stage.
	//
	// If a stage is lengthy, the parser generator calls this function
	// repeatedly during the stage.  The amount parameter is 1 for the first
	// call to generatorWork() during a given stage, and is incremented by 1
	// on each succeeding call during the stage.  Thus, the amount parameter
	// is an indication of how much work has been done during the current
	// stage.
	//
	// The units of work are not specified;  they do not directly correspond to
	// anything in the grammar specification.  However, the number of calls to
	// generatorWork() is fixed for a given grammar specification and set of
	// generation options;  the number of calls does not vary from run-to-run.

	public void generatorWork (int amount);
	
	
	// Error flag values.
	//
	// These are the bits that may be set in the errorFlags parameter of the
	// generatorEnd() function.
	
	public static final int efInterrupted = 0x0001;
	public static final int efAborted = 0x0002;
	public static final int efBadFilename = 0x0004;
	public static final int efJaccOpen = 0x0008;
	public static final int efJaccRead = 0x0010;
	public static final int efGrammarSpec = 0x0020;
	public static final int efParserTable = 0x0040;
	public static final int efScannerTable = 0x0080;
	public static final int efInternalError = 0x0100;
	public static final int efOutWrite = 0x0200;
	public static final int efGenWrite = 0x0400;
	public static final int efParserJavaWrite = 0x0800;
	public static final int efScannerJavaWrite = 0x1000;
	public static final int efNoJavaName = 0x2000;

	
}
