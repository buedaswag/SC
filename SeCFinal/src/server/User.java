package server;

import java.io.IOException;
import java.util.*;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class User {
	private String userId;
	private String password;
	private Collection<String> followers;
	private Collection<Photo> photos;

	/**
	 * Constructs a new User object from it's userId and it's password. 
	 * 
	 * @param userId 
	 * @param password
	 */
	public User(String userId, String password) {
		this.userId = userId;
		this.password = password;
		this.followers = new LinkedList<>();
		this.photos = new LinkedList<>();
	}

	/**
	 * Checks if this user has followUser as a follower
	 * 
	 * @param followUser
	 *            - the userId of the followUser
	 */
	public boolean follows(User user) {
		return followers.contains(user.getuserId());
	}

	/**
	 * Checks if any of the followUsers is already a follower
	 * 
	 * @param followuserIds
	 *            - the userIds of the followUsers
	 */
	public boolean follows(String[] followuserIds) {
		// check one by one
		for (String f : followers)
			for (String u : followuserIds)
				if (f.equals(u))
					return true;
		return false;
	}

	/**
	 * Checks if any of the followUsers is not a follower of this user
	 * 
	 * @param followuserIds
	 *            - the userIds of the followUsers
	 * @return true - if any of the followUsers is not a follower of this user, or
	 *         false if all of the followUsers are followers of this user
	 */
	public boolean isNotFollower(String[] followuserIds) {
		// check one by one
		for (String f : followuserIds)
			if (!followers.contains(f))
				return true;
		return false;
	}

	/**
	 * adds followers to this User´s list of followers
	 * 
	 * @requires the followers have been added to this user's persistent storage
	 * @param followers
	 *            - the followers to be added
	 */
	public void addFollowers(List<String> followers) {
		this.followers.addAll(followers);
	}

	/**
	 * removes followers from this User's list of followers
	 * 
	 * @param followers
	 *            - the followers to be removed
	 */
	public void removeFollowers(List<String> followers) {
		this.followers.removeAll(followers);
	}

	/**
	 * adds a single follower to this User´s followers list
	 * 
	 * @requires the follower has been added to this user's persistent storage
	 * @param follower
	 *            the follower to be added
	 */
	public void addFollower(String follower) {
		this.followers.add(follower);
	}

	/**
	 * removes a single followers to this User´s followers list
	 * 
	 * @param follower
	 *            the follower to be removed
	 */
	public void removeFollower(String follower) {
		this.followers.remove(follower);
	}

	/**
	 * checks if the user has a photo with the given name
	 * 
	 * @param name
	 *            - the name to check
	 * @return true if the User does has a photo with the given name
	 */
	public boolean hasPhoto(String name) {
		for (Photo p : photos)
			if (p.getName().equals(name))
				return true;
		return false;
	}

	/**
	 * checks if the user has any photo with any of the given names
	 * 
	 * @param names
	 *            of the photos given
	 * @return true if the User does have a photo with the given names
	 */
	public boolean hasPhotos(String[] names) {
		for (String name : names)
			if (hasPhoto(name))
				return true;
		return false;
	}

	/**
	 * 
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		photos.add(photo);
	}

	/**
	 * Adds the photos with the given names to this user
	 * 
	 * @requires the photos have been added to this user's persistent storage
	 * @param names
	 */
	public void addPhotos(String[] names) {
		for (String name : names)
			addPhoto(new Photo(name, (new Date()).getTime()));
	}

	/**
	 * 
	 * @return this user's userId
	 */
	public String getuserId() {
		return userId;
	}

	/**
	 * 
	 * @return this user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Adds a comment made by user in the commentedUser's photo
	 * 
	 * @requires the comment has been added to this user's persistent storage
	 * @param comment
	 *            - the comment to be made
	 * @param userId
	 *            - the userId of the user
	 * @param commenteduserId
	 *            - the userId of the commentedUser
	 * @param name
	 *            - the name of the commentedUser's photo
	 * @throws IOException
	 */
	// TODO enviar erro para o cliente
	public void addComment(String comment, String userId, String name) {
		if (hasPhoto(name))
			getPhoto(name).addComment(comment, userId);
		else
			System.out.println("this user doesnt have any photo with this " + "name");
	}

	/**
	 * returns the photo with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Photo getPhoto(String name) {
		for (Photo p : photos)
			if (p.getName().equals(name))
				return p;
		return null;
	}

	/**
	 * Adds a like made by user in the likedUser's photo
	 * 
	 * @requires the comment has been added to this user's persistent storage
	 * @param userId
	 *            - the userId of the user
	 * @param likeduserId
	 *            - the userId of the likedUser param name - the name of the
	 *            commentedUser's photo
	 */
	public void addLike(String userId, String name) {
		if (hasPhoto(name))
			getPhoto(name).addLike(userId);
		else
			System.out.println("this user doesnt have any photo with this " + "name");
	}

	/**
	 * Adds a dislike made by user in the dislikedUser's photo
	 * 
	 * @requires the comment has been added to this user's persistent storage
	 * @param userId
	 *            - the userId of the user
	 * @param dislikeduserId
	 *            - the userId of the dislikedUser
	 */
	// TODO enviar erro para o cliente
	public void addDislike(String userId, String dislikeduserId) {
		if (hasPhoto(dislikeduserId))
			getPhoto(dislikeduserId).addDisLike(userId);
		else
			System.out.println("this user doesnt have any photo with this " + "name");
	}

	/**
	 * gets the photos from this user
	 * 
	 * @return photos - the photos
	 */
	public List<Photo> getPhotos() {
		return photos;
	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(userId);
		sb.append(" ");
		sb.append(password);
		return sb.toString();
	}
}
