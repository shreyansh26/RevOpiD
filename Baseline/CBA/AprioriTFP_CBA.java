/* -------------------------------------------------------------------------- */
/*                                                                            */
/*            APRIORI-TFP CBA (CLASSIFICATION BASED ON ASSOCIATIONS)          */
/*                                                                            */
/*                               Frans Coenen                                 */
/*                                                                            */
/*                             Friday 12 March 2004                           */
/*         (Bug fixes and maintenance: 10/2/2005, 12/10/2006, 5/3/2012,       */
/*			 17 August 2015 (default rule used if no rules generated)         */
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
			+--AprioriTFPclass
				|
				+-- AprioriTFP_CARgen
					|
					+-- AprioriTFP_CBA	*/

// Java packages

import java.util.*;			
import java.io.*;

// Java GUI packages
import javax.swing.*;

/** Methods to produce classification rules using Wenmin Li, Jiawei Han and
Jian Pei's CBA (Classification based on Multiple associate Rules) algorithm
but founded on Apriori-TFP. Assumes that input dataset is orgnised such that
classifiers are at the end of each record. Note: number of classifiers value is
stored in the <TT>numClasses</TT> field.
@author Frans Coenen
@version 12 October 2006 */

/* Thanks to Cheng Zhou for suggested update to pruneUsingCBAapproach method so a
default method is created when no other rues are generated.	*/

public class AprioriTFP_CBA extends AprioriTFP_CARgen {

    /* ------ FIELDS ------ */

    // --- Data structures ---

    /** Structure to store rules that the current ruke overrrides, ie rules
    that have lower precedence than the given rule. <P>Used where the current
    rule wrongly classifies a record and the alternative lower precedence rule
    (overriden by the current rule) correctly classifies the same record.*/

    private class Overrides {
        /** A potential cRule that may Overrides an alternative, but lower
        precedence, cRule. */
        RuleNodeCBA cRule;
        /** The record TID in the training set. */
        int tid;
        /** The associated class label. */
        short classLabel;
        /** Link to next node. */
        Overrides next = null;

        /** Three argument constructor.
        @param cr the given crule the correctly classifies at least one record.
        @param tidNumber the TID number foe  arecord in the training set.
        @param cl the class label for the given record.  */

        private Overrides(RuleNodeCBA cr, int tidNumber, short cl) {
            cRule      = cr;
            tid        = tidNumber;
            classLabel = cl;
            }
        }

    /** Rule node in linked list of rules for CBA Classification rules. */

    private class RuleNodeCBA {
    	/** The Antecedent of the CAR. */
		private short[] antecedent;
		/** The Consequent of the CAR. */
		private short[] consequent;
		/** List of possible alternative rules that can be used to replace the 
		current rules. */
		Overrides replaceList = null;
		/** The confidence value associate with the rule represented by this
		node. */
		private double confidenceForRule=0.0;
		/** The support value associate with the rule represented by this
		node. */
		private double supportForRule=0.0;
		/** The (local) dustribution array for the rule describing the number 
		of records covered/satisfied per class (taking into consideration 
		records already covered by previous rules with higher precedence). */
		private int[] classCasesCovered = new int[numClasses];
		/** A flag to indicate whether the rule is a cRule for at least one
		record. */
		private boolean isAcRule = false;
		/** A flag to indicate whether the rule Is a strong cRule for at
		least one record, i.e. is has greater orecedence than the associated
		wRule. */
		private boolean isAstrongCrule = false;
		/** The default class associated with this rule. */
		private short defaultClass = 0;
		/** The total errors associated with this rule. */
		private int totalErrors = 0;
		/** Link to next node. */
		private RuleNodeCBA next = null;
	
		/** Four argument constructor
		@param antecedent the antecedent (LHS) of the AR.
    	@param consequent the consequent (RHS) of the AR.
		@param consvalue the associated confidence value.
    	@param suppValue the associated support value.   */
	
		private RuleNodeCBA(short[] ante, short[]cons,  double confValue,
						double suppValue) {
	        antecedent        = ante;
	        consequent        = cons;
	        confidenceForRule = confValue;
	        supportForRule    = suppValue;
	
	        // Initialise class records array
	        for (int index=0;index<numClasses;index++) classCasesCovered[index]=0;
	        }
	    }

    /** Set A structure */

    private class SetA {
        /** The TID number of a record in the training set. */
        private int tid;
        /** The class of the record with the given TID. */
        private short classLabel;
        /** The associated cRule. */
        private RuleNodeCBA cRule;
        /** The associated wRule. */
        private RuleNodeCBA wRule;
	    /** Link to next node. */
	    private SetA next = null;
	
        /** Four argument constructor
	    @param tidNumber the record number
    	@param classID the record class.
	    @param cr the associated cRule.
    	@param ar the associated wRule.   */
	
	    private SetA(int tidNumber, short classID, RuleNodeCBA cr,
					RuleNodeCBA  wr) {
            tid = tidNumber;
            classLabel = classID;
            cRule = cr;
            wRule = wr;
            }
        }

    /** The reference to the start of the CBA rule list. */
    protected RuleNodeCBA startCBAruleList = null;
    /** The reference to the start of the SetA list. */
    protected SetA startSetAlist = null;

    /* ------ CONSTRUCTORS ------ */

    /** Constructor processes command line arguments.
    @param args the command line arguments (array of String instances). */

    public AprioriTFP_CBA(String[] args) {
	    super(args);
	    }

    /** Constructor with argument from existing instance of class
    AssocRuleMining.
    @param armInstance the given instance of the <TT>AssocRuleMining</TT>
    class. */

    public AprioriTFP_CBA(AssocRuleMining armInstance) {
	    super(armInstance);
        }

    /* ------ METHODS ------ */

    /*--------------------------------------------------------------------- */
    /*                                                                      */
    /*                       START CBA CLASSIFICATION                       */
    /*                                                                      */
    /*--------------------------------------------------------------------- */

    /* START CBA CLASSIFICATION */

    /** Starts CBA classifier generation proces. <P> Proceeds as follows:<OL>
    <LI>Generate all CARs using Apriori-TFP and place selected CARs into linked
    list of rules.
    <LI>Prune list according the cover stratgey.
    <LI>Test classification using Chi-Squared Weighting approach.</OL>	*/

    public void startClassification() {
       String s = "START APRIORI-TFP CBA\n------------------------------\n" +
	                           "\nMax number of CARS   = " + MAX_NUM_CARS +
	           "\nMax size antecedent  = " + MAX_SIZE_OF_ANTECEDENT + "\n";
	    if (textArea==null) System.out.println(s);
        else textArea.append(s);

	    // Proceed
	    startClassification2();
	    }

    /** Starts CBA classifier generation proces, GUI version.
    @param tArea the text area to output data to.		*/

    public void startClassification(JTextArea tArea) {
        // Set text area
        textArea = tArea;

        // proceed
        startClassification2();
	    }

    /** Continues process of starting the CMAR classifier generation proces.	*/

    protected void startClassification2() {
	    // Set data structure references to null
	    startSetAlist = null;
	    startCBAruleList = null;

	    // Generate all CARs using Apriori-TFP and place selected CARs into
        // linked list of rules.
        startCARgeneration2();

        // Prune linked list of rules using CBA "cover" principal
	    pruneUsingCBAapproach(copyItemSet(dataArray));

	    // Output rule list (uses local method).
        if (ruleListAttNumOutputFlag || ruleListSchemaOutputFlag) 
                                    outputCBArules();

	    // Test classification using the test set.
	    testClassification();
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*             APRIORI-TFP CBA WITH TEN CROSS VALIDATION (TCV)            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* COMMEMCE TEN CROSS VALIDATION WITH OUTPUT*/

    /** Start CBA Ten Cross Validation (TCV) process with output of individual
    accuracies. */

    public void commenceTCVwithOutput() {
        double[][] parameters = new double[10][5];
	String s = "START TCV APRIORI-TFP CBA CLASSIFICATION\n" +
	            "------------------------------\nMax number of CARS   = " +
		                   MAX_NUM_CARS + "\nMax size antecedent  = " +
		                                 MAX_SIZE_OF_ANTECEDENT + "\n";
	if (textArea==null) System.out.println(s);
        else textArea.append(s);

	// Loop through tenths data sets
	for (int index=0;index<10;index++) {
	    s = "[--- " + (index+1) + " ---] ";
	    if (textArea==null) System.out.println(s);
            else textArea.append(s);
	    // Create training and test sets
	    createTrainingAndTestDataSets(index);
	    // Mine data, produce T-tree and generate CRs
	    startClassification2();
	    parameters[index][0] = accuracy;
	    parameters[index][1] = aucValue;
	    parameters[index][2] = numFrequentSets;
	    parameters[index][3] = numUpdates;
	    // Final rule set contained in the standrad list of rules not the
	    // CBA rule list defined in this class
	    parameters[index][4] = getNumRules();
	    }

	// Output
	tcvOutput(parameters);
	}

    /** Start CBA Ten Cross Validation (TCV) process with output of individual
    accuracies, GUI version.
    @param tArea the given instance of the <TT>JTextArea</TT> class. */

    public void commenceTCVwithOutput(JTextArea tArea) {
        textArea = tArea;

        // Proceed
        commenceTCVwithOutput();
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*            CLASSIFICATION ASSOCIATION RULE (CAR) GENERATION            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* GENERATE CLASSIFICATION ASSOCIATION RULES (RIGHT LEVEL). */

    /** Generating classificationh association rules from a given array of
    T-tree nodes. <P> For each rule generated add to rule list if: (i)
    Chi-Squared value is above a specified critical threshold (5% by default),
    and (ii) the CR tree does not contain a more general rule with a higher
    ordering. Rule added to rule list according to CBA ranking (ordering).
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array lavel in the T-tree.
    @param consequent the current consequent (classifier) for the CAR.
    @param linkRef the reference to the current array lavel in the T-tree. */

    protected void generateCARsRightLevel(short[] itemSetSofar, int size,
    				    short[] consequent, TtreeNode[] linkRef) {
	    // Loop through T-tree array
	    for (int index=1; index < size; index++) {
	        // Check if node exists
	        if (linkRef[index] != null) {
		        // Generate Antecedent
		        short[] tempItemSet = realloc2(itemSetSofar,(short) index);
		        // Determine confidence
		        double confidenceForCAR = getConfidence(tempItemSet,
		    				       linkRef[index].support);
		        // Add CAR to linked list structure if confidence greater
		        // than minimum confidence threshold.
		        if (confidenceForCAR >= confidence) {
		            numCarsSoFar++;
		            double suppForConcequent = (double)
		   		                     getSupportForItemSetInTtree(consequent);
		            insertRinRlistCBAranking(tempItemSet,consequent,
				    confidenceForCAR,linkRef[index].support);
	                }
		        }
	        }
	    }

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*        RULE LINKED LIST ORDERED ACCORDING TO CBA RANKING         */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* Methods for inserting rules into a linked list of rules ordered
    according to CBA ranking. Each rule described in terms of 4 fields: 1)
    Antecedent (an item set), 2) a consequent (an item set), 3) a total support
    value and 4) a confidence value (double). */

    /* INSERT ASSOCIATION CLASSIFICATION) RULE INTO RULE LINKED LIST (ORDERED
    ACCORDING TO CBA RANKING). */

    /** Inserts an (association/classification) rule into the linkedlist of
    rules pointed at by <TT>startCBArulelist</TT>. <P> List is ordered according
    to "CBA" ranking.
    @param antecedent the antecedent (LHS) of the rule.
    @param consequent the consequent (RHS) of the rule.
    @param confidenceForRule the associated confidence value.
    @param supportForRule the associated support value. */

    protected void insertRinRlistCBAranking(short[] antecedent,
    			     short[] consequent, double confidenceForRule,
			     double supportForRule) {
	    // Create new node
	    RuleNodeCBA newNode = new RuleNodeCBA(antecedent,consequent,
					confidenceForRule,supportForRule);

        // Empty list situation
	    if (startCBAruleList == null) {
	        startCBAruleList = newNode;
	        return;
	        }

	// Add new node to start
	if (ruleIsCBAgreater(newNode,startCBAruleList)) {
        newNode.next = startCBAruleList;
        startCBAruleList  = newNode;
        return;
        }

	// Add new node to middle
	RuleNodeCBA markerNode = startCBAruleList;
	RuleNodeCBA linkRuleNode = startCBAruleList.next;
	while (linkRuleNode != null) {
        if (ruleIsCBAgreater(newNode,linkRuleNode)) {
            markerNode.next = newNode;
		newNode.next    = linkRuleNode;
		return;
		}
        markerNode = linkRuleNode;
        linkRuleNode = linkRuleNode.next;
        }

	// Add new node to end
	markerNode.next = newNode;
	}

    /* RULE IS CBA GREATER */

    /** Compares two rules and returns true if the first is "CBA greater" (has
    a higher ranking) than the second. <P> CBA ordering (same as CMAR) is as
    follows:
    <OL>
    <LI>Confidence, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if
    <TT>confidence(r1) &gt; confidence(r2)</TT>.
    <LI>Support, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if
    <TT>confidence(r1)==confidence(r2) &amp;&amp; support(r1)&gt;support(r2)
    </TT>.
    <LI>Size of antecedent, a rule <TT>r1</TT> has priority over a rule
    <TT>r2</TT> if <TT>confidence(r1)==confidence(r2) &amp;&amp;
    support(r1)==spoort(r2) &amp;&amp;|A<SUB>r1</SUB>|&lt;|A<SUB>r2</SUB>|
    </TT>.
    </OL>
    @param rule1 the given rule to be compared to the second.
    @param rule2 the rule which the given rule1 is to be compared to.
    @return true id rule1 is greater then rule2, and false otherwise. */

    private boolean ruleIsCBAgreater(RuleNodeCBA rule1, RuleNodeCBA rule2) {

	// Compare confidences
	if (rule1.confidenceForRule > rule2.confidenceForRule) return(true);

	// If confidences are the same compare support values
        if (similar2dec(rule1.confidenceForRule,rule2.confidenceForRule)) {
	   if (rule1.supportForRule > rule2.supportForRule) return(true);
	   // If confidences and supports are the same compare antecedents
	   if (similar2dec(rule1.supportForRule,rule2.supportForRule)) {
           if (rule1.antecedent.length < rule2.antecedent.length)
           							return(true);
           }
	   }

	// Otherwise return false
	return(false);
	}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                         PRUNE CBA RULES                          */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* PRUNE CBA RULES */
    /** Commences process of generating local distribution arrays and producing
    the final classifier. <P>Done in four stages:</P>
    <OL>
    <LI>C AND W RULE IDENTIFICATION: Identify all cRules and wRules in the rule
    list. Record in the appropriate distribution arrays the number of records
    covered by strong cRules. Where ever the wRule has higher precedence than
    the corresponding cRule store the effected training set records in a linked
    list of SetA structures, <TT>A</TT>.
    <LI>CONSIDER WRONGLY CLASSIFIED RECORDS: Process the set <TT>A</TT> (the
    list of records that are wrongly classified). If the "offending" wRule
    can safely be removed from <TT>R</TT> identify other rules that
    <I>override</I> (have higher precedence) the cRule corresponding to the
    wRule. If the offending wRule cannot be removed (because it is a CRule with
    respect to some other record) then adjust local distribution array for the
    wRule accordingly.
    <LI>DETERMINE DEFAULT CLASSES AND TOTAL ERROR COUNTS: Adjust local
    distribution arrays with respect to overridden rules identified in Stage 2.
    Then for each "strong" cRule determine the deafault class and the total
    error count.
    <LI>GENERATE CLASSIFIER Create classifier.
    </OL> 
    Note that if no rules are generated a default rule is used. */

    protected void pruneUsingCBAapproach(short[][] trainingSet) {
        // Check for empty rule list
	    if (startCBAruleList==null) {
         	System.out.println("NO RULES GENERATED, CREATING DEFAULT RULE");
         	int[] classDistrib = genClassclassCasesCovered(trainingSet);
			int defaultIndex = 0;
			// Loop through class disatribution array
			for (int index = 1; index < classDistrib.length; index++) {
				if (classDistrib[index] > classDistrib[defaultIndex])
				defaultIndex = index;
				}
			short defaultClass = (short) (defaultIndex + numOneItemSets - numClasses + 1);
			short[] consequent = new short[1];
			consequent[0] = defaultClass;
			if (startCBAruleList == null) {
				startRulelist = new RuleNode(null, consequent, 0.0, 0.0);
				}
        	return;
        	}

        // (1) Find C and W rules
		findCandWrules(trainingSet);

        // (2) Process wrongly classified records
        procesSetAlinkedList(trainingSet);

		// (3) Process rule list
		processRuleList(trainingSet);

        // (4) enerate classifier
        generateClassifier();
        }

    /* -------------------------------------------------------- */
    /*           STAGE 1: C AND W RULE IDENTIFICATION           */
    /* -------------------------------------------------------- */

    /* FIND C AND W RULES */
    /** Processes training set and identifies appropriate cRules and wRules.
    <P> Procceds as follows, for each record in the training set: <OL>
    <LI>Identify the first rule (in the ordered list of CBA rules) that
    correctly classifies the record and the first rule that wrongly classifies
    the record (these are the corresponding cRule and wRule for the record).
    <LI>Compare the identified cRule and Wrule:<OL>
    	<LI>No cRule, do nothing
    	<LI>cRule exits but no wRule, mark cRule as a "Strong" cRule
    	<LI>cRule and wRule exist, and cRule has greater precedence than wRule,
    	mark as a "Strong" cRule.
    	<LI>cRule and wRule exist, and wRule has greater precedence than cRule,
    	create a SetA structure and add to linked list of SetA structtures.</OL>
    </OL>
    On completion: (i) all records which are wrongly classified (but for which
    there is a corresponding cRule) will be collated into a linked list of SetA
    structres ready for further consideration and (ii) all rules which are
    "strong" cRules with respect to at least one record will be identified.
    @param trainingSet the array of arrays of training set records. */

    private void findCandWrules(short[][] trainingSet) {

	for(int index=0;index<trainingSet.length;index++) {
        // Get class
        int lastIndex = trainingSet[index].length-1;
        short classLabel = trainingSet[index][lastIndex];
        // Find cRule
        RuleNodeCBA cRule = getCrule(trainingSet[index],classLabel);
        // Find wRule
        RuleNodeCBA wRule = getWrule(trainingSet[index],classLabel);
        // Compare cRule and wRule.
        if (cRule != null) {
        	if (wRule == null) cRule.isAstrongCrule = true;	// Option 2
        	else {
                if (ruleIsCBAgreater(cRule,wRule))
                		cRule.isAstrongCrule = true;	// Option 3
                else insertIntoSetAlist(index,classLabel,cRule,
                					wRule);	// Option 4
                }
            }
        }
	}

    /* GET C RULE */

    /** Returns first rule in CBA list (i.e. rule with highest precedence) that
    correctly classifies the given record and also increments the appropriate
    element in the identified rule's distribution array (an array of integers
    indicating, for each record that the rule satisfies, the class to which
    the record belongs to).
    @param record the given record.
    @param classLabel the class for the given record.
    @param the first rule that correctly classifies the record. */

    private RuleNodeCBA getCrule(short[] record, short classLabel) {
        RuleNodeCBA linkRef = startCBAruleList;

		while (linkRef != null) {
        	if (isSubset(linkRef.antecedent,record) &&
        				linkRef.consequent[0]==classLabel) {
            	linkRef.isAcRule=true;
				linkRef.classCasesCovered[classLabel-numOneItemSets+numClasses-1]++;
				return(linkRef);
				}
        	linkRef=linkRef.next;
        	}

		// Default rerturn
		return(null);
		}

    /* GET W RULE */

    /** Returns first rule in CBA list (i.e. rule with highest precedence) that
    wrongly classifies the given record.
    @param record the given record.
    @param classLabel the class for the given record.
    @param the first rule that wrongly classifies the record. */

    private RuleNodeCBA getWrule(short[] record, short classLabel) {
        RuleNodeCBA linkRef = startCBAruleList;

		while (linkRef != null) {
        	if (isSubset(linkRef.antecedent,record) &&
        				linkRef.consequent[0]!=classLabel) return(linkRef);
        	linkRef=linkRef.next;
        	}

		// Default rerturn
		return(null);
		}

    /* INSERT INTO SET A LIST */
    /** Insets details concerning a record where the wRule has stronger
    precedence than the cRule (or there is no cRule) into a Set A record and
    places into a linked list of such records.
    @param tidNumber the record number
    @param classID the record class.
    @param cr the associated cRule.
    @param ar the associated wRule.   */

    private void insertIntoSetAlist(int tidNumber, short classID,
    					RuleNodeCBA cr, RuleNodeCBA  wr) {
        SetA tempSetA = new SetA(tidNumber,classID,cr,wr);
		tempSetA.next  = startSetAlist;
		startSetAlist = tempSetA;
        }

    /* ---------------------------------------------------------------- */
    /*           STAGE 2: CONSIDER WRONGLY CLASSIFIED RECORDS           */
    /* ---------------------------------------------------------------- */

    /* PROCESS SET A LINKED LIST */
    /** Processes the Set A linked list, the list of records that have been
    wrongly clasified. <P> Proceeds as follows:</P><OL>
    <LI>If wRule associated with a record is marked as a "cRule", i.e.
    it correctly classifies at least one other record, adjust classCasesCovered
    array for wRule and corresponding cRule.
    <LI>If wRule is not marked, i.e. it is not a "cRule" for any record,
    determine all the rules after the wRule, that wrongly classify the
    record upto when the corresponding cRule is reached and place these in a
    linked list of <TT>Overrides</TT> structures (these are then all the rules
    which we would like to remove from the rule list, if possible, so that
    the record in question will be correctly classified.</OL>
    @param trainingSet the array of arrays of training set records. */

    private void procesSetAlinkedList(short[][] trainingSet) {
        SetA linkRef = startSetAlist;

        // Loop through the list of records that have been wrongly classified
	while (linkRef != null) {
            // Get class index
            int index = linkRef.classLabel-numOneItemSets+numClasses-1;
            // If wRule is a strong cRule for some other record, we cannot get
        // rid of it, therefore adjust distribution array accordingly.
            if (linkRef.wRule.isAcRule) {
                linkRef.cRule.classCasesCovered[index]--;
                linkRef.wRule.classCasesCovered[index]++;
                }
            // Else wRule is not a strong cRule for some other record,
        // therefore identify all rules that overide the corresponding
        // cRule for the record.
            else {
            	allCoverRules(trainingSet[linkRef.tid],linkRef.tid,
	                   linkRef.classLabel,linkRef.cRule,linkRef.wRule);
            	}
            linkRef = linkRef.next;
            }
        }

    /* ALL COVER RULES */
    /** Finds all rules with higher precedence than the given cRule that
    wrongly classify the record, commencing with the given wRule, before
    the c rule is reached. <P>Note that we only have to consider rules marked
    as cRules (with respect to some other record(s)), other rules can safely
    be removed from the data set. Proceeds as follows: For each rule between
    the wRule and the cRule that are CRules with respect to some other
    record:</P><OL>
    <LI>Add the cRule to the rules overrides list.
    <LI>Increment the classCasesCovered for the rule.
    </OL>
    @param record the current record in the training sert.
    @param tid the TID number for the current record in the training set.
    @param classLabel the label for the record.
    @param the given cRule up to which the rule list will be processed.
    @param the given cWule from where the rule list will be processed.   */

    private void allCoverRules(short[] record, int tid, short classLabel,
    			RuleNodeCBA cRule, RuleNodeCBA wRule) {
        // Determine class index
        int classIndex = classLabel-numOneItemSets+numClasses-1;

        // Process wRule list
        RuleNodeCBA linkRef = wRule.next;
        while (linkRef != null) {
            // If reached the given cRule jump out of the loop
            if (linkRef == cRule) break;
            // Check if current rule is also a cRule (i.e. satisfies at least
            // one record in the training set), otherwise ignore
            if (linkRef.isAcRule) {
            // Check if identified cRule satisfies current record and if so
        // add to overides linked list.
                if (isSubset(linkRef.antecedent,record)) {

            // Add to replace list for rule identified by linkRef
            Overrides newOverrides = new Overrides(cRule,tid,
                                    classLabel);
            newOverrides.next   = linkRef.replaceList;
            linkRef.replaceList = newOverrides;
            // Increment counter
            linkRef.classCasesCovered[classIndex]++;
            // Set to true
            linkRef.isAstrongCrule = true;
            }
        }
            linkRef = linkRef.next;
            }
        }

    /* --------------------------------------------------------------- */
    /*    STAGE 3: PROCESS RULE LIST   */
    /* --------------------------------------------------------------- */

    /* PROCESS RULE LIST */
    /** Creates the final list of CBA rules by processesing the rule list to
    identify, for each "strong"  cRule the default class and the total error
    count. <P> Commences with the generation of a global class distribution
    array which contains the number of training cases for each class. Next the
    "overriden" rule list for each "strong" cRule is considered, if such a
    list exists, it is processed. Next the default class is identified and the
    total error count calculated.
    @param trainingSet the array of arrays of training set records. */

    private void processRuleList(short[][] trainingSet) {
        // Local fields
        int ruleErrors=0;
        // Class distribution array
        int[] classDistrib = genClassclassCasesCovered(trainingSet);

        // Process rule list
        RuleNodeCBA linkRef = startCBAruleList;
    while(linkRef!=null) {
        // Consider "strong" cRules only
        if (linkRef.isAstrongCrule) {
            // Get index in class records array for consequent of rule
            int classIndex = linkRef.consequent[0]-numOneItemSets+
                                                         numClasses-1;
        // Check that rule correctly satisfies at least one record
        // (otherwise ignore).
        if (linkRef.classCasesCovered[classIndex] != 0) {
            // Process overrides linked list for current "strong" cRule
            Overrides orLinkRef = linkRef.replaceList;
            while (orLinkRef != null) {
                int cIndex = orLinkRef.classLabel-numOneItemSets+
                                                         numClasses-1;
                // The TID referenced by the current overriding rule
            // Is already satisfied by a previous rule decrement
            // Current rule distribution array, otherwise
            // decrement overriding rule's distribution array
            if (coveredByPreviousRule(orLinkRef.tid,linkRef,
                                trainingSet)) {
                linkRef.classCasesCovered[cIndex]--;
                }
            else orLinkRef.cRule.classCasesCovered[cIndex]--;
            orLinkRef = orLinkRef.next;
            }
            // Determine number of accumulated miss-classifications
            // that will result if the given rule were the last rule in
            // the classifier (i.e. the default rule)
            for (int index=0;index<linkRef.classCasesCovered.length;
                                    index++)
                if (classIndex!=index) ruleErrors=ruleErrors+
                             linkRef.classCasesCovered[index];
            // Adjust global distribution array
            adjustclassCasesCovered(linkRef.classCasesCovered,
                                                        classDistrib);
            // Select default class and return default error
            int defaultErrors = selectDefaultRule(linkRef,classDistrib);
            // Total errors
                linkRef.totalErrors = ruleErrors+defaultErrors;
            }
        // Redefine class as not a strong class
        else linkRef.isAstrongCrule=false;
        }
        linkRef = linkRef.next;
        }
    }

    /* GENERATE CLASS DISTRIBUTION ARRAY */
    /** Generates a Class distribution array --- i.e. an array indicating
    the number of records in the training set that correspond to individual
    classess.
    @param trainingSet the array of arrays of training set records. */

    private int[] genClassclassCasesCovered(short[][] trainingSet) {
        // Create and initailise distribution array
        int [] classDistrib = new int [numClasses];
        for (int index=0;index<classDistrib.length;index++)
                            classDistrib[index]=0;

        // Process training set
        for (int index=0;index<trainingSet.length;index++) {
            int lastIndex = trainingSet[index].length-1;
            int classIndex = trainingSet[index][lastIndex]-
                                numOneItemSets+numClasses-1;
            classDistrib[classIndex]++;
            }

        // Returm
        return(classDistrib);
        }

    /* ADJUST DISTRIBUTION ARRAY */
    /** Adjusts class distribution array to take into consideration latest rule.
    @param ruleDistrib the number records of each class satisfied by the rule.
    @param classDistribution the current distribution of classes, amoungst the
    remaining records in the training set, to be adjusted according to the
    rule distribution. */

    private void adjustclassCasesCovered(int [] ruleDistrib, int [] classDistrib) {
        for (int index=0;index<ruleDistrib.length;index++) {
            classDistrib[index]=classDistrib[index]-ruleDistrib[index];
            }
        }

    /* SELECT A DEFAULT RULE */
    /** Selects a default rule according to the current class distribution
    within the remaining records in the training set and determines and returns
    the number of errors that would result.
    @param cRule the current "string" cRule.
    @param classDistribution the current distribution of classes, amoungst the
    remaining records in the training set.
    @return the number of errors that would result from application of the
    suggested default rule at this stage. */

    private int selectDefaultRule(RuleNodeCBA cRule, int [] classDistrib) {
        int defaultIndex=0;

        // Loop through class disatribution array
        for (int index=1;index<classDistrib.length;index++) {
            if (classDistrib[index]>classDistrib[defaultIndex])
                                    defaultIndex=index;
            }

        // Add to defualt field for cRule
        cRule.defaultClass = (short) (defaultIndex+numOneItemSets-numClasses+1);

        // Calculate errors
        int errors=0;
        for (int index=0;index<classDistrib.length;index++) {
            if (index!=defaultIndex) errors=errors+classDistrib[index];
            }

        // Return
        return(errors);
        }

    /** Determines wherther the given record in the training set has been
    covered (i.e. wrongly classified) by a strong c rule with higher precedence.
    @param tid the record number in the training set for the given record.
    @param currentRuleRef the reference to the current cRule.
    @param trainingSet the array of arrays of training set records.
    @return true if record is covered by a previous rule and false
    otherwise. */

    private boolean coveredByPreviousRule(int tid, RuleNodeCBA currentRuleRef,
                            short[][] trainingSet) {
        boolean isCoveredByPreviousRule = true;

    // Process CBA rule list until current cRule is reached.
    RuleNodeCBA linkRef = startCBAruleList;
    while (linkRef!=null) {
        // Reached current rule
        if (linkRef == currentRuleRef) break;
        // Check if rule is a strong c rule and if so if it satsfies record
        if (linkRef.isAstrongCrule &&
                     (isSubset(linkRef.antecedent,trainingSet[tid]))) {
            isCoveredByPreviousRule = false;
        break;
        }
        linkRef = linkRef.next;
        }

    // Return
    return(isCoveredByPreviousRule);
    }

    /* ------------------------------------------------ */
    /*           STAGE 4: GENERATE CLASSIFIER           */
    /* ------------------------------------------------ */

    /*  GENERATE CLASSIFIER */

    /** Generates classifier by finding first strong c rule (that satisfies at
    least one record) with the lowest total error in the CBA rule list. <P> The
    final classifier comprises all the rules upto and including the identified
    rule pluss a default rule that produces the default class associated with
    the identified rule. */

    private void generateClassifier() {
        RuleNodeCBA linkRef = startCBAruleList;
        // Find lowest total error
        int error = findLowestTotalError();
        // Process rule list
        while(linkRef!=null) {
            if (linkRef.isAstrongCrule) {
                int classIndex = linkRef.consequent[0]-numOneItemSets+
                                                         numClasses-1;
                if (linkRef.classCasesCovered[classIndex] != 0) {
                    insertRuleIntoRulelist(linkRef.antecedent,linkRef.consequent,
                                           linkRef.confidenceForRule,
                                           linkRef.supportForRule);
                    }
                }
            if (linkRef.totalErrors==error) break;
            linkRef = linkRef.next;
            }

        // Add default rule
        short[] consequent = new short[1];
        consequent[0] = linkRef.defaultClass;
        insertRuleIntoRulelist(null,consequent,0.0,0.0);
        
        // // Number Rule in BinTree
        numberRulesInBinTree();
        }

    /* FIND LOWEST TOTAL ERROR */

    /** Finds the first strong c rule (that satisfies at least one record) in
    the CBA rule list with the lowest total error.
    @return the minimum total error. */

    private int findLowestTotalError() {
        int error=numRows;    // Maximum error, all records miss classified

        // Process rule list
        RuleNodeCBA linkRef = startCBAruleList;
        while(linkRef!=null) {
            if (linkRef.isAstrongCrule) {
            int classIndex = linkRef.consequent[0]-numOneItemSets+
                                                         numClasses-1;
            if (linkRef.classCasesCovered[classIndex] != 0) {
                    if (linkRef.totalErrors<error) error=linkRef.totalErrors;
            }
                }
        linkRef = linkRef.next;
        }

    // Return
    return(error);
    }

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                        TEST CLASSIFICATION                       */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* TEST CLASSIFICATION */
    /** Tests the generated classification rules using test sets and calculates
    percentage accuracy. */

    /* protected void testClassification() {
    	int correctClassCounter = 0;
    	int wrongClassCounter   = 0;
    	int unclassifiedCounter = 0;

   	 	// Check if test data exists, if not return' 0'
    	if (testDataArray==null) {
			JOptionPane.showMessageDialog(null,"No test data",
                    "CLASSIFICATION ERROR",JOptionPane.ERROR_MESSAGE);
        	return;
        	}

    	// Check if any classification rules have been generated, if not
    	// return'0'.
    	if (startRulelist==null) {
        	System.out.println("No classification rules generated!");
        	return;
        	}

    	// Loop through test set
    	int index=0;
        for(;index<testDataArray.length;index++) {
        	// Note: classifyRecord methods are contained in the
        	// AssocRuleMining class. To classify without default use
        	// classifyRecord, with defualt use classifyRecordDefault.
            short classResult = classifyRecordDefault(testDataArray[index]);
        	if (classResult==0) unclassifiedCounter++;
        	else {
            	short classActual = getLastElement(testDataArray[index]);
        		if (classResult == classActual) correctClassCounter++;
            	else wrongClassCounter++;
        		}
        	}

    	accuracy = ((double) correctClassCounter*100.0/(double) index);
    	} */

    /* ----------------------------------- */
    /*                                     */
    /*              GET METHODS            */
    /*                                     */
    /* ----------------------------------- */

    /* GET NUMBER OF CBA CLASSIFICATION RULES */

    /**  Returns the number of generated CBA classification rules.
    @return the number of CRs. */

    public int getNumCBA_CRs() {
        int number = 0;
        RuleNodeCBA linkRuleNode = startCBAruleList;

    // Loop through linked list
    while (linkRuleNode != null) {
        number++;
        linkRuleNode = linkRuleNode.next;
        }

    // Return
    return(number);
    }

    /* ------------------------------ */
    /*                                */
    /*              OUTPUT            */
    /*                                */
    /* ------------------------------ */

    /* OUTPUT CBA RULE LINKED LIST */
    /** Outputs contents of CBA rule linked list (if any) */

    public void outputCBArules() {
        outputRules(startCBAruleList);
    	}

    /** Outputs given CBA rule list.
    @param ruleList the given rule list. */

    public void outputRules(RuleNodeCBA ruleList) {

        // Check for empty rule list
        if (ruleList==null) {
            String s = "No rules generated!\n";
            if (textArea==null) System.out.println(s);
            else textArea.append(s);
            return;
            }
            
        // Heading
        String s = "(#) Ante -> Cons (confidence %, support) Overides list" +
                "[cases covered]\n---------------------------------------------\n";
        if (textArea==null) System.out.println(s);
        else textArea.append(s);

        // Loop through rule list
        int number = 1;
        RuleNodeCBA linkRuleNode = ruleList;
        while (linkRuleNode != null) {
            if (textArea==null) outputRule(number,linkRuleNode);
            else outputRule(textArea,number,linkRuleNode);
            number++;
            linkRuleNode = linkRuleNode.next;
            }
        }

    /** Outputs a CBA rule.
    @param number the rule number.
    @param rule the rule to be output. */

    private void outputRule(int number, RuleNodeCBA rule) {
        System.out.print("(" + number + ") ");
        
        if (rule==null) System.out.println("Null");
        else {
            outputRule(rule);
            System.out.print(" (" + twoDecPlaces(rule.confidenceForRule) +
                      "%, " + rule.supportForRule + ")");
            // Output if cRule
            if (rule.isAcRule) {
               if (rule.isAstrongCrule) System.out.print(" * STRONG cRule *");
                else  System.out.print(" * cRule *");
                }                        
            // Output replaceList
            outputOverridesLinkedList(rule.replaceList);
            // Output class cases covered array
            System.out.print(" [" + rule.classCasesCovered[0]);
            for(int index=1;index<rule.classCasesCovered.length;index++)
                    System.out.print("," + rule.classCasesCovered[index]);
            System.out.print("]");
            // Output default class and total errors unless default is 0
            if (rule.defaultClass!=0) System.out.println(", D-Class = " +
                                    rule.defaultClass + ", T-errors = " +
                                                       rule.totalErrors);
            else System.out.println();
            }
        }

    /** Outputs a CBA rule (GUI Version).
    @param number the rule number.
    @param textArea the text area to output data to.
    @param rule the rule to be output. */

    private void outputRule(JTextArea textArea, int number, RuleNodeCBA rule) {
        textArea.append("(" + number + ") ");
        
        if (rule==null) textArea.append("Null");
        else {
            outputRule(textArea,rule);
            textArea.append(" (" + twoDecPlaces(rule.confidenceForRule) +
                                        "%, " + rule.supportForRule + ")");
            // Output if cRule
            if (rule.isAcRule) {
               if (rule.isAstrongCrule) textArea.append(" * STRONG cRule *");
                else  textArea.append(" * cRule *");
                }                        
            // Output replaceList
            outputOverridesLinkedList(textArea,rule.replaceList);
            // Output class cases covered array
            textArea.append(" [" + rule.classCasesCovered[0]);
            for(int index=1;index<rule.classCasesCovered.length;index++)
                    textArea.append("," + rule.classCasesCovered[index]);
            textArea.append("]");
            // Output default class and total errors unless default is 0
            if (rule.defaultClass!=0) textArea.append(", D-Class = " +
                                    rule.defaultClass + ", T-errors = " +
                                                       rule.totalErrors);
            else textArea.append("\n");
            }
        }

    /* Outputs a single CBA rule antecedent and consequent. 
    @param rule the rule to be output. */
    
    private void outputRule(RuleNodeCBA rule) {
        // Antecedent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(rule.antecedent);
        else outputItemSet(rule.antecedent);
        
        // Operator
        System.out.print(" -> ");
    
        // Consequent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(rule.consequent);
        else outputItemSet(rule.consequent);
        }

    /* Outputs a single CBA rule antecedent and consequent (GUI version). 
    @param textArea the text area to output data to.
    @param rule the rule to be output. */
    
    private void outputRule(JTextArea textArea, RuleNodeCBA rule) {
        // Antecedent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(textArea,rule.antecedent);
        else outputItemSet(textArea,rule.antecedent);
        
        // Operator
        textArea.append(" -> ");
    
        // Consequent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(textArea,rule.consequent);
        else outputItemSet(textArea,rule.consequent);
        }
        
    /* OUTPUT OVERRIDES LINKED LIST */

    /** Outputs the given overrides linked list associated with a particular
    CBA rule.
    @param listRef the reference to the overrides linked list. */

    private void outputOverridesLinkedList(Overrides listRef) {
        Overrides linkRef = listRef;

        // Check for empty list
        if (linkRef== null) {
            System.out.print(", Overrides linked list: null");
            return;
            }
        else System.out.print(", Overrides linked list:");

        // Loop through list
        int counter = 0;
        while (linkRef != null) {
            if (counter==0) System.out.print(" ");
            else System.out.print(", ");
            counter++;
            outputRule(linkRef.cRule);
            System.out.println(", TID = " + linkRef.tid + ", classLabel = " +
                        linkRef.classLabel);
            linkRef = linkRef.next;
            }
        }
        
    /* OUTPUT OVERRIDES LINKED LIST */

    /** Outputs the given overrides linked list associated with a particular
    CBA rule (GUI versiu
    on).
    @param listRef the reference to the overrides linked list. */

    private void outputOverridesLinkedList(JTextArea textArea, Overrides listRef) {
        Overrides linkRef = listRef;

        // Check for empty list
        if (linkRef== null) {
            textArea.append(" Overrides linked list: null");
            return;
            }
        else textArea.append(" Overrides linked list:");

        // Loop through list
        while (linkRef != null) {
            textArea.append("\t");
            outputRule(textArea,linkRef.cRule);
            textArea.append(", TID = " + linkRef.tid + ", classLabel = " +
                        linkRef.classLabel);
            linkRef = linkRef.next;
            }
        }

    /* OUTPUT NUMBER OF CBA RULES */

    /** Outputs number of generated rules (ARs or CARS). */

    public void outputNumCBArules() {
        System.out.println("Number of CBA rules     = " + getNumCBA_CRs());
    	}

    /* ----------------------------------------- */
    /*                                           */
    /*              DIAGNOSTIC OUTPUT            */
    /*                                           */
    /* ----------------------------------------- */

    /* OUTPUT SET A LINKED LIST */
    /** Outputs the Set A linked list for diagnostic purposes.
    @param trainingSet the array of arrays of training set records. */

    private void outputSetAlinkedList(short[][] trainingSet) {
        int  number  = 1;
        SetA linkRef = startSetAlist;

        while (linkRef!=null) {
        System.out.print(number + ". TID = " + linkRef.tid +
                				", Itemset = ");
        	outputItemSet(trainingSet[linkRef.tid]);
            System.out.print("\ncRule = ");
            outputRule(linkRef.cRule);
            System.out.print("wRule = ");
            outputRule(linkRef.wRule);
            linkRef = linkRef.next;
        	number++;
            }
    	}

    /* OUTPUT INT ARRAY */

    /* Outputs an array of integers.
    @param theArray the gievn array. */

    private void outputIntArray(int[] theArray) {
        if (theArray==null) {
            System.out.print("null");
            return;
            }

        System.out.print("[" + theArray[0]);
        for(int index=1;index<theArray.length;index++) {
            System.out.print("," + theArray[index]);
            }

        // End
        System.out.println("]");
        }    
    }
