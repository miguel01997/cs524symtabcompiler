// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import invisible.jacc.parse.InterruptedCompilerException;

import invisible.jacc.util.ArrayIO;
import invisible.jacc.util.IntSet;
import invisible.jacc.util.IODataFormatException;
import invisible.jacc.util.UnaryIntPredicate;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

import java.util.Enumeration;


/*->

  class CharCategoryTable

  An object of this class holds an array of bytes which contains a character
  category (0-255) for each character in the character set.  Typically the
  length of the array is 256 bytes for the ASCII character set, or 65536 bytes
  for the Unicode character set.  However, any length is allowed.

  The character category is used as an index into the transition table of a
  finite automaton.  The idea is that characters which always cause the same
  transitions are placed into the same category.  The lexical analyzers for
  real programming languages always have fewer than 256 categories.

  An accessor function provides direct access to the array of bytes.  Clients
  should treat the array as read-only.

->*/


public final class CharCategoryTable 
{

	// Data stream signature

	public static final long streamSignature = 0x4953FF0043433031L;


	// Table that contains the category for each character

	private byte[] _categoryTable;

	public final byte[] categoryTable ()
	{
		return _categoryTable;
	}

	public final int tableSize ()
	{
		return _categoryTable.length;
	}


	// The number of different categories in this table

	private int _categoryCount;

	public final int categoryCount ()
	{
		return _categoryCount;
	}


	// The size of common character sets

	public static final int charSetSizeASCII = 0x100;

	public static final int charSetSizeUnicode = 0x10000;




	// The constructor with no arguments creates an object with no table

	public CharCategoryTable ()
	{
		super ();

		// Empty category table

		_categoryTable = new byte[0];
		_categoryCount = 0;

		// Done

		return;
	}




	// Write the category table to a data stream

	public void writeToStream (DataOutput stream) throws IOException
	{

		// Write the table signature

		stream.writeLong (streamSignature);

		// Write the number of categories

		stream.writeInt (_categoryCount);

		// Write the character category table

		ArrayIO.writeByte1D (stream, ArrayIO.formatByteRLE, _categoryTable);

		// Done writing stream

		return;
	}



	
	// Read the category table from a data stream

	public void readFromStream (DataInput stream) throws IOException
	{

		// Read the character category table signature

		long inputSignature = stream.readLong ();

		if (inputSignature != streamSignature)
		{
			throw new IODataFormatException (
				"CharCategoryTable.readFromStream: Invalid signature");
		}

		// Read the number of categories

		_categoryCount = stream.readInt ();

		// Read the character category table

		_categoryTable = ArrayIO.readByte1D (stream, ArrayIO.formatByteRLE);

		// Done reading stream

		return;
	}




	// This function checks the object to make sure that its contents are
	// internally consistent.
	//
	// If the contents are consistent, the function returns null.
	//
	// If any inconsistency is found, the function returns a String describing
	// the problem.

	public String checkInvariant ()
	{

		// Check for missing category table

		if (_categoryTable == null)
		{
			return "Missing category table";
		}

		// Check category count

		if (
			   (
				   (_categoryTable.length == 0)
				&& (_categoryCount != 0)
			   )
			|| (
				   (_categoryTable.length > 0)
				&& ((_categoryCount <= 0) || (_categoryCount > 256))
			   )
		   )
		{
			return "Invalid category count";
		}

		// Check category table contents

		for (int i = 0; i < _categoryTable.length; ++i)
		{
			if ((_categoryTable[i] & 0xFF) >= _categoryCount)
			{
				return "Invalid category table entry";
			}
		}

		// Success

		return null;
	}




	// Calculates the category table for a set of character groups.  The
	// groups are passed in as an array of UnaryIntPredicate objects.  Each
	// predicate object returns true for each character in the group.
	//
	// The charSetSize argument specifies the number of characters in the
	// character set.  Characters are represented as integers ranging from 0 to
	// charSetSize-1.  Typically, this is 256 for ASCII characters or 65536 for
	// Unicode characters.
	//
	// The function returns an array of IntSet objects, each of which contains
	// the categories that comprise the group.  The function returns null if
	// there were too many categories.
	//
	// The optional GeneratorStatus parameter can be used to monitor the progress
	// of, and send interrupts to, this function.  If the function is
	// interrupted, the CharCategroyTable object is in an indeterminate state.

	public IntSet[] calculateCategories (UnaryIntPredicate[] charGroups, int charSetSize)
	{
		try
		{
			return calculateCategories (charGroups, charSetSize, null);
		}
		catch (InterruptedCompilerException e)
		{
			throw new InternalError();
		}
	}
	

	public IntSet[] calculateCategories (UnaryIntPredicate[] charGroups, int charSetSize,
			GeneratorStatus generatorStatus) throws InterruptedCompilerException
	{

		// Validate the arguments

		if (charGroups == null)
		{
			throw new NullPointerException ("CharCategoryTable.calculateCategories");
		}

		if (charSetSize <= 0)
		{
			throw new IllegalArgumentException ("CharCategoryTable.calculateCategories");
		}

		// Allocate an array to hold the character categories

		IntSet[] categorySet = new IntSet[charGroups.length];

		// Allocate an empty set for each group

		for (int i = 0; i < categorySet.length; ++i)
		{
			categorySet[i] = new IntSet ();
		}

		// A table used to indicate which categories are in the current group:
		// 0 = unknown, 1 = in group, 2 = not in group
		 
		int[] categoryStatus = new int[256];

		// A table used to indicate which categories are being split:
		// -1 = not split, other = new category
		 
		int[] categorySplits = new int[256];

		// Category for the current character

		int category = 0;

		// Status for the current character

		int status = 0;

		// Allocate the category table

		_categoryTable = new byte[charSetSize];

		// One category

		_categoryCount = 1;

		// Initialize table to category zero

		for (int i = 0; i < _categoryTable.length; ++i)
		{
			_categoryTable[i] = 0;
		}

		// Loop over all character groups

		for (int group = 0; group < charGroups.length; ++group)
		{
			
			// Report progress
			
			if (generatorStatus != null)
			{
				generatorStatus.statusWork ();
			}
			
			// Initialize tables to unknown status, not split

			for (int i = 0; i < _categoryCount; ++i)
			{
				categoryStatus[i] = 0;
				categorySplits[i] = -1;
			}

			// Scan all possible characters
			 
			for (int ch = 0; ch < _categoryTable.length; ++ch)
			{

				// Check if character is in group

				if (charGroups[group].value (ch))
				{
					status = 1;
				}
				else
				{
					status = 2;
				}

				// Get current character category

				category = _categoryTable[ch] & 0xFF;

				// Check for unknown status

				if (categoryStatus[category] == 0)
				{

					// Save the category status

					categoryStatus[category] = status;

					// If character is in group, add category to group's set

					if (status == 1)
					{
						categorySet[group].addElement (category);
					}
				}

				// Check for incorrect status

				if (categoryStatus[category] != status)
				{

					// If not split, then split the category

					if (categorySplits[category] == -1)
					{

						// Check for too many categories

						if (_categoryCount == 256)
						{

							// Dump the category table

							_categoryTable = new byte[0];
							_categoryCount = 0;

							// Return error

							return null;
						}

						// Split the category

						int newCategory = _categoryCount++;

						categorySplits[category] = newCategory;

						// If current group contains character, add new category

						if (status == 1)
						{
							categorySet[group].addElement (newCategory);
						}

						// Loop over all groups already processed

						for (int i = 0; i < group; ++i)
						{

							// If the prior group contains this category

							if (categorySet[i].isElement (category))
							{

								// Add the new category

								categorySet[i].addElement (newCategory);
							}
						}
					}

					// Change character to the new category

					_categoryTable[ch] = (byte) categorySplits[category];
				}

			}	// end loop over characters

		}	// end loop over groups

		// Compact the sets

		for (int i = 0; i < categorySet.length; ++i)
		{
			categorySet[i].compact();
		}

		// Return success

		return categorySet;
	}




	// Converts the category table to a string.  Overrides the toString
	// method of class Object.  This is used mainly for debugging.

	public String toString ()
	{

		// Get a StringBuffer to use for constructing the string

		StringBuffer buf = new StringBuffer ();

		// Start with a left brace

		buf.append ("{");

		// Loop over table, writing run length encoded entries

		for (int index = 0; index < _categoryTable.length; )
		{

			// Save current index and category

			int oldIndex = index;
			byte oldCategory = _categoryTable[index];

			// Advance index until category changes, or end of table

			while (++index < _categoryTable.length)
			{

				// If category changed, break out of loop

				if (_categoryTable[index] != oldCategory)
				{
					break;
				}
			}

			// If this isn't the first run, append a comma

			if (oldIndex != 0)
			{
				buf.append (", ");
			}

			// Append the start of the run, in hex

			buf.append (Integer.toHexString(oldIndex).toUpperCase());

			// If the run length is longer than 1 ...

			if ((index - oldIndex) > 1)
			{

				// Append a range indicator

				buf.append ("..");

				// Append the end of the run, in hex

				buf.append (Integer.toHexString(index-1).toUpperCase());
			}

			// Append an equal sign

			buf.append ("=");

			// Append the category

			buf.append (((int) oldCategory) & 0xFF);
		}

		// End with a right brace

		buf.append ("}");

		// Return resulting string

		return buf.toString ();
	}


}

