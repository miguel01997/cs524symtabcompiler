// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  Prescanner is a common superinterface for PrescannerByte and
  PrescannerChar.  Prescanner abstracts away from the data type of the
  source, and allows all sources to be handled polymorphically.

->*/


public interface Prescanner 
{

	// This function is called by the scanner when it is finished using the
	// source.

	public void close () throws IOException;


}

