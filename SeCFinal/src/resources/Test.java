package resources;

public class Test {

	public static void main(String[] args) {
		
		
		String user = "user";
		String pwd = "i think i know who mallory is..";
		
		Passwords obj, obj1 = new Passwords();
		
		byte[] sal = Passwords.getNextSalt();
		
		byte[] hash = Passwords.hash(pwd, sal);
		
		boolean result = Passwords.isExpectedPassword(pwd, sal, hash);
				
		System.out.println(result);
		
		System.out.println(sal);
		System.out.println(hash);
		
		System.out.println(user + ":" + sal + ":" + hash);
		
	}

}
