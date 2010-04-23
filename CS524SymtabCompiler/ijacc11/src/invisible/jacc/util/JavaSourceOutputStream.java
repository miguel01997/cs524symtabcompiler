// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.io.PrintStream;


/*->

  A JavaSourceOutputStream object is a text output stream that is intended
  for writing machine-generated Java source files.

  When you create a JavaSourceOutputStream, you must supply a PrintStream
  object which serves as the sink of data.

  Note:  This class is a good example of why Java needs templates!

->*/


public class JavaSourceOutputStream 
{

	// The PrintStream that we use to write our text

	private PrintStream _ps;

	// This flag is true if we are at the start of a line

	private boolean _startOfLine;

	// The current indentation level

	private int _indentation;

	// Hexadecimal characters for Unicode escapes

	private static final char[] hexChar =
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };




	// The constructor saves the supplied PrintStream, and sets the indentation
	// to zero.

	public JavaSourceOutputStream (PrintStream ps)
	{
		super ();

		// Save the print stream

		if (ps == null)
		{
			throw new NullPointerException ("JavaSourceOutputStream.JavaSourceOutputStream");
		}

		_ps = ps;

		// Other initialization

		_startOfLine = true;

		_indentation = 0;

		return;
	}




	// Close the contained PrintStream object.

	public void close ()
	{
		_ps.close();

		return;
	}




	// This private function checks if we are at the start of a line.  If so,
	// it writes the indentation, and then sets _startOfLine to false.

	private void checkLine ()
	{

		// If we are at the start of a line ...

		if (_startOfLine)
		{

			// Clear the flag

			_startOfLine = false;

			// Write the indentation, 4 spaces per level

			for (int i = 0; i < _indentation; ++i)
			{
				_ps.print("    ");
			}
		}

		return;
	}




	// This private function prints any character.  It is for use in writing
	// characters within character literals and string literals.
	//
	// If the character has an escape sequence (as defined in the Java Language
	// Specification, section 3.10.6), the escape sequence is written.
	// Otherwise, if the character is between 0x20 and 0x7E, the character is
	// written.  Otherwise, a Unicode escape is written.

	private void printChar (char c)
	{

		// Switch on the character value

		switch (c)
		{

		case 0x0008:
			_ps.print("\\b");
			break;

		case 0x0009:
			_ps.print("\\t");
			break;

		case 0x000A:
			_ps.print("\\n");
			break;

		case 0x000C:
			_ps.print("\\f");
			break;

		case 0x000D:
			_ps.print("\\r");
			break;

		case 0x0022:
			_ps.print("\\\"");
			break;

		case 0x0027:
			_ps.print("\\\'");
			break;

		case 0x005C:
			_ps.print("\\\\");
			break;

		default:

			// If it's an ASCII character, print the character itself

			if ((c >= 0x0020) && (c <= 0x007E))
			{
				_ps.print(c);
			}

			// Otherwise, print a Unicode escape

			else
			{
				_ps.print("\\u");
				_ps.print(hexChar[(c >> 12) & 0x000F]);
				_ps.print(hexChar[(c >> 8) & 0x000F]);
				_ps.print(hexChar[(c >> 4) & 0x000F]);
				_ps.print(hexChar[c & 0x000F]);
			}

			break;
		}

		return;
	}




	// Write a literal int.

	public void literal (int i)
	{
		checkLine();

		_ps.print(i);

		return;
	}




	// Write a literal short.

	public void literal (short h)
	{
		checkLine();

		_ps.print(h);

		return;
	}




	// Write a literal byte.

	public void literal (byte b)
	{
		checkLine();

		_ps.print(b);

		return;
	}




	// Write a literal long.

	public void literal (long l)
	{
		checkLine();

		_ps.print(l);
		_ps.print("L");

		return;
	}




	// Write a literal float.

	public void literal (float f)
	{
		checkLine();

		_ps.print(f);
		_ps.print("F");

		return;
	}




	// Write a literal double.

	public void literal (double d)
	{
		checkLine();

		_ps.print(d);
		_ps.print("D");

		return;
	}




	// Write a literal boolean.

	public void literal (boolean b)
	{
		checkLine();

		_ps.print(b);

		return;
	}




	// Write a literal char.

	public void literal (char c)
	{
		checkLine();

		_ps.print("\'");
		printChar(c);
		_ps.print("\'");

		return;
	}




	// Write a literal String.

	public void literal (String s)
	{
		checkLine();

		// If the string is null, just print "null"

		if (s == null)
		{
			_ps.print("null");
		}

		// Otherwise, print all the characters of the string, between quotes

		else
		{
			_ps.print("\"");

			for (int i = 0; i < s.length(); ++i)
			{
				printChar(s.charAt(i));
			}

			_ps.print("\"");
		}

		return;
	}




	// Write a new-line.  This ends the current line.
	//
	// If we are currently at the start of a line, this writes a blank line.

	public void line ()
	{

		// Write the new-line

		_ps.println();

		// Set flag to indicate we are at start of line

		_startOfLine = true;

		return;
	}




	// Write the specified string, followed by a new-line.  The string is
	// appended onto the end of the current line.
	//
	// Only the low-order byte of each character in the string is written.

	public void line (String s)
	{
		checkLine();

		// Write the string, followed by a new-line

		_ps.println(s);

		// Set flag to indicate we are at start of line

		_startOfLine = true;

		return;
	}




	// Write the specified string.  The string is appended onto the end of
	// the current line.
	//
	// Only the low-order byte of each character in the string is written.

	public void text (String s)
	{
		checkLine();

		// Write the string

		_ps.print(s);

		return;
	}




	// If we are not at the start of a line, write a new-line.  Otherwise,
	// do nothing.
	//
	// This function ensures that the next text appears at the start of a line.

	public void flush ()
	{

		// If not at start of line ...

		if (!_startOfLine)
		{

			// Write the new-line

			_ps.println();

			// Set flag to indicate we are at start of line

			_startOfLine = true;
		}

		return;
	}




	// Open a scope.
	//
	// If we are at the start of a line, "{" is written;  otherwise, " {" is
	// written.  Then, a new-line is written.  Then, the indentation is
	// incremented

	public void openScope ()
	{

		// Write the open brace

		if (_startOfLine)
		{
			line ("{");
		}
		else
		{
			line (" {");
		}

		// Increment the indentation

		++_indentation;

		return;
	}




	// Close a scope.
	//
	// If we are not at the start of a line, new-line is written.  Then, the
	// indentation is decremented if it is not already zero.  Then, "}" is
	// written, followed by a new-line.

	public void closeScope ()
	{

		// Flush the current line

		flush();

		// Decrement the indentation

		if (_indentation != 0)
		{
			--_indentation;
		}

		// Write the close brace

		line ("}");

		return;
	}




	// Close a scope.
	//
	// If we are not at the start of a line, new-line is written.  Then, the
	// indentation is decremented if it is not already zero.  Then, "}" is
	// written, followed by the specified string.  Note that a final new-line
	// is not written.
	//
	// Only the low-order byte of each character in the string is written.

	public void closeScope (String s)
	{

		// Flush the current line

		flush();

		// Decrement the indentation

		if (_indentation != 0)
		{
			--_indentation;
		}

		// Write the close brace

		text ("}");

		// Write the string

		text (s);

		return;
	}




	// Write a literal array of int

	public void literal (int[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 20, print comma and start new line

				if ((i % 20) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of short

	public void literal (short[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 20, print comma and start new line

				if ((i % 20) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of byte

	public void literal (byte[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 20, print comma and start new line

				if ((i % 20) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of char

	public void literal (char[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 20, print comma and start new line

				if ((i % 20) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of long

	public void literal (long[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 10, print comma and start new line

				if ((i % 10) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of float

	public void literal (float[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 10, print comma and start new line

				if ((i % 10) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of double

	public void literal (double[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 10, print comma and start new line

				if ((i % 10) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of boolean

	public void literal (boolean[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Write opening brace

		text ("{");

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// If multiple of 20, print comma and start new line

				if ((i % 20) == 0)
				{
					line (",");
				}

				// Otherwise, print comma and space

				else
				{
					text (", ");
				}
			}

			// Print the array element

			literal (x[i]);
		}

		// Write closing brace

		text ("}");

		return;
	}




	// Write a literal array of String

	public void literal (String[] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the array element

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of int

	public void literal (int[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of short

	public void literal (short[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of byte

	public void literal (byte[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of char

	public void literal (char[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of long

	public void literal (long[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of float

	public void literal (float[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of double

	public void literal (double[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}




	// Write a literal array of array of boolean

	public void literal (boolean[][] x)
	{

		// If the array is null, just print "null"

		if (x == null)
		{
			text ("null");

			return;
		}

		// Open a scope

		openScope ();

		// Loop over elements of x ...

		for (int i = 0; i < x.length; ++i)
		{

			// If this isn't the first element ...

			if (i != 0)
			{

				// Print comma and start new line

				line (",");
			}

			// Print the subarray

			literal (x[i]);
		}

		// Close scope, without starting a new line

		closeScope ("");

		return;
	}





}

