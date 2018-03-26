/* -------------------------------------------------------------------------- */
/*                                                                            */
/*               CLASSIFICATION CBA APPLICATION 2 FILE                       */
/*                                                                            */
/*                            Frans Coenen                                    */
/*                                                                            */
/*                           Saturday 3 March 2012                            */
/*                                                                            */
/*                      Department of Computer Science                        */
/*                        The University of Liverpool                         */
/*                                                                            */ 
/* -------------------------------------------------------------------------- */

import java.io.*;

/* Prooduces a single-class clasifier from a given data set given particular 
support and confidence thresholds as input using the CBA algorithm. Takes 
two input files: (i) training set, (ii) test set. Orders the input files 
according to frequency of single items. 

Compile using:

javac ClassCBA_2file_App.java

Run using java, Example:

java ClassCBA_2file_App -FtestFile.D8.N20.C2.num -TtestFile.D8.N6.C2.num -S10 -C70 -N2

(-F filename, -T test set filename, -N number of classes, -S minimum 
support threshold, -C minimum confidence threshold).

Note accuracy of classification is entirely dependent on the user
supplied support and confidence thresholds. */


public class ClassCBA_2file_App {
    
    // ------------------- FIELDS ------------------------
    
    // None
    
    // ---------------- CONSTRUCTORS ---------------------
    
    // None
    
    // ------------------ METHODS ------------------------
    
    public static void main(String[] args) throws IOException {
        // Start timer
    	double time1 = (double) System.currentTimeMillis();
        
		// Create instance of class AprioriT_CRgen	
		AprioriTFP_CBA newClassification = 
	                                    new AprioriTFP_CBA(args);
	
		// Read training input file (method in AssocRuleMining class), and
		// test inputfile (method in AprioriTclass).
		newClassification.inputDataSet();	
		newClassification.inputTestDataSet();
	
		// Reorder training data according to frequency of single attributes
		// excluding classifiers. Proceed as follows: (1) create a conversion
		// array (with classifiers left at end), (2) reorder the attributes 
		// according to this array. Do not throw away unsupported attributes 
		// as when data set is split (if distribution is not exactly even) we 
		// may have thrown away supported attributes that contribute to the 
		// generation of CRs. NB Never throw away classifiers even if
		// unsupported! On completion reorder test data according to the 
		// ordering adopted for the training set.
		newClassification.idInputDataOrdering();  // ClassificationAprioriT
		newClassification.recastInputData();      // AssocRuleMining
		newClassification.recastTestData(); 	  // AprioriTclass
	
		// Mine data, produce T-tree and generate CRs
		newClassification.startClassification();
		newClassification.outputDuration(time1,
							(double) System.currentTimeMillis());
	 
		// Standard output
		newClassification.outputNumFreqSets();
		newClassification.outputNumUpdates();
		newClassification.outputStorage();
		newClassification.outputNumRules();
		double accuracy = newClassification.getAccuracy();
		System.out.println("Accuracy = " + twoDecPlaces(accuracy));
		double aucValue = newClassification.getAUCvalue();
		System.out.println("AUC value = " + fourDecPlaces(aucValue));
		
		// Additional output
		//newClassification.outputTtree();
		newClassification.outputRules();
	
		// End	
		System.exit(0);
		}
		
    /* -------------------------------------------------------------- */
    /*                                                                */
    /*                    OUTPUT METHODS                              */
    /*                                                                */
    /* -------------------------------------------------------------- */
	
    /* TWO DECIMAL PLACES */
    
    /** Converts given real number to real number rounded up to two decimal 
    places. 
    @param number the given number.
    @return the number to two decimal places. */ 
    
    protected static double twoDecPlaces(double number) {
    	int numInt = (int) ((number+0.005)*100.0);
		number = ((double) numInt)/100.0;
		return(number);
		}/* FOUR DECIMAL PLACES */

    /** Converts given real number to real number rounded up to four decimal
    places.
    @param number the given number.
    @return the number to gour decimal places. */

    protected static double fourDecPlaces(double number) {
    	int numInt = (int) ((number+0.00005)*10000.0);
		number = ((double) numInt)/10000.0;
		return(number);
		}	
    }
    
