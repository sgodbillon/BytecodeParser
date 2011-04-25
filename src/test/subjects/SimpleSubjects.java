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

import static test.subjects.Common.*;

public class SimpleSubjects {
	public void simple() {
		int myInt = 89;
		long date = System.currentTimeMillis();
		String subject = "test";
		classic(subject, myInt, date);
		classic(null, 0, date);
		classic("", 1, 2L);
	}
	
	public void simpleWithParams(String s1, int i1) {
		int myInt = 89;
		long date = System.currentTimeMillis();
		String subject = "test";
		classic(subject, myInt, date);
		classic(null, 0, date);
		classic("", 1, 2L);
	}
	
	public void simpleWithConditionals(String s1, int i1) {
		int myInt = 89;
		long date = System.currentTimeMillis();
		String subject = "test";
		if(i1 > 0) {
			myInt = 90;
			int myInt2 = myInt + 1;
			classic(subject, myInt2, date);
		}
		classic(null, 0, date);
		classic("", 1, 2L);
	}
	
	public void varargs() {
		int myInt = 89;
		int myInt2 = 99;
		long date = System.currentTimeMillis();
		String subject = "test";
		Common.varargs();
		Common.varargs(myInt, 3, myInt2);
		Common.varargs(1);
		Common.mixed(subject, date);
		Common.mixed(subject, date, myInt);
		Common.mixed(subject, date, myInt, 1);
		Common.mixed(subject, date, 1);
	}
	
	public void exceptions() {
		int myInt = 89;
		long date = System.currentTimeMillis();
		try {
			String subject = "test";
			classic(subject, myInt, date);
		} catch(Exception e) {
			classic(null, 0, date);
		} finally {
			classic("", 1, 2L);
		}
	}
	
	public void tableswitchBlock(int switcher) {
		int myInt = 89;
		long date = System.currentTimeMillis();
		String subject = "test";
		switch(switcher) {
			case 1:
				classic(subject, myInt, date);
				break;
			case 2:
				classic(null, 0, date);
				break;
			case 3:
				classic("", 1, 2L);
				break;
		}
	}
	
	public void lookupswitchBlock(int switcher) {
		int myInt = 89;
		long date = System.currentTimeMillis();
		String subject = "test";
		switch(switcher) {
			case 1:
				classic(subject, myInt, date);
				break;
			case 2:
				classic(null, 0, date);
				break;
			case 7:
				classic("", 1, 2L);
				break;
		}
	}
	
	public void multinewarray() {
		int[][] integers = new int[2][5];
		String[][][] strings = new String[5][6][7];
		integers[1][4] = 555;
		long[] longs = new long[66];
		String[] simpleStrings = new String[2];
		simpleStrings[1] = strings[1][2][3] = "toto";
		longs[1] = 4669292874L;
	}
}
