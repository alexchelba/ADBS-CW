package ed.inf.adbs.minibase.operators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.ComparisonOperator;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;


/**
 * 
 * Evaluates join conditions on 2 tuples.
 *
 */
public class JoinEvaluation {

	//tuples
    private Tuple tuple1;
    private Tuple tuple2;
    //list of conditions to be checked
    private List<ComparisonAtom> e;
    //common variables between the two tuples (equi-joins)
    private List<String> commonVars;
    
    //map of variable name to its position for first tuple
    Map<String, Integer> attrPos1;
    //map of variable name to its position for second tuple
	Map<String, Integer> attrPos2;

    /**
     * JoinEvaluation constructor
     * @param leftTuple tuple 1
     * @param rightTuple tuple 2
     * @param e list of conditions to be met
     * @param cv list of common variables names
     */
    public JoinEvaluation(Tuple leftTuple, Tuple rightTuple, List<ComparisonAtom> e, List<String> cv) {
        tuple1 = leftTuple;
        tuple2 = rightTuple;
        this.e = e;
        this.commonVars = cv;
        attrPos1 = new HashMap<>();
    	attrPos2 = new HashMap<>();
    	List<String> lt = tuple1.getAttrSchema(); //get tuple 1 schema
    	List<String> rt = tuple2.getAttrSchema(); //get tuple 2 schema
    	for(int i=0;i<lt.size();i++) { // construct mapping for tuple 1
    		String[] splitLine = lt.get(i).split("\\.");
    		attrPos1.put(splitLine[1], i);
    	}
    	for(int i=0;i<rt.size();i++) { // construct mapping for tuple 2
    		String[] splitLine = rt.get(i).split("\\.");
    		attrPos2.put(splitLine[1], i);
    	}
    }
    
    /**
	 * Compares two integers according to the sign indicated
	 * @param cmp ComparisonOperator sign
	 * @param x first term of the comparison
	 * @param y second term of the comparison
	 * @return true if the expression is fulfilled else false
	 */
	private boolean evalCmp(ComparisonOperator cmp, int x, int y) {
		if(cmp.toString().matches("=")) return x==y;
		if(cmp.toString().matches("!=")) return x!=y;
		if(cmp.toString().matches(">")) return x>y;
		if(cmp.toString().matches(">=")) return x>=y;
		if(cmp.toString().matches("<")) return x<y;
		if(cmp.toString().matches("<=")) return x<=y;
		return false;
	}
	
	/**
	 * Compares two strings according to the sign indicated
	 * @param cmp ComparisonOperator sign
	 * @param x first term of the comparison
	 * @param y second term of the comparison
	 * @return true if the expression is fulfilled else false
	 */
	private boolean evalCmp(ComparisonOperator cmp, String x, String y) {
		if(cmp.toString().matches("=")) return x.equals(y);
		if(cmp.toString().matches("!=")) return !x.equals(y);
		if(cmp.toString().matches(">")) return x.compareTo(y)>0;
		if(cmp.toString().matches(">=")) return x.compareTo(y)>=0;
		if(cmp.toString().matches("<")) return x.compareTo(y)<0;
		if(cmp.toString().matches("<=")) return x.compareTo(y)<=0;
		return false;
	}
    
	/**
	 * Checks if condition is met
	 * @param ca ComparisonAtom item, expressing condition
	 * @return true if condition is met, else false
	 */
    public boolean evaluateAtom(ComparisonAtom ca) {
    	String n1 = ((Variable) ca.getTerm1()).getName(); //get name of first Term item
    	String n2 = ((Variable) ca.getTerm2()).getName(); //get name of second Term item
    	ComparisonOperator cmp = ca.getOp(); //get the sign
    	Term t1 = tuple1.getValuePos(attrPos1.get(n1)); //get position of first Term in tuple 1
    	Term t2 = tuple2.getValuePos(attrPos2.get(n2)); //get position of second Term in tuple 2
    	
    	if(t1 instanceof IntegerConstant) {
			if(t2 instanceof IntegerConstant) { //if both values are integers, return expression evaluation
				return evalCmp(cmp, ((IntegerConstant) t1).getValue(), ((IntegerConstant) t2).getValue());
			}
    	}
		else
		if(t1 instanceof StringConstant) {
			if(t2 instanceof StringConstant) { //if both values are strings, return expression evaluation
				return evalCmp(cmp, ((StringConstant) t1).getValue(), ((StringConstant) t2).getValue());
			}
		}
		return false;
    	
    }
    
    /**
     * checks equi-join conditions on variable var
     * @param var variable name
     * @return true if condition is met, else false
     */
    public boolean evaluateAtom(String var) {
    	Term t1 = tuple1.getValuePos(attrPos1.get(var));
    	Term t2 = tuple2.getValuePos(attrPos2.get(var));
    	if(t1 instanceof IntegerConstant) {
			if(t2 instanceof IntegerConstant) {
				return ((IntegerConstant) t1).getValue() == ((IntegerConstant) t2).getValue();
			}
    	}
		else
		if(t1 instanceof StringConstant) {
			if(t2 instanceof StringConstant) {
				return ((StringConstant) t1).getValue().equals(((StringConstant) t2).getValue());
			}
		}
		return false;
    }
    
    /**
     * Checks whether the tuples match all restrictions
     * @return returns true if all restrictions are met, else false
     */
    public boolean evaluate() {
    	boolean b = true;
    	if(e.size()>0) 
	    	for(ComparisonAtom a : e) {
	    		b = b & evaluateAtom(a);
	    	}
    	if(commonVars.size()>0) {
    		for(String var : commonVars) {
    			b = b & evaluateAtom(var);
    		}
    	}
    	return b;
    }
}
