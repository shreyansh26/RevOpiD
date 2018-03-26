/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                   APRIORI-TFP CBA (CLASSIFICATION BASED ON                 */
/*                        ASSOCIATIONS) TCV APPLICATIONC                      */
/*                                                                            */
/*                             Frans Coenen                                   */
/*                                                                            */
/*                        Tuesday 19 January 2005                             */
/*                          Updated: 6 March 2012                             */
/*                                                                            */
/*                      Department of Computer Science                        */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

import java.io.*;

/* Ten Cross Validation Classification application using the CBA 
(Classification Based on Associationse) algorithm proposed by Bing Liu, Wynne 
Hsu and Yiming Ma, but founded on Apriori-TFP.

Compile using:

javac ClassCBA_App10.java

Run using java, Example:

java ClassCBA_App10 -FpimaIndians.D42.N768.C2.num -N2 -S1 -C50

(-F filename, -N number of classifiers).              */

public class ClassCBA_App10 {

    // ------------------- FIELDS ------------------------

    // None

    // ---------------- CONSTRUCTORS ---------------------

    // None

    // ------------------ METHODS ------------------------

    public static void main(String[] args) throws IOException {
	// Start timer
	double time1 = (double) System.currentTimeMillis();

	// Create instance of class ClassificationCMAR	
	AprioriTFP_CBA newClassification = new AprioriTFP_CBA(args);
				
	// Read data to be mined from file (method in AssocRuleMining class)
	newClassification.inputDataSet();
	newClassification.setNumRowsInInputSet();
	
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
	
	// Create tenths data sets (method in ClassificationAprioriT class)
	newClassification.createTenthsDataSets();
	
	// Mine data, produce T-tree and generate CRs
	newClassification.commenceTCVwithOutput();
	double duration = newClassification.outputDuration(time1,
				(double) System.currentTimeMillis());
	
	// Output
	System.out.println("<TR><TH>" + newClassification.getFileName() + 
		"</TH><TD>"+ twoDecPlaces(duration) + "</TD><TD>" +
		twoDecPlaces(newClassification.getAverageNumFreqSets()) + 
		"</TD><TD>" +
		twoDecPlaces(newClassification.getAverageNumCRs()) + 
		"</TD><TD>" +
		twoDecPlaces(newClassification.getAverageAccuracy()) + 
		"</TD><TD>" +
		twoDecPlaces(newClassification.getSDaccuracy()) + "</TD>");
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
	}
    }
