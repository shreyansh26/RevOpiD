
/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                           TOTAL SUPPORT TREE NODE                         */
/*                                                                           */
/*                                Frans Coenen                               */
/*                                                                           */
/*                            Wednesday 2 July 2003                          */
/*                      (Revisions 11/10/2006, 8/10/2008)                    */
/*                                                                           */
/*                       Department of Computer Science                      */
/*                         The University of Liverpool                       */
/*                                                                           */ 
/* ------------------------------------------------------------------------- */
      
import java.io.*;
import java.util.*;

/** Methods concerned with Ttree node structure. Arrays of these structures 
are used to store nodes at the same level in any sub-branch of the T-tree.
@author Frans Coenen
@version 11 October 2006 */

public class TtreeNode {

    /* ------ FIELDS ------ */

    /** The support associate with the itemset represented by the node. */
    public int support = 0;

    /** A reference variable to the child (if any) of the node. */
    public TtreeNode[] childRef = null;

    // Diagnostics
    /** The number of nodes in the T-tree. */
    protected static int numberOfNodes = 0;

    /* ------ CONSTRUCTORS ------ */

    /** Default constructor */

    public TtreeNode() {
	    numberOfNodes++;
	    }

    /** One argument constructor.
    @param sup the support value to be included in the structure. */

    public TtreeNode(int sup) {
	    support = sup;
	    numberOfNodes++;
	    }

    /** Copy constructor with reference to T-tree node.
    @param node the reference to the DIC t-tree node. */

    public TtreeNode(TtreeNode node) {
        support = node.support;
        }

    /* ------ METHODS ------ */

    /** Gets the number of T-tree nodes created.
    @return the number of t-tree nodes. */

    public static int getNumberOfNodes() {
        return(numberOfNodes);
	    }

    /** Set number of T-tree nodes to zero. */

    public static void setNumberOfNodesFieldToZero() {
        numberOfNodes=0;
	    }
    }
