package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sun.org.apache.bcel.internal.util.ClassPath;

import bclibs.LocalVariablesEnhancer;
import bclibs.analysis.CodeParser;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes;
import bclibs.analysis.StackOpHandler;
import bclibs.analysis.decoders.DecodedMethodInvocationOp;
import bclibs.analysis.decoders.DecodedMethodInvocationOp.MethodParam;
import bclibs.analysis.opcodes.MethodInvocationOpcode;
import bclibs.analysis.opcodes.Op;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackAnalyzer;
import bclibs.analysis.stack.StackAnalyzer.Frame;
import bclibs.analysis.stack.StackAnalyzer.Frames;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TOP;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.ValueFromLocalVariable;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;

import static test.CommonTests.*;

public class Test {
	@org.junit.Test
	public void simpleSubjectsSimple() throws BadBytecode {
		System.out.println("simpleSubjectsSimple");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "simple");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 15:
							assertDeepEquals(names, new String[] {"subject", "myInt", "date"});
							break;
						case 21:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 30:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsSimpleWithParams() throws BadBytecode {
		System.out.println("simpleSubjectsSimpleWithParams");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "simpleWithParams");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 17:
							assertDeepEquals(names, new String[] {"subject", "myInt", "date"});
							break;
						case 24:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 33:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsSimpleWithConditionals() throws BadBytecode {
		System.out.println("simpleSubjectsSimpleWithConditionals");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "simpleWithConditionals");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 30:
							assertDeepEquals(names, new String[] {"subject", "myInt2", "date"});
							break;
						case 37:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 46:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsVarargs() throws BadBytecode {
		System.out.println("simpleSubjectsVarargs");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "varargs");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("varargs")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 14:
							assertDeepEquals(names, new String[] {});
							break;
						case 24:
							assertDeepEquals(names, new String[] {"myInt"});
							break;
						case 34:
							assertDeepEquals(names, new String[] {null});
							break;
						case 43:
							assertDeepEquals(names, new String[] {"subject", "date"});
							break;
						case 56:
							assertDeepEquals(names, new String[] {"subject", "date", "myInt"});
							break;
						case 73:
							assertDeepEquals(names, new String[] {"subject", "date", "myInt", null});
							break;
						case 86:
							assertDeepEquals(names, new String[] {"subject", "date", null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsExceptions() throws BadBytecode {
		System.out.println("simpleSubjectsExceptions");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "exceptions");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 15:
							assertDeepEquals(names, new String[] {"subject", "myInt", "date"});
							break;
						case 35:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 24:
						case 44:
						case 58:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsTableSwitchBlock() throws BadBytecode {
		System.out.println("simpleSubjectsTableSwitchBlock");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "tableswitchBlock");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 44:
							assertDeepEquals(names, new String[] {"subject", "myInt", "date"});
							break;
						case 53:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 65:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	
	@org.junit.Test
	public void simpleSubjectsLookupSwitchBlock() throws BadBytecode {
		System.out.println("simpleSubjectsLookupSwitchBlock");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "lookupswitchBlock");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		Frames frames = analyzer.analyze();
		for(Frame frame : frames) {
			if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
				DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
				if(dmio.getName().equals("classic")) {
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame);
					switch(frame.index) {
						case 52:
							assertDeepEquals(names, new String[] {"subject", "myInt", "date"});
							break;
						case 61:
							assertDeepEquals(names, new String[] {null, null, "date"});
							break;
						case 73:
							assertDeepEquals(names, new String[] {null, null, null});
							break;
						default:
							throw new RuntimeException("could not handle index " + frame.index);
					}
					System.out.println(dmio.getName() + " -> " + Arrays.toString(names));
				}
			}
		}
	}
	

	
	/*
5		int i;
6		i = 1;
7		int j = 0;
8		int k = j + i;
9		smth.length();
10		for(long l = 0; l < "toto".length(); l++) {
11			Subject s = new Subject();
12			s.say("truc" + l);
13		}
14		final Object o = new Object() {
15			@Override
16			public int hashCode() {
17				// TODO Auto-generated method stub
18				return super.hashCode();
19			}
20		};
21		o.toString();
	 */
	
	public static void main(String[] args) {
		
	}
}
