package ed.inf.adbs.minibase.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.inf.adbs.minibase.DatabaseCatalog;
import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.ComparisonOperator;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;

/**
 * 
 * Class that builds and runs the query plan.
 *
 */
public class SelectStatement {

	//variables to project
    private static List<Term> projVars;
    //all distinct variables
    private static List<Term> allVars;
    //tables involved
    private static List<String> schema;
    //only select conditions per table
    private static Map<String, List<ComparisonAtom> > selectionConds;
    //variables per table
    private static Map<String, List<Term>> selectItems;
    //only join conditions per table
    private static Map<String, List<ComparisonAtom> > joinConds;
    //attribute position map
    private static Map<String, Integer> attrPos;

    /**
     * SelectStatement constructor
     * @param s query statement
     */
    public SelectStatement(Query s) {
        RelationalAtom head = s.getHead();
        projVars = head.getTerms();
        List<Atom> body = s.getBody();
        schema = new ArrayList<String>();
        selectionConds = new HashMap<>();
        selectItems = new HashMap<>();
        joinConds = new HashMap<>();
        attrPos = new HashMap<>();
        allVars = new ArrayList<>();
        int aliasIdx=0;
        for(Atom a : body) {
        	if(a instanceof RelationalAtom) { 
        		String from = ((RelationalAtom) a).getName();
        		List<Term> terms = ((RelationalAtom) a).getTerms();
        		String newAlias = from + aliasIdx; //construct alias for table
        		DatabaseCatalog.setAlias(newAlias, from); //set alias for table
        		List<String> newSchema = createTableSchema(newAlias, terms);
        		aliasIdx = aliasIdx+1;
        		DatabaseCatalog.setTableSchema(newAlias, newSchema); // set schema for table
        	}
        }
        DatabaseCatalog.setAttrPos(attrPos); //set mapping of attributes to their positions in their tables
        for(Atom a : body) {
        	if(a instanceof ComparisonAtom) //split set of conditions in selection and join conditions
        		decideConditionType((ComparisonAtom) a);
        }
    }
    
    /**
     * Adds ComparisonAtom to selectionConds if condition implicates only one table, else to joinConds
     * @param a ComparisonAtom item
     */
    private void decideConditionType(ComparisonAtom a) {
    	Term t1 = ((ComparisonAtom) a).getTerm1();
		Term t2 = ((ComparisonAtom) a).getTerm2();
    	for(String table : selectItems.keySet()) {
			List<Term> tableItems = selectItems.get(table);
			boolean b1 = (tableItems.contains(t1) & !tableItems.contains(t2));
			boolean b2 = (!tableItems.contains(t1) & tableItems.contains(t2));
			boolean b3 = (tableItems.contains(t1) & tableItems.contains(t2));
			boolean b4 = (!tableItems.contains(t1) & !tableItems.contains(t2));
			if(b3) //add condition to selection list if both terms in it are from table
				addCond(selectionConds, table, (ComparisonAtom) a);
			else
			if(b1 || b2) {
				if((t1 instanceof Constant) ^ (t2 instanceof Constant)) {
					//add condition to selection list if only one variable is from table and the other is constant
					addCond(selectionConds, table, (ComparisonAtom) a);
				}
				else {
					//add condition to join list if one variable is from table and the other is from another
					addCond(joinConds, table, (ComparisonAtom) a);
				}
			}
			else
			if(b4) { //if no variables are from table
				if((t1 instanceof Constant) & (t2 instanceof Constant)) {
					//if both variables are constants, add condition to selection condition
					addCond(selectionConds, table, (ComparisonAtom) a);
				}
			}
		}
    }
    
    /**
     * Adds ComparisonAtom to correspondent map
     * @param conds map of each table name to their associated list of ComparisonAtom iterm
     * @param table table's name
     * @param a ComparisonAtom a
     */
    private static void addCond(Map<String, List<ComparisonAtom> > conds, String table, ComparisonAtom a) {
    	if(conds.containsKey(table)) {
			List<ComparisonAtom> exps = conds.get(table);
			exps.add((ComparisonAtom) a);
			conds.replace(table, exps);
		}
		else {
			List<ComparisonAtom> exps = new ArrayList<ComparisonAtom>();
			exps.add((ComparisonAtom) a);
			conds.put(table, exps);
		}
    }
    
    /**
     * Creates schema for table described by name and list of Term objects
     * @param from table's name
     * @param terms list of Term objects
     * @return list of new column names (schema) for the table
     */
    private static List<String> createTableSchema(String from, List<Term> terms) {
    	List<String> newSchema = new ArrayList<String>();
		selectItems.put(from, terms); //associate name of the table with the list of items
		schema.add(from); //add table to the list of tables involved in the query
		for (int i=0;i<terms.size();i++) {
			Term t = terms.get(i);
			if(t instanceof Variable) {//if Term is a variable, add its name to the table's schema
				String varName = ((Variable) t).getName();
				String name = from + "." + varName;
				newSchema.add(name);
				attrPos.put(name, i);
				if(!allVars.contains(t))
					allVars.add(t);
			}
			else { //if term is a Constant, construct variable for it and add it to the table's schema
				String name = "";
				Variable t1;
				if(t instanceof IntegerConstant) {
    				name = from + ".ci" + ((IntegerConstant) t).getValue();
    				attrPos.put(name, i);
    				t1 = new Variable("ci" + ((IntegerConstant) t).getValue());
				}
				else {
					name = from + ".cs" + ((StringConstant) t).getValue();
					attrPos.put(name, i);
					t1 = new Variable("cs" + ((StringConstant) t).getValue());
				}
				newSchema.add(name);
				//create condition that values on this column are equal to Constant
				ComparisonAtom c = new ComparisonAtom(t1, t, ComparisonOperator.fromString("="));
				addCond(selectionConds, from, c);
			}
		}
		return newSchema;
    }
    

    /**
     * Constructs join conditions between two tables
     * @param t1 list of join conditions for table 1
     * @param t2 list of join conditions for table 2
     * @return list of join conditions between the two tables
     */
    private List<ComparisonAtom> getTablesJoinConds(List<ComparisonAtom> t1, List<ComparisonAtom> t2) {
        List<ComparisonAtom> joinConds = new ArrayList<ComparisonAtom>();
        if(t1.size()==0) return joinConds; 
        for(ComparisonAtom cond1 : t1) {
        	boolean ok = false;
        	if(t2.size()==0) return joinConds;
        	for(ComparisonAtom cond2 : t2) {
        		if(ok) continue;
        		if(cond1.equals(cond2)) ok = true;
        	}
        	if(ok) joinConds.add(cond1);
        }
        return joinConds;
    }
    
    /**
     * Constructs list of join conditions for table 2 that imply other tables than table 1
     * @param t1 list of join conditions for table 1
     * @param t2 list of join conditions for table 2
     * @return list of join conditions for table 2 that imply other tables than table 1
     */
    private List<ComparisonAtom> getRestOfJoinConds(List<ComparisonAtom> t1, List<ComparisonAtom> t2) {
    	List<ComparisonAtom> joinConds = new ArrayList<ComparisonAtom>();
    	if(t2.size()==0) return joinConds;
    	for(ComparisonAtom cond2 : t2) {
    		boolean ok = false;
    		if(t1.size()==0) return t2;
    		for(ComparisonAtom cond1 : t1) {
    			if(ok) continue;
    			if(cond2.equals(cond1)) ok = true;
    		}
    		if(!ok) joinConds.add(cond2);
    	}
    	return joinConds;
    }

    /**
     * Generates the operator tree and executes the query
     */
    public void generateAndExecuteQueryPlan() {
        Operator root = new ScanOperator(schema.get(0)); //create a scan operator for the first table
        List<ComparisonAtom> whereSelect = selectionConds.getOrDefault(schema.get(0), new ArrayList<ComparisonAtom>());
        if (whereSelect.size()>0) { //if that table has select conditions
        	root = new SelectOperator((ScanOperator) root, whereSelect);
        }
        //get join conditions for the first table
        List<ComparisonAtom> allJoinConds = joinConds.getOrDefault(schema.get(0), new ArrayList<>());
        //get table schema for the first table
        List<String> table1Attr = new ArrayList<>(DatabaseCatalog.getTableSchema(schema.get(0)));
        for (int i = 1; i < schema.size(); ++i) { //for the following tables (if any)
            String t = schema.get(i); //get the table name
            Operator root2 = new ScanOperator(t); //create a scan operator for this table
            List<ComparisonAtom> whereSelect2 = selectionConds.getOrDefault(schema.get(i), new ArrayList<ComparisonAtom>());
            if (whereSelect2.size() > 0)  //if this table has select conditions
                root2 = new SelectOperator((ScanOperator) root2, whereSelect2); //create a select operator for it
            
            //get the join conditions for this table
            List<ComparisonAtom> tableJoinConds = joinConds.getOrDefault(schema.get(i), new ArrayList<>());
            //get join conditions implying only the first table and this table (may be empty: cartesian product)
            List<ComparisonAtom> necessaryJoinConds = getTablesJoinConds(allJoinConds, tableJoinConds);
            //get this table's schema
            List<String> table2Attr = new ArrayList<>(DatabaseCatalog.getTableSchema(t));
            Map<String, Integer> attrPos1 = new HashMap<>();
        	List<String> commonVars = new ArrayList<>();
        	//check for common variables between the 2 tables
        	for(int k=0;k<table2Attr.size();k++) {
        		String[] splitLine = table2Attr.get(k).split("\\.");
        		attrPos1.put(splitLine[1], k);
        	}
        	for(int k=0;k<table1Attr.size();k++) {
        		String[] splitLine = table1Attr.get(k).split("\\.");
        		if(attrPos1.containsKey(splitLine[1])) {
        			commonVars.add(splitLine[1]);
        			table2Attr.remove(splitLine[1]);
        		}
        	}
        	
        	//remove the common join conditions with this table from first table's list
            allJoinConds = getRestOfJoinConds(necessaryJoinConds, allJoinConds);
            //add the remaining join conditions of this table to the first table's list
            allJoinConds.addAll(getRestOfJoinConds(necessaryJoinConds, tableJoinConds));
            //do the join between first table and this table
            root = new JoinOperator(root, root2, necessaryJoinConds, commonVars); //create a join operator for the first/last operator and the new one
            //update the schema of the first table
            table1Attr.addAll(table2Attr);
        }
        
        //check whether projection is needed
        boolean areEqual = true;
        if(projVars.size()!=allVars.size()) areEqual = false;
        else {
        	for(int i=0;i<projVars.size() && areEqual;i++)
        		if(!projVars.get(i).equals(allVars.get(i)))
        			areEqual = false;
        }
        if(!areEqual) {//if we need to project variables
        	//create projection operator
        	root = new ProjectOperator(projVars, root, schema.get(0));
        	List<Tuple> t = root.getQueryResult();
        	//take only the distinct elements out of the result
        	root = new DistinctOperator(t);
        	//output the list of unique elements
        	root.dump(((DistinctOperator) root).getUniqueTuples());
        }
        else {
        	//output all elements
        	root.dump();
        }
    }

    /**
     * Returns a select expression where table is involved
     * @param table table name
     * @return select expression where table is involved
     */
    public static List<ComparisonAtom> getSelectionCondsTable(String table) {
        return selectionConds.get(table);
    }
}
