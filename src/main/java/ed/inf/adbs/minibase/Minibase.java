package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.operators.SelectStatement;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.List;

/**
 * 
 * In-memory database system
 * Instantiates the database and starts the execution of the query plan
 *
 */
public class Minibase {

	/**
     * main method
     * @param args main arguments: database directory, input file and output file
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);
    }
    
    
    /**
     * Executes the query from the input file and stores the result in the output file
     * @param db database directory
     * @param input input file
     * @param output output file
     */
    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
    	try {
	    	Query statement = QueryParser.parse(Paths.get(inputFile)); //get the query statement
	        DatabaseCatalog dbCat = DatabaseCatalog.getInstance(); //get the DatabaseCatalog instance
	        dbCat.initialiseInfo(databaseDir, outputFile); //initialise it
	        if (statement != null) {
	            SelectStatement selState = new SelectStatement(statement); //create a SelectStatement instance with the input query
	            selState.generateAndExecuteQueryPlan(); //generate the operator's tree and execute the query
	        }
    	} catch (Exception e) {
    		System.err.println("Exception occurred during parsing");
            e.printStackTrace();
    	}
    }
}
