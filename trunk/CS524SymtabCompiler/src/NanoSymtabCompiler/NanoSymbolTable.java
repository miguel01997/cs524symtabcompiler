package NanoSymtabCompiler;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

//
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//				NANO SYMBOL TABLE MAIN CLASS
//			(final inner support classes below)
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//
public class NanoSymbolTable 
{
	//Some constants that are useful
	public static final int UNK_TYPE = -1;
	public static final int INT_TYPE = 0;
	public static final int BOOL_TYPE = 1;
	public static final int INT_ARRAY_TYPE = 2;
	public static final int BOOL_ARRAY_TYPE = 3;
	public static final int PROC_TYPE = 4;
		 
	
	//Utility class included here for proximity to original constant names
	//Used in verbose output for tracing contents of symbol table
	public static final String getTypeName(int type)
	{
		String result = null;
		if (type==INT_TYPE) result = "integer";
		else if (type==BOOL_TYPE) result = "boolean";
		else if (type==INT_ARRAY_TYPE) result = "integer array";
		else if (type==BOOL_ARRAY_TYPE) result = "boolean array";
		else if (type==PROC_TYPE) result = "procedure";
		else if (type==UNK_TYPE) result = "unknown type (error)";
		return result;
	}
	
	//For narrating symbol table behavior
	private boolean verbose;
	private int verbosityBlockCount = 0; //just for making it easy to narrate the verbose symtab
	
	//Main instance variables of the chained hash-block symbol table architecture
	private Stack blockEntryStack; //this is the stack of entries collected together
	 							   //according to lexical blocks in the source code
	private int stackTopOffset;	//this is what acts as an actual 
								//address into the RunTime Memory model
	private NSTBlockEntry currentBlock;
								//Points to entry for local variables for lexical context 
								//currently being parsed; include as inner final class below 

	//Utility support for IdLists
	private int currTempNum;
	private ArrayList tempIdList;
	
	//Constructors
	//Default
	public NanoSymbolTable()
	{  new NanoSymbolTable(false); } //In case someone wants to call it with no params
	
	//Primary constructor
	public NanoSymbolTable(boolean verbose)
	{
		blockEntryStack = new Stack();
		stackTopOffset = 0;
		currentBlock = null;
		currTempNum = 0;
		tempIdList = new ArrayList();
		this.verbose = verbose; 
		if (this.verbose)
		{
			System.out.println("\n\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println("Nano Symbol Table (NST) Verbose Output...");
			System.out.println("Every action of the symbol table will be narrated;\n"+
							  "to change this rerun SymbolTable driver or Compiler with verbose variable set to false\n");
			System.out.println("Most variables can be understood by the informed reader");
			System.out.println("For questions refer to class discussions or mailing list or see instructor");
			System.out.println("Despite the structural mechanisms (i.e. hash tables per lexical \"block\" of code, chained together,");
			System.out.println("...the main variable of interest is the stackTopOffset which is the next address to be assigned to any");
			System.out.println("variable, constant or array or internally generated temporary variable");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\n");
			System.out.println("NST constructor executed...");
			System.out.println("Created empty symbol table:");
			System.out.println("-->Has empty blockEntryStack, currentBlock = null, stackTopOffset = 0\n\n");
		}
	}

	/*
	 * Method to provide access to current stackTopOffset for use in PARM quads
	 */
	public int getStackTopOffset() { return stackTopOffset; }
	
	/*
	 * Method to call to initialize a new block in the chain corresponding to a new lexical scope
	 * Should always be paired with a call to endCurrentBlock when the same scope reaches closure
	 */
	public void startNewBlock()
	{
		verbosityBlockCount++;
		currentBlock = new NSTBlockEntry(stackTopOffset,currentBlock,verbosityBlockCount);
		blockEntryStack.push(currentBlock);
		if (verbose)
		{
			System.out.println("	=> startNewBlock() called");
			System.out.println("	   Starting new symbol table block:");
			System.out.println("	   Block count now: "+verbosityBlockCount);
			System.out.println("	   Current stackTopOffset: "+stackTopOffset+"\n");
		}
	}

	/*
	 * Method to call to remove a current block in the chain corresponding to a lexical scope
	 * that is ending. Should always be paired with a prior call to startCurrentBlock for the same scope lexical scope
	 */
	public void endCurrentBlock()
	{
		verbosityBlockCount--;
		NSTBlockEntry be = (NSTBlockEntry) blockEntryStack.pop();
		stackTopOffset -= be.lengthOfEntries();
		if (verbose)
		{
			System.out.println("	=> endCurrentBlock() called");
			System.out.println("	   Releasing a closed symbol table block (closed lexical context):");
			System.out.println("	   Block count now: "+verbosityBlockCount);
			System.out.println("	   Current stackTopOffset: "+stackTopOffset+"\n");
		}
	}
	
	/*
	 * Method to add a declared scalar to the curent block; applicable for integers and booleans
	 */
	
	public NSTIndScalarEntry
	addScalarToCurrentBlock(String name, int type)
	{
		NSTIndScalarEntry e;
		if (!currentBlock.entries.containsKey(name))
		{
			e = currentBlock.put(name, type, false);
			stackTopOffset++;
			if (verbose)
			{
				System.out.println("	=> addScalarToCurrentBlock() called (as during a declaration)");
				System.out.println("	   (This one is for scalars of either integer or boolean type");
				System.out.println("	   Adding identifier to block "+verbosityBlockCount+":");
				System.out.println("	   Name: "+name);
				System.out.println("	   Type: "+getTypeName(type));
				System.out.println("	   StackTopOffset (address+1): "+stackTopOffset+"\n");
			}
			return e;
		}
		else
		{
			return null;
		}	
	}


	/*
	 * Method to add a declared constant variable (no arrays or booleans) to the curent block
	 * Recall that constants get runtime memory assignments just like variables but have
	 * a special flag set so that the compiler can ensure that no instructions modify those values
	 */
	public NSTIndScalarEntry addConstIntToCurrentBlock(String name)
	{
		NSTIndScalarEntry e;
		if (!currentBlock.entries.containsKey(name))
		{
			e = currentBlock.put(name, NanoSymbolTable.INT_TYPE, true);
			stackTopOffset++;
			if (verbose)
			{
				System.out.println("	=> addConstToCurrentBlock() called (as during a declaration)");
				System.out.println("	   (This one is for integer constants only, since the lexemes \n" +
								   "           true and false serve as their own constants");
				System.out.println("	   (Recall the symbol table is generally not around at runtime \n" +
						           "           so we must generate memory for these constants");
				System.out.println("	   Adding identifier to block "+verbosityBlockCount+":");
				System.out.println("	   Name: "+name);
				System.out.println("	   Type: "+getTypeName(NanoSymbolTable.INT_TYPE));
				System.out.println("	   StackTopOffset (address+1): "+stackTopOffset+"\n");
			}
			return e;
		}
		else
		{
			return null;
		}	
	}

	/*
	 * Method to add a declared array variable to the curent block; applicable for integers and booleans
	 */
	public NSTIndArrayEntry addArrayToCurrentBlock(String name, int type, int size)
	{
		NSTIndArrayEntry e;
		if (!currentBlock.entries.containsKey(name))
		{
			e = currentBlock.put(name, type, size);
			stackTopOffset += size;
			if (verbose)
			{
				System.out.println("	=> addArrayToCurrentBlock() called (as during an array declaration)");
				System.out.println("	   (This one is for array variables of either integer or boolean type");
				System.out.println("	   Adding array identifier to block "+verbosityBlockCount+":");
				System.out.println("	   Name: "+name);
				System.out.println("	   Type: "+getTypeName(type));
				System.out.println("	   Size: "+size);
				System.out.println("	   StackTopOffset (starting address + size + 1): "+stackTopOffset+"\n");
			}
			return e;
		}
		else
		{
			return null;
		}
	}
			
	
	/*
	 * Truthfully in Nano all procedures are declared before the first block but all variables 
	 * declared there are considered visible inside that main block so we do the same here
	 * with all the procedures. This has the side-effect of being easy to modify, should Nano
	 * ever evolve toward having procedures declared inside inner blocks.
	 */
	public NSTIndProcEntry addProcedureToCurrentBlock
		(String name, int numParams, int startQuad, int endQuad)
	{
		NSTIndProcEntry e;
		if (!currentBlock.entries.containsKey(name))
		{
			e = currentBlock.put(name, numParams, startQuad, endQuad);
			//Procedures use space on the stack, but dynamically when they are called
			//For now this information is just kept in the symbol table for ease of access
			//in generating and referring to the quads that make up a procedure
			if (verbose)
			{
				System.out.println("	=> addProcedureToCurrentBlock() called (as during a declaration)");
				System.out.println("	   (This really doesn't take up stack space, since the space\n" +
								   "        gets demanded on call, but is kept here for ease of reference to quads");
				System.out.println("	   Adding identifier to block "+verbosityBlockCount+":");
				System.out.println("	   Name: "+name);
				System.out.println("	   Type: "+getTypeName(NanoSymbolTable.PROC_TYPE)+"\n");
			}
			return e;
		}
		else
		{
			return null;
		}
	}
			
	public NSTIndScalarEntry addNewTempToCurrentBlock(int type)
	{
		String name = getNewTempName();
		NSTIndScalarEntry e = addScalarToCurrentBlock(name,type);
		tempIdListAdd(name);
		return e;
	}
				
	/*
	 *  Method to lookup an identifier at the main symbol table level,
	 *  which triggers the local hash table checks and the walks up the chain.
	 *  Returns an instance of the NanoSymbolTableIndividualEntry, a final inner class 
	 *  defined below which has in it all necessary information for participating
	 *  in synthesized attributes. 
	 *  !!Returns null if entry is nowhere in the symbol table!!
	 */
	public NSTIndEntry get(String name)
	{
		NSTBlockEntry blockEntry = currentBlock;
		NSTIndEntry entry = null;
		while (blockEntry != null)
		{
			entry = blockEntry.get(name);
			if (entry != null) break;
			blockEntry = blockEntry.beneath;
		}
		return entry;
	}
	
	/*
	 * Workhorse of the tracing of the symbol table. 
	 * Keep in mind that all this machinery is mostly for the compiler developer,
	 * to aid in the process of constructing a correct compiler that uses the symtab appropriately.
	 */
	public void showContents()
	{
		//Not controlled by verbose so contents can be tracked without all the other
			Iterator besi = blockEntryStack.iterator();
			System.out.println("Nano Symbol Table =================>\n\n");
			while (besi.hasNext())
			{
				System.out.println(besi.next());
			}
			System.out.println("=======================================\n\n\n");

	}
			
	public void tempIdListClear()
	{
		tempIdList = new ArrayList();
	}

	
	/*
	 * Once one or more temps have been generated for some subtree of the parse
	 * it is helpful to have this iterator to spit them back out for use in quads.
	 * Also, this is used by the repeated reductions by a production that is grabbing
	 * one in a sequence of definitions such as x,y,z : integer
	 */
	public Iterator getTempIdListIterator()
	{
		return tempIdList.iterator();
	}

	/*Private service method triggered by "addNewTempToCurrentBlock".
	 *At end of a block the list of temporary variables and 
	 *their names must be reset by another method.
	 */
	private String getNewTempName()
	{
		String name = "T"+(new Integer(currTempNum)).toString();
		currTempNum++;
		return name;
	}

	/*
	 * Service method triggered by "addNewTempToCurrentBlock" or as described above in iterator.
	 * When in need of a temporary variable the symbol table keeps them for the 
	 * compiler write in this array list and this method is the workhorse
	 * for that task.
	 */
	public void tempIdListAdd(String identifier)
	{
		tempIdList.add(identifier);
	}
	
//
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//				NANO SYMBOL TABLE BLOCK ENTRY CLASS
//			 (final inner support class for the above)
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//
	
public final class NSTBlockEntry 
{
	//Main instance variables	
	private int blockOffset;	 //What is the base address of this block on its creation
	private int entryOffset;	 //What is the offset within the block of the individual entry
	private Hashtable entries;	 //The actual hashtable of entries
	private NSTBlockEntry beneath; //The pointer to the one "under it" in the stack
	private int verbosityBlockCount;
	
	/*
	 * Constructor for making a new BLOCK
	 * Must know the address where the block will start and a pointer to the block beneath it
	 * (null the first time around)
	 */
	public NSTBlockEntry(int blockOffset, NSTBlockEntry beneath, int vbc)
	{
		this.blockOffset = blockOffset; 	//Obtained from stack of run-time memory
		this.beneath = beneath;				//Used for iterating top to bottom, since iterator doesn't
		this.entryOffset = 0;
		this.entries = new Hashtable();		//Typically so small the default should be fine
		this.verbosityBlockCount = vbc;			//For tracking for the user...
	}
	
	/*
	 * The block level version of "get" by name an entry--gets passed to the get on the hashtable
	 * Again, returns null if not found
	 */
	public NSTIndEntry get(String name)
	{
		return (NSTIndEntry) entries.get(name);
	}

	/*
	 * For scalars the compiler need only give the name and type and constant status
	 * The address is calculated by the symbol table 
	 */
	public NSTIndScalarEntry put(String name, int type, boolean isConstant)
	{
		NSTIndScalarEntry e = 
			new NSTIndScalarEntry		
					(name,type,isConstant,blockOffset+entryOffset); 
		entries.put(name, e);
		entryOffset++;
		return e;
	}	
	

	/*
	 * For arrays of either integer or boolean the compiler need give name, type and size
	 * The rest of the addressing information is done by the symbol table
	 */
	public NSTIndArrayEntry put(String name, int type, int size)
	{
		NSTIndArrayEntry e =
			new NSTIndArrayEntry(name,type,false,blockOffset+entryOffset,size);
		entries.put(name, e);
		entryOffset+=size;
		return e;
	}
	
	/*
	 * For procedures the compiler need give name, number of parameters and start and end quads
	 */
	public NSTIndProcEntry put(String name, int numParams, int startQuad, int endQuad)
	{
		NSTIndProcEntry e =
			new NSTIndProcEntry(name,NanoSymbolTable.PROC_TYPE,true,numParams,startQuad,endQuad);
		entries.put(name, e);
		return e;
	}
	
	/*
	 * Used to subtract off free space from the runtime stack
	 */
	private int lengthOfEntries()
	{
		return entryOffset;
	}

	/*
	 * Easy way to get block contents in printable form
	 */
	public String toString() 
	{

			String result = "			---------SymTab Block Entry----------\n" +
							"			This is block number " + verbosityBlockCount + "\n" +
							"			BlockOffset: " + blockOffset + "\n\n";
			Set eks = entries.keySet();
			Iterator eksi = eks.iterator();
			while (eksi.hasNext())
			{
				result += entries.get(eksi.next()).toString();
			}
			result += "			-----------end of SymTab Block Entry-------------";
			return result;
	}

}


//
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//				NANO SYMBOL TABLE INDIVIDUAL ENTRY CLASS (and subclasses)
//			         (final inner support classes for the above)
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//

public class NSTIndEntry
{
	//All the things we want to know about an identifier in the symbol table
	private String name;
	private int actualType;		//Must be one of INT_TYPE, BOOL_TYPE, 
								//INT_ARRAY_TYPE, BOOL_ARRAY_TYPE, or PROC_TYPE
	private boolean isConstant = false;	//Again to be overridden if otherwise

	public NSTIndEntry
	(String name, int type, boolean isConstant)
	{
		this.name = name;
		this.actualType = type;
		this.isConstant =  isConstant;
	}

	public boolean isInteger() { return actualType == INT_TYPE; }
	public boolean isBoolean() { return actualType == BOOL_TYPE; }
	public boolean isConstant() { return isConstant; }
	public boolean isBooleanArray() { return actualType == BOOL_ARRAY_TYPE; }
	public boolean isIntArray() { return actualType == INT_ARRAY_TYPE; }
	public boolean isProcedure() { return actualType == PROC_TYPE; }
	public int getActualType() { return actualType; }
	public String getName() { return name; }

	public String toString()
	{
			String actType =(actualType==INT_TYPE)?"integer":
				( (actualType==BOOL_TYPE)?"boolean":
					( (actualType==INT_ARRAY_TYPE)?"integer array":
						( (actualType==BOOL_ARRAY_TYPE?"boolean array":
							( (actualType==PROC_TYPE)?"procedure":"unknown")))));

			return
				"\n			Symbol table individual entry: \n" +
				"  			Name: " + name + "\n" +
				"  			IsConstant?: " + isConstant + "\n" +
				"			Type: " + actType + "\n";
	}
}	

public final class NSTIndScalarEntry 
extends NSTIndEntry
{
	private int address;
	
	public NSTIndScalarEntry
	(String name, int type, boolean isConstant, int address)
	{
		super(name,type,isConstant);
		this.address = address;
	}
	
	public int getAddress() { return address; }
	
	public String toString() 
	{
			return
			super.toString() +
			"  			Address: " + address + "\n\n";
	}
}

public final class NSTIndArrayEntry 
extends NSTIndEntry
{
	private int address;
	private int size;
	
	public NSTIndArrayEntry
	(String name, int type, boolean isConstant, int address, int size)
	{
		super(name,type,isConstant);
		this.address = address;
		this.size = size;
	}
	
	public int getAddress() { return address; }
	public int getSize() {return size; }
	
	public String toString() 
	{
			return
			super.toString()  +
			"			Address: " + address + "\n" +
			"			Size:	 " + size + "\n\n";
	}
}

public final class NSTIndProcEntry 
extends NSTIndEntry
{
	private int numberInputParamSlots;
	private int startQuadNumber;
	private int endQuadNumber;

	public NSTIndProcEntry
	(String name, int type, boolean isConstant, int numberInputParamSlots, 
			int startQuadNumber, int endQuadNumber)
	{
		super(name,type,isConstant);
		this.numberInputParamSlots = numberInputParamSlots;
		this.startQuadNumber = startQuadNumber;	
		this.endQuadNumber = endQuadNumber;
	}

	public int getNumInputs() { return numberInputParamSlots; }
	public int getStartQuadNumber() { return startQuadNumber; }
	public int getEndQuadNumber() { return endQuadNumber; }
	
	public String toString() 
	{
			return
			super.toString()  +
			" Number Input Parameters: " + numberInputParamSlots + "\n" +
			"	    Start Quad Number: " + startQuadNumber + 
			"         End Quad Number: " + endQuadNumber + "\n\n";
	}
	

}



	



public static void main(String[] args)
{
	//The following sequence of interactions with the symbol table correspond 
	//roughly to the Nano input below
	NanoSymbolTable symtab = new NanoSymbolTable(true);
	
	symtab.startNewBlock();
	symtab.addConstIntToCurrentBlock("const1");
	symtab.addScalarToCurrentBlock("var1", NanoSymbolTable.INT_TYPE);
	symtab.addScalarToCurrentBlock("var2", NanoSymbolTable.INT_TYPE);
	
	//symtab.lookup("var2");
	
	symtab.showContents();	
	
	
	symtab.startNewBlock();
	symtab.addConstIntToCurrentBlock("newconst1");
	symtab.addScalarToCurrentBlock("newboolvar2", NanoSymbolTable.BOOL_TYPE);
	symtab.addArrayToCurrentBlock("newarrayvarofint1",NanoSymbolTable.INT_ARRAY_TYPE,5);
	symtab.addArrayToCurrentBlock("newarrayvarofbool1",NanoSymbolTable.BOOL_ARRAY_TYPE,10);
	symtab.addScalarToCurrentBlock("var2", NanoSymbolTable.INT_TYPE);
	
	//symtab.lookup("var2");
	
	symtab.showContents();	
	
	
	symtab.endCurrentBlock();
	symtab.showContents();
	
	symtab.endCurrentBlock();
	symtab.showContents();
}

/* A pretend Nano program whose lexical structure might result in the above calls.
 * 
program
 const const1 = 417; ///Value doesn't matter in the above; a quad is generated to store this in mem
 var var1, var2 : integer;
 begin
   //some use of var2, such as:
     if (var2>5)
     //-->This is where the first showContents is called on the whole symbol table
     begin
        const newconst1 = 147; //Doesn't matter; not kept in symtab anyway 
        var newboolvar2 : boolean;
        var newarrayvarofint1[5] : integer;
        var newarrayvarofbool1[10] : boolean;
        var var2 : integer;
        //--> Point of first lookup finding the innermost var2
        //--> Point of the second showContents called on the whole symbol table
      end
      //-->Point of the third showContents
 end
 //Point of the final (empty showContents call)
 * 
 * 
 * 
 */


}

/* in addTempto...
 * When a temporary variable is needed in a computation, its name will only be in scope
 * in the block where it is created. So we have a local list of temporary variables for each block.
 * This method allows us to add a new temporary variable, assigning memory and entering it into
 * the symbol table for quad generation and synthesized attributes (type-checking). The name
 * is automatically generated. When the block is exited so is the memory freed for these 
 * temporaries or declared memory usage. There are no temporary arrays or procedures or 
 * constants, so it only requires one of the two parameters NanoSymbolTable.INT_TYPE or 
 * NanoSymbolTable.BOOL_TYPE. The name is returned so the compiler can track it 
 * for correct placement in quad.
 * Temporary variables are used in expressions and are only of type integer or boolean.
 */

/* in tempIdListClear
 * Once temps have been placed in quads, knowing their slots in runtime memory
 * will be cleared automatically when the block is exited, the list can be cleared
 * for future use. Technically the names could be reset, but this could lead to
 * confusion in the produced code so we allow the # after the T to increase "indefinitely"
 * Needs to be public because use of the temp list is tied to formal lists, not necessarily
 * cleared by calls to endCurrentBlock
 */
