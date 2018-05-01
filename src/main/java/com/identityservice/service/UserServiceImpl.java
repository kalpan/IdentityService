package com.identityservice.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.identityservice.controller.UserController;
import com.identityservice.dto.User;

/*
 * User Identity Service Implementation
 * 
 * Methods are secured by Roles.
 * 
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private static final AtomicLong counter = new AtomicLong();
	private static List<User> usersCache;

	static {
		usersCache = populateDemoDatabase();
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findById(long id) {
		for (User user : usersCache) {
			if (user.getId() == id) {
				return user;
			}
		}
		return null;
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findByUserName(String userName) {
		for (User user : usersCache) {
			if (user.getUserName().equalsIgnoreCase(userName)) {
				return user;
			}
		}
		return null;
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Async
	@Override
	public CompletableFuture<User> findByUserNameAsync(String userName) {
		for (User user : usersCache) {
			if (user.getUserName().equalsIgnoreCase(userName)) {
				return CompletableFuture.completedFuture(user);
			}
		}
		return null;
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void saveUser(User user) {
		user.setId(counter.incrementAndGet());
		usersCache.add(user);
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void updateUser(User user) {
		int index = usersCache.indexOf(user);
		usersCache.set(index, user);
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteUserById(long id) {
		for (Iterator<User> iterator = usersCache.iterator(); iterator.hasNext();) {
			User user = iterator.next();
			if (user.getId() == id) {
				iterator.remove();
			}
		}
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public List<User> findAllUsers() {
		return usersCache;
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteAllUsers() {
		usersCache.clear();
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public boolean isUserExist(User user) {
		return findByUserName(user.getUserName()) != null;
	}

	/*
	 * New users are added to this demo database, in other words users cache.
	 */
	private static List<User> populateDemoDatabase() {
		List<User> users = new ArrayList<User>();
		users.add(new User(counter.incrementAndGet(), "admin", "admin", "admin", "admin"));
		users.add(new User(counter.incrementAndGet(), "guest", "guest", "guest", "guest"));
		return users;
	}

}
