package bytecodeparser;

import java.util.ArrayList;

import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;

public class MultiMarkerCodeIterator extends CodeIterator {
	public ArrayList<Integer> marks = new ArrayList<Integer>();

	protected MultiMarkerCodeIterator(CodeAttribute ca) {
		super(ca);
	}

	@Override
	protected void updateCursors(int pos, int length) {
		super.updateCursors(pos, length);
		for(int i = 0; i < marks.size(); i++) {
			int mark = marks.get(i);
			if(mark > pos)
				marks.set(i, mark + length);
		}
	}
	
	public int putMark(int index) {
		marks.add(index);
		return marks.size() - 1;
	}
	
	public int getMark(int mark) {
		return marks.get(mark);
	}
}
