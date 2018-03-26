
/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                            Apriori-TFP CLASSIFIER                          */
/*                                                                            */
/*                                Frans Coenen                                */
/*                                                                            */
/*                           Monday 2 February 2004                           */
/*            (Revised: 20/1/2005, 6/10/2005, 12/10/2006, 4/3/2012)           */
/*                                                                            */
/*                       Department of Computer Science                       */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/* Class structure

AssocRuleMining
      |
      +-- TotalSupportTree
                |
		+-- PartialSupportTree
			  |
			  +-- AprioriTFPclass		*/

// Java packages

import java.util.*;
import java.io.*;

// Java GUI packages
import javax.swing.*;

/* To compile:

javac -d ~frans/LUCS-KDD/ -classpath ~frans/LUCS-KDD/ AprioriTFPclass.java */

/** Methods to produce classification rules using a Apriori-TFP appraoch. Assumes
that input dataset is orgnised such that classifiers are at the end of each
record. Note: number of classifiers value is stored in the <TT>numClasses</TT>
field in <TT>AssocRuleMining</TT> class.
@author Frans Coenen
@version 3 March 2012 */

public class AprioriTFPclass extends PartialSupportTree {

    /* ------ FIELDS ------ */

    // Limits
    /** The maximum number of rules to be considered per class when classifying
    records during testing using "first best K" approach; usually set to 5. */
    protected int kValue = 5;
    /** Maximum size of classification rule antecedent. */
    protected final int MAX_SIZE_OF_ANTECEDENT = 6;

    // Data structures

    /** 2-D array to hold the test data. <P> Note that classifiaction
    involves producing a set of Classification Rules (CRs) from a training
    set and then testing the effectiveness of the CRs on a test set. */
    protected short[][] testDataArray = null;
    /** 3-data array to hold 10th sets of input data. <P> Used in
    conjunction with "10 Cross Validation" where the input data is divided
    into 10 subsets and CRs are produced using each subset in turn and validated
    against the remaininmg 9 sets. The oveerall average accuracy is then the
    total accuracy divided by 10. */
    protected short[][][] tenthDataSets = new short[10][][];

    // Flags
    /** Output rule list using attribute numbers flag, true if desired false
    otherwise. */
    protected boolean ruleListAttNumOutputFlag = false;
    /** Output rule list flag using input schema, true if desired false
    otherwise. */
    protected boolean ruleListSchemaOutputFlag = false;

    // Other fields
    /** Number of rows in input data set, not the same as the number of rows
    in the classification training set. <P> Used for temporery storage of total
    number of rows when using Ten Cross Validation (TCV) approach only. <P> The
    <TT>numRows</TT> field inherited from the super class records is used
    throughout the CR generation process. Set to number of rows in the training
    set using <TT>setNumRowsInInputSet</TT> method called by application
    class. */
    protected int numRowsInInputSet;
    /** Number of rows in test set, again not the same as the number of rows
    in the classification training set. */
    protected int numRowsInTestSet;
    /** Number of columns in test set, used for diagnostic purposes only ---
    should be same as in training set. */
    protected int numColsInTestSet;
    /** Number of rows in training set, also not the same as the number of rows
    in the classification training set. */
    protected int numRowsInTrainingSet;
    /** Percentage describing classification accuarcy. */
    protected double accuracy;
    /** Number of classification rules generated. */
    protected int numCRs;
    /** AUC (Area Under the receiver operating Curve) value. */
    protected double aucValue;
    /** Instance of the class JTEXTArea. */
    protected JTextArea textArea = null;

    // Diagnostic fields
    /** Average accuracy as the result of TCV. */
    protected double averageAccuracy;
    /** Average AIUC value as the result of TCV. */
    protected double averageAUCvalue; 
    /** Standard deviation from average accuracy. */
    protected double sdAccuracy;
    /** Average number of frequent sets as the result of TCV. */
    protected double averageNumFreqSets;
    /** Average number of updates as the result of TCV. */
    protected double averageNumUpdates;
    /** Average number of callsification rules as the result of TCV. */
    protected double averageNumCRs;

    /* ------ CONSTRUCTORS ------ */

    /** Constructor with command line arguments to be processed.
    @param args the command line arguments (array of String instances). */

    public AprioriTFPclass(String[] args) {
	super(args);
	TtreeNode.setNumberOfNodesFieldToZero();
	}

    /** Constructor with argument from existing instance of class
    AssocRuleMining.
    @param armInstance the given instance of the <TT>AssocRuleMining</TT>
    class. */

    public AprioriTFPclass(AssocRuleMining armInstance) {
	super(armInstance);
        numClasses = armInstance.numClasses;
	TtreeNode.setNumberOfNodesFieldToZero();
        }

    /** Default constructor. */

    public AprioriTFPclass() {
	TtreeNode.setNumberOfNodesFieldToZero();
	}

    /* ------ METHODS ------ */

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                        COMMAND LINE ARGUMENTS                    */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* IDENTIFY ARGUMENT */

    /** Identifies nature of individual command line agruments: -C = confidence, 
    -F = file name, -N = number of classes, -O = output file name, -S = support,
     -T = test file name. <P>(Overides higher level method.)
    @param argument the given argument. */

    protected void idArgument(String argument) {
	if (argument.length()<3) {
	    JOptionPane.showMessageDialog(null,"Command line argument '" +
                               argument + "' too short.","COMMAND LINE " +
                                 "INPUT ERROR",JOptionPane.ERROR_MESSAGE);
            errorFlag = false;
            }
        else if (argument.charAt(0) == '-') {
	    char flag = argument.charAt(1);
	    argument = argument.substring(2,argument.length());
	    switch (flag) {
		case 'C':
	            confidence = Double.parseDouble(argument);
		    break;
	        case 'F':
	    	    fileName = argument;
		    break;
		case 'N':
		    numClasses = Integer.parseInt(argument);
		    break;
		case 'O':
		    outputFileName = argument;
		    setOutputRuleSetToFileFlag(true);
		    break;
	        case 'S':
	            support = Double.parseDouble(argument);
		    break;
		case 'T':
		    testSetFileName = argument;
		    break;
	        default:
	            JOptionPane.showMessageDialog(null,"Unrecognise command "+
                                  "line argument: '" + flag + argument + "'.",
                        "COMMAND LINE INPUT ERROR",JOptionPane.ERROR_MESSAGE);
		    errorFlag = false;
	        }
            }
        else {
	    JOptionPane.showMessageDialog(null,"All command line arguments " +
                   "must commence with a '-' character ('" + argument + "').",
                        "COMMAND LINE INPUT ERROR",JOptionPane.ERROR_MESSAGE);
            errorFlag = false;
            }
	}

    /* CHECK INPUT ARGUMENTS */

    /** Invokes methods to check values associated with command line arguments
    (overides higher level method). */

    protected void CheckInputArguments() {

	// Check support and confidence input
	checkSupportAndConfidence();

	// Check file name
	checkFileName();

	// Check number of classes
	checkNumClasses();

	// Return
	if (errorFlag) outputSettings();
	else outputMenu();
	}

    /* CHECK NUMBER OF CLASSES */

    /** Checks if number of classes command line parameter has been set
    appropriately. */

    private void checkNumClasses() {
	if (numClasses == 0) {
	    JOptionPane.showMessageDialog(null,"Must specify number of " +
                                "classes (-N)","COMMAND LINE INPUT ERROR",
                                               JOptionPane.ERROR_MESSAGE);
	    errorFlag = false;
	    }
	if (numClasses < 0) {
	    JOptionPane.showMessageDialog(null,"Number of classes must be a " +
	        		 "positive integer","COMMAND LINE INPUT ERROR",
                                                    JOptionPane.ERROR_MESSAGE);
	    errorFlag = false;
	    }
	}

    /* -------------------------------------------------------------- */
    /*                                                                */
    /*                     READ TEST SET FROM FILE                    */
    /*                                                                */
    /* -------------------------------------------------------------- */

    /* INPUT TEST DATA SET */

    /** Commences process of loading the test data set (called from application
    class when training and test data sets are to be provided separately). */

    public void inputTestDataSet() {
        // Read the file
		readTestFile();

		// Check ordering (only if input format is OK)
		if (inputFormatOkFlag) {
	    	if (checkTestSetOrdering()) {
	        	countNumColsInTestSet();
                String s = "Num. records in testset = " + numRowsInTestSet +
                                            "\nNum. columns in testset = " +
                                                    numColsInTestSet + "\n";
				System.out.print(s);
                if (numColsInTestSet != numCols) {
                    JOptionPane.showMessageDialog(null,"Number of columns " +
                               "in test set is not the\nsame as number of " +
                              "columns in training set\n","FILE INPUT ERROR",
                                                  JOptionPane.ERROR_MESSAGE);
	            	System.exit(1);
		    		}
				}
	    	else {
	        	JOptionPane.showMessageDialog(null,"Unable to read file " +
                                        fileName + "\n","FILE INPUT ERROR",
                                                JOptionPane.ERROR_MESSAGE);
	        	closeFile();
	        	System.exit(1);
				}
	   		 }

		// Set number of rows in training set. If we have a specific user
		// defined test data set then the entire data array is the training
		// set, not the case where the input data is split 50:50 or when
		// using TCV.
		numRowsInTrainingSet = numRows;
		}

    /* READ TEST FILE */

    /** Reads test data from file specified in command line argument
    <TT>testSetFileName</TT>. <P>Note that it is assumed that no empty records
    are included. Proceeds as follows:
    <OL>
    <LI>Gets number of rows (lines) in file, checking format of each line
    (space separated integers), if incorrectly formatted line found
    <TT>inputFormatOkFlag</TT> set to <TT>false</TT>.
    <LI>Dimensions input array.
    <LI>Reads data
    </OL> */

    private void readTestFile() {
        try {
	    // Dimension data structure. getNumberOfLines is in AssocRuleMining
	    // class.
	    inputFormatOkFlag=true;
	    numRowsInTestSet = getNumberOfLines(testSetFileName);
	    if (inputFormatOkFlag) {
	        testDataArray = new short[numRowsInTestSet][];
	        // Read file
		System.out.println("Reading test set input file: " +
							testSetFileName);
	        readTestDataSet();
		}
	    else JOptionPane.showMessageDialog(null,"Unable to read file: " +
	    		 	   testSetFileName + "\n","FILE INPUT ERROR",
                                                  JOptionPane.ERROR_MESSAGE);
	    }
	catch(IOException ioException) {
	    JOptionPane.showMessageDialog(null,"Unable to read file","FILE " +
                                     "INPUT ERROR",JOptionPane.ERROR_MESSAGE);
	    closeFile();
	    System.exit(1);
	    }
	}

    /* READ TEST DATA SET */
    /** Reads test data from file specified in command line argument. */

    public void readTestDataSet() throws IOException {
        int rowIndex=0;

	// Open the file
	openFileName(testSetFileName);

	// Get first row.
	String line = fileInput.readLine();

	// Preocess rest of file
	while (line != null) {
	    // Process line
	    if (!processTestSetLine(line,rowIndex)) break;
	    // Increment first (row) index in 2-D data array
	    rowIndex++;
	    // get next line
            line = fileInput.readLine();
	    }

	// Close file
	closeFile();
	}

    /* PROCESS TEST SET LINE */

    /**	Processes a line from the test set file and places it in the
    <TT>testDataArray</TT> structure.
    @param line the line to be processed from the input file
    @param rowIndex the index to the current location in the
    <TT>testDataArray</TT> structure.
    @rerturn true if successfull, false if empty record. */

    private boolean processTestSetLine(String line, int rowIndex) {
        // If no line return false
	if (line==null) return(false);

	// Tokenise line
	StringTokenizer dataLine = new StringTokenizer(line);
        int numberOfTokens = dataLine.countTokens();

	// Empty line or end of file found, return false
	if (numberOfTokens == 0) return(false);

	// Convert input string to a sequence of short integers
	short[] code = binConversion(dataLine,numberOfTokens);

	// Dimension row in 2-D dataArray
	int codeLength = code.length;
	testDataArray[rowIndex] = new short[codeLength];
	// Assign to elements in row
	for (int colIndex=0;colIndex<codeLength;colIndex++)
		testDataArray[rowIndex][colIndex] = code[colIndex];

	// Return
	return(true);
	}

    /* CHECK TEST SET ORDERING */
    /** Checks that test set is ordered correctly.
    @return true if appropriate ordering, false otherwise. */

    protected boolean checkTestSetOrdering() {
        boolean result = true;

	// Loop through input data
	for(int index=0;index<testDataArray.length;index++) {
	    if (!checkLineOrdering(index+1,testDataArray[index])) {
		result=false;
		}
	    }

	// Return
	return(result);
	}

    /* COUNT NUMBER OF COLUMNS IN TEST SET */
    /** Counts number of columns in test set data. */

    private void countNumColsInTestSet() {
        int maxAttribute=0;

	// Loop through data array
        for(int index=0;index<testDataArray.length;index++) {
	    int lastIndex = testDataArray[index].length-1;
	    if (testDataArray[index][lastIndex] > maxAttribute)
	    		maxAttribute = testDataArray[index][lastIndex];
	    }

	numColsInTestSet = maxAttribute;
	}

    /* RECAST INPUT DATA. */

    /** Recasts the contents of the test data array so that each record is
    ordered according to conversion array.
    <P>Proceed as follows:

    1) For each record in the test data array. Create an empty new itemSet
       array.
    2) Place into this array attribute/column numbers that correspond to the
       appropriate equivalents contained in the conversion array.
    3) Reorder this itemSet and return into the test data array. */

    public void recastTestData() {
        short[] itemSet;
	int attribute;

	// Step through data array using loop construct

        for(int rowIndex=0;rowIndex<testDataArray.length;rowIndex++) {
	    itemSet = new short[testDataArray[rowIndex].length];
	    // For each element in the itemSet replace with attribute number
	    // from conversion array
	    for(int colIndex=0;colIndex<testDataArray[rowIndex].length;
	    							colIndex++) {
	        attribute = testDataArray[rowIndex][colIndex];
		itemSet[colIndex] = (short) conversionArray[attribute][0];
		}
	    // Sort itemSet and return to data array
	    sortItemSet(itemSet);
	    testDataArray[rowIndex] = itemSet;
	    }
	}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                      DATA SET UTILITIES                          */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* REORDER INPUT DATA: */

    /** Reorders input data according to frequency of single attributes but
    excluding classifiers which are left unordered at the end of the attribute
    list. <P> Overides method in <TT>AssocRuleMining</TT> class. Note
    reordering makes for more efficient executuion of the T-tree (and P-tree)
    algorithms. Identical method in <TT>AprioriTclass</TT> class.     */

    public void idInputDataOrdering() {
	// Count singles and store in countArray;
	int[][] countArray = countSingles();

	// Bubble sort count array on support value (second index)
	orderFirstNofCountArray(countArray,numCols-numClasses);

        // Define conversion and reconversion arrays
        defConvertArrays(countArray,numCols-numClasses);

	// Set sorted flag
	isOrderedFlag = true;
	}

    /* PRUNE UNSUPPORTED ATTRIBUTES */

    /** Removes single attributes (not classifiers) from input data set which
    do not meet the minimum support requirement. Identical method in
    <TT>AprioriTclass</TT> class.   */

    public void pruneUnsupportedAtts() {
        short[] itemSet;
	int attribute;

	// Step through data array using loop construct

        for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) {
	    // Check for empty row
	    if (dataArray[rowIndex]!= null) {
	        itemSet = null;
	        // For each attribute in the current record (not the classifier)
		// find if supported with reference to the conversion array. If
		// so add to "itemSet".
		int maxLength = dataArray[rowIndex].length-1;
	    	for(int colIndex=0;colIndex<maxLength;colIndex++) {
	            attribute = dataArray[rowIndex][colIndex];
		    // Check support
		    if (conversionArray[attribute][1] >= minSupport) {
		        itemSet = reallocInsert(itemSet,
		    			(short) conversionArray[attribute][0]);
		        }
		    }
		// Add classifier
		itemSet = reallocInsert(itemSet,
					dataArray[rowIndex][maxLength]);
	        // Return new item set to data array
	        dataArray[rowIndex] = itemSet;
	 	}
	    }

	// Reset number of one item sets field
        numOneItemSets = getNumSupOneItemSets();

	// Adjust classifiers
	recastClassifiers();
	}

    /* RECAST CLASSIFIERS */

    /** Adjusts classifier IDs in data array where attributes have been pruned
    using <TT>pruneUnsupportedAtts</TT> method. <P> Proceeds by looping
    through data table and subtracting a value equal to the number of removed
    attributes from the value of the last element (the classifier) in each
    record. Identical method in <TT>AprioriTclass</TT> class.      */

    private void recastClassifiers() {
        short difference = (short) (numCols-numOneItemSets);

        // Step through data array using loop construct
	int lastIndex;
        for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) {
	    lastIndex = dataArray[rowIndex].length-1;
	    dataArray[rowIndex][lastIndex] =
	    		  (short) (dataArray[rowIndex][lastIndex]-difference);
	    }

	// Adjust reconversion arry
        int startIclassesInRA = numOneItemSets-numClasses+1;
        short classNum = (short) (numCols-numClasses+1);
        for (int index=0;index<numClasses;index++) {
            reconversionArray[startIclassesInRA+index] = classNum;
            classNum++;
            }

        // Pad reconversion array
        for (int index=startIclassesInRA+numClasses;
                                     index<reconversionArray.length;index++) {
            reconversionArray[index]=0;
            }
        }

    /* GET NUM OF SUPPORTE ONE ITEM SETS */
    /** Gets number of supported attributess (note this is not necessarily
    the same as the number of columns/attributes in the input set) plus the
    number of classifiers. <P> Overides parent method which returns the number
    of support 1 itemsets. This would exclude any classifiers whose support
    value was below the minimum support threshold.
    @return Number of supported 1-item stes */

    protected int getNumSupOneItemSets() {
        int counter = 0;

	// Step through conversion array incrementing counter for each
	// supported element found

	int length = conversionArray.length-numClasses;
	for (int index=1;index < length;index++) {
	    if (conversionArray[index][1] >= minSupport) counter++;
	    }

	// Return
	return(counter+numClasses);
	}

    /* CREATE TRAINING AND TEST DATA SETS. */

    /** Populates test and training datasets. <P> Note: (1) assumes a 50:50
    split, (2) training data set is stored in the dataArray structure in which
    the input data is stored, (3) method called from application class as same
    training and test sets may be required if using (say) "hill climbing"
    approach to maximise accuracy, (4) method is not called from constructor
    partly for same reason as 3 but also because the input data set may (given
    a particular application) first require ordering and possibly also pruning
    and recasting (see recastClassifiers method). */

    public void createTrainingAndTestDataSets() {
        // Determine size of training and test sets.
		final double percentageSizeOfTestSet = 50.0;
        numRowsInTestSet     = (int) ((double) numRows*
					percentageSizeOfTestSet/100.0);
		numRowsInTrainingSet = numRows-numRowsInTestSet;
		numRows              = numRowsInTrainingSet;

		// Dimension and populate training set.
		short[][] trainingSet = new short[numRowsInTrainingSet][];
		int index1=0;
		for (;index1<numRowsInTrainingSet;index1++)
				trainingSet[index1] = dataArray[index1];

		// Dimension and populate test set
		testDataArray = new short[numRowsInTestSet][];
		for (int index2=0;index1<dataArray.length;index1++,index2++)
	 			testDataArray[index2] = dataArray[index1];

		// Assign training set label to input data set label.
		dataArray = trainingSet;
		}

    /** Populates test and training datasets. <P> Note: (1) works on a 9:1
    split with nine of the tenths data sets forming the training set and
    the remaining one tenth the test set, (2) training data set is stored in
    the same dataArray structure in which the initial input data is stored,
    (3) this method is not called from the constructor as the input data set may
    (given a particular application) first require ordering and possibly also
    pruning.
    @param testSetIndex the index of the tenths data sets to be used as the
    test set. */

    public void createTrainingAndTestDataSets(int testSetIndex) {
        // Dimension and populate test set.
        numRowsInTestSet = tenthDataSets[testSetIndex].length;
		testDataArray    = tenthDataSets[testSetIndex];
        // Dimension of and populate training set.
		numRowsInTrainingSet = numRowsInInputSet-numRowsInTestSet;
		numRows              = numRowsInTrainingSet;
		short[][] trainingSet = new short[numRows][];
		int trainingSetIndex=0;
		// Before test set
		for(int index=0;index<testSetIndex;index++) {
	    	for (int tenthsIndex=0;tenthsIndex<tenthDataSets[index].length;
								tenthsIndex++,trainingSetIndex++) {
	        	trainingSet[trainingSetIndex] =
								tenthDataSets[index][tenthsIndex];
	        	}
	    	}
		// After test set
		for(int index=testSetIndex+1;index<tenthDataSets.length;index++) {
	    	for (int tenthsIndex=0;tenthsIndex<tenthDataSets[index].length;
							tenthsIndex++,trainingSetIndex++) {
	        	trainingSet[trainingSetIndex] =
						tenthDataSets[index][tenthsIndex];
	        	}
	    	}

		// Assign training set label to input data set label.
		dataArray = trainingSet;
		}

    /* CREATE TENTHS DATA SETS. */

    /** Populates ten tenths data sets for use when doing Ten Cross Validation
    (TCV) --- test and training datasets. <P> Note: this method is not called
    from the constructor as the input data set may (given a particular
    application) first require ordering (and possibly also pruning!). */

    public void createTenthsDataSets() {
		// If number of rows is less than 10 cannot create appropriate data
		// sets
		if (numRows<10) {
	    	JOptionPane.showMessageDialog(null,"Only " + numRows +
                             ", therefore cannot create tenths data sets!",
                                    "TCV ERROR",JOptionPane.ERROR_MESSAGE);
	    	System.exit(1);
	    	}

		// Determine size of first nine tenths data sets.
		int tenthSize = numRows/10;
		int remainder = numRows - (tenthSize*10);
	
		// Dimension first n tenths data sets where n isequal to the reaminder
		int index=0;
		for(int i=0;i<remainder;i++,index++)
			tenthDataSets[index] = new short[tenthSize+1][];
		// Dimension remaining tenths data set
		for(;index<tenthDataSets.length;index++) 
			tenthDataSets[index] = new short[tenthSize][];
	
		// Populate tenth data sets
		int inputDataIndex=0;
		for(index=0;index<tenthDataSets.length;index++) {
	    	for(int tenthIndex=0;tenthIndex<tenthDataSets[index].length;
	    				tenthIndex++,inputDataIndex++) {
	        	tenthDataSets[index][tenthIndex] = dataArray[inputDataIndex];
				}
	    	}
		}

    /*------------------------------------------------------------------ */
    /*                                                                   */
    /*                       START CLASSIFICATION                        */
    /*                                                                   */
    /*------------------------------------------------------------------ */

    /* START CLASSIFICATION */

    /** 	Starts classifier generation process.	*/

    public void startClassification() {

        /* STUBB */

        }

    /** Starts classifier generation process, GUI version.
    @param tArea the text area to output data to.
    @return The classification accuarcay (%).		*/

    public void startClassification(JTextArea tArea) {

        /* STUBB */

		}

    /*------------------------------------------------------------------ */
    /*                                                                   */
    /*             APRIORI-TFP WITH TEN CROSS VALIDATION (TCV)           */
    /*                                                                   */
    /*------------------------------------------------------------------ */

    /* COMMEMCE TEN CROSS VALIDATION WITH OUTPUT*/

    /** Start Ten Cross Validation (TCV) process with output of individual
    accuracies. */

    public void commenceTCVwithOutput() {
        /* STUBB */
        }

    /** Start Ten Cross Validation (TCV) process with output of individual
    accuracies, GUI version.
    @param tArea the given instance of the <TT>JTextArea</TT> class. */

    public void commenceTCVwithOutput(JTextArea tArea) {
        textArea = tArea;

        /* STUBB */
        }

    /* ---------------------------------------------------------- */
    /*                                                            */
    /*                    HILL CLIMBING METHODS                   */
    /*                                                            */
    /* ---------------------------------------------------------- */

    /* START HILL CLIMBING */

    /** Commences hill climbing process. <P> Finds optimum support and
    confidence values to provide best classification accuracy through a hill
    climbing process. Classification Algorithm uses a 3-D "hill climbing"
    method.
    @param startDsupp the start change in support threshold.
    @param startDconf the start changein confidence threshold.
    @param minDsupp the minimum change in support threshold.
    @param minDconf the minimum change in confidence threshold. */

    public void startHillClimbing(double startDsupp, double startDconf,
    				double minDsupp, double minDconf) {
    	/* STUBB */
    	}


    /* START HILL CLIMBING */

    /** Commences hill climbing process. <P> Finds optimum support and
    confidence values to provide best classification accuracy through a hill
    climbing process. Classification Algorithm uses a 2-D "hill climbing"
    method (GUI version).
    @param tArea the text area to output data to.
    @param startDsupp the start change in support threshold.
    @param startDconf the start changein confidence threshold.
    @param minDsupp the minimum change in support threshold.
    @param minDconf the minimum change in confidence threshold. */

    public void startHillClimbing(JTextArea tArea, double startDsupp,
                       double startDconf, double minDsupp, double minDconf) {
        /* STUBB */
        }


    /* ------------------------------------------------ */
    /*                                                  */
    /*              IDENTIFY DEFAULT RULE               */
    /*                                                  */
    /* ------------------------------------------------ */

    /*  FIND DEFAULT RULE */

    /** Top level method for procedure to identify a default rule (used only in
    conjunction with rules that have a single attribute consequent, e.g.
    classification rules). <P>Identical method in <TT>AprioriTclass</TT>. */

    protected void findDefaultRule() {
        // Check for empty ruleset.
        if (numRules==0) return;

        // Get consequent of last rule
        int ruleCounter     = numRules;
        short[] consequent1 = getConsequentOfRuleN(ruleCounter);
    	while (true) {
            ruleCounter--;
            if (ruleCounter==0) {
                ruleCounter++;
                break;
                }
            short[] consequent2 = getConsequentOfRuleN(ruleCounter);
            if (consequent1[0]!=consequent2[0])  {
                ruleCounter++;
                break;
                }
            }

        // Generate new rule list
        RuleNode linkRuleNode = startRulelist;
        startRulelist=null;
        numRules = ruleCounter;
        copyFirstNrules(numRules,linkRuleNode);
        }

    /* ------------------------------------------------------------- */
    /*                                                               */
    /*                         CLASSIFIER                            */
    /*                                                               */
    /* ------------------------------------------------------------- */

    /* CLASSIFY RECORD (HIGHEST CONFIDENCE AND DEFAULT RULE) */

    /** Searches through rule data looking for a rule antecedent which is a
    subset of the input set or the default rule (last rule). <P> Copy of
    method in AprioriTclass.
    @param itemSet the record to be classified.
    @return the classification. */

    protected short classifyRecordDefault(short[] itemSet) {
        return(classifyRecordDefault(itemSet,startRulelist));
        }

    /** Continues process of searching through rule data looking for a rule
    antecedent which is a subset of the input set or the default rule (last
    rule).
    @param itemSet the record to be classified.
    @param node the currentNode.
    @return the classification. */

    protected short classifyRecordDefault(short[] itemSet, RuleNode node) {
        // Process node
        if (node != null) {
	    // Left branch
            short consClass = classifyRecordDefault(itemSet,node.leftBranch);
	    if (consClass!=0) return(consClass);
            // Node
            if ((node.ruleNumber==numRules) ||
               (isSubset(node.antecedent,itemSet))) return(node.consequent[0]);
	    // Right branch
            return(classifyRecordDefault(itemSet,node.rightBranch));
	    }

        // Return
        return(0);
        }

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                  TEST CLASSIFICATION (BEST FIRST)                */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* TEST CLASSIFICATION */
    /** Tests the generated classification rules using the test set and returns
    percentage accuracy. */

    protected void testClassification() {
		// Check if test data and classifier exists, if not return '0'.
		if (!canTestingBeUndertaken()) {
			accuracy=0.0;
			aucValue=0.0;
			return;
	    	}
	    	
	    // Dimension signal (truth) and response tables. Singal table is the "ground
	    // truth", response table is what is produced by the generated classifier.
		int signalTable[][]   = new int[testDataArray.length][numClasses];
		int responseTable[][] = new int[testDataArray.length][numClasses];
		for (int i=0;i<signalTable.length;i++) {
			for (int j=0;j<signalTable[i].length;j++) {
				signalTable[i][j]=0;
				}
			}
		for (int i=0;i<responseTable.length;i++) {
			for (int j=0;j<responseTable[i].length;j++) {
				responseTable[i][j]=0;
				}
			}
		
		// Set Counters	and varaiables
		int correctClassCounter = 0;
		int wrongClassCounter   = 0;	
		int unclassifiedCounter = 0;
		int offset = numOneItemSets-numClasses+1;
		
		// Commernce testing. Loop through test set
		int index=0;
    	for(;index<testDataArray.length;index++) {
	    	// Note: classifyRecord methods are contained in the 
	    	// AssocRuleMining class. To classify without default use 
	    	// classifyRecord, with defualt use classifyRecordDefault.
            short classResult = 
	               classifyRecordDefault(testDataArray[index]);
	    	if (classResult==0) unclassifiedCounter++;
	    	else {
	        	// Get actual class and add to signal table
	        	short classActual = getLastElement(testDataArray[index]);
	        	int classIndex = classActual-offset;
	        	signalTable[index][classIndex]=1;
	    		// Add to response table.
            	classIndex = classResult-offset;
	        	responseTable[index][classIndex]=1;
	        	// Update counters
				if (classResult == classActual) correctClassCounter++;
	        	else wrongClassCounter++;
				}
	    	}
        
		// Calculate accuracy
		accuracy = ((double) correctClassCounter*100.0/(double) index);

    	// CalculateAUC value.
    	double valueAtotal = 0.0;
    	for (int i=0;i<numClasses-1;i++) {
			for (int j=i+1;j<numClasses;j++) {
				double mww_ij = calcMWWstatValue(i,j,responseTable,signalTable);
				double mww_ji = calcMWWstatValue(j,i,responseTable,signalTable);
				double valueA = (mww_ij + mww_ji)/2;
				valueAtotal = valueAtotal + valueA;
				}
			}
    	
    	// Calculate and assign AUC value
    	aucValue = (2.0/(numClasses*(numClasses-1.0))) * valueAtotal;	
		}
    	
    /** Claculate the MWW (Man-Whitney-Wilcoxon) statistic for the given class 
    pair.
    @param i response MWW table column class identifier.
    @param j signal MWW table columnclass identifier.
    @param responseTable the response table.
    @param signalTable the signal table.
    @return the MWWW statistic value. */
    
    protected double calcMWWstatValue(int class_i, int class_j, int[][] responseTable, 
    				int[][] signalTable) {
    	// Find number ofrecords in signal (truth) table feature class i or j 
    	int numRecords = 0;
    	for (int i=0;i<signalTable.length;i++) {
			if (signalTable[i][class_i]==1 || signalTable[i][class_j]==1)
						numRecords++;
			}
		
		// Populate MWW table
		int mwwTable[][] = new int[numRecords][2];
		int index = 0;
		for (int i=0;i<signalTable.length;i++) {
			if (signalTable[i][class_i]==1 || signalTable[i][class_j]==1) {
				mwwTable[index][0] = responseTable[i][class_i];
				mwwTable[index][1] = signalTable[i][class_i];
				index++;
				}
			}
					
		// Rank MWWW Table
		rankMWWtable(mwwTable);

		// Calculate n1 and n2
		int n1 = 0;
		int n2 = 0;
		int totalRanking = 0;
		for (int i=0;i<mwwTable.length;i++) {
			if (mwwTable[i][1]==1) {
				n1++;
				totalRanking = totalRanking+i+1;
				}
			else n2++;
			}
    	
    	// Calculare MWW statistic and return.
		double mwwValue;
		if (n1==0 || n2==0) mwwValue = 0.0;
    	else mwwValue = ((double) totalRanking-(((double) n1*(n1+1))/2.0))/(double) (n1*n2);
    	return(mwwValue);
    	}
    	
    /** Rank MWWW table */
    
    private void rankMWWtable(int[][] mwwTable) {
        int temp1, temp2;	
        boolean isOrdered;
        int index;

        do {
	    	isOrdered = true;
            index     = 0;
            while (index < (mwwTable.length-1)) {
                if (isBeforeInMWWtable(mwwTable[index][0],mwwTable[index][1],
                			mwwTable[index+1][0],mwwTable[index+1][1])) index++;
	        	else {
	            	isOrdered=false;
                    // Swap
		    		temp1                = mwwTable[index][0];
		    		temp2                = mwwTable[index][1];
	            	mwwTable[index][0]   = mwwTable[index+1][0];
	            	mwwTable[index][1]   = mwwTable[index+1][1];
                    mwwTable[index+1][0] = temp1;
                    mwwTable[index+1][1] = temp2;
	            	// Increment index
		    		index++;
	            	}
	  			}
	    	} while (isOrdered==false);
    	}
    
    /** Determines whether one MWW table record ius fanked before another.
    
    Schema: Response1, Signal1, Response2, Signal2.
    {0,1,0,1} equal
    {0,1,0,0} before
    {0,1,1,0} before
    {0,1,1,1} before
    {0,0,0,1} after (swop)
    {0,0,0,0} equal
    {0,0,1,0} before
    {0,0,1,1} before
    {1,0,0,1} after  (swop)
    {1,0,0,0} after  (swop)
    {1,0,1,0} equal
    {1,0,1,1} before
    {1,1,0,1} after  (swop)
    {1,1,0,0} after  (swop)
    {1,1,1,0} after  (swop)
    {1,1,1,1} equal		
    
    @param response1 response value (1 or 0) for record 1.
    @param signal1 signal value (1 or 0) for record 1.
    @param response2 response value (1 or 0) for record 2.
    @param signal2 signal value (1 or 0) for record 2.
    @return true if record 1 is before record 2, and false otherwise. */
    
    private boolean isBeforeInMWWtable(int response1, int signal1, int response2, 
    						int signal2) {		
    	boolean before=false;
    	
    	// Response1 = 0
    	if (response1==0) {
    		// Response1 = 0, Signal1 = 1.
    		if (signal1==1) before = true;  // Four case
    		// Response1 = 0, Signal1 = 0;
    		else {
    			// Response1 = 0, Signal1 = 1, Response2 = 0.
    			if (response2==0) {
    				// Response1 = 0, Signal1 = 1, Response2 = 0, Signal2 = 1
    				if (signal2==1) before=false;
    				else before=true;
    				}
    			// Response1 = 0, Signal1 = 1, Response2 = 1.
    			else before=true; // Two cases
    			}
    		}
    	// Response1 = 1
    	else {
    		// Response1 = 1, Signal1 = 0;
    		if (signal1==0) {
    			// Response1 = 1, Signal1 = 0, Rsponse2 = 0.
    			if (response2==0) before=false; // Two cases
    			// Response1 = 1, Signal1 = 0, Rsponse2 = 1.
    			else before = true;
    			}
    		// Response1 = 1, Signal1 = 1;
    		else {
    			// Response1 = 1, Signal1 = 1, Response2 = 0.
    			if (response2==0) before=false; // Two cases
    			else {
    				// Response1 = 1, Signal1 = 1, Response2 = 1, Signal2 = 0
    				if (signal2==0) before=false;
    				// Response1 = 1, Signal1 = 1, Response2 = 1, Signal2 = 1
    				else before=true;
    				}
    			}
    		}
    		
    	// Return
    	return(before);
    	}
    		
    /** Determine if the testing of generated classifier can be undertaken, i.e. 
    is there a test set? and is there a classifier?
    private boolean true if OK, false otherwise. */
    
    private boolean canTestingBeUndertaken() {

		// Check if test data exists, if not return 'false'
		if (testDataArray==null) {
	    	String s = "WARNING: No test data\n";
	    	if (textArea==null) System.out.print(s);
            else textArea.append(s);
	    	return(false);
	    	}
	
		// Check if any classification rules have been generated, if not 
		// return 'false'
		if (startRulelist==null) {
	    	String s = "No classification rules generated!\n";
	    	if (textArea==null) System.out.print(s);
            else textArea.append(s);
	    	return(false);
	    	}
    
    	// Otherwise return "true"
    	return(true);
    	}

    /* ----------------------------------------------------- */
    /*                                                       */
    /*                TEST CLASSIFIER (BEST K)               */
    /*                                                       */
    /* ----------------------------------------------------- */

    /* TEST CLASSIFICATION BEST K */
    /** Tests the generated classification rules using test set with chi^2 and
    best K strategy, and return percentage accuracy. <P>Rules ordered according
    to chi^2 value and clasas selected by considering the average result of
    firing K rules.
    @return the perecentage accuarcy. */

    protected double testClassificationBestK() {
        int correctClassCounter = 0;

	// Check if test data exists, if not return' 0'
	if (testDataArray==null) {
	    if (textArea==null) System.out.println("WARNING: No test data");
            textArea.append("WARNING: No test data\n");
	    return(0);
	    }

	// Check if any classification rules have been generated, if not
	// return'0'
	if (startRulelist==null) {
	    String s = "No classification rules generated!\n";
	    if (textArea==null) System.out.print(s);
            else textArea.append(s);
	    return(0);
	    }

	// Create array of classes
	short[] classifiers = createClassifiersArray();
	// Loop through test set
	int index=0;
    	for(;index<testDataArray.length;index++) {
            short classResult = classifyRecordBestKaverage(classifiers,
	    				testDataArray[index]);
	    if (classResult!=0) {
	        short classActual = getLastElement(testDataArray[index]);
		if (classResult == classActual) correctClassCounter++;
		}
	    }

	// Output classification results
	double accuracy = ((double) correctClassCounter*100.0/(double) index);

	// Return
	return(accuracy);
	}

    /* ------------------------------------------------------------- */
    /*                                                               */
    /*                TEST CLASSIFIER (BEST K AVERAGE)               */
    /*                                                               */
    /* ------------------------------------------------------------- */

    /* CLASSIFY RECORD (BEST K AVERAGE) */
    /** Selects the best rule in a rule list according to the average expected
    accuracy. <P> Operates as follows:
    1) Obtain all rules whose antecedent is a subset of the given record.
    2) Select the best K rules for each class (rule with highest ranking).
    3) Determine the average expected accuracy over the selected rules for each
    class,
    4) Select the class with the best average expected accuracy.
    @param classification the possible classers.
    @param itemSet the record to be classified.
    @return the class label.		*/

    protected short classifyRecordBestKaverage(short[] classification,
    							short[] itemSet) {
        RuleNode linkRef      = startRulelist;
	RuleNode tempRulelist = startRulelist;
	startRulelist         = null;

	// Obtain rules that satisfy record (itemSet)
	obtainAllRulesForRecord(itemSet,linkRef);

	// Keep only best K rules for each class.
	keepBestKrulesPerClass(kValue,classification);

	// Determine average expected accuracies for selected K rules
	double[] averages = getAverageAccuracies(classification[0]);

	// Select class with best average
        short classLabel =
		(short) selectClassWithBestAverage(averages,classification[0]);

	// Reset global rule list reference
	startRulelist=tempRulelist;

	// Return class
	return(classLabel);
	}

    /* OBTAIN RULES FOR RECORD */

    /** Places all rules that satisfy the given record in a rule linked list
    pointed at by startRulelist field, in the order that rules are presented.
    <P> Used in Best K Average. Identical method in <TT>Classification</TT>
    class.
    @param itemset the record to be classified.
    @param linkref The reference to the start of the existing list of rules. */

    private void obtainAllRulesForRecord(short[] itemSet, RuleNode linkRef) {
        // Process node
        if (linkRef != null) {
	    // Left branch
            obtainAllRulesForRecord(itemSet,linkRef.leftBranch);
	    // Node, if rule satisfies record add to new rule list
	    if (isSubset(linkRef.antecedent,itemSet)) {
	   	insertRuleIntoRulelist(linkRef.antecedent,linkRef.consequent,
                           linkRef.confidenceForRule,linkRef.supportForRule);
	        }
	    // Right branch
            obtainAllRulesForRecord(itemSet,linkRef.rightBranch);
	    }
	}

    /* KEEP BEST K RULES PER CLASS */
    /** Keep only best K best rules for each class by processing selected
    rules list. <P>Bug reports: Thanks to Jian (Ella) Chen, Dept Comp.
    Sci. School of Information Sci. and Tech. Sun Yat-sen University, China.
    Bin tree is process and a new bin tree created containing the selected
    rules. Note also that rules are stored according to some ordering.
    Identical method in <TT>Classification</TT> class.
    @param kValue the maximum number of rules to be kept per class.
    @param classification the possible classes. */

    private void keepBestKrulesPerClass(int kValue, short[] classification) {
	// Copy reference to strat of selected rule list and set
	// startRulelist field to null ready for new list.
        RuleNode linkRef = startRulelist;
        startRulelist    = null;

        // Loop through classification array
	for (int index=0;index<classification.length;index++) {
	    // Walk through rule binary tree and keep first K rules that
	    // include current consequent
	    getFirstKrulesInBinTree(kValue,classification[index],linkRef);
	    }
	}

    /** Identifies first K rules in bin tree with given consequent and places
    them in the rule bin tree the start of which is pointed at by the
    <TT>startRulelist</TT> field. <P>Identical method in
    <TT>Classification</TT> class.
    @param kValue the maximum number of rules to be kept per class.
    @param classification the possible classes.
    @param node the current rule node.
    @return the vurent K value as decremented sofar. */

    private int getFirstKrulesInBinTree(int kValue, short consClass,
                                                              RuleNode node) {
        if (node!=null) {
            // Left branch
            kValue = getFirstKrulesInBinTree(kValue,consClass,node.leftBranch);
            if (kValue==0) return(kValue);
            // Node. Test if rule consequent matches current class and if do
            // insert into new list and decrement K value
	    if (consClass==node.consequent[0]) {
	   	insertRuleIntoRulelist(node.antecedent,node.consequent,
                                   node.confidenceForRule,node.supportForRule);
                if (kValue==1) return(0);
                }
            // Right branch
            return(getFirstKrulesInBinTree(kValue,consClass,node.rightBranch));
            }

        // Returm
        return(kValue);
        }

    /* GET AVERAGE ACCURACIES */
    /** Returns average accuracies for each class. <P>Accuracy here is
    measured in terms of confidence or some other measure stored in the
    "confidenceForRule" field. Identical method in <TT>Classification</TT>
    class.
    @param decrement the label for the first class in the classification array
    which is used here to relate class labels to indexes.
    @return the accuracy array describing accuracies for each class. */

    private double[] getAverageAccuracies(int decrement) {
	// Loop through linked list of best selected rules that satisfy the
	// given record and determine total "accuracies" for each class
	RuleNode linkRef   = startRulelist;
	double[][] results = new double[2][numClasses];
	getAverageAccuracies(results,decrement,linkRef);

	// Determine averages
	double[] accuracies = new double[numClasses];
	for(int index=0;index<accuracies.length;index++) {
	    if (results[0][index]!=0) accuracies[index] =
                                         results[0][index]/results[1][index];
	    }

	// return
	return(accuracies);
	}

    /** Continues process of returning average accuracies for each class.
    <P>Identical method in <TT>Classification</TT> class.
    @param results the results array describing accuracies and totals for each
    class.
    @param decrement the label for the first class in the classification array
    which is used here to relate class labels to indexes.
    @param node the current rule node.       */

    private void getAverageAccuracies(double[][] results, int decrement,
                                                              RuleNode node) {
        if (node!=null) {
            // Left branch
            getAverageAccuracies(results,decrement,node.leftBranch);
            // Node
            int index = node.consequent[0]-decrement;
	    results[0][index] = results[0][index]+node.confidenceForRule;
	    results[1][index] = results[1][index]+1.0;
	    // Right branch
            getAverageAccuracies(results,decrement,node.rightBranch);
            }
        }

    /* SELECT CLASS WITH BEST AVERAGE */
    /** Returns the class label with the best average accuracy associated with
    it. <P>Identical method in <TT>Classification</TT> class.
    @param increment the label for the first class in the classification array
    which is used here to relate class labels to indexes.
    @param the given list of averages
    @return the class label. */

    private int selectClassWithBestAverage(double[] averages, int increment) {
	int bestIndex=0;
	double bestAverage=averages[bestIndex];

	// Loop through array of avergaes
	int index=1;
	for ( ;index<averages.length;index++) {
	    if (averages[index]>bestAverage) {
	        bestIndex=index;
		bestAverage=averages[index];
		}
	    }

	// Return class
        return(bestIndex+increment);
	}

    /* CREATE CLASSIFIERS ARRAY */

    /** Creates a 1-D array of class labels. */

    private short[] createClassifiersArray() {
	// Dimenison classifier array

	short[] classifiers = new short[numClasses];

	// Get label for first classifier
	short firstClassifier = (short) (numOneItemSets-numClasses+1);

	// Populate classifiers array
	for (int index=0;index<classifiers.length;index++)
			classifiers[index]= (short) (firstClassifier + index);

	// Return
	return(classifiers);
	}

    /*------------------------------------------------------------------- */
    /*                                                                    */
    /*                             SET METHODS                            */
    /*                                                                    */
    /*------------------------------------------------------------------- */

    /* SET NUM ROWS IN INPUT SET */
    /** Assigns value of <TT>numTows</TT> field to the
    <TT>numRowsInInputSet</TT> field. <P> Used in conjunction with TCV to
    "remember" the overall number of rows in the input data set. <P> Usually
    called from application classes. */

    public void setNumRowsInInputSet() {
        numRowsInInputSet = numRows;
		}

    /* SET NUM ROWS IN TRAINING SET */
    /** Assigns a value equavalent to the number of rows to the number of
    rows in training set field. <P> used when the entire data set is
    considered as the training set.
    @param numTSrows given number of rows in training set.	*/

    public void setNumRowsInTrainingSet(int numTSrows) {
        numRowsInTrainingSet = numTSrows;
		}

    /* SET TEST DATA ARRAY */
    /** Sets reference to test data array.
    @param newTestDataArray the given 2-D array of terst data attributes. */

    public void setTestDataArray(short[][] newTestDataArray) {
        testDataArray = newTestDataArray;
		}

    /* SET K-VALUE */
    /** Sets the value for the kValue field.
    @param newKvalue the given value. */

    public void setKvalue(int newKvalue) {
        kValue = newKvalue;
        }

    /* SET RULE LIST ATTRIBUTE NUMBER OUTPUT FLAG */
    /** Sets the rule list output as attribute numbers flag.
    @param newValue the given value. */

    public void setOutputRLattNumFlag(boolean newValue) {
        ruleListAttNumOutputFlag=newValue;
        }

    /* SET RULE LIST SCHEMA OUTPUT FLAG */
    /** Sets the rule list output using input schema flag.
    @param newValue the given value. */

    public void setOutputRLschemaFlag(boolean newValue) {
        ruleListSchemaOutputFlag=newValue;
        }

    /* SET SUPPORT AND CONFIDENCE */
    /** Sets new values for the support and confidence fields.
    @param newSupport the new support value.
    @param newConfidence the new confidence value. */

    public void setSupportAndConfidence(double newSupport,
    						double newConfidence) {
        support    = newSupport;
		confidence = newConfidence;
		}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                             GET METHODS                          */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* GET AVERAGE ACCURACY */
    /** Gets value for average accuracy field.
    @return average accuracy. */

    public double getAverageAccuracy() {
        return(averageAccuracy);
	}

    /* GET STANDARD DEVIATION */
    /** Gets value for average accuracy field.
    @return average accuracy. */

    public double getSDaccuracy() {
        return(sdAccuracy);
		}

    /* GET ACCURACY */
    /** Gets the value of the <TT>accuracyt</TT> field.
    @return the accuracy value (%). */

    public double getAccuracy() {
        return(accuracy);
		}
	
	/* GET AVERAGE ACCURACY */
    /** Gets value for average accuracy field.
    @return average accuracy. */
   
    public double getAverageAUCvalue() {
        return(averageAUCvalue);
		}
	
    /* GET AUC VALUE */
    /** Gets the value of the <TT>aucValue</TT> field. 
    @return the AUC value. */
    
    public double getAUCvalue() {
        return(aucValue);
		}	

    /* GET AVERAGE NUMBER OF FREQUENT SETS */
    /** Gets value for average umber of frequent sets field.
    @return average number of frequent sets. */

    public double getAverageNumFreqSets() {
        return(averageNumFreqSets);
		}

    /* GET AVERAGE NUMBER OF UPDATES */
    /** Gets value for average number of updates field.
    @return average number of updates. */

    public double getAvergaeNumUpdates() {
        return(averageNumUpdates);
		}

    /* GET AVERAGE NUMBER OF CLASSIFICATION RULES */
    /** Gets value for average number of generated classification rules field.
    @return average number of classification rules. */

    public double getAverageNumCRs() {
        return(averageNumCRs);
		}

    /* GET K VALYE */
    /** Gets value for K value field.
    @return K value. */

    public int getKvalue() {
        return(kValue);
        }

    /* GET NUM CRs (VERSION 2)*/

    /** Returns the number of generated classification rules. <P> Different to
    <TT>getNumCRS</TT> method in <TT>AssocRuleMining</TT> class which actually
    calculates the number of rules.
    @return the number of rules. */

    public int getNumCRsVersion2() {
        return(numCRs);
	}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                               OUTPUT                             */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* OUTPUT MENU */

    /** Outputs menu for command line arguments. (Overides higher level method)
    */

    protected void outputMenu() {
        System.out.println();
	System.out.println("-C  = Confidence (default 80%)");
	System.out.println("-F  = Training file name");
	System.out.println("-N  = Number of classes");
	System.out.println("-O  = Output file name (optional)");
	System.out.println("-S  = Support (default 20%)");
	System.out.println("-T  = Test set file name (optional)");
	System.out.println();

	// Exit

	System.exit(1);
	}

    /* OUTPUT SETTINGS */

    /** Outputs command line values provided by user. (Overides higher level
    method.) */

    protected void outputSettings() {
        System.out.println("SETTINGS\n--------");
	System.out.println("Training file name            = " + fileName);
	if (testSetFileName!=null) System.out.println("Test set file name " + 
	                                 "(optional) = " + testSetFileName);
	if (outputFileName!=null) System.out.println("Output file name " +
	                                "(optional)   = " + outputFileName);
	System.out.println("Support (default 20%)         = " + support);
	System.out.println("Confidence (default 80%)      = " + confidence);
	System.out.println("Number of classes             = " + numClasses);
	System.out.println();
        }

    /** Outputs limits. */

    protected void outputLimits() {
        System.out.println("Max num frequent sets  = " +
                                                       MAX_NUM_FREQUENT_SETS);
	System.out.println("Max size of antecedent = " +
					              MAX_SIZE_OF_ANTECEDENT);
	System.out.println("Number of records in training set = " + numRows);
	if (isOrderedFlag) System.out.println("NOTE: Data set reordered");
        }

    /** Outputs limits (GUI version).
    @param tArea the text area to output data to. */

    protected void outputLimits(JTextArea tArea) {
        textArea.append("Max num frequent sets  = " + MAX_NUM_FREQUENT_SETS +
               "\nMax size of antecedent = " + MAX_SIZE_OF_ANTECEDENT + "\n");
	textArea.append("Number of records in training set = " + numRows + "\n");
	if (isOrderedFlag) textArea.append("NOTE: Data set reordered\n");
        }

    /* OUTPUT NUMBER OF CLASSES */

    /** Outputs number of classes. */

    public void outputNumClasses() {
        System.out.println("Number of classes = " + numClasses);
	}

    /* OUTPUT ACCURACY */

    /** Outputs classification accuracy. */

    public void outputAccuracy() {
        System.out.println("Accuracy = " + twoDecPlaces(accuracy));
	}

    /* OUTPUT TEST DATA TABLE */

    /** Outputs stored test set input data set read from input data file. */

    public void outputTestDataArray() {
        System.out.println("TEST DATA ARRAY\n---------------");
        for(int index=0;index<testDataArray.length;index++) {
	    System.out.print("Test rec #" + (index+1) + ": ");
	    outputItemSet(testDataArray[index]);
	    System.out.println();
	    }

	// End
	System.out.println();
	}

    /* ----------------------------- */
    /*                               */
    /*        OUTPUT METHODS         */
    /*                               */
    /* ------------------------------ */

    /* TCV OUTPUT */

    /** Output values from TCV excercise.
    @param parameters the 2D array contaning details of results. */

    protected void tcvOutput(double[][] parameters) {
		double totalAccuracy    = 0.0;
		double totalAUCvalue    = 0.0;
		double totalNumFreqSets = 0.0;
		double totalNumUpdates  = 0.0;
		double totalNumCRs      = 0.0;

		// Determine totals
		ouputTCVparam(parameters);
		for (int index=0;index<parameters.length;index++) {
	    	totalAccuracy    = totalAccuracy+parameters[index][0];
	    	totalAUCvalue    = totalAUCvalue + parameters[index][1];
	    	totalNumFreqSets = totalNumFreqSets+parameters[index][2];
	    	totalNumUpdates  = totalNumUpdates+parameters[index][3];
	    	totalNumCRs      = totalNumCRs+parameters[index][4];
	    	}

		// Calculate averages
		averageAccuracy    = totalAccuracy/10.0; 
		averageAUCvalue    = totalAUCvalue/10.0;
        averageNumFreqSets = totalNumFreqSets/10.0;
    	averageNumUpdates  = totalNumUpdates/10.0; 
    	averageNumCRs      = totalNumCRs/10.0;

		// Calculate standard deviation for accuracy
		double residuals = 0.0;
		for (int index=0;index<parameters.length;index++) {
	    	double residual = parameters[index][0]-averageAccuracy;
	    	residuals = residuals + Math.pow(residual,2.0);
	    	}
		sdAccuracy = Math.sqrt(residuals/9.0);

        // Output avergaes
        outputTCVaverages(averageAccuracy,sdAccuracy,averageAUCvalue,
        				averageNumFreqSets,averageNumUpdates,averageNumCRs);
		}

    /** Outputs TCV accuraces.
    @param avAcc the average accuracy as the result of TCV.
    @param sdAccuracy the standard deviation from average accuracy.
    @param aveAUC the average AUC calue as a result of TCV.
    @param aveNumFreqSets the average number of frequent sets as the result of
    TCV.
    @param aveNumUpdates the number of updates as the result of TCV.
    @param aveNumCRs the average number of callsification rules as the result
    of TCV. */

    private void outputTCVaverages(double aveAcc, double sdAccuracy,
    								double aveAUC, double aveNumFreqSets, 
    								double aveNumUpdates, double aveNumCRs) {
        // Generate string
        String s = "---------------------------------------\n" +
                "Average Accuracy\t= "      + twoDecPlaces(aveAcc) +
                "\nSD Accuracy\t\t= "       + twoDecPlaces(sdAccuracy) +
				"\nAverage AUC value = "    + fourDecPlaces(averageAUCvalue) +
                "\nAve. # Freq. Sets\t= "   + twoDecPlaces(aveNumFreqSets) +
                "\nAvergae Num Updates\t= " + twoDecPlaces(aveNumUpdates) +
                "\nAverage Num CRs\t= "     + twoDecPlaces(aveNumCRs) +
                "\n---------------------------------------";

        // Output
        if (textArea==null) System.out.println(s);
        else textArea.append(s + "\n");
	}

    /** Outputs TCV parameters.
    @param parameters the given parameters array. */

    private void ouputTCVparam(double[][] parameters) {
	// Processs parameters aray
        for (int index=0;index<parameters.length;index++) {
            String s = "(" + (index+1) + ") Accuracy = " +
	    		twoDecPlaces(parameters[index][0]) + ", AUC value = " + 
	    		fourDecPlaces(parameters[index][1]) + ", \t# Freq. Sets = " + 
	    		((int) parameters[index][2]) + ", \t# Updates = " + 
				((int) parameters[index][3]) + ", \t# CRs = " + 
				((int) parameters[index][4]) + "\n";
	    	// Output
            if (textArea==null) System.out.println(s);
            else textArea.append(s);
            }
        }
    }
