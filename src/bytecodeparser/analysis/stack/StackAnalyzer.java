/*
 *  Copyright (C) 2011 Stephane Godbillon
 *  
 *  This file is part of BytecodeParser. See the README file in the root
 *  directory of this project.
 *
 *  BytecodeParser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  BytecodeParser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BytecodeParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package bytecodeparser.analysis.stack;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import bytecodeparser.CodeParser;
import bytecodeparser.Context;
import bytecodeparser.OpHandler;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.decoders.DecodedBranchOp;
import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp;
import bytecodeparser.analysis.decoders.DecodedOp;
import bytecodeparser.analysis.decoders.DecodedSwitchOpcode;
import bytecodeparser.analysis.opcodes.BranchOpCode;
import bytecodeparser.analysis.opcodes.ExitOpcode;
import bytecodeparser.analysis.opcodes.Op;
import bytecodeparser.analysis.opcodes.SwitchOpcode;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

public class StackAnalyzer {
	private static final Logger LOGGER = Logger.getLogger(StackAnalyzer.class);
	
	public final Context context;
	final Stack stack;
	final Frame[] frames;
	
	public StackAnalyzer(CtBehavior behavior) {
		this.context = new Context(behavior);
		this.stack = new Stack();
		this.frames = new Frame[context.behavior.getMethodInfo().getCodeAttribute().getCodeLength()];
	}
	
	public Frames analyze() throws BadBytecode {
		if(frames[0] == null) {
			long start = System.currentTimeMillis();
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Start analyzis of " + context.behavior.getLongName());
			init();
			analyze(0, new Stack());
			parseCatchBlocks();
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Analyzis ended, took " + (System.currentTimeMillis() - start) + " ms");
		}
		return new Frames(context.behavior, frames);
	}
	
	void init() throws BadBytecode {
		new CodeParser(context.behavior).parse(new OpHandler() {
			@Override
			public void handle(Op op, int index) {
				Frame frame = frames[index] = new Frame();
				frame.index = index;
			}
		});
	}
	
	void parseCatchBlocks() throws BadBytecode {
		for(int index : context.exceptionHandlers) {
			analyze(index, new Stack().push(new Whatever()));
		}
	}
	
	void analyze(int from, Stack stack) throws BadBytecode {
		StringBuffer trace = new StringBuffer();
		try {
			if(frames[from].isAccessible) // already parsed
				return;
			CodeIterator iterator = context.behavior.getMethodInfo().getCodeAttribute().iterator();
			iterator.move(from);
			Stack currentStack = stack.copy();
			while(iterator.hasNext()) {
				int index = iterator.next();
				Op op = Opcodes.OPCODES.get(iterator.byteAt(index)).init(context, index);
				trace.append("\n").append(index).append(":").append(op.getName()).append(" --> ");
				Frame frame = frames[index];
				frame.isAccessible = true;
				frame.stackBefore = currentStack.copy();
				frame.decodedOp = op.decode(context, index);
				if(frame.decodedOp instanceof DecodedBranchOp)
					trace.append(" [jump to ").append(((DecodedBranchOp)frame.decodedOp).getJump()).append("] ");
				if(frame.decodedOp instanceof DecodedMethodInvocationOp)
					trace.append(" [params = ").append(StackElementLength.add(((DecodedMethodInvocationOp)frame.decodedOp).getPops())).append(" -> ").append(Arrays.toString(((DecodedMethodInvocationOp)frame.decodedOp).getParameterTypes())).append("] ");
				frame.decodedOp.simulate(currentStack);
				frame.stackAfter = currentStack.copy();
				trace.append(frame.stackAfter);
				
				if( !(op instanceof ExitOpcode || (op instanceof BranchOpCode && !((BranchOpCode)op).isConditional()) || op instanceof SwitchOpcode) )
					trace.append(". Next is ").append(iterator.lookAhead());
				
				if(LOGGER.isTraceEnabled())
					LOGGER.trace(trace);
				
				if(op instanceof ExitOpcode)
					return;
				
				if(op instanceof BranchOpCode) {
					BranchOpCode branchOpCode = op.as(BranchOpCode.class);
					int jump = branchOpCode.decode(context, index).getJump();
					analyze(jump, frame.stackAfter);
					if(!branchOpCode.isConditional())
						return;
				}
				
				if(op instanceof SwitchOpcode) {
					SwitchOpcode switchOpcode = op.as(SwitchOpcode.class);
					DecodedSwitchOpcode decodedSwitchOpcode = switchOpcode.decode(context, index);
					for(int offset : decodedSwitchOpcode.offsets)
						analyze(offset, frame.stackAfter);
					analyze(decodedSwitchOpcode.defaultOffset, frame.stackAfter);
					return;
				}
			}
		} catch (Exception e) {
			LOGGER.error("BCLIBS ERROR !! " + trace.toString(), e);
			throw new RuntimeException(e);
		}
	}
	
	public static class Frame {
		public Stack stackBefore;
		public Stack stackAfter;
		public int index;
		public DecodedOp decodedOp;
		public boolean isAccessible = false;
		
		@Override
		public String toString() {
			return "Frame " + index + " (" + decodedOp.op.getName() + "):" + stackBefore + " -> " + stackAfter + " " + (isAccessible ? "" : " NOT ACCESSIBLE");
		}
	}
	
	public static class Frames implements Iterable<Frame> {
		public final Frame[] frames;
		public final CtBehavior behavior;

		public Frames(CtBehavior behavior, Frame[] frames) {
			this.frames = frames;
			this.behavior = behavior;
		}

		@Override
		public FrameIterator iterator() {
			return new FrameIterator();
		}

		public class FrameIterator implements Iterator<Frame> {
			private int i = -1;
			private FrameCodeIterator iterator = new FrameCodeIterator(behavior
					.getMethodInfo().getCodeAttribute(), frames);

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Frame next() {
				int nextIndex = nextIndex();
				if (nextIndex > -1) {
					i = nextIndex;
					return frames[nextIndex];
				}
				throw new IllegalStateException();
			}

			@Override
			public boolean hasNext() {
				return nextIndex() > -1;
			}

			public boolean isFirst() {
				return i == 0;
			}

			public boolean isLast() {
				return !hasNext();
			}

			public Frame lookAhead() {
				if (nextIndex() != -1)
					return frames[nextIndex()];
				return null;
			}

			private int nextIndex() {
				for (int j = i + 1; j < frames.length; j++)
					if (frames[j] != null)
						return j;
				return -1;
			}

			public void insert(byte[] code, boolean after) throws BadBytecode {
				int index = 0;
				if (!after && i != -1)
					index = frames[i].index;
				if (after && lookAhead() != null)
					index = lookAhead().index;
				iterator.move(index);
				iterator.insert(code);
			}
		}

		static class FrameCodeIterator extends CodeIterator {
			final Frame[] frames;

			public FrameCodeIterator(CodeAttribute codeAttribute, Frame[] frames) {
				super(codeAttribute);
				this.frames = frames;
			}

			@Override
			protected void updateCursors(int pos, int length) {
				super.updateCursors(pos, length);
				for (Frame frame : frames) {
					if (frame != null && frame.index > pos)
						frame.index += length;
				}
			}
		}
	}
}