package ed.inf.adbs.minibase.base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Overrides equals function used by Java in comparing objects
     */
    @Override
    public boolean equals(Object t2) {
    	
    	if(t2 instanceof Variable) {
    		String n = ((Variable) t2).getName();
    		return this.name.equals(n);
    	}
    	return false;
    }
    
    /**
     * Overrides equals function used by Java in comparing objects
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
}
