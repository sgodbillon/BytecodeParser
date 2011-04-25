/**
 * 
 */
package bytecodeparser;

import bytecodeparser.analysis.opcodes.Op;

public interface OpHandler {
	void handle(Op op, int index);
}