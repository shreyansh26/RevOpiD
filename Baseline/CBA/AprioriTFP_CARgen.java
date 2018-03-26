/* -------------------------------------------------------------------------- */
/*                                                                            */
/*        APRIORI-TFP CLASSIFICATION ASSOCIATION RULE (CAR) GENERATION        */
/*                                                                            */
/*                               Frans Coenen                                 */
/*                                                                            */
/*                           Tuesday 27 January 2004                          */
/*                       (Revised 5/2/2004, 12/10/2006)                       */
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
                +-- AprioriTFP_CARgen       */

// Java packages

import java.util.*;
import java.io.*;

// Java GUI packages
import javax.swing.*;

/** Methods to produce classification rules using a Apriori-TFP appraoch.
Assumes that input dataset is orgnised such that classifiers are at the end of
each record. CARs differ from ARs in that they have only a single consequent
and that the number of admissable consequents is limited. Note that: (i) number
of classifiers value is stored in the <TT>numClasses</TT> field, (ii) note that
classifiers are assumed to be listed at the end of the attribute list.
@author Frans Coenen
@version 12 October 2006 */

public class AprioriTFP_CARgen extends AprioriTFPclass {

    /* ------ FIELDS ------ */

    // CONSTANTS
    /** The maximum number of CARs */
    protected final int MAX_NUM_CARS = 80000;

    // OTHER FIELDS
    /** The number of CARs generated so far. */
    protected int numCarsSoFar = 0;

    // HILL CLIMBING FIELDS (ONLY USED BY SPECIFIC APPLICATIONS)

    // Constants
    /** Minimum value that support or confidence may be decremented to during
    hill climbing. <P> Note we do not want a support of 0.0 as in this case
    everything will be supported. */
    protected static double minimumSupAndConfValue          = 0.01;
    /** Maximum value that support or confidence may be incremented to during
    hill climbing. */
    protected static final double MAXIMUM_SUP_OR_CONF_VALUE = 99.9;
    /** Ratio of best locations against available locations. <P> used in hill
    climbing process to stop process when a majority of best locations have
    been obtained. */
    protected static final double BEST_TO_AVAILABLE_RATIO   = 2.0/3.0;

    // Hill climbing data structure
    /** Data structure to hold information concerning a particular location in
    the playing area.  */
    protected class HClocData {
    /** Support coordinate */
    double suppCoord = 0.0;
    /** Confidence coordinate */
    double confCoord = 0.0;
    /** Associated accuracy */
    double accuracy = 0.0;
    /** Number of CRs */
    int numberOfCRs = 0;
    /** Flag indicating if location is outside playing area or not (true
    by default) */
    boolean inArea = true;
    /** Flag indicating if accuracy for location has not been claculated
    (true by default) */
    boolean notCalculated=true;

    /** Default constructor */
    protected HClocData() {
        }

    /** Five argument constructor.
    @param supp the Support coordinate
    @param conf the Confidence coordinate
    @param acc the associated accuracy
    @param numCRs the number of clasification rules generated
    @param locationFlag the indicator for whether the location is
    outside palying area or not.
    @param calcFlag the indicator for whether the accuracy for the location
    has been claculated ort not.     */

    protected HClocData(double supp, double conf, double acc, int numCRs,
            boolean  locationFlag, boolean calcFlag) {
        suppCoord     = supp;
        confCoord     = conf;
        accuracy      = acc;
        numberOfCRs   = numCRs;
        inArea        = locationFlag;
        notCalculated = calcFlag;
        }
    }

    // Other fields used in hill climbing

    /** The minimum support value (%) that can be arrived at through the hill
    climbing process. */
    protected double minimumDiffSupport    = 0.1;   // Default
    /** The minimum confidence value (%) that can be arrived at through the
    hill climbing process. */
    protected double minimumDiffConfidence = 1.0;   // Default
    /** 1-D array to hold locatiopns for gill climbing process. */
    protected HClocData[] locData = null;

    /* ------ CONSTRUCTORS ------ */

    /** Constructor processes command line arguments.
    @param args the command line arguments (array of String instances). */

    public AprioriTFP_CARgen(String[] args) {
    super(args);
    }

    /** Constructor with argument from existing instance of class
    AssocRuleMining.
    @param armInstance the given instance of the <TT>AssocRuleMining</TT>
    class. */

    public AprioriTFP_CARgen(AssocRuleMining armInstance) {
    super(armInstance);
        }

    /* ------ METHODS ------ */

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                         START CLASSIFICATION                           */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* START CLASSIFICATION */

    /** Starts CAR generation proces using TFPC.        */

    public void startCARgeneration() {
        // Output nature of algorithm
        String s = "START CLASSIFICATION (BEST FIRST), TFPC CAR GENERATION " +
                                   "WITH X-CHEKING\n-----------------------" +
                                "----------------------------------------\n" +
                                 "Max number of CARS  = " + MAX_NUM_CARS +
               "\nMax size antecedent = " + MAX_SIZE_OF_ANTECEDENT + "\n";

        // proceed
        startCARgeneration(s);
        }

    /** Starts CAR generation proces using TFPC (GUI version).
    @param tArea the text area to output data to.   */

    public void startCARgeneration(JTextArea tArea) {
        // Set text area
        textArea = tArea;

        // proceed
        startCARgeneration();
        }


    /** Starts CAR generation proces using TFPC (version with input string
    argument).
    @param s String to be output to GUI/Command line interface.     */

    protected void startCARgeneration(String s) {
        if (textArea==null) { System.out.print(s); outputLimits(); }
        else { textArea.append(s); outputLimits(textArea); }

        // Continue CAR generation process
        startCARgeneration2();

        // Number Rule in BinTree
        numberRulesInBinTree();
            
        // Output generated rule set if requested
        if (outputRuleSetToFileFlag) outputRulesToFile();
        }

    /** Continues process of genertaing CARS using apriori TFP. */

    protected void startCARgeneration2() {
        // Calculate minimum support threshold in terms of number of
        // records in the training set.
        minSupport = numRowsInTrainingSet*support/100.0;
        String s = "Support = " + twoDecPlaces(support) + ", Confidence = " +
                          twoDecPlaces(confidence) + "\nMinimum support = " +
                                   twoDecPlaces(minSupport) + " (Records)\n";
        if (textArea==null) { System.out.print(s); outputLimits(); }
        else { textArea.append(s); outputLimits(textArea); }

        // Set rule list to null. Note that startRuleList is defined in the
        // AssocRuleMining parent class and is also used to store Association
        // Rules (ARs) with respect ARM.
        startRulelist = null;
        numCarsSoFar = 0;

        // Create P-tree and then generate T-tree and generate CARs (method
        // contained in PartialSupportTree class)
        if (textArea==null) {
            createPtree();
            createTotalSupportTree();
            }
        else {
            createPtree(textArea);
            createTotalSupportTree(textArea);
            }
        }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                         TEN CROSS VALIDATION (TCV)                     */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* COMMEMCE TEN CROSS VALIDATION WITH OUTPUT */

    /** Start Ten Cross Validation (TCV) process with CAR generation. */

    public void commenceTCVwithOutput() {
        double[][] parameters = new double[10][4];

        System.out.println("START TCV APRIORI-TFP CAR GENERATION\n" +
                            "------------------------------------");
        System.out.println("Max number of CARS  = " + MAX_NUM_CARS);
        System.out.println("Max size antecedent = " + MAX_SIZE_OF_ANTECEDENT);
        
        // Loop through tenths data sets
        for (int index=0;index<10;index++) {
            String s = "[--- " + (index+1) + " ---] ";
            if (textArea==null) System.out.println(s);
            else textArea.append(s);
            // Create training and test sets
            createTrainingAndTestDataSets(index);
            // Mine data, produce T-tree and generate CRs
            startCARgeneration();
            // Set parameters
            parameters[index][0] = numFrequentSets;
            parameters[index][1] = numUpdates;
            parameters[index][2] = calculateStorage();
            parameters[index][3] = getNumRules();
            }

        // Determine totals
        ouputTCVparam(parameters);
        double totalNumFreqSets = 0;
        double totalNumUpdates  = 0;
        double totalStorage     = 0;
        double totalNumCARs     = 0;
        for (int index=0;index<parameters.length;index++) {
            totalNumFreqSets = totalNumFreqSets+parameters[index][0];
            totalNumUpdates  = totalNumUpdates+parameters[index][2];
            totalStorage     = totalStorage+parameters[index][2];
            totalNumCARs     = totalNumCARs+parameters[index][3];
            }

        // Calculate averages
        averageNumFreqSets = totalNumFreqSets/10;
        averageNumUpdates  = totalNumUpdates/10;
        averageNumCRs      = totalNumCARs/10;

        // Output avergaes
        outputTCVaverages(averageNumFreqSets,averageNumUpdates,averageNumCRs);
        }

    /** Start Ten Cross Validation (TCV) process with CAR generation only,
    GUI version.
    @param tArea the given instance of the <TT>JTextArea</TT> class. */

    public void commenceTCVwithOutput(JTextArea tArea) {
        textArea = tArea;

        // Proceed
        commenceTCVwithOutput();
        }

    /** Outputs TCV accuraces.
    @param aveNumFreqSets the average number of frequent sets as the result of
    TCV.
    @param aveNumUpdates the number of updates as the result of TCV.
    @param aveNumCRs the average number of callsification rules as the result
    of TCV. */

    private void outputTCVaverages(double aveNumFreqSets, double aveNumUpdates,
                    double aveNumCRs) {
        // Generate string
        String s = "---------------------------------------\n" +
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
            String s = "(" + (index+1) + ") # Freq. Sets = " +
            twoDecPlaces(parameters[index][0]) + ",\t# Updates = " +
            ((int) parameters[index][1]) + ",\tStoraga = " +
        ((int) parameters[index][2]) + ",\t# CARs = " +
        ((int) parameters[index][3]);
        // Output
            if (textArea==null) System.out.println(s);
            else textArea.append(s + "\n");
            }
        }

    /* ------------------------------- */
    /*         T-TREE METHODS          */
    /* ------------------------------- */

    /* CREATE T-TREE LEVEL N */

    /** Commences the process of determining the remaining levels in the T-tree
    (other than the top level), level by level in an "Apriori" manner. <P>
    Follows an add support, prune, generate loop until there are no more levels
    to generate. Overides method in AprioriTFPclass class which in turn
    overides method TotalSupportTree class. Distinctiion between the methods is
    that this version produces CARs as opposed to CRs or ARs respectively. */

    protected void createTtreeLevelN() {
        int nextLevel=2;

    // Loop while a further level exists
    while (nextLevelExists) {
        // Add support
        addSupportToTtreeLevelN(nextLevel);
        // Prune unsupported candidate sets (method defined in
        // PartialSupportTree class)
        pruneLevelN(startTtreeRef,nextLevel);
        // Generate Classification Association Rules (CARs)
        generateCARs(nextLevel);
        // Check number of frequent sets generated so far
        if (numFrequentSets>MAX_NUM_FREQUENT_SETS) {
            String s ="Level = " + nextLevel + ", Number of frequent " +
             "sets (" + numFrequentSets + ") generted so far " +
                               "exceeds limit of " + MAX_NUM_FREQUENT_SETS +
                          ", generation process stopped!\n";
                if (textArea==null) System.out.println(s);
                else textArea.append(s);
        break;
        }
        // Check antecedent size, level will indicate size of any
        // frequent sets generated so far. The antecedent size will this be
        // the level number minus 1
        if (nextLevel > MAX_SIZE_OF_ANTECEDENT) {
            String s = "Current CR antecedent size (" + nextLevel +
                            ") exceeds limit of " + MAX_SIZE_OF_ANTECEDENT +
                              ", generation process stopped!\n";
                if (textArea==null) System.out.println(s);
                else textArea.append(s);
        break;
        }
        // Attempt to generate next level (method defined in
        // PartialSupportTree class)
        nextLevelExists=false;
        generateLevelN(startTtreeRef,nextLevel,null);
        nextLevel++;
        }
    }

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*            CLASSIFICATION ASSOCIATION RULE (CAR) GENERATION            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* GENERATE CLASSIFICATION ASSOCIATION RULES */

    /** Initiates process of generating Classification Association Rules (CARS),
    Loops through top level of T-tree as part of the CAR generation process.
    <P>CARs differ from ARs in that they have only a single consequent and that
    the number of admissable consequents is limited. Note that classifiers are
    assumed to be listed at the end of the attribute list.
    @param level the current level in the T-tree. */

    protected void generateCARs(int level) {

    // Loop through classifiers
    for (int index=numOneItemSets-numClasses+1;
                    index<=numOneItemSets;index++) {
        // Check number of CARS generated so far
        if (numCarsSoFar>MAX_NUM_CARS) {
            String s = "Number of CARs (" + numCarsSoFar + ") generted " +
                           "so far exceeds limit of " + MAX_NUM_CARS +
                             ", generation process stopped!\n";
                if (textArea==null) System.out.println(s);
                else textArea.append(s);
            return;
            }
        // Else process
        if (startTtreeRef[index]!=null &&
                        startTtreeRef[index].childRef!=null) {
            if (startTtreeRef[index].support >= minSupport) {
            short[] consequent = new short[1];
            consequent[0] = (short) index;
            generateCARs(null,index,level-1,consequent,
                            startTtreeRef[index].childRef);
            }
        }
        }
    }

    /* GENERATE CLASSIFICATION ASSOCIATION RULES */

    /** Continues process of generating classification association rules from
    a T-tree by recursively looping through T-tree level by level.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array lavel in the T-tree.
    @param level the current level in the T-tree
    @param consequent the current consequent (classifier) for the CAR.
    @param linkRef the reference to the current array lavel in the T-tree. */

    protected void generateCARs(short[] itemSetSofar, int size, int level,
                         short[] consequent, TtreeNode[] linkRef) {
    // If no more nodes return
    if (linkRef == null) return;

    // At right level
    if (level==1) generateCARsRightLevel(itemSetSofar,size,consequent,
                                      linkRef);

    // Wrong level, Otherwise process
    else generateCARsWrongLevel(itemSetSofar,size,consequent,level,
                                     linkRef);
    }

    /* GENERATE CLASSIFICATION ASSOCIATION RULES (RIGHT LEVEL). */

    /** Generating classificationh association rules from a given array of
    T-tree nodes.
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
                insertRuleIntoRulelist(tempItemSet,consequent,
                          confidenceForCAR,linkRef[index].support);
                }
            }
        }
    }

    /* GENERATE CLASSIFICATION ASSOCIATION RULES (WRONG LEVEL). */

    /** Generating classificationh association rules from a given array of
    T-tree nodes.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array lavel in the T-tree.
    @param consequent the current consequent (classifier) for the CAR.
    @param level the current level in the T-tree.
    @param linkRef the reference to the current array lavel in the T-tree. */

    protected void generateCARsWrongLevel(short[] itemSetSofar, int size,
                short[] consequent, int level, TtreeNode[] linkRef) {
        // Loop through T-tree array
    for (int index=1; index < size; index++) {
        // Check if node exists
        if (linkRef[index] != null && linkRef[index].childRef!=null) {
            short[] tempItemSet = realloc2(itemSetSofar,(short) index);
        // Proceed down child branch
        generateCARs(tempItemSet,index,level-1,consequent,
                                  linkRef[index].childRef);
        }
        }
    }

    /* ========================================================== */
    /*                                                            */
    /*                       HILL CLIMBING METHODS                */
    /*                                                            */
    /* ========================================================== */

    /* Apriori-TFP classification with 8 point hill climbing. The hill
    climbing approach assumes a 2D space of the form:

    Support
    ^
    |
    |
    |
    |
    +---------> Confidence

    Thus increasing support has the effect of moving "north" and decreasing
    support "south". We increase/decrease support/confidence using the dsupp
    and doconf values. The algorithm thus moves through the space adjusting
    support and confiodence coordinates with a view to maximising accuracy.

    We test <B>EIGHT</B> points round a central location and pick either (i)
    the central location to "zero in" on, or (ii) one pf the eight points to
    move to. Alternatives that have been tried (at least with respect to
    Apriori-TFP) include 4 point hill climbing, however the results achieved
    have not been as good (although 4 point hill climbing requirers less
    time). */

    /* HILL CLIMBING */

    /** Commences hill climbing process to maximise accuracy by adjusting
    support and confidence thresholds. <P> This is done by generating accuracies
    for eight points round the current lcation:

    <PRE>
            7--------0--------1
        |        |        |
     +dsupp |        |        |
            |        |        |
        6--------8--------2
        |        |        |
     -dsupp |        |        |
            |        |        |
        5--------4--------3
              -dconf   +dconf
    </PRE>

    provided that we do not: (i) calculate an accuracy we have already
    calculated (information available in the location data) or (ii) move
    out of the search space.
    @param currentSupp the current support threshold.
    @param currentConf the current confidence threshold.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value.
    @param currentAccuracy the accuracy given the current support
    (<TT>currentSupp</TT>) and confidence (<TT>currentConf</TT>) thresholds.
    @param currentNumCRs the number of rules generated given the current
    support (<TT>currentSupp</TT>) and confidence (<TT>currentConf</TT>)
    thresholds.     */

    protected void hillClimbing(double currentSupp, double currentConf,
                        double dsupp, double dconf, double currentAccuracy,
            int currentNumCRs) {
    // Stubb
    }

    /* ---------------------------------------------------------- */
    /*                                                            */
    /*                       MOVE OR ZERO IN                      */
    /*                                                            */
    /* ---------------------------------------------------------- */

    /* IDENTIFY HILL CLIMBING OPTION */

    /** Determines whether to implement a change in the value of the support or
    confidence or to "zero in" on the current support and confidence.<P>
    Proceed as follows:
    1) Determine best accuracy
    2) Options:
       i) If
    2) Determine number of best accuracies:
        a) If Only one best accuracy:
            i)  If best accuracy at center "zero" in
            ii) Otherwise move to best accuracy and repaeat
        b) If number of best accuracy locations equivalent to number of
           available locations (i.e. excluding loctaions outside playing area)
           emd.
        c) Otherwise select best location out of available best accuracies.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value.
    (<TT>currentSupp</TT>) and confidence (<TT>currentConf</TT>) thresholds.*/

    protected void idHCoption(double dsupp, double dconf) {

        // Find location with maximum accuracy (if more than one location have
    // the same maximum accuracy then the first such location found is
    // recorded).
        int maxIndex = getLocationWithBestAccuracy();
        double maxAccuracy = locData[maxIndex].accuracy;
        String s = "Best Accuracy = " + twoDecPlaces(maxAccuracy) + "\n";
        if (textArea==null) System.out.println(s);
        else textArea.append(s);

        // Determine number of location with the best accuracy
        int numBestAccuracies = getNumBestAccuracyLocations(maxAccuracy);

        // If only one best accuracy then either "zero in" or "move to new
    // location as appropriate.
    if (numBestAccuracies==1) {
        moveOrZeroIn(maxIndex,locData[maxIndex].suppCoord,
                  locData[maxIndex].confCoord,dsupp,dconf,
              locData[maxIndex].accuracy,locData[maxIndex].numberOfCRs);
        return;
        }

        // Determine number of available locations (i.e. excluding locations
    // outside the paying area), maximum is 9
        int availableLocations = getNumAvailableLocations();

        // If number of locations with "best accuracy" is equal to the number
    // of available locations -1 (i.e. nearly all locations have best
    // accuracy) then end.
    if (numBestAccuracies>=
                   ((double) availableLocations*BEST_TO_AVAILABLE_RATIO)) {
            endHillClimbing(locData[maxIndex].suppCoord,
                    locData[maxIndex].confCoord,maxAccuracy,
                locData[maxIndex].numberOfCRs);
        return;
        }

    // Select one of the available locations with "best accuracy" and
    // proceed
    identifyNewLocation(dsupp,dconf,maxAccuracy);
        }

    /* GET LOCATION WITH BEST ACCURACY */
    /** Finds the location with the best accuracy. <P> Note that if there
    exists more than one location with equal best accurcy the first such
    location found returned.
    @return index of location with best accuracy. */

    private int getLocationWithBestAccuracy() {
        double maxAccuracy = locData[8].accuracy;   // Start max accuarcy
    int    maxIndex    = 8;             // Default

    // Loop through location data
    for(int index=7;index>=0;index--) {
        if (locData[index].accuracy>maxAccuracy) {
            maxAccuracy=locData[index].accuracy;
        maxIndex=index;
        }
        }

    // Return
    return(maxIndex);
    }

    /* GET NUMBER OF BEST ACCURACY LOCATIONS */

    /** Returns the number of locations that feature the identified best
    accuracy.
    @param maxAccuracy the identified best accuracy.
    @return the number of "best accuracy" lcations. */

    private int getNumBestAccuracyLocations(double maxAccuracy) {
        int counter=0;

    // Loop through location data
        for(int index=0;index<locData.length;index++) {
        if (similar2dec(locData[index].accuracy,maxAccuracy)) counter++;
            }

    // Return
    return(counter);
    }

    /* GET NUMBER AVAILABLE  LOCATIONS */

    /** Calculates the  number of available locations (i.e. excluding
    locations outside the paying area), maximum is 9.
    @return the number of locations. */

    private int getNumAvailableLocations() {
        int availableLocations=0;

    // Loop through location data
    for (int index=0;index<locData.length;index++) {
            if (locData[index].inArea) availableLocations++;
            }

    // Return
    return(availableLocations);
    }

    /* END HILL CLIMBING */

    /** Ends the hill climbing process where a uniform accuracy has been
    achieved. <P> Alternative mechamism whereby hill climbing can be ended
    is where the support and confidence have been reduced to a minimum.
    @param supp the current support threshold.
    @param conf the current confidence threshold.
    @param acc the accuracy for the given support and confidence.
    @param numRules number of classification rules for the given support and
    confidence.     */

    private void endHillClimbing(double supp, double conf, double acc,
                                int numRules) {
    if (textArea==null) System.out.println("END");
        else textArea.append("END\n");
        support    = supp;
    confidence = conf;
    accuracy   = acc;
    numCRs     = numRules;
        }

    /* MOVE OR ZERO IN */

    /** Moves to new location or zeroes in on ciurrent loacation within hill
    climbing algorithm.
    @param maxInde the index of the most accurate location within the
    newAccuracies array which is used as a flag to move the loccation
    data array as appropriate.
    @param supp the current support adjustment value.
    @param conf the current confidence adjustmernt value.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value.
    @param accuracy the accuracy associated with the current support and
    confidence threshold values.
    @param numCRs the number of classification rules associated with the
    current support and confidence threshold values.     */

    private void moveOrZeroIn(int maxIndex, double supp, double conf,
                              double dsupp, double dconf, double accuracy,
                  int numCRs) {
    // If max index is eight then current accuraccy is the best accuracy
    // therefore "zero in" on current support and confidence.
    if (maxIndex==8) zeroInOnSuppAndConf(supp,conf,dsupp,dconf,accuracy,
                    numCRs);

    // Otherwise move to new location
    else moveToNewLocation(maxIndex,supp,conf,dsupp,dconf,accuracy,numCRs);
    }

    /* IDENTIFY NEW LOCATION */

    /** Continues hill climbing process where a number of best accuracy
    locations have been identified. <P> Procceds as follows:
    1) If center is one of the best accuracy locations then zero in on center.
    2) Otherwise, For each best accuracy increment a total counter (N, E, S or 
       W) according to location,
    3) Identify a location by considering totals.
       a) If identified location is a "best" location move to this location.
       b) If identified location is not a "best" location (i.e. dies not have
          the best accuracy associated with it) but centre is a
          best location zero in on the center location.
       c) Otherwise select location as follows by mobing outward in both a
          clockwise and anti-clockwise direction from identified loaction.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value.
    @param maxAccuracy the best accuracy so far. */

    private void identifyNewLocation(double dsupp, 
                        double dconf, double maxAccuracy) {
    // If center is best location zero in on this location
    if (similar2dec(locData[8].accuracy,maxAccuracy)) {
        zeroInOnSuppAndConf(locData[8].suppCoord,locData[8].confCoord,
                        dsupp,dconf,locData[8].accuracy,
                    locData[8].numberOfCRs);
        return;
        }
        
        // Determine total support and confidence values (exclude center)
        int totalN=0, totalE=0, totalS=0, totalW=0;
        for(int index=0;index<locData.length-1;index++) {
        if (similar2dec(locData[index].accuracy,maxAccuracy)) {
            switch (index) {
                case 0: totalN++; break;        // N
                case 1: totalN++; totalE++; break;  // NE
                case 2: totalE++; break;        // E
                case 3: totalS++; totalE++; break;  // SE
                case 4: totalS++; break;        // S
                case 5: totalS++; totalW++; break;  // SW
                case 6: totalW++; break;        // W
                case 7: totalN++; totalW++; break;  // NW
                }
            }
            }

        // Identify a best location (note this may not necessarily be a best
    // accuracy location).
        int bestLocIndex = identifyLocation(totalN,totalE,totalS,totalW);
    
    // If identified location is not a "best accuracy" identify a best 
    // accuracy location nearest to the identified location
        if (!similar2dec(locData[bestLocIndex].accuracy,maxAccuracy)) {
           bestLocIndex = findNextBestLocation(bestLocIndex,maxAccuracy);
       }
   
    // Move to identified new location
    moveToNewLocation(bestLocIndex,locData[bestLocIndex].suppCoord,
                           locData[bestLocIndex].confCoord,dsupp,dconf,
                       locData[bestLocIndex].accuracy,
                               locData[bestLocIndex].numberOfCRs);
    return;
    }
    
    /* IDENTIFY LOCATION */

    /** Identifies a best location where a number of best avvuracy locatiopns 
    have been identified. <P> Best location identified by number of "counts" 
    indicating north, south east or west. For example if greatest number of 
    counts is to the N and east-west counts are equivalent mobe north. Note: 
    may produce a location which is not a best accuracy loaction in which case 
    this must be resolved later.
    @param totalN the number of counts in favour of going north.
    @param totalE the number of counts in favour of going east.
    @param totalS the number of counts in favour of going south.
    @param totalW the number of counts in favour of going west.
    @param return the chosen lication index.    */

    private int identifyLocation(int totalN, int totalE, int totalS,
                                int totalW) {
        int index;
         
        // Move to north?
        if  (totalN>totalS) {   
            if (totalW>totalE) index = 7;   // Move NW
            else if (totalE>totalW) index=1;    // Move NE
            else index=0;           // Move N
            }

        // Move to south?
        else if (totalS>totalN) {
            if (totalW>totalE) index = 5;   // Move SW
            else if (totalE>totalW) index=3;    // Move SE
            else index=4;           // Move S
            }

        // Move west or east or (default) stay in center?
        else {
            if (totalW>totalE) index = 6;   // Move W
            else if (totalE>totalW) index=2;    // Move E
            else index=8;           // Center
            }
        
        // Return
        return(index);
        }

    /* FIND NEXT BEST LOCATION */

    /** Commences the process of finding the nearest best accuraccy location 
    to a given best location (which is not a best accuracy location).<P>
    commence by testing whether given best location is at center or not,
    if so move "north", otherwise proceed.
    @param index the index of the best accuracy loaction  .
    @param maxAccuracy the best accuracy so far.
    @param return index of "next best" location. */

    private int findNextBestLocation(int index, double maxAccuracy) {
    
        // If at center move "north" first
        if (index==8) index=0;
    
        // Try moving left (anti-clockwise)
        return(findNextBestLocation(index,index,maxAccuracy));
        }
    
    /** Finds the next best location where the identified location is not a
    "best" location (i.e. does not hava the maximum accuracy associated with it.
    <P> proceeds in a breadth first fasion moving out in both anti-clockwise
    and clockwise direction direction from the current index.
    @param indexLeft the index from which to move left (anti-clockwise).
    @param indexRight the index from which to move right (clockwise).
    @param maxAccuracy the best accuracy so far.
    @param return index of "next best" location. */

    private int findNextBestLocation(int indexLeft, int indexRight,
                                double maxAccuracy) {
        // Try moving left (anti-clockwise)
        if (indexLeft==0) indexLeft=7;
        else indexLeft=indexLeft-1;
        if (similar2dec(locData[indexLeft].accuracy,maxAccuracy)) 
                                return(indexLeft);
        
        // Try Moving right (clockwise)
        if (indexRight==7) indexRight=0;
        else indexRight=indexRight+1;
        if (similar2dec(locData[indexRight].accuracy,maxAccuracy)) 
                                return(indexRight);
        
        // Repeat and return
        return(findNextBestLocation(indexLeft,indexRight,maxAccuracy));
        }
        
    /* ZERO IN ON SUPPORT AND CONFIDENCE */
    
    /** "Zero's" in on current support and confidence thresholds. <P> Location
    array indexes are as follows:
    <PRE>
    +---+---+---+   ^
    | 7 | 0 | 1 |   | Support
    +---+---+---+   |
    | 6 | 8 | 2 |
    +---+---+---+
    | 5 | 4 | 3 |
    +---+---+---+   ---> Confidende
    </PRE>
    @param currentSupp the current support threshold.
    @param currentConf the current confidence threshold.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value. 
    @param currentAccuracy the accuracy given the current support
    (<TT>currentSupp</TT>) and confidence (<TT>currentConf</TT>) thresholds. 
    @param currentNumCRs the number of classification rules generated given the 
    current support (<TT>currentSupp</TT>) and confidence 
    (<TT>currentConf</TT>) thresholds.  */
                        
    private void zeroInOnSuppAndConf(double currentSupp, double currentConf, 
                double dsupp, double dconf, double currentAccuracy,
            int currentNumCRs) {
        if (textArea==null) System.out.println("ZERO IN");
        else textArea.append("ZERO IN\n");
    
    // Define new location array    
    createLoactaionArray();
    locData[8] = new HClocData(currentSupp,currentConf,currentAccuracy,
            currentNumCRs,true,false);  // Start location done
    
    // If 'dsupp' already at minimum, reduce confidence only, therefore
    // do not calculate accuracies for indexes 0,1,3,4,5 and 7
    if (dsupp < minimumDiffSupport) {
        for (int index=0;index<6;index++) locData[index].inArea=false;
        locData[7].inArea=false;
        double newDconf = dconf/2.0;
        hillClimbing(currentSupp,currentConf,dsupp,newDconf,
                            currentAccuracy,currentNumCRs);
        return;
        }
    
    // If 'dconf' already at minimum, reduce support only, therefore
    // do not calculate accuracies for indexes 1,2,3,5,6 and 7
    if (dconf < minimumDiffConfidence) {
        for (int index=1;index<4;index++) locData[index].inArea=false;
        for (int index=5;index<8;index++) locData[index].inArea=false;
        double newDsupp = dsupp/2.0;
        hillClimbing(currentSupp,currentConf,newDsupp,dconf,
                            currentAccuracy,currentNumCRs);
        return;
        }    
        
    // Otherwise reduce both support and confidance
    double newDconf = dconf/2.0;
    double newDsupp = dsupp/2.0;
    hillClimbing(currentSupp,currentConf,newDsupp,newDconf,
                            currentAccuracy,currentNumCRs);
    }
    
    /* CHECK SUPPORT */
    
    /** Checks whether adjusted support value is still within range. <P> used
    during hill climbing where support value may be increment/decrement out of 
    the "paying" area.
    @param newSupp the given support value.
    @return true if within range, and false otherwise. */
    
    protected boolean checkSupport(double newSupp) {
        if (newSupp<=minimumSupAndConfValue || 
            newSupp>=MAXIMUM_SUP_OR_CONF_VALUE) return(false);
    else return(true);
    }
    
    /* CHECK CONFIDENCE */
    
    /** Checks whether adjusted confidence value is still within range. <P> 
    used during hill climbing where confidence value may be increment/decrement 
    out of the "palying" area.
    @param newConf the given confidence value.
    @return true if within range, and false otherwise. */
    
    protected boolean checkConfidence(double newConf) {
        if (newConf<=minimumSupAndConfValue || 
            newConf>=MAXIMUM_SUP_OR_CONF_VALUE) return(false);
    else return(true);
    }
    
    /* MOVE TO NEW LOCATION */
    
    /** Implements change in support or confidence value. <P> Moving along the
    N-S axis has the effiect of increasing/decreasing support, and moving along
    the E-W axis the effect of increasing/decreasing condidence.
    @param flag the idicator for whether we are increassing or lowering support 
    and/or confidence.
    @param supp the current support threshold.
    @param conf the current confidence threshold.
    @param dsupp the current support adjustment value.
    @param dconf the current confidence adjustmernt value. 
    @param accuracy the accuracy given the current support and confidence. 
    @param numCRs the number ogf generated classification rules given the 
    current support and confidence.     */
    
    protected void moveToNewLocation(int flag, double supp, double conf, 
                 double dsupp, double dconf, double accuracy, int numCRs) {
    switch (flag) {
        case 0: // Increase support (north)
            moveLocDataForIncSupp();
        break;      
        case 1: // Increase support and confidence (north-east)
        moveLocDataForIncSuppIncConf();
        break;
        case 2: // Increase confidence (east)
            moveLocDataForIncConf();
        break;      
        case 3:    // Decrease support and increase confidence (south-east)
        moveLocDataForIncSuppDecConf();
        break;      
        case 4: // Decrease support (south))
            moveLocDataForDecSupp();
        break;      
        case 5: // Decrease support and confidence (south-west)         
        moveLocDataForDecSuppDecConf();
        break;
        case 6: // Decrease confidence (west)
            moveLocDataForDecConf();
        break;      
        case 7:    // Increase support and decrease confidence (north-west)
        moveLocDataForDecSuppIncConf();
        break;
        default:
            JOptionPane.showMessageDialog(null,"Unexpected hill climbing " +
                       "error","HILL CLIMBING ERROR",JOptionPane.ERROR_MESSAGE);
        System.exit(1);
        }

        if (textArea==null) System.out.println("MOVE");
        else textArea.append("MOVE\n");
    // Proceed with move
    hillClimbing(supp,conf,dsupp,dconf,accuracy,numCRs);
    }
        
    /* -------------------------------------------------------------- */
    /*                                                                */
    /*           LOCATION DATA ARRAY MANIPULATION METHODS             */
    /*                                                                */
    /* -------------------------------------------------------------- */  
       
    /* CREATE LOCATION ARRAY */
    
    /** Creates a location array to allow hill climbing approach to keep track 
    of where "it has been". */
    
    public void createLoactaionArray() {
        // Dimensionarray
        locData = new HClocData[9];
    
    // Populate grid with default values
    for (int index=0;index<locData.length;index++) {
        locData[index]=new HClocData();
        }
    }
    
    /* MOVE LOCATION ARRAY FOR INCREASED SUPPORT (NORTH)*/
    
    /** Adjusts hill climbing grid when increasing support.  */
    
    public void moveLocDataForIncSupp() {
    int[] fromArray  = {6,8,2,7,0,1};
    int[] toArray    = {5,4,3,6,8,2};
    int[] resetArray = {7,0,2};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }
    
    /* MOVE LOCATION ARRAY FOR INCREASED SUPPORT AND CONFIDENCE (NORTH-EAST)*/
    
    /** Adjusts hill climbing grid when increasing support and confidence.   */
    
    public void moveLocDataForIncSuppIncConf() {
        int[] fromArray  = {8,0,1,2};
    int[] toArray    = {5,6,8,4};
    int[] resetArray = {7,0,1,2,3};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
    
    /* MOVE LOCATION ARRAY FOR INCREASED CONFIDENCE (EAST) */
    
    /** Adjusts hill climbing grid when increasing confidence.    */
    
    public void moveLocDataForIncConf() {
        int[] fromArray  = {0,8,4,1,2,3};
    int[] toArray    = {7,6,5,0,8,4};
    int[] resetArray = {1,2,3};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
    
    /* MOVE LOCATION ARRAY FOR INCREASED SUPPORT AND DECREASED CONFIDENCE 
    (SOUTH-EAST) */
    
    /** Adjusts hill climbing grid when increasing support and decreasing 
    confidence.     */
    
    public void moveLocDataForIncSuppDecConf() {
        int[] fromArray  = {8,2,3,4};
    int[] toArray    = {7,8,8,6};
    int[] resetArray = {1,2,3,4,5};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
        
    /* MOVE LOCATION ARRAY FOR DECREASED SUPPORT (SOUTH) */
    
    /** Adjusts hill climbing grid when decreasing support.         */
    
    public void moveLocDataForDecSupp() {
        int[] fromArray  = {6,8,2,5,4,3};
    int[] toArray    = {7,0,1,6,8,2};
    int[] resetArray = {5,4,3};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
        
    /* MOVE LOCATION ARRAY FOR DECREASED SUPPORT AND CONFIDENCE (SOUTH-WEST) */
    
    /** Adjusts hill climbing grid when decreasing support and confidence. */
    
    public void moveLocDataForDecSuppDecConf() {
        int[] fromArray  = {8,4,5,6};
    int[] toArray    = {1,2,8,0};
    int[] resetArray = {3,4,5,6,7};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
    
    /* MOVE LOCATION ARRAY FOR DECREASED CONFIDENCE (WEST)*/
    
    /** Adjusts hill climbing grid when decreasing confidence.   */
    
    public void moveLocDataForDecConf() {
        int[] fromArray  = {0,8,4,7,6,5};
    int[] toArray    = {1,2,3,0,8,4};
    int[] resetArray = {7,6,5};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }   
       
    /* MOVE LOCATION ARRAY FOR DECREASED SUPPORT AND INCREASED CONFIDENCE 
    (NORTH-WEST) */
    
    /** Adjusts hill climbing grid when decreasing support and increasing 
    confidence.    */
    
    public void moveLocDataForDecSuppIncConf() {
        int[] fromArray  = {8,7,0,6};
    int[] toArray    = {3,8,2,4};
    int[] resetArray = {7,6,5,0,1};
    
    moveLocData(fromArray,toArray);
    resetLocData(resetArray);
    }       

    /* MOVE LOCATION DATA */
    
    /** Moves location data from one part of loactaion array to another. <P>
    Mote that it is up to the programmer to order the to and from arrays so as
    to ensure that required data is not "over written". 
    @param fromArray the array of indexes to move data from.
    @param toArray the array of indexes to move data to. */
    
    private void moveLocData(int[] fromArray, int toArray[]) {
        // Loop through from arrray
    for (int index=0;index<fromArray.length;index++) {
        locData[toArray[index]] = locData[fromArray[index]];
        }
    }
    
    /* MOVE LOCATION DATA */
    
    /** Resets cells in location data array to satar values. 
    @param resetArray the array of indexes to be reset. */
    
    private void resetLocData(int[] resetArray) {
        // Loop through from arrray
    for (int index=0;index<resetArray.length;index++) {
        locData[resetArray[index]] = new HClocData();
        }
    }
    
    /* -------------------------------------------------- */
    /*                                                    */
    /*                   GET METHODS                      */
    /*                                                    */
    /* -------------------------------------------------- */
    
    /* GET NUM CRs (VERSION 2)*/
    
    /** Returns the number of generated classification rules. <P> Different to
    <TT>getNumCRS</TT> method in <TT>AssocRuleMining</TT> class which actually
    calculates the number of rules.
    @return the number of rules. */
    
    public int getNumCRsVersion2() {
        return(numCRs);
    }
    
    /* ---------------------------------------------- */
    /*                                                */
    /*                   OUTPUT                       */
    /*                                                */
    /* ---------------------------------------------- */

    /** Outputs grid with support and confidence vlaues. <P> Used for
    diagnostic purposes only.        */

    protected void outputHClocData(){
        String s = "---------------------------------\n";
        if (textArea==null) System.out.println(s);
        else textArea.append(s);
    String[] labels = {" N","NE"," E","SE"," S","SW"," W","NW"," C"};

    for (int index=0;index<locData.length;index++) {
        s = labels[index] + ":\tacc = " +
        twoDecPlaces(locData[index].accuracy) + ",\tnumCRs = " +
        locData[index].numberOfCRs + ",\tsupp = " +
        twoDecPlaces(locData[index].suppCoord) + ",\tconf = " +
        twoDecPlaces(locData[index].confCoord) + ",\tinArea = " +
        locData[index].inArea + ",\tnotCalculated = " +
        locData[index].notCalculated + "\n";
        if (textArea==null) System.out.println(s);
            else textArea.append(s);
            }

    s = ("---------------------------------\n");   
    if (textArea==null) System.out.println(s);
        else textArea.append(s);
        }
    }