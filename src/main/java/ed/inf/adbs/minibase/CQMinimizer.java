package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Variable;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.misc.Pair;

/**
 *
 * Contains the algorithm for minimizing conjunctive queries
 * The algorithm firstly checks for possible minimizations inside each table
 * and then checks each possibility's validity. If a possibility is valid,
 * it minimizes the query and reloads the process.
 * 
 */
public class CQMinimizer {

	//Map of table's name to its query tableau
	private static Map<String, ArrayList<ArrayList<Term>> > queryTableaux;
	//Set of distinguished variables
	private static Set<String> distVariables;
	
	
	/**
	 * Main class which takes input 2 strings, input file's path and output file's path,
	 * and calls the minimization function
	 * @param args list of 2 strings, input file's path and output's file path
	 */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);

    }
    
    /**
     * Looks up possible homomorphisms for each query done on table with name r
     * @param r table's name
     * @return list of pairs of indices, each pair (a,b) meaning there is a possible homomorphism from
     * query with index a to query with index b
     */
    public static List<Pair<Integer, Integer>> LookUpPossibleHomomorphisms(String r) {
    	
    	//get associated query tableau for table r
    	ArrayList<ArrayList<Term>> tableau = queryTableaux.get(r);
		List< Pair<Integer, Integer> > possibleHomomorphisms = new ArrayList<Pair<Integer,Integer>>();
		for(int p=0;p<tableau.size();p++) {
			//consider every query on table r as a possible candidate for minimization
			boolean ok=true;
			ArrayList<Term> t_remove = tableau.get(p);
			for(int i=0;i<tableau.size() && ok;i++) {
				// consider every other query on table r as being possibly contained in query p
				if(p==i) continue;
				ArrayList<Term> t_keep = tableau.get(i);
				// check which query is shorter
				int size = Math.min(t_keep.size(), t_remove.size());
				for(int k=0; k<size&&ok; k++) {
					Term tkk = t_keep.get(k);
					Term trk = t_remove.get(k);
					
					if(!tkk.equals(trk)) {
						//if the variables are different
						if(trk instanceof Constant) // if variable in query p is a constant
							ok = false;
						else
						if(trk instanceof Variable) { // if variable in query p is a variable
							String name = ((Variable) trk).getName();
							if(distVariables.contains(name)) { // if it's a distinguished variable
								ok = false;
							}
						}
					}
				}
				
				if(ok) { // if containment of query i in query p is a possibility
					Integer pi = i;
					Integer pp = p;
					Pair<Integer, Integer> pr = new Pair<Integer, Integer> (pp,pi);
					possibleHomomorphisms.add(pr); // add pair(p,i) to the list
				}
				
			}
		}
		
		return possibleHomomorphisms;
    }
    
    /**
     * Checks whether the mappings implied by a possible homomorphism in table with name r
     * are valid over the whole formula 
     * @param r table's name
     * @param formerIdx index of the query that possibly contains another query
     * @param mappings map representing transformations from key to value
     * @return true if all mappings are valid, else false
     */
    public static boolean checkMapping(String r, int formerIdx, Map<Term, Term> mappings) {
    	for(String other : queryTableaux.keySet()) {
    		//get the query tableau for table other
			ArrayList<ArrayList<Term>> otherTableau = queryTableaux.get(other);
			for(int idx = 0;idx<otherTableau.size();idx++) { // check every query on table other
				if(other.equals(r) && idx==formerIdx) continue; // skip the candidate query for removal
				ArrayList<Term> a = otherTableau.get(idx);
				RelationalAtom a11 = new RelationalAtom(other, a);
				ArrayList<Term> corr = new ArrayList<Term>(a);
				boolean needsChecking = false;
				for(int i=0;i<a.size();i++) { // check every term in query with index idx
					if(mappings.containsKey(a.get(i))) { //if query idx has a variable that may get mapped
						RelationalAtom a1 = new RelationalAtom(other, a);
						corr.set(i, mappings.get(a.get(i)));
						RelationalAtom a2 = new RelationalAtom(other, corr);
    					//construct new query with the variable transformed and mark the query as needed to be verified
						needsChecking=true;
						
					}
				}
				if(needsChecking) { //if query has at least one variable that will get mapped to another variable
					boolean isHomomorphism = false;
					for(int idx2 = 0;idx2<otherTableau.size();idx2++) { //check every query
						if(other.equals(r) && formerIdx==idx2) continue; //skip candidate query for removal
						ArrayList<Term> b = otherTableau.get(idx2);
						if(checkArrayEquality(b,corr)) { //if equivalent query is found, then mapping works
							RelationalAtom a1 = new RelationalAtom(r, b);
	    					RelationalAtom a2 = new RelationalAtom(r, corr);
							isHomomorphism = true;
							break;
						}
					}
					//if mapping doesn't work, return false
					if(!isHomomorphism) return false;
				}
			}
		}
    	return true;
    }
    
    /**
     * Checks whether 2 arraylists of Term items are equal
     * @param a1 first arraylist of Term items
     * @param a2 second arraylist of Term items
     * @return true if they are equal, else false
     */
    public static boolean checkArrayEquality(ArrayList<Term> a1, ArrayList<Term> a2) {
    	if(a1.size()!=a2.size())
    		return false;
    	for(int i=0;i<a1.size();i++)
    		if(!a1.get(i).equals(a2.get(i)))
    			return false;
    	return true;
    }
    
    /**
     * Creates file in the path indicated by filename
     * @param filename path of the file to be created
     */
    public static void createFile(String filename) {
    	try {
    		File myObj = new File(filename);
    		myObj.getParentFile().mkdirs();
    	    if (myObj.createNewFile()) {
    	    	System.out.println("File created: " + myObj.getName());
    	    } else {
    	    	System.out.println("File already exists.");
    	    }
        } catch (IOException e) {
        	System.out.println("An error occurred.");
    	    e.printStackTrace();
    	}
    }
    
    /**
     * writes input string in file filename
     * @param filename name of file
     * @param input text that will be written in file
     */
    public static void writeFile(String filename, String input) {
    	try {
    		FileWriter myWriter = new FileWriter(filename);
    	    myWriter.write(input);
    	    myWriter.close();
    	    System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
        	System.out.println("An error occurred.");
    	    e.printStackTrace();
    	}
    }
    
    /**
     * Builds the string of the minimized conjunctive query for the CQ in the input file
     * @param head of the formula
     * @return string of the minimized CQ
     */
    public static String buildAnswer(RelationalAtom head) {
    	String ans = "";
    	ans += head.toString();
    	ans += " :- ";
    	List<RelationalAtom> raSet = new ArrayList<RelationalAtom>();
    	for(String r : queryTableaux.keySet()) {
    		List<ArrayList<Term>> atomSet = queryTableaux.get(r);
    		for(ArrayList<Term> a : atomSet) {
    			RelationalAtom ra = new RelationalAtom(r, a);
    			raSet.add(ra);
    		}
    	}
    	ans += Utils.join(raSet, ", ");
    	return ans;
    }
    
    /**
     * function that implements the CQ minimization algorithm
     */
    public static void runMinimizationAlgorithm() {
    	boolean change = false;
		do { //do until there is no change to the formula
			change = false;
    		for(String r : queryTableaux.keySet()) {
    			//get list of possible transformations for queries on table r
    			List< Pair<Integer, Integer> > ph = LookUpPossibleHomomorphisms(r);
    			if(ph.size()>0) { //if there are possible transformations
    				ArrayList<ArrayList<Term>> tableau = queryTableaux.get(r);
    				boolean[] wasRemoved = new boolean[tableau.size()];
    				for(Pair<Integer, Integer> p : ph) { //check every possible transformation
    					if(!wasRemoved[p.a]&&!wasRemoved[p.b]) { // if both queries are still in the formula
	    					ArrayList<Term> possibleRemoval = tableau.get(p.a);
	    					ArrayList<Term> keeper = tableau.get(p.b);
	    					Map<Term, Term> mappings = new HashMap<Term, Term>();
	    					int size = Math.min(possibleRemoval.size(), keeper.size());
	    					for(int i=0;i<size;i++) { //construct map of mappings implied by the possible transformation
	    						Term t1 = possibleRemoval.get(i);
	    						Term t2 = keeper.get(i);
	    						if(!t1.equals(t2))
	    							mappings.put(t1, t2);
	    					}
	    					boolean canBeRemoved = checkMapping(r, p.a, mappings);
	    					if(canBeRemoved) { // if transformation can happen, mark query that can be removed
	    						wasRemoved[p.a] = true;
	    						change = true;
	    					}
    					}
    				}
    				if(change) { //if changes were made, construct new minimized query tableau for table r
	    				ArrayList<ArrayList<Term>> newTableau = new ArrayList<ArrayList<Term>>();
	    				for(int i=0;i<tableau.size();i++) {
	    					if(!wasRemoved[i]) {
	    						newTableau.add(tableau.get(i));
	    					}
	    				}
	    				queryTableaux.replace(r, newTableau); //associate new tableau with table r
    				}
    			}
    		}
		}while(change);
    }
    
    /**
     * skeleton function for CQ minimization of the CQ given in the input file.
     * Its minimized version is written in the output file
     * @param inputFile path to the input files
     * @param outputFile path to the output file
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
    	queryTableaux = new HashMap< String, ArrayList<ArrayList<Term>> >();
    	distVariables = new HashSet<String>();
    	try {
    		Query query = QueryParser.parse(Paths.get(inputFile)); //get input formula
    		RelationalAtom head = query.getHead();
    		List<Atom> body = query.getBody();
    		List<Term> distTerms = head.getTerms();
    		for(Term t : distTerms) { // establish the distinguished variables
    			if(t instanceof Variable) {
    				String name = ((Variable) t).getName();
    				distVariables.add(name);
    			}
    		}
    
    		for (Atom a : body) { //for every atom in the body of the formula
    			if(a instanceof RelationalAtom) {
    				String name = ((RelationalAtom) a).getName();
    				//add query to corresponding tableau
    				if(queryTableaux.containsKey(name)) 
    				{
    					ArrayList<ArrayList<Term>> tableau = queryTableaux.get(name);
    					List<Term> terms = ((RelationalAtom) a).getTerms();
    					tableau.add((ArrayList<Term>) terms);
    					queryTableaux.replace(name, tableau);
    				}
    				else
    				{
    					ArrayList<ArrayList<Term>> tableau = new ArrayList<ArrayList<Term>>();
    					List<Term> terms = ((RelationalAtom) a).getTerms();
    					ArrayList<Term> t = new ArrayList<Term>(terms);
    					tableau.add(t);
    					queryTableaux.put(name, tableau);
    				}
    			}
    		}
    		runMinimizationAlgorithm(); //run minimization algorithm
    		String answer = buildAnswer(head); //construct minimized query in string
    		createFile(outputFile); //create output file
    		writeFile(outputFile, answer); //write minimized CQ in the output file
    	}
    	catch (Exception e) {
    		System.err.println("Exception occurred during parsing");
            e.printStackTrace();
    	}
    }

}
