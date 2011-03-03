package test.subjects;

public class Subject {
	public void say(String smth) {
		int i;
		i = 1;
		int j = 0;
		int k = j + i;
		System.out.println(k);
		/*if(j == 0)
			throw new RuntimeException("yop");*/
		smth.length();
		for(long l = 0; l < 6; l++) {
			String t = "truc" + l; 
			System.out.println(t);
		};i++;
		java.util.Date dd = new java.util.Date();
		dd.toString();
		i++;
		String name = "truc";
		String name2 = "truc2";
		String[] names = new String[] { "y1", "y2" };
		long toto = 3;
		Object object__ = new Object();
		machin("kbce", name2, names, toto *6, object__);
	}
	
	public void machin(String coucou, String bidule, String[] chponk, long toto, Object o) {
		
	}
	
	public void truc() {
		int i = 0;
		i = i + 1;
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
