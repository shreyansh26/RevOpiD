/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                APRIORI-TFP CBA(CLASSIFICATION BASED ON                     */
/*                        ASSOCIATIONS) APPLICATION                           */
/*                                                                            */
/*                              Frans Coenen                                  */
/*                                                                            */
/*                           Monday 10 May 2004                               */
/*                                                                            */
/*                      Department of Computer Science                        */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

import java.io.*;

/* Classification application the CBA (Classification Based on Associations) 
algorithm proposed by Bing Liu, Wynne Hsu and Yiming Ma, but founded on 
Apriori-TFP.

Compile using:

javac ClassCBA_App.java

Run using java, Example:

java ClassCBA_App -FpimaIndians.D42.N768.C2.num -N2 -S1 -C50

(-F filename, -N number of classifiers).              */

public class ClassCBA_App {

    // ------------------- FIELDS ------------------------

    // None

    // ---------------- CONSTRUCTORS ---------------------

    // None

    // ------------------ METHODS ------------------------

    public static void main(String[] args) throws IOException {
		double time1 = (double) System.currentTimeMillis();
	
		// Create instance of class ClassificationPRM	
		AprioriTFP_CBA newClassification = new AprioriTFP_CBA(args);
				
		// Read data to be mined from file (method in AssocRuleMining class)
		newClassification.inputDataSet();
	
		// Reorder input data according to frequency of single attributes
		// excluding classifiers. Proceed as follows: (1) create a conversion
		// array (with classifiers left at end), (2) reorder the attributes 
		// according to this array. Do not throw away unsupported attributes 
		// as when data set is split (if distribution is not exactly even) we 
		// may have thrown away supported attributes that contribute to the 
		// generation of CRs. NB Never throw away classifiers even if
		// unsupported!
		newClassification.idInputDataOrdering();  // ClassificationAprioriT
		newClassification.recastInputData();      // AssocRuleMining
	
		// Create training data set (method in ClassificationAprioriT class)
		// assuming a 50:50 split
        newClassification.createTrainingAndTestDataSets();
	
		// Mine data, produce T-tree and generate CRs
		newClassification.startClassification();
		newClassification.outputDuration(time1, (double) System.currentTimeMillis());
		/*
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
		*/
		System.out.println("Frequent Itemsets");
		newClassification.outputFrequentSets();
		
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
