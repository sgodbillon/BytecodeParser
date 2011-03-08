/**
 * 
 */
package bclibs.analysis.stack;

public abstract class StackElement { 
	public abstract StackElement copy();
	@Override public String toString() {
		return this.getClass().toString();
	}
}