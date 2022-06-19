package ed.inf.adbs.minibase.operators;

import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;


/**
 * 
 * Operator that returns the projection of an operator's elements.
 *
 */
public class ProjectOperator extends Operator {

	//child operator
    private Operator op;
    //table name on which projection is done
    private String tableName;
    //attributes wanted in the select clause
    private List<Term> attrs;

    /**
     * ProjectOperator constructor
     * @param attrProj attributes wanted in the select clause
     * @param o child operator
     * @param table name of the table
     */
    public ProjectOperator(List<Term> attrProj, Operator o, String table) {
        attrs = attrProj;
        op = o;
        tableName = table;
    }

    /**
     * Returns the next tuple with only the attributes wanted in the select clause
     * @return tuple with the desired attributes
     */
    @Override
    public Tuple getNextTuple() {
        Tuple t = op.getNextTuple();
        List<Integer> attrsTuple = new ArrayList<>();
        List<String> tupleSchema = new ArrayList<>();
        for (Term attr:attrs) {
        	String name = tableName + "." + ((Variable)attr).getName();
            attrsTuple.add(t.getAttrPos(name)); //store the position of the attributes wanted in the tuple
            tupleSchema.add(name); //store the new tuple's schema
        }
        Term[] newt;
        newt = new Term[attrs.size()];
        for (int i = 0; i < attrsTuple.size(); ++i) //for every attribute in the position array
        	newt[i] = t.getValuePos(attrsTuple.get(i)); //add the value of the tuple in that position

        return new Tuple(newt, tupleSchema);
    }

    /**
     * Calls the child operator's reset method
     */
    @Override
    public void reset() {
        op.reset();
    }
}
