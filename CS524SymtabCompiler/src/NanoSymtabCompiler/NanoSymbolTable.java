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
		switch(type){
		default: return "Unknown Type";
		case 0: return "Integer Type";
		case 1: return "Boolean Type";
		case 2: return "Integer Array Type";
		case 3: return "Boolean Array Type";
		case 4: return "Process Type";
		}
	}
	
	//For narrating symbol table behavior
	private boolean verbose;
	private int verbosityBlockCount = 0; 
		//just for making it easy to narrate the verbose symtab
	
	//Main instance variables of the chained 
	//hash-block symbol table architecture
	
	private Stack blockEntryStack; 
		//this is the stack of entries collected together
	 	//according to lexical blocks in the source code
	
	private int stackTopOffset;	
		//this is what acts as an actual 
		//address into the RunTime Memory model
	
	private NanoSymbolTableBlockEntry currentBlock;
		//Points to entry for local variables for lexical context 
		//currently being parsed; include as inner final class below 

	//Utility support for IdLists
	private int currTempNum;
	private ArrayList tempIdList;
	
	//Constructors
	//Default
	public NanoSymbolTable()
	{
		blockEntryStack = new Stack();
		stackTopOffset = 0;
		currentBlock = null;
		currTempNum = 0;
		tempIdList = new ArrayList();
		this.verbose = false; //default not verbose
	}
		
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
			//Yadayadyada…
		}
	}

	/*
	 * Method to call to initialize a new block in the chain 
	 * corresponding to a new lexical scope
	 * Should always be paired with a call to endCurrentBlock 
	 * when the same scope reaches closure
	 */
	public void startNewBlock()
	{
		NanoSymbolTableBlockEntry temp = new NanoSymbolTableBlockEntry(stackTopOffset++, currentBlock);
		currentBlock = temp;
		blockEntryStack.push(currentBlock);
	
	}
	

	/*
	 * Method to call to remove a current block in the chain 
	 *  corresponding to a lexical scope
	 * that is ending. Should always be paired with a 
	 * prior call to startCurrentBlock for the same scope lexical scope
	 */
	public void endCurrentBlock()
	{
		
	}
	
	
	/*
	 * Method to add a declared scalar to the curent block; 
	* applicable for integers and booleans
	 */
	public void addScalarToCurrentBlock(String name, int type)
	{
		
	}
	
	/*
	 * Method to add a declared array variable to the curent block; 	 	 	* applicable for integers and booleans
	 */
	public void addArrayToCurrentBlock(String name, int type, int size)
	{
		
	}
	
		
	/*
	 * Method to add a declared constant variable (no arrays or booleans) 
	*  to the curent block
	 * Recall that constants get runtime memory assignments just 
	*  like variables but have
	 * a special flag set so that the compiler can ensure that no 
	 * instructions modify those values
	 */
	public void addConstIntToCurrentBlock(String name)
	{
		
	}
	
	
	/*
	 * Truthfully in Nano all procedures are declared before the 
	* first block but all variables 
	 * declared there are considered visible inside that main 
	* block so we do the same here
	 * with all the procedures. This has the side-effect of being 
	* easy to modify, should Nano
	 * ever evolve toward having procedures declared inside inner blocks.
	 */
	public void addProcedureToCurrentBlock
		(String name, int numParams, int startQuad, int endQuad)
	{
		
	}
	
			
	/*
	 * When a temporary variable is needed in a computation, 
	* its name will only be in scope
	 * in the block where it is created. So we have a local 
	* list of temporary variables for each block.
	 * This method allows us to add a new temporary variable, 
	* assigning memory and entering it into
	 * the symbol table for quad generation and synthesized 
	* attributes (type-checking). The name
	 * is automatically generated. When the block is exited so is 
	*  the memory freed for these 
	 * temporaries or declared memory usage. There are no 
	* temporary arrays or procedures or 
	 * constants, so it only requires one of the two parameters 
	* NanoSymbolTable.INT_TYPE or NanoSymbolTable.BOOL_TYPE. 
	* The name is returned so the compiler can track it 
	 * for correct placement in quad.
	 * Temporary variables are used in expressions and are 
	* only of type integer or boolean.
	 */
	public String addNewTempToCurrentBlock(int type)
	{
		
	}
	
				
	/*
	 *  Method to lookup an identifier at the main symbol table level,
	 *  which triggers the local hash table checks and then walks 
	* up the chain. Returns an instance of the
	* NanoSymbolTableIndividualEntry, a final inner class 
	 *  defined below which will have in it all necessary information 
	* for participating
	 *  in synthesized attributes. 
	*
	 *  !!Returns null if entry is nowhere in the symbol table!!
	 */
	public NanoSymbolTableIndividualEntry get(String name)
	{
		
	}
	
	
	/*
	 * Workhorse of the tracing of the symbol table. 
	 * Keep in mind that all this machinery is mostly for the 
	* compiler developer, to aid in the process of constructing a 
	* correct compiler that uses the symtab appropriately.
	 */
	public void showContents()
	{
		
	}
	
			
	/*
	 * Once temps have been placed in quads, knowing their slots 
	* in runtime memory will be cleared automatically when the 
	* block is exited, the list can be cleared
	 * for future use. Technically the names could be reset, but this 
	* could lead to confusion in the produced code so we allow the 
	* number  after the T to increase "indefinitely"
	 * Needs to be public because use of the temp list is tied to formal lists, 	* not necessarily cleared by calls to endCurrentBlock and thus 
	* sometimes needing to be called by a grammar rule processor directly
	 */
	public void tempIdListClear()
	{
		
	}

	
	/*
	 * Once one or more temps have been generated for some subtree 
	* of the parse it is helpful to have this iterator to spit them back out for 	* use in quads.  Also, this is used by the repeated reductions by 
	* a production that is grabbing
	 * one in a sequence of definitions such as x,y,z : integer
	 */
	public Iterator getTempIdListIterator()
	{
		
	}
	

	/*Private service method triggered by "addNewTempToCurrentBlock".
	 */
	private String getNewTempName()
	{
		
	}
	
	/*
	 * Service method triggered by "addNewTempToCurrentBlock" or 
	* as described above in iterator.
	 * When in need of a temporary variable the symbol table keeps 
	* them for the compiler write in this array list and this 
	* method is the workhorse for that task.
	 */
	public void tempIdListAdd(String identifier)
	{
		
	}
	
//
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//				NANO SYMBOL TABLE BLOCK ENTRY CLASS
//			 (final inner support class for the above)
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//
	
public final class NanoSymbolTableBlockEntry 
{
	//Main instance variables	
	private int blockOffset;	 
		//What is the base address of this block on its creation
	private int entryOffset;	 
		//What is the offset within the block of the individual entry
	private Hashtable entries;	 
		//The actual hashtable of entries
	private NanoSymbolTableBlockEntry beneath; 
		//The pointer to the one "under it" in the stack
	private int verbosityBlockCount;
	
	public NanoSymbolTableBlockEntry
	(int blockOffset, NanoSymbolTableBlockEntry beneath /*, int verbosityBlockCount*/ ){
		this.blockOffset = blockOffset;
		entryOffset = 0;
		entries = new Hashtable();
		this.beneath = beneath;
		//this.verbosityBlockCount = verbosityBlockCount;
	}

	public NanoSymbolTableIndividualEntry get(String name)
	{
		
	}
	
	/*
	 * For scalars the compiler need only give the name and type
	 * The address is calculated by the symbol table and 
	*  isConstant is always false
	 */
	public void put(String name, int type)
	{
		
	}
	
	/*
	 * For constant integers the compiler need only give the name
	 * The address is calculated by the symbol table and 
	* isConstant is always false
	 */
	public void put(String name)
	{
		
	}
	

	/*
	 * For arrays of either integer or boolean the compiler need 
	* give name, type and size
	 * The rest of the addressing information is done by the symbol table
	 */
	public void put(String name, int type, int size)
	{
		
	}
	
	
	/*
	 * For procedures the compiler need give name, 
	* number of parameters and start and end quads
	 */
	public void put(String name, int numParams, int startQuad, int endQuad)
	{
		
	}
	
	
	/*
	 * Used to subtract off free space from the runtime stack
	 */
	private int lengthOfEntries()
	{
		
	}
	/*
	 * Easy way to get block contents in printable form
	 */
	public String toString()
	{
		
	}
}


	//
	///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
	//				NANO SYMBOL TABLE INDIVIDUAL ENTRY CLASS (and subclasses)
	//			         (final inner support classes for the above)
	///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
	//
	
public class NanoSymbolTableIndividualEntry
{
		//All the things we want to know about an identifier in the symbol table
	
		public boolean isInteger(){}
		public boolean isBoolean(){}
		public boolean isConstant(){}
		public boolean isBooleanArray(){}
		public boolean isIntArray(){}
		public boolean isProcedure(){}
		public int getActualType(){} //Uses the constants defined at top
		public String toString(){}	
}
	
public final class NanoSymbolTableIndividualScalarEntry 
	extends NanoSymbolTableIndividualEntry
	{
		public int getAddress(){}
		public String toString(){}
	}
	
public final class NanoSymbolTableIndividualArrayEntry 
	extends NanoSymbolTableIndividualEntry
	{
		public int getAddress(){}
		public int getSize(){}
		public String toString(){}
	}
	
public final class NanoSymbolTableIndividualProcedureEntry
	extends NanoSymbolTableIndividualEntry
	{
		public int getAddress(){}
		public boolean isProcedure(){}
		public String toString(){}
	}}
