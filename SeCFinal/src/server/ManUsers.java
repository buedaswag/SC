package server;

import java.io.IOException;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class ManUsers {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Adds a line to the usersTxt file, in the format userId:salt:salted_password_hash
	 * @param userId
	 * @param password
	 */
	public static void addUser(String userId, String password) {
		
	}
	
	/**
	 * Removes a line from the usersTxt file, in the format userId:salt:salted_password_hash,
	 * in which userId is the given userId.
	 * @param userId
	 */
	public static void removeUser(String userId) {
		
	}

	/**
	 * Update the usersTxt file, in the format userId:salt:salted_password_hash,
	 * in which salted_password_hash is the salted hash of the newPassword.
	 * @param userId
	 * @param oldPassword
	 * @param newPassword
	 */
	public static void changePassword(String userId, String oldPassword, String newPassword) {
		
	}
}
