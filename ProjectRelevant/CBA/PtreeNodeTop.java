/* -------------------------------------------------------------------------- */
/*                                                                            */
/*          P A R T I A L   S U P P O R T   T R E E  N O D E   T O P          */
/*                                                                            */
/*                               Frans Coenen                                 */
/*                                                                            */
/*                          Wednesday 9 January 2003                          */
/*                             (Revised 5/7/2003)                             */
/*                                                                            */
/*                       Department of Computer Science                       */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/** Top level Ptree node structure. <P> An array of such structures is created 
in which to store the top level of the Ptree. 
@author Frans Coenen
@version 5 July 2003 */

public class PtreeNodeTop {
    
    /*------------------------------------------------------------------------*/
    /*                                                                        */
    /*                                   FIELDS                               */
    /*                                                                        */
    /*------------------------------------------------------------------------*/
    
    /** Partial support for the rows. */
    public int support = 1;
	
    /** Pointer to child structure. */
    public PtreeNode childRef = null;
	   
    /*---------------------------------------------------------------------*/
    /*                                                                     */
    /*                           CONSTRUCTORS                              */
    /*                                                                     */
    /*---------------------------------------------------------------------*/
    
    /** Zero argument constructor. */
    public PtreeNodeTop() {
       }   
    
    /** One argument constructor 
    @param sup the support for the node. */
    /*public PtreeNodeTop(int sup) {
       support=sup;
       }  */ 
    }
