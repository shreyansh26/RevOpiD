/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                          R U L E   N O D E                                 */
/*                                                                            */
/*                            Frans Coenen                                    */
/*                                                                            */
/*                        Tuesday 14 Match 2006                               */
/*                         (Revision 11/10/2006)                              */
/*                                                                            */
/*                    Department of Computer Science                          */
/*                     The University of Liverpool                            */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/** Class for storing binary tree of ARs or CARs as appropriate. (Used to
be defined as an inner class in AssocRuleMining class.)
@author Frans Coenen
@version 11 October 2006 */

/* To Compile: javac RuleNode.java */

public class RuleNode {

    /* ------ FIELDS ------ */

    /** Antecedent of AR. */
    public short[] antecedent;

    /** Consequent of AR. */
    public short[] consequent;

    /** The confidence value associate with the rule represented by this
    node. <P>Same field is used if rules are ordered in some other way,
    e.g. Laplace accuracy or Chi^2 values. */
    public float confidenceForRule= (float) 0.0;
    
    /** The support for a rule. <P>Again same field may be used for some
    other ordering value. */
    public float supportForRule= (float) 0.0;

    /** Rule number, added when bin-tree containing rules is complete. */
    public short ruleNumber=0;

    /** Links to next node */
    public RuleNode leftBranch  = null;
    public RuleNode rightBranch = null;

    /* ------ CONSTRUCTOR ------ */

    /** Three argument constructor
    @param ante the antecedent (LHS) of the AR/CAR.
    @param cons the consequent (RHS) of the AR/CAR.
    @param confValue the associated "confidence" value.
    @param suppValue the associate "support" value.     */

    public RuleNode(short[] ante, short[]cons, double confValue, 
                                                          double suppValue) {
	antecedent        = ante;
	consequent        = cons;
	confidenceForRule = (float) confValue;
        supportForRule    = (float) suppValue;
	}

    /* ------ METHODS ------ */

    /* NONE */

    }
