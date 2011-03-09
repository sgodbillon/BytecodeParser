package bclibs.analysis.stack;

public class VoidElement extends StackElement {
	@Override
	public StackElement copy() {
		return new VoidElement();
	}
	@Override
	public String toString() {
		return "Void";
	}
}
