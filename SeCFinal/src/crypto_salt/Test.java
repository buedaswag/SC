package crypto_salt;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class Test {

	public static void main(String[] args) throws IOException {


		String user = "user";
		String pwd = "i think i know who mallory is..";

		byte[] salt = Passwords.getNextSalt();
	

		byte[] hash = Passwords.hash(pwd, salt);
		
		byte[] sal1 = Arrays.copyOfRange(salt, 0, 8);
		byte[] sal2 = Arrays.copyOfRange(salt, 8, 16);
		
		byte[] hash1 = Arrays.copyOfRange(hash, 0, 8);
		byte[] hash2 = Arrays.copyOfRange(hash, 8, 16);
		
		long longSal1 = bytesToLong(sal1);
		long longSal2 = bytesToLong(sal2);
		
		long longHash1 = bytesToLong(hash1);
		long longHash2 = bytesToLong(hash2);
		
		//long concatLongSalt = Long.valueOf(String.valueOf(longSal1) + String.valueOf(longSal2));
		
		//long concatLongHash = Long.valueOf(String.valueOf(longHash1) + String.valueOf(longHash2));
		
		String linha = user + ":" + longSal1 + "" + longSal2  + ":" + longHash1 + "" + longHash2;

		File ficheiroPasswords = new File("passwords.txt");
		FileWriter fw = new FileWriter(ficheiroPasswords);
		fw.write(linha);
		fw.close();

		///////// AGORA LER A LINHA
		
		FileReader fr = new FileReader(ficheiroPasswords);
		String linhaLida = readFile("passwords.txt");
		
		System.out.println(linhaLida);
		
		String[] parts = linhaLida.split(":");
		
		String longSalTotal = parts[1];
		String longHashTotal = parts[2];
		
		int mid_longSalTotal = longSalTotal.length() / 2; //get the middle of the String
		String[] parts_longSalTotal = {longSalTotal.substring(0, mid_longSalTotal),
				longSalTotal.substring(mid_longSalTotal)};
		
		int mid_longHashTotal = longHashTotal.length() / 2; //get the middle of the String
		String[] parts_longHashTotal = {longHashTotal.substring(0, mid_longHashTotal),
				longHashTotal.substring(mid_longHashTotal)};
		
		long salt_lido1 = Long.parseLong(parts_longSalTotal[0]);
		long salt_lido2 = Long.parseLong(parts_longSalTotal[1]);

	//	byte[] byteLido1_salt = longToBytes(salt_lido1);
	//	byte[] byteLido2_salt = longToBytes(salt_lido2);
		
	 //  long hash_lido1 = Long.parseLong(parts_longHashTotal[0]);
	 //  long hash_lido2 = Long.parseLong(parts_longHashTotal[1]);
//		
//		byte[] byteLido1_hash = longToBytes(hash_lido1);
//		byte[] byteLido2_hash = longToBytes(hash_lido2);
//		
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//		outputStream.write( byteLido1_salt );
//		outputStream.write( byteLido2_salt );
//
//		byte salt_recuperado[] = outputStream.toByteArray( );
//		
//		
//		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream( );
//		outputStream.write( byteLido1_hash );
//		outputStream.write( byteLido2_hash );
//
//		byte hash_recuperado[] = outputStream2.toByteArray( );

		//boolean result = Passwords.isExpectedPassword(pwd, 
		//		salt_recuperado, hash_recuperado);

		//System.out.println(result);

	}
	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	
	public static String readFile(String file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
}
