---
layout: default
title: BytecodeParser
---

# BytecodeParser {: .toto}

> BytecodeParser is a java library designed to help you to parse java bytecode
> by extracting as much information as possible.
> It is based upon [Javassist](http://www.csg.is.titech.ac.jp/~chiba/javassist/).
>
> It can also statically analyze each method's frame, for particular purposes like
> retrieving the names of the local variable names given as parameters of a method call
> (its original purpose), check if all the frames are reachable, etc.
>
> BytecodeParser simulates the stack operations made by the opcodes in the CodeAttribute.
> It can give you the state of the stack before and after the frame is run.

At runtime, a lot of information is lost by the compilation process: for example, you cannot get the local variable names anymore, the parameter names, the original line numbers in the source code, etc.

Actually, even if the java runtime library does not allow you to access that kind of information, the java class files may contain it if they are compiled with the ```-g``` flag (or debugging flag).

Some libraries like [Javassist](http://www.csg.is.titech.ac.jp/~chiba/javassist/) can parse the class files and extract some raw information. Sadly, these libs are not very easy to use, especially when you want to extract very specific information - the bytecode parsing/enhancement can be very difficult. BytecodeParser aims to help you to retrieve every single piece of information it is possible to extract from the compiled classes, if they contain debugging info.

BytecodeParser makes all the hard work of parsing for you. You don't have to know all the opcodes by heart anymore.

## Use cases
### Easy bytecode parsing

The simplest case is to parse the bytecode and get the opcode with its index and print them:

{% highlight java %}

    import javassist.ClassPool;
    import javassist.CtClass;
    import javassist.CtMethod;
    import javassist.NotFoundException;
    import javassist.bytecode.BadBytecode;
    import bytecodeparser.CodeParser;
    import bytecodeparser.OpHandler;
    import bytecodeparser.analysis.decoders.*;
    import bytecodeparser.analysis.opcodes.*;

    public class MyApp {
      public static void main(String args[]) throws NotFoundException, BadBytecode {
        ClassPool cp = new ClassPool();
        CtClass ctClass = cp.getCtClass("org.myapp.MyClass");
        for(CtMethod method: ctClass.getMethods()) {
          new CodeParser(method).parse(new OpHandler() {
            
            @Override
            public void handle(Op op, int index) {
              System.out.println(op.getName() + " at index " + index);
            }
          });
        }
      }
    }

{% endhighlight %}

All the main types of opcodes are in the ```bytecodeparser.analysis.opcodes``` package.

If you want to filter only particular opcodes, you can match them with their types. Let's say that you want to print only the method invocations opcodes:

{% highlight java %}
    @Override
    public void handle(Op op, int index) {
      if(op instanceof MethodInvocationOpcode) {
        System.out.println(op.getName() + " at index " + index);
      }
    }
{% endhighlight %}

or by opcode:

{% highlight java %}
    import javassist.bytecode.Opcode;
    //...

    @Override
    public void handle(Op op, int index) {
      if(Opcode.INVOKESTATIC == op.code) {
        System.out.println("INVOKESTATIC at index " + index);
      }
    }
{% endhighlight %}

Now, more interesting information can be extracted from the context by calling the method ```decode(Context context, int index)```:

{% highlight java %}
    @Override
    public void handle(Op op, int index) {
      if(op instanceof MethodInvocationOpcode) {
        System.out.println(op.getName() + " at index " + index);
        DecodedMethodInvocationOp decodedOp = op.as(MethodInvocationOpcode.class).decode(parser.context, index);
        System.out.println("method invocation: '" + 
          decodedOp.getName() +
          "' with parameters: " +
          java.util.Arrays.toString(decodedOp.getParameterTypes()));
      }
    }
{% endhighlight %}

The same logic applies to get the name of a local variable (assuming the class was compiled with debug information):

{% highlight java %}
    @Override
    public void handle(Op op, int index) {
      if(op instanceof LocalVariableOpcode) {
        System.out.println(op.getName() + " at index " + index + " is about the localVariable '" +
        op.as(LocalVariableOpcode.class).decode(parser.context, index).localVariable.name + "')");
      }
    }
{% endhighlight %}

### Even more information: the stack state

You can simulate the stack execution with the ```StackAnalyzer``` class:

{% highlight java %}
    import javassist.ClassPool;
    import javassist.CtClass;
    import javassist.CtMethod;
    import javassist.NotFoundException;
    import javassist.bytecode.BadBytecode;
    import bytecodeparser.CodeParser;
    import bytecodeparser.OpHandler;
    import bytecodeparser.analysis.decoders.*;
    import bytecodeparser.analysis.opcodes.*;
    import bytecodeparser.analysis.stack.StackAnalyzer;
    import bytecodeparser.analysis.stack.StackAnalyzer.Frame;

    public class MyApp {
      public static void main(String args[]) throws NotFoundException, BadBytecode {
        ClassPool cp = new ClassPool();
        CtClass ctClass = cp.getCtClass("org.myapp.MyClass");
        for(CtMethod method: ctClass.getMethods()) {
          StackAnalyzer stackAnalyzer = new StackAnalyzer(method);
          for(Frame frame: stackAnalyzer.analyze()) {
            System.out.println(frame.stackBefore + " => " + frame.stackAfter);
          }
        }
      }
    }
{% endhighlight %}

This is where BytecodeParser is powerful: every stack element is tracked during the simulation. So, given this piece of code:

{% highlight java %}
    int counter = 4;
    someMethod(counter);
    int copyOfCounter = counter;
{% endhighlight %}

When BytecodeParser simulates the stack, it knows that when you call ```someMethod(counter)```, the value ```4``` comes from the local variable ```counter```. This is the way BytecodeParser guesses the names of the parameters of a method call.

Also, BytecodeParser is capable to guess that ```copyOfCounter```'s value will be 4, and that its new value comes from ```counter```.

It even tracks when a stack element is pushed into an array. So when you call a method with varargs, BytecodeParser can tell you the names of the local variables that were given as arguments.

### A concrete case: get the parameter names given as arguments to a method

Thanks to the stack simulation, you can get a lot of information on many things, including the names of the paramaters that are given as arguments to a method.

{% highlight java %}
    import javassist.ClassPool;
    import javassist.CtClass;
    import javassist.CtMethod;
    import javassist.NotFoundException;
    import javassist.bytecode.BadBytecode;
    import bytecodeparser.CodeParser;
    import bytecodeparser.OpHandler;
    import bytecodeparser.analysis.decoders.*;
    import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp.MethodParam;
    import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp.MethodParams;
    import bytecodeparser.analysis.opcodes.*;
    import bytecodeparser.analysis.stack.StackAnalyzer;
    import bytecodeparser.analysis.stack.StackAnalyzer.Frame;

    public class MyApp {
      public static void main(String args[]) throws NotFoundException, BadBytecode {
        ClassPool cp = new ClassPool();
        CtClass ctClass = cp.getCtClass("org.myapp.MyClass");
        for(CtMethod method: ctClass.getMethods()) {
          StackAnalyzer stackAnalyzer = new StackAnalyzer(method);
          for(Frame frame: stackAnalyzer.analyze()) {
            System.out.println(frame.stackBefore + " => " + frame.stackAfter);
            if(frame.decodedOp instanceof DecodedMethodInvocationOp) {
              DecodedMethodInvocationOp dmio = (DecodedMethodInvocationOp) frame.decodedOp;
              MethodParams methodParams = DecodedMethodInvocationOp.resolveParameters(frame);
              MethodParam[] params = methodParams.merge();
              System.out.println("method '" + dmio.getName() + "' has been called with the following arguments: " + java.util.Arrays.toString(params));
            }
          }
        }
      }
    }
{% endhighlight %}

## Release

BytecodeParser is being actively developped. The latest version is 0.3 and can be downloaded [here](releases/0.3/bytecodeparser-0.3.jar).

### Dependencies

* [javassist](http://javassist.org) >= 3.0.9.GA
* [log4j](http://logging.apache.org/log4j/) >= 1.2.16
* [junit](http://junit.org) >= 4.8.2 (for building/testing)

## Documentation

You can browse the [javadoc](releases/0.3/api/index.html) to get a complete documentation about the API.

## License

BytecodeParser is released under the terms of the [LGPL v3](http://www.gnu.org/licenses/lgpl.html) (or any later version at your option).
