package ed.inf.adbs.minibase.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.minibase.DatabaseCatalog;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

public class Tuple implements Comparable<Tuple> {

	//tuple values
    private Term[] tuple;
    //tuple schema
    private List<String> attrSchema;

    /**
     * Tuple constructor
     * @param t values
     * @param s schema
     */
    public Tuple(Term[] t, List<String> s) {
    	this.tuple = t;
    	this.attrSchema = s;
    }

    /**
     * Tuple constructor that joins two tuples to create a new one
     * @param leftTuple tuple
     * @param rightTuple tuple
     */
    public Tuple(Tuple leftTuple, Tuple rightTuple) {
        tuple = new Term[leftTuple.getTupleSize() + rightTuple.getTupleSize()];
        for (int i = 0; i < tuple.length; ++i) { //fill new tuple with attributes from both tuples
            if (i < leftTuple.getTupleSize())
                tuple[i] = leftTuple.getValuePos(i);
            else tuple[i] = rightTuple.getValuePos(i - leftTuple.getTupleSize());
        }
        List<String> initialSchema = rightTuple.getAttrSchema();
        List<String> finalSchema = new ArrayList<>();
        String table = leftTuple.getAttrSchema().get(0).split("\\.")[0];
        for(String col : initialSchema) {//Modify schema of the second tuple
        	String [] splitCol = col.split("\\.");
        	String ans = table + "." + splitCol[1];
        	finalSchema.add(ans);
        }
        attrSchema = new ArrayList<>(); //create the schema of the new tuple
        attrSchema.addAll(leftTuple.getAttrSchema());
        attrSchema.addAll(finalSchema);
    }

    /**
     * Returns the value of the tuple in a position
     * @param i index of the tuple
     * @return value of the tuple in i
     */
    public Term getValuePos(int i) {
        return tuple[i];
    }

    /**
     * Returns the tuple values
     * @return tuple values
     */
    public Term[] getTuple() {
        return tuple;
    }

    /**
     * Returns the tuple size
     * @return tuple length
     */
    public int getTupleSize() {
        return tuple.length;
    }

    /**
     * Returns the tuple values in a string
     * @return tuple values in a string
     */
    @Override
    public String toString() {
        String t = "";
        for (int i = 0; i < tuple.length; ++i) {
        	if(tuple[i] instanceof IntegerConstant) 
        		t += ((IntegerConstant) tuple[i]).toString().replaceAll("'", "");
        	else
        		t += ((StringConstant) tuple[i]).toString();
        	if (i != tuple.length - 1) t += ",";
        }
        return t;
    }

    /**
     * Appends the tuple to the end of the output file
     */
    public void dump() {
        try {
            String outputPath = DatabaseCatalog.getOutputFile();
            String t = toString() + "\n";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
            bw.write(t);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the tuple's schema
     * @return tuple's schema
     */
    public List<String> getAttrSchema() {
        return attrSchema;
    }

    /**
     * Returns the position of an attribute in the tuple
     * @param attr attribute of the tuple
     * @return position of an attribute in the tuple
     */
    public int getAttrPos(String attr) {
        if (attr.equals("*")) return -1;
        return attrSchema.indexOf(attr);
    }
    
    /**
     * Used for building a TreeSet of Tuple elements in DistinctOperator
     * Overrides the function used by TreeSet to compare 2 objects
     * Main interest is when 2 tuples are equal, no attention is given to when they are not equal
     */
    @Override
    public int compareTo(Tuple t2) {
		Term[] t2_elems = t2.getTuple();
		if(tuple.length == t2_elems.length) {
			boolean areEqual = true;
			for(int i=0;i<tuple.length && areEqual;i++) {
				if(!tuple[i].equals(t2_elems[i]))
					areEqual = false;
			}
			if(areEqual) return 0;
			return -1;
		}
		return -1;
	}

}
