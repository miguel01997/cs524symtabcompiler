// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/*->

  ArrayIO is a class that defines a set of I/O functions for arrays.

  There are eight types of functions defined in this class:

  1. Array write functions.  These functions write a series of array elements
  to a stream.  These functions all accept an offset and length, so you can
  use them with subarrays.

  2. Array read functions.  These functions read a series of array elements
  from a stream.  These functions all accept an offset and length, so you can
  use them with subarrays.

  3. Element write functions.  These functions write a single data element to
  a stream.

  4. Element read functions.  These functions read a single data element from
  a stream.

  5. One-dimensional array write functions.  These functions write an entire
  one-dimensional array, including length information, to a stream.

  6. One-dimensional array read functions.  These functions read an entire
  two-dimensional array, including length information, from a stream.  These
  functions use the length information to automatically create the array.

  7. Two-dimensional array write functions.  These functions write an entire
  one-dimensional array, including length information, to a stream.

  8. Two-dimensional array read functions.  These functions read an entire
  two-dimensional array, including length information, from a stream.  These
  functions use the length information to automatically create the array.

  Each function is supported for every primitive type, and for String type.

  This class defines a variety of format codes, which represent external data
  formats.  That is, a format code describes the format in which data is stored
  within the stream.

  Supported external data formats include:  (a) boolean data, encoded one per
  byte;  (b) 8-bit, 16-bit, 32-bit, and 64-bit integers, each of which can be
  considered signed or unsigned;  (c) 32-bit and 64-bit floating point data;
  (d) run-length-encoded 1-bit (boolean) data;  (e) run-length-encoded 8-bit
  or 16-bit integers, each of which can be considered signed or unsigned;
  (f) UTF-encoded strings;  and (g) strings with 8-bit or 16-bit characters.

  Each function accepts a format parameter which specifies the external data
  format to be used.  The functions allow any sensible combination of internal
  data type and external data format.  The functions automatically convert
  between the internal data type and external data format, thereby giving a
  great deal of flexibility.

  Since all functions are static, there is no need to ever create an instance
  of this class.

->*/


public final class ArrayIO 
{




	// ----- Data Format Constants -----


	// The following set of constants define data formats.  A data format
	// refers to the format with which data is stored in a stream.  Data with
	// a given format can be written from, or read into, variables of
	// different types.  For example, byte (8-bit) format can be written from
	// or read into byte, char, short, int, or long data variables.
	//
	// Because of the large number of possible format and data type
	// combinations, many functions accept a data format parameter.  This
	// avoids the need to create a separate function signature for each
	// possible combination.
	//
	// Following Java convention, all multi-byte data formats are stored high
	// byte first.


	// Boolean (1-bit) format.  Boolean data is stored one data element per
	// byte, with 0 representing false and 1 representing true.

	public static final int formatBoolean = 0;


	// Signed byte (8-bit) format.  Conversions to and from this data
	// format always use sign-extension.

	public static final int formatByte = 1;


	// Unsigned byte (8-bit) format.  Conversions to and from this data
	// format always use zero-extension.

	public static final int formatByteUnsigned = 2;


	// Signed short (16-bit) format.  Conversions to and from this data
	// format always use sign-extension.

	public static final int formatShort = 3;


	// Unsigned short (16-bit) format.  Conversions to and from this data
	// format always use zero-extension.

	public static final int formatShortUnsigned = 4;


	// Signed int (32-bit) format.  Conversions to and from this data
	// format always use sign-extension.

	public static final int formatInt = 5;


	// Unsigned int (32-bit) format.  Conversions to and from this data
	// format always use zero-extension.

	public static final int formatIntUnsigned = 6;


	// Signed long (64-bit) format.  Conversions to and from this data
	// format always use sign-extension.

	public static final int formatLong = 7;


	// Unsigned long (64-bit) format.  Conversions to and from this data
	// format always use zero-extension.

	public static final int formatLongUnsigned = 8;


	// Float (32-bit) format.  This can only be converted to or from floating
	// data types.

	public static final int formatFloat = 9;


	// Double (64-bit) format.  This can only be converted to or from floating
	// data types.

	public static final int formatDouble = 10;


	// Boolean run-length-encoded format.  This encodes boolean (1-bit) data
	// in RLE form.  Each byte of encoded data contains a repetition count in
	// the low 7 bits, and a data value (0 = false, 1 = true) in the high bit.
	// Refer to class ArrayRLE for further details.

	public static final int formatBooleanRLE = 11;


	// Byte run-length-encoded format, where each byte is considered signed.
	// This encodes byte (8-bit) data in RLE form.  The encoded data consists
	// of records, each 2 bytes long.  The first byte is a repetition count,
	// and the second byte is a data value.  Refer to class ArrayRLE for
	// further details.

	public static final int formatByteRLE = 12;


	// Byte run-length-encoded format, where each byte is considered unsigned.
	// This encodes byte (8-bit) data in RLE form.  The encoded data consists
	// of records, each 2 bytes long.  The first byte is a repetition count,
	// and the second byte is a data value.  Refer to class ArrayRLE for
	// further details.

	public static final int formatByteRLEUnsigned = 13;


	// Short run-length-encoded format, where each short is considered signed.
	// This encodes short (16-bit) data in RLE form.  The encoded data consists
	// of records, each either 2 or 4 bytes long.  A 2-byte record contains an
	// unrepeated data value.  A 4-byte record contains a 1-byte escape code,
	// followed by a 1-byte repetition count, followed by a 2-byte data value.
	// Refer to class ArrayRLE for further details.

	public static final int formatShortRLE = 14;


	// Short run-length-encoded format, where each short is considered unsigned.
	// This encodes short (16-bit) data in RLE form.  The encoded data consists
	// of records, each either 2 or 4 bytes long.  A 2-byte record contains an
	// unrepeated data value.  A 4-byte record contains a 1-byte escape code,
	// followed by a 1-byte repetition count, followed by a 2-byte data value.
	// Refer to class ArrayRLE for further details.

	public static final int formatShortRLEUnsigned = 15;


	// String UTF format.  See java.io.DataOutput for documentation on this
	// format.  This format can be converted to or from a non-null String.

	public static final int formatStringUTF = 16;


	// String byte format.  The encoded data consists of the length of the
	// string, followed by the low-order byte of each character in the string.
	// See writeLength() below for the length format.  This format can be
	// converted to or from a String, including a null String.

	public static final int formatStringBytes = 17;


	// String char format.  The encoded data consists of the length of the
	// string, followed by the 16-bit characters in the string.  See
	// writeLength() below for the length format.  This format can be converted
	// to or from a String, including a null String.

	public static final int formatStringChars = 18;




	// ----- Private Data Format Definitions -----

	// The following constants and routines are used internally to construct
	// the various data formats.


	// Boolean RLE format repetition field mask.

	private static final int booleanRLERepMask = 0x7F;

	// Boolean RLE format data field mask.

	private static final int booleanRLEDataMask = 0x80;

	// Short RLE initial escape code, shifted 8 bits.

	private static final int shortRLEEscInitial = 0x8100;

	// Short RLE escape code increment, shifted 8 bits.

	private static final int shortRLEEscIncrement = 0x5300;


	// Write an array length.
	//
	// The argument is the length of an array, or -1 if the array is null.
	//
	// If the length is -1, this function writes two bytes containing 0xFFFF.
	// Otherwise, if the length is less than or equal to 0x7FFF, this function
	// writes two bytes containing the length.  Otherwise, this function writes
	// four bytes containing the logical-or of the length and 0x80000000.

	private static void writeLength (DataOutput stream, int len) throws IOException
	{
		if (len <= 0x7FFF)
		{
			stream.writeShort (len);
		}
		else
		{
			stream.writeInt (len | 0x80000000);
		}

		return;
	}


	// Read an array length.

	private static int readLength (DataInput stream) throws IOException
	{
		int result = stream.readShort();

		if (((result & 0x8000) != 0) && (result != -1))
		{
			result = ((result & 0x7FFF) << 16) + stream.readUnsignedShort();
		}

		return result;
	}




	// ----- Array Write Functions -----


	// write (DataOutput stream, int format, X[] data, int off, int len)
	//
	// Writes array elements to the stream.  Elements are written beginning at
	// data[off].  The number of elements written is len.  The number of bytes
	// written to the stream is len times the size of the specified data
	// format (for fixed-size formats).
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is sign-extended if a signed format is
	// specified, or zero-extended if an unsigned format is specified.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has more bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has fewer bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is truncated by discarding high-order bits.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.




	public static void write (DataOutput stream, int format,
		byte[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write bytes

				stream.write (data, off, len);

				return;
			}


		case formatShort:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort (data[srcIndex++]);
				}

				return;
			}


		case formatShortUnsigned:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort (0xFF & data[srcIndex++]);
				}

				return;
			}


		case formatInt:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (data[srcIndex++]);
				}

				return;
			}


		case formatIntUnsigned:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (0xFF & data[srcIndex++]);
				}

				return;
			}


		case formatLong:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong ((long) data[srcIndex++]);
				}

				return;
			}


		case formatLongUnsigned:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong (0xFFL & (long) data[srcIndex++]);
				}

				return;
			}


		case formatByteRLE:
		case formatByteRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Get first data element

				byte prevElement = data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					byte nextElement = data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the repeat code for previous element

							stream.writeByte (repCount);
							stream.writeByte (prevElement);

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// Write the repeat code for previous element

						stream.writeByte (repCount);
						stream.writeByte (prevElement);

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// Write the repeat code for last element

				stream.writeByte (repCount);
				stream.writeByte (prevElement);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		char[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write bytes

				while (srcIndex < srcLimit)
				{
					stream.writeByte (data[srcIndex++]);
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort (data[srcIndex++]);
				}

				return;
			}


		case formatInt:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt ((int)(short) data[srcIndex++]);
				}

				return;
			}


		case formatIntUnsigned:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (0xFFFF & data[srcIndex++]);
				}

				return;
			}


		case formatLong:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong ((long)(short) data[srcIndex++]);
				}

				return;
			}


		case formatLongUnsigned:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong (0xFFFFL & (long) data[srcIndex++]);
				}

				return;
			}


		case formatByteRLE:
		case formatByteRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Get first data element

				byte prevElement = (byte) data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					byte nextElement = (byte) data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the repeat code for previous element

							stream.writeByte (repCount);
							stream.writeByte (prevElement);

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// Write the repeat code for previous element

						stream.writeByte (repCount);
						stream.writeByte (prevElement);

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// Write the repeat code for last element

				stream.writeByte (repCount);
				stream.writeByte (prevElement);

				return;
			}


		case formatShortRLE:
		case formatShortRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Get first data element

				short prevElement = (short) data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					short nextElement = (short) data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// If the previous element is repeated more than once ...

						if (repCount > 0)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}
						}

						// Otherwise, if the previous element had escape code as high byte ...

						else if ((prevElement & 0xFF00) == escape)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// Change escape code

							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}

						// Otherwise, we can write a single element ...

						else
						{

							// Write the previous element

							stream.writeShort (prevElement);
						}

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// If the last element is repeated more than once, or had escape code as high byte ...

				if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
				{

					// Write the escape code and repeat code for last element

					stream.writeShort (escape + repCount);

					// Write the last element

					stream.writeShort (prevElement);
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Write the last element

					stream.writeShort (prevElement);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		short[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write bytes

				while (srcIndex < srcLimit)
				{
					stream.writeByte (data[srcIndex++]);
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort (data[srcIndex++]);
				}

				return;
			}


		case formatInt:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (data[srcIndex++]);
				}

				return;
			}


		case formatIntUnsigned:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (0xFFFF & data[srcIndex++]);
				}

				return;
			}


		case formatLong:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong ((long) data[srcIndex++]);
				}

				return;
			}


		case formatLongUnsigned:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong (0xFFFFL & (long) data[srcIndex++]);
				}

				return;
			}


		case formatByteRLE:
		case formatByteRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Get first data element

				byte prevElement = (byte) data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					byte nextElement = (byte) data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the repeat code for previous element

							stream.writeByte (repCount);
							stream.writeByte (prevElement);

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// Write the repeat code for previous element

						stream.writeByte (repCount);
						stream.writeByte (prevElement);

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// Write the repeat code for last element

				stream.writeByte (repCount);
				stream.writeByte (prevElement);

				return;
			}


		case formatShortRLE:
		case formatShortRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Get first data element

				short prevElement = data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					short nextElement = data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// If the previous element is repeated more than once ...

						if (repCount > 0)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}
						}

						// Otherwise, if the previous element had escape code as high byte ...

						else if ((prevElement & 0xFF00) == escape)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// Change escape code

							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}

						// Otherwise, we can write a single element ...

						else
						{

							// Write the previous element

							stream.writeShort (prevElement);
						}

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// If the last element is repeated more than once, or had escape code as high byte ...

				if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
				{

					// Write the escape code and repeat code for last element

					stream.writeShort (escape + repCount);

					// Write the last element

					stream.writeShort (prevElement);
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Write the last element

					stream.writeShort (prevElement);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		int[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write bytes

				while (srcIndex < srcLimit)
				{
					stream.writeByte (data[srcIndex++]);
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort (data[srcIndex++]);
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt (data[srcIndex++]);
				}

				return;
			}


		case formatLong:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong ((long) data[srcIndex++]);
				}

				return;
			}


		case formatLongUnsigned:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong (0xFFFFFFFFL & (long) data[srcIndex++]);
				}

				return;
			}


		case formatByteRLE:
		case formatByteRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Get first data element

				byte prevElement = (byte) data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					byte nextElement = (byte) data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the repeat code for previous element

							stream.writeByte (repCount);
							stream.writeByte (prevElement);

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// Write the repeat code for previous element

						stream.writeByte (repCount);
						stream.writeByte (prevElement);

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// Write the repeat code for last element

				stream.writeByte (repCount);
				stream.writeByte (prevElement);

				return;
			}


		case formatShortRLE:
		case formatShortRLEUnsigned:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Get first data element

				short prevElement = (short) data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					short nextElement = (short) data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == 0xFF)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// If the previous element is repeated more than once ...

						if (repCount > 0)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// If previous element had escape code as high byte, change escape code

							if ((prevElement & 0xFF00) == escape)
							{
								escape = (escape + shortRLEEscIncrement) & 0xFF00;
							}
						}

						// Otherwise, if the previous element had escape code as high byte ...

						else if ((prevElement & 0xFF00) == escape)
						{

							// Write the escape code and repeat code for previous element

							stream.writeShort (escape + repCount);

							// Write the previous element

							stream.writeShort (prevElement);

							// Change escape code

							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}

						// Otherwise, we can write a single element ...

						else
						{

							// Write the previous element

							stream.writeShort (prevElement);
						}

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// If the last element is repeated more than once, or had escape code as high byte ...

				if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
				{

					// Write the escape code and repeat code for last element

					stream.writeShort (escape + repCount);

					// Write the last element

					stream.writeShort (prevElement);
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Write the last element

					stream.writeShort (prevElement);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		long[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write bytes

				while (srcIndex < srcLimit)
				{
					stream.writeByte ((int) data[srcIndex++]);
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write shorts

				while (srcIndex < srcLimit)
				{
					stream.writeShort ((int) data[srcIndex++]);
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Write ints

				while (srcIndex < srcLimit)
				{
					stream.writeInt ((int) data[srcIndex++]);
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Write longs

				while (srcIndex < srcLimit)
				{
					stream.writeLong (data[srcIndex++]);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		float[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Write floats

				while (srcIndex < srcLimit)
				{
					stream.writeFloat (data[srcIndex++]);
				}

				return;
			}


		case formatDouble:
			{

				// Write doubles

				while (srcIndex < srcLimit)
				{
					stream.writeDouble (data[srcIndex++]);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		double[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Write floats

				while (srcIndex < srcLimit)
				{
					stream.writeFloat ((float) data[srcIndex++]);
				}

				return;
			}


		case formatDouble:
			{

				// Write doubles

				while (srcIndex < srcLimit)
				{
					stream.writeDouble (data[srcIndex++]);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		boolean[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatBoolean:
			{

				// Write booleans

				while (srcIndex < srcLimit)
				{
					stream.writeBoolean (data[srcIndex++]);
				}

				return;
			}


		case formatBooleanRLE:
			{

				// If the source length is zero, do nothing

				if (len == 0)
				{
					return;
				}

				// Get first data element

				boolean prevElement = data[srcIndex++];

				// Repetition count for prevElement

				int repCount = 0;

				// Loop over data array ...

				while (srcIndex < srcLimit)
				{

					// Get the next element

					boolean nextElement = data[srcIndex++];

					// If it repeats the previous element ...

					if (nextElement == prevElement)
					{

						// If the repetition count is already maxed out ...

						if (repCount == booleanRLERepMask)
						{

							// Write the repeat code for previous element

							stream.writeByte (repCount + (prevElement ? booleanRLEDataMask : 0));

							// Restart the repetition count

							repCount = 0;
						}

						// Otherwise, increment the repetition count

						else
						{
							++repCount;
						}
					}

					// Otherwise, it's a new element ...

					else
					{

						// Write the repeat code for previous element

						stream.writeByte (repCount + (prevElement ? booleanRLEDataMask : 0));

						// Restart the repetition count

						repCount = 0;

						// The next element becomes the previous element

						prevElement = nextElement;
					}

				}	// end loop over data array

				// Write the repeat code for the last element

				stream.writeByte (repCount + (prevElement ? booleanRLEDataMask : 0));

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	public static void write (DataOutput stream, int format,
		String[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.write");
		}

		// Source index

		int srcIndex = off;

		// Source limit

		int srcLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatStringUTF:
			{

				// Write UTF strings

				while (srcIndex < srcLimit)
				{
					stream.writeUTF (data[srcIndex++]);
				}

				return;
			}


		case formatStringBytes:
			{

				// Write byte strings

				while (srcIndex < srcLimit)
				{
					String element = data[srcIndex++];

					if (element == null)
					{
						writeLength (stream, -1);
					}
					else
					{
						writeLength (stream, element.length());
						stream.writeBytes (element);
					}
				}

				return;
			}


		case formatStringChars:
			{

				// Write char strings

				while (srcIndex < srcLimit)
				{
					String element = data[srcIndex++];

					if (element == null)
					{
						writeLength (stream, -1);
					}
					else
					{
						writeLength (stream, element.length());
						stream.writeChars (element);
					}
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.write");
	}




	// ----- Array Read Functions -----


	// read (DataInput stream, int format, X[] data, int off, int len)
	//
	// Reads array elements from the stream.  Elements are read beginning at
	// data[off].  The number of elements read is len.  The number of bytes
	// read from the stream is len times the size of the specified data
	// format (for fixed-size formats).
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is truncated by discarding high-order
	// bits.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has fewer bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has more bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is sign-extended if a signed format is specified, or
	// zero-extended if an unsigned format is specified.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// For RLE formats, if the end of the array does not coincide with the end
	// of a repetition block, the function throws IODataFormatException.




	public static void read (DataInput stream, int format,
		byte[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Read bytes

				stream.readFully (data, off, len);

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (byte) stream.readShort();
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (byte) stream.readInt();
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read longs

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (byte) stream.readLong();
				}

				return;
			}


		case formatByteRLE:
		case formatByteRLEUnsigned:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					byte nextElement = stream.readByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		char[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (char) stream.readByte();
				}

				return;
			}


		case formatByteUnsigned:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (char) stream.readUnsignedByte();
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (char) stream.readShort();
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (char) stream.readInt();
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read longs

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (char) stream.readLong();
				}

				return;
			}


		case formatByteRLE:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					char nextElement = (char) stream.readByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatByteRLEUnsigned:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					char nextElement = (char) stream.readUnsignedByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatShortRLE:
		case formatShortRLEUnsigned:
			{

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get next data element

					int nextElement = stream.readShort();

					// If high byte is escape code ...

					if ((nextElement & 0xFF00) == escape)
					{

						// Get the repetition limit

						int repLimit = dstIndex + (nextElement & 0xFF) + 1;

						// Range check the repetition

						if (repLimit > dstLimit)
						{
							throw new IODataFormatException ("ArrayIO.read");
						}

						// Get next data element

						nextElement = stream.readShort();

						// Write the repetitions

						do
						{
							data[dstIndex++] = (char) nextElement;

						} while (dstIndex < repLimit);

						// If high byte is escape code, change the escape code

						if ((nextElement & 0xFF00) == escape)
						{
							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}
					}

					// Otherwise, just write a single short

					else
					{

						// Write the element

						data[dstIndex++] = (char) nextElement;
					}

				}	// end loop over data

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		short[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readByte();
				}

				return;
			}


		case formatByteUnsigned:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (short) stream.readUnsignedByte();
				}

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readShort();
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (short) stream.readInt();
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read longs

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (short) stream.readLong();
				}

				return;
			}


		case formatByteRLE:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					short nextElement = stream.readByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatByteRLEUnsigned:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					short nextElement = (short) stream.readUnsignedByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatShortRLE:
		case formatShortRLEUnsigned:
			{

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get next data element

					int nextElement = stream.readShort();

					// If high byte is escape code ...

					if ((nextElement & 0xFF00) == escape)
					{

						// Get the repetition limit

						int repLimit = dstIndex + (nextElement & 0xFF) + 1;

						// Range check the repetition

						if (repLimit > dstLimit)
						{
							throw new IODataFormatException ("ArrayIO.read");
						}

						// Get next data element

						nextElement = stream.readShort();

						// Write the repetitions

						do
						{
							data[dstIndex++] = (short) nextElement;

						} while (dstIndex < repLimit);

						// If high byte is escape code, change the escape code

						if ((nextElement & 0xFF00) == escape)
						{
							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}
					}

					// Otherwise, just write a single short

					else
					{

						// Write the element

						data[dstIndex++] = (short) nextElement;
					}

				}	// end loop over data

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		int[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readByte();
				}

				return;
			}


		case formatByteUnsigned:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readUnsignedByte();
				}

				return;
			}


		case formatShort:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readShort();
				}

				return;
			}


		case formatShortUnsigned:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readUnsignedShort();
				}

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readInt();
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read longs

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (int) stream.readLong();
				}

				return;
			}


		case formatByteRLE:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					int nextElement = stream.readByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatByteRLEUnsigned:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get the repetition limit

					int repLimit = dstIndex + stream.readUnsignedByte() + 1;

					// Get next data element

					int nextElement = stream.readUnsignedByte();

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextElement;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		case formatShortRLE:
			{

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get next data element

					int nextElement = stream.readShort();

					// If high byte is escape code ...

					if ((nextElement & 0xFF00) == escape)
					{

						// Get the repetition limit

						int repLimit = dstIndex + (nextElement & 0xFF) + 1;

						// Range check the repetition

						if (repLimit > dstLimit)
						{
							throw new IODataFormatException ("ArrayIO.read");
						}

						// Get next data element

						nextElement = stream.readShort();

						// Write the repetitions

						do
						{
							data[dstIndex++] = nextElement;

						} while (dstIndex < repLimit);

						// If high byte is escape code, change the escape code

						if ((nextElement & 0xFF00) == escape)
						{
							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}
					}

					// Otherwise, just write a single short

					else
					{

						// Write the element

						data[dstIndex++] = nextElement;
					}

				}	// end loop over data

				return;
			}


		case formatShortRLEUnsigned:
			{

				// Initial escape code, shifted 8 bits

				int escape = shortRLEEscInitial;

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get next data element

					int nextElement = stream.readUnsignedShort();

					// If high byte is escape code ...

					if ((nextElement & 0xFF00) == escape)
					{

						// Get the repetition limit

						int repLimit = dstIndex + (nextElement & 0xFF) + 1;

						// Range check the repetition

						if (repLimit > dstLimit)
						{
							throw new IODataFormatException ("ArrayIO.read");
						}

						// Get next data element

						nextElement = stream.readUnsignedShort();

						// Write the repetitions

						do
						{
							data[dstIndex++] = nextElement;

						} while (dstIndex < repLimit);

						// If high byte is escape code, change the escape code

						if ((nextElement & 0xFF00) == escape)
						{
							escape = (escape + shortRLEEscIncrement) & 0xFF00;
						}
					}

					// Otherwise, just write a single short

					else
					{

						// Write the element

						data[dstIndex++] = nextElement;
					}

				}	// end loop over data

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		long[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readByte();
				}

				return;
			}


		case formatByteUnsigned:
			{

				// Read bytes

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readUnsignedByte();
				}

				return;
			}


		case formatShort:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readShort();
				}

				return;
			}


		case formatShortUnsigned:
			{

				// Read shorts

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readUnsignedShort();
				}

				return;
			}


		case formatInt:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readInt();
				}

				return;
			}


		case formatIntUnsigned:
			{

				// Read ints

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = 0xFFFFFFFFL & (long) stream.readInt();
				}

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read longs

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readLong();
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		float[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Read floats

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readFloat();
				}

				return;
			}


		case formatDouble:
			{

				// Read doubles

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = (float) stream.readDouble();
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		double[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Read floats

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readFloat();
				}

				return;
			}


		case formatDouble:
			{

				// Read doubles

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readDouble();
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		boolean[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatBoolean:
			{

				// Read booleans

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readBoolean();
				}

				return;
			}


		case formatBooleanRLE:
			{

				// Loop over data

				while (dstIndex < dstLimit)
				{

					// Get next data element

					int nextElement = stream.readByte();

					// Get the next boolean

					boolean nextBoolean = ((nextElement & booleanRLEDataMask) != 0);

					// Get the repetition limit

					int repLimit = dstIndex + (nextElement & booleanRLERepMask) + 1;

					// Range check the repetition

					if (repLimit > dstLimit)
					{
						throw new IODataFormatException ("ArrayIO.read");
					}

					// Write the repetitions

					do
					{
						data[dstIndex++] = nextBoolean;

					} while (dstIndex < repLimit);

				}	// end loop over data

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	public static void read (DataInput stream, int format,
		String[] data, int off, int len) throws IOException
	{

		// Validate arguments

		if ((len < 0)
			|| (off < 0)
			|| (off + len > data.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayIO.read");
		}

		// Destination index

		int dstIndex = off;

		// Destination limit

		int dstLimit = off + len;

		// Switch on format

		switch (format)
		{

		case formatStringUTF:
			{

				// Read UTF strings

				while (dstIndex < dstLimit)
				{
					data[dstIndex++] = stream.readUTF();
				}

				return;
			}


		case formatStringBytes:
			{

				// Read byte strings

				while (dstIndex < dstLimit)
				{
					int strLen = readLength (stream);

					if (strLen == -1)
					{
						data[dstIndex++] = null;
					}
					else
					{
						char[] strData = new char[strLen];
						read (stream, formatByteUnsigned, strData, 0, strData.length);
						data[dstIndex++] = new String (strData);
					}
				}

				return;
			}


		case formatStringChars:
			{

				// Read char strings

				while (dstIndex < dstLimit)
				{
					int strLen = readLength (stream);

					if (strLen == -1)
					{
						data[dstIndex++] = null;
					}
					else
					{
						char[] strData = new char[strLen];
						read (stream, formatShortUnsigned, strData, 0, strData.length);
						data[dstIndex++] = new String (strData);
					}
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.read");
	}




	// ----- Element Write Functions -----


	// writeX (DataOutput stream, int format, X data)
	//
	// Writes a single data element to the stream.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte, char, short, int, or long.  In this case, the
	// format can be formatByte, formatShort, formatInt, formatLong, and the
	// unsigned variants of these.  If the specified format has more bits than
	// type X, then the data element is sign-extended if a signed format is
	// specified, or zero-extended if an unsigned format is specified.  If the
	// specified format has fewer bits than type X, then the data element is
	// truncated by discarding high-order bits.
	//
	// 2. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on the data element.
	//
	// 3. Type X can be boolean.  In this case, the format can be
	// formatBoolean.
	//
	// 4. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// Note that these functions duplicate functions that are already available
	// in DataOutput (except for formatStringBytes and formatStringChars).
	// They are provided for convenience.




	public static void writeByte (DataOutput stream, int format,
		byte data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write byte

				stream.writeByte (data);

				return;
			}


		case formatShort:
			{

				// Write short

				stream.writeShort (data);

				return;
			}


		case formatShortUnsigned:
			{

				// Write short

				stream.writeShort (0xFF & data);

				return;
			}


		case formatInt:
			{

				// Write int

				stream.writeInt (data);

				return;
			}


		case formatIntUnsigned:
			{

				// Write int

				stream.writeInt (0xFF & data);

				return;
			}


		case formatLong:
			{

				// Write long

				stream.writeLong ((long) data);

				return;
			}


		case formatLongUnsigned:
			{

				// Write long

				stream.writeLong (0xFFL & (long) data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeByte");
	}




	public static void writeChar (DataOutput stream, int format,
		char data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write byte

				stream.writeByte (data);

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write short

				stream.writeShort (data);

				return;
			}


		case formatInt:
			{

				// Write int

				stream.writeInt ((int)(short) data);

				return;
			}


		case formatIntUnsigned:
			{

				// Write int

				stream.writeInt (0xFFFF & data);

				return;
			}


		case formatLong:
			{

				// Write long

				stream.writeLong ((long)(short) data);

				return;
			}


		case formatLongUnsigned:
			{

				// Write long

				stream.writeLong (0xFFFFL & (long) data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeChar");
	}




	public static void writeShort (DataOutput stream, int format,
		short data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write byte

				stream.writeByte (data);

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write short

				stream.writeShort (data);

				return;
			}


		case formatInt:
			{

				// Write int

				stream.writeInt (data);

				return;
			}


		case formatIntUnsigned:
			{

				// Write int

				stream.writeInt (0xFFFF & data);

				return;
			}


		case formatLong:
			{

				// Write long

				stream.writeLong ((long) data);

				return;
			}


		case formatLongUnsigned:
			{

				// Write long

				stream.writeLong (0xFFFFL & (long) data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeShort");
	}




	public static void writeInt (DataOutput stream, int format,
		int data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write byte

				stream.writeByte (data);

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write short

				stream.writeShort (data);

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Write int

				stream.writeInt (data);

				return;
			}


		case formatLong:
			{

				// Write long

				stream.writeLong ((long) data);

				return;
			}


		case formatLongUnsigned:
			{

				// Write long

				stream.writeLong (0xFFFFFFFFL & (long) data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeInt");
	}




	public static void writeLong (DataOutput stream, int format,
		long data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Write byte

				stream.writeByte ((int) data);

				return;
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Write short

				stream.writeShort ((int) data);

				return;
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Write int

				stream.writeInt ((int) data);

				return;
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Write long

				stream.writeLong (data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeLong");
	}




	public static void writeFloat (DataOutput stream, int format,
		float data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Write float

				stream.writeFloat (data);

				return;
			}


		case formatDouble:
			{

				// Write double

				stream.writeDouble (data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeFloat");
	}




	public static void writeDouble (DataOutput stream, int format,
		double data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Write float

				stream.writeFloat ((float) data);

				return;
			}


		case formatDouble:
			{

				// Write double

				stream.writeDouble (data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeDouble");
	}




	public static void writeBoolean (DataOutput stream, int format,
		boolean data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatBoolean:
			{

				// Write boolean

				stream.writeBoolean (data);

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeBoolean");
	}




	public static void writeString (DataOutput stream, int format,
		String data) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatStringUTF:
			{

				// Write UTF string

				stream.writeUTF (data);

				return;
			}


		case formatStringBytes:
			{

				// Write byte string

				if (data == null)
				{
					writeLength (stream, -1);
				}
				else
				{
					writeLength (stream, data.length());
					stream.writeBytes (data);
				}

				return;
			}


		case formatStringChars:
			{

				// Write char string

				if (data == null)
				{
					writeLength (stream, -1);
				}
				else
				{
					writeLength (stream, data.length());
					stream.writeChars (data);
				}

				return;
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.writeString");
	}




	// ----- Element Read Functions -----


	// readX (DataInput stream, int format)
	//
	// Reads a single data element from the stream, and returns it.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte, char, short, int, or long.  In this case, the
	// format can be formatByte, formatShort, formatInt, formatLong, and the
	// unsigned variants of these.  If the specified format has fewer bits than
	// type X, then the data element is sign-extended if a signed format is
	// specified, or zero-extended if an unsigned format is specified.  If the
	// specified format has more bits than type X, then the data element is
	// truncated by discarding high-order bits.
	//
	// 2. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on the data element.
	//
	// 3. Type X can be boolean.  In this case, the format can be
	// formatBoolean.
	//
	// 4. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// Note that these functions duplicate functions that are already available
	// in DataInput (except for formatStringBytes and formatStringChars).
	// They are provided for convenience.




	public static byte readByte (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
		case formatByteUnsigned:
			{

				// Read byte

				return stream.readByte();
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read short

				return (byte) stream.readShort();
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read int

				return (byte) stream.readInt();
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read long

				return (byte) stream.readLong();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readByte");
	}




	public static char readChar (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read byte

				return (char) stream.readByte();
			}


		case formatByteUnsigned:
			{

				// Read byte

				return (char) stream.readUnsignedByte();
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read short

				return (char) stream.readShort();
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read int

				return (char) stream.readInt();
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read long

					return (char) stream.readLong();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readChar");
	}




	public static short readShort (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read byte

				return stream.readByte();
			}


		case formatByteUnsigned:
			{

				// Read byte

				return (short) stream.readUnsignedByte();
			}


		case formatShort:
		case formatShortUnsigned:
			{

				// Read short
				return stream.readShort();
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read int

				return (short) stream.readInt();
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read long

				return (short) stream.readLong();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readShort");
	}




	public static int readInt (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read byte

				return stream.readByte();
			}


		case formatByteUnsigned:
			{

				// Read byte

				return stream.readUnsignedByte();
			}


		case formatShort:
			{

				// Read short

				return stream.readShort();
			}


		case formatShortUnsigned:
			{

				// Read short

				return stream.readUnsignedShort();
			}


		case formatInt:
		case formatIntUnsigned:
			{

				// Read int

				return stream.readInt();
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read long

				return (int) stream.readLong();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readInt");
	}




	public static long readLong (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatByte:
			{

				// Read byte

				return stream.readByte();
			}


		case formatByteUnsigned:
			{

				// Read byte

				return stream.readUnsignedByte();
			}


		case formatShort:
			{

				// Read short

				return stream.readShort();
			}


		case formatShortUnsigned:
			{

				// Read short

				return stream.readUnsignedShort();
			}


		case formatInt:
			{

				// Read int

				return stream.readInt();
			}


		case formatIntUnsigned:
			{

				// Read int

				return 0xFFFFFFFFL & (long) stream.readInt();
			}


		case formatLong:
		case formatLongUnsigned:
			{

				// Read long

				return stream.readLong();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readLong");
	}




	public static float readFloat (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Read float

				return stream.readFloat();
			}


		case formatDouble:
			{

				// Read double

				return (float) stream.readDouble();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readFloat");
	}




	public static double readDouble (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatFloat:
			{

				// Read float

				return stream.readFloat();
			}


		case formatDouble:
			{

				// Read double

				return stream.readDouble();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readDouble");
	}




	public static boolean readBoolean (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatBoolean:
			{

				// Read boolean

				return stream.readBoolean();
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readBoolean");
	}




	public static String readString (DataInput stream, int format) throws IOException
	{

		// Switch on format

		switch (format)
		{

		case formatStringUTF:
			{

				// Read UTF string

				return stream.readUTF();
			}


		case formatStringBytes:
			{

				// Read byte string

				int strLen = readLength (stream);

				if (strLen == -1)
				{
					return null;
				}
				else
				{
					char[] strData = new char[strLen];
					read (stream, formatByteUnsigned, strData, 0, strData.length);
					return new String (strData);
				}
			}


		case formatStringChars:
			{

				// Read char string

				int strLen = readLength (stream);

				if (strLen == -1)
				{
					return null;
				}
				else
				{
					char[] strData = new char[strLen];
					read (stream, formatShortUnsigned, strData, 0, strData.length);
					return new String (strData);
				}
			}


		}	// end switch on format

		throw new IllegalArgumentException ("ArrayIO.readString");
	}




	// ----- One-Dimensional Array Write Functions -----


	// writeX1D (DataOutput stream, int format, X[] data)
	//
	// Writes an entire one-dimensional array to the stream.  This function
	// first writes the array length, and then writes all the elements of the
	// array.  If the array is null, this function just writes a length of -1.
	//
	// The array length is written using 2 or 4 bytes;  see writeLength() for
	// details of the format.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is sign-extended if a signed format is
	// specified, or zero-extended if an unsigned format is specified.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has more bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has fewer bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is truncated by discarding high-order bits.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.




	public static void writeByte1D (DataOutput stream, int format,
		byte[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeChar1D (DataOutput stream, int format,
		char[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeShort1D (DataOutput stream, int format,
		short[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeInt1D (DataOutput stream, int format,
		int[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeLong1D (DataOutput stream, int format,
		long[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeFloat1D (DataOutput stream, int format,
		float[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeDouble1D (DataOutput stream, int format,
		double[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeBoolean1D (DataOutput stream, int format,
		boolean[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}




	public static void writeString1D (DataOutput stream, int format,
		String[] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write array

		write (stream, format, data, 0, data.length);

		return;
	}





	// ----- One-Dimensional Array Read Functions -----


	// readX1D (DataInput stream, int format)
	//
	// Reads an entire one-dimensional array from the stream.  This function
	// first reads the array length, then creates the array, then reads all the
	// elements of the array, and returns the resulting array.  If the length
	// is -1, this function returns null.
	//
	// The array length is read from 2 or 4 bytes;  see writeLength() for
	// details of the format.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is truncated by discarding high-order
	// bits.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has fewer bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has more bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is sign-extended if a signed format is specified, or
	// zero-extended if an unsigned format is specified.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// For RLE formats, if the end of the array does not coincide with the end
	// of a repetition block, the function throws IODataFormatException.




	public static byte[] readByte1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		byte[] result = new byte[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static char[] readChar1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		char[] result = new char[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static short[] readShort1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		short[] result = new short[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static int[] readInt1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		int[] result = new int[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static long[] readLong1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		long[] result = new long[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static float[] readFloat1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		float[] result = new float[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static double[] readDouble1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		double[] result = new double[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static boolean[] readBoolean1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		boolean[] result = new boolean[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	public static String[] readString1D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		String[] result = new String[len];

		// Read array

		read (stream, format, result, 0, len);

		// Return result

		return result;
	}




	// ----- Two-Dimensional Array Write Functions -----


	// writeX2D (DataOutput stream, int format, X[][] data)
	//
	// Writes an entire two-dimensional array to the stream.  This function
	// first writes the array length, and then writes all the second-level
	// arrays (using writeX1D()).  If the array is null, this function just
	// writes a length of -1.
	//
	// The array length is written using 2 or 4 bytes;  see writeLength() for
	// details of the format.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is sign-extended if a signed format is
	// specified, or zero-extended if an unsigned format is specified.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has more bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has fewer bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is truncated by discarding high-order bits.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// Note that for RLE formats, the run-length-coding is restarted at the
	// beginning of each second-level array.  In particular, repetition blocks
	// may not span second-level array boundaries.




	public static void writeByte2D (DataOutput stream, int format,
		byte[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeByte1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeChar2D (DataOutput stream, int format,
		char[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeChar1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeShort2D (DataOutput stream, int format,
		short[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeShort1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeInt2D (DataOutput stream, int format,
		int[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeInt1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeLong2D (DataOutput stream, int format,
		long[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeLong1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeFloat2D (DataOutput stream, int format,
		float[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeFloat1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeDouble2D (DataOutput stream, int format,
		double[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeDouble1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeBoolean2D (DataOutput stream, int format,
		boolean[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeBoolean1D (stream, format, data[i]);
		}

		return;
	}




	public static void writeString2D (DataOutput stream, int format,
		String[][] data) throws IOException
	{

		// Handle null array

		if (data == null)
		{
			writeLength (stream, -1);
			return;
		}

		// Write array length

		writeLength (stream, data.length);

		// Write second-level arrays

		for (int i = 0; i < data.length; ++i)
		{
			writeString1D (stream, format, data[i]);
		}

		return;
	}




	// ----- Two-Dimensional Array Read Functions -----


	// readX2D (DataInput stream, int format)
	//
	// Reads an entire two-dimensional array from the stream.  This function
	// first reads the array length, then creates the array, then reads all the
	// second-level arrays (using readX1D()), and returns the resulting array.
	// If the length is -1, this function returns null.
	//
	// The array length is read from 2 or 4 bytes;  see writeLength() for
	// details of the format.
	//
	// This function is defined for the following argument types:
	//
	// 1. Type X can be byte.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, formatByteRLE, and the unsigned
	// variants of these.  If the specified format has more than 8 bits, then
	// the value of each array element is truncated by discarding high-order
	// bits.
	//
	// 2. Type X can be char, short, or int.  In this case, the format can be
	// formatByte, formatShort, formatInt, formatLong, formatByteRLE,
	// formatShortRLE, and the unsigned variants of these.  If the specified
	// format has fewer bits than type X, then the value of each array element
	// is sign-extended if a signed format is specified, or zero-extended if an
	// unsigned format is specified.  If the specified format has more bits
	// than type X, then the value of each array element is truncated by
	// discarding high-order bits.
	//
	// 3. Type X can be long.  In this case, the format can be formatByte,
	// formatShort, formatInt, formatLong, and the unsigned variants of these.
	// If the specified format has fewer than 64 bits, then the value of each
	// array element is sign-extended if a signed format is specified, or
	// zero-extended if an unsigned format is specified.
	//
	// 4. Type X can be float or double.  In this case, the format can be
	// formatFloat or formatDouble.  If the specified format has a different
	// number of bits than type X, then a floating-point conversion is
	// performed on each array element.
	//
	// 5. Type X can be boolean.  In this case, the format can be formatBoolean
	// or formatBooleanRLE.
	//
	// 6. Type X can be String.  In this case, the format can be
	// formatStringUTF, formatStringBytes, or formatStringChars.
	//
	// For RLE formats, if the end of the array does not coincide with the end
	// of a repetition block, the function throws IODataFormatException.
	//
	// Note that for RLE formats, the run-length-coding is restarted at the
	// beginning of each second-level array.  In particular, repetition blocks
	// may not span second-level array boundaries.




	public static byte[][] readByte2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		byte[][] result = new byte[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readByte1D (stream, format);
		}

		// Return result

		return result;
	}




	public static char[][] readChar2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		char[][] result = new char[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readChar1D (stream, format);
		}

		// Return result

		return result;
	}




	public static short[][] readShort2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		short[][] result = new short[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readShort1D (stream, format);
		}

		// Return result

		return result;
	}




	public static int[][] readInt2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		int[][] result = new int[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readInt1D (stream, format);
		}

		// Return result

		return result;
	}




	public static long[][] readLong2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		long[][] result = new long[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readLong1D (stream, format);
		}

		// Return result

		return result;
	}




	public static float[][] readFloat2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		float[][] result = new float[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readFloat1D (stream, format);
		}

		// Return result

		return result;
	}




	public static double[][] readDouble2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		double[][] result = new double[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readDouble1D (stream, format);
		}

		// Return result

		return result;
	}




	public static boolean[][] readBoolean2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		boolean[][] result = new boolean[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readBoolean1D (stream, format);
		}

		// Return result

		return result;
	}




	public static String[][] readString2D (DataInput stream, int format) throws IOException
	{

		// Read length

		int len = readLength (stream);

		// If -1, return a null array

		if (len == -1)
		{
			return null;
		}

		// Allocate the array

		String[][] result = new String[len][];

		// Read second-level arrays

		for (int i = 0; i < len; ++i)
		{
			result[i] = readString1D (stream, format);
		}

		// Return result

		return result;
	}




}

