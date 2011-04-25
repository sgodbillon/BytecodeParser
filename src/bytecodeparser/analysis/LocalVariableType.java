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
package bytecodeparser.analysis;

import java.util.HashMap;
import java.util.Map;

import javassist.CtClass;
import javassist.bytecode.Descriptor;
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
			throw new RuntimeException("not a class ?");
		} catch(Exception e) {
			// not a class
			String typeName = primitiveSymbols.get("" + signature.charAt(dimensions));
			if(typeName == null)
				throw new RuntimeException("unknown signature: " + signature, e);
			return new LocalVariableType(signature, addArrayTypeInfo(typeName, dimensions), typeName, true, dimensions);
		}
	}
	
	public static LocalVariableType from(CtClass clazz) {
		return parse(Descriptor.of(clazz));
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