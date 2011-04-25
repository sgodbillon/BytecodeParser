package test;

import static test.CommonTests.assertDeepEquals;
import static test.CommonTests.getCtClass;
import static test.CommonTests.getMethod;

import java.util.Arrays;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp;
import bytecodeparser.analysis.stack.StackAnalyzer;
import bytecodeparser.analysis.stack.StackAnalyzer.Frame;
import bytecodeparser.analysis.stack.StackAnalyzer.Frames;

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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, true);
					switch(frame.index) {
						case 17:
							assertDeepEquals(names, new String[] {});
							break;
						case 35:
							assertDeepEquals(names, new String[] {"myInt", null, "myInt2"});
							break;
						case 45:
							assertDeepEquals(names, new String[] {null});
							break;
						case 54:
							assertDeepEquals(names, new String[] {"subject", "date"});
							break;
						case 67:
							assertDeepEquals(names, new String[] {"subject", "date", "myInt"});
							break;
						case 84:
							assertDeepEquals(names, new String[] {"subject", "date", "myInt", null});
							break;
						case 97:
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
					String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, false);
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
	
	@org.junit.Test
	public void simpleMultinewarray() throws BadBytecode {
		System.out.println("multinewarray");
		CtClass clazz = getCtClass("test.subjects.SimpleSubjects");
		CtMethod method = getMethod(clazz, "multinewarray");
		StackAnalyzer analyzer = new StackAnalyzer(method);
		analyzer.analyze();
	}
}
