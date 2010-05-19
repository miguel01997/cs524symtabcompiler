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
	private int blockCount = 0; //just for making it easy to narrate the symtab output
	
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
	private ArrayList tempTargetList;
	private ArrayList tempFormalList;
	private ArrayList tempExprList;
	
	private Hashtable procEntries; //These are kept separate from the block structure
								   //because the procedure definitions cannot be nested or
	 							   //have access to nested variables other than those in 
								   //the scope of the call statement that invokes them

	//Because past copies of the symbol table have provided ample access to verbosity
	//it is not included in this release; however comments will be instructive where necessary
	public NanoSymbolTable()
	{
		blockEntryStack = new Stack();
		stackTopOffset = 0;
		currentBlock = null;
		currTempNum = 0;
		tempIdList = new ArrayList();
		tempTargetList = new ArrayList();
		tempFormalList = new ArrayList();
		tempExprList = new ArrayList();
		procEntries = new Hashtable();
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
		blockCount++;
		currentBlock = new NSTBlockEntry(stackTopOffset,currentBlock,blockCount);
		blockEntryStack.push(currentBlock);
	}

	/*
	 * Method to call to remove a current block in the chain corresponding to a lexical scope
	 * that is ending. Should always be paired with a prior call to 
	 * startCurrentBlock for the same scope lexical scope
	 */
	public void endCurrentBlock()
	{
		blockCount--;
		NSTBlockEntry be = (NSTBlockEntry) blockEntryStack.pop();
		stackTopOffset -= be.lengthOfEntries();
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
			return e;
		}
		else
		{
			return null;
		}	
	}

	/*
	 * Method to add a declared array variable to the curent block; 
	 * applicable for integers and booleans
	 */
	public NSTIndArrayEntry addArrayToCurrentBlock(String name, int type, int size)
	{
		NSTIndArrayEntry e;
		if (!currentBlock.entries.containsKey(name))
		{
			e = currentBlock.put(name, type, size);
			stackTopOffset += size;
			return e;
		}
		else
		{
			return null;
		}
	}


	/*
	 * For temps created by compiler writer or automatically otherwise
	 * This is why there is no tempIdListAdd method below with the others
	 */
	public NSTIndScalarEntry addNewTempToCurrentBlock(int type)
	{
		String name = getNewTempName();
		NSTIndScalarEntry e = addScalarToCurrentBlock(name,type);
		tempIdListAdd(name);
		return e;
	}

	/*
	 * For creating temporary structures for immediate values
	 */
	public NSTIndImmediateEntry createImmediate(int type, Object intOrBool)
	{
		NSTIndImmediateEntry imm =
			new NSTIndImmediateEntry(type,intOrBool);
		//Strangely enough we don't have to put these in the symbol table
		//We just need the data structure that contains the information
		//to be instantiated and passed to the compiler
		return imm;
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
	 * Clearing methods for the support tempLists
	 */
	public void tempIdListClear()
	{
		tempIdList = new ArrayList();
	}
	public void tempTargetListClear()
	{
		tempTargetList = new ArrayList();
	}
	public void tempFormalListClear()
	{
		tempFormalList = new ArrayList();
	}
	public void tempExprListClear()
	{
		tempExprList = new ArrayList();
	}	

	/*
	 * Once one or more temps have been generated for some subtree of the parse
	 * it is helpful to have these iterators to spit them back out for use in quads.
	 * Also, this is used by the repeated reductions by a production that is grabbing
	 * one in a sequence of definitions such as x,y,z : integer
	 */
	public Iterator getTempIdListIterator()
	{
		return tempIdList.iterator();
	}
	public Iterator getTempTargetListIterator()
	{
		return tempTargetList.iterator();
	}
	public Iterator getTempFormalListIterator()
	{
		return tempFormalList.iterator();
	}
	public Iterator getTempExprListIterator()
	{
		return tempExprList.iterator();
	}

	/*
	 * Service method triggered by "addNewTempToCurrentBlock" or as described above in iterator.
	 * When in need of a temporary variable the symbol table keeps them for the 
	 * compiler writer in this array list and this method is the workhorse
	 * for that task. Can also be leveraged "manually" for things like IdLists
	 * It's easier to have symtab built-in methods to do this rather
	 */
	public void tempIdListAdd(String identifier)
	{
		tempIdList.add(identifier);
	}
	/* Plus some others....*/
	public void tempTargetListAdd(NSTIndEntry e)
	{
		tempTargetList.add(e);
	}
	public void tempFormalListAdd(FormalContainer f)
	{
		tempFormalList.add(f);
	}
	public void tempExprListAdd(NSTIndEntry e)
	{
		tempExprList.add(e);
	}
	
	/*Private service method triggered by "addNewTempToCurrentBlock".
	 *At end of a block the list of temporary variables and 
	 *their names must be reset by another method.
	 */
	private String getNewTempName()
	{
		String name = "$T"+(new Integer(currTempNum)).toString();
		currTempNum++;
		return name;
	}

	/*
	 * In Nano all procedures are declared before the first block and all parameters passed in or
	 * variables declared within are the only ones considered visible inside the procedure block so 
	 * the procedures are added to a different storage mechanism, and lookups on "outside" variables
	 * are not only not possible but are therefore an opportunity to provide efficiency. Should we 
	 * need any of those values in the main scope blocks we would pass them in. 
	 */
	public NSTIndProcEntry addProcedureToSymbolTable
		(String name, int numParams, Hashtable formalContainerList)
	{
		//Procedures are in their own hashtable separate from the block stack	
		if (this.get(name)!=null)
		{
			System.out.println("SYMTAB ERROR: Nano defines procedures for use in any "+
							   "block and this procedure name is already taken by a variable "+
							   "somewhere in the Nano code being compiled\n\n");
			System.exit(1); //OUCH!
		}
		NSTIndProcEntry e = new NSTIndProcEntry(name,NanoSymbolTable.PROC_TYPE,numParams,formalContainerList);		
		procEntries.put(name, e);
		return e;
	}
	

	public NSTIndProcEntry getProcedure(String procName)
	{ Object o = procEntries.get(procName); 
	  return (NSTIndProcEntry) o; }
	
	/*
	 * Workhorse of the tracing of the symbol table. 
	 * Keep in mind that all this machinery is mostly for the compiler developer,
	 * to aid in the process of constructing a correct compiler that uses the symtab appropriately.
	 */
	public void showContents()
	{
			Iterator besi = blockEntryStack.iterator();
			System.out.println("Nano Symbol Table =================>\n\n");
			while (besi.hasNext())
			{
				System.out.println(besi.next());
			}
			
			String result = "			-----------beginning of Procedure Entries -------------\n\n";
			Iterator procEntriesIterator = procEntries.values().iterator();
			while (procEntriesIterator.hasNext())
			{
				result += procEntriesIterator.next().toString();
			}
			result += "			-----------end of Procedure Entries -------------\n\n";
			System.out.println("=======================================\n\n\n");

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
	private int blockCount;
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
		this.blockCount = vbc;			//For tracking for the user...
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
			new NSTIndArrayEntry(name,type,blockOffset+entryOffset,size);
		entries.put(name, e);
		entryOffset+=size;
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
							"			This is block number " + blockCount + "\n" +
							"			BlockOffset: " + blockOffset + "\n\n";
			Set eks = entries.keySet();
			Iterator eksi = eks.iterator();
			while (eksi.hasNext())
			{
				result += entries.get(eksi.next()).toString();
			}
			result += "			-----------end of SymTab Block Entry-------------\n\n";

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
	protected String name;
	protected int actualType;		//Must be one of types declared at class beginning
	protected boolean isConstant = false;	//Again to be overridden if otherwise
	protected boolean isImmediate = false; //Same
	public NSTIndEntry
	(String name, int type, boolean isConstant, boolean isImmediate)
	{
		this.name = name;
		this.actualType = type;
		this.isConstant =  isConstant;
		this.isImmediate = isImmediate;
	}
	public boolean isInteger() { return actualType == INT_TYPE; }
	public boolean isBoolean() { return actualType == BOOL_TYPE; }
	public boolean isScalar() { return (actualType==INT_TYPE)||(actualType==BOOL_TYPE); }
	public boolean isConstant() { return isConstant; }
	public boolean isImmediate() { return isImmediate; }	
	public boolean isBooleanArray() { return actualType == BOOL_ARRAY_TYPE; }
	public boolean isIntArray() { return actualType == INT_ARRAY_TYPE; }
	public boolean isProcedure() { return actualType == PROC_TYPE; }
	public int getActualType() { return actualType; }
	public void setActualType(int type) { actualType = type; } 
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
				"			IsImmediate?: " + isImmediate + "\n" +
				"			Type: " + actType + "\n";
	}
}	

public final class NSTIndImmediateEntry
extends NSTIndEntry
{	//Operates like a union from C
	private int intValue;
	private boolean boolValue;
	public NSTIndImmediateEntry(int type, Object intOrBool)
	{
		super("",type,true,true);
		if (type==NanoSymbolTable.INT_TYPE) intValue = ((Integer) intOrBool).intValue();
		if (type==NanoSymbolTable.BOOL_TYPE) boolValue = ((Boolean) intOrBool).booleanValue();
	}
	public int getIntValue() { return intValue; }
	public boolean getBoolValue() { return boolValue; }
	public String toString() 
	{
			return
			super.toString() + " " + (  (actualType==NanoSymbolTable.INT_TYPE)?
										(new Integer(intValue)).toString():
										(new Boolean(boolValue)).toString() 
									  ) + "\n\n";
	}
}

public final class NSTIndScalarEntry 
extends NSTIndEntry
{
	private int address;
	public NSTIndScalarEntry
	(String name, int type, boolean isConstant, int address)
	{
		super(name,type,isConstant,false);
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
	(String name, int type, int address, int size)
	{
		super(name,type,false,false);
		this.address = address;
		this.size = size;
	}
	public int getAddress() { return address; }
	public int getSize() { return size; }
	public String toString() 
	{
			return
			super.toString()  +
			"			Address: " + address + "\n" +
			"			Size:	 " + size + "\n\n";
	}
}

//
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//				NANO SYMBOL TABLE PROCEDURE ENTRY CLASS
//			 (final inner support class for the above)
///\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
//
public final class FormalContainer
{
	private String idLexeme;
	private int typeFlag;
	private int negativeOffset;
	
	public FormalContainer(String idLexeme, int typeFlag, int negativeOffset)
	{
		this.idLexeme = idLexeme;
		this.typeFlag = typeFlag;
		this.negativeOffset = negativeOffset;
	}
	public String getName() { return idLexeme; }
	public int getType() { return typeFlag; }
	public int getNegOffset() { return negativeOffset; }
	public String toString() 
	{ return	"			FormalContainer entry:\n"+
			 	"			lexeme: " + idLexeme +"\n" +
				"			type: " + NanoSymbolTable.getTypeName(typeFlag) + "\n" +
				"			negOffset: " + negativeOffset +"\n\n"; 
	}
}

public final class NSTIndProcEntry 
extends NSTIndEntry
{
	private int numberInputParamSlots;
	private int startQuadNumber;
	private int endQuadNumber;
	private Hashtable formalContainerList;
	private Stack callQuadIndex;
	public NSTIndProcEntry
	(String name, int type, int numberInputParamSlots, Hashtable formalContainerList)
	{
		super(name,type,false,false);
		this.numberInputParamSlots = numberInputParamSlots;
		this.formalContainerList = formalContainerList;
		this.callQuadIndex = new Stack();
	}
	public int getNumInputs() { return numberInputParamSlots; }
	public int getStartQuadNumber() { return startQuadNumber; }
	public int getEndQuadNumber() { return endQuadNumber; }
	public int getNumberQuads() { return endQuadNumber - startQuadNumber + 1; }
	public void setStartQuadNumber(int sqn) { startQuadNumber = sqn; }
	public void setEndQuadNumber(int eqn) { endQuadNumber = eqn; }
	public void pushCallQuadIndex(int cqi) { callQuadIndex.push(new Integer(cqi)); }
	public Integer popCallQuadIndex() {  Integer tmp;
										tmp = (Integer) callQuadIndex.pop();
										return tmp;}
	public Hashtable getHashtable() { return formalContainerList; }
	public String toString() 
	{
		String result
			= 
			super.toString()  +
			" 			Number Input Parameters: " + numberInputParamSlots + "\n" +
			"	    			Start Quad Number: " + startQuadNumber + "\n" +
			"         			End Quad Number: " + endQuadNumber + "\n\n";
			Iterator i = formalContainerList.values().iterator();
			while (i.hasNext())
				result = result + ((FormalContainer) i.next()).toString();
			result += "\t\t\t---end procedure: " + name + "----\n\n\n";
			return result;		
	}
}
	

}


