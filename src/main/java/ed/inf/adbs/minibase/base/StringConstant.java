package ed.inf.adbs.minibase.base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
    
    /**
     * Overrides equals function used by Java in comparing objects
     */
    @Override
    public boolean equals(Object t2) {
    	if(t2 instanceof StringConstant) {
    		String s = ((StringConstant) t2).getValue();
    		return this.value.equals(s);
    	}
    	return false;
    }
    
    /**
     * Overrides hashCode function used by Java in comparing objects
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
}