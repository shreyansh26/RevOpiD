/* -------------------------------------------------------------------------- */
/*                                                                            */
/*             P A R T I A L   S U P P O R T   T R E E  N O D E               */
/*                                                                            */
/*                               Frans Coenen                                 */
/*                                                                            */
/*                          Wednesday 9 January 2003                          */
/*                       (Revised 5/7/2003, 11/10/2006)                       */
/*                                                                            */
/*                       Department of Computer Science                       */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/** Tree structure to store Ptree nodes. <P> Same as top level structure but 
with the addition of a sibling branch. 
@author Frans Coenen
@version 11 October 2006 */

public class PtreeNode {
    
    /*------------------------------------------------------------------------*/
    /*                                                                        */
    /*                                   FIELDS                               */
    /*                                                                        */
    /*------------------------------------------------------------------------*/
    
    /** Partial support for the rows. */
    public int support = 1;

    /** Array of short (16 bit) integers describing the row. */
    public short[] itemSet = null;
	
    /** Pointer to child structure. */
    public PtreeNode childRef = null;
    
    /** Pointer to sibling structurte.  */
    public PtreeNode siblingRef = null;
	   
    /*---------------------------------------------------------------------*/
    /*                                                                     */
    /*                           CONSTRUCTORS                              */
    /*                                                                     */
    /*---------------------------------------------------------------------*/
    
    /** Create P-tree node (other than top-level node)
    @param newItemSet the itemset to be stored at the node.   */ 

    public PtreeNode(short[] newItemSet) {
        itemSet = newItemSet;
	}  
    }
