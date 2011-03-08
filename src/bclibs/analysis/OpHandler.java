/**
 * 
 */
package bclibs.analysis;

import bclibs.analysis.opcodes.Op;

public interface OpHandler {
	void handle(Op op, int index);
}