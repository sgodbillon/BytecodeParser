package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sun.org.apache.bcel.internal.util.ClassPath;

import bclibs.LocalVariablesEnhancer;
import bclibs.analysis.CodeParser;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes;
import bclibs.analysis.StackOpHandler;
import bclibs.analysis.StackParser;
import bclibs.analysis.decoders.DecodedMethodInvocationOp;
import bclibs.analysis.opcodes.MethodInvocationOpcode;
import bclibs.analysis.opcodes.Op;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TOP;
import bclibs.analysis.stack.TrackableArray;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Test {
	@org.junit.Test
	public void coucou() {
		try {
			//File file = new File("/Users/sgo/code/bclibs/tmp/classes/test/subjects/Subject.class");
			//System.out.println(file.getAbsolutePath() +"::"+ file.exists());
			ClassPool cp =
			ClassPool.getDefault();
		CtClass ctClass = cp.get("test.subjects.Subject");//cp.makeClass(new FileInputStream(file));
		//ctClass.toBytecode(new DataOutputStream(System.out));
		
		CtMethod behavior = null;
		for(CtMethod ctMethod : ctClass.getMethods()) {
			if(ctMethod.getName().equals("say")) {
				behavior = ctMethod;
				break;
			}
		}
		
		final StackParser parser = new StackParser(behavior);
		parser.parse(new StackOpHandler() {
			@Override
			public void beforeComputeStack(Op op, int index) {
				LinkedList<StackElement> stack = parser.getCurrentStack();
				System.out.println(stack);
				if(op instanceof MethodInvocationOpcode) {
					MethodInvocationOpcode mop = (MethodInvocationOpcode) op;
					DecodedMethodInvocationOp decoded = mop.decode(parser.parser.context, index);
					String name = decoded.getName();
					//System.out.println("method " + name + " " + decoded.getDescriptor());
					//System.out.println("found " + name + " (" + decoded.getNbParameters() + " params), stack is: " + stack);
					String s = ")";
					int i = 0;
					int nbParams = 0;
					while(nbParams < decoded.getNbParameters()) {
						if(nbParams != 0)
							s = "," + s;
						StackElement se = stack.get(i++);
						if(se instanceof TOP)
							se = stack.get(i++);
						String toAppend = se.toString();
						if(se instanceof TrackableArray) {
							TrackableArray trackableArray = (TrackableArray) se;
							if(!trackableArray.isDirty && nbParams == 0) { // varargs
								StringBuffer asb = new StringBuffer();
								if(trackableArray.elements.length > 0) {
									asb.append(trackableArray.elements[0]);
									for(int j = 1; j < trackableArray.elements.length; j++)
										asb.append(",").append(trackableArray.elements[j]);
								}
								toAppend = asb.toString();
							}
						}
						s = toAppend + s;
						nbParams++;
					}
					s = name + "(" + s;
					int line = parser.parser.context.behavior.getMethodInfo().getLineNumber(index);
					System.out.println("method ::: " + s + " at line " + line);
				}
			}
		});
		//LocalVariablesEnhancer enhancer = new LocalVariablesEnhancer(behavior);
		//enhancer.proceed();
		//enhancer.yop();
		
		ctClass.defrost();
		Class<?> clazz = ctClass.toClass(new ClassLoader() {
		});
		clazz.getMethod("say", String.class).invoke(clazz.newInstance(), "");
		
		//LocalVariablesFinder localeVariablesFinder = new LocalVariablesFinder(behavior);
		//JavaBeanLoader<LocalVariablesFinder> jbl = new JavaBeanLoader<LocalVariablesFinder>(LocalVariablesFinder.class);
		//LocalVariablesFinder expected = jbl.load(new FileInputStream(new File("/Users/sgo/code/bclibs/src/test/coucou.yml")));
		//assertEquals(localeVariablesFinder.variables, expected.variables);
		//show("read", localeVariablesFinder.reads);
		//show("write", localeVariablesFinder.writes);
		assertEquals("t", "t");
		//System.out.println("yop");
		} catch (Exception e) {
			System.out.println("--->" + e);
			e.printStackTrace();
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
		new Test().coucou();
	}
}
