package ed.inf.adbs.minibase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * A catalog of the database.
 * Initialises an instance of the database and keeps track of each table's attributes
 * and their characteristics.
 *
 */
public class DatabaseCatalog {

   	//DatabaseCatalog instance
	private static DatabaseCatalog dbCatalog = null;
	//paths
	private static String outputFile = "";
	private static String dataDir = "";
	private static String schema = "";
	//position for each attribute of the schema file
	private static Map<String, Integer > attrPos = new HashMap<>();
	//data types for columns per table
	private static Map<String, List<String>> initialTypes = new HashMap<>();
	//map of table's name to its schema
	private static Map<String, List<String>> tableSchemas = new HashMap<>();
	//map of table aliases
	private static Map<String, String> aliases = new HashMap<>();
	
	/**
	 * DatabaseCatalog constructor
	 */
	private DatabaseCatalog() {
	}
	
	/**
	 * Returns the instance of the DatabaseCatalog, making sure there is only one instance
	 * @return DatabaseCatalog instance
	 */
	public static DatabaseCatalog getInstance() {
	    if (dbCatalog == null) //if there is no instance, create one and return it. Else return that instance
	        dbCatalog = new DatabaseCatalog();
	    return dbCatalog;
	}
	
	/**
	 * Initialises the information of the DatabaseCatalog (paths and schema)
	 * @param db db directory path
	 * @param op output file path
	 */
	public void initialiseInfo(String db, String op) {
	    try {
	        dataDir = db + "/files";
	        schema = db + "/schema.txt";
	        outputFile = op;
	        aliases = new HashMap<>();
	        attrPos = new HashMap<>();
	        tableSchemas = new HashMap<>();
	        BufferedReader br = new BufferedReader(new FileReader(schema));
	        String line;
	        while ((line = br.readLine()) != null) {                //for every table in the schema,
	            String[] splitLine = line.split("\\s+");     //split string by spaces
	            String table = splitLine[0].trim(); // extract table name
	            List<String> initialType = new ArrayList<String>();
	            for (int i = 1; i < splitLine.length; ++i) { //add data type for each column in the arraylist
	            	initialType.add(splitLine[i].trim());
	            }
	            initialTypes.put(table, initialType); //map list of data types to the table
	            
	        }
	        br.close();
	        File myObj = new File(outputFile); //create output file
    		myObj.getParentFile().mkdirs();
	        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
	        bw.write(""); //empty the output file
	        bw.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Sets alias for original table
	 * @param aliasTable new table's name
	 * @param origTable original table's name
	 */
	public static void setAlias(String aliasTable, String origTable) {
		aliases.put(aliasTable, origTable);
	}
	
	/**
	 * Gets the original table's name
	 * @param table alias used for table
	 * @return original table's name
	 */
	public static String getAlias(String table) {
		if(aliases.containsKey(table))
			return aliases.get(table);
		return null;
	}
	
	/**
	 * Returns the path of a table's data file
	 * @param table table name or alias
	 * @return path of the table's data file
	 */
	public static String getTablePath(String table) {
	    return dataDir + "/" + table + ".csv";
	}
	
	/**
	 * Returns the output file path
	 * @return output file path
	 */
	public static String getOutputFile() {
	    return outputFile;
	}
	
	/**
	 * Return the position of the attribute in the table's schema
	 * @param attr attribute of a table (structure: Table.Attr)
	 * @return position of the attribute in the table's schema
	 */
	
	public static int getAttrPos(String attr) {
	    return attrPos.get(attr);
	    
	}
	
	/**
	 * Set the positions of attributes in the table's schema
	 * @param attrPos Map of attribute's name to its position in the table's schema
	 */
	public static void setAttrPos(Map<String, Integer> attrPos) {
        DatabaseCatalog.attrPos = attrPos;
    }
	
	/**
	 * Returns the table schema (attributes)
	 * @param t table name
	 * @return table t's schema
	 */
	public static List<String> getTableSchema(String t) {
	    return tableSchemas.get(t);
	}
	
	/**
	 * Sets a table's schema
	 * @param t table name
	 * @param s schema for table t
	 */
	public static void setTableSchema(String t, List<String> s) {
		tableSchemas.put(t, s);
	}
	
	/**
	 * Returns the initial data types for columns of table
	 * @param t table name
	 * @return initial data types for table t's columns
	 */
	public static List<String> getInitialTypes(String t) {
	    return initialTypes.get(t);
	}
}
