package ed.inf.adbs.minibase.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ed.inf.adbs.minibase.base.Tuple;

/**
 * 
 * Operator that, given a list of tuples, returns the unique tuples in it
 *
 */
public class DistinctOperator extends Operator {
	
	//tuples of which the duplicates will be eliminated
    private List<Tuple> tuples;
    //set of unique tuples
    private TreeSet<Tuple> uniqueTuples;
    //unique tuples in order of their appearance in the initial list
    private List<Tuple> finalTuples;

    /**
     * DistinctOperator constructor
     * @param tuples sorted tuples
     */
    public DistinctOperator(List<Tuple> tuples) {
        this.tuples = tuples;
        uniqueTuples = new TreeSet<>();
        finalTuples = new ArrayList<>();
    }
	
    /**
     * Returns list of unique tuples in order of their appearance in the initial list of tuples
     * @return list of unique tuples
     */
    public List<Tuple> getUniqueTuples() {
    	for(Tuple t : tuples) {
    		if(!uniqueTuples.contains(t)) {
    			finalTuples.add(t);
    			uniqueTuples.add(t);
    		}
    	}
    	return finalTuples;
    }
	
    /**
     * Not used
     */
	@Override
	public Tuple getNextTuple() {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public void reset() {
		
	}

	
}
