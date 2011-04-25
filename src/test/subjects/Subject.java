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
package test.subjects;

public class Subject {
	public String subj = "subj";
	public void say(String smth) {
		int i;
		i = 15;
		int j = 0;
		int k = j + i;
		try {
		System.out.println(k);
		} catch(Exception e) {
			System.out.println("b");
		} finally {
			System.out.println("finally");
		}
		/*if(j == 0)
			throw new RuntimeException("yop");*/
		smth.length();
		for(long l = 0; l < 6; l++) {
			String t = "truc" + l; 
			System.out.println(t);
		};i++;
		java.util.Date dd = new java.util.Date();
		Subject subject = new Subject();
		subject.subj = "koko";
		try {
		dd.toString();
		i++;
		} catch(RuntimeException e) {
			System.out.println(e);
		}
		
		String name = "truc";
		String name2 = "truc2";
		String[] names = new String[] { "y1", "y2" };
		long toto = 398738947098720L;
		Object object__ = new Object();
		machin("kbce", name2, names, x6(toto), object__);
		process(toto, "a", "b");
		processStatic("c", "d");
		String ff = "ff";
		process(toto, ff);
	}
	
	static int hehe = 0;
	public static String hello() {
		try {
		try {
		String bidule = "bidule";
		System.out.println(bidule);
		return bidule;
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			"trc".length();
		}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void machin(String coucou, String bidule, String[] chponk, long toto, Object o) {
		
	}
	
	public void process(long t, String...strings) {
		// blob
	}
	
	public static void processStatic(String...strings) {
		
	}
	
	public void truc() {
		int i = 0;
		i = i + 1;
	}
	
	public long x6(long i) {
		return i * 6;
	}
	/*
		read :: 9 -> smth
		read :: 11 -> s
		write :: 6 -> i
		write :: 7 -> j
		write :: 8 -> k
		write :: 10 -> s
	*/
}
