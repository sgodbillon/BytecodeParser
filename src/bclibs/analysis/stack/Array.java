/**
 * 
 */
package bclibs.analysis.stack;


public class Array extends StackElement {
	//public int dimensions;
	public final String signature;
	public Array(String signature) {
		this.signature = signature;
	}
	@Override
	public Array copy() {
		return new Array(signature);
	}
	@Override
	public String toString() {
		return "simple array of '" + signature + "'";
	}
}