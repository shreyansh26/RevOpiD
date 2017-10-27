
/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                             TOTAL SUPPORT TREE                            */
/*                                                                           */
/*                                Frans Coenen                               */
/*                                                                           */
/*                               10 January 2003                             */
/*  (Revised 23/1/2003, 8/2/2003, 18/3/2003, 3/3/2003, 7/4/2004, 19/1/2005,  */
/*	     3/2/2006, 31/5/2006, 16/10/2006, 21/11/2006, 26/3/2007)             */
/*                                                                           */
/*                       Department of Computer Science                      */
/*                         The University of Liverpool                       */
/*                                                                           */ 
/* ------------------------------------------------------------------------- */

/* Structure:

AssocRuleMining
      |
      +-- TotalSupportTree	 */

//package lucsKDD_ARM;

/* Java packages */
import java.io.*;
import java.util.*;

// Java GUI packages
import javax.swing.*;

/** Methods concerned with the generation, processing and manipulation of
T-tree data storage structures used to hold the total support counts for large
itemsets.
@author Frans Coenen
@version 21 November 2006 */

public class TotalSupportTree extends AssocRuleMining {

    /* ------ FIELDS ------ */

    // Data structures
    /** The reference to start of t-tree. */
    protected TtreeNode[] startTtreeRef;
    /** The serialisation array (used for distributed ARM applications
    when sending T-trees from one processor to another via a JavaSpace). */
    protected int[] serializationArray;
    /** The marker to the "current" location in the serialisation array. <P>
    initialised to zero. */
    protected int serializationRef = 0;

    // Constants
    /** The maximum number of frequent sets that may be generated. */
    protected final int MAX_NUM_FREQUENT_SETS = 500000;

    // Other fields
    /** The next level indicator flag: set to <TT>true</TT> if new level
    generated and by default. */
    protected boolean nextLevelExists = true ;
    /** Number of levels in T-tree. */
    protected int numLevelsInTtree = 0;
    /** The maximum number of T-tree nodes that can be output to a graph. */
    private int maxTtreeGraphNodes = 0;
    /** Flag indicating that T-tree statistics output is desired. */
    protected boolean outputTtreeStatsFlag = false;
    /** Flag indicating that T-tree (as text) output is desired. */
    protected boolean outputTtreeFlag = false;
    /** Flag indicating that T-tree grpah output is desired. */
    protected boolean outputTtreeGraphFlag = false;

    // Diagnostics
    /** The number of updates required to generate the T-tree. */
    protected long numUpdates   = 0l;
    /** Time to generate T-tree. */
    protected String duration = null;

    /* ------ CONSTRUCTORS ------ */

    /** One argument constructor with command line arguments to be process.
    @param args the command line arguments (array of String instances). */

    public TotalSupportTree(String[] args) {
		super(args);
		}

    /** One argument constructior with argument from existing instance of
    class AssocRuleMining.
    @param armInstance the given instance of the <TT>AssocRuleMining</TT>
    class. */

    public TotalSupportTree(AssocRuleMining armInstance) {
        super(armInstance);
	    numRows           = armInstance.numRows;
	    numCols           = armInstance.numCols;
	    support           = armInstance.support;
	    confidence        = armInstance.confidence;
        dataArray         = armInstance.dataArray;
	    minSupport        = armInstance.minSupport;
	    numOneItemSets    = armInstance.numOneItemSets;
        conversionArray   = armInstance.conversionArray;
	    reconversionArray = armInstance.reconversionArray;
	    }

    /** Default constructor */

    public TotalSupportTree() {
        }

    /* ------ METHODS ------ */

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                       T-TREE BUILDING METHODS                          */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* CREATE TOTAL SUPPORT TREE */
    /** Commences start process of generating a total support tree (T-tree). */

    public void createTotalSupportTree() {
		System.out.println("Apriori-T with X-CheckinnMinimum support " +
	            "threshold = " + twoDecPlaces(support) + "% " + "(" +
				 twoDecPlaces(minSupport) + " records)");

		// If no data (possibly as a result of an order and pruning operation)
		// return
		if (numOneItemSets==0) return;

		// Initilise T-tree data structure and diagnostic counters. Set number 
		// of t-tree nodes to zero (this is a static field so will not be reset 
		// in repeat calls to the T-tree constructor).
		startTtreeRef   = null;
		numFrequentSets = 0;
		numUpdates      = 0l;
		TtreeNode.setNumberOfNodesFieldToZero();

        // Continue
        contCreateTtree();

        // Potential output
        if (outputTtreeStatsFlag) outputTtreeStats();
        if (outputTtreeFlag) outputTtree();
        //if (outputTtreeGraphFlag) drawTtreeGraph();
        }

    /** Commences pstart rocess of generating a total support tree (T-tree): GUI
    version.
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void createTotalSupportTree(JTextArea textArea) {
		textArea.append("Apriori-T with X-CcheckinnMinimum support " +
	              "threshold = " + twoDecPlaces(support) + "% (" +
                            twoDecPlaces(minSupport) + " records)\n");

		// If no data (possibly as a result of an order and pruning operation)
		// return
		if (numOneItemSets==0) return;

		// Initilise T-tree data structure and diagnostic counters. Set number 
		// of t-tree nodes to zero (this is a static field so will not be reset 
		// in repeat calls to the T-tree constructor).
		startTtreeRef   = null;
		numFrequentSets = 0;
		numUpdates      = 0l;
		TtreeNode.setNumberOfNodesFieldToZero();

        // Continue
        contCreateTtree(textArea);

        // Potential output
        if (outputTtreeStatsFlag) outputTtreeStats(textArea);
        if (outputTtreeFlag) outputTtree(textArea);
        //if (outputTtreeGraphFlag) drawTtreeGraph();
		}

    /** Continues start process of generating a total support tree (T-tree). */

    protected void contCreateTtree() {
		// Create Top level of T-tree (First pass of dataset). Defined in
		// in TotlaSupportTree class.
		createTtreeTopLevel();

		// Generate level 2
		generateLevel2();

		// Further passes of the dataset
		createTtreeLevelN();
		}

    /** Continues start process of generating a total support tree (T-tree), GUI
    version.
    @param textArea the given instance of the <TT>JTextArea</TT> class.*/

    protected void contCreateTtree(JTextArea textArea) {
		// Create Top level of T-tree (First pass of dataset). Defined in
		// in TotlaSupportTree class.
        createTtreeTopLevel();

		// Generate level 2
        generateLevel2();

		// Further passes of the dataset
        createTtreeLevelN(textArea);
		}

    /* CREATE T-TREE TOP LEVEL */
    /** Generates level 1 (top) of the T-tree. */

    protected void createTtreeTopLevel() {
	// Dimension and initialise top level of T-tree
	startTtreeRef = new TtreeNode[numOneItemSets+1];
	for (int index=1;index<=numOneItemSets;index++)
	    			startTtreeRef[index] = new TtreeNode();

        // Add support for each 1 itemset
	createTtreeTopLevel2();

	// Prune top level, setting any unsupported 1-itemsets to null
	pruneLevelN(startTtreeRef,1);
	}

    /** Adds supports to level 1 (top) of the T-tree. */

    protected void createTtreeTopLevel2() {
        numLevelsInTtree = 1;

        // Loop through data set record by record and add support for each
	// 1 itemset
	for (int index1=0;index1<dataArray.length;index1++) {
	    // Non null record (if initial data set has been reordered and
	    // pruned some records may be empty!
	    if (dataArray[index1] != null) {
    	        for (int index2=0;index2<dataArray[index1].length;index2++) {
		    startTtreeRef[dataArray[index1][index2]].support++;
		    numUpdates++;
		    }
		}
	    }
	}

    /* CREATE T-TREE LEVEL N */
    /** Commences the process of determining the remaining levels in the T-tree
    (other than the top level), level by level in an "Apriori" manner. <P>
    Follows an add support, prune, generate loop until there are no more levels
    to generate. */

    protected void createTtreeLevelN() {
        int nextLevel=2;

	// Loop while a further level exists

	while (nextLevelExists) {
            // Add support
	    addSupportToTtreeLevelN(nextLevel);
	    // Prune unsupported candidate sets
	    pruneLevelN(startTtreeRef,nextLevel);
	    // Check number of frequent sets generated so far
	    if (numFrequentSets>MAX_NUM_FREQUENT_SETS) {
	        System.out.println("Number of frequent sets (" +
				numFrequentSets + ") generted so far " +
				"exceeds limit of " + MAX_NUM_FREQUENT_SETS +
				", generation process stopped!");
		break;
		}
	    // Attempt to generate next level
	    nextLevelExists=false;
	    generateLevelN(startTtreeRef,nextLevel,null);
	    nextLevel++;
	    }

	// End
	numLevelsInTtree = nextLevel-1;
	}

    /** Commences the process of determining the remaining levels in the T-tree
    (other than the top level), level by level in an "Apriori" manner --- GUI
    version.
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    protected void createTtreeLevelN(JTextArea textArea) {
        int nextLevel=2;

	// Loop while a further level exists

	while (nextLevelExists) {
	    // Add support
	    addSupportToTtreeLevelN(nextLevel);
	    // Prune unsupported candidate sets
	    pruneLevelN(startTtreeRef,nextLevel);
	    // Attempt to generate next level
	    nextLevelExists=false;
	    generateLevelN(startTtreeRef,nextLevel,null);
	    nextLevel++;
	    }

	//End
	numLevelsInTtree = nextLevel-1;
	textArea.append("Levels in T-tree = " + numLevelsInTtree + "\n");
	}

    /* CREATE TOTAL SUPPORT TREE */

    /** Dummay method to commences process of generating a total support tree
    (T-tree) using negative boarder concept for a given segment of the data. */

    public void createTotalSupportTreeNB() {}

    /** Dummy method to commences process of generating a total support tree
    (T-tree) using negative boarder concept for a given segment of the data.
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void createTotalSupportTreeNB(JTextArea textArea) {}

    /* ---------------------------- */
    /* ADD SUPPORT VALUES TO T-TREE */
    /* ---------------------------- */

    /* ADD SUPPORT VALUES TO T-TREE LEVEL N */
    /** Commences process of adding support to a given level in the T-tree
    (other than the top level).
    @param level the current level number (top level = 1). */

    protected void addSupportToTtreeLevelN(int level) {
		// Loop through data set record by record
        for (int index=0;index<dataArray.length;index++) {
	    // Non null record (if initial data set has been reordered and
	    // pruned some records may be empty
	    if (dataArray[index] != null) addSupportToTtree(startTtreeRef,
	                      level,dataArray[index].length,dataArray[index]);
	    }
	}

    /* ADD SUPPORT TO T-TREE FIND LEVEL */
    /** Adds support to a given level in the T-tree (other than the top level).
    <P> Operates in a recursive manner to first find the appropriate level in
    the T-tree before processing the required level (when found).
    @param linkRef the reference to the current sub-branch of T-tree (start at
    top of tree)
    @param level the level marker, set to the required level at the start and
    then decremented by 1 on each recursion.
    @param endIndex the index into the item set array at which processing
    should be stopped. This should be the index of the attribute in the item
    set that is the parent T-tree node of the current level. On start this
    will usually be the length of the input item set.
    @param itemSet the current itemset (record from data array) under
    consideration. */

    protected void addSupportToTtree(TtreeNode[] linkRef, int level,
    			                      int endIndex, short[] itemSet) {
//System.out.print("addSupportToTtree: level = " + level + ", endIndex = " +
//endIndex + ", itemSet = ");
//outputItemSet(itemSet);
//System.out.println();

	// At right level;
	if (level == 1) {
//System.out.println("*** In Level 1 ***");
	    // Step through itemSet
	    for (int index1=0;index1<endIndex;index1++) {
			// If valid node update, i.e. a non null node
			if (linkRef[itemSet[index1]] != null) {
//System.out.println("linkRef[itemSet[index1]].support = " + linkRef[itemSet[index1]].support);
		    	linkRef[itemSet[index1]].support++;
		    	numUpdates++;
		    	}
//else System.out.println("NULL");
			}
	    }

	// At wrong level
	else {
	    // Step through itemSet
	    for (int index=level-1;index<endIndex;index++) {
			// If child branch step down branch
			if (linkRef[itemSet[index]] != null) {
		    	if (linkRef[itemSet[index]].childRef != null)
		    	 addSupportToTtree(linkRef[itemSet[index]].childRef,
						     level-1,index,itemSet);
		    	}
			}
	    }
	}

    /*---------------------------------------------------------------------- */
    /*                                                                       */
    /*                                 PRUNING                               */
    /*                                                                       */
    /*---------------------------------------------------------------------- */

    /* PRUNE LEVEL N */

    /** Prunes the given level in the T-tree. <P> Operates in a recursive
    manner to first find the appropriate level in the T-tree before processing
    the required level (when found). Pruning carried out according to value of
    <TT>minSupport</TT> field.
    @param linkRef The reference to the current sub-branch of T-tree (start at
    top of tree)
    @param level the level marker, set to the required level at the start and
    then decremented by 1 on each recursion.
    @return true if all nodes at a given level in the given branch of the
    T-tree have been pruned (in which case the t-tree generation processing can
    be stopped), false otherwise. */

    protected boolean pruneLevelN(TtreeNode [] linkRef, int level) {
        int size = linkRef.length;
	// At right level;
	if (level == 1) {
	    boolean allUnsupported = true;
	    // Step through level and set to null where below min support
	    for (int index1=1;index1<size;index1++) {
	        if (linkRef[index1] != null) {
	            if (linkRef[index1].support < minSupport)
		    		linkRef[index1] = null;
	            else {
		        numFrequentSets++;
			allUnsupported = false;
			}
		    }
		}
	    return(allUnsupported);
	    }

	// Wrong level, Step through row
	for (int index1=level;index1<size;index1++) {
	    if (linkRef[index1] != null) {
		// If child branch step down branch
		if (linkRef[index1].childRef != null) {
		    if (pruneLevelN(linkRef[index1].childRef,level-1))
			    		linkRef[index1].childRef=null;
		    }
		}
	    }
	return(false);
	}

    /*---------------------------------------------------------------------- */
    /*                                                                       */
    /*                            LEVEL GENERATION                           */
    /*                                                                       */
    /*---------------------------------------------------------------------- */

    /* GENERATE LEVEL 2 */

    /** Generates level 2 of the T-tree. <P> The general
    <TT>generateLevelN</TT> method assumes we have to first find the right
    level in the T-tree, that is not necessary in this case of level 2. */

    protected void generateLevel2() {

	// Set next level flag
	nextLevelExists=false;

	// loop through top level (start at index 2 because cannot generate a
	// level from index 1 as there will be no proceeding attributes,
	// remember index 0 is unused.
	for (int index=2;index<startTtreeRef.length;index++) {
	    // If supported T-tree node (i.e. it exists)
	    if (startTtreeRef[index] != null) generateNextLevel(startTtreeRef,
	    				index,realloc2(null,(short) index));
	    }
	}

    /* GENERATE LEVEL N */

    /** Commences process of generating remaining levels in the T-tree (other
    than top and 2nd levels). <P> Proceeds in a recursive manner level by level
    until the required level is reached. Example, if we have a T-tree of the form:

    <PRE>
    (A) ----- (B) ----- (C)
               |         |
	       |         |
	      (A)       (A) ----- (B)
    </PRE><P>
    Where all nodes are supported and we wish to add the third level we would
    walk the tree and attempt to add new nodes to every level 2 node found.
    Having found the correct level we step through starting from B (we cannot
    add a node to A), so in this case there is only one node from which a level
    3 node may be attached.
    @param linkRef the reference to the current sub-branch of T-tree (start at
    top of tree).
    @param level the level marker, set to the required level at the start and
    then decremented by 1 on each recursion.
    @param itemSet the current itemset (T-tree node) under consideration. */

    protected void generateLevelN(TtreeNode[] linkRef, int level,
    							short[] itemSet) {
	int localSize = linkRef.length;

	// Correct level
	if (level == 1) {
	    // Step through T-tree array in current branch at current level
	    for (int index=2;index<localSize;index++) {
	        // If T-tree node exists (i.e. is supported) add next level
	    	if (linkRef[index] != null) {
			generateNextLevel(linkRef,index,
					     realloc2(itemSet,(short) index));
		    }
		}
	    }

	// Wrong level
	else {
	    for (int index=level;index<localSize;index++) {
	        // If T-tree node exists and it is has a child node proceed
	        if (linkRef[index]!=null && linkRef[index].childRef!=null)
		               generateLevelN(linkRef[index].childRef,level-1,
		    			     realloc2(itemSet,(short) index));
		}
	    }
	}

    /* GENERATE NEXT LEVEL */

    /** Generates a new level in the T-tree from a given "parent" node. <P>
    Example 1, given the following:

    <PRE>
    (A) ----- (B) ----- (C)
               |         |
	       |         |
	      (A)       (A) ----- (B)
    </PRE><P>
    where we wish to add a level 3 node to node (B), i.e. the node {A}, we
    would proceed as follows:
    <OL>
    <LI> Generate a new level in the T-tree attached to node (B) of length
    one less than the numeric equivalent of B i.e. 2-1=1.
    <LI> Loop through parent level from (A) to node immediately before (B).
    <LI> For each supported parent node create an itemset label by combing the
    index of the parent node (e.g. A) with the complete itemset label for B ---
    {C,B} (note reverse order), thus for parent node (B) we would get a new
    level in the T-tree with one node in it --- {C,B,A} represented as A.
    <LI> For this node to be a candidate large item set its size-1 subsets must
    be supported, there are three of these in this example {C,A}, {C,B} and
    {B,A}. We know that the first two are supported because they are in the
    current branch, but {B,A} is in another branch. So we must generate this
    set and test it. More generally we must test all cardinality-1 subsets
    which do not include the first element. This is done using the method
    <TT>testCombinations</TT>.
    </OL>
    <P>Example 2, given:
    <PRE>
    (A) ----- (D)
               |
	       |
	      (A) ----- (B) ----- (C)
	                           |
				   |
				  (A) ----- (B)
    </PRE><P>
    where we wish to add a level 4 node (A) to (B) this would represent the
    complete label {D,C,B,A}, the N-1 subsets will then be {{D,C,B},{D,C,A},
    {D,B,A} and {C,B,A}}. We know the first two are supported because they are
    contained in the current sub-branch of the T-tree, {D,B,A} and {C,B,A} are
    not.
    </OL>
    @param parentRef the reference to the level in the sub-branch of the T-tree
    under consideration.
    @param endIndex the index of the current node under consideration.
    @param itemSet the complete label represented by the current node (required
    to generate further itemsets to be X-checked). */

    protected void generateNextLevel(TtreeNode[] parentRef, int endIndex,
    			short[] itemSet) {
	parentRef[endIndex].childRef = new TtreeNode[endIndex];	// New level
        short[] newItemSet;

	// Generate a level in Ttree
	TtreeNode currentNode = parentRef[endIndex];

	// Loop through parent sub-level of siblings upto current node
	for (int index=1;index<endIndex;index++) {
	    // Check if "uncle" element is supported (i.e. it exists)
	    if (parentRef[index] != null) {
		// Create an appropriate itemSet label to test
	        newItemSet = realloc2(itemSet,(short) index);
		if (testCombinations(newItemSet)) {
		    currentNode.childRef[index] = new TtreeNode();
		    nextLevelExists=true;
		    }
	        else currentNode.childRef[index] = null;
	        }
	    }
	}

    /* TEST COMBINATIONS */

    /** Commences the process of testing whether the N-1 sized sub-sets of a
    newly created T-tree node are supported elsewhere in the Ttree --- (a
    process referred to as "X-Checking"). <P> Thus given a candidate large
    itemsets whose size-1 subsets are contained (supported) in the current
    branch of the T-tree, tests whether size-1 subsets contained in other
    branches are supported. Proceed as follows:
    <OL>
    <LI> Using current item set split this into two subsets:
    <P>itemSet1 = first two items in current item set
    <P>itemSet2 = remainder of items in current item set
    <LI> Calculate size-1 combinations in itemSet2
    <LI> For each combination from (2) append to itemSet1
    </OL>
    <P>Example 1:
    <PRE>
    currentItemSet = {A,B,C}
    itemSet1 = {B,A} (change of ordering)
    size = {A,B,C}-2 = 1
    itemSet2 = {C} (currentItemSet with first two elements removed)
    calculate combinations between {B,A} and {C}
    </PRE>
    <P>Example 2:
    <PRE>
    currentItemSet = {A,B,C,D}
    itemSet1 = {B,A} (change of ordering)
    itemSet2 = {C,D} (currentItemSet with first two elements removed)
    calculate combinations between {B,A} and {C,D}
    </PRE>
    @param currentItemSet the given itemset.		*/

    protected boolean testCombinations(short[] currentItemSet) {
	// No need to test 1- and 2-itemsets
        if (currentItemSet.length < 3) return(true);

	// Create itemSet1 (note ordering)

	short[] itemSet1 = new short[2];
	itemSet1[0] = currentItemSet[1];
	itemSet1[1] = currentItemSet[0];

	// Creat itemSet2

	int size = currentItemSet.length-2;
	short[] itemSet2 = removeFirstNelements(currentItemSet,2);

	// Calculate combinations

	return(combinations(null,0,2,itemSet1,itemSet2));
	}

    /* COMBINATIONS */

    /** Determines the cardinality N combinations of a given itemset and then
    checks whether those combinations are supported in the T-tree. <P>
    Operates in a recursive manner.
    <P>Example 1: Given --- sofarSet=null,
    startIndex=0, endIndex=2, itemSet1 = {B,A} and itemSet2 = {C}
    <PRE>
    itemSet2.length = 1
    endIndex = 2 greater than itemSet2.length if condition succeeds
    tesSet = null+{B,A} = {B,A}
    retutn true if {B,A} supported and null otherwise
    </PRE>
    <P>Example 2: Given --- sofarSet=null,
    startIndex=0, endIndex=2, itemSet1 = {B,A} and itemSet2 = {C,D}
    <PRE>
    endindex not greater than length {C,D}
    go into loop
    tempSet = {} + {C} = {C}
    	combinations with --- sofarSet={C}, startIndex=1,
			endIndex=3, itemSet1 = {B,A} and itemSet2 = {C}
	endIndex greater than length {C,D}
	testSet = {C} + {B,A} = {C,B,A}
    tempSet = {} + {D} = {D}
    	combinations with --- sofarSet={D}, startIndex=1,
			endIndex=3, itemSet1 = {B,A} and itemSet2 = {C}
	endIndex greater than length {C,D}
	testSet = {D} + {B,A} = {D,B,A}
    </PRE>
    @param sofarSet The combination itemset generated so far (set to null at
    start)
    @param startIndex the current index in the given itemSet2 (set to 0 at
    start).
    @param endIndex The current index of the given itemset (set to 2 at start)
    and incremented on each recursion until it is greater than the length of
    itemset2.
    @param itemSet1 The first two elements (reversed) of the total label for the
    current item set.
    @param itemSet2 The remainder of the current item set.
    */

    private boolean combinations(short[] sofarSet, int startIndex,
    		    int endIndex, short[] itemSet1, short[] itemSet2) {
	// At level
	if (endIndex > itemSet2.length) {
	    short[] testSet = append(sofarSet,itemSet1);
	    // If testSet exists in the T-tree sofar then it is supported
	    return(findItemSetInTtree(testSet));
	    }

	// Otherwise
	else {
	    short[] tempSet;
	    for (int index=startIndex;index<endIndex;index++) {
	        tempSet = realloc2(sofarSet,itemSet2[index]);
	        if (!combinations(tempSet,index+1,endIndex+1,itemSet1,
				itemSet2)) return(false);
	        }
	    }

	// Return
	return(true);
	}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                           ADD TO T-TREE                          */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* ADD TO T-TREE */

    /** Commences process of adding an itemset (with its support value) to a
    T-tree when using a T-tree either as a storage mechanism, or when adding to
    an existing T-tree.
    @param itemSet The given itemset. Listed in numeric order (not reverse
    numeric order!).
    @param support The support value associated with the given itemset. */

    public void addToTtree(short[] itemSet, int support) {
        // Determine index of last elemnt in itemSet.
	int endIndex = itemSet.length-1;

	// Add itemSet to T-tree.
        startTtreeRef = addToTtree(startTtreeRef,numOneItemSets+1,
			endIndex,itemSet,support);
	}

    /* ADD TO T-TREE */

    /** Inserts a node into a T-tree. <P> Recursive procedure.
    @param linkRef the reference to the current array in Ttree.
    @param size the size of the current array in T-tree.
    @param endIndex the index of the last element/attribute in the itemset,
    which is also used as a level counter.
    @param itemSet the given itemset.
    @param support the support value associated with the given itemset.
    @return the reference to the revised sub-branch of t-tree. */

    protected TtreeNode[] addToTtree(TtreeNode[] linkRef, int size, int endIndex,
    				short[] itemSet, int support) {
	// If no array describing current level in the T-tree or T-tree
	// sub-branch create one with "null" nodes.
	if (linkRef == null) {
	    linkRef = new TtreeNode[size];
	    for(int index=1;index<linkRef.length;index++)
			linkRef[index] = null;
	    }

	// If null node at index of array describing current level in T-tree
	// (T-tree sub-branch) create a T-tree node describing the current
	// itemset sofar.
	int currentAttribute = itemSet[endIndex];
	if (linkRef[currentAttribute] == null)
	    		linkRef[currentAttribute] = new TtreeNode();

	// If at right level add support
	if (endIndex == 0) {
	    linkRef[currentAttribute].support =
	    			linkRef[currentAttribute].support + support;
	    return(linkRef);
	    }

	// Otherwise proceed down branch and return
	linkRef[currentAttribute].childRef =
			addToTtree(linkRef[currentAttribute].childRef,
				currentAttribute,endIndex-1,itemSet,support);
	// Return
	return(linkRef);
	}

    /*---------------------------------------------------------------------- */
    /*                                                                       */
    /*                        T-TREE SEARCH METHODS                          */
    /*                                                                       */
    /*---------------------------------------------------------------------- */

    /* FIND ITEM SET IN T-TREE */

    /** Commences process of determining if an itemset exists in a T-tree. <P>
    Used to X-check existence of Ttree nodes when generating new levels of the
    Tree. Note that T-tree node labels are stored in "reverse", e.g. {3,2,1}.
    @param itemSet the given itemset (IN REVERSE ORDER).
    @return returns true if itemset found and false otherwise. */

    protected boolean findItemSetInTtree(short[] itemSet) {

    	// first element of itemset in Ttree (Note: Ttree itemsets stored in
	// reverse)
  	if (startTtreeRef[itemSet[0]] != null) {
    	    int lastIndex = itemSet.length-1;
	    // If "current index" is 0, then this is the last element (i.e the
	    // input is a 1 itemset) and therefore item set found
	    if (lastIndex == 0) return(true);
	    // Otherwise continue down branch
	    else if (startTtreeRef[itemSet[0]].childRef!=null) {
	        return(findItemSetInTtree2(itemSet,1,lastIndex,
			startTtreeRef[itemSet[0]].childRef));
	        }
	    else return(false);
	    }
	// Item set not in Ttree
    	else return(false);
	}

    /** Returns true if the given itemset is found in the T-tree and false
    otherwise. <P> Operates recursively.
    @param itemSet the given itemset.
    @param index the current index in the given T-tree level (set to 1 at
    start).
    @param lastIndex the end index of the current T-tree level.
    @param linRef the reference to the current T-tree level.
    @return returns true if itemset found and false otherwise. */

    protected boolean findItemSetInTtree2(short[] itemSet, int index,
    			int lastIndex, TtreeNode[] linkRef) {

        // Attribute at "index" in item set exists in Ttree
  	if (linkRef[itemSet[index]] != null) {
  	    // If attribute at "index" is last element of item set then item set
	    // found
	    if (index == lastIndex) return(true);
	    // Otherwise continue
	    else if (linkRef[itemSet[index]].childRef!=null) {
	        return(findItemSetInTtree2(itemSet,index+1,lastIndex,
	    		linkRef[itemSet[index]].childRef));
	        }
	    else return(false);
	    }
	// Item set not in Ttree
	else return(false);
    	}

    /* GET SUPPORT FOT ITEM SET IN T-TREE */

    /** Commences process for finding the support value for the given item set
    in the T-tree (which is know to exist in the T-tree). <P> Used when
    generating Association Rules (ARs). Note that itemsets are stored in
    reverse order in the T-tree therefore the given itemset must be processed
    in reverse.
    @param itemSet the given itemset.
    @return returns the support value (0 if not found). */

    protected int getSupportForItemSetInTtree(short[] itemSet) {
	int endInd = itemSet.length-1;

        // Test if endItem exists in top level.
        if (itemSet[endInd]>=startTtreeRef.length) return(0);

    	// Last element of itemset in Ttree (Note: Ttree itemsets stored in
	// reverse)
  	if (startTtreeRef[itemSet[endInd]] != null) {
	    // If "current index" is 0, then this is the last element (i.e the
	    // input is a 1 itemset)  and therefore item set found
	    if (endInd == 0) return(startTtreeRef[itemSet[0]].support);
	    // Otherwise continue down branch
	    else {
	    	TtreeNode[] tempRef = startTtreeRef[itemSet[endInd]].childRef;
	        if (tempRef != null) return(getSupForIsetInTtree2(itemSet,
							   endInd-1,tempRef));
	    	// No further branch therefore rerurn 0
		else return(0);
		}
	    }
	// Item set not in Ttree thererfore return 0
    	else return(0);
	}

    /** Returns the support value for the given itemset if found in the T-tree
    and 0 otherwise. <P> Operates recursively.
    @param itemSet the given itemset.
    @param index the current index in the given itemset.
    @param linRef the reference to the current T-tree level.
    @return returns the support value (0 if not found). */

    private int getSupForIsetInTtree2(short[] itemSet, int index,
    							TtreeNode[] linkRef) {
        // Element at "index" in item set exists in Ttree
  	if (linkRef[itemSet[index]] != null) {
  	    // If "current index" is 0, then this is the last element of the
	    // item set and therefore item set found
	    if (index == 0) return(linkRef[itemSet[0]].support);
	    // Otherwise continue provided there is a child branch to follow
	    else if (linkRef[itemSet[index]].childRef != null)
	    		          return(getSupForIsetInTtree2(itemSet,index-1,
	    		                    linkRef[itemSet[index]].childRef));
	    else return(0);
	    }
	// Item set not in Ttree therefore return 0
	else return(0);
    	}

    /* SET CHILD REF TO NULL */

    /** Commences process of setting child reference for the indicated node to
    null (used with respect to TFPC algorithm). <P>Note that itemsets are
    stored in reverse order in the T-tree therefore the given itemset must be
    processed in reverse.
    @param itemSet the given itemset. */

    protected void setChildRefToNull(short[] itemSet) {

    	// First element of itemset in Ttree (Note: Ttree itemsets stored in
	// reverse)
  	if (startTtreeRef[itemSet[0]] != null) {
	    int lastIndex = itemSet.length-1;
	    // If "current index" is 0, then this is the last element (i.e the
	    // input is a 1 itemset) and therefore item set found
	    if (lastIndex == 0) startTtreeRef[itemSet[0]] = null;
	    // Otherwise continue down branch
	    else if (startTtreeRef[itemSet[0]].childRef != null)
	        setChildRefToNull(itemSet,1,lastIndex,
			          startTtreeRef[itemSet[0]].childRef);
	    }
	}

    /** Sets the child reference for the indicated node to null.
    @param itemSet the given itemset.
    @param index the current index in the given itemset.
    @param lastIndex the end index of the current T-tree level.
    @param linRef the reference to the current T-tree level.  */

    private void setChildRefToNull(short[] itemSet, int index,
    					int lastIndex, TtreeNode[] linkRef) {
    	// Element at "index" in item set exists in Ttree
  	if (linkRef[itemSet[index]] != null) {
  	    // If attribute at "index" is last element of item set then item set
	    // found --- set child reference to null
	    if (index == lastIndex) linkRef[itemSet[index]] = null;
	    // Otherwise continue
	    else if (linkRef[itemSet[index]].childRef != null)
	    	setChildRefToNull(itemSet,index+1,lastIndex,
	    				    linkRef[itemSet[index]].childRef);
	    }
	}

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                SERIALIZE T-TREE WITHOUT SUPPORT VALUES                 */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* SERIALIZE T-TREE WITHOUT SUPPORT VALUES */

    /** Commences the process of serializing a T-tree, but without support
    values. <P> Used in with respect to distributed application of Toivonen's
    negative boarder concept. Three elements per node: (1) label, (2) childRef
    and (3) sibling ref. If no "null" childRef or siblingRef then -1 will be
    inserted. */

    protected void serializeTteeNoSupValues() {
        int newIndex=0, linkIndex=0;
	final int NODE_SIZE = 3;

	// Dimension serialization array and initialise serialization index to 0
	int size           = countNumberOfTtreeNodes()*NODE_SIZE;
	serializationArray = new int[size];
	serializationRef   =0;
	System.out.println("Serialized Ttree (No sup values) = " + (size*4) +
					" (bytes)");

        // Loop through Ttree
	for(int index=1;index<startTtreeRef.length;index++) {
	    // If non-null node add to serialisation array
	    if (startTtreeRef[index] !=null) {
	    	newIndex = addToSerializationArray(index);
		if (newIndex!=0) serializationArray[linkIndex+2] = newIndex;
		linkIndex = newIndex;
	        // Add ChildRef
		serializationArray[linkIndex+1] =
			serializeTteeNoSupValues(startTtreeRef[index].childRef);
	        }
	    }
	}

    /** Continues process of serializing a T-tree without including support
    values.
    @param linkRef the reference to the "current" portion of the tree. */

    private int serializeTteeNoSupValues(TtreeNode[] linkRef) {

	// Check for empty tree
	if (linkRef == null) return(-1);

        // Loop through child branch.
	int startIndex = -1;
	int newIndex=0, linkIndex=0;
        for(int index=1;index<linkRef.length;index++) {
	    if (linkRef[index] !=null) {
	        newIndex = addToSerializationArray(index);
	        if (startIndex==-1) startIndex = newIndex;
		else serializationArray[linkIndex+2] = newIndex;
		linkIndex = newIndex;
	        // Add ChildRef
		serializationArray[linkIndex+1] =
			serializeTteeNoSupValues(linkRef[index].childRef);
	        }
	    }

	// Return
	return(startIndex);
	}

    /* ADD TO SERIALIZATION ARRAY */

    /** Adds a T-tree node to the serialization array and returns the index.
    @param label the label for the T-tree node.
    @return the index to the node in the serialization array. */

    private int addToSerializationArray(int label) {
        int index = serializationRef;
	final int NODE_SIZE = 3;

        serializationArray[serializationRef] = label;
	serializationArray[serializationRef+1] = -1;
	serializationArray[serializationRef+2] = -1;

	// new reference
	serializationRef=serializationRef+NODE_SIZE;

	// Return index
	return(index);
	}

    /*--------------------------------------------------------------------- */
    /*                                                                      */
    /*                 SERIALIZE T-TREE WITH SUPPORT VALUES                 */
    /*                                                                      */
    /*--------------------------------------------------------------------- */

    /* SERIALIZE T-TREE */

    /** Commences the process of serializing a T-tree. <P> Used in distributed
    ARM application where entire trees or portions of trees need to be
    transmitted through a JavaSpace. Four elements per node: (1) label, (2)
    suppoprt, (3) childRef and (4) sibling ref. If no "null" childRef or
    siblingRef then -1 will be inserted. */

    protected void serializeTtee() {
        int newIndex=0, linkIndex=0;
	final int NODE_SIZE = 4;

	// Dimension serialization array and initialise serialization index to 0
	int size           = countNumberOfTtreeNodes()*NODE_SIZE;
	serializationArray = new int[size];
	serializationRef   = 0;
	System.out.println("Serialized Ttree = " + (size*4) + " (bytes)");

        // Loop through Ttree
	for(int index=1;index<startTtreeRef.length;index++) {
	    // If non-null node add to serialisation array
	    if (startTtreeRef[index] !=null) {
	    	newIndex = addToSerializationArray(index,
					startTtreeRef[index].support);
		if (newIndex!=0) serializationArray[linkIndex+3] = newIndex;
		linkIndex = newIndex;
	        // Add ChildRef
		serializationArray[linkIndex+2] =
				serializeTtee(startTtreeRef[index].childRef);
	        }
	    }
	}

    /** Continues process of serializing a T-tree.
    @param linkRef the reference to the "current" portion of the tree. */

    private int serializeTtee(TtreeNode[] linkRef) {
        // Check for empty tree
	if (linkRef == null) return(-1);


        // Loop through child branch.
	int startIndex = -1;
	int newIndex=0, linkIndex=0;
        for(int index=1;index<linkRef.length;index++) {
	    if (linkRef[index] !=null) {
	        newIndex = addToSerializationArray(index,
					linkRef[index].support);
	        if (startIndex==-1) startIndex = newIndex;
		else serializationArray[linkIndex+3] = newIndex;
		linkIndex = newIndex;
	        // Add ChildRef
		serializationArray[linkIndex+2] =
				serializeTtee(linkRef[index].childRef);
	        }
	    }

	// Return
	return(startIndex);
	}

    /* ADD TO SERIALIZATION ARRAY */

    /** Adds a T-tree node to the serialization array and returns the index.
    @param label the label for the T-tree node.
    @param support the associated support value.
    @return the index to the node in the serialization array. */

    private int addToSerializationArray(int label, int support) {
        int index = serializationRef;
	final int NODE_SIZE = 4;

        serializationArray[serializationRef] = label;
	serializationArray[serializationRef+1] = support;
	serializationArray[serializationRef+2] = -1;
	serializationArray[serializationRef+3] = -1;

	// new reference
	serializationRef=serializationRef+NODE_SIZE;

	// Return index
	return(index);
	}

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                        SERIALIZE T-TREE BRANCHS                        */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* SERIALIZE T-TREE BRANCHES */

    /** Commences the process of serializing a sequence of T-tree branches
    defined by a start and end label for the top level nodes in the tree. <P>
    Used in distributed ARM application where entire trees or portions of trees
    need to be transmitted through a JavaSpace.
    @param startIndex the start top level node in the T-tree.
    @param endIndex the end top level node in the T-tree. */

    protected void serializeTteeBranches(int startIndex, int endIndex) {
        int newIndex=0, linkIndex=0;

	// Dimension serialization array
	int size = countNumTtreeNodesInNbranches(startIndex,endIndex)*4;
	serializationArray = new int[size];
	System.out.println("Serialized Ttree branch = " + (size*4) + " (bytes)");

	// Initialise serialization index to 0
	serializationRef=0;

        // Loop through Ttree
	for(int index=startIndex;index<=endIndex;index++) {
	    // If non-null node add to serialisation array
	    if (startTtreeRef[index] !=null) {
	    	newIndex = addToSerializationArray(index,
					startTtreeRef[index].support);
		if (newIndex!=0) serializationArray[linkIndex+3] = newIndex;
		linkIndex = newIndex;
	        // Add ChildRef
		serializationArray[linkIndex+2] =
				serializeTtee(startTtreeRef[index].childRef);
	        }
	    }
	}

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                         SERIALIZE T-TREE LEVEL                         */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* SERIALIZE T-TREE LEVEL N */
    /** Commences the process of serializing level N of a T-tree. <P> Used in
    distributed ARM application where the count distribution (CD) approach is
    used.
    @param level the required level. */

    protected void serializeTteeLevelN(int level) {
        int newIndex=0, linkIndex=0;

	// Dimemsion serialization array
	int arraySize = countNumTtreeNodesLevelN(0,level,startTtreeRef) *
						(level+1);
	serializationArray = new int[arraySize];
	System.out.println("Serialized Ttree level = " + (arraySize*4) + " (bytes)");

	// Initialise serialization index to 0
	serializationRef=0;

        // Serialize
	serializeTteeLevelN(level,startTtreeRef,null);
	}

    /** Continues process of serializing all level N nodes of a T-tree.
    @param level the required level.
    @param linkRef the current location in the T-tree.
    @param itemSet the itemSet so far*/
    private void serializeTteeLevelN(int level, TtreeNode[] linkRef,
    						short[] itemSet) {

        // No nodes in this sub-branch level?
	if (linkRef == null) return;

	// If at right level serilize nodes
	if (level == 1) {
	    for(int index=1;index<linkRef.length;index++) {
	        if ((linkRef[index] != null) && (linkRef[index].support>0)) {
		    addToLevelNserializationArray(itemSet,index,
		    				linkRef[index].support);
		    }

	        }
	    }

	// If at wrong level proceed down child branches
	else {
	    for (int index=1;index<linkRef.length;index++) {
	        if (linkRef[index] != null) serializeTteeLevelN(level-1,
		   linkRef[index].childRef,realloc1(itemSet,(short) index));
		}
	    }
	}

    /* ADD TO LEVEL N SERIALIZATION ARRAY */
    /** Adds a T-tree node to the serialization array and returns the index.
    @param itemset the item set so far.
    @param lastAttribute the last attribute to form part of the total itemSet
    represent by the T-tree node.
    @param support the associated support value. */

    private void addToLevelNserializationArray(short[] itemSet, int
    	lastAttribute, int support) {

	// Add item set sofar
	if (itemSet != null) {
	    for (int itemSetIndex=0;itemSetIndex<itemSet.length;itemSetIndex++) {
	        serializationArray[serializationRef] = itemSet[itemSetIndex];
	        serializationRef++;
		}
	    }

	// Add last attribute number
	serializationArray[serializationRef] = lastAttribute;
	serializationRef++;

	// Add support value
	serializationArray[serializationRef] = support;
	serializationRef++;
	}

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                        UNPACK SERIALISATION                            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* MERGE SEIRIALIZATION WITH T-TREE */

    /** Commences process of merging a serialization representation of a T-tree
    with an existing Ttree. <P> Used in distribute ARM algorithms where a T-tree
    has been serialized to allow transmission through a JavaSpace. */

    protected void mergeSerializationAndTtree() {
	int indexSibRef = 0;

	// Process top level of serialization.
	while (indexSibRef != -1) {
	    // set value for current Ttree level array index
	    int index = serializationArray[indexSibRef];
	    // Check if node exists, if not create one
	    if (startTtreeRef[index] == null) {
	    	startTtreeRef[index] =
	    		       new TtreeNode(serializationArray[indexSibRef+1]);
	        numUpdates++;
		}
	    // Otherwise update support
	    else {
	        startTtreeRef[index].support = startTtreeRef[index].support +
			serializationArray[indexSibRef+1];
		numUpdates++;
		}
	    // Process child branch
	    startTtreeRef[index].childRef =
	    	       mergeSerializationAndTtree(startTtreeRef[index].childRef,
	    			       index,serializationArray[indexSibRef+2]);
	    // Increment serialization sibling index
	    indexSibRef = serializationArray[indexSibRef+3];
	    }
        }

    /** Continues process of merging a serialization representation of a T-tree
    with an existing Ttree. <P> Used in distribute ARM algorithms where a
    T-tree has been serialized to allow transmission through a JavaSpace.
    @param linkRef the reference to the current level in the given T-tree
    branch.
    @param size the size of the current level in the given T-tree branch.
    @param indexST the index of the current sibling in the serialization array.
    @return the revise local T-tree reference. */

    private TtreeNode[] mergeSerializationAndTtree(TtreeNode[] linkRef,
    					int size, int indexST) {

	// if no serialised data for this level branch return
	if (indexST == -1) return(linkRef);

        // If serialised data but no level array in this Ttree Branch create
	// new level array
	if (linkRef == null) linkRef = new TtreeNode[size];

	// Loop through serialization.
	while (indexST != -1) {
	    // set value for current Ttree level array index
	    int index = serializationArray[indexST];
	    // Check if node exists, if not create one
	    if (linkRef[index] == null) {
	        linkRef[index] = new TtreeNode(serializationArray[indexST+1]);
		numUpdates++;
		}
	    // Otherwise update support
	    else {
	        linkRef[index].support = linkRef[index].support +
					serializationArray[indexST+1];
		numUpdates++;
		}
	    // Process child branch
	    linkRef[index].childRef =
	    		mergeSerializationAndTtree(linkRef[index].childRef,
	    			       index,serializationArray[indexST+2]);
	    // Increment serialization sibling index
	    indexST = serializationArray[indexST+3];
	    }

	// Return
	return(linkRef);
        }

    /* CONVERT SEIRIALIZATION TO T-TREE (WITHOUT SUPPORT VALUES). */

    /** Commences process of converting a serialization representation of a
    T-tree (without support values) to a "proper" Ttree. <P> Used in distribute
    ARM algorithms where  */

    protected void serialization2TtreeNoSupValues() {
	int indexSibRef = 0;

	// Create top level in T-tree
	startTtreeRef = new TtreeNode[numOneItemSets+1];

	// Process top level of serialization.
	while (indexSibRef != -1) {
	    // set value for current Ttree level array index
	    int index = serializationArray[indexSibRef];
	    // Check if node exists, if not create one
	    if (startTtreeRef[index] == null) {
	    	startTtreeRef[index] = new TtreeNode(0);
		}
	    // Process child branch
	    startTtreeRef[index].childRef =
	    	   serialization2TtreeNoSupValue(startTtreeRef[index].childRef,
	    			       index,serializationArray[indexSibRef+1]);
	    // Increment serialization sibling index
	    indexSibRef = serializationArray[indexSibRef+2];
	    }
        }

    /** Continues process of converting a serialization representation of a
    T-tree (without support values) to a "proper" Ttree.  <P> Used in
    distribute ARM algorithms where a negative boarder T-tree has been
    serialized to allow transmission through a JavaSpace.
    @param linkRef the reference to the current level in the given T-tree
    branch.
    @param size the size of the current level in the given T-tree branch.
    @param indexST the index of the current sibling in the serialization array.
    @return the revise local T-tree reference. */

    private TtreeNode[] serialization2TtreeNoSupValue(TtreeNode[] linkRef,
    					int size, int indexST) {

	// if no serialised data for this level branch return
	if (indexST == -1) return(linkRef);

        // If serialised data but no level array in this Ttree Branch create
	// new level array
	if (linkRef == null) linkRef = new TtreeNode[size];

	// Loop through serialization.
	while (indexST != -1) {
	    // set value for current Ttree level array index
	    int index = serializationArray[indexST];
	    // Check if node exists, if not create one
	    if (linkRef[index] == null) {
	        linkRef[index] = new TtreeNode(0);
		numUpdates++;
		}
	    // Process child branch
	    linkRef[index].childRef =
	    		serialization2TtreeNoSupValue(linkRef[index].childRef,
	    			       index,serializationArray[indexST+1]);
	    // Increment serialization sibling index
	    indexST = serializationArray[indexST+2];
	    }

	// Return
	return(linkRef);
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                    ASSOCIATION RULE (AR) GENERATION                    */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* GENERATE ASSOCIATION RULES (SUPPORT-CONFIDENCE FRAMEWORK) */
    
    /** Initiates process of generating Association Rules (ARs) by processing
    the T-tree and storing rules above minimum confidence threshold in rule
    list. */

    public void generateARs() {
	// Command line interface output
	System.out.println("GENERATE ARs (support-confidence framework):\n" +
                             "--------------------------------------------");

        // Set flag and rule data structure to null
        supConfFworkFlag = true;
        startRulelist    = null;

	    // Generate
	    generateARs2();

        // Reset flag
        supConfFworkFlag = false;

        // Output number of generated rules
        numberRulesInBinTree();
        }

    /* GENERATE ASSOCIATION RULES (SUPPORT-LIFT FRAMEWORK) */

    /** Initiates process of generating Association Rules (ARs) by processing
    the T-tree and storing rules above minimum confidence threshold in rule
    list. */

    public void generateARsLift() {
	// Command line interface output
	System.out.println("GENERATE ARs (support-lift framework):\n" +
                                     "--------------------------------------");

        // Set flag and rule data structure to null
        supLiftFworkFlag = true;
	    startRulelist    = null;

	    // Generate
	    generateARs2();

        // Reset flag
        supLiftFworkFlag = false;

        // Number generated rule set
        numberRulesInBinTree();
	    }

    /* GENERATE ASSOCIATION RULES (SUPPORT-CONFIDENCE FRAMEWORK), GUI VERSION */

    /** Initiaites process of generating Association Rules (ARs) from a T-tree:
    GUI version.
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void generateARs(JTextArea textArea) {
	    // Set flag and rule data structure to null
        supConfFworkFlag = true;
        startRulelist    = null;

	    // Set rule data structure to null
	    startRulelist = null;

	    // Generate
	    generateARs2();

        // Reset flag
        supConfFworkFlag = false;

        // Number generated rule set
        numberRulesInBinTree();
	    }

    /* GENERATE ASSOCIATION RULES (SUPPORT-LIFT FRAMEWORK), GUI VERSION */

    /** Initiates process of generating Association Rules (ARs) by processing
    the T-tree and storing rules above minimum confidence threshold in rule
    list.
    @param textArea the given instance of the <TT>JTextArea</TT> class.  */

    public void generateARsLift(JTextArea textArea) {
	    // Set flag and rule data structure to null
        supLiftFworkFlag = true;
	    startRulelist    = null;

	    // Generate
	    generateARs2();

        // Reset flag
        supLiftFworkFlag = false;

        // Number generated rule set
        numberRulesInBinTree();
	    }

    /** Loops through top level of T-tree as part of the AR generation
    process. */

    protected void generateARs2() {
	    // Loop
	    for (short index=1;index <= numOneItemSets;index++) {
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) {
	            short[] itemSetSoFar = new short[1];
		        itemSetSoFar[0] = index;
		        generateARs(itemSetSoFar,index,startTtreeRef[index].childRef);
		        }
		    }
	    }
	}

    /* GENERATE ASSOCIATION RULES */

    /** Continues process of generating association rules from a T-tree by
    recursively looping through T-tree level by level.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree. */

    protected void generateARs(short[] itemSetSofar, int size,
    							TtreeNode[] linkRef) {

	// If no more nodes return
	if (linkRef == null) return;

	// Otherwise process
	for (int index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) {
		    	// Temp itemset
		    	short[] tempItemSet = realloc2(itemSetSofar,(short) index);
		    	// Generate ARs for current large itemset
		    	generateARsFromItemset(tempItemSet,linkRef[index].support);
	            // Continue generation process
		    	generateARs(tempItemSet,index,linkRef[index].childRef);
	            }
			}
	    }
	}

    /* GENERATE ASSOCIATION RULES */

    /** Generates all association rules for a given large item set found in a
    T-tree structure. <P> Called from <TT>generateARs</TT> method.
    @param itemSet the given large itemset.
    @param support the associated support value for the given large itemset. */
    
    protected void generateARsFromItemset(short[] itemSet, double support) {    
	    if (itemSet == null) return;
	    else {
	        // Fields
	        int itemIndex     = 0;
	        short[] combSofar = null;
	        // Get combinations
	        generateARsFromItemset(itemSet,itemIndex,combSofar,support);
	        }
	    }
	    
    /* OLD VERSION 29 APRIL 2010
    protected void generateARsFromItemset(short[] itemSet, double support) {
    	// Determine combinations
	    short[][] combinations = combinations(itemSet);

	    // Loop through combinations
	    for (int index=0;index<combinations.length;index++) {
            // Find complement of combination in given itemSet
	        short[] complement = complement(combinations[index],itemSet);
	        // If complement is not empty generate rule
	        if (complement != null) {
	            // Support confidence framework
                if (supConfFworkFlag) testRuleSupConfFwork(combinations[index],
                                                       complement,support);
	            // Support lift framework
                else if (supLiftFworkFlag)
                                      testRuleSupLiftFwork(combinations[index],
                                                           complement,support);
		        }
	        }
	    } */
	
    /** Recursively calculates all possible combinations of a given item 
    set. 
    @param inputSet the given item set.
    @param inputIndex the index within the input set marking current 
    element under consideration (0 at start).
    @param sofar the part of a combination determined sofar during the
    recursion (null at start). 
    @param support the support value for the given large itemset. */
    
    private void generateARsFromItemset(short[] inputSet, int inputIndex, 
    	        	short[] sofar, double support) {
    	short[] tempSet;
	    int index=inputIndex;
	
    	// Loop through input set
	    while(index < inputSet.length) {
            // Add item to itemset	    
            tempSet = realloc1(sofar,inputSet[index]);
            // Find complement of combination in given itemSet
	        short[] complement = complement(tempSet,inputSet);
	        // If complement is not empty generate rule
	        if (complement != null) {
	            // Support confidence framework
                if (supConfFworkFlag) testRuleSupConfFwork(tempSet,
                                                       complement,support);
	            // Support lift framework
                else if (supLiftFworkFlag)
                                      testRuleSupLiftFwork(tempSet,
                                                           complement,support);
    	        }
    	    // Loop
    	    generateARsFromItemset(inputSet,index+1,copyItemSet(tempSet),
    	                                                             support);	
    	    // Incremnet
    	    index++;
	        }  
       }

    /* TEST RULE USING SUPPORT CONFIDENCE FRAMEWORK */

    /** Tests given rule using support-confidence framework. */

    protected void testRuleSupConfFwork(short[] antecedent, short[] consequent,
                                                       double totalSupport) {
        // Calculate confidence for antecedent
        double confidenceForAR = getConfidence(antecedent,totalSupport);

        // Test confidence
        if (confidenceForAR >= confidence) insertRuleIntoRulelist(antecedent,
		     		    consequent,confidenceForAR,totalSupport);
		}

    /* TEST RULE USING SUPPORT LIFT FRAMEWORK */

    /** Tests given rule using support-lift framework. */

    protected void testRuleSupLiftFwork(short[] antecedent, short[] consequent,
                                                       double totalSupport) {
        // Calculate support for antecedent and consequent
		double anteSupport = (double) getSupportForItemSetInTtree(antecedent);
		double consSupport = (double) getSupportForItemSetInTtree(consequent);
        consSupport        = 100.0 * consSupport/numRows;

        // Calculate lift
        double confidence = 100.0*(((double) totalSupport)/anteSupport);
		double lift = confidence/consSupport;

		// If lift greater than 1 add to rule list, second ordinal value set
        // to 0.0.
		if (lift > 1.0) insertRuleIntoRulelist(antecedent,consequent,lift,0.0);
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                                SET METHODS                             */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* SET LEVELS IN T-TREE */
    /** Sets the levels in the T-tree field to the given value. <P>Used when
    passing information netween objects.
    @param numLevels the given number of levels in the T tree value. */

    public void setLevelsInTtree(int numLevels) {
        numLevelsInTtree = numLevels;
        }

    /* SET NUM UPDATES */
    /** Sets the number of T-tree updates.
    @param nUpdates the number if T-tree updates value. */

    public void setNumUpdates(long nUpdates) {
	numUpdates = nUpdates;
	}
    /** Sets flag indicating that T-tree statistics output is desired.
    @param value the value for the flag. */

    public void setOutputTtreeStatsFlag(boolean value) {
        outputTtreeStatsFlag = value;
        }

    /** Sets flag indicating that T-tree (as text) output is desired.
    @param value the value for the flag. */

    public void setOutputTtreeFlag(boolean value) {
        outputTtreeFlag = value;
        }

    /** Sets flag indicating that T-tree grpah output is desired.
    @param maxNodes The maximum number of Y-tree nodes that can be output to
    a graph.
    @param value the value for the flag. */

    public void setOutputTtreeGraphFlag(boolean value, int maxNodes) {
        outputTtreeGraphFlag = value;
        maxTtreeGraphNodes = maxNodes;
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                                GET METHODS                             */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* GET CONFIDENCE */

    /** Calculates and returns the confidence for an AR given the antecedent
    item set and the support for the total item set.
    @param antecedent the antecedent (LHS) of the AR.
    @param support the support for the large itemset from which the AR is
    generated.
    @return the associated confidence value (as a precentage) correct to two
    decimal places. */

    protected double getConfidence(short[] antecedent, double support) {
        // Get support for antecedent
        double supportForAntecedent = (double)
				getSupportForItemSetInTtree(antecedent);

	// Return confidence
	double confidenceForAR = ((double) support/supportForAntecedent)*10000;
	int tempConf = (int) confidenceForAR;
	confidenceForAR = (double) tempConf/100;
	return(confidenceForAR);
	}

    /* GET CONFIDENCE */

    /** Calculates and returns the confidence for an AR given the support for
    both the antecedent and the entire item set.
    @param antecedentSupp the support for antecedent (LHS) of the AR expressed
    as an absolute value (not a precentage).
    @param totalSupp the support for the large itemset from which the AR is
    generated.
    @return the associated confidence value. */

    protected double getConfidence(double antecedentSupp, double totalSupp) {
        // Return confidence
	double confidenceForAR = ((double) totalSupp/antecedentSupp)*10000;
	int tempConf = (int) confidenceForAR;
	confidenceForAR = (double) tempConf/100;
	return(confidenceForAR);
	}

    /* GET START OF T-TRRE */
    /** Returns the reference to the start of the T-tree.
    @return The start of the T-tree. */

    public TtreeNode[] getStartOfTtree() {
    	return(startTtreeRef);
	}

    /* GET NUMBER OF FREQUENT SETS */
    /** Returns number of frequent/large (supported) sets in T-tree.
    @return the number of support (frequent/large) sets. */

    public int getNumFreqSets() {
        return(numFrequentSets);
	}

    /* GET NUMBER OF FREQUENT ONE ITEM SETS */
    /** Returns number of frequent/large (supported) one itemsets in T-tree.
    @return the number of supported one itemsets. */

    public int getNnumFreqOneItemSets() {
	return(numOneItemSets);
	}

    /* GET MAXIMUM NUMBER OF FREQUENT SES */
    /** Returns the maximum number of frequent sets that may be generted.
    @return the value of the MAX_NUM_FREQUENT_SETS field. */

    public int getMaxNumFrequentSets() {
	return(MAX_NUM_FREQUENT_SETS);
	}

    /* GET MINIMUM SUPPORT VALUE */
    /** Returns the minimum support threshold value in terms of a number
    records.
    @return the minimum support value. */

    public double getMinSupport() {
	return(minSupport);
	}

    /* GET LEVELS IN T-TREE */
    /** Returns the levels in the T-tree.
    @return the level in the T tree value. */

    public int getLevelsInTtree() {
        return(numLevelsInTtree);
        }

    /* GET NUM UPDATES */
    /** Returns the number of T-tree updates.
    @return the number of t-tree upadtes. */

    public long getNumUpdates() {
	return(numUpdates);
	}

    /* ------------------------------------------------------------------- */
    /*                                                                     */
    /*                             COPY METHODS                            */
    /*                                                                     */
    /* ------------------------------------------------------------------- */

    /** Commences process of copying one T-tree to another T-tree. <P>Used
    in connection with the DIC algorithm.</P>
    @param startOldTtreeRef the reference to start of the old T-tree.      */

    public void copyTtree(TtreeNode[] startOldTtreeRef) {
        // Copy top row
        startTtreeRef = new TtreeNode[startOldTtreeRef.length];
        for (int index=0;index<startOldTtreeRef.length;index++) {
            if (startOldTtreeRef[index]!=null) {
                startTtreeRef[index] = new TtreeNode(startOldTtreeRef[index]);
                if (startOldTtreeRef[index].childRef!=null) {
                    startTtreeRef[index].childRef =
                              copyTtree2(startOldTtreeRef[index].childRef);
                    }

                }
             else startTtreeRef[index] = null;
             }
        }

    /** Continues process of recursively copying T-tree to new T-tree.
    <P>Used in connection with the DOC algorithm.</P>
    @param oldTtreeLevel the current level in the old T-tree.
    @return the current new level in the new T-tree. */

    protected TtreeNode[] copyTtree2(TtreeNode[] oldTtreeLevel) {
        // Create new T-tree level
        TtreeNode[] newTtreeLevel = new TtreeNode[oldTtreeLevel.length];

        // Popu;ate new Ttree level
        for (int index=0;index<oldTtreeLevel.length;index++) {
            if (oldTtreeLevel[index]!=null) {
                newTtreeLevel[index] = new TtreeNode(oldTtreeLevel[index]);
                if (oldTtreeLevel[index].childRef!=null) {
                    newTtreeLevel[index].childRef =
                              copyTtree2(oldTtreeLevel[index].childRef);
                    }
                }
             else newTtreeLevel[index] = null;
             }

        // Return
        return(newTtreeLevel);
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                              UTILITY METHODS                           */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* SET NUMBER ONE ITEM SETS */

    /** Sets the number of one item sets field (<TT>numOneItemSets</TT> to
    the number of supported one item sets. */

    public void setNumOneItemSets() {
        numOneItemSets=getNumSupOneItemSets();
	}

    /* SET SUPPORT REDUCTION VALUE */

    /** Set value for support reduction value field (the magnitude by which the
    support percentage threshold is to be reduced. <P>Dummy method the body
    for which is given in the <TT>TotalSupportTreeNegBorder</TT> class.
    @param newSupportReductionValue the new support reduction value. */

    public void setSupportReductionValue(double newSupportReductionValue) {
        }

    /* GET SUPPORT REDUCTION VALUE */

    /** Get value for support reduction value field (the magnitude by which the
    support percentage threshold is to be reduced. <P>Abstrat method the body
    for which is given in the <TT>TotalSupportTreeNegBorder</TT> class.
    @return the support reduction value. */

    public double getSupportReductionValue() {
        return(0.1);
        }

    /* COUNT T-TREE NODES */

    /** Commences process of counting the number of nodes in the T-tree. <P>
    Note: This is not the same as counting the number of nodes created as some
    of these may have been pruned. In the case of ARM they will have been
    pruned because they are not supported. In the case of CARM they will have
    neen pruned becauser either: (i) they are not supported  or (ii) they
    have been used in the generation of a classification rule and cannot be
    expected to be used as part of the the further classifiocation rule
    generation process.
    @return the number of nodes. */

    protected int countNumberOfTtreeNodes() {
        int counter = 0;

	// Loop
	for (int index=1; index < startTtreeRef.length; index++) {
	    if (startTtreeRef[index] !=null) {
	        counter++;
	        counter = countNumberOfTtreeNodes(counter,
						startTtreeRef[index].childRef);
		}
	    }

	// Return
	return(counter);
	}

    /* COUNT T-TREE NODES IN N SUB-BRANCHES*/

    /** Commences process of counting the number of nodes in a sequence of
    T-tree sub-branches.
    @return the number of nodes. */

    private int countNumTtreeNodesInNbranches(int startIndex, int endIndex) {
        int counter = 0;

	// Loop

	for (int index=startIndex; index <= endIndex; index++) {
	    if (startTtreeRef[index] !=null) {
	        counter++;
	        counter = countNumberOfTtreeNodes(counter,
						startTtreeRef[index].childRef);
		}
	    }

	// Return
	return(counter);
	}

    /** Continues process of counting number of nodes in a T-tree.
    @param counter the count sofar.
    @param linkref the reference to the current location in the T-tree.
    @return the updated count. */

    private int countNumberOfTtreeNodes(int counter,TtreeNode[] linkRef) {
        // Check for empty branch/sub-branch.
	if (linkRef == null) return(counter);

	// Loop through current level of branch/sub-branch.
	for (int index=1;index<linkRef.length;index++) {
	    if (linkRef[index] != null) {
	        counter++;
	        counter = countNumberOfTtreeNodes(counter,
						linkRef[index].childRef);
		}
	    }

	// Return
	return(counter);
	}

    /** Counts number of nodes in T-tree at level N.
    @param counter the count sofar.
    @param level the required level.
    @param linkref the reference to the current location in the T-tree.
    @return the updated count. */

    private int countNumTtreeNodesLevelN(int counter, int level,
    		TtreeNode[] linkRef) {
        // No nodes in this sub-branch level?
	if (linkRef == null) return(counter);

	// If at right level count nodes with support of 1 or more
	if (level == 1) {
	    for(int index=1;index<linkRef.length;index++) {
	        if ((linkRef[index] != null) && (linkRef[index].support>0))
								counter++;
	        }
	    }

	// If at wrong level proceed down child branches
	else {
	    for (int index=1;index<linkRef.length;index++) {
	        if (linkRef[index] != null) counter =
	    			countNumTtreeNodesLevelN(counter,level-1,
						linkRef[index].childRef);
		}
	    }

	// Return
	return(counter);
	}

    /* CALCULATE NUMBER OF LEVELS IN T-TREE */

    /** Commences process of calculating number of levels in T-tree. */

    protected void calcNumLevelsInTtree() {
        if (startTtreeRef==null) numLevelsInTtree = 0;

        // otherwise process
        numLevelsInTtree = calcNumLevelsInTtree(startTtreeRef);
        }

    /** Continues recursive process of obtaining levels in the T-tree.
    @param linkRef the reference to the current location in the T-tree.
    @return the local number of levels.  */
    protected int calcNumLevelsInTtree(TtreeNode[] linkRef) {
        int maxLength = 0;
        int length    = 0;

        // Loop through level
        for (int index=1;index<linkRef.length;index++) {
            if (linkRef[index]!=null) {
                if (linkRef[index].childRef!=null) length = 1 +
                             calcNumLevelsInTtree(linkRef[index].childRef);
                else length=1;
                if (maxLength<length) maxLength=length;
                }
            }

        // End
        return(maxLength);
        }

    /* CALCULATE NUMBER OF FREQUENT SETS */

    /** Commences process of calculating number of frequent sets in T-tree. */

    protected void calcNumFrequentSets() {
        if (startTtreeRef==null) numFrequentSets=0;

        // otherwise process
        calcNumFrequentSets(startTtreeRef);
        }

    /** Continues recursive process of obtaining number of frequent sets in the
    T-tree.
    @param linkRef the reference to the current location in the T-tree.  */

    protected void calcNumFrequentSets(TtreeNode[] linkRef) {
        // Loop through level
        for (int index=1;index<linkRef.length;index++) {
            if (linkRef[index]!=null) {
                if (linkRef[index].support>minSupport) numFrequentSets++;
                if (linkRef[index].childRef!=null)
                            calcNumFrequentSets(linkRef[index].childRef);
                }
            }
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                              OUTPUT METHODS                            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* Nine output options:
    
    (1)  Output T-tree (also GUI version)
    (2)  Output T-tree branch
    (3)  Output frequent sets (also GUI versions)
    (4)  Output number of frequent sets
    (5)  Output number of frequent sets per T-tree branch
    (6)  T-tree statistics (also GUI versions)   
    (7)  Output number of updates and nodes created
    (8)  Output T-tree storage
    (9)  Draw T-tree graph
    (10) Output duration (GUI version only)
    (11) Output serialization array
    (12) Output serialized T-tree array (with support values)
    (13) Output serialized T-tree array (no support values)
    (14) Output serialized T-tree for level N              */

    /* ---------------- */
    /* 1. OUTPUT T-TRRE */
    /* ---------------- */

    /** Commences process of outputting T-tree structure contents to screen. */
    public void outputTtree() {
	int number = 1;

	// Start
	System.out.println("T-TREE:\n-------");
	System.out.println("Format: [N] {I} = S, where N is the node " +
		"number, I is the item set and S the support.");

        // Check
        if (startTtreeRef==null) {
            System.out.println("Ttree empty!");
            return;
            }

	// Loop
	for (short index=1;index<startTtreeRef.length;index++) {
	    if (startTtreeRef[index] !=null) {
	        // Create itemset so far array
                short[] itemSetSofar = new short[1];
	        itemSetSofar[0] = index;
                // Output
                System.out.print("[" + number + "]");
                outputItemSet(itemSetSofar);
	        System.out.println("= " + startTtreeRef[index].support);
	        outputTtree(new Integer(number).toString(),itemSetSofar,
			                        startTtreeRef[index].childRef);
		number++;
		}
	    }

	// End
        System.out.println("--------------------------------");
	}

    /** Continue process of outputting T-tree. <P> Operates in a recursive
    manner.
    @param number the ID number of a particular node.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param linkRef the reference to the current array level in the T-tree. */

    private void outputTtree(String number, short[] itemSetSofar,
    				TtreeNode[] linkRef) {
	// Set output local variables.
	int num=1;
	number = number + ".";

	// Check for empty branch/sub-branch.
	if (linkRef == null) return;

	// Loop through current level of branch/sub-branch.
	for (short index=1;index<linkRef.length;index++) {
	    if (linkRef[index] != null) {
	        short[] newItemSet = realloc2(itemSetSofar,index);
	        System.out.print("[" + number + num + "]");
                outputItemSet(newItemSet);
	        System.out.println("= " + linkRef[index].support);
	        outputTtree(number + num,newItemSet,linkRef[index].childRef);
	        num++;
		}
	    }
	}

    /** Commences process of outputting T-tree structure contents to screen
    (GUI version).
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void outputTtree(JTextArea textArea) {
	int number = 1;

        // Start
	textArea.append("T-TREE:\n-------\n");
	textArea.append("Format: [N] {I} = S, where N is the node " +
		"number, I is the item set and S the support.\n");

        // Check
        if (startTtreeRef==null) {
            textArea.append("Ttree empty!\n--------------------------------");
            return;
            }

	// Loop
	for (short index=1; index < startTtreeRef.length; index++) {
	    if (startTtreeRef[index] !=null) {
	        // Create itemset so far array
                short[] itemSetSofar = new short[1];
	        itemSetSofar[0] = index;
                // Output
                textArea.append("[" + number + "]");
                outputItemSet(textArea,itemSetSofar);
	        textArea.append("= " + startTtreeRef[index].support + "\n");
	        outputTtree(textArea,new Integer(number).toString(),
                                itemSetSofar,startTtreeRef[index].childRef);
		number++;
		}
	    }

	// End
        textArea.append("------------------------------------\n");
        }

    /** Continue process of outputting T-tree. <P> Operates in a recursive
    manner.
    @param textArea the given instance of the <TT>JTextArea</TT> class.
    @param number the ID number of a particular node.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param linkRef the reference to the current array level in the T-tree. */

    private void outputTtree(JTextArea textArea, String number,
                                short[] itemSetSofar, TtreeNode[] linkRef) {
	// Set output local variables.
	int num=1;
	number = number + ".";

	// Check for empty branch/sub-branch.
	if (linkRef == null) return;

	// Loop through current level of branch/sub-branch.
	for (short index=1;index<linkRef.length;index++) {
	    if (linkRef[index] != null) {
	        short[] newItemSet = realloc2(itemSetSofar,index);
	        textArea.append("[" + number + num + "]");
	        outputItemSet(textArea,newItemSet);
	        textArea.append("= " + linkRef[index].support + "\n");
	        outputTtree(textArea,number + num,newItemSet,
                                                    linkRef[index].childRef);
	        num++;
		}
	    }
	}

    /* ----------------------- */
    /* 2. OUTPUT T-TREE BRANCH */
    /* ----------------------- */
    /** Commences process of outputting contents of a given T-tree branch to
    screen.
    @param linkRef the reference to the start of the branch*/

    public void outputTtreeBranch(TtreeNode[] linkRef) {
	int number = 1;

	// Check for empty tree

	if (linkRef == null) return;

	// Loop

	for (short index=1; index<linkRef.length; index++) {
	    if (linkRef[index] !=null) {
	        short[] newItemSet = new short[1];
                newItemSet[0] = index;
	        System.out.print("[" + number + "]");
	        outputItemSet(newItemSet);
	        System.out.println("= " + linkRef[index].support);
	        outputTtree(new Integer(number).toString(),newItemSet,
			                         linkRef[index].childRef);
		number++;
		}
	    }
	}

    /* ----------------------- */
    /* 3. OUTPUT FREQUENT SETS */
    /* ----------------------- */

    /* Three versions: (i) Output as attribute numbers, (ii) Output as
    attribute numbers (GUI version) and (iii) output as schema labels. */

    /** Commences the process of outputting the frequent sets contained in
    the T-tree. */

    public void outputFrequentSets() {
	int number = 1;

	System.out.println("FREQUENT (LARGE) ITEM SETS:\n" +
	                    	"---------------------------");
	System.out.println("Format: [N] {I} = S, where N is a sequential " +
		"number, I is the item set and S the support.");

	// Loop
	for (short index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) {
	            short[] itemSetSofar = new short[1];
	            itemSetSofar[0] = index;
	            System.out.print("[" + number + "]");
                    outputItemSet(itemSetSofar);
		    System.out.println("= " + startTtreeRef[index].support);
	            number = outputFrequentSets(number+1,itemSetSofar,
		    		       index,startTtreeRef[index].childRef);
		    }
		}
	    }

	// End
	System.out.println("\n");
	}

    /** Outputs T-tree frequent sets. <P> Operates in a recursive manner.
    @param number the number of frequent sets so far.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @return the incremented (possibly) number the number of frequent sets so
    far. */

    private int outputFrequentSets(int number, short[] itemSetSofar, int size,
    							TtreeNode[] linkRef) {

	// No more nodes
	if (linkRef == null) return(number);

	// Otherwise process
	for (short index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) {
	            short[] newItemSet = realloc2(itemSetSofar,index);
	            System.out.print("[" + number + "]");
                    outputItemSet(newItemSet);
		    System.out.println("= " + linkRef[index].support);
	            number = outputFrequentSets(number + 1,newItemSet,index,
		    			           linkRef[index].childRef);
	            }
		}
	    }

	// Return
	return(number);
	}

    /* OUTPUT FREQUENT SETS (GUI VERSION) */
    /** Commences the process of outputting the frequent sets contained in
    the T-tree (GUI version).
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void outputFrequentSets(JTextArea textArea) {
	int number = 1;

	textArea.append("Format: [N] {I} = S, where N is a sequential " +
		"number, I is the item set and S the support.\n");

	// Loop
	for (short index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) {
	            short[] itemSetSofar = new short[1];
	            itemSetSofar[0] = index;
	            textArea.append("[" + number + "]");
                    outputItemSet(textArea,itemSetSofar);
		    textArea.append("= " + startTtreeRef[index].support + "\n");
	            number = outputFrequentSets(textArea,number+1,itemSetSofar,
		    			  index,startTtreeRef[index].childRef);
		    }
		}
	    }

	// End
	System.out.println("\n");
	}

    /** Outputs T-tree frequent sets (GUI version). <P> Operates in a
    recursive manner.
    @param textArea the given instance of the <TT>JTextArea</TT> class.
    @param number the number of frequent sets so far.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @return the incremented (possibly) number the number of frequent sets so
    far. */

    private int outputFrequentSets(JTextArea textArea, int number,
                        short[] itemSetSofar, int size, TtreeNode[] linkRef) {

	// No more nodes
	if (linkRef == null) return(number);

	// Otherwise process
	for (short index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) {
	            short[] newItemSet = realloc2(itemSetSofar,index);
	            textArea.append("[" + number + "]");
                    outputItemSet(textArea,newItemSet);
                    textArea.append("= " + linkRef[index].support + "\n");
		    number = outputFrequentSets(textArea,number + 1,newItemSet,
                                                index,linkRef[index].childRef);
	            }
		}
	    }

	// Return
	return(number);
	}

    /** Commences the process of outputting the frequent sets contained in
    the T-tree as series of schema labels (GUI version).
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void outputFrequentSetsSchema(JTextArea textArea) {
	int number = 1;

	textArea.append("Format: [N] {I} = S, where N is a sequential " +
		"number, I is the item set and S the support.\n");

	// Loop
	for (short index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) {
	            short[] itemSetSofar = new short[1];
	            itemSetSofar[0] = index;
	            textArea.append("[" + number + "]");
                    outputItemSetSchema(textArea,itemSetSofar);
                    textArea.append("= " + startTtreeRef[index].support + "\n");
	            number = outputFrequentSetsSchema(textArea,number+1,
                          itemSetSofar,index,startTtreeRef[index].childRef);
		    }
		}
	    }

	// End

	System.out.println("\n");
	}

    /** Outputs T-tree frequent sets (GUI version). <P> Operates in a
    recursive manner.
    @param textArea the given instance of the <TT>JTextArea</TT> class.
    @param number the number of frequent sets so far.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @return the incremented (possibly) number the number of frequent sets so
    far. */

    private int outputFrequentSetsSchema(JTextArea textArea, int number,
                        short[] itemSetSofar, int size, TtreeNode[] linkRef) {

	// No more nodes
	if (linkRef == null) return(number);

	// Otherwise process
	for (short index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) {
	            short[] newItemSet = realloc2(itemSetSofar,index);
	            textArea.append("[" + number + "]");
                    outputItemSetSchema(textArea,newItemSet);
                    textArea.append("= " + linkRef[index].support + "\n");
		    number = outputFrequentSetsSchema(textArea,number + 1,
                                     newItemSet,index,linkRef[index].childRef);
	            }
		}
	    }

	// Return
	return(number);
	}

    /* ------------------------------ */
    /* 4. OUTPUT NUMBER FREQUENT SETS */
    /* ------------------------------ */

    /** Commences the process of counting and outputing number of supported
    nodes in the T-tree.<P> A supported set is assumed to be a non null node
    in the T-tree. */

    public void outputNumFreqSets() {

	// If empty tree (i.e. no supported sets) do nothing
	if (startTtreeRef== null) System.out.println("Number of frequent " +
					"sets = 0");
	// Otherwise count and output
	else System.out.println("Number of frequent sets = " + numFrequentSets);
	}

    /* COUNT NUMBER OF FRQUENT SETS */
    /** Commences process of counting the number of frequent (large/supported)
    sets contained in the T-tree. */

    /*
NO LONGER REQYIRED NOVEMBER 2006
    protected int countNumFreqSets() {
        // If empty tree return 0
	if (startTtreeRef ==  null) return(0);

	// Otherwise loop through T-tree starting with top level
	int num=0;
	for (int index=1; index <= numOneItemSets; index++) {
	    // Check for null valued top level Ttree node.
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport)
			num = countNumFreqSets(index,
	    				startTtreeRef[index].childRef,num+1);
		}
	    }

	// Return
	return(num);
	} */

    /** Counts the number of supported nodes in a sub branch of the T-tree.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @param num the number of frequent sets sofar. */

    /*
NO LONGER REQYIRED NOVEMBER 2006
    protected int countNumFreqSets(int size, TtreeNode[] linkRef, int num) {

	if (linkRef == null) return(num);

	for (int index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport)
	            			num = countNumFreqSets(index,
					linkRef[index].childRef,num+1);
		}
	    }

	// Return

	return(num);
	}*/

    /* --------------------------------------------------- */
    /* 5. OUTPUT NUMBER OF FREQUENT SETS PER T-TREE BRANCH */
    /* --------------------------------------------------- */
    /** Outputs the number of supported sets per T-tree branch descending from
    the top-level of the tree. <P> Used for diagnostic purposes. */

    /*
NO LONGER REQYIRED NOVEMBER 2006
    public void outputNumFreqSetsPerBranch() {

	System.out.println("Number of frequent sets per branch");


	for (int index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) {
	        System.out.println("(" + index + ")" + countNumFreqSets(index,
					startTtreeRef[index].childRef,1));
		}
	    }
	}*/

    /* --------------------------- */
    /* 6. OUTPUT T-TREE STATISTICS */
    /* --------------------------- */
    /** Commences the process of outputting T-tree statistics (for diagnostic
    purposes): (a) Storage, (b) Number of nodes on P-tree, (c) number of
    partial support increments (updates) and (d) generation time. */

    public void outputTtreeStats() {
        System.out.println("T-TREE STATISTICS (ARM)\n-----------------");
		System.out.println(numLevelsInTtree + " Levels in T-tree");
        System.out.println(TtreeNode.getNumberOfNodes() + " Total # nodes " +
                                                          "created");
		System.out.println(numUpdates + " Total # support value increments");
        System.out.println(numFrequentSets + " # Frequent sets");
        System.out.println(calculateStorage() + " Total storage (Bytes)" +
                                              " on completion");
		System.out.println("-----------------------------------");
		}

    /** Commences the process of outputting T-tree statistics (GUI version).
    @param textArea the given instance of the <TT>JTextArea</TT> class. */

    public void outputTtreeStats(JTextArea textArea) {
        textArea.append("T-TREE STATISTICS (ARM)\n-----------------\n");
	textArea.append(numLevelsInTtree + " Levels in T-tree\n");
        textArea.append(TtreeNode.getNumberOfNodes() + " Total # nodes " +
                                                       "created\n");
	textArea.append(numUpdates + " Total # support value increments\n");
	textArea.append(numFrequentSets + " Frequent sets\n");
        textArea.append(calculateStorage() + " Total storage (Bytes)" +
                                              " on completion\n");
	textArea.append("-----------------------------------\n");
        }

    /* --------------------------- */
    /* 7. OUTPUT NUMBER OF UPDATES */
    /* --------------------------- */

    /** Outputs the number of update and number of nodes created during the
    generation of the T-tree (the later is not the same as the number of
    supported nodes). */

    public void outputNumUpdates() {
	System.out.println("Number of T-tree nodes created = " +
			TtreeNode.getNumberOfNodes());
	System.out.println("Number of T-tree Updates       = " + numUpdates);
	}

    /* ----------------- */
    /* 8. OUTPUT STORAGE */
    /* ----------------- */
    /** Commences the process of determining and outputting the storage
    requirements (in bytes) for the T-tree. <P> Example: Given ---
    <PRE>
        	{1,2,3}
    		{1,2,3}
		{1,2,3}
    		{1,2,3}
		{1,2,3}
    </PRE>
    This will produce a T-tree as shown below:
    <PRE>
    +---+---+---+---+
    | 0 | 1 | 2 | 3 |
    +---+---+---+---+
          |   |   |
	  |   |   +-----------+
	  |   |               |
	  |   +---+         +---+---+---+
          |       |         | 0 | 1 | 2 |
	( 5 )   +---+---+   +---+---+---+
	(nul)   | 0 | 1 |         |   |
	        +---+---+         |   +----+
		      |           |        |
		      |           |      +---+---+
		    ( 5 )         |      | 0 + 1 |
		    (nul)       ( 5 )    +---+---+
	                        (nul)          |
				               |
					     ( 5 )
					     (nul)
    </PRE>
    0 elements require 4 bytes of storage, null nodes (not shown above) 4 bytes
    of storage, others 12 bytes of storage.			*/

    public void outputStorage() {
	// If empty tree (i.e. no supported sets) do nothing
	if (startTtreeRef ==  null) return;

	/* Otherwise calculate storage */
	System.out.println("T-tree Storage          = " + calculateStorage() +
			" (Bytes)");
	}

    /* CALCULATE STORAGE */
    /** Commences process of calculating storage requirements for  T-tree. */

    protected int calculateStorage() {
        // If emtpy tree (i.e. no supported sets) return 0
	if (startTtreeRef ==  null) return(0);

	/* Step through top level */
	int storage = 4;	// For element 0
	for (int index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) storage = storage + 12 +
	    		calculateStorage(0,startTtreeRef[index].childRef);
	    else storage = storage+4;
	    }
	// Return
	return(storage);
	}

    /** Calculate storage requirements for a sub-branch of the T-tree.
    @param localStorage the storage as calculated sofar (set to 0 at start).
    @param linkRef the reference to the current sub-branch of the T-tree. */

    private int calculateStorage(int localStorage, TtreeNode[] linkRef) {
	if (linkRef == null) return(0);

	// Loop through current level in current sub-nranch of T-tree
	for (int index=1; index < linkRef.length; index++) {
	    if (linkRef[index] !=null) localStorage = localStorage + 12 +
	    		calculateStorage(0,linkRef[index].childRef);
	    else localStorage = localStorage + 4;
	    }

	 /* Return */
	 return(localStorage+4);	// For element 0
	 }

    /* ---------------------- */
    /* 9. OUTPUT T TREE GRAPH */
    /* ---------------------- */

    /** Draws T-tree graph to a seperate window. */

    /* public void drawTtreeGraph() {
        // If more than 1000 frequent sets return
        if (getNumFreqSets()>maxTtreeGraphNodes) {
	    JOptionPane.showMessageDialog(null,"TOO MANY FREQUENT SETS:\n" +
	       	       "Only T-trees with less than " + maxTtreeGraphNodes +
			    "\nlarge sets can be graphically presented.\n");
	    return;
	    }

		// Otherwise process
		TtreeWindow tTreeWinApp = new TtreeWindow(numOneItemSets,startTtreeRef,
					                           minSupport);
        tTreeWinApp.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        } */

    /* ------------------- */
    /* 10. OUTPUT DURATION */
    /* ------------------- */

    /* OUTPUT DURATION (GUI VERSION). */
    /** Outputs difference between two given times.
    @param textArea the given instance of the <TT>JTextArea</TT> class.
    @param time1 the first time.
    @param time2 the second time.   */

    public void outputDuration(JTextArea textArea, double time1, double time2) {
        double duration = (time2-time1)/1000;
	textArea.append("Generation time = " + twoDecPlaces(duration) +
			" seconds (" + twoDecPlaces(duration/60) + " mins)\n");
	}

    /* ------------------------------ */
    /* 11. OUTPUT SERIALISATION ARRAY */
    /* ------------------------------ */
    /** Outputs a serialised Ttree as a sequence of integers. <P> Used for
    distributed T-tree applications. */

    protected void outputSerializationArray() {
        for (int index=0;index<serializationArray.length;index=index+4) {
	    System.out.println("(" + index + ") {" + serializationArray[index] +
	    "} = " + serializationArray[index+1] + ", " +
	    serializationArray[index+2] + ", " + serializationArray[index+3]);
	    }
        }

    /* ------------------------------------------------- */
    /* 12. OUTPUT SERIALIZED T-TREE (WITH SUPPORT VALUES) */
    /* ------------------------------------------------- */
    
    /** Commences process of outputting a serialised Ttree structure contents
    to screen. <P> Used for distributed T-tree applications. */

    protected void outputSerializedTtree() {
        int number   = 1;
	int indexSibRef = 0;

	while (indexSibRef != -1) {
	    String label = new
			Integer(serializationArray[indexSibRef]).toString();
	    System.out.println("[" + number + "] {" + label + "} = " +
				serializationArray[indexSibRef+1]);
	    outputSerializedTtree(new Integer(number).toString(),
				label,serializationArray[indexSibRef+2]);
	    // Increment node number
	    number++;
	    indexSibRef = serializationArray[indexSibRef+3];
	    }
        }

    /** Continue process of outputting a serialised T-tree. <P> Operates in a
    recursive manner.
    @param number the ID number of a particular node.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param indexSibRef the index to the current sibling node. */

    protected void outputSerializedTtree(String number, String itemSetSofar,
    						int indexSibRef) {
	// Set output local variables.
	int num=1;
	number = number + ".";
	itemSetSofar = itemSetSofar + " ";

	// Check for empty branch/sub-branch.
	if (indexSibRef == -1) return;

	// Loop through current level of branch/sub-branch.
	while (indexSibRef != -1) {
	    String label = new
			Integer(serializationArray[indexSibRef]).toString();
	    System.out.println("[" + number + num + "] {" + itemSetSofar +
	    		label + "} = " + serializationArray[indexSibRef+1]);
	    String newitemSet = itemSetSofar + label;
	    outputSerializedTtree(number + num,newitemSet,
					serializationArray[indexSibRef+2]);
	    num++;
	    indexSibRef = serializationArray[indexSibRef+3];
	    }
	}

    /* ------------------------------------------------ */
    /* 13. OUTPUT SERIALIZED T-TREE (NO SUPPORT VALUES) */
    /* ------------------------------------------------ */
    /** Commences process of outputting a serialised Level N Ttree structure
    contents to screen where support values have been omitted. <P> Used for
    distributed T-tree applications. */

    protected void outputSerializedTtreeNoSupValues() {
	int number   = 1;
	int indexSibRef = 0;

	while (indexSibRef != -1) {
	    String label = new
			Integer(serializationArray[indexSibRef]).toString();
	    System.out.println("[" + number + "] {" + label + "}");
	    outputSerializedTtreeNoSupValues(new Integer(number).toString(),
				label,serializationArray[indexSibRef+1]);
	    // Increment node number
	    number++;
	    indexSibRef = serializationArray[indexSibRef+2];
	    }
	}

    /** Continue process of outputting a serialised T-tree without support
    values. <P>Operates in a recursive manner.
    @param number the ID number of a particular node.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param linkRef the reference to the current array level in the T-tree. */

    private void outputSerializedTtreeNoSupValues(String number, String itemSetSofar,
    						int indexSibRef) {
	// Set output local variables.
	int num=1;
	number = number + ".";
	itemSetSofar = itemSetSofar + " ";

	// Check for empty branch/sub-branch.
	if (indexSibRef == -1) return;

	// Loop through current level of branch/sub-branch.
	while (indexSibRef != -1) {
	    String label = new
			Integer(serializationArray[indexSibRef]).toString();
	    System.out.println("[" + number + num + "] {" + itemSetSofar +
	    		label + "}");
	    String newitemSet = itemSetSofar + label;
	    outputSerializedTtreeNoSupValues(number + num,newitemSet,
					serializationArray[indexSibRef+1]);
	    num++;
	    indexSibRef = serializationArray[indexSibRef+2];
	    }
	}

    /* ------------------------------------ */
    /* 14. OUTPUT SERIALIZED T-TREE LEVEL N */
    /* ------------------------------------ */
    /** Commences process of outputting a serialised Level N Ttree structure
    contents to screen. <P> Used for distributed T-tree applications.
    @param level the required level*/

    protected void outputSerializedTtreeLevelN(int level) {

        for(int index=0,number=1;index<serializationArray.length;
			index=index+level+1,number++) {
	    System.out.print("[" + number + "] { ");
	    int localIndex;
	    for (localIndex=0;localIndex<level;localIndex++) {
	    	System.out.print(serializationArray[index+localIndex] + " ");
                }
	    System.out.println("} = " + serializationArray[index+localIndex]);
	    }
        }
    }


