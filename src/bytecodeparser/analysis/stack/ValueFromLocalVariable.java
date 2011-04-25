/**
 * 
 */
package bytecodeparser.analysis.stack;

import bytecodeparser.analysis.LocalVariable;

public class ValueFromLocalVariable extends StackElement {
	public final LocalVariable localVariable;
	public ValueFromLocalVariable(LocalVariable localVariable) {
		this.localVariable = localVariable;
	}
	@Override
	public StackElement copy() {
		return new ValueFromLocalVariable(localVariable);
	}
	
	@Override
	public String toString() {
		return "ValueFromLocalVariable '" + (localVariable != null ? localVariable.name : "NONAME") + "'";
	}
}