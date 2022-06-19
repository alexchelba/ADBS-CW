package ed.inf.adbs.minibase.base;

public class IntegerConstant extends Constant {
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
    
    /**
     * Overrides equals function used by Java in comparing objects
     */
    @Override
    public boolean equals(Object t2) {
    	if(t2 instanceof IntegerConstant) {
    		int i = ((IntegerConstant) t2).getValue();
    		return (this.value == i);
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
