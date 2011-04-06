package bclibs.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bclibs.analysis.opcodes.BranchOpCode;
import bclibs.analysis.opcodes.Op;
import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ExceptionTable;

public class CodeBlocksParser {
	private final CtBehavior behavior;
	private final CodeParser parser;
	private final List<Integer> exceptionHandlers = new ArrayList<Integer>();
	private final Map<Integer, Integer> jumps = new HashMap<Integer, Integer>();
	
	public CodeBlocksParser(CtBehavior behavior) {
		this.behavior = behavior;
		this.parser = new CodeParser(behavior);
	}
	
	public void parse() throws BadBytecode {
		parseHandlers();
		parser.parse(new OpHandler() {
			@Override
			public void handle(Op op, int index) {
				if(op instanceof BranchOpCode)
					jumps.put(index, op.as(BranchOpCode.class).decode(parser.context, index).getJump());
			}
		});
	}
	
	private void parseHandlers() {
		ExceptionTable table = behavior.getMethodInfo().getCodeAttribute().getExceptionTable();
		for(int i = 0; i < table.size(); i++) {
			exceptionHandlers.add(table.handlerPc(i));
		}
	}
	
	public List<Integer> getExceptionHandlers() {
		return exceptionHandlers;
	}
	
	public Map<Integer, Integer> getJumps() {
		return jumps;
	}
}
