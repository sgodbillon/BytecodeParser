/**
 * 
 */
package bytecodeparser.analysis.stack;

public class Whatever extends StackElement {
	@Override
	public StackElement copy() {
		return new Whatever();
	}
	@Override
	public String toString() {
		return "Whatever";
	}
}