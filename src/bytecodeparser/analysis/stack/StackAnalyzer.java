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

/**
 * A class to analyze a behavior.
 * Any statement can be inserted after or before the frames produced by the analysis using the FrameIterator.
 * @author Stephane Godbillon
 *
 */
public class StackAnalyzer {
	private static final Logger LOGGER = Logger.getLogger(StackAnalyzer.class);
	
	/**
	 * Context of this analysis.
	 */
	public final Context context;
	final Stack stack;
	final Frame[] frames;
	
	/**
	 * Constructs an analyzer for the given behavior.
	 * @param behavior
	 */
	public StackAnalyzer(CtBehavior behavior) {
		this.context = new Context(behavior);
		this.stack = new Stack();
		this.frames = new Frame[context.behavior.getMethodInfo().getCodeAttribute().getCodeLength()];
	}
	
	/**
	 * Analyzes the behavior and returns the frames of its code.
	 * @return the frames of the bytecode.
	 * @throws BadBytecode thrown by javassist if the bytecode of this method is wrong.
	 */
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
	
	/**
	 * A bytecode frame. A frame instance holds a reference of the stack (before and after it).
	 * @author Stephane Godbillon
	 *
	 */
	public static class Frame {
		/**
		 * The state of the stack before the frame is run.
		 */
		public Stack stackBefore;
		/**
		 * The state of the stack after the frame is run.
		 */
		public Stack stackAfter;
		/**
		 * The index of this frame in the bytecode. A frame produced by the StackAnalyzer gets its index updated if any bytecode is inserted.
		 */
		public int index;
		/**
		 * The decoded op.
		 */
		public DecodedOp decodedOp;
		/**
		 * States if the frame is accessible.
		 * Generally, a frame which isAccessible field is false denotes a wrong bytecode.
		 */
		public boolean isAccessible = false;
		
		/**
		 * A String representation of this frame.
		 */
		@Override
		public String toString() {
			return "Frame " + index + " (" + decodedOp.op.getName() + "):" + stackBefore + " -> " + stackAfter + " " + (isAccessible ? "" : " NOT ACCESSIBLE");
		}
	}
	
	/**
	 * An collection of Frame, allowing to iterate over the frames and insert some bytecode if needed.
	 * @author Stephane Godbillon
	 *
	 */
	public static class Frames implements Iterable<Frame> {
		/**
		 * The backed frames.
		 */
		public final Frame[] frames;
		/**
		 * The behavior containing these frames.
		 */
		public final CtBehavior behavior;

		public Frames(CtBehavior behavior, Frame[] frames) {
			this.frames = frames;
			this.behavior = behavior;
		}

		@Override
		public FrameIterator iterator() {
			return new FrameIterator();
		}

		/**
		 * An iterator of Frames allowing to insert some bytecode before or after the iterated frames.
		 * The indexes of the backed frames always get updated after an insertion.
		 * @author Stephane Godbillon
		 *
		 */
		public class FrameIterator implements Iterator<Frame> {
			private int i = -1;
			private FrameCodeIterator iterator = new FrameCodeIterator(behavior
					.getMethodInfo().getCodeAttribute(), frames);

			/**
			 * Should never be used, this operation is not supported.
			 * @throws UnsupportedOperationException
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Returns the next frame.
			 * @return the next frame.
			 */
			@Override
			public Frame next() {
				int nextIndex = nextIndex();
				if (nextIndex > -1) {
					i = nextIndex;
					return frames[nextIndex];
				}
				throw new IllegalStateException();
			}

			/**
			 * States if there is a next frame.
			 * @return true if there is at least one remaining frame, false if not.
			 */
			@Override
			public boolean hasNext() {
				return nextIndex() > -1;
			}

			/**
			 * States if the iterator is at the start of the backed frames array.
			 * @return true if at the start of the backed frames array, false if not.
			 */
			public boolean isFirst() {
				return i == 0;
			}

			/**
			 * States if the iterator is at the end of the backed frames array.
			 * @return true if at the end of the backed frames array, false if not.
			 */
			public boolean isLast() {
				return !hasNext();
			}

			/**
			 * Gets the next frame, but does not update the iterator cursor. This operation is indempotent.
			 * @return the next frame.
			 */
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

			/**
			 * Inserts the given bytecode before or after the current Frame.
			 * @param code Bytecode.
			 * @param after true if after, false if before the current Frame.
			 * @throws BadBytecode if the given bytecode is wrong.
			 */
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