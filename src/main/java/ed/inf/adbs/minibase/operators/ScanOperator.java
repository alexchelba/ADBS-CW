package ed.inf.adbs.minibase.operators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import ed.inf.adbs.minibase.DatabaseCatalog;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;


/**
 * 
 * Operator that scans a table for data and saves the data in Tuple elements.
 *
 */
public class ScanOperator extends Operator {
	
	//table name to be scanned
    private String tableName;
    //original table
    private String tableOrig;
    //path of the table's data file
    private String path;
    //buffer reader to scan the data file
    private BufferedReader br;

    /**
     * ScanOperator constructor
     * @param table table name
     */
    public ScanOperator(String table) {
        try {
            this.tableName = table;
            this.tableOrig = DatabaseCatalog.getAlias(table); //get name of the original table
            this.path = DatabaseCatalog.getTablePath(tableOrig); //get path to the table
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
        	
            e.printStackTrace();
        }
    }

    /**
     * Reads the next line of the data file
     * @return tuple with the line's values
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String line = br.readLine(); //read next line
            if (line == null) return null; //if there are no more lines, we have finished the scanning
            line.trim();
            String[] parts = line.split(",\\s+"); //separate the values of the line (csv file => split by comma and spaces)
            Term[] terms = new Term[parts.length];
            List<String> initialTypes = DatabaseCatalog.getInitialTypes(tableOrig);//get data types for columns
            for(int i=0;i<parts.length;i++) {
            	if(initialTypes.get(i).matches("int")) {
        			terms[i] = new IntegerConstant(Integer.valueOf(parts[i]));
        		}
        		else {
        			String res = parts[i].replaceAll("'", "");
        			terms[i] = new StringConstant(res);
        		}
            }
            List<String> tableSchema = DatabaseCatalog.getTableSchema(tableName);
            return new Tuple(terms, tableSchema); //create the new tuple with its schema
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Resets the buffer reader (used for the inner table in join)
     */
    @Override
    public void reset() {
        try {
            br.close();
            br = new BufferedReader(new FileReader(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the table name of the scan operator
     * @return table name
     */
    public String getTable() {
        return tableName;
    }
}
