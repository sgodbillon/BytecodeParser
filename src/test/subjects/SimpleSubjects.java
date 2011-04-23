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
}
