/**
 * 
 */
package bclibs.analysis.stack;

public class TOP extends StackElement {
	@Override
	public StackElement copy() {
		return new TOP();
	}
	@Override
	public String toString() {
		return "TOP";
	}
}