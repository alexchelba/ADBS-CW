package ed.inf.adbs.minibase.base;

public class ComparisonAtom extends Atom {

    private Term term1;

    private Term term2;

    private ComparisonOperator op;

    public ComparisonAtom(Term term1, Term term2, ComparisonOperator op) {
        this.term1 = term1;
        this.term2 = term2;
        this.op = op;
    }

    public Term getTerm1() {
        return term1;
    }

    public Term getTerm2() {
        return term2;
    }

    public ComparisonOperator getOp() {
        return op;
    }
    
    /**
     * checks if this ComparisonAtom item is equal to ComparisonAtom t2
     * @param t2 ComparisonAtom item
     * @return true if they are equal, else false
     */
    public boolean equals(ComparisonAtom t2) {
    	return this.term1.equals(t2.getTerm1())
    			&& this.term2.equals(t2.getTerm2())
    			&& this.op.toString().matches(t2.getOp().toString());
    }

    @Override
    public String toString() {
        return term1 + " " + op + " " + term2;
    }

}
