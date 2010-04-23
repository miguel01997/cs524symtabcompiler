// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;
import java.io.InputStream;


/*->

  PrescannerJavaSource is a prescanner that reads a Java source file from an
  InputStream.  The Java source file is presumed to be stored one character per
  byte.  PrescannerJavaSource converts each byte to a char, and also recognizes
  and converts Unicode escape sequences.

  PrescannerJavaSource implements the PrescannerChar interface.

  When you create a PrescannerJavaSource, you must supply both an InputStream
  and a PrescannerJavaSourceClient.  When the prescanner encounters an invalid
  unicode escape sequence, it calls the PrescannerJavaSourceClient to report
  the error.

  Note: The presence of unicode escapes for characters 0x000A and 0x000D (line
  feed and carriage return) will throw off the scanner's line count, because
  the token factories consider them to be line ends, but the text editor does
  not consider them to be line ends.  However, the Visual J++ compiler also
  miscounts lines when these unicode escapes are present, in exactly the same
  way.  So our behavior in this regard matches Visual J++.

->*/



public class PrescannerJavaSource implements PrescannerChar
{

	// The contained InputStream object.

	private InputStream _stream;

	// Our client object.

	private PrescannerJavaSourceClient _client;

	// Source file buffer.

	private byte[] _sourceBuffer;

	// End of data currently in the source file buffer.

	private int _sourceEnd;

	// Current position in the source file buffer.

	private int _sourcePos;

	// This flag is true if we have reached end-of-file on the input source.

	private boolean _sourceEOF;

	// This flag is true if the last read operation was terminated by an
	// invalid escape character.

	private boolean _invalidEscape;




	// This private function fills the source buffer.

	private void fillSource () throws IOException
	{

		// If we already reached end-of-file, do nothing

		if (_sourceEOF)
		{
			return;
		}

		// Return the source position to the start of the buffer

		_sourcePos = 0;

		// Read some data from the input source

		_sourceEnd = Math.max (0, _stream.read (_sourceBuffer, 0, _sourceBuffer.length));

		// If we reached end-of-file, set the flag

		if (_sourceEnd == 0)
		{
			_sourceEOF = true;
		}

		return;
	}




	// The constructor saves the supplied client object and InputStream.
	//
	// The internal buffer size is set to bufSize.

	public PrescannerJavaSource (PrescannerJavaSourceClient client,
		InputStream stream, int bufSize)
	{
		super ();

		// Save the client

		_client = client;

		// Save the input stream

		_stream = stream;

		// Allocate the buffer

		if (bufSize < 1)
		{
			throw new IllegalArgumentException ("PrescannerJavaSource.PrescannerJavaSource");
		}

		_sourceBuffer = new byte[bufSize];

		// Initialize variables

		_sourceEnd = 0;
		_sourcePos = 0;
		_sourceEOF = false;

		_invalidEscape = false;

		return;
	}




	// Read chars from the source.
	//
	// Implements the read() method of PrescannerChar.

	public int read (char[] dstArray, int dstOffset, int dstLength, Token token)
		throws IOException, SyntaxException
	{

		// If the last call was terminated by an invalid escape character ...

		if (_invalidEscape)
		{

			// Report invalid escape character

			_client.javaSourceInvalidEscape (token);

			// Clear the flag

			_invalidEscape = false;
		}

		// If buffer is empty, refill it

		if (_sourcePos == _sourceEnd)
		{
			fillSource();
		}

		// If the length is less than 2, request a bigger buffer

		if (dstLength < 2)
		{
			return -1;
		}

		// Index into destination array

		int dstIndex = dstOffset;

		// The maximum number of source bytes we will process is the length of
		// the destination buffer minus one, or the number of bytes in the
		// source buffer, whichever is less.  (We leave an extra space in the
		// destination buffer so that we can always write in the character
		// following a backslash.)

		int srcLimit = Math.min (_sourceEnd, _sourcePos + dstLength - 1);

		// Loop until we exhaust the source buffer

	charLoop:
		while (_sourcePos < srcLimit)
		{

			// Get next character from the input buffer

			int c = _sourceBuffer[_sourcePos++] & 0xFF;

			// If it's not a backslash ...

			if (c != '\\')
			{

				// Write character into destination

				dstArray[dstIndex++] = (char) c;

				// Continue with next character

				continue charLoop;
			}

			// If buffer is empty ...

			if (_sourcePos == _sourceEnd)
			{

				// Refill the buffer

				fillSource();

				// If the buffer is still empty ...

				if (_sourcePos == _sourceEnd)
				{

					// Write backslash into destination

					dstArray[dstIndex++] = '\\';

					// Terminate the loop

					break charLoop;
				}

				// Set limit to terminate loop after this iteration

				srcLimit = 0;
			}

			// Get next character from the input buffer

			c = _sourceBuffer[_sourcePos++] & 0xFF;

			// If it's not a unicode escape ...

			if (c != 'u')
			{

				// Write backslash into destination

				dstArray[dstIndex++] = '\\';

				// Write character into destination

				dstArray[dstIndex++] = (char) c;

				// Continue with next character

				continue charLoop;
			}

			// Hexadecimal value of unicode escape

			int hexValue = 0;

			// Loop until we see 4 hexadecimal digits

			for (int hexDigits = 0; hexDigits < 4; )
			{

				// If buffer is empty ...

				if (_sourcePos == _sourceEnd)
				{

					// Refill the buffer

					fillSource();

					// If the buffer is still empty ...

					if (_sourcePos == _sourceEnd)
					{

						// If not first destination character, set the error
						// flag and terminate the loop

						if (dstIndex != dstOffset)
						{
							_invalidEscape = true;
							break charLoop;
						}

						// Report invalid escape character

						_client.javaSourceInvalidEscape (token);

						// Terminate the loop

						break charLoop;
					}

					// Set limit to terminate loop after this iteration

					srcLimit = 0;
				}

				// Get next character from the input buffer

				c = _sourceBuffer[_sourcePos++] & 0xFF;

				// Handle decimal digits

				if ((c >= '0') && (c <= '9'))
				{
					hexValue = (hexValue << 4) + (c - 0x30);
					++hexDigits;
				}

				// Handle uppercase hex digits

				else if ((c >= 'A') && (c <= 'F'))
				{
					hexValue = (hexValue << 4) + (c - 0x37);
					++hexDigits;
				}

				// Handle lowercase hex digits

				else if ((c >= 'a') && (c <= 'f'))
				{
					hexValue = (hexValue << 4) + (c - 0x57);
					++hexDigits;
				}

				// Handle invalid characters

				else if ((c != 'u') || (hexDigits != 0))
				{

					// Back up to rescan the invalid character

					--_sourcePos;

					// If not first destination character, set the error
					// flag and terminate the loop

					if (dstIndex != dstOffset)
					{
						_invalidEscape = true;
						break charLoop;
					}

					// Report invalid escape character

					_client.javaSourceInvalidEscape (token);

					// Next character

					continue charLoop;
				}

			}	// end loop until 4 hexadecimal digits

			// Write hexadecimal value into destination

			dstArray[dstIndex++] = (char) hexValue;

		}	// end charLoop

		// Return number of characters written to destination

		return dstIndex - dstOffset;
	}




	// Close the source.
	//
	// Implements the close() method of PrescannerByte

	public void close () throws IOException
	{
		_stream.close();
		return;
	}


}


