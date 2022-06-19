package ed.inf.adbs.minibase.operators;

import java.util.List;

import ed.inf.adbs.minibase.DatabaseCatalog;
import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.ComparisonOperator;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;

/**
 * 
 * Operator that extracts data from given table which fulfills all selection criteria.
 *
 */

public class SelectEvaluation {

	//table name from where the elements are
	String table;
	//tuple to be evaluated
	Tuple t;
	//list of restrictions that the tuple has to fulfill
	List<ComparisonAtom> comp;
	
	/**
	 * SelectEvaluation constructor
	 * @param table table name
	 * @param t tuple that needs evaluated
	 * @param comp list of restrictions
	 */
	public SelectEvaluation(String table, Tuple t, List<ComparisonAtom> comp) {
		this.table = table;
		this.t = t;
		this.comp = comp;
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
	 * Compares two Constant objects according to the sign indicated
	 * @param cmp ComparisonOperator sign
	 * @param t1 first term of the comparison
	 * @param t2 second term of the comparison
	 * @return true if the expression is fulfilled else false
	 */
	private boolean compareConstants(ComparisonOperator cmp, Constant t1, Constant t2) {
		if(t1 instanceof IntegerConstant) {
			int val = ((IntegerConstant) t1).getValue();
			if(t2 instanceof IntegerConstant) {
				int val2 = ((IntegerConstant) t2).getValue();
				return evalCmp(cmp, val, val2);
			}
			else if(t2 instanceof StringConstant) {
				return true;
			}
		}
		else
		if(t1 instanceof StringConstant) {
			String val = ((StringConstant) t1).getValue();
			if(t2 instanceof IntegerConstant) {
				return true;
			}
			else if(t2 instanceof StringConstant) {
				String val2 = ((StringConstant) t2).getValue();
				return evalCmp(cmp, val, val2);
			}
		}
		return false;
	}
	
	/**
	 * Evaluates expression indicated by ComparisonAtom element
	 * @param c ComparisonAtom element
	 * @return true if expression is fulfilled else false
	 */
	private boolean evalAtom(ComparisonAtom c) {
		Term t1 = c.getTerm1();
		Term t2 = c.getTerm2();
		ComparisonOperator cmp = c.getOp();
		if(t1 instanceof Constant) {
			if(t2 instanceof Constant) {
				return compareConstants(cmp, (Constant) t1, (Constant) t2);
			}
			else
			if(t2 instanceof Variable) {
				String name = ((Variable) t2).getName();
				String attr = table + "." + name;
				int idx = DatabaseCatalog.getAttrPos(attr);
				Term item = t.getValuePos(idx);
				return compareConstants(cmp, (Constant) t1, (Constant) item);
			}
		}
		else
		if(t1 instanceof Variable) {
			String name = ((Variable) t1).getName();
			String attr = table + "." + name;
			int idx = DatabaseCatalog.getAttrPos(attr);
			Term item = t.getValuePos(idx);
			if(t2 instanceof Constant) {
				return compareConstants(cmp, (Constant) item, (Constant) t2);
			}
			else
			if(t2 instanceof Variable) {
				String name2 = ((Variable) t2).getName();
				String attr2 = table + "." + name2;
				int idx2 = DatabaseCatalog.getAttrPos(attr2);
				Term item2 = t.getValuePos(idx2);
				return compareConstants(cmp, (Constant) item, (Constant) item2);
			}
		}
		return false;
	}
	
	/**
	 * Evaluates list of ComparisonAtom elements
	 * @return true if all ComparisonAtom elements are true else false
	 */
	public boolean evaluate() {
		boolean b = true;
		for(ComparisonAtom c : comp) {
			b = b & evalAtom(c);
		}
		return b;
	}
	
}
