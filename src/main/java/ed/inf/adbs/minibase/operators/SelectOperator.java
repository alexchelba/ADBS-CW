package ed.inf.adbs.minibase.operators;

import java.util.List;

import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.Tuple;


/**
 * 
 * Operator that extracts data from given table which fulfills all selection criteria.
 *
 */
public class SelectOperator extends Operator {
	//child operator (scan)
    private ScanOperator scanOp;
    //list of restrictions to evaluate
    private List<ComparisonAtom> where;
    //name of the table on which select is performed
    String table;

    /**
     * SelectOperator constructor
     * @param so scan operator (child)
     * @param e list of restrictions to evaluate
     */
    public SelectOperator(ScanOperator so, List<ComparisonAtom> e) {
        scanOp = so;
        where = e;
        table = so.getTable();
    }

    /**
     * Returns the next tuple that fulfills all the given ComparisonAtom restrictions
     * @return next tuple that fulfills all the given ComparisonAtom restrictions
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = scanOp.getNextTuple(); //get next tuple
            while (t != null) {
            	SelectEvaluation see = new SelectEvaluation(table, t, where); //evaluate conditions
                if (see.evaluate()) //if the tuple fulfills those conditions, return it
                	return t;
                t = scanOp.getNextTuple(); //else get the next tuple
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calls the child operator's reset method
     */
    @Override
    public void reset() {
        scanOp.reset();
    }
}
