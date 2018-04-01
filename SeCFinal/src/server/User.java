package server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class User {
	private String userid;
	private String password;
	private List<String> followers;
	private List<Photo> photos;

	/**
	 * constructs a new User object
	 * 
	 * @param userid
	 *            of this User
	 * @param password
	 *            of this User
	 */
	public User(String userid, String password) {
		this.userid = userid;
		this.password = password;
		this.followers = new CopyOnWriteArrayList<>();
		this.photos = new CopyOnWriteArrayList<>();
	}

	/**
	 * Checks if this user has followUser as a follower
	 * 
	 * @param followUser
	 *            - the userid of the followUser
	 */
	public boolean follows(User user) {
		return followers.contains(user.getUserid());
	}

	/**
	 * Checks if any of the followUsers is already a follower
	 * 
	 * @param followUserIds
	 *            - the userids of the followUsers
	 */
	public boolean follows(String[] followUserIds) {
		// check one by one
		for (String f : followers)
			for (String u : followUserIds)
				if (f.equals(u))
					return true;
		return false;
	}

	/**
	 * Checks if any of the followUsers is not a follower of this user
	 * 
	 * @param followUserIds
	 *            - the userids of the followUsers
	 * @return true - if any of the followUsers is not a follower of this user, or
	 *         false if all of the followUsers are followers of this user
	 */
	public boolean isNotFollower(String[] followUserIds) {
		// check one by one
		for (String f : followUserIds)
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
	 * @return this user's userid
	 */
	public String getUserid() {
		return userid;
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
	 * @param userid
	 *            - the userid of the user
	 * @param commentedUserid
	 *            - the userid of the commentedUser
	 * @param name
	 *            - the name of the commentedUser's photo
	 * @throws IOException
	 */
	// TODO enviar erro para o cliente
	public void addComment(String comment, String userid, String name) {
		if (hasPhoto(name))
			getPhoto(name).addComment(comment, userid);
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
	 * @param userid
	 *            - the userid of the user
	 * @param likedUserid
	 *            - the userid of the likedUser param name - the name of the
	 *            commentedUser's photo
	 */
	public void addLike(String userid, String name) {
		if (hasPhoto(name))
			getPhoto(name).addLike(userid);
		else
			System.out.println("this user doesnt have any photo with this " + "name");
	}

	/**
	 * Adds a dislike made by user in the dislikedUser's photo
	 * 
	 * @requires the comment has been added to this user's persistent storage
	 * @param userid
	 *            - the userid of the user
	 * @param dislikedUserid
	 *            - the userid of the dislikedUser
	 */
	// TODO enviar erro para o cliente
	public void addDislike(String userid, String dislikedUserid) {
		if (hasPhoto(dislikedUserid))
			getPhoto(dislikedUserid).addDisLike(userid);
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
		StringBuilder sb = new StringBuilder(userid);
		sb.append(" ");
		sb.append(password);
		return sb.toString();
	}
}
