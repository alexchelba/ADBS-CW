package ed.inf.adbs.minibase.operators;

import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.minibase.base.Tuple;

/**
 * 
 * Abstract operator class, defining the methods used by all operators.
 *
 */
public abstract class Operator {

    public abstract Tuple getNextTuple();

	public abstract void reset();

    /**
     * Gets the resulting tuples and writes them in the output file
     */
	public void dump() {
        try {
            Tuple t = getNextTuple();
            while (t != null) {
                t.dump();
                t = getNextTuple();
            }
        } catch (NullPointerException e) {
        	
        }
    }

    /**
     * Dump method used in queries with projection
     * @param result list of the resulting tuples
     */
    public void dump(List<Tuple> result) {
        for (Tuple t:result)
            t.dump();
    }
    
    /**
     * Gets all the resulting tuples and stores them in a list
     * @return list of resulting tuples
     */
    public List<Tuple> getQueryResult() {
	    List<Tuple> result =  new ArrayList<>();
        try {
            Tuple t = getNextTuple();
            while (t != null) {
                result.add(t);
                t = getNextTuple();
            }
        } catch (NullPointerException e) {
        	
        }
        return result;
    }

}
