/**
 * 
 */
package bclibs;

import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.ArrayType;
import javassist.bytecode.SignatureAttribute.ClassType;

public class LocalVariableType {
	public final boolean isPrimitive;
	public final String signature;
	public final String typeName;
	public final String shortTypeName;
	public final int dimensions;
	
	private LocalVariableType(String signature, String typeName, String shortTypeName, boolean isPrimitive, int dimensions) {
		this.signature = signature;
		this.typeName = typeName;
		this.shortTypeName = shortTypeName;
		this.isPrimitive = isPrimitive;
		this.dimensions = dimensions;
	}
	public boolean isArray() {
		return dimensions > 0;
	}
	
	public static LocalVariableType parse(String signature) {
		int dimensions = 0;
		for(int i = 0; i < signature.length(); i++) {
			if(signature.charAt(i) == '[')
				dimensions++;
			else break;
		}
		try {
			javassist.bytecode.SignatureAttribute.Type objectType = SignatureAttribute.toFieldSignature(signature);
			if(objectType instanceof ArrayType)
				objectType = ((ArrayType) objectType).getComponentType();
			if(objectType instanceof ClassType) {
				String typeName = ((ClassType) objectType).getName();
				return new LocalVariableType(signature, addArrayTypeInfo(typeName, dimensions), typeName, false, dimensions);
			}
			throw new RuntimeException("unknown signature: " + signature + ", objectType=" + objectType.getClass());
		} catch(BadBytecode e) {
			// not a class
			String typeName = primitiveSymbols.get("" + signature.charAt(dimensions));
			return new LocalVariableType(signature, addArrayTypeInfo(typeName, dimensions), typeName, true, dimensions);
		}
	}
	
	private static String addArrayTypeInfo(String typeName, int dimensions) {
		String result = typeName;
		for(int i = 0; i < dimensions; i++)
			result += "[]";
		return result;
	}
	
	private static Map<String, String> primitiveSymbols = new HashMap<String, String>();
	static {
		primitiveSymbols.put("V", "void");
		primitiveSymbols.put("Z", "boolean");
		primitiveSymbols.put("B", "byte");
		primitiveSymbols.put("C", "char");
		primitiveSymbols.put("S", "short");
		primitiveSymbols.put("I", "int");
		primitiveSymbols.put("J", "long");
		primitiveSymbols.put("F", "float");
		primitiveSymbols.put("D", "double");
	}
}