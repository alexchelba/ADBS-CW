package ed.inf.adbs.minibase.operators;

import java.util.List;

import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.Tuple;


/**
 * 
 * Operator that calculates the join between 2 operators.
 *
 */
public class JoinOperator extends Operator {

	//operators of the tables to join
    private Operator leftOp;
    private Operator rightOp;
    //join list of conditions
    private List<ComparisonAtom> e;
    //tuples of each table
    private Tuple leftTuple;
    private Tuple rightTuple;
    //list of common variables of the 2 tuples
    private List<String> commonVars;

    /**
     * JoinOperator constructor
     * @param lo outer table operator
     * @param ro inner table operator
     * @param e list of join conditions
     * @param cv list of common variables names
     */
    public JoinOperator (Operator lo, Operator ro, List<ComparisonAtom> e, List<String> cv) {
        leftOp = lo;
        rightOp = ro;
        this.e = e;
        this.commonVars = cv;
        //we get the first tuple of each table ("initialise the join")
        leftTuple = leftOp.getNextTuple();
        rightTuple = rightOp.getNextTuple();
    }

    /**
     * Returns the next tuple resulting of the join condition (simple (tuple) nested loop join)
     * @return next tuple resulting of the join condition
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = null;
            while (leftTuple != null) { //while we still have tuples from the outer table to explore
                JoinEvaluation jee = new JoinEvaluation(leftTuple, rightTuple, e, commonVars);
                //if there is no expression (cartesian product) or the tuples fulfill the join condition
                if (jee.evaluate())
                    t = new Tuple(leftTuple, rightTuple); //create a new tuple that has all the attributes of both tuples

                //update tuples
                if (rightTuple != null) //get next tuple for the inner table
                    rightTuple = rightOp.getNextTuple();

                if (rightTuple == null) { //if it was the last one
                    rightOp.reset(); //reset the inner operator (to start the inner table scan again)
                    rightTuple = rightOp.getNextTuple(); //get the first tuple of the inner table
                    leftTuple = leftOp.getNextTuple(); //get the next outer table's tuple
                }
                if (t != null) //after updating the tuples, we check if we got a tuple that fulfills the join condition
                    return t; //if so, we return it
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null; //if no tuple fulfills the condition we return null
    }

    //Not used
    @Override
    public void reset() {
    	
    }
	
}
